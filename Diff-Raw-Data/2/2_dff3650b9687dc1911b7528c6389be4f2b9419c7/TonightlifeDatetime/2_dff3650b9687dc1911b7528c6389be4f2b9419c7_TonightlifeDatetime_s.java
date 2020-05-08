 package com.tabbie.android.radar;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *  TonightlifeDatetime.java
  * 
  *  Created on: Aug 3, 2012
  *      Author: Valeri Karpov
  * 
  *  Wrapper around Datetime classes for convenience
  */
 
 public class TonightlifeDatetime implements Comparable<TonightlifeDatetime> {
   protected Date d;
   public final String initializer;
 
   public String makeYourTime() {
     final String minutes = d.getMinutes() > 9 ? ":" + d.getMinutes() : ":0"
         + d.getMinutes();
     final int hours = d.getHours();
     if (0 == hours) {
       return "12" + minutes + "am";
     } else if (hours > 0 && hours < 12) {
       return Integer.toString(hours) + minutes + "am";
     } else {
      return Integer.toString(hours - 12) + minutes + "pm";
     }
   }
   
   public TonightlifeDatetime(String datestring) {
     this.initializer = datestring;
     Date d = new Date();
 
     // if there is no time zone, we don't need to do any special parsing.
     if (datestring.endsWith("Z")) {
       try {
         SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");// spec
                                                                               // for
                                                                               // RFC3339
         d = s.parse(datestring);
       } catch (java.text.ParseException pe) {// try again with optional decimals
         SimpleDateFormat s = new SimpleDateFormat(
             "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");// spec for RFC3339 (with
                                                // fractional seconds)
         s.setLenient(true);
         try {
           d = s.parse(datestring);
         } catch (ParseException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
       this.d = d;
       return;
     }
 
     // step one, split off the timezone.
     String firstpart = datestring.substring(0, datestring.lastIndexOf('-'));
     String secondpart = datestring.substring(datestring.lastIndexOf('-'));
 
     // step two, remove the colon from the timezone offset
     secondpart = secondpart.substring(0, secondpart.indexOf(':'))
         + secondpart.substring(secondpart.indexOf(':') + 1);
     datestring = firstpart + secondpart;
     SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");// spec
                                                                         // for
                                                                         // RFC3339
     try {
       d = s.parse(datestring);
     } catch (java.text.ParseException pe) {// try again with optional decimals
       s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");// spec for
                                                                 // RFC3339 (with
                                                                 // fractional
                                                                 // seconds)
       s.setLenient(true);
       try {
         d = s.parse(datestring);
       } catch (ParseException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     }
     this.d = d;
   }
 
   @Override
   public int compareTo(TonightlifeDatetime arg0) {
     return this.d.compareTo(arg0.d);
   }
 }
