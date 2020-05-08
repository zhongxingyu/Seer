 package com.samteladze.delta.statistics.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 import android.os.Environment;
 
 public class LogManager
 {
 	public static final String	LOG_FILE_PATH	= "DeltaStatistics/log.txt";
 		
 	public static boolean LogExists()
 	{
 		File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE_PATH);
 		
 		return (logFile.exists() && !logFile.isDirectory());
 	}
 	
 	public static void CreateLog()
 	{
 		File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE_PATH);
 		
 		try
 		{
 			if (logFile.createNewFile())
 			{
 				LogManager.Log(LogManager.class.getSimpleName(), "Log file was created");
 			}
 		} 
 		catch (IOException e)
 		{
 			e.printStackTrace(System.err);
 		}
 	}
 	
 	public static void Log(String tag, String logMessage)
 	{		
 		File logFile = new File(Environment.getExternalStorageDirectory(), LOG_FILE_PATH);
 		
 		try
 		{
 			BufferedWriter out = new BufferedWriter(new FileWriter(logFile, logFile.exists()));
 
			out.write(new Date().toString() + " | " + tag + " : " + logMessage);
			out.newLine();
 
 			out.close();
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace(System.err);			 
 		}
 	}
 }
