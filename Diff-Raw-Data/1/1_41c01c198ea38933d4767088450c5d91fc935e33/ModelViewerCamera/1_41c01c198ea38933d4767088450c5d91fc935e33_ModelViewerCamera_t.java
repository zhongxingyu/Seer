 package de.tum.in.jrealityplugin.jogl;
 
 import org.apache.commons.math.geometry.Rotation;
 import org.apache.commons.math.geometry.RotationOrder;
 import org.apache.commons.math.geometry.Vector3D;
 import org.apache.commons.math.linear.MatrixUtils;
 import org.apache.commons.math.linear.RealMatrix;
 
 public class ModelViewerCamera {
 	private static final double ROTATE_SENSITIVITY = 0.01;
 	
 	private Rotation rotation;
 	private Vector3D position;
 	private Vector3D lookAt;
 	private Vector3D up;
 	private RealMatrix transform;
 	
 	private double zNear, zFar;
 	private double fieldOfView; // In degrees
 	private double aspectRatio;
 	private RealMatrix perspectiveTransform;	
 	
 	public ModelViewerCamera() {
 		rotation = Rotation.IDENTITY;
 		position = new Vector3D(0, 0, 1);
 		lookAt = new Vector3D(0, 0, 0);
 		up = new Vector3D(0, 1, 0);
 		transform = MatrixUtils.createRealMatrix(4, 4);
 		updateTransform();
 		
 		zNear = 0.1;
 		zFar = 100.0;
 		fieldOfView = 45.0;
 		aspectRatio = 4.0/3.0;
 		perspectiveTransform = MatrixUtils.createRealMatrix(4, 4);
 		updatePerspectiveTransform();		
 	}
 	
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
 	}
 
 	protected void updateTransform() {
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
 	
 	public void lookAt(Vector3D position, Vector3D lookAt, Vector3D up) {
 		this.position = position;
 		this.up = up;
 		this.lookAt = lookAt;
 		updateTransform();
 	}
 
 	public RealMatrix getTransform() {
 		return transform;
 	}
 	
 	public void setPerspective(double fieldOfView, double aspect, double zNear, double zFar) {
 		this.fieldOfView = fieldOfView;
 		this.aspectRatio = aspect;
 		this.zNear = zNear;
 		this.zFar = zFar;
 		updatePerspectiveTransform();
 	}
 	
 	public RealMatrix getPerspectiveTransform() {
 		return perspectiveTransform;
 	}
 	
 	public Rotation getRotation() {
 		return rotation;
 	}
 
 	public void setRotation(Rotation rotation) {
 		this.rotation = rotation;
 		updateTransform();
 	}
 
 	public Vector3D getPosition() {
 		return position;
 	}
 
 	public void setPosition(Vector3D position) {
 		this.position = position;
 		updateTransform();
 	}
 
 	public Vector3D getLookAt() {
 		return lookAt;
 	}
 
 	public void setLookAt(Vector3D lookAt) {
 		this.lookAt = lookAt;
 		updateTransform();
 	}
 
 	public Vector3D getUp() {
 		return up;
 	}
 
 	public void setUp(Vector3D up) {
 		this.up = up;
 		updateTransform();
 	}
 
 	public void mouseDragged(double dx, double dy) {
 		rotation = new Rotation(RotationOrder.XYZ, dy * ROTATE_SENSITIVITY, dx
 				* ROTATE_SENSITIVITY, 0).applyTo(rotation);
 
 		updateTransform();
 	}
 
 	public void mouseWheelMoved(int wheelRotation) {
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
 }
