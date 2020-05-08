 package com.luzi82.randomwallpaper;
 
 import java.io.UnsupportedEncodingException;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.widget.Toast;
 
 public class Settings extends PreferenceActivity implements
 		Preference.OnPreferenceChangeListener,
 		SharedPreferences.OnSharedPreferenceChangeListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Load the preferences from an XML resource
 		getPreferenceManager().setSharedPreferencesName(
 				LiveWallpaper.PREFERENCE_NAME);
 		initValue(getPreferenceManager().getSharedPreferences());
 		addPreferencesFromResource(R.xml.preferences);
 		getPreferenceManager().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 
 		// listen refresh
 		Preference refreshPreference = findPreference(REFRESH_PEROID_KEY);
 		refreshPreference.setOnPreferenceChangeListener(this);
 
 		// listen color
 		Preference colorPreference = findPreference(COLOR_KEY);
 		colorPreference.setOnPreferenceChangeListener(this);
 
 		// app ver
 		Preference appVerPreference = findPreference("preference_info_about_version");
 		PackageManager pm = getPackageManager();
 		String pn = getPackageName();
 		String appVerString = "unknown";
 		try {
 			PackageInfo pi = pm.getPackageInfo(pn, 0);
 			appVerString = String.format("%1$s (%2$d)", pi.versionName,
 					pi.versionCode);
 		} catch (NameNotFoundException e1) {
 			e1.printStackTrace();
 		}
 		appVerPreference.setSummary(appVerString);
 
 		// jni ver
 		Preference jniVerPreference = findPreference("preference_info_about_lib_version");
 		byte[] buf = new byte[15];
 		LiveWallpaper.getVersion(buf);
 		String libVer = "unknown";
 		try {
 			libVer = new String(buf, "ASCII");
 		} catch (UnsupportedEncodingException e) {
 		}
 		jniVerPreference.setSummary(libVer);
 
 		// email intent
 		Preference emailPreference = findPreference("preference_info_about_email");
 		Intent emailIntent = new Intent(Intent.ACTION_SEND);
 		emailIntent.setType("message/rfc882");
 		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getResources()
 				.getString(R.string.preference_info_about_email_address) });
 		emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(
 				R.string.app_name));
 		emailPreference.setIntent(Intent.createChooser(emailIntent,
 				emailPreference.getTitle()));
 
 		// update stuff
 		resetRefreshPeriodPreferenceSummary();
 		resetColorPreferenceSummary();
 	}
 
 	static public final String REFRESH_PEROID_KEY = "preference_setting_refreshperiod";
 	static public final int REFRESH_PEROID_DEFAULT_VALUE = 100;
 	static public final String REFRESH_PEROID_DEFAULT_VALUE_STRING = String
 			.valueOf(REFRESH_PEROID_DEFAULT_VALUE);
	static public final int REFRESH_PEROID_VALUE_MIN = 50;
 	static public final int REFRESH_PEROID_VALUE_MINX = 1;
 	static public final int REFRESH_PEROID_VALUE_MAX = 99999999;
 
 	static public final String REFRESH_PEROID_UNLOCK_KEY = "preference_setting_refreshperiod_unlock";
 
 	static public final String COLOR_KEY = "preference_setting_color";
 	static public final int COLOR_DEFAULT_VALUE = 0;
 	static public final String COLOR_DEFAULT_VALUE_STRING = "00000000";
 
 	static public final String COLOR_CHEAT_KEY = "preference_setting_color_cheat";
 
 	static public void initValue(SharedPreferences sp) {
 		SharedPreferences.Editor editor = sp.edit();
 		if (!sp.contains(REFRESH_PEROID_KEY)) {
 			editor.putString(REFRESH_PEROID_KEY,
 					REFRESH_PEROID_DEFAULT_VALUE_STRING);
 		}
 		if (!sp.contains(COLOR_KEY)) {
 			editor.putString(COLOR_KEY, COLOR_DEFAULT_VALUE_STRING);
 		}
 		editor.commit();
 	}
 
 	static public int getRefreshPeroid(SharedPreferences sp) {
 		try {
 			String retString = sp.getString(Settings.REFRESH_PEROID_KEY,
 					REFRESH_PEROID_DEFAULT_VALUE_STRING);
 			return Integer.parseInt(retString);
 		} catch (Throwable t) {
 			return REFRESH_PEROID_DEFAULT_VALUE;
 		}
 	}
 
 	static public boolean getColorCheat(SharedPreferences sp) {
 		return sp.getBoolean(COLOR_CHEAT_KEY, false);
 	}
 
 	static public int getColor(SharedPreferences sp) {
 		try {
 			String retString = getColorString(sp).toLowerCase();
 			int retInt = (int) (Long.parseLong(retString, 16));
 			int alpha = (retInt >> 24) & 0xff;
 			int red = (retInt >> 16) & 0xff;
 			int green = (retInt >> 8) & 0xff;
 			int blue = (retInt >> 0) & 0xff;
 			int color = Color.argb(alpha, red, green, blue);
 			return color;
 		} catch (Throwable t) {
 			return COLOR_DEFAULT_VALUE;
 		}
 	}
 
 	static public String getColorString(SharedPreferences sp) {
 		try {
 			String retString = sp.getString(Settings.COLOR_KEY,
 					COLOR_DEFAULT_VALUE_STRING);
 			return retString;
 		} catch (Throwable t) {
 			return COLOR_DEFAULT_VALUE_STRING;
 		}
 	}
 
 	static public boolean getRefreshPeroidUnlock(SharedPreferences sp) {
 		return sp.getBoolean(REFRESH_PEROID_UNLOCK_KEY, false);
 	}
 
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
 		if (preference.getKey().equals(REFRESH_PEROID_KEY)) {
 			try {
 				String valueStr = (String) newValue;
 
 				// unlock stuff
 				if (valueStr
 						.equals(getResources()
 								.getString(
 										R.string.preference_setting_refreshperiod_dialog_cheatcode))) {
 					SharedPreferences.Editor ed = getPreferenceManager()
 							.getSharedPreferences().edit();
 					ed.putBoolean(REFRESH_PEROID_UNLOCK_KEY, true);
 					ed.commit();
 					Toast
 							.makeText(
 									this,
 									R.string.preference_setting_refreshperiod_dialog_cheatenable,
 									Toast.LENGTH_SHORT).show();
 				}
 
 				int valueInt = Integer.parseInt(valueStr);
 				if (valueInt > REFRESH_PEROID_VALUE_MAX) {
 					Toast
 							.makeText(
 									this,
 									getResources()
 											.getString(
 													R.string.preference_setting_refreshperiod_dialog_error_less,
 													REFRESH_PEROID_VALUE_MAX),
 									Toast.LENGTH_SHORT).show();
 					return false;
 				}
 				int min = getRefreshPeroidUnlock(preference
 						.getSharedPreferences()) ? REFRESH_PEROID_VALUE_MINX
 						: REFRESH_PEROID_VALUE_MIN;
 				if (valueInt < min) {
 					Toast
 							.makeText(
 									this,
 									getResources()
 											.getString(
 													R.string.preference_setting_refreshperiod_dialog_error_more,
 													min), Toast.LENGTH_SHORT)
 							.show();
 					return false;
 				}
 			} catch (Throwable t) {
 				Toast
 						.makeText(
 								this,
 								R.string.preference_setting_refreshperiod_dialog_error_value,
 								Toast.LENGTH_SHORT).show();
 				return false;
 			}
 			return true;
 		} else if (preference.getKey().equals(COLOR_KEY)) {
 			try {
 				String valueStr = (String) newValue;
 
 				// random
 				if (valueStr.equals(getResources().getString(
 						R.string.preference_setting_color_dialog_cheatcode))) {
 					SharedPreferences.Editor ed = getPreferenceManager()
 							.getSharedPreferences().edit();
 					ed.putBoolean(COLOR_CHEAT_KEY, true);
 					ed.commit();
 					return false;
 				}
 				SharedPreferences.Editor ed = getPreferenceManager()
 						.getSharedPreferences().edit();
 				ed.putBoolean(COLOR_CHEAT_KEY, false);
 				ed.commit();
 
 				if (valueStr.length() != 8) {
 					Toast
 							.makeText(
 									this,
 									R.string.preference_setting_color_dialog_error_format,
 									Toast.LENGTH_SHORT).show();
 					return false;
 				}
 
 				Long.parseLong(valueStr.toLowerCase(), 16);
 			} catch (Throwable t) {
 				t.printStackTrace();
 				Toast.makeText(this,
 						R.string.preference_setting_color_dialog_error_format,
 						Toast.LENGTH_SHORT).show();
 				return false;
 			}
 			return true;
 		}
 		return true;
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals(REFRESH_PEROID_KEY)) {
 			resetRefreshPeriodPreferenceSummary();
 		}
 		if (key.equals(COLOR_KEY) || key.equals(COLOR_CHEAT_KEY)) {
 			resetColorPreferenceSummary();
 		}
 	}
 
 	public void resetRefreshPeriodPreferenceSummary() {
 		Preference refreshPreference = findPreference(REFRESH_PEROID_KEY);
 		refreshPreference.setSummary(getResources()
 				.getString(
 						R.string.preference_setting_refreshperiod_value_format,
 						getRefreshPeroid(getPreferenceManager()
 								.getSharedPreferences())));
 	}
 
 	public void resetColorPreferenceSummary() {
 		Preference refreshPreference = findPreference(COLOR_KEY);
 		SharedPreferences sp = refreshPreference.getSharedPreferences();
 		if (getColorCheat(sp)) {
 			refreshPreference
 					.setSummary(R.string.preference_setting_color_value_cheat);
 		} else {
 			String colorString = getColorString(sp);
 			refreshPreference.setSummary(getResources().getString(
 					R.string.preference_setting_color_value_format,
 					colorString.toUpperCase()));
 		}
 	}
 
 }
