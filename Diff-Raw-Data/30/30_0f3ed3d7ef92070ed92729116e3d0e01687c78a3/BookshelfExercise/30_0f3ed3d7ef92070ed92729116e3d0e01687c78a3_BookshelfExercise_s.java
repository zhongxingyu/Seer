 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bookshelf;
 
 import com.jme.renderer.ColorRGBA;
 import com.jme.system.DisplaySystem;
 import edu.gatech.statics.exercise.Schematic;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.modes.distributed.DistributedExercise;
 import edu.gatech.statics.modes.distributed.objects.ConstantDistributedForce;
 import edu.gatech.statics.modes.distributed.objects.DistributedForce;
 import edu.gatech.statics.modes.distributed.objects.DistributedForceObject;
 import edu.gatech.statics.objects.DistanceMeasurement;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.bodies.Beam;
 import edu.gatech.statics.ui.AbstractInterfaceConfiguration;
 import edu.gatech.statics.ui.windows.navigation.Navigation3DWindow;
 import java.math.BigDecimal;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class BookshelfExercise extends DistributedExercise {
 
     @Override
     public AbstractInterfaceConfiguration createInterfaceConfiguration() {
         AbstractInterfaceConfiguration interfaceConfiguration = (AbstractInterfaceConfiguration) super.createInterfaceConfiguration();
         interfaceConfiguration.setNavigationWindow(new Navigation3DWindow());
         return interfaceConfiguration;
     }
 
     @Override
     public void initExercise() {
         setName("Bookshelf");
         setDescription("What is the loading on the supports of the bookshelf?");
 
         // *****
         // UNITS ARE ISSUE
         // problem specifies distribution of mass for the books, not the forces
         // *****
 
         Unit.setSuffix(Unit.distance, " mm");
         Unit.setSuffix(Unit.moment, " N*mm");
         Unit.setSuffix(Unit.force, " N");
         Unit.setSuffix(Unit.mass, " kg");
        Unit.setSuffix(Unit.forceOverDistance, " N/m");
         Unit.setDisplayScale(Unit.distance, new BigDecimal(".01"));
 
         getDisplayConstants().setMomentSize(0.5f);
         getDisplayConstants().setForceSize(0.5f);
         getDisplayConstants().setPointSize(0.5f);
         getDisplayConstants().setCylinderRadius(0.5f);
         getDisplayConstants().setForceLabelDistance(5f);
         getDisplayConstants().setMomentLabelDistance(10f);
         getDisplayConstants().setMeasurementBarSize(0.2f);
     }
 
     @Override
     public void loadExercise() {
 
         DisplaySystem.getDisplaySystem().getRenderer().setBackgroundColor(new ColorRGBA(.9f, .9f, .9f, 1.0f));
 
         Schematic schematic = getSchematic();
 
         Point A = new Point("A", "0", "0", "0");
         Point B = new Point("B", "10", "0", "0");
 
         Point mid1 = new Point("mid1", "2", "0", "0");
         Point mid2 = new Point("mid2", "6", "0", "0");
         Point mid3 = new Point("mid3", "7", "0", "0");
 
         Beam bookshelf = new Beam("Bookshelf", A, B);
 
         DistributedForce dlBooks1 = new ConstantDistributedForce("books1", bookshelf, A, mid1,
                 new Vector(Unit.forceOverDistance, Vector3bd.UNIT_Y.negate(), new BigDecimal("20")));
         DistributedForceObject dlObjectBooks1 = new DistributedForceObject(dlBooks1, "1");
         bookshelf.addObject(dlObjectBooks1);
 
         DistributedForce dlBooks2 = new ConstantDistributedForce("books2", bookshelf, mid1, mid2,
                 new Vector(Unit.forceOverDistance, Vector3bd.UNIT_Y.negate(), new BigDecimal("30")));
         DistributedForceObject dlObjectBooks2 = new DistributedForceObject(dlBooks2, "2");
         bookshelf.addObject(dlObjectBooks2);
 
         DistributedForce dlBooks3 = new ConstantDistributedForce("books3", bookshelf, mid3, B,
                 new Vector(Unit.forceOverDistance, Vector3bd.UNIT_Y.negate(), new BigDecimal("15")));
         DistributedForceObject dlObjectBooks3 = new DistributedForceObject(dlBooks3, "3");
         bookshelf.addObject(dlObjectBooks3);
 
         bookshelf.addObject(mid1);
         bookshelf.addObject(mid2);
         bookshelf.addObject(mid3);
 
         A.createDefaultSchematicRepresentation();
         B.createDefaultSchematicRepresentation();
         bookshelf.createDefaultSchematicRepresentation();
         
         int arrowDensity = 2;
         dlObjectBooks1.createDefaultSchematicRepresentation(20f / 6, 2 * arrowDensity);
         dlObjectBooks2.createDefaultSchematicRepresentation(30f / 6, 4 * arrowDensity);
         dlObjectBooks3.createDefaultSchematicRepresentation(15f / 6, 3 * arrowDensity);
 
         DistanceMeasurement measureFull = new DistanceMeasurement(A, B);
         measureFull.createDefaultSchematicRepresentation(2);
         schematic.add(measureFull);
 
         DistanceMeasurement measure1 = new DistanceMeasurement(A, mid1);
         measure1.createDefaultSchematicRepresentation();
         schematic.add(measure1);
 
         DistanceMeasurement measure2 = new DistanceMeasurement(mid1, mid2);
         measure2.createDefaultSchematicRepresentation();
         schematic.add(measure2);
 
         DistanceMeasurement measure3 = new DistanceMeasurement(mid3, B);
         measure3.createDefaultSchematicRepresentation();
         schematic.add(measure3);
 
         schematic.add(bookshelf);
     }
 }
