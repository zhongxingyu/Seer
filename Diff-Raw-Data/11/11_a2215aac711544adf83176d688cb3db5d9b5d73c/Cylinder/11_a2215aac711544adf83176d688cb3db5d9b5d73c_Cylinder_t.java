 package ray.surface;
 
 import ray.IntersectionRecord;
 import ray.Ray;
 import ray.math.Point3;
 //import ray.math.Vector3;
 import ray.math.Vector3;
 
 public class Cylinder extends Surface {
 
 	/** The center of the cylinder */
 	protected final Point3 center = new Point3();
 
 	public void setCenter(Point3 center) {
 		this.center.set(center);
 	}
 
 	/** The radius of the cylinder. */
 	protected double radius = 1.0;
 
 	public void setRadius(double radius) {
 		this.radius = radius;
 	}
 
 	/**
 	 * The height of the cylinder in the z-direction. The cylinder extends from
 	 * center.z - height/2 to center.z + height/2
 	 */
 	protected double height = 1.0;
 
 	public void setHeight(double height) {
 		this.height = height;
 	}
 
 	public Cylinder() {
 	}
 
 	/**
 	 * Tests this surface for intersection with ray. If an intersection is found
 	 * record is filled out with the information about the intersection and the
 	 * method returns true. It returns false otherwise and the information in
 	 * outRecord is not modified.
 	 * 
 	 * @param outRecord
 	 *            the output IntersectionRecord
 	 * @param ray
 	 *            the ray to intersect
 	 * @return true if the surface intersects the ray
 	 */
 	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
 
 		Point3 p = new Point3(rayIn.origin);
 		Vector3 d = new Vector3(rayIn.direction);
 
 		Point3 c = center;
 		double R = radius;
 		double H = height;
 		Point3 pc = new Point3(p.x - c.x, p.y - c.y, p.z - c.z);
 
 		double A = Math.pow(d.x, 2) + Math.pow(d.y, 2);
 		double B = 2 * (pc.x * d.x + pc.y * d.y);
 		double C = Math.pow(pc.x, 2) + Math.pow(pc.y, 2) - Math.pow(R, 2);
 
 		double discriminant = (Math.pow(B, 2) - (4 * A * C));
 		if (discriminant < 0.0) {
 			return false;
 		} else {
 
 			double t0 = ((-B) + Math.sqrt(discriminant)) / (2 * A);
 			double t1 = ((-B) - Math.sqrt(discriminant)) / (2 * A);
 
 			double t = 0.0;
 
 			Point3 q0 = new Point3();
 			Point3 q1 = new Point3();
 
 			rayIn.evaluate(q0, t0);
 			rayIn.evaluate(q1, t1);
 
 			if ((q0.z >= c.z - H / 2 && q0.z <= c.z + H / 2)
 					&& (q1.z >= c.z - H / 2 && q1.z <= c.z + H / 2)) {
 				if (t0 < t1) {
 					// System.out.println("both t0");
 					t = t0;
 				} else {
 					// System.out.println("both t1");
 					t = t1;
 				}
 			} else if ((q0.z >= c.z - H / 2 && q0.z <= c.z + H / 2)
 					&& !(q1.z >= c.z - H / 2 & q1.z <= c.z + H / 2)) {
 				// System.out.println("only t0");
 				t = t0;
 			} else if (!(q0.z >= c.z - H / 2 && q0.z <= c.z + H / 2)
 					&& (q1.z >= c.z - H / 2 && q1.z <= c.z + H / 2)) {
 				// System.out.println("only t1");
 				t = t1;
 			} else {
 				// System.out.println("none");
 				return false;
 			}
 
 			// Calculate the outRecord
 			Point3 q = new Point3();
 
 			rayIn.evaluate(q, t);
 			outRecord.location.set(q);
 			outRecord.surface = this;
 			outRecord.t = t;
 
			outRecord.normal.set(new Vector3(q.x-c.x, q.y-c.y, 0));
 			outRecord.normal.normalize();
 			outRecord.normal.scale(-1);
 
 			return true;
 		}
 
 	}
 
 	/**
 	 * @see Object#toString()
 	 */
 	public String toString() {
 		return "Cylinder " + center + " " + radius + " " + height + " "
 				+ shader + " end";
 	}
 }
