 package js;
 
 import java.util.Calendar;
 
 
 /**
  * JSLog is an extension of <code>JSTextFileUtility</code> which allows a simple "log" to be kept.
  * By default, when calling the <code>append</code> method, the date and time are printed first,
  * followed by the text passed into the method. This makes the class very useful for event tracking,
  * for example.
  * 
  * @author Josh
  *
  */
 public class JSLog extends JSTextFileUtility {
 	
 	private boolean shouldUseDate = true;
 	private int dateFormat = LONG_FORMAT;
 	
 	public static final int SHORT_FORMAT = 0;
 	public static final int LONG_FORMAT = 1;
 	
 	/**
 	 * Creates a new log which is saved to the specified filename. If the file does not already
 	 * exist, it will be created.
 	 * 
 	 * @param filename the path to which the log should be saved
 	 */
 	public JSLog(String filename) {
 		super(filename);
 	}
 	
 	/**
 	 * Writes the specified text to the end of the file. If <code>shouldUseDate</code> is set to
 	 * <code>true</code>, the date will be written on the same line as the text passed in.
 	 * 
 	 * @param textToAppend the text to write to the file
 	 */
 	public void append(String textToAppend) {
 		String text = (shouldUseDate) ? (getDate() + textToAppend) : (textToAppend);
 		appendToFileWithSeparator(text, "\n");
 	}
 
 	/**
 	 * Sets whether the date should be written to the file when <code>append</code> is called.
 	 * 
 	 * @param shouldUseDate <code>true</code> if the date should be written, otherwise <code>false</code>
 	 */
 	public void setShouldUseDate(boolean shouldUseDate) {
 		this.shouldUseDate = shouldUseDate;
 	}
 	
 	/**
 	 * Sets whether the time should also be written along with the date. Pass <code>SHORT_FORMAT</code>
 	 * if the time should not be written, or <code>LONG_FORMAT</code> if it should.
 	 * 
 	 * @param dateFormat an integer representing the date format to be used
 	 */
 	public void setDateFormat(int dateFormat) {
 		this.dateFormat = dateFormat;
 	}
 	
 	private String getDate() {
 		Calendar now = Calendar.getInstance();
 		if (dateFormat == SHORT_FORMAT) {
 			return dateToString(now) + " ";
 		} else {
 			return dateToString(now) + ", " + timeToString(now) + " "; 
 		}
 	}
 	
 	private String dateToString(Calendar date) {
		return Integer.toString(date.get(Calendar.YEAR)) + "-" + getDoubleDigits(date.get(Calendar.MONTH)) +
 				"-" + getDoubleDigits(date.get(Calendar.DATE));
 	}
 	
 	private String timeToString(Calendar date) {
 		return getDoubleDigits(date.get(Calendar.HOUR_OF_DAY)) + ":" + getDoubleDigits(date.get(Calendar.MINUTE)) +
 				":" + getDoubleDigits(date.get(Calendar.SECOND));
 	}
 	
 	private String getDoubleDigits(int number) {
 		if (number < 10)
 			return "0" + number;
 		else
 			return Integer.toString(number);
 	}
 	
 }
