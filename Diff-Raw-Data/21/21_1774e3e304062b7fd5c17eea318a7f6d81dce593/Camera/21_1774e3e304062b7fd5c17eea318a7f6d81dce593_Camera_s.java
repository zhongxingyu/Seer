 package org.tavatar.tavimator;
 
 import android.content.Context;
 import android.opengl.Matrix;
 
 /**
  * This class manages the camera's view matrix based on user input. It does not manage the perspective matrix
  * @author tapple
  *
  * Camera is on a rig as follows:
  * origin: The point the camera rotates around
  * offset: a vector from the origin to the ideal camera, in camera-local coordinates
  * transitionRail: a vector the camera animates along when changing from one origin to another. It runs from the ideal camera to the actual camera
  * 
  * baseOrientation: the orientation before the device orientation is taken into account. This is animated inertially in response to swipes
  * deviceOrientation: the current orientation of the phone
  * baseDeviceOrientation: the orientation of the phone at which the camera is at its base orientation
  */
 
 public class Camera {
 
 	/**
 	 * The computed view matrix
 	 */
 	private float[] viewMatrix = new float[16];
 
 	private float[] temporaryMatrix = new float[16];
 	
 	/**
 	 * The point about which the camera rotates
 	 */
 	float originX;
 	float originY;
 	float originZ;
 	
 	/**
 	 * Vector from the origin to the ideal camera, in camera orientation
 	 */
 	private float[] offset = new float[4];
 	
 	/**
 	 * Trackball that keeps track of orientation set by touch
 	 */
 	TwoFingerTrackball trackball;
 	
 	/**
 	 * Gyroscope that keeps track of orientation set by device orientation
 	 */
 	Gyroscope gyroscope;
 	
 	public Camera(Context context) {
 		trackball = new TwoFingerTrackball(context);
 		gyroscope = new Gyroscope(context);
 		// Start out facing and rotating about the origin, pointing in the default OpenGL direction of z, with y being up and x right
 		initializeCamera(
 				0.0f, 0.0f, 1.0f,
 				0.0f, 0.0f, 0.0f,
 				0.0f, 1.0f, 0.0f,
 				0.0f, 0.0f, 0.0f);
 	}
 	
 	public void initializeCamera(
 			float eyeX, float eyeY, float eyeZ, 
 			float lookX, float lookY, float lookZ,
 			float upX, float upY, float upZ,
 			float originX, float originY, float originZ) {
 		
		this.originX = originX;
		this.originY = originY;
		this.originZ = originZ;
 		
		this.offset[0] = eyeX - originX;
		this.offset[1] = eyeY - originY;
		this.offset[2] = eyeZ - originZ;
 		this.offset[3] = 1.0f;
 		
 		trackball.setLookAt(eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
		Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
 		Matrix.multiplyMV(offset, 0, trackball.getOrientation(), 0, offset, 0);
 	}
 	
 	public void onResume() {
 		gyroscope.onResume();
 	}
 
 	public void onPause() {
 		gyroscope.onPause();
 	}	
 
 	public void updateViewMatrix() {
 		trackball.updateOrientation();
 		gyroscope.updateOrientation();
 		Matrix.transposeM(trackball.getCameraToTrackballOrientation(), 0, gyroscope.getOrientation(), 0);
 
 		Matrix.setIdentityM(viewMatrix, 0);
		Matrix.translateM(viewMatrix, 0, originX, originY, originZ);
 		Matrix.multiplyMM(temporaryMatrix, 0, viewMatrix, 0, gyroscope.getOrientation(), 0);
 		Matrix.multiplyMM(viewMatrix, 0, temporaryMatrix, 0, trackball.getOrientation(), 0);
		Matrix.translateM(viewMatrix, 0, offset[0], offset[1], offset[2]);
 	}
 	
 	// returns the view matrix. Copy it if you intend to pass it around; this one might change
 	public float[] getViewMatrix() {
 		return viewMatrix;
 	}
 	
 	public TwoFingerTrackball getTrackball() {
 		return trackball;
 	}
 	
 	public Gyroscope getGyroscope() {
 		return gyroscope;
 	}
 }
