 package org.mule.module.google.calendar.automation.testcases;
 
import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
import org.mule.api.MuleEvent;
import org.mule.api.processor.MessageProcessor;
 import org.mule.module.google.calendar.model.Calendar;
 import org.mule.module.google.calendar.model.Event;
 import org.mule.module.google.calendar.model.EventDateTime;
 import org.mule.modules.google.api.client.batch.BatchResponse;
 
 public class BatchDeleteEventTestCases extends GoogleCalendarTestParent {
 
 
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("batchDeleteEvent");
 
 			// Insert calendar and get reference to retrieved calendar
 			Calendar calendar = insertCalendar((Calendar) testObjects.get("calendarRef"));
 			
 			// Replace old calendar instance with new instance
 			testObjects.put("calendarRef", calendar);
 			testObjects.put("calendarId", calendar.getId());			
 
 			// Get start and end time beans.
 			EventDateTime eventStartTime = (EventDateTime) context.getBean("eventStartTime");
 			EventDateTime eventEndTime = (EventDateTime) context.getBean("eventEndTime");			
 			int numEvents = Integer.parseInt(testObjects.get("numEvents").toString());
 			
 			// Instantiate the events that we want to batch insert
 			List<Event> events = new ArrayList<Event>();
 			for (int i = 0; i < numEvents; i++) {
 				Event event = getEvent("Test Event", eventStartTime, eventEndTime);
 				events.add(event);
 			}
 			
 			// Store the successfully persisted events in testObjects for later access
 			BatchResponse<Event> batchResponse = insertEvents(calendar, events);
 			List<Event> successful = batchResponse.getSuccessful();
 			testObjects.put("calendarEventsRef", successful);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, SanityTests.class})
 	@Test
 	public void testBatchDeleteEvent() {
 		try {			
 						
			Calendar calendar = (Calendar) testObjects.get("calendarRef");
 			List<Event> succcessful = (List<Event>) testObjects.get("calendarEventsRef");
			 
 			deleteEvents(calendar, succcessful);
 			
			MessageProcessor flow = lookupFlowConstruct("get-all-events");
			MuleEvent response = flow.process(getTestEvent(testObjects));
			
			List<Event> events = (List<Event>) response.getMessage().getPayload();
			assertTrue(events.isEmpty());
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
