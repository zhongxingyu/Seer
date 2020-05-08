 package com.ouchadam.fang.audio;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 
 public class SeekbarReceiver extends BroadcastReceiver {
 
     private final OnSeekUpdate onSeekUpdate;
    private boolean registered;
 
     public SeekbarReceiver(OnSeekUpdate onSeekUpdate) {
         this.onSeekUpdate = onSeekUpdate;
     }
 
     public interface OnSeekUpdate {
         void onUpdate(PodcastPosition position);
     }
 
     public void register(Context context) {
        if (!registered) {
            registered = true;
            context.registerReceiver(this, getIntentFilter());
        }
     }
 
     private IntentFilter getIntentFilter() {
         return new IntentFilter(PodcastPositionBroadcaster.POSITION_ACTION);
     }
 
     public void unregister(Context context) {
        if (registered) {
            context.unregisterReceiver(this);
            registered = false;
        }
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         PodcastPosition podcastPosition = (PodcastPosition) intent.getSerializableExtra(PodcastPositionBroadcaster.EXTRA_POSITION);
         onSeekUpdate.onUpdate(podcastPosition);
     }
 
 }
