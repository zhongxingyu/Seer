 package thrust.audio;
 
 /**
  * In-game music.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 /**
  An example of loading and playing a sound using a Clip. This complete class
  * isn't in the book ;)
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 public class Music {
 
   //@ public model boolean is_playing;
 
 
   /**
    * @return Is music playing?
    */
   //@ ensures \result == is_playing;
   /**
    *
    */
   private Clip my_music;
 
   /**
    *
    */
   public Music() throws LineUnavailableException, IOException,
   UnsupportedAudioFileException {
    final File musicFile = new File("/.../media/Thrustmusic.wav");
    final AudioInputStream sound;
    sound = AudioSystem.getAudioInputStream(musicFile);
     final DataLine.Info info;
    info = new DataLine.Info(Clip.class, sound.getFormat());
     Clip clip;
 
     clip = (Clip) AudioSystem.getLine(info);
 
    clip.open(sound);
 
   }
   /**
    *@return music.isRunning().;
    */
 
   public final  /*@ pure @*/ boolean playing() {
 
     return my_music.isRunning();
 
   }
 
   /**
    * Start playing the music.
    */
 
   public final void start() {
     my_music.loop(Clip.LOOP_CONTINUOUSLY);
   }
 
   /**
    * Stop playing the music.
    */
   //@ ensures !is_playing;
   public final void stop() {
     my_music.stop();
   }
 
 }
