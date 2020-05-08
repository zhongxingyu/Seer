 package com.bot.utils;
 
 public class StringUtils {
 	
 	public static String stripPunctuation(String word) {
		return isEndOfSentence(word) || hasComma(word) ? word.substring(0, (word.length()-1)) : word;
 	}
 
	private static boolean hasComma(String word) {
		return word.endsWith(",");
	}

	public static boolean isEndOfSentence(String word) {
		return (word.endsWith(".") || word.endsWith("?") || word.endsWith("!"));
 	}
 }
