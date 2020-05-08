 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.calendar;
 
 import java.util.Date;
 import java.util.List;
 
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.calendar.api.EventApi;
 import com.celements.calendar.engine.ICalendarEngineRole;
 import com.celements.calendar.search.EventSearchResult;
 import com.celements.calendar.search.IEventSearchQuery;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 public interface ICalendar {
 
   public DocumentReference getDocumentReference();
   
   public WikiReference getWikiRef();
 
   public XWikiDocument getCalDoc();
 
   public boolean isArchive();
 
   public Date getStartDate();
 
   /**
    * Sets date and time of Calendar
    * 
    * @param newStartDate
    */
   public ICalendar setStartTimestamp(Date newStartDate);
 
   /**
    * Sets only date of Calendar (time to midnight)
    * 
    * @param newStartDate
    */
   public ICalendar setStartDate(Date newStartDate);
 
   public String getLanguage();
 
   public ICalendar setLanguage(String language);
   
   public SpaceReference getEventSpaceRef();
   
   public List<String> getAllowedSpaces();
 
   /**
    * getAllEvents
    * 
    * @return
    * 
    * @deprecated instead use getAllEvents from CalendarApi or getAllEventsInternal
    *             getAllEvents returning EventsApi will be moved to CalendarApi class.
    */
   @Deprecated
   public List<EventApi> getAllEvents();
 
   public List<IEvent> getAllEventsInternal();
 
   /**
    * getEvents
    * 
    * @return
    * 
    * @deprecated instead use getAllEvents from CalendarApi or getEventsInternal
    *             getEvents returning EventsApi will be moved to CalendarApi class.
    */
   @Deprecated
   public List<EventApi> getEvents(int start, int nb);
 
   /**
    * use internal only. Do not return IEvent objects to the velocity
    * 
    * @param start
    * @param nb
    * @return
    */
   public List<IEvent> getEventsInternal(int start, int nb);
 
   public EventSearchResult searchEvents(IEventSearchQuery query);
 
   public long getNrOfEvents();
 
   public boolean isEmpty();
 
   public IEvent getFirstEvent();
 
   public IEvent getLastEvent();
 
   public List<String> getOverviewFields();
 
   public List<String> getDetailviewFields();
 
   public List<String> getCalOverviewPropertyNames();
 
   /**
    * @deprecated use getCalOverviewPropertyNames() instead
    */
   @Deprecated
   public List<String> getCalOverviewPropertyNames(XWikiContext context);
 
   public List<String> getEventPropertyNames();
 
   /**
    * @deprecated use getEventPropertyNames() instead
    */
   @Deprecated
   public List<String> getEventPropertyNames(XWikiContext context);
 
   public boolean hasDetailLink();
 
   public boolean isSubscribable();
 
   public ICalendarEngineRole getEngine();
 
  public ICalendarEngineRole getEngineWithoutLimitCheck();

 }
