 package uk.me.doitto.orbits;
 
 import java.io.Serializable;
 
 /**
  * @author ian
  *
  * Holds per-particle data
  */
 public class Particle implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	double qX, qY, qZ, pX, pY, pZ, mass;
 	
 	public Particle (double qX, double qY, double qZ, double pX, double pY, double pZ, double mass) {
 		this.qX = qX;
 		this.qY = qY;
 		this.qZ = qZ;
 		this.pX = pX;
 		this.pY = pY;
 		this.pZ = pZ;
 		this.mass = mass;
 	}
 	public String toString () {
		return "{\"qX\":" + this.qX + ",\"qY\":" + this.qY + ",\"qZ\":" + this.qZ + ",\"pX\":" + this.pX + ",\"pY\":" + this.pY + ",\"pZ\":" + this.pZ + ",\"mass\":" + this.mass + "}";
 	}
 }
