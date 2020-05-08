 package tesseract.forces;
 
 import javax.vecmath.Vector3f;
 
 import tesseract.objects.PhysicalObject;
 
 /**
  * Generic downward force class (aka Gravity).
  * 
  * @author Jesse Morgan
  */
 public class Gravity extends Force {
 	/**
 	 * Default gravity force.
 	 */
 	private static final float DEFAULT_GRAVITY = 1f;
 	
 	/**
 	 * The force used here.
 	 */
 	private float myGravity;
 	
 	/**
 	 * Create a default gravity.
 	 */
 	public Gravity() {
 		myGravity = DEFAULT_GRAVITY;
 	}
 	
 	/**
 	 * Create gravity with a custom strength.
 	 * 
 	 * @param gravity The strength of gravity.
 	 */
 	public Gravity(final float gravity) {
 		myGravity = gravity;
 	}
 	
 	/**
 	 * Calculate the force of gravity...
 	 * 
 	 * @param obj The object the force is calculated for.
 	 * @return A vector describing the force
 	 */
 	protected Vector3f calculateForce(final PhysicalObject obj) {
		return new Vector3f(0, -myGravity, 0);
 	}
 
 }
