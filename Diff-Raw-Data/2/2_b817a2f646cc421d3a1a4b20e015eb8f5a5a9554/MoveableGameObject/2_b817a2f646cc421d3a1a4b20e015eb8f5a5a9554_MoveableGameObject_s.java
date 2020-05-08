 package vooga.fighter.objects;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 import vooga.fighter.util.Location;
 import vooga.fighter.util.Pixmap;
 import vooga.fighter.util.Vector;
 
 
 /**
  * Represents a game object that is moveable.
  * 
  * @author alanni, james
  * 
  */
 public class MoveableGameObject extends GameObject {
 
     private Vector myVelocity;
     private List<Vector> myAccelerations;
 
     public MoveableGameObject(Pixmap image, Location center) {
         super(image, center);
         myVelocity = new Vector();
         myAccelerations = new ArrayList<Vector>();
     }
 
     public void update(double elapsedTime, Dimension bounds) {
         for (Vector acceleration : myAccelerations) {
             myVelocity.sum(acceleration);
         }
         Vector v = new Vector(myVelocity);
         translate(v);
     }      
     
     /**
      * Adds an acceleration force to the list of accelerations.
      */
     public void addAcceleration(Vector v) {
         myAccelerations.add(v);
     }
     
     /**
      * Clears the list of acceleration forces.
      */
     public void clearAccelerations() {
         myAccelerations.clear();
     }
 
     /**
      * Moves the center for this moveable game object by a given vector.
      */
     public void translate(Vector v) {
         getCenter().translate(v);
     }
 
     /**
      * Sets the velocity for this moveable game object.
      */
     public void setVelocity(double angle, double magnitude) {
         myVelocity = new Vector(angle, magnitude);
     }
     
     /**
      * Returns the velocity for this moveable game object.
      */
     public Vector getVelocity() {
         return myVelocity;
     }
 
 }
