 /*
  * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
  * Copyright (C) 2013 Adrian Gygax
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see {http://www.gnu.org/licenses/}.
  */
 
 package com.github.notizklotz.derbunddownloader.settings;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceFragment;
 import com.github.notizklotz.derbunddownloader.R;
 import com.github.notizklotz.derbunddownloader.download.AutomaticIssueDownloadAlarmReceiver;
 
 import java.util.Calendar;
 
 public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.preferences);
 
         updateSummaries(getPreferenceScreen().getSharedPreferences());
     }
 
     @Override
     public void onResume() {
         super.onResume();
         SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
         if (sharedPreferences == null) {
             throw new IllegalStateException("could not get shared preferences");
         }
         sharedPreferences.registerOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
         if (sharedPreferences == null) {
             throw new IllegalStateException("could not get shared preferences");
         }
         sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         updateSummaries(sharedPreferences);
 
         Activity activity = getActivity();
         if (activity == null) {
             throw new IllegalStateException("This fragment is not associated with an Activity");
         }
 
         AlarmManager alarms = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
         Context applicationContext = activity.getApplicationContext();
         if (applicationContext == null) {
             throw new IllegalStateException("ApplicationContext was null");
         }
 
         PendingIntent pendingIntent = PendingIntent.getBroadcast(
                 applicationContext, 0,
                 new Intent(applicationContext, AutomaticIssueDownloadAlarmReceiver.class),
                 PendingIntent.FLAG_CANCEL_CURRENT);
 
         if (sharedPreferences.getBoolean(Settings.KEY_AUTO_DOWNLOAD_ENABLED, false)) {
             String auto_download_time = sharedPreferences.getString(Settings.KEY_AUTO_DOWNLOAD_TIME, null);
             if (auto_download_time != null) {
                 Calendar now = Calendar.getInstance();
 
                 Integer[] time = TimePickerPreference.toHourMinuteIntegers(auto_download_time);
                 Calendar updateTime = Calendar.getInstance();
                 updateTime.clear();
                 //noinspection MagicConstant
                 updateTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), time[0], time[1]);
 
                 if (updateTime.before(now)) {
                     updateTime.roll(Calendar.DAY_OF_MONTH, true);
                 }
 
                 alarms.setRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
             }
 
         } else {
             alarms.cancel(pendingIntent);
         }
 
     }
 
     private void updateSummaries(SharedPreferences sharedPreferences) {
         String auto_download_time = sharedPreferences.getString(Settings.KEY_AUTO_DOWNLOAD_TIME, null);
         Preference auto_download_time_preference = getPreferenceScreen().findPreference(Settings.KEY_AUTO_DOWNLOAD_TIME);
         if (auto_download_time_preference != null) {
             auto_download_time_preference.setSummary(auto_download_time);
         }
 
         Preference usernamePreference = getPreferenceScreen().findPreference(Settings.KEY_USERNAME);
         assert usernamePreference != null;
         usernamePreference.setSummary(sharedPreferences.getString(Settings.KEY_USERNAME, this.getString(R.string.username_summary)));
 
         Preference passwordPreference = getPreferenceScreen().findPreference(Settings.KEY_PASSWORD);
         assert passwordPreference != null;
 
         if (sharedPreferences.contains(Settings.KEY_PASSWORD)) {
             passwordPreference.setSummary("****");
         }
         else
             passwordPreference.setSummary(this.getString(R.string.password_summary));
     }
 }
