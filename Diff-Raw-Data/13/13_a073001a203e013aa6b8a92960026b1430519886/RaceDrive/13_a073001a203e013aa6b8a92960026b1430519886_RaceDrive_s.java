 package edu.kit.curiosity.behaviors.race;
 
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.subsumption.Behavior;
 import edu.kit.curiosity.Settings;
 
 /**
  * This class describes the Behavior to drive forward.
  * 
  * @author Team Curiosity
  * 
  */
 public class RaceDrive implements Behavior {
 	private DifferentialPilot pilot;
 	private boolean suppressed = false;
 
 	/**
 	 * Constructs a new RaceDrive Behavior
 	 */
 	public RaceDrive() {
 		this.pilot = Settings.PILOT;
 	}
 
 	/**
 	 * Takes always Control. returns true
 	 */
 	public boolean takeControl() {
 		return Settings.atStart;
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
 		System.out.println("Drive");
 		Settings.atStart = false;
 		suppressed = false;
 		while (!suppressed) {
 			pilot.steer(-50, -10, true);
 		}
 //		pilot.stop();
 	}
 }
