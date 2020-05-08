 package com.davidjennes.ElectroJam.Server;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
 import android.util.Log;
 
 import com.davidjennes.ElectroJam.R;
 
 public class ServerService extends Service {
 	private static final String TAG = "ServerService";
 	private static final int NOTIFICATION = R.string.server_started;
 	
 	private NotificationManager m_nm;
 	private Server m_server;
 	private boolean m_running;
 	private final IBinder m_binder = new LocalBinder();
 	
 	/**
 	 * Simple binder for local service
 	 */
 	public class LocalBinder extends Binder {
 		ServerService getService() {
             return ServerService.this;
         }
     }
 	
 	public void onCreate() {
         m_nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         
         // init server
         m_running = false;
         m_server = new Server();
     }
 	
 	/**
 	 * Receive intents
 	 */
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.i(TAG, "Received start id " + startId + ": " + intent);
 		
 		// start or stop the actual server
 		if (intent.getAction().equals("start")) {
 			m_server.start(intent.getStringExtra("name"),  intent.getStringExtra("description"));
 			m_running = true;
 			
 			// Notifications
 			showNotification();
 		} else if (intent.getAction().equals("stop")) {
 			m_server.stop();
 			m_running = false;
 			
 			// Notifications
 			m_nm.cancel(NOTIFICATION);
 			stopSelf();
 		}
 		
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 	    return START_STICKY;
 	}
 	 
 	 public void onDestroy() {
 		 super.onDestroy();
 		 
 		 m_server.stop();
 		 m_server = null;
 		 
 		 m_nm.cancel(NOTIFICATION);
 	 }
 	
 	public IBinder onBind(Intent arg0) {
 		return m_binder;
 	}
 	
 	/**
	 * Check whether the actual thread server is running
 	 * @return True when running
 	 */
 	public boolean isServerRunning() {
 		return m_running;
 	}
 
 	/**
 	 * Show a notification while this service is running.
 	 */
 	private void showNotification() {
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ServerActivity.class), 0);
 		CharSequence text = getText(R.string.server_started);
 		
		Builder builder = new NotificationCompat.Builder(this);
		
		// Build notification
		builder.setSmallIcon(R.drawable.ic_launcher)
			.setTicker(text)
			.setWhen(System.currentTimeMillis())
			.setOngoing(true)
			.setContentTitle(getText(R.string.app_name))
			.setContentText(text)
			.setContentIntent(contentIntent);
         
         // Send the notification.
        m_nm.notify(NOTIFICATION, builder.getNotification());
     }
 }
