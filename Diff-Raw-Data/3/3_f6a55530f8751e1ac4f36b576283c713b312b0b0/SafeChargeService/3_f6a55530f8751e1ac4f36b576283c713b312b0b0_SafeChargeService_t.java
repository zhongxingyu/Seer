 package com.sash.safecharge;
 
 import android.annotation.SuppressLint;
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Build;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.preference.PreferenceManager;
 
 import com.sash.sodeliminator.R;
 
 public class SafeChargeService extends Service {
 	
 	private static final String POWER_CONNECTED = "com.sash.sodpreventer.ACQUIRE_WAKELOCK";
 	private static final String POWER_DISCONNECTED = "com.sash.sodpreventer.RELEASE_WAKELOCK";
 	private static final String CHECK_ON_BOOT = "com.sash.sodpreventer.CHECK_ON_BOOT";
 	private static final String RECHECK_LOCK = "com.sash.sodpreventer.RECHECK_LOCK";
 	private static final int NOTIFICATION_ID = 6243;
 	
 	private static WakeLock mWakeLock;
 	private static long notificationTime;
 	private static final long RECHECK_INTERVAL = 60 * 1000l;
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		String action = intent.getAction();
 		if (action != null) {
 			if (action.equals(POWER_CONNECTED))
 				acquireWakeLock();
 			else if (action.equals(POWER_DISCONNECTED))
 				releaseWakeLock();
 			else if (action.equals(CHECK_ON_BOOT) || action.equals(RECHECK_LOCK))
 				checkIfPlugged();
 		}
 		return START_REDELIVER_INTENT;
 	}
 	
 	@Override
 	public void onDestroy() {
 		if (mWakeLock != null) {
 			mWakeLock.release();
 		}
 		mWakeLock = null;
 		super.onDestroy();
 	}
 	
 	/**
 	 * Acquires a wakelock and requests the service to be run as a foreground service
 	 */
 	private void acquireWakeLock() {
 		if (mWakeLock == null) {
 			mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName());
 			mWakeLock.setReferenceCounted(false);
 		}
 		mWakeLock.acquire();
 		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_start_automatically", false)) {
 			scheduleRecheck();
 		}
 		startForeground(NOTIFICATION_ID, getNotification());
 	}
 	
 	/**
 	 * Checks if the power is connected and sends an intent to acquire wakelock if it is
 	 */
 	private void checkIfPlugged() {
 		Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
         int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
         boolean connected = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT >= 17) {
        	connected = connected || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
         if (connected) {
         	Intent start = new Intent(POWER_CONNECTED);
         	startService(start);
         } else {
         	Intent stop = new Intent(POWER_DISCONNECTED);
         	startService(stop);
         }
 	}
 	
 	/**
 	 * Stops the service. The actual wakelock release is done in the onDestroy() callback to exit clean if the service by any other means
 	 */
 	private void releaseWakeLock() {
 		Intent intent = new Intent(RECHECK_LOCK);
 		intent.setClass(this, SafeChargeService.class);
 		PendingIntent recheck = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).cancel(recheck);
 		stopForeground(true);
 		stopSelf();
 	}
 	
 	private void scheduleRecheck() {
 		long nextCheck = System.currentTimeMillis() + RECHECK_INTERVAL;
 		Intent intent = new Intent(RECHECK_LOCK);
 		intent.setClass(this, SafeChargeService.class);
 		PendingIntent recheck = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 		((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, nextCheck, recheck);
 	}
 	
 	/**
 	 * Creates a notification to be used for the foreground service
 	 * @return The notification
 	 */
 	@SuppressLint("NewApi") @SuppressWarnings("deprecation")
 	private Notification getNotification() {
 		Notification.Builder builder = new Notification.Builder(this);
 		builder.setSmallIcon(R.drawable.ic_stat_acquired)
 				.setContentTitle(getText(R.string.app_name))
 				.setContentText(getText(R.string.service_started))
 				.setWhen(notificationTime);
 		Notification result = null;
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 			result = builder.build();
 		} else {
 			result = builder.getNotification();
 		}
 		return result;
 	}
 	
 }
