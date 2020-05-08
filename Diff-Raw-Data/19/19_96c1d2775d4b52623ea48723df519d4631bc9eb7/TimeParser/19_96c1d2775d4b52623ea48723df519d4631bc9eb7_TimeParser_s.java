 package com.karhatsu.suosikkipysakit.datasource.parsers;
 
 public class TimeParser {
 
 	public String format(String time) {
 		if (time.length() == 3) {
 			return "0" + time.substring(0, 1) + ":" + time.substring(1, 3);
 		}
 		return time.substring(0, 2) + ":" + time.substring(2, 4);
 	}
 
 }
