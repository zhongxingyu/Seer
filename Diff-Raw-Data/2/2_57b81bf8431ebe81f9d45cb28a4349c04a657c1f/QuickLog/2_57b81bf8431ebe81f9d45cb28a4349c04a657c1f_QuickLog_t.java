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
 	private String destination;
 	
 	private StringBuffer log;
 	
 	private QuickLog() {
 		logLevel = 0;
 		counter = 0;
 		
 		console = true;
 		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
 		Date date = new Date();
 		
 		destination = "log-" + format.format(date) + ".csv";
 		
 		log = new StringBuffer();
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
 	
 	public static void setDestination(String filepath) {
 		getInstance().destination = filepath;
 	}
 	
 	public static void log(String source, long time, String message) {
 		log(source, time, message, 0);
 	}
 	
 	public static void log(String source, long time, String message, int level) {
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
 	
 	private void internal_logToConsole(String source, long time, String message) {
 		System.err.println("[" + String.valueOf(++counter) + "] ["
 				+ source + "] [T=" + String.valueOf(time) + "] " + message);
 	}
 	
 	private void internal_logToFile(String source, long time, String message) {
 		String msg = String.valueOf(++counter) + ","
 				+ source + ","
 				+ String.valueOf(time) + ","
 				+ message
 				+ "\n";
 		
 		synchronized (this) {
 			log.append(msg);
 		}
 	}
 	
 	public static void writeToFile(String prefix) {
 		getInstance().internal_writeToFile(prefix);
 	}
 	
 	private void internal_writeToFile(String prefix) {
 		String filename = destination;
 		if (prefix != null)
 			filename = prefix + "-" + destination;
 		
 		print("Writing log to " + filename);
 		try {
 			FileWriter fileWriter = new FileWriter(filename, true);
 			BufferedWriter outBuffer = new BufferedWriter(fileWriter);
 			
 			synchronized(this) {
 				outBuffer.write(getInstance().log.toString());
 			}
 			outBuffer.close();
 			
 		} catch (Exception e) {
 			System.err.println("ERROR: Could not write to file " + filename);
 		}
 	}
 }
