 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.thura.utilities.photo.gui;
 
 import java.awt.Event;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import javax.swing.JOptionPane;
 
 
 /**
  *
  * @author Alexander Colvin
  */
 public class PhotoCanvasMouseListener implements MouseListener, MouseWheelListener, KeyListener, MouseMotionListener{
     
     /**
      * The parent of this canvas
      */
     PhotoControl parent;
     
     /**
      * Status of shift
      */
     Boolean controlPressed;
     
     public PhotoCanvasMouseListener(PhotoControl parent){
         this.parent = parent;
         controlPressed = false;
     }
     
     public void mouseClicked(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
         
         try{
             parent.selectPhoto(x, y);
         } catch (Exception ex){
             JOptionPane.showMessageDialog(parent, 
                     "Please note that selection is unavailable at this time");
         }
         
         parent.redraw("Canvas click redraw");
     }
 
     public void mousePressed(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseReleased(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseEntered(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseExited(MouseEvent e) {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
     
     public void mouseWheelMoved(MouseWheelEvent event) {
 
         
         int change = event.getWheelRotation();
         if(parent.getScollPosition() == 0 && change < 0){
 
         } else if(parent.getScollPosition() == (parent.getScollMax() - parent.SCROLL_BASE)
                 && change > 0){
 
         } else {
             parent.setScollPosition(parent.getScollPosition() + change);
             parent.redraw("WHEEL MOVED: " + parent.getScollPosition());
         }
           
     }
 
     public void keyTyped(KeyEvent e) {
         System.out.println("key released");
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void keyPressed(KeyEvent e) {
         System.out.println("key released");
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void keyReleased(KeyEvent e) {
         System.out.println("key released");
        if((e.getKeyCode() == Event.CTRL_MASK)){
            controlPressed = false;
            System.out.println("Control released");
        }
     }
 
     public void mouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void mouseMoved(MouseEvent e) {
        parent.highlightPhoto(e.getX(),e.getY());   
     }
 
     
     
 }
