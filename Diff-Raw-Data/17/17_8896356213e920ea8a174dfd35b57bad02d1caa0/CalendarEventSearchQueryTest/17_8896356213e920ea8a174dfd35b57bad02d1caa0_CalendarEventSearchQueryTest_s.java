 package com.celements.calendar.search;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.calendar.ICalendar;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 
 public class CalendarEventSearchQueryTest extends AbstractBridgedComponentTestCase {
   
   private static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");
   
   private ICalendar calMock;
   private String database;
   
   @Before
   public void setUp_CalendarEventSearchQueryTest() {
     calMock = createMockAndAddToDefault(ICalendar.class);
     database = "theDB";
     DocumentReference docRef = new DocumentReference(database, "someSpace", "someCal");
     expect(calMock.getDocumentReference()).andReturn(docRef).anyTimes();
   }
   
   @Test
   public void testNewCalendarEventSearchQuery() throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Arrays.asList("field1", "field2");
     
     replayDefault();
     IEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
     verifyDefault();
     
     assertEquals(database, query.getDatabase());
     assertEquals(sortFields, query.getSortFields());
   }
   
   @Test
   public void testNewCalendarEventSearchQuery_defaultSortFields() throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Collections.emptyList();
     
     replayDefault();
     IEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
     verifyDefault();
     
     assertEquals(database, query.getDatabase());
     assertDefaultSortFields(query.getSortFields(), isArchive);
   }
   
   @Test
   public void testNewCalendarEventSearchQuery_fromQuery() throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Arrays.asList("field1", "field2");
     
     replayDefault();
     IEventSearchQuery fromQuery = new DefaultEventSearchQuery(database, null, sortFields);
     CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, fromQuery);
     verifyDefault();
 
     assertEquals(database, query.getDatabase());
     assertEquals(sortFields, query.getSortFields());
   }
   
   @Test
   public void testNewCalendarEventSearchQuery_fromQuery_defaultSortFields(
       ) throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Collections.emptyList();
     
     replayDefault();
     IEventSearchQuery fromQuery = new DefaultEventSearchQuery(database, null, sortFields);
     CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, fromQuery);
     verifyDefault();
     
     assertEquals(database, query.getDatabase());
     assertDefaultSortFields(query.getSortFields(), isArchive);
   }
   
   @Test
   public void testGetAsLuceneQuery() throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Collections.emptyList();
     
     replayDefault();
     CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
     verifyDefault();
 
     assertEquals(database, query.getDatabase());
     String expQueryString = "(wiki:(+\"theDB\") "
         + "AND object:(+\"Classes.CalendarEventClass\") "
         + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
         + "AND Classes.CalendarEventClass.lang:(+\"de\") "
         + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
     assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
     assertDefaultSortFields(query.getSortFields(), isArchive);
   }
   
   @Test
   public void testGetAsLuceneQuery_isArchive() throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = true;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Collections.emptyList();
     
     replayDefault();
     CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
     verifyDefault();
 
     assertEquals(database, query.getDatabase());
     String expQueryString = "(wiki:(+\"theDB\") "
         + "AND object:(+\"Classes.CalendarEventClass\") "
         + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
         + "AND Classes.CalendarEventClass.lang:(+\"de\") "
         + "AND Classes.CalendarEventClass.eventDate:({000101010000 TO 201405090125}))";
     assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
     assertDefaultSortFields(query.getSortFields(), isArchive);
   }
   
   @Test
   public void testGetAsLuceneQuery_sortFields() throws Exception {
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace1", "myCalSpace2");
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Arrays.asList("field1", "field2");
     
     replayDefault();
     CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
     verifyDefault();
 
     assertEquals(database, query.getDatabase());
     String expQueryString = "(wiki:(+\"theDB\") "
         + "AND object:(+\"Classes.CalendarEventClass\") "
         + "AND (space:(+\"myCalSpace1\") OR space:(+\"myCalSpace2\")) "
         + "AND Classes.CalendarEventClass.lang:(+\"de\") "
         + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
     assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
     assertEquals(sortFields, query.getSortFields());
   }
   
   @Test
   public void testGetAsLuceneQuery_noSpaces() throws Exception {
     String database = "theDB";
     Date startDate = SDF.parse("201405090125");
     boolean isArchive = false;
     String lang = "de";
     List<String> spaces = Collections.emptyList();
     expectForCalMock(startDate, isArchive, lang, spaces);
     List<String> sortFields = Collections.emptyList();
     
     replayDefault();
     CalendarEventSearchQuery query = new CalendarEventSearchQuery(calMock, sortFields);
     verifyDefault();
 
     assertEquals(database, query.getDatabase());
     String expQueryString = "(wiki:(+\"theDB\") "
         + "AND object:(+\"Classes.CalendarEventClass\") "
         + "AND space:(+\".\") "
         + "AND Classes.CalendarEventClass.lang:(+\"de\") "
         + "AND Classes.CalendarEventClass.eventDate:([201405090125 TO 999912312359]))";
     assertEquals(expQueryString, query.getAsLuceneQuery().getQueryString());
     assertDefaultSortFields(query.getSortFields(), isArchive);
   }
   
   @Test
   public void testGetSortFields() {
     List<String> inSortFields = Arrays.asList("field1", "field2");
     assertEquals(inSortFields, CalendarEventSearchQuery.getDefaultSortFields(inSortFields, 
         false));
   }
   
   @Test
   public void testGetSortFields_default() {
     boolean inverted = false;
     List<String> sortFields = CalendarEventSearchQuery.getDefaultSortFields(null, 
         inverted);
     assertDefaultSortFields(sortFields, inverted);
   }
   
   @Test
   public void testGetSortFields_default_inverted() {
     boolean inverted = true;
     List<String> inSortFields = Collections.emptyList();
     List<String> sortFields = CalendarEventSearchQuery.getDefaultSortFields(inSortFields, 
         inverted);
     assertDefaultSortFields(sortFields, inverted);
   }
   
   private void expectForCalMock(Date startDate, boolean isArchive, String lang, 
       List<String> spaces) {
     expect(calMock.getStartDate()).andReturn(startDate).once();
     expect(calMock.isArchive()).andReturn(isArchive).times(2);
     expect(calMock.getLanguage()).andReturn(lang).once();
     expect(calMock.getAllowedSpaces()).andReturn(spaces).once();
   }
   
   private void assertDefaultSortFields(List<String> sortFields, boolean inverted) {
     String pref = inverted ? "-" : "";
     assertNotNull(sortFields);
     assertEquals(3, sortFields.size());
     assertEquals(pref + "Classes.CalendarEventClass.eventDate", sortFields.get(0));
     assertEquals(pref + "Classes.CalendarEventClass.eventDate_end", sortFields.get(1));
     assertEquals(pref + "Classes.CalendarEventClass.l_title", sortFields.get(2));
   }
   
 }
