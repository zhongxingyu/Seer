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
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.description.Description;
 import edu.gatech.statics.modes.frame.FrameExercise;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.FixedAngleMeasurement;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.PointAngleMeasurement;
 import edu.gatech.statics.objects.bodies.Bar;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.objects.bodies.PointBody;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Roller2d;
 import edu.gatech.statics.objects.representations.ModelNode;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import edu.gatech.statics.tasks.Solve2FMTask;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class BicycleExercise extends FrameExercise {
 
     public BicycleExercise() {
     }
 
     @Override
     public Description getDescription() {
         Description description = new Description();
 
         description.setTitle("Bicycle");
 
         description.setNarrative(
                 "Anna Smith is a ME student at GT who has a passion for bikes. " +
                 "She's learned in her MSE 2001 materials lab class that titanium has " +
                 "the highest strength-to-weight ratio of any metal, and she's " +
                 "thinking of using titanium to repair a broken bar on her bike. " +
                 "First, however, she needs to determine the necessary strength the " +
                 "bar must have, and for that she needs to know what is the typical " +
                 "force carried by each structural element in the bike during normal " +
                 "work conditions. This is where her Statics knowledge will be " +
                 "helpful. By assuming constant speed, and estimating the payload due " +
                 "to her weight on the seat and her grip at the front, she can treat " +
                 "the bike’s structure as a frame, each bar as a two-force member, " +
                 "and solve for the forces at each connection. ");
 
         description.setProblemStatement(
                 "This is a model of a bicycle. The body FBGA is a beam, but the " +
                 "rest of the members are bars. You can select the joints D, C, and E by clicking on them.");
 
         description.setGoals("Solve for the tension or compression in each of the two-force members.");
 
        description.addImage("bicycle/assets/bicycle-main.png");
        description.addImage("bicycle/assets/IMG_3406.JPG");
        description.addImage("bicycle/assets/IMG_3410.JPG");
        description.addImage("bicycle/assets/IMG_3416.JPG");

         return description;
     }
 
 
     @Override
     public void initExercise() {
 //        setName("Bicycle");
 //
 //        setDescription(
 //                "This is a model of a bicycle. The body FBGA is a beam, but the " +
 //                "rest of the members are bars. You can select the joints D, C, and E by clicking on them. " +
 //                "Try solving the frame and the beam first, and then use the method of joints to complete the exercise.");
 
         Unit.setSuffix(Unit.distance, " m");
         Unit.setSuffix(Unit.moment, " N*m");
         Unit.setDisplayScale(Unit.distance, new BigDecimal("10"));
         Unit.setPrecision(Unit.distance, 2);
 
         getDisplayConstants().setPointSize(.5f);
         getDisplayConstants().setMomentSize(.5f);
         getDisplayConstants().setForceSize(.5f);
         //getDisplayConstants().setDrawScale(.5f);
         //getDisplayConstants().setForceLabelDistance(1);
         getDisplayConstants().setMeasurementBarSize(.25f);
     }
     private Point A,  B,  C,  D,  E,  F,  G;
     private Roller2d rollerA;
     private Roller2d rollerD;
     //private Connector2ForceMember2d twoForceMiddleB,  twoForceTopB,  twoForceBackC,  twoForceTopC,  twoForceSeatC,  twoForceBottomE,  twoForceFrontE,  twoForceSeatE,  twoForceFrontG;
     private Bar CB_topBar,  CD_backBar,  DE_bottomBar,  GE_frontBar,  BE_middleBar,  CE_seatPoleBar;
     private Body FA_handlebarBeam,  jointAtD,  jointAtE,  jointAtC;
 
     @Override
     public void loadExercise() {
         Schematic schematic = getSchematic();
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.6f, .7f, .9f, 1.0f));
         StaticsApplication.getApp().getCamera().setLocation(new Vector3f(0.0f, 0.0f, 65.0f));
 
         A = new Point("A", "0", "0", "0");
         B = new Point("B", "3", "5.196", "0");
         C = new Point("C", "9", "5.196", "0");
         D = new Point("D", "12", "0", "0");
         E = new Point("E", "6", "0", "0");
         F = new Point("F", "4", "6.928", "0");
         G = new Point("G", "1.5", "2.598", "0");
 
         FA_handlebarBeam = new Beam("Beam FBGA", F, A);
         CE_seatPoleBar = new Bar("Bar CE", C, E);
         CB_topBar = new Bar("Bar CB", C, B);
         CD_backBar = new Bar("Bar CD", C, D);
         DE_bottomBar = new Bar("Bar DE", D, E);
         GE_frontBar = new Bar("Bar GE", G, E);
         BE_middleBar = new Bar("Bar BE", B, E);
         jointAtD = new PointBody("Joint D", D);
         jointAtC = new PointBody("Joint C", C);
         jointAtE = new PointBody("Joint E", E);
 
         DistanceMeasurement distance1 = new DistanceMeasurement(F, A);
         distance1.setName("Measure FA");
         distance1.createDefaultSchematicRepresentation(1.0f);
         distance1.forceVertical();
         schematic.add(distance1);
 
         DistanceMeasurement distance1a = new DistanceMeasurement(F, B);
         distance1a.setName("Measure FB");
         distance1a.createDefaultSchematicRepresentation(4.5f);
         distance1a.forceVertical();
         schematic.add(distance1a);
 
 
         DistanceMeasurement distance2 = new DistanceMeasurement(A, E);
         distance2.setName("Measure AE");
         distance2.createDefaultSchematicRepresentation(0.5f);
         schematic.add(distance2);
 
         DistanceMeasurement distance3 = new DistanceMeasurement(E, D);
         distance3.setName("Measure ED");
         distance3.createDefaultSchematicRepresentation(0.5f);
         schematic.add(distance3);
 
         DistanceMeasurement distance4 = new DistanceMeasurement(B, A);
         distance4.setName("Measure AB");
         distance4.createDefaultSchematicRepresentation(0.5f);
         distance4.forceVertical();
         schematic.add(distance4);
 
         DistanceMeasurement distance5 = new DistanceMeasurement(F, A);
         distance5.setName("Measure AF");
         distance5.createDefaultSchematicRepresentation(0.5f);
         distance5.forceHorizontal();
         schematic.add(distance5);
 
         DistanceMeasurement distance6 = new DistanceMeasurement(F, G);
         distance6.setName("Measure AG");
         distance6.createDefaultSchematicRepresentation(1.0f);
         distance6.forceHorizontal();
         schematic.add(distance6);
 
         PointAngleMeasurement angle1 = new PointAngleMeasurement(B, G, E);
         angle1.setName("Angle GBE");
         angle1.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle1);
 
         PointAngleMeasurement angle2 = new PointAngleMeasurement(B, E, C);
         angle2.setName("Angle EBC");
         angle2.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle2);
 
         PointAngleMeasurement angle3 = new PointAngleMeasurement(C, B, E);
         angle3.setName("Angle BCE");
         angle3.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle3);
 
         PointAngleMeasurement angle4 = new PointAngleMeasurement(D, C, E);
         angle4.setName("Angle CDE");
         angle4.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle4);
 
         PointAngleMeasurement angle6 = new PointAngleMeasurement(E, C, D);
         angle6.setName("Angle CED");
         angle6.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle6);
 
         PointAngleMeasurement angle7 = new PointAngleMeasurement(E, G, A);
         angle7.setName("Angle GEA");
         angle7.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle7);
 
         PointAngleMeasurement angle8 = new PointAngleMeasurement(A, G, E);
         angle8.setName("Angle GAE");
         angle8.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle8);
 
         Force seatWeight = new Force(C, Vector3bd.UNIT_Y.negate(), new BigDecimal(500));
         seatWeight.setName("Seat Y");
         jointAtC.addObject(seatWeight);
 
         Force pedalWeight = new Force(E, Vector3bd.UNIT_Y.negate(), new BigDecimal(150));
         pedalWeight.setName("Pedals");
         jointAtE.addObject(pedalWeight);
 
         Force wedgeForce = new Force(C, Vector3bd.UNIT_X, new BigDecimal(17.3205));
         wedgeForce.setName("Seat X");
         jointAtC.addObject(wedgeForce);
 
         Vector3bd handleForceDirection = new Vector3bd(
                 new BigDecimal(-Math.cos(Math.PI / 6)),
                 new BigDecimal(-Math.sin(Math.PI / 6)), BigDecimal.ZERO);
         handleForceDirection = handleForceDirection.normalize();
 
         FixedAngleMeasurement angle5 = new FixedAngleMeasurement(F, Vector3bd.UNIT_X.negate(), handleForceDirection.toVector3f());
         angle5.setName("Angle F");
         angle5.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle5);
 
         Force handleWeight = new Force(F, handleForceDirection, new BigDecimal(20));
         handleWeight.setName("Handlebar Force");
         FA_handlebarBeam.addObject(handleWeight);
 
         // **** CONNECTORS ***
         // *** all connectors that attach to the beam part of the frame.
         Connector2ForceMember2d c2fm_GE_G = new Connector2ForceMember2d(G, GE_frontBar);
         Connector2ForceMember2d c2fm_BE_B = new Connector2ForceMember2d(B, BE_middleBar);
         Connector2ForceMember2d c2fm_CB_B = new Connector2ForceMember2d(B, CB_topBar);
         c2fm_GE_G.attach(GE_frontBar, FA_handlebarBeam);
         c2fm_BE_B.attach(BE_middleBar, FA_handlebarBeam);
         c2fm_CB_B.attach(CB_topBar, FA_handlebarBeam);
 
         //rollerA = new Roller2d(A);
         rollerA = new Roller2d(A);
         rollerA.attachToWorld(FA_handlebarBeam);
         rollerA.setDirection(Vector3bd.UNIT_Y);
         rollerA.setName("Roller A");
 
         // *** all connectors that attach to C
         Connector2ForceMember2d c2fm_CB_C = new Connector2ForceMember2d(C, CB_topBar);
         Connector2ForceMember2d c2fm_CE_C = new Connector2ForceMember2d(C, CE_seatPoleBar);
         Connector2ForceMember2d c2fm_CD_C = new Connector2ForceMember2d(C, CD_backBar);
         c2fm_CB_C.attach(CB_topBar, jointAtC);
         c2fm_CE_C.attach(CE_seatPoleBar, jointAtC);
         c2fm_CD_C.attach(CD_backBar, jointAtC);
 
         // *** all connectors that attach to E
         Connector2ForceMember2d c2fm_GE_E = new Connector2ForceMember2d(E, GE_frontBar);
         Connector2ForceMember2d c2fm_BE_E = new Connector2ForceMember2d(E, BE_middleBar);
         Connector2ForceMember2d c2fm_CE_E = new Connector2ForceMember2d(E, CE_seatPoleBar);
         Connector2ForceMember2d c2fm_DE_E = new Connector2ForceMember2d(E, DE_bottomBar);
         c2fm_BE_E.attach(BE_middleBar, jointAtE);
         c2fm_GE_E.attach(GE_frontBar, jointAtE);
         c2fm_CE_E.attach(CE_seatPoleBar, jointAtE);
         c2fm_DE_E.attach(DE_bottomBar, jointAtE);
 
         // *** all connectors that attach to D
         Connector2ForceMember2d c2fm_DE_D = new Connector2ForceMember2d(D, DE_bottomBar);
         Connector2ForceMember2d c2fm_CD_D = new Connector2ForceMember2d(D, CD_backBar);
         c2fm_DE_D.attach(DE_bottomBar, jointAtD);
         c2fm_CD_D.attach(CD_backBar, jointAtD);
 
         rollerD = new Roller2d(D);
         rollerD.setName("Roller D");
         rollerD.setDirection(Vector3bd.UNIT_Y);
         rollerD.attachToWorld(jointAtD);
 
         A.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         C.createDefaultSchematicRepresentation();
         D.createDefaultSchematicRepresentation();
         E.createDefaultSchematicRepresentation();
         F.createDefaultSchematicRepresentation();
         G.createDefaultSchematicRepresentation();
 
         jointAtD.createDefaultSchematicRepresentation();
         jointAtC.createDefaultSchematicRepresentation();
         jointAtE.createDefaultSchematicRepresentation();
 
         seatWeight.createDefaultSchematicRepresentation();
         pedalWeight.createDefaultSchematicRepresentation();
         handleWeight.createDefaultSchematicRepresentation();
         wedgeForce.createDefaultSchematicRepresentation();
 
         schematic.add(FA_handlebarBeam);
         schematic.add(CB_topBar);
         schematic.add(CE_seatPoleBar);
         schematic.add(GE_frontBar);
         schematic.add(CD_backBar);
         schematic.add(BE_middleBar);
         schematic.add(DE_bottomBar);
         schematic.add(jointAtD);
         schematic.add(jointAtC);
         schematic.add(jointAtE);
 
         float scale = 1.0f;
         Vector3f modelTranslation = new Vector3f(4.25f, -3.5f, 0);
 
         ModelNode modelNode = ModelNode.load("bicycle/assets/", "bicycle/assets/bicycle.dae");
         modelNode.extractLights();
 
         ModelRepresentation rep;
 
         Matrix3f matrix = new Matrix3f();
         matrix.fromStartEndVectors(Vector3f.UNIT_Z, Vector3f.UNIT_X.negate());
 
         rep = modelNode.extractElement(CD_backBar, "VisualSceneNode/model/bike/bar_CD");
         CD_backBar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(DE_bottomBar, "VisualSceneNode/model/bike/bar_ED");
         DE_bottomBar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(FA_handlebarBeam, "VisualSceneNode/model/bike/beam_AGBF");
         FA_handlebarBeam.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(GE_frontBar, "VisualSceneNode/model/bike/bar_GE");
         GE_frontBar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(CE_seatPoleBar, "VisualSceneNode/model/bike/bar_CE");
         CE_seatPoleBar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(CB_topBar, "VisualSceneNode/model/bike/bar_BC");
         CB_topBar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         rep.setModelRotation(matrix);
         rep.setLocalScale(scale);
         rep.setModelOffset(modelTranslation);
 
         rep = modelNode.extractElement(BE_middleBar, "VisualSceneNode/model/bike/bar_BE");
         BE_middleBar.addRepresentation(rep);
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
 
         //addTask(new CompleteFBDTask("Solve FBGA", new BodySubset(FA_handlebarBeam)));
         addTask(new Solve2FMTask("Solve BE", BE_middleBar, c2fm_BE_B));
         addTask(new Solve2FMTask("Solve CB", CB_topBar, c2fm_CB_B));
         addTask(new Solve2FMTask("Solve CD", CD_backBar, c2fm_CD_C));
         addTask(new Solve2FMTask("Solve CE", CE_seatPoleBar, c2fm_CE_C));
         addTask(new Solve2FMTask("Solve DE", DE_bottomBar, c2fm_DE_D));
         addTask(new Solve2FMTask("Solve GE", GE_frontBar, c2fm_GE_E));
     }
 }
