 package game;
 
 import java.awt.Image;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.ImageIcon;
 
 /**
  * The <code>GameAssets</code> class loads and provides
  * references for all image and sound files used in the
  * game.
  * @author Nikolaos Bukas
  */
 public class GameAssets {
 
     //all sounds and images are accessible
     /**
      * Sound for victory.
      */
     public static Sound victory;
     /**
      * Sound for game over.
      */
     public static Sound gameOver;
     /**
      * Sound for no bomb.
      */
     public static Sound noBombs;
     /**
      * Sound for bomb used.
      */
     public static Sound bombUsed;
     /**
      * Sound for 3 shield points.
      */
     public static Sound shields3;
     /**
      * Sound for 2 shield points.
      */
     public static Sound shields2;
     /**
      * Sound for 1 shield point.
      */
     public static Sound shields1;
     /**
      * Sound for no shield points left.
      */
     public static Sound shields0;
     /**
      * Sound for crashing.
      */
     public static Sound crash;
     /**
      * Sound for explosion.
      */
     public static Sound explosion;
     /**
      * Sound for player firing.
      */
     public static Sound playerFire;
     /**
      * Sound for alien firing.
      */
     public static Sound alienFire;
     /**
      * Sound for theme.
      */
     public static Sound theme;
     /**
      * Sound for missile.
      */
     public static Sound missile;
     /**
      * Sound for alien saucer.
      */
     public static Sound alienSaucer;
     /**
      * Sound for alien detection.
      */
     public static Sound alienDetected;
     /**
      * Sound for thrusters.
      */
     public static Sound thrusters;
     /**
      * Sound for warp.
      */
     public static Sound warp;
     /**
      * Sound for power up.
      */
     public static Sound powerUp;    
     /**
      * Sound for space.
      */
     public static Sound spaceSound;        
     /**
      * Image for game background.
      */
     public static Image spaceBackground;
     /**
      * Image for main menu.
      */
     public static Image menuImage;
     /**
      * Image for tutorial.
      */
     public static Image tutorialImage;
     /**
      * Image for game over.
      */
     public static Image gameOverImage;
     /**
      * Image for title page.
      */
     public static Image titleImage;
 
     /**
      * Load all the sounds.
      */
     public static void loadSounds() {
         try {
             victory = new Sound("GoalsComplete.wav");
             noBombs = new Sound("NoTorpedos.wav");
             bombUsed = new Sound("TorpedoFired.wav");
             shields3 = new Sound("Shields100.wav");
             shields2 = new Sound("Shields75.wav");
             shields1 = new Sound("Shields25.wav");
             shields0 = new Sound("ShieldsFailed.wav");
             crash = new Sound("crash.wav");
             explosion = new Sound("explosion.wav");
             playerFire = new Sound("Cardassian Cannon.wav");
             alienFire = new Sound("fire.wav");
             theme = new Sound("menu.wav");
             missile = new Sound("missle.wav");
             alienSaucer = new Sound("saucer.wav");
             alienDetected = new Sound("Incoming1.wav");
             thrusters = new Sound("engine1.wav");
             warp = new Sound("enter warp.wav");
             gameOver = new Sound("GameOver.wav");
             powerUp = new Sound("PowerUp.wav");
             spaceSound = new Sound("spaceSound.wav");
         } catch (UnsupportedAudioFileException ex) {
             Logger.getLogger(GameAssets.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(GameAssets.class.getName()).log(Level.SEVERE, null, ex);
         } catch (LineUnavailableException ex) {
             Logger.getLogger(GameAssets.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NullPointerException e) {
         }
     }
 
     /**
      * Loads all images.
      */
     public static void loadImages() {
         spaceBackground = new ImageIcon(GameAssets.class.getResource("images/spaceBackground.jpg")).getImage();
         tutorialImage = new ImageIcon(GameAssets.class.getResource("images/keyboard.png")).getImage();
         gameOverImage = new ImageIcon(GameAssets.class.getResource("images/GameOver.jpg")).getImage();
         menuImage = new ImageIcon(GameAssets.class.getResource("images/asteroids.jpg")).getImage();
        titleImage = new ImageIcon(GameAssets.class.getResource("images/AMBroSIA.png")).getImage();
     }
 }
