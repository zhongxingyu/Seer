 package com.flaptor.util;
 
 import org.apache.nutch.analysis.lang.LanguageIdentifier;
 
 /**
  * Utility class for language identifying (a wrapper for nutch's language identifiers)
  *  
  * @author Martin Massera
  */
 public class LangUtils {
 
	private static LanguageIdentifier languageIdentifier = LanguageIdentifier.getInstance();
 
     public static String identify(String text) {
     	return languageIdentifier.identify(text.toLowerCase());
     }
 
     public static boolean isEnglish(String text) {
     	return "en".equals(identify(text));
     }
 }
