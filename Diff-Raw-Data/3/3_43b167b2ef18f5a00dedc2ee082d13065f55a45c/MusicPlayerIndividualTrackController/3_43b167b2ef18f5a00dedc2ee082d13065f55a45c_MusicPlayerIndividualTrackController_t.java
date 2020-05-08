 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package muvis.view.controllers;
 
 import javazoom.jlgui.basicplayer.BasicPlayerException;
 import muvis.Elements;
 import muvis.Environment;
 import muvis.audio.AudioMetadata;
 import muvis.audio.MuVisAudioPlayer;
 import muvis.util.Observable;
 import muvis.util.Observer;
 
 /**
  *
  * @author Ricardo
  */
 public class MusicPlayerIndividualTrackController implements MusicPlayerControllerInterface, Observer {
 
     private int trackId;
     private boolean isPlaying;
     private boolean enabled;
     private MuVisAudioPlayer player;
 
     public MusicPlayerIndividualTrackController(){
         isPlaying = false;
         enabled = false;
         player = Environment.getEnvironmentInstance().getAudioPlayer();
         Environment.getEnvironmentInstance().getAudioPlayer().registerObserver(this);
     }
 
     public void setTrackId(int trackId){
         this.trackId = trackId;
     }
 
     @Override
     public AudioMetadata getTrackPlayingMetadata() {
         return Environment.getEnvironmentInstance().getDatabaseManager().getTrackMetadata(trackId);
     }
 
     @Override
     public void playNextTrack() {
         //no operation
     }
 
     @Override
     public void playPreviousTrack() {
         //no operation
     }
 
     @Override
     public void playTrack() {
 
         try {
             if (player.isPlaying()){
                 player.stop();
             }
             player.play(Environment.getEnvironmentInstance().getDatabaseManager().getFilename(trackId));
            isPlaying = true;
         } catch (BasicPlayerException ex) {
             ex.printStackTrace();
         }
     }
 
     @Override
     public void pauseTrack() {
         try {
             player.pause();
             isPlaying = false;
         } catch (BasicPlayerException ex) {
             ex.printStackTrace();
         }
     }
 
     @Override
     public void setPlayerVolume(int value) throws BasicPlayerException {
         Environment.getEnvironmentInstance().getAudioPlayer().setVolume(value);
     }
 
     @Override
     public void stopTrack() {
         try {
             isPlaying = false;
             player.stop();
         } catch (BasicPlayerException ex) {
             ex.printStackTrace();
         } catch (Exception e){
             e.printStackTrace();
         }
     }
 
     @Override
     public void setEnable(boolean enabled) {
         this.enabled = enabled;
     }
 
     @Override
     public boolean isEnabled() {
         return enabled;
     }
 
     @Override
     public boolean isPlaying() {
         return isPlaying;
     }
 
     @Override
     public void update(Observable obs, Object arg) {
         if (enabled){
             if (obs instanceof MuVisAudioPlayer) {
                 if (MuVisAudioPlayer.Event.STOPPED.equals(arg)) {
                 }
             }
         }
     }
 
 }
