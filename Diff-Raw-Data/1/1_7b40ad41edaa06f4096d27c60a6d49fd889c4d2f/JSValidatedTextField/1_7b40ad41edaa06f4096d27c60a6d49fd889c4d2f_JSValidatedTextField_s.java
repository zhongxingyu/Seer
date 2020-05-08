 package js;
 
 import java.awt.Color;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 
 /**
  * JSValidatedTextField is a JTextField which can perform validation on its text, one of
  * presence, format, range or length checks. It can perform this validation on command
  * or automatically when focus is lost.
  */
 public class JSValidatedTextField extends JTextField implements FocusListener {
 
 	private static final int PRESENCE_CHECK = 0;
 	private static final int LENGTH_CHECK = 1;
 	private static final int RANGE_CHECK = 2;
 	private static final int FORMAT_CHECK = 3;
 	
 	private int type;
 	private int minLength;
 	private int maxLength;
 	private double minValue;
 	private double maxValue;
 	private String pattern;
 	private boolean auto;
 	private String error;
 	private String name;
 	private JSPopover popover;
 	private JLabel errorLabel;
 	
 	private JSValidatedTextField() {
 		addFocusListener(this);
 		popover = new JSPopover(JSPopover.HORIZONTAL);
 		popover.setSize(225, 75);
 		popover.setStrokeColor(new Color(255, 55, 55));
 		errorLabel = new JLabel();
 		errorLabel.setVerticalAlignment(JLabel.CENTER);
 		errorLabel.setVerticalTextPosition(JLabel.CENTER);
 		errorLabel.setBounds(5, 5, 205, 45);
 		popover.add(errorLabel);
 	}
 	
 	/**
 	 * Creates a new text field which can perform a presence check, i.e. a required text field.
 	 * 
 	 * @return a JSValidatedTextField configured for a presence check.
 	 */
 	public static JSValidatedTextField createPresenceCheckField() {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = PRESENCE_CHECK;
 		return field;
 	}
 	
 	/**
 	 * Creates a new text field which can perform a length check, i.e. checking that the entered text has
 	 * the correct number of characters. 
 	 * 
 	 * @param min the minimum number of characters the entry requires. 
 	 * @param max the maximum number of characters the entry can have.
 	 * @return A JSValidatedTextField configured for a length check.
 	 */
 	public static JSValidatedTextField createLengthCheckField(int min, int max) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = LENGTH_CHECK;
 		field.minLength = min;
 		field.maxLength = max;
 		return field;
 	}
 	
 	/**
 	 * Creates a new text field which can perform a range check, i.e. checking that the entered text is
 	 * a) numeric and b) between certain values. 
 	 * 
 	 * @param min the minimum numeric value the field can take.
 	 * @param max the maximum numeric value the field can take.
 	 * @return A JSValidatedTextField configured for a range check.
 	 */
 	public static JSValidatedTextField createRangeCheckField(double min, double max) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = RANGE_CHECK;
 		field.minValue = min;
 		field.maxValue = max;
 		return field;
 	}
 	
 	/**
 	 * Creates a new text field which can perform a format check, i.e. checking that the entered text
 	 * matches a given pattern.
 	 * 
 	 * @param regex a Regular Expression which the text has to match to pass validation.
 	 * @return A JSValidatedTextField configured for a format check.
 	 */
 	public static JSValidatedTextField createFormatCheckField(String regex) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = FORMAT_CHECK;
 		field.pattern = regex;
 		return field;
 	}
 	
 	/**
 	 * Performs the text field's configured validation on the text that has been entered into it. The
 	 * field's <code>error</code> property is updated with the results of the check. 
 	 */
 	public void validate() {
 		String text = getText();
 		boolean failed = false;
 		switch (type) {
 		case PRESENCE_CHECK:
 			if (text.length() == 0) {
 				error = " is required.";
 				failed = true;
 			}
 			break;
 		case LENGTH_CHECK:
 			if (text.length() < minLength) {
 				failed = true;
 				error = " must contain at least " + minLength + " characters.";
 			} else if (text.length() > maxLength) {
 				error = " must contain " + maxLength + " characters or less.";
 				failed = true;
 			}
 			break;
 		case RANGE_CHECK:
 			try {
 				double number = Double.parseDouble(text);
 				if (number < minValue) {
 					error = " must be at least " + minValue + ".";
 					failed = true;
 				} else if (number > maxValue) {
 					error = " must be " + maxValue + " or less.";
 					failed = true;
 				}
 			} catch (NumberFormatException e) {
 				error = " must contain a number.";
 				failed = true;
 			}
 			break;
 		case FORMAT_CHECK:
 			if (! text.matches(pattern)) {
 				error = " is not in the correct format.";
 				failed = true;
 			}
 			break;
 		}
 		if (failed) {
 			if (name != null && name.length() > 0)
 				error = name + error;
 			else
 				error = "This field" + error;
 		} else
 			error = "";
 		errorLabel.setText("<html>" + error + "</html>");
 	}
 	
 	/**
 	 * Determines whether the text in the field passed validation the last time <code>validate()</code>
 	 * was called.
 	 * 
 	 * @return <code>true</code> if the text passed validation, or <code>false</code> if not.
 	 */
 	public boolean passedValidation() {
 		return error.length() == 0;
 	}
 	
 	/**
 	 * Sets the name of the field, which is used when displaying the popover after automatic
 	 * validation.
 	 * 
 	 * @param name the name of the field.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Gets the name of the field.
 	 * 
 	 * @return the name of the field.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Gets the error message from the most recent <code>validate()</code> call.
 	 * 
 	 * @return a String containing the error message.
 	 */
 	public String getErrorMessage() {
 		return error;
 	}
 	
 	public void showErrorMessage() {
		errorLabel.setText("<html>" + error + "</html>");
 		popover.setLocation(getLocationOnScreen().x + getWidth(), getLocationOnScreen().y + (getHeight() / 2));
 		popover.setVisible(true);
 	}
 	
 	public void hideErrorMessage() {
 		popover.setVisible(false);
 	}
 	
 	public JSPopover getPopover() {
 		return popover;
 	}
 	
 	/**
 	 * Sets whether automatic validation is enabled. If it is enabled, the field
 	 * will perform its validation automatically when focus is lost and display a
 	 * popover with the error message.
 	 * 
 	 * @param state a boolean representing the state of automatic validation.
 	 */
 	public void setAutoValidationEnabled(boolean state) {
 		auto = state;
 	}
 	
 	/**
 	 * Determines whether automatic validation is currently enabled for this text field.
 	 * 
 	 * @return a boolean representing the state of automatic validation.
 	 */
 	public boolean isAutoValidationEnabled() {
 		return auto;
 	}
 
 	public void focusGained(FocusEvent e) {
 		if (auto && popover != null && popover.isShowing())
 			popover.setVisible(false);
 	}
 
 	public void focusLost(FocusEvent e) {
 		if (auto) {
 			validate();
 			if (! passedValidation())
 				showErrorMessage();
 		}
 	}
 	
 }
