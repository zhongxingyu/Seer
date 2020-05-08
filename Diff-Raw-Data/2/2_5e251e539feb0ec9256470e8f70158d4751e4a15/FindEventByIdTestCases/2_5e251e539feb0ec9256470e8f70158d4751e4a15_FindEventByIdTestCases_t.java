 package org.mule.module.google.calendar.automation.testcases;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.google.calendar.model.Calendar;
 import org.mule.module.google.calendar.model.Event;
 
 public class FindEventByIdTestCases extends GoogleCalendarTestParent {
 
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("findEventById");
 			
 			// Insert calendar and get reference to retrieved calendar
 			Calendar calendar = insertCalendar((Calendar) testObjects.get("calendarRef"));
 			
 			// Replace old calendar instance with new instance
 			testObjects.put("calendarRef", calendar);
 			testObjects.put("calendarId", calendar.getId());
 			
 			// Insert the event
 			MessageProcessor flow = lookupFlowConstruct("insert-event");
 			MuleEvent event = flow.process(getTestEvent(testObjects));
 			
 			// Place the returned event and its ID into testObjects for later access
 			Event returnedEvent = (Event) event.getMessage().getPayload();
 			testObjects.put("event", returnedEvent);
 			testObjects.put("eventId", event.getId());
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, SanityTests.class})	
 	@Test
	public void testFindEventById() {
 		try {
 			Event originalEvent = (Event) testObjects.get("event");
 			
 			// Find the event based on previously set ID
 			MessageProcessor flow = lookupFlowConstruct("find-event-by-id");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			// Perform assertions			
 			Event returnedEvent = (Event) response.getMessage().getPayload();
 			assertTrue(returnedEvent.getId().equals(originalEvent.getId()));
 			assertTrue(returnedEvent.getStart().equals(originalEvent.getStart()));
 			assertTrue(returnedEvent.getEnd().equals(originalEvent.getEnd()));
 			assertTrue(returnedEvent.getSummary().equals(originalEvent.getSummary()));
 			assertTrue(returnedEvent.equals(originalEvent));			
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			String calendarId = testObjects.get("calendarId").toString();
 			deleteCalendar(calendarId);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	
 }
