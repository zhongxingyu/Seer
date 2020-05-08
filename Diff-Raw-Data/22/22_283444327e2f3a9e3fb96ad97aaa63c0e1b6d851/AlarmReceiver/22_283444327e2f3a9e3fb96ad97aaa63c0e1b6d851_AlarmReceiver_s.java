 package de.team06.psychoapp;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.media.RingtoneManager;
 import android.net.Uri;
 
 /**
  * Created by Andi on 30.06.13.
  */
 
public class AlarmReceiver extends BroadcastReceiver
{
     @Override
    public void onReceive(Context ourAppScope, Intent intentFromAlarmManager)
    {
         /*
             make vibrate pattern
             Start without a delay
             Vibrate for 250 milliseconds
             Sleep for 800 milliseconds
          */
         long[] vibratePattern = {0, 1500, 800};
 
         //Define sound URI
         Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
 
         NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ourAppScope);
         mBuilder.setSmallIcon(R.drawable.ic_launcher);
         mBuilder.setContentTitle("Psycho-App");
         mBuilder.setContentText("Bitte soziale Kontakte eintragen.");
         mBuilder.setSound(soundUri);
         mBuilder.setVibrate(vibratePattern);
         mBuilder.setAutoCancel(true);
 
         // Creates an explicit intent for an Activity in your app
         Intent resultIntent = new Intent(ourAppScope, MainActivity.class);
         PendingIntent resultPendingIntent = PendingIntent.getActivity(ourAppScope, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
         mBuilder.setContentIntent(resultPendingIntent);
 
        NotificationManager mNotificationManager = (NotificationManager)ourAppScope.getSystemService(Context.NOTIFICATION_SERVICE);
         // mId allows you to update the notification later on.
         mNotificationManager.notify(0, mBuilder.build());
     }
 }
