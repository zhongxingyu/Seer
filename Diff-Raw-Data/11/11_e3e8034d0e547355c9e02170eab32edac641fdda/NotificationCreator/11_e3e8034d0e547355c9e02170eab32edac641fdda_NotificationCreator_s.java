 package se.chalmers.dat255.craycray.notifications;
 
 import se.chalmers.dat255.craycray.R;
 import se.chalmers.dat255.craycray.activity.MainActivity;
 import se.chalmers.dat255.craycray.util.Constants;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 /**
  * Creates notifications.
  */
 public class NotificationCreator {
 
 	private Context ctx;
 
 	/**
 	 * Creates a NotificationCreator with the given Context.
 	 * @param context
 	 */
 	public NotificationCreator(Context ctx){
 		this.ctx = ctx;
 	}
 
 	/**
 	 * Creates a notification telling the user
 	 * CrayCray has died.
 	 */
 	public Notification createDeadNotification(){	
 		//Set activity shown when clicked.
 		Intent intent = new Intent(ctx, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(ctx, Constants.DEAD_NOTI, intent, 0);
 
 		//Make notification.
 		Notification noti = new Notification.Builder(ctx)
 		.setContentTitle("You're a bad person")
 		.setContentText("Your sweet CrayCray died.")
 		.setSmallIcon(R.drawable.dead_baby)
 		.setContentIntent(pIntent)
 		.build();	
 		
 		noti.flags = Notification.FLAG_AUTO_CANCEL;
 		
 		return noti;
 	}
 
 	/**
 	 * Creates a notification telling the user
 	 * CrayCray is Dirty.
 	 */
 	public Notification createDirtyNotification(){
 		//Set activity shown when clicked.
 		Intent intent = new Intent(ctx, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(ctx, Constants.DIRTY_NOTI, intent, 0);
 		//Make notification.
 		Notification noti = new Notification.Builder(ctx)
 		.setContentTitle("I'm Dirty")
 		.setContentText("*hihi*")
 		.setSmallIcon(R.drawable.dirty_baby)
 		.setContentIntent(pIntent)
 		.build();	
 		
 		noti.flags = Notification.FLAG_AUTO_CANCEL;
 
 		return noti;
 	}
 	
 	/**
 	 * Creates a notification telling the user
 	 * CrayCray is Ill.
 	 */
 	public Notification createIllNotification(){
 		//Set activity shown when clicked.
 		Intent intent = new Intent(ctx, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(ctx, Constants.ILL_NOTI, intent, 0 );
 		//Make notification.
 		Notification noti = new Notification.Builder(ctx)
 		.setContentTitle("I'm feeling sick :(")
 		.setContentText("Please, please cuuuuuure me!")
 		.setSmallIcon(R.drawable.sick_baby)
 		.setContentIntent(pIntent)
 		.build();	
 		
 		noti.flags = Notification.FLAG_AUTO_CANCEL;
 		
 		return noti;
 	}
 	
 
 }
