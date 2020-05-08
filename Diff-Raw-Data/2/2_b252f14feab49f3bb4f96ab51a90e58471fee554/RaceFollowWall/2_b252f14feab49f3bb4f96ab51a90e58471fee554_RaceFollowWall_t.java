 package edu.kit.curiosity.behaviors.race;
 
 import lejos.nxt.UltrasonicSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.robotics.subsumption.Behavior;
 import edu.kit.curiosity.Settings;
 
 /**
  * The class {@code FollowWall} describes the behavior to follow a wall.
  * 
  * @author Team Curiosity
  */
 public class RaceFollowWall implements Behavior {
 
 	private boolean suppressed = false;
 
 	private UltrasonicSensor sonic;
 	private DifferentialPilot pilot;
 
 	private int distanceToWall;
 
 	/**
 	 * Constructs a new FollowWall Behavior.
 	 * 
 	 * @param distance
 	 *            - the distance in which the wall is followed.
 	 */
 	public RaceFollowWall(int distance) {
 		distanceToWall = distance;
 		pilot = Settings.PILOT;
 		sonic = Settings.SONIC;
 	}
 
 	/**
 	 * The Behavior takes control if the robot is not in the swamp
 	 */
 	@Override
 	public boolean takeControl() {
 		return (!Settings.atStart);
 	}
 
 	/**
 	 * The robot moves along the Wall and turns to the right, if the wall is too
 	 * far away.
 	 */
 	@Override
 	public void action() {
 		suppressed = false;
		Settings.readState = true;
 		System.out.println("Follow");
 		while (!suppressed && !((Settings.TOUCH_L.isPressed() || Settings.TOUCH_R.isPressed()))) {
 			if (sonic.getDistance() > (distanceToWall + 20)) {
 				pilot.arc(-15, -90, true);
 			} else if (sonic.getDistance() <= distanceToWall) {
 				pilot.arc(100, 20, true);
 			} else if (sonic.getDistance() > distanceToWall) {
 				pilot.arc(-100, -20, true);
 			}
 		}
 		// pilot.stop();
 	}
 
 	/**
 	 * Initiates the cleanup when this Behavior is suppressed
 	 */
 	@Override
 	public void suppress() {
 		suppressed = true;
 	}
 
 }
