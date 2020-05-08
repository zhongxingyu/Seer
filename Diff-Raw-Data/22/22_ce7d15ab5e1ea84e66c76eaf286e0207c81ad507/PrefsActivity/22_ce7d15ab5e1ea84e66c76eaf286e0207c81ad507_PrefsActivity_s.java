 /*
  * lo-lo Copyright (C) 091 Labs 2012 info@091labs.com
  *
  * lo-lo is free software: you can
  * redistribute it and/or modify it under the terms
  * of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later
  * version.
 
  * lo-lo is distributed in the hope that it
  * will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
 
  * You should have received a copy of the GNU
  * General Public License along with this program.
  * If not, see http://www.gnu.org/licenses.
  */
 
 package com.codeskraps.lolo;
 
 import android.appwidget.AppWidgetManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 
 public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener,
 		OnPreferenceClickListener {
 	private static final String TAG = PrefsActivity.class.getSimpleName();
 
 	private SharedPreferences prefs = null;
 	private ListPreference lstOnClick = null;
 	private EditTextPreference eURL = null;
 	private CheckBoxPreference chkSync = null;
 	private CheckBoxPreference chk24 = null;
 	private ListPreference lstInterval = null;
 
 	private String[] entries_OnClick = null;
 	private String[] entries_Interval = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.xml.preferences);
 		setContentView(R.layout.prefs);
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(this);
 
 		entries_OnClick = getResources().getStringArray(R.array.OnClick_entries);
 		entries_Interval = getResources().getStringArray(R.array.interval_entries);
 
 		lstOnClick = (ListPreference) findPreference(Constants.ONCLICK);
 		eURL = (EditTextPreference) findPreference(Constants.EURL);
 		chkSync = (CheckBoxPreference) findPreference(Constants.SHOW_SYNC);
 		chk24 = (CheckBoxPreference) findPreference(Constants.HOUR24);
 		lstInterval = (ListPreference) findPreference(Constants.INTERVAL);
 		Preference prefAbout = (Preference) findPreference(Constants.ABOUT);
 
 		prefAbout.setOnPreferenceClickListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		prefs.registerOnSharedPreferenceChangeListener(this);
 
 		String lstSync = prefs.getString(Constants.LAST_SYNC, null);
 		if (lstSync == null) chkSync.setSummary(getString(R.string.prefsSync_summarNot));
 		else chkSync.setSummary(lstSync);
 
 		boolean hour24 = prefs.getBoolean(Constants.HOUR24, true);
 		if (hour24) chk24.setSummary(getString(R.string.prefs24_summaryTwo));
 		else chk24.setSummary(getString(R.string.prefs24_summaryOne));
 
 		String onClick = prefs.getString(Constants.ONCLICK, "0");
 		int action = Integer.parseInt(onClick);
 		lstOnClick.setSummary(entries_OnClick[action]);
 
 		String url = prefs.getString(Constants.EURL, getString(R.string.prefsURL_default));
 		String urlSummary = String.format("%s %s", getString(R.string.prefsURL_summary), url);
 		eURL.setSummary(urlSummary);
 
 		String intervalString = prefs.getString(Constants.INTERVAL, "1");
 		int interval = Integer.parseInt(intervalString);
 		lstInterval.setSummary(entries_Interval[interval]);
 
 		if (action != 3) eURL.setEnabled(false);
 
 		if (prefs.getBoolean(Constants.FIRST_LAUNCH, true)) {
 			startActivity(new Intent(this, AboutActivity.class));
 			SharedPreferences.Editor editor = prefs.edit();
 			editor.putBoolean(Constants.FIRST_LAUNCH, false);
 			editor.commit();
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
 		super.onPause();
 		prefs.unregisterOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		if (BuildConfig.DEBUG) Log.d(TAG, "onSharedPreferenceChanged");
 
 		if (key.equals(Constants.HOUR24)) {
			boolean hour24 = prefs.getBoolean(Constants.HOUR24, true);
			if (hour24) chk24.setSummary(getString(R.string.prefs24_summaryTwo));
 			else chk24.setSummary(getString(R.string.prefs24_summaryOne));
 
 		} else if (key.equals(Constants.ONCLICK)) {
 			String onClick = prefs.getString(Constants.ONCLICK, "0");
 			int action = Integer.parseInt(onClick);
 			lstOnClick.setSummary(entries_OnClick[action]);
 
 			if (action != 3) eURL.setEnabled(false);
 			else eURL.setEnabled(true);
 
 		} else if (key.equals(Constants.EURL)) {
 			String url = prefs.getString(Constants.EURL, getString(R.string.prefsURL_default));
 			String urlSummary = String.format("%s %s", getString(R.string.prefsURL_summary), url);
 			eURL.setSummary(urlSummary);
 
 		} else if (key.equals(Constants.INTERVAL)) {
 			String intervalString = prefs.getString(Constants.INTERVAL, "1");
 			int interval = Integer.parseInt(intervalString);
 			lstInterval.setSummary(entries_Interval[interval]);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (BuildConfig.DEBUG) Log.d(TAG, "onKeyDown");
 
 		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
 				&& keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			// Take care of calling this method on earlier versions of
 			// the platform where it doesn't exist.
 			Log.d(TAG, "SDK < Eclair");
 			onBackPressed();
 		}

 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public void onBackPressed() {
 		// This will be called either automatically for you on 2.0
 		// or later, or by the code above on earlier versions of the
 		// platform.
 		if (BuildConfig.DEBUG) Log.d(TAG, "onBackPressed");
 		resultIntent();
 		finish();
 		return;
 	}
 
 	private void resultIntent() {
 		if (BuildConfig.DEBUG) Log.d(TAG, "resultIntent");
 
 		if (Constants.CONFIGURE_ACTION.equals(getIntent().getAction())) {
 			Intent intent = getIntent();
 			Bundle extras = intent.getExtras();
 			if (extras != null) {
 				int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
 						AppWidgetManager.INVALID_APPWIDGET_ID);
 				Intent result = new Intent();
 
 				result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
 				setResult(RESULT_OK, result);
 			}
 		}
 		sendBroadcast(new Intent(Constants.FORCE_WIDGET_UPDATE));
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference pref) {
		if (pref.getKey().equals(Constants.ABOUT)) {
 			startActivity(new Intent(this, AboutActivity.class));
		}
		return false;
 	}
 }
