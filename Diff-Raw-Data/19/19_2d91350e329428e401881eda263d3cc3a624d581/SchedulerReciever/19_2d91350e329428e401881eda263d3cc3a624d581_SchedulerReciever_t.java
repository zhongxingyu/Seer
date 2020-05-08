 package com.rj.memomatic;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class SchedulerReciever extends BroadcastReceiver {
 	public final static String PROCESS_SHARED_PREFS = SchedulerReciever.class.getPackage().toString() + ".PROCESS_SHARED_PREFS";
 	public final static String LASTSCANNED_SHARED_PREFS = SchedulerReciever.class.getPackage().toString() + ".LASTSCANNED_SHARED_PREFS";
 	public final static String TIME_OF_LAST_SCAN = SchedulerReciever.class.getPackage().toString() + ".LAST_SCAN";
 	public final static String NEXT_SCHEDULED_SCAN = SchedulerReciever.class.getPackage().toString() + ".NEXT_SCHEDULED_SCAN";
 	public final static String INTERVAL = SchedulerReciever.class.getPackage().toString() + ".INTERVAL";
 	public final static String ENABLED = SchedulerReciever.class.getPackage().toString() + ".ENABLED";
 	public final static String PAUSED = SchedulerReciever.class.getPackage().toString() + ".PAUSED";
 	
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		try {
 			PhotoTaker.takePhoto(context);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			saveTimestamp(context);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		startSelf(context);
 	}
 	
 	
 	private void saveTimestamp(Context context) {
 		SharedPreferences prefs = context.getSharedPreferences(LASTSCANNED_SHARED_PREFS, 0);
 		Editor edit = prefs.edit();
 		edit.putLong(TIME_OF_LAST_SCAN, System.currentTimeMillis());
 		edit.putLong(NEXT_SCHEDULED_SCAN, System.currentTimeMillis()+getInterval(context));
 		edit.commit(); 
 	}
 	private static void setNextScheduledScan(Context context, long time) {
 		SharedPreferences prefs = context.getSharedPreferences(LASTSCANNED_SHARED_PREFS, 0);
 		Editor edit = prefs.edit();
 		edit.putLong(NEXT_SCHEDULED_SCAN, time);
 		edit.commit(); 
 	}
 	
 	public static long getLastScan(Context context) {
 		SharedPreferences prefs = context.getSharedPreferences(LASTSCANNED_SHARED_PREFS, 0);
 		return prefs.getLong(TIME_OF_LAST_SCAN, -1);
 	}
 	public static long getNextScan(Context context) {
 		SharedPreferences prefs = context.getSharedPreferences(LASTSCANNED_SHARED_PREFS, 0);
 		return prefs.getLong(NEXT_SCHEDULED_SCAN, -1);
 	}
 
 	
 	
 	
 	
 	public static void startSelf(Context context) {
 		startStopSelf(context, true);
 	}
 	public static void stopSelf(Context context) {
 		startStopSelf(context, false);
 	}
 	public static void startStopSelf(Context context, boolean start) {
         AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
         Intent i=new Intent(context, SchedulerReciever.class);
         PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
 
     	mgr.cancel(pi);
 
     	long now = System.currentTimeMillis();
     	long scheduledTime = getNextScan(context);
     	//if we missed this photo, skip it and schedule another soon.
     	if (now > scheduledTime) {
     		long repeatTime = getInterval(context);
     		scheduledTime = now + repeatTime;
     		Log.d("MemoMatic - Scheduler", "Missed the photo. new one in "+((repeatTime)/1000)+" seconds in the future: "+start);
     		setNextScheduledScan(context, scheduledTime);
     	}
 		Log.d("MemoMatic - Scheduler", "Scheduling Picture for "+((scheduledTime-now)/1000)+" seconds in the future: "+start);
 		if (isPaused(context) == true || isEnabled(context) == false) {
 			Log.d("MemoMatic - Scheduler", "either paused or disabled. not scheduling");
 			return;
 		}
         if (start) {
         	//mgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), repeatTime, pi);
         	mgr.set(AlarmManager.RTC, scheduledTime, pi);
         }
 
 	}
 
 	
 	
 	public static boolean isEnabled(Context context) {
 		SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
 		return prefs.getBoolean(ENABLED, false);
 	}
 	
 	
 	public static void setEnabled(Context context, boolean running) {        
 		SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
 		Editor edit = prefs.edit();
     	edit.putBoolean(ENABLED, running);
         edit.apply();
 	}
 	
 	public static boolean isPaused(Context context) {
 		SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
 		return prefs.getBoolean(PAUSED, false);
 	}
 	
 	
 	public static void setPaused(Context context, boolean paused) {        
 		SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
 		Editor edit = prefs.edit();
     	edit.putBoolean(PAUSED, paused);
         edit.apply();
 	}
 
 
 
 	
 	
 	public static long getInterval(Context context) {
 		SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
		return prefs.getLong(INTERVAL, 20*60*1000);//20*60*1000);
 	}
 	
 
 	
 	public static void setInterval(Context context, long value) {
 		SharedPreferences prefs = context.getSharedPreferences(PROCESS_SHARED_PREFS, 0);
 		Editor edit = prefs.edit();
 		edit.putLong(INTERVAL, value);
 		edit.apply();
 	}
 	
 	
 	
 	
 	
 	public static void actionPause(Context context) {
 		Log.d("MemoMatic - Scheduler", "Pausing");
 		setPaused(context, true);
 		stopSelf(context);
 	}
 	
 	public static void actionUnpause(Context context) {
 		Log.d("MemoMatic - Scheduler", "Unpausing");
 		setPaused(context, false);
 		startSelf(context);
 	}
 
 	
 	public static void actionEnable(Context context) {
 		Log.d("MemoMatic - Scheduler", "Enabling");
 		setEnabled(context, true);
 		startSelf(context);
 	}
 	
 	public static void actionDisable(Context context) {
 		Log.d("MemoMatic - Scheduler", "Disabling");
 		setEnabled(context, false);
 		stopSelf(context);
 	}
 	
 
 	
 
 }
