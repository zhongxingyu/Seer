 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package bicycle;
 
 import com.jme.math.Vector3f;
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.Representation;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.FBDExercise;
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
 import edu.gatech.statics.objects.joints.Connector2ForceMember2d;
 import edu.gatech.statics.objects.joints.Pin2d;
 import edu.gatech.statics.objects.joints.Roller2d;
 import edu.gatech.statics.objects.representations.ModelRepresentation;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class BicycleExercise extends FBDExercise {
 
     public BicycleExercise() {
         super(new Schematic());
     }
 
     @Override
     public void initExercise() {
         setName("Bicycle");
         
         setDescription(
                 "Someone pushing against a wedge on a bike!"
                 );
         
         Unit.setSuffix(Unit.distance, " mm");
         Unit.setSuffix(Unit.moment, " N*m");
         Unit.setDisplayScale(Unit.distance, new BigDecimal(".1"));       
     }
     
     Point A, I, H, J, G, F, B, K;
     Roller2d rollerA, rollerB;
     Connector2ForceMember2d twoForceF, twoForceH, twoForceJH, twoForceJB, twoForceGF, twoForceGB;
     @Override
     public float getDrawScale() {
         return 0.2f;
     }
 
     @Override
     public void loadExercise() { 
         Schematic schematic = getSchematic();
         
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.2f, .2f, .9f, 1.0f));
         StaticsApplication.getApp().getCamera().setLocation(new Vector3f(0.0f, 0.0f, 65.0f));
         
         A = new Point("-2.145","1.75","0");
         I = new Point("-0.15","5.25","0");
         H = new Point("-0.65","4.35","0");
         J = new Point("2.35","4.35","0");
         G = new Point("0.85","1.75","0");
         F = new Point("-1.05","3.65","0");
         B = new Point("3.85","1.75","0");
         K = new Point("2.85","5.21","0");
         
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
                 new BigDecimal(-Math.cos(Math.PI/6)), 
                 new BigDecimal(-Math.sin(Math.PI/6)), BigDecimal.ZERO);
         handleForceDirection = handleForceDirection.normalize();
         
         FixedAngleMeasurement angle5 = new FixedAngleMeasurement(I, Vector3bd.UNIT_X.negate(), handleForceDirection.toVector3f());
         angle5.createDefaultSchematicRepresentation(0.5f);
         schematic.add(angle5);
         
         Force handleWeight = new Force(I, handleForceDirection, new BigDecimal(20));
         handleWeight.setName("Handlebar");
         handlebar.addObject(handleWeight);
         
         Moment handleMoment = new Moment(I, Vector3bd.UNIT_Z, new BigDecimal(5)); // use symbol here
         handleMoment.setSymbol("Handlebar");
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
         handlebar.createDefaultSchematicRepresentation();
         handleMoment.createDefaultSchematicRepresentation();
         top.createDefaultSchematicRepresentation();
         seatPole.createDefaultSchematicRepresentation();
         front.createDefaultSchematicRepresentation();
         back.createDefaultSchematicRepresentation();
         bottom.createDefaultSchematicRepresentation();
         seatWeight.createDefaultSchematicRepresentation();
         pedalWeight.createDefaultSchematicRepresentation();
         handleWeight.createDefaultSchematicRepresentation();
         schematic.add(handlebar);
         schematic.add(top);
         schematic.add(seatPole);
         schematic.add(front);
         schematic.add(back);
         schematic.add(bottom);
         
         float scale = 0.1f;
         
         Representation rep = new ModelRepresentation(handlebar, "bicycle/assets/", "bicycle/assets/handlepole.dae");
         rep.setLocalScale(scale);
         handlebar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
     
         rep = new ModelRepresentation(handlebar, "bicycle/assets/", "bicycle/assets/handlebar.dae");
         rep.setLocalScale(scale);
         handlebar.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         
         rep = new ModelRepresentation(back, "bicycle/assets/", "bicycle/assets/back.dae");
         rep.setLocalScale(scale);
         back.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         
 //        rep = new ModelRepresentation(handlebar, "bicycle/assets/", "bicycle/assets/body.dae");
 //        rep.setLocalScale(scale);
 //        handlebar.addRepresentation(rep);
 //        rep.setSynchronizeRotation(false);
 //        rep.setSynchronizeTranslation(false);
 //        
 //        rep = new ModelRepresentation(handlebar, "bicycle/assets/", "bicycle/assets/bike.dae");
 //        rep.setLocalScale(scale);
 //        handlebar.addRepresentation(rep);
 //        rep.setSynchronizeRotation(false);
 //        rep.setSynchronizeTranslation(false);
         
         rep = new ModelRepresentation(bottom, "bicycle/assets/", "bicycle/assets/bottom.dae");
         rep.setLocalScale(scale);
         bottom.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         
         rep = new ModelRepresentation(front, "bicycle/assets/", "bicycle/assets/front.dae");
         rep.setLocalScale(scale);
         front.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         
         rep = new ModelRepresentation(seatPole, "bicycle/assets/", "bicycle/assets/seat.dae");
         rep.setLocalScale(scale);
         seatPole.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         
         rep = new ModelRepresentation(seatPole, "bicycle/assets/", "bicycle/assets/seatpole.dae");
         rep.setLocalScale(scale);
         seatPole.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
         
         rep = new ModelRepresentation(top, "bicycle/assets/", "bicycle/assets/top.dae");
         rep.setLocalScale(scale);
         top.addRepresentation(rep);
         rep.setSynchronizeRotation(false);
         rep.setSynchronizeTranslation(false);
     }
 }
