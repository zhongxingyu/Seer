 package util;
 
 
 import java.text.DecimalFormat;
 
 public class NumberPrettifier {
 
 
     // Note:  Right now I'm using strict TDD
     // so some of this will be hard-coded until
     // I get to a test that forces otherwise
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
         String sizeSuffix = determineSizeSuff(aNumber);
         String stringNumber = new DecimalFormat("#").format(aNumber);
         char  firstDigit = stringNumber.charAt(0);
         char  secondDigit = stringNumber.charAt(1);
         if( secondDigit == '0') {
            return "1" + sizeSuffix;
         }
         else {
             return "" + firstDigit + "." + secondDigit + sizeSuffix;
         }
     }
 
     private String determineSizeSuff(Double aNumber) {
         String sizeSuffix = null;
         if( aNumber > 999999999)
             sizeSuffix = "B";
         else if( aNumber > 999999)
             sizeSuffix = "M";
         return sizeSuffix;
     }
 
 }
