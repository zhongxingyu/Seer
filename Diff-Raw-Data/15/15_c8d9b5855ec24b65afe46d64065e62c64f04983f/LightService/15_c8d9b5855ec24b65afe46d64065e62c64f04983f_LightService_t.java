 package com.fleurey.android.light.backgroundmanager;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 
 import com.fleurey.android.light.R;
 import com.fleurey.android.light.flashmanager.FlashManager;
 import com.fleurey.android.light.flashmanager.FlashManager.LightPower;
 
 public class LightService extends Service {
 
 	public final static String SERVICE_RUNNING = LightService.class.getName() + ".PREF_SERVICE_RUNNING";
 	
 	private static final String ACTION_STOP_REQUEST = LightService.class.getName() + ".ACTION_STOP_REQUEST";
 	private static final int SERVICE_ID = 79290;
 	
 	private FlashManager mFlashManager = new FlashManager();
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (intent.getAction() != null && ACTION_STOP_REQUEST.equals(intent.getAction())) {
 			stopSelfResult(startId);
 		}
 		mFlashManager.setPower(LightPower.ON);
 		startForeground(SERVICE_ID, buildRunningNotification());
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(SERVICE_RUNNING, true).commit();
 		return START_NOT_STICKY;
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		mFlashManager.release();
 		stopForeground(true);
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(SERVICE_RUNNING, false).commit();
 	}
 
 	private Notification buildRunningNotification() {
 		Intent stopService = new Intent(getApplicationContext(), getClass());
 		stopService.setAction(ACTION_STOP_REQUEST);
 		PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), 0, stopService, 0);
 		NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
 		builder.setContentTitle("Light");
 		builder.setContentText("Touch to power off");
 		builder.setSmallIcon(R.drawable.ic_service_running);
 		builder.setContentIntent(pIntent);
 		Notification runningNotification = builder.build();
 		return runningNotification;
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 }
