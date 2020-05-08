 package RPGGrid.input;
 
 import javax.sound.sampled.*;
 import java.io.*;
 
 /**
  * A <code>SoundPlayer<code> manages the sound for a RPGWorld.
  */
 public class SoundPlayer
 {
     private static Clip clip;
    private static File f;
     
     /**
      * Plays a .wav file.
      * @param filename is the name of a .wav file in the projects root directory
      */
     public static void play(String filename)
     {
         filename = filename + ".wav";
        f = new File(filename);
         try
         {
             clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(f));
             clip.start();
         }
         catch (Exception ex)
         {
             ex.printStackTrace(System.out);
         }
     }
     
     /**
     * Stops all sounds currently playing.  Sets f equal to null to avoid out of memeory errors.
      */
     public static void pause()
     {
         clip.stop();
        f = null;
     }
 }
