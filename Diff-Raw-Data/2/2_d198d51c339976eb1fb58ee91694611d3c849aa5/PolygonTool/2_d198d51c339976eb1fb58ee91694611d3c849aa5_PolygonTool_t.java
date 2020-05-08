 package de.sofd.draw2d.viewer.tools;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 import de.sofd.draw2d.PolygonObject;
 import de.sofd.draw2d.viewer.DrawingViewer;
 
 /**
  * DrawingViewerTool that allows the end user to interactively create closed
  * {@link PolygonObject}s on the drawing by dragging the outline of the
  * new polygon with the mouse. After the user releases the mouse button,
  * the polygon is closed and its creation is complete.
  * <p>
  * Sets the {@link TagNames#TN_CREATION_COMPLETED} tag whenever a new polygon
  * has been completed.
  *
  * @author olaf
  */
 public class PolygonTool extends DrawingViewerTool {
 
     /**
      * The polygon we're currently creating
      */
     private PolygonObject currentPolygon;
     
     private Point2D latestPointDisp;
     
     private static final double MIN_EDGE_LENGTH_DISP = 3.0;
 
     @Override
     public void associateWithViewer(DrawingViewer viewer) {
         super.associateWithViewer(viewer);
         currentPolygon = null;
     }
     
     @Override
     public void disassociateFromViewer() {
         if (null != currentPolygon) {
             getAssociatedViewer().getDrawing().removeDrawingObject(currentPolygon);
         }
         super.disassociateFromViewer();
     }
 
     protected PolygonObject createNewPolygon() {
         PolygonObject result = new PolygonObject();
         result.setColor(Color.RED);
         return result;
     }
     
     @Override
     public void mousePressed(MouseEvent e) {
         if (null != currentPolygon) {
             getAssociatedViewer().getDrawing().removeDrawingObject(currentPolygon);
             currentPolygon = null;
         }
         latestPointDisp = e.getPoint();
     }
     
     @Override
     public void mouseDragged(MouseEvent e) {
         Point2D currPointDisp = e.getPoint();
        if(latestPointDisp == null)
            return;
         if (getDistance(currPointDisp, latestPointDisp) < MIN_EDGE_LENGTH_DISP) {
             return;
         }
         if (null == currentPolygon) {
             Point2D latestPoint = getAssociatedViewer().displayToObj(latestPointDisp);
             currentPolygon = createNewPolygon();
             currentPolygon.setClosed(false);
             currentPolygon.appendPoint(latestPoint);
             getAssociatedViewer().getDrawing().addDrawingObject(currentPolygon);
         }
         Point2D currPoint = getAssociatedViewer().displayToObj(currPointDisp);
         currentPolygon.appendPoint(currPoint);
         latestPointDisp = currPointDisp;
     }
     
     @Override
     public void mouseReleased(MouseEvent e) {
         if (null != currentPolygon) {
             currentPolygon.setClosed(true);
             PolygonObject p = currentPolygon;
             currentPolygon = null;
             p.setTag(TagNames.TN_CREATION_COMPLETED, true);
         }
     }
 
     private static double getDistance(Point2D pt1, Point2D pt2) {
         double dx = pt1.getX() - pt2.getX();
         double dy = pt1.getY() - pt2.getY();
         return Math.sqrt(dx*dx + dy*dy);
     }
 }
