 package com.celements.calendar.service;
 
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.celements.calendar.Calendar;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.util.CalendarUtils;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 @Component
 public class CalendarService implements ICalendarService {
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(CalendarService.class);
 
   public static final String CALENDAR_SERVICE_START_DATE =
     "com.celements.calendar.service.CalendarService.startDate";
 
   @Requirement
   private QueryManager queryManager;
 
   @Requirement
  EntityReferenceResolver<String> stringRefResolver;

  @Requirement
   Execution execution;
 
   private XWikiContext getContext() {
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
 
   public String getEventSpaceForCalendar(DocumentReference calDocRef
       ) throws XWikiException {
     return CalendarUtils.getInstance().getEventSpaceForCalendar(
         getContext().getWiki().getDocument(calDocRef, getContext()), getContext());
   }
 
   public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException {
     return CalendarUtils.getInstance().getAllowedSpacesHQL(calDoc, getContext());
   }
 
   public ICalendar getCalendarByCalRef(DocumentReference calDocRef, boolean isArchive) {
     LOGGER.trace("getCalendarByCalRef: create Calendar reference for [" + calDocRef
         + "], isArchive [" + isArchive + "].");
     return new Calendar(calDocRef, isArchive);
   }
 
   public DocumentReference getCalendarDocRefByCalendarSpace(String calSpace) {
     String xwql = "from doc.object(Classes.CalendarConfigClass) as calConfig";
     xwql += " where calConfig.calendarspace = :calSpace";
     Query query;
     try {
       query = queryManager.createQuery(xwql, Query.XWQL);
       query.bindValue("calSpace", calSpace);
      List<String> blogList = query.execute();
       if(blogList.size() > 0){
        DocumentReference calDocRef = new DocumentReference(stringRefResolver.resolve(
            blogList.get(0), EntityType.DOCUMENT));
        calDocRef.setWikiReference(new WikiReference(getContext().getDatabase()));
        return calDocRef;
       } else {
         LOGGER.error("getCalendarDocRefByCalendarSpace: no calendar found for space ["
             + calSpace + "].");
       }
     } catch (QueryException exp) {
       LOGGER.error("getCalendarDocRefByCalendarSpace: failed to execute XWQL [" + xwql
           + "].", exp);
     }
     return null;
   }
 
 }
