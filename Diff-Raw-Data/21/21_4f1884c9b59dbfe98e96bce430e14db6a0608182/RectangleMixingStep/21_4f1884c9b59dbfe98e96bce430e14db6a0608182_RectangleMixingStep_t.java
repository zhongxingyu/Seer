 package nl.tue.fingerpaint.client.model;
 
import java.io.Serializable;

 /**
  * Class for a mixing step for the rectangular geometry.
  * 
  * @author Group Fingerpaint
  *
  */
public class RectangleMixingStep extends MixingStep implements Serializable {
 	/** Randomly generated serialVersionUID */
 	private static final long serialVersionUID = 9188459726499715212L;
 
 	/**
 	 * The direction of the wall movement; true is clockwise, false is
 	 * counterclockwise
 	 */
 	private boolean direction;
 
 	/** The wall that is moved; true for the top wall, false for the bottom wall */
 	private boolean wall;
 
 	/**
	 * Default constructor, necessary for serialisation.
	 */
	public RectangleMixingStep() { }
	
	/**
 	 * Initialises a RectangleMixingStep from a string.
 	 * @param stepRep The textual representation of this mixing step.
 	 */
 	public RectangleMixingStep(String stepRep) {
 		this.fromString(stepRep);
 	}
 	
 	/**
 	 * Creates a RectangleMixingStep with the specified parameters.
 	 * 
 	 * @param stepSize
 	 *            The stepSize of the wall movement, should be divisible by 0.25
 	 * @param direction
 	 *            The direction of the wall movement; true is clockwise, false
 	 *            counterclockwise
 	 * @param wall
 	 *            The wall that is moved; true for the top wall, false for the
 	 *            bottom wall
 	 */
 	public RectangleMixingStep(double stepSize, boolean direction, boolean wall) {
 		setStepSize(stepSize);
 		setDirection(direction);
 		setWall(wall);
 	}
 
 	/**
 	 * Returns the direction of this step.
 	 * 
 	 * @return {@code true} if the wall is moving clockwise, {@code false}
 	 *         otherwise
 	 */
 	public boolean isClockwise() {
 		return direction;
 	}
 
 	/**
 	 * Sets the direction of this step.
 	 * 
 	 * @param direction
 	 *            The direction the wall moves in, {@code true} if clockwise,
 	 *            {@code false} otherwise
 	 */
 	public void setDirection(boolean direction) {
 		this.direction = direction;
 	}
 
 	/**
 	 * Returns the wall this step belongs to.
 	 * 
 	 * @return {@code true} if the top wall moves, {@code false} otherwise
 	 */
 	public boolean isTopWall() {
 		return wall;
 	}
 
 	/**
 	 * Sets the wall this step belongs to.
 	 * 
 	 * @param wall
 	 *            The wall that moves this mixing step, {@code true} for the top
 	 *            wall, {@code false} for the bottom wall
 	 */
 	public void setWall(boolean wall) {
 		this.wall = wall;
 	}
 	
 	@Override
 	public String getName() {
 		return (isTopWall() ? (isClockwise() ? "TR" : "TL")
 				: (isClockwise() ? "BL" : "BR"));
 	}
 	
 	@Override
 	public String toString() {
 		String stepString = "";
 		if (wall && direction) {
 			stepString = "T";
 		} else if (wall && !direction) {
 			stepString = "-T";
 		} else if (!wall && direction) {
 			stepString = "B";
 		} else { // (!wall && !direction) {
 			stepString = "-B";
 		}
 
 		stepString += "[" + (double) nrUnits / 4 + "]";
 		return stepString;
 	}
 	
 	/**
 	 * Sets the parameters of this mixing step to those as specified in
 	 * {@code step}.
 	 * 
 	 * @param step
 	 *            The string to be converted.
 	 */
 	@Override
 	public void fromString(String step) {		
 		if (step.startsWith("-")) {
 			setDirection(false);
 			step = step.substring(1);
 		} else {
 			setDirection(true);
 		}
 		setWall(step.startsWith("T"));
 		
 		// Remove the letter (B or T) and the brackets ([ and ]) from the string.
 		step = step.substring(2, step.length() - 1);
 		// Convert the resulting string and add it to the result.
 		setStepSize(Double.parseDouble(step));
 	}
 }
