 package org.startsmall.openalarm;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceCategory;
 import android.text.method.DigitsKeyListener;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class AlarmHandler extends AbsHandler {
     private static final String TAG = "AlarmHandler";
 
     static final String EXTRA_KEY_VIBRATE = "vibrate";
     static final String EXTRA_KEY_RINGTONE = "ringtone";
     static final String EXTRA_KEY_SNOOZE_DURATION = "snooze_duration";
     static final String EXTRA_KEY_LOCK_MODE = "lock_mode";
     static final String EXTRA_KEY_LOCK_MODE_PASSWORD = "lock_mode_password";
     static final int LOCK_MODE_NONE = 1;
     static final int LOCK_MODE_MATH = 2;
     static final int LOCK_MODE_PASSWORD = 3;
 
     private static final int DEFAULT_SNOOZE_DURATION = 2; // 2 minutes
 
     public void onReceive(Context context, Intent intent) {
         // Log.i(TAG, "===> onReceive(): " + Calendar.getInstance());
 
         // Parse extra data in the Intent and put them into Intent.
         final String extra = intent.getStringExtra(AlarmColumns.EXTRA);
         putBundleIntoIntent(intent, getBundleFromExtra(extra));
 
         // Start an activity to play ringtone.
         intent.setClass(context, FireAlarmActivity.class)
             .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
         context.startActivity(intent);
     }
 
     /**
      * Add preferences of this handler into Extra Settings.
      *
      * @param context
      * @param category
      * @param extra
      */
     @Override
     public void addMyPreferences(final Context context,
                                  final PreferenceCategory category,
                                  final String extra) {
         // Vibrate
         CheckBoxPreference vibratePref = new CheckBoxPreference(context);
         vibratePref.setKey(EXTRA_KEY_VIBRATE);
         vibratePref.setPersistent(true);
         vibratePref.setTitle(R.string.alarm_handler_vibrate_title);
         vibratePref.setSummaryOn(R.string.on);
         vibratePref.setSummaryOff(R.string.off);
         category.addPreference(vibratePref);
 
         // Ringtone;
         RingtonePreference ringtonePref = new RingtonePreference(context);
         ringtonePref.setShowDefault(false);
         ringtonePref.setShowSilent(false);
         ringtonePref.setTitle(R.string.ringtone_title);
         ringtonePref.setKey(EXTRA_KEY_RINGTONE);
         ringtonePref.setPersistent(true);
         ringtonePref.setRingtoneType(RingtoneManager.TYPE_ALL);
         category.addPreference(ringtonePref);
 
         // Snooze duration
         EditTextPreference snoozeDurationPref = new EditTextPreference(context);
         snoozeDurationPref.setPersistent(true);
         snoozeDurationPref.setTitle(R.string.alarm_handler_snooze_duration_title);
         snoozeDurationPref.setKey(EXTRA_KEY_SNOOZE_DURATION);
         snoozeDurationPref.setDefaultValue("2");
         snoozeDurationPref.getEditText().setKeyListener(
             DigitsKeyListener.getInstance(false, false));
         snoozeDurationPref.setDialogTitle(R.string.alarm_handler_snooze_duration_dialog_title);
         snoozeDurationPref.setOnPreferenceChangeListener(
             new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference p, Object newValue) {
                     ((EditTextPreference)p).setSummary(
                         Integer.parseInt((String)newValue) + " minutes");
                     return true;
                 }
             });
         category.addPreference(snoozeDurationPref);
 
         final EditTextPreference passwordPref = new EditTextPreference(context);
         passwordPref.setKey(EXTRA_KEY_LOCK_MODE_PASSWORD);
         passwordPref.setPersistent(true);
         passwordPref.setTitle(R.string.alarm_handler_deep_sleeper_mode_password_title);
         passwordPref.setDialogTitle(R.string.alarm_handler_deep_sleeper_mode_password_dialog_title);
         passwordPref.getEditText().setKeyListener(DigitsKeyListener.getInstance("123456789"));
         passwordPref.setOnPreferenceChangeListener(
             new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference p, Object newValue) {
                     p.setSummary((String)newValue);
                     return true;
                 }
             });
 
         ListPreference lockModePref = new ListPreference(context);
         lockModePref.setKey(EXTRA_KEY_LOCK_MODE);
         lockModePref.setPersistent(true);
         lockModePref.setTitle(R.string.alarm_handler_deep_sleeper_mode_title);
         lockModePref.setDialogTitle(R.string.alarm_handler_deep_sleeper_mode_dialog_title);
         category.addPreference(lockModePref);
         lockModePref.setEntries(R.array.alarm_handler_deep_sleeper_mode);
         lockModePref.setEntryValues(new CharSequence[]{"1", "2", "3"});
         lockModePref.setOnPreferenceChangeListener(
             new Preference.OnPreferenceChangeListener() {
                 @Override
                 public boolean onPreferenceChange(Preference p, Object newValue) {
                     int index = Integer.parseInt((String)newValue) - 1;
                     passwordPref.setEnabled(index == 2 ? true : false);
                     ListPreference lockPref = (ListPreference)p;
                     lockPref.setValueIndex(index);
                     lockPref.setSummary(lockPref.getEntry());
                     return true;
                 }
             });
 
         category.addPreference(passwordPref);
 
         if (TextUtils.isEmpty(extra)) {
             vibratePref.setChecked(false);
             ringtonePref.setRingtoneUri(null);
             ringtonePref.setSummary("");
             snoozeDurationPref.setText("");
             snoozeDurationPref.setSummary("");
             lockModePref.setValueIndex(0);
             passwordPref.setText("");
         } else {
             Bundle result = getBundleFromExtra(extra);
 
             boolean vibrate = result.getBoolean(EXTRA_KEY_VIBRATE, false);
             vibratePref.setChecked(vibrate);
 
             String rtString = result.getString(EXTRA_KEY_RINGTONE);
             if(!TextUtils.isEmpty(rtString)) {
                 Uri rtUri = Uri.parse(rtString);
                 ringtonePref.setRingtoneUri(rtUri);
             }
 
             int snoozeDuration = result.getInt(EXTRA_KEY_SNOOZE_DURATION, -1);
             if (snoozeDuration != -1) {
                 snoozeDurationPref.setSummary(
                     Integer.toString(snoozeDuration) + " minutes");
                 snoozeDurationPref.setText(String.valueOf(snoozeDuration));
             }
 
             int lockMode = result.getInt(EXTRA_KEY_LOCK_MODE, 1);
             lockModePref.setValueIndex(lockMode - 1);
 
             String password = result.getString(EXTRA_KEY_LOCK_MODE_PASSWORD);
             if (password == null) {
                 password = "";
             }
             passwordPref.setText(password);
         }
 
         lockModePref.setSummary(lockModePref.getEntry());
        passwordPref.setEnabled(lockModePref.getValue().equals("3") ? true : false);
         passwordPref.setSummary(passwordPref.getText());
     }
 
     @Override
     protected void putBundleIntoIntent(Intent intent, Bundle bundle) {
         final boolean vibrate = bundle.getBoolean(EXTRA_KEY_VIBRATE, false);
         intent.putExtra(EXTRA_KEY_VIBRATE, vibrate);
 
         final String uriString = bundle.getString(EXTRA_KEY_RINGTONE);
         if (!TextUtils.isEmpty(uriString)) {
             intent.putExtra(EXTRA_KEY_RINGTONE, uriString);
         }
 
         final int ringtoneDuration = bundle.getInt(EXTRA_KEY_SNOOZE_DURATION, DEFAULT_SNOOZE_DURATION);
         intent.putExtra(EXTRA_KEY_SNOOZE_DURATION, ringtoneDuration);
 
         final int lockMode = bundle.getInt(EXTRA_KEY_LOCK_MODE, 1);
         intent.putExtra(EXTRA_KEY_LOCK_MODE, lockMode);
 
         final String password = bundle.getString(EXTRA_KEY_LOCK_MODE_PASSWORD);
         intent.putExtra(EXTRA_KEY_LOCK_MODE_PASSWORD, password);
     }
 
     @Override
     protected Bundle getBundleFromExtra(String extra) {
         Bundle result = new Bundle();
         if (!TextUtils.isEmpty(extra)) {
             String[] values = TextUtils.split(extra, SEPARATOR);
             for (String value : values) {
                 if (TextUtils.isEmpty(value) ||
                     !value.matches("(\\w+)=.*")) {
                     continue;
                 }
 
                 String[] elems = value.split("=");
                 if (elems[0].equals(EXTRA_KEY_VIBRATE)) {
                     boolean vibrate = false;
                     if (elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         vibrate = Boolean.parseBoolean(elems[1]);
                     }
                     result.putBoolean(EXTRA_KEY_VIBRATE, vibrate);
                 } else if (elems[0].equals(EXTRA_KEY_RINGTONE)) {
                     if(elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putString(EXTRA_KEY_RINGTONE, elems[1]);
                     }
                 } else if (elems[0].equals(EXTRA_KEY_SNOOZE_DURATION)) {
                     if (elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putInt(EXTRA_KEY_SNOOZE_DURATION,
                                       Integer.parseInt(elems[1]));
                     }
                 } else if (elems[0].equals(EXTRA_KEY_LOCK_MODE)) {
                     if (elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putInt(EXTRA_KEY_LOCK_MODE, Integer.parseInt(elems[1]));
                     }
                 } else if (elems[0].equals(EXTRA_KEY_LOCK_MODE_PASSWORD)) {
                     if (elems.length == 2 && !TextUtils.isEmpty(elems[1])) {
                         result.putString(EXTRA_KEY_LOCK_MODE_PASSWORD, elems[1]);
                     }
                 }
             }
         }
         return result;
     }
 }
