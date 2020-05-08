 package com.servegame.abendstern.tunnelblick.backend;
 
 import java.awt.event.*;
 
 /**
  * Input driver which translates mouse motion into pointer and/or body
  * control.
  *
  * The driver is modal in that, at any given time, it is controlling EITHER
  * pointer zero or body 0. The mouse whell is used to select mode; up shifts to
  * pointer mode, down to body mode.
  */
 public final class ModalMouseMotionInputDriver
 extends AWTInputDriver
 implements MouseMotionListener, MouseWheelListener {
   private boolean pointerMode = true;
   private GameManager manager = null;
 
   /**
    * Sets the mode to use. True is pointer mode, false is body mode.
    */
   public void setPointerMode(boolean pointerMode) {
     this.pointerMode = pointerMode;
   }
 
   public void installInto(GameManager man) {
     manager = man;
     man.getCanvas().addMouseMotionListener(this);
     man.getFrame ().addMouseWheelListener (this);
   }
 
   public void mouseMoved(MouseEvent evt) {
     java.awt.Component canvas = manager.getCanvas();
     float x = evt.getX() / (float)canvas.getWidth();
     float y = evt.getY() / (float)canvas.getHeight() * manager.vheight();
     //OpenGL's Y axis is upside-down
     y = manager.vheight() - y;
 
     InputStatus is = manager.getSharedInputStatus();
 
    synchronized (manager) {
      if (pointerMode) {
        is.pointers[0][0] = x;
        is.pointers[0][1] = y;
        enqueue(new InputEvent(InputEvent.TYPE_POINTER_MOVEMENT, 0, x, y));
      } else {
        is.bodies[0] = x;
        enqueue(new InputEvent(InputEvent.TYPE_BODY_MOVEMENT, 0, x, y));
      }
     }
   }
 
   public void mouseDragged(MouseEvent evt) {
     mouseMoved(evt);
   }
 
   public void mouseWheelMoved(MouseWheelEvent evt) {
     pointerMode = (evt.getWheelRotation() < 0);
   }
 }
