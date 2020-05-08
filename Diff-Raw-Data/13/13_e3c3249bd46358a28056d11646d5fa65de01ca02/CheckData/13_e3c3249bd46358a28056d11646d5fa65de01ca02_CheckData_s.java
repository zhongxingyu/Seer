 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 package org.bedework.appcommon;
 
 import org.bedework.calfacade.BwEvent;
 
 import java.util.Calendar;
 
 /** A class to check various data fields for consistency and correctness
  * Based on DataChecker class by Greg Barnes
  *
  * @author Mike Douglass   douglm@rpi.edu
  * @author Greg Barnes
  * @version 1.2
  */
 public class CheckData {
   private static final int DATE_STRING_LENGTH = 8;
   private static final int YEAR_START_INDEX = 0;
   private static final int MONTH_START_INDEX = 4;
   private static final int DAY_START_INDEX = 6;
 
   private static final int maxMonth;
   private static final int maxDay;
 
   static {
     /** Used to obtain a number of values we might need.
      */
     Calendar refCal = Calendar.getInstance();
 
     maxMonth = refCal.getMaximum(Calendar.MONTH);
     maxDay = refCal.getMaximum(Calendar.DATE);
   }
 
   private CheckData() {}  // No instantiation allowed.
 
   /** Check that a date string, purportedly in YYYYMMDD format, actually is
    * and represents a valid date.
    *
    * Note that not all errors are flagged.  In particular, days that
    * are too large for a given month (e.g., Feb 30) or months too large
    * for a given year (not possible in the Gregorian calendar, but
    * perhaps in others) are not flagged as long as the day/month
    * represent valid values in *some* month/year.  These 'overflow'
    * dates are handled per the explanation in the
    * <code>java.util.Calendar</code> documentation (e.g., Feb 30, 1999
    * becomes Mar 2, 1999).
    *
    *  @param val       String to check. Should be of form yyyymmdd
    *  @return boolean  true for OK
    */
   public static boolean checkDateString(String val) {
     if (val == null || val.length() != DATE_STRING_LENGTH) {
       return false;
     }
 
     for (int i = 0; i < DATE_STRING_LENGTH; i++) {
       if (val.charAt(i) < '0' || val.charAt(i) > '9') {
         return false;
       }
     }
 
     if (monthNum(val) < 0 ||
         dayNum(val) < 1 ||
         monthNum(val) > maxMonth ||
         dayNum(val) > maxDay ||
         yearNum(val) < 1) {    // there was no year zero
       return false;
     }
 
     return true;
   }
 
     /**
      Extract the year from an eight digit date of the form YYYYMMDD
 
      @param eightDigitDate The eight digit date
      @return The year number
      */
   public static int yearNum(String eightDigitDate) {
      return Integer.parseInt(eightDigitDate.substring(YEAR_START_INDEX,
        YEAR_START_INDEX + 4));
   }
 
     /**
      Extract the month number from an eight digit date of the
      form YYYYMMDD.  Following java.util.Date, the first month
      is month number 0
 
      @param eightDigitDate The eight digit date
      @return The month number
      */
   public static int monthNum(String eightDigitDate) {
      return Integer.parseInt(eightDigitDate.substring(MONTH_START_INDEX,
         MONTH_START_INDEX + 2)) - 1;
   }
 
     /**
      Extract the day number from an eight digit date of the
      form YYYYMMDD.
 
      @param eightDigitDate The eight digit date
      @return The day number
      */
   public static int dayNum(String eightDigitDate) {
      return Integer.parseInt(eightDigitDate.substring(DAY_START_INDEX,
        DAY_START_INDEX + 2));
   }
 
   /** Check for valid transparency setting
    *
    * @param val
    * @return boolean true for ok
    */
   public static boolean checkTransparency(String val) {
    return  BwEvent.transparencyOpaque.equals(val) ||
            BwEvent.transparencyTransparent.equals(val);
   }
 
   /** Check for valid status setting
    *
    * @param val
    * @return boolean true for ok
    */
   public static boolean checkStatus(String val) {
    return  BwEvent.statusConfirmed.equals(val) ||
            BwEvent.statusTentative.equals(val) ||
            BwEvent.statusCancelled.equals(val);
   }
 }
