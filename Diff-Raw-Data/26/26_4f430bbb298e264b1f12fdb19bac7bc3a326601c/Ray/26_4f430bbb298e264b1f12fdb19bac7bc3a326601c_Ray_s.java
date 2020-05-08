 package math;
 
 /**
  * This will work only if you will add the Vec Class from Ex2 - Make sure its working properly.
  * You will also need to implement your own Point3D class.
  * Remove the comments
  */
 public class Ray {
 	
 	// instance variables
 	public Point3D p_origin;
 	public Vec direction;
 	
 	/**
 	 * constructs a new ray
	 * @param p - point of origin
	 * @param v - ray direction
 	 */
 
 	public Ray(Point3D p_origin, Vec direction) {
 		this.p_origin = p_origin;
 		 this.direction = direction;
 	}
 
 }
