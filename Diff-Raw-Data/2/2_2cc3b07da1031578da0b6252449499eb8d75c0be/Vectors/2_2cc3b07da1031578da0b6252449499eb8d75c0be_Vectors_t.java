 package com.oschrenk.humangeo.cs;
 
 import com.oschrenk.humangeo.core.Cartesian2dCoordinate;
 import com.oschrenk.humangeo.core.Cartesian3dCoordinate;
 
 /**
  * 
  * @author Oliver Schrenk <oliver.schrenk@q2web.de>
  */
 public class Vectors {
 
 	/**
 	 * Length of vector
 	 * 
 	 * @param cartesianCoordinate
 	 *            the cartesian coordinate
 	 * @return the length of the vector
 	 */
 	public static final double length(
 			final Cartesian2dCoordinate cartesianCoordinate) {
 		return Math.sqrt(cartesianCoordinate.getX()
 				* cartesianCoordinate.getX() + cartesianCoordinate.getY()
 				* cartesianCoordinate.getY());
 	}
 
 	/**
 	 * Length of vector
 	 * 
 	 * @param cartesianCoordinate
 	 *            the cartesian coordinate
 	 * @return length of the vector
 	 */
 	public static final double length(
 			final Cartesian3dCoordinate cartesianCoordinate) {
 		return Math.sqrt(cartesianCoordinate.getX()
 				* cartesianCoordinate.getX() + cartesianCoordinate.getY()
 				* cartesianCoordinate.getY() + cartesianCoordinate.getZ()
				* cartesianCoordinate.getZ());
 	}
 
 	/**
 	 * Length of vector with arbitrary dimensions
 	 * 
 	 * @param v
 	 *            the vector v
 	 * @return the length of the vector
 	 */
 	public static final double length(final double[] v) {
 		double distanceSquared = 0;
 		for (final double element : v) {
 			distanceSquared += element * element;
 		}
 		return Math.sqrt(distanceSquared);
 	}
 
 	/**
 	 * a dot b
 	 * 
 	 * @param a
 	 *            the vector a
 	 * @param b
 	 *            the vector a
 	 * @return dot product
 	 */
 	public static final double dot(final Cartesian3dCoordinate a,
 			final Cartesian3dCoordinate b) {
 		return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
 	}
 
 	/**
 	 * a cross b.
 	 * 
 	 * @param a
 	 *            the vector a
 	 * @param b
 	 *            the vector b
 	 * @return the cross product of a and b
 	 */
 	public static final Cartesian3dCoordinate cross(
 			final Cartesian3dCoordinate a, final Cartesian3dCoordinate b) {
 		return new Cartesian3dCoordinate(a.getY() * b.getZ() - a.getZ()
 				* b.getY(), a.getZ() * b.getX() - a.getX() * b.getZ(), a.getX()
 				* b.getY() - a.getY() * b.getX());
 	}
 
 	/**
 	 * c * v.
 	 * 
 	 * @param c
 	 *            the constant c
 	 * @param v
 	 *            the vector v
 	 * @return the cartesian3d coordinate
 	 */
 	public static final Cartesian3dCoordinate mult(final double c,
 			final Cartesian3dCoordinate v) {
 		return new Cartesian3dCoordinate(c * v.getX(), c * v.getY(), c
 				* v.getZ());
 	}
 
 	/**
 	 * v-w
 	 * 
 	 * @param v
 	 *            the v
 	 * @param w
 	 *            the w
 	 * @return the double[]
 	 */
 	public static final Cartesian3dCoordinate minus(
 			final Cartesian3dCoordinate v, final Cartesian3dCoordinate w) {
 		return new Cartesian3dCoordinate(v.getX() - w.getX(), v.getY()
 				- w.getY(), v.getZ() - w.getZ());
 	}
 
 }
