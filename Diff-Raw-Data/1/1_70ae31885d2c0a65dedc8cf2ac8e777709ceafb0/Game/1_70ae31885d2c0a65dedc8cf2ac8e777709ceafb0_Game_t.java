 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioNode;
 import com.jme3.font.BitmapText;
 import com.jme3.scene.Node;
 
 /**
  *
  * @author Vincent Séguin
  */
 public class Game {
 
     private AudioNode countdownSound;
     private AudioNode goSound;
     private Map map;
     private BitmapText bitmapText;
     private boolean isCountdownStarted = true;
     private int timerCount = 3;
 
     public Game(Map map, BitmapText text, AssetManager manager, Node node) {
         this.map = map;
         this.bitmapText = text;
         initializeSound(manager, node);
     }
 
     public void update(com.jme3.system.Timer timer, Team currentTeam) {
         if (isCountdownStarted()) {
             if (timer.getTimeInSeconds() >= 5) {
                 currentTeam.getCurrentPlayer().setCanShoot(true);
                 map.shootTarget();
                 setTimerText("");
                 timer.reset();
                 setIsCountdownStarted(false);
             } else if (timer.getTimeInSeconds() >= 4 && timerCount == 0) {
                 start();
             } else if (timer.getTimeInSeconds() >= 3 && timerCount == 1) {
                 count("1");
             } else if (timer.getTimeInSeconds() >= 2 && timerCount == 2) {
                 count("2");
             } else if (timer.getTimeInSeconds() >= 1 && timerCount == 3) {
                 count("3");
             }
         } else {
            timerCount = 3;
             timer.reset();
         }
     }
 
     public boolean isCountdownStarted() {
         return isCountdownStarted;
     }
 
     public void setIsCountdownStarted(boolean isStarted) {
         this.isCountdownStarted = isStarted;
     }
 
     private void start() {
         setTimerText("GO!!!");
         goSound.playInstance();
         timerCount--;
     }
 
     private void count(String countValue) {
         setTimerText(countValue);
         countdownSound.playInstance();
         timerCount--;
     }
 
     private void setTimerText(String value) {
         bitmapText.setText(value);
     }
 
     private void initializeSound(AssetManager assetManager, Node rootNode) {
         countdownSound = new AudioNode(assetManager, "Sounds/Beep.wav", false);
         countdownSound.setLooping(false);
         countdownSound.setVolume(3);
         rootNode.attachChild(countdownSound);
         goSound = new AudioNode(assetManager, "Sounds/Beep2.wav", false);
         goSound.setLooping(false);
         goSound.setVolume(3);
         rootNode.attachChild(goSound);
     }
 }
