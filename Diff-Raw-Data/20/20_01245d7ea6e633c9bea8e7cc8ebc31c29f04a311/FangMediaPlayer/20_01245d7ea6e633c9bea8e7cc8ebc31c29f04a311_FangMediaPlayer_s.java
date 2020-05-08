 package com.ouchadam.fang.audio;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Handler;
 
 import com.ouchadam.fang.Broadcaster;
 import com.ouchadam.fang.audio.event.PlayerEvent;
 import com.ouchadam.fang.audio.event.PodcastPlayerEventBroadcaster;
 import com.ouchadam.fang.domain.PodcastPosition;
 
 import java.io.IOException;
 
 public class FangMediaPlayer implements FangPlayer {
 
     private static final int ONE_SECOND_MS = 1000;
     private static final int MAX_RETRIES = 2;
 
     private final Handler seekHandler = new Handler();
     private final Context context;
     private final Broadcaster<PodcastPosition> positionBroadcaster;
     private final OnBadSourceHandler onBadSourceHandler;
     private final PodcastPlayerEventBroadcaster playerEventBroadcaster;
 
     private MediaPlayer mediaPlayer;
     private Uri source;
     private int retryCount;
 
     public interface OnBadSourceHandler {
         void onBadSource(Exception e);
     }
 
     public static FangPlayer from(Context context, OnBadSourceHandler onBadSourceHandler) {
         return new FangMediaPlayer(context, new PodcastPositionBroadcaster(context), onBadSourceHandler, new PodcastPlayerEventBroadcaster(context));
     }
 
     public FangMediaPlayer(Context context, Broadcaster<PodcastPosition> positionBroadcaster, OnBadSourceHandler onBadSourceHandler, PodcastPlayerEventBroadcaster playerEventBroadcaster) {
         this.context = context;
         this.positionBroadcaster = positionBroadcaster;
         this.onBadSourceHandler = onBadSourceHandler;
         this.playerEventBroadcaster = playerEventBroadcaster;
         this.source = null;
         this.retryCount = 0;
     }
 
     @Override
     public void setSource(Uri source) {
         this.source = source;
         if (mediaPlayer != null) {
             mediaPlayer.release();
         }
        mediaPlayer = newMediaPlayer(context, source);
        prepare(source);
     }
 
    private MediaPlayer newMediaPlayer(Context context, Uri source) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, source);
         mediaPlayer.setOnCompletionListener(onCompletionWrapper);
        return mediaPlayer;
     }
 
     private void prepare(Uri source) {
         try {
             mediaPlayer.prepare();
             retryCount = 0;
         } catch (IllegalStateException e) {
             retrySetSource(source, e);
         } catch (IOException e) {
             onBadSourceHandler.onBadSource(e);
         }
     }
 
     private void retrySetSource(Uri source, IllegalStateException e) {
         retryCount ++;
         if (canRetry()) {
             setSource(source);
         } else {
             onBadSourceHandler.onBadSource(e);
         }
     }
 
     private boolean canRetry() {
         return retryCount < MAX_RETRIES;
     }
 
     private final MediaPlayer.OnCompletionListener onCompletionWrapper = new MediaPlayer.OnCompletionListener() {
 
         @Override
         public void onCompletion(MediaPlayer mediaPlayer) {
             playerEventBroadcaster.broadcast(new PlayerEvent.Factory().complete());
         }
     };
 
     @Override
     public void play(PodcastPosition position) {
         setInitialPlayPosition(position);
         mediaPlayer.start();
         scheduleSeekPositionUpdate();
     }
 
     private void setInitialPlayPosition(PodcastPosition position) {
         if (hasNonCompletedPosition(position)) {
             goTo(position);
         }
     }
 
     private boolean hasNonCompletedPosition(PodcastPosition position) {
         return position != null && !position.isCompleted();
     }
 
     @Override
     public void goTo(PodcastPosition podcastPosition) {
         mediaPlayer.seekTo(podcastPosition.value());
     }
 
     private final Runnable seekUpdater = new Runnable() {
         @Override
         public void run() {
             if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                 positionBroadcaster.broadcast(new PodcastPosition(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration()));
                 scheduleSeekPositionUpdate();
             }
         }
     };
 
     private void scheduleSeekPositionUpdate() {
         seekHandler.postDelayed(seekUpdater, ONE_SECOND_MS);
     }
 
     @Override
     public void pause() {
         mediaPlayer.pause();
     }
 
     public boolean isPlaying() {
         return mediaPlayer.isPlaying();
     }
 
     public boolean isPrepared() {
         return mediaPlayer != null;
     }
 
     public PodcastPosition getPosition() {
         return new PodcastPosition(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
     }
 
     @Override
     public Uri getSource() {
         return source;
     }
 
     @Override
     public void release() {
         if (mediaPlayer != null) {
             mediaPlayer.release();
         }
         this.source = null;
         this.mediaPlayer = null;
     }
 
 }
