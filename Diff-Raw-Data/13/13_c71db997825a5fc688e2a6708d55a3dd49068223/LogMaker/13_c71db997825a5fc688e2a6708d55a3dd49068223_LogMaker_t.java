 package controllers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import models.User;
 
 public class LogMaker {
 
 	public static Boolean isLogEnabled = true;
 	
 	public static int maxAllowedLogDays = 10;
 
 	
 	//Each day a new directory is created, so we can manage to delete old logs
 	public static void log(String logType, User signedUser, String message) {
 		if (isLogEnabled) {
 			
 			Date date = new Date();
 			
 			@SuppressWarnings("deprecation")
 			File logDir = new File("logs");
 			if (!logDir.exists())
 			  {
 				logDir.mkdir();  
 			  }
 			
 			File todayDir = new File(""+logDir+"\\"+date.getDate() +"_" +date.getMonth()+"_" +date.getYear() );
 
 			  // if the directory does not exist, create it
 			  if (!todayDir.exists())
 			  {
 			    boolean result = todayDir.mkdir();  
 			    if(result){    
 			       System.out.println("todayDir created");  
 			     }
 
 			  }
 
			File logFile = new File(todayDir + "\\" + logType + "_log.txt");
 
 			try {
 				FileWriter fileWritter = new FileWriter(logFile, true);
 				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				if (signedUser != null)
					bufferWritter.write(signedUser.id + ": " + date + ": "
							+ message + "\n");

				else
					bufferWritter.write("No id" + ": " + date + ": " + message
							+ "\n");
 
 				bufferWritter.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
