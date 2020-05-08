 package org.mule.module.google.calendar.automation.testcases;
 
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.google.calendar.model.Calendar;
 import org.mule.module.google.calendar.model.Event;
 import org.mule.module.google.calendar.model.FreeBusy;
 
 import com.google.api.services.calendar.model.FreeBusyCalendar;
 import com.google.api.services.calendar.model.TimePeriod;
 
 public class GetFreeTimeTestCases extends GoogleCalendarTestParent {
 
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("getFreeTime");
 			
 			// Insert the calendar and the event
 			Calendar calendar = insertCalendar((Calendar) testObjects.get("calendarRef"));
 			Event event = insertEvent(calendar, (Event) testObjects.get("event"));
 			
 			// Replace the existing "event" bean with the updated one
 			testObjects.put("event", event);
 			testObjects.put("eventId", event.getId());
 			testObjects.put("calendar", calendar);
 			testObjects.put("calendarId", calendar.getId());
 			
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({SmokeTests.class, SanityTests.class})	
 	@Test
 	public void testGetFreeTime() {
 		try {
 			String calendarId = testObjects.get("calendarId").toString();
 			Event event = (Event) testObjects.get("event");
 			
 			String timeMin = event.getStart().getDate();
 			String timeMax = event.getEnd().getDate();
 			
 			testObjects.put("timeMin", timeMin);
 			testObjects.put("timeMax", timeMax);
 			
 			MessageProcessor flow = lookupFlowConstruct("get-free-time");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			FreeBusy freeBusy =(FreeBusy) response.getMessage().getPayload();
 			
 			// We should only be working with the calendar created specifically for this test
 			FreeBusyCalendar freeBusyCalendar = freeBusy.getCalendars().get(calendarId);
 			
 			List<TimePeriod> busyTimePeriods = freeBusyCalendar.getBusy();
 			
 			System.out.println(ToStringBuilder.reflectionToString(busyTimePeriods));
 			
 			fail("This test is broken");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
		String calendarId = testObjects.get("calendarId").toString();
		deleteCalendar(calendarId);
 	}
 	
 }
