 package com.scholastic.sbam.shared.util;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
 
 public class AppConstants {
	public static final String VERSION = "0.0dev";
 	public static final String VERSION_DESCRIPTION = "In Development.";
 	
 	public static final DateTimeFormat APP_DATE_TIME_FORMAT = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);	//	DateTimeFormat.getFormat("MM/dd/yy");
 	
 	public static final char STATUS_ACTIVE		= 'A';
 	public static final char STATUS_INACTIVE	= 'I';
 	public static final char STATUS_DELETED		= 'X';
 	public static final char STATUS_EXPIRED		= 'E';
 	
 	public static final char PATH_DELIMITER		= '/';
 	public static final char PATH_ESCAPE		= '\\';
 	
 	public static final String REPLACEMENT_START = "[$";
 	public static final String REPLACEMENT_END   = "$]";
 	public static final String CHUNK_START		= "[[";
 	public static final String CHUNK_END		= "]]";
 	public static final String CHUNK_PART		= "::";
     
     public static final String DIGITS			= "0123456789";
     public static final String LETTERS			= "abcdefghijklmnopqrstuvwxyz";
     public static final String LETTERS_ALL		= "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
 	
 	public static final String []	NUMBER_WORDS_0_19		=	{ 
 																	"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
 																	"Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen" 
 																};
 	public static final String []	NUMBER_WORDS_0_90		=	{ 
 																	"Zero", "Ten", "Twenty", "Thirty", "Fourty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
 																};
 	
 	
     
     public static boolean isLetter(char chr) {
     	return LETTERS.indexOf(Character.toLowerCase(chr)) >= 0;
     }
     
     public static boolean isDigit(char chr) {
     	return DIGITS.indexOf(chr) >= 0;
     }
     
     public static boolean isLetter(String chr) {
     	if (chr == null)
     		return false;
     	if (chr.length() > 1)
     		chr = chr.substring(0, 1);
     	return LETTERS.indexOf(chr.toLowerCase()) >= 0;
     }
     
     public static boolean isDigit(String chr) {
     	if (chr.length() > 1)
     		chr = chr.substring(0, 1);
     	return DIGITS.indexOf(chr) >= 0;
     }
     
     public static boolean isEmpty(String str) {
     	return str == null || str.length() == 0 || str.trim().length() == 0;
     }
 	
 	public static String getNumberWord(int number) {
 		String prefix = (number < 0) ? "Negative " : "";
 		if (number < 20)
 			return prefix + NUMBER_WORDS_0_19 [number];
 		if (number >= 10000)
 			return number + "";
 		if (number >= 1000) {
 			prefix += NUMBER_WORDS_0_19 [number / 100] + " Thousand"; 
 			if (number % 1000 == 0)
 				return prefix;
 			prefix += ", ";
 			number = number % 1000;
 		}
 		if (number >= 100) {
 			prefix += NUMBER_WORDS_0_19 [number / 10] + " Hundred";
 			if (number % 100 == 0)
 				return prefix;
 			prefix += ", ";
 			number = number % 100;
 		}
 		if (number >= 20) {
 			prefix += NUMBER_WORDS_0_90 [number / 10];
 			if (number % 10 == 0)
 				return prefix;
 			prefix += " ";
 			number = number % 10;
 		}
 		return prefix + NUMBER_WORDS_0_19 [number];
 	}
 	
 	public static String getNumberWordLower(int number) {
 		return getNumberWord(number).toLowerCase();
 	}
 }
