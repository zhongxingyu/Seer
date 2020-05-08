 package main;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * This class is used to write messages to the user. If setVerbos(true) was
  * called, all messages will be printed, if not, only messages with
  * Severity.ERROR will be printed.
  * 
  * @author Daniel
  * 
  */
 public class Logger {
 
 	private static final String SPACE_STRING = " ";
 	private static final String ERROR_STRING = "ERROR: ";
 
 	private static final SimpleDateFormat formatter = new SimpleDateFormat(
 			"yyyyMMdd-hhmmss");
 
 	private boolean verbose = false;
 
 	public enum Severity {
 		ERROR, NORMAL;
 	}
 
 	public Logger(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	/**
 	 * Writes messaged to the out stream or error stream according to the
 	 * message severity.
 	 * 
 	 * @param message
 	 *            the message to write.
 	 * @param severity
 	 *            the severity of the message.
 	 */
 	public void sendLog(String message, Severity severity) {
 		if (severity.equals(Severity.ERROR)) {
			System.err.println(ERROR_STRING + getDate() + SPACE_STRING
 					+ message);
 		} else if (verbose) {
 			System.out.println(getDate() + SPACE_STRING + message);
 		}
 	}
 
 	/**
 	 * Sets verbose to the new value.
 	 * 
 	 * @param verbose
 	 *            new value for verbose
 	 */
 	public void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	// get current date in the correct format
 	private String getDate() {
 		return formatter.format(new Date());
 	}
 }
