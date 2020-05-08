 package com.ouchadam.fang.audio;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Handler;
 
 import com.ouchadam.fang.Broadcaster;
 
 import java.io.IOException;
 
 public class PodcastPlayer {
 
     private MediaPlayer mediaPlayer;
     private final Broadcaster<PodcastPosition> positionBroadcaster;
 
     private boolean isPaused = false;
 
     private final Handler seekHandler = new Handler();
 
     public PodcastPlayer(Broadcaster<PodcastPosition> positionBroadcaster) {
         this.positionBroadcaster = positionBroadcaster;
     }
 
     public void setSource(Context context, Uri source) throws IOException {
         mediaPlayer = new MediaPlayer();
         mediaPlayer.setDataSource(context, source);
         mediaPlayer.prepare();
     }
 
     public void play(PodcastPosition position) {
         isPaused = false;
         goTo(position.value());
         mediaPlayer.start();
         scheduleSeekPositionUpdate();
     }
 
     private void goTo(int position) {
         mediaPlayer.seekTo(position);
     }
 
     private final Runnable seekUpdater = new Runnable() {
         @Override
         public void run() {
             positionBroadcaster.broadcast(new PodcastPosition(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration()));
             if (mediaPlayer.isPlaying()) {
                 scheduleSeekPositionUpdate();
             }
         }
     };
 
     private void scheduleSeekPositionUpdate() {
         seekHandler.postDelayed(seekUpdater, 1000);
     }
 
     public void pause() {
         isPaused = true;
         mediaPlayer.pause();
     }
 
     public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = null;
     }
 
     public boolean isPaused() {
         return isPaused;
     }
 
     public boolean isPlaying() {
         return mediaPlayer.isPlaying();
     }
 
     public void sync(long itemId, AudioServiceBinder.OnStateSync listener) {
         if (isIdle()) {
             listener.onSync(SyncEvent.idle());
         } else {
             PodcastPosition position = new PodcastPosition(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
             listener.onSync(new SyncEvent(mediaPlayer.isPlaying(), position, itemId));
         }
     }
 
     private boolean isIdle() {
         return mediaPlayer == null;
     }
 
 }
