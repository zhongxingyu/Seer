 package com.lacike.ciphertools;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 @SuppressWarnings("deprecation")
 public class SettingsActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	public static final String THEME = "theme";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		TypedArray themes = getResources().obtainTypedArray(R.array.themes_id);
 		int themeId = themes.getResourceId(SettingsActivity.getTheme(this),
 				R.style.AppTheme);
 		setTheme(themeId);
 
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.settings);
 
 		SharedPreferences sharedPreferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals(THEME)) {
 			Intent intent = new Intent(this, SettingsActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 		}
 	}
 
 	public static int getTheme(Context context) {
 		SharedPreferences sp = PreferenceManager
 				.getDefaultSharedPreferences(context);
		return Integer.valueOf(sp.getString(THEME, "0"));
 	}
 
 }
