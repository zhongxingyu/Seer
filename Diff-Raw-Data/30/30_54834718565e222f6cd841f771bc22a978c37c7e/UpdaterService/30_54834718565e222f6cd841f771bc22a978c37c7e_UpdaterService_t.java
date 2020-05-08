 package edu.rit.csh.androidwebnews;
 
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.HandlerThread;
 import android.os.IBinder;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 
 public class UpdaterService extends IntentService {
 	Vibrator mVibrator;
 	  /** 
 	   * A constructor is required, and must call the super IntentService(String)
 	   * constructor with a name for the worker thread.
 	   */
 	  public UpdaterService() {
 	      super("Service");
 	      //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		  //boolean run_service = sharedPref.getBoolean("run_service", false);
 	  }
 
 	  /**
 	   * The IntentService calls this method from the default worker thread with
 	   * the intent that started the service. When this method returns, IntentService
 	   * stops the service, as appropriate.
 	   */
 	  @Override
 	  protected void onHandleIntent(Intent intent) {
 		  Log.d("jddebug", "service started");
 		  SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		  String apiKey = sharedPref.getString("api_key", "");
 		  HttpsConnector hc = new HttpsConnector(this);
 		  int[] statuses = new int[3];
 		  //Log.d("jddebug", hc.getUnreadCount().toString());
 		  if ((statuses = hc.getUnreadCount()) != null) { // if valid api key
 			  
 			  if (statuses[0] != 0 && statuses[0] != sharedPref.getInt("number_of_unread", 0)) { // if there are new posts and that number is different than last time the update ran
 				  
 				  SharedPreferences.Editor editor = sharedPref.edit();
 				  editor.putInt("number_of_unread", statuses[0]);
 				  editor.commit();
 				  
 				  NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
 				  mBuilder.setContentTitle("CSH Webnews");
				  mBuilder.setSmallIcon(R.drawable.notification_icon);
 				  mBuilder.setAutoCancel(true);
 				  if (statuses[2] != 0) {
 					  if (statuses[2] == 1) {
 						  mBuilder.setContentText(statuses[2] + " reply to your post");  
 					  } else {
 						  mBuilder.setContentText(statuses[2] + " reply to your posts");
 					  }
 				  } else if (statuses[1] != 0) {
 					  if (statuses[1] == 1) {
 						  mBuilder.setContentText(statuses[1] + " unread post in your thread");  
 					  } else {
 						  mBuilder.setContentText(statuses[1] + " unread posts in your thread");
 					  }
 					  
 				  } else {
 					  if (statuses[0] == 1) {
 						  mBuilder.setContentText(statuses[0] + " unread post");
 					  } else {
 						  mBuilder.setContentText(statuses[0] + " unread posts");
 					  }
 				  }
 				  if (sharedPref.getBoolean("vibrate_service", true)) {
 					  mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 					  mVibrator.vibrate(500);  
 				  }
				  
 			      // Creates an explicit intent for an Activity in your app
 				  Intent resultIntent = new Intent(this, RecentActivity.class);
 
 				  // The stack builder object will contain an artificial back stack for the
 				  // started Activity.
 				  // This ensures that navigating backward from the Activity leads out of
 				  // your application to the Home screen.
 				  TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 
 				  // Adds the back stack for the Intent (but not the Intent itself)
 				  stackBuilder.addParentStack(SettingsActivity.class);
 				  // Adds the Intent that starts the Activity to the top of the stack
 				  stackBuilder.addNextIntent(resultIntent);
 				  PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 				  mBuilder.setContentIntent(resultPendingIntent);
 				  NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 				  // mId allows you to update the notification later on.
 				  mNotificationManager.notify(0, mBuilder.build());
 			  }
 			  Log.d("jddebug", "notify");
 		  
 		  } else { // invalid api key
 			  
 			  Log.d("jddebug - UpdaterService", "invalid api key");
 			  NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
 			  mBuilder.setContentTitle("CSH Webnews");
			  mBuilder.setSmallIcon(R.drawable.notification_icon);
 			  mBuilder.setContentText("Invalid API Key");
 			  mBuilder.setAutoCancel(true);
 
 		      // Creates an explicit intent for an Activity in your app
 			  Intent resultIntent = new Intent(this, SettingsActivity.class);
 
 			  // The stack builder object will contain an artificial back stack for the
 			  // started Activity.
 			  // This ensures that navigating backward from the Activity leads out of
 			  // your application to the Home screen.
 			  TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 
 			  // Adds the back stack for the Intent (but not the Intent itself)
 			  stackBuilder.addParentStack(SettingsActivity.class);
 			  // Adds the Intent that starts the Activity to the top of the stack
 			  stackBuilder.addNextIntent(resultIntent);
 			  PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 			  mBuilder.setContentIntent(resultPendingIntent);
 			  NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 			  // mId allows you to update the notification later on.
 			  mNotificationManager.notify(0, mBuilder.build());
 		  }
 
 
 
 
 	  }
 	}
