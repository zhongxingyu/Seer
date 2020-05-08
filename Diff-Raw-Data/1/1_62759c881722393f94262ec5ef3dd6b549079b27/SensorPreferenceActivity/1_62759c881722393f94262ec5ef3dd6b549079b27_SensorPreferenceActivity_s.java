 package edu.dartmouth.cs.audiorecorder;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.UUID;
 
 import org.ohmage.mobility.blackout.Blackout;
 import org.ohmage.mobility.blackout.BlackoutDesc;
 import org.ohmage.mobility.blackout.base.TriggerDB;
 import org.ohmage.mobility.blackout.base.TriggerInit;
 import org.ohmage.mobility.blackout.ui.TriggerListActivity;
 import org.ohmage.mobility.blackout.utils.SimpleTime;
 import org.ohmage.probemanager.ProbeBuilder;
 import org.ohmage.probemanager.StressSenseProbeWriter;
 
 import edu.dartmouth.cs.audiorecorder.SensorlabRecorderActivity.AudioRecorderStatusRecevier;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.Preference.OnPreferenceClickListener;
 
 public class SensorPreferenceActivity extends PreferenceActivity implements
 		OnSharedPreferenceChangeListener {
 
 	public static final String STRESSSENSE = "stresssense";
 	public static final String ONOFF_KEY = "pref_onoff";
 	public static final String BLACKOUT_KEY = "pref_key";
 	//public static final String LOCATION_KEY = "pref_loc";
 	public static final String IS_ON = "stresssense_on";
 	public static final String ACTIVITY_LOADED = "edu.dartmouth.besafe.Activity.intent.LOADED";
 	public static final String ACTIVITY_ON = "edu.dartmouth.besafe.Activity.intent.ON";
 
 	private boolean running = false;
 	private String message;
 	private Preference connectionPref;
 	//private CheckBoxPreference mobility_on;
 	private static StressSenseProbeWriter probeWriter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		
 		/*
 		mobility_on = (CheckBoxPreference) findPreference(LOCATION_KEY);
 		if (!MobilityHelper.isMobilityInstalled(this)) {
 			removePreference(mobility_on);
 		}*/
 		
 		
 		connectionPref = findPreference(BLACKOUT_KEY);
 		connectionPref.setOnPreferenceClickListener(mOnClickListener);
 		
 		mActivityLoaded = new ActivityLoadedReceiver();
 		mActivityon = new ActivityOnReceiver();
 		registerReceiver(mActivityLoaded, new IntentFilter(ACTIVITY_LOADED));
 		registerReceiver(mActivityon, new IntentFilter(ACTIVITY_ON));
 		sMessageHandler = mHandler;
 		probeWriter = new StressSenseProbeWriter(this);
 		probeWriter.connect();
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
 
 	@Override
 	protected void onDestroy() {
 		unregisterReceiver(mActivityLoaded);
 		unregisterReceiver(mActivityon);
 		sMessageHandler = null;
 		probeWriter.close();
 	}
 
 	/*-------------------------------PREFERENCE FUNCTIONALITY-------------------------------*/
 
 	private final OnPreferenceClickListener mOnClickListener = new OnPreferenceClickListener() {
 
 		@Override
 		public boolean onPreferenceClick(Preference preference) {
 			Intent intent = new Intent(SensorPreferenceActivity.this,
 					TriggerListActivity.class);
 			SensorPreferenceActivity.this.startActivity(intent);
 			return false;
 		}
 	};
 
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
 
 	/*-------------------------------HANDLER FUNCTIONALITY-------------------------------*/
 
 	Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {			
 			message = msg.getData().getString(
 					AudioRecorderService.AUDIORECORDER_NEWTEXT_CONTENT);		
 			if(probeWriter != null) {
 				ProbeBuilder probe = new ProbeBuilder();
 				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
 				String nowAsString = df.format(new Date());
 				probe.withTimestamp(nowAsString);
 				probeWriter.write(probe, message);
 	        }
 		}
 	};
 
 	private static Handler sMessageHandler;
 
 	public static Handler getHandler() {
 		return sMessageHandler;
 	}
 
 	/*-------------------------------BLACKOUT FUNCTIONALITY-------------------------------*/
 
 	public static void startRunning(Context context) {
 		context.startService(new Intent(context, AudioRecorderService.class));
 	}
 
 	public static void stopRunning(Context context, boolean blackout) {
 		context.stopService(new Intent(context, AudioRecorderService.class));
 	}
 
 	public static void start(Context context) {
 		TriggerDB db = new TriggerDB(context);
 		db.open();
 		boolean canRunNow = true;
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
 				if (!start.isAfter(now) && end.isAfter(now)) {
 					canRunNow = false;
 				}
 
 			} while (c.moveToNext());
 		}
 		c.close();
 		db.close();
 		TriggerInit.initTriggers(context);
 		if (canRunNow)
 			startRunning(context);
 	}
 
 	public static void stop(Context context) {
 
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
 				new Blackout().stopTrigger(context, trigId,
 						db.getTriggerDescription(trigId));
 
 			} while (c.moveToNext());
 		}
 		c.close();
 		db.close();
 		// TriggerInit.initTriggers(context);
 		if (runningNow)
 			stopRunning(context, false);
 		// LogProbe.close(context);
 	}
 
 	/*-------------------------------BROADCASTRECEIVER FUNCTIONALITY-------------------------------*/
 	
 	private ActivityLoadedReceiver mActivityLoaded;
 	private ActivityOnReceiver mActivityon;
 
 	class ActivityLoadedReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction().equals(ACTIVITY_LOADED)) {
 				Intent i = new Intent();
 				if (running)
 					i.setAction(AudioRecorderService.AUDIORECORDER_ON);
 				else
 					i.setAction(AudioRecorderService.AUDIORECORDER_OFF);
 				sendBroadcast(i);
 			}
 		}
 	}
 
 	class ActivityOnReceiver extends BroadcastReceiver {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Intent i = new Intent();
 			i.setAction(AudioRecorderService.AUDIORECORDER_NEWTEXT_CONTENT);
 			i.putExtra("Mode", message);
 			sendBroadcast(i);
 		}
 	}
 
 }
