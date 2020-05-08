 package com.github.norwae.whatiread.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Strings {
 	
 
 	public static CharSequence join(String sep, List<String> strings) {
		if (strings == null) {
			return "";
		}
 		return join(sep, strings.toArray(new String[strings.size()]));
 	}
 	
 	public static CharSequence join(String sep, String... strings) {
 		StringBuilder temp = new StringBuilder();
 		String comma = "";
 		
 		for (String str : strings) {
 			temp.append(comma).append(str);
 			comma = sep;
 		}
 		
 		return temp;
 	}
 
 	public static List<String> split(String sep, String string) {
 		List<String> result = new ArrayList<String>();
 		for (String str : string.split(sep)) {
 			result.add(str);
 		}
 		return result;
 	}
 
 }
