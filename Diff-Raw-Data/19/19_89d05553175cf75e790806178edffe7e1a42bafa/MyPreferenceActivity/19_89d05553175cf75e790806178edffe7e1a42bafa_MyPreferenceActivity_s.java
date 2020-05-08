 package edu.hm.androidsweeper.activities;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import edu.hm.androidsweeper.R;
 
 
 /**
  * Preference activity.
  */
 public class MyPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
     
     /** Player name preference key. */
     public static final String KEY_PLAYERNAME_PREFERENCE = "player_name";
     
     private EditTextPreference playerName;
     
     @SuppressWarnings("deprecation")
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
         
         Preference pref = getPreferenceScreen().findPreference(KEY_PLAYERNAME_PREFERENCE);
         if (!(pref instanceof EditTextPreference)) {
             throw new AssertionError();
         }
         playerName = (EditTextPreference)pref;
         
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         
         setPlayerNamePref(PreferenceManager.getDefaultSharedPreferences(this));
         
         PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
     }
     
     
     
     @Override
     public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
         if (key.equals(KEY_PLAYERNAME_PREFERENCE)) {
             setPlayerNamePref(sharedPreferences);
         }
         
     }
     
     private void setPlayerNamePref(final SharedPreferences sharedPreferences) {
         String value = sharedPreferences.getString(KEY_PLAYERNAME_PREFERENCE, null);
         String summary;
         if (value.equals(getResources().getString(R.string.prefs_default_player_name))) {
             summary = getResources().getString(R.string.prefs_default_player_name_summary);
         }
         else {
             summary = value;
         }
         playerName.setSummary(summary);
     }
     
     
     /** Loads and sets the default preferences.
      * @param context The application context.
      * @param override If the values should be overridden if they already exist.
      */
     public static void loadDefaultPreferences(final Context context, final boolean override) {
        PreferenceManager.setDefaultValues(context, R.xml.settings, override);
     }
 }
