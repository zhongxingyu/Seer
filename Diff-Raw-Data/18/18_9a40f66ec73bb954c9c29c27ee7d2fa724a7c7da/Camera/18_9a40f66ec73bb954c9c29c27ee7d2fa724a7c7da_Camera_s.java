 package pong.view;
 
 import static pong.model.Const.*;
 
 import java.util.List;
 
 
 import pong.control.GameEngine;
 import pong.model.Ball;
 import pong.model.GameItem;
 import pong.model.Paddle;
 
 public class Camera {
 	// Contains the position of the camera. (x,y,z)
 	private static float[] position = new float[3];
 
 	// Contains the point which the camera is looking at. (x,y,z)
 	private static float[] lookPoint = new float[3];
 
 	// Contains the Up vector for the camera. (This will probably never be changed)
 	private static float[] upVector = new float[3];
 
 	// The mode of the camera.
 	private static int mode = CAM_STATIC;
 
 	private static GameEngine ge;
 
	private static float rotatedY;

 	public Camera(GameEngine engine) {
 
 		// Set standard startinglocation for the Camera
 		position[0] = CAMERA_POSITION_X;
 		position[1] = CAMERA_POSITION_Y;
 		position[2] = CAMERA_POSITION_Z;
 
 		// Set standard point to look at
 		lookPoint[0] = CAMERA_LOOK_AT_X;
 		lookPoint[1] = CAMERA_LOOK_AT_Y;
 		lookPoint[2] = CAMERA_LOOK_AT_Z;
 
 		// Set standard Up vector.
 		upVector[0] = 0.0f;
 		upVector[1] = 1.0f;
 		upVector[2] = 0.0f;
 
 		ge = engine;
 	}
 
 	public static void tick() {
 		if (ge.getGameState() == IN_GAME) {
 			if (mode == CAM_STATIC) {
 				staticCam();
 			} else if (mode == CAM_LOOKAT_BALLS) {
 				lookAtBalls();
 			} else if (mode == CAM_FOLLOW_BALLS) {
 				followBalls();
 			} else if (mode == CAM_PADDLE1) {
 				followPaddle1();
 			} else if (mode == CAM_PADDLE2) {
 				followPaddle2();
 			}
 		}
 	}
 
 	private static void staticCam() {
 		position[0] = CAMERA_POSITION_X;
 		position[1] = CAMERA_POSITION_Y;
 		position[2] = CAMERA_IN_GAME_POSITION_Z;
 		resetLookat();
 		resetUpVector();
 	}
 
 	private static void lookAtBalls() {
 		resetPosition();
 		resetUpVector();
 		float xAvg=0, yAvg=0, zAvg=0;
 		int count = 0;
 		
 		List<GameItem> items = ge.getGameItems();
 		
 		for(GameItem item : items){
 			xAvg+=item.getxPos();
 			yAvg+=item.getyPos();
 			zAvg+=item.getzPos();
 			count++;
 		}
 		xAvg = xAvg/count;
 		yAvg = yAvg/count;
 		zAvg = zAvg/count;
 		
 		lookPoint[0] = xAvg * CAM_SENSITIVITY;
 		lookPoint[1] = yAvg * CAM_SENSITIVITY;
 		lookPoint[2] = zAvg * CAM_SENSITIVITY;
 	}
 
 	private static void followBalls() {
 		resetUpVector();
 
 		float xAvg=0, yAvg=0, zAvg=0;
 		int count = 0;
 		
 		List<GameItem> items = ge.getGameItems();
 		
 		for(GameItem item : items){
 			xAvg+=item.getxPos();
 			yAvg+=item.getyPos();
 			zAvg+=item.getzPos();
 			count++;
 		}
 		xAvg = xAvg/count;
 		yAvg = yAvg/count;
 		zAvg = zAvg/count;
 		
 		
 		lookPoint[0] = xAvg * CAM_SENSITIVITY;
 		lookPoint[1] = yAvg * CAM_SENSITIVITY;
 		lookPoint[2] = zAvg * CAM_SENSITIVITY;
 
 		position[0] = xAvg * CAM_SENSITIVITY;
 		position[1] = yAvg * CAM_SENSITIVITY;
 		position[2] = CAMERA_IN_GAME_POSITION_Z;
 	}
 
 	private static void followPaddle1() {
 		resetUpVector();
 
 		Paddle pad = ge.getPlayer1().getPaddle();
 		lookPoint[0] = pad.getxPos();
 		lookPoint[1] = pad.getyPos();
 		lookPoint[2] = pad.getzPos();
 
 		position[0] = pad.getxPos();
 		position[1] = pad.getyPos() - 40;
 		position[2] = pad.getzPos() + 20;
 
 	}
 
 	private static void followPaddle2() {
 		Paddle pad = ge.getPlayer2().getPaddle();
 		lookPoint[0] = pad.getxPos();
 		lookPoint[1] = pad.getyPos();
 		lookPoint[2] = pad.getzPos();
 
 		position[0] = pad.getxPos();
 		position[1] = pad.getyPos() + 40;
 		position[2] = pad.getzPos() + 20;
 
 		upVector[1] = -1;
 	}
 
 	/** Makes a smooth increment or decrement of zPos of the camera. */
 	public static void smoothZoom(float zTarget) {
 		float startPos = position[2];
 		float dist;
 		float distTraveled = 0;
 		float acc = 0.0001f;
 		float speed = 0;
 		boolean moveInward;
 		dist = Math.abs(startPos - zTarget);
 		//if already at target, return
 		if(dist == 0){
 			return;
 		}
 			
 		if (zTarget < startPos) {
 			moveInward = true;
 		} else {
 			moveInward = false;
 		}
 
 		try {
 			while (distTraveled <= dist) {
 				// Increment or decrement zPos based on if you move inwards (zPos decreasing) or outwards (zPos
 				// increasing)
 				if (moveInward) {
 					position[2] -= speed;
 					distTraveled = startPos - position[2];
 				} else {
 					position[2] += speed;
 					distTraveled = position[2] - startPos;
 				}
 
 				// Accceleration (first 1/3 of the distance)
 				if (distTraveled <= (dist / 3)) {
 					speed = speed + acc;
 				}
 				// Constant speed (from 1/3 to 2/3 of the distance)
 				else if (distTraveled <= 2 * (dist / 3)) {
 					// Do nothing
 				}
 
 				// Deaccelerate speed (from 2/3 to the end)
 				else {
 					speed = speed - acc;
 
 					// The camera has stopped. This is here to stop the camera when zooming out.
 					if (speed <= 0)
 						break;
 				}
 				Thread.sleep(1);
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		// The position is very(!) close to target but not exact.
 		// Sets the position to be exactly the target Z.
 		position[2] = zTarget;
 	}
 
 	// Set standard location for the Camera
 	public static void resetPosition() {
 		position[0] = CAMERA_POSITION_X;
 		position[1] = CAMERA_POSITION_Y;
 		position[2] = CAMERA_IN_GAME_POSITION_Z;
 	}
 
 	// Set standard point to look at
 	public static void resetLookat() {
 		lookPoint[0] = CAMERA_LOOK_AT_X;
 		lookPoint[1] = CAMERA_LOOK_AT_Y;
 		lookPoint[2] = CAMERA_LOOK_AT_Z;
 	}
 
 	// Set standard up-vector
 	public static void resetUpVector() {
 		upVector[0] = CAMERA_UPVECTOR_X;
 		upVector[1] = CAMERA_UPVECTOR_Y;
 		upVector[2] = CAMERA_UPVECTOR_Z;
 	}
 
 	public static void setMode(int camMode) {
 		mode = camMode;
 	}
 	public static int getMode() {
 		return mode;
 	}
 
 	public static float[] getPosition() {
 		return position;
 	}
 
 	public static float[] getLookPoint() {
 		return lookPoint;
 	}
 
 	public static float[] getUpVector() {
 		return upVector;
 	}
 }
