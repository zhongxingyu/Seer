 package epsilon.game;
 
 
 import java.io.BufferedInputStream;
 import java.net.URL;
 
 import javazoom.jl.player.Player;
 
 /**
  * Sound-player for playing MP3 files. Using a thread for each sound that is
  * played. Each object of SoundPlayer contains one MP3-file path.
  *
  * @author vz
  */
 public class SoundPlayer {
 
     private Player player;
    private String filename;
 
     /**
      * Creating a object that plays the MP3-file, the method SoundPlayer.play()
      * needs to be run before the MP3-file starts playing.
      *
      * @param filename file relative to classpath
      */
     public SoundPlayer(String filename) {
        this.filename = filename;
 
     }
 
     /**
      * Closing the MP3-file.
      */
     public void close() {
         if (player != null) {
             player.close();
         }
     }
 
     // play the MP3 file to the sound card
     public void play() {
         try {
            // getting the inputstream and buffering it
            BufferedInputStream bis = new BufferedInputStream(this.getClass().getResourceAsStream(filename));
             player = new Player(bis);
         } catch (Exception e) {
             System.out.println("Problem playing file ");
             System.out.println(e);
         }
 
         // run in new thread to play in background
         // TODO: thread should maybe be handled different
         new Thread() {
 
             @Override
             public void run() {
                 try {
                     player.play();
                 } catch (Exception e) {
                     System.out.println(e);
                 }
             }
         }.start();
     }
 
     /**
      * Checks if the sound is finished, and plays the MP3-file again if
      * it is completed.
      */
     public void repeat() {
         if (player.isComplete()) {
             play();
         }
     }
 }
 
