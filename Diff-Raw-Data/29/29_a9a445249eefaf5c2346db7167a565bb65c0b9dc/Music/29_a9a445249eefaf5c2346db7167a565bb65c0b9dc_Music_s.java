 package thrust.audio;
 
 import java.io.File;
 import javax.sound.sampled.*;
 /**
  * In-game music.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 2 April 2008
  */
 public class Music {
   //@ public model boolean is_playing;
 
   private File musicFile = new File("../../../media/Thrust_music.mp3");
   Clip music = null;
   
  /**
   * @return Is music playing?
   */
  //@ ensures \result == is_playing;
   public Music(){
      AudioInputStream audioStream = null;
     
     try{
       audioStream = AudioSystem.getAudioInputStream(musicFile);
     }  catch (Exception e){
       e.printStackTrace();
     }
     
     final AudioFormat aFormat = audioStream.getFormat();
     final DataLine.Info info = new DataLine.Info(SourceDataLine.class, aFormat);
   
     try {
       music = (Clip) AudioSystem.getLine(info);
       music.open(audioStream);
     }catch (Exception e){
       e.printStackTrace();
     }
     
   }
   public /*@ pure @*/ boolean playing() {
     return music.isRunning();
    //@ assert false;
   }
 
   /**
    * Start playing the music.
    */
   //@ ensures is_playing;
   public void start() {
     music.loop(Clip.LOOP_CONTINUOUSLY);
   }
 
   /**
    * Stop playing the music.
    */
   //@ ensures !is_playing;
   public void stop() {
     music.stop();
   }
 }
