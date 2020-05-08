 package com.test9.irc.display;
 
 import java.util.Comparator;
 
 public class MyComparator implements Comparator<String> {
 
 	public int compare(String o1, String o2) {
 		char o1c = o1.charAt(0);
 		char o2c = o2.charAt(0);
		
 		if((!Character.isLetter(o1c)) && (!Character.isDigit(o1c)))
 		{
 			return(compare(o1.substring(1), o2));
 		}
		
 		if((!Character.isLetter(o2c)) && (!Character.isDigit(o2c)))
 		{
 			return(compare(o1, o2.substring(1)));
 		}
		
 		return(o1.toLowerCase().compareTo(o2.toLowerCase()));
 	}
 
 }
