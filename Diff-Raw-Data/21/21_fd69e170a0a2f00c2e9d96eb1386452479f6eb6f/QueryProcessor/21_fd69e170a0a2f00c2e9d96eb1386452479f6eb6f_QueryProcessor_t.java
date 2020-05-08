 package com.develogical;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class QueryProcessor {
 
     public String process(String query) {
     	System.out.println(query);    	
     	
         if (query.contains("SPA2012")) {
             return "SPA is a conference";
         } else if (query.contains("Eendje")) {
         	return "Fantastische workshops!";
         } else if (query.contains("Lunch")) {
         	return "Didn't you have enough?";
         } else if (query.contains("plus")) {
         	return addUp(query);
         } else if (query.contains("largest")) {
         	return findLargest(query);
         }
         return "";
     }
 
 	private String findLargest(String query) {
 		List<Integer> numbers = new ArrayList<Integer>();
 
 		Pattern p = Pattern.compile("\\d+");
 		Matcher m = p.matcher(query); 
 		while (m.find()) {
 		   numbers.add(Integer.parseInt(m.group()));
 		}
		Integer response = 0;
		for (Integer number: numbers) {
			if (number > response) response = number;
		}
		return Integer.toString(response);
 	}
 
 	private String addUp(String query) {
 		String[] pieces = query.split(" ");
 		int total = 0;
 		for (String string : pieces) {
 			try {
 				total += Integer.parseInt(string);
 			} catch (Exception e) {}
 		}
 		return Integer.toString(total);
 	}
 }
