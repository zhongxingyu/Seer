 package net.skweez.sipgate.activity;
 
 import net.skweez.sipgate.R;
import net.skweez.sipgate.R.layout;
import net.skweez.sipgate.R.string;
import net.skweez.sipgate.R.xml;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.util.Log;
 
 public class SetupActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	private static final String TAG = SetupActivity.class.getName();
 
 	private EditTextPreference userNamePreference;
 
 	private String KEY_USERNAME;
 
 	private String KEY_VERSION;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		KEY_USERNAME = getString(R.string.prefs_username_key);
 		KEY_VERSION = getString(R.string.prefs_version_key);
 
 		setContentView(R.layout.setup);
 		addPreferencesFromResource(R.xml.preferences);
 		userNamePreference = (EditTextPreference) getPreferenceScreen()
 				.findPreference(KEY_USERNAME);
 
 		loadVersionInformation();
 	}
 
 	/**
 	 * Gets the version number from the package manager and sets it as the
 	 * summary of the version preference.
 	 */
 	private void loadVersionInformation() {
 		PackageManager pm = getPackageManager();
 		PackageInfo info = null;
 		try {
 			info = pm.getPackageInfo("net.skweez.sipgate", 0);
 		} catch (NameNotFoundException e) {
 			Log.e(TAG, "Cannot find package info.");
 		}
 		Preference version = findPreference(KEY_VERSION);
 		version.setSummary(info.versionName);
 	}
 
 	/** {@inheritDoc} */
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals(KEY_USERNAME)) {
 			userNamePreference.setSummary(sharedPreferences.getString(
 					KEY_USERNAME, getString(R.string.prefs_username_summary)));
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		userNamePreference.setSummary(getPreferenceScreen()
 				.getSharedPreferences().getString(KEY_USERNAME,
 						getString(R.string.prefs_username_summary)));
 
 		getPreferenceScreen().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences()
 				.unregisterOnSharedPreferenceChangeListener(this);
 	}
 }
