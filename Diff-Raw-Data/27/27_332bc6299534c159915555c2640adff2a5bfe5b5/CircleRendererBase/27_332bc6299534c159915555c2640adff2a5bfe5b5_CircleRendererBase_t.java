 package de.tum.in.cindy3dplugin.jogl.primitives.renderers;
 
 import org.apache.commons.math.geometry.Rotation;
 import org.apache.commons.math.geometry.Vector3D;
 import org.apache.commons.math.linear.MatrixUtils;
 import org.apache.commons.math.linear.RealMatrix;
 
 import de.tum.in.cindy3dplugin.jogl.Util;
 import de.tum.in.cindy3dplugin.jogl.primitives.Circle;
 
 /**
  * Base class for different kinds of circle renderers.
  * 
  * This intermediate class contains methods needed by more than one specialized
  * circle renderer.
  */
 public abstract class CircleRendererBase extends PrimitiveRenderer<Circle> {
 	/**
 	 * Builds the transformation matrix transforming a standard circle into the
 	 * specified circle.
 	 * 
 	 * The standard circle has the origin (0, 0, 0) and radius 1 and lies in the
 	 * xy-plane to that its normal is (0, 0, 1). A transformation matrix is
 	 * build that transforms this standard circle into a circle with origin,
 	 * radius and orientation specified in <code>circle</circle>.
 	 * 
 	 * @param circle
 	 *            destination circle
	 * @return row-major transformation matrix
 	 */
 	protected static float[] buildTransform(Circle circle) {
 		RealMatrix transform = MatrixUtils.createRealIdentityMatrix(4);
 		
 		// Translation
 		transform.setColumn(3, new double[] { circle.getCenter().getX(),
 				circle.getCenter().getY(), circle.getCenter().getZ(), 1 });
 
 		// Rotation
 		Rotation rotation = new Rotation(Vector3D.PLUS_K, circle.getNormal());
 		RealMatrix rotationMatrix = MatrixUtils.createRealIdentityMatrix(4);
 		rotationMatrix.setSubMatrix(rotation.getMatrix(), 0, 0);
 
 		// Scaling
 		RealMatrix scaleMatrix = MatrixUtils
 				.createRealDiagonalMatrix(new double[] { circle.getRadius(),
 						circle.getRadius(), circle.getRadius(), 1 });
 
 		transform = transform.multiply(rotationMatrix).multiply(scaleMatrix);
 
 		return Util.matrixToFloatArray(transform);
 	}
 }
