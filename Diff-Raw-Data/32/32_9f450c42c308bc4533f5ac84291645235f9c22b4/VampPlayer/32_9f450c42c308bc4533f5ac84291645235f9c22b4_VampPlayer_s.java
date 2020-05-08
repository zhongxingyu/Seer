 package com.player.bll;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import javazoom.jl.player.Player;
 
 public class VampPlayer {
 
     private Queue playlist;
     private int playlistIndex;
     private boolean isPlaying;
     private Mp3Task mp3Task;
     
     private long startTime;
 
     public VampPlayer(Queue playlistToUse) {
         playlist = playlistToUse;
         playlistIndex = 0;
         isPlaying = false;
         startTime = 0;
         getTask();
     }
     
     public Mp3Task getTask() {
       if ( mp3Task == null && playlist.size() > 0 ) {
         mp3Task = new Mp3Task( this, this.getCurrentSong() );
       }
       return mp3Task;
     }
 
     public Song getCurrentSong() {
       if ( playlist.size() > 0 && getPosition() < playlist.size() && getPosition() >= 0 ) {
         return playlist.get( playlistIndex );
       }
       return null;
     }
     
     public String getSongTitleOfCurrentTrack() {
         String songTitle = getCurrentSong().getTitle();        
         return songTitle; 
     }
     
     public void play() {
       if ( getCurrentSong() != null && getTask() != null && isPlaying == false ) {
         
         getTask().setSong( getCurrentSong() );
         getTask().setStartTime( startTime );
         getTask().createThread();
         isPlaying = true;
       }
     }
     public void stop() {
         startTime = 0;
         getTask().setStartTime( startTime );
         if (getTask() != null) {
             getTask().closeThread();
             isPlaying = false;
         }
     }
 
     public boolean isPlaying() {
       return ( isPlaying & getTask() != null );
     }
 
     public void skipForward() {
       boolean wasPlaying = isPlaying();
      System.out.println( "WasPlaying? :" +wasPlaying );
       stop();
       if ((playlistIndex + 1) < playlist.size()) {
           ++playlistIndex;
       }
       if ( wasPlaying ) {
         play();
       }
     }
 
     public void skipToEnd() {
       boolean wasPlaying = isPlaying();
       stop();
       playlistIndex = playlist.size() - 1;
       if ( wasPlaying ) {
         play();
       }
     }
     
     public void setPosition( int i ) {
       playlistIndex = i;
     }
     
     public int getPosition()  {
       if ( playlistIndex < 0 || playlistIndex >= playlist.size() ) {
         playlistIndex = 0;
       }
       return playlistIndex;
     }
 
     
     
     public void skipBackward() {
 
       boolean wasPlaying = isPlaying();
            System.out.println( "WasPlaying? :" +wasPlaying );
       stop();
       if ((playlistIndex - 1) >= 0) {
           --playlistIndex;
       }
       if ( wasPlaying ) {
         play();
       }
     }
 
     public Queue getPlaylist() {
         return playlist;
     }
 
     public void pause() {
       if ( getTask() != null ) {
         long resumeTime = getTime();
         stop();
         startTime = resumeTime;
         mp3Task.setStartTime( resumeTime );
       }
     }
 
     public void seekToPosition(double percent)
             throws javazoom.jl.decoder.JavaLayerException {
       if ( getTask() != null ) {
         double length = getCurrentSong().getHeader().getTrackLength() * 1000;
         long time = (long)(length * percent);
         seekToTime( time );
       }
     }
 
     public void seekToTime( long time )
             throws javazoom.jl.decoder.JavaLayerException {
       if ( getTask() != null ) {
         boolean wasPlaying = isPlaying;
         getTask().setStartTime( startTime );
         startTime = time;
         if ( wasPlaying ) {
           getTask().closeThread();
           getTask().setSong( getCurrentSong() );
           getTask().setStartTime( startTime );
           getTask().createThread();
           isPlaying = true;
           //play();
         } else {
           getTask().setStartTime( startTime );
         }
       }
     }
     
     
     public int getTime() {
       if ( getTask() != null ) {
         return mp3Task.getTime();
       }
       return 0;
     }
     
     public String getFormatedTime() {
       long timeMillis = getTime(); 
       long time = timeMillis / 1000; 
       String seconds = Integer.toString((int)(time % 60)); 
       String minutes = Integer.toString((int)((time % 3600) / 60)); 
       String hours = Integer.toString((int)(time / 3600)); 
       for (int i = 0; i < 2; i++) { 
         if (seconds.length() < 2) { 
           seconds = "0" + seconds; 
         } 
         if (minutes.length() < 2) { 
           minutes = "0" + minutes; 
         } 
         if (hours.length() < 2) { 
           hours = "0" + hours; 
         } 
       }
       return hours+":"+minutes+":"+seconds;
     }
     
     public double getProgressPercent() {
       double percent = ( Double.valueOf( getTime() ) / Double.valueOf( getCurrentSong().getTrackLength() * 1000 ) );
       return percent;
     }
 }
