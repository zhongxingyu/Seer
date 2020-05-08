 package p0_Race;
import lejos.nxt.TouchSensor;
 import lejos.robotics.subsumption.Behavior;
 import general.Calibration;
 import general.Movement;
 import general.SensorCache;
 
 
 public class P0_TurnLeft implements Behavior {
 	
 	boolean suppressed;
 	Movement movement = Movement.getInstance();
 
 	public P0_TurnLeft(int speed, int rotationSpeed) {
 		movement.setSpeed(speed);
 		movement.setRotationSpeed(rotationSpeed);
 	}
 	
 	public boolean takeControl() {
 		return (SensorCache.getInstance().bumperPressed && SensorCache.getInstance().normalizedLightValue < Calibration.THRESHOLD);
 	}
 	
 	public void action() {
 		suppressed = false;
 		Constants.alreadyStopped = false;
 		movement.setTravelSpeed(100);
 
 		movement.arcBackward(-40);
 		//movement.steerBackward(-40);
 		
 		movement.turn_left(90);		
 	}
 	
 	public void suppress() {
 		suppressed = true;
 	}
 }
