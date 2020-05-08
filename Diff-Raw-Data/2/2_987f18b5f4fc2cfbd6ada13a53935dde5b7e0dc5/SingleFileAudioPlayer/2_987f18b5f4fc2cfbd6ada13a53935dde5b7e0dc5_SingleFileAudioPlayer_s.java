 package com.lastcrusade.soundstream.audio;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.util.Log;
 
 import com.lastcrusade.soundstream.model.PlaylistEntry;
 import com.lastcrusade.soundstream.net.message.PlayStatusMessage;
 import com.lastcrusade.soundstream.service.MessagingService;
 import com.lastcrusade.soundstream.service.PlaylistService;
 import com.lastcrusade.soundstream.service.ServiceLocator;
 import com.lastcrusade.soundstream.service.ServiceNotBoundException;
 import com.lastcrusade.soundstream.util.BroadcastIntent;
 
 /**
  * A simple audio player that expects an audio file to be located in an
  * accessible folder.
  * 
  * This player takes in the path to the file to play, and can play
  * 
  * @author Jesse Rosalia
  */
 public class SingleFileAudioPlayer implements IPlayer {
 
     public static final String ACTION_SONG_FINISHED = SingleFileAudioPlayer.class.getName() + ".action.SongFinished";
 
     private static final String TAG = SingleFileAudioPlayer.class.getName();
     private PlaylistEntry entry;
     private MediaPlayer player;
 
     private boolean paused;
 
     private ServiceLocator<MessagingService> messagingService;
 
     private Context context;
 
     public SingleFileAudioPlayer(Context context, ServiceLocator<MessagingService> messagingServiceLocator) {
         this.player = new MediaPlayer();
         this.context = context;
         this.messagingService = messagingServiceLocator;
         player.setOnCompletionListener(
                 new OnCompletionListener(){
                     @Override public void onCompletion(MediaPlayer mp) {
                         new BroadcastIntent(SingleFileAudioPlayer.ACTION_SONG_FINISHED).send(SingleFileAudioPlayer.this.context);
                     }
         });
     }
 
     /**
      * Set the song path and accompanying metadata to play.
      * 
      * NOTE: these can be null, to clear the currently playing song.
      * 
      * @param filePath
      * @param song
      */
     public void setSong(PlaylistEntry song) {
         this.entry = song;
         //This is sending a playlist entry not a SongMetadata
         new BroadcastIntent(PlaylistService.ACTION_SONG_PLAYING)
             .putExtra(PlaylistService.EXTRA_SONG, this.entry)
             .send(this.context);
     }
 
     @Override
     public boolean isPlaying() {
         return player.isPlaying() && !paused;
     }
 
     public void play() {
         if (isValidPath()) {
             try {
                 if (player.isPlaying()) {
                     player.stop();
                 }
                 this.paused = false;
                 player.reset();
                 //changed to use the underlying file descriptor, because this doesnt want to work on a Samsung Galaxy S3
                //..see http://stackoverflow.com/questions/1972027/android-playing-mp3-from-byte
                 FileInputStream fis = new FileInputStream(entry.getFilePath());
                 player.setDataSource(fis.getFD());
                 player.prepare();
                 player.start();
                 this.messagingService.getService().sendPlayStatusMessage(
                         this.entry, true);
             } catch (Exception e) {
                 Log.wtf(TAG, "Unable to play song: " + entry.getFilePath());
             }
         } else {
             Log.w(TAG, "File Path was not valid");
         }
     }
 
     private boolean isValidPath() {
         boolean isValid = false;
         try {
             //This will fail and throw and Exception if the filepath is bad
             new File((new File(entry.getFilePath()).getParentFile().list())[0]).exists();
             isValid = true;
         } catch (Exception e) {
             isValid = false;
             e.printStackTrace();
         }
         return isValid;
     }
 
     @Override
     public void pause() {
         if (player.isPlaying()) {
             player.pause();
         }
         this.paused = true;
 
         try {
             this.messagingService
                 .getService()
                 .sendPlayStatusMessage(this.entry, false);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
 
     /**
      * Stop the player, and clear the active song.
      * 
      * Note that currently, stop is not a function exposed to the rest of the application
      * so we report that the player is "paused".
      * 
      */
     public void stop() {
         this.paused = false;
         this.player.stop();
         this.setSong(null);
         //TODO revisit the decision to treat stop the same as pause
         //indicate the system is paused
         new BroadcastIntent(PlaylistService.ACTION_PAUSED_AUDIO).send(this.context);
         try {
             this.messagingService
                 .getService()
                 .sendPlayStatusMessage(this.entry, false);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     @Override
     public void resume() {
         player.start();
         paused = false;
         try {
             this.messagingService.getService().sendPlayStatusMessage(this.entry, true);
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
     }
 
     @Override
     public void skip() {
         // since this is a single file player, skip == stop
         if (player.isPlaying()) {
             player.stop();
         }
         //send this action to move to the next song
         new BroadcastIntent(SingleFileAudioPlayer.ACTION_SONG_FINISHED).send(this.context);
         paused = false;
     }
 
     public boolean isPaused() {
         return paused;
     }
 }
