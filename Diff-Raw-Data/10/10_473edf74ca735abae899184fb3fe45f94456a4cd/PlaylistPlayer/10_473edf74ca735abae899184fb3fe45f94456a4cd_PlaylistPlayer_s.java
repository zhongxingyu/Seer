 package com.tiggerpalace.automan;
 
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.util.Log;
 
 public class PlaylistPlayer implements OnCompletionListener {
   private static PlaylistPlayer instance = null;
 
   private ArrayList<String> playlist;
   private ListIterator<String> playlistIterator;
   //private PlaylistRunnable runnable;
   private MediaPlayer player;
   
   public static PlaylistPlayer getInstance() {
     if(instance == null) {
       instance = new PlaylistPlayer();
     }
     return instance;
   }
   
   public void setPlaylist(ArrayList<String> list) {
     playlist = list;
     playlistIterator = list.listIterator();
   }
   
   public void start() {
     Log.d("AutoDroid:PlaylistPlayer.start", "begin");    
     stop();
     player = buildPlayer();
     if(playlistIterator.hasNext()) {
       try {
         String filename = playlistIterator.next();
         player.setDataSource(filename);
         player.prepare();
         player.start();
        Log.d("AutoDroid:PlaylistPlayer:run", new StringBuffer().append("Now playing: '").append(filename).append("'").toString());
       } catch(Exception e) {
        Log.e("AutoDroid:PlaylistPlayer:run", e.getMessage());
       }
     }
     Log.d("AutoDroid:PlaylistPlayer.start", "done");    
   }
 
   public void stop() {
     Log.d("AutoDroid:PlaylistPlayer.stop", "begin");
     if(player != null) {
       player.stop();
       player.release();
       player = null;
     }
     Log.d("AutoDroid:PlaylistPlayer.stop", "done");    
   }
   
   public void previousTrack() {
     Log.d("AutoDroid:PlaylistPlayer.previousTrack", "begin");
     if(playlistIterator.hasPrevious()) {
       boolean wasPlaying = isPlaying();
       stop();
       playlistIterator.previous();
       if(playlistIterator.hasPrevious()) {
         playlistIterator.previous();
       }
       if(wasPlaying) {
         start();
       }
     }
     Log.d("AutoDroid:PlaylistPlayer.previousTrack", "done");    
   }
   
   public void nextTrack() {
     Log.d("AutoDroid:PlaylistPlayer.nextTrack", "begin");    
     if(playlistIterator.hasNext()) {
       boolean wasPlaying = isPlaying();
       stop();
       if(wasPlaying) {
         start();
       } 
     }
     Log.d("AutoDroid:PlaylistPlayer.nextTrack", "done");      
   }
   
   public void togglePlayPause() {
     Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "begin");
     if(isPlaying()) {
       Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "Pausing");
       player.pause();
     } else {
       Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "Starting play");
       try {
         if(player != null) {
           Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "Attempting to resume play via MediaPlayer.start");
           player.start();
           Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "Success!");
        } 
       } catch(Exception e) {
         Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "Failure! Caught an exception in MediaPlayer.start; starting from scratch on next track");
         start();
       }
     }
     Log.d("AutoDroid:PlaylistPlayer.togglePlayPause", "done");    
   }
   
   public boolean isPlaying() {
     boolean retval = false;
     if (player != null) {
       try {
         retval = player.isPlaying();
       } catch(IllegalStateException e) {
         // We don't really care because it's still not playing if this is thrown
       }
     }
     return retval;
   }
   
   public void onCompletion(MediaPlayer mp) {
     if(playlistIterator != null && playlistIterator.hasNext()) {
       start();
     }
   }
   
   private MediaPlayer buildPlayer() {
     MediaPlayer player = new MediaPlayer();
     player.setLooping(false);
     player.setOnCompletionListener(this);
     return player;
   }
 }
