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
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package org.bedework.calfacade;
 
 import net.fortuna.ical4j.model.Dur;
 import net.fortuna.ical4j.model.property.Duration;
 
 import java.io.Serializable;
 
 /** Class representing a duration.
  *
  * @author Mike Douglass   douglm@rpi.edu
  *  @version 1.0
  */
 public class BwDuration implements Serializable {
   private int days;
   private int hours;
   private int minutes;
   private int seconds;
 
   /** The above or this
    */
   private int weeks;
 
   /** A duration can be negative, e.g. so many hours before an event, for alarms,
    * or positive, e.g. an event lasts x days.
    */
   private boolean negative = false;
 
   /** Constructor
    *
    */
   public BwDuration() {
   }
 
   /** Set the days
    *
    * @param val    int days
    */
   public void setDays(int val) {
     days = val;
   }
 
   /** Get the days
    *
    * @return int    the days
    */
   public int getDays() {
     return days;
   }
 
   /** Set the hours
    *
    * @param val    int hours
    */
   public void setHours(int val) {
     hours = val;
   }
 
   /** Get the hours
    *
    * @return int    the hours
    */
   public int getHours() {
     return hours;
   }
 
   /** Set the minutes
    *
    * @param val    int minutes
    */
   public void setMinutes(int val) {
     minutes = val;
   }
 
   /** Get the minutes
    *
    * @return int    the minutes
    */
   public int getMinutes() {
     return minutes;
   }
 
   /** Set the seconds
    *
    * @param val    int seconds
    */
   public void setSeconds(int val) {
     seconds = val;
   }
 
   /** Get the seconds
    *
    * @return int    the seconds
    */
   public int getSeconds() {
     return seconds;
   }
 
   /** Set the weeks
    *
    * @param val    int weeks
    */
   public void setWeeks(int val) {
     weeks = val;
   }
 
   /** Get the weeks
    *
    * @return int    the weeks
    */
   public int getWeeks() {
     return weeks;
   }
 
   /** Flag a negative duration
    *
    * @param val    boolean negative
    */
   public void setNegative(boolean val) {
     negative = val;
   }
 
   /** Get the negative flag
    *
    * @return boolean    the negative
    */
   public boolean getNegative() {
     return negative;
   }
 
   /* ====================================================================
    *                        Convenience methods
    * ==================================================================== */
 
   /** Return a BwDuration populated from the given String value.
    *
    * @param val    String
    * @return BwDuration
    * @throws CalFacadeException
    */
   public static BwDuration makeDuration(String val) throws CalFacadeException {
     BwDuration db = new BwDuration();
 
     populate(db, val);
 
     return db;
   }
 
   /** Populate the bean from the given String value.
    *
    * @param db    BwDuration
    * @param val   String value
    * @throws CalFacadeException
    */
   public static void populate(BwDuration db, String val) throws CalFacadeException {
     try {
       if (val == null) {
         return;
       }
 
       Dur d = new Dur(val);
 
       if (d.getWeeks() != 0) {
         db.setWeeks(d.getWeeks());
 
         return;
       }
 
       db.setDays(d.getDays());
       db.setHours(d.getHours());
       db.setMinutes(d.getMinutes());
       db.setSeconds(d.getSeconds());
       db.setNegative(d.isNegative());
     } catch (Throwable t) {
       throw new CalFacadeException("Invalid duration");
     }
   }
 
   /** Make an ical Duration
    *
    * @return Duration
    */
   public Duration makeDuration() {
     Dur d;
 
     if (weeks != 0) {
       d = new Dur(getWeeks());
     } else {
       d = new Dur(getDays(), getHours(), getMinutes(), getSeconds());
     }
 
     return new Duration(d);
   }
 
   /* ====================================================================
    *                        Object methods
    * ==================================================================== */
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     if (negative) {
       sb.append("-");
     }
 
     sb.append("P");
 
     if (getWeeks() != 0) {
       sb.append(getWeeks());
       sb.append("W");
     } else {
       if (getDays() != 0) {
         sb.append(getDays());
         sb.append("D");
       }
 
       boolean addedT = false;
 
       addedT = addTimeComponent(sb, getHours(), "H", addedT);
       addedT = addTimeComponent(sb, getMinutes(), "M", addedT);
       addedT = addTimeComponent(sb, getSeconds(), "S", addedT);
     }
 
     return sb.toString();
   }
 
   private boolean addTimeComponent(StringBuffer sb, int val, String flag, boolean addedT) {
     if (val == 0) {
       return addedT;
     }
 
     if (!addedT) {
       sb.append("T");
       addedT = true;
     }
 
     sb.append(val);
     sb.append(flag);
 
     return addedT;
   }
 }
 
