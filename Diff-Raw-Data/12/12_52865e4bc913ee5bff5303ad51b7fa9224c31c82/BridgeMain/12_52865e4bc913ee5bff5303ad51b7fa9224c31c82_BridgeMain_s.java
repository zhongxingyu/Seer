 package edu.kit.curiosity.behaviors.bridge;
 
 import lejos.nxt.Button;
 import lejos.nxt.ButtonListener;
 import lejos.robotics.subsumption.Arbitrator;
 import lejos.robotics.subsumption.Behavior;
 import edu.kit.curiosity.LightCalibrate;
 import edu.kit.curiosity.SensorHeadCalibrate;
 import edu.kit.curiosity.Settings;
 import edu.kit.curiosity.behaviors.DriveForward;
 import edu.kit.curiosity.behaviors.ReadCodes;
 import edu.kit.curiosity.behaviors.SensorHeadPosition;
 
 public class BridgeMain implements ButtonListener {
 	public BridgeMain() {
 		Button.ESCAPE.addButtonListener(this);
 	}
 
 	public static void main(String[] args) throws Exception {
 		new BridgeMain();
 
 		new LightCalibrate();
 		new SensorHeadCalibrate();
 		
 		Settings.PILOT.setTravelSpeed(30);
 
 		Settings.motorAAngle = Settings.SENSOR_RIGHT;
 		/**
 		 * Bridge behavior and arbitrator
 		 */
 		Behavior b0 = new DriveForward();
 		Behavior b1 = new HitWallBeforeBridge();
 		Behavior b2 = new DriveUntilAbyss();
 		Behavior b3 = new AbyssDetected();
 		Behavior b4 = new ReachedEndOfBridge();
 		Behavior b5 = new ReadCodes();
 		Behavior b6 = new SensorHeadPosition();
 		Behavior[] bridgeBehavior = { b0, b1, b2, b3, b4, b5, b6 };
 
 		Arbitrator arb = new Arbitrator(bridgeBehavior);
 		arb.start();
 
 	}
 
 	@Override
 	public void buttonPressed(Button b) {
 		// TODO Auto-generated method stub
 		// stopRunning();
 	}
 
 	@Override
 	public void buttonReleased(Button b) {
 		// TODO Auto-generated method stub
 		stopRunning();
 	}
 
 	private void stopRunning() {
 		// Stop the arbitrator, the main program and the motors.
 		System.exit(0);
 		// arbitrator.stop();
 		// resetMotors();
 	}
 }
