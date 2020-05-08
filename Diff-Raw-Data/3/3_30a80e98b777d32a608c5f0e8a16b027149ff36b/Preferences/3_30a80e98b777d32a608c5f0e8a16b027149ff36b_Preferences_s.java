 package com.tuit.ar.activities;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 
 import com.tuit.ar.R;
 import com.tuit.ar.models.Settings;
 import com.tuit.ar.models.SettingsObserver;
 import com.tuit.ar.preferences.DialogPreference;
 import com.tuit.ar.preferences.DialogPreferenceListener;
 import com.tuit.ar.preferences.EditTextPreference;
 
 public class Preferences extends PreferenceActivity implements SettingsObserver {
 	ListPreference updateInterval;
 	CheckBoxPreference automaticUpdate;
 	EditTextPreference filter;
 	DialogPreference filterDelete;
 	CheckBoxPreference showAvatar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Settings.getInstance().addObserver(this);
 
 		addPreferencesFromResource(R.layout.preferences);
 
 		automaticUpdate = (CheckBoxPreference) findPreference(Settings.AUTOMATIC_UPDATE);
 		automaticUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
 				updateInterval.setEnabled(automaticUpdate.isChecked());
 				updateSettings();
 				return false;
 			}
 		});
 
 		updateInterval = (ListPreference) findPreference(Settings.UPDATE_INTERVAL);
 		updateInterval.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
 				updateSettings();
 				return false;
 			}
 		});
 
 		filter = (EditTextPreference)getPreferenceScreen().findPreference(Settings.FILTER);
 		filter.setDialogPreferenceListener(new DialogPreferenceListener() {
 			public void onDialogClosed(boolean positiveValue) {
 				if (positiveValue) {
 					filter.setText(filter.getEditText().getText().toString());
 				}
 				updateSettings();
 			}
 		});
 
 		filterDelete = (com.tuit.ar.preferences.DialogPreference)findPreference(Settings.FILTER_DELETE);
 		filterDelete.setDialogPreferenceListener(new DialogPreferenceListener() {
 			public void onDialogClosed(boolean positiveValue) {
 				if (positiveValue) {
 					filter.setText("");
 					updateSettings();
 				}
 			}
 		});
 
 		showAvatar = (CheckBoxPreference) findPreference(Settings.SHOW_AVATAR);
 		showAvatar.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			public boolean onPreferenceClick(Preference preference) {
 				updateSettings();
 				return false;
 			}
 		});
 		setValues();
 	}
 
 	protected void updateSettings() {
 		Settings settings = Settings.getInstance();
 		Editor editor = settings.getSharedPreferences(this).edit();
 		editor.putBoolean(Settings.AUTOMATIC_UPDATE, automaticUpdate.isChecked());
 		editor.putString(Settings.UPDATE_INTERVAL, updateInterval.getValue());
 		editor.putString(Settings.FILTER, filter.getText());
 		editor.putBoolean(Settings.SHOW_AVATAR, showAvatar.isChecked());
 		if (editor.commit()) {
 			settings.callObservers();
 		}
 	}
 
 	public void settingsHasChanged(Settings settings) {
 		setValues();
 	}
 
 	public void setValues() {
 		Settings settings = Settings.getInstance();
 		setTitle(getString(R.string.preferencesType).replaceAll("%s", settings.getSettingsName(this)));
 		SharedPreferences preferences = settings.getSharedPreferences(this);
		updateInterval.setEnabled(automaticUpdate.isChecked());
 		automaticUpdate.setChecked(preferences.getBoolean(Settings.AUTOMATIC_UPDATE, Settings.AUTOMATIC_UPDATE_DEFAULT));
 		updateInterval.setValue(preferences.getString(Settings.UPDATE_INTERVAL, Settings.UPDATE_INTERVAL_DEFAULT));
 		filter.setText(preferences.getString(Settings.FILTER, ""));
 		showAvatar.setChecked(preferences.getBoolean(Settings.SHOW_AVATAR, Settings.SHOW_AVATAR_DEFAULT));
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Settings.getInstance().removeObserver(this);
 		automaticUpdate.setOnPreferenceClickListener(null);
 		updateInterval.setOnPreferenceClickListener(null);
 		filter.setDialogPreferenceListener(null);
 		filterDelete.setDialogPreferenceListener(null);
 		showAvatar.setOnPreferenceClickListener(null);
 	}
 }
