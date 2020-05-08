 /**
 *	NotifyService.java
 *
 *	The service responsible for showing notifications.
 *
 *	@author Johan
 *	@copyright (c) 2012 Johan Brook, Robin Andersson, Lisa Stenberg, Mattias Henriksson
 *	@license MIT
 */
 
 package se.chalmers.watchme.notifications;
 
 import java.io.Serializable;
 
 import se.chalmers.watchme.activity.MovieDetailsActivity;
 import se.chalmers.watchme.model.Movie;
 import android.R;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.Binder;
 import android.util.Log;
 
 public class NotifyService extends Service {
 	
 	public class ServiceBinder extends Binder {
 		NotifyService getService() {
 			return NotifyService.this;
 		}
 	}
 	
 	/** Intent key extra */
 	public static final String INTENT_NOTIFY = "se.chalmers.watchme.notifications.INTENT_NOTIFY";
 	
 	/** Intent key extra for a Movie */
 	public static final String INTENT_MOVIE = "se.chalmers.watchme.notifications.INTENT_MOVIE";
 	
 	private NotificationManager manager;
 	private IBinder binder = new ServiceBinder();
 	
 
 	@Override
 	public void onCreate() {
 		// Fetch the system's notification service manager
 		this.manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 	}
 	
 	@Override
 	public void onDestroy() {
 		Log.i("Custom", "** Destroy notifications");
 		this.manager.cancelAll();
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startID) {
 		Log.i("Custom", "Received start id " + startID + ": " + intent.getBooleanExtra(INTENT_NOTIFY, false));
 		
 		// If this service was started by AlarmTask, show a notification
 		if(intent.getBooleanExtra(INTENT_NOTIFY, false)) {
 			showNotification( (Movie) intent.getSerializableExtra(INTENT_MOVIE));
 		}
 		
 		// If the service is killed, it's no big deal as we've delivered our notification
 		return START_NOT_STICKY;
 	}
 	
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return this.binder;
 	}
 
 	
 	private void showNotification(Notifiable obj) {
 		
 		int id = obj.getNotificationId();
 
 		// The content of the notification box
 		CharSequence title = getString(se.chalmers.watchme.R.string.notification_title);
		CharSequence text = "'" + obj.toString() + "' "+getString(se.chalmers.watchme.R.string.notification_suffix);
 		int icon = R.drawable.ic_popup_reminder;
 		
 		// The intent to launch an activity if the user presses this notification
 		Intent detailsIntent = new Intent(this, MovieDetailsActivity.class);
 		detailsIntent.putExtra(MovieDetailsActivity.MOVIE_EXTRA, (Serializable) obj);
 		
 		PendingIntent pending = PendingIntent.getActivity(this, 0, detailsIntent, 0);
 
 		// Build the notification
 
 		Notification notification = new Notification.Builder(this)
 			.setSmallIcon(icon)
 			.setContentIntent(pending)
 			.setContentTitle(title)
 			.setContentText(text)
 			.setAutoCancel(true)
 			.setWhen(System.currentTimeMillis())
 			.build();
 		
 		// Send the notification to the system along with our id
 		this.manager.notify(id, notification);
 		
 		Log.i("Custom", "Sent notification for movie "+ obj +", with id: "+id);
 	}
 
 }
