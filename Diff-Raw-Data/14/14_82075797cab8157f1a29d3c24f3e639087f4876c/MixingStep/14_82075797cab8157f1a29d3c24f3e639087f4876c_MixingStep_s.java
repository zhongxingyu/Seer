 package nl.tue.fingerpaint.client;
 
 import java.io.Serializable;
 
 import org.jsonmaker.gwt.client.Jsonizer;
 
 /**
  * MixingStep is a class that stores information for an individual mixing step
  * of a mixing protocol
  * 
  * @author Group Fingerpaint
  * 
  */
 public class MixingStep implements Serializable {
 
 	/** Randomly generated serialVersionUID */
 	private static final long serialVersionUID = -4628728115890489404L;
 	/** the minimum step size, all step sizes should be a multiple of this */
 	public static final double STEP_UNIT = 0.25;
 	/** lowest allowed step size */
 	public static final double STEP_MIN = 0.25;
 	/** highest allowed step size */
 	public static final double STEP_MAX = 100;
 	/** default step size */
 	public static final double STEP_DEFAULT = 1;
 
 	/** number of times stepUnit time is applied in the mixing step */
 	private int nrUnits;
 
 	/**
 	 * direction the direction of the wall movement, true is clockwise, false is
 	 * counterclockwise
 	 */
 	private boolean direction;
 
 	/** the wall that is moved, true for the top wall, false for the bottom wall */
 	private boolean wall;
 
 	/**
 	 * Default constructor. Creates a clockwise top wall step with size 1.0.
 	 */
 	public MixingStep() {
 		this(1.0, true, true);
 	}
 
 	/**
 	 * Adds a single mixing step to the end of the current program.
 	 * 
 	 * @param stepSize
 	 *            The stepSize of the wall movement, should be divisible by 0.25
 	 * @param direction
 	 *            the direction of the wall movement, true is clockwise, false
 	 *            counterclockwise
 	 * @param wall
 	 *            the wall that is moved, true for the top wall, false for the
 	 *            bottom wall
 	 */
 	public MixingStep(double stepSize, boolean direction, boolean wall) {
 		setStepSize(stepSize);
 		setDirection(direction);
 		setWall(wall);
 	}
 
 	/**
 	 * Returns the size of this step.
 	 * 
 	 * @return result = {@code STEP_UNIT} * x with x an integer
 	 */
 	public double getStepSize() {
 		return nrUnits * STEP_UNIT;
 	}
 
 	/**
 	 * Sets the size of this mixing step.
 	 * 
 	 * If the precondition does not not hold, stepSize will be rounded to
 	 * produce an integer x.
 	 * 
 	 * <pre>
 	 * stepSize = {@code STEP_UNIT} * x, with x an integer
 	 * 
 	 * @param stepSize
 	 * The time the mixing step is executed for
 	 */
 	public void setStepSize(double stepSize) {
 		nrUnits = (int) Math.round(stepSize / STEP_UNIT);
 	}
 
 	/**
 	 * Returns the direction of this step.
 	 * 
 	 * @return {@code true} if the wall is moving clockwise, {@code false}
 	 *         otherwise
 	 */
 	public boolean movesForward() {
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
 
 	/**
 	 * Returns the minimum increment value for the step size.
 	 * 
 	 * @return {@code STEP_UNIT}
 	 */
 	public static double getStepUnit() {
 		return STEP_UNIT;
 	}
 
 	/**
 	 * Returns the minimum step size of a step.
 	 * 
 	 * @return {@code STEP_MIN}
 	 */
 	public static double getStepMin() {
 		return STEP_MIN;
 	}
 
 	/**
 	 * Returns the maximum step size of a step.
 	 * 
 	 * @return {@code STEP_MAX}
 	 */
 	public static double getStepMax() {
 		return STEP_MAX;
 	}
 
 	/**
 	 * Returns the default step size of a step.
 	 * 
 	 * @return {@code STEP_DEFAULT}
 	 */
 	public static double getStepDefault() {
 		return STEP_DEFAULT;
 	}
 
 	/**
 	 * Returns the string representation of this mixing step, as it is needed by
 	 * the server.
 	 * 
 	 * @return The name of the mixing step
 	 */
 	public String getName() {
		StringBuilder builder = new StringBuilder();
		if (isTopWall()) {
			builder.append('T');
		} else {
			builder.append('B');
		}
		if (movesForward()) {
			builder.append('R');
		} else {
			builder.append('L');
		}
		return builder.toString();
 	}
 
 	/**
 	 * Returns the string representation of this mixing step.
 	 */
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
 	 * Converts a string to a mixing step.
 	 * 
 	 * @param step
 	 *            The string to be converted
 	 * @return A mixing step object that matches the given input string
 	 */
 	public static MixingStep fromString(String step) {
 		MixingStep result = new MixingStep();
 		if (step.startsWith("-")) {
 			result.setDirection(false);
 			step = step.substring(1);
 		} else {
 			result.setDirection(true);
 		}
 		result.setWall(step.startsWith("T"));
 		step = step.substring(2, step.length() - 1);
 		result.setStepSize(Double.parseDouble(step));
 		return result;
 	}
 
 	/** MixingStep JSONizer Interface */
 	public interface MixingStepJsonizer extends Jsonizer {
 		// Empty on purpose
 	}
 
 }
