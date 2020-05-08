 package com.gethotdrop.service;
 
 import java.util.List;
 import android.annotation.SuppressLint;
 import android.app.ActivityManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
 import android.view.View;
 
 public class Updater implements Runnable {
 	private LocationManager locManager;
 	private Cache cache;
 	private Handler m_handler;
 	private long m_interval;
 	private Runnable m_update;
 	private ActivityManager manager;
 	private Context con;
 
 	public Updater(LocationManager lM, Cache c, Handler h, long interval,
 			Context cIn) {
 		locManager = lM;
 		cache = c;
 		m_handler = h;
 		m_interval = interval;
 		m_update = this;
 		con = cIn;
 		manager = (ActivityManager) con
 				.getSystemService(Context.ACTIVITY_SERVICE);
 	}
 
 	public void run() {
 		String provider = LocationManager.GPS_PROVIDER;
 		Location lastKnown = locManager.getLastKnownLocation(provider);
 		if (lastKnown == null) lastKnown = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		
 		new APIQuery().execute(lastKnown);
 	}
 
 	public void makeNotifications() {
 		if (isForeground("com.gethotdrop.ui")) {
 			Intent i = new Intent("com.gethotdrop.service.UPDATE_FEED");
 			LocalBroadcastManager.getInstance(con).sendBroadcast(i);
 		} else {
 			createNotification(null, "Hotdrop found!");
 
 		}
 	}
 
 	public boolean isForeground(String myPackage) {
 		List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
 				.getRunningTasks(1);
 		ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
 		if (componentInfo.getPackageName().equals(myPackage))
 			return true;
 		return false;
 	}
 
 	@SuppressLint("NewApi")
 	public void createNotification(View view, String type) {
 		// Prepare intent which is triggered if the
 		// notification is selected
 		// Intent intent = new Intent(this, NotificationReceiver.class);
 		// Build notification
 		// Actions are just fake
 		Notification noti = new Notification.Builder(con)
 				.setContentTitle(type + " Warning")
 				.setContentText("Dangerous levels of " + type).build();
 
 		NotificationManager notificationManager = (NotificationManager) con
 				.getSystemService(Context.NOTIFICATION_SERVICE);
 		// Hide the notification after its selected
 		noti.flags |= Notification.FLAG_AUTO_CANCEL;
 		notificationManager.notify(0, noti);
 	}
 	
 	public void reset() {
 		m_handler.removeCallbacks(this);
 		this.run();
 	}
	
 	public class APIQuery extends AsyncTask<Location, Void, Boolean> {
 		@Override
 		protected Boolean doInBackground(Location... loc) {
			return cache.refreshCache(loc[0]);//
 		}
 		
 		protected void onPostExecute(Boolean b) {
 			if (b)
 			makeNotifications();
 			m_handler.postDelayed(m_update, m_interval);
 		}
 		
 		
 	}
 
 }
