 package com.ouchadam.fang.audio;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.os.Binder;
 import android.os.IBinder;
 
 import com.ouchadam.fang.notification.PodcastPlayerNotificationEventBroadcaster;
 import com.ouchadam.fang.presentation.controller.AudioFocusManager;
 import com.ouchadam.fang.presentation.controller.PlayerEvent;
 
 public class AudioService extends Service implements ServiceManipulator {
 
     private final LocalBinder binder;
 
     private PlayerEventReceiver playerEventReceiver;
     private AudioServiceBinder.OnStateSync listener;
     private ExternalReceiver externalReceiver;
     private PlayerHandler playerHandler;
 
     public AudioService() {
         binder = new LocalBinder();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
 
     @Override
     public void stop() {
         stopSelf();
     }
 
     public class LocalBinder extends Binder {
         public AudioService getService() {
             return AudioService.this;
         }
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         initReceivers();
        playerHandler = PlayerHandler.from(this, onSync, onComplete, this);
     }
 
     private final PlayerHandler.AudioSync onSync = new PlayerHandler.AudioSync() {
         @Override
         public void onSync(long itemId, PlayerEvent playerEvent) {
             sync(itemId, playerEvent);
         }
     };
 
     private void sync(long itemId, PlayerEvent playerEvent) {
         if (isWithinApp()) {
             sync();
         } else {
             broadcastToNotification(itemId, playerEvent);
         }
     }
 
     private void broadcastToNotification(long itemId, PlayerEvent playerEvent) {
         new PodcastPlayerNotificationEventBroadcaster(itemId, this).broadcast(playerEvent);
     }
 
     private void initReceivers() {
         playerEventReceiver = new PlayerEventReceiver(playerHandler);
         playerEventReceiver.register(this);
         externalReceiver = new ExternalReceiver();
         externalReceiver.register(this);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return START_STICKY;
     }
 
     public void setSyncListener(AudioServiceBinder.OnStateSync listener) {
         this.listener = listener;
         sync();
     }
 
     private final MediaPlayer.OnCompletionListener onComplete = new MediaPlayer.OnCompletionListener() {
         @Override
         public void onCompletion(MediaPlayer mediaPlayer) {
             if (hasNext()) {
                 // TODO implement a queue
             } else {
                 if (isWithinApp()) {
                     playerHandler.onPause();
                 } else {
                     playerHandler.onStop();
                 }
             }
         }
     };
 
     private boolean hasNext() {
         return false;
     }
 
     private void sync() {
         listener.onSync(playerHandler.asSyncEvent());
     }
 
     private boolean isWithinApp() {
         return listener != null;
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         removeSyncListener();
         return super.onUnbind(intent);
     }
 
     private void removeSyncListener() {
         listener = null;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         unregisterReceivers();
     }
 
     private void unregisterReceivers() {
         playerEventReceiver.unregister(this);
         externalReceiver.unregister(this);
     }
 
 }
