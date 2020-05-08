package edu.mines.csci598.recycler.backend;
 
import java.awt.event.*;
import java.util.HashMap;
 
 /**
  * Input driver which maps keyboard press events into gesture events.
  */
 public final class KeyboardGestureInputDriver
     extends ButtonGestureInputDriver
     implements KeyListener {
    public void installInto(GameManager man) {
        man.getFrame().addKeyListener(this);
     }
 
     public void keyPressed(KeyEvent e) {
         pressed(e.getKeyCode());
     }
 
     public void keyReleased(KeyEvent e) {}
     public void keyTyped(KeyEvent e) {}
 }
