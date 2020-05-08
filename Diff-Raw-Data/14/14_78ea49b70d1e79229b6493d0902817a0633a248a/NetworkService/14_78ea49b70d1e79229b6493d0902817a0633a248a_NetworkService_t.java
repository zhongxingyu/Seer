 package com.subspace.android;
 
 import java.io.IOException;
 
 import com.subspace.network.NetworkGame;
 import com.subspace.network.NetworkSubspace;
 import com.subspace.redemption.*;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class NetworkService extends Service {
 
      static final String TAG = "Subspace";
 	
 	 private NotificationManager mNM;
 	 private NetworkGame subspace;
 	
 	// Unique Identification Number for the Notification.
     // We use it on Notification start, and to cancel it.
     private int NOTIFICATION = R.string.network_service_started;
 
     /**
      * Class for clients to access.  Because we know this service always
      * runs in the same process as its clients, we don't need to deal with
      * IPC.
      */
     public class LocalBinder extends Binder {
     	public NetworkService getService() {
             return NetworkService.this;
         }
     }
 	 	
 	@Override
     public void onCreate() {
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 
         // Display a notification about us starting.  We put an icon in the status bar.
       //  showNotification();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.i(TAG, "Received start id " + startId + ": " + intent);
                
         
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 
     @Override
     public void onDestroy() {
     	
     	//disconnect if we are connected
     	if(subspace.isConnected())
     	{
     		try {
     			subspace.SSDisconnect();
     		} catch(IOException ioe)
     		{
     			Log.e(TAG, Log.getStackTraceString(ioe));
     		}
     	}
     	
         // Cancel the persistent notification.
         mNM.cancel(NOTIFICATION);
 
         // Tell the user we stopped.
         Toast.makeText(this, R.string.network_service_stopped, Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
 
     // This is the object that receives interactions from clients.  See
     // RemoteService for a more complete example.
     private final IBinder mBinder = new LocalBinder();
 
     /**
      * Show a notification while this service is running.
      */
     private void showNotification() {
         // In this sample, we'll use the same text for the ticker and the expanded notification
         CharSequence text = getText(R.string.network_service_started);
 
         // Set the icon, scrolling text and timestamp
         Notification notification = new Notification(R.drawable.icon, text,
                 System.currentTimeMillis());
 
         Intent notificationIntent = new Intent(this, HomeActivity.class);
         notificationIntent.addFlags(Notification.FLAG_ONGOING_EVENT);
         // The PendingIntent to launch our activity if the user selects this notification
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
         		notificationIntent, 0);
         
         notification.contentIntent = contentIntent;
 
         // Set the info for the views that show in the notification panel.
         notification.setLatestEventInfo(this, 
         		//getText(R.string.local_service_label)
         		"hello",
         		text,null);//, contentIntent);
 
         // Send the notification.
         mNM.notify(NOTIFICATION, notification);
     }
     
     public void Connect(String zoneName,String ipAddress, int port)
     {
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	boolean logConnection =  prefs.getBoolean("pref_logConnection", true);
 		boolean logCorePackets =  prefs.getBoolean("pref_logCorePackets", true);
 		boolean logGamePackets =  prefs.getBoolean("pref_logGamePackets", true);	
     	
     	try {
     		subspace = new NetworkGame(getApplicationContext(),zoneName);
     		//setup logging as set in settings
     		NetworkGame.LOG_CONNECTION = logConnection;    		
     		NetworkGame.LOG_CORE_PACKETS = logCorePackets;
     		NetworkGame.LOG_GAME_PACKETS = logGamePackets;
     		
     		//temp local test
    		ipAddress= "86.163.6.11";
    		port = 2000;
     		
 			subspace.SSConnect(ipAddress,port);
 		} catch (Exception e) {
 			Log.e(TAG,Log.getStackTraceString(e)); 
 		}
     }   
     
     public NetworkGame getSubspace()
     {
     	return subspace;
     }
     	
 }
