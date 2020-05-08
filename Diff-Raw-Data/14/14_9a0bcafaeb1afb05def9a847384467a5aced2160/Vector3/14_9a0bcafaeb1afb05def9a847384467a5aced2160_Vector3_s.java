 /**
  *
  */
 package org.jmist.toolkit;
 
 import java.io.Serializable;
 
 /**
  * The difference between two points in three dimensional space.
  * This class is immutable.
  * @author brad
  */
 public final class Vector3 implements Serializable {
 
 	/**
 	 * Initializes the components for the vector.
 	 * @param x The length of the vector along the x axis.
 	 * @param y The length of the vector along the y axis.
 	 * @param z The length of the vector along the z axis.
 	 */
 	public Vector3(double x, double y, double z) {
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 
 	/**
 	 * Gets the distance from the origin along the x-axis.
 	 * Equivalent to {@code this.dot(Vector3.I);}
 	 * @return The distance from the origin along the x-axis.
 	 * @see {@link #I}, {@link #dot(Vector3)}.
 	 */
 	public double x() {
 		return x;
 	}
 
 	/**
 	 * Gets the distance from the origin along the y-axis.
 	 * Equivalent to {@code this.dot(Vector3.J);}
 	 * @return The distance from the origin along the y-axis.
 	 * @see {@link #J}, {@link #dot(Vector3)}.
 	 */
 	public double y() {
 		return y;
 	}
 
 	/**
 	 * Gets the distance from the origin along the z-axis.
 	 * Equivalent to {@code this.dot(Vector3.K);}
 	 * @return The distance from the origin along the z-axis.
 	 * @see {@link #K}, {@link #dot(Vector3)}.
 	 */
 	public double z() {
 		return z;
 	}
 
 	/**
 	 * Computes the magnitude of the vector.
 	 * @return The magnitude of the vector.
 	 */
 	public double length() {
 		return Math.sqrt(dot(this));
 	}
 
 	/**
 	 * Computes the square of the magnitude of this vector.
 	 * @return The square of the magnitude of this vector.
 	 */
 	public double squaredLength() {
 		return dot(this);
 	}
 
 	/**
 	 * Returns the opposite of this vector.
 	 * @return The opposite of this vector.
 	 */
 	public Vector3 opposite() {
 		return new Vector3(-x, -y, -z);
 	}
 
 	/**
 	 * Computes the sum of two vectors.
 	 * @param v The vector to add to this vector.
 	 * @return The sum of this vector and v.
 	 */
 	public Vector3 plus(Vector3 v) {
 		return new Vector3(x + v.x, y + v.y, z + v.z);
 	}
 
 	/**
 	 * Computes the difference between two vectors.
 	 * @param v The vector to subtract from this vector.
 	 * @return The difference between this vector and v.
 	 */
 	public Vector3 minus(Vector3 v) {
 		return new Vector3(x - v.x, y - v.y, z - v.z);
 	}
 
 	/**
 	 * Computes this vector scaled by a constant factor.
 	 * @param c The factor to scale this vector by.
 	 * @return This vector scaled by c.
 	 */
 	public Vector3 times(double c) {
 		return new Vector3(c * x, c * y, c * z);
 	}
 
 	/**
 	 * Computes this vector scaled by the reciprocal of
 	 * a constant factor.
 	 * Equivalent to {@code this.times(1.0 / c).}
 	 * @param c The factor to divide this vector by.
 	 * @return The vector scaled by 1.0 / c.
 	 * @see {@link #times(double)}.
 	 */
 	public Vector3 divide(double c) {
 		return new Vector3(x / c, y / c, z / c);
 	}
 
 	/**
 	 * Computes the dot product between two vectors.
 	 * @param v The vector to compute the dot product of with this vector.
 	 * @return The dot product of this vector and v.
 	 */
 	public double dot(Vector3 v) {
 		return (x * v.x) + (y * v.y) + (z * v.z);
 	}
 
 	/**
 	 * Computes the cross product of two vectors.
 	 * @param v The vector to compute the cross product of with this vector.
 	 * @return The cross product of this vector and v.
 	 */
 	public Vector3 cross(Vector3 v) {
 		return new Vector3(
 					(y * v.z) - (z * v.y),
 					(z * v.x) - (x * v.z),
 					(x * v.y) - (y * v.x)
 				);
 	}
 
 	/**
 	 * Computes the unit vector in the same direction as this vector.
 	 * @return The unit vector in the same direction as this vector.
 	 */
 	public Vector3 unit() {
 		return this.times(1.0 / length());
 	}
 
 	/**
 	 * Returns a new unit vector in the same direction as
 	 * the vector with the specified components.
 	 * Equivalent to {@code new Vector3(x, y, z).unit()}.
 	 * @param x The magnitude of the vector along the x-axis.
 	 * @param y The magnitude of the vector along the y-axis.
 	 * @param z The magnitude of the vector along the z-axis.
 	 * @return A unit vector in the same direction as the
 	 * 		vector with the indicated components.
 	 * @see {@link #unit()}.
 	 */
 	public static Vector3 unit(double x, double y, double z) {
 		double r = Math.sqrt(x * x + y * y + z * z);
 		return new Vector3(x / r, y / r, z / r);
 	}
 
 	/**
 	 * Returns an arbitrary <code>Vector3</code> that is perpendicular to this
 	 * <code>Vector3</code> (i.e., it is guaranteed that
 	 * {@code this.dot(this.perp()) == 0.0}.
 	 * @return An arbitrary <code>Vector3</code> perpendicular to this one.
 	 */
 	public Vector3 perp() {
		if (x < y && x < z) {
 			return new Vector3(0, -z, y);
		} else if (y < z) {
 			return new Vector3(z, 0, -x);
 		} else {
 			return new Vector3(-y, x, 0);
 		}
 	}
 
 	/**
 	 * The zero vector (represents the vector between two identical points).
 	 */
 	public static final Vector3 ZERO = new Vector3(0.0, 0.0, 0.0);
 
 	/**
 	 * The unit vector along the x-axis.
 	 */
 	public static final Vector3 I = new Vector3(1.0, 0.0, 0.0);
 
 	/**
 	 * The unit vector along the y-axis.
 	 */
 	public static final Vector3 J = new Vector3(0.0, 1.0, 0.0);
 
 	/**
 	 * The unit vector along the z-axis.
 	 */
 	public static final Vector3 K = new Vector3(0.0, 0.0, 1.0);
 
 	/**
 	 * The unit vector along the negative x-axis.
 	 */
 	public static final Vector3 NEGATIVE_I = new Vector3(-1.0, 0.0, 0.0);
 
 	/**
 	 * The unit vector along the negative y-axis.
 	 */
 	public static final Vector3 NEGATIVE_J = new Vector3(0.0, -1.0, 0.0);
 
 	/**
 	 * The unit vector along the negative z-axis.
 	 */
 	public static final Vector3 NEGATIVE_K = new Vector3(0.0, 0.0, -1.0);
 
 	/**
 	 * The lengths of the vector along each axis.
 	 */
 	private final double x, y, z;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 6028310806663933497L;
 
 }
