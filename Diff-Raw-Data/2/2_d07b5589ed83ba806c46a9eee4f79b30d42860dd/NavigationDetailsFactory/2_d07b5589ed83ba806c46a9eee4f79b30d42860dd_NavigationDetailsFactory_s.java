 package com.celements.calendar.navigation.factories;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.calendar.Calendar;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.IEvent;
 import com.celements.calendar.manager.IEventManager;
 import com.celements.calendar.search.EventSearchQuery;
 import com.xpn.xwiki.web.Utils;
 
 public class NavigationDetailsFactory implements INavigationDetailsFactory {
 
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       NavigationDetailsFactory.class);
 
   private IEventManager eventMgr;
 
   public NavigationDetails getNavigationDetails(Date startDate, int offset) {
     return new NavigationDetails(startDate, offset);
   }
 
   public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
       IEvent event) {
     return getNavigationDetails(calConfigDocRef, event, null);
   }
 
   public NavigationDetails getNavigationDetails(DocumentReference calConfigDocRef,
       IEvent event, EventSearchQuery query) {
     LOGGER.debug("getNavigationDetails for '" + event + "'");
     Date eventDate = event.getEventDate();
     if (eventDate != null) {
       ICalendar cal = new Calendar(calConfigDocRef, false);
       cal.setStartDate(eventDate);
       int offset = 0;
       int nb = 10;
       int eventIndex, start = 0;
       List<IEvent> events;
       boolean hasMore, notFound;
       do {
         if (query == null) {
           events = getEventMgr().getEventsInternal(cal, start, nb);
         } else {
           events = getEventMgr().searchEvents(cal, query).getEventList(start, nb);
         }
         hasMore = events.size() == nb;
         eventIndex = events.indexOf(event);
         notFound = eventIndex < 0;
         offset = start + eventIndex;
         start = start + nb;
         nb = nb * 2;
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug("getNavigationDetails: events '" + events + "'");
           LOGGER.debug("getNavigationDetails: index for event '" + eventIndex);
         }
       } while (notFound && hasMore);
       if (!notFound) {
         NavigationDetails navDetail = new NavigationDetails(cal.getStartDate(), offset);
         LOGGER.debug("getNavigationDetails: found '" + navDetail + "'");
         return navDetail;
       } else {
        LOGGER.debug("getNavigationDetails: not found");
       }
     } else {
       LOGGER.error("getNavigationDetails: eventDate is null for '" + event + "'");
     }
     return null;
   }
 
   private IEventManager getEventMgr() {
     if (eventMgr == null) {
       eventMgr = Utils.getComponent(IEventManager.class);
     }
     return eventMgr;
   }
 
   void injectEventMgr(IEventManager eventMgr) {
     this.eventMgr = eventMgr;
   }
 
 }
