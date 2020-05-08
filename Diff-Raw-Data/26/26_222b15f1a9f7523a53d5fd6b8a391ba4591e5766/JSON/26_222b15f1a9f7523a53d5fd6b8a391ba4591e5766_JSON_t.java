 package com.chaman.util;
 
 public class JSON {
 
 	public static String GetValueFor(String key, String json) {
 		
 		String result = null;
 		
 		try {
 			
 			if (json.indexOf(key) != -1) {
 				
				int keyIndex 	= json.indexOf(key + "\":");
				
				int startIndex	= keyIndex + key.length() + 2;
				int endIndex 	= json.indexOf("\"", startIndex + 1);
				
				result = json.substring(startIndex, endIndex);
				result = result.replace("\"", "");
				
				if (result.endsWith(",")) {
					
					result = result.substring(0, result.length() - 1);
				}

				// old code was not efficient EX: {name:aaaaidaaaa,street:bbbb} returns bbbb
			/*if (json.indexOf(key) != -1) {
				
 				int keyIndex 	= json.indexOf(key);
 				int columnIndex = json.indexOf(":", keyIndex);
 				
 				int startIndex	= columnIndex + 1;
 				int endIndex 	= json.indexOf("\"", startIndex + 1);
 				
 				result = json.substring(startIndex, endIndex);
 				result = result.replace("\"", "");
 				
 				if (result.endsWith(",")) {
 					
 					result = result.substring(0, result.length() - 1);
				}*/
 			}
 		} catch (Exception ex) {
 			
 			// value or key not found
 		}
 		
 		return result;
 	}
 }
