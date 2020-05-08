 package org.jtrace.cameras;
 
 import org.jtrace.Jay;
 import org.jtrace.primitives.Point3D;
 import org.jtrace.primitives.Vector3D;
 
 public class PinHoleCamera extends Camera {
 
 	public PinHoleCamera(final Point3D eye, final Point3D lookAt, final Vector3D up) {
 		this.eye = eye;
 		this.lookAt = lookAt;
 		this.up = up;
 
 		computeUVW();
 	}
 
 
 	@Override
	public Jay createJay(final int r, final int c, final int vres, final int hres) {
		final double viewPlaneX = 0.5*(c - hres/2 + 0.5);
		final double viewPlaneY = 0.5*(r - vres/2 + 0.5);
 
 		final Vector3D dU = u.multiply(viewPlaneX);
 		final Vector3D dV = v.multiply(viewPlaneY);
 		final Vector3D dW = w.multiply(viewPlaneDistance);
 
 		final Vector3D jayDirection = dU.add(dV).subtract(dW).normal();
 
 		return new Jay(eye, jayDirection);
 	}
 }
