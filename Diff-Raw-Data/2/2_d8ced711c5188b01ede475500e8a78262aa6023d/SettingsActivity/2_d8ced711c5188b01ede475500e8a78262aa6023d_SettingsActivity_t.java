 package com.thoughtworks;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 
 public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
     public static final String KEY_DEFAULT_TEXT = "default_text_preference";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.layout.settings);
         Preference default_text_preference = findPreference(KEY_DEFAULT_TEXT);
        default_text_preference.setSummary(getPreferenceManager().getSharedPreferences().getString(KEY_DEFAULT_TEXT, getString(R.string.summary_default_text_preference)));
     }
 
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         if (key.equals(KEY_DEFAULT_TEXT)) {
             Preference preference = findPreference(key);
             preference.setSummary(sharedPreferences.getString(key, ""));
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
     }
 }
