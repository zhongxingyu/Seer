 package irgame.input;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 
 /**
  *
  * @author Daniel
  */
 public class Keyboard implements KeyListener{
   
    private boolean[] keys = new boolean[65536];    //65536 = Maximum value of the character array.
     public boolean up, left, right, r;
     
     public void update(){   //Sets the key variables to true or false depending on if the keys arraylist return true.
         up = keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W];
         left = keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A];
         right = keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D];
         r = keys[KeyEvent.VK_R];
     }
     
     public void keyPressed(KeyEvent e) {    //Checks if a key is pressed and if so, stores true at the position of the key code value in the keys array
         keys[e.getKeyCode()] = true;
     }
 
     public void keyReleased(KeyEvent e) {   //Checks if a key is pressed and if so, stores false at the position of the key code value in the keys array
         keys[e.getKeyCode()] = false;
     }
     
     public void keyTyped(KeyEvent e) {    
     }
 }
