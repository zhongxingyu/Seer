 package com.celements.calendar.manager;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.VelocityContext;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.EntityReferenceResolver;
 import org.xwiki.model.reference.EntityReferenceSerializer;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.calendar.Event;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.api.EventApi;
 import com.celements.calendar.plugin.CelementsCalendarPlugin;
 import com.celements.calendar.service.ICalendarService;
 import com.celements.calendar.util.CalendarUtils;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.store.XWikiStoreInterface;
 
 @Component("default")
 public class EventsManager implements IEventManager {
 
   @Requirement
   Execution execution;
 
   private static final Log mLogger = LogFactory.getFactory().getInstance(
       EventsManager.class);
 
   @Requirement
   private ICalendarService calService;
 
   //TODO we must change to 'default' serializer with the wikiname included
   @Requirement("local")
   private EntityReferenceSerializer<String> refLocalSerializer;
 
   @Requirement("default")
   private EntityReferenceSerializer<String> refDefaultSerializer;
 
  @Requirement("default/reference")
  private EntityReferenceResolver<String> relativeRefResolver;
 
   public EventsManager() {}
 
   public List<EventApi> getEvents(XWikiDocument calDoc, int start, int nb,
       boolean isArchive) throws XWikiException {
     String query = getQuery(calDoc, isArchive, false);
     List<EventApi> eventList = new ArrayList<EventApi>();
     try {
       XWikiStoreInterface storage = getContext().getWiki().getStore();
       List<String> eventDocs = storage.search(query, nb, start, getContext());
       mLogger.debug(eventDocs.size() + " events found. " + eventDocs);
       for (String eventDocName : eventDocs) {
        mLogger.debug(eventDocName);
         Event theEvent = new Event(getDocRefFromFullName(eventDocName), getContext());
         if(checkEventSubscription(calDoc, theEvent)){
          eventList.add(new EventApi(theEvent, getContext())
          );
         }
       }
     } catch (XWikiException e) {
       mLogger.error(e);
     }
     return eventList;
   }
 
   private DocumentReference getDocRefFromFullName(String eventDocName) {
    DocumentReference eventRef = (DocumentReference) relativeRefResolver.resolve(
        eventDocName, EntityType.DOCUMENT);
     eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
     return eventRef;
   }
 
   public long countEvents(XWikiDocument calDoc, boolean isArchive) {
     List<Object> eventCount = null;
     try {
       eventCount = getContext().getWiki().getStore().search(getQuery(calDoc, isArchive,
           true), 0, 0, getContext());
     } catch (XWikiException e) {
       mLogger.error("Exception while counting number of events for calendar '" + 
           ((calDoc != null)?calDoc.getDocumentReference():calDoc) + "'", e);
     }
     if((eventCount != null) && (eventCount.size() > 0)) {
       mLogger.debug("Count resulted in " + eventCount.get(0) + " which is of class " +
           eventCount.get(0).getClass());
       return (Long)eventCount.get(0);
     }
     return 0;
   }
   
   private String getQuery(XWikiDocument calDoc, boolean isArchive, boolean count
       ) throws XWikiException {
     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     
     String timeComp = ">=";
     String sortOrder = "asc";
     String selectEmptyDates = "or ec.eventDate is null";
     if(isArchive){
       timeComp = "<";
       sortOrder = "desc";
       selectEmptyDates = "";
     }
     String hql = "select ";
     if(count){
       hql += "count(obj.name)";
     } else {
       hql += "obj.name";
     }
     hql += " from XWikiDocument doc, BaseObject as obj, ";
     hql += CelementsCalendarPlugin.CLASS_EVENT + " as ec ";
     hql += "where doc.fullName = obj.name and doc.translation = 0 and ec.id.id=obj.id ";
     VelocityContext vcontext = ((VelocityContext) getContext().get("vcontext"));
     String defaultLanguage = (String)vcontext.get("default_language");
     hql += "and ec.lang='" + defaultLanguage + "' ";
     hql += "and (ec.eventDate " + timeComp + " '"
       + format.format(getMidnightDate()) + "' " + selectEmptyDates + ") and ";
     hql += CalendarUtils.getInstance().getAllowedSpacesHQL(calDoc, getContext());
     hql += " order by ec.eventDate " + sortOrder + ", ec.eventDate_end " + sortOrder;
     mLogger.debug(hql);
     
     return hql;
   }
   
   private Date getMidnightDate() {
     return getMidnightDate(new Date());
   }
 
   /**
    * getMidnightDate
    * 
    * @param startDate may not be null
    * @return
    */
   private Date getMidnightDate(Date startDate) {
     java.util.Calendar cal = java.util.Calendar.getInstance();
     cal.setTime(startDate);
     cal.set(java.util.Calendar.HOUR, 0);
     cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
     cal.set(java.util.Calendar.MINUTE, 0);
     cal.set(java.util.Calendar.SECOND, 0);
     Date dateMidnight = cal.getTime();
     mLogger.debug("date is: " + dateMidnight);
     return dateMidnight;
   }
   
   private boolean checkEventSubscription(XWikiDocument calDoc, Event theEvent
       ) throws XWikiException {
     return isHomeCalendar(calDoc.getDocumentReference(), theEvent)
         || isEventSubscribed(calDoc.getDocumentReference(), theEvent);
   }
 
   private boolean isHomeCalendar(DocumentReference calDocRef, Event theEvent
       ) throws XWikiException {
     return theEvent.getDocumentReference().getLastSpaceReference().equals(
         calService.getEventSpaceForCalendar(calDocRef, getContext()));
   }
   
   private boolean isEventSubscribed(DocumentReference calDocRef, Event theEvent
       ) throws XWikiException {
     BaseObject obj = getSubscriptionObject(calDocRef, theEvent);
 
     ICalendar calendar = theEvent.getCalendar(getContext());
     BaseObject calObj = null;
     if ((calendar != null) && (calendar.getCalDoc() != null)){
       calObj = calendar.getCalDoc().getXObject(getCalenderConfigClassRef());
     }
     boolean isSubscribed = false;
     if((obj != null) && (obj.getIntValue("doSubscribe") == 1)
         && (calObj != null) && (calObj.getIntValue("is_subscribable") == 1)){
       isSubscribed = true;
     }
     return isSubscribed;
   }
 
   private DocumentReference getCalenderConfigClassRef() {
     return new DocumentReference(getContext().getDatabase(),
         CelementsCalendarPlugin.CLASS_CALENDAR_SPACE,
         CelementsCalendarPlugin.CLASS_CALENDAR_DOC);
   }
 
   private BaseObject getSubscriptionObject(DocumentReference calDocRef, Event event) {
     BaseObject subscriptObj = event.getEventDocument().getXObject(
         getSubscriptionClassRef(), "subscriber", refDefaultSerializer.serialize(
             calDocRef), false);
     if (subscriptObj == null) {
       //for backwards compatibility
       subscriptObj = event.getEventDocument().getXObject(getSubscriptionClassRef(),
           "subscriber", refLocalSerializer.serialize(calDocRef), false);
     }
     return subscriptObj;
   }
 
   private DocumentReference getSubscriptionClassRef() {
     return new DocumentReference(getContext().getDatabase(),
         CelementsCalendarPlugin.SUBSCRIPTION_CLASS_SPACE,
         CelementsCalendarPlugin.SUBSCRIPTION_CLASS_DOC);
   }
   
   private XWikiContext getContext() {
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
 
 }
