 package com.nsn.uwr.panio;
 
 import java.text.DateFormat;
 
 public class Logger {
 
 	public void loggMessage(String message, ELoggLevel loglevel) {
 
 		DateFormat datetimeFormatter = DateFormat.getDateTimeInstance();
 
 		java.util.Date date = new java.util.Date();
 		String dateString = datetimeFormatter.format(date);
 
 		System.out.println("[] " + dateString + " [" + loglevel + "] "
 				+ message);
 
 	};
 
 	public void loggWarning(String message) {
 
 		loggMessage(message, ELoggLevel.WARNING);
 
 	};
 
 	public void loggInfo(String message) {
 
 		loggMessage(message, ELoggLevel.INFO);
 
 	};
 
 	public void loggError(String message) {
 
 		loggMessage(message, ELoggLevel.ERROR);
 
 	};
 
 }
