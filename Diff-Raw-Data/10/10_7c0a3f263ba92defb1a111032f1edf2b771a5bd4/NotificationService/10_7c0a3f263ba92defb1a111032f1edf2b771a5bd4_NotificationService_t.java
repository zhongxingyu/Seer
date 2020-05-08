 /**
 *	NotificationService.java
 *
 *	@author Johan
 */
 
 package se.chalmers.watchme.notifications;
 
 import java.util.Calendar;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
import android.util.Log;
 
 public class NotificationService extends Service {
 	
 	private final IBinder binder = new ServiceBinder();
 
 	public class ServiceBinder extends Binder {
 		NotificationService getService() {
 			return NotificationService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return this.binder;
 	}
 	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		Log.i("Custom", "Received start id " + startID + ": " + intent);
				
 		// This service is sticky - it runs until it's stopped.
 		return START_STICKY;
 	}
 	
 	public void setAlarm(Calendar c) {
		Log.i("Custom", "Set alarm");
 		// Start a new task for the alarm on another thread (separated from the UI thread)
 		new AlarmTask(this, c).run();
 	}
 }
