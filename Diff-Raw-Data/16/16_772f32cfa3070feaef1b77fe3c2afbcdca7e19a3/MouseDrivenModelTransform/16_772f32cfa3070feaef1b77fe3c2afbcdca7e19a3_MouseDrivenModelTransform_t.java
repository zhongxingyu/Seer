 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.ui;
 
 import java.awt.Component;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 /**
  *
  * @author epsilon
  */
 public class MouseDrivenModelTransform extends ModelTransform implements MouseListener, MouseMotionListener, MouseWheelListener {
 
     public static final int ORIGIN_TRANSLATE_BUTTON_MASK = MouseEvent.BUTTON2_DOWN_MASK;
     public static final int SCALE_BY_CENTER_BUTTON_MASK = MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK;
     public static final double DEFAULT_DRAG_SCALE_RATIO = 0.04;
     public static final double DEFAULT_WHEEL_SCALE_RATIO = 0.33;
     int translateStartX, translateStartY;
     private double dragScaleRatio = DEFAULT_DRAG_SCALE_RATIO;
     private double wheelScaleRatio = DEFAULT_WHEEL_SCALE_RATIO;
     private int scaleCenterX;
     private int scaleCenterY;
    private int scaleLastDragY;
     boolean zoomAllNeeded = false;
 
     @Override
     public void mousePressed(MouseEvent e) {
         if (isOriginTranslate(e)) {
             setOriginTranslateStartPoint(e.getX(), e.getY());
         } else if (isScaleByCenter(e)) {
            startScale(e.getX(), e.getY());
         }
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         if (isOriginTranslate(e)) {
             translateOrigin(e.getX() - translateStartX, e.getY() - translateStartY);
             setOriginTranslateStartPoint(e.getX(), e.getY());
             e.getComponent().repaint();
         } else if (isScaleByCenter(e)) {
             scaleByDrag(e.getY());
             e.getComponent().repaint();
         }
     }
 
     @Override
     public void mouseWheelMoved(MouseWheelEvent e) {
        startScale(e.getX(), e.getY());
         scaleByWheel(e.getWheelRotation());
         e.getComponent().repaint();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON2 && e.getClickCount() == 2) {
             zoomAllNeeded = true;
             e.getComponent().repaint();
         }
     }
 
     public void addMouseActionListenersTo(Component comp) {
         comp.addMouseListener(this);
         comp.addMouseMotionListener(this);
         comp.addMouseWheelListener(this);
     }
 
     private void setOriginTranslateStartPoint(int startX, int startY) {
         translateStartX = startX;
         translateStartY = startY;
     }
 
    private void startScale(int x, int y) {
         scaleCenterX = x;
         scaleCenterY = y;
        scaleLastDragY = y;
     }
 
     void scaleByDrag(int y) {
        int dy = y - scaleLastDragY;
        scaleLastDragY = y;
         double scale = Math.pow(2, -dy * dragScaleRatio);
         scaleByCenter(scaleCenterX, scaleCenterY, scale);
     }
 
     void scaleByWheel(double clicks) {
         double scale = Math.pow(2, -clicks * wheelScaleRatio);
         scaleByCenter(scaleCenterX, scaleCenterY, scale);
     }
 
     public boolean isZoomAllNeeded() {
         return zoomAllNeeded;
     }
 
     public void setZoomAllNeeded(boolean zoomAllNeeded) {
         this.zoomAllNeeded = zoomAllNeeded;
     }
 
     private boolean isOriginTranslate(MouseEvent e) {
         return (e.getModifiersEx() & ORIGIN_TRANSLATE_BUTTON_MASK) == ORIGIN_TRANSLATE_BUTTON_MASK;
     }
 
     private boolean isScaleByCenter(MouseEvent e) {
         return (e.getModifiersEx() & SCALE_BY_CENTER_BUTTON_MASK) == SCALE_BY_CENTER_BUTTON_MASK;
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
     }
 }
