 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bicycle;
 
 import com.jme.math.Matrix3f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.OrdinaryExercise;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.FixedAngleMeasurement;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.PointAngleMeasurement;
 import edu.gatech.statics.objects.bodies.Bar;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Roller2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class BicycleExercise extends OrdinaryExercise {
 
     public BicycleExercise() {
         super(new Schematic());
     }
 
     @Override
     public void initExercise() {
         setName("Bicycle");
 
         setDescription(
                 "Someone pushing against a wedge on a bike!");
 
         Unit.setSuffix(Unit.distance, " mm");
         Unit.setSuffix(Unit.moment, " N*m");
         Unit.setDisplayScale(Unit.distance, new BigDecimal(".1"));
        getDisplayConstants().setMomentSize(0.2f);
         getDisplayConstants().setForceSize(0.2f);
         getDisplayConstants().setPointSize(0.2f);
         getDisplayConstants().setCylinderRadius(0.2f);
         getDisplayConstants().setForceLabelDistance(1f);
         getDisplayConstants().setMomentLabelDistance(0f);
        getDisplayConstants().setMeasurementSize(0.1f);
     }
     Point A, I, H, J, G, F, B, K;
     Roller2d rollerA, rollerB;
     Connector2ForceMember2d twoForceF, twoForceH, twoForceJH, twoForceJB, twoForceGF, twoForceGB;
 
     @Override
     public void loadExercise() {
         Schematic schematic = getSchematic();
 
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.2f, .2f, .9f, 1.0f));
         StaticsApplication.getApp().getCamera().setLocation(new Vector3f(0.0f, 0.0f, 65.0f));
 
         A = new Point("0", "0", "0");
         I = new Point("4", "7", "0");
         H = new Point("3", "5.26", "0");
         J = new Point("9", "5.26", "0");
         G = new Point("6", "0", "0");
         F = new Point("2.2", "3.8", "0");
         B = new Point("12", "0", "0");
         K = new Point("10", "7", "0");
 
         Body handlebar = new Beam(I, A);
         handlebar.setName("Handle Bar");
         Bar top = new Bar(J, H);
         top.setName("Top Bar");
         Body seatPole = new Beam(K, G);
         seatPole.setName("Seat Pole");
         Bar front = new Bar(F, G);
         front.setName("Front Bar");
         Bar back = new Bar(J, B);
         back.setName("Back Bar");
         Bar bottom = new Bar(B, G);
         bottom.setName("Bottom Bar");
 
         rollerA = new Roller2d(A);
         rollerB = new Roller2d(B);
 
         twoForceF = new Connector2ForceMember2d(F, front);
         twoForceH = new Connector2ForceMember2d(H, top);
         twoForceJH = new Connector2ForceMember2d(J, top);
         twoForceJB = new Connector2ForceMember2d(J, back);
         twoForceGF = new Connector2ForceMember2d(G, front);
         twoForceGB = new Connector2ForceMember2d(G, bottom);
 
         rollerA.setDirection(Vector3bd.UNIT_Y);
         rollerB.setDirection(Vector3bd.UNIT_Y);
 
         DistanceMeasurement distance1 = new DistanceMeasurement(I, A);
         distance1.createDefaultSchematicRepresentation(0.5f);
         distance1.forceVertical();
         schematic.add(distance1);
 
         DistanceMeasurement distance2 = new DistanceMeasurement(I, A);
         distance2.createDefaultSchematicRepresentation(0.5f);
         distance2.forceHorizontal();
         schematic.add(distance2);
 
         DistanceMeasurement distance3 = new DistanceMeasurement(B, K);
         distance3.createDefaultSchematicRepresentation(4.1f);
         distance3.forceHorizontal();
         schematic.add(distance3);
 
         DistanceMeasurement distance4 = new DistanceMeasurement(A, G);
         distance4.createDefaultSchematicRepresentation(0.5f);
         schematic.add(distance4);
 
         DistanceMeasurement distance5 = new DistanceMeasurement(G, B);
         distance5.createDefaultSchematicRepresentation(0.5f);
         schematic.add(distance5);
 
         PointAngleMeasurement angle1 = new PointAngleMeasurement(A, G, I);
         angle1.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle1);
 
         PointAngleMeasurement angle2 = new PointAngleMeasurement(G, A, F);
         angle2.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle2);
 
         PointAngleMeasurement angle3 = new PointAngleMeasurement(G, K, B);
         angle3.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle3);
 
         PointAngleMeasurement angle4 = new PointAngleMeasurement(B, J, G);
         angle4.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle4);
 
 
 
         Force seatWeight = new Force(K, Vector3bd.UNIT_Y.negate(), new BigDecimal(500));
         seatWeight.setName("Seat");
         seatPole.addObject(seatWeight);
 
         Force pedalWeight = new Force(G, Vector3bd.UNIT_Y.negate(), new BigDecimal(150));
         pedalWeight.setName("Pedals");
         seatPole.addObject(pedalWeight);
 
         Vector3bd handleForceDirection = new Vector3bd(
                 new BigDecimal(-Math.cos(Math.PI / 6)),
                 new BigDecimal(-Math.sin(Math.PI / 6)), BigDecimal.ZERO);
         handleForceDirection = handleForceDirection.normalize();
 
         FixedAngleMeasurement angle5 = new FixedAngleMeasurement(I, Vector3bd.UNIT_X.negate(), handleForceDirection.toVector3f());
         angle5.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle5);
 
         Force handleWeight = new Force(I, handleForceDirection, new BigDecimal(20));
         handleWeight.setName("Handlebar");
         handlebar.addObject(handleWeight);
 
         Moment handleMoment = new Moment(I, Vector3bd.UNIT_Z, new BigDecimal(5)); // use symbol here
         handleMoment.setName("Handlebar");
         handlebar.addObject(handleMoment);
 
         rollerA.attachToWorld(handlebar);
         rollerB.attach(back, bottom);
 
         twoForceF.attach(front, handlebar);
         twoForceH.attach(top, handlebar);
         twoForceJH.attach(top, seatPole);
         twoForceJB.attach(back, seatPole);
         twoForceGF.attach(front, seatPole);
         twoForceGB.attach(bottom, seatPole);
         A.setName("A");
         I.setName("I");
         H.setName("H");
         J.setName("J");
         G.setName("G");
         F.setName("F");
         B.setName("B");
         K.setName("K");
 
         A.createDefaultSchematicRepresentation();
         I.createDefaultSchematicRepresentation();
         H.createDefaultSchematicRepresentation();
         J.createDefaultSchematicRepresentation();
         G.createDefaultSchematicRepresentation();
         F.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         K.createDefaultSchematicRepresentation();
         //handlebar.createDefaultSchematicRepresentation();
         handleMoment.createDefaultSchematicRepresentation();
         //top.createDefaultSchematicRepresentation();
         //seatPole.createDefaultSchematicRepresentation();
         //front.createDefaultSchematicRepresentation();
         //back.createDefaultSchematicRepresentation();
         //bottom.createDefaultSchematicRepresentation();
         seatWeight.createDefaultSchematicRepresentation();
         pedalWeight.createDefaultSchematicRepresentation();
         handleWeight.createDefaultSchematicRepresentation();
         schematic.add(handlebar);
         schematic.add(top);
         schematic.add(seatPole);
         schematic.add(front);
         schematic.add(back);
         schematic.add(bottom);
 
         float scale = 1.0f;
         Vector3f modelTranslation = new Vector3f(4.25f, -3.5f, 0);
 
         ModelNode modelNode = ModelNode.load("bicycle/assets/", "bicycle/assets/bicycle.dae");
         modelNode.extractLights();
 
         ModelRepresentation rep;
 
         Matrix3f matrix = new Matrix3f();
         matrix.fromStartEndVectors(Vector3f.UNIT_Z, Vector3f.UNIT_X.negate());
 
         rep = modelNode.extractElement(back, "VisualSceneNode/model/bike/beam3");
         back.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(bottom, "VisualSceneNode/model/bike/beam4");
         bottom.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(handlebar, "VisualSceneNode/model/bike/beam6");
         handlebar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
         
         rep = modelNode.extractElement(front, "VisualSceneNode/model/bike/beam1");
         front.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(seatPole, "VisualSceneNode/model/bike/beam5");
         seatPole.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(seatPole, "VisualSceneNode/model/bike/seat");
         seatPole.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(top, "VisualSceneNode/model/bike/beam2");
         top.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.getRemainder(schematic.getBackground());
         schematic.getBackground().addRepresentation(rep);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
     }
 }
