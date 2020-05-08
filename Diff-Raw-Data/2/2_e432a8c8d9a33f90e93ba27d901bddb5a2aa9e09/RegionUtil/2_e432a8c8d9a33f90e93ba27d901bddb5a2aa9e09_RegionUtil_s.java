 package common;
 
 import java.util.HashMap;
 
 import org.apache.commons.lang.WordUtils;
 
 public class RegionUtil {
 	
 	private static HashMap<String, String> statesList = null;
 
 	public static String getCountryCode(String name) {
 		String countryCode = "US";
 		if (name.equals("United States")
 				|| name.equals("USA")
 				|| name.equals("United States of America")) {
 			countryCode = "US";
 		} else if (name.equals("Canada")) {
 			countryCode = "CA";
 		} else if (name.equals("China")
 				|| name.equals("CHN")
 				|| name.equals("CN")) {
 			countryCode = "CN";
 		} else if (name.equals("Hong Kong")
 				|| name.equals("HKG")
 				|| name.equals("HK")) {
 			countryCode = "HK";
 		} else {
 			countryCode = "US";
 		}
 		return countryCode;
 	}
 
 	public static String inferCountryFromPhoneNumber(String from) {
 		String name = "USA";
 		String compStr = from;
 		if (from.startsWith(" ")) {
 			compStr = from.substring(1);
 		}
 		if (compStr.startsWith("+")) {
 			compStr = from.substring(1);
 		} 
 		if (compStr.startsWith("1")) {
 			name = "USA";
 		} else if (compStr.startsWith("852")) {
 			name = "HKG";
 		} else if (compStr.startsWith("86")) {
 			name = "China";
 		}
 		return name;
 	}
 	
 	public static String getStateShortCode(String state) {
 		String canoncialState = state.toLowerCase();
 		canoncialState = WordUtils.capitalize(canoncialState);
 		if (statesList == null) {
 			instantiateStatesList();
 		}
 		String shortCode = statesList.get(canoncialState);
 		if (shortCode == null) {
 			return "";
 		} else {
 			return shortCode;
 		}
 	}
 
 	private static void instantiateStatesList() {
 		statesList = new HashMap<String, String> ();
 		
 		statesList.put("Alabama", "AL");
 		statesList.put("Alaska", "AK");
 		statesList.put("Arizona", "AZ");
 		statesList.put("Arkansas", "AR");
 		statesList.put("California", "CA");
 
 		statesList.put("Colorado", "CO");
 		statesList.put("Connecticut", "CT");
 		statesList.put("Delaware", "DE");
 		statesList.put("Florida", "FL");
 		statesList.put("Georgia", "GA");
 
 		statesList.put("Hawaii", "HI");
 		statesList.put("Idaho", "ID");
 		statesList.put("Illinois", "IL");
 		statesList.put("Indiana", "IN");
 		statesList.put("Iowa", "IA");
 
 		statesList.put("Kansas", "KS");
 		statesList.put("Kentucky", "KY");
 		statesList.put("Louisiana", "LA");
 		statesList.put("Maine", "ME");
 		statesList.put("Maryland", "MD");
 
 		statesList.put("Massachusetts", "MA");
 		statesList.put("Michigan", "MI");
 		statesList.put("Minnesota", "MN");
 		statesList.put("Mississippi", "MS");
 		statesList.put("Missouri", "MO");
 
 		statesList.put("Montana", "MT");
 		statesList.put("Nebraska", "NE");
 		statesList.put("Nevada", "NV");
 		statesList.put("New Hampshire", "NH");
 		statesList.put("New Jersey", "NJ");
 
 		statesList.put("New Mexico", "NM");
 		statesList.put("New York", "NY");
 		statesList.put("North Carolina", "NC");
 		statesList.put("North Dakota", "ND");
 		statesList.put("Ohio", "OH");
 
 		statesList.put("Oklahoma", "OK");
 		statesList.put("Oregon", "OR");
 		statesList.put("Pennsylvania", "PA");
 		statesList.put("Rhode Island", "RI");
 		statesList.put("South Carolina", "SC");
 
 		statesList.put("South Dakota", "SD");
 		statesList.put("Tennessee", "TN");
 		statesList.put("Texas", "TX");
 		statesList.put("Utah", "UT");
 		statesList.put("Vermont", "VT");
 
 		statesList.put("Virginia", "VA");
 		statesList.put("Washington", "WA");
 		statesList.put("West Virginia", "WV");
 		statesList.put("Wisconsin", "WI");
 		statesList.put("Wyoming", "WY");
 
 		statesList.put("United States", "US");
 		statesList.put("Alberta", "AB");
 		statesList.put("British Columbia", "BC");
 		statesList.put("Manitoba", "MB");
 		statesList.put("New Brunswick", "NB");
 
 		statesList.put("Newfoundland and Labrador", "NL");
 		statesList.put("Northwest Territories", "NT");
 		statesList.put("Nova Scotia", "NS");
 		statesList.put("Nunavut", "NU");
 		statesList.put("Ontario", "ON");
 
 		statesList.put("Prince Edward Island", "PE");
 		statesList.put("Quebec", "QC");
		statesList.put("QuÃ©bec", "QC");
 		statesList.put("Saskatchewan", "SK");
 		statesList.put("Yukon", "YT");
 
 	}
 }
