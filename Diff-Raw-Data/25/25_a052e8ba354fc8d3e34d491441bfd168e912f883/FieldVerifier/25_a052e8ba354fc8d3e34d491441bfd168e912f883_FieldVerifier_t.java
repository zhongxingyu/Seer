 package project.shared;
 
 
 /**
  * <p>
  * FieldVerifier validates that the name the user enters is valid.
  * </p>
  * <p>
  * This class is in the <code>shared</code> package because we use it in both
  * the client code and on the server. On the client, we verify that the name is
  * valid before sending an RPC request so the user doesn't have to wait for a
  * network round trip to get feedback. On the server, we verify that the name is
  * correct to ensure that the input is correct regardless of where the RPC
  * originates.
  * </p>
  * <p>
  * When creating a class that is used on both the client and the server, be sure
  * that all code is translatable and does not use native JavaScript. Code that
  * is not translatable (such as code that interacts with a database or the file
  * system) cannot be compiled into client side JavaScript. Code that uses native
  * JavaScript (such as Widgets) cannot be run on the server.
  * </p>
  */
 public class FieldVerifier {
 
 	/**
 	 * Verifies that the specified name is valid for our service.
 	 * 
 	 * In this example, we only require that the name is at least four
 	 * characters. In your application, you can use more complex checks to ensure
 	 * that usernames, passwords, email addresses, URLs, and other fields have the
 	 * proper syntax.
 	 * 
 	 * @param name the name to validate
 	 * @return true if valid, false if invalid
 	 */
 	public static boolean isValidName(String name) {
 		if (name == null) {
 			return false;
 		}
 		return name.length() > 3;
 	}
 	
	/**
	 * Checks to see if the provided string is null or not.
	 * @param option to be checked for null
	 * @return true if is null
	 */
 	public static boolean isNull(String option) {
 		if (option == null) {
 			return true;
 		} else {
 		return false;
 		}
 	}
 	
	/**
	 * Checks to see if the provided string is empty or not
	 * @param option to be checked for empty
	 * @return true if empty || ""
	 */
 	public static boolean isEmpty(String option) {
 		if (option.isEmpty() || option == "") {
 			return true;
 		} else {
 		 return false;
 		}
 	}
 	
	/**
	 * Checks to see if the provided string contains letters 
	 * @param option to be checked for letters
	 * @return true if string has letters
	 */
 	public static boolean containsLetters(String option) {
 		if (option.matches("^[a-zA-Z]*$")) {
 			return true;
 		} else {
 		return false;
 		}
 	}
 	
	/**
	 * Checks to see if the provided string contains numbers
	 * @param option to be checked for numbers
	 * @return true if string has numbers or a decimal(.)
	 */
 	public static boolean containsNum(String option) {
 		if (option.matches("^[0-9.]*$")) {
 			return true;
 		} else {
 		return false;
 		}
 	}
 }
