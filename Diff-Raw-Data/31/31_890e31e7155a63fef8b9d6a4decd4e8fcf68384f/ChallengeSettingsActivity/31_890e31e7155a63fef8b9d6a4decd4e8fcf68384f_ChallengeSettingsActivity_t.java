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
 import se.chalmers.dat255.sleepfighter.android.preference.EnablePlusSettingsPreference;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeFactory;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 /**
  * ChallengeSettingsActivity handles enabling / disabled challenge types<br/>
  * and forwards user for more advanced settings.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 5, 2013
  */
 public class ChallengeSettingsActivity extends PreferenceActivity {
 
 	private Alarm alarm;
 	private ChallengeConfigSet challengeSet;
 
 	private String[] names;
 	private String[] descriptions;
 
 	@Override
 	// Uses non-fragment based preferences
 	@SuppressWarnings("deprecation")
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.alarm = AlarmIntentHelper.fetchAlarmOrPreset( this );
 		this.challengeSet = this.alarm.getChallengeSet();
 
 		addPreferencesFromResource(R.xml.pref_alarm_challenge);
 
 		// Programmatically add Preference entries in category for all
 		// challenges defined in for challenge set.
 		PreferenceCategory pc = (PreferenceCategory) findPreference("pref_challenge_category");
 
 		for ( final ChallengeType type : this.challengeSet.getDefinedTypes() ) {
 			Preference p = makeChallengePreference(type);
 			pc.addPreference(p);
 		}
 	}
 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
 	/**
 	 * Create a Preference for a ChallengeType
 	 * 
 	 * @param type the ChallengeType
 	 * @return a Preference for the ChallengeType
 	 */
 	private Preference makeChallengePreference( final ChallengeType type ) {
 		// Setup L&F of preference.
 		final EnablePlusSettingsPreference preference = new EnablePlusSettingsPreference(this);
 		preference.setPersistent(false);
 		preference.setTitleColor( this.getResources().getColor( R.color.holo_red_light ) );
 
 		// Set name & summary.
 		String name = getName(type);
 		String description = getDescription(type);
 		preference.setTitle(name);
 		preference.setSummary(description);
 
 		// Set enabled/disabled.
 		boolean enabled = this.challengeSet.getConfig(type).isEnabled();
 		preference.setChecked(enabled);
 
 		// Bind listener for when enabled/disabled changes.
 		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				challengeEnabledChanged( type, (Boolean) newValue );
 				return true;
 			}
 		});
 
 		// Bind listener for advanced button click.
 		ChallengePrototypeDefinition def = ChallengeFactory.getPrototypeDefinition( type );
 		if ( def.hasParams() ) {
 			preference.setOnButtonClickListener( new OnClickListener() {
 				@Override
 				public void onClick( View v ) {
 					gotoPreferencesSettings( type );
 				}
 				
 			} );
 		} else {
 			preference.setUseButton( false );
 		}
 
 		return preference;
 	}
 
 	/**
 	 * Changes enabled/disabled for type.
 	 *
 	 * @param type the type to change for.
 	 * @param isEnabled whether to enable/disable.
 	 */
 	protected void challengeEnabledChanged( ChallengeType type, boolean isEnabled ) {
 		this.challengeSet.setEnabled( type, isEnabled );
 	}
 
 	/**
 	 * Moves the user to settings screen for a challenge type.
 	 *
 	 * @param type the challenge type.
 	 */
 	protected void gotoPreferencesSettings( ChallengeType type ) {
 		Intent i = new Intent( this, ChallengeParamsSettingsActivity.class );
 		IntentUtils intentUtils = new IntentUtils(i);
 		if (this.alarm.isPresetAlarm()) {
 			intentUtils.setSettingPresetAlarm(true);
 		} else {
 			intentUtils.setAlarmId(alarm);
 		}
 
 		i.putExtra( ChallengeParamsSettingsActivity.EXTRAS_CHALLENGE_TYPE, type );
 
 		this.startActivity( i );
 	}
 
 	/**
 	 * Gets the localized name for a challenge.
 	 * 
 	 * @param type
 	 *            the ChallengeType
 	 * @return the localized name for the challenge
 	 */
 	private String getName(ChallengeType type) {
 		if (this.names == null) {
 			this.names = getResources().getStringArray(R.array.challenge_names);
 		}
 		int ordinal = type.ordinal();
 		if (ordinal >= this.names.length) {
 			// No name in array!
 			// Return enum name, perhaps should throw exception later
 			return type.name();
 		}
 		return this.names[ordinal];
 	}
 
 	/**
 	 * Gets the localized description for a challenge.
 	 * 
 	 * @param type
 	 *            the ChallengeType
 	 * @return the localized description for the challenge
 	 */
 	private String getDescription(ChallengeType type) {
 		if (this.descriptions == null) {
 			this.descriptions = getResources().getStringArray(
 					R.array.challenge_descriptions);
 		}
 		int ordinal = type.ordinal();
 		if (ordinal >= this.descriptions.length) {
 			// No description in array!
 			// Return empty string, perhaps should throw exception later
 			return "";
 		}
 		return this.descriptions[ordinal];
 	}
 }
