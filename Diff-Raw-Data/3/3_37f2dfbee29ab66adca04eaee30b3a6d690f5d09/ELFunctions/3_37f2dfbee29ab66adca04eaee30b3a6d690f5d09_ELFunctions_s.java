 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.logmanager.web.taglib;
 
 import java.text.DecimalFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.openmrs.module.logmanager.impl.EventProxy;
 
 /**
  * Functions for use in EL
  */
 public class ELFunctions {
 	
 	private static final Format dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	private static final Format timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
 	private static final Format secondsFormat1 = new DecimalFormat("#0.000s");
 	private static final Format secondsFormat2 = new DecimalFormat("#0.00s");
 	
 	/**
 	 * Formats a time stamp
 	 * @param timeStamp the time stamp value
 	 * @param asDateAndTime true if should be formatted as date and time
 	 * @return the formatted date time string
 	 */
 	public static String formatTimeStamp(Long timeStamp, Boolean asDateAndTime) {
 		Date date = new Date(timeStamp);
 		return asDateAndTime ? dateFormat.format(date) : timeFormat.format(date);
 	}
 	
 	/**
 	 * Formats a time diff value
 	 * @param timeDiff the timeDiff value
 	 * @return the formatted string
 	 */
 	public static String formatTimeDiff(Long timeDiff) {
 		float seconds = timeDiff / 1000.0f;
 		
 		if (timeDiff < 1000)
 			return secondsFormat1.format(seconds);
 		else if (timeDiff < 60000)
 			return secondsFormat2.format(seconds);
 		else
 			return (int)seconds + "s";
 	}
 	
 	/**
 	 * Formats the location info from a logging event
 	 * @param event the event
 	 * @return the formatted location string
 	 */
 	public static String formatLocInfo(EventProxy event) {
		if (event.getClassName().equals("?") || event.getMethodName().equals("?"))
 			return "Unknown";
 		
 		String clazz = event.getClassName();
 		int lastPeriod = clazz.lastIndexOf('.');
 		if (lastPeriod >= 0)
 			clazz = clazz.substring(lastPeriod + 1);
 		
 		return clazz + "." + event.getMethodName() + "(" + event.getLineNumber() + ")";
 	}
 	
 	/**
 	 * Formats a logging event message by replacing newlines and escaping HTML chars
 	 * @param msg the message
 	 * @return the formatted message string
 	 */ 
 	public static String formatMessage(String msg) {
 		return msg.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
 	}
 	
 	/**
 	 * Checks if a line from stack trace is referring to OpenMRS code
 	 * @param line the line
 	 * @return true if its from OpenMRS
 	 */ 
 	public static Boolean isStackLineFromOpenMRS(String line) {
 		return (line != null) ? line.startsWith("\tat org.openmrs.") : false;
 	}
 }
