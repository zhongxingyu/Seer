 package interiores.presentation.swing.views.editor.tools;
 
 import interiores.business.controllers.FixedElementController;
import interiores.core.business.BusinessException;
 import interiores.core.presentation.SwingController;
 import interiores.presentation.swing.views.map.InteractiveRoomMap;
 import interiores.utils.Dimension;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 /**
  *
  * @author alvaro
  */
 public class PillarTool
     extends EditorTool
 {
     private FixedElementController fixedController;
     private Point minPosition;
     private Point maxPosition;
     private final int MOUSE_THRESHOLD = 6;
     
     public PillarTool(SwingController swing) {
         super("Pillar", "cube.png", KeyEvent.VK_P);
         fixedController = swing.getBusinessController(FixedElementController.class);
     }
     
     @Override
     public boolean mouseReleased(MouseEvent evt, InteractiveRoomMap map) {
 
         updatePositions(map.unpad(map.normDiscretize(evt.getPoint())));
        Dimension pillarArea =  new Dimension(0,0);
         if (minPosition != null && maxPosition != null) {
            pillarArea = new Dimension((maxPosition.x - minPosition.x),
                                       (maxPosition.y - minPosition.y));
        }
        if (pillarArea.depth != 0 && pillarArea.width != 0) {
            fixedController.addPillar(minPosition, pillarArea);
             return true;
         }
         return false;
     }
     
     @Override 
     public boolean mouseDragged(MouseEvent evt, InteractiveRoomMap map) {
         if (minPosition != null && maxPosition != null) {
             updatePositions(map.unpad(map.normDiscretize(evt.getPoint())));
             map.previewPillar(minPosition, new Dimension((maxPosition.x - minPosition.x),
                                                          (maxPosition.y - minPosition.y)));
             return true;
         }
         return false;
     }
     
     @Override
     public boolean mousePressed(MouseEvent evt, InteractiveRoomMap map) {
         setPositions(map.unpad(map.normDiscretize(evt.getPoint())));
         return false;
     }
     
     
     private void setPositions(Point p) {     
         minPosition = new Point(p);
         maxPosition = new Point(p);
     }
     
     private void updatePositions(Point p) {
         if (minPosition != null && maxPosition != null) {
             if (testIn(minPosition.x, maxPosition.x, p.x, MOUSE_THRESHOLD) || p.x < minPosition.x) minPosition.x = p.x;
             if (testIn(minPosition.y, maxPosition.y, p.y, MOUSE_THRESHOLD) || p.y < minPosition.y) minPosition.y = p.y;
             if (testIn(maxPosition.x, minPosition.x, p.x, MOUSE_THRESHOLD) || p.x > maxPosition.x) maxPosition.x = p.x;
             if (testIn(maxPosition.y, minPosition.y, p.y, MOUSE_THRESHOLD) || p.y > maxPosition.y) maxPosition.y = p.y;
         }
     }
     
     private boolean testIn(int x, int y, int value, int threshold) {
         // We have moved, but are we far enough from x but close enough to y?
         return Math.abs(x - value) < threshold && Math.abs(y - value) > threshold;
     }
     
     @Override
     public boolean keyReleased(KeyEvent evt, InteractiveRoomMap map) {
         if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
             minPosition = null;
             maxPosition = null;
             return true;
         }
         return false;
     }
 }
