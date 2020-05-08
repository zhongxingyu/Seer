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
 	
	public int startCommand(Intent intent, int flags, int startID) {

 		// This service is sticky - it runs until it's stopped.
 		return START_STICKY;
 	}
 	
 	public void setAlarm(Calendar c) {
 		// Start a new task for the alarm on another thread (separated from the UI thread)
 		new AlarmTask(this, c).run();
 	}
 }
