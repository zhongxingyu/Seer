 package com.witwatersrand.androidapplication.scheduler;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 
 /**
 * The scheduler 
  * @author Kailesh Ramjee - University of Witwatersrand - School of Electrical & Information Engineering
  *
  */
 public class DailyResetTimerTask extends Service {
 	final static private String LOGGER_TAG = "WITWATERSRAND";
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
 	 */
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d(LOGGER_TAG, "DailyResetTimerTask -- onStartCommand() -- Scheduler called");
 		this.stopSelf();
 		return super.onStartCommand(intent, flags, startId);
 	}
 }
