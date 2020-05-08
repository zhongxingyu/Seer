 /*
  * CreateForceTool.java
  *
  * Created on July 16, 2007, 3:45 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd.tools;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.objects.manipulators.*;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.VectorListener;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class CreateForceTool2D extends CreateLoadTool /*implements ClickListener*/ {
 
     protected Force force;
     protected Diagram diagram;
     protected Orientation2DSnapManipulator orientationManipulator;
 
     /** Creates a new instance of CreateForceTool */
     public CreateForceTool2D(Diagram diagram) {
         super(diagram);
         this.diagram = diagram;
     }
 
     protected List<Load> createLoad(Point anchor) {
         force = new Force(anchor, new Vector3f(1.5f, 1f, 0).normalize(), "F");
         force.createDefaultSchematicRepresentation();
         return Collections.singletonList((Load) force);
     }
 
     @Override
     protected void onActivate() {
         super.onActivate();
 
         StaticsApplication.getApp().setAdvice(
                 java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_tools_createForce1"));
     }
 
     @Override
     protected void onFinish() {
         super.onFinish();
         
         VectorListener forceListener = new VectorOverlapDetector(diagram, force);
         force.addListener(forceListener);
     }
 
     @Override
     protected void showLabelSelector() {
         LabelSelector labelTool = new LabelSelector(new LoadLabelListener(force), force.getAnchor().getTranslation());
         labelTool.setAdvice("Please give a name or a value for your force");
         labelTool.setUnits(Unit.force.getSuffix());
         labelTool.setHintText("");
         labelTool.setIsCreating(true);
         labelTool.createPopup();
     }
 
     protected void enableOrientationManipulator() {
 
         final List<Vector3f> snapDirections = diagram.getSensibleDirections(getSnapPoint());
         orientationManipulator = new Orientation2DSnapManipulator(force.getAnchor(), Vector3f.UNIT_Z, snapDirections);
         orientationManipulator.addListener(new MyOrientationListener());
         addToAttachedHandlers(orientationManipulator);
 
         StaticsApplication.getApp().setAdvice(
                 java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_tools_createForce2"));
     }
 
     private class MyOrientationListener implements OrientationListener {
 
         public void onRotate(Matrix3f rotation) {
             force.setRotation(rotation);
         }
     }
 
     // NOTE: 
     // TODO: this is not a good means for handling releasing the orientation 
     // manipulator. We need something that is slightly more responsive.
     @Override
     public void update(float time) {
         super.update(time);
 
         if (orientationManipulator != null) {
             if (orientationManipulator.mouseReleased()) {
                 releaseOrientationManipulator();
             }
         }
     }
 
     @Override
     public void onMouseDown() {
        //super.onMouseDown();

         if (getDragManipulator() != null) {
             if (releaseDragManipulator()) {
                 enableOrientationManipulator();
             }
         }
     }
 
     public void releaseOrientationManipulator() {
         if (orientationManipulator.getCurrentSnap() != null) {
 
             force.setVectorValue(orientationManipulator.getCurrentSnap());
 
             orientationManipulator.setEnabled(false);
             removeFromAttachedHandlers(orientationManipulator);
             orientationManipulator = null;
 
             finish();
             showLabelSelector();
         }
     }
 }
