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
 import se.chalmers.dat255.sleepfighter.android.resources.ResourcesDynamicUtil;
 import se.chalmers.dat255.sleepfighter.android.utils.ActivityUtils;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeFactory;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeParamsReadWriter;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition.ParameterDefinition;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition.PrimitiveValueType;
 import se.chalmers.dat255.sleepfighter.helper.AlarmIntentHelper;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.utils.string.StringUtils;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.view.MenuItem;
 
 /**
  * ChallengeParamsSettingsActivity is the activity for configuring the parameters/settings for one ChallengeType.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 4, 2013
  */
 public class ChallengeParamsSettingsActivity extends PreferenceActivity {
 	public static final String EXTRAS_CHALLENGE_TYPE = "challenge_type";
 
 	private Alarm alarm;
 	private ChallengeConfigSet challengeSet;
 
 	private ChallengeParamsReadWriter readWriter;
 
 	private ChallengePrototypeDefinition definition;
 
 	private PreferenceCategory preferenceCategory;
 
 	@TargetApi( Build.VERSION_CODES.HONEYCOMB )
 	private void setupActionBar() {
 		if ( Build.VERSION.SDK_INT >= 11 ) {
 			ActionBar actionBar = this.getActionBar();
 			actionBar.setTitle( this.getActivityTitle() );
 			ActivityUtils.setupStandardActionBar( this );
 		}
 	}
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState ) {
 		super.onCreate( savedInstanceState );
 
 		this.alarm = AlarmIntentHelper.fetchAlarmOrPreset( this );
 		this.challengeSet = this.alarm.getChallengeSet();
 
 		this.fetchDefinition();
 
 		this.readWriter = new ChallengeParamsReadWriter( this.challengeSet, this.definition.getType() );
 
 		this.setupPreferenceCategory();
 
 		for ( ParameterDefinition paramDef : definition.get() ) {
 			this.addPreference( paramDef );
 		}
 		
 		this.setupDependers();
 
 		this.setupActionBar();
 	}

	/**
	 * Sets up any dependencies there might be.
	 */
 	private void setupDependers() {
 		for ( final ParameterDefinition paramDef : definition.get() ) {
 			if(paramDef.getDependers() != null) {
 				
 				final Preference pref = findPreference(paramDef.getKey());
 
 				// Disable the dependent prefs if paramDef has been set to false. 	
 				pref.setOnPreferenceChangeListener(null);
 				pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 					@Override
 					public boolean onPreferenceChange(Preference preference, Object newValue) {
 						handleChange( preference, newValue );
 						final boolean b = (Boolean)newValue;
 
 						for(final String depender : paramDef.getDependers()) {
 							final Preference dependentPref = findPreference(depender);
 							dependentPref.setEnabled(b);
 						}
 						
 
 						return true;
 					}
 				});
 				
 				// set the initial disabled value of the dependers, when you first enter this
 				// preference menu. 
 				for(final String depender : paramDef.getDependers()) {
 					final Preference dependentPref = findPreference(depender);
 					dependentPref.setEnabled(this.readWriter.getBoolean( paramDef.getKey(), (Boolean) paramDef.getDefaultValue() ));
 				}
 				
 			}
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
 	 * Sets up the preference category to add preferences to.
 	 */
 	@SuppressWarnings( "deprecation" )
 	private void setupPreferenceCategory() {
 		this.addPreferencesFromResource(R.xml.pref_alarm_challenge_params);
 		this.preferenceCategory = (PreferenceCategory) this.findPreference("pref_challenge_param_category");
 	}
 
 	/**
 	 * Fetches the challenge type from intent.
 	 */
 	private ChallengeType fetchChallengeType() {
 		ChallengeType type =  (ChallengeType) this.getIntent().getSerializableExtra( EXTRAS_CHALLENGE_TYPE );
 		if ( type == null ) {
 			throw new IllegalArgumentException( "No ChallengeType was supplied!" );
 		}
 
 		return type;
 	}
 
 	/**
 	 * Returns the definition for the handled challenge type.
 	 */
 	private void fetchDefinition() {
 		this.definition = ChallengeFactory.getPrototypeDefinition( this.fetchChallengeType() );
 	}
 
 	/**
 	 * Adds a preference for a given ParameterDefinition.
 	 *
 	 * @param paramDef the parameter definition.
 	 */
 	private void addPreference( ParameterDefinition paramDef ) {
 		Preference preference = this.makePreference( paramDef );
 
 		// Make sure nothing is stored in SharedPreferences
 		preference.setPersistent( false );
 		preference.setKey( paramDef.getKey() );
 
 		// Set title.
 		String name = this.getParamTitle( paramDef );
 		preference.setTitle(name);
 
 		// Set summary if available.
 		String summary = this.getParamSummary( paramDef );
 		if ( summary != null ) {
 			preference.setSummary( summary );
 		}
 
 		
 		// Set listener for change.
 		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
 			@Override
 			public boolean onPreferenceChange(Preference preference, Object newValue) {
 				handleChange( preference, newValue );
 				return true;
 			}
 		});
 
 		this.preferenceCategory.addPreference( preference );
 	}
 
 	/**
 	 * Makes a preference from a ParameterDefinition.
 	 *
 	 * @param paramDef the definition.
 	 * @return the preference.
 	 */
 	private Preference makePreference(  ParameterDefinition paramDef ) {
 		Preference preference = null;
 
 		// Handle the various types.
 		if ( paramDef.getType() instanceof PrimitiveValueType ) {
 			preference = this.makePrimitivePreference( paramDef );
 		}
 
 		return preference;
 	}
 
 	/**
 	 * Handles a change in preference.
 	 *
 	 * @param preference the preference that was changed.
 	 * @param newValue the new value.
 	 */
 	private void handleChange( Preference preference, Object newValue ) {
 		String key = preference.getKey();
 		ParameterDefinition paramDef = this.definition.get( key );
 
 		if ( paramDef.getType() instanceof PrimitiveValueType ) {
 			this.handlePrimitiveChange( key, paramDef, newValue );
 		}
 	}
 	
 	/**
 	 * Creates a preference for primitive value types.
 	 *
 	 * @param paramDef the parameter definition.
 	 * @return the made preference.
 	 */
 	private Preference makePrimitivePreference( ParameterDefinition paramDef ) {
 		Preference preference = null;
 
 		switch ( (PrimitiveValueType) paramDef.getType() ) {
 		case BOOLEAN:
 			preference = new CheckBoxPreference(this);
 			((CheckBoxPreference) preference).setChecked( this.readWriter.getBoolean( paramDef.getKey(), (Boolean) paramDef.getDefaultValue() ) );
 			break;
 
 		// TODO
 		case DOUBLE:
 			break;
 		case FLOAT:
 			break;
 		case INTEGER:
 			break;
 		case LONG:
 			break;
 		case STRING:
 			break;
 		default:
 			break;
 		}
 
 		return preference;
 	}
 
 	/**
 	 * Handles a change for a primitive value type.
 	 *
 	 * @param key the parameter key.
 	 * @param paramDef the parameter definition.
 	 * @param newValue the new value.
 	 */
 	private void handlePrimitiveChange( String key, ParameterDefinition paramDef, Object newValue ) {
 		switch ( (PrimitiveValueType) paramDef.getType() ) {
 		case BOOLEAN:
 			this.readWriter.setBoolean( key, (Boolean) newValue ); 
 			break;
 
 		// TODO
 		case DOUBLE:
 			break;
 		case FLOAT:
 			break;
 		case INTEGER:
 			break;
 		case LONG:
 			break;
 		case STRING:
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Returns the activity title to use from string values.
 	 *
 	 * @return the title.
 	 */
 	private String getActivityTitle() {
 		return getChallengeString( "settings_title", true );
 	}
 
 	/**
 	 * Returns the title for the parameter from string values.
 	 *
 	 * @param paramDef the parameter definition.
 	 * @return the title.
 	 */
 	private String getParamTitle( ParameterDefinition paramDef ) {
 		return getChallengeString( paramDef.getKey() + "_title", true );
 	}
 
 	/**
 	 * Returns the summary for the parameter from string values.
 	 *
 	 * @param paramDef the parameter definition.
 	 * @return the summary.
 	 */
 	private String getParamSummary( ParameterDefinition paramDef ) {
 		return getChallengeString( paramDef.getKey() + "_summary", false );
 	}
 
 	/**
 	 * Returns a string from resources for given challenge type & a case_string string needed.
 	 *
 	 * @param case_string the case as a string, e.g "title", "summary", etc.
 	 * @param checked whether or not to throw exception if string wasn't available.
 	 * @return the string.
 	 */
 	private String getChallengeString( String case_string, boolean checked ) {
 		String name = "challenge_" + StringUtils.castLower( this.definition.getType().toString() ) + "_" + case_string;
 
 		return checked ? ResourcesDynamicUtil.getResourceStringCheck( name, this ) : ResourcesDynamicUtil.getResourceString( name, this );
 	}

	@SuppressWarnings( "deprecation" )
	public Preference findPreference( CharSequence key ) {
		return super.findPreference( key );
	}
 }
