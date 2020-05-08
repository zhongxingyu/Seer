 package com.op.kclock;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 //import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 
 import com.op.kclock.misc.Log;
 import com.op.kclock.settings.FileChooserActivity;
 
 public class SettingsActivity extends PreferenceActivity implements
 		OnPreferenceChangeListener {
 
 	public static final String SYSTEM_SOUND_VALUE = "system";
 	private static final String WHEEL_DIALOG_VALUE = "wheel";
 	private SharedPreferences mPrefs;
 	public static final String CUSTOM_SOUNDFILE_KEY = "pref_soundfile_key";
 	private static final int REQUEST_PICK_FILE = 4;
 	public static final String v = "soundfile";
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
 		addPreferencesFromResource(R.xml.settings);
 
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(this
 				.getApplicationContext());
 		String version = "0";
 		try {
 			PackageInfo pi = getPackageManager().getPackageInfo(
 					getPackageName(), 0);
 			if (pi != null) version = pi.versionName;
 		} catch (PackageManager.NameNotFoundException e) {
 			Log.e(MainActivity.TAG, "Package name not found", e);
 			version = getString(R.string.pref_info_version_error);
 		}
 
 		findPreference(getString(R.string.pref_info_version_key)).setSummary(
 				version);
 
 		Preference soundSource = findPreference(getString(R.string.pref_soundsource_key));
 		if (soundSource != null){
 			soundSource.setOnPreferenceChangeListener(this);
 			soundSourceChanged(((ListPreference)soundSource).getValue());
 		}
 		
 		Preference pickstyle = findPreference(getString(R.string.pref_pickstyle_key));
 		if (pickstyle != null){ 
 			pickstyle.setOnPreferenceChangeListener(this);
 			dialogTypeChanged(((ListPreference) pickstyle).getValue());
 		}
 
 		Preference syssoundPref = findPreference(getString(R.string.pref_notification_ringtone_key));
 		if (syssoundPref != null){ 			
 			syssoundPref.setOnPreferenceChangeListener(this);
 		}
 
 		final Preference soundFilePath = findPreference(getString(R.string.pref_soundfile_path_key));
 		if (soundFilePath != null && ((EditTextPreference)soundFilePath).getText() != null
 				&& ((EditTextPreference)soundFilePath).getText().length() > 0
 				&& ((EditTextPreference) soundFilePath).getText().toCharArray()[0] == '/') {
 			((EditTextPreference) soundFilePath).setTitle(((EditTextPreference) soundFilePath).getText());
 		}
 
 		// add onclick for select shader
 		Preference customPref = findPreference(CUSTOM_SOUNDFILE_KEY);
 		if (customPref != null) {
 			customPref
 					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 						public boolean onPreferenceClick(Preference preference) {
 							// spawn the file chooser
 							Intent fileChooserI = new Intent(
 									SettingsActivity.this,
 									FileChooserActivity.class);
 							if (soundFilePath != null
 									&& ((EditTextPreference) soundFilePath).getText() != null
 									&& ((EditTextPreference) soundFilePath).getText().length() > 0
 									&& ((EditTextPreference) soundFilePath).getText().toCharArray()[0] == '/') {
 								fileChooserI.putExtra(
 										FileChooserActivity.EXTRA_FILE_PATH,
 										((EditTextPreference) soundFilePath).getText().substring(
 												0,
 												((EditTextPreference) soundFilePath).getText()
 														.lastIndexOf("/")));
 							}
 							startActivityForResult(fileChooserI,
 									REQUEST_PICK_FILE);
 							return true;
 						}
 					});
 		}
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK) {
 			switch (requestCode) {
 			case REQUEST_PICK_FILE:
 				if (data.hasExtra(FileChooserActivity.EXTRA_FILE_PATH)) {
 					findViewById(R.string.pref_soundfile_key);
 					String filePath = data
 							.getStringExtra(FileChooserActivity.EXTRA_FILE_PATH);
 					EditTextPreference customPref2 = (EditTextPreference) findPreference(getString(R.string.pref_soundfile_path_key));
 					customPref2.setText(filePath);
 					customPref2.setTitle(filePath);
 				}
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onPreferenceChange(Preference pref, Object value) {
 		if (pref.getKey().equals(getString(R.string.pref_soundsource_key)))
 			soundSourceChanged(value);
 		if (pref.getKey().equals(getString(R.string.pref_pickstyle_key)))
 			dialogTypeChanged(value);
 		if (pref.getKey().equals(
 				getString(R.string.pref_notification_ringtone_key))) {
 			// dialogTypeChanged(value);
 		}
 
 		return true;
 	}
 
 	private void dialogTypeChanged(Object value) {
 		Preference simmetricpick = findPreference(getString(R.string.pref_simmetricpick_key));
 		Preference cyclmins = findPreference(getString(R.string.pref_cyclicmins_key));
 		if (((String) value).equals(WHEEL_DIALOG_VALUE)) {
 			simmetricpick.setEnabled(true);
 			cyclmins.setEnabled(true);
 		} else {
 			simmetricpick.setEnabled(false);
 			cyclmins.setEnabled(false);
 		}
 
 	}
 
 	public void soundSourceChanged(Object value) {
 		Preference customPref = findPreference(getString(R.string.pref_soundfile_path_key));
 		Preference customPrefBtn = findPreference(getString(R.string.pref_soundfile_key));
 		Preference syssoundPref = findPreference(getString(R.string.pref_notification_ringtone_key));
 		customPref.setEnabled(false);
 		if (((String) value).equals(SYSTEM_SOUND_VALUE)) {
 			customPrefBtn.setEnabled(false);
 			syssoundPref.setEnabled(true);
 		} else {
 			// customPref.setEnabled(true);
 			customPrefBtn.setEnabled(true);
 			syssoundPref.setEnabled(false);
 		}
 	}
 	
 	public void volumeChanged(Object value) {
		VolumePreference volumePref = (VolumePreference)findPreference(getString(R.string.pref_volume_key));		
 	}
 }
