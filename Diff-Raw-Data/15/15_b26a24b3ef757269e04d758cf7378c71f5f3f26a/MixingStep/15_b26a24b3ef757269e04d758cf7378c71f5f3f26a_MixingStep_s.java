 package nl.tue.fingerpaint.client.model;
 
 import java.io.Serializable;
 
 import org.jsonmaker.gwt.client.Jsonizer;
 
 /**
  * MixingStep is a class that stores information for an individual mixing step
  * of a mixing protocol
  * 
  * @author Group Fingerpaint
  * 
  */
 public abstract class MixingStep implements Serializable {
 
 	/** Randomly generated serialVersionUID */
	protected static final long serialVersionUID = -4628728115890489404L;
 	/** the minimum step size, all step sizes should be a multiple of this */
 	public static final double STEP_UNIT = 0.25;
 	/** lowest allowed step size */
 	public static final double STEP_MIN = 0.25;
 	/** highest allowed step size */
 	public static final double STEP_MAX = 100;
 	/** default step size */
 	public static final double STEP_DEFAULT = 1;
 
 	/** number of times stepUnit time is applied in the mixing step */
 	protected int nrUnits = 4;
 
 	/**
 	 * Empty constructor. Necessary for serialising.
 	 */
 	public MixingStep() { }
 	
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
 	 * If the precondition does not hold, stepSize will be rounded to
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
 	public abstract String getName();
 
 	/**
 	 * Returns the string representation of this mixing step.
 	 */
 	public abstract String toString();
 
 	/**
 	 * Sets this step's parameters to those as specified in the string.
 	 * @param string Textual representation of the step.
 	 */
 	public abstract void fromString(String string);
 	
 	/** MixingStep JSONizer Interface */
 	public interface MixingStepJsonizer extends Jsonizer {
 		// Empty on purpose
 	}
 
 }
