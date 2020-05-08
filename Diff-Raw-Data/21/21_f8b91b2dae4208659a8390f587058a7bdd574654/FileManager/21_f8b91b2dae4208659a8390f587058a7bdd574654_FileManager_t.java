 
 package com.samteladze.delta.statistics.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import android.os.Environment;
 
 public class FileManager
 {
 	// Files with statistics to send to the server
 	public static final String	APP_STAT_FILE_PATH	= "DeltaStatistics/Statistics/AppStat.txt";
 	public static final String	NET_STAT_FILE_PATH	= "DeltaStatistics/Statistics/NetStat.txt";
 	// Files with statistics to e-mail to the user
 	public static final String USER_APP_STAT_FILE_PATH = "DeltaStatistics/Statistics/UserAppStat.txt";
 	public static final String USER_NET_STAT_FILE_PATH	= "DeltaStatistics/Statistics/UserNetStat.txt";
 	
 	private static final String STAT_FOLDER_PATH = "DeltaStatistics/Statistics";
 
 	// File path should point to a file on the device's external storage
	public static void SaveStatistics(String statistics, String statisticsFilePath, boolean replaceFlag)
 	{
 		File statFile = new File(Environment.getExternalStorageDirectory(), statisticsFilePath);
 
 		try
 		{
			BufferedWriter out = new BufferedWriter(new FileWriter(statFile, !replaceFlag));
 
 			out.write(statistics);
 
 			out.close();
 			
 			LogManager.Log(FileManager.class.getSimpleName(), "Statistics was saved to " + statisticsFilePath);
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace(System.err);
 			
 			LogManager.Log(FileManager.class.getSimpleName(), "ERROR! Could not save statistics to " + statisticsFilePath);
 			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
 		}
 	}
 	
 	// File path should point to a file on the device's external storage
 	public static void CleanFile(String filePath)
 	{
 		File statFile = new File(Environment.getExternalStorageDirectory(), filePath);
 		
 		try
 		{
 			BufferedWriter out = new BufferedWriter(new FileWriter(statFile, false));
 			out.close();
 			
 			LogManager.Log(FileManager.class.getSimpleName(), "File " + filePath + " was cleaned");
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace(System.err);
 			
 			LogManager.Log(FileManager.class.getSimpleName(), "ERROR! Could not clean file " + filePath);
 			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
 		}
 	}
 
 	public static boolean HasFolderStructure()
 	{
 		File statDataFolder = new File(Environment.getExternalStorageDirectory(), STAT_FOLDER_PATH);
 
 		// Test statistics data folder
 		if (!statDataFolder.exists() || !statDataFolder.isDirectory())
 		{
 			return (false);
 		}
 
 		return (true);
 	}
 
 	public static void CreateFolderStructure()
 	{
 		File statDataFolder = new File(Environment.getExternalStorageDirectory(), STAT_FOLDER_PATH);
 
 		// Create folders structure
 		if (!statDataFolder.exists())
 		{
 			if (statDataFolder.mkdirs())
 			{		        	
 				LogManager.Log(FileManager.class.getSimpleName(), "Folders structure was created.");
 			}
 		}				
 	}
 	
 	public static File GetFile(String filePath)
 	{
 		return (new File(Environment.getExternalStorageDirectory(), filePath));
 	}
 }
