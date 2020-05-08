 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.ui.select;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Rectangle2D;
 import java.util.LinkedList;
 
 /**
  *
  * @author epsilon
  */
 public class RectangleMouseSelector extends MouseAdapter {
 
     private int rectangleSelectStartX, rectangleSelectStartY;
    LinkedList<RectangleSelectionListener> listeners;
     private boolean inRectangleSelecting = false;
 
     @Override
     public void mousePressed(MouseEvent e) {
         if (isRectanglePickingStart(e)) {
             inRectangleSelecting = true;
             setRectangleStart(e);
         }
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         if (isRectanglePickingStart(e)) {
             reportCadidateSelection(e);
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         if (isRectanglePicked(e)) {
             reportNewSelection(e);
             inRectangleSelecting = false;
         }
     }
 
     private boolean isRectanglePickingStart(MouseEvent e) {
         int mask = e.getModifiersEx();
         return ((mask & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
                 && ((mask & (MouseEvent.BUTTON2_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) == 0);
     }
 
     private void setRectangleStart(MouseEvent e) {
         rectangleSelectStartX = e.getX();
         rectangleSelectStartY = e.getY();
     }
 
     private boolean isRectanglePicked(MouseEvent e) {
         return inRectangleSelecting && e.getButton() == MouseEvent.BUTTON1;
     }
 
     synchronized private void reportNewSelection(MouseEvent e) {
         RectangleSelectionEvent rectangleSelectionEvent = genRectangleSelectionEvent(e);
         for (RectangleSelectionListener l : listeners) {
             l.rectangleSelected(rectangleSelectionEvent);
         }
     }
 
     private RectangleSelectionEvent genRectangleSelectionEvent(MouseEvent e) {
         Rectangle2D rectangle = new Rectangle2D.Double(
                 rectangleSelectStartX, rectangleSelectStartY,
                 -rectangleSelectStartX + e.getX(), -rectangleSelectStartY + e.getY());
         RectangleSelectionEvent rectangleSelectionEvent =
                 new RectangleSelectionEvent(this, rectangle, isKeepFormerSelections(e));
         return rectangleSelectionEvent;
     }
 
     private boolean isKeepFormerSelections(MouseEvent e) {
         return e.isShiftDown();
     }
 
     synchronized private void reportCadidateSelection(MouseEvent e) {
         RectangleSelectionEvent rectangleSelectionEvent = genRectangleSelectionEvent(e);
         for (RectangleSelectionListener l : listeners) {
             l.candidateRectangleSelected(rectangleSelectionEvent);
         }
     }
 
     synchronized public void addRectangleSelectionListener(RectangleSelectionListener l) {
         listeners.add(l);
     }
 
     synchronized public void removeRectangleSelectiongListener(RectangleSelectionListener l) {
         listeners.remove(l);
     }
 }
