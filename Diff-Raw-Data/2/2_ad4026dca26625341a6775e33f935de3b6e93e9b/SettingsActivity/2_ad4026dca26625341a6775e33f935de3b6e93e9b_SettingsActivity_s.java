 /*
  * BerryMotes android 
  * Copyright (C) 2013 Daniel Triendl <trellmor@trellmor.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.trellmor.berrymotes;
 
 import com.trellmor.berrymotes.sync.SyncUtils;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences.Editor;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.view.MenuItem;
 
 /**
  * A {@link PreferenceActivity} that presents a set of application settings. On
  * handset devices, settings are presented as a single list. On tablets,
  * settings are split by category, with category headers shown to the left of
  * the list of settings.
  * <p>
  * See <a href="http://developer.android.com/design/patterns/settings.html">
  * Android Design: Settings</a> for design guidelines and the <a
  * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
  * API Guide</a> for more information on developing a Settings UI.
  */
 public class SettingsActivity extends PreferenceActivity {
 	public final static String KEY_SHOW_NSFW = "show_nsfw";
 	public final static String KEY_SYNC_CONNECTION = "sync_connection";
 	public final static String KEY_SYNC_NSFW = "sync_nsfw";
 	public final static String KEY_SYNC_FREQUENCY = "sync_frequency";
 	public final static String KEY_SYNC_LAST_MODIFIED = "sync_last_modified";
 	public final static String KEY_SYNC_SUBREDDITS = "sync_subreddits";
 
 	public final static String VALUE_SYNC_CONNECTION_WIFI = "wifi";
 	public final static String VALUE_SYNC_CONNECTION_ALL = "all";
 	
 	public final static String DEFALT_SYNC_SUBREDDITS = "#ALL";
 	public final static String SEPERATOR_SYNC_SUBREDDITS = ";";
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 
 		setupSimplePreferencesScreen();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Shows the simplified settings UI if the device configuration if the
 	 * device configuration dictates that a simplified, single-pane UI should be
 	 * shown.
 	 */
 	@SuppressWarnings("deprecation")
 	private void setupSimplePreferencesScreen() {
 		// In the simplified UI, fragments are not used at all and we instead
 		// use the older PreferenceActivity APIs.

 		// Add 'general' preferences.
 		addPreferencesFromResource(R.xml.pref_general);
 
 		// Add 'data and sync' preferences, and a corresponding header.
 		PreferenceCategory fakeHeader = new PreferenceCategory(this);
 		fakeHeader.setTitle(R.string.pref_header_data_sync);
 		getPreferenceScreen().addPreference(fakeHeader);
 		addPreferencesFromResource(R.xml.pref_data_sync);
 
 		bindPreferenceSummaryToValue(findPreference(KEY_SYNC_FREQUENCY));
 		bindPreferenceSummaryToValue(findPreference(KEY_SYNC_CONNECTION));
 		findPreference(KEY_SYNC_NSFW).setOnPreferenceChangeListener(
 				sResyncListener);
 		findPreference(KEY_SYNC_SUBREDDITS).setOnPreferenceChangeListener(sResyncListener);
 	}
 
 	/**
 	 * A preference value change listener that updates the preference's summary
 	 * to reflect its new value.
 	 */
 	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object value) {
 			String stringValue = value.toString();
 
 			if (preference instanceof ListPreference) {
 				if (preference.getKey().equals(KEY_SYNC_CONNECTION)) {
 					if (stringValue
 							.equals(SettingsActivity.VALUE_SYNC_CONNECTION_ALL)) {
 						preference
 								.setSummary(R.string.pref_description_sync_connection_all);
 					} else {
 						preference
 								.setSummary(R.string.pref_description_sync_connection_wifi);
 
 						// Stop current sync
 						SyncUtils.cancelSync();
 					}
 				} else {
 					// For list preferences, look up the correct display value
 					// in the preference's 'entries' list.
 					ListPreference listPreference = (ListPreference) preference;
 					int index = listPreference.findIndexOfValue(stringValue);
 
 					// Set the summary to reflect the new value.
 					preference.setSummary(index >= 0 ? listPreference
 							.getEntries()[index] : null);
 				}
 			} else {
 				// For all other preferences, set the summary to the value's
 				// simple string representation.
 				preference.setSummary(stringValue);
 			}
 
 			if (preference.getKey().equals(SettingsActivity.KEY_SYNC_FREQUENCY)) {
 				// Adjust sync frequency
 				SyncUtils.setSyncFrequency(Integer.parseInt(stringValue));
 			}
 			return true;
 		}
 	};
 
 	/**
 	 * Binds a preference's summary to its value. More specifically, when the
 	 * preference's value is changed, its summary (line of text below the
 	 * preference title) is updated to reflect the value. The summary is also
 	 * immediately updated upon calling this method. The exact display format is
 	 * dependent on the type of preference.
 	 * 
 	 * @see #sBindPreferenceSummaryToValueListener
 	 */
 	private static void bindPreferenceSummaryToValue(Preference preference) {
 		// Set the listener to watch for value changes.
 		preference
 				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 
 		// Trigger the listener immediately with the preference's
 		// current value.
 		sBindPreferenceSummaryToValueListener.onPreferenceChange(
 				preference,
 				PreferenceManager.getDefaultSharedPreferences(
 						preference.getContext()).getString(preference.getKey(),
 						""));
 	}
 
 	/**
 	 * A preference value change listener that clears the last sync date
 	 */
 	private static Preference.OnPreferenceChangeListener sResyncListener = new Preference.OnPreferenceChangeListener() {
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object value) {
 			clearLastModified(preference.getContext());
 			SyncUtils.cancelSync();
 			return true;
 		}
 
 		private void clearLastModified(Context context) {
 			Editor editor = PreferenceManager.getDefaultSharedPreferences(
 					context).edit();
 			editor.remove(KEY_SYNC_LAST_MODIFIED);
 			editor.commit();
 		}
 	};
 }
