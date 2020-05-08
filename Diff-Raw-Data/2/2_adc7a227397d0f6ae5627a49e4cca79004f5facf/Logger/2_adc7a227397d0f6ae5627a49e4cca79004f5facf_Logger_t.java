 package Util;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Logger.
  * Class made for error logging.
  * It is possible to record a log message without creating an instance of 
  * the class by calling the static method write().
  * 
  * <code>
  *     Logger.write("This is a sample error-message");
  * </code>
  * 
  * Or you can create an instance of the class.
  * 
  * <code>
  *     Logger log = new Logger();
  *     log.writeToLog("This is a sample error-message");
  *     log.close();
  * </code>
  * 
  * @author Niklas Hansen
  * @version 0.1
  */
 public class Logger {
 	
 	private FileWriter writer;
 	
 	static private Logger logger;
 	
 	/**
 	 * Constructor for objects of class Logger.
 	 * Initializes the File Writer, and opens the file.
 	 */
 	public Logger () {
 		try {
			this.writer = new FileWriter("error.log", true);
 		} catch (IOException e) {
 			System.err.println("Couldn't open file: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Write a message to the log.
 	 * 
 	 * @param errorMsg The error message to be recorded.
 	 */
 	static public void write (String errorMsg) {
 		logger = new Logger();
 		logger.writeToLog(errorMsg);
 		logger.close();
 		logger = null;
 	}
 	
 	/**
 	 * Write a message to the log.
 	 * 
 	 * @param errorMsg The error message to be recorded.
 	 */
 	public void writeToLog (String errorMsg) {
 		if (writer == null)
 			return;
 		
 		try {
 			DateFormat dateFormat = new SimpleDateFormat();
 			Date date = new Date();
 			
 			writer.write('[' + dateFormat.format(date) + ']');
 			writer.write(" " + errorMsg);
 			writer.write('\n');
 		} catch (IOException e) {
 			System.err.println("Couldn't open file: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Close the Logger and the File Writer.
 	 */
 	public void close () {
 		if (writer == null)
 			return;
 		
 		try {
 			writer.close();
 		} catch (IOException e) {
 			System.err.println("Couldn't open file: " + e.getMessage());
 		}
 	}
 }
