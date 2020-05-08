 package dev.simple;
 
 import pcm.PCM3D;
 import pcm.util.V;
 import pcm.util.Vector;
 
 /**
  * This class contains all information for the photon
  * 
  * @author John Stewart
  */
 public class Photon {
 
   public static final double X = 1, Y = 1, Z = 1;
 
   public Vector r = new Vector(), n = new Vector();
 
   public Statistic stat = new Statistic();
 
   public Photon() {
   }
 
   /**
    * This method randomizes the variables in the photon for a new simulation
    * The photon starts at the top with a downward trajectory
    */
   public void reset() {
     double theta = Math.PI / 2 * (PCM3D.rnd.nextDouble() + 1), phi = Math.PI * 2 * PCM3D.rnd.nextDouble();
     n.x = Math.sin(theta) * Math.cos(phi);
     n.y = Math.sin(theta) * Math.sin(phi);
     n.z = Math.cos(theta);
     r.x = X * PCM3D.rnd.nextDouble();
     r.y = Y * PCM3D.rnd.nextDouble();
     r.z = Z;
     stat.newPhoton(r);
   }
 
   /**
    * Moves the photon to the indicated position
    * 
    * @param v The new position
    */
   public void move(Vector v) {
     r.set(v);
   }
 
   public void move(double d) {
     r.add(V.mult(d, n));
   }
 
   /**
    * Reflects the velocity on the vector given and makes a statistical recording
    * Checks if photon is absorbed instead.
    * 
    * @param v The surface normal vector
    * @return Return true if the photon was absorbed, false if not
    */
   public boolean bounce(Vector v) {
     n.add(V.mult(-2 * n.dot(v), v));
     // Statistics here
     if (absorpsionChance()) {
       absorb();
       return true;
     } else {
       stat.addPath(r);
       return false;
     }
   }
 
   /**
    * This method calculates whether the photon is absorbed or not
    * 
    * @return true if absorbed
    */
   public boolean absorpsionChance() {
    return (PCM3D.rnd.nextDouble() < .2);
   }
 
   /**
    * This method handles absorbtion of the photon
    */
   public void absorb() {
     stat.addPath(r);
     stat.absorb(r);
   }
 
 }
