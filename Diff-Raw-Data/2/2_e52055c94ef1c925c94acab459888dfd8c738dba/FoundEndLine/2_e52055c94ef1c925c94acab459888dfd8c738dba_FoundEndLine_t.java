 package edu.kit.curiosity.behaviors.maze;
 
 import edu.kit.curiosity.Settings;
 import lejos.nxt.LightSensor;
 import lejos.robotics.subsumption.Behavior;
 
 /**
  * The class {@code FoundEndLine} describes the Behavior which takes place, if
  * the rbot found the end line of the maze.
  * 
  * @author Team Curiosity
  * 
  */
 public class FoundEndLine implements Behavior {
 
 	private LightSensor light;
 
 	/**
 	 * Constructs a new FoundEndLine Behavior
 	 */
 	public FoundEndLine() {
 		light = Settings.LIGHT;
 	}
 
 	/**
 	 * The Behavior takes control if the robot has passed the swamp and detects
 	 * a high light value.
 	 */
 	@Override
 	public boolean takeControl() {
		return (!Settings.endOfMaze && Settings.afterSwamp && light.getNormalizedLightValue() > Settings.swampLight);
 	}
 
 	/**
 	 * Sets the endOfMaze flag in {@link Settings}
 	 */
 	@Override
 	public void action() {
 
 		Settings.endOfMaze = true;
 
 	}
 
 	/**
 	 * Initiates the cleanup when this Behavior is suppressed
 	 */
 	@Override
 	public void suppress() {
 
 	}
 
 }
