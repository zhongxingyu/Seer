 package com.anderspersson.xbmcwidget.configuration;
 
 import com.anderspersson.xbmcwidget.R;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class ApplicationPreferenceActivity extends PreferenceActivity {
 	
 	private RecentTvRefreshPrefUpdater recentTvRefreshPrefUpdater;
 	private PreferenceChangedListener preferenceChangedListener;
 	private OnPreferenceChangeListener updateUsernameSummary = new OnPreferenceChangeListener() {
 		public boolean onPreferenceChange(Preference preference, Object newValue) {
 			final EditTextPreference pref = (EditTextPreference)findPreference("username_preference");
 			pref.setSummary((String)newValue);
 			return true;
 		}};
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.layout.settings_pre_v11);
 		setUsername();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		preferenceChangedListener = new PreferenceChangedListener(this);
 		prefs.registerOnSharedPreferenceChangeListener(preferenceChangedListener);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		setLastRefreshed();
 	}
 
 	private void setUsername() {
 		final EditTextPreference pref = (EditTextPreference)findPreference("username_preference");
 		pref.setSummary(pref.getText());
 		pref.setOnPreferenceChangeListener(updateUsernameSummary);
 	}
 
 	private void setLastRefreshed() {
 		final EditTextPreference pref = (EditTextPreference)findPreference("recenttv_last_refresh");
 		recentTvRefreshPrefUpdater = new RecentTvRefreshPrefUpdater(pref);
 	}
 }
