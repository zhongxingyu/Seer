 package edu.kit.curiosity.behaviors.race;
 
 import lejos.nxt.Button;
 import lejos.nxt.ButtonListener;
 import lejos.nxt.Motor;
 import lejos.robotics.subsumption.Behavior;
 import lejos.robotics.subsumption.CustomArbitrator;
 import edu.kit.curiosity.SensorHeadCalibrate;
 import edu.kit.curiosity.Settings;
 import edu.kit.curiosity.behaviors.MotorAStall;
 import edu.kit.curiosity.behaviors.SensorHeadPosition;
 
 public class RaceMain implements ButtonListener {
 
 
 	public RaceMain() {
 		Button.ESCAPE.addButtonListener(this);
 	}
 
 	public static void main(String[] args) throws Exception {
 		new RaceMain();
 		
 		
 		// Race
 		Settings.PILOT.setTravelSpeed(Settings.PILOT.getMaxTravelSpeed() * 0.70);
 		Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() / 4);
 		Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 		new SensorHeadCalibrate();
 		Settings.motorAAngle = 0;
 		
 		
		Behavior b1 = new RaceFollowWall(14);
 		Behavior b2 = new Race();
 		Behavior b3 = new RaceDrive();
 		//Behavior b3 = new ReadCodes();
 		Behavior b6 = new SensorHeadPosition();
		Behavior[] bArray = { b1, b2, b3, b6};
 
 		CustomArbitrator arbitrator = new CustomArbitrator(bArray);		
 		Thread t = new Thread(arbitrator);
 		t.start();
 		
 	}
 
 	@Override
 	public void buttonPressed(Button b) {
 		stopRunning();
 	}
 
 	@Override
 	public void buttonReleased(Button b) {
 		stopRunning();
 	}
 
 	private void stopRunning() {
 		// Stop the arbitrator, the main program and the motors.
 		System.exit(0);
 		// arbitrator.stop();
 		// resetMotors();
 	}
 }
