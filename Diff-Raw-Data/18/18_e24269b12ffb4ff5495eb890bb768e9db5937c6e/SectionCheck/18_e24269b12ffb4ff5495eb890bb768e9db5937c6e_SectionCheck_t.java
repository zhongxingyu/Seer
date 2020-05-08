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
 		
 			Mysqlfunctions sql = new Mysqlfunctions();
 		
 			JSONObject errors = new JSONObject();
 			boolean openTag = false;
 			boolean closeTag =  false;
 			boolean startComment = false;
 			boolean startPhp = false;
 			boolean whiteSpaceFlag = false;
 			boolean selfClosing = false;
 			boolean openAttr = false;
 			int tagStart = 0;
 			int attrStart = 0;
 			String tag = "";
 			int errorCount = 0;
 			boolean endTagName = false;
 			boolean faultyTag = false;
 			boolean tagChecked = false;
 			boolean selfClosingError = false;
 			
 			// variables used for escaping script/style tag content
 			boolean openScript = false;
 			boolean openStyle = false;
 			int endEscapedTagPhase = 0; // used to find </script> and </style>
 			// 0: < not found
 			// 1: < found, / not found - whitespace permitted
 			// 2: / found, script/style not found - whitespace permitted
 			// 3: s found - whitespace not permitted
 			// 4: c/t found
 			// 5: r/y found
 			// 6: i/l found
 			// 7: p/e found
 			// 8: t found - looking for >
 			
 			
 			JSONObject error;
 			
 			
 			/* Iterates over the lines of the given file. */
 			for (int i=0; i<fileContents.size(); i++) {
 			
 			
 
             	String nextLine = fileContents.get(i);
 				
 				/* Initialise the character array on the new line. */
 				char[] intermediate = nextLine.toCharArray();
 				CharArray charArray = new CharArray(nextLine.toCharArray());
 			
 				
 				//Check for open tags
 				for(int j=0; j<charArray.getLength(); j++) {
 				
 				
 					// ==============================================
 					// check whether a style tag is open, in which case content will be unchecked
 					// until </style> is found
 					// ==============================================
 					if (openStyle) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								endEscapedTagPhase = 2;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 's') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 't') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'y') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'l') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == 'e') {
 								// look for next char
 								endEscapedTagPhase = 7;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 7) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								openStyle = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					// ==============================================
 					// check whether a style tag is open, in which case content will be unchecked
 					// until </script> is found
 					// ==============================================
 					if (openScript) {
 						if (endEscapedTagPhase == 0) {
 							if (charArray.getChar(j) == '<') {
 								// look for next char
 								endEscapedTagPhase = 1;
 							}
 						} else if (endEscapedTagPhase == 1) {
 							if (charArray.getChar(j) == '/') {
 								// look for next char
 								endEscapedTagPhase = 2;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 2) {
 							if (charArray.getChar(j) == 's') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'c') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'r') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'i') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == 'p') {
 								// look for next char
 								endEscapedTagPhase = 7;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 7) {
 							if (charArray.getChar(j) == 't') {
 								// look for next char
 								endEscapedTagPhase = 8;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 8) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								openScript = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					// ==============================================
 					// Script/style checking done
 					// ==============================================
 				
 				
 				
 					if(charArray.getChar(j)=='<') {
 						
 						openTag = true;
 						while (charArray.getChar(j+1) == ' ') {
 							j = j+1;
 						}
 						tagStart = j+1;
 						// Check if opened a comment tag
 						if (charArray.getLength() >= j+4) {
 							if((charArray.getChar(j+1)=='!') && (charArray.getChar(j+2)=='-') && (charArray.getChar(j+3)=='-')) {
 								startComment = true;
 							}
 						}
 						
 						// Check if opened a php tag
 						if (charArray.getLength() >= j+5) {
 							if((charArray.getChar(j+1)=='?') && (charArray.getChar(j+2)=='p') && (charArray.getChar(j+3)=='h') && (charArray.getChar(j+4)=='p')) {
 								startPhp = true;
 							}
 						}
 						
 						j = j+1;
 						
 						
 						
 								
 						
 					}
 			
 			
 								
 					
 			
 			
 					// As long as a comment tag is not open, another tag is open and 
 					// whitespace has not been reached to signal the end of the tag name:
 					if ((startComment==false) && (openTag==true) && (startPhp==false)) {
 						if(whiteSpaceFlag==false) {
 							if((charArray.getChar(j)==' ')||(charArray.getChar(j)=='>')) {
 								if(charArray.getChar(j)==' ') {
 									whiteSpaceFlag = true;
 								}
 								if (openAttr == true) {
 									openAttr = false;
 									// check attribute stuff here
 								}
 								if (!endTagName) {
 									tag = charArray.getString(tagStart, j-1);
 									
 									if (tag.substring(0,1).equals("/")) {
 										
 										tag = tag.substring(1);
 									}
 									endTagName = true;
 								}
 								
 								
 								
 								
 								
 								if (!tagChecked) {
 								
 								
 								
 									// If it is not a valid tag
 									if(!sql.checkValidTag(tag)) {
 										
 										// Note that some of these additions should use database references in future
 										error = new JSONObject();
 										error.put("message", tag + " is not a valid HTML tag");
 										error.put("type", "syntax");
 										error.put("line", i+1);
 										error.put("col", j);
										error.put("errorExcerpt", tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 										faultyTag = true;
 										
 									}	
 									// If it a deprecated tag
 									else if(!sql.isDeprecated(tag)) {
 										error = new JSONObject();
 										error.put("message", tag + " tag is a deprecated tag");
 										error.put("type", "deprecated");
 										error.put("line", i+1);
 										error.put("col", j);
										error.put("errorExcerpt", tag);
 										errors.put(errorCount, error);
 										errorCount += 1;
 										faultyTag = true;
 									}
 									tagChecked = true;
 								}
 								
 							
 								if(charArray.getChar(j)=='>') {
 								
 								
 									/* ################################################# */
 								/* ################################################# */
 								/* ################################################# */
 								/* ################################################# */
 								/* ################################################# */
 								/*
 								error = new JSONObject();
 								error.put("message", tagStart + " " + j + " " + tag + " is not a valid HTML tag");
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								*/
 								/* ################################################# */
 								/* ################################################# */
 								/* ################################################# */
 								/* ################################################# */
 								/* ################################################# */
 								
 								
 									// Check if self closing
 									selfClosing = sql.isSelfClosing(tag);
 								
 								
 									int closingChecker = j-1;
 									while (charArray.getChar(closingChecker) == ' ' && closingChecker > 0) {
 										
 										closingChecker = closingChecker-1;
 										
 									}
 								
 								
 									// Check if comment tag closed
 									
 									if (selfClosing) { 		
 										
 										if(charArray.getChar(closingChecker) != '/') {
 											
 											selfClosingError = true;
 											
 											if (selfClosingError) {
 												error = new JSONObject();
 												error.put("message", tag + " is self-closing but is not self closed.");
 												error.put("type", "warning");
 												error.put("line", i+1);
 												error.put("col", closingChecker);
												error.put("errorExcerpt", tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										} else {
 										
 											selfClosingError = false;
 											if (closingChecker > 0) {
 												if (charArray.getChar(closingChecker-1) != ' ') {
 													selfClosingError = true;
 												}
 											}
 											
 											if (selfClosingError) {
 												error = new JSONObject();
 												error.put("message", tag + " is self-closing but is not self closed. You may want to include a space before the closing '/'.");
 												error.put("type", "warning");
 												error.put("line", i+1);
 												error.put("col", closingChecker);
												error.put("errorExcerpt", tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										
 										}
 										selfClosing = false;
 									} else if (!selfClosing) { 						
 										if(charArray.getChar(closingChecker) == '/') {
 											
 											selfClosingError = true;
 											
 											if (closingChecker > 0) {
 												if (charArray.getChar(closingChecker-1) != ' ') {
 													selfClosingError = false;
 												}
 											}
 											
 											if (selfClosingError) {									
 										
 												error = new JSONObject();
 												error.put("message", tag + " is self-closed but is not allowed to be.");
 												error.put("type", "semantic");
 												error.put("line", i+1);
 												error.put("col", closingChecker);
												error.put("errorExcerpt", tag);
 												errors.put(errorCount, error);
 												errorCount += 1;
 											}
 										}
 										selfClosing = false;
 									}
 									
 									if (tag.equals("script")) {
 										openScript = true;
 									}
 									
 									else if (tag.equals("style")) {
 										openStyle = true;
 									}
 							
 									/* Resets flag values and tag string */
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}	
 							}
 						
 						}
 						else {
 							if (j != 0) {
 									
 								if((charArray.getChar(j) != '>')) {
 									if ((charArray.getChar(j-1) == ' ') && (charArray.getChar(j) != ' ')) {
 										if( (Character.isLetter(charArray.getChar(j))) == true) {
 											attrStart = j;
 											openAttr = true;
 										
 										}
 									}
 								} else {
 									j = j - 1;
 								}
 							}
 							whiteSpaceFlag = false;
 						}
 						
 						
 					}
 					if (startComment == true && j > 2) {
 						if((charArray.getChar(j-2)=='-') && (charArray.getChar(j-1)=='-') && (charArray.getChar(j)=='>')) {
 							startComment = false;
 						}
 					}
 					if (startPhp == true && j > 1) {
 						if((charArray.getChar(j-1)=='?') && (charArray.getChar(j)=='>')) {
 							startPhp = false;
 						}
 					}
 					
 					// If an attribute has been reached and finished via the = operator
 					if(charArray.getChar(j) == '=') {
 						if(openAttr == true) {
 							String attr = charArray.getString(attrStart, j-1);
 							List<String> attrList = new ArrayList<String>();
 							attrList = sql.getAttr(tag);
 							boolean validAttr = false;
 							for (int a = 0; a < attrList.size(); a++) {
 								if(attrList.get(a).equals(attr)) {
 									validAttr = true;	
 								}
 							
 							}
 							if (!validAttr) {
 								error = new JSONObject();
 								// Actual DB reference test
 								error.put("message", attr + " " + getErrMsg(23));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
								error.put("errorExcerpt", attr);
 								errors.put(errorCount, error);
 								errorCount += 1;
 							}
 					
 						}
 					}
 					
 					
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
			 * @param strStart the start index of the string
			 * @param strEnd the end index of the string
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
