 package com.celements.calendar.classes;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.classes.AbstractClassCollection;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 import com.xpn.xwiki.objects.classes.DateClass;
 
 @Component("celements.CalendarClasses")
 public class CalendarClasses extends AbstractClassCollection {
 
   public static final String CALENDAR_CONFIG_CLASS_SPACE = "Classes";
   public static final String CALENDAR_CONFIG_CLASS_DOC = "CalendarConfigClass";
   public static final String CALENDAR_CONFIG_CLASS = CALENDAR_CONFIG_CLASS_SPACE + "."
       + CALENDAR_CONFIG_CLASS_DOC;
   public static final String PROPERTY_IS_SUBSCRIBABLE = "is_subscribable";
   public static final String PROPERTY_EVENT_PER_PAGE = "event_per_page";
   public static final String PROPERTY_OVERVIEW_COLUMN_CONFIG = "overview_column_config";
   public static final String PROPERTY_EVENT_COLUMN_CONFIG = "event_column_config";
   public static final String PROPERTY_HAS_MORE_LINK = "hasMoreLink";
   public static final String PROPERTY_SUBSCRIBE_TO = "subscribe_to";
   public static final String PROPERTY_CALENDAR_SPACE = "calendarspace";
 
   public static final String CALENDAR_EVENT_CLASS_SPACE = "Classes";
   public static final String CALENDAR_EVENT_CLASS_DOC = "CalendarEventClass";
   public static final String CALENDAR_EVENT_CLASS = CALENDAR_EVENT_CLASS_SPACE + "."
       + CALENDAR_EVENT_CLASS_DOC;
   public static final String PROPERTY_LANG = "lang";
   public static final String PROPERTY_TITLE = "l_title";
   public static final String PROPERTY_TITLE_RTE = "l_title_rte";
   public static final String PROPERTY_DESCRIPTION = "l_description";
   public static final String PROPERTY_LOCATION = "location";
   public static final String PROPERTY_LOCATION_RTE = "location_rte";
   public static final String PROPERTY_EVENT_DATE = "eventDate";
   public static final String PROPERTY_EVENT_DATE_END = "eventDate_end";
   public static final String PROPERTY_EVENT_IS_SUBSCRIBABLE = "isSubscribable";
 
   public static final String SUBSCRIPTION_CLASS_SPACE = "Classes";
   public static final String SUBSCRIPTION_CLASS_DOC = "SubscriptionClass";
   public static final String SUBSCRIPTION_CLASS = SUBSCRIPTION_CLASS_SPACE + "."
       + SUBSCRIPTION_CLASS_DOC;
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(CalendarClasses.class);
 
   @Override
   protected Log getLogger() {
     return LOGGER;
   }
 
   @Override
   protected void initClasses() throws XWikiException {
     getCalendarClass();
     getCalendarEventClass();
     getSubscriptionClass();
   }
 
   public String getConfigName() {
     return "celCalendar";
   }
 
   private BaseClass getCalendarClass() throws XWikiException {
     DocumentReference classRef = getCalendarClassRef(getContext().getDatabase());
     XWikiDocument doc;
     boolean needsUpdate = false;
 
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (Exception exception) {
       LOGGER.error("Exception while getting doc for ClassRef'" + classRef
           + "'", exception);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     needsUpdate |= bclass.addTextField(PROPERTY_CALENDAR_SPACE, PROPERTY_CALENDAR_SPACE,
         30);
     String hql = "select doc.fullName from XWikiDocument as doc, BaseObject as obj,";
     hql += " IntegerProperty as int ";
     hql += "where obj.name=doc.fullName ";
     hql += "and not doc.fullName='$doc.getFullName()' ";
     hql += "and obj.className='" + CALENDAR_CONFIG_CLASS + "' ";
     hql += "and int.id.id=obj.id ";
     hql += "and int.id.name='" + PROPERTY_IS_SUBSCRIBABLE + "' ";
     hql += "and int.value='1' ";
     hql += "order by doc.fullName asc";
     needsUpdate |= bclass.addDBListField(PROPERTY_SUBSCRIBE_TO, PROPERTY_SUBSCRIBE_TO, 5,
         true, hql);
     needsUpdate |= bclass.addTextField(PROPERTY_OVERVIEW_COLUMN_CONFIG,
         PROPERTY_OVERVIEW_COLUMN_CONFIG, 30);
     needsUpdate |= bclass.addTextField(PROPERTY_EVENT_COLUMN_CONFIG,
         PROPERTY_EVENT_COLUMN_CONFIG, 30);
     needsUpdate |= bclass.addNumberField(PROPERTY_EVENT_PER_PAGE, PROPERTY_EVENT_PER_PAGE,
         5, "integer");
     needsUpdate |= bclass.addBooleanField(PROPERTY_HAS_MORE_LINK, PROPERTY_HAS_MORE_LINK,
         "yesno");
     needsUpdate |= bclass.addBooleanField(PROPERTY_IS_SUBSCRIBABLE,
         PROPERTY_IS_SUBSCRIBABLE, "yesno");
 
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCalendarClassRef(String wikiName) {
     return new DocumentReference(wikiName, CALENDAR_CONFIG_CLASS_SPACE,
         CALENDAR_CONFIG_CLASS_DOC);
   }
 
   private BaseClass getCalendarEventClass() throws XWikiException {
     DocumentReference classRef = getCalendarEventClassRef(getContext().getDatabase());
     XWikiDocument doc;
     boolean needsUpdate = false;
 
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (Exception exception) {
       LOGGER.error("Exception while getting doc for ClassRef'" + classRef
           + "'", exception);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     needsUpdate |= bclass.addTextField(PROPERTY_LANG, PROPERTY_LANG, 30);
     needsUpdate |= bclass.addTextField(PROPERTY_TITLE, PROPERTY_TITLE, 30);
     needsUpdate |= bclass.addTextAreaField(PROPERTY_TITLE_RTE, PROPERTY_TITLE_RTE, 80,
         15);
     needsUpdate |= bclass.addTextAreaField(PROPERTY_DESCRIPTION, PROPERTY_DESCRIPTION, 80,
         15);
     needsUpdate |= bclass.addTextField(PROPERTY_LOCATION, PROPERTY_LOCATION, 30);
     needsUpdate |= bclass.addTextAreaField(PROPERTY_LOCATION_RTE, PROPERTY_LOCATION_RTE,
         80, 15);
     needsUpdate |= addDateField(bclass, PROPERTY_EVENT_DATE, PROPERTY_EVENT_DATE,
         "dd.MM.yyyy HH:mm:ss", 20, 0, getRegexDate(false, true), 
         "cel_calendar_validation_event_date");
     needsUpdate |= addDateField(bclass, PROPERTY_EVENT_DATE_END, PROPERTY_EVENT_DATE_END,
         "dd.MM.yyyy HH:mm:ss", 20, 0, getRegexDate(true, true), 
         "cel_calendar_validation_event_end_date");
     needsUpdate |= bclass.addBooleanField(PROPERTY_EVENT_IS_SUBSCRIBABLE,
         PROPERTY_EVENT_IS_SUBSCRIBABLE, "yesno");
 
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
 
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCalendarEventClassRef(String wikiName) {
     return new DocumentReference(wikiName, CALENDAR_EVENT_CLASS_SPACE,
         CALENDAR_EVENT_CLASS_DOC);
   }
 
   private BaseClass getSubscriptionClass() throws XWikiException {
     DocumentReference classRef = getSubscriptionClassRef(getContext().getDatabase());
     XWikiDocument doc;
     boolean needsUpdate = false;
 
     try {
       doc = getContext().getWiki().getDocument(classRef, getContext());
     } catch (Exception exception) {
       LOGGER.error("Exception while getting doc for ClassRef'" + classRef
           + "'", exception);
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
 
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     needsUpdate |= bclass.addTextField("subscriber", "subscriber", 30);
     needsUpdate |= bclass.addBooleanField("doSubscribe", "doSubscribe", "yesno");
 
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getSubscriptionClassRef(String wikiName) {
     return new DocumentReference(wikiName, SUBSCRIPTION_CLASS_SPACE,
         SUBSCRIPTION_CLASS_DOC);
   }
   
   private String getRegexDate(boolean allowEmpty, boolean withTime) {
     String regex = "(0[1-9]|[12][0-9]|3[01])\\.(0[1-9]|1[012])\\.([0-9]{4})";
     if (withTime) {
       regex += " ([01][0-9]|2[0-4])(\\:[0-5][0-9]){1,2}";
     }
     return "/" + (allowEmpty ? "(^$)|" : "") + "^(" + regex + ")$" + "/";
   }
 
 }
