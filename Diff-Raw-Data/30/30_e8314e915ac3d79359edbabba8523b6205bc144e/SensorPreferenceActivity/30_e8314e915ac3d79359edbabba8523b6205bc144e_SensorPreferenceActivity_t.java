 package edu.dartmouth.cs.audiorecorder;
 
 import org.ohmage.mobility.blackout.Blackout;
 import org.ohmage.mobility.blackout.BlackoutDesc;
 import org.ohmage.mobility.blackout.base.TriggerDB;
 import org.ohmage.mobility.blackout.base.TriggerInit;
 import org.ohmage.mobility.blackout.ui.TriggerListActivity;
 import org.ohmage.mobility.blackout.utils.SimpleTime;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 
 /**
  * This is the configuration portion of StressSense. 
  * 
  * Allows user to:
  * 		Set blackout times-intervals in which StressSense is not active
  * 		Turn on/off StressSense
  */
 public class SensorPreferenceActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	// String for Blackout utility
 	public static final String STRESSSENSE = "stresssense";
 	
 	// Keys for Preference components
 	public static final String ONOFF_KEY = "pref_onoff";
 	public static final String BLACKOUT_KEY = "pref_key";
 	
 	// Key for SharedPreferenceSettings for on/off status
 	public static final String IS_ON = "stresssense_on";
 	
 	// Blackout Listing Preference
 	private Preference connectionPref;
 	
 	private boolean running = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 
 		connectionPref = findPreference(BLACKOUT_KEY);
 		connectionPref.setOnPreferenceClickListener(mOnClickListener);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 		running = getPreferenceScreen().getSharedPreferences().getBoolean(
 				IS_ON, false);
 		if (running)
 			SensorPreferenceActivity.start(SensorPreferenceActivity.this
 					.getApplicationContext());
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences()
 				.unregisterOnSharedPreferenceChangeListener(this);
 
 	}
 
 	/*-------------------------------PREFERENCE FUNCTIONALITY-------------------------------*/
 
 	/**
 	 * Listener for the On/Off switch
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals(ONOFF_KEY)) {
 			running = !running;
 			Editor editor = sharedPreferences.edit();
 			editor.putBoolean(IS_ON, running);
 			editor.commit();
 			if (running)
 				SensorPreferenceActivity.start(SensorPreferenceActivity.this
 						.getApplicationContext());
 			else
 				SensorPreferenceActivity.stop(SensorPreferenceActivity.this
 						.getApplicationContext());
 		}
 	}
 
 	/**
 	 * Listener for the Blackout Listings
 	 */
 	private final OnPreferenceClickListener mOnClickListener = new OnPreferenceClickListener() {
 
 		@Override
 		public boolean onPreferenceClick(Preference preference) {
 			//SensorPreferenceActivity.stop(SensorPreferenceActivity.this.getApplicationContext());
 			Intent intent = new Intent(SensorPreferenceActivity.this,
 					TriggerListActivity.class);
 			SensorPreferenceActivity.this.startActivity(intent);
 			return false;
 		}
 	};
 
 	/*-------------------------------BLACKOUT FUNCTIONALITY-------------------------------*/
 
 	public static void startRunning (Context context) {
 		context.startService(new Intent(context, AudioRecorderService.class));
 	}
 	
 	public static void stopRunning (Context context) {
 		context.stopService(new Intent(context, AudioRecorderService.class));
 	}
 	public static void start(Context context) {
		canRunNow(context, true);
		startRunning(context);
 	}
 
 	public static void stop(Context context) {
		canRunNow(context, false);
		stopRunning(context);
 	}
 
 	/**
 	 * Determines whether or not the service can currently run based on blackout times
 	 * @param startCall if true, initializes triggers, otherwise stop triggers.
 	 */
 	private static boolean canRunNow(Context context, boolean startCall) {
 		TriggerDB db = new TriggerDB(context);
 		db.open();
 		boolean runningNow = true;
 		Cursor c = db.getAllTriggers();
 		if (c.moveToFirst()) {
 			do {
 				int trigId = c
 						.getInt(c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
 
 				String trigDesc = db.getTriggerDescription(trigId);
 				BlackoutDesc conf = new BlackoutDesc();
 
 				if (!conf.loadString(trigDesc)) {
 					continue;
 				}
 				SimpleTime start = conf.getRangeStart();
 				SimpleTime end = conf.getRangeEnd();
 				SimpleTime now = new SimpleTime();
 				if (!start.isAfter(now) && !end.isBefore(now)) {
 					runningNow = false;
 				}
 
 				if (!startCall)
 					new Blackout().stopTrigger(context, trigId,
 							db.getTriggerDescription(trigId));
 
 			} while (c.moveToNext());
 		}
 		c.close();
 		db.close();
 		if (startCall)
 			TriggerInit.initTriggers(context);
 		return runningNow;
 	}
 	
 }
