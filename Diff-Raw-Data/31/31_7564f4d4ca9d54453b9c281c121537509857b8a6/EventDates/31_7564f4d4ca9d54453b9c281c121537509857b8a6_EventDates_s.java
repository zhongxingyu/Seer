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
 
 package org.bedework.webcommon;
 
 import org.bedework.appcommon.CalendarInfo;
 import org.bedework.calfacade.BwDateTime;
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.ifs.CalTimezones;
 import org.bedework.calsvci.CalSvcI;
 
 import edu.rpi.sss.util.log.MessageEmit;
 
 /** The dates (and/or duration which define when an event happens. These are
  * stored in objects which allow manipulation of indiviual date and time
  * components.
  */
 public class EventDates extends EntityDates {
   /** Starting values or date to go to
    */
   private TimeDateComponents startDate;
 
   /** This takes the 1 character values defined in BwEvent
    */
   private String endType = String.valueOf(BwEvent.endTypeDate);
 
   /** Ending date for events
    */
   private TimeDateComponents endDate;
 
   /** Duration of event
    */
   private DurationBean duration;
 
   /**
    * @param svci
    * @param calInfo
    * @param hour24
    * @param minIncrement
    * @param err
    * @param debug
    */
   public EventDates(CalSvcI svci, CalendarInfo calInfo,
                     boolean hour24, int minIncrement,
                     MessageEmit err, boolean debug) {
     super(svci, calInfo, hour24, minIncrement, err, debug);
   }
 
   /** We set the time date components from the event.
    *
    * <p>If the end date is a date only value it is adjusted backwards by one
    * day to take account of accepted practice.
    *
    * @param val
    * @param timezones
    */
   public void setFromEvent(BwEvent val, CalTimezones timezones) {
     try {
       getStartDate().setDateTime(val.getDtstart());
 
       BwDateTime dtEnd = val.getDtend();
       if (val.getDtstart().getDateType()) {
         dtEnd = dtEnd.getPreviousDay(timezones);
       }
 
       getEndDate().setDateTime(dtEnd);
       duration = DurationBean.makeDurationBean(val.getDuration());
       setEndType(String.valueOf(val.getEndType()));
     } catch (Throwable t) {
       if (debug) {
         t.printStackTrace();
       }
       err.emit(t);
     }
   }
 
   /** We set the time date components to be today. The end date is
    * set to the current date + 1 hour.
    *
    * @param val
    * @param timezones
    */
   public void setNewEvent(BwEvent val, CalTimezones timezones) {
     try {
       java.util.Date now = new java.util.Date(System.currentTimeMillis());
 
       getStartDate().setDateTime(now);
 
       duration = DurationBean.makeOneHour();
 
       BwDateTime end = getStartDate().getDateTime().addDuration(duration, timezones);
       getEndDate().setDateTime(end);
 
       setEndType(String.valueOf(val.getEndType()));
     } catch (Throwable t) {
       if (debug) {
         t.printStackTrace();
       }
       err.emit(t);
     }
   }
 
   /** Update the event from the components. Little validation takes place at this
    * point.
    *
    * <p>If the end date is a date only value it is adjusted backwards by one
    * day to take account of accepted practice.
    *
    * @param val
    * @param timezones
    * @return boolean
    */
   public boolean updateEvent(BwEvent val, CalTimezones timezones) {
     try {
       val.setDtstart(getStartDate().getDateTime());
 
       BwDateTime end = getEndDate().getDateTime();
       end.setDateType(val.getDtstart().getDateType());

       if ((getEndType() == null) ||
           (getEndType().length() != 1)) {
         err.emit("org.bedework.validation.error.invalid.endtype");
         return false;
       }
       val.setEndType(getEndType().charAt(0));
 
       if (val.getEndType() == BwEvent.endTypeDate) {
         if (end.getDateType()) {
           // Adjust forward 1 day
           end = end.getNextDay(timezones);
         }
       }
 
       val.setDtend(end);
       val.setDuration(getDuration().toString());
 
       return true;
     } catch (Throwable t) {
       if (debug) {
         t.printStackTrace();
       }
       err.emit(t);
       return false;
     }
   }
 
   /** Return an object representing an events start date.
    *
    * @return TimeDateComponents  object representing date and time
    */
   public TimeDateComponents getStartDate() {
     if (startDate == null) {
       startDate = getNowTimeComponents();
     }
 
     return startDate;
   }
 
   /** Return an object representing an events end date.
    *
    * @return TimeDateComponents  object representing date and time
    */
   public TimeDateComponents getEndDate() {
     if (endDate == null) {
       endDate = getNowTimeComponents();
     }
 
     return endDate;
   }
 
   /** Return an object representing an events duration.
    *
    * @return Duration  object representing date and time
    */
   public DurationBean getDuration() {
     if (duration == null) {
       duration = new DurationBean();
     }
 
     return duration;
   }
 
   /**
    *
    * @param   val     String defining end type
    */
   public void setEndType(String val) {
     endType = val;
   }
 
   /**
    *
    * @return String defining end type
    */
   public String getEndType() {
     return endType;
   }
 
   /* ====================================================================
    *                        Object methods
    * ==================================================================== */
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("EventDates{startDate=");
     sb.append(startDate);
     sb.append(" endType=");
     sb.append(getEndType());
     sb.append(", endDate=");
     sb.append(endDate);
     sb.append(", ");
     sb.append(duration);
     sb.append("}");
 
     return sb.toString();
   }
 }
 
