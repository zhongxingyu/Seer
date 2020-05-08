 package net.int9.person;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 class Validator {
     private static final Logger LOG = Logger.getLogger(Validator.class.toString());
 
     public static boolean isValid(String ssn) {
         return
                 null != ssn
                         && Validator.isNumeric(ssn)
                         && Validator.correctLength(ssn)
                         && Validator.validCheckDigits(ssn);
     }
 
     private static boolean correctLength(String ssn) {
         if (11 == ssn.length()) {
             return true;
         } else {
             LOG.log(Level.FINE, "Length not as expected (11 digits). -- " + ssn.length() + " : " + ssn);
             return false;
         }
     }
 
     private static boolean isNumeric(String ssn) {
        try {
            Double.parseDouble(ssn);
            return true;
        } catch (NumberFormatException e) {
            LOG.log(Level.FINE, "Fodselsnummer not numeric.");
            return false;
         }
     }
 
     private static boolean validCheckDigits(String ssn) {
         Integer cc1 = 11 - (
                 (3 * getDigit(ssn, 0)
                         + 7 * getDigit(ssn, 1)
                         + 6 * getDigit(ssn, 2)
                         + 1 * getDigit(ssn, 3)
                         + 8 * getDigit(ssn, 4)
                         + 9 * getDigit(ssn, 5)
                         + 4 * getDigit(ssn, 6)
                         + 5 * getDigit(ssn, 7)
                         + 2 * getDigit(ssn, 8)) % 11);
         if (cc1.equals(11)) {
             cc1 = 0;
         }
         if (getDigit(ssn, 9).equals(cc1)) {
             Integer cc2 = 11 - ((5 * getDigit(ssn, 0) + 4 * getDigit(ssn, 1) + 3 * getDigit(ssn, 2) + 2 * getDigit(ssn, 3)
                     + 7 * getDigit(ssn, 4) + 6 * getDigit(ssn, 5) + 5 * getDigit(ssn, 6) + 4 * getDigit(ssn, 7) + 3 * getDigit(ssn, 8) + 2 * cc1) % 11);
             if (cc2.equals(11)) {
                 cc2 = 0;
             }
             if (getDigit(ssn, 10).equals(cc2)) {
                 return true;
             } else {
                 LOG.log(Level.FINE, String.format("Invalid checksum digit 2. %s (%d vs %d) ", ssn, cc2, getDigit(ssn, 10)));
                 return false;
             }
         } else {
             LOG.log(Level.FINE, String.format("Invalid checksum digit 1. %s (%d vs %d) ", ssn, cc1, getDigit(ssn, 9)));
             return false;
         }
     }
 
     private static Integer getDigit(String ssn, int i) {
         return Integer.valueOf("" + ssn.charAt(i));
     }
 }
