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
 			int tagStart = 0;
 			String tag = "";
 			int errorCount = 0;
 			boolean endTagName = false;
 			boolean faultyTag = false;
 			boolean tagChecked = false;
 			boolean selfClosingError = false;
 			boolean openDoctype = false;
 			
 			List<String> singularTags = new ArrayList<String>();
 			List<String> ids = new ArrayList<String>();
 			
 			// variables used for escaping script/style tag content
 			boolean openScript = false;
 			boolean openStyle = false;
 			boolean openSvg = false;
 			boolean openMath = false;
 			boolean openCode = false;
 			int endEscapedTagPhase = 0; // used to find </script> and </style>
 			// 0: < not found
 			// 1: < found, / not found - whitespace permitted
 			// 2: / found, script/style not found - whitespace permitted
 			// 3: s/m/c found - whitespace not permitted
 			// 4: c/t/a/v/o found
 			// 5: r/y/g/t/d found
 			// 6: i/l/h/e found
 			// 7: p/e found
 			// 8: t found - looking for >
 			
 			// variables used for checking attributes and values
 			int attrStart = 0;
 			int attrValStart = 0;
 			boolean openAttr = false;
 			int attrPhase = 0;
 			String attribute = "";
 			// 1: attribute key name started - ends at whitespace or =
 			// 2: attribute key name finished, looking for =
 			// 3: = found - looking for quotes to start value - whitespace permitted
 			// 4: " found - ignoring everything until matching " found
 			// 5: ' found - ignoring everything until matching ' found
 			// 6: value not enclosed in quotes - add error when end of attribute value found
 			
 			
 			
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
 							if (charArray.getChar(j) == 's' || charArray.getChar(j) == 'S') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'y' || charArray.getChar(j) == 'Y') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'l' || charArray.getChar(j) == 'L') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == 'e' || charArray.getChar(j) == 'E') {
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
 							if (charArray.getChar(j) == 's' || charArray.getChar(j) == 'S') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'c' || charArray.getChar(j) == 'C') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'r' || charArray.getChar(j) == 'R') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'i' || charArray.getChar(j) == 'I') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == 'p' || charArray.getChar(j) == 'P') {
 								// look for next char
 								endEscapedTagPhase = 7;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 7) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
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
 					
 					if (openSvg) {
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
 							if (charArray.getChar(j) == 's' || charArray.getChar(j) == 'S') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'v' || charArray.getChar(j) == 'V') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'g' || charArray.getChar(j) == 'G') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								openSvg = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						}
 						continue;
 					}
 					
 					if (openMath) {
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
 							if (charArray.getChar(j) == 'm' || charArray.getChar(j) == 'M') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'a' || charArray.getChar(j) == 'A') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'h' || charArray.getChar(j) == 'H') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								openMath = false;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} 
 						continue;
 					}
 					
 					if (openCode) {
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
 							if (charArray.getChar(j) == 'c' || charArray.getChar(j) == 'C') {
 								// look for next char
 								endEscapedTagPhase = 3;
 							} else if (charArray.getChar(j) != ' ') {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 3) {
 							if (charArray.getChar(j) == 'o' || charArray.getChar(j) == 'O') {
 								// look for next char
 								endEscapedTagPhase = 4;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 4) {
 							if (charArray.getChar(j) == 'd' || charArray.getChar(j) == 'D') {
 								// look for next char
 								endEscapedTagPhase = 5;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 5) {
 							if (charArray.getChar(j) == 'e' || charArray.getChar(j) == 'E') {
 								// look for next char
 								endEscapedTagPhase = 6;
 							} else {
 								// reset tag
 								endEscapedTagPhase = 0;
 							}
 						} else if (endEscapedTagPhase == 6) {
 							if (charArray.getChar(j) == '>') {
 								// style has been closed
 								endEscapedTagPhase = 0;
 								openCode = false;
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
 				
 				
 					// ==============================================
 					// Doctype fix start
 					// ==============================================
 				
 					if (openDoctype) {
 						if (attrPhase == 1) {
 							if (charArray.getChar(j) == ' ') {
 								continue;
 							}
 							if (charArray.getChar(j) == 'h' || charArray.getChar(j) == 'H') {
 								attrPhase = 2;
 								continue;
 							} else {
 								error = new JSONObject();
 								error.put("message",  sql.getErrMsg(1));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 2) {
 							if (charArray.getChar(j) == 't' || charArray.getChar(j) == 'T') {
 								attrPhase = 3;
 								continue;
 							} else {
 								error = new JSONObject();
 								error.put("message",  sql.getErrMsg(1));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 3) {
 							if (charArray.getChar(j) == 'm' || charArray.getChar(j) == 'M') {
 								attrPhase = 4;
 								continue;
 							} else {
 								error = new JSONObject();
 								error.put("message",  sql.getErrMsg(1));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 4) {
 							if (charArray.getChar(j) == 'l' || charArray.getChar(j) == 'L') {
 								attrPhase = 5;
 								continue;
 							} else {
 								error = new JSONObject();
 								error.put("message",  sql.getErrMsg(1));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 5;
 								if (charArray.getChar(j) == '>') {
 									openDoctype = false;
 									closeTag = true;
 									openTag = false;
 									tag = null;
 									endTagName = false;
 									tagChecked = false;
 									faultyTag = false;
 								}
 								continue;
 							}
 						}
 						if (attrPhase == 5) {
 							if (charArray.getChar(j) == '>') {
 								openDoctype = false;
 								closeTag = true;
 								openTag = false;
 								tag = null;
 								endTagName = false;
 								tagChecked = false;
 								faultyTag = false;
 							} else if (charArray.getChar(j) != ' ') {
 								error = new JSONObject();
 								error.put("message",  sql.getErrMsg(1));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", tag);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								attrPhase = 6;
 							}
 							continue;
 						}
 						if (attrPhase == 6) {
 							if (charArray.getChar(j) == '>') {
 								openDoctype = false;
 								closeTag = true;
 								openTag = false;
 								tag = null;
 								endTagName = false;
 								tagChecked = false;
 								faultyTag = false;
 							}
 							continue;
 						}
 					
 					
 					
 					}
 				
 					// ==============================================
 					// Doctype fix end
 					// ==============================================
 				
 					// ==============================================
 					// Attribute checking start
 					// ==============================================
 					if (openAttr) {
 						if (attrPhase == 1) {
 							// looking for end of attribute key
 							if (charArray.getChar(j) == ' ') {
 								// attribute key has ended
 								attribute = charArray.getString(attrStart, j-1);
 								
 								List<String> attrList = new ArrayList<String>();
 								attrList = sql.getAttr(tag);
 								boolean validAttr = false;
 								for (int a = 0; a < attrList.size(); a++) {
 									if(attrList.get(a).equalsIgnoreCase(attribute)) {
 										validAttr = true;	
 									}
 								}
 								if (!validAttr) {
 									if (attribute.length() > 4) {
 										if (attribute.substring(0,5).equalsIgnoreCase("data-")) {
 											validAttr = true;
 										}
 									}
 									if (!validAttr) {
 										error = new JSONObject();
 										error.put("message", attribute + " " + sql.getErrMsg(23));
 										error.put("type", "syntax");
 										error.put("line", i+1);
 										error.put("col", j);
 										error.put("errorExcerpt", attribute);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 								} else if (sql.isDeprecatedAttribute(attribute, tag)) {
 									error = new JSONObject();
 									error.put("message", attribute + " is a deprecated attribute for " + tag);
 									error.put("type", "deprecated");
 									error.put("line", i+1);
 									error.put("col", j);
 									error.put("errorExcerpt", attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								attrPhase = 2;
 							} else if (charArray.getChar(j) == '=') {
 								// attribute key has ended
 								attribute = charArray.getString(attrStart, j-1);
 								// =====================================
 								// CHECK ATTRIBUTE STUFF LIKE VALID ATTRIBUTE/REQUIRED ATTRIBUTE HERE		
 								// =====================================
 								List<String> attrList = new ArrayList<String>();
 								attrList = sql.getAttr(tag);
 								boolean validAttr = false;
 								for (int a = 0; a < attrList.size(); a++) {
 									if(attrList.get(a).equalsIgnoreCase(attribute)) {
 										validAttr = true;	
 									}
 								}
 								if (!validAttr) {
 									if (attribute.length() > 4) {
 										if (attribute.substring(0,5).equalsIgnoreCase("data-")) {
 											validAttr = true;
 										}
 									}
 									if (!validAttr) {
 										error = new JSONObject();
 										error.put("message", attribute + " " + sql.getErrMsg(23));
 										error.put("type", "syntax");
 										error.put("line", i+1);
 										error.put("col", j);
 										error.put("errorExcerpt", attribute);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									}
 								} else if (sql.isDeprecatedAttribute(attribute, tag)) {
 									error = new JSONObject();
 									error.put("message", attribute + " is a deprecated attribute for " + tag);
 									error.put("type", "deprecated");
 									error.put("line", i+1);
 									error.put("col", j);
 									error.put("errorExcerpt", attribute);
 									errors.put(errorCount, error);
 									errorCount += 1;
 								}
 								
 								attrPhase = 3;
 							}
 							continue;
 						} else if (attrPhase == 2) {
 							// looking for the end of whitespace before the =
 							if (charArray.getChar(j) == ' ') {
 								continue;
 							} else if (charArray.getChar(j) == '=') {
 								attrPhase = 3;
 								continue;
 							} else {
 								// did not find a value for the key
 								error = new JSONObject();
 								error.put("message", attribute + " must have an associated value. Use = 'value' to set a value to this attribute.");
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", attribute);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								
 								openAttr = false;
 								attribute = "";
 							}
 						} else if (attrPhase == 3) {
 							// looking for quotes to start 
 							if (charArray.getChar(j) == ' ') {
 							// do nothing
 							} else if (charArray.getChar(j) == '"') {
 								attrPhase = 4;
 								attrValStart = j;
 							} else if (charArray.getChar(j) == '\'') {
 								attrPhase = 5;
 								attrValStart = j;
 							} else if (charArray.getChar(j) == '#' && attribute.equalsIgnoreCase("href")) {
 								if (charArray.getChar(j+1) == ' ' || charArray.getChar(j+1) == '>') {
 									// unquoted # for href
 									error = new JSONObject();
 									error.put("message", "Even though # is not required to be quoted, it is considered best practice for consistency.");
 									error.put("type", "warning");
 									error.put("line", i+1);
 									error.put("col", j);
 									error.put("errorExcerpt", "#");
 									errors.put(errorCount, error);
 									errorCount += 1;
 									
 									openAttr = false;
 									attribute = "";
 								}
 								continue;
 							} else {
 								// value not enclosed in quotes
 								attrPhase = 6;
 								attrValStart = j;
 							}
 							continue;
 						} else if (attrPhase == 4) {
 							// looking for end of double quotes
 							if (charArray.getChar(j) == '"') {
 							
 								// check for unique id
 								if (attribute.equalsIgnoreCase("id")) {
 									String attributeVal = charArray.getString(attrValStart+1, j-1);
 									boolean matchedID = false;
 									for (int a = 0; a < ids.size(); a++) {
 										if(ids.get(a).equalsIgnoreCase(attributeVal)) {
 											matchedID = true;
 										}
 							
 									}
 									if (matchedID) {
 										// error for duplicate id
 										error = new JSONObject();
 										error.put("message", attributeVal + " is already used as an ID value earlier in the file.");
 										error.put("type", "semantic");
 										error.put("line", i+1);
 										error.put("col", j);
 										error.put("errorExcerpt", attributeVal);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									} else {
 										ids.add(attributeVal);
 									}
 								}
 							
 								// reset attribute flags
 								openAttr = false;
 								attribute = "";
 							}
 							continue;
 						} else if (attrPhase == 5) {
 							// looking for end of double quotes
 							if (charArray.getChar(j) == '\'') {
 								
 								// check for unique id
 								if (attribute.equalsIgnoreCase("id")) {
 									String attributeVal = charArray.getString(attrValStart+1, j-1);
 									boolean matchedID = false;
 									for (int a = 0; a < ids.size(); a++) {
 										if(ids.get(a).equalsIgnoreCase(attributeVal)) {
 											matchedID = true;
 										}
 							
 									}
 									if (matchedID) {
 										// error for duplicate id
 										error = new JSONObject();
 										error.put("message", attributeVal + " is already used as an ID value earlier in the file.");
 										error.put("type", "semantic");
 										error.put("line", i+1);
 										error.put("col", j);
 										error.put("errorExcerpt", attributeVal);
 										errors.put(errorCount, error);
 										errorCount += 1;
 									} else {
 										ids.add(attributeVal);
 									}
 								}
 							
 								// reset attribute flags
 								openAttr = false;
 								attribute = "";
 							}
 							continue;
 						} else if (attrPhase == 6) {
 							// looking for end of double quotes
 							if (charArray.getChar(j) == ' ') {
 								String attributeVal = charArray.getString(attrValStart, j-1);
 								error = new JSONObject();
 								error.put("message", attributeVal + " is the value of an attribute and should be inside quotes.");
 								error.put("type", "semantic");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", attributeVal);
 								errors.put(errorCount, error);
 								errorCount += 1;
 								// reached end of attribute value
 								openAttr = false;
 								attribute = "";
 							}
 							continue;
 						}
 						
 					}
 				
 				
 				
 				
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
 								openTag = false;
 							}
 						}
 						
 						// Check if opened a php tag
 						if (charArray.getLength() >= j+5) {
 							if((charArray.getChar(j+1)=='?') && (charArray.getChar(j+2)=='p' || charArray.getChar(j+2)=='P') && (charArray.getChar(j+3)=='h' || charArray.getChar(j+3)=='H') && (charArray.getChar(j+4)=='p' || charArray.getChar(j+4)=='P')) {
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
 									
 									if (tag.equalsIgnoreCase("!DOCTYPE")) {
 										openDoctype = true;
 										attrPhase = 1;
 										j--;
 									}
 									
 									if(tag.equalsIgnoreCase("html")||tag.equalsIgnoreCase("head")||tag.equalsIgnoreCase("body")||tag.equalsIgnoreCase("!DOCTYPE")||tag.equalsIgnoreCase("title")) {
 										if(singularTags.contains(tag.toLowerCase())) {
 											error = new JSONObject();
 											error.put("message", tag + " is a singular tag but is used more than once!");
 											error.put("type", "syntax");
 											error.put("line", i+1);
 											error.put("col", j);
 											error.put("errorExcerpt", tag);
 											errors.put(errorCount, error);
 											errorCount += 1;
 										}
 										else {
 											singularTags.add(tag.toLowerCase());
 										}
 									}
 									
 									if (tag.length() > 0) {
 										if (tag.substring(0,1).equalsIgnoreCase("/")) {
 											tag = tag.substring(1);
 										}
 									}
 									endTagName = true;
 									
 									
 								}
 								
 								
 								
 								
 								
 								if (!tagChecked) {
 								
 								
 								
 									// If it is not a valid tag
 									if(!sql.checkValidTag(tag)) {
 										
 										// Note that some of these additions should use database references in future
 										error = new JSONObject();
 										if (tag.equalsIgnoreCase("doctype")) {
 											error.put("message", sql.getErrMsg(1));
 										} else {
 											error.put("message", tag + " is not a valid tag");
 										}
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
 									
 									if (tag.equalsIgnoreCase("script")) {
 										openScript = true;
 									}
 									
 									else if (tag.equalsIgnoreCase("style")) {
 										openStyle = true;
 									}
 									
 									else if(tag.equalsIgnoreCase("svg")) {
 										openSvg = true;
 									}
 									
 									else if(tag.equalsIgnoreCase("math")) {
 										openMath = true;
 									}
 									
 									else if(tag.equalsIgnoreCase("code")) {
 										openCode = true;
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
 											attrPhase = 1;
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
 								if(attrList.get(a).equalsIgnoreCase(attribute)) {
 									validAttr = true;	
 								}
 							
 							}
 							if (!validAttr) {
 								error = new JSONObject();
 								// Actual DB reference test
 								error.put("message", attr + " " + sql.getErrMsg(23));
 								error.put("type", "syntax");
 								error.put("line", i+1);
 								error.put("col", j);
 								error.put("errorExcerpt", attribute);
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
 		
 		private JSONObject errorConstructor(String message, String type, int line, int col, String errorExcerpt) {
 			error = new JSONObject();
 			error.put("message", message);
 			error.put("type", type);
 			error.put("line", line);
 			error.put("col", col);
 			error.put("errorExcerpt", errorExcerpt);
 			return error;
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
