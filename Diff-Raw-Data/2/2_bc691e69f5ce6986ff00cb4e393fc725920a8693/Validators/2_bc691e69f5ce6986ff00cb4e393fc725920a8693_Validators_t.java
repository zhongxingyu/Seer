 package com.ted.validators;
 
 import java.util.regex.Pattern;
 
 /**
  * @author std09200
  */
 public final class Validators {
 
 	public static final int MIN_PASS_LENGTH = 6;
 	public static final int MIN_USERNAME_LENGTH = 5;
 	/*
 	 * supposed to match any letter(s) followed by apostrophe or space and then
 	 * any letter(s)
 	 */
 	// http://stackoverflow.com/questions/3263978/java-regex-to-validate-a-name
 	// http://www.regular-expressions.info/unicode.html
 	// http://stackoverflow.com/questions/93976/how-to-determine-whether-a-character-is-a-letter-in-java
 	// http://stackoverflow.com/questions/5315330/matching-e-g-a-unicode-letter-with-java-regexps
 	public static final Pattern VALID_CHARS_NAME_SURNAME = Pattern
 			.compile("(\\p{L}\\p{M}*)*([ \']+(\\p{L}\\p{M}*)+)*");
 	/*
 	 * supposed to match any letter(s)/number(s) followed by comma period or
 	 * space and then repeat
 	 */
 	public static final Pattern VALID_CHARS_ADDRESS = Pattern
 			.compile("([0-9]*|(\\p{L}\\p{M}*)*)([ ,.]+([0-9]+|(\\p{L}\\p{M}*)+))*");
 	/*
 	 * supposed to match any letter(s)/number(s) followed by comma period
 	 * apostrophe or space and then repeat
 	 */
 	public static final Pattern VALID_CHARS_OCCUPATION = Pattern
 			.compile("([0-9]*|(\\p{L}\\p{M}*)*)([ ,.']+([0-9]+|(\\p{L}\\p{M}*)+))*");
 	public static final Pattern VALID_TELEPHONE = Pattern.compile("[0-9-+()]+");
 	/*
 	 * http://stackoverflow.com/questions/153716/verify-email-in-java
 	 */
 	public static final Pattern RFC2822 = Pattern
 			.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
 	/*
 	 * supposed to match letters numbers and _ these are the valid chars for
 	 * password AND user name
 	 */
 	private static final Pattern VALID_CHARS_USERNAME_PASSWORD = Pattern
 			.compile("[0-9_a-zA-Z]+");
 	/*
 	 * http://www.mkyong.com/regular-expressions/how-to-validate-date-with-regular
 	 * -expression/ dd/mm/yyyy (dd:1 h 2 pshfia) ,(mm :1 h 2 pshfia)
 	 */
 	public static final Pattern VALID_DATE = Pattern
 			.compile("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)");
 
 	/**
 	 * Returns TRUE if the passed in string is null or empty
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if the string s is null or empty otherwise false
 	 */
 	public static boolean isNullOrEmpty(String s) {
 		if (s == null || s.isEmpty() || s.trim().isEmpty()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns TRUE if the passed string is not a big enough password. Must be
 	 * used AFTER checking that the pass is not null.
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if the pass is too short otherwise false
 	 */
 	public static boolean isPasswordTooShort(String s) {
 		if (s.trim().length() < MIN_PASS_LENGTH) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns TRUE if the passed string is not a big enough user name. Must be
 	 * used AFTER checking that the user name is not null.
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if the user name is too short otherwise false
 	 */
 	public static boolean isUsernameameTooShort(String s) {
 		if (s.trim().length() < MIN_USERNAME_LENGTH) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns TRUE if the passed string contains invalid chars for name or
 	 * surname
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if s contains invalid chars otherwise false
 	 */
 	public static boolean nameSurnameContainsInvalidChars(String s) {
 		if (VALID_CHARS_NAME_SURNAME.matcher(s.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns TRUE if the passed string contains invalid chars for user name or
 	 * pass
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if s contains invalid chars otherwise false
 	 */
 	public static boolean usernamePassContainsInvalidChars(String s) {
 		if (VALID_CHARS_USERNAME_PASSWORD.matcher(s.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns TRUE if the passed string contains invalid chars for telephone
 	 * number
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if s contains invalid chars otherwise false
 	 */
 	public static boolean telephoneContainsInvalidChars(String s) {
 		if (VALID_TELEPHONE.matcher(s.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns TRUE if the passed in string is NOT a valid email
 	 *
 	 * @author std09200
 	 * @param s
 	 * @return true if the string s is null or empty otherwise false
 	 */
 	public static boolean isEmailInvalid(String email) {
 		if (RFC2822.matcher(email.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean addressContainsInvalidChars(String address) {
 		if (VALID_CHARS_ADDRESS.matcher(address.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean occupationContainsInvalidChars(String occupation) {
 		if (VALID_CHARS_OCCUPATION.matcher(occupation.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean dateIsInvalid(String s) {
 		if (VALID_DATE.matcher(s.trim()).matches()) {
 			return false;
 		}
 		return true;
 	}

	private Validators() {}// disallow instantiation
 }
