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
 
 import java.util.Locale;
 
 import net.engio.mbassy.listener.Handler;
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.android.preference.EnablePlusSettingsPreference;
 import se.chalmers.dat255.sleepfighter.android.preference.MultiSelectListPreference;
 import se.chalmers.dat255.sleepfighter.android.preference.NumberPickerDialogPreference;
 import se.chalmers.dat255.sleepfighter.android.preference.TimepickerPreference;
 import se.chalmers.dat255.sleepfighter.android.preference.VolumePreference;
 import se.chalmers.dat255.sleepfighter.android.utils.DialogUtils;
 import se.chalmers.dat255.sleepfighter.audio.AudioDriver;
 import se.chalmers.dat255.sleepfighter.audio.AudioDriverFactory;
 import se.chalmers.dat255.sleepfighter.helper.AlarmIntentHelper;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.AudioChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.Alarm.Field;
 import se.chalmers.dat255.sleepfighter.model.Alarm.MetaChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.speech.SpeechLocalizer;
 import se.chalmers.dat255.sleepfighter.speech.TextToSpeechUtil;
 import se.chalmers.dat255.sleepfighter.text.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.text.MetaTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceScreen;
 import android.speech.tts.TextToSpeech;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 /**
  * Contains preferences for specific alarms.
  * 
  * @author Hassel
  *
  */
 public class AlarmSettingsActivity extends PreferenceActivity {
 
 	public static final String EXTRA_ALARM_IS_NEW = "alarm_is_new";
 
 	private static final String NAME = "pref_alarm_name";
 	private static final String TIME = "pref_alarm_time";
 	private static final String DAYS = "pref_enabled_days";
 	private static final String REPEAT = "pref_alarm_repeat";
 	private static final String DELETE = "pref_delete_alarm";
 	private static final String VIBRATION = "pref_alarm_vibration";	
 	private static final String RINGER_SUBSCREEN = "perf_alarm_ringtone";
 	private static final String CHALLENGE = "pref_challenge";
 	private static final String VOLUME = "pref_volume";
 	private static final String ENABLE_SNOOZE = "pref_alarm_snooze_enabled";
 	private static final String SNOOZE_TIME = "pref_alarm_snooze_time";
 	
 	private static final String SPEECH = "pref_alarm_speech";
 	private static final String SPEECH_SAMPLE = "pref_speech_sample";
 	
 	private static final String FLASH = "pref_alarm_flash_enabled";
 	
 	private Preference ringerPreference;
 
 	private Alarm alarm;
 	private AlarmList alarmList;
 
 	private SFApplication app() {
 		return SFApplication.get();
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= 11) {
 			ActionBar actionBar = getActionBar();
 		    // add the custom view to the action bar
 		    actionBar.setCustomView(R.layout.alarm_settings_actionbar);
 
 		    View customView = actionBar.getCustomView();
 
 		    // Setup name field.
 		    getActionBar().getCustomView().findViewById(R.id.global_alarm_hidden_title).setVisibility(View.INVISIBLE);
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
 
 			// Remove the Name preference... no need for duplicate, just looks ugly.
 			this.removeEditName();
 
 			
 			// Setup activated switch.
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
 	protected void onResume() {
 		super.onResume();
 		// Check if an alarm is ringing, if so, send the user to AlarmActivity
 		Alarm ringingAlarm = SFApplication.get().getRingingAlarm();
 		if (ringingAlarm != null) {
 			Intent i = new Intent(this, AlarmActivity.class);
 			new AlarmIntentHelper(i).setAlarmId(ringingAlarm);
 			startActivity(i);
 			finish();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu ) {
 		this.getMenuInflater().inflate(R.menu.alarm_settings_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		// If this is the preset alarm, the options menu won't be shown, only
 		// removing the "delete" menu might be appropriate if more options are
 		// added
 		if(this.alarm.isPresetAlarm()) {
 			return false;
 		}
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
 	
 	@Handler
 	public void handleRingerChange(AudioChangeEvent e) {
 		if (e.getModifiedField() == Field.AUDIO_SOURCE) {
 			updateRingerSummary();
 		}
 	}
 	
 	private void removeFlashLightPref() {
 		Preference pref = (Preference) findPreference(FLASH);
 		PreferenceCategory category = (PreferenceCategory) findPreference("pref_category_misc");
 		category.removePreference(pref);
 	}
 	
 	private void removeMiscCategoryIfEmpty() {
 		PreferenceCategory category = (PreferenceCategory) findPreference("pref_category_misc");
 		
 		if(category.getPreferenceCount() == 0) {
 			PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("alarm_preference_screen");
 			preferenceScreen.removePreference(category);
 		}
 	}
 
 	private void removeDeleteButton() {
 		Preference pref = (Preference) findPreference(DELETE);
 		PreferenceCategory category = (PreferenceCategory) findPreference("pref_category_misc");
 		category.removePreference(pref);		
 	}
 	
 	private void removeEditName() {
 		this.removeDecendantOfScreen( findPreference(NAME) );
 	}
 
 	@SuppressWarnings( "deprecation" )
 	private void removeDecendantOfScreen( Preference pref ) {
 		if ( pref != null ) {
 			this.getPreferenceScreen().removePreference( pref );
 		}
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void removeEditTitle() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().getCustomView().findViewById(R.id.alarm_edit_title_field).setVisibility(View.INVISIBLE);
 			
 			getActionBar().getCustomView().findViewById(R.id.global_alarm_hidden_title).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void removeAlarmToggle() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().getCustomView().findViewById(R.id.alarm_actionbar_toggle).setVisibility(View.INVISIBLE);
 		}
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		TextToSpeechUtil.checkTextToSpeech(this);
 		
 		alarmList = app().getAlarms();
 		app().getBus().subscribe(this);
 
 		this.alarm = AlarmIntentHelper.fetchAlarmOrPreset( this );
 
 		this.setTitle(MetaTextUtils.printAlarmName(this, alarm));
 
 		setupSimplePreferencesScreen();
 
 		setupActionBar();
 
 		
 		// remove the flash light setting of the device doesn't support it. 
		if(this.deviceSupportsFlashLight()) {
 			this.removeFlashLightPref();
 		}
 		
 		if(alarm.isPresetAlarm()) {
 			// having a delete button for the presets alarm makes no sense, so remove it. 
 			removeDeleteButton();
 			removeEditTitle();
 			removeAlarmToggle();		
 		}
 		
 		removeMiscCategoryIfEmpty();
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
 	
 	private boolean deviceSupportsFlashLight() {
 		Context context = this;
 		PackageManager pm = context.getPackageManager();
 
 		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
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
 		bindPreferenceSummaryToValue(findPreference(VOLUME));
 		bindPreferenceSummaryToValue(findPreference(CHALLENGE));
 		bindPreferenceSummaryToValue(findPreference(ENABLE_SNOOZE));
 		bindPreferenceSummaryToValue(findPreference(SNOOZE_TIME));
 		bindPreferenceSummaryToValue(findPreference(SPEECH));	
 		bindPreferenceSummaryToValue(findPreference(FLASH));
 
 			
 		findPreference(DELETE).setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				deleteAlarm();
 				return true;
 			}
 		});
 
 		findPreference(SPEECH_SAMPLE).setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				Debug.d("speech sample here");
 			
 				TextToSpeech tts = SFApplication.get().getTts();
 				
 				languageHasNoVoice(tts);
 				
 				
 				String s = new SpeechLocalizer(tts, AlarmSettingsActivity.this).getSpeech("Dry and mostly cloudy");
 				TextToSpeechUtil.speakAlarm(tts, s);				
 				return true;
 			}
 		});
 		
 		this.bindChallengeAdvancedButton();
 
 		this.setupRingerPreferences();
 	}
 
 	// if the user's current language doesn't have a voice installed
 	// notify the user that the English voice will used instead.
 	// also recommend the user to install a voice for his/her language. 
 	public void languageHasNoVoice(TextToSpeech tts ) {
 		Locale deviceLocale = Locale.getDefault();
 		if(!TextToSpeechUtil.languageHasVoice(deviceLocale, tts, this)) {
 			DialogUtils.showDoNotShowAgainMessageBox(this, 
 					 getResources().getString(R.string.no_voice_installed_title),
 			 getResources().getString(R.string.no_voice_installed_message),
 
 					"no_voice_installed_for_language");
 
 		}
 		
 	}
 
 	private void bindChallengeAdvancedButton() {
 		((EnablePlusSettingsPreference) findPreference(CHALLENGE)).setOnButtonClickListener(new OnClickListener() {
 			@Override
 			public void onClick( View v ) {
 				Intent i = new Intent(AlarmSettingsActivity.this, ChallengeSettingsActivity.class);
 				AlarmIntentHelper intentUtils = new AlarmIntentHelper(i);
 				if (AlarmSettingsActivity.this.alarm.isPresetAlarm()) {
 					intentUtils.setSettingPresetAlarm(true);
 				} else {
 					intentUtils.setAlarmId(alarm);
 				}
 				startActivity(i);
 			}
 		});
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
 		AudioDriverFactory factory = app().getAudioDriverFactory();
 		AudioDriver driver = factory.produce( this, this.alarm.getAudioSource() );
 		this.ringerPreference.setSummary( driver.printSourceName() );
 	}
 
 	@Handler
 	public void handleAudioChange( AudioChangeEvent evt ) {
 		Debug.d("handle audio change");
 		if ( evt.getModifiedField() == Field.AUDIO_SOURCE ) {
 			this.updateRingerSummary();
 		}
 	}
 
 	private void startRingerEdit() {
 		Intent intent = new Intent(this, RingerSettingsActivity.class );
 
 		AlarmIntentHelper intentUtils = new AlarmIntentHelper( intent );
 
 		if ( this.alarm.isPresetAlarm() ) {
 			intentUtils.setSettingPresetAlarm( true );
 		} else {
 			intentUtils.setAlarmId( alarm );
 		}
 	
 		
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
 				
 			}
 			else if (VIBRATION.equals(preference.getKey())) {
 				alarm.getAudioConfig().setVibrationEnabled(("true".equals(stringValue)) ? true : false);
 			}
 			else if (VOLUME.equals(preference.getKey())) {
 				alarm.getAudioConfig().setVolume(Integer.parseInt(stringValue));
 				preference.setSummary(stringValue + "%");
 			}
 			else if (CHALLENGE.equals(preference.getKey())) {
 				boolean enabled = (Boolean) value;
 				AlarmSettingsActivity.this.alarm.getChallengeSet().setEnabled( enabled );
 			}
 			else if (ENABLE_SNOOZE.equals(preference.getKey())) {
 				alarm.getSnoozeConfig().setSnoozeEnabled("true".equals(stringValue) ? true : false);
 			}
 			else if (SNOOZE_TIME.equals(preference.getKey())) {
 				if (stringValue.equals("") || stringValue.equals("0")) {
 					stringValue = preference.getSummary().toString();
 				}
 				int time = Integer.parseInt(stringValue);
 				alarm.getSnoozeConfig().setSnoozeTime(time);
 				String summary = time + " " + getResources()
 						.getQuantityText(R.plurals.minute, time).toString();
 				preference.setSummary(summary);
 			}
 			else if (SPEECH.equals(preference.getKey())) {
 				TextToSpeech tts = SFApplication.get().getTts();
 				languageHasNoVoice(tts);
 				alarm.setSpeech(("true".equals(stringValue)) ? true : false);
 			}
 			else if (FLASH.equals(preference.getKey())) {
 				alarm.setFlash(("true".equals(stringValue)) ? true : false);
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
 			((MultiSelectListPreference) preference).setEntryChecked(alarm.getEnabledDays());;
 			preference.setSummary(DateTextUtils.makeEnabledDaysText(alarm));	
 		}
 		else if (REPEAT.equals(preference.getKey())) {
 			((CheckBoxPreference) preference).setChecked(alarm.isRepeating());
 		}
 		else if (VIBRATION.equals(preference.getKey())) {
 			((CheckBoxPreference) preference).setChecked(alarm.getAudioConfig().getVibrationEnabled());
 		}
 		else if (VOLUME.equals(preference.getKey())) {
 			int vol = alarm.getAudioConfig().getVolume();
 			((VolumePreference) preference).setVolume(vol);
 			preference.setSummary(vol + "%");
 		}
 		else if (CHALLENGE.equals(preference.getKey())) {
 			boolean enabled = this.alarm.getChallengeSet().isEnabled();
 			((CheckBoxPreference) preference).setChecked(enabled);
 		}
 		else if (ENABLE_SNOOZE.equals(preference.getKey())) {
 			boolean enabled = this.alarm.getSnoozeConfig().isSnoozeEnabled();
 			((CheckBoxPreference) preference).setChecked(enabled);
 		}
 		else if (SNOOZE_TIME.equals(preference.getKey())) {
 			int time = this.alarm.getSnoozeConfig().getSnoozeTime();
 			NumberPickerDialogPreference pref = ((NumberPickerDialogPreference) preference);
 			pref.setValue(time);
 			String summary = time + " " + getResources().getQuantityText(R.plurals.minute, time).toString();
 			pref.setSummary(summary);
 		}else if (SPEECH.equals(preference.getKey())) {
 			((CheckBoxPreference) preference).setChecked(alarm.isSpeech());
 		}else if (FLASH.equals(preference.getKey())) {
 			((CheckBoxPreference) preference).setChecked(alarm.isFlashEnabled());
 		}
 
 		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 	}
 	
 	private void initiateTimePicker(TimepickerPreference tp) {
 		tp.setHour(alarm.getHour());
 		tp.setMinute(alarm.getMinute());
 	}
 }
