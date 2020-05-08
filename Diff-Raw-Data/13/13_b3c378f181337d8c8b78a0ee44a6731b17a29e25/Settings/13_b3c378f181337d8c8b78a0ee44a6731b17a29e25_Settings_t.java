 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Settings;
 
 import Main.GameMenu;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Random;
import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
 import javax.swing.JPanel;
 
 /**
  *
  * @author
  * Rune
  */
 public class Settings {
 
     public int WITDH = 640;
     public int HEIGHT = 420;
     public int FPS = 30;
     public double avrageFPS = 0;
     public boolean sound = true;
     public String songName = "";
     public AudioInputStream ais;
     public Clip clip;
 
     public void setAvrageFPS(double avrageFPS) {
         this.avrageFPS = avrageFPS;
     }
 
     /**
      * Set
      * the
      * desired
      * amount
      * of
      * frames
      * per
      * second
      * to
      * be
      * generated
      * for
      * the
      * visualization
      * of
      * the
      * in-game
      * graphics.
      *
      * @param
      * FPS
      * Integer
      * value
      * of
      * the
      * desired
      * FPS.
      */
     public void setFPS(int FPS) {
         this.FPS = FPS;
     }
 
     public void setWITDH(int WITDH) {
         this.WITDH = WITDH;
     }
 
     public void setHEIGHT(int HEIGHT) {
         this.HEIGHT = HEIGHT;
     }
 
     public void setSound(boolean sound) {
         this.sound = sound;
     }
     
     public void startMusic(JPanel p) throws Exception {
         if(p instanceof GameMenu){
             
 File file  = new File("./res/music/z3fairy.mid");
             ais = AudioSystem.
                 getAudioInputStream(file);
         }else{
         ArrayList<String> musicFiles = new ArrayList();
         musicFiles.add("wakemeup.mid");
         musicFiles.add("mario1.mid");
         musicFiles.add("pokemon1.mid");
         musicFiles.add("pokemon2.mid");
         musicFiles.add("pokemon3.mid");
         musicFiles.add("pokemon4.mid");
         musicFiles.add("zelda1.mid");
         musicFiles.add("aoe2.mid");
         musicFiles.add("tetris1.mid");
         musicFiles.add("sandstorm.mid");
         musicFiles.add("itsmylife.mid");
         musicFiles.add("jurassicpark.mid");
         musicFiles.add("jamesbond.mid");
         Random rn = new Random();
         int songnr = rn.nextInt(13);
         String song = musicFiles.get(songnr);
 
         //Description of songs
         if (song.contains("aoe")) {
             songName = "Theme from Age of Empires";
         } else if (song.contains("pokemon")) {
             songName = "Theme from Pokemon";
         } else if (song.contains("mario")) {
             songName = "Theme from Super Mario";
         } else if (song.contains("zelda")) {
             songName = "Theme from Zelda";
         } else if (song.contains("sandstorm")) {
             songName = "Darude - Sandstorm";
         } else if (song.contains("tetris")) {
             songName = "Theme from Tetris";
         } else if (song.contains("wakemeup")) {
             songName = "Avicii - Wake me up";
         } else if (song.contains("itsmylife")) {
             songName = "Bon Jovi - Its my life";
         } else if (song.contains("jurassicpark.mid")) {
             songName = "Theme from Jurassic Park";
         } else if (song.contains("jamesbond")) {
             songName = "Theme from James Bond";
        }
         File file = new File("./res/music/" + song);
         ais = AudioSystem.
                 getAudioInputStream(file);
         }
        AudioFormat format = ais.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
         // getAudioInputStream() also accepts a File or InputStream
        clip = (Clip) AudioSystem.getLine(info);
         clip.open(ais);
         clip.loop(Clip.LOOP_CONTINUOUSLY);
         
     }
 }
