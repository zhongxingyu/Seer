 /*
  * Copyright () 2011 The Johns Hopkins University Applied Physics Laboratory.
  * All Rights Reserved.  
  */
 package org.rapidsms.java.core.parser.interpreter;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Adjoa Poku adjoa.poku@jhuapl.edu
  * @created May 10, 2011
  */
 public class DateInterpreter implements IParseInterpreter {
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.rapidsms.java.core.parser.interpreter.IParseInterpreter#interpretValue
 	 * (java.lang.String)
 	 */
 
 	Pattern mPattern;
 
 	public DateInterpreter() {
 		// MUST HAVE ZERO GROUPS AND OTHER TOKENIZING JUNK
		mPattern = Pattern.compile("((\\d{2,2}\\.{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\/{1,1}){2,2}\\d{4,4})|((\\d{2,2}\\-{1,1}){2,2}\\d{4,4})|(\\d{4,4}(\\.\\d{2,2}){2,2})|(\\d{4,4}(\\/\\d{2,2}){2,2})|(\\d{4,4}(\\-\\d{2,2}){2,2})\\.*");
 	}
 
 	public Object interpretValue(String token) {
 		Matcher m = mPattern.matcher(token);
 		if (m.find()) {
 			try {
 				return token;
 			} catch (Exception ex) {
 				return null;
 			}
 
 		}
 		return null;
 	}
 }
