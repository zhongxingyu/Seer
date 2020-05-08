 package com.cs456.a2;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.os.Environment;
 
 /**
  * A Logging singleton which will write to the log.txt file on the SD card
  * @author Janson
  *
  */
 public class Logger {
 	
 	private final String LOG_FILENAME = "log.txt"; // The name of the file the logs are saved to
 	private File logFile; // The file we are storing log information in
 	private BufferedWriter bufferedWriter; // The writer for the log file
 	
 	private static Logger singleton = null; // A reference to the singleton object
 	
 	/**
 	 * Gets the instance of the singleton logger
 	 * @return The singleton object
 	 */
 	public static Logger getInstance() {
 		if(singleton == null) {
 			singleton = new Logger();
 		}
 		
 		return singleton;
 	}
 	
 	/**
 	 * Initializes the logger by opening the log file
 	 */
 	private Logger() {
 		File root = Environment.getExternalStorageDirectory(); // Get the directory path to the SD card
     	logFile = new File(root, LOG_FILENAME); // Create the scan file object		
     	
     	openLogFile();		      
 	}
 	
 	/**
 	 * A logging method which logs the provided data proceeded by a time stamp
 	 * @param text The text which you want logged
 	 */
 	public void log(String text) {
 		if(bufferedWriter == null) {
 			openLogFile();
 		}
 		try {
 			String curTime = getHumanReadableDate(System.currentTimeMillis());
 			bufferedWriter.write("<" + curTime + ">: " + text + "\n");
			bufferedWriter.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Opens the log file for writing
 	 */
 	private void openLogFile() {
 		// Open a writing connection to the file for future use
 		FileWriter fileWriter;
 		try {
 			fileWriter = new FileWriter(logFile, true);
 			bufferedWriter = new BufferedWriter(fileWriter);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Closes the log file
 	 */
 	public void closeLogFile() {
 		if(bufferedWriter != null) {
 			// Close the buffered writer
 			try {
 				bufferedWriter.close();
 				bufferedWriter = null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			} 			
 		}
 	}
 	
 	// Converts a timestamp in milliseconds to a nicely formatted displayable date
     private String getHumanReadableDate(long milli) {
     	Date date = new Date(milli);
     	DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     	return sdf.format(date);
     }
 
 }
