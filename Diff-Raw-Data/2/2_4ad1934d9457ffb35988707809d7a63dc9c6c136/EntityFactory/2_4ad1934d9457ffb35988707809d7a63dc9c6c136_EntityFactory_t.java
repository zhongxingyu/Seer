 package uk.co.danielrendall.asteroids.entities;
 
 import uk.co.danielrendall.asteroids.entities.BasicShape;
 import uk.co.danielrendall.asteroids.entities.Shape;
 import uk.co.danielrendall.mathlib.geom2d.BoundingBox;
 import uk.co.danielrendall.mathlib.geom2d.Point;
 import uk.co.danielrendall.mathlib.geom2d.Vec;
 
 import java.awt.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniel
  * Date: 29-Apr-2010
  * Time: 08:21:15
  * To change this template use File | Settings | File Templates.
  */
 public class EntityFactory {
     public enum AsteroidSize {
         LARGE(24, 32.0d, 16.0d, Color.RED),
         MEDIUM(12, 16.0d, 8.0d, Color.GREEN),
         SMALL(6, 8.0d, 4.0d, Color.BLUE);
         private final int points;
         private final double radius;
         private final double crinkliness;
         private final Color initialColor;
 
         AsteroidSize(int points, double radius, double crinkliness, Color initialColor) {
             this.points = points;
             this.radius = radius;
             this.crinkliness = crinkliness;
             this.initialColor = initialColor;
         }
 
         public int getPoints() {
             return points;
         }
 
         public double getRadius() {
             return radius;
         }
 
         public double getCrinkliness() {
             return crinkliness;
         }
 
         public Color getInitialColor() {
             return initialColor;
         }
     }
     public final double MAX_VELOCITY = 5.0d;
     public final double MAX_ANGULAR_VELOCITY = Shape.TWO_PI / 24.0d;
     private final BoundingBox velocitySpace = new BoundingBox(-MAX_VELOCITY, MAX_VELOCITY, -MAX_VELOCITY, MAX_VELOCITY);
 
     public Entity createAsteroid(BoundingBox bounds, AsteroidSize size) {
         Point pos = bounds.randomPoint();
         Vec vel = new Vec(velocitySpace.randomPoint());
         Shape shape = createAsteroidShape(size.getRadius(), size.getCrinkliness(), size.getPoints());
         double angularVelocity = Math.random() * MAX_ANGULAR_VELOCITY - (MAX_ANGULAR_VELOCITY / 2.0d);
        Entity ret = new BasicEntity(pos, vel, Vec.ZERO, Math.random() * Shape.TWO_PI, angularVelocity);
         ret.setRepresentation(shape);
         ret.setColor(size.getInitialColor());
         return ret;
     }
 
     public Shape createAsteroidShape(double size, double crinkliness, int numPoints) {
         Vec[] pointDisplacements = new Vec[numPoints];
         final double theta = Shape.TWO_PI / (double) numPoints;
         final double halfCrinkliness = crinkliness / 2.0d;
         for (int i = 0; i < numPoints; i++) {
             double arg = theta * (double) i;
             double radius = size + (Math.random() * crinkliness) - halfCrinkliness;
             pointDisplacements[i] = Vec.unit(arg).scale(radius);
         }
         return new BasicShape(pointDisplacements);
     }
 
 
 }
