package edu.mines.acmX.exhibit.input_services.keyboard;
 
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import edu.mines.acmX.exhibit.input_services.ButtonGestureInputDriver;
 
 /**
  * Input driver which maps keyboard press events into gesture events.
  */
 public final class KeyboardGestureInputDriver
     extends ButtonGestureInputDriver
     implements KeyListener {
    public void installInto(Component man) {
        man.addKeyListener(this);
     }
 
     public void keyPressed(KeyEvent e) {
         pressed(e.getKeyCode());
     }
 
     public void keyReleased(KeyEvent e) {}
     public void keyTyped(KeyEvent e) {}
 }
