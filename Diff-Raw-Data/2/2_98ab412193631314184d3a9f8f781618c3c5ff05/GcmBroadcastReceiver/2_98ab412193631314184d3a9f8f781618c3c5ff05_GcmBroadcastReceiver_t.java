 package uk.me.tom_fitzhenry.motionremote.gcm;
 
 import uk.me.tom_fitzhenry.motionremote.ChooseStreamActivity;
 import uk.me.tom_fitzhenry.motionremote.R;
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 
 public class GcmBroadcastReceiver extends BroadcastReceiver {
     private static final int NOTIFICATION_ID = 1;
     
     @Override
     public void onReceive(Context context, Intent intent) {
         sendNotification(context, intent.getExtras().getString("msg"));
         setResultCode(Activity.RESULT_OK);
     }
 
     private void sendNotification(Context ctx, String msg) {
     	NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
 
         PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, new Intent(ctx, ChooseStreamActivity.class), 0);
 
         Notification notification = new NotificationCompat.Builder(ctx)
        	.setSmallIcon(R.drawable.security_camera)
         	.setContentTitle("MotionRemote")
         	.setContentText(msg)
         	.setContentIntent(contentIntent)
         	.setAutoCancel(true)
         	.build();
         
         mNotificationManager.notify(NOTIFICATION_ID, notification);
     }
 }
