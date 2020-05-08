 package circlesvssquares;
 
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 public class CirclesVsSquares extends PApplet {
 
     public static final boolean USE_FULLSCREEN = false;
 
     private static final long serialVersionUID = 7397694443868429500L;
 
     private static CirclesVsSquares instance;
     public static CirclesVsSquares instance() {
         return instance; 
     }
 
     public static void main(String[] args) {
         PApplet.main(new String[] { CirclesVsSquares.class.getName() });
     }
 
     public boolean[] keys = new boolean[526];
     boolean mousePressed;
     boolean mouseClick;
     
     public boolean checkKey(String k) {
         for(int i = 0; i < keys.length; i++) {
             if(KeyEvent.getKeyText(i).toLowerCase().equals(k.toLowerCase())) {
                 return keys[i];
             }
         }
         return false;
     }
     
     private Scene currentScene = null;
     private Scene nextScene = null;
     
     public Scene getCurrentScene() {
         return currentScene;
     }
 
     @Override
     public boolean sketchFullScreen() {
         return USE_FULLSCREEN;
     }
     
     @Override
     public void setup() {
         instance = this;
         
         if (USE_FULLSCREEN) {
             size(displayWidth, displayHeight, P3D);
         } else {
             size(1000, 500, P3D);
         }
         
         // Setup a large font that won't look bad when it gets scaled
         PFont f = this.loadFont("UbuntuMono-Regular-50.vlw");
         this.textFont(f);
 
         // Make window resizable
         if (frame != null) {
             frame.setResizable(true);
         }
         
         // Antialiasing is good.
         smooth(8);
         
         resetValues();
         nextScene = new MenuScene(this);
         
         this.addFocusListener(new FocusListener() {
 
             @Override
             public void focusGained(FocusEvent e) {
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 resetValues();
             }
             
         });
     }
 
     private void resetValues() {
         this.mouseClick = false;
         this.mousePressed = false;
     }
     
     @Override
     public void draw() {
         if (nextScene != null) {
             if (currentScene != null) currentScene.cleanUp();
             currentScene = nextScene;
             
             // Reset mouseClick to avoid double clicks
             resetValues();
             currentScene.init();
             nextScene = null;
         }
         
         if (currentScene != null) {
             currentScene.update();
             currentScene.draw();
         }
     }
     
     @Override
     public void keyPressed() { 
         keys[keyCode] = true;
     }
 
     @Override
     public void keyReleased() { 
         keys[keyCode] = false; 
     }
 
     @Override
     public void mousePressed() {
         mousePressed = true;
         mouseClick = true;
     }
 
     @Override
     public void mouseReleased() {
         mousePressed = false;
        mouseClick = false;
     }
     
     public void changeScene(Scene scene) {
         nextScene = scene;
     }
 }
