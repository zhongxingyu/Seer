 package RPGGrid.input;
 
 import javax.sound.sampled.*;
 import java.io.*;
 
 /**
  * A <code>SoundPlayer<code> manages the sound for a RPGWorld.
  */
 public class SoundPlayer
 {
     private static Clip clip;
     
     /**
      * Plays a .wav file.
      * @param filename is the name of a .wav file in the projects root directory
      */
     public static void play(String filename)
     {
         filename = filename + ".wav";
         try
         {
             clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(filename)));
             clip.start();
         }
         catch (Exception ex)
         {
             ex.printStackTrace(System.out);
         }
     }
     
     /**
     * Stops all sounds currently playing.
      */
     public static void pause()
     {
         clip.stop();
     }
 }
