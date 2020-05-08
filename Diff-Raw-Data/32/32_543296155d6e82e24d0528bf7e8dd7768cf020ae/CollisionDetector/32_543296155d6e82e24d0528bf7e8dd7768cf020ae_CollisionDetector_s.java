 package de.engine.colldetect;
 
 import java.util.*;
 
 import de.engine.DebugMonitor;
 import de.engine.colldetect.Grid.CollPair;
 import de.engine.environment.Scene;
 import de.engine.math.*;
 import de.engine.math.DistanceCalcer.*;
 import de.engine.math.Vector;
 import de.engine.objects.Circle;
 import de.engine.objects.Ground;
 import de.engine.objects.ObjectProperties;
 import de.engine.objects.Polygon;
 import de.engine.physics.ContactCreator.Contact;
 import de.engine.physics.PhysicsCalcer;
 
 public class CollisionDetector
 {
     
     private Grid grid;
     private Scene scene;
     private de.engine.math.Vector v = null;
     
     private DistanceCalcer distCalcer;
     private Contact gcontact = null;
     
     public CollisionDetector(Scene scene)
     {
         v = new de.engine.math.Vector();
         v = v.setUnitVector(v);
         
         grid = new Grid(scene);
         this.scene = scene;
         
         distCalcer = new DistanceCalcer(0.1);
     }
     
     public void checkScene()
     {
         grid.scanScene();
         CollPair collPair = grid.getNextCollision();
         while (collPair != null)
         {
             if (null != collPair.coll_time)
             {
                 PhysicsCalcer.run(collPair.obj1, collPair.obj2, collPair.coll_time);
                 grid.update(collPair.obj1);
                 grid.update(collPair.obj2);
             }
             collPair = grid.getNextCollision();
         }
         
         // Tests the collision between objects and ground
         if (scene.existGround())
         {
             objectGroundCollision();
 //            objectGroundCollision2();
         }
         
     }
     
     public void objectGroundCollision()
     {
         long time = System.currentTimeMillis();
 
         if (gcontact==null) gcontact = new Contact(new Vector(), new Vector());
         
         for (ObjectProperties object : scene.getObjects())
         {
             if (scene.getCount() > 0 && object != null && scene.getGround() != null)
             {
                 Ground ground = scene.getGround();
                 Util.ground = ground;
                 Util.object = object;
                 Double xn = Util.newtonIteration();
                 
                 object.last_intersection.setX( xn );
                 object.last_intersection.setY( ground.function(xn) );
                 
                 double x = object.last_intersection.getX();
                 double y = object.last_intersection.getY();
 
                // calc distance by pythagoras
                 double c = Math.sqrt( Math.pow(x - object.getPosition().getX(), 2.0) + Math.pow(y - object.getPosition().getY(), 2.0) );
 
                 if (c <= object.getRadius())
                 {
                    Vector coll_normal = new Vector( x, -x/Util.derive1Dr( x ) + y).norm();
                     
                    Vector r_o1 = new Vector( x - object.getPosition().getX(), y - object.getPosition().getY()).norm();
                     double r_o1_cross_n = Util.crossProduct(r_o1, coll_normal);
 
                     double j_z = -Util.scalarProduct(object.velocity, coll_normal) * (1d + (object.surface.elasticity() + ground.surface.elasticity()) / 2d);
                     double j_n = (1d / object.getMass()) + (r_o1_cross_n * r_o1_cross_n) / object.moment_of_inertia;
                     
                     double j = j_z / j_n;
                     
                    Vector v_vec = object.velocity.add( coll_normal.multi( j ).multi( 1d / object.getMass() ));
                     
                     object.velocity.setX( v_vec.getX() );
                     object.velocity.setY( v_vec.getY() );       
                 }
             }
         }
         
         DebugMonitor.getInstance().updateMessage("groundColl", "" + (System.currentTimeMillis() - time));
        // System.out.println(System.currentTimeMillis() - time + " ms / " + "SP: [ " + (int) scene.getObject(0).last_intersection.getX() + ", " + (int) scene.getObject(0).last_intersection.getY() + " ]");
     }
 
     public void objectGroundCollision2()
     {
         long t = System.currentTimeMillis();
         IFunction func = new IFunction()
         {
             @Override
             public double function(double x)
             {
                 return scene.getGround().function((int) x);
             }
         };
         
         for (ObjectProperties object : scene.getObjects())
         {
             if (object.isPinned)
                 continue;
             // this helps to define an interval
             // double range = object.velocity.getX() * 2;
             double range = object.getRadius() * 5;
             de.engine.math.Vector nextPos = object.getNextPosition();
             
             distCalcer.setPoint(nextPos);
             distCalcer.setFunction(func);
             double dist = distCalcer.calculateDistanceBetweenFunctionPoint(nextPos.getX() - range, nextPos.getX() + range);
             
             object.closest_point = new de.engine.math.Vector(distCalcer.getLastSolvedX(), scene.getGround().function((int) distCalcer.getLastSolvedX()));
             
             if (dist < object.getRadius())
             {
                 if (object instanceof Circle)
                 {
                     object.isPinned = true;
                 }
                 else if (object instanceof Polygon)
                 {
                     Polygon polygon = (Polygon) object;
                     List<Vector> rotatedPoints = new ArrayList<>();
                     
                     for (Vector point : polygon.points)
                     {
                         
                         rotatedPoints.add(Util.add(polygon.world_position.rotation.getMatrix().multVector(point),
                                 polygon.getPosition()));
                     }
                     
                     int length = polygon.points.length;
                     
                     for (int i = 0; i < length; i++)
                     {
                         Vector p1 = rotatedPoints.get(i);
                         Vector p2 = rotatedPoints.get((i + 1)% length);
                         
                         if (p1.getX() == p2.getX())
                         {
                             continue;
                         }
                         
                         StraightLine line = new StraightLine(p1, p2);
                         
                         distCalcer.setStraightLine(line);
                         dist = distCalcer.findRootBetweenFunctionLine();
                         
                         if (dist <= 0.1)
                         {
                             double x = distCalcer.getLastSolvedX();
                             double y = line.function(x);
                             
                             polygon.closest_point = new Vector(x, y);
                             polygon.isPinned = true;
                             break;
                         }
                     }
                 }
             }
         }
         DebugMonitor.getInstance().updateMessage("distCalc", "" + (System.currentTimeMillis() - t));
     }
 }
