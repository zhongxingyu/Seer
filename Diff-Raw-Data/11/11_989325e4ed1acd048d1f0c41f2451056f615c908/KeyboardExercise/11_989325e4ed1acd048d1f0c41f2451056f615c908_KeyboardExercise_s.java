 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package keyboard;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.modes.frame.FrameExercise;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Bar;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Pin2d;
 import edu.gatech.statics.objects.connectors.Roller2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.Solve2FMTask;
 import edu.gatech.statics.ui.AbstractInterfaceConfiguration;
 import edu.gatech.statics.ui.windows.navigation.ViewConstraints;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class KeyboardExercise extends FrameExercise {
 
     @Override
     public AbstractInterfaceConfiguration createInterfaceConfiguration() {
 
         AbstractInterfaceConfiguration ic = super.createInterfaceConfiguration();
         ic.setCameraSpeed(.2f, 0.02f, .05f);
 
 
         //AbstractInterfaceConfiguration ic = (AbstractInterfaceConfiguration) super.createInterfaceConfiguration();
         // ideally, we shouldn't need to create a subclass here.
         // we should just be able to modify the camera control directly, but so it goes.
         /*AbstractInterfaceConfiguration ic = new DefaultInterfaceConfiguration() {
 
         @Override
         public void setupCameraControl(CameraControl cameraControl) {
         super.setupCameraControl(cameraControl);
         cameraControl.setMovementSpeed(.2f, .02f, .05f);
         }
         };*/
         ViewConstraints vc = new ViewConstraints();
         vc.setPositionConstraints(-2, 2, -1, 4);
         vc.setZoomConstraints(0.5f, 1.5f);
         ic.setViewConstraints(vc);
         // this throws a null pointer
         //ic.getNavigationWindow().getCameraControl().setMovementSpeed(.2f, .02f, .05f);
         return ic;
     }
 
     @Override
     public Description getDescription() {
         Description description = new Description();
 
         description.setTitle("Keyboard Stand");
         description.setNarrative(
                 "Kasiem Hill is passionate about music, and figures his Civil Engineering " +
                 "degree from GT will be a good backup in case his songs don’t make it. " +
                 "He got a cool keyboard for his birthday, but he doesn’t have the money " +
                 "to buy a keyboard stand. He decides to build one with scrap wood, and " +
                 "would like to know how much force he can expect at each connection if he " +
                 "assumes the keyboard stand can be modeled as a frame. ");
 
         description.setProblemStatement(
                 "The keyboard stand supported by two beams and a cross bar, PQ. " +
                 "The supports at B and E are rollers, and the floor is frictionless.");
 
         description.setGoals(
                 "Find the force in PQ, and define whether it is in tension or compression.");
 
         return description;
     }
 
     @Override
     public void initExercise() {
 //        setName("Keyboard Stand");
 //
 //        setDescription(
 //                "This is a keyboard stand supported by two beams and a cross bar, PQ. " +
 //                "Find the force in PQ and define whether it is in tension or compression. " +
 //                "The supports at B and E are rollers, and the floor is frictionless.");
 
         Unit.setSuffix(Unit.distance, " m");
         Unit.setSuffix(Unit.moment, " N*m");
         Unit.setDisplayScale(Unit.distance, new BigDecimal("10"));
         getDisplayConstants().setMomentSize(0.5f);
         getDisplayConstants().setForceSize(0.5f);
         getDisplayConstants().setPointSize(0.5f);
         getDisplayConstants().setCylinderRadius(0.5f);
         //getDisplayConstants().setForceLabelDistance(1f);
         //getDisplayConstants().setMomentLabelDistance(0f);
         //getDisplayConstants().setMeasurementBarSize(0.1f);
     }
     Point A, B, C, D, E, P, Q;
     Pin2d jointC;
     Connector2ForceMember2d jointP, jointQ;
     Roller2d jointB, jointE;
     Body leftLeg, rightLeg;
     Bar bar;
 
     @Override
     public void loadExercise() {
         Schematic schematic = getSchematic();
 
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.7f, .7f, .9f, 1.0f));
         StaticsApplication.getApp().getCamera().setLocation(new Vector3f(0.0f, 0.0f, 65.0f));
 
         A = new Point("A", "0", "6", "0");
         D = new Point("D", "8", "6", "0");
         B = new Point("B", "8", "0", "0");
         E = new Point("E", "0", "0", "0");
         C = new Point("C", "4", "3", "0");
         P = new Point("P", "2.7", "4", "0");
         Q = new Point("Q", "5.3", "4", "0");
 
         leftLeg = new Beam("Left Leg", B, A);
         bar = new Bar("Bar", P, Q);
         rightLeg = new Beam("Right Leg", E, D);
 
         jointC = new Pin2d(C);
         jointP = new Connector2ForceMember2d(P, bar);  //Pin2d(P);
 
         jointQ = new Connector2ForceMember2d(Q, bar); //new Pin2d(Q);
 
         jointB = new Roller2d(B);
         jointE = new Roller2d(E);
 
         jointB.setDirection(Vector3bd.UNIT_Y);
         jointE.setDirection(Vector3bd.UNIT_Y);
 
         DistanceMeasurement distance1 = new DistanceMeasurement(D, A);
         distance1.setName("Measure AD");
         distance1.createDefaultSchematicRepresentation(0.5f);
         distance1.addPoint(E);
         distance1.addPoint(B);
         schematic.add(distance1);
 
         DistanceMeasurement distance2 = new DistanceMeasurement(C, D);
         distance2.setName("Measure CD");
         distance2.createDefaultSchematicRepresentation(0.5f);
         distance2.forceVertical();
         distance2.addPoint(A);
         schematic.add(distance2);
 
         DistanceMeasurement distance3 = new DistanceMeasurement(C, Q);
         distance3.setName("Measure CQ");
         distance3.createDefaultSchematicRepresentation(1f);
         distance3.forceVertical();
         distance3.addPoint(P);
         schematic.add(distance3);
 
         DistanceMeasurement distance4 = new DistanceMeasurement(B, D);
         distance4.setName("Measure BD");
         distance4.createDefaultSchematicRepresentation(2.4f);
         distance4.addPoint(A);
         distance4.addPoint(E);
         schematic.add(distance4);
 
         Force keyboardLeft = new Force(A, Vector3bd.UNIT_Y.negate(), new BigDecimal(50));
         keyboardLeft.setName("Keyboard Left");
         leftLeg.addObject(keyboardLeft);
 
         Force keyboardRight = new Force(D, Vector3bd.UNIT_Y.negate(), new BigDecimal(50));
         keyboardRight.setName("Keyboard Right");
         rightLeg.addObject(keyboardRight);
 
         jointC.attach(leftLeg, rightLeg);
         jointC.setName("Joint C");
         jointP.attach(leftLeg, bar);
         jointP.setName("Joint P");
         jointQ.attach(bar, rightLeg);
         jointQ.setName("Joint Q");
         jointE.attachToWorld(rightLeg);
         jointE.setName("Joint E");
         jointB.attachToWorld(leftLeg);
         jointB.setName("Joint B");
 
         A.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         C.createDefaultSchematicRepresentation();
         D.createDefaultSchematicRepresentation();
         E.createDefaultSchematicRepresentation();
         P.createDefaultSchematicRepresentation();
         Q.createDefaultSchematicRepresentation();
 
         keyboardLeft.createDefaultSchematicRepresentation();
         keyboardRight.createDefaultSchematicRepresentation();
         //leftLeg.createDefaultSchematicRepresentation();
         //bar.createDefaultSchematicRepresentation();
         //rightLeg.createDefaultSchematicRepresentation();
 
         schematic.add(leftLeg);
         schematic.add(bar);
         schematic.add(rightLeg);
 
         ModelNode modelNode = ModelNode.load("keyboard/assets/", "keyboard/assets/keyboard.dae");
 
         float scale = .28f;
 
         ModelRepresentation rep = modelNode.extractElement(leftLeg, "VisualSceneNode/stand/leg1");
         rep.setLocalScale(scale);
         rep.setModelOffset(new Vector3f(14f, 0, 0));
         leftLeg.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(rightLeg, "VisualSceneNode/stand/leg2");
         rep.setLocalScale(scale);
         rep.setModelOffset(new Vector3f(14f, 0, 0));
         rightLeg.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.extractElement(bar, "VisualSceneNode/stand/middle_support");
         rep.setLocalScale(scale);
         rep.setModelOffset(new Vector3f(14f, 0, 0));
         bar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         rep = modelNode.getRemainder(schematic.getBackground());
 
         schematic.getBackground().addRepresentation(rep);
         rep.setLocalScale(scale);
         rep.setModelOffset(new Vector3f(14f, 0, 0));
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
 
         addTask(new Solve2FMTask("Solve PQ", bar, jointP));
     }
 }
