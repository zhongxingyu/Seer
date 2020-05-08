 /**
  * Mule Google Calendars Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.google.calendar.automation.testcases;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.Timeout;
 import org.mule.api.MuleEvent;
 import org.mule.api.config.MuleProperties;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.api.store.ObjectStore;
 import org.mule.api.store.ObjectStoreException;
 import org.mule.module.google.calendar.model.AclRule;
 import org.mule.module.google.calendar.model.Calendar;
 import org.mule.module.google.calendar.model.CalendarList;
 import org.mule.module.google.calendar.model.Event;
 import org.mule.module.google.calendar.model.EventDateTime;
 import org.mule.module.google.calendar.oauth.GoogleCalendarConnectorOAuthState;
 import org.mule.modules.google.api.client.batch.BatchResponse;
 import org.mule.modules.google.api.datetime.DateTime;
 import org.mule.tck.junit4.FunctionalTestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class GoogleCalendarTestParent extends FunctionalTestCase {
 
 	// Set global timeout of tests to 10minutes
     @Rule
     public Timeout globalTimeout = new Timeout(600000);
     
 	protected static final String[] SPRING_CONFIG_FILES = new String[] { "AutomationSpringBeans.xml","HelperSpringBeans.xml" };
 	protected static ApplicationContext context;
 	protected Map<String, Object> testObjects;
 
 	@Override
 	protected String getConfigResources() {
 		return "automation-test-flows.xml";
 	}
 
 	protected MessageProcessor lookupFlowConstruct(String name) {
 		return (MessageProcessor) muleContext.getRegistry()
 				.lookupFlowConstruct(name);
 	}
 	
 	@Before
 	public void init() throws ObjectStoreException {
 		ObjectStore objectStore = muleContext.getRegistry().lookupObject(MuleProperties.DEFAULT_USER_OBJECT_STORE_NAME);
 		objectStore.store("accessTokenId", (GoogleCalendarConnectorOAuthState)context.getBean("connectorOAuthState"));
 	}
 
 	@BeforeClass
 	public static void beforeClass() {
 		context = new ClassPathXmlApplicationContext(SPRING_CONFIG_FILES);
 	}
 	
 	/*
 	 * Helper methods below
 	 */
 	
 	protected Calendar insertCalendar(Calendar calendar) throws Exception {
 		testObjects.put("calendarRef", calendar);
 		MessageProcessor flow = lookupFlowConstruct("create-calendar");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Calendar) response.getMessage().getPayload();
 	}
 	
 	protected BatchResponse<Calendar> insertCalendars(List<Calendar> calendars) throws Exception {
 		testObjects.put("calendarsRef", calendars);
 		MessageProcessor flow = lookupFlowConstruct("batch-insert-calendar");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (BatchResponse<Calendar>) response.getMessage().getPayload();
 	}
 		
 	protected void deleteCalendar(Calendar calendar) throws Exception {
 		deleteCalendar(calendar.getId());
 	}
 	
 	protected void deleteCalendar(String id) throws Exception {
 		testObjects.put("id", id);
 		MessageProcessor flow = lookupFlowConstruct("delete-calendar");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 	}
 	
 	protected void deleteCalendars(List<Calendar> calendars) throws Exception {
 		testObjects.put("calendarsRef", calendars);
 		MessageProcessor flow = lookupFlowConstruct("batch-delete-calendar");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 	}
 	
 	protected Event quickAddEvent(Calendar calendar, String eventSummary) throws Exception {
 		return quickAddEvent(calendar.getId(), eventSummary);
 	}
 	
 	protected Event quickAddEvent(String calendarId, String eventSummary) throws Exception {
 		testObjects.put("calendarId", calendarId);
 		testObjects.put("text", eventSummary);
 		MessageProcessor flow = lookupFlowConstruct("quick-add-event");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Event) response.getMessage().getPayload();
 	}
 	
 	protected Event insertEvent(Calendar calendar, Event event) throws Exception {
 		return insertEvent(calendar.getId(), event);
 	}
 	
 	protected Event insertEvent(String calendarId, Event event) throws Exception {
 		testObjects.put("calendarId", calendarId);
 		testObjects.put("calendarEventRef", event);
 		MessageProcessor flow = lookupFlowConstruct("insert-event");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (Event) response.getMessage().getPayload();
 	}
 	
 	protected BatchResponse<Event> insertEvents(Calendar calendar, List<Event> events) throws Exception {
 		return insertEvents(calendar.getId(), events);
 	}
 	
 	protected BatchResponse<Event> insertEvents(String calendarId, List<Event> events) throws Exception {
 		testObjects.put("calendarId", calendarId);
 		testObjects.put("calendarEventsRef", events);
 		MessageProcessor flow = lookupFlowConstruct("batch-insert-event");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 		return (BatchResponse<Event>) response.getMessage().getPayload();
 	}
 	
 	protected void deleteEvent(String calendarId, Event event) throws Exception {
 		deleteEvent(calendarId, event.getId());
 	}
 	
 	protected void deleteEvent(Calendar calendar, Event event) throws Exception {
 		deleteEvent(calendar.getId(), event.getId());
 	}
 	
 	protected void deleteEvent(String calendarId, String eventId) throws Exception {
 		testObjects.put("calendarId", calendarId);
 		testObjects.put("eventId", eventId);
 		MessageProcessor flow = lookupFlowConstruct("delete-event");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 	}
 	
 	protected void deleteEvents(Calendar calendar, List<Event> events) throws Exception {
 		deleteEvents(calendar.getId(), events);
 	}
 	
 	protected void deleteEvents(String calendarId, List<Event> events) throws Exception {
 		testObjects.put("calendarId", calendarId);
 		testObjects.put("calendarEventsRef", events);
 		MessageProcessor flow = lookupFlowConstruct("batch-delete-event");
 		MuleEvent response = flow.process(getTestEvent(testObjects));
 	}
 	
 	protected Calendar getCalendar(String summary) {
 		Calendar calendar = new Calendar();
 		calendar.setSummary(summary);
 		return calendar;
 	}
 	
 	protected Event getEvent(String title) {
 		Event event = new Event();
 		event.setSummary(title);
 		return event;
 	}
 
 	protected Event getEvent(String title, Date startTime, Date endTime) {
 		Event event = getEvent(title);
 		
 		EventDateTime start = new EventDateTime();
 		start.setDateTime(new DateTime(startTime));
 		
 		EventDateTime end = new EventDateTime();
 		end.setDateTime(new DateTime(endTime));
 		
 		event.setStart(start);
 		event.setEnd(end);
 		return event;
 	}
 	
 	protected Event getEvent(String title, String startTime, String endTime) {
 		Event event = getEvent(title);
 		
 		EventDateTime start = new EventDateTime();
 		start.setDate(startTime);
 		
 		EventDateTime end = new EventDateTime();
 		end.setDate(endTime);
 		
 		event.setStart(start);
 		event.setEnd(end);
 		return event;
 	}
 	
 	protected Event getEvent(String title, EventDateTime startTime, EventDateTime endTime) {
 		Event event = getEvent(title);
 		event.setStart(startTime);
 		event.setEnd(endTime);
 		return event;
 	}
 	
 	protected boolean isCalendarInList(List<CalendarList> list, Calendar toSearch) {
 		for (CalendarList calendar : list) {
 			if (calendar.getId().equals(toSearch.getId())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected boolean isAclRuleInList(List<AclRule> list, AclRule toSearch) {
 		for (AclRule aclRule : list) {
 			if (aclRule.getId().equals(toSearch.getId())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected boolean isEventInList(List<Event> events, Event event) {
 		for (Event e : events) {
			if (event.getId().equals(e)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 }
