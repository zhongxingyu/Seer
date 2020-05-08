 package edu.stockton;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Scanner;
 import org.json.JSONObject;
 import org.json.JSONException;
 
 /**
  * Number parsing and identifying utility.
  * 
  * @author Jico Baligod
  * @version 0.1a
  *
  */
 public class NumberParser {
 	private static JSONObject numDictionary;
 	private static String numSourcePath = "numbersJSON.txt";
 	
 	public NumberParser() throws FileNotFoundException, JSONException {
 		// Initialize numbers dictionary
 		FileReader sourceReader = new FileReader(numSourcePath);
 		Scanner jsonIn = new Scanner(sourceReader);
 		String jsonNumbers = "";
 		while(jsonIn.hasNext()) jsonNumbers += jsonIn.next();
 		numDictionary = new JSONObject(jsonNumbers);
 	}
 	
 	/**
 	 * Determines whether a string of text of natural language is
 	 * a number.
 	 * @param text English word of number.
 	 * @return True if the English word represents a number, false otherwise.
 	 */
 	public static boolean isNumber(String text) {
 		if(numDictionary.has(text)) return true;
 		else return false;
 	}
 	
 	/**
 	 * Converts a string of numbers in English word format to numeric format.
 	 * i.e. "one hundred four" to "104", "thirteen two two" to "1322", etc.
 	 * @param text The string to convert.
 	 * @return The corresponding numeric string representation.
 	 */
 	public static String toNum(String text) {
 		String[] keys = text.split("\\-|\\ ");
		LinkedList<String> numbers = new LinkedList<String>();
 		for(String k : keys) numbers.add(k);
		ListIterator<String> cursor = numbers.listIterator();
 		
 		String numString = "";
 		while(cursor.hasNext()) {
			String current = cursor.next();
 			current = current.toLowerCase();
 			try {
 				if(current.equalsIgnoreCase("hundred")) {
 					if(cursor.hasNext()) {
						String next = cursor.next();
						int nextVal = numDictionary.getJSONObject(next).getInt("value");
						int nextWeight = numDictionary.getJSONObject(next).getInt("weight");
 						int nextNum = nextVal * nextWeight;
 						if(nextNum < 10) numString += "0" + nextNum;
 						else if(nextNum < 20 || !cursor.hasNext()) numString += nextNum; 
 						else cursor.previous();
 					} else {
 						numString += "00";
 					}
 				} else {
					JSONObject currentObj = numDictionary.getJSONObject(current);
					numString += currentObj.getInt("value");
					/*
					if(cursor.hasNext()) {
						String next = cursor.next();
						JSONObject nextObj = numDictionary.getJSONObject(next);
						
					}
					*/
 				}
 			} catch (JSONException e) {
 				String error = e.getLocalizedMessage();
 				int f = error.indexOf("[");
 				int l = error.indexOf("]");
 				String unidentified = error.substring(f+1, l);
				return unidentified + " is not a number.";
 			}
 			
 			
 		}
 		
 		return numString;
 	}
 }
