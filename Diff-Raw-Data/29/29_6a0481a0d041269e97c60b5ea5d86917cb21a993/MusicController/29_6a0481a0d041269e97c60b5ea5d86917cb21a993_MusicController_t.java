 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package superkidsapplication.controllers;
 
 import java.net.URL;
 import javax.sound.sampled.*;
 
 /**
 * @author baris
 *
 * MUSIC by www.nosoapradio.us track: GameForest
  *
  * a method to play audio clips:
  * http://www3.ntu.edu.sg/home/ehchua/programming/java/J8c_PlayingSound.html
  *
  * also http://www.developer.com/java/other/print.php/2173111#Summary
  *
  * a large wav file cannot be played using the method in the first link refer to
  * this article: http://codeidol.com/java/swing/Audio/Play-Non-Trivial-Audio/
  * you get this exception: javax.sound.sampled.LineUnavailableException: Failed
  * to allocate clip data: Requested buffer too large. it needs to be buffered,
  * thats why we are using the PlayThread.
  *
  */
 public class MusicController {
 
     private AudioFormat audioFormat;
     private AudioInputStream audioInputStream;
     private SourceDataLine sourceDataLine;
     private Boolean stopPlayback;
     private URL urlOfFile;
 
     private MusicController() {
         //initally not playing
         stopPlayback = true;
     }
 
     private static class MusicControllerHolder {
 
         public static final MusicController INSTANCE = new MusicController();
     }
 
     public static MusicController getInstance() {
         return MusicController.MusicControllerHolder.INSTANCE;
     }
 
     //load main theme
     public void loadThemeMusic() {
         try {
             urlOfFile = getClass().getResource("/music/GameForest.wav");
             audioInputStream = AudioSystem.getAudioInputStream(urlOfFile);
             audioFormat = audioInputStream.getFormat();
             System.out.println("Audio Format: " + audioFormat);
 
             DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
 
             sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
 
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
 
     public void playMusic() {
        //if the music has been stopped before...
        if (stopPlayback == true) {
            //then restart.
            //this prevents creating new threads when there is one already running
            stopPlayback = false;
            System.out.println("Start play thread");
            new PlayThread().start();
        }
     }
 
     public void stopMusic() {
         stopPlayback = true;
     }
 
     public void setVolume(double gain) {
         FloatControl gainControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
        // set the gain
         float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
         gainControl.setValue(dB);
     }
 
 //=============================================//
 //Inner class to play back the data from the
 // audio file.
     class PlayThread extends Thread {
 
         byte tempBuffer[] = new byte[10000];
 
         public void run() {
             try {
                 sourceDataLine.open(audioFormat);
                 sourceDataLine.start();
 
                 int cnt;
                 //Keep looping until stopPlayback is set to true.
                 while (stopPlayback == false) {
                     cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length);
                     if (cnt > 0) {
                         //Write data to the internal buffer of
                         // the data line where it will be
                         // delivered to the speaker.
                         sourceDataLine.write(
                                 tempBuffer, 0, cnt);
                     }//if end of stream then reset
                     if (cnt == -1) {
                         System.out.println("Reset the input(open file again)");
                         audioInputStream = AudioSystem.getAudioInputStream(urlOfFile);
                     }
                 }//end while
                 //Block and wait for internal buffer of the
                 // data line to empty.
                 sourceDataLine.drain();
                 sourceDataLine.close();
 
             } catch (Exception e) {
                 e.printStackTrace();
                 System.exit(0);
             }//end catch
         }//end run
     }//end inner class PlayThread
 //===================================//
 }
