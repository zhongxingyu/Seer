 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.tools;
 
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.math.AnchoredVector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.modes.fbd.actions.AddLoad;
 import edu.gatech.statics.modes.fbd.actions.RemoveLoad;
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
     private List<Load> loads;
     private FreeBodyDiagram diagram;
     private DragSnapManipulator dragManipulator;
     private Point snapPoint;
     private MousePressInputAction pressAction;
     private boolean hasBeenAdded;
     private LabelSelector labelTool;
 
     /**
      * This is true if the load has been added to the diagram and made part of its state.
      * @return
      */
     protected boolean hasBeenAdded() {
         return hasBeenAdded;
     }
 
     protected FreeBodyDiagram getDiagram() {
         return diagram;
     }
 
     protected Point getSnapPoint() {
         return snapPoint;
     }
 
     protected DragSnapManipulator getDragManipulator() {
         return dragManipulator;
     }
 
     protected abstract LabelSelector createLabelSelector();
 
     protected void showLabelSelector() {
         labelTool = createLabelSelector();
         //labelTool.setUnits(Unit.force.getSuffix());
         labelTool.setHintText("");
         labelTool.setIsCreating(true);
         labelTool.popup();
     }
 
     public CreateLoadTool(FreeBodyDiagram diagram) {
         this.diagram = diagram;
         loadAnchor = new Point("default", new Vector3bd());
         loads = createLoads(loadAnchor);
 
         for (Load load : loads) {
             //diagram.addUserObject(aLoad);
             //AddLoad addLoadAction= new AddLoad(aLoad);
             //load.createDefaultSchematicRepresentation();
             diagram.addTemporaryLoad(load);
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
     abstract protected List<Load> createLoads(Point anchor);
 
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
         for (Load load : loads) {
             //diagram.removeUserObject(aLoad);
             if (hasBeenAdded) {
                 performRemove();
             } else {
                 diagram.removeTemporaryLoad(load);
             }
         }
 
         if (labelTool != null) {
             labelTool.dismiss();
         }
        diagram.stateChanged();
     }
 
     @Override
     protected void onFinish() {
     }
 
     protected void enableDragManipulator() {
 
         List<Point> pointList = new ArrayList();
         for (SimulationObject obj : diagram.getCentralObjects()) {
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
         for (Load aLoad : loads) {
             aLoad.setAnchor(snapPoint);
         }
 
         dragManipulator.setEnabled(false);
         removeFromAttachedHandlers(dragManipulator);
         dragManipulator = null;
 
         performAdd();
 
         return true;
     }
 
     /**
      * This method actually adds the loads to the diagram, as an undoable action.
      * This method is called by releaseDragManipulator() if the release is 
      * successful.
      */
     protected void performAdd() {
         List<AnchoredVector> loadVectors = new ArrayList<AnchoredVector>();
         for (Load load : loads) {
             loadVectors.add(load.getAnchoredVector());
         }
         AddLoad addLoadAction = new AddLoad(loadVectors);
         diagram.performAction(addLoadAction);
         hasBeenAdded = true;
     }
 
     /**
      * Like performAdd, this method removes the loads carried by the tool.
      * This will only happen, though, if the user is cancelling the add.
      */
     protected void performRemove() {
         List<AnchoredVector> loadVectors = new ArrayList<AnchoredVector>();
         for (Load load : loads) {
             loadVectors.add(load.getAnchoredVector());
         }
         RemoveLoad removeLoadAction = new RemoveLoad(loadVectors);
         diagram.performAction(removeLoadAction);
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
