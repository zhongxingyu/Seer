 package com.player.bll;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import javazoom.jl.player.Player;
 
 public class VampPlayer {
 
     private Playlist playlist;
     private int playlistIndex;
     private boolean isPlaying;
     private Mp3Task mp3Task;
     private Object bis;
     private Object song;
 
     public VampPlayer(Playlist playlistToUse) {
         playlist = playlistToUse;
         playlistIndex = 0;
         isPlaying = false;
         getTask();
     }
     
     public Mp3Task getTask() {
       if ( mp3Task == null && playlist.size() > 0 ) {
         System.out.println( "CREATE TASK! Size:"+playlist.size() );
         mp3Task = new Mp3Task( this.getCurrentSong() );
       }
       return mp3Task;
     }
 
     public Song getCurrentSong() {
            return playlist.get(playlistIndex);
     }
     
     public String getSongTitleOfCurrentTrack() {
         String songTitle = getCurrentSong().getTitle();        
         return songTitle; 
     }
     
     public void play() {
       if ( getTask() != null && isPlaying == false ) {
         getTask().setSong( getCurrentSong() );
         System.out.println( "Set song: "+getCurrentSong().getTitle()+": "+getCurrentSong().getFileName()  );
         getTask().createThread();
         isPlaying = true;
       }
     }
 
     public void stop() {
         //TODO: add code to check if thread is active.  If so, stop thread.
         if (getTask() != null) {
             getTask().closeThread();
             isPlaying = false;
         }
     }
 
     public boolean isPlaying() {
         return ( getTask().isAlive() && isPlaying );
     }
 
     public void skipForward() {
      boolean wasPlaying = isPlaying();
      stop();
         if ((playlistIndex + 1) < playlist.size()) {
             ++playlistIndex;
         }
        if ( wasPlaying ) {
          play();
        }
     }
 
     public void skipBackward() {
      boolean wasPlaying = isPlaying();
      stop();
         if ((playlistIndex - 1) >= 0) {
             --playlistIndex;
         }
        if ( wasPlaying ) {
          play();
        }
     }
 
     public Playlist getPlaylist() {
         return playlist;
     }
 
     public void pause() {
       if ( getTask() != null ) {
         getTask().interrupt();
         isPlaying = false;
       }
     }
 
     public void seekToPosition(double percent)
             throws javazoom.jl.decoder.JavaLayerException {
       if ( getTask() != null ) {
         getTask().closeThread();
         getCurrentSong().getFile().length();
         long fileSize = getCurrentSong().getFile().length();
         long bufferedPosition = (long) (percent * fileSize);
         getTask().skipBuffered(bufferedPosition);
       }
     }
 }
