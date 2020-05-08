 package dk.itu.ecdar.text.generator.environment;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class QuickLog {
 
 	private static QuickLog _instance = null;
 	
 	private int logLevel;
 	private volatile int counter;
 	
 	private boolean console ;
 	private String prefix, destination;
 	
 	private QuickLog() {
 		logLevel = 0;
 		counter = 0;
 		
 		console = true;
 		
 		DateFormat format = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");
 		Date date = new Date();
 		
 		prefix = null;
 		destination = "log-" + format.format(date) + ".csv";
 	}
 	
 	private static QuickLog getInstance() {
 		if (_instance == null) {
 			_instance = new QuickLog();
 		}
 		return _instance;
 	}
 	
 	public static void print(String message) {
 		print(message, 0);
 	}
 	
 	public static void print(String message, int level) {
 		if (level <= getInstance().logLevel)
 			System.err.println("[QuickLog] " + message);
 	}
 	
 	public static void setLogLevel(int level) {
 		getInstance().logLevel = level;
 	}
 	
 	public static void logToConsole() {
 		getInstance().console = true;
 	}
 	
 	public static void logToFile() {
 		getInstance().console = false;
 	}
 	
 	public static void logToFile(String prefix) {
 		logToFile();
 		getInstance().prefix = prefix;
 	}
 	
 	public static void setDestination(String filepath) {
 		getInstance().destination = filepath;
 	}
 	
 	public static void log(String source, long time, String message) {
 		log(source, time, message, 0);
 	}
 	
 	public static synchronized void log(String source, long time, String message, int level) {
 		getInstance().internal_log(source, time, message, level);
 	}
 	
 	private void internal_log(String source, long time, String message, int level) {
 		if (level <= logLevel) {
 			if (console)
 				internal_logToConsole(source, time, message);
 			else
 				internal_logToFile(source, time, message);
 		}
 	}
 	
 	private synchronized void internal_logToConsole(String source, long time, String message) {
 		System.err.println("[" + String.valueOf(++counter) + "] ["
 				+ source + "] [T=" + String.valueOf(time) + "] " + message);
 	}
 	
 	private synchronized void internal_logToFile(String source, long time, String message) {
 		String msg = String.valueOf(++counter) + ","
 				+ source + ","
 				+ String.valueOf(time) + ","
 				+ message;
 		
 		try {
			String filename = destination;
			if (prefix != null)
				filename = prefix + "-" + destination;
			
 			FileWriter fileWriter = new FileWriter(filename, true);
 			BufferedWriter outBuffer = new BufferedWriter(fileWriter);
 
 			String newLine = System.getProperty("line.separator");
 			outBuffer.write(msg + newLine);
 
 			outBuffer.close();
 		} catch (Exception e) {
			System.err.println("ERROR: Could not write to file " + destination);
 		}
 	}
 }
