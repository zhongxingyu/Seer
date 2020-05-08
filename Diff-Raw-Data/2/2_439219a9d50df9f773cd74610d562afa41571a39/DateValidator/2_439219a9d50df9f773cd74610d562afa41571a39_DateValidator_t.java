 package validator;
 
 public class DateValidator {
 
 	private DateValidator() {
 		// static util class
 	}
 
 	/**
 	 * Checks if the date (String) is in the right format.
 	 * 
 	 * @param date
 	 *            Date to be validated
 	 * @param nullable
 	 *            true if date field can be null
 	 * @return If date (String) format is valid return true, otherwise false
 	 */
 	public static boolean validate(String date, boolean nullable) {
 
 		if (nullable)
 			return (date == null) || validate(date);
 		else
			return (date != null) && validate(date);
 	}
 
 	private static boolean validate(String date) {
 
 		boolean a = false, b = false, c = false;
 
 		if (date.matches("[0-9]{3,4}(-[0-9]{2})?(-[0-9]{2})?")) {
 			
 			String[] str = date.split("-");
 
 			for (int i = 0; i < str.length; i++) {
 				String s = str[i];
 				switch (i) {
 				case 0:
 					a = s.length() == 3 || s.length() == 4;
 					break;
 				case 1:
 					b = s.length() == 2 && Integer.parseInt(s) <= 12;
 					break;
 				case 2:
 					c = s.length() == 2 && Integer.parseInt(s) <= 31;
 					break;
 				default:
 					break;
 				}
 			}
 
 			if (str.length == 1)
 				return a;
 			else if (str.length == 2)
 				return a && b;
 			else
 				return a && b && c;
 		}
 
 		return false;
 	}
 
 }
