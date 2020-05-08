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
 package org.bedework.calfacade.svc;
 
 import org.bedework.calfacade.BwEvent;
 import org.bedework.calfacade.BwEventAlarm;
 import org.bedework.calfacade.BwEventAnnotation;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 /** This class provides information about an event for a specific user and
  * session.
  *
  * <p>This class allows us to handle thread, or user, specific information.
  *
  * @author Mike Douglass       douglm@rpi.edu
  */
 public class EventInfo implements Comparable, Comparator, Serializable {
   protected BwEvent event;
 
   /** editable is set at retrieval to indicate an event owned by the current
    * user. This only has significance for the personal calendar.
    */
   protected boolean editable;
 
   protected boolean fromRef;
 
   /* ENUM
    * XXX these need changing
    */
 
   /** actual event entry */
   public final static int kindEntry = 0;
   /** 'added' event - from a reference */
   public final static int kindAdded = 1;
   /** from a subscription */
   public final static int kindUndeletable = 2;
 
   private int kind;
 
   private static final String[] kindStr = {
     "entry",
     "reffed",
     "subscribed",
   };
 
   /** This field is set by those input methods which might need to retrieve
    * an event for update, for example the icalendar translators.
    *
    * <p>They retrieve the event based on the guid. If the guid is not found
    * then we assume a new event. Otherwise this flag is set false.
    */
   private boolean newEvent;
 
  /** If this event came from a subscription, this provides the object.
    */
   private BwSubscription subscription;
 
   /** A Collection of related BwEventAlarm objects. These may just be the alarms
    * defined in an ical calendar or all alarms for the given event.
    *
    * <p>These are not fetched while fetching the event. Call getAlarms()
    */
   private Collection alarms = null;
 
   /** If the event is a recurring event master this is any instance overrides
    */
   private Collection overrides;
 
   /** If non-null this event comes from a recurrence
    */
   private String recurrenceId;
 
   /**
    *
    */
   public EventInfo() {
   }
 
   /**
    * @param event
    */
   public EventInfo(BwEvent event) {
     setEvent(event);
   }
 
   /**
    * @param val
    */
   public void setEvent(BwEvent val) {
     event = val;
     fromRef = val instanceof BwEventAnnotation;
   }
 
   /**
    * @return BwEvent associated with this object
    */
   public BwEvent getEvent() {
     return event;
   }
 
   /** editable is set at retrieval to indicate an event owned by the current
    * user. This only has significance for the personal calendar.
    *
    * XXX - not applicable in a shared world?
    *
    * @param val
    */
   public void setEditable(boolean val) {
     editable = val;
   }
 
   /**
    * @return true if object is considered editable
    */
   public boolean getEditable() {
     return editable;
   }
 
   /** Return true if this event is included as a reference
    *
    * @return true if object is from a ref
    */
   public boolean getFromRef() {
     return fromRef;
   }
 
   /**
    * @param val
    */
   public void setKind(int val) {
     kind = val;
   }
 
   /**
    * @return int kind of event
    */
   public int getKind() {
     return kind;
   }
 
   /** Set the newEvent flag
    *
    *  @param  val    boolean true if a new event
    */
   public void setNewEvent(boolean val) {
     newEvent = val;
   }
 
   /** Is the event new?
    *
    *  @return boolean    true if the event is new
    */
   public boolean getNewEvent() {
     return newEvent;
   }
 
   /** This is set in the svci level after retrieval and should always be non-null.
    *
    * @param val
    */
   public void setSubscription(BwSubscription val) {
     subscription = val;
   }
 
   /**
    * @return BwSubscription causing event retrieval
    */
   public BwSubscription getSubscription() {
     return subscription;
   }
 
   /** Get an <code>Iterator</code> over the event's alarms
    *
    * @return Iterator  over the event's alarms
    */
   public Iterator iterateAlarms() {
     return getAlarms().iterator();
   }
 
   /**
    * @param val
    */
   public void setAlarms(Collection val) {
     alarms = val;
   }
 
   /**
    * @return Collection of alarms
    */
   public Collection getAlarms() {
     if (alarms == null) {
       alarms = new TreeSet();
     }
 
     return alarms;
   }
 
   /** clear the event's alarms
    */
   public void clearAlarms() {
     getAlarms().clear();
   }
 
   /**
    * @param val
    */
   public void addAlarm(BwEventAlarm val) {
     Collection as = getAlarms();
 
     if (!as.contains(val)) {
       as.add(val);
     }
   }
 
   /** For a new event this is a set of overrides to apply at event creation.
    *
    * @param val
    */
   public void setOverrides(Collection val) {
     overrides = val;
   }
 
   /**
    * @return Collection of alarms
    */
   public Collection getOverrides() {
     if (overrides == null) {
       overrides = new TreeSet();
     }
 
     return overrides;
   }
 
   /** Set the event's recurrence id
    *
    *  @param val     recurrence id
    */
   public void setRecurrenceId(String val) {
      recurrenceId = val;
   }
 
   /** Get the event's recurrence id
    *
    * @return the event's recurrence id
    */
   public String getRecurrenceId() {
     return recurrenceId;
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   public int compare(Object o1, Object o2) {
     if (!(o1 instanceof EventInfo)) {
       return -1;
     }
 
     if (!(o2 instanceof EventInfo)) {
       return 1;
     }
 
     if (01 == 02) {
       return 0;
     }
 
     EventInfo e1 = (EventInfo)o1;
     EventInfo e2 = (EventInfo)o2;
 
     return e1.getEvent().compare(e1.getEvent(), e2.getEvent());
   }
 
   public int compareTo(Object o2) {
     return compare(this, o2);
   }
 
   public int hashCode() {
     return getEvent().hashCode();
   }
 
   public boolean equals(Object obj) {
     if (this == obj) {
       return true;
     }
 
     if (!(obj instanceof EventInfo)) {
       return false;
     }
 
     return compareTo(obj) == 0;
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("EventInfo{eventid=");
 
     if (getEvent() == null) {
       sb.append("UNKNOWN");
     } else {
       sb.append(getEvent().getId());
     }
     sb.append(", editable=");
     sb.append(getEditable());
     sb.append(", kind=");
     sb.append(kindStr[getKind()]);
 
     Iterator it = iterateAlarms();
 
     while (it.hasNext()) {
       sb.append(", alarm=");
       sb.append(it.next());
     }
     sb.append(", recurrenceId=");
     sb.append(getRecurrenceId());
     sb.append("}");
 
     return sb.toString();
   }
 }
 
