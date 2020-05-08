 package com.celements.calendar.migrator;
 
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.easymock.EasyMock.same;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.calendar.classes.CalendarClasses;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.migrations.celSubSystem.ICelementsMigrator;
 import com.celements.migrator.ChangeCalendarDateFormatMigrator;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 import com.xpn.xwiki.objects.classes.DateClass;
 import com.xpn.xwiki.web.Utils;
 
 public class ChangeCalendarDateFormatMigratorTest 
 extends AbstractBridgedComponentTestCase {
   private XWikiContext context;
   private XWiki xwiki;
   
   private ChangeCalendarDateFormatMigrator migrator;
   
   @Before
   public void setUp_DocumentMetaDataMigratorTest() {
     context = getContext();
     xwiki = getWikiMock();
     context.setWiki(xwiki);
    migrator = (ChangeCalendarDateFormatMigrator) Utils.getComponent(ICelementsMigrator.class,
        "ChangeCalendarDateFormatMigrator");
   }
   
   @Test
   public void testGetName() {
     assertEquals("ChangeCalendarDateFormatMigrator", migrator.getName());
   }
   
   @Test
   public void testMigrate() throws Exception {
     XWikiDocument docMock = createMockAndAddToDefault(XWikiDocument.class);
     DocumentReference docRef = new DocumentReference(context.getDatabase(),
         "Classes", "CalendarEventClass");
     expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(docMock).once();
     BaseClass bClass = new BaseClass();
     bClass.addDateField("eventDate", "eventDate", "dd.MM.yyyy HH:mm", 0);
     bClass.addDateField("eventDate_end", "eventDate_end", "dd.MM.yyyy HH:mm", 0);
     expect(docMock.getXClass()).andReturn(bClass).once();
     xwiki.saveDocument(docMock, context);
     expectLastCall();
     replayDefault();
     migrator.migrate(null, context);
     verifyDefault();
     assertTrue(((DateClass) bClass.get(CalendarClasses.PROPERTY_EVENT_DATE)
         ).getValidationRegExp().length() > 0);
     assertTrue(((DateClass) bClass.get(CalendarClasses.PROPERTY_EVENT_DATE_END)
         ).getValidationRegExp().length() > 0);
    assertEquals("cel_calendar_validation_eventDate", ((DateClass) bClass.get(
         CalendarClasses.PROPERTY_EVENT_DATE)).getValidationMessage());
    assertEquals("cel_calendar_validation_eventDate_end", ((DateClass) bClass.get(
         CalendarClasses.PROPERTY_EVENT_DATE_END)).getValidationMessage());
     assertEquals("dd.MM.yyyy HH:mm", ((DateClass) bClass.get(
         CalendarClasses.PROPERTY_EVENT_DATE)).getDateFormat());
     assertEquals("dd.MM.yyyy HH:mm", ((DateClass) bClass.get(
         CalendarClasses.PROPERTY_EVENT_DATE_END)).getDateFormat());
   }
   
 }
