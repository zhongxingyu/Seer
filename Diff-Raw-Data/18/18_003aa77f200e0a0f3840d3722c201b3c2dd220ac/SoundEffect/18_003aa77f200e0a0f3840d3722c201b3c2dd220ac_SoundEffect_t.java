 package thrust.audio;
 
 import java.io.File;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.Clip;
 import java.io.IOException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.sound.sampled.LineUnavailableException;
 
 /**
  * Any sound made in response to a event.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 public class SoundEffect {
   /**
    * Clip for playing sound effect.
    */
   private Clip my_sound;
 
   /**
    * This is your sound effect.
    * @param the_sound_effect_file the sound effect to make.
    * @return the new sound effect for the effect stored in 's'.
    */
  public final SoundEffect make(final File the_sound_effect_file) {
    AudioInputStream my_audio_stream = null;
     try {
       my_audio_stream = AudioSystem.getAudioInputStream(the_sound_effect_file);
     } catch (IOException e) {
       e.printStackTrace();
     } catch (UnsupportedAudioFileException e) {
       e.printStackTrace();
     }
 
     final DataLine.Info info =
       new DataLine.Info(SourceDataLine.class, my_audio_stream.getFormat());
 
     try {
       my_sound = (Clip) AudioSystem.getLine(info);
       my_sound.open(my_audio_stream);
     } catch (IOException e) {
       e.printStackTrace();
     } catch (LineUnavailableException e) {
       e.printStackTrace();
     } /* Not really sure about this next line returning */
     return (SoundEffect) my_sound;
   }
   /**
    * Start playing your effect.
    */
   public void start() {
     my_sound.loop(1); /* play file once */
   }
 }
