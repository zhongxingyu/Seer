 package org.dodgybits.android.shuffle.service;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.widget.RemoteViews;
 import org.dodgybits.android.shuffle.R;
 import org.dodgybits.android.shuffle.model.Preferences;
 import org.dodgybits.android.shuffle.activity.SynchronizeActivity;
 import org.dodgybits.android.shuffle.server.tracks.SyncProgressListener;
 import org.dodgybits.android.shuffle.server.tracks.TracksSynchronizer;
 import org.dodgybits.android.shuffle.server.tracks.WebClient;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Morten
  * Date: 2010-jan-04
  * Time: 20:13:59
  * This service handles synchronization in the background.
  */
 public class SynchronizationService extends Service implements SyncProgressListener {
     private NotificationManager mNotificationManager;
     private Timer synchronizationTimer = null;
     private Timer preferencersTimer = null;
     private long interval = 0;
     private TracksSynchronizer synchronizer = null;
     private RemoteViews contentView;
 
     @Override
     public IBinder onBind(Intent intent) {
 
         return null;
     }
 
     @Override
     public void onCreate() {
         String ns = Context.NOTIFICATION_SERVICE;
         mNotificationManager = (NotificationManager) getSystemService(ns);
 
         contentView = new RemoteViews(getPackageName(), R.layout.synchronize_notification_layout);
 
         contentView.setProgressBar(R.id.progress_horizontal, 100, 50, true);
         contentView.setImageViewResource(R.id.image, R.drawable.shuffle_icon);
 
 
 
         preferencersTimer = new Timer();
         preferencersTimer.scheduleAtFixedRate(new TimerTask() {
 
             public void run() {
 
                 updateTimers();
 
 
             }
 
         }, 0, 5 * 60 * 1000);
     }
 
     private void updateTimers() {
         int newInterval = calcualteIntervalInMiliseconds(Preferences.getTracksInterval(this));
         if (interval != newInterval) {
             interval = newInterval;
             scheduleSynchronization();
         }
     }
 
     private int calcualteIntervalInMiliseconds(int selected) {
 
         switch (selected) {
             case 1:
                 return 30 * 60 * 1000;
             case 2:
                 return 60 * 60 * 1000;
             case 3:
                 return 2 * 60 * 60 * 1000;
             case 4:
                 return 3 * 60 * 60 * 1000;
             default:
                 return 0;
         }
     }
 
     private void scheduleSynchronization() {
         if (synchronizationTimer != null)
             synchronizationTimer.cancel();
         if (interval != 0) {
             synchronizationTimer = new Timer();
             synchronizationTimer.scheduleAtFixedRate(new TimerTask() {
 
                 public void run() {
 
                     synchronize();
 
 
                 }
 
             }, 0, interval);
         }
     }
 
     private void synchronize() {
 
         try {
             synchronizer = TracksSynchronizer.getActiveSynchronizer(this);
         } catch (WebClient.ApiException ignored) {
 
         }  
         if (synchronizer != null) {
             synchronizer.RegisterListener(this);
 
         }
        scheduleSynchronization();
 
 
         if (synchronizer != null && Preferences.validateTracksSettings(this)) {
             if (synchronizer.getStatus() != AsyncTask.Status.RUNNING)
                 synchronizer.execute();
 
         }
 
     }
 
     private static final int NOTIFICATION_ID = 1;
 
     private void createNotification() {
 
         int icon = R.drawable.shuffle_icon;
         CharSequence tickerText = this.getText(R.string.app_name);
         long when = System.currentTimeMillis();
 
         Notification notification = new Notification(icon, tickerText, when);
 
         Intent notificationIntent = new Intent(this, SynchronizeActivity.class);
 
         notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         notification.contentView = contentView;
 
         mNotificationManager.notify(NOTIFICATION_ID, notification);
 
     }
 
     private void clearNotification() {
         mNotificationManager.cancel(NOTIFICATION_ID);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         if (synchronizationTimer != null)
             synchronizationTimer.cancel();
         if (preferencersTimer != null)
             preferencersTimer.cancel();
         if (synchronizer != null) {
             synchronizer.unRegisterListener(this);
         }
     }
 
     @Override
     public void progressUpdate(Progress progress) {
         if (progress.getProgressPercent() == 100) clearNotification();
         if (progress.getProgressPercent() == 0) createNotification();
 
 
     }
 }
