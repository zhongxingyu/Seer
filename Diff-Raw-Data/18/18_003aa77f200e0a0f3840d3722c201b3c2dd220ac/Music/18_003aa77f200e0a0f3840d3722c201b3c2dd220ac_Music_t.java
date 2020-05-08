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
  * In-game music.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 public class Music {
   //@ public model boolean is_playing;
   /**
    *  Create new clip for music.
    */
   Clip my_clip;
 
   /**
    * Opens music file.
    */
   public void openMusic() {
     final File music_file =
      new File("../../media/thrustmusic.wav");
 
    AudioInputStream my_audio_stream = null;
     try {
       my_audio_stream = AudioSystem.getAudioInputStream(music_file);
     } catch (UnsupportedAudioFileException e) {
       e.printStackTrace();
     } catch (IOException e) {
       e.printStackTrace();
     } //Catch all possible exceptions.
     final DataLine.Info info =
       new DataLine.Info(SourceDataLine.class, my_audio_stream.getFormat());
     try {
       my_clip = (Clip) AudioSystem.getLine(info);
       my_clip.open(my_audio_stream);
     } catch (IOException e) {
       e.printStackTrace();
     } catch (LineUnavailableException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * @return Is music playing?
    */
   //@ ensures \result == is_playing;
   public /*@ pure @*/ boolean playing() {
     return my_clip.isRunning();
   }
 
   /**
    * Start playing the music.
    */
   //@ ensures is_playing;
   public void start() {
     //@ assert !playing();
     my_clip.loop(Clip.LOOP_CONTINUOUSLY);
   }
 
   /**
    * Stop playing the music.
    */
   //@ ensures !is_playing;
   public void stop() {
     //@ assert playing();
     my_clip.stop();
   }
 }
