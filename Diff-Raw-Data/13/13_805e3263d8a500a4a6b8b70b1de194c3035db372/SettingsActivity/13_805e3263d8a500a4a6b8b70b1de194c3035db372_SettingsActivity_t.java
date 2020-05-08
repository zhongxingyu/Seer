 package com.projectnocturne;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.preference.RingtonePreference;
 import android.text.TextUtils;
 
 /**
  * A {@link PreferenceActivity} that presents a set of application settings. On
  * handset devices, settings are presented as a single list. On tablets,
  * settings are split by category, with category headers shown to the left of
  * the list of settings.
  * <p/>
  * See <a href="http://developer.android.com/design/patterns/settings.html">
  * Android Design: Settings</a> for design guidelines and the <a
  * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
  * API Guide</a> for more information on developing a Settings UI.
  */
 public class SettingsActivity extends PreferenceActivity {
     public static final String PREF_BED_SENSOR_POL_ENABLED = "pref_enable_sensortag_polling";
     public static final boolean PREF_BED_SENSOR_POL_ENABLED_DEFAULT = true;
     public static final String PREF_BED_SENSOR_POL_INTERVAL = "pref_bed_sensor_pol_interval";
     public static final String PREF_BED_SENSOR_POL_INTERVAL_DEFAULT = "5";
     public static final String PREF_SERVER_ADDRESS = "pref_server_address";
    public static final String PREF_SERVER_ADDRESS_DEFAULT = "192.168.1.218";
     public static final String PREF_SERVER_PORT = "pref_server_port";
    public static final String PREF_SERVER_PORT_DEFAULT = "9090";
 
     /**
      * Determines whether to always show the simplified settings UI, where
      * settings are presented in a single list. When false, settings are shown
      * as a master/detail two-pane view on tablets. When true, a single pane is
      * shown on tablets.
      */
     private static final boolean ALWAYS_SIMPLE_PREFS = false;
     /**
      * A preference value change listener that updates the preference's summary
      * to reflect its new value.
      */
     private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object value) {
             String stringValue = value.toString();
 
             if (preference instanceof ListPreference) {
                 // For list preferences, look up the correct display value in
                 // the preference's 'entries' list.
                 ListPreference listPreference = (ListPreference) preference;
                 int index = listPreference.findIndexOfValue(stringValue);
 
                 // Set the summary to reflect the new value.
                 preference.setSummary(
                         index >= 0
                                 ? listPreference.getEntries()[index]
                                 : null);
 
             } else if (preference instanceof RingtonePreference) {
                 // For ringtone preferences, look up the correct display value
                 // using RingtoneManager.
                 if (TextUtils.isEmpty(stringValue)) {
                     // Empty values correspond to 'silent' (no ringtone).
                     //    preference.setSummary(R.string.pref_ringtone_silent);
 
                 } else {
                     Ringtone ringtone = RingtoneManager.getRingtone(
                             preference.getContext(), Uri.parse(stringValue));
 
                     if (ringtone == null) {
                         // Clear the summary if there was a lookup error.
                         preference.setSummary(null);
                     } else {
                         // Set the summary to reflect the new ringtone display
                         // name.
                         String name = ringtone.getTitle(preference.getContext());
                         preference.setSummary(name);
                     }
                 }
 
             } else {
                 // For all other preferences, set the summary to the value's
                 // simple string representation.
                 preference.setSummary(stringValue);
             }
             return true;
         }
     };
 
     /**
      * Helper method to determine if the device has an extra-large screen. For
      * example, 10" tablets are extra-large.
      */
     private static boolean isXLargeTablet(Context context) {
         return (context.getResources().getConfiguration().screenLayout
                 & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
     }
 
 
     /**
      * Binds a preference's summary to its value. More specifically, when the
      * preference's value is changed, its summary (line of text below the
      * preference title) is updated to reflect the value. The summary is also
      * immediately updated upon calling this method. The exact display format is
      * dependent on the type of preference.
      *
      * @see #sBindPreferenceSummaryToValueListener
      */
     private static void bindPreferenceSummaryToValue(Preference preference) {
         // Set the listener to watch for value changes.
         preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 
         // Trigger the listener immediately with the preference's current value.
         if (preference.getKey().equalsIgnoreCase(PREF_BED_SENSOR_POL_ENABLED)) {
             sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                     PreferenceManager
                             .getDefaultSharedPreferences(preference.getContext())
                             .getBoolean(preference.getKey(), PREF_BED_SENSOR_POL_ENABLED_DEFAULT));
         } else {
             sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                     PreferenceManager
                             .getDefaultSharedPreferences(preference.getContext())
                             .getString(preference.getKey(), ""));
         }
     }
 
     private String formatTimeStr(final String timeStr) {
         String frmtdStr = "";
         if (timeStr.length() < 5) {
             final String[] timeParts = timeStr.split(":");
             if (timeParts[0].length() == 1) {
                 frmtdStr = "0";
             }
             frmtdStr += timeParts[0] + ":";
             if (timeParts[1].length() == 1) {
                 frmtdStr += "0";
             }
             frmtdStr += timeParts[1];
         }
         return frmtdStr;
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
         // Add 'general' preferences.
         addPreferencesFromResource(R.xml.pref_general);
 
 
         // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
         // their values. When their values change, their summaries are updated
         // to reflect the new value, per the Android Design guidelines.
         bindPreferenceSummaryToValue(findPreference("pref_enable_sensortag_polling"));
         bindPreferenceSummaryToValue(findPreference("pref_bed_sensor_pol_interval"));
         bindPreferenceSummaryToValue(findPreference("pref_server_address"));
         bindPreferenceSummaryToValue(findPreference("pref_server_port"));
     }
 
 
     /** {@inheritDoc} */
     @Override
     public boolean onIsMultiPane() {
         return isXLargeTablet(this);
     }
 
 }
