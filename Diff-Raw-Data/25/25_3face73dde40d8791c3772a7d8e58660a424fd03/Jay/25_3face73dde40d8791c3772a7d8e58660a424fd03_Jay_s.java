 package org.jtrace;
 
 import org.jtrace.primitives.Point3D;
 import org.jtrace.primitives.Vector3D;
 
 public class Jay {
	private final Point3D point;
 	private final Vector3D direction;
 
	public Jay(final Point3D point, final Vector3D direction) {
		this.point = point;
 		this.direction = direction;
 	}
 
 	public Point3D getPoint() {
		return point;
 	}
 
 	public Vector3D getDirection() {
 		return direction;
 	}
 
 }
