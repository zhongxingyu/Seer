 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Refract.
  *
  * Refract is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Refract is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Refract. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.refract.activity;
 
 import com.ijuru.refract.R;
 import com.ijuru.refract.utils.Utils;
 
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.widget.Toast;
 
 /**
  * Activity for application preferences
  */
 public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {
 
 	private ListPreference iterFunctionPref, paletteMappingPref;
 	private EditTextPreference itersPerFramePref, paletteSizePref;
 	
 	/**
 	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		
 		iterFunctionPref = (ListPreference)getPreferenceScreen().findPreference("iterfunction");
 		iterFunctionPref.setOnPreferenceChangeListener(this);
 		
 		itersPerFramePref = (EditTextPreference)getPreferenceScreen().findPreference("itersperframe");
 		itersPerFramePref.setOnPreferenceChangeListener(this);
 		
 		paletteMappingPref = (ListPreference)getPreferenceScreen().findPreference("palettemapping");
		iterFunctionPref.setOnPreferenceChangeListener(this);
 		
 		paletteSizePref = (EditTextPreference)getPreferenceScreen().findPreference("palettesize");
 		paletteSizePref.setOnPreferenceChangeListener(this);
 		
 		updatePreferenceSummary(iterFunctionPref, iterFunctionPref.getValue());
 		updatePreferenceSummary(itersPerFramePref, itersPerFramePref.getText());
 		updatePreferenceSummary(paletteMappingPref, paletteMappingPref.getValue());
 		updatePreferenceSummary(paletteSizePref, paletteSizePref.getText());
 	}
 
 	/**
 	 * @see android.preference.Preference.OnPreferenceChangeListener#onPreferenceChange(Preference, Object)
 	 */
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object value) {
 		Resources res = getResources();
 		
 		if (preference == itersPerFramePref) {
 			if (!validateRange(value, res.getInteger(R.integer.min_itersperframe), res.getInteger(R.integer.max_itersperframe)))
 				return false;
 		} 
 		else if (preference == paletteSizePref) {
 			if (!validateRange(value, res.getInteger(R.integer.min_palettesize), res.getInteger(R.integer.max_palettesize)))
 				return false;
 		}
 		
 		updatePreferenceSummary(preference, value);
 		return true;
 	}
 	
 	/**
 	 * Updates the summary of the given preference based on the given value (not necessarily the value itself)
 	 * @param preference the preference
 	 * @param value the value
 	 */
 	private void updatePreferenceSummary(Preference preference, Object value) {
 		if (preference instanceof ListPreference) {
 			ListPreference listPref = (ListPreference)preference;
 			CharSequence[] entries = listPref.getEntries();
 			int index = listPref.findIndexOfValue(value.toString());
 			if (index >= 0)
 				listPref.setSummary(entries[index]);
 		}
 		else {
 			preference.setSummary(value.toString());
 		}
 	}
 	
 	/**
 	 * Validates a integer value to check that it is in the given range
 	 * @param value the integer value
 	 * @param min the minimum
 	 * @param max the maximum
 	 * @return true if valid
 	 */
 	private boolean validateRange(Object value, int min, int max) {
 		Integer val = Utils.parseInteger(value.toString());
 		if (val == null || val < min || val > max) {
 			String message = String.format(getString(R.string.err_numberrange), min, max);
 			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		return true;
 	}
 }
