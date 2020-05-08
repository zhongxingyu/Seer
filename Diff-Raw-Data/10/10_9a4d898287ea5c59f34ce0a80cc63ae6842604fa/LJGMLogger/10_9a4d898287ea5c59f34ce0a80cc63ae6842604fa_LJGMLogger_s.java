 package net.dean.ljgm.logging;
 
 import static net.dean.ljgm.logging.LoggingLevel.DEBUG;
 import static net.dean.ljgm.logging.LoggingLevel.ERROR;
 import static net.dean.ljgm.logging.LoggingLevel.INFO;
 import static net.dean.ljgm.logging.LoggingLevel.WARN;
 
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 
 import net.dean.ljgm.LJGM;
 
// TODO: Auto-generated Javadoc
 /**
  * This class is responsible for logging data the the standard output streams.
  */
 public class LJGMLogger {
 
 	/**
 	 * Instantiates a new LJGM logger.
 	 */
 	public LJGMLogger() {
 
 	}
 
 	/**
 	 * Gets the time prefix. It's format is "[HH:mm:ss:SSS]"
 	 * 
 	 * @return The time prefix
 	 */
 	public static String getTimePrefix() {
 		// return "[" + System.nanoTime() / 100000000L + "] ";
 		return "[" + new SimpleDateFormat("HH:mm:ss:SSS").format(new java.util.Date()) + "]";
 	}
 
 	/**
 	 * Gets the method that called one of the logging methods.
 	 * 
 	 * @return The calling method
 	 */
 	public static String getCallingMethod() {
 		StackTraceElement elem = Thread.currentThread().getStackTrace()[4];
 		return "[" + elem.getClassName().substring(elem.getClassName().lastIndexOf('.') + 1) + "." + elem.getMethodName()
 				+ "()]";
 	}
 
 	/**
 	 * Outputs an information message to the standard out.
 	 * 
 	 * @param message
 	 *            The message
 	 */
 	public void info(String message) {
 		log(format(message, INFO), System.out);
 	}
 
 	/**
 	 * Outputs a warning to the standard error.
 	 * 
 	 * @param warning
 	 *            The warning
 	 */
 	public void warn(String warning) {
 		log(format(warning, WARN), System.err);
 	}
 
 	/**
 	 * Outputs an error message to the standard error.
 	 * 
 	 * @param error
 	 *            the error
 	 */
 	public void err(String error) {
 		log(format(error, ERROR), System.err);
 	}
 
 	/**
 	 * Outputs an exception to the standard error.
 	 * 
 	 * @param t
 	 *            The exception.
 	 */
 	public void throwable(Throwable t) {
 		throwable(t, "");
 	}
 
 	/**
 	 * Outputs an exception and a message to the standard error.
 	 * 
 	 * @param t
 	 *            The exception
 	 * @param message
 	 *            The message
 	 */
 	public void throwable(Throwable t, String message) {
		log(format("[" + t.getClass().getSimpleName() + "] " + message + ":" + t.getLocalizedMessage(), ERROR), System.err);
 	}
 
 	/**
 	 * If debug mode is enabled (
 	 * <code>JLGM.instance().getConfigManager().isDebug()</code> returns
 	 * <code>true</code>), then output a debug message to the standard out.
 	 * 
 	 * 
 	 * @param message
 	 *            The message
 	 */
 	public void debug(String message) {
 		if (LJGM.instance().getConfigManager().isDebug()) {
 			log(format(message, DEBUG), System.out);
 		}
 	}
 
 	/**
 	 * Formats a message to be ready to be logged.
 	 * 
 	 * @param message
 	 *            The message
 	 * @param level
 	 *            The level of logging
 	 * @return A formatted string.
 	 */
 	public String format(String message, LoggingLevel level) {
 		return getTimePrefix() + "[" + level + "]" + " " + message;
 	}
 
 	/**
 	 * Logs a message to a {@link PrintStream}.
 	 *
 	 * @param msg The string to output
 	 * @param out The PrintStream to output the method to
 	 */
 	private void log(String msg, PrintStream out) {
 		out.println(msg);
 	}
 
 }
