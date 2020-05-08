 
 package util;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author Alex Hughes
  */
 public class StrVal {
 
     //This function replaces all quotes (') with dollar sign characters ($)
     public static String cmflS(String input) {
 
         String output = input.replace("'", "/$/");
         return output;
     }
 
     //This function removes all quotes characters(')
     public static String sntS(String input) {
        StringBuilder r = new StringBuilder();
         r.setLength(input.length());
         
         int current = 0;
         for (int i = 0; i < input.length(); i++) {
             char cur = input.charAt(i);
             if (cur != '\'' && cur != '\"') {
                 r.setCharAt(current++, cur);
             }
         }
         return r.toString();
     }
 
     //This function replaces all dollar sign characters($) with quotes (')
     //To be used with cmflS
     public static String unCmflS(String input) {
         String output = "";
         
         if (input != null && !input.equals("")) {
             output = input.replace("/$/", "'");
         }
         return output;
     }
 
     //This function returns the result of the subtraction of our desired 
     //limit and the string length.
     public static int measureString(String input, int aLimit) {
         int result = input.length() - aLimit;
         return result;
     }
 
     //This function returns whether a string has a valid length.
     public static boolean validStringLength(String input, int aLimit) {
         boolean isValid;
 
         if (aLimit - input.length() >= 0) {
             isValid = true;
         } else {
             isValid = false;
         }
         return isValid;
     }
     
     public static boolean hasDuplicates(ArrayList<String> aStrings, String aString){
         boolean hasDuplicates = false;
         
         for(int i=0; i<aStrings.size(); i++){
             if(aStrings.get(i).equalsIgnoreCase(aString)){
                 hasDuplicates = true;
                 break;
             }
         }
         return hasDuplicates;
     }
     
     public static String SqlStringToString(String aSqlDate) {
         String date = "";
         String day = null, month = null, year = null;
 
         Scanner scanner = new Scanner(aSqlDate);
         scanner.useDelimiter("-");
 
         while (scanner.hasNext()) {
             year = scanner.next();
             month = scanner.next();
             day = scanner.next();
         }
         date = day + "/" + month + "/" + year;
 
         return date;
     }
 }
