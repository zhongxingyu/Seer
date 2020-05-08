 /*
  * An outline view for a specific graph component.
  * This fixes a bug in the current jgraphXimplementation where
  * you could zoom in indefinitely via minimap and crash the program.
  *
  * $Header$
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 
 package teo.isgci.drawing;
 
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JScrollBar;
 
 import com.mxgraph.swing.mxGraphComponent;
 import com.mxgraph.swing.mxGraphOutline;
 import com.mxgraph.view.mxGraphView;
 
 /**
  * An outline view for a specific graph component.
  * This fixes a bug in the current jgraphXimplementation where
  * you could zoom in indefinitely via minimap and crash the program.
  */
 class ISGCImxGraphOutline extends mxGraphOutline {
 
     /** serial version. */
     private static final long serialVersionUID = 1L;
 
 
     /**
      * Creates a custom isgci graphoutline with maximum zoomlevels.
      * @param graphComponent
      *          The corresponding graphComponent
      */
     public ISGCImxGraphOutline(mxGraphComponent graphComponent) {
         super(graphComponent);
         
         // remove mousemotionlistener and mouselistener
         MouseListener[] mouseListeners = getMouseListeners();
         for (MouseListener ml : mouseListeners) {
             removeMouseListener(ml);
         }
         
         MouseMotionListener[] mouseMotionListeners = getMouseMotionListeners();
         for (MouseMotionListener mml : mouseMotionListeners) {
             removeMouseMotionListener(mml);
         }
         
         // add bug-free listeners
         BuglessMouseTracker bugFreeTracker = new BuglessMouseTracker();
         
         addMouseMotionListener(bugFreeTracker);
         addMouseListener(bugFreeTracker);
     }
 
 
     /**
          *
          */
    class BuglessMouseTracker implements MouseListener, MouseMotionListener {
         /** startpoint. */
         protected Point start = null;
 
         @Override
         public void mousePressed(MouseEvent e) {
             zoomGesture = hitZoomHandle(e.getX(), e.getY());
 
             if (graphComponent != null && !e.isConsumed()
                     && !e.isPopupTrigger()
                     && (finderBounds.contains(e.getPoint()) || zoomGesture)) {
                 start = e.getPoint();
            } else { 
                System.out.println("vsdf");                    
                int dx = (int) ((e.getX() - translate.x 
                        - finderBounds.getWidth() / 2) / scale);
                int dy = (int) ((e.getY() - translate.y 
                        - finderBounds.getHeight() / 2) / scale);

                // Keeps current location as start for delta movement
                // of the scrollbarsq

                graphComponent.getHorizontalScrollBar().setValue(dx);
                graphComponent.getVerticalScrollBar().setValue(dy);
             }
         }
 
         @Override
         public void mouseDragged(MouseEvent e) {
             if (isEnabled() && start != null) {
                 if (zoomGesture) {
                     Rectangle bounds = graphComponent.getViewport()
                             .getViewRect();
                     double viewRatio = bounds.getWidth() / bounds.getHeight();
 
                     bounds = new Rectangle(finderBounds);
                     bounds.width = (int) Math.max(0,
                             (e.getX() - bounds.getX()));
                     bounds.height = (int) Math.max(0,
                             (bounds.getWidth() / viewRatio));
 
                     updateFinderBounds(bounds, true);
                 } else {
                     // TODO: To enable constrained moving, that is, moving
                     // into only x- or y-direction when shift is pressed,
                     // we need the location of the first mouse event, since
                     // the movement can not be constrained for incremental
                     // steps as used below.
                     int dx = (int) ((e.getX() - start.getX()) / scale);
                     int dy = (int) ((e.getY() - start.getY()) / scale);
 
                     // Keeps current location as start for delta movement
                     // of the scrollbars
                     start = e.getPoint();
 
                     graphComponent.getHorizontalScrollBar().setValue(
                             graphComponent.getHorizontalScrollBar().getValue()
                                     + dx);
                     graphComponent.getVerticalScrollBar().setValue(
                             graphComponent.getVerticalScrollBar().getValue()
                                     + dy);
                 }
             }
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
             if (start != null) {
                 if (zoomGesture) {
                     double dx = e.getX() - start.getX();
                     double w = finderBounds.getWidth();
 
                     final JScrollBar hs = graphComponent
                             .getHorizontalScrollBar();
                     final double sx;
 
                     if (hs != null) {
                         sx = (double) hs.getValue() / hs.getMaximum();
                     } else {
                         sx = 0;
                     }
 
                     final JScrollBar vs = graphComponent
                             .getVerticalScrollBar();
                     final double sy;
 
                     if (vs != null) {
                         sy = (double) vs.getValue() / vs.getMaximum();
                     } else {
                         sy = 0;
                     }
 
                     mxGraphView view = graphComponent.getGraph().getView();
                     double scale = view.getScale();
                     double newScale = scale - (dx * scale) / w;
                     newScale = Math.min(newScale, 8);
 
                     double factor = newScale / scale;
                     view.setScale(newScale);
 
                     if (hs != null) {
                         hs.setValue((int) (sx * hs.getMaximum() * factor));
                     }
 
                     if (vs != null) {
                         vs.setValue((int) (sy * vs.getMaximum() * factor));
                     }
                 }
 
                 zoomGesture = false;
                 start = null;
             }
         }
 
         /**
          *  Taken from mxGraphOutline.
          * @param x Parameter x.
          * @param y Parameter y.
          * @return a boolean.
          */
         public boolean hitZoomHandle(int x, int y) {
             return new Rectangle(finderBounds.x + finderBounds.width - 6,
                     finderBounds.y + finderBounds.height - 6, 8, 8).contains(
                     x, y);
         }
 
         @Override
         public void mouseMoved(MouseEvent e) {
             if (hitZoomHandle(e.getX(), e.getY())) {
                 setCursor(new Cursor(Cursor.HAND_CURSOR));
             } else if (finderBounds.contains(e.getPoint())) {
                 setCursor(new Cursor(Cursor.MOVE_CURSOR));
             } else {
                 setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
         }
 
         @Override
         public void mouseClicked(MouseEvent e) {
             // ignore
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {
             // ignore
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
             // ignore
         }
 
     }
 
 }
 
 /* EOF */
