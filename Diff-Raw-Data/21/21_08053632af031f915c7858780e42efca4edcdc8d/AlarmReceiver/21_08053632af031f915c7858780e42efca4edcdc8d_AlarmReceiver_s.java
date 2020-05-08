 package com.MetroSub.utils;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.widget.Toast;
 import com.MetroSub.R;
 import com.MetroSub.activity.MapActivity;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: Shenil
  * Date: 8/17/13
  * Time: 7:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class AlarmReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent)
     {
         Toast.makeText(context, "Subway alert!",Toast.LENGTH_SHORT).show();
         Bundle extras=intent.getExtras();
 
         String contentText =extras.getString("note");
          int NOTIFICATION_ID = 1;
 
 
         // Only used if we don't want the notification click to launch the app
         PendingIntent contentIntent = PendingIntent.getActivity(context,
                 NOTIFICATION_ID,
                 new Intent(context, AlarmReceiver.class), 0);
 
 
         PendingIntent pIndent = PendingIntent.getActivity(context, 1, new Intent(context, MapActivity.class), 0);
 
         // Build notification
 
         NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
 
         builder.setContentTitle("Subway Alert")
                 .setContentText(contentText)
                .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.metro_icon_transparent)).getBitmap())
                 .setSmallIcon(R.drawable.metro_icon_transparent)
                 .setContentIntent(pIndent);
         builder.setDefaults(Notification.DEFAULT_ALL);
         builder.setAutoCancel(true);
         builder.setWhen(System.currentTimeMillis());
 
              /*
         NotificationCompat notification = new NotificationCompat.Builder(context)
                 .setContentTitle("Subway Alert")
                 .setContentText(contentText)
                 .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.metro_icon_transparent)).getBitmap())
                 .setSmallIcon(R.drawable.metro_icon_transparent)
                 .setContentIntent(pIndent).build();     */
 
         NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
 
         // Hide the notification after its selected
         /*
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         // Vibrate/ring/light for notification
         notification.defaults |= Notification.DEFAULT_ALL;
         //notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE; */
 
         notificationManager.notify(0, builder.build());
     }
 }
