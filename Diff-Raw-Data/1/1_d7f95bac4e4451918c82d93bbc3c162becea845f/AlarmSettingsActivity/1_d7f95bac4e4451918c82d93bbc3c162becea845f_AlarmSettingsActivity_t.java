 package se.chalmers.dat255.sleepfighter.activities;
 
 import java.util.HashSet;
 import java.util.Locale;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.view.MenuItem;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.TimepickerPreference;
 import se.chalmers.dat255.sleepfighter.debug.Debug;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.SFApplication;
 
 public class AlarmSettingsActivity extends PreferenceActivity {
 
 	private static final String NAME = "pref_alarm_name";
 	private static final String TIME = "pref_alarm_time";
 	private static final String DAYS = "pref_enabled_days";
 	
 	// is used in sBindPreferenceSummaryToValueListener
 	private static String[] weekdayStrings;
 	
 	private static Alarm alarm;	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		 weekdayStrings = AlarmSettingsActivity.this.getResources().getStringArray(R.array.week_days);
 			
 		if (getIntent().getExtras() == null) {
 			throw new IllegalArgumentException();
 		}
 		
 		int id = this.getIntent().getExtras().getInt("id");
 		
 		AlarmList alarms = ((SFApplication) getApplication()).getAlarms();
 		
 		alarm = alarms.getById(id);
 		
 		if (alarm == null) {
 			// TODO: Better handling for final product
 			Toast.makeText(this, "Alarm is null (ID: " + id + ")", Toast.LENGTH_SHORT).show();
 			finish();
 		}
 		
 		if (!"".equals(alarm.getName())) {
 			this.setTitle(alarm.getName());
 		}
 		
 		// TODO: Remove this debug thing
 		this.setTitle(this.getTitle() + " (ID: " + alarm.getId() + ")");
 		
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
 	}
 	
 	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
 		@Override
 		public boolean onPreferenceChange(Preference preference, Object value) {
 			
 			if (TIME.equals(preference.getKey())) {
 				TimepickerPreference tpPref = (TimepickerPreference) preference;
 				
 				int hour = tpPref.getHour();
 				int minute = tpPref.getMinute();
 				
 				alarm.setTime(hour, minute);
 				
 				preference.setSummary((hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute);
 			}
 			else if (NAME.equals(preference.getKey())) {
 				String stringValue = value.toString();
 	
 				alarm.setName(stringValue);
 				preference.setSummary(stringValue);
 			} else if(DAYS.equals(preference.getKey())) {
 				
 
 				boolean[] enabledDays = { false, false, false, false, false, false, false };
 
 				// a set of all the selected weekdays. 
				@SuppressWarnings("unchecked")
 				HashSet<String> set = (HashSet<String>)value;
 		
 				for(int i = 0; i < weekdayStrings.length; ++i) {
 					if(set.contains(weekdayStrings	[i])) {
 						Debug.d("day enabled: " + weekdayStrings[i]);
 						enabledDays[i] = true;
 					}
 				}
 				
 				alarm.setEnabledDays(enabledDays);
 				preference.setSummary(formatDays(alarm));	
 			}
 			return true;
 		}
 	};
 	
 	private static String formatDays(final Alarm alarm) {
 		String formatted = "";
 		
 		// Compute weekday names & join.
 		final int indiceLength = 2;
 		final String[] days = DateTextUtils.getWeekdayNames( indiceLength, Locale.getDefault() );
 		final boolean[] enabled = alarm.getEnabledDays();
 		
 		for(int i = 0; i < days.length; ++i) {
 			if(enabled[i] == true) {
 				formatted += days[i] + " ";
 			}
 		}
 		
 		return formatted;
 	}
 	
 	private static void bindPreferenceSummaryToValue(Preference preference) {
 		preference.setPersistent(false);
 		
 		if (NAME.equals(preference.getKey())) {
 			preference.setSummary(alarm.getName());
 		}
 		else if (TIME.equals(preference.getKey())) {
 			preference.setSummary(alarm.getTimeString());
 		} else if(DAYS.equals(preference.getKey())) {
 			preference.setSummary(formatDays(alarm));	
 		}
 		
 		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 	}
 }
