 package org.saseros.cleanstorms;
 
 import lejos.nxt.Button;
 import lejos.nxt.Motor;
 
 import org.saseros.cleanstorms.Sensor;
 
 public class Robot {
 
 	private Sensor sensor;
 	private int turn = -15;
	private int recursiveDepth = 0;
 
 	public Robot(Sensor sensor) {
 		this.sensor = sensor;
 	}
 
 	public boolean isPathClear() {
		recursiveDepth++;
		if (recursiveDepth >= 246) {
			atEndOfPathIsDetected();
			System.out.println("Path not clear");
			return false;
		}
 		if (sensor.isObstacleDetected()) {
 			return isPathClear();
 		}
 		Motor.B.rotate(turn);
 		System.out.println("Surveying");
		if (Motor.B.getTachoCount() <= -45) {
 			turn = +15;
 			return isPathClear();
		} else if (Motor.B.getTachoCount() >= 45) {
 			System.out.println("Done Surveying!");
			atEndOfPathIsDetected();
 			return true;
 		} else {
 			return isPathClear();
 		}
 	}
 
	private void atEndOfPathIsDetected() {
		moveUltrasonicSensorToDefaultPosition();
		recursiveDepth = 0;
		turn = -15;
	}

 	private void moveUltrasonicSensorToDefaultPosition() {
 		Motor.B.rotateTo(0);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Sensor sensor = new Sensor(true, 20, 30);
 		Robot robot = new Robot(sensor);
		System.out.println("Method returns: " + robot.isPathClear());
		//robot.isPathClear();
 		Button.waitForAnyPress();
 		// while(!Button.ESCAPE.isDown()){
 
 		// }
 
 	}
 
 }
