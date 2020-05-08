 package edu.unsw.comp9321.assign2;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /*
  * validates input from forms
  */
 public class validator {
 	private static Pattern pattern;
 	private static Matcher matcher;
 	
 	// used to check username and other values with no spaces
 	public static boolean checkText(String text) {
 		System.out.println("validation string: " + text);
		String regex = "[^=*][a-zA-Z0-9]*"; // work for user 1
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(text);
 		return matcher.matches();
 	}
 	
 	public static boolean checkPassword(String password) {
 		System.out.println("validation string: " + password);
		String regex = "[^=*][a-zA-Z0-9]*"; // work for user 1
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(password);
 		return matcher.matches();
 	}
 	
 	public static boolean checkCreditCard(String sequence) { 
 		String regex = "[0-9]{16}";
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(sequence);
 		return matcher.matches();
 	}
 	
 	public static boolean checkEmail(String email) {
 		String regex = "^[a-z0-9]*@[a-z.]*";
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(email);
 		return matcher.matches();
 	}
 	
 	public static boolean checkYear(int year) {
 		if (year > 1900 && year < 2099) {
 			return true;
 		}
 		return false;
 	}
 	
 	// can be used for any long text sequence, allows space
 	public static boolean checkLongText(String text) {
 		System.out.println("validation string: " + text);
 		String regex = "[^=*][a-zA-Z0-9 ]*"; // work for user 1
 		pattern = Pattern.compile(regex);
 		matcher = pattern.matcher(text);
 		return matcher.matches();
 	}
 	
 	public static boolean checkURL(String url) {
 		return true;
 	}
 }
