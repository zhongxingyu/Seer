 package nl.tue.fingerpaint.client.gui.spinners;
 
 import io.ashton.fastpress.client.fast.PressEvent;
 import io.ashton.fastpress.client.fast.PressHandler;
 import nl.tue.fingerpaint.client.gui.buttons.FastButton;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DoubleBox;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 
 /**
  * NumberSpinner Custom Control
  * 
  * Code of this class was obtained from:
  * http://pavanandhukuri.wordpress.com/2012/01/28/gwt-number-spinner-control/
  * 
  * @author Pavan Andhukuri
  * @author Group Fingerpaint
  */
 public class NumberSpinner extends Composite implements ChangeHandler, KeyUpHandler,
		MouseDownHandler, MouseMoveHandler, MouseOutHandler {
 
 	private DoubleBox numberBox;
 	private double RATE;
 	private double MAX;
 	private double MIN;
 	private boolean hasLimits;
 	private NumberSpinnerListener spinnerListener;
 	private HorizontalPanel horPanel;
 	private boolean disableScrollDrag = false;
 
 	// ----Constructors--------------------------------------------
 
 	/**
 	 * Initialises a number spinner with default value 1, increase-rate 1 and
 	 * without limits
 	 */
 	public NumberSpinner() {
 		this(1, 1, 0, 0, false);
 	}
 
 	/**
 	 * Initialises a number spinner with default value {@code defaultValue},
 	 * increase-rate 1 and without limits
 	 * 
 	 * @param defaultValue
 	 *            The value that is initially displayed on this number spinner
 	 */
 	public NumberSpinner(double defaultValue) {
 		this(defaultValue, 1, 0, 0, false);
 	}
 
 	/**
 	 * Initialises a number spinner with default value {@code min},
 	 * increase-rate 1 and limits {@code min} and {@code max}
 	 * 
 	 * @param min
 	 *            The minimum value this spinner can take
 	 * @param max
 	 *            The maximum value this spinner can take
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if {@code min} > {@code max}
 	 */
 	public NumberSpinner(double min, double max)
 			throws IllegalArgumentException {
 		this(min, 1, min, max, true);
 	}
 
 	/**
 	 * Initialises a number spinner with default value {@code defaultValue},
 	 * increase-rate {@code rate} and limits {@code min} and {@code max}
 	 * 
 	 * @param defaultValue
 	 *            The default value of this spinner
 	 * @param rate
 	 *            The amount with which the value if the spinner is
 	 *            increased/decreased when using the up/down buttons
 	 * @param min
 	 *            The minimum value this spinner can take
 	 * @param max
 	 *            The maximum value this spinner can take
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if {@code defaultvalue} < {@code min} || {@code defaultValue}
 	 *             > {@code max} || {@code min} > {@code max}
 	 * 
 	 */
 	public NumberSpinner(double defaultValue, double rate, double min,
 			double max) throws IllegalArgumentException {
 		this(defaultValue, rate, min, max, true);
 	}
 
 	/**
 	 * Initialises a number spinner with default value {@code defaultValue},
 	 * increase-rate {@code rate} and limits {@code min} and {@code max} if
 	 * {@code limits} == {@code true}
 	 * 
 	 * @param defaultValue
 	 *            The default value of this spinner
 	 * @param rate
 	 *            The amount with which the value if the spinner is
 	 *            increased/decreased when using the up/down buttons
 	 * @param min
 	 *            The minimum value this spinner can take
 	 * @param max
 	 *            The maximum value this spinner can take
 	 * @param limits
 	 *            {@code true} if this NumberSpinner has limits, {@code false}
 	 *            otherwise
 	 * 
 	 * @throws IllegalArgumentException
 	 *             if {@code limits} == {@code true} && ( {@code defaultvalue} <
 	 *             {@code min} || {@code defaultvalue} > {@code max} ||
 	 *             {@code min} > {@code max})
 	 * 
 	 */
 	public NumberSpinner(double defaultValue, double rate, double min,
 			double max, boolean limits) throws IllegalArgumentException {
 		if (limits) {
 			if (defaultValue < min) {
 				throw new IllegalArgumentException(
 						"The default value is smaller than the minimum value");
 			}
 			if (defaultValue > max) {
 				throw new IllegalArgumentException(
 						"The default value is larger than the maximum value");
 			}
 			if (min > max) {
 				throw new IllegalArgumentException(
 						"The minimum value must be smaller than the maximum value");
 			}
 			MAX = max;
 			MIN = min;
 		}
 		this.RATE = rate;
 		this.hasLimits = limits;
 
 		numberBox = new DoubleBox();
 		numberBox.setValue(defaultValue);
 		
 		numberBox.addChangeHandler(this);
 		numberBox.addKeyUpHandler(this);
 		numberBox.addMouseDownHandler(this);
 		numberBox.addMouseMoveHandler(this);
		numberBox.addMouseOutHandler(this);
 
 		FastButton upButton = new FastButton("+");// backup old value: ("▲")
 		upButton.addPressHandler(new PressHandler() {
 			public void onPress(PressEvent event) {
 				if (!hasLimits || getValue() <= MAX - RATE) {
 					setValue(getValue() + RATE);
 				}
 			}
 		});
 		upButton.setStyleName("dp-spinner-upbutton");
 
 		FastButton downButton = new FastButton("-");// backup old value: ("‭▼")
 		downButton.addPressHandler(new PressHandler() {
 			public void onPress(PressEvent event) {
 				if (!hasLimits || getValue() >= MIN + RATE) {
 					setValue(getValue() - RATE);
 				}
 			}
 		});
 
 		numberBox.setStyleName("spinnerNumberBox");
 
 		downButton.setStyleName("dp-spinner-downbutton");
 
 		horPanel = new HorizontalPanel();
 		horPanel.add(downButton);
 		horPanel.add(numberBox);
 		horPanel.add(upButton);
 
 		initWidget(horPanel);
 	}
 
 	// ----Getters and Setters--------------------------------------------
 
 	/**
 	 * Returns the value being held.
 	 * 
 	 * @return {@code integerBox.getValue()}
 	 */
 	public double getValue() {
 		return numberBox.getValue() == null ? 0 : numberBox.getValue();
 	}
 
 	/**
 	 * Sets the value to the control
 	 * 
 	 * @param d
 	 *            Value to be set
 	 */
 	public void setValue(double d) {
 		setValue(d, false);
 	}
 
 	/**
 	 * Sets the value to the control and possibly performs rounding of the given
 	 * .
 	 * 
 	 * @param d
 	 *            Value to be set
 	 * @param round
 	 *            Boolean to indicate whether rounding should be performed.
 	 */
 	public void setValue(double d, boolean round) {
 		numberBox.setValue(d);
 		if (spinnerListener != null) {
 			spinnerListener.onValueChange(d, getRoundedValue(d));
 		}
 
 		if (round) {
 			roundValue();
 		}
 	}
 
 	/**
 	 * Sets the rate at which increment or decrement is done.
 	 * 
 	 * @param rate
 	 *            Increase rate to be set
 	 */
 	public void setRate(double rate) {
 		this.RATE = rate;
 	}
 
 	/**
 	 * Change or set the listener attached to this NumberSpinner.
 	 * 
 	 * @param spinnerListener
 	 *            The (new) listener that will be attached to this spinner.
 	 */
 	public void setSpinnerListener(NumberSpinnerListener spinnerListener) {
 		this.spinnerListener = spinnerListener;
 	}
 
 	/**
 	 * Returns the value of the spinner rounded to a valid value for this spinner.
 	 * 
 	 * @return Value of this spinner, rounded to the nearest valid value.
 	 */
 	public double getRoundedValue() {
 		return getRoundedValue(getValue());
 	}
 	
 	/**
 	 * Returns the given value, rounded so that it is a valid one for this spinner.
 	 * 
 	 * @param value The value to round.
 	 */
 	private double getRoundedValue(double value) {
 		double result = value;
 		if (hasLimits) {
 			if (result < MIN) {
 				result = MIN;
 			} else if (getValue() > MAX) {
 				result = MAX;
 			}
 		}
 		return Math.round(result / RATE) * RATE;
 	}
 	
 	/**
 	 * Rounds the current value of this numberspinner.
 	 */
 	private void roundValue() {
 		setValue(getRoundedValue());
 	}
 
 	@Override
 	public void onChange(ChangeEvent event) {
 		roundValue();
 	}
 
 	@Override
 	public void onKeyUp(KeyUpEvent event) {
 		if (spinnerListener != null) {
 			spinnerListener.onValueChange(getValue(), getRoundedValue());
 		}
 	}
 
 	@Override
 	public void onMouseDown(MouseDownEvent event) {
 		// Google Chrome bug fix; http://stackoverflow.com/a/16751089/962603
 		disableScrollDrag = true;
 		numberBox.getElement().getStyle().setProperty("pointerEvents", "none");
 	}
 	
 	@Override
 	public void onMouseMove(MouseMoveEvent event) {
 		// Google Chrome bug fix; http://stackoverflow.com/a/16751089/962603
		if (disableScrollDrag) {
 			numberBox.getElement().getStyle().setProperty("pointerEvents", "auto");
 			disableScrollDrag = false;
 		}
 	}
	
	@Override
	public void onMouseOut(MouseOutEvent event) {
		// Improved on the Google Chrome bug fix: when leaving the input with
		// mouse down, pointerEvents would still be disabled
		disableScrollDrag = false;
		numberBox.getElement().getStyle().setProperty("pointerEvents", "auto");
	}
 }
