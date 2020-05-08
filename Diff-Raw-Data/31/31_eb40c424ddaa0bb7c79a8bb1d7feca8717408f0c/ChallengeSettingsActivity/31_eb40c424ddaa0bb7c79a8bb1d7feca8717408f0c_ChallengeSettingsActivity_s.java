 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.activity;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeType;
import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.CheckBoxPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 
 public class ChallengeSettingsActivity extends PreferenceActivity {
	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.pref_alarm_challenge);
 		PreferenceCategory pc = (PreferenceCategory) findPreference("pref_challenge_category");

 		ChallengeType[] types = ChallengeType.values();
 		for (final ChallengeType type : types) {
 			final CheckBoxPreference p = new CheckBoxPreference(this);
 			p.setPersistent(false);
 			p.setTitle(type.name());
 			p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
 				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					Debug.d(type.name() + " " + p.isChecked());
 					return true;
 				}
 			});
 		}
 	}
 }
