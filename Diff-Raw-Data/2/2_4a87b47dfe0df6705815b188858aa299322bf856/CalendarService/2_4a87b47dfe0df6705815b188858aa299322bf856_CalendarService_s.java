 package com.celements.calendar.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.query.Query;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 
 import com.celements.calendar.Calendar;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.classes.CalendarClasses;
 import com.celements.common.classes.IClassCollectionRole;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 @Component
 public class CalendarService implements ICalendarService {
 
   @Requirement("celements.CalendarClasses")
   private IClassCollectionRole calClasses;
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(CalendarService.class);
 
   public static final String CALENDAR_SERVICE_START_DATE =
       "com.celements.calendar.service.CalendarService.startDate";
 
   @Requirement
   private QueryManager queryManager;
 
   @Requirement
   private IWebUtilsService webUtils;
 
   @Requirement
   private Execution execution;
 
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 
   public List<DocumentReference> getAllCalendars() {
     return getAllCalendars(new HashSet<DocumentReference>());
   }
 
   public List<DocumentReference> getAllCalendars(Collection<DocumentReference> excludes) {
     List<DocumentReference> allCalendars = new ArrayList<DocumentReference>();
     Set<DocumentReference> excludesSet = new HashSet<DocumentReference>(excludes);
     try {
       Query query = queryManager.createQuery(getAllXWQL(), Query.XWQL);
       for (Object fullName : query.execute()) {
         DocumentReference calDocRef = webUtils.resolveDocumentReference(
             fullName.toString());
         if (!excludesSet.contains(calDocRef)) {
           allCalendars.add(calDocRef);
         }
       }
     } catch (QueryException exc) {
       LOGGER.error("failed to execute query [" + getAllXWQL() + "]", exc);
     }
     return allCalendars;
   }
 
   private String getAllXWQL() {
    return "from doc.object(" + CalendarClasses.CALENDAR_EVENT_CLASS 
         + ") as cal where doc.translation = 0";
   }
 
   public String getEventSpaceForCalendar(DocumentReference calDocRef
       ) throws XWikiException {
     XWikiDocument doc = getContext().getWiki().getDocument(calDocRef, getContext());
     String space = doc.getDocumentReference().getName();
     BaseObject obj = doc.getXObject(getCalClasses().getCalendarClassRef(getContext(
         ).getDatabase()));
     if (obj != null) {
       space = obj.getStringValue(CalendarClasses.PROPERTY_CALENDAR_SPACE).trim();
     }
     return space;
   }
 
   public List<String> getAllowedSpaces(DocumentReference calDocRef) throws XWikiException {
     List<String> spaces = new ArrayList<String>();
     BaseObject calObj = getContext().getWiki().getDocument(calDocRef, getContext()
         ).getXObject(getCalClasses().getCalendarClassRef(getContext().getDatabase()));
     if (calObj != null) {
       addNonEmptyString(spaces, calObj.getStringValue(
           CalendarClasses.PROPERTY_CALENDAR_SPACE));
       spaces.addAll(getSubscribedSpaces(calObj));
     }
     return spaces;
   }
 
   private List<String> getSubscribedSpaces(BaseObject calObj) throws XWikiException {
     List<String> spaces = new ArrayList<String>();
     if (calObj != null) {
       DocumentReference calConfRef = getCalClasses().getCalendarClassRef(getContext(
           ).getDatabase());
       for (Object subDocName : calObj.getListValue(
           CalendarClasses.PROPERTY_SUBSCRIBE_TO)) {
         DocumentReference subDocRef = webUtils.resolveDocumentReference(
             subDocName.toString());
         BaseObject subscCalObj = getContext().getWiki().getDocument(subDocRef,
             getContext()).getXObject(calConfRef);
         if (subscCalObj != null) {
           addNonEmptyString(spaces, subscCalObj.getStringValue(
               CalendarClasses.PROPERTY_CALENDAR_SPACE));
         }
       }
     }
     return spaces;
   }
 
   private void addNonEmptyString(List<String> list, String str) {
     str = str.trim();
     if ((str != null) && (str.length() > 0)) {
       list.add(str);
     }
   }
 
   @Deprecated
   public String getAllowedSpacesHQL(XWikiDocument calDoc) throws XWikiException {
     String spaceHQL = "";
     List<String> spaces = getAllowedSpaces(calDoc.getDocumentReference());
     for (String space : spaces) {
       if (spaceHQL.length() > 0) {
         spaceHQL += " or ";
       }
       spaceHQL += "obj.name like '" + space + ".%'";
     }
     if (spaceHQL.length() > 0) {
       spaceHQL = "(" + spaceHQL + ")";
     } else {
       spaceHQL = "(obj.name like '.%')";
     }
     return spaceHQL;
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
       if (blogList.size() > 0) {
         return webUtils.resolveDocumentReference(blogList.get(0));
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
 
   public Date getMidnightDate(Date date) {
     Date dateMidnight = null;
     if (date != null) {
       java.util.Calendar cal = java.util.Calendar.getInstance();
       cal.setTime(date);
       cal.set(java.util.Calendar.HOUR, 0);
       cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
       cal.set(java.util.Calendar.MINUTE, 0);
       cal.set(java.util.Calendar.SECOND, 0);
       dateMidnight = cal.getTime();
     }
     LOGGER.debug("date is: " + dateMidnight);
     return dateMidnight;
   }
 
   private CalendarClasses getCalClasses() {
     return (CalendarClasses) calClasses;
   }
 
 }
