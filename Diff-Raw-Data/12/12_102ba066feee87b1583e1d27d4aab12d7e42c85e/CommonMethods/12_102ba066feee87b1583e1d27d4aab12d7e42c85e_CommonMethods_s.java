 package dk.bigherman.android.pisviewer;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public final class CommonMethods
 {
 	 private static boolean validateIcaoWithRegex(String icao, String regexPattern)
 	 {
 	  Pattern r = Pattern.compile(regexPattern);
 	  Matcher m = r.matcher(icao);
 	  if (m.find())
 	  {
 		  return true;
 	  }
 	  return false;
 	 }
 	 
 	 public static boolean validateIcao(String icao, String regexPattern, DataBaseHelper dbHelper)
 	 {
 		 Boolean regexFlag = validateIcaoWithRegex(icao, regexPattern);
 		 if (regexFlag)
 		 {
 			 Boolean dbFlag = dbHelper.validateIcaoWithDb(icao);
 			 if (dbFlag)
 			 {
 				 return true;
 			 }
 		 }
 		 return false;
 	 }
 
 }
