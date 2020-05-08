 /*
 The contents of this file are subject to the Jbilling Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.jbilling.com/JPL/
 
 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.
 
 The Original Code is jbilling.
 
 The Initial Developer of the Original Code is Emiliano Conde.
 Portions created by Sapienter Billing Software Corp. are Copyright 
 (C) Sapienter Billing Software Corp. All Rights Reserved.
 
 Contributor(s): ______________________________________.
 */
 
 package com.sapienter.jbilling.common;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.log4j.Logger;
 
 /**
  * Client miscelaneous utility functions
  */
 public class Util {
     /**
      * Creates a date object with the given parameters only if they belong to a
      * valid day, so February 30th would be returning null.
      * @param year
      * @param month
      * @param day
      * @return null if the parameters are invalid, otherwise the date object
      */
     static public Date getDate(Integer year, Integer month, 
             Integer day) {
         Date retValue = null;
         
         try {
             GregorianCalendar cal = new GregorianCalendar();
             cal.setLenient(false);
             cal.clear();
             cal.set(year.intValue(), month.intValue()-1, day.intValue());
         
             retValue = cal.getTime();
         } catch (Exception e) {
             
         }
         return retValue;
     }
     
     /**
      * Converts a string in the format yyyy-mm-dd to a Date.
      * If the string can't be converted, it returns null
      * @param str
      * @return
      */
     static public Date parseDate(String str) {
         if (str == null || str.length() < 8 || str.length() > 10) {
             return null;
         }
         
         if (str.charAt(4) != '-' || str.lastIndexOf('-') < 6 || 
                 str.lastIndexOf('-') > 7) {
             return null;
         }
         
         try {
             int year = getYear(str);
             int month = getMonth(str);
             int day = getDay(str);
         
             return getDate(new Integer(year), new Integer(month),
                     new Integer(day));
         } catch (Exception e) {
             return null;
         }
     }
     
     /**
      * Recives date in sql format yyyy-mm-dd and extracts the day
      * @param day
      * @return
      */
     static public int getDay(String str) 
             throws SessionInternalError {
         // from the last '-' to the end
         try {
             return Integer.valueOf(str.substring(str.lastIndexOf('-') + 1)).
                     intValue();
         } catch (NumberFormatException e) {
             throw new SessionInternalError("Cant get the day from " + str);
         }
     }
 
     static public int getMonth(String str) 
             throws SessionInternalError {
         // from the first '-' to the second '-'
         try {
             return Integer.valueOf(str.substring(str.indexOf('-') + 1, 
                     str.lastIndexOf('-'))).intValue();
         } catch (NumberFormatException e) {
             throw new SessionInternalError("Cant get the month from " + str);
         }
                     
     }
 
     static public int getYear(String str) 
             throws SessionInternalError {
         // from the begining to the first '-'
         try {
             return Integer.valueOf(str.substring(0, str.indexOf('-'))).intValue();
         } catch (NumberFormatException e) {
             throw new SessionInternalError("Cant get the year from " + str);
         }
     }
     
     /**
      * Compares to dates, contemplating the posibility of null values.
      * If both are null, they are consider equal.
      * @param date1
      * @param date2
      * @return true if equal, otherwise false. 
      */
    
     static public boolean equal(Date date1, Date date2) {
         boolean retValue;
         if (date1 == null && date2 == null) {
             retValue = true;
         } else if ((date1 == null && date2 != null) ||
                 (date1 != null && date2 == null)) {
             retValue = false;
         } else {
            retValue = (date1.compareTo(date2) == 1);
         }
         
         return retValue;
     }
 
     static public Date truncateDate(Date arg) {
         GregorianCalendar cal = new GregorianCalendar();
         
         cal.setTime(arg);
         cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
         cal.set(GregorianCalendar.MINUTE, 0);
         cal.set(GregorianCalendar.SECOND, 0);
         cal.set(GregorianCalendar.MILLISECOND, 0);
         
         return cal.getTime();
     }
 
     /**
      * Takes a date and returns it as String with the format 'yyyy-mm-dd'
      * @param date
      * @return
      */
     static public String parseDate(Date date) {
         GregorianCalendar cal = new GregorianCalendar();
         
         cal.setTime(date);
         return cal.get(GregorianCalendar.YEAR) + "-" +
                 (cal.get(GregorianCalendar.MONTH) + 1) + "-" +
                 cal.get(GregorianCalendar.DATE); 
     }
     
     static public Integer getPaymentMethod(String creditCardNumber) {
         Integer retValue = null;
         char firstDigit = creditCardNumber.charAt(0);
         
         switch (firstDigit) {
         case '4':
             retValue = Constants.PAYMENT_METHOD_VISA;
             break;
         case '5':
             retValue = Constants.PAYMENT_METHOD_MASTERCARD;
             break;
         case '3':
             // both diners and american expers start with a 3
             if (creditCardNumber.charAt(1) == '7') {
                 retValue = Constants.PAYMENT_METHOD_AMEX;
             } else if (creditCardNumber.charAt(1) == '8') {
                 retValue = Constants.PAYMENT_METHOD_DINERS;
             }
             break;
         case '6':
             retValue = Constants.PAYMENT_METHOD_DISCOVERY;
             break;
         }
         
         return retValue;
     }     
 
     static public String truncateString(String str, int length) {
         if (str == null) return null;
         String retValue;
         if (str.length() <= length) {
             retValue = str;
         } else {
             retValue = str.substring(0, length);
         }
         
         return retValue;
     }
 
     public static String getSysProp(String key) { 
         try {
             return SystemProperties.getSystemProperties().get(key);
         } catch (Exception e) {
             Logger.getLogger(Util.class).error("Can't ready sys property " + 
                     key, e);
             return null;
         }
     }
 
 }
