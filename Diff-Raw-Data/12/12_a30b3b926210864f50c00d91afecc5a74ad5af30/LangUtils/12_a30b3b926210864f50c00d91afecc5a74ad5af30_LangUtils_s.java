 package com.flaptor.util;
 
 import org.apache.nutch.analysis.lang.LanguageIdentifier;
import org.apache.nutch.util.NutchConfiguration;
 
 /**
  * Utility class for language identifying (a wrapper for nutch's language identifiers)
  *  
  * @author Martin Massera
  */
 public class LangUtils {
 
	private static LanguageIdentifier languageIdentifier = new LanguageIdentifier(NutchConfiguration.create());
 
     public static String identify(String text) {
     	return languageIdentifier.identify(text.toLowerCase());
     }
 
     public static boolean isEnglish(String text) {
     	return "en".equals(identify(text));
     }
 }
