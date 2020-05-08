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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.EntityReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.calendar.api.EventApi;
 import com.celements.calendar.classes.CalendarClasses;
 import com.celements.calendar.engine.ICalendarEngineRole;
 import com.celements.calendar.manager.IEventManager;
 import com.celements.calendar.search.EventSearchResult;
 import com.celements.calendar.search.IEventSearchQuery;
 import com.celements.calendar.service.ICalendarService;
 import com.celements.web.service.IWebUtilsService;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.objects.classes.BaseClass;
 import com.xpn.xwiki.web.Utils;
 
 public class Calendar implements ICalendar {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(Calendar.class);
 
   private static final String _OVERVIEW_DEFAULT_CONFIG =
       "date,time,l_title,location";
   private static final String _DETAILVIEW_DEFAULT_CONFIG =
       "date,time,l_title,location,l_description";
   private static final List<String> _NON_EVENT_PROPERTYS = Arrays.asList("lang", 
       "isSubscribable", "eventDate", "eventDate_end");
 
   private DocumentReference calConfigDocRef;
   private Date startDate = new Date();
   private boolean isArchive;
   private String defaultLang;
   private String language;
   private ICalendarEngineRole engine;
 
   private IEventManager eventMgr;
   private ICalendarService calService;
 
   /**
    * 
    * @param calConfigDoc
    * @param isArchive
    * @param context
    * 
    * @deprecated use Calendar(DocumentReference, boolean) instead
    */
   @Deprecated
   public Calendar(XWikiDocument calConfigDoc, boolean isArchive, XWikiContext context){
     this(calConfigDoc.getDocumentReference(), isArchive);
   }
 
   /**
    * 
    * @param calConfigDocRef
    * @param isArchive
    * @param context
    * 
    * @deprecated use Calendar(DocumentReference, boolean) instead
    */
   @Deprecated
   public Calendar(DocumentReference calConfigDocRef, boolean isArchive,
       XWikiContext context) {
     this(calConfigDocRef, isArchive);
   }
 
   public Calendar(DocumentReference calConfigDocRef, boolean isArchive) {
     this.calConfigDocRef = calConfigDocRef;
     this.isArchive = isArchive;
   }
 
   public Calendar(DocumentReference calConfigDocRef, boolean isArchive, Date startDate) {
     this.calConfigDocRef = calConfigDocRef;
     this.isArchive = isArchive;
     if (startDate != null) {
       this.startDate = startDate;
     }
   }
 
   @Override
   public DocumentReference getDocumentReference() {
     return calConfigDocRef;
   }
 
   @Override
   public WikiReference getWikiRef() {
     return getWebUtilsService().getWikiRef((EntityReference) getDocumentReference());
   }
 
   @Override
   public XWikiDocument getCalDoc() {
     try {
       return getContext().getWiki().getDocument(this.calConfigDocRef, getContext());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get cal doc for [" + this.calConfigDocRef + "].", exp);
     }
     return null;
   }
 
   @Override
   public boolean isArchive(){
     return isArchive;
   }
 
   @Override
   public Date getStartDate() {
     return this.startDate;
   }
 
   @Override
   public ICalendar setStartTimestamp(Date newStartDate) {
     if (newStartDate != null) {
       this.startDate = newStartDate;
     }
     return this;
   }
 
   @Override
   public ICalendar setStartDate(Date newStartDate) {
     return setStartTimestamp(getCalService().getMidnightDate(newStartDate));
   }
 
   @Override
   public String getLanguage() {
     if (this.language != null) {
       return this.language;
     }
     return getDefaultLang();
   }
 
   @Override
   public ICalendar setLanguage(String language) {
     this.language = language;
     return this;
   }
 
   private final String getDefaultLang() {
     if (defaultLang == null) {
       SpaceReference spaceRef = getEventSpaceRef();
       if (spaceRef != null) {
         defaultLang = getWebUtilsService().getDefaultLanguage(spaceRef.getName());
       }
     }
     return defaultLang;
   }
 
   @Override
   public SpaceReference getEventSpaceRef() {
     SpaceReference ret = null;
     try {
       ret = getCalService().getEventSpaceRefForCalendar(getDocumentReference());
     } catch (XWikiException exc) {
       LOGGER.error("getEventSpaceRef: failed to get event space for '" 
           + getDocumentReference() + "'", exc);
     }
     return ret;
   }
 
   @Override
   public List<String> getAllowedSpaces() {
     try {
       return getCalService().getAllowedSpaces(getDocumentReference());
     } catch (XWikiException exp) {
       LOGGER.error("Failed to get allowed spaces for '" + getDocumentReference() + "'", 
           exp);
     }
     return new ArrayList<String>();
   }
 
   @Deprecated
   @Override
   public List<EventApi> getAllEvents(){
     return getEventMgr().getEvents(this, 0, 0);
   }
 
   @Override
   public List<IEvent> getAllEventsInternal() {
     return getEventMgr().getAllEventsInternal(this);
   }
 
   @Deprecated
   public List<EventApi> getEvents(int start, int nb) {
     return getEventMgr().getEvents(this, start < 0 ? 0 : start, nb < 0 ? 0 : nb);
   }
 
   @Override
   public List<IEvent> getEventsInternal(int start, int nb) {
     return getEventMgr().getEventsInternal(this, start < 0 ? 0 : start, nb < 0 ? 0 : nb);
   }
 
   @Override
   public EventSearchResult searchEvents(IEventSearchQuery query) {
     return getEventMgr().searchEvents(this, query);
   }
 
   @Override
   public long getNrOfEvents() {
     return getEventMgr().countEvents(this);
   }
 
   @Override
   public boolean isEmpty() {
     return getNrOfEvents() == 0;
   }
 
   @Override
   public IEvent getFirstEvent() {
     return getEventMgr().getFirstEvent(this);
   }
 
   @Override
   public IEvent getLastEvent() {
     return getEventMgr().getLastEvent(this);
   }
 
   @Override
   public List<String> getOverviewFields() {
     return Arrays.asList(getOverviewConfig().split(","));
   }
 
   private String getOverviewConfig() {
     String overviewConfig = _OVERVIEW_DEFAULT_CONFIG;
     BaseObject calConfigObj = getConfigObject();
     if ((calConfigObj != null)
         && (getPropertyStringValueForOverviewConfig(calConfigObj) != null)
         && (!"".equals(getPropertyStringValueForOverviewConfig(calConfigObj)))) {
       overviewConfig = getPropertyStringValueForOverviewConfig(calConfigObj);
     }
     LOGGER.debug("overview config: '" + overviewConfig + "'");
     return overviewConfig;
   }
 
   private String getPropertyStringValueForOverviewConfig(BaseObject calConfigObj) {
     return calConfigObj.getStringValue(
         CalendarClasses.PROPERTY_OVERVIEW_COLUMN_CONFIG);
   }
 
   @Override
   public List<String> getDetailviewFields() {
     return Arrays.asList(getDetailviewConfig().split(","));
   }
 
   private String getDetailviewConfig() {
     String detailviewConfig = _DETAILVIEW_DEFAULT_CONFIG;
     BaseObject calConfigObj = getConfigObject();
     if ((calConfigObj != null)
         && (calConfigObj.getStringValue(CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG) != null)
         && (!"".equals(calConfigObj.getStringValue(CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG)))) {
       detailviewConfig = calConfigObj.getStringValue(CalendarClasses.PROPERTY_EVENT_COLUMN_CONFIG);
     }
     LOGGER.debug("detailview config: '" + detailviewConfig + "'");
     return detailviewConfig;
   }
 
   protected BaseObject getConfigObject() {
     return getCalDoc().getXObject(new DocumentReference(getContext().getDatabase(),
         CalendarClasses.CALENDAR_CONFIG_CLASS_SPACE,
         CalendarClasses.CALENDAR_CONFIG_CLASS_DOC));
   }
 
   @Override
   public List<String> getCalOverviewPropertyNames() {
     return getCalOverviewPropertyNames_internal(getContext());
   }
 
   /**
    * @deprecated use getCalOverviewPropertyNames() instead
    */
   @Deprecated
   @Override
   public List<String> getCalOverviewPropertyNames(XWikiContext context) {
     return getCalOverviewPropertyNames_internal(context);
   }
 
   private List<String> getCalOverviewPropertyNames_internal(XWikiContext context) {
     List<String> propNames = getEventPropertyNames(context);
     if (hasDetailLink()) {
       propNames.add("detaillink");
     }
     return propNames;
   }
 
   @Override
   public List<String> getEventPropertyNames() {
     return getEventPropertyNames_internal(getContext());
   }
 
   /**
    * @deprecated use getEventPropertyNames() instead
    */
   @Deprecated
   @Override
   public List<String> getEventPropertyNames(XWikiContext context) {
     return getEventPropertyNames_internal(context);
   }
 
   private List<String> getEventPropertyNames_internal(XWikiContext context) {
     List<String> propNames = new ArrayList<String>();
     try {
       XWikiDocument doc = context.getWiki().getDocument(new DocumentReference(
           context.getDatabase(), CalendarClasses.CALENDAR_EVENT_CLASS_SPACE,
           CalendarClasses.CALENDAR_EVENT_CLASS_DOC), context);
       BaseClass bclass = doc.getXClass();
       Object[] props = bclass.getPropertyNames();
       for(int i = 0; i < props.length; i++) {
         String propertyName = (String) props[i];
         if (!_NON_EVENT_PROPERTYS.contains(propertyName)) {
           propNames.add(propertyName);
         }
       }
       propNames.add("date");
       propNames.add("time");
       propNames.add("date_end");
       propNames.add("time_end");
     } catch (XWikiException e) {
       LOGGER.error("Event Class Document not available", e);
     }
     return propNames;
   }
 
   @Override
   public boolean hasDetailLink() {
     BaseObject configObj = getConfigObject();
     return ((configObj != null) && (configObj.getIntValue("hasMoreLink") == 1));
   }
 
   @Override
   public boolean isSubscribable() {
     BaseObject configObj = getConfigObject();
     return (configObj.getIntValue(
         CalendarClasses.PROPERTY_IS_SUBSCRIBABLE) == 1);
   }
 
   @Override
   public ICalendarEngineRole getEngine() {
     if (engine == null) {
       ICalendarEngineRole defaultEngine = Utils.getComponent(ICalendarEngineRole.class);
      engine = getEngineWithoutLimitCheck();
       if (!StringUtils.equals(engine.getName(), defaultEngine.getName())) {
         long limit = engine.getEngineLimit();
         long size = engine.countEvents(this);
         if ((limit != 0) && (size >= limit)) {
           engine = defaultEngine;
         }
         LOGGER.debug("getEngine: size {}, limit {}", size, limit);
       }
       LOGGER.debug("getEngine: returning '{}' for  cal '{}'", engine.getName(), 
           getDocumentReference());
     }
     return engine;
   }
 
   @Override
  public ICalendarEngineRole getEngineWithoutLimitCheck() {
    String engineHint = getContext().getWiki().getXWikiPreference("calendar_engine",
        "calendar.engine", "default", getContext());
    return Utils.getComponent(ICalendarEngineRole.class, engineHint);
  }

  @Override
   public int hashCode() {
     return new HashCodeBuilder().append(calConfigDocRef).append(startDate).append(
         isArchive).toHashCode();
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof Calendar) {
       Calendar other = (Calendar) obj;
       return new EqualsBuilder().append(this.calConfigDocRef, other.calConfigDocRef
           ).append(this.startDate, other.startDate).append(this.isArchive, other.isArchive
           ).isEquals();
     }
     return false;
   }
 
   private XWikiContext getContext() {
     Execution execution = Utils.getComponent(Execution.class);
     return (XWikiContext)execution.getContext().getProperty("xwikicontext");
   }
 
   private IEventManager getEventMgr() {
     if (eventMgr == null) {
       eventMgr = Utils.getComponent(IEventManager.class, "default");
     }
     return eventMgr;
   }
 
   protected void injectEventManager(IEventManager eventMgr) {
     this.eventMgr = eventMgr;
   }
 
   private ICalendarService getCalService() {
     if (calService == null) {
       calService = Utils.getComponent(ICalendarService.class);
     }
     return calService;
   }
   
   protected void injectCalService(ICalendarService calService) {
     this.calService = calService;
   }
 
   private IWebUtilsService getWebUtilsService() {
     return Utils.getComponent(IWebUtilsService.class);
   }
 
   @Override
   public String toString() {
     return "Calendar [calConfigDocRef=" + getWebUtilsService().getRefDefaultSerializer(
         ).serialize(calConfigDocRef) + ", startDate=" + startDate + ", isArchive=" 
         + isArchive + ", language=" + getLanguage() + ", allowedSpaces=" 
         + getAllowedSpaces() + ", engine=" + getEngine().getName() + "]";
   }
 
 }
