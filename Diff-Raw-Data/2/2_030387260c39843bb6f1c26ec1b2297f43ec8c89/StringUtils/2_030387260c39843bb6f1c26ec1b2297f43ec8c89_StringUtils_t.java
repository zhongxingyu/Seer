 package com.example.helloandroid.utils;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 public class StringUtils {
 	private StringUtils() { }
 
 	public static String join(String[] strings, String delimiter, String wrapper) {
 	     StringBuilder builder = new StringBuilder();
 	     for (int i = 0, l = strings.length; i < l; i++) {
 	    	 if (wrapper != null) {
 	    		 builder.append(wrapper);
 	    	 }
 	    	 builder.append(strings[i]);
 	    	 if (wrapper != null) {
 	    		 builder.append(wrapper);
 	    	 }
 	         if (i == l-1) {
 	           break;                  
 	         }
 	         builder.append(delimiter);
 	     }
 	     return builder.toString();		
 	}
 	
 	public static String join(Collection<?> s, String delimiter) {
 	     StringBuilder builder = new StringBuilder();
	     Iterator<?> iter = s.iterator();
 	     while (iter.hasNext()) {
 	         builder.append(iter.next());
 	         if (!iter.hasNext()) {
 	           break;                  
 	         }
 	         builder.append(delimiter);
 	     }
 	     return builder.toString();
 	 }
 
 	
 	public static String join(String[] strings, String delimiter) {
 		return join(strings, delimiter, null);
 	}
 }
