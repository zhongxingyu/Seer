 package com.karhatsu.suosikkipysakit.datasource.parsers;
 
 public class TimeParser {
 
 	public String format(String time) {
 		if (time.length() == 3) {
 			return "0" + time.substring(0, 1) + ":" + time.substring(1, 3);
		} else if (time.length() == 2) {
			return "00:" + time;
		} else if (time.length() == 1) {
			return "00:0" + time;
 		}
 		return time.substring(0, 2) + ":" + time.substring(2, 4);
 	}
 
 }
