 /*
  * Copyright (C) 2007 The Android Open Source Project
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
 
 package com.hlidskialf.android.alarmclock;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceScreen;
 import com.hlidskialf.android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 /**
  * Manages each alarm
  */
 public class SetAlarm extends PreferenceActivity
         implements Alarms.AlarmSettings, TimePickerDialog.OnTimeSetListener {
 
     private static final int OPTION_MENU_DELETE=1;
     private static final int OPTION_MENU_FIRE=2;
     private static final int OPTION_MENU_DEBUG=3;
 
     private CheckBoxPreference mAlarmOnPref;
     private Preference mTimePref;
     private AlarmPreference mAlarmPref;
     private CheckBoxPreference mVibratePref;
     private RepeatPreference mRepeatPref;
     private SliderPreference mVolumePref;
     private SliderPreference mCrescendoPref;
     private SliderPreference mSnoozePref;
     private SliderPreference mDurationPref;
     private SliderPreference mDelayPref;
     private EditTextPreference mNamePref;
     private ContentObserver mAlarmsChangeObserver;
     private MenuItem mDeleteAlarmItem;
     private MenuItem mTestAlarmItem;
 
     private int mId;
     private int mHour;
     private int mMinutes;
     private Alarms.DaysOfWeek mDaysOfWeek = new Alarms.DaysOfWeek();
 
     private int mSnooze;
     private int mDuration;
     private int mDelay;
     private boolean mVibrateOnly;
     private int mVolume;
     private int mCrescendo;
     private String mName;
 
     private boolean mReportAlarmCalled;
 
     private static final int DIALOG_TIMEPICKER = 0;
 
     private class RingtoneChangedListener implements AlarmPreference.IRingtoneChangedListener {
         public void onRingtoneChanged(Uri ringtoneUri) {
             saveAlarm(false);
         }
     }
 
     private class OnRepeatChangedObserver implements RepeatPreference.OnRepeatChangedObserver {
         public void onRepeatChanged(Alarms.DaysOfWeek daysOfWeek) {
             if (!mDaysOfWeek.equals(daysOfWeek)) {
                 mDaysOfWeek.set(daysOfWeek);
                 saveAlarm(true);
             }
         }
         public Alarms.DaysOfWeek getDaysOfWeek() {
             return mDaysOfWeek;
         }
     }
 
     private class AlarmsChangeObserver extends ContentObserver {
         public AlarmsChangeObserver() {
             super(new Handler());
         }
         @Override
         public void onChange(boolean selfChange) {
             Alarms.getAlarm(getContentResolver(), SetAlarm.this, mId);
         }
     }
 
 
     /**
      * Set an alarm.  Requires an Alarms.ID to be passed in as an
      * extra
      */
     @Override
     protected void onCreate(Bundle icicle) {
         super.onCreate(icicle);
 
 
         addPreferencesFromResource(R.xml.alarm_prefs);
         mAlarmOnPref = (CheckBoxPreference)findPreference("on");
         mTimePref = findPreference("time");
         mAlarmPref = (AlarmPreference) findPreference("alarm");
         mVibratePref = (CheckBoxPreference) findPreference("vibrate");
         mRepeatPref = (RepeatPreference) findPreference("setRepeat");
 
         mVolumePref = (SliderPreference) findPreference("volume");
         mCrescendoPref = (SliderPreference) findPreference("crescendo");
         mSnoozePref = (SliderPreference) findPreference("snooze");
         mDurationPref = (SliderPreference) findPreference("duration");
         mDelayPref = (SliderPreference) findPreference("delay");
         mNamePref = (EditTextPreference) findPreference("name");
 
         Intent i = getIntent();
         mId = i.getIntExtra(Alarms.ID, -1);
         if (Log.LOGV) Log.v("In SetAlarm, alarm id = " + mId);
 
         mReportAlarmCalled = false;
         /* load alarm details from database */
         Alarms.getAlarm(getContentResolver(), this, mId);
         /* This should never happen, but does occasionally with the monkey.
          * I believe it's a race condition where a deleted alarm is opened
          * before the alarm list is refreshed. */
         if (!mReportAlarmCalled) {
             Log.e("reportAlarm never called!");
             finish();
         }
 
         mAlarmsChangeObserver = new AlarmsChangeObserver();
         getContentResolver().registerContentObserver(
                 Alarms.AlarmColumns.CONTENT_URI, true, mAlarmsChangeObserver);
 
         mAlarmPref.setRingtoneChangedListener(new RingtoneChangedListener());
         mRepeatPref.setOnRepeatChangedObserver(new OnRepeatChangedObserver());
 
         mNamePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
           public boolean onPreferenceChange(Preference pref, Object newValue) {
             mName = newValue.toString();
             updateName();
             saveAlarm(false);
             return true;
           }
         });
 
         mVolumePref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
             public int getValue() { return mVolume; }
             public void onSliderChanged(int value) { mVolume = value; updateVolume(); saveAlarm(false); }
             public int progressToValue(int progress) { return progress; }
             public int valueToProgress(int value) { return value; }
         });
 
         mCrescendoPref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
             public int getValue() { return mCrescendo; }
             public void onSliderChanged(int value) { mCrescendo = value; updateCrescendo(); saveAlarm(false); }
             public int progressToValue(int progress) { return (int)(60.0*(progress / 100.0)); }
             public int valueToProgress(int value) { return (int)(100.0*(value/60.0)); }
         });
         
         mSnoozePref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
             public int getValue() { return mSnooze; }
             public void onSliderChanged(int value) { mSnooze = value; updateSnooze(); saveAlarm(false); }
             public int progressToValue(int progress) { return (int)(60.0*(progress / 100.0)); }
             public int valueToProgress(int value) { return (int)(100.0*(value/60.0)); }
         });
 
         mDurationPref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
             public int getValue() { return mDuration; }
             public void onSliderChanged(int value) { mDuration = value; updateDuration(); saveAlarm(false); }
             public int progressToValue(int progress) { return (int)(60.0*(progress / 100.0)); }
             public int valueToProgress(int value) { return (int)(100.0*(value/60.0)); }
         });
 
         mDelayPref.setOnSliderChangedListener(new SliderPreference.OnSliderChangedListener() {
             public int getValue() { return mDelay; }
             public void onSliderChanged(int value) { mDelay = value; updateDelay(); saveAlarm(false); }
             public int progressToValue(int progress) { return (int)(1000.0*(progress / 100.0)); }
             public int valueToProgress(int value) { return (int)(100.0*(value/1000.0)); }
         });
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         getContentResolver().unregisterContentObserver(mAlarmsChangeObserver);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog d;
 
         switch (id) {
         case DIALOG_TIMEPICKER:
             d = new TimePickerDialog(
                     SetAlarm.this,
                     this,
                     0,
                     0,
                     DateFormat.is24HourFormat(SetAlarm.this));
             d.setTitle(getResources().getString(R.string.time));
             break;
         default:
             d = null;
         }
 
         return d;
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         super.onPrepareDialog(id, dialog);
 
         switch (id) {
         case DIALOG_TIMEPICKER:
             TimePickerDialog timePicker = (TimePickerDialog)dialog;
             timePicker.updateTime(mHour, mMinutes);
             break;
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 
         if (preference == mTimePref) {
             showDialog(DIALOG_TIMEPICKER);
         } else if (preference == mAlarmOnPref) {
             saveAlarm(true);
         } else if (preference == mNamePref) {
             if (mName == null || mName.length() < 1) mNamePref.setText(getString(R.string.alarm_noname));
             mNamePref.setText(mName);
             saveAlarm(false);
         } else if (preference == mVibratePref) {
             if (!mVibratePref.isChecked()) {
               if (!mVibrateOnly) {
                 mVibrateOnly = true;
                 mVibratePref.setChecked(true);
                 mVibratePref.setTitle(R.string.alarm_vibrate_only);
 
                 mVolumePref.setEnabled(false);
                 mAlarmPref.setEnabled(false);
                 mCrescendoPref.setEnabled(false);
                 mDelayPref.setEnabled(false);
               } else {
                 mVibrateOnly = false;
                 mVibratePref.setChecked(false);
                 mVibratePref.setTitle(R.string.alarm_vibrate);
 
                 mVolumePref.setEnabled(true);
                 mAlarmPref.setEnabled(true);
                 mCrescendoPref.setEnabled(true);
                 mDelayPref.setEnabled(true);
               }
             }
             saveAlarm(false);
         }
 
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
         mHour = hourOfDay;
         mMinutes = minute;
         mAlarmOnPref.setChecked(true);
         saveAlarm(true);
     }
 
     /**
      * Alarms.AlarmSettings implementation.  Database feeds current
      * settings in through this call
      */
     public void reportAlarm(
             int idx, boolean enabled, int hour, int minutes,
             Alarms.DaysOfWeek daysOfWeek, boolean vibrate,
             int snooze, int duration, int delay, boolean vibrate_only , int volume, int crescendo, String name,
             String message, String alert) {
 
         mHour = hour;
         mMinutes = minutes;
         mAlarmOnPref.setChecked(enabled);
         mDaysOfWeek.set(daysOfWeek);
         mVibratePref.setChecked(vibrate);
         mSnooze = snooze;
         mDuration = duration;
         mDelay = delay;
         mVibrateOnly = vibrate_only;
         mVolume = volume;
         mCrescendo = crescendo;
         mName = name;
 
         if (mVibrateOnly) {
             mVibratePref.setTitle(R.string.alarm_vibrate_only);
             mVibratePref.setChecked(true);
             mVolumePref.setEnabled(false);
             mAlarmPref.setEnabled(false);
             mCrescendoPref.setEnabled(false);
             mDelayPref.setEnabled(false);
         }
 
         if (alert == null || alert.length() == 0) {
             if (Log.LOGV) Log.v("****** reportAlarm null or 0-length alert");
             mAlarmPref.mAlert = getDefaultAlarm();
             if (mAlarmPref.mAlert == null) {
                 Log.e("****** Default Alarm null");
             }
         } else {
             mAlarmPref.mAlert = Uri.parse(alert);
             if (mAlarmPref.mAlert == null) {
                 Log.e("****** Parsed null alarm. URI: " + alert);
             }
         }
         if (Log.LOGV) Log.v("****** reportAlarm uri " + alert + " alert " +
                             mAlarmPref.mAlert);
         updateTime();
         updateRepeat();
         updateAlarm(mAlarmPref.mAlert);
 
         updateVolume();
         updateCrescendo();
         updateSnooze();
         updateDuration();
         updateDelay();
         updateName();
 
         mReportAlarmCalled = true;
     }
 
     /**
      * picks the first alarm available
      */
     private Uri getDefaultAlarm() {
         RingtoneManager ringtoneManager = new RingtoneManager(this);
         ringtoneManager.setType(RingtoneManager.TYPE_ALARM);
         return ringtoneManager.getRingtoneUri(0);
     }
 
     private void updateTime() {
         if (Log.LOGV) Log.v("updateTime " + mId);
         mTimePref.setSummary(Alarms.formatTime(this, mHour, mMinutes, mDaysOfWeek));
     }
 
     private void updateAlarm(Uri ringtoneUri) {
         if (Log.LOGV) Log.v("updateAlarm " + mId);
         Ringtone ringtone = RingtoneManager.getRingtone(SetAlarm.this, ringtoneUri);
         if (ringtone != null) {
             mAlarmPref.setSummary(ringtone.getTitle(SetAlarm.this));
         }
     }
 
     private void updateRepeat() {
         if (Log.LOGV) Log.v("updateRepeat " + mId);
         mRepeatPref.setSummary(mDaysOfWeek.toString(this, true));
     }
 
     private void updateVolume() {
         if (mVolume == 0) mVolumePref.setSummary(R.string.volume_zero);
         else mVolumePref.setSummary(String.valueOf(mVolume)+" "+getString(R.string.volume_unit));
     }
     private void updateCrescendo() {
         if (mCrescendo == 0) mCrescendoPref.setSummary(R.string.crescendo_zero);
         else mCrescendoPref.setSummary(String.valueOf(mCrescendo)+" "+getString(R.string.crescendo_unit));
     }
     private void updateSnooze() { 
         if (mSnooze == 0) mSnoozePref.setSummary(R.string.snooze_disabled);
         else mSnoozePref.setSummary(String.valueOf(mSnooze)+" "+getString(R.string.snooze_unit));
     }
     private void updateDuration() {
         if (mDuration == 0) mDurationPref.setSummary(R.string.duration_infinite);
         else mDurationPref.setSummary(String.valueOf(mDuration)+" "+getString(R.string.duration_unit));
     }
     private void updateDelay() {
         if (mDelay == 0) mDelayPref.setSummary(R.string.delay_zero);
         else mDelayPref.setSummary(String.valueOf(mDelay)+" "+getString(R.string.delay_unit));
     }
     private void updateName() {
       if (mName == null || mName.length() < 1) mNamePref.setSummary(R.string.alarm_noname);
       else mNamePref.setSummary(mName);
     }
 
     private void saveAlarm(boolean popToast) {
         if (mReportAlarmCalled && mAlarmPref.mAlert != null) {
             String alertString = mAlarmPref.mAlert.toString();
             saveAlarm(this, mId, mAlarmOnPref.isChecked(), mHour, mMinutes,
                       mDaysOfWeek, mVibratePref.isChecked(), alertString,
                       mSnooze, mDuration, mDelay, mVibrateOnly, mVolume, mCrescendo, mName,
                       popToast);
         }
     }
 
     /**
      * Write alarm out to persistent store and pops toast if alarm
      * enabled
      */
     private static void saveAlarm(
             Context context, int id, boolean enabled, int hour, int minute,
             Alarms.DaysOfWeek daysOfWeek, boolean vibrate, String alert,
             int snooze, int duration, int delay, boolean vibrate_only , int volume, int crescendo, String name,
             boolean popToast) {
         if (Log.LOGV) Log.v("** saveAlarm " + id + " " + enabled + " " + hour +
                             " " + minute + " vibe " + vibrate);
 
         // Fix alert string first
         Alarms.setAlarm(context, id, enabled, hour, minute, daysOfWeek, vibrate,
                         snooze, duration, delay, vibrate_only, volume, crescendo, name,
                         "", alert);
 
         if (enabled && popToast) {
             popAlarmSetToast(context, hour, minute, daysOfWeek);
         }
     }
 
     /**
      * Display a toast that tells the user how long until the alarm
      * goes off.  This helps prevent "am/pm" mistakes.
      */
     static void popAlarmSetToast(Context context, int hour, int minute,
                                  Alarms.DaysOfWeek daysOfWeek) {
 
         String toastText = formatToast(context, hour, minute, daysOfWeek);
         Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
         ToastMaster.setToast(toast);
         toast.show();
     }
 
     /**
      * format "Alarm set for 2 days 7 hours and 53 minutes from
      * now"
      */
     static String formatToast(Context context, int hour, int minute,
                               Alarms.DaysOfWeek daysOfWeek) {
         long alarm = Alarms.calculateAlarm(hour, minute,
                                            daysOfWeek).getTimeInMillis();
         long delta = alarm - System.currentTimeMillis();;
         long hours = delta / (1000 * 60 * 60);
         long minutes = delta / (1000 * 60) % 60;
         long days = hours / 24;
         hours = hours % 24;
 
         String daySeq = (days == 0) ? "" :
                 (days == 1) ? context.getString(R.string.day) :
                 context.getString(R.string.days, Long.toString(days));
 
         String minSeq = (minutes == 0) ? "" :
                 (minutes == 1) ? context.getString(R.string.minute) :
                 context.getString(R.string.minutes, Long.toString(minutes));
 
         String hourSeq = (hours == 0) ? "" :
                 (hours == 1) ? context.getString(R.string.hour) :
                 context.getString(R.string.hours, Long.toString(hours));
 
         boolean dispDays = days > 0;
         boolean dispHour = hours > 0;
         boolean dispMinute = minutes > 0;
 
         String ret;
         if (!(dispDays || dispHour || dispMinute)) {
             ret = context.getString(R.string.subminute);
         } else {
             String parts[] = new String[5];
             parts[0] = daySeq;
             parts[1] = !dispDays ? "" :
                     dispHour && dispMinute ? context.getString(R.string.space) :
                     !dispHour && !dispMinute ? "" :
                     context.getString(R.string.and);
             parts[2] = dispHour ? hourSeq : "";
             parts[3] = dispHour && dispMinute ? context.getString(R.string.and) : "";
             parts[4] = dispMinute ? minSeq : "";
             ret = context.getString(R.string.combiner, (Object[])parts);
         }
 
         ret = context.getString(R.string.alarm_set, ret);
         /* if (Log.LOGV) Log.v("** TOAST daySeq " + daySeq + " hourSeq " + hourSeq +
            " minSeq " + minSeq + " ret " + ret); */
         return ret;
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         MenuItem mi;
         mi = menu.add(0, 0, OPTION_MENU_FIRE, R.string.fire_alarm);
        mi.setIcon(android.R.drawable.ic_menu_view);
 
         mi = menu.add(0, 0, OPTION_MENU_DELETE, R.string.delete_alarm);
         mi.setIcon(android.R.drawable.ic_menu_delete);
 
 
         if (AlarmClock.DEBUG) {
             mTestAlarmItem = menu.add(0, 0, OPTION_MENU_DEBUG, "test alarm");
         }
 
 
         return true;
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getOrder()) {
           case OPTION_MENU_DELETE:
             Alarms.deleteAlarm(this, mId);
             finish();
             return true;
           case OPTION_MENU_FIRE:
             Alarms.enableAlert(this, mId, System.currentTimeMillis());
             return true;
           case OPTION_MENU_DEBUG:
             if (AlarmClock.DEBUG) {
                 if (item == mTestAlarmItem) {
                     setTestAlarm();
                     return true;
                 }
             }
             break;
         }
 
         return false;
     }
 
 
     /**
      * Test code: this is disabled for production build.  Sets
      * this alarm to go off on the next minute
      */
     void setTestAlarm() {
 
         // start with now
         java.util.Calendar c = java.util.Calendar.getInstance();
         c.setTimeInMillis(System.currentTimeMillis());
 
         int nowHour = c.get(java.util.Calendar.HOUR_OF_DAY);
         int nowMinute = c.get(java.util.Calendar.MINUTE);
 
         int minutes = (nowMinute + 1) % 60;
         int hour = nowHour + (nowMinute == 0? 1 : 0);
 
         saveAlarm(this, mId, true, hour, minutes, 
                   mDaysOfWeek, true, mAlarmPref.mAlert.toString(),
                   mSnooze, mDuration, mDelay, mVibrateOnly, mVolume, mCrescendo, mName,
                   true);
     }
 
 }
