 package com.celements.calendar.manager;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 
 import com.celements.calendar.Calendar;
 import com.celements.calendar.ICalendar;
 import com.celements.calendar.IEvent;
 import com.celements.calendar.engine.CalendarEngineLucene;
 import com.celements.calendar.engine.ICalendarEngineRole;
 import com.celements.calendar.search.DefaultEventSearchQuery;
 import com.celements.calendar.search.EventSearchResult;
 import com.celements.calendar.search.IEventSearchQuery;
 import com.celements.calendar.service.ICalendarService;
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.web.Utils;
 
 public class EventsManagerTest extends AbstractBridgedComponentTestCase {
 
   private XWikiContext context;
   private EventsManager eventsMgr;
   private ICalendarService calServiceMock;
   private ICalendarEngineRole engineMock;
   private ICalendar calMock;
   
   private DocumentReference calDocRef;
   private SpaceReference evSpaceRef;
 
   @Before
   public void setUp_EventsManagerTest() {
     context = getContext();
     eventsMgr = (EventsManager) Utils.getComponent(IEventManager.class);
     calServiceMock = createMockAndAddToDefault(ICalendarService.class);
     eventsMgr.injectCalService(calServiceMock);
     engineMock = createMockAndAddToDefault(CalendarEngineLucene.class);
     calMock = createMockAndAddToDefault(Calendar.class);
     calDocRef = new DocumentReference(context.getDatabase(), "mySpace", "myCalDoc");
     evSpaceRef = new SpaceReference("evSpace", new WikiReference(context.getDatabase())); 
     expect(calMock.getDocumentReference()).andReturn(calDocRef).anyTimes();
     expect(calMock.getEngine()).andReturn(engineMock).anyTimes();
   }
 
   @Test
   public void getEventsInternal() throws XWikiException {
     int offset = 2;
     int limit = 5;
     DocumentReference eventDocRef1 = new DocumentReference("myEvent1", evSpaceRef);
     IEvent event1 = createMockAndAddToDefault(IEvent.class);
     expect(event1.getDocumentReference()).andReturn(eventDocRef1).once();
     DocumentReference eventDocRef2 = new DocumentReference("myEvent2", evSpaceRef);
     IEvent event2 = createMockAndAddToDefault(IEvent.class);
     expect(event2.getDocumentReference()).andReturn(eventDocRef2).once();
 
     expect(engineMock.getEvents(same(calMock), eq(offset), eq(limit))).andReturn(
         Arrays.asList(event1, event2)).once();
     expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))
         ).andReturn(evSpaceRef).times(2);
 
     replayDefault();
     List<IEvent> events = eventsMgr.getEventsInternal(calMock, offset, limit);
     verifyDefault();
 
     assertNotNull(events);
     assertEquals(2, events.size());
     assertEquals(event1, events.get(0));
     assertEquals(event2, events.get(1));
   }
 
   @Test
   public void testIsHomeCalendar() throws XWikiException {
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
         "Event1");
     expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))).andReturn(
         new SpaceReference("inbox", new WikiReference(context.getDatabase()))).once();
     replayDefault();
     assertTrue("Expect true for Event1 in space 'inbox' if EventSpaceForCalender is"
         + " 'inbox' too.", eventsMgr.isHomeCalendar(calDocRef, eventDocRef));
     verifyDefault();
   }
 
   @Test
   public void testIsHomeCalendar_false() throws XWikiException {
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
         "Event1");
     expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))).andReturn(
         new SpaceReference("progonall", new WikiReference(context.getDatabase()))).once();
     replayDefault();
     assertFalse(eventsMgr.isHomeCalendar(calDocRef, eventDocRef));
     verifyDefault();
   }
 
   @Test
   public void testIsHomeCalendar_false_otherDB() throws XWikiException {
     DocumentReference calDocRef = new DocumentReference(context.getDatabase(), "mySpace",
         "myCalDoc");
     DocumentReference eventDocRef = new DocumentReference(context.getDatabase(), "inbox",
         "Event1");
     expect(calServiceMock.getEventSpaceRefForCalendar(eq(calDocRef))).andReturn(
         new SpaceReference("inbox", new WikiReference("db"))).once();
     replayDefault();
     assertFalse(eventsMgr.isHomeCalendar(calDocRef, eventDocRef));
     verifyDefault();
   }
 
   @Test
   public void testSearchEvents_dirtyLuceneWorkaraound() throws Exception {
     IEventSearchQuery query = new DefaultEventSearchQuery(getContext().getDatabase());
     EventSearchResult mockEventSearchResults = createMockAndAddToDefault(
         EventSearchResult.class);
     expect(((CalendarEngineLucene) engineMock).searchEvents(same(calMock), same(query))
         ).andReturn(mockEventSearchResults).once();
     //!! IMPORTANT getSize MUST be called imadiatelly
     expect(mockEventSearchResults.getSize()).andReturn(10).once();
     replayDefault();
     assertSame(mockEventSearchResults, eventsMgr.searchEvents(calMock, query));
     verifyDefault();
   }
 
 }
