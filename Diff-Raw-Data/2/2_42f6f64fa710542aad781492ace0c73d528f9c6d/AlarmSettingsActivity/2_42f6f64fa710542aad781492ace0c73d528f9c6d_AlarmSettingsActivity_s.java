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
 
 import net.engio.mbassy.listener.Handler;
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.audio.AudioDriver;
 import se.chalmers.dat255.sleepfighter.audio.AudioDriverFactory;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.AudioChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.Alarm.Field;
 import se.chalmers.dat255.sleepfighter.model.Alarm.MetaChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.preference.MultiSelectListPreference;
 import se.chalmers.dat255.sleepfighter.preference.TimepickerPreference;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.MetaTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.DialogUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class AlarmSettingsActivity extends PreferenceActivity {
 
 	public static final String EXTRA_ALARM_IS_NEW = "alarm_is_new";
 
 	private final String NAME = "pref_alarm_name";
 	private final String TIME = "pref_alarm_time";
 	private final String DAYS = "pref_enabled_days";
 	private final String REPEAT = "pref_alarm_repeat";
 	private final String DELETE = "pref_delete_alarm";
 	private final String VIBRATION = "pref_alarm_vibration";
 	
 	private final String RINGER_SUBSCREEN = "perf_alarm_ringtone";
 	private Preference ringerPreference;
 
 	private Alarm alarm;
 	private AlarmList alarmList;
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= 11) {
 			ActionBar actionBar = getActionBar();
 		    // add the custom view to the action bar
 		    actionBar.setCustomView(R.layout.alarm_settings_actionbar);
 
 		    View customView = actionBar.getCustomView();
 
 		    EditText edit_title_field = (EditText) customView.findViewById(R.id.alarm_edit_title_field);
 		    edit_title_field.setText(MetaTextUtils.printAlarmName(this, alarm));
 		    edit_title_field.setOnEditorActionListener(new OnEditorActionListener() {
 				@Override
 				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 					alarm.setName(v.getText().toString());
 					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 					return false;
 				}
 		    });
 		    edit_title_field.clearFocus();
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM);
 
 			CompoundButton activatedSwitch = (CompoundButton) customView.findViewById( R.id.alarm_actionbar_toggle );
 			activatedSwitch.setChecked( this.alarm.isActivated() );
 			activatedSwitch.setOnCheckedChangeListener( new OnCheckedChangeListener() {
 				@Override
 				public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
 					alarm.setActivated( isChecked );
 				}
 			} );
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu ) {
		this.getMenuInflater().inflate( R.menu.alarm_settings_menu, menu );
 		return true;
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Handler
 	public void handleNameChange(MetaChangeEvent e) {
 		if (e.getModifiedField() == Field.NAME) {
 			String name = MetaTextUtils.printAlarmName(this, e.getAlarm());
 			findPreference(NAME).setSummary(name);
 			
 			if (Build.VERSION.SDK_INT >= 11) {
 				((EditText)this.getActionBar().getCustomView().findViewById(R.id.alarm_edit_title_field)).setText(name);
 			}
 		}
 	}
 
 	private void removeDeleteButton() {
 		Preference pref = (Preference) findPreference(DELETE);
 		PreferenceCategory category = (PreferenceCategory) findPreference("pref_category_misc");
 		category.removePreference(pref);		
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		alarmList = ((SFApplication) getApplication()).getAlarms();
 		((SFApplication) getApplication()).getBus().subscribe(this);
 		
 		if( new IntentUtils( this.getIntent() ).isSettingPresetAlarm()) {
 			alarm = alarmList.getPresetAlarm();
 		}else{
 			final int id = new IntentUtils( this.getIntent() ).getAlarmId();
 			alarm = alarmList.getById(id);
 		}
 			
 		this.setTitle(MetaTextUtils.printAlarmName(this, alarm));
 
 		setupActionBar();
 		setupSimplePreferencesScreen();
 		
 		if(alarm.isPresetAlarm()) {
 			// having a delete button for the presets alarm makes no sense, so remove it. 
 			removeDeleteButton();			
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.alarm_settings_action_remove:
 			this.deleteAlarm();
 			break;
 
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	// Using deprecated methods because we need to support Android API level 8
 	@SuppressWarnings("deprecation")
 	private void setupSimplePreferencesScreen() {
 		addPreferencesFromResource(R.xml.pref_alarm_general);
 
 		bindPreferenceSummaryToValue(findPreference(TIME));
 		bindPreferenceSummaryToValue(findPreference(NAME));
 		bindPreferenceSummaryToValue(findPreference(DAYS));
 		bindPreferenceSummaryToValue(findPreference(REPEAT));
 		bindPreferenceSummaryToValue(findPreference(VIBRATION));
 		
 		findPreference(DELETE).setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				deleteAlarm();
 				return true;
 			}
 		});
 
 		this.setupRingerPreferences();
 	}
 
 	private void deleteAlarm() {
 		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				alarmList.remove(alarm);
 				finish();
 			}
 		};
 		DialogUtils.showConfirmationDialog(
 				getResources().getString(R.string.confirm_delete),
 				AlarmSettingsActivity.this,
 				dialogClickListener);
 	}
 
 	private void setupRingerPreferences() {
 		this.ringerPreference = this.findPreference( RINGER_SUBSCREEN );
 		this.ringerPreference.setOnPreferenceClickListener( new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick( Preference preference ) {
 				startRingerEdit();
 				return true;
 			}
 		} );
 
 		this.updateRingerSummary();
 	}
 
 	private void updateRingerSummary() {
 		AudioDriverFactory factory = SFApplication.get().getAudioDriverFactory();
 		AudioDriver driver = factory.produce( this, this.alarm.getAudioSource() );
 		this.ringerPreference.setSummary( driver.printSourceName() );
 	}
 
 	@Handler
 	public void handleAudioChange( AudioChangeEvent evt ) {
 		if ( evt.getModifiedField() == Field.AUDIO_SOURCE ) {
 			this.updateRingerSummary();
 		}
 	}
 
 	private void startRingerEdit() {
 		Intent intent = new Intent(this, RingerSettingsActivity.class );
 		
 
 		if(this.alarm.isPresetAlarm()) {
 			new IntentUtils( intent ).setSettingPresetAlarm(true);
 		} else
 			new IntentUtils( intent ).setAlarmId( alarm );
 	
 		
 		this.startActivity( intent );
 	}
 
 	@SuppressWarnings( "deprecation" )
 	public Preference findPreference( CharSequence key ) {
 		return super.findPreference( key );
 	}
 
 	private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object value) {
 			String stringValue = value.toString();
 			
 			if (TIME.equals(preference.getKey())) {
 				TimepickerPreference tpPref = (TimepickerPreference) preference;
 				
 				int hour = tpPref.getHour();
 				int minute = tpPref.getMinute();
 				
 				alarm.setTime(hour, minute);
 				
 				preference.setSummary((hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute);
 			}
 			else if (NAME.equals(preference.getKey())) {
 				alarm.setName(stringValue);
 				preference.setSummary(stringValue);
 			}
 			else if(DAYS.equals(preference.getKey())) {
 				alarm.setEnabledDays(((MultiSelectListPreference) preference).getEntryChecked());
 				preference.setSummary(DateTextUtils.makeEnabledDaysText(alarm));	
 			}
 			else if (REPEAT.equals(preference.getKey())) {
 				alarm.setRepeat(("true".equals(stringValue)) ? true : false);
 				Debug.d("setting repeat: " + alarm.isRepeating() );
 				
 			}
 			else if (VIBRATION.equals(preference.getKey())) {
 				alarm.setVibrationEnabled(("true".equals(stringValue)) ? true : false);
 			}
 			
 			return true;
 		}
 	};
 	
 	private void bindPreferenceSummaryToValue(Preference preference) {
 		preference.setPersistent(false);
 		
 		if (NAME.equals(preference.getKey())) {
 			((EditTextPreference) preference).setText(MetaTextUtils.printAlarmName(this, alarm));
 			preference.setSummary(MetaTextUtils.printAlarmName(this, alarm));
 		}
 		else if (TIME.equals(preference.getKey())) {
 			initiateTimePicker((TimepickerPreference)preference);
 			preference.setSummary(alarm.getTimeString());
 		}
 		else if(DAYS.equals(preference.getKey())) {
 			initiateWeekdayPicker((MultiSelectListPreference) preference);
 			preference.setSummary(DateTextUtils.makeEnabledDaysText(alarm));	
 		}
 		else if (REPEAT.equals(preference.getKey())) {
 			
 			((CheckBoxPreference) preference).setChecked(alarm.isRepeating());
 		}
 		else if (VIBRATION.equals(preference.getKey())) {
 			((CheckBoxPreference) preference).setChecked(alarm.getVibrationEnabled());
 		}
 		
 		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 	}
 	
 	private void initiateTimePicker(TimepickerPreference tp) {
 		tp.setHour(alarm.getHour());
 		tp.setMinute(alarm.getMinute());
 	}
 	
 	private void initiateWeekdayPicker(MultiSelectListPreference preference) {
 		preference.setEntryChecked(alarm.getEnabledDays());
 	}
 }
