 package dgh.game;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class InputHandler implements KeyListener {
 
     public InputHandler(Game game) {
         game.addKeyListener(this);
     }
 
     public class Key {
         private int numTimesPressed = 0;
         private boolean pressed = false;
 
         public int getNumTimesPressed() {
             return numTimesPressed;
         }
 
         public boolean isPressed() {
             return pressed;
         }
 
         public void toggle(boolean isPressed) {
             pressed = isPressed;
             if (isPressed) numTimesPressed++;
         }
     }
 
     public Key up = new Key();
     public Key down = new Key();
     public Key left = new Key();
     public Key right = new Key();
     public Key space = new Key();
     public Key i = new Key();
     public Key c = new Key();
     public Key esc = new Key();
     public Key one = new Key();
     public Key two = new Key();
     public Key three = new Key();
     public Key four = new Key();
     public Key five = new Key();
 
     public void keyPressed(KeyEvent e) {
         toggleKey(e.getKeyCode(), true);
        if(e.getKeyCode() == KeyEvent.VK_I) {
        	Game.playerHud.showInventory = Game.playerHud.showInventory ? false:true;
        }
     }
 
     public void keyReleased(KeyEvent e) {
         toggleKey(e.getKeyCode(), false);
     }
 
     public void keyTyped(KeyEvent e) {
     }
 
     public void toggleKey(int keyCode, boolean isPressed) {
         if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_UP) {
             up.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_S || keyCode == KeyEvent.VK_DOWN) {
             down.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_LEFT) {
             left.toggle(isPressed);
         }
         if (keyCode == KeyEvent.VK_D || keyCode == KeyEvent.VK_RIGHT) {
             right.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_SPACE) {
         	space.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_I) {
         	i.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_C) {
         	c.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_ESCAPE) {
         	esc.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_1) {
         	one.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_2) {
         	two.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_3) {
         	three.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_4) {
         	four.toggle(isPressed);
         }
         if(keyCode == KeyEvent.VK_5) {
         	five.toggle(isPressed);
         }
     }
 }
