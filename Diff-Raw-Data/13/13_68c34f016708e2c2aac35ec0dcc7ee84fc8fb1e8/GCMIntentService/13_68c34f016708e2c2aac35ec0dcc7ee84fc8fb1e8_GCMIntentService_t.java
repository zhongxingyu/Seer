 package org.in2teck.codice.AdivinaMe;
 
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import com.google.android.gcm.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import com.plugin.GCM.GCMPlugin;
 
 
 public class GCMIntentService extends GCMBaseIntentService {
 
   public static final String ME="GCMReceiver";
 
   public GCMIntentService() {
     super("GCMIntentService");
   }
   private static final String TAG = "GCMIntentService";
 
   @Override
   public void onRegistered(Context context, String regId) {
 
     Log.v(ME + ":onRegistered", "Registration ID arrived!");
     Log.v(ME + ":onRegistered", regId);
 
     JSONObject json;
 
     try
     {
       json = new JSONObject().put("event", "registered");
       json.put("regid", regId);
 
       Log.v(ME + ":onRegisterd", json.toString());
 
       // Send this JSON data to the JavaScript application above EVENT should be set to the msg type
       // In this case this is the registration ID
       GCMPlugin.sendJavascript( json );
 
     }
     catch( JSONException e)
     {
       // No message to the user is sent, JSON failed
       Log.e(ME + ":onRegisterd", "JSON exception");
     }
   }
 
   @Override
   public void onUnregistered(Context context, String regId) {
     Log.d(TAG, "onUnregistered - regId: " + regId);
   }
 
   @Override
   protected void onMessage(Context context, Intent intent) {
     Log.d(TAG, "onMessage - context: " + context);
 
     // Extract the payload from the message
     Bundle extras = intent.getExtras();
     if (extras != null) {
     	JSONObject json;
     	try {
     		//if  app is running
    		if(!Activity.isActivityVisible())
     		{
    		  	String message = extras.getString("message");
     	    	String title = extras.getString("title");
     	    	Notification notif = new Notification(android.R.drawable.btn_star_big_on, message, System.currentTimeMillis() );
     	    	notif.flags = Notification.FLAG_AUTO_CANCEL;
     	    	notif.defaults |= Notification.DEFAULT_SOUND;
     	    	notif.defaults |= Notification.DEFAULT_VIBRATE;
     	    	 
     	    	Intent notificationIntent = new Intent(context, AdivinaMe.class);
     	    	notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
     	    	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
     	    	 
     	    	notif.setLatestEventInfo(context, title, message, contentIntent);
     	    	String ns = Context.NOTIFICATION_SERVICE;
     	    	NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
     	    	mNotificationManager.notify(1, notif);
    		}
    		json = new JSONObject().put("event", "messageReceived");
	        GCMPlugin.sendJavascript( json );
     	}
     	catch( Exception e)
         {
           Log.e(ME + ":onMessage", e.getMessage() );
         }  	
     }
 
   }
 
   @Override
   public void onError(Context context, String errorId) {
     Log.e(TAG, "onError - errorId: " + errorId);
   }
 
 
 
 
 }
