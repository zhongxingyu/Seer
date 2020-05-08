 package org.saseros.cleanstorms;
 
 import lejos.nxt.Button;
 import lejos.nxt.Motor;
 
 import org.saseros.cleanstorms.Sensor;
 
 public class Robot {
 
 	private Sensor sensor;
 	private int turn = -15;
 
 	public Robot(Sensor sensor) {
 		this.sensor = sensor;
 	}
 
 	public boolean isPathClear() {
 		if (sensor.isObstacleDetected()) {
 			return isPathClear();
 		}
 		Motor.B.rotate(turn);
 		System.out.println("Surveying");
		if (Motor.B.getTachoCount() <= -70) {
 			turn = +15;
 			return isPathClear();
		} else if (Motor.B.getTachoCount() >= 70) {
 			System.out.println("Done Surveying!");
			turn = -1;
			moveUltrasonicSensorToDefaultPosition();
 			return true;
 		} else {
 			return isPathClear();
 		}
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
		robot.isPathClear();
		System.out.println("Test complete");
 		Button.waitForAnyPress();
 		// while(!Button.ESCAPE.isDown()){
 
 		// }
 
 	}
 
 }
