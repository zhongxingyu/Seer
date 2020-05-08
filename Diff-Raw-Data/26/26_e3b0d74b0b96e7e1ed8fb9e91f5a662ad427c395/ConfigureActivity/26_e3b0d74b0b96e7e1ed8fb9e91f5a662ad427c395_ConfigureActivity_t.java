 package com.nelson.umbrellaalarm;
 
 import android.app.AlarmManager;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.TimePicker;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.concurrent.TimeUnit;
 
 public class ConfigureActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, TimePickerDialog.OnTimeSetListener {
 
     private static final int TIME_PICKER_DIALOG = 0;
 
     private static String notificationsKey;
     private static String chooseDialogKey;
 
     private SharedPreferences sharedPreferences;
     private ListPreference mTimeListPreference;
 
     @Override
     @SuppressWarnings("deprecation")
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ConfigureActivity.this);
         notificationsKey = getString(R.string.notifications_key);
         chooseDialogKey = getString(R.string.choose_dialog_key);
 
         addPreferencesFromResource(R.xml.preferences);
 
         mTimeListPreference = (ListPreference) super.findPreference(chooseDialogKey);
 
         boolean enabled =  sharedPreferences.getBoolean(notificationsKey, true);
         if (enabled) {
             startCheckAlarmService();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         // set summary onResume since we can't change the alarm in app
         String alarmDialogValue = sharedPreferences.getString(chooseDialogKey, "poop");
 
         if(alarmDialogValue.equals(getString(R.string.at_alarm_key))) {
             setAlarmSummaryText();
         } else if (alarmDialogValue.equals(getString(R.string.at_time_key))) {
             int hour = sharedPreferences.getInt(getString(R.string.time_hour_key), 9);
             int minute = sharedPreferences.getInt(getString(R.string.time_min_key), 0);
             setAtTimeSummary(hour, minute);
         }
         sharedPreferences.registerOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
     }
 
     @Override
    @SuppressWarnings("deprecation")
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         String notifyType = sharedPreferences.getString(chooseDialogKey, "poop");
         if (key.equals(notificationsKey)) {
             boolean enabled = sharedPreferences.getBoolean(notificationsKey, true);
             if (enabled && notifyType.equals(getString(R.string.at_alarm_key))) {
                 startCheckAlarmService();
             }
         } else if (key.equals(chooseDialogKey)) {
             if (notifyType.equals(getString(R.string.at_alarm_key))) {
                 setAlarmSummaryText();
             } else if (notifyType.equals(getString(R.string.at_time_key))) {
                 showDialog(TIME_PICKER_DIALOG);
             } else if (notifyType.equals("poop")) {
                 Log.wtf(getClass().toString(), "This should never happen");
             }
         }
     }
 
     @Override
     @SuppressWarnings("deprecation")
     protected Dialog onCreateDialog(int id, Bundle args) {
         switch (id) {
             case TIME_PICKER_DIALOG:
                 int hour = sharedPreferences.getInt(getString(R.string.time_hour_key), 9);
                 int min = sharedPreferences.getInt(getString(R.string.time_min_key), 0);
                 new TimePickerDialog(ConfigureActivity.this, this, hour, min, false).show();
                 break;
         }
         return super.onCreateDialog(id, args);
     }
 
     @Override
     public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putInt(getString(R.string.time_hour_key), hourOfDay);
         editor.putInt(getString(R.string.time_min_key), minute);
         editor.commit();
 
         AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
         Intent intent = new Intent(getBaseContext(), UmbrellaAlarmService.class);
         PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 0, intent, 0);
         long triggerAtMillis = getMillisToTime(hourOfDay, minute);
         alarmManager.setRepeating(AlarmManager.RTC, triggerAtMillis, TimeUnit.DAYS.toMillis(1), pendingIntent);
 
         setAtTimeSummary(hourOfDay, minute);
     }
 
     private void setAlarmSummaryText() {
         String nextAlarm = Settings.System.getString(getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
         if (nextAlarm != null && !nextAlarm.equals("")) {
             mTimeListPreference.setSummary(String.format(getString(R.string.at_alarm_summary), nextAlarm));
         } else {
             mTimeListPreference.setSummary(getString(R.string.at_alarm_no_alarm_summary));
         }
     }
 
     private void setAtTimeSummary(int hourOfDay, int minute) {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minute);
         SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
         mTimeListPreference.setSummary(String.format(getString(R.string.at_time_summary), sdf.format(calendar.getTime())));
     }
 
     private long getMillisToTime(int hour, int min) {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.HOUR_OF_DAY, hour);
         calendar.set(Calendar.MINUTE, min);
         calendar.set(Calendar.SECOND, 0);
 
         if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
             calendar.add(Calendar.DATE, 1);
         }
         return calendar.getTimeInMillis();
     }
 
     private void startCheckAlarmService() {
        Intent startServiceIntent = new Intent(getApplicationContext(), CheckAlarmService.class);
         startService(startServiceIntent);
     }
 }
