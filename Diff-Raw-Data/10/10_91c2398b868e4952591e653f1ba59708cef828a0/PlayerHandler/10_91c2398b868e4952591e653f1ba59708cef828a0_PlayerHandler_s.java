 package com.ouchadam.fang.audio;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.novoda.notils.java.Collections;
 import com.novoda.notils.logger.Novogger;
 import com.ouchadam.fang.audio.event.PlayerEvent;
 import com.ouchadam.fang.domain.PodcastPosition;
 import com.ouchadam.fang.notification.FangNotification;
 import com.ouchadam.fang.presentation.AudioFocusManager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 class PlayerHandler implements PlayerEventReceiver.PlayerEventCallbacks {
 
     private final PlayingItemStateManager itemStateManager;
     private final FangNotification notification;
     private final ServiceManipulator serviceManipulator;
     private final ActivityCompletionCallback completionCallback;
     private final ServiceLocation serviceLocation;
     private final AudioHandler audioHandler;
 
     static PlayerHandler from(Context context, AudioSync audioSync, ServiceManipulator serviceManipulator, RemoteHelper remoteHelper, ActivityCompletionCallback completionCallback, ServiceLocation serviceLocation) {
         AudioHandler audioHandler = new AudioHandler(FangMediaPlayer.from(context), AudioFocusManager.from(context), audioSync, Playlist.from(context), new AudioStateManager(), remoteHelper, PauseRewinder.from(context));
         FangNotification notification = FangNotification.from(context);
         PlayingItemStateManager itemStateManager = PlayingItemStateManager.from(context);
         return new PlayerHandler(audioHandler, itemStateManager, notification, serviceManipulator, completionCallback, serviceLocation);
     }
 
     PlayerHandler(AudioHandler audioHandler, PlayingItemStateManager itemStateManager, FangNotification notification, ServiceManipulator serviceManipulator, ActivityCompletionCallback completionCallback, ServiceLocation serviceLocation) {
         this.audioHandler = audioHandler;
         this.itemStateManager = itemStateManager;
         this.notification = notification;
         this.serviceManipulator = serviceManipulator;
         this.completionCallback = completionCallback;
         this.serviceLocation = serviceLocation;
     }
 
     @Override
     public void onNewSource(int playlistPosition, String playlistName) {
         audioHandler.setSource(playlistPosition);
     }
 
     @Override
     public void onPlay() {
         audioHandler.onPlay();
     }
 
     @Override
     public void onPlay(PodcastPosition position) {
         audioHandler.onPlay(position);
     }
 
     @Override
     public void onPause() {
         if (audioHandler.isPrepared()) {
             audioHandler.onPause();
             saveCurrentPlayState();
         }
     }
 
     @Override
     public void onPlayPause() {
         audioHandler.onPlayPause();
     }
 
     @Override
     public void onStop() {
        saveCurrentPlayState();
         audioHandler.onStop();
         notification.dismiss();
         serviceManipulator.stop();
     }
 
     @Override
     public void onFastForward() {
         audioHandler.onFastForward();
     }
 
     @Override
     public void onRewind() {
         audioHandler.onRewind();
     }
 
     private void saveCurrentPlayState() {
         audioHandler.saveCurrentPlayState(itemStateManager);
     }
 
     @Override
     public void gotoPosition(PodcastPosition position) {
         audioHandler.goToPosition(position);
         saveCurrentPlayState();
     }
 
     @Override
     public void onComplete() {
         Log.e("XXX", "onComplete");
         if (audioHandler.hasNext()) {
             completeCurrentAndPlayNext();
         } else {
             if (serviceLocation.isWithinApp()) {
                 lastItemComplete();
             } else {
                 completeCurrent();
                 audioHandler.onStop();
             }
         }
     }
 
     private void lastItemComplete() {
         completeCurrent();
         completionCallback.onComplete();
     }
 
     private void completeCurrentAndPlayNext() {
         completeCurrent();
         audioHandler.playNext();
     }
 
     private void completeCurrent() {
         audioHandler.completeCurrent();
         saveCompletedState();
     }
 
     private void saveCompletedState() {
         audioHandler.saveCompletedState(itemStateManager);
     }
 
     @Override
     public void onReset() {
         audioHandler.onReset();
         itemStateManager.resetCurrentItem();
     }
 
     public SyncEvent asSyncEvent() {
         return audioHandler.asSyncEvent();
     }
 
     public void restoreItem() {
         PlayItem playItem = itemStateManager.getStoredItem();
         audioHandler.restore(playItem);
     }
 
 }
