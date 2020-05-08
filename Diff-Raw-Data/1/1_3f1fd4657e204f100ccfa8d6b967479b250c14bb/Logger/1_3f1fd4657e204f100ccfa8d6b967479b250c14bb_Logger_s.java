 package de.phaenovum.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.Date;
 
 /**
  * This class provides an simple loggin solution
  * @author marcel
  *
  */
 public class Logger {
 	
 	private String loggerPath;
 	private File loggerFile;
 	private Timestamp time;
 	private BufferedWriter writer;
 	
 	/**
 	 * Constructor taking the path to the log File as an argument
 	 * @param loggerPath
 	 */
 	public Logger(String loggerPath){
 		this.loggerPath = loggerPath;
 		// Create file
 		loggerFile = new File(this.loggerPath);
 		// Create Timeobject
 		time = new Timestamp(new Date().getTime());
 		
 		// Open the writer
 		try {
 			writer = new BufferedWriter(new FileWriter(loggerFile));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Function to get current timestamp
 	 * @return
 	 */
 	public String getCurrentTime(){
 		return time.toString();
 	}
 
 	/**
 	 * Methode to log Informations to file
 	 * @param message
 	 * @param error
 	 */
 	public void logMessage(String message,boolean error){
 		String logMessage;
 		if(error){
 			logMessage = "[ERROR] "+ message;
 		}else{
 			logMessage = "[INFO] " + message;
 		}
 		// Write message to file
 		try {
 			writer.write(getCurrentTime()+"\t"+logMessage);
 			writer.newLine();
 			System.out.println(getCurrentTime()+"\t"+logMessage);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
