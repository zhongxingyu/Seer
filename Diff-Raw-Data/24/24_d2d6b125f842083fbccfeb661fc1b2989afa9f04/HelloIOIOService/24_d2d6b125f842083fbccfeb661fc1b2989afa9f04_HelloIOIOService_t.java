 package uk.org.opensystem;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.os.IBinder;
 import android.os.Binder;
 import android.util.Log;
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.IOIO;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.BaseIOIOLooper;
 import ioio.lib.util.IOIOLooper;
 import ioio.lib.util.android.IOIOService;
 import android.app.Activity;
 
 import java.util.Random;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import java.util.Date;
 import android.content.Context;
 import android.app.Service;
 
 
 import uk.org.opensystem.R;
 import uk.org.opensystem.R.drawable;
 import uk.org.opensystem.plugin.IOIOconnect;
 import org.apache.cordova.*;
 
 
 /**
  * An example IOIO service. While this service is alive, it will attempt to
  * connect to a IOIO and blink the LED. A notification will appear on the
  * notification bar, enabling the user to stop the service.
  * For external communications try: https://github.com/nickfox/Update-Android-UI-from-a-Service
  */
 public class HelloIOIOService extends IOIOService {
 	private static String TAG = "IOIOBroadcast";
 	private int interval = 1000;
 	private boolean onoff  = false;
 	private int counter = 0;
 	Context thiscontext;
 	
	public int getextra(){
		return 44;
	}
 	// MANAGE MESSAGING
 	// This is the object that receives interactions from clients.  See
     // RemoteService for a more complete example.
     //private final IBinder mBinder = new LocalBinder();
    //public class LocalBinder extends Binder {
    //	HelloIOIOService getService() {
    //        return HelloIOIOService.this;
    //    }
    //}
     @Override
     public IBinder onBind(Intent intent) {
    	//Log.d("helloIOIOService.java", "IOIO bound service");
        //return mBinder;
        return null;
     }
 	
     // USUAL IOIO SERVICE STUFF
 	@Override
 	public void onStart(Intent intent, int startId) {  
 		 
		thiscontext = getApplicationContext();
 		
 		// Service has been started
 		super.onStart(intent, startId);
 		Log.d("helloIOIOService.java", "IOIO started service");
 		
         // IOIO When service is started load external vars (if set)
 		int loadinterval = intent.getIntExtra("loadinterval", -1);
 		if(loadinterval>=0){ interval = loadinterval; }
 	        
 		// Native IOIO stuff
 		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		
 		if (intent != null && intent.getAction() != null && intent.getAction().equals("stop")) {
 			// User clicked the notification. Need to stop the service.
 			nm.cancel(0);
 			stopSelf();
 		} else {
 			// Service starting. Create a notification.
 			Notification notification = new Notification(
 					R.drawable.ic_launcher, "IOIO service running",
 					System.currentTimeMillis());
 			notification
 					.setLatestEventInfo(this, "IOIO Service", "Click to stop",
 							PendingIntent.getService(this, 0, new Intent(
 									"stop", null, this, this.getClass()), 0));
 			notification.flags |= Notification.FLAG_ONGOING_EVENT;
 			nm.notify(0, notification);
 		}
 		
 	}
 
     // IOIO Thread
 	@Override
 	protected IOIOLooper createIOIOLooper() {
 		return new BaseIOIOLooper() {
 			private DigitalOutput led_;
 
 			@Override
 			protected void setup() throws ConnectionLostException,
 					InterruptedException {
 				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
 			}
 
 			@Override
 			public void loop() throws ConnectionLostException,
 				InterruptedException {
 					led_.write(onoff);
 					Thread.sleep(interval);
 					led_.write(onoff);
 					Thread.sleep(interval);
 					onoff = !onoff;
 					counter++;
 					if(counter>=2) interval = 60;
 					sendMessage();
 			}
 		};
 	}
 
 
 	 // Various methods to get data
     private void sendMessage()
     {
     	
         //Intent intent = new Intent("IOIOData");     
         //intent.putExtra("LED", get_LED());
         //LocalBroadcastManager.getInstance(activity_name).sendBroadcast();
         /*
 		LocalBroadcastManager.getInstance(activity_name).sendBroadcast(intent);
         intent.putExtra("MAG", get_MAG());
 		LocalBroadcastManager.getInstance(activity_name).sendBroadcast(intent);
         intent.putExtra("BAR", get_BAR());
 		LocalBroadcastManager.getInstance(activity_name).sendBroadcast(intent);
         intent.putExtra("GYRO", get_GYRO());
 		LocalBroadcastManager.getInstance(activity_name).sendBroadcast(intent);
         intent.putExtra("EULER", get_EULER());
 		LocalBroadcastManager.getInstance(activity_name).sendBroadcast(intent);
         intent.putExtra("GGA", gps_GGA);
 		LocalBroadcastManager.getInstance(activity_name).sendBroadcast(intent);
 		*/
     }
 
 
 
 }
