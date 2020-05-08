 /**
  * OOSE Project - Group 4
  * {@link FieldVerifier2}.java
  */
 package edu.jhu.twacker.shared;
 
 import com.google.gwt.regexp.shared.RegExp;
 
 /**
  * FieldVerifier validates that the name the user enters is valid.
  * 
  * @author Disa Mhembere, Alex Long
 */
 public class FieldVerifier
 {
 	
 	public final static String PRIMARY_DEFAULT = "(required)";
 	public final static String SECONDARY_DEFAULT = "(optional)";
 	
 	/**
 	 * Verify that an email address provided is at least of valid format
 	 * @param emailAdd the users email address
 	 * @return true is email address matches a known pattern for email addresses
 	 */
 	public static boolean isValidEmail(String emailAdd)
 	{
 		RegExp p = RegExp.compile(".+@.+\\..+");
 		return p.test(emailAdd);
 	}
 
 	/**
 	 * Validates whether user name is valid based on our criteria for validity
 	 * @param name the name of user
 	 * @return true if valid else false
 	 */
 	public static boolean isValidUserName(String name)
 	{
 
 		RegExp p = RegExp.compile("[^a-zA-Z0-9]"); // No special Characters
 		
 		if (name == null || p.test(name) || name.length() < 3)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Validate whether a password is permissible or not
 	 * @param password
 	 * @return true is valid else false
 	 */
 	public static boolean isValidPassword(String password)
 	{
 		RegExp p = RegExp.compile("\\s"); // No spaces in password
 		return !(password == null || p.test(password) || password.length() < 5);
 	 
 	}
 	
 	/**
 	 * Validate whether a users first name OR last name are 
 	 * reasonable to insert
 	 * @param name
 	 * @return
 	 */
 	public static boolean isValidName(String name)
 	{
 		RegExp p = RegExp.compile("^[A-Z][a-z]"); // No special Characters (^([A-Z])[a-z]*)|([a-z]+)
 		return p.test(name);
 		
 	}
 	
 	/**
 	 * Verifies that the search term is not the default or empty.
 	 * The only other requirement is that the String is at least 3 characters.
 	 * 
 	 * @param search the user's search term
 	 * @return true if valid, else false
 	 */
 	public static boolean isValidSearch(String search) 
 	{
 		if (search == null || search.equals(PRIMARY_DEFAULT) || search.equals(SECONDARY_DEFAULT)) {
 			return false;
 		}
 		return search.length() >= 3;
 	}
 }
