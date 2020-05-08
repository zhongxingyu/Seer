 /*
  * Copyright (C) 2011 Savaged-Zen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.savagedzen.szparts.activities;
 
 import com.savagedzen.szparts.R;
 import com.savagedzen.szparts.utils.ShellCommand;
 import com.savagedzen.szparts.utils.ShellCommand.CommandResult;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.widget.Toast;
 
 import java.io.File;
 
 //
 // Kernel Related Settings
 //
 public class KernelActivity extends PreferenceActivity {
 
     public static final String HAVS_SCREEN = "HAVSActivity";
     public static final String SBC_PREF = "pref_sbc";
     private static final String TAG = "KernelSettings";
 
     private boolean vddExists = new File("/system/etc/vdd_profiles").exists();
    private boolean sbcExists = new File("/sys/kernel/batt_options/sbc/sysctl_batt_sbc").exists();
 
     private PreferenceScreen mHAVSScreen;
     private CheckBoxPreference mSBCPref;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.kernel_title);
         addPreferencesFromResource(R.xml.kernel_settings);
 
         PreferenceScreen PrefScreen = getPreferenceScreen();
 
         mHAVSScreen = (PreferenceScreen) PrefScreen.findPreference(HAVS_SCREEN);
         mSBCPref= (CheckBoxPreference) PrefScreen.findPreference(SBC_PREF);
 
 	IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 	registerReceiver(battery_receiver, filter);
         // NEED TO UNREGISTER FOR LEAK
 
         if (vddExists) {
             mHAVSScreen.setEnabled(true);
             mHAVSScreen.setSummary(R.string.havs_summary);
         } else if (!vddExists) {
             mHAVSScreen.setEnabled(false);
             mHAVSScreen.setSummary(R.string.unsupported_feature);
         }
 
         if (sbcExists) {
             mSBCPref.setEnabled(true);
             mSBCPref.setSummary(R.string.kernel_sbc_summary);
            mSBCPref.isChecked();
         } else if (!sbcExists) {  
             mSBCPref.setEnabled(false);
             mSBCPref.setSummary(R.string.unsupported_feature);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 
         if (preference == mSBCPref) {
             if (mSBCPref.isChecked()) {
                 changeSBC(true);
             } else {
                 changeSBC(false);
             }
             return true;
         }
         return false;
     }
 
     public boolean changeSBC(boolean onOFF) {
 
 	IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 	registerReceiver(battery_receiver, filter);
 
         ShellCommand cmd = new ShellCommand();
         if (onOFF) {
             SystemProperties.set("sys.kernel.sbc", "true");
             CommandResult r = cmd.sh.runWaitFor("echo 1 > /sys/kernel/batt_options/sbc/sysctl_batt_sbc");
             if (!r.success()) {
                 Log.d(TAG, "Starting SBC returned error: " + r.stderr);
                 Toast.makeText(this, "Error starting SBC", Toast.LENGTH_SHORT).show();
             } else {
                Log.d(TAG, "SBC successfully started! Result: " + r.stdout);
                Toast.makeText(this, "SBC successfully started!", Toast.LENGTH_SHORT).show();
             }
             return true;
         } else if (!onOFF) {
             SystemProperties.set("sys.kernel.sbc", "false");
             CommandResult r = cmd.sh.runWaitFor("echo 0 > /sys/kernel/batt_options/sbc/sysctl_batt_sbc");
             if (!r.success()) {
                 Log.d(TAG, "Starting SBC returned error: " + r.stderr);
                 Toast.makeText(this, "Error starting SBC", Toast.LENGTH_SHORT).show();
             } else {
                Log.d(TAG, "SBC successfully stopped! Result: " + r.stdout);
                Toast.makeText(this, "SBC successfully stopped!", Toast.LENGTH_SHORT).show();
             }
             return true;
         }
         return false;
     }
 
     private BroadcastReceiver battery_receiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
         int mPluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);;
 
         if (sbcExists) {
             if (mPluggedIn == 0) {
                 mSBCPref.setEnabled(true);
                 mSBCPref.setSummary(R.string.kernel_sbc_summary);
             } else {
                 mSBCPref.setEnabled(false);
                 mSBCPref.setSummary(R.string.kernel_charging);
             }
                  Log.d(TAG, "RECEIVED: " + intent.toString());
         } else if (!sbcExists) {
             mSBCPref.setEnabled(false);
             mSBCPref.setSummary(R.string.unsupported_feature);
         }
         }
     };
 }
