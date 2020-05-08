 package com.celements.calendar.engine;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.calendar.Calendar;
 import com.celements.calendar.Event;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.IEvent;
 import com.celements.calendar.search.EventSearchResult;
 import com.celements.calendar.search.IEventSearch;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.celements.search.lucene.IQueryService;
 import com.celements.search.lucene.query.LuceneQueryApi;
 import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.web.Utils;
 
 public class CalendarEngineLuceneTest extends AbstractBridgedComponentTestCase {
 
   private XWikiContext context;
   private CalendarEngineLucene engine;
   private IQueryService queryServiceMock;
   private IEventSearch eventSearchMock;
   private LuceneQueryApi queryMock;
   private EventSearchResult eventSearchResultMock;
 
   @Before
   public void setUp_EventsManagerTest() {
     context = getContext();
     engine = (CalendarEngineLucene) Utils.getComponent(ICalendarEngineRole.class, "lucene");
     queryServiceMock = createMock(IQueryService.class);
     engine.injectQueryService(queryServiceMock);
     eventSearchMock = createMock(IEventSearch.class);
     engine.injectEventSearch(eventSearchMock);
     queryMock = createMock(LuceneQueryApi.class);
     eventSearchResultMock = createMock(EventSearchResult.class);
   }
 
   @Test
   public void testGetEvents() throws XWikiException {
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace");
     Date startDate = new Date();
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     ICalendar cal = new Calendar(calDocRef, false);
     cal.setStartDate(startDate);
     LuceneQueryRestrictionApi spaceRestriction = new LuceneQueryRestrictionApi("space",
        "\"" + spaces.get(0) + "\"");
     LuceneQueryRestrictionApi langRestriction = new LuceneQueryRestrictionApi("lang",
         "\"de\"");
     IEvent event = new Event(new DocumentReference(context.getDatabase(),
         "mySpace", "myEvent"));
     IEvent event2 = new Event(new DocumentReference(context.getDatabase(),
         "mySpace", "myEvent2"));
 
    expect(queryServiceMock.createRestriction(eq("space"), eq("\"" + spaces.get(0)
        + "\""))).andReturn(spaceRestriction).once();
     expect(queryServiceMock.createRestriction("Classes.CalendarEventClass.lang",
         "\"" + lang + "\"")).andReturn(langRestriction).once();
     expect(queryServiceMock.createQuery()).andReturn(queryMock).once();
     expect(queryMock.addOrRestrictionList(Arrays.asList(spaceRestriction))).andReturn(
         queryMock).once();
     expect(queryMock.addRestriction(eq(langRestriction))).andReturn(queryMock).once();
     expect(eventSearchMock.getSearchResultFromDate(same(queryMock), eq(startDate))
         ).andReturn(eventSearchResultMock).once();
     expect(eventSearchResultMock.getEventList(2, 5)).andReturn(Arrays.asList(event,
         event2)).once();
 
     replayAll();
     List<IEvent> events = engine.getEvents(startDate, false, lang, spaces, 2, 5);
     verifyAll();
 
     assertNotNull(events);
     assertEquals(2, events.size());
     assertEquals(event, events.get(0));
     assertEquals(event2, events.get(1));
   }
 
   @Test
   public void testGetEvents_isArchive() throws XWikiException {
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace");
     Date startDate = new Date();
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     ICalendar cal = new Calendar(calDocRef, true);
     cal.setStartDate(startDate);
     LuceneQueryRestrictionApi spaceRestriction = new LuceneQueryRestrictionApi("space",
        "\"" + spaces.get(0) + "\"");
     LuceneQueryRestrictionApi langRestriction = new LuceneQueryRestrictionApi("lang",
         "\"de\"");
     IEvent event = new Event(new DocumentReference(context.getDatabase(),
         "mySpace", "myEvent"));
     IEvent event2 = new Event(new DocumentReference(context.getDatabase(),
         "mySpace", "myEvent2"));
 
    expect(queryServiceMock.createRestriction(eq("space"), eq("\"" + spaces.get(0)
        + "\""))).andReturn(
         spaceRestriction).once();
     expect(queryServiceMock.createRestriction("Classes.CalendarEventClass.lang",
         "\"" + lang + "\"")).andReturn(langRestriction).once();
     expect(queryServiceMock.createQuery()).andReturn(queryMock).once();
     expect(queryMock.addOrRestrictionList(Arrays.asList(spaceRestriction))).andReturn(
         queryMock).once();
     expect(queryMock.addRestriction(eq(langRestriction))).andReturn(queryMock).once();
     expect(eventSearchMock.getSearchResultUptoDate(same(queryMock), eq(startDate))
         ).andReturn(eventSearchResultMock).once();
     expect(eventSearchResultMock.getEventList(2, 5)).andReturn(Arrays.asList(event,
         event2)).once();
 
     replayAll();
     List<IEvent> events = engine.getEvents(startDate, true, lang, spaces, 2, 5);
     verifyAll();
 
     assertNotNull(events);
     assertEquals(2, events.size());
     assertEquals(event, events.get(0));
     assertEquals(event2, events.get(1));
   }
 
   @Test
   public void testCountEvents() throws XWikiException {
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace");
     Date startDate = new Date();
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     ICalendar cal = new Calendar(calDocRef, false);
     cal.setStartDate(startDate);
 
     ICalendarEngineRole hqlEngineMock = createMock(ICalendarEngineRole.class);
     engine.injectHQLEngine(hqlEngineMock);
 
     expect(hqlEngineMock.countEvents(eq(startDate), eq(false), eq(lang), eq(spaces))
         ).andReturn(2L).once();
 
     replayAll(hqlEngineMock);
     long countEvent = engine.countEvents(startDate, false, lang, spaces);
     verifyAll(hqlEngineMock);
 
     assertEquals(2L, countEvent);
   }
 
   @Test
   public void testGetFirstEventDate() {
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace");
     Date startDate = new Date(0);
     boolean isArchive = false;
     IEvent event = new Event(null);
 
     ICalendarEngineRole hqlEngineMock = createMock(ICalendarEngineRole.class);
     engine.injectHQLEngine(hqlEngineMock);
 
     expect(hqlEngineMock.getFirstEvent(eq(startDate), eq(isArchive), eq(lang),
         eq(spaces))).andReturn(event).once();
 
     replayAll(hqlEngineMock);
     IEvent firstEvent = engine.getFirstEvent(startDate, isArchive, lang, spaces);
     verifyAll(hqlEngineMock);
 
     assertTrue(firstEvent == event);
   }
 
   @Test
   public void testGetLastEventDate() {
     String lang = "de";
     List<String> spaces = Arrays.asList("myCalSpace");
     Date startDate = new Date(0);
     boolean isArchive = false;
     IEvent event = new Event(null);
 
     ICalendarEngineRole hqlEngineMock = createMock(ICalendarEngineRole.class);
     engine.injectHQLEngine(hqlEngineMock);
 
     expect(hqlEngineMock.getLastEvent(eq(startDate), eq(isArchive), eq(lang),
         eq(spaces))).andReturn(event).once();
 
     replayAll(hqlEngineMock);
     IEvent lastEvent = engine.getLastEvent(startDate, isArchive, lang, spaces);
     verifyAll(hqlEngineMock);
 
     assertTrue(lastEvent == event);
   }
 
   private void replayAll(Object ... mocks) {
     replay(queryServiceMock, eventSearchMock, queryMock,	eventSearchResultMock);
     replay(mocks);
   }
 
   private void verifyAll(Object ... mocks) {
     verify(queryServiceMock, eventSearchMock, queryMock,	eventSearchResultMock);
     verify(mocks);
   }
 
 }
