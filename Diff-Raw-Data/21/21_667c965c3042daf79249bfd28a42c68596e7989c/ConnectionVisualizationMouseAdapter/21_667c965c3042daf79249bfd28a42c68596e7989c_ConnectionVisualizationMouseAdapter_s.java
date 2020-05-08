 /**
  * ConnectionVisualizationMouseAdapter
  * <p>
  * Handler for mouse events on the ConnectionVisualization frame
  * 
  * @author Marius Spix
  */
 
 package de.bwv_aachen.dijkstra.gui;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Rectangle2D;
 import java.util.Map;
 
 import javax.swing.JFrame;
 
 import de.bwv_aachen.dijkstra.model.Airport;
 
 public class ConnectionVisualizationMouseAdapter extends MouseAdapter {
     
         private ConnectionVisualization cv;
     
         public ConnectionVisualizationMouseAdapter(ConnectionVisualization cv) {
             this.cv = cv;
         }
 
         @Override
         public void mouseClicked(MouseEvent me) {
             Point clicked = me.getPoint();          
 
            for (Map.Entry<Rectangle2D.Double,Airport> entry : cv.getActionAreas().entrySet()) {
                Rectangle2D.Double area = entry.getKey();
                Airport            ap   = entry.getValue();
                
                 //check whether the clicked point is within a actionArea
                 if (area.contains(clicked)) {
                      if (cv.getStartAirport() == null) {
                          //Selection of the start airport
                          cv.setStartAirport(ap);
                      }
                      
                      else if (cv.getDestinationAirport() == null) {
                          //Selection of the destinationAirport
                          
                          if (ap == cv.getStartAirport()) {
                              //The destination airport is equal to the start airport
                              //Undo the selection
                              cv.reset();
                          } else {
                              //Set the destination airport
                              cv.setDestinationAirport(ap);
                          }
                      }
                      
                      else {
                          //another airport is clicked, reset the connection visualization
                          cv.reset();
                      }
 
                      //repaint everything
                     cv.getContentPane().repaint();
                 }
             }
       
          }
         
         @Override
         public void mouseMoved(MouseEvent me) {
              Point movedTo = me.getPoint();
              Component source = (Component) me.getSource();
              
              //The mouse has been moved
              
              //Has the mouse been moved to an airport?
              for (Map.Entry<Rectangle2D.Double,Airport> entry : cv.getActionAreas().entrySet()) {
                  Rectangle2D.Double area = entry.getKey();
                  Airport            ap   = entry.getValue();
                  
                  //Yes, then use the hand cursor!
                  if (area.contains(movedTo)) {
                      source.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                      return;
                  }
              }
              
              //No, then use the default cursor!
              source.setCursor(Cursor.getDefaultCursor());
          }
 
 }
