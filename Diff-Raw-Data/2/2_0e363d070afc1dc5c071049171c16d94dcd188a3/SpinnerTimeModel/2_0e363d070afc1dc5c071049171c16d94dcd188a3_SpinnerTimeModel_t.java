 package com.alexrnl.commons.time;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.AbstractSpinnerModel;
 
 /**
  * Model for a JSpinner on {@link Time}.<br />
  * Allow to set a value using a {@link Time} or a {@link String} which can be formatted using
  * {@link Time#get(String)}.
  * @author Alex
  */
 public class SpinnerTimeModel extends AbstractSpinnerModel {
 	/** Logger */
 	private static Logger		lg					= Logger.getLogger(SpinnerTimeModel.class.getName());
 	
 	/** Serial version UID */
 	private static final long	serialVersionUID	= 5142588648167228978L;
 	
 	// Times
 	/** The value of the model */
 	private Time				value;
 	/** The step between two values */
 	private final Time			step;
 	/** The maximum value reachable */
 	private final Time			maxValue;
 	/** The minimum value reachable */
 	private final Time			minValue;
 	
 	/**
 	 * Constructor #1.<br />
 	 * @param value
 	 *        the value of the spinner.
 	 * @param minValue
 	 *        the minimum time reachable.
 	 * @param maxValue
 	 *        the maximum time reachable.
 	 * @param step
 	 *        the step between two distinct times.
 	 */
 	public SpinnerTimeModel (final Time value, final Time minValue, final Time maxValue, final Time step) {
 		super();
 		this.step = step;
 		this.maxValue = maxValue;
 		this.minValue = minValue;
 		if (!isValid(value)) {
 			lg.warning("Value for SpinnerTimeModel is not valid: " + value + " < " + minValue
					+ " OR " + value + " > "  + maxValue);
 			throw new IllegalArgumentException("Value for TimeSpinnerModel is not in specified bounds: min="
 					+ minValue + "; max=" + maxValue + "; value=" + value);
 		}
 		this.value = value;
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * @param value
 	 *        the value of the spinner.
 	 * @param minValue
 	 *        the minimum time reachable.
 	 * @param maxValue
 	 *        the maximum time reachable.
 	 */
 	public SpinnerTimeModel (final Time value, final Time minValue, final Time maxValue) {
 		this(value, minValue, maxValue, new Time(0, 1));
 	}
 	
 	@Override
 	public Object getValue () {
 		return value;
 	}
 	
 	@Override
 	public void setValue (final Object value) {
 		if ((value == null) || (!(value instanceof Time) && !(value instanceof String))) {
 			throw new IllegalArgumentException("Illegal value for time " + value);
 		}
 		if (!value.equals(this.value)) {
 			final Time newValue;
 			if (value instanceof Time) {
 				newValue = (Time) value;
 			} else if (value instanceof String) {
 				newValue = Time.get((String) value);
 			} else {
 				lg.severe("Should not arrived at this point, severe class casting problem with " +
 						value + " of class " + value.getClass());
 				throw new IllegalArgumentException("Should not arrive here: value is not Time or " +
 						"String but is" + value.getClass());
 			}
 			if (isValid(newValue)) {
 				this.value = newValue;
 				if (lg.isLoggable(Level.FINE)) {
 					lg.fine("New value for SpinnerTimeModel: " + value);
 				}
 			} else if (lg.isLoggable(Level.INFO)) {
 				lg.info("Could not set " + newValue + " for SpinnerTimeModel, out of ["
 						+ minValue + ";" + maxValue + "]");
 			}
 			fireStateChanged();
 		}
 	}
 	
 	@Override
 	public Object getNextValue () {
 		return incrTime(1);
 	}
 	
 	@Override
 	public Object getPreviousValue () {
 		return incrTime(-1);
 	}
 	
 	/**
 	 * Check if the new value for the model is within the bounds of this model.
 	 * @param newValue
 	 *        the new value to test.
 	 * @return <code>true</code> if the value is between the minimum and maximum time allowed.
 	 */
 	private boolean isValid (final Time newValue) {
 		if ((maxValue != null) && (maxValue.compareTo(newValue) < 0)) {
 			return false;
 		}
 		if ((minValue != null) && (minValue.compareTo(newValue) > 0)) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Increment the current Time by one step in the direction specified.
 	 * @param direction
 	 *        direction of the step.
 	 * @return the new time to be set.
 	 */
 	private Time incrTime (final int direction) {
 		Time newValue = null;
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("incrTime in dir " + direction + " (value: " + value + ")");
 		}
 		
 		// Changing value
 		if (direction > 0) {
 			newValue = value.add(step);
 		} else if (direction < 0) {
 			newValue = value.sub(step);
 		} else {
 			throw new IllegalArgumentException("Direction value " + direction + " is not valid.");
 		}
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("newValue: " + newValue);
 		}
 		
 		return isValid(newValue) ? newValue : null;
 	}
 	
 }
