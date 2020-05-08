 package org.saseros.cleanstorms;
 
 import lejos.nxt.Motor;
 
 /**
  * A thread-class for moving the UltrasonicSensor attached to the head.
  * 
  * @author Pers
  * 
  */
 public class UltrasonicHeadMover extends Thread {
 
 	private final int turningDegree = 45;
 	private boolean check = true;
 
 	/**
 	 * The code that should be executed once the thread has been initiated
 	 */
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				if (Motor.B.getTachoCount() != 0) {
 					executeHeadTurn(0);
 				} else {
 					if (check) {
 						executeHeadTurn(turningDegree);
 					} else {
						executeHeadTurn(turningDegree);
 					}
 				}
 				Thread.sleep(1000);
 			} catch (InterruptedException ie) {
 				System.out.println("Thread turning Ultrasonic head failed");
 			}
 		}
 	}
 
 	/**
 	 * A help-method for the thread that executes the head-turning
 	 * 
 	 * @param rotation
 	 *            The rotation the head should execute
 	 */
 	private void executeHeadTurn(int rotation) {
 		Motor.B.rotateTo(rotation);
 		this.check = !check;
 	}
 }
