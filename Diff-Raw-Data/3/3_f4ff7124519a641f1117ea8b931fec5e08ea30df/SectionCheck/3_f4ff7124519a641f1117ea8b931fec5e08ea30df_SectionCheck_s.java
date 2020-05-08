 package com.htmlthor;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import org.json.simple.JSONObject;
 
 
 public class SectionCheck {
 	
 	/* Just an empty constructor */
 	public SectionCheck() {
 	
 	}
 
 		/* This is the coding for the NEW error checking */
 		public JSONObject findErrors(List<String> fileContents) {
 		
 		
 			JSONObject errors = new JSONObject();
 			boolean openTag = false;
 			boolean closeTag =  false;
 			boolean startComment = false;
 			boolean whiteSpaceFlag = false;
 			boolean selfClosing = false;
 			boolean openAttr = false;
 			int tagStart = 0;
 			int attrStart = 0;
 			String tag = null;
 			int errorCount = 0;
 			
 			
 			/* Iterates over the lines of the given file. */
 			for (int i=0; i<fileContents.size(); i++) {
 
             	String nextLine = fileContents.get(i);
 				
 				/* Initialise the character array on the new line. */
 				char[] intermediate = nextLine.toCharArray();
 				CharArray charArray = new CharArray(nextLine.toCharArray());
 			
 				
 				//Check for open tags
 				for(int j=0; j<charArray.getLength(); j++) {
 					if(charArray.getChar(j)=='<') {
 						openTag = true;
 						tagStart = j+1;
 						// Check if opened a comment tag
 						if((charArray.getChar(j+1)=='!') && (charArray.getChar(j+2)=='-') && (charArray.getChar(j+3)=='-')) {
 							startComment = true;
 						}
 					}
 			
 					// As long as a comment tag is not open, another tag is open and 
 					// whitespace has not been reached to signal the end of the tag name:
 					if ((startComment==false) && (openTag==true)) {
 						if(whiteSpaceFlag==false) {
 							if((charArray.getChar(j)==' ')||(charArray.getChar(j)=='>')) {
 								if(charArray.getChar(j)==' ') {
 									whiteSpaceFlag = true;
 								}
 								
 								tag = charArray.getString(tagStart, j-1);
 								
 								// If it is not a valid tag
 								if(!Mysqlfunctions.checkValidTag(tag)) {
 									
 									// Note that some of these additions should use database references in future
 									JSONObject error = new JSONObject();
 									error.put("message", tag + " is not a valid HTML tag");
 									error.put("type", "syntax");
 									error.put("line", i+1);
 									error.put("col", j);
 									errors.put(errorCount, error);
 									errorCount += 1;
 									
 								}	
 								// If it a deprecated tag
 								else if(!Mysqlfunctions.isDeprecated(tag)) {
 									JSONObject error = new JSONObject();
 									error.put("message", tag + " tag is a deprecated tag");
 									error.put("type", "semantic");
 									error.put("line", i+1);
 									error.put("col", j);
 									errors.put(errorCount, error);
 									errorCount += 1;
 									
 								}
 								
 							
 								if(charArray.getChar(j)=='>') {
 									// Check if self closing
 									selfClosing = Mysqlfunctions.isSelfClosing(tag);
 								
 									/* Resets flag values and tag string */
 									closeTag = true;
 									openTag = false;
 									tag = null;
 								
 								
 									// Check if comment tag closed
 									if((charArray.getChar(j-1)=='-') && (charArray.getChar(j-2)=='-') && (startComment==true)) {
 										startComment = false;
 									}
 									else if (selfClosing) { 						
 										if(charArray.getChar(j-1) != '/') {
 										
 											JSONObject error = new JSONObject();
 											error.put("message", tag + " is self-closing but is not self closed");
 											error.put("type", "semantic");
 											error.put("line", i+1);
 											error.put("col", j);
 											errors.put(errorCount, error);
 											errorCount += 1;
 										}
 										selfClosing = false;
 									} else if (!selfClosing) { 						
 										if(charArray.getChar(j-1) == '/') {
 										
 											JSONObject error = new JSONObject();
 											error.put("message", tag + " is self-closed but is not allowed to be");
 											error.put("type", "semantic");
 											error.put("line", i+1);
 											error.put("col", j);
 											errors.put(errorCount, error);
 											errorCount += 1;
 										}
 										selfClosing = false;
 									}
 							
 								}	
 							}
 						}
 						else {
 							if (j != 0) {
 								if((charArray.getChar(j-1) == ' ') && (charArray.getChar(j) != ' ') && (charArray.getChar(j) != '>')) {
 									if( (Character.isLetter(charArray.getChar(j))) == true) {
 										attrStart = j;
 										openAttr = true;
 									}
 								}
 							}
 						}
 					}
 					
 					/* Will need to be moved into the startComment==false if loop */
 					/* Checks if attribute has been detected, need to check if
 					 * having code such as <img src = "... (space between
 					 * attribute and '=' validates). If so, will need to strip
 					 * out whitespace after attribute name before throwing it
 					 * into check. */
 					if(charArray.getChar(j) == '=') {
 						if(openAttr == true) {
 							String attr = charArray.getString(attrStart, j-1);
 							List<String> attrList = new ArrayList<String>();
 							
 							attrList = Mysqlfunctions.getAttr(tag);
 							if(!(attrList.contains(attr))) {
 								
 								JSONObject error = new JSONObject();
 								error.put("message", "This is not a valid attribute for that tag");
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								errors.put(errorCount, error);
 								errorCount += 1;
 							
 							}
 					
 						}
 					}
 					
 					/* Will need to be moved into the startComment==false if loop */
 					
 					
 				}
 			}
 			errors.put("count", errorCount);
 			return errors;
 		}			
 		
 		
 		
 		
 		
 		/**
 		 * Class for accessing the character array of the line of the HTML file
 		 * being parsed. 
 		 *
 		 * @author Ameer Sabri
 		 */
 		public class CharArray {
 				
 			private char[] charArray;
 				
 			/**
 			 * Constructor for the CharArray.
 			 */
 			public CharArray(char[] charArray) {
 				this.charArray = charArray;				
 			}
 					
 			/**
 			 * Returns the character at the index given.
 			 *
 			 * @param i the index of the character in the array.
 			 * @return the character at the specified index.
 			 */
 			public char getChar(int i) {
 				return charArray[i];
 			}
 					
 			/**
 			 * Returns the length of the CharArray.
 			 *
 			 * @return the length of the CharArray
 			 */
 			public int getLength() {
 				return charArray.length;
 			}
 					
 			/**
 			 * Returns a String that returns the tag given between the string
 			 * start and end index given in the array.
 			 *
 			 * @param tagStart the start index of the string
 			 * @param tagEnd the end index of the string
 			 * @return the string specified by the string's start and end index. 
 			 */
 			public String getString(int strStart, int strEnd) {
 					
 				StringBuilder str = new StringBuilder();
 				
 				/* Iterates through the array between the indices
 				 * and adds each character to the tag string. 
 				 */
 				for(int j=strStart; j < (strEnd + 1); j++) {
 					str.append(this.getChar(j));
 				}
 					
 				return str.toString();
 			}			
 		}
 
 }
