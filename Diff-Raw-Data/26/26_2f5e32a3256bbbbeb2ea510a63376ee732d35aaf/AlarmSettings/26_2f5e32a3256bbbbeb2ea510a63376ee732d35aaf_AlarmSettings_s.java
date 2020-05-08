 /**
  * @file   AlarmSettings.java
  * @author josh <yenliangl at gmail dot com>
  * @date   Tue Jan  5 22:09:25 2010
  *
  * @brief
  *
  *
  */
 package org.startsmall.openalarm;
 
 import android.app.Dialog;
 import android.os.Bundle;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.lang.reflect.Method;
 
 public class AlarmSettings extends PreferenceActivity {
     private static final String TAG = "AlarmSettings";
 
     // Dialog IDs
     private static final int DIALOG_ID_ENTER_LABEL = 1;
     private static final int DIALOG_ID_PICKUP_TIME = 2;
     private static final int DIALOG_ID_PICKUP_HANDLER = 3;
     private static final int DIALOG_ID_PICKUP_REPEAT_DAYS = 4;
 
     AlarmLabelPreference mLabelPreference;
     AlarmTimePreference mTimePreference;
     AlarmActionPreference mActionPreference;
     AlarmRepeatOnPreference mRepeatOnPreference;
     PreferenceCategory mExtraSettingsCategory;
 
     private int mDebugCounter;
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
 
         Log.d(TAG, "====> AlarmSettings.onCreate(" + bundle + ")");
 
         addPreferencesFromResource(R.xml.alarm_settings);
 
         PreferenceManager preferenceManager = getPreferenceManager();
         mTimePreference =
             (AlarmTimePreference)preferenceManager.findPreference(
                 getString(R.string.alarm_settings_time_key));
         mLabelPreference =
             (AlarmLabelPreference)preferenceManager.findPreference(
                 getString(R.string.alarm_settings_label_key));
         mActionPreference =
             (AlarmActionPreference)preferenceManager.findPreference(
                 getString(R.string.alarm_settings_action_key));
         mActionPreference.setOnSelectActionListener(
             new AlarmActionPreference.IOnSelectActionListener() {
                 public void onSelectAction(String handlerClassName) {
                     inflateExtraSettings(handlerClassName, null);
                 }
             });
         mRepeatOnPreference =
             (AlarmRepeatOnPreference)preferenceManager.findPreference(
                 getString(R.string.alarm_settings_repeat_days_key));
         mExtraSettingsCategory =
             (PreferenceCategory)preferenceManager.findPreference(
                 getString(R.string.alarm_settings_extra_category_key));
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                          Preference preference) {
         int id = -1;
         if(preference == mLabelPreference) {
             id = DIALOG_ID_ENTER_LABEL;
         } else if(preference == mTimePreference) {
             id = DIALOG_ID_PICKUP_TIME;
         } else if(preference == mActionPreference) {
             id = DIALOG_ID_PICKUP_HANDLER;
         } else if(preference == mRepeatOnPreference) {
             id = DIALOG_ID_PICKUP_REPEAT_DAYS;
         }
 
         if(id != -1) {
             showDialog(id);
             return true;
         }
 
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
 
     // Save the current state of this activity into Bundle. It
     // will be restored in onCreate() with saved state passed in.
     // protected void onSaveInstanceState(Bundle outState) {
     //     Log.d(TAG, "===> AlarmSettings.onSaveInstanceState()" + (mDebugCounter++) );
     //     super.onSaveInstanceState(outState);
     // }
 
     // protected void onRestoreInstanceState(Bundle outState) {
     //     Log.d(TAG, "===========> onRestoreInstanceState()");
     //     super.onRestoreInstanceState(outState);
     // }
 
     // protected void onDestroy() {
     //     super.onDestroy();
     //     Log.d(TAG, "======> AlarmSettings.onDestroy()");
     // }
 
     /**
      * All dialogs are managed by this activity. Things on the
      * screen must be snapshot into a Bundle through their
      * onSaveInstanceState() and restored through
      * onRestoreInstanceState(). \sa MyPreference.
      *
      * @param dialogId ID of dialog. Must be consistent with the one used in showDialog().
      *
      * @return Created Dialog object reference.
      */
     @Override
     protected Dialog onCreateDialog(int dialogId) {
         Dialog dialog;
         switch(dialogId) {
         case DIALOG_ID_ENTER_LABEL:
             dialog = mLabelPreference.getDialog();
             break;
 
         case DIALOG_ID_PICKUP_TIME:
             dialog = mTimePreference.getDialog();
             break;
 
         case DIALOG_ID_PICKUP_HANDLER:
             dialog = mActionPreference.getDialog();
             break;
 
         case DIALOG_ID_PICKUP_REPEAT_DAYS:
             dialog = mRepeatOnPreference.getDialog();
             break;
 
         default:
             throw new IllegalArgumentException("wrong dialog id");
         }
         return dialog;
     }
 
     /**
      * Commit alarm settings into backing SQL database and
      * re-calculate its time if it is needed.
      *
      */
     @Override
     protected void onPause() {
         Log.d(TAG, "===> AlarmSettings.onPause(): " + (mDebugCounter++));
 
         super.onPause();
 
         Intent i = getIntent();
         final int alarmId = i.getIntExtra(Alarms.AlarmColumns._ID, -1);
         final String newLabel = (String)mLabelPreference.getPreferenceValue();
         final int newTime = (Integer)mTimePreference.getPreferenceValue();
         final int newHourOfDay = newTime / 100;
         final int newMinutes = newTime % 100;
         final int newRepeatOnDaysCode = (Integer)mRepeatOnPreference.getPreferenceValue();
         final String newHandler = (String)mActionPreference.getPreferenceValue();
         final String newExtra = generateValueOfExtraSettings(mExtraSettingsCategory);
 
         // Fetch settings from persistent database
         Uri alarmUri = Alarms.getAlarmUri(alarmId);
         Alarms.GetAlarmSettings dbSettings = new Alarms.GetAlarmSettings();
         Alarms.forEachAlarm(this, alarmUri, dbSettings);
 
         Log.d(TAG, "===> Get alarm settings in SQL database: id=" + alarmId +
               ", label=" + dbSettings.label +
               ", enabled=" + dbSettings.enabled +
               ", time=" + dbSettings.hour + ":" + dbSettings.minutes +
               ", repeat on='" + Alarms.RepeatWeekdays.toString(dbSettings.repeatOnDaysCode) + "'" +
               ", handler=" + dbSettings.handler +
               ", extra=" + dbSettings.extra);
 
         ContentValues newValues = new ContentValues();
         if (!newLabel.equals(dbSettings.label)) {
             newValues.put(Alarms.AlarmColumns.LABEL, newLabel);
         }
 
         if (newHourOfDay != dbSettings.hour) {
             newValues.put(Alarms.AlarmColumns.HOUR, newHourOfDay);
         }
 
         if (newMinutes != dbSettings.minutes) {
             newValues.put(Alarms.AlarmColumns.MINUTES, newMinutes);
         }
 
         if (newRepeatOnDaysCode != dbSettings.repeatOnDaysCode) {
             newValues.put(Alarms.AlarmColumns.REPEAT_DAYS, newRepeatOnDaysCode);
         }
 
         if (!TextUtils.isEmpty(newHandler) &&
             !newHandler.equals(dbSettings.handler)) {
             newValues.put(Alarms.AlarmColumns.HANDLER, newHandler);
         }
 
         if (!TextUtils.isEmpty(newExtra) && !newExtra.equals(dbSettings.extra)) {
             newValues.put(Alarms.AlarmColumns.EXTRA, newExtra);
         }
 
         boolean updated = false;
         // If this alarm is already enabled, we need to calculate
         // a new time for it.
         if (dbSettings.enabled &&
             // If these values were updated, we need to
             // re-schedule the alarm with these new values.
             (newValues.containsKey(Alarms.AlarmColumns.HOUR) ||
              newValues.containsKey(Alarms.AlarmColumns.MINUTES) ||
              newValues.containsKey(Alarms.AlarmColumns.REPEAT_DAYS) ||
              newValues.containsKey(Alarms.AlarmColumns.HANDLER) ||
              newValues.containsKey(Alarms.AlarmColumns.EXTRA))) {
 
             // Deactivate the old alarm when the following
             // situation happens,
             //
             //  ---+------+--------+------->
             //   now     old      new
             //
 
             // If the alert was snoozed and then
             // re-scheduled, no need to go through
             // Alarms.cancelSnoozedAlarm() because I can
             // cancel all alarm triggered by this kind of
             // Intent, same as snoozed alert.
 
             // Disable the alert enabled by dbSettings.handler;
             Alarms.disableAlarm(this, alarmId, dbSettings.handler);
 
             Alarms.updateAlarm(this, alarmUri, newValues);
             Alarms.setAlarmEnabled(this, alarmUri, true);
 
             updated = true;
         }
 
         if (newValues.size() > 0 && !updated) {
             Alarms.updateAlarm(this, alarmUri, newValues);
         }
     }
 
     /**
      * Resume this activity and populate alarm settings from
      * saved SQL rows.
      *
      */
     @Override
     protected void onResume() {
         super.onResume();
 
         // Fetch alarm settings from persistent content
         Intent i = getIntent();
         final int alarmId = i.getIntExtra(Alarms.AlarmColumns._ID, -1);
         Alarms.GetAlarmSettings dbSettings = new Alarms.GetAlarmSettings();
         Alarms.forEachAlarm(this, Alarms.getAlarmUri(alarmId), dbSettings);
 
         // Populate alarm settings into Preferences.
         mLabelPreference.setPreferenceValue(dbSettings.label);
         mTimePreference.setPreferenceValue(
             dbSettings.hour * 100 + dbSettings.minutes);
         mRepeatOnPreference.setPreferenceValue(dbSettings.repeatOnDaysCode);
        mActionPreference.setPreferenceValue(dbSettings.handler);
 
         // If a preference can cause AlarmSettings.onPause() to
         // be called (i.e, AlarmSettings is overlapped by some
         // other activity) and the value of this preference is
         // changed by the user. The value of the preference is
         // newer than the stored value. We should pass this value
         // to the preference inflation process. (Take a look at
         // RingtonePreference).
         String valueOfExtraSettings = generateValueOfExtraSettings(mExtraSettingsCategory);
         if (TextUtils.isEmpty(valueOfExtraSettings)) {
             inflateExtraSettings(dbSettings.handler, dbSettings.extra);
         } else {
             inflateExtraSettings(dbSettings.handler, valueOfExtraSettings);
         }
         Log.d(TAG, "===> valueOfExtraSettings='" + valueOfExtraSettings + "', dbSettings.extra=" + dbSettings.extra);
     }
 
     /**
      * Load preferences of an alarm handler. Use Class.forName()
      * and reflective methods to call addMyPreferences() of an
      * alarm handler which installs its preferences under Extra
      * Settings category.
      *
      * @param handlerClassName Class name of alarm handler.
      * @param defaultValue Concatenated values of all preferences under Extra Settings category.
      */
     private void inflateExtraSettings(String handlerClassName, String defaultValue) {
         if(TextUtils.isEmpty(handlerClassName)) {
             return;
         }
         mExtraSettingsCategory.removeAll();
 
         try {
             Class<?> handler = Alarms.getHandlerClass(handlerClassName);
             Method m = handler.getDeclaredMethod(
                 "addMyPreferences",
                 Class.forName("android.content.Context"),
                 Class.forName("android.preference.PreferenceCategory"),
                 Class.forName("java.lang.String"));
             m.invoke(handler.newInstance(), this, mExtraSettingsCategory, defaultValue);
         } catch(Exception e) {
             Log.d(TAG, e.getMessage());
         }
     }
 
     private String generateValueOfExtraSettings(PreferenceCategory category) {
         SharedPreferences sharedPreferences = category.getSharedPreferences();
         StringBuilder sb = new StringBuilder();
         final int numberOfPreferences = category.getPreferenceCount();
         for (int i = 0; i < numberOfPreferences; i++) {
             Preference preference = category.getPreference(i);
 
             // Preference must be persisted in order for me to
             // use unified SharedPreference to get preference
             // value.
             if (preference.hasKey() && preference.isPersistent()) {
                 String key = preference.getKey();
 
                 try {
                     String value = sharedPreferences.getString(key, null);
                     sb.append(key + "=" + value + ';');
                     continue;
                 } catch (ClassCastException e) {
                 }
 
                 try {
                     boolean value = sharedPreferences.getBoolean(key, false);
                     sb.append(key + "=" + value + ';');
                     continue;
                 } catch (ClassCastException e) {
                 }
 
                 try {
                     float value = sharedPreferences.getFloat(key, -1.0f);
                     sb.append(key + "=" + value + ';');
                     continue;
                 } catch (ClassCastException e) {
                 }
 
                 try {
                     int value = sharedPreferences.getInt(key, -1);
                     sb.append(key + "=" + value + ';');
                     continue;
                 } catch (ClassCastException e) {
                 }
 
                 try {
                     long value = sharedPreferences.getLong(key, -1);
                     sb.append(key + "=" + value + ';');
                     continue;
                 } catch (ClassCastException e) {
                 }
             }
         }
 
         return sb.toString();
     }
 }
