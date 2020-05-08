 package edu.stockton;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.*;
 
 /**
  * General number interpreter.
  * Handles textual number representations and converts
  * them to numerical representations.
  *
  */
 public class NumberEngine {
 	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	private static DocumentBuilder loader;
 	private static Document numbersDoc;
 	private static Element tree;
 	private static NodeList numbersList;
 	private static String xmlFilename = "numbers.xml";
 	private static HashMap numbers;
 	private static boolean debug = false;
 	
 	/**
 	 * Initializes the engine and loads number library.
 	 * Optional to explicitly initialize, since methods
 	 * initialize the engine if it hasn't been.
 	 */
 	public static void init() {
 		try {
 			loader = factory.newDocumentBuilder();
 			numbersDoc = loader.parse(xmlFilename);
 			tree = numbersDoc.getDocumentElement();
 			numbersList = tree.getChildNodes();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		numbers = new HashMap();
 		for(int i = 0; i < numbersList.getLength(); i++) {
 			// Disregard any non-element nodes (i.e. #text nodes)
 			if(numbersList.item(i).getNodeType() != 1) continue;
 			NodeList numberNodes = numbersList.item(i).getChildNodes();
 			
 			// Number constructor params
 			int value = -1;
 			int weight = -1;
 			String text = "";
 			
 			// Retrieve number data 
 			for(int j = 0; j < numberNodes.getLength(); j++) {
 				if(numberNodes.item(j).getNodeType() != 1) continue;
 				
 				Element numberNode = (Element) numberNodes.item(j);
 				String xmlTag = numberNode.getTagName();
 				
 				if(xmlTag.equalsIgnoreCase("value")) value = Integer.parseInt(numberNode.getTextContent());
 				if(xmlTag.equalsIgnoreCase("weight")) weight = Integer.parseInt(numberNode.getTextContent());
 				if(xmlTag.equalsIgnoreCase("text")) text = numberNode.getTextContent();
 
 			}
 			
 			Number number = new Number(text, value, weight);
 			numbers.put(text, number);
 	
 		}
 		
 	}
 	
 	
 	/**
 	 * Checks to see if a word represents a number.
 	 * @param s The string of text
 	 * @return True if the word represents a number, false otherwise
 	 */
 	public static boolean isNumber(String s) {
 		if(numbers == null) init();
 		return numbers.containsKey(s);
 	}
 	
 	/**
 	 * Takes a textually represented number and converts it
 	 * to numeric format.
 	 * i.e. "fourteen hundred" converts to "1400"
 	 * @param text The number in word format
 	 * @return The number converted to numeric format
 	 */
 	public static String toNumeric(String text) {
 		if(numbers == null) init();
 		
 		String[] tokens = text.split("\\-|\\ ");
 
 		LinkedList<String> numberTokens = new LinkedList<String>();
 		for(String token : tokens) numberTokens.add(token);
 		ListIterator<String> cursor = numberTokens.listIterator();
 		
 		if(debug) System.out.println("\n[NE] parsing number: " + text);
 		
 		String numericString = "";
 		while(cursor.hasNext()) {
 			String current = cursor.next();
 			current = current.toLowerCase();
 			
 			if(debug) System.out.print("[NE] token: " + current + " => ");
 			
 			if(current.equalsIgnoreCase("thousand")) {
 				if(cursor.hasNext()) {
 					String next = cursor.next();
 					int nextVal = getValue(next);
 					int nextWeight = getWeight(next);
 					int nextNum = nextVal * nextWeight;
 					if(nextNum < 10 && !cursor.hasNext()) { 
 						numericString += "00" + nextNum;
					} else if(nextNum % 10 == 0) {
						if(!cursor.hasNext()) numericString += "0" + nextNum;
						else {
							numericString += "0";
							cursor.previous();
						}
					} else if(nextNum > 10) {
						if(!cursor.hasNext()) numericString += "0" + nextNum;
					} else cursor.previous();
 				} else numericString += "000";
 			} else if(current.equalsIgnoreCase("hundred")) {
 				if(cursor.hasNext()) {
 					String next = cursor.next();
 					int nextVal = getValue(next);
 					int nextWeight = getWeight(next);
 					int nextNum = nextVal * nextWeight;
 					if(nextNum < 10) {
 						numericString += "0" + nextNum;
 						if(debug) System.out.print("next token: " + nextNum + " => ");
 					}
 					else if(nextNum < 20 || !cursor.hasNext()) {
 						numericString += nextNum; 
 						if(debug) System.out.print("next token: " + nextNum + " => ");
 					}
 					else cursor.previous();
 				} else numericString += "00";
 				
 			} else {
 				if(!isNumber(current)) throw new ParseException("Not a recognized number");
 				numericString += getValue(current);
 				int currentWeight = getWeight(current);
 				if(currentWeight != 1) {
 					if(cursor.hasNext()) {
 						String next = cursor.next();
 						int nextWeight = getWeight(next);
 						if(nextWeight != 1) numericString += "0";
 						cursor.previous();
 					} else numericString += "0";
 				}
 				
 			}
 			
 			if(debug) System.out.println(numericString);
 		}
 		
 		return numericString;
 	}
 	
 	/**
 	 * Returns the value (not actual numeric representation) of the string
 	 * as specified in numbers.xml document
 	 * @param s String of the number word
 	 * @return integer value of number word, returns -1 if string is NAN
 	 */
 	public static int getValue(String s) {
 		if(numbers == null) init();
 		
 		Number number = (Number) numbers.get(s);
 		return number.getValue();
 		
 	}
 	
 	/**
 	 * Returns the weight of the word string
 	 * @param s String of the number word
 	 * @return integer weight value of the string, returns -1 if string is NAN
 	 */
 	public static int getWeight(String s) {
 		if(numbers == null) init();
 		
 		Number number = (Number) numbers.get(s);
 		return number.getWeight();
 	}
 	
 	/**
 	 * Sets debug option, which prints low-level process details
 	 * @param s true or false to set on/off
 	 */
 	public static void setDebug(boolean s) {
 		debug = s;
 	}
 
 }
