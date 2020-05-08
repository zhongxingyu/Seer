 package com.sndurkin.notificationcheck;
 
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.*;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.*;
 import android.provider.Settings;
 import com.crashlytics.android.Crashlytics;
 
 // This PreferenceActivity is the main activity for the application,
 // as it mostly runs in the background.
 public class SettingsActivity extends PreferenceActivity {
 
     enum FilterByApp {
         ALL_APPS,
         ONLY_SELECTED_APPS,
         ALL_BUT_SELECTED_APPS
     }
     enum PhoneRinger {
         SILENT,
         VIBRATE,
         NORMAL
     }
     enum NotificationType {
         NEW_NOTIFICATIONS,
         NEW_NON_PERSISTED_NOTIFICATIONS,
         ALL_NOTIFICATIONS_UNTIL_DISMISSED,
         NON_PERSISTED_NOTIFICATIONS_UNTIL_DISMISSED
     }
     enum VibrationPattern {
         SHORT,
         LONG,
         TWO_SHORT,
         TWO_LONG
     }
 
     public static final int SHORT_VIBRATION_LENGTH = 300;
     public static final int LONG_VIBRATION_LENGTH = 600;
 
     private static final int ACCESSIBILITY_ALERT_DIALOG = 0;
     private static final int NOTIFICATION_LISTENER_ALERT_DIALOG = 1;
     private static final int HELP_DIALOG = 2;
 
     SharedPreferences preferences;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
         /*
         if(!BuildConfig.DEBUG) {
             Crashlytics.start(this);
         }
         */
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.preferences);
 
         initActivePref();
         initFilterNotificationsPref();
         initPhoneRingerPref();
         initNotificationTypePref();
         initVibrationPatternPref();
         initHelpPref();
 
         launchHelpIfApplicable();
         startService(new Intent(this, ScreenOnService.class));
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         final Preference prefActive = findPreference("pref_active");
         if(!isNotificationAccessEnabled()) {
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 ((SwitchPreference) prefActive).setChecked(false);
             }
             else {
                 ((CheckBoxPreference) prefActive).setChecked(false);
             }
         }
     }
 
     private void initActivePref() {
         final Preference prefActive = findPreference("pref_active");
         if(!isNotificationAccessEnabled()) {
             if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                 ((SwitchPreference) prefActive).setChecked(false);
             }
             else {
                 ((CheckBoxPreference) prefActive).setChecked(false);
             }
         }
         prefActive.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
             @Override
             public boolean onPreferenceChange(final Preference preference, Object newValue) {
                 if (((Boolean) newValue) && !isNotificationAccessEnabled()) {
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                         showDialog(NOTIFICATION_LISTENER_ALERT_DIALOG);
                     }
                     else {
                         showDialog(ACCESSIBILITY_ALERT_DIALOG);
                     }
 
                     return false;
                 }
 
                 return true;
             }
         });
     }
 
     private void initFilterNotificationsPref() {
         final Preference prefNotifications  = findPreference("pref_notifications");
         final ListPreference prefWhatToCheck = (ListPreference) findPreference("pref_what");
         prefWhatToCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 bindPreferenceSummaryListener.onPreferenceChange(preference, newValue);
                 int whatToCheck = Integer.parseInt(newValue.toString());
                 prefNotifications.setEnabled(whatToCheck != FilterByApp.ALL_APPS.ordinal());
                 return true;
             }
         });
         initPreferenceSummaryValue(prefWhatToCheck);
 
         int whatToCheck = Integer.parseInt(prefWhatToCheck.getValue().toString());
         prefNotifications.setEnabled(whatToCheck != FilterByApp.ALL_APPS.ordinal());
     }
 
     private void initPhoneRingerPref() {
         MultiSelectListPreference prefPhoneRinger = (MultiSelectListPreference) findPreference("pref_phone_ringer_v2");
         Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
             @Override
             public boolean onPreferenceChange(Preference preference, Object newValue) {
                 MultiSelectListPreference listPreference = (MultiSelectListPreference) preference;
 
                 CharSequence[] values;
                 if(newValue instanceof String) {
                     String valueStr = (String) newValue;
                    if(valueStr.isEmpty()) {
                         values = new CharSequence[0];
                     }
                     else {
                         values = valueStr.split(MultiSelectListPreference.SEPARATOR_REGEX);
                     }
                 }
                 else {
                     values = (CharSequence[]) newValue;
                 }
 
                 if(values.length == 0) {
                     preference.setSummary(getString(R.string.pref_phone_ringer_summary_empty));
                 }
                 else {
                     String displayStr = "";
                     for(int i = 0; i < values.length; ++i) {
                         if(i > 0) {
                             displayStr += ", ";
                         }
                         displayStr += listPreference.getEntries()[listPreference.findIndexOfValue(values[i].toString())];
                     }
 
                     preference.setSummary(getString(R.string.pref_phone_ringer_summary, displayStr));
                 }
 
                 return true;
             }
         };
         prefPhoneRinger.setOnPreferenceChangeListener(changeListener);
 
         String val = preferences.getString(prefPhoneRinger.getKey(), "");
         changeListener.onPreferenceChange(prefPhoneRinger, val);
     }
 
     private void initNotificationTypePref() {
         final EnhancedListPreference prefNotificationType = (EnhancedListPreference) findPreference("pref_notification_type");
         bindPreferenceSummaryToValue(prefNotificationType);
         if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
             prefNotificationType.setRestricted(getString(R.string.restricted_4_3));
         }
     }
 
     private void initVibrationPatternPref() {
         final EnhancedListPreference prefVibrationPattern = (EnhancedListPreference) findPreference("pref_vibration_pattern");
         bindPreferenceSummaryToValue(prefVibrationPattern);
     }
 
     private void initHelpPref() {
         findPreference("pref_help").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
             @Override
             public boolean onPreferenceClick(Preference preference) {
                 showDialog(HELP_DIALOG);
                 return true;
             }
         });
     }
 
     // Launches a Help dialog if this is the first run of the application.
     private void launchHelpIfApplicable() {
         boolean previouslyStarted = preferences.getBoolean("pref_previously_started", false);
         if(!previouslyStarted) {
             SharedPreferences.Editor edit = preferences.edit();
             edit.putBoolean("pref_previously_started", Boolean.TRUE);
             edit.commit();
 
             showDialog(HELP_DIALOG);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch(id) {
             case ACCESSIBILITY_ALERT_DIALOG:
                 return new AlertDialog.Builder(this)
                     .setIcon(android.R.drawable.ic_dialog_alert)
                     .setTitle(R.string.accessibility_alert_title)
                     .setMessage(R.string.accessibility_alert_message)
                     .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             try {
                                 startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                             }
                             catch (ActivityNotFoundException e) {
                                 startActivity(new Intent(Settings.ACTION_SETTINGS));
                             }
                         }
                     })
                     .setNegativeButton(R.string.no, null)
                     .create();
             case NOTIFICATION_LISTENER_ALERT_DIALOG:
                 return new AlertDialog.Builder(this)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle(R.string.notification_listener_alert_title)
                         .setMessage(R.string.notification_listener_alert_message)
                         .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 try {
                                     startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                                 }
                                 catch (ActivityNotFoundException e) {
                                     startActivity(new Intent(Settings.ACTION_SETTINGS));
                                 }
                             }
                         })
                         .setNegativeButton(R.string.no, null)
                         .create();
             case HELP_DIALOG:
                 return new AlertDialog.Builder(this)
                     .setIcon(android.R.drawable.ic_dialog_info)
                     .setTitle(R.string.help_dialog_title)
                     .setMessage(R.string.help_dialog_message)
                     .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             dialog.dismiss();
                         }
                     })
                     .setNegativeButton(null, null)
                     .create();
             default:
                 return null;
         }
     }
 
     private boolean isNotificationAccessEnabled() {
         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
             // Check if the notification listener service is enabled.
             return NotificationService.isNotificationAccessEnabled;
         }
         else {
             // Check if the accessibility service is enabled.
             int accessibilityEnabled = 0;
             try {
                 accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
             }
             catch (Settings.SettingNotFoundException e) {
                 // We can ignore this and just return false.
             }
 
             if (accessibilityEnabled == 1) {
                 String accessibilityServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                 if (accessibilityServices != null) {
                     for(String accessibilityService : accessibilityServices.split(":")) {
                         if (accessibilityService.equalsIgnoreCase(NotificationAccessibilityService.SERVICE_NAME)){
                             return true;
                         }
                     }
                 }
             }
         }
 
         return false;
     }
 
     // A preference value change listener that updates the preference's summary to reflect its new value.
     private static Preference.OnPreferenceChangeListener bindPreferenceSummaryListener = new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object value) {
             String stringValue = value.toString();
 
             if(preference instanceof ListPreference) {
                 // For list preferences, look up the correct display value in
                 // the preference's 'entries' list.
                 ListPreference listPreference = (ListPreference) preference;
                 int index = listPreference.findIndexOfValue(stringValue);
 
                 // Set the summary to reflect the new value.
                 preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
             }
             else {
                 // For all other preferences, set the summary to the value's
                 // simple string representation.
                 preference.setSummary(stringValue);
             }
             return true;
         }
     };
 
     // Binds a preference's summary to its value. More specifically, when the
     // preference's value is changed, its summary (line of text below the
     // preference title) is updated to reflect the value. The summary is also
     // immediately updated upon calling this method. The exact display format is
     // dependent on the type of preference.
     private static void bindPreferenceSummaryToValue(Preference preference) {
         if(preference != null) {
             // Set the listener to watch for value changes.
             preference.setOnPreferenceChangeListener(bindPreferenceSummaryListener);
             initPreferenceSummaryValue(preference);
         }
     }
 
     private static void initPreferenceSummaryValue(Preference preference) {
         String val = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                                       .getString(preference.getKey(), "");
         bindPreferenceSummaryListener.onPreferenceChange(preference, val);
     }
 
 }
