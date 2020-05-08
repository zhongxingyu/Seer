 package com.pjab.apper;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.Connection;
 import java.util.Properties;
 
 
 import com.pjab.apper.AppData;
 import com.pjab.apper.AppConfig;
 import com.pjab.apper.AppParser;
 import com.pjab.apper.Crawler;
 import com.pjab.apper.DataMapping;
 import com.pjab.apper.DatabaseConfig;
 import com.pjab.apper.DatabaseUtils;
 import com.pjab.apper.URLInfo;
 import com.pjab.apper.Utils;
 
 public class AppFetcher {
 	
   	private final Properties props;
 	private DataMapping dm;
 
 	public AppFetcher ()
 	{
 	  	AppConfig aconfig = AppConfig.getInstance();
 		props = aconfig.getProperties();
 		dm = aconfig.getMapping("itunes_web_html_mapping");
 
 	}
 
 
 	public boolean fetchApp(String appUrl)
 	{
 		ConcurrentLinkedQueue<URLInfo>processQueue = new ConcurrentLinkedQueue<URLInfo>();
 		ConcurrentHashMap<String, Boolean> seenURLs = new ConcurrentHashMap<String, Boolean>();
 		Connection conn = DatabaseConfig.getInstance().getConnection();
 		
 		
 		URLInfo newURLInfo = new URLInfo(appUrl,"",0);
 		String outputAppDir = props.getProperty(ApperConstants.OUTPUT_APP_DIR);
 		String appDataDir = props.getProperty(ApperConstants.OUTPUT_DATA_DIR);
 		
 		Crawler crawler = new Crawler(processQueue,seenURLs,0,outputAppDir);
 		String fileName = crawler.crawl(newURLInfo);
 		System.out.println(fileName);
 	
 		if(fileName.length() == 0)
 		  	return false;
 		
 
 		AppParser parser = new AppParser(outputAppDir + "/" + fileName);
     	try {
     		AppData appData = parser.parseWithDataMappings(dm);
    		String appDataFile = appDataDir + fileName;
     		System.out.println(appDataFile);
     		Utils.printToFile(appDataFile, appData.toJSON());
     		
     		System.out.println("Writing to database");
     		DatabaseUtils.insertAppInfoIfNotExists(conn, appData);
 			return true;
     	}catch (Exception e)
     	{
     		System.out.println("Exception thrown for file" + e.getMessage());
     		e.printStackTrace();
     		//break;
     	}
 		return false;
 	}
 
 	
 	public static void main(String [] args) throws Exception
 	{
 	  	AppConfig aconfig = AppConfig.getInstance();
 		aconfig.init(args);
 	
 		Properties prop = Utils.loadProperties("default.properties");		
 		AppFetcher fetcher = new AppFetcher();
 		
 		String seedURL = "http://www.appvamp.com/ihandy-flashlight-free/";
 	
 		fetcher.fetchApp(seedURL);
 	}
 
 }
