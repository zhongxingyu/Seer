 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package player;
 
 import java.awt.KeyEventDispatcher;
 import java.awt.event.KeyEvent;
 import util.EnemyDeletable;
 
 /**
  *
  * @author harrison
  */
 public class KeyboardControlledPlayer extends Player implements KeyEventDispatcher {
     private int up, left, down, right, speed, sbskey;
     private boolean mUp = false, mLeft = false, mDown = false, mRight = false;
     
     public KeyboardControlledPlayer(int _x, int _y, int _lives, boolean _active,
                                     int _sbsk, EnemyDeletable _parent,
                                     int _up, int _left, int _down, int _right,
                                     int _speed, int _sbskey) {
         super(_x, _y, _lives, _active, _sbsk, _parent);
         up = _up; down = _down; left = _left; right = _right;
         speed = _speed;
         sbskey = _sbskey;
     }
 
     public boolean dispatchKeyEvent(KeyEvent e) {
         boolean pressed = e.getID() == KeyEvent.KEY_PRESSED;
         int ev = e.getKeyCode();
         if (ev == up) {
             mUp = pressed;
             return true;
         }
         if (ev == left) {
             mLeft = pressed;
             return true;
         }
         if (ev == down) {
             mDown = pressed;
             return true;
         }
         if (ev == right) {
             mRight = pressed;
             return true;
         }
         if (ev == sbskey) {
             senbonSakura();
             return true;
         }
         return false;
     }
 
     public void move() {
        if(mRight)
        {
            x++;
        }
        if(mLeft)
        {
            x--;
        }
        if(mDown)
        {
            y++;
        }
        if(mUp)
        {
            y--;
        }
     }
 }
