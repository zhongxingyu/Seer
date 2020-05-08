 package me.ddos.doxel;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Quaternion;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.lwjgl.util.vector.Vector4f;
 
 /**
  * The main class of this application.
  *
  * @author DDoS
  */
 public class DoxelApp {
 	// Constants
 	private static final float MOUSE_SENSITIVITY = 0.01f;
 	private static final float CAMERA_SPEED = 0.4f;
 	// Input
 	private static final Vector2f cameraRotation = new Vector2f();
 
 	/**
	 * The entry point for the application
 	 *
 	 * @param args Unused.
 	 */
 	public static void main(String[] args) {
		new DoxelApp().start();
	}

	private void start() {
 		Doxel.create("Doxel", 1200, 800, 75);
 		Doxel.generateModelMesh(-10, -10, -10, 30, 30, 30);
 		Doxel.createModel();
 		Mouse.setGrabbed(true);
 		while (!Display.isCloseRequested()) {
 			try {
 				processInput();
 				Doxel.logic();
 				Doxel.render();
 				Thread.sleep(50);
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				System.exit(-1);
 			}
 		}
 		Mouse.setGrabbed(false);
 		Doxel.destroy();
 	}
 
	private void processInput() {
		cameraRotation.x -= Mouse.getDY() * MOUSE_SENSITIVITY;
 		cameraRotation.y -= Mouse.getDX() * MOUSE_SENSITIVITY;
 		final Quaternion yaw = new Quaternion();
 		yaw.setFromAxisAngle(new Vector4f(1, 0, 0, cameraRotation.x));
 		final Quaternion pitch = new Quaternion();
 		pitch.setFromAxisAngle(new Vector4f(0, 1, 0, cameraRotation.y));
 		Quaternion.mul(pitch, yaw, Doxel.modelRotation());
 		final Vector3f right = Doxel.cameraRight();
 		final Vector3f up = Doxel.cameraUp();
 		final Vector3f forward = Doxel.cameraForward();
 		final Vector3f position = Doxel.modelPosition();
 		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
 			Vector3f.add(position, scale(forward, CAMERA_SPEED), position);
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
 			Vector3f.add(position, scale(forward, -CAMERA_SPEED), position);
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
 			Vector3f.add(position, scale(right, CAMERA_SPEED), position);
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
 			Vector3f.add(position, scale(right, -CAMERA_SPEED), position);
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 			Vector3f.add(position, scale(up, CAMERA_SPEED), position);
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 			Vector3f.add(position, scale(up, -CAMERA_SPEED), position);
 		}
 		Doxel.lightPosition(position.negate(null));
 	}
 
 	private static Vector3f scale(Vector3f v, float s) {
 		return new Vector3f(v.x * s, v.y * s, v.z * s);
 	}
 }
