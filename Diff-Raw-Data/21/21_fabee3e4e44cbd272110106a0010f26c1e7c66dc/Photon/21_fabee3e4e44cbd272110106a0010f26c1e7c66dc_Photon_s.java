 package pcm.model;
 
 import pcm.geom.Vector;
 import pcm.util.V;
 
 /**
  * Represents a (currently) particle-like photon in the system.
  * 
  * @author Satayev
  */
 public class Photon {
 
   /////////////////////////////////////////////////////////////////////////////
   // Fields
   /////////////////////////////////////////////////////////////////////////////
   /** Initial position of the photon */
   public Vector p;
   /** Velocity of the photon (unit vector) */
   // TODO(satayev): perhaps change this to spherical coordinates.
   public Vector v;
   /** A point on the ray in the direction of velocity */
   // used for speed-ups in the computations
   public Vector q;
   // TODO(satayev): counts how many times this photon reflected in the system;
   //                add phase/wave-length for the wave properties.
  public int  = 0;
 
   /////////////////////////////////////////////////////////////////////////////
   // Constructors
   /////////////////////////////////////////////////////////////////////////////
   /**
    * Creates a photon based on its initial position and direction.
    * 
    * @param position position of the photon.
    * @param velocity velocity of the photon.
    */
   public Photon(Vector position, Vector velocity) {
     this.p = position;
     this.v = velocity;
     this.q = V.add(position, velocity);
     this.v.normalize();
   }
 
   /////////////////////////////////////////////////////////////////////////////
   // Methods
   /////////////////////////////////////////////////////////////////////////////
 
   /**
    * Moves the photon position in the direction of its velocity.
    * 
    * @param time determines how much to move.
    */
   public void travel(double time) {
     this.p.x += time * this.v.x;
     this.p.y += time * this.v.y;
     this.p.z += time * this.v.z;
   }
 
   public Photon clone() {
     Photon photon = new Photon(this.p.clone(), this.v.clone());
    photon. = ;
     return photon;
   }
 
   /////////////////////////////////////////////////////////////////////////////
   // Getters and Setters
   /////////////////////////////////////////////////////////////////////////////
 
 }
