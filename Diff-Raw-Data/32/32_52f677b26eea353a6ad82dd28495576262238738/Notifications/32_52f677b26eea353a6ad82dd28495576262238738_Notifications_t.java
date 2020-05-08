 package net.meneame.fisgodroid;
 
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TaskStackBuilder;
 import android.content.Context;
 import android.content.Intent;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.support.v4.app.NotificationCompat;
 
 public class Notifications
 {
    private static boolean msOnForeground = false;
     private static int msTheyMentionedMeId = 1;
     
     private static boolean isRunningInForeground ( Context context )
     {
        return msOnForeground;
    }
    
    public static void setOnForeground ( boolean onForeground )
    {
        msOnForeground = onForeground;
     }
     
     private static void sendNotification ( Context context, int notificationId, Notification notification )
     {
         if ( !isRunningInForeground(context) )
         {
             NotificationManager mNotificationManager =
                     (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
             mNotificationManager.notify(notificationId, notification);
         }
     }
     
     public static void theyMentionedMe ( Context context, String who, String message )
     {
         String title = context.getResources().getString(R.string.they_mentioned_me_title);
         title = String.format(title, who);
         
         Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.ding);
         
         NotificationCompat.Builder mBuilder =
                 new NotificationCompat.Builder(context)
                 .setSmallIcon(R.drawable.ic_launcher)
                 .setContentTitle(title)
                 .setContentText(message)
                 .setSound(soundUri)
                 .setAutoCancel(true)
                 .setLights(0xffff8c00, 500, 1000);
         
         NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
         bigTextStyle.setBigContentTitle(title);
         bigTextStyle.bigText(message);
         mBuilder.setStyle(bigTextStyle);
         
         // Creates an explicit intent for ChatActivity
         Intent resultIntent = new Intent(context, ChatActivity.class);
         resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
         resultIntent.setAction("android.intent.action.MAIN");
         
         PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         mBuilder.setContentIntent(resultPendingIntent);
 
         sendNotification(context, msTheyMentionedMeId, mBuilder.build());
 
         ++msTheyMentionedMeId;
     }
 }
