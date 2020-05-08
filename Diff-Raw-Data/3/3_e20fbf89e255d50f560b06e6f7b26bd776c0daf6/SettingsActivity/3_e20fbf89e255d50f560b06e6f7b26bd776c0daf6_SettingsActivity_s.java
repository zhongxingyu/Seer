 package com.gmail.at.zhuikov.aleksandr.rssreader;
 
 import static android.text.TextUtils.isEmpty;
 import static android.widget.Toast.makeText;
 import static com.gmail.at.zhuikov.aleksandr.rssreader.util.Preferences.REFRESH_INTERVAL_PREFERENCE_KEY;
 import static com.gmail.at.zhuikov.aleksandr.rssreader.util.Preferences.RSS_URL_PREFERENCE_KEY;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 
 import com.gmail.at.zhuikov.aleksandr.rssreader.util.Preferences;
 import com.gmail.at.zhuikov.aleksandr.rssreader.util.RSSPollServiceScheduler;
 
 /**
  * Activity for managing application preferences
  */
 public class SettingsActivity extends PreferenceActivity implements
 		SharedPreferences.OnSharedPreferenceChangeListener {
 
 	private static final String TAG = SettingsActivity.class.getSimpleName();
 
 	private ListPreference refreshIntervalPreference;
 	private RSSPollServiceScheduler rssPollServiceScheduler;
 	private EditTextPreference rssUrlPreference;
 
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 
 		RSSReaderServiceLocator serviceLocator = (RSSReaderServiceLocator) getApplication();
 		Preferences preferences = serviceLocator.getPreferences();
 		rssPollServiceScheduler = serviceLocator.getRssPollServiceScheduler();
 
		addPreferencesFromResource(R.xml.settings);
 		getPreferenceManager().setSharedPreferencesName(preferences.getPreferencesFileName());
 
 		rssUrlPreference = (EditTextPreference) getPreferenceScreen()
 				.findPreference(RSS_URL_PREFERENCE_KEY);
 		refreshIntervalPreference = (ListPreference) getPreferenceScreen()
 				.findPreference(REFRESH_INTERVAL_PREFERENCE_KEY);
 		rssUrlPreference
 				.setOnPreferenceChangeListener(new UrlPreferenceChangeListener());
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences()
 				.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updatePreferenceSummaries();
 		getPreferenceScreen().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		updatePreferenceSummaries();
 		rssPollServiceScheduler.schedule();
 	}
 
 	private void updatePreferenceSummaries() {
 		if (!isEmpty(rssUrlPreference.getText())) {
 			rssUrlPreference.setSummary(rssUrlPreference.getText());
 		}
 
 		refreshIntervalPreference.setSummary(refreshIntervalPreference.getEntry());
 	}
 
 	ListPreference getRefreshIntervalPreference() {
 		return refreshIntervalPreference;
 	}
 
 	EditTextPreference getRssUrlPreference() {
 		return rssUrlPreference;
 	}
 
 	private class UrlPreferenceChangeListener implements
 			Preference.OnPreferenceChangeListener {
 
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object value) {
 
 			if (!isValidUrl((String) value)) {
 				makeText(SettingsActivity.this, R.string.wrong_url, 0).show();
 				Log.w(TAG, "Wrong url " + value);
 				return false;
 			}
 
 			return true;
 		}
 
 		private boolean isValidUrl(String urlString) {
 			try {
 				new URL(urlString);
 			} catch (MalformedURLException e) {
 				return false;
 			}
 			return true;
 		}
 	}
 }
