 /*
  * Copyright 2005-2007 Open Source Applications Foundation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.osaf.cosmo.calendar;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.ComponentList;
 import net.fortuna.ical4j.model.Date;
 import net.fortuna.ical4j.model.DateList;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Dur;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.Period;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.PropertyList;
 import net.fortuna.ical4j.model.Recur;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.parameter.Value;
 import net.fortuna.ical4j.model.property.DtEnd;
 import net.fortuna.ical4j.model.property.DtStart;
 import net.fortuna.ical4j.model.property.Duration;
 import net.fortuna.ical4j.model.property.RDate;
 import net.fortuna.ical4j.model.property.RRule;
 
 /**
  * Utility class that contains apis that that involve
  * expanding recurring components.
  */
 public class RecurrenceExpander {
     
     private static Date MAX_EXPAND_DATE = null;
     private static Date MAX_EXPAND_DATE_TIME = null;
     
     static {
         try {
             // Expand out to 2030 for those recurrence rules
             // that have an end.  Recurring events with no end
             // will be indexed as infinite.
             MAX_EXPAND_DATE = new Date("20300101");
             MAX_EXPAND_DATE_TIME = new DateTime("20300101T010000");
         } catch (ParseException e) {}
     }
     
     public RecurrenceExpander() {
         super();
     }
     
     /**
      * Return start and end Date that represent the start of the first 
      * occurrence of a recurring component and the end of the last
      * occurence.  If the recurring component has no end(infinite recurring event),
      * then no end date will be returned.
      * @param calendar Calendar containing master and modification components
      * @return array containing start (located at index 0) and end (index 1) of
      *         recurring component.
      */
     public Date[] calculateRecurrenceRange(Calendar calendar) {
         ComponentList vevents = calendar.getComponents().getComponents(
                 Component.VEVENT);
         
         List<Component> exceptions = new ArrayList<Component>();
         Component masterComp = null;
         
         // get list of exceptions (VEVENT with RECURRENCEID)
         for (Iterator<VEvent> i = vevents.iterator(); i.hasNext();) {
             VEvent event = i.next();
             if (event.getRecurrenceId() != null)
                 exceptions.add(event);
             else
                 masterComp = event;
             
         }
         
         return calculateRecurrenceRange(masterComp, exceptions);
     }
 
     /**
      * Return a start and end Date that represents the start of the first
      * occurence of a recurring component and the end of the last occurence.  If
      * the recurring component has no end(infinite recurring event),
      * then no end date will be returned.
      * 
      * @param comp Component to analyze
      * @return array containing start (located at index 0) and end (index 1) of
      *         recurring component.
      */
     public Date[] calculateRecurrenceRange(Component comp) {
         return calculateRecurrenceRange(comp, new ArrayList<Component>(0));
         
     }    
     /**
      * Return a start and end Date that represents the start of the first
      * occurence of a recurring component and the end of the last occurence.  If
      * the recurring component has no end(infinite recurring event),
      * then no end date will be returned.
      * 
      * @param comp Component to analyze
      * @param modifications modifications to component
      * @return array containing start (located at index 0) and end (index 1) of
      *         recurring component.
      */
     public Date[] calculateRecurrenceRange(Component comp, List<Component> modifications) {
 
         Date[] dateRange = new Date[2];
         Date start = getStartDate(comp);
         
         // must have start date
         if (start == null) {
             return null;
         }
         
         Dur duration = null;
         Date end = getEndDate(comp);
         if (end == null) {
             if (start instanceof DateTime) {
                 // Its an timed event with no duration
                 duration = new Dur(0, 0, 0, 0);
             } else {
                 // Its an all day event so duration is one day
                 duration = new Dur(1, 0, 0, 0);
             }
             end = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(start), start);
         } else {
             if(end instanceof DateTime) {
                 // Handle case where dtend is before dtstart, in which the duration
                 // will be 0, since it is a timed event
                 if(end.before(start)) {
                     end = org.osaf.cosmo.calendar.util.Dates.getInstance(
                             new Dur(0, 0, 0, 0).getTime(start), start);
                 }
             } else {
                 // Handle case where dtend is before dtstart, in which the duration
                 // will be 1 day since its an all-day event
                 if(end.before(start)) {
                     end = org.osaf.cosmo.calendar.util.Dates.getInstance(
                             new Dur(1, 0, 0, 0).getTime(start), start);
                 }
             }
             duration = new Dur(start, end);
         }
         
         // Always add master's occurence
         dateRange[0] = start;
         dateRange[1] = end;
         
         // Now tweak range based on RDATE, RRULE, and component modifications
         // For now, ignore EXDATE and EXRULE because RDATE and RRULE will 
         // give us the broader range.
         
         // recurrence dates..
         PropertyList rDates = comp.getProperties()
                 .getProperties(Property.RDATE);
         for (Iterator i = rDates.iterator(); i.hasNext();) {
             RDate rdate = (RDate) i.next();
             // Both PERIOD and DATE/DATE-TIME values allowed
             if (Value.PERIOD.equals(rdate.getParameters().getParameter(
                     Parameter.VALUE))) {
                 for (Iterator j = rdate.getPeriods().iterator(); j.hasNext();) {
                     Period period = (Period) j.next();
                     if (period.getStart().before(dateRange[0]))
                         dateRange[0] = period.getStart();
                     if (period.getEnd().after(dateRange[1]))
                         dateRange[1] = period.getEnd();
                     
                 }
             } else {
                 for (Iterator j = rdate.getDates().iterator(); j.hasNext();) {
                     Date startDate = (Date) j.next();
                     Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration
                             .getTime(startDate), startDate);
                     if (startDate.before(dateRange[0]))
                         dateRange[0] = startDate;
                     if (endDate.after(dateRange[1]))
                         dateRange[1] = endDate;
                 }
             }
         }
 
         // recurrence rules..
         PropertyList rRules = comp.getProperties()
                 .getProperties(Property.RRULE);
         for (Iterator i = rRules.iterator(); i.hasNext();) {
             RRule rrule = (RRule) i.next();
             Recur recur = rrule.getRecur();
             
             // If this is an infinite recurring event, we are done processing
             // the rules
             if(recur.getCount()==-1 && recur.getUntil()==null) {
                 dateRange[1] = null;
                 break;
             }
             
             // DateList startDates = rrule.getRecur().getDates(start.getDate(),
             // adjustedRangeStart, rangeEnd, (Value)
             // start.getParameters().getParameter(Parameter.VALUE));
             DateList startDates = rrule.getRecur().getDates(start, start,
                     getMaxExpandDate(start),
                     (start instanceof DateTime) ? Value.DATE_TIME : Value.DATE);
             
             // Dates are sorted, so get the last occurence, and calculate the end
             // date and update dateRange if necessary
             if(startDates.size()>0) {
                 Date lastStart = (Date) startDates.get(startDates.size()-1);
                 Date endDate = org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getTime(lastStart), start);
                 
                 if (endDate.after(dateRange[1]))
                     dateRange[1] = endDate;
             }
         }
         
         // event modifications....
         for(Component modComp : modifications) {
             Date startMod = getStartDate(modComp);
             Date endMod = getEndDate(modComp);
             if (startMod.before(dateRange[0]))
                 dateRange[0] = startMod;
            if (dateRange[1] != null && endMod.after(dateRange[1]))
                 dateRange[1] = endMod;
             
             // TODO: handle THISANDFUTURE/THISANDPRIOR edge cases
         }
         
         // make sure timezones are consistent with original timezone
         if(start instanceof DateTime) {
             ((DateTime) dateRange[0]).setTimeZone(((DateTime) start).getTimeZone());
             if(dateRange[1]!=null)
             ((DateTime) dateRange[0]).setTimeZone(((DateTime) start).getTimeZone());
         }
         
         return dateRange;
     }
 
     /**
      * Expand recurring event for given time-range.
      * @param calendar calendar containing recurring event and modifications
      * @param rangeStart expand start
      * @param rangeEnd expand end
      * @return InstanceList containing all occurences of recurring event during
      *         time range
      */
     public InstanceList getOcurrences(Calendar calendar, Date rangeStart, Date rangeEnd) {
         ComponentList vevents = calendar.getComponents().getComponents(
                 Component.VEVENT);
         
         List<Component> exceptions = new ArrayList<Component>();
         Component masterComp = null;
         
         // get list of exceptions (VEVENT with RECURRENCEID)
         for (Iterator<VEvent> i = vevents.iterator(); i.hasNext();) {
             VEvent event = i.next();
             if (event.getRecurrenceId() != null)
                 exceptions.add(event);
             else
                 masterComp = event;
             
         }
         
         return getOcurrences(masterComp, exceptions, rangeStart, rangeEnd);
     }
     
     /**
      * Expand recurring compnent for given time-range.
      * @param component recurring component to expand
      * @param rangeStart expand start date
      * @param rangeEnd expand end date
      * @return InstanceList containing all occurences of recurring event during
      *         time range
      */
     public InstanceList getOcurrences(Component component, Date rangeStart, Date rangeEnd) {
         return getOcurrences(component, new ArrayList<Component>(0), rangeStart, rangeEnd);
     }
     
     /**
      * Expand recurring compnent for given time-range.
      * @param component recurring component to expand
      * @param modifications modifications to recurring component
      * @param rangeStart expand start date
      * @param rangeEnd expand end date
      * @return InstanceList containing all occurences of recurring event during
      *         time range 
      */
     public InstanceList getOcurrences(Component component, List<Component> modifications, Date rangeStart, Date rangeEnd) {
         InstanceList instances = new InstanceList();
         instances.addMaster(component, rangeStart, rangeEnd);
         for(Component mod: modifications)
             instances.addOverride(mod);
         
         return instances;
     }
     
     private Date getStartDate(Component comp) {
         DtStart prop = (DtStart) comp.getProperties().getProperty(
                 Property.DTSTART);
         return (prop != null) ? prop.getDate() : null;
     }
 
     private Date getEndDate(Component comp) {
         DtEnd dtEnd = (DtEnd) comp.getProperties().getProperty(Property.DTEND);
         // No DTEND? No problem, we'll use the DURATION if present.
         if (dtEnd == null) {
             Date dtStart = getStartDate(comp);
             Duration duration = (Duration) comp.getProperties().getProperty(
                     Property.DURATION);
             if (duration != null) {
                 dtEnd = new DtEnd(org.osaf.cosmo.calendar.util.Dates.getInstance(duration.getDuration()
                         .getTime(dtStart), dtStart));
             }
         }
         return (dtEnd != null) ? dtEnd.getDate() : null;
     }
     
     private Date getMaxExpandDate(Date date) {
         if(date instanceof DateTime)
             return MAX_EXPAND_DATE_TIME;
         else
             return MAX_EXPAND_DATE;
     }
 }
