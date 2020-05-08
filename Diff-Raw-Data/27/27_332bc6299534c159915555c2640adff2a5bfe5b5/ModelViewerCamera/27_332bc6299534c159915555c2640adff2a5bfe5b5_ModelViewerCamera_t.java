 package de.tum.in.cindy3dplugin.jogl;
 
 import org.apache.commons.math.geometry.Rotation;
 import org.apache.commons.math.geometry.RotationOrder;
 import org.apache.commons.math.geometry.Vector3D;
 import org.apache.commons.math.linear.LUDecompositionImpl;
 import org.apache.commons.math.linear.MatrixUtils;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.RealVector;
 
 /**
  * Camera entity.
  * 
  * The <code>ModelViewerCamera</code> is a camera orbiting around a look-at
  * point. It provides methods for placing the camera, adjusting the perspective
  * projection properties, and the retrieval of homogeneous matrices for
  * rendering.
  */
 public class ModelViewerCamera {
 	/**
 	 * Rotation angle per moved pixel (in radians)
 	 */
 	private static final double ROTATE_SENSITIVITY = 0.01;
 	/**
 	 * Panning distance per moved pixel
 	 */
 	private static final double PAN_SENSITIVITY = 0.05;
 	
 	/**
 	 * Camera orientation
 	 */
 	private Rotation rotation;
 	/**
 	 * Camera position
 	 */
 	private Vector3D position;
 	/**
 	 * Look at vector
 	 */
 	private Vector3D lookAt;
 	/**
 	 * Up vector
 	 */
 	private Vector3D up;
 	/**
 	 * View transformation matrix
 	 */
 	private RealMatrix transform;
 	
 	/**
 	 * Distance of near clipping plane
 	 */
 	private double zNear;
 	/**
 	 * Distance of far clipping plane
 	 */
 	private double zFar;
 	
 	/**
 	 * Field of view in degrees
 	 */
 	private double fieldOfView;
 	/**
 	 * Aspect ratio of the viewport (width / height)
 	 */
 	private double aspectRatio;
 	/**
 	 * Perspective projection matrix
 	 */
 	private RealMatrix perspectiveTransform;
 	
 	/**
 	 * Factor for world space error computation
 	 * 
 	 * @see #getWorldSpaceError(double, double)
 	 */
 	private double lodFactor;
 	
 	/**
 	 * View frustum clipping planes, in view space
 	 */
 	private Plane[] clippingPlanes = new Plane[6];
 	
 	/**
 	 * Constructs a camera with default parameters.
 	 * 
 	 * The instance has the same internal state as if set by 
 	 * <pre>
 	 *   camera.lookAt(Vector3D.PLUS_K, Vector3D.ZERO, Vector3D.PLUS_J);
 	 *   camera.setPerspective(45.0, 640, 480, 0.1, 100.0);
 	 * </pre>
 	 */
 	public ModelViewerCamera() {
 		transform = MatrixUtils.createRealMatrix(4, 4);
 		perspectiveTransform = MatrixUtils.createRealMatrix(4, 4);
 		rotation = Rotation.IDENTITY;
 		
 		lookAt(Vector3D.PLUS_K, Vector3D.ZERO, Vector3D.PLUS_J);
 		setPerspective(45.0, 640, 480, 0.1, 100.0);
 	}
 	
 	/**
 	 * Calculates the perspective projection matrix from the camera parameters.
 	 */
 	private void updatePerspectiveTransform() {
 		double f = 1.0/Math.tan(Math.toRadians(fieldOfView)*0.5);
 		double nearMinusFar = zNear - zFar;
 		perspectiveTransform.setEntry(0, 0, f/aspectRatio);
 		perspectiveTransform.setEntry(1, 1, f);
 		perspectiveTransform.setEntry(2, 2, (zFar + zNear)
 				/ nearMinusFar);
 		perspectiveTransform.setEntry(2, 3, 2 * zFar * zNear
 				/ nearMinusFar);
 		perspectiveTransform.setEntry(3, 2, -1);
 		
 		buildClippingPlanes();
 	}
 	
 	/**
 	 * Calculates the view transformation matrix from the camera parameters.
 	 */
 	private void updateTransform() {
 		// Lookat
 		Vector3D forward = lookAt.subtract(position);
 		Rotation lookAtRotation = new Rotation(up, forward, Vector3D.PLUS_J,
 				Vector3D.MINUS_K);
 		RealMatrix lookAtMatrix = MatrixUtils.createRealIdentityMatrix(4);
 		lookAtMatrix.setSubMatrix(lookAtRotation.getMatrix(), 0, 0);
 		lookAtMatrix.setColumn(
 				3,
 				new double[] { -position.getX(), -position.getY(),
 						-position.getZ(), 1 });
 		
 		// Model rotation
 		RealMatrix modelRotationMatrix = MatrixUtils
 				.createRealIdentityMatrix(4);
 		modelRotationMatrix.setSubMatrix(rotation.getMatrix(), 0, 0);
 		
 		transform = lookAtMatrix.multiply(modelRotationMatrix);
 	}
 	
 	/**
 	 * Calculates the frustum's clipping planes.
 	 */
 	private void buildClippingPlanes() {
 		// Get the inverse of the projection matrix
 		RealMatrix invProjection = new LUDecompositionImpl(perspectiveTransform)
 				.getSolver().getInverse();
 		
 		// Coordinates of the normalized view frustum
 		RealVector[] f = new RealVector[] {
 				MatrixUtils.createRealVector(new double[] { -1, -1,  1, 1 }),
 				MatrixUtils.createRealVector(new double[] { -1, -1, -1, 1 }),
 				MatrixUtils.createRealVector(new double[] { -1,  1, -1, 1 }),
 				MatrixUtils.createRealVector(new double[] {  1,  1, -1, 1 }),
 				MatrixUtils.createRealVector(new double[] {  1,  1,  1, 1 }),
 				MatrixUtils.createRealVector(new double[] {  1, -1,  1, 1 })
 		};
 	
 		// Transform view frustum into camera space
 		for (int i = 0; i < 6; ++i) {
 			f[i] = invProjection.operate(f[i]);
 			f[i].mapDivideToSelf(f[i].getEntry(3));
 		}
 
 		Vector3D[] frustumVertices = new Vector3D[6];
 		for (int i = 0; i < 6; ++i) {
 			frustumVertices[i] = new Vector3D(f[i].getEntry(0),
 					f[i].getEntry(1), f[i].getEntry(2));
 		}
 
 		for (int i = 0; i < 6; ++i) {
 			Vector3D v1 = frustumVertices[(i + 1) % 6]
 					.subtract(frustumVertices[i]);
 			Vector3D v2 = frustumVertices[(i + 2) % 6]
 					.subtract(frustumVertices[i]);
 
 			Vector3D normal = Vector3D.crossProduct(v1, v2);
 			if (i % 2 == 1) {
 				normal = normal.negate();
 			}
 			
 			clippingPlanes[i] = new Plane(normal.normalize(),
 					frustumVertices[i]);
 		}
 	}
 
 	/**
 	 * Sets the camera's view parameters.
 	 * 
 	 * @param position
 	 *            camera position
 	 * @param lookAt
 	 *            point the camera is looking at
 	 * @param up
 	 *            up vector
 	 */
 	public void lookAt(Vector3D position, Vector3D lookAt, Vector3D up) {
 		this.position = position;
 		this.up = up;
 		this.lookAt = lookAt;
 		updateTransform();
 	}
 
 	/**
 	 * Returns the homogeneous 4x4 view transform matrix.
 	 * 
	 * This matrix transforms coordinates in global world space to view space,
 	 * where the camera is in the origin, looking along the negative z axis and
 	 * having the positive y axis as up vector.
 	 * 
 	 * @return view transform matrix
 	 */
 	public RealMatrix getTransform() {
 		return transform;
 	}
 	
 	/**
 	 * @return view frustum clipping planes, in view space
 	 */
 	public Plane[] getClippingPlanes() {
 		return clippingPlanes;
 	}
 
 	/**
 	 * Sets the camera's perspective projection parameters.
 	 * 
 	 * @param fieldOfView
 	 *            horizontal field of view, in degrees
 	 * @param width
 	 *            width of viewport
 	 * @param height
 	 *            height of viewport
 	 * @param zNear
 	 *            distance of near clipping plane
 	 * @param zFar
 	 *            distance of far clipping plane
 	 */
 	public void setPerspective(double fieldOfView, int width, int height,
 			double zNear, double zFar) {
 		if (height <= 0)
 			height = 1;
 		this.aspectRatio = (double) width / height;
 		this.fieldOfView = fieldOfView;
 		this.zNear = zNear;
 		this.zFar = zFar;
 		updatePerspectiveTransform();
 
 		lodFactor = 2.0
 				* Math.tan(0.5 * Math.toRadians(this.fieldOfView) / aspectRatio)
 				/ ((double) height);
 	}
 	
 	/**
 	 * Returns the homogeneous 4x4 perspective projection matrix.
 	 * 
 	 * @return perspective projection matrix
 	 */
 	public RealMatrix getPerspectiveTransform() {
 		return perspectiveTransform;
 	}
 	
 	/**
 	 * @return camera orientation
 	 */
 	public Rotation getRotation() {
 		return rotation;
 	}
 
 	/**
 	 * @param rotation
 	 *            new camera orientation
 	 */
 	public void setRotation(Rotation rotation) {
 		this.rotation = rotation;
 		updateTransform();
 	}
 
 	/**
 	 * @return camera position
 	 */
 	public Vector3D getPosition() {
 		return position;
 	}
 
 	/**
 	 * @param position
 	 *            new camera position
 	 */
 	public void setPosition(Vector3D position) {
 		this.position = position;
 		updateTransform();
 	}
 
 	/**
 	 * @return look at vector
 	 */
 	public Vector3D getLookAt() {
 		return lookAt;
 	}
 
 	/**
 	 * @param lookAt
 	 *            new look at vector
 	 */
 	public void setLookAt(Vector3D lookAt) {
 		this.lookAt = lookAt;
 		updateTransform();
 	}
 
 	/**
 	 * @return up vector
 	 */
 	public Vector3D getUp() {
 		return up;
 	}
 
 	/**
 	 * @param up
 	 *            new up vector
 	 */
 	public void setUp(Vector3D up) {
 		this.up = up;
 		updateTransform();
 	}
 	
 	/**
 	 * @return field of view in degrees
 	 */
 	public double getFieldOfView() {
 		return fieldOfView;
 	}
 	
 	/**
 	 * @return aspect ratio of the viewport (width / height)
 	 */
 	public double getAspectRatio() {
 		return aspectRatio;
 	}
 	
 	/**
 	 * @return distance of near clipping plane
 	 */
 	public double getZNear() {
 		return zNear;
 	}
 	
 	/**
 	 * @return distance of far clipping plane
 	 */
 	public double getZFar() {
 		return zFar;
 	}
 
 	/**
 	 * Rotates the camera around the look-at point due to mouse movement.
 	 * 
 	 * @param dx
 	 *            x component of mouse movement delta
 	 * @param dy
 	 *            y component of mouse movement delta
 	 */
 	public void mouseRotate(double dx, double dy) {
 		rotation = new Rotation(RotationOrder.XYZ, dy * ROTATE_SENSITIVITY, dx
 				* ROTATE_SENSITIVITY, 0).applyTo(rotation);
 
 		updateTransform();
 	}
 
 	/**
 	 * Pans the camera due to mouse movement.
 	 * 
 	 * @param dx
 	 *            x component of mouse movement delta
 	 * @param dy
 	 *            y component of mouse movement delta
 	 */
 	public void mousePan(double dx, double dy) {
 		Vector3D forward = lookAt.subtract(position).normalize();
 		Vector3D right = Vector3D.crossProduct(forward, up).normalize();
 
 		Vector3D movement = new Vector3D(0, 0, 0).add(-dx * PAN_SENSITIVITY,
 				right).add(dy * PAN_SENSITIVITY, up);
 
 		position = position.add(movement);
 		lookAt = lookAt.add(movement);
 
 		updateTransform();
 	}
 
 	/**
 	 * Moves the camera back or forth due to mouse wheel movement.
 	 * 
 	 * @param wheelRotation
 	 *            number of clicks the mouse wheel was rotated
 	 */
 	public void mouseWheel(int wheelRotation) {
 		if (wheelRotation == 0) return;
 		Vector3D lookAtToPosition = position.subtract(lookAt);
 		Vector3D newPosition;
 		if (wheelRotation > 0) {
 			newPosition = lookAt.add(1.1, lookAtToPosition);
 		} else {
 			newPosition = lookAt.add(1.0/1.1, lookAtToPosition);
 		}
 		setPosition(newPosition);
 	}
 
 	/**
 	 * Returns the allowed world space error.
 	 * 
 	 * @param screenSpaceError
 	 *            maximum screen space error, in pixels
 	 * @param cameraSpaceZ
 	 *            z component of camera space coordinate. Note that this value
 	 *            is negative for objects in front of the camera.
 	 * @return maximum world space distance of two points at
 	 *         <code>cameraSpaceZ</code>, such that their screen space distance
 	 *         is at most <code>screenSpaceError</code>
 	 */
 	public double getWorldSpaceError(double screenSpaceError,
 			double cameraSpaceZ) {
 		return lodFactor * screenSpaceError * -cameraSpaceZ;
 	}
 }
