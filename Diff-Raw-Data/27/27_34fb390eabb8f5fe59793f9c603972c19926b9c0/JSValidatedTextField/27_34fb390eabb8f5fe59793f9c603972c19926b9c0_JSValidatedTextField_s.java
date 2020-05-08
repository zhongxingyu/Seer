 package js.incomplete;
 
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JTextField;
 
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
 	
 	JSValidatedTextField() {
 		addFocusListener(this);
 	}
 	
 	public static JSValidatedTextField createPresenceCheckField() {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = PRESENCE_CHECK;
 		return field;
 	}
 	
 	public static JSValidatedTextField createLengthCheckField(int min, int max) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = LENGTH_CHECK;
 		field.minLength = min;
 		field.maxLength = max;
 		return field;
 	}
 	
 	public static JSValidatedTextField createRangeCheckField(double min, double max) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = RANGE_CHECK;
 		field.minValue = min;
 		field.maxValue = max;
 		return field;
 	}
 	
 	public static JSValidatedTextField createFormatCheckField(String regex) {
 		JSValidatedTextField field = new JSValidatedTextField();
 		field.type = FORMAT_CHECK;
 		field.pattern = regex;
 		return field;
 	}
 	
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
 	}
 	
 	public boolean passedValidation() {
 		return error.length() == 0;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getErrorMessage() {
 		return error;
 	}
 	
 	public void setAutoValidationEnabled(boolean state) {
 		auto = state;
 	}
 	
 	public boolean isAutoValidationEnabled() {
 		return auto;
 	}
 
 	public void focusGained(FocusEvent e) {

 	}
 
 	public void focusLost(FocusEvent e) {

 	}
 	
 }
