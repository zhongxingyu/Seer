 package com.example.intervaltraining;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceFragment;
 
 // TODO: Music settings
 public class SettingsFragment extends PreferenceFragment implements
 		OnSharedPreferenceChangeListener {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Load the preferences from an XML resource
 		addPreferencesFromResource(R.xml.preferences);
 		setSummaries();
 	}
 
 	private void setSummaries() {
 		SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
 
		setSummary(prefs, "key_pref_interval_time");
 		setSummary(prefs, "key_pref_interval_distance");
 		setSummary(prefs, "key_pref_time_decrement");
 		setSummary(prefs, "key_pref_interval_beep");
 
 	}
 
 	private void setSummary(SharedPreferences prefs, String key) {
 		String summaryText = "";
 		String prefValue = prefs.getString(key, "");
 
 		Preference connectionPref = findPreference(key);
 
		if (key.equals("key_pref_interval_time")) {
 			summaryText = prefValue + " seconds";
 		} else if (key.equals("key_pref_interval_distance")) {
 			summaryText = prefValue + " meters";
 		} else if (key.equals("key_pref_time_decrement")) {
 			summaryText = prefValue + "%";
 		} else if (key.equals("key_pref_interval_beep")) {
 			summaryText = prefValue;
 		}
 
 		connectionPref.setSummary(summaryText);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		setSummary(prefs, key);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences()
 				.unregisterOnSharedPreferenceChangeListener(this);
 	}
 }
