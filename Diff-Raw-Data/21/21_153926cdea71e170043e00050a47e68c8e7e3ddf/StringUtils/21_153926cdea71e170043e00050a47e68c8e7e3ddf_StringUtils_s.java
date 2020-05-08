 package com.bot.utils;
 
 public class StringUtils {
 	
 	public static String stripPunctuation(String word) {
		return hasPunctuation(word) ? word.substring(0, (word.length()-1)) : word;
 	}
 
	public static boolean hasPunctuation(String nextWord) {
		return (nextWord.endsWith(".") || nextWord.endsWith("?") || nextWord.endsWith("!"));
 	}
 }
