 /**
  *
  */
 package framework.core;
 
 /**
  * A location in three dimensional space.
  * @author brad
  */
 public final class Point3 {
 
 	/**
 	 * The distances from the origin along each axis.
 	 */
 	public double x, y, z;
 
 	/**
 	 * Default constructor.
 	 *
 	 */
 	public Point3() {
 	}
 
 	/**
 	 * Initializes the components for the point.
 	 * @param x The distance from the origin along the x axis.
 	 * @param y The distance from the origin along the y axis.
 	 * @param z The distance from the origin along the z axis.
 	 */
 	public Point3(double x, double y, double z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	/**
 	 * Computes the square of the distance from this point to the
 	 * specified point.
 	 * @param p The point to compute the square of the distance to.
 	 * @return The square of the distance between this point and
 	 * the specified point.
 	 */
 	public double squaredDistanceTo(Point3 p) {
 		return ((x - p.x) * (x - p.x)) + ((y - p.y) * (y - p.y)) + ((z - p.z) * (z - p.z));
 	}
 
 	/**
 	 * Computes the distance between this point and the specified point.
 	 * @param p The point to compute the distance to.
 	 * @return The distance between this point and p.
 	 */
 	public double distanceTo(Point3 p) {
 		return Math.sqrt(squaredDistanceTo(p));
 	}
 
 	/**
 	 * Computes the vector from this point to the specified point.
 	 * @param p The point at the end of the vector.
 	 * @return The vector from this point to p.
 	 */
 	public Vector3 vectorTo(Point3 p) {
 		return new Vector3(p.x - x, p.y - y, p.z - z);
 	}
 
 	/**
 	 * Computes the vector from the specified point to this point.
 	 * @param p The point at the start of the vector.
 	 * @return The vector from p to this point.
 	 */
 	public Vector3 vectorFrom(Point3 p) {
 		return new Vector3(x - p.x, y - p.y, z - p.z);
 	}
 
 	/**
 	 * Returns this point translated according to the specified vector.
 	 * @param v The vector to translate this point by.
 	 * @return The value of this point translated by v.
 	 */
 	public Point3 plus(Vector3 v) {
 		return new Point3(x + v.x, y + v.y, z + v.z);
 	}
 
 	/**
 	 * Translates this point by the specified vector.
 	 * Equivalent to {@code this = this.plus(v);}
 	 * @param v The vector to translate this point along.
 	 * @see plus
 	 */
 	public void add(Vector3 v) {
 		x += v.x;
 		y += v.y;
		z += v.z;
 	}
 
 	/**
 	 * Returns this point translated in the opposite direction of the
 	 * specified vector.
 	 * @param v The opposite of the vector to translate by.
 	 * @return The value of this point translated by -v.
 	 */
 	public Point3 minus(Vector3 v) {
 		return new Point3(x - v.x, y - v.y, z - v.z);
 	}
 
 	/**
 	 * Translates this point by the opposite of the specified vector.
 	 * Equivalent to {@code this = this.minus(v);}
 	 * @param v The opposite of the vector to translate this point by.
 	 * @see minus
 	 */
 	public void subtract(Vector3 v) {
 		x -= v.x;
 		y -= v.y;
 	}
 
 	/**
 	 * The origin of three dimensional space.
 	 */
 	public static final Point3 ORIGIN = new Point3(0.0, 0.0, 0.0);
 
 }
