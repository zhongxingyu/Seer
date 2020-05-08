 package org.xmms2.server;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.support.v4.app.NotificationCompat;
 import android.widget.RemoteViews;
 
 /**
  * @author Eclipser
  */
 public class NotificationHandler implements PlaybackStatusListener, MetadataListener
 {
     private static final String ACTION_TOGGLE_PLAYBACK = "org.xmms2.server.action.TOGGLE_PLAYBACK";
     private static final String ACTION_NEXT = "org.xmms2.server.action.NEXT";
     private static final String ACTION_STOP_PLAYBACK = "org.xmms2.server.action.STOP_PLAYBACK";
 
     private final Context context;
     private final RemoteViews notificationView;
     private final NotificationUpdater updater;
     private final NotificationCompat.Builder builder;
 
     private PlaybackStatus status = PlaybackStatus.STOPPED;
 
     private final BroadcastReceiver notificationActionReceiver = new BroadcastReceiver()
     {
         @Override
         public void onReceive(Context context, Intent intent)
         {
             if (ACTION_TOGGLE_PLAYBACK.equals(intent.getAction())) {
                 if (status == PlaybackStatus.PLAYING) {
                     Server.pause();
                 } else {
                     Server.play();
                 }
             } else if (ACTION_STOP_PLAYBACK.equals(intent.getAction())) {
                 Server.stop();
             } else if (ACTION_NEXT.equals(intent.getAction())) {
                 Server.next();
             }
         }
     };
 
     public NotificationHandler(Context context, NotificationUpdater updater)
     {
         this.updater = updater;
         this.context = context.getApplicationContext();
         Intent intent = new Intent(Server.ACTION_START_CLIENT);
         intent.putExtra("address", "tcp://localhost:9667");
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         PendingIntent clientStartIntent = PendingIntent.getActivity(this.context, 0, intent, 0);
 
         notificationView = new RemoteViews(this.context.getPackageName(), R.layout.notification);
         Intent toggle = new Intent(ACTION_TOGGLE_PLAYBACK);
         notificationView.setOnClickPendingIntent(R.id.toggle, PendingIntent.getBroadcast(this.context, 0, toggle, 0));
 
         Intent next = new Intent(ACTION_NEXT);
         notificationView.setOnClickPendingIntent(R.id.next, PendingIntent.getBroadcast(this.context, 0, next, 0));
 
         Intent stop = new Intent(ACTION_STOP_PLAYBACK);
         notificationView.setOnClickPendingIntent(R.id.close, PendingIntent.getBroadcast(this.context, 0, stop, 0));
 
         IntentFilter notificationActionFilter = new IntentFilter();
         notificationActionFilter.addAction(ACTION_TOGGLE_PLAYBACK);
         notificationActionFilter.addAction(ACTION_NEXT);
         notificationActionFilter.addAction(ACTION_STOP_PLAYBACK);
         this.context.registerReceiver(notificationActionReceiver, notificationActionFilter);
 
         builder = new NotificationCompat.Builder(this.context);
         builder.setContentIntent(clientStartIntent);
         builder.setContent(notificationView);
         builder.setOngoing(true);
         builder.setSound(null);
         builder.setSmallIcon(R.drawable.notification);
     }
 
     @Override
     public void metadataChanged(MetadataHandler metadataHandler)
     {
         builder.setContentTitle(metadataHandler.getTitle());
         notificationView.setTextViewText(R.id.title, metadataHandler.getTitle());
         builder.setContentText(metadataHandler.getArtist());
         notificationView.setTextViewText(R.id.artist, metadataHandler.getArtist());
         builder.setTicker(metadataHandler.getTicker());
         Bitmap coverArt = metadataHandler.getCoverArt();
         builder.setLargeIcon(coverArt);
         if (coverArt != null) {
             notificationView.setImageViewBitmap(R.id.icon, coverArt);
         } else {
             notificationView.setImageViewResource(R.id.icon, R.drawable.notification);
         }

        updater.updateNotification(builder.build());
     }
 
     @Override
     public void playbackStatusChanged(PlaybackStatus newStatus)
     {
         status = newStatus;
         if (status != PlaybackStatus.STOPPED) {
             notificationView.setImageViewResource(R.id.toggle, status == PlaybackStatus.PLAYING ? R.drawable.pause : R.drawable.play);
             builder.setContentInfo(status.getLiteralString(context.getResources()));
 
             updater.updateNotification(builder.build());
         } else {
             updater.removeNotification();
         }
     }
 
     public void unregisterReceiver()
     {
         context.unregisterReceiver(notificationActionReceiver);
     }
 }
