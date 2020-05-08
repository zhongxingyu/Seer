 package com.sampleProject.ua;
 
 import com.sampleProject.sampleProjectActivity;
 import com.sampleProject.sampleProjectApplication;
 import com.sampleProject.plugins.PushNotificationPlugin;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import com.phonegap.api.Plugin;
 import com.urbanairship.UAirship;
 import com.urbanairship.push.PushManager;
 
 public class IntentReceiver extends BroadcastReceiver {
 
 	private static final String TAG = IntentReceiver.class.getSimpleName();
 	private PushNotificationPlugin plugin;
 	public static String intApid;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.i(TAG, "Received intent: " + intent.toString());
 		String action = intent.getAction();
 
 		if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {
 		    int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);
 
 		    Log.i(TAG, "Received push notification. Alert: " + intent.getStringExtra(PushManager.EXTRA_ALERT)
 				+ ". Payload: " + intent.getStringExtra(PushManager.EXTRA_STRING_EXTRA) + ". NotificationID="+id);
 		    
 		    String alert = intent.getStringExtra(PushManager.EXTRA_ALERT);
 		    String extra = intent.getStringExtra(PushManager.EXTRA_STRING_EXTRA);
 		    
 		    plugin = PushNotificationPlugin.getInstance();
		    if(plugin != null) plugin.sendResultBack(alert, extra);
 
 		} else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
 			Log.i(TAG, "User clicked notification. Message: " + intent.getStringExtra(PushManager.EXTRA_ALERT)
 					+ ". Payload: " + intent.getStringExtra(PushManager.EXTRA_STRING_EXTRA));
 
             Intent launch = new Intent(Intent.ACTION_MAIN);
 			launch.setClass(UAirship.shared().getApplicationContext(), sampleProjectActivity.class);
 			launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			
             UAirship.shared().getApplicationContext().startActivity(launch);
             
 		} else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
             Log.i(TAG, "Registration complete. APID:" + intent.getStringExtra(PushManager.EXTRA_APID)
                     + ". Valid: " + intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));
             intApid = intent.getStringExtra(PushManager.EXTRA_APID);
             Log.i(TAG, "Sending Apid to JS.");
 		}
 	}
 }
