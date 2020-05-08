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
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import java.io.IOException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.sound.sampled.LineUnavailableException;
 /**
  * In-game music.
  * @author Nicholas McCarthy (nicholas.mccarthy@gmail.com)
  * @version 24 April 2008 */
 public class Music {
 
 /** Boolean for checking if music is playing. */
  private transient boolean my_is_playing_boolean = false;
 /** Location of .wav file. */
  private transient String my_music_location = "../../../media/Thrust_music.wav";
 /** Creates music_file File. */
   private transient File my_music_file;
   /** Ahem. */
   private transient AudioInputStream my_music_stream;
   /** Ahem. */
   private transient AudioFormat my_music_format;
   /** Ahem. */
   private transient Clip my_music_clip;
 
   public Music() {
     my_music_file = new File(my_music_location);

     try {
       my_music_stream = AudioSystem.getAudioInputStream(my_music_file);
     } catch (UnsupportedAudioFileException e) {
       return;
    } catch (IOException e) {
       return;
    }
 
     my_music_format = my_music_stream.getFormat();

    DataLine.Info info = new DataLine.Info(Clip.class, my_music_format);
    try {
      Clip my_music_clip = (Clip) AudioSystem.getLine(info);
     } catch (LineUnavailableException e) {
       return;
     }
   }
 
   /**
    * @return Is music playing?
    */
   //@ ensures \result == is_playing;
   public boolean playing() {
 
     return my_is_playing_boolean;
 
   }
 
   /**
    * Start playing the music.
    */
   //@ ensures is_playing;
   public void start() {
 
     my_music_clip.start();
     my_is_playing_boolean = true;
 
   }
 
   /**
    * Stop playing the music.
    */
   //@ ensures !is_playing;
   public void stop() {
 
     my_music_clip.stop();
     my_music_clip.flush();
     my_is_playing_boolean = false;
 
   }
 }
