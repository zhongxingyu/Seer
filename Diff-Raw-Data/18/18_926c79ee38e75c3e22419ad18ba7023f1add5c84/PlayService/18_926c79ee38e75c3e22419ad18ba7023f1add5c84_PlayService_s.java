 package ru.moskva.fm;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.nfc.Tag;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.IOException;
 
 public class PlayService extends Service implements MediaPlayer.OnCompletionListener {
 
     private NotificationManager mNM;
     static final String TAG = "Moskva.fm";
 
     private Channel currentChannel;
 
     private Track nextTrack;
     private File trackFile;
     private File playFile;
 
     static boolean isStarted = false;
 
     private boolean isPlaying = false;
 
     private MediaPlayer player;
 
     static final int NOTIFICATION = R.string.app_name;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onStart(Intent intent, int startId) {
 
         isStarted = true;
 
         Log.d(TAG, "PlayService::onStart");
 
         Toast toast = Toast.makeText(this, R.string.start_service_toast, Toast.LENGTH_SHORT);
 
         toast.setGravity(Gravity.CENTER, 0, 0);
 
         toast.show();
 
         currentChannel = new Channel("4015");
 
         long fromTime = intent.getLongExtra("datetime", 0);
 
         if(fromTime > 0) {
 
             nextTrack = currentChannel.getTrackFromTime(fromTime);
 
             downloadTrack(nextTrack);
 
             play();
 
         } else {
             this.stopSelf();
         }
 
     }
 
     private void play() {
 
         if (trackFile != null) {
 
             isPlaying = true;
 
             playFile = trackFile;
 
             trackFile = null;
 
             player.reset();
 
             nextTrack = currentChannel.getNextTrack(nextTrack);
 
             new Thread() {
                 public void run() {
                     downloadTrack(nextTrack);
                 }
             }.start();
 
             try {
 
                 player.setDataSource(playFile.getAbsolutePath());
 
                 player.prepare();
 
 
                 player.seekTo(2000);
 
                 player.start();
 
                 Log.d(TAG, "Playing: " + playFile.getAbsolutePath());
 
             } catch (IOException e) {
                 Log.e(TAG, e.getMessage());
             }
 
         } else {
             this.stopSelf();
         }
 
     }
 
     private void downloadTrack(Track current) {
 
         Track next = currentChannel.getNextTrack(current);
 
         try {
 
             trackFile = Utils.downloadFile(next.url);
 
 //            if (!isPlaying) {
 //                play();
 //            }
 
         } catch (IOException e) {
             Log.d(TAG, e.getMessage());
         }
 
     }
 
     @Override
     public void onCreate() {
         mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         showNotification();
 
         if (player == null) {
             player = new MediaPlayer();
             player.setOnCompletionListener(this);
         }
 
     }
 
     @Override
     public void onDestroy() {
 
         isStarted = false;
 
         Log.d(TAG, "PlayService::onDestroy");
 
         Toast toast = Toast.makeText(this, R.string.stop_service_toast, Toast.LENGTH_SHORT);
 
         toast.setGravity(Gravity.CENTER, 0, 0);
 
         toast.show();
 
         mNM.cancel(NOTIFICATION);
 
         if (player != null) {
             player.release();
             player = null;
         }
 
         if (playFile != null) {
             Log.d(TAG, "Deleting file: " + playFile.getAbsolutePath());
             playFile.delete();
         }
 
         if (trackFile != null) {
             Log.d(TAG, "Deleting file: " + trackFile.getAbsolutePath());
             trackFile.delete();
         }
 
 
         super.onDestroy();
     }
 
     private void showNotification() {
         // In this sample, we'll use the same text for the ticker and the expanded notification
         CharSequence text = getText(R.string.app_name);
 
         // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                 System.currentTimeMillis());
 
         // The PendingIntent to launch our activity if the user selects this notification
         PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, ListenRadio.class), 0);
 
         // Set the info for the views that show in the notification panel.
         notification.setLatestEventInfo(this, getText(R.string.start_service_toast),
                 text, contentIntent);
 
         // Send the notification.
         mNM.notify(NOTIFICATION, notification);
     }
 
 
     public void onCompletion(MediaPlayer mediaPlayer) {
 
         final File fileToDelete = playFile;
 
         new Thread() {
             public void run() {
                 Log.d(TAG, "Deleting file: " + fileToDelete.getAbsolutePath());
                 fileToDelete.delete();
             }
         }.start();
 
 
         isPlaying = false;
 
         play();
 
     }
 }
