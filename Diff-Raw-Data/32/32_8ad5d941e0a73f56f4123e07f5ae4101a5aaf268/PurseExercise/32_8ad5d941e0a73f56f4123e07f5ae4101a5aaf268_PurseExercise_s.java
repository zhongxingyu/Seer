 /*
  * PurseExercise.java
  *
  * Created on August 4, 2007, 11:28 AM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package example01;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.RepresentationLayer;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.FBDExercise;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.UnitUtils;
 import edu.gatech.statics.objects.AngleMeasurement;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.bodies.Cable;
 import edu.gatech.statics.objects.joints.Connector2ForceMember2d;
 import edu.gatech.statics.objects.joints.Pin2d;
 import edu.gatech.statics.objects.representations.ImageRepresentation;
 import edu.gatech.statics.ui.DefaultInterfaceConfiguration;
 import edu.gatech.statics.ui.InterfaceConfiguration;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class PurseExercise extends FBDExercise {
 
     public InterfaceConfiguration createInterfaceConfiguration() {
         return new DefaultInterfaceConfiguration();
     }
 
     /** Creates a new instance of PurseExercise */
     public PurseExercise() {
         super(new Schematic());
     }
 
     @Override
     public void initExercise() {
         setName("Holding a Purse");
         StaticsApplication.getApp().createDisplayGroup("Bones", "bones");
 
         setDescription(
                 "Here is a simplified version of the human arm. " +
                 "Please build a Free Body Diagram of the Forearm, and solve for the tension in the tendon. " +
                 "The weight of the forearm is 9 N and its center of mass is at G. " +
                 "The weight of the purse is 19.6 N.");
         Unit.setUtils(new UnitUtils() {
 
             @Override
             public String getSuffix(Unit unit) {
                 switch (unit) {
                     case angle:
                         return "";
                     case distance:
                         return " cm";
                     case force:
                         return " N";
                     case moment:
                         return " N*cm";
                     case none:
                         return "";
                     default:
                         throw new IllegalArgumentException("Unrecognized unit: " + unit);
                 }
             }
         });
     }
     protected float handPoint = -17;
     protected float tendonAnchorB = 13;
     protected float tendonAnchorD = 13;
     protected float shoulderHeight = 16;
     protected float forearmWeight = 9;
     protected float purseWeight = 19.6f;
     protected float centerGravityOffset = 0;
     Point A, B, C, D, E, G;
     Connector2ForceMember2d jointB, jointD;
     Pin2d jointC, jointE;
 
     @Override
     public float getDrawScale() {
         return 2.0f;
     }
     
     @Override
     public void loadExercise() {
 
         Schematic world = getSchematic();
 
 
         //DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.2f, .2f, .2f, 1.0f));
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(0.96f, 0.98f, 0.90f, 1.0f));
 
         StaticsApplication.getApp().getCamera().setLocation(new Vector3f(0.0f, 0.0f, 65.0f));
         //StaticsApplication.getApp().setWorldScale(2f);
 
         /*A = new Point(new Vector3f(handPoint, -16 + 6, 0));
         B = new Point(new Vector3f(tendonAnchorB, -16 + 6, 0));
         C = new Point(new Vector3f(18, -16 + 6, 0));
         D = new Point(new Vector3f(18, tendonAnchorD + 6, 0));
         E = new Point(new Vector3f(18f, shoulderHeight + 6, 0));*/
         A = new Point(""+handPoint, "-10", "0");
         B = new Point(""+tendonAnchorB, "-10", "0");
         C = new Point("18", "-10", "0");
         D = new Point("18", ""+(tendonAnchorD + 6), "0");
         E = new Point("18", ""+(shoulderHeight + 6), "0");
 
         Body upperArm = new Beam(E, C);
         Body forearm = new Beam(C, A);
         Cable tendon = new Cable(D, B);
 
         upperArm.setName("Upper Arm");
         forearm.setName("Forearm");
         tendon.setName("Bicep Muscle");
 
         jointB = new Connector2ForceMember2d(B, tendon);
         jointD = new Connector2ForceMember2d(D, tendon);
         jointC = new Pin2d(C);
         jointE = new Pin2d(E);
 
         G = new Point(""+(centerGravityOffset+3), "-10", "0");
 
         DistanceMeasurement distance1 = new DistanceMeasurement(A, C);
         distance1.createDefaultSchematicRepresentation(6f);
         world.add(distance1);
 
         DistanceMeasurement distance2 = new DistanceMeasurement(G, B);
         distance2.createDefaultSchematicRepresentation(3f);
         world.add(distance2);
 
         DistanceMeasurement distance3 = new DistanceMeasurement(B, C);
         distance3.createDefaultSchematicRepresentation(3f);
         world.add(distance3);
 
         DistanceMeasurement distance4 = new DistanceMeasurement(C, D);
         distance4.createDefaultSchematicRepresentation(5f);
         world.add(distance4);
 
         DistanceMeasurement distance5 = new DistanceMeasurement(D, E);
         distance5.createDefaultSchematicRepresentation(5f);
         world.add(distance5);
 
         AngleMeasurement angle1 = new AngleMeasurement(B, D, C);
         angle1.createDefaultSchematicRepresentation(2f);
         world.add(angle1);
         
         //AngleMeasurement angle2 = new AngleMeasurement(A, B, E);
         //angle2.createDefaultSchematicRepresentation(2f);
         //world.add(angle2);
         
         AngleMeasurement angle3 = new AngleMeasurement(D, B, C);
         angle3.createDefaultSchematicRepresentation(2f);
         world.add(angle3);
         
         Force purse = new Force(A, new Vector3f(0, -1, 0), new BigDecimal(purseWeight));
         purse.setName("Purse");
         forearm.addObject(purse);
 
         Moment shoulder = new Moment(E, new Vector3f(0, 0, -1), "M shoulder"); // use symbol here
         shoulder.setSymbol("Shoulder");
         upperArm.addObject(shoulder);
 
         jointB.attach(forearm, tendon);
         jointC.attach(forearm, upperArm);
         jointD.attach(upperArm, tendon);
         jointE.attachToWorld(upperArm);
 
         E.setName("E");
         D.setName("D");
         C.setName("C");
         B.setName("B");
         A.setName("A");
         G.setName("G");
 
         E.createDefaultSchematicRepresentation();
         D.createDefaultSchematicRepresentation();
         C.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         A.createDefaultSchematicRepresentation();
         G.createDefaultSchematicRepresentation();
         upperArm.createDefaultSchematicRepresentation();
         forearm.createDefaultSchematicRepresentation();
         tendon.createDefaultSchematicRepresentation();
         purse.createDefaultSchematicRepresentation();
         shoulder.createDefaultSchematicRepresentation();
         //weight.createDefaultSchematicRepresentation();
 
         forearm.getWeight().setValue(new BigDecimal(forearmWeight)); // ???
         forearm.setCenterOfMassPoint(G);
 
         world.add(upperArm);
         world.add(forearm);
         world.add(tendon);
 
         RepresentationLayer bones = new RepresentationLayer("bones", RepresentationLayer.modelBodies.getPriority() - 1);
         RepresentationLayer.addLayer(bones);
 
         ImageRepresentation imageRep;
         float repScale = .025f;
 
         imageRep = new ImageRepresentation(forearm, loadTexture("example01/assets/lowerArm.png"));
         imageRep.setScale(repScale * 1738, repScale * 501f);
         imageRep.setTranslation(-1, 1.75f, 0);
         forearm.addRepresentation(imageRep);
 
         imageRep = new ImageRepresentation(forearm, loadTexture("example01/assets/lowerArm_bone.png"));
         imageRep.setLayer(bones);
         imageRep.setScale(repScale * 1617, repScale * 310f);
         imageRep.setTranslation(-2.25f, -0.5f, .02f);
         forearm.addRepresentation(imageRep);
 
 
         imageRep = new ImageRepresentation(upperArm, loadTexture("example01/assets/upperArm.png"));
         imageRep.setScale(repScale * 483f, repScale * 1539f);
         imageRep.setTranslation(-0.75f, 1.0f, 0);
         upperArm.addRepresentation(imageRep);
 
         imageRep = new ImageRepresentation(upperArm, loadTexture("example01/assets/upperArm_bone.png"));
         imageRep.setLayer(bones);
         imageRep.setScale(repScale * 191f, repScale * 1453f);
         imageRep.setTranslation(-0.5f, 1, 0);
         upperArm.addRepresentation(imageRep);
 
 
         imageRep = new ImageRepresentation(A, loadTexture("example01/assets/purse.png"));
         imageRep.setScale(repScale * 538f, repScale * 835f);
         imageRep.setTranslation(0, -11.0f, .02f);
         A.addRepresentation(imageRep);
 
         imageRep = new ImageRepresentation(tendon, loadTexture("example01/assets/muscle.png"));
         imageRep.setLayer(bones);
         imageRep.setRotation(-.075f);
         //imageRep.setScale(repScale * 121f,repScale * 347f);
         imageRep.setScale(.9f * repScale * 213f, .9f * repScale * 1331f);
         imageRep.setTranslation(0.0f, 0.5f, .05f);
         tendon.addRepresentation(imageRep);
     }
 
 }
