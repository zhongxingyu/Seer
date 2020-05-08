 package edu.kit.curiosity.behaviors.bridge;
 
 import lejos.robotics.subsumption.Behavior;
 import edu.kit.curiosity.Settings;
 
 /**
  * This behavior is activated, when the end of bridge is reached.
  * 
  * @author Curiosity
  */
 public class ReachedEndOfBridge implements Behavior {
 
 	/**
 	 * This behavior will take control if the number of turns without finding a
 	 * light ground is higher than a given value.
 	 */
 	@Override
 	public boolean takeControl() {
		return (Settings.whipAndBridgeCounter >= 10);
 	}
 
 	/**
 	 * Drive forward and turn the number of turns without finding a light ground
 	 * times the angle that it turns to find light ground.
 	 */
 	@Override
 	public void action() {
 		Settings.reachedBridge = false;
 		Settings.PILOT.travel(20);
 		Settings.PILOT.rotate(Settings.whipAndBridgeCounter * (-10));
 		Settings.whipAndBridgeCounter = 0;
 		// TODO What happens when end of bridge is reached.
 	}
 
 	@Override
 	public void suppress() {
 	}
 
 }
