 package de.tum.in.cindy3dplugin.jogl;
 
 import org.apache.commons.math.geometry.Vector3D;
 
 /**
  * Plane
  */
 public class Plane {
 	/**
 	 * Plane normal
 	 */
 	private Vector3D normal;
 	/**
 	 * Distance of plane from origin
 	 */
 	private double distance;
 	
 	/**
 	 * Constructs a new plane.
 	 * 
 	 * @param normal
 	 * 			Plane normal
 	 * @param position
	 * 			Coordinates of a point on plain
 	 */
 	public Plane(Vector3D normal, Vector3D position) {
 		this(normal, -Vector3D.dotProduct(normal, position));
 	}
 	
 	/**
 	 * Constructs a new plane.
 	 * 
 	 * @param normal
 	 * 			Plane normal
 	 * @param distance
 	 * 			Distance of plane from origin
 	 */
 	public Plane(Vector3D normal, double distance) {
 		this.normal = normal;
 		this.distance = distance;
 	}
 
 	/**
 	 * Computes the intersection between a ray and the plane.
 	 * 
 	 * @param rayOrigin
 	 * 			Coordinates of the ray origin
 	 * @param rayDirection
 	 * 			Ray direction as a vector
 	 * @return
 	 * 			Distance from origin in given direction to intersection point
 	 */
 	public double intersectRay(Vector3D rayOrigin, Vector3D rayDirection) {
 		double denom = Vector3D.dotProduct(rayDirection, normal);
 		// Ray parallel to plane, so no intersection point is found
 		if (Math.abs(denom) < 10E-8) {
 			return Double.MAX_VALUE;
 		}
 		double lambda = -(Vector3D.dotProduct(rayOrigin, normal) + distance)
 				/ denom;
 		return lambda;
 	}
 
 	/**
 	 * Computes signed distance between a point and the plane.
 	 * 
 	 * @param point
 	 * 			Point coordinates
 	 * @return
 	 * 			Signed distance from point to plane
 	 */
 	public double distance(Vector3D point) {
 		return Vector3D.dotProduct(normal, point) + distance;
 	}
 }
