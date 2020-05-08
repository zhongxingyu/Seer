 package se.slide.timy;
 
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Set;
 
 public class AlarmReceiver extends BroadcastReceiver {
     
     public final static int NOTIFICATION_ID = 0;
 
     @Override
     public void onReceive(Context context, Intent intent) {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
         boolean reminder = sharedPreferences.getBoolean("remind_me_audio", false);
         Set<String> prefDays = sharedPreferences.getStringSet("remind_me_when", null); 
         
         if (!fireAlarmToday(prefDays))
             return;
         
         if (reminder)
             playAlarm(context);
         
         showNotification(context);
         
     }
     
     private void playAlarm(Context context) {
         Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
         if(alert == null){
             // alert is null, using backup
             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
             if(alert == null){  // I can't see this ever being null (as always have a default notification) but just incase
                 // alert backup is null, using 2nd backup
                 alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);               
             }
         }
         
        final MediaPlayer player = new MediaPlayer();
         try {
            player.setDataSource(context, alert);
             
             final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
             if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                       player.setAudioStreamType(AudioManager.STREAM_ALARM);
                       player.setLooping(false);
                       player.prepare();
                       player.start();
              }
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         Runnable stopSoundRunnable = new Runnable() {
 
             @Override
             public void run() {
                  player.stop();
             }
         };
         
        int duration = player.getDuration();
 
         Handler handler = new Handler();
         handler.postDelayed(stopSoundRunnable, duration);
         
     }
     
     private void showNotification(Context context) {
         NotificationCompat.Builder mBuilder =
                 new NotificationCompat.Builder(context)
                 .setSmallIcon(R.drawable.ic_launcher)
                 .setAutoCancel(true)
                 .setContentTitle(context.getString(R.string.notification_title))
                 .setContentText(context.getString(R.string.notification_text));
         
         // Creates an explicit intent for an Activity in your app
         Intent resultIntent = new Intent(context, MainActivity.class);
 
         // The stack builder object will contain an artificial back stack for the
         // started Activity.
         // This ensures that navigating backward from the Activity leads out of
         // your application to the Home screen.
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
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
             (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
     }
 
     private boolean fireAlarmToday(Set<String> prefDays) {
         if (prefDays == null)
             return false;
         
         Calendar checkDay = Calendar.getInstance();
         int dayOfWeek = checkDay.get(Calendar.DAY_OF_WEEK);
         
         for (String day : prefDays) {
             if (dayOfWeek == Calendar.MONDAY && day.equals("1"))
                 return true;
             if (dayOfWeek == Calendar.TUESDAY && day.equals("2"))
                 return true;
             if (dayOfWeek == Calendar.WEDNESDAY && day.equals("3"))
                 return true;
             if (dayOfWeek == Calendar.THURSDAY && day.equals("4"))
                 return true;
             if (dayOfWeek == Calendar.FRIDAY && day.equals("5"))
                 return true;
             if (dayOfWeek == Calendar.SATURDAY && day.equals("6"))
                 return true;
             if (dayOfWeek == Calendar.SUNDAY && day.equals("7"))
                 return true;
         }
         
         return false;
     }
 }
