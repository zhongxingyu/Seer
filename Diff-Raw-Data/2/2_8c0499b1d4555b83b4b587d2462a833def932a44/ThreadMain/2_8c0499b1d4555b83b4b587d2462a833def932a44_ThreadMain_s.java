 package edu.kit.curiosity;
 
 import lejos.nxt.Button;
 import lejos.nxt.ButtonListener;
 import lejos.nxt.MotorPort;
 import lejos.robotics.subsumption.Behavior;
 import lejos.robotics.subsumption.CustomArbitrator;
 import edu.kit.curiosity.behaviors.*;
 
 public class ThreadMain implements ButtonListener {
 
 	//private static CustomArbitrator arbitrator;
 
 	public ThreadMain() {
 		Button.ESCAPE.addButtonListener(this);
 	}
 
 	public static void main(String[] args) throws Exception {
 		Behavior b1 = new DriveForward();
 		Behavior b2 = new WallTooClose();
 		Behavior b3 = new WallTooFar();
 		Behavior b4 = new HitWall();
 		Behavior[] bArray = { b1, b2, b3, b4 };
 		CustomArbitrator arbitrator = new CustomArbitrator(bArray);
 
 		Thread t = new Thread(arbitrator);
 		t.start();
 	}
 
 	@Override
 	public void buttonPressed(Button b) {
 		// TODO Auto-generated method stub
 		stopRunning();
 	}
 
 	@Override
 	public void buttonReleased(Button b) {
 		// TODO Auto-generated method stub
 		stopRunning();
 	}
 
 	private void stopRunning() {
 		// Stop the arbitrator, the main program and the motors.
 		while (true) {
 			System.out.println("blablbal");
 		}
 		// arbitrator.stop();
 		// resetMotors();
 	}
 
 	private void resetMotors() {
 		try {
 			int step = 100;
 			for (int i = 0; i < 1000; i += step) {
 				MotorPort.C.controlMotor(0, 3);
 				MotorPort.B.controlMotor(0, 3);
 				Thread.sleep(step);
 			}
 		} catch (InterruptedException e) {
 		}
 	}
 }
