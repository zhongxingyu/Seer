 package edu.kit.curiosity.behaviors;
 import edu.kit.curiosity.Settings;
 import lejos.nxt.Motor;
 import lejos.robotics.subsumption.*;
 
 /**
  * This class describes the Behavior when Motor A is stalled.
  * @author Team Curiosity
  *
  */
 public class MotorAStall implements Behavior {
 	private boolean suppressed = false;
 	
 	/**
 	 * Constructs a new DriveForward Behavior
 	 */
 	public MotorAStall() {
 	}
 
 	/**
 	 * Takes always Control.
 	 * returns true
 	 */
 	public boolean takeControl() {
 		return (Motor.A.isStalled());
 	}
 
 	/**
 	 * Initiates the cleanup when this Behavior is suppressed
 	 */
 	public void suppress() {
 		suppressed = true;
 	}
 
 	/**
 	 * Moves forward as long as this Behavior is active
 	 */
 	public void action() {
 		System.out.println("Stall.");
 		suppressed = false;
 		Motor.A.flt();
		Motor.A.rotateTo(Settings.motorAAngle);
 		while (!suppressed && Motor.A.isStalled()) {
 			Thread.yield();
 		}
 		Motor.A.stop();
 	}
 }
