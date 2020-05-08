 package org.irmacard.personalisation;
 
 import java.io.BufferedOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Date;
 
 public class Logger {
 	private static final String LOG_FILENAME = "IrmaWriter.log";
 	
 	public static void log(String message, Exception e) {
 		PrintStream stream = null;
 		try {
 			stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(LOG_FILENAME, true)));
			stream.append(String.format("===========================================%n" +
										"%tc: %s%n", new Date(), message));
 			e.printStackTrace(stream);
			stream.append(String.format("%n%n"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		finally {
 			if(stream != null) {
 				stream.close();
 			}
 		}
 	}
 
 	public static void log(String message) {
 		PrintStream stream = null;
 		try {
 			stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(LOG_FILENAME, true)));
			stream.append(String.format("===========================================%n" +
										"%tc: %s%n", new Date(), message));
			stream.append(String.format("%n%n"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		finally {
 			if(stream != null) {
 				stream.close();
 			}
 		}
 	}
 }
