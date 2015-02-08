/*
 *  Copyright 2014 Ahsan Rabbani
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xargsgrep.portknocker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.xargsgrep.portknocker.R;
import com.xargsgrep.portknocker.activity.EditHostActivity;
import com.xargsgrep.portknocker.adapter.ApplicationArrayAdapter;
import com.xargsgrep.portknocker.asynctask.RetrieveApplicationsAsyncTask;
import com.xargsgrep.portknocker.db.DatabaseManager;
import com.xargsgrep.portknocker.model.Application;
import com.xargsgrep.portknocker.model.Host;
import com.xargsgrep.portknocker.utils.StringUtils;

import java.util.List;

public class MiscFragment extends SherlockFragment
{
    public static final String TAG = "MiscFragment";

    private DatabaseManager databaseManager;
    private String selectedLaunchIntent;
    private int delay;
    private int tcpConnectTimeout;
    private ApplicationArrayAdapter applicationAdapter;
    private boolean savedInstanceState = false;

    public static MiscFragment newInstance(Long hostId)
    {
        MiscFragment fragment = new MiscFragment();
        if (hostId != null)
        {
            Bundle args = new Bundle();
            args.putLong(EditHostActivity.KEY_HOST_ID, hostId);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        databaseManager = new DatabaseManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.misc_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Spinner launchIntentSpinner = getLaunchIntentSpinner();
        SeekBar delaySeekBar = (SeekBar) getView().findViewById(R.id.delay_seekbar);
        SeekBar tcpConnectTimeoutSeekBar = (SeekBar) getView().findViewById(R.id.tcp_timeout_seekbar);
        final TextView delayDisplay = (TextView) getView().findViewById(R.id.delay_display);
        final TextView tcpConnectTimeoutDisplay = (TextView) getView().findViewById(R.id.tcp_timeout_display);

        launchIntentSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedLaunchIntent = ((ApplicationArrayAdapter) parent.getAdapter()).getItem(position).getIntent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        tcpConnectTimeoutSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                tcpConnectTimeoutDisplay.setText(String.valueOf(progress));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                tcpConnectTimeout = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
        });

        delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                delayDisplay.setText(String.valueOf(progress));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                delay = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
        });

        Bundle args = getArguments();

        if (this.savedInstanceState)
        {
            delaySeekBar.setProgress(delay);
            tcpConnectTimeoutSeekBar.setProgress(tcpConnectTimeout);
            launchIntentSpinner.setAdapter(applicationAdapter);
            setSelectedLaunchIntent();
        }
        else if (args != null)
        {
            Long hostId = args.getLong(EditHostActivity.KEY_HOST_ID);
            Host host = databaseManager.getHost(hostId);

            delaySeekBar.setProgress(host.getDelay());
            tcpConnectTimeoutSeekBar.setProgress(host.getTcpConnectTimeout());
            selectedLaunchIntent = host.getLaunchIntentPackage();
        }
        else
        {
            // editing a new host
            delaySeekBar.setProgress(Host.DEFAULT_DELAY);
            tcpConnectTimeoutSeekBar.setProgress(Host.DEFAULT_TCP_CONNECT_TIMEOUT);
        }

        if (applicationAdapter == null)
        {
            RetrieveApplicationsAsyncTask retrieveAppsTask = new RetrieveApplicationsAsyncTask(getActivity(), this);
            retrieveAppsTask.execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        delay = getDelaySeekBar().getProgress();
        tcpConnectTimeout = getTcpConnectTimeoutSeekBar().getProgress();
        savedInstanceState = true;
    }

    public void initializeApplicationAdapter(List<Application> applications)
    {
        applicationAdapter = new ApplicationArrayAdapter(getActivity(), applications);
        getLaunchIntentSpinner().setAdapter(applicationAdapter);
        setSelectedLaunchIntent();
    }

    private void setSelectedLaunchIntent()
    {
        if (applicationAdapter != null && StringUtils.isNotBlank(selectedLaunchIntent))
        {
            for (int i = 0; i < applicationAdapter.getCount(); i++)
            {
                Application application = applicationAdapter.getItem(i);
                if (application.getIntent().equals(selectedLaunchIntent))
                {
                    getLaunchIntentSpinner().setSelection(i);
                    break;
                }
            }
        }
    }

    public String getSelectedLaunchIntent()
    {
        return selectedLaunchIntent;
    }

    public SeekBar getDelaySeekBar()
    {
        return (SeekBar) getView().findViewById(R.id.delay_seekbar);
    }

    public SeekBar getTcpConnectTimeoutSeekBar()
    {
        return (SeekBar) getView().findViewById(R.id.tcp_timeout_seekbar);
    }

    private Spinner getLaunchIntentSpinner()
    {
        return (Spinner) getView().findViewById(R.id.launch_intent);
    }
}
