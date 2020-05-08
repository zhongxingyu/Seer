 package simulation;
 
 import java.awt.event.KeyEvent;
 import java.util.Scanner;
 import util.Vector;
 /**
  * @author Wayne You and Ross Cahoon
  * Calculates and tracks the gravity force.
  */
 public class GravityForce extends Force {
 
    private boolean myGravityActive = true;
     private final Vector DEFAULT_GRAVITY = new Vector(90, .2);
     /**
      * The vector for gravity force.
      */
     private Vector myGravity;
     /**
      * Used to construct the default GravityForce object.
      */
     public GravityForce() {
         myGravity = DEFAULT_GRAVITY;
     }
     /**
      * Used to construct the GravityForce object.
      * @param gravity assigned to myGravity
      */
     public GravityForce(Vector gravity) {
         myGravity = gravity;
     }
 
     @Override
     public final void applyForce(final Mass m) {
         if (myGravityActive) {
             Vector massGravity = new Vector(myGravity);
             massGravity.scale(m.getMass());
             m.applyForce(massGravity);
         }
     }
 
     // create gravity vector from formatted data
     private void gravityCommand(Scanner line) {
         double angle = line.nextDouble();
         double magnitude = line.nextDouble();
         myGravity = new Vector(angle, magnitude);
 
     }
 
     /**
      * Toggles whether gravity is active.
      */
     public void toggle (int key) {
         if (key == KeyEvent.VK_G) {
             myGravityActive = !myGravityActive;
         }
     }
 
 }
