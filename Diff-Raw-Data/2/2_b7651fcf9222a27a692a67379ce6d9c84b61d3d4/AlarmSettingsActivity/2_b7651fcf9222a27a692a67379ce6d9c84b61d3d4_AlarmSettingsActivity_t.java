 package se.chalmers.dat255.sleepfighter.activity;
 
 import net.engio.mbassy.listener.Handler;
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.Field;
 import se.chalmers.dat255.sleepfighter.model.Alarm.MetaChangeEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.preference.MultiSelectListPreference;
 import se.chalmers.dat255.sleepfighter.preference.TimepickerPreference;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.MetaTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.DialogUtils;
 import se.chalmers.dat255.sleepfighter.utils.android.IntentUtils;
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
 import android.support.v4.app.NavUtils;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 public class AlarmSettingsActivity extends PreferenceActivity {
 	public static final String EXTRA_ALARM_IS_NEW = "alarm_is_new";
 
 	private final String NAME = "pref_alarm_name";
 	private final String TIME = "pref_alarm_time";
 	private final String DAYS = "pref_enabled_days";
 	private final String REPEAT = "pref_alarm_repeat";
 	private final String DELETE = "pref_delete_alarm";
 
 	private final String RINGER_SUBSCREEN = "perf_alarm_ringtone";
 
 	private Alarm alarm;
 	private AlarmList alarmList;
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= 11) {
 			ActionBar actionBar = getActionBar();
 		    // add the custom view to the action bar
 		    actionBar.setCustomView(R.layout.alarm_settings_actionbar);
 		    EditText edit_title_field = (EditText) actionBar.getCustomView().findViewById(R.id.alarm_edit_title_field);
 		    edit_title_field.setText(MetaTextUtils.printAlarmName(this, alarm));
 		    edit_title_field.setOnEditorActionListener(new OnEditorActionListener() {
 
 				@Override
 				public boolean onEditorAction(TextView v, int actionId,
 						KeyEvent event) {
 					alarm.setName(v.getText().toString());
 					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 					return false;
 				}
 		    });
 		    edit_title_field.clearFocus();
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM);
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
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
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		final int id = new IntentUtils( this.getIntent() ).getAlarmId();
 
 		alarmList = ((SFApplication) getApplication()).getAlarms();
 		((SFApplication) getApplication()).getBus().subscribe(this);
 		
 		alarm = alarmList.getById(id);
 
 		if (alarm == null) {
 			// TODO: Better handling for final product
 			Toast.makeText(this, "Alarm is null (ID: " + id + ")", Toast.LENGTH_SHORT).show();
 			finish();
 		}
 
 		this.setTitle(MetaTextUtils.printAlarmName(this, alarm));
 
 		// TODO: Remove this debug thing
 		this.setTitle(this.getTitle() + " (ID: " + alarm.getId() + ")");
 		
 		setupActionBar();
 		
 		setupSimplePreferencesScreen();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
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
 		
 		findPreference(DELETE).setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						alarmList.remove(alarm);
 						finish();
 					}
 				};
 				DialogUtils
 				.showConfirmationDialog(getResources()
 						.getString(R.string.confirm_delete),
 						AlarmSettingsActivity.this,
 						dialogClickListener);
 				return true;
 			}
 		});
 
 		findPreference( RINGER_SUBSCREEN ).setOnPreferenceClickListener( new OnPreferenceClickListener() {
 			@Override
 			public boolean onPreferenceClick( Preference preference ) {
 				startRingerEdit();
 				return true;
 			}
 		} );
 	}
 
 	protected void startRingerEdit() {
 		Intent intent = new Intent(this, RingerSettingsActivity.class );
 		new IntentUtils( intent ).setAlarmId( alarm );
 		this.startActivity( intent );
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
