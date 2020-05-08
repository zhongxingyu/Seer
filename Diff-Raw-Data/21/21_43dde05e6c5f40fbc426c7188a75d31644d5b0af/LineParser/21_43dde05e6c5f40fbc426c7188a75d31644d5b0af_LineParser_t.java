 package com.karhatsu.suosikkipysakit.datasource.parsers;
 
 public class LineParser {
 
 	private static final String METRO = "M";
	private static final String FERRY = "L";
 
 	public String format(String lineCode) {
 		if (isJoukoLine(lineCode)) {
 			return formatJoukoLine(lineCode);
 		} else if (isLocalTrain(lineCode)) {
 			return formatLocalTrain(lineCode);
 		} else if (isMetro(lineCode)) {
 			return METRO;
		} else if (isFerry(lineCode)) {
			return FERRY;
 		}
 		return formatLine(lineCode);
 	}
 
 	private boolean isJoukoLine(String lineCode) {
 		return lineCode.substring(4, 5).equals("J");
 	}
 
 	private String formatJoukoLine(String lineCode) {
 		return lineCode.substring(2, 5);
 	}
 
 	private boolean isLocalTrain(String lineCode) {
 		return lineCode.startsWith("3");
 	}
 
 	private String formatLocalTrain(String lineCode) {
 		return lineCode.substring(4, 5);
 	}
 
 	private boolean isMetro(String lineCode) {
 		return lineCode.startsWith("1300");
 	}
 
	private boolean isFerry(String lineCode) {
		return lineCode.startsWith("1019");
	}

 	private String formatLine(String lineCode) {
 		return stripLeadingZeros(lineCode.substring(1, 5).trim());
 	}
 
 	private String stripLeadingZeros(String lineCode) {
 		if (lineCode.startsWith("00")) {
 			return lineCode.substring(2, lineCode.length());
 		} else if (lineCode.startsWith("0")) {
 			return lineCode.substring(1, lineCode.length());
 		}
 		return lineCode;
 	}
 }
