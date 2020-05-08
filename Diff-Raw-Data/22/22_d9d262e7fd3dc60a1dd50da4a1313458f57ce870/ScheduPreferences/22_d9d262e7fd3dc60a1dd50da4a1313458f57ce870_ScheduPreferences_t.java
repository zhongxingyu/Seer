 /**
  * @author Nick Huebner and Mark Redden
  * @version 1.0
  */
 package com.selagroup.schedu.activities;
 
 import java.util.Calendar;
 import java.util.Map;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 import com.selagroup.schedu.AlarmSystem;
 import com.selagroup.schedu.R;
 import com.selagroup.schedu.ScheduApplication;
 
 /**
  * The Class PreferencesActivity.
  */
 public class ScheduPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
 	public static final String PREF_KEY_SILENT = "autoSilence";
 	
 	public static final String PREF_KEY_COURSE_REMIND = "remindCourse";
 	public static final String PREF_KEY_COURSE_REMIND_TIME = "courseLeadTime";
 	
 	public static final String PREF_KEY_EXAM_REMINDERS = "remindExam";
 	public static final String PREF_KEY_EXAM_LEAD_TIME = "examLeadTime";
 	
 	public static final String PREF_KEY_HIGHLIGHT_RED = "highlightRed";
 	public static final String PREF_KEY_HIGHLIGHT_YELLOW = "highlightYellow";
 
 	private SharedPreferences sharedPref;
 	private Map<String, ?> mAllPreferences;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preferences);
 		addPreferencesFromResource(R.xml.preferences);
 		getListView().setCacheColorHint(0); // make listview bg transparent
 
 		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 	}
 
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 
 		mAllPreferences = sharedPref.getAll();
 
 		if (key.equals(PREF_KEY_SILENT)) {
 			ScheduApplication app = (ScheduApplication) getApplication();
 			AlarmSystem alarmSys = app.getAlarmSystem();
 			if ((Boolean) mAllPreferences.get(PREF_KEY_SILENT)) {
 				// Schedule alarms
 				alarmSys.scheduleEventsForDay(
 						app.getCourseManager().getAllForTerm(app.getCurrentTerm().getID()), Calendar.getInstance(), false);
 				
 			} else {
 				// Restore ringer mode
 				alarmSys.restoreRingerMode();
 				
				// Clear alarms
				alarmSys.clearAlarms();
 			}
 		} else if (key.equals(PREF_KEY_COURSE_REMIND)) {
 
 		} else if (key.equals(PREF_KEY_COURSE_REMIND_TIME)) {
 
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 	}
 }
