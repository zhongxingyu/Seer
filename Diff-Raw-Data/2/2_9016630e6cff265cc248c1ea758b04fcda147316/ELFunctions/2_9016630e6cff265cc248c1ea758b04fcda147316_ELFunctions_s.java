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
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.log4j.spi.LocationInfo;
 
 /**
  * Functions for use in EL
  */
 public class ELFunctions {
 	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 	
 	/**
 	 * Formats a timestamp
 	 * @param timestamp the timestamp value
 	 * @return the formatted date time string
 	 */
 	public static String formatTimestamp(Long timestamp) {
 		return dateFormat.format(new Date(timestamp));
 	}
 	
 	/**
 	 * Formats a location info from a logging event
 	 * @param locInfo the location info
 	 * @return the formatted location string
 	 */
 	public static String formatLocInfo(LocationInfo locInfo) {
 		String clazz = locInfo.getClassName();
 		int lastPeriod = clazz.lastIndexOf('.');
 		if (lastPeriod >= 0)
 			clazz = clazz.substring(lastPeriod + 1);
 		
 		return clazz + "." + locInfo.getMethodName() + "(" + locInfo.getLineNumber() + ")";
 	}
 	
 	/**
 	 * Formats a logging event message by replacing newlines and tabs
 	 * @param the message
 	 * @return the formatted message string
 	 */
 	public static String formatMessage(String msg) {
 		return msg.replace("\n", "<br/>");
 	}
 }
