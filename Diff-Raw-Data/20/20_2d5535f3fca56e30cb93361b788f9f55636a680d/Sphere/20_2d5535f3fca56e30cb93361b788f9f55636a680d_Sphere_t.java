 package ray.surface;
 
 import ray.IntersectionRecord;
 import ray.Ray;
 import ray.math.Point3;
 import ray.math.Vector3;
 
 /**
  * Represents a sphere as a center and a radius.
  *
  * @author ags
  */
 public class Sphere extends Surface {
 	
 	/** The center of the sphere. */
 	protected final Point3 center = new Point3();
 	public void setCenter(Point3 center) { this.center.set(center); }
 	
 	/** The radius of the sphere. */
 	protected double radius = 1.0;
 	public void setRadius(double radius) { this.radius = radius; }
 	
 	public Sphere() { }
 	
 	/**
 	 * Tests this surface for intersection with ray. If an intersection is found
 	 * record is filled out with the information about the intersection and the
 	 * method returns true. It returns false otherwise and the information in
 	 * outRecord is not modified.
 	 *
 	 * @param outRecord the output IntersectionRecord
 	 * @param ray the ray to intersect
 	 * @return true if the surface intersects the ray
 	 */
 	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
         //compute ray-sphere intersection (see book p. 77)
         Vector3 d = rayIn.direction;
         Vector3 ec = new Vector3();
         ec.sub(rayIn.origin, center); // e-c
         // the parameters of the quadratic equation A*t^2 + B*t + C = 0:
         double A = d.dot(d);
         double B = 2 * d.dot(ec);
         double C = ec.dot(ec) - radius * radius;
                 
         // the solution
         double discr = B*B - 4*A*C;
         if (discr >= 0.0) {
             double s = Math.sqrt(discr);
             double t1 = (-B + s) / (2 * A);
             double t2 = (-B - s) / (2 * A);
             double t = Math.min(t1,  t2);
                         
             if (t < rayIn.start || t > rayIn.end)
                 return false;
                         
             Point3 p = new Point3(); // intersection point
             rayIn.evaluate(p, t);
             outRecord.location.set(p);
             outRecord.normal.sub(p, center);
             outRecord.normal.normalize();
             outRecord.surface = this;
             outRecord.t = t;
 
             return true;
         }
  
         return false;
 	}
 	
 	/**
 	 * @see Object#toString()
 	 */
 	public String toString() {
 		return "sphere " + center + " " + radius + " " + shader + " end";
 	}
 
 	@Override
 	public void computeBoundingBox() {

		maxBound = new Point3(center);
		minBound = new Point3(center);
 		
		maxBound.add(new Vector3(radius, radius, radius));
		minBound.sub(new Vector3(radius, radius, radius));
				
 		averagePosition=new Point3(maxBound);
		averagePosition.add(new Vector3(minBound));
 		averagePosition.scale(1/2);
 	}
 
 }
