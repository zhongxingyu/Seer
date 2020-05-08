 package com.deepmine.by;
 
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.IBinder;
 import android.util.Log;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.content.Context;
 
 import com.androidquery.AQuery;
 import com.androidquery.callback.AjaxCallback;
 import com.androidquery.callback.AjaxStatus;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created by zyr3x on 01.10.13.
  */
 public class RadioService extends Service {
     public static MediaPlayer mediaPlayer = null;
     public static String TAG = "DEEPMINE:RadioService";
     public static boolean isStartService = false;
     private AQuery aq = new AQuery(this);
     private static String lastTitle = "";
     public static Timer timer = new Timer();
     public static final int NOTIFICATION_ID = 1;
     private static NotificationManager mNotificationManager = null;
 
     @Override
     public void onCreate() {
         Log.d(TAG, "START SERVICE");
         isStartService = true;
         try {
             mediaPlayer = new MediaPlayer();
             mediaPlayer.setDataSource("http://deepmine.by:8000/deepmine");
             mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
             mediaPlayer.prepare();
             mediaPlayer.start();
         } catch (Exception e) {
             Log.d(TAG, "Exception in streaming mediaplayer e = " + e);
         }
 
         updateTitle();
 
         super.onCreate();
     }
 
     protected void updateTitle()
     {
         class UpdateTask extends TimerTask {
 
             public void run() {
                 if(isStartService)
                 {
                     aq.ajax("http://deepmine.by/d/index.php/ajaxRadioTitle", JSONObject.class, new AjaxCallback<JSONObject>() {
 
                         @Override
                         public void callback(String url, JSONObject json, AjaxStatus status) {
                             if (json != null) {
                                 try
                                 {
 
                                     if(!lastTitle.equals(json.getString("title")))
                                     {
                                         lastTitle = json.getString("title");
 
                                         if(mNotificationManager!=null)
                                             updateNotification(json.getString("artist"),json.getString("track"));
                                         else
                                             initNotification(json.getString("artist"),json.getString("track"));
                                     }
 
                                 }
                                 catch (JSONException e)
                                 {
                                     Log.d(TAG, "Exception parse");
                                 }
                             }
                         }
                     });
                 }
 
             }
         }
 
         TimerTask updateTask = new UpdateTask();
         timer.scheduleAtFixedRate(updateTask, 0, 5000);
     }
 
 
     @Override
     public IBinder onBind(Intent intent) {
         // TODO Auto-generated method stub
         return null;
     }
 
     @Override
     public void onDestroy() {
         Log.d(TAG, "STOP SERVICE");
         stop();
         super.onDestroy();
     }
 
     public static void stop()
     {
         isStartService = false;
         lastTitle = "";
 
         if(mNotificationManager!=null)
             mNotificationManager.cancel(NOTIFICATION_ID);
 
         if(timer!=null)
             timer.cancel();
 
         if(mediaPlayer!=null)
             mediaPlayer.stop();
     }
 
     public static boolean isPlaying()
     {
        if(mediaPlayer!= null)
            return  isStartService;
         else
            return false;
     }
 
     // Create Notification
     private void initNotification(String title1, String title2) {
 
         mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 
         Notification notification = new Notification(R.drawable.ic_media_play, "DEEPMINE", System.currentTimeMillis());
         notification.flags = Notification.FLAG_ONGOING_EVENT;
 
         Intent notificationIntent = new Intent(this, MainActivity.class);
         PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,notificationIntent, 0);
         notification.setLatestEventInfo(getApplicationContext(), title1, title2, contentIntent);
 
         mNotificationManager.notify(NOTIFICATION_ID, notification);
     }
 
     private void updateNotification(String title1, String title2) {
         Notification notification = new Notification(R.drawable.ic_media_play, "DEEPMINE", System.currentTimeMillis());
         notification.flags = Notification.FLAG_ONGOING_EVENT;
         Intent notificationIntent = new Intent(this, MainActivity.class);
         PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,notificationIntent, 0);
         notification.setLatestEventInfo(getApplicationContext(), title1, title2, contentIntent);
 
         mNotificationManager.notify(NOTIFICATION_ID, notification);
     }
 
 
 }
