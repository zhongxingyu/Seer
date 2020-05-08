 package thrust.audio;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.SourceDataLine;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 
 /**
  * In-game music.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 public class Music {
   //@ public model boolean is_playing;
   /**
    *indicates the music file to load.
    */
   private final transient File my_clipFile = new File("");
   /**
    * the name of our new file.
    */
   private transient Clip my_clip;
 
   public Music() throws Exception, Exception {
     AudioInputStream myaudioInputStream = null;
 
     final DataLine.Info info =
       new DataLine.Info(SourceDataLine.class, myaudioInputStream.getFormat());
 
     try {
      myaudioInputStream = AudioSystem.getAudioInputStream(clipFile);
     } catch (UnsupportedAudioFileException e) {
       e.printStackTrace();
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     try {
      myclip = (Clip) AudioSystem.getLine(info);
     } catch (LineUnavailableException e) {
       e.printStackTrace();
     }
    myclip.open(myaudioInputStream);
   }
     /**
      * @return Is music playing?
     */
     //@ ensures \result == is_playing;
   public /*@ pure @*/ boolean playing() {
     assert false; //@ assert false;
     return false;
   }
   /**
    * Start playing the music.
    */
   //@ ensures is_playing;
   public void start() {
    myclip.loop(Clip.LOOP_CONTINUOUSLY);
     assert false; //@ assert false;
   }
 
  /**
    * Stop playing the music.
    */
   //@ ensures !is_playing;
   public void stop() {
    myclip.stop();
     assert false; //@ assert false;
   }
 }
