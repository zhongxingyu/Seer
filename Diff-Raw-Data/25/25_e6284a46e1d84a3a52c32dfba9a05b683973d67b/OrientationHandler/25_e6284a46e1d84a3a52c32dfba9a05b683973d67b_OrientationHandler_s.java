 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.tools;
 
 import com.jme.input.InputHandler;
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.math.AnchoredVector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.fbd.FreeBodyDiagram;
 import edu.gatech.statics.modes.fbd.actions.OrientLoad;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.manipulators.MousePressInputAction;
 import edu.gatech.statics.objects.manipulators.MousePressListener;
 import edu.gatech.statics.objects.manipulators.Orientation2DSnapManipulator;
 import edu.gatech.statics.objects.manipulators.OrientationListener;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class OrientationHandler {
 
     private InputHandler inputHandler;
     private FreeBodyDiagram diagram;
     private Force force;
     private Moment moment;
     private Orientation2DSnapManipulator orientationManipulator;
     private AnchoredVector oldVector;
     private boolean enabled;
 
 
     // the timestamp of when the handler was created.
     private long timestamp;
     private static final long CLICK_TIME = 100; // time in milliseconds; less than this registers a click
     private boolean firstClick; // set to true if the user did not position the force before releasing the mouse, if the user simply released the mouse afterward
     private boolean secondClick; // set to true when the mouse has been clicked a second time.
 
     /*
      * An orientation handler for allowing orientation of moments. Added by Jayraj 11/2/2010
      */
 
     public OrientationHandler(FreeBodyDiagram diagram, InputHandler inputHandler, Moment moment) {
         this.inputHandler = inputHandler;
         this.diagram = diagram;
         this.moment = moment;
 
         // make a copy just in case.
         oldVector = new AnchoredVector(moment.getAnchoredVector());
 
         final List<Vector3f> snapDirections = diagram.getSensibleDirections(moment.getAnchor());
         orientationManipulator = new Orientation2DSnapManipulator(moment.getAnchor(), Vector3f.UNIT_Z, snapDirections);
         orientationManipulator.addListener(new MyOrientationListener());
         inputHandler.addToAttachedHandlers(orientationManipulator);
         //System.out.println("*** Created orientation handler for moment");
 
         StaticsApplication.getApp().enableDrag(false);
         enabled = true;
 
         timestamp = System.currentTimeMillis();
         MousePressInputAction clickAction = new MousePressInputAction();
         clickAction.addListener(new ClickListener());
         inputHandler.addAction(clickAction);
     }
 
     /**
      * Constructs an OrientationHandler. This also activates and enables the OrientationHandler.
      * 
      * @param diagram
      * @param inputHandler
      * @param force
      */
     public OrientationHandler(FreeBodyDiagram diagram, InputHandler inputHandler, Force force) {
         this.inputHandler = inputHandler;
         this.diagram = diagram;
         this.force = force;
 
         // make a copy just in case.
         oldVector = new AnchoredVector(force.getAnchoredVector());
 
        final List<Vector3f> snapDirections = new ArrayList<Vector3f>();
        
        
         orientationManipulator = new Orientation2DSnapManipulator(force.getAnchor(), Vector3f.UNIT_Z, snapDirections);
        
        
             
         orientationManipulator.addListener(new MyOrientationListener());
         inputHandler.addToAttachedHandlers(orientationManipulator);
 
         StaticsApplication.getApp().enableDrag(false);
         enabled = true;
 
         timestamp = System.currentTimeMillis();
         MousePressInputAction clickAction = new MousePressInputAction();
         clickAction.addListener(new ClickListener());
         inputHandler.addAction(clickAction);
     }
 
     /**
      * This performs the simple check where 
      */
     private class ClickListener implements MousePressListener {
 
         private boolean releasedOnce;
         private long downTimestamp;
 
         public void onMouseDown() {
             if (releasedOnce) {
                 downTimestamp = System.currentTimeMillis();
             }
         }
 
         public void onMouseUp() {
             long now;
             if (releasedOnce) {
                 now = System.currentTimeMillis();
                 if (now - downTimestamp < CLICK_TIME) {
                     secondClick = true;
                 }
             }
             now = System.currentTimeMillis();
             if(now - timestamp < CLICK_TIME) {
                 firstClick = true;
             }
 
             releasedOnce = true;
         }
     }
 
     /**
      * Returns true if it is allowable to release the orientation listener.
      * This will return true if the user has simply released the mouse, but not clicked it.
      * This means, releasing after a sufficient amount of time has passed. Otherwise, it will
      * release if the user has clicked the mouse a second time.
      * @return
      */
     private boolean canRelease() {
         return (!firstClick && orientationManipulator.mouseReleased()) || secondClick;
     }
 
     private class MyOrientationListener implements OrientationListener {
 
         public void onRotate(Matrix3f rotation) {
             if(force != null)
                 force.setRotation(rotation);
             if(moment != null)
                 moment.setRotation(rotation);
         }
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     /**
      * This method should be called when the orientation handler should stop and
      * not complete its action. This might be called when the user presses escape, or something.
      */
     public void stop() {
         if (!enabled) {
             return;
         }
         orientationManipulator.setEnabled(false);
         inputHandler.removeFromAttachedHandlers(orientationManipulator);
         orientationManipulator = null;
         enabled = false;
         StaticsApplication.getApp().enableDrag(true);
     }
 
     /**
      * This releases the orientation handler, and executes the actual orientation action.
      * This is what is called when the orientation handler completes successfully.
      * This will only go through if the underlying orientation manipulator has released
      * the mouse, so it can be called repeatedly until it succeeds.
      * @return
      */
     public boolean release() {
         //System.out.println("attempting to release manipulator");
         if (!enabled) {
             return false;
         }
         // if the user has let up the mouse on the orientation manipulator, 
         if (!canRelease()) {
             return false;
         }
 
         if (orientationManipulator.getCurrentSnap() != null) {
             //System.out.println("releasing manipulator");
 
             Vector3f currentSnap = orientationManipulator.getCurrentSnap();
             Vector3bd vbd = new Vector3bd(
                     BigDecimal.valueOf(currentSnap.x),
                     BigDecimal.valueOf(currentSnap.y),
                     BigDecimal.valueOf(currentSnap.z));
 
             // actually run the orientation action
             AnchoredVector newVector = new AnchoredVector(oldVector);
             newVector.getVector().setVectorValue(vbd);
             OrientLoad orientLoad = new OrientLoad(oldVector, newVector);
             diagram.performAction(orientLoad);
 
             if(force != null)
                 force.setVectorValue(vbd);
             if(moment != null)
                 moment.setVectorValue(vbd);
 
             orientationManipulator.setEnabled(false);
             inputHandler.removeFromAttachedHandlers(orientationManipulator);
             orientationManipulator = null;
 
             enabled = false;
 
             StaticsApplication.getApp().enableDrag(true);
             return true;
         }
         return false;
     }
 }
