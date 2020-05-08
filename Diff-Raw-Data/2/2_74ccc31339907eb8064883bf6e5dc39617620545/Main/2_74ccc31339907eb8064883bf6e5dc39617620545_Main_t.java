 package edu.kit.curiosity.behaviors.race;
 
 import lejos.nxt.Button;
 import lejos.nxt.ButtonListener;
 import lejos.nxt.LCD;
 import lejos.nxt.Motor;
 import lejos.robotics.subsumption.Arbitrator;
 import lejos.robotics.subsumption.Behavior;
 import lejos.robotics.subsumption.CustomArbitrator;
 import edu.kit.curiosity.Settings;
 import edu.kit.curiosity.behaviors.*;
 import edu.kit.curiosity.behaviors.gate.RollFloor;
 import edu.kit.curiosity.behaviors.maze.*;
 import edu.kit.curiosity.behaviors.tape.GapFound;
 import edu.kit.curiosity.behaviors.tape.LineFollow;
 import edu.kit.curiosity.behaviors.tape.TapeLost;
 
 public class Main implements ButtonListener {
 
 
 	public Main() {
 		Button.ESCAPE.addButtonListener(this);
 	}
 
 	public static void main(String[] args) throws Exception {
 		new Main();
 		
 		
 		// Race
 		Settings.PILOT.setTravelSpeed(Settings.PILOT.getMaxTravelSpeed());
 		Settings.PILOT.setRotateSpeed(Settings.PILOT.getMaxRotateSpeed() / 4);
 		Motor.A.setSpeed(Motor.A.getMaxSpeed() / 5);
 		Settings.motorAAngle = 90;
 		
 		
 		Behavior b1 = new RaceDrive();
 		Behavior b2 = new Race();
 		Behavior b6 = new SensorHeadPosition();
 		Behavior b7 = new MotorAStall();
 		
 		// Not in first row
		if (Settings.SONIC.getDistance() < 40) {
 			Settings.inFirstRow = false;
 			
 		// In first row
 		} else {
 			Settings.inFirstRow = true;
 		}
  		
 		
 		Behavior[] bArray = { b1, b2, b6, b7};
 
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
