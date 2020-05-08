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
 
 import org.bedework.calfacade.base.BwShareableContainedDbentity;
 
 import edu.rpi.cct.uwcal.access.Acl.CurrentAccess;
 
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 /** A calendar in Bedework. This is roughly equivalent to a folder with some
  * rules attached.
  *
  * For caldav compatability we currently do not allow calendar collections
  * inside calendar collections. The flag calendarCollection is set true if
  * if this is intended to hold a collection of calendar objects.
  *
  *   @author Mike Douglass douglm @ rpi.edu
  *  @version 1.0
  */
 public class BwCalendar extends BwShareableContainedDbentity implements Comparable {
   /** The internal name of the calendar
    */
   private String name;
 
   /** Path to this calendar - including this one.
    * The names concatenated with intervening '/'
    */
   private String path;
 
   /** A printable (one line) summary for the calendar
    */
   private String summary;
 
   /** Some sort of description - may be null
    */
   private String description;
 
   /** This identifies an associated mailing list. Its actual value is set
    * by the mailer interface.
    */
   private String mailListId;
 
   /** true if this is to 'hold' calendar objects
    */
   private boolean calendarCollection;
 
   /** The children of this calendar */
   private Collection children;
 
   /* The type of calendar */
   // ENUM
   private int calType;
 
   /** Normal folder */
   public final static int calTypeFolder = 0;
 
   /** Normal calendar collection */
   public final static int calTypeCollection = 1;
 
   /** Trash  */
   public final static int calTypeTrash = 2;
 
   /** Deleted  */
   public final static int calTypeDeleted = 3;
 
   /** Busy */
   public final static int calTypeBusy = 4;
 
   /** Inbox  */
   public final static int calTypeInbox = 5;
 
   /** Outbox  */
   public final static int calTypeOutbox = 6;
 
   /* This field must only be used for cloned copies of an entity as it is
    * specific to a current thread.
    */
   private CurrentAccess currentAccess;
 
   /** Constructor
    */
   public BwCalendar() {
     super();
   }
 
   /** Constructor
    *
    * @param owner          BwUser user who owns the entity
    * @param publick       boolean true if the event is viewable by everyone
    * @param creator        BwUser user who created the entity
    * @param access
    * @param name
    * @param path
    * @param summary
    * @param description
    * @param mailListId
    * @param calendarCollection
    * @param parent
    * @param children
    * @param calType
    */
   public BwCalendar(BwUser owner,
                     boolean publick,
                     BwUser creator,
                     String access,
                     String name,
                     String path,
                     String summary,
                     String description,
                     String mailListId,
                     boolean calendarCollection,
                     BwCalendar parent,
                     Collection children,
                     int calType) {
     super(owner, publick, creator, access);
     this.name = name;
     this.path = path;
     this.summary = summary;
     this.description = description;
     this.mailListId = mailListId;
     this.calendarCollection = calendarCollection;
     setCalendar(parent);
     this.children = children;
     this.calType = calType;
   }
 
   /** Set the name
    *
    * @param val    String name
    */
   public void setName(String val) {
     name = val;
   }
 
   /** Get the name
    *
    * @return String   name
    */
   public String getName() {
     return name;
   }
 
   /** Set the path
    *
    * @param val    String path
    */
   public void setPath(String val) {
     path = val;
   }
 
   /** Get the path
    *
    * @return String   path
    */
   public String getPath() {
     return path;
   }
 
   /** Set the summary
    *
    * @param val    String summary
    */
   public void setSummary(String val) {
     summary = val;
   }
 
   /** Get the summary
    *
    * @return String   summary
    */
   public String getSummary() {
     if (summary == null) {
       return getName();
     }
     return summary;
   }
 
   /** Set the description
    *
    * @param val    description
    */
   public void setDescription(String val) {
     description = val;
   }
 
   /** Get the description
    *
    *  @return String   description
    */
   public String getDescription() {
     return description;
   }
 
   /** Set the mail list id
    *
    * @param val    String mail list id
    */
   public void setMailListId(String val) {
     mailListId = val;
   }
 
   /** Get the mail list id
    *
    *  @return String   mail list id
    */
   public String getMailListId() {
     return mailListId;
   }
 
   /** true if this is to 'hold' calendar objects
    *
    * @param val   boolean true if this is to 'hold' calendar objects
    */
   public void setCalendarCollection(boolean val) {
     calendarCollection = val;
   }
 
   /** true if this is to 'hold' calendar objects
    *
    * @return boolean  true if this is to 'hold' calendar objects
    */
   public boolean getCalendarCollection() {
     return calendarCollection;
   }
 
   /**  Set the set of children
    *
    * @param   val   SortedSet children for this calendar
    */
   public void setChildren(Collection val) {
     children = val;
   }
 
   /**  Get the set of children
    *
    * @return Collection   Calendar children for this calendar
    */
   public Collection getChildren() {
     if (children == null) {
       children = new TreeSet();
     }
     return children;
   }
 
   /** Set the type
    *
    * @param val    type
    */
   public void setCalType(int val) {
     calType = val;
   }
 
   /** Get the description
    *
    *  @return String   description
    */
   public int getCalType() {
     return calType;
   }
 
   /* ====================================================================
    *                   Transient object methods
    * ==================================================================== */
 
   /** Only call fro cloned object
    *
    * @param val CurrentAccess
    */
   public void setCurrentAccess(CurrentAccess val) {
     currentAccess = val;
   }
 
   /**
    * @return CurrentAccess
    */
   public CurrentAccess getCurrentAccess() {
     return currentAccess;
   }
 
   /* ====================================================================
    *                   Convenience methods
    * ==================================================================== */
 
   /** Add a calendar to the set of children that make up this calendar
    * @param val   CalendarVO to add
    */
   public void addChild(BwCalendar val) {
     getChildren().add(val);
   }
 
   /** Remove a calendar from the set of children
    *
    * @param val   BwCalendar to remove
    */
   public void removeChild(BwCalendar val) {
     getChildren().remove(val);
   }
 
   /** Iterate over the children
    *
    * @return Iterator
    */
   public Iterator iterateChildren() {
     return getChildren().iterator();
   }
 
   /**  Any children?
    *
    * @return boolean   true if Calendar has children
    */
   public boolean hasChildren() {
     return getChildren().size() > 0;
   }
 
   /** Check a possible name for validity
    *
    * @param val
    * @return  boolean true for valid calendar name
    */
   public static boolean checkName(String val) {
     if ((val == null) || (val.length() == 0)) {
       return false;
     }
 
     /* First character - letter or digit  */
 
     if (!Character.isLetterOrDigit(val.charAt(0))) {
       return false;
     }
 
     for (int i = 1; i < val.length(); i++) {
       char ch = val.charAt(i);
 
       if (!Character.isLetterOrDigit(ch) &&
           (ch != '_') && (ch != ' ')) {
         return false;
       }
     }
 
     return true;
   }
 
   /** Generate an encoded url referring to this calendar.
    *
    * XXX This should not be here
    * @return String encoded url (or path)
    * @throws CalFacadeException
    */
   public String getEncodedPath() throws CalFacadeException {
    if (getPath() == null) {
      return null;
    }
     try {
       return URLEncoder.encode(getPath(), "UTF-8");
     } catch (Throwable t) {
       throw new CalFacadeException(t);
     }
   }
 
   /** Create a copy of this object but do not clone the children
    *
    * @return BwCalendar object
    */
   public BwCalendar shallowClone() {
     return new BwCalendar((BwUser)getOwner().clone(),
                           getPublick(),
                           (BwUser)getCreator().clone(),
                           getAccess(),
                           getName(),
                           getPath(),
                           getSummary(),
                           getDescription(),
                           getMailListId(),
                           getCalendarCollection(),
                           getCalendar(),
                           null,
                           getCalType());
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   /** Compare this calendar and an object
    *
    * @param  o    object to compare.
    * @return int -1, 0, 1
    */
   public int compareTo(Object o) {
     if (o == null) {
       return -1;
     }
 
     if (!(o instanceof BwCalendar)) {
       return -1;
     }
 
     BwCalendar that = (BwCalendar)o;
 
     return getPath().compareTo(that.getPath());
   }
 
   public int hashCode() {
     if (getPath() == null) {
       return 1;
     }
     return getPath().hashCode();
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer("BwCalendar{");
 
     toStringSegment(sb);
     sb.append(", name=");
     sb.append(String.valueOf(getName()));
     sb.append(", path=");
     sb.append(String.valueOf(getPath()));
     sb.append(", summary=");
     sb.append(String.valueOf(getSummary()));
     sb.append(", description=");
     sb.append(String.valueOf(getDescription()));
     sb.append(", mailListId=");
     sb.append(String.valueOf(getMailListId()));
     sb.append(", calendarCollection=");
     sb.append(String.valueOf(getCalendarCollection()));
 
     sb.append(", children(");
     Iterator it = iterateChildren();
     boolean donech = false;
 
     while (it.hasNext()) {
       BwCalendar ch = (BwCalendar)it.next();
 
       if (!donech) {
         donech = true;
       } else {
         sb.append(", ");
       }
 
       sb.append(ch.getPath());
     }
     sb.append(")");
 
     if (getCurrentAccess() != null) {
       sb.append(", currentAccess=");
       sb.append(getCurrentAccess());
     }
     sb.append("}");
 
     return sb.toString();
   }
 
   public Object clone() {
     return new BwCalendar((BwUser)getOwner().clone(),
                           getPublick(),
                           (BwUser)getCreator().clone(),
                           getAccess(),
                           getName(),
                           getPath(),
                           getSummary(),
                           getDescription(),
                           getMailListId(),
                           getCalendarCollection(),
                           getCalendar(),
                           getChildren(),
                           getCalType());
   }
 }
