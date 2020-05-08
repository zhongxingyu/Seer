 package com.conbit.FactbookParser;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 public class MyLogger {
 	
 	private Logger logger = Logger.getLogger("FactbookParser");
 	private static MyLogger myLogger = new MyLogger();
 	
 	public MyLogger(){
		PropertyConfigurator.configure("log4j.conf");
 	}
 	
 	public Logger getLogger(){
 		return logger;
 	}
 	
 	public static synchronized Logger getInstance(){
 		if(myLogger==null){
 			myLogger = new MyLogger();
 			return myLogger.getLogger();
 		}
 		return myLogger.getLogger();
 	}
 }
