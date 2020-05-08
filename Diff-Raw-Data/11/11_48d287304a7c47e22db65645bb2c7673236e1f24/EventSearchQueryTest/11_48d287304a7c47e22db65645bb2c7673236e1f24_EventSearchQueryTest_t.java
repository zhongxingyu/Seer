 package com.celements.calendar.search;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.celements.search.lucene.IQueryService;
 import com.celements.search.lucene.query.LuceneQueryApi;
 import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
 
 public class EventSearchQueryTest {
 
   private static final String SPACE = "space";
   private static final String DATE = "Classes.CalendarEventClass.eventDate";
   private static final String TITLE = "Classes.CalendarEventClass.l_title";
   private static final String DESCR = "Classes.CalendarEventClass.l_description";
 
   private IQueryService queryServiceMock;
 
   @Before
   public void setUp_EventSearchResultTest() {
     queryServiceMock = createMock(IQueryService.class);
   }
 
   @Test
   public void testGetAsLuceneQuery_noFuzzy() throws ParseException {
     String dbName = "myDB";
     String spaceName = "mySpace";
     String searchTerm = "some search term";
     Date fromDate = new SimpleDateFormat("yyyyMMddHHmm").parse("200001010000");
     Date toDate = new SimpleDateFormat("yyyyMMddHHmm").parse("201001010000");
 
     expect(queryServiceMock.createQuery()).andReturn(new LuceneQueryApi(dbName)).once();
     expect(queryServiceMock.createRestriction(eq(SPACE), eq("\"" + spaceName + "\""),
        eq(true))).andReturn(new LuceneQueryRestrictionApi(SPACE, "\"" + spaceName + "\"",
            true)).once();
     expect(queryServiceMock.createRangeRestriction(eq(DATE), eq("200001010000"),
         eq("201001010000"), eq(true))).andReturn(new LuceneQueryRestrictionApi(DATE,
             "[200001010000 TO 201001010000]", false));
     expect(queryServiceMock.createRestriction(eq(TITLE), eq(searchTerm), eq(true))
         ).andReturn(new LuceneQueryRestrictionApi(TITLE, searchTerm, true)).once();
     expect(queryServiceMock.createRestriction(eq(DESCR), eq(searchTerm), eq(true))
         ).andReturn(new LuceneQueryRestrictionApi(DESCR, searchTerm, true)).once();
 
     replayAll();
     EventSearchQuery query = new EventSearchQuery(spaceName, fromDate, toDate, searchTerm);
     query.injectQueryService(queryServiceMock);
     LuceneQueryApi luceneQuery = query.getAsLuceneQuery();
     verifyAll();
 
     String compareQueryString = "space:(+\"mySpace\") AND " +
         "Classes.CalendarEventClass.eventDate:([200001010000 TO 201001010000]) AND " +
         "(Classes.CalendarEventClass.l_title:(+some* +search* +term*) OR " +
         "Classes.CalendarEventClass.l_description:(+some* +search* +term*)) AND " +
         "wiki:myDB";
     assertEquals(compareQueryString, luceneQuery.getQueryString());
   }
 
   @Test
   public void testGetAsLuceneQuery_fuzzy() throws ParseException {
     String dbName = "myDB";
     String spaceName = "mySpace";
     String searchTerm = "some search term";
     Date fromDate = new SimpleDateFormat("yyyyMMddHHmm").parse("200001010000");
     Date toDate = new SimpleDateFormat("yyyyMMddHHmm").parse("201001010000");
 
     expect(queryServiceMock.createQuery()).andReturn(new LuceneQueryApi(dbName)).once();
     expect(queryServiceMock.createRestriction(eq(SPACE), eq("\"" + spaceName + "\""),
        eq(true))).andReturn(new LuceneQueryRestrictionApi(SPACE, "\"" + spaceName + "\"",
            true)).once();
     expect(queryServiceMock.createRangeRestriction(eq(DATE), eq("200001010000"),
         eq("201001010000"), eq(true))).andReturn(new LuceneQueryRestrictionApi(DATE,
             "[200001010000 TO 201001010000]", false));
     expect(queryServiceMock.createRestriction(eq(TITLE), eq(searchTerm), eq(true))
         ).andReturn(new LuceneQueryRestrictionApi(TITLE, searchTerm, true)).once();
     expect(queryServiceMock.createRestriction(eq(DESCR), eq(searchTerm), eq(true))
         ).andReturn(new LuceneQueryRestrictionApi(DESCR, searchTerm, true)).once();
 
     replayAll();
     EventSearchQuery query = new EventSearchQuery(spaceName, fromDate, toDate, searchTerm);
     query.injectQueryService(queryServiceMock);
     query.setFuzzy();
     LuceneQueryApi luceneQuery = query.getAsLuceneQuery();
     verifyAll();
 
    String compareQueryString = "space:(+\"mySpace\") AND "
        + "Classes.CalendarEventClass.eventDate:([200001010000 TO 201001010000]) AND "
        + "(Classes.CalendarEventClass.l_title:((some* OR some~) AND (search* OR search~)"
        + " AND (term* OR term~)) OR Classes.CalendarEventClass.l_description:("
        + "(some* OR some~) AND (search* OR search~) AND (term* OR term~))"
        + ") AND wiki:myDB";
     assertEquals(compareQueryString, luceneQuery.getQueryString());
   }
 
   private void replayAll(Object ... mocks) {
     replay(queryServiceMock);
     replay(mocks);
   }
 
   private void verifyAll(Object ... mocks) {
     verify(queryServiceMock);
     verify(mocks);
   }
 
 }
