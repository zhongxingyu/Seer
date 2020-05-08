 package org.k3x.vemprarua;
 
 import java.util.List;
 
 import org.k3x.vemprarua.api.LocationAPI;
 import org.k3x.vemprarua.api.LocationAPIHandler;
 import org.k3x.vemprarua.model.FieldError;
 import org.k3x.vemprarua.model.User;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 import com.google.android.gcm.GCMBaseIntentService;
 
 public class GCMIntentService extends GCMBaseIntentService implements LocationAPIHandler {
 
 	private static final int PUSH_TYPE_FULLSCREEN_INFO = 1;
 	private static final int PUSH_TYPE_VEMPRARUA = 2;
 
 	@Override
 	protected void onRegistered(Context context, String regId) {
 		Log.i ("GCM_ONREGISTERED", "REGISTERED " + regId);
 		User user = User.getUser(this);
 		user.regid = regId;
 		
 		if(user.id != null) {
 			LocationAPI api = new LocationAPI();
 			api.update(user, this);
 		}
 	}
 
 	/** 
 	 * Called when the device tries to register or unregister,
 	 * but GCM returned an error. Typically, there is nothing
 	 * to be done other than evaluating the error
 	 * (returned by errorId) and trying to fix the problem.
 	 */
 	@Override
 	protected void onError(Context context, String errorId) {
 		Log.i ("GCM_ONERROR", "ERROR " + errorId);
 	}
 
 	/**
 	 * Called when your server sends a message to GCM,
 	 * and GCM delivers it to the device.
 	 * If the message has a payload, its contents are
 	 * available as extras in the intent.
 	 */
 	@Override
 	protected void onMessage(Context context, Intent intent) {
 		Log.i ("GCM_ONMESSAGE", "MESSAGE " + intent.toString());
 		Log.i ("GCM_ONMESSAGE", "EXTRAS " + intent.getExtras().toString());
 		
 		int code = Integer.parseInt(intent.getStringExtra("code"));
 		switch (code) {
 		case PUSH_TYPE_FULLSCREEN_INFO:
 			Intent intentFullscreen = new Intent(this, FullscreenNotificationActivity.class);
 			intentFullscreen.putExtra("title", intent.getStringExtra("title"));
			intentFullscreen.putExtra("text", intent.getStringExtra("text"));
 			intentFullscreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(intentFullscreen);
 			break;
 		case PUSH_TYPE_VEMPRARUA:
 			
 			String title = intent.getStringExtra("title");
 			String message = intent.getStringExtra("message");
 			int zoom = Integer.parseInt(intent.getStringExtra("zoom"));
 			double latitude = Double.parseDouble(intent.getStringExtra("latitude"));
 			double longitude = Double.parseDouble(intent.getStringExtra("longitude"));
 			
 			
 
 			NotificationCompat.Builder mBuilder =
 			        new NotificationCompat.Builder(this)
 			        .setSmallIcon(R.drawable.ic_launcher)
 			        .setContentTitle(title)
 			        .setContentText(message)
 			        .setAutoCancel(true);
 			// Creates an explicit intent for an Activity in your app
 			Intent resultIntent = new Intent(this, MainActivity.class);
 			resultIntent.putExtra("title", title);
 			resultIntent.putExtra("message", message);
 			resultIntent.putExtra("zoom", zoom);
 			resultIntent.putExtra("latitude", latitude);
 			resultIntent.putExtra("longitude", longitude);
 
 			// The stack builder object will contain an artificial back stack for the
 			// started Activity.
 			// This ensures that navigating backward from the Activity leads out of
 			// your application to the Home screen.
 			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 			// Adds the back stack for the Intent (but not the Intent itself)
 			stackBuilder.addParentStack(MainActivity.class);
 			// Adds the Intent that starts the Activity to the top of the stack
 			stackBuilder.addNextIntent(resultIntent);
 			PendingIntent resultPendingIntent =
 			        stackBuilder.getPendingIntent(
 			            0,
 			            PendingIntent.FLAG_UPDATE_CURRENT
 			        );
 			mBuilder.setContentIntent(resultPendingIntent);
 			NotificationManager mNotificationManager =
 			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 			// mId allows you to update the notification later on.
 			mNotificationManager.notify(PUSH_TYPE_VEMPRARUA, mBuilder.build());
 			
 			break;
 			
 		case -1:
 		default:
 			// ignore
 			break;
 		}
 	}
 
 	/**
 	 * Called after the device has been unregistered from GCM.
 	 * Typically, you should send the regid to the
 	 * server so it unregisters the device.
 	 */
 	@Override
 	protected void onUnregistered(Context context, String regId) {
 		Log.i ("GCM_ONUNREGISTERED", "UNREGISTERED " + regId);
 		User user = User.getUser(this);
 		user.regid = "";
 		
 		LocationAPI api = new LocationAPI();
 		api.update(user, this);
 	}
 
 	@Override
 	public void onCreated(boolean success, User user, List<FieldError> errors) {
 		// not used
 	}
 
 	@Override
 	public void onUpdated(boolean success, User user, List<FieldError> errors) {
 		// nice!
 	}
 
 	@Override
 	public void onListed(boolean success, int total, List<User> users,
 			List<FieldError> errors) {
 		// not used
 	}
 }
