 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Joe Kiniry (kiniry@acm.org)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "March 2007"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust.audio;
 
 import java.io.File;
 import javax.sound.sampled.*;
 /**
  * Any sound made in response to a event.
  * @author Neil McCarthy (neil.mccarthy@ucdconnect.ie)
  * @author Ciaran Hale (ciaran.hale@ucdconnect.ie)
  * @version 24 April 2008
  */
 public class SoundEffect {
 
   /**
    *indicates the sound file to load.
    */
   private final transient File my_sound_clipFile = new File("");
   /**
    * the name of our new sound file.
    */
   private transient Clip my_sound_clip;
 
   public SoundEffect() throws Exception {
     AudioInputStream my_audio_input_stream = null;
 
     final DataLine.Info info =
       new DataLine.Info(SourceDataLine.class,
         my_audio_input_stream.getFormat());
 
 
 
     my_audio_input_stream = AudioSystem.getAudioInputStream(my_sound_clipFile);
 
     my_sound_clip = (Clip) AudioSystem.getLine(info);
 
     my_sound_clip.open(my_audio_input_stream);
   }
     /**
      * @return Is the sound clip playing?
      */
     //@ ensures \result == is_playing;
   public /*@ pure @*/ boolean playing() {
 
     return false;
   }
   /**
    * Start playing the sound clip.
    */
   //@ ensures is_playing;
   public void start() {
    my_sound_clip.start;
 
 
   }
 
  /**
    * Stop playing the sound clip.
    */
   //@ ensures !is_playing;
   public void stop() {
     my_sound_clip.stop();
 
   }
 }
