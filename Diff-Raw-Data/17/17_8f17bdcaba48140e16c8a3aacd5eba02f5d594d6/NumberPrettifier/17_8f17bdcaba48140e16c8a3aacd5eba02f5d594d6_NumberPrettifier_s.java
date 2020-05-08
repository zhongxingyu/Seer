 package util;
 
 
 import java.text.DecimalFormat;
 
 public class NumberPrettifier {
 
 
     public String prettify(Double aNumber) {
         if( aNumber > 999999) {
            return handleNumbersOverSixDigits(aNumber);
         }
         else {
            return handleNumbersSixDigitsOrUnder(aNumber);
         }
     }
 
 
    private String handleNumbersSixDigitsOrUnder(Double aNumber) {
         return new Integer(aNumber.intValue()).toString();
     }
 
 
    private String handleNumbersOverSixDigits(Double aNumber) {
         String sizeSuffix = determineSizeSuffix(aNumber);
         String stringNumber = new DecimalFormat("#").format(aNumber);
         char  firstDigit = stringNumber.charAt(0);
         char  secondDigit = stringNumber.charAt(1);
         if( secondDigit == '0') {
             return firstDigit + sizeSuffix;
         }
         else {
             return "" + firstDigit + "." + secondDigit + sizeSuffix;
         }
     }
 
     private String determineSizeSuffix(Double aNumber) {
         String sizeSuffix = null;
         if( aNumber > 999999999999D)
             sizeSuffix = "T";
         else if( aNumber > 999999999)
             sizeSuffix = "B";
         else if( aNumber > 999999)
             sizeSuffix = "M";
         return sizeSuffix;
     }
 
 }
