 package draw;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Observable;
 import javax.swing.JPanel;
 
 /*
  * drawingPanel.java
  * @author Steven Horowitz
  * This is a sub-class that handles the drawing of all objects on the screen.
  */
 public class DrawingPanel extends JPanel{
     public DrawingPanel(){
         setSize(400,400);
         this.setBackground(Color.white);
 		this.setFocusable(true);
 		this.requestFocus(); 
     }
     
     /*
      * paintComponents
      * @param g - the graphics object for the class; handles drawing of all objects.
      * This is the method where all items are drawn in.
      */
     public void paintComponent(Observable myCanvas){
         super.paintComponent(this.getGraphics());
         Canvas newCanvas = (Canvas) myCanvas;
         ArrayList<DrawingPrimitive> myPrimitives = newCanvas.getPrimitives();
         for (int i = 0;i<myPrimitives.size(); i++)
         	myPrimitives.get(i).draw((Graphics2D)this.getGraphics());
     }
    
    public void repaint(){
    	
    }
 
     /*
      * public mouseClicked
      * @param arg0- the click of a mouse
      * This method registers any and all mouse clicks. Will likely be replaced by a more involved action handler.
      */
     public void mouseClicked(MouseEvent arg0) {}
     public void mouseEntered(MouseEvent arg0) {}
     public void mouseExited(MouseEvent arg0) {}
     public void mousePressed(MouseEvent arg0) {}
     public void mouseReleased(MouseEvent arg0) {}
     public void mouseDragged(MouseEvent arg0) {}
     public void mouseMoved(MouseEvent arg0)  {}
 }
