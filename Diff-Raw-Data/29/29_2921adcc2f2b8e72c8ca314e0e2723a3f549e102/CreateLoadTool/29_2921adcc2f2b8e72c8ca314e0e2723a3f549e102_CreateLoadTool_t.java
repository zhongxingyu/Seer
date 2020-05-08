 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.tools;
 
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.manipulators.DragListener;
 import edu.gatech.statics.objects.manipulators.DragSnapListener;
 import edu.gatech.statics.objects.manipulators.DragSnapManipulator;
 import edu.gatech.statics.objects.manipulators.MousePressInputAction;
 import edu.gatech.statics.objects.manipulators.MousePressListener;
 import edu.gatech.statics.objects.manipulators.Tool;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public abstract class CreateLoadTool extends Tool implements MousePressListener {
 
     private Point loadAnchor;
     private List<Load> load;
     private Diagram diagram;
     private DragSnapManipulator dragManipulator;
     private Point snapPoint;
     private MousePressInputAction pressAction;
 
     protected Point getSnapPoint() {
         return snapPoint;
     }

     protected DragSnapManipulator getDragManipulator() {
         return dragManipulator;
     }

    protected void showLabelSelector() {
    }

     public CreateLoadTool(Diagram diagram) {
         this.diagram = diagram;
         loadAnchor = new Point(new Vector3f());
         load = createLoad(loadAnchor);
 
         for (Load aLoad : load) {
             diagram.add(aLoad);
         }
 
         pressAction = new MousePressInputAction();
         pressAction.addListener(this);
         addAction(pressAction);
     }
 
     /**
      * Subclasses should override this to indicate what load is being created
      * and placed by this tool.
      * @param anchor
      * @return
      */
     abstract protected List<Load> createLoad(Point anchor);
 
     public void onMouseDown() {
        if (getDragManipulator() != null) {
            if (releaseDragManipulator()) {
                showLabelSelector();
                finish();
            }
         }
     }
 
     public void onMouseUp() {
     }
 
     @Override
     protected void onActivate() {
         enableDragManipulator();
     }
 
     @Override
     protected void onCancel() {
         for (Load aLoad : load) {
             diagram.remove(aLoad);
         }
     }
 
     @Override
     protected void onFinish() {
     }
 
     protected void enableDragManipulator() {
 
         List<Point> pointList = new ArrayList();
         for (SimulationObject obj : diagram.allObjects()) {
             if (obj instanceof Point) {
                 pointList.add((Point) obj);
             }
         }
 
         dragManipulator = new DragSnapManipulator(loadAnchor.getTranslation(), pointList);
         addToAttachedHandlers(dragManipulator);
 
         DragListenerImpl listener = new DragListenerImpl();
         dragManipulator.addListener(listener);
         dragManipulator.addSnapListener(listener);
     }
 
     /**
      * attempts to release the drag manipulator, fixing the load at a snap point.
      * Subclasses that have orientation or do something afterwards may wish to have handler here.
      * @return true if the release succeeds. 
      */
     protected boolean releaseDragManipulator() {
 
         if (snapPoint == null) {
             return false;
         }
 
         // snap at the drag manipulator and terminate,
         // enable the orientation manipulator
         for (Load aLoad : load) {
             aLoad.setAnchor(snapPoint);
         }
 
         dragManipulator.setEnabled(false);
         removeFromAttachedHandlers(dragManipulator);
         dragManipulator = null;
 
         return true;
     }
 
     protected void updateLoad(Vector3f worldPosition) {
         loadAnchor.setTranslation(worldPosition);
     }
 
     private class DragListenerImpl implements DragListener, DragSnapListener {
 
         public void onDrag(Vector2f mousePosition, Vector3f worldPosition) {
             updateLoad(worldPosition);
         }
 
         public void onSnap(Point point) {
             snapPoint = point;
         }
     }
 }
