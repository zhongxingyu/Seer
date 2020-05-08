 package com.ouchadam.fang.audio;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 
 import com.ouchadam.fang.presentation.controller.AudioFocusManager;
 
 import java.io.IOException;
 
 public class AudioService extends Service implements PlayerEventReceiver.PlayerEventCallbacks {
 
     private final LocalBinder binder;
 
     private AudioFocusManager audioFocusManager;
     private PodcastPlayer podcastPlayer;
     private PlayerEventReceiver playerEventReceiver;
     private AudioServiceBinder.OnStateSync listener;
 
     private long playingItemId = -1L;
 
     public AudioService() {
         binder = new LocalBinder();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return binder;
     }
 
     public class LocalBinder extends Binder {
         public AudioService getService() {
             return AudioService.this;
         }
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         audioFocusManager = new AudioFocusManager((AudioManager) getSystemService(Context.AUDIO_SERVICE));
         podcastPlayer = new PodcastPlayer(new PodcastPositionBroadcaster(this));
         playerEventReceiver = new PlayerEventReceiver(this);
         playerEventReceiver.register(this);
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         return START_STICKY;
     }
 
     public void setSyncListener(AudioServiceBinder.OnStateSync listener) {
         this.listener = listener;
         podcastPlayer.sync(playingItemId, listener);
     }
 
     @Override
     public void onPlay(PodcastPosition position) {
         play(position);
         podcastPlayer.sync(playingItemId, listener);
     }
 
     private void play(PodcastPosition position) {
         audioFocusManager.requestFocus();
         podcastPlayer.play(position);
     }
 
     @Override
     public void onPause() {
         pause();
         podcastPlayer.sync(playingItemId, listener);
     }
 
     private void pause() {
         podcastPlayer.pause();
     }
 
     @Override
     public void onStop() {
         stop();
         podcastPlayer.release();
         stopSelf();
     }
 
     private void stop() {
         audioFocusManager.abandonFocus();
         unregisterEventReciever();
     }
 
     private void unregisterEventReciever() {
         playerEventReceiver.unregister(this);
     }
 
     @Override
     public void onNewSource(long itemId, Uri source) {
         this.playingItemId = itemId;
         setSource(source);
     }
 
     private void setSource(Uri uri) {
         try {
             podcastPlayer.setSource(this, uri);
         } catch (IOException e) {
             e.printStackTrace();
             throw new RuntimeException("couldn't find : " + uri);
         }
     }
 
     public boolean isPlaying() {
         return podcastPlayer.isPlaying();
     }
 
 }
