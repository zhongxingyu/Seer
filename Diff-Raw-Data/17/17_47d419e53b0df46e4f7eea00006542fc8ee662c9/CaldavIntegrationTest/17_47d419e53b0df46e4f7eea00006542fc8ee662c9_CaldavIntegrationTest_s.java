 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.jasig.schedassist.impl.caldav.integration;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.ComponentList;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.PropertyList;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.parameter.PartStat;
 import net.fortuna.ical4j.model.property.Attendee;
 import net.fortuna.ical4j.model.property.DtStart;
 import net.fortuna.ical4j.model.property.RDate;
 import net.fortuna.ical4j.model.property.Summary;
 import net.fortuna.ical4j.model.property.Transp;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jasig.schedassist.ICalendarDataDao;
 import org.jasig.schedassist.SchedulingException;
 import org.jasig.schedassist.impl.caldav.CaldavCalendarDataDaoImpl;
 import org.jasig.schedassist.impl.caldav.CalendarWithURI;
 import org.jasig.schedassist.model.AppointmentRole;
 import org.jasig.schedassist.model.AvailabilityReflection;
 import org.jasig.schedassist.model.AvailableBlock;
 import org.jasig.schedassist.model.AvailableBlockBuilder;
 import org.jasig.schedassist.model.AvailableSchedule;
 import org.jasig.schedassist.model.CommonDateOperations;
 import org.jasig.schedassist.model.IEventUtils;
 import org.jasig.schedassist.model.InputFormatException;
 import org.jasig.schedassist.model.Preferences;
 import org.jasig.schedassist.model.SchedulingAssistantAppointment;
 import org.jasig.schedassist.model.VisitorLimit;
 import org.jasig.schedassist.model.mock.MockCalendarAccount;
 import org.jasig.schedassist.model.mock.MockDelegateCalendarAccount;
 import org.jasig.schedassist.model.mock.MockScheduleOwner;
 import org.jasig.schedassist.model.mock.MockScheduleVisitor;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Integration test for {@link CaldavCalendarDataDaoImpl}.
  * Depends on Spring applicationContext configuration "integration-test.xml".
  * 
  * Note this test is specifically excluded from the maven surefire configuration
  * as it is dependent on a CalDAV Server and test accounts being provided.
  * 
  * @author Nicholas Blair, npblair@wisc.edu
  * @version $Id: CaldavIntegrationTest.java 50 2011-05-05 21:07:25Z nblair $
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"classpath:integration-test.xml"})
 public class CaldavIntegrationTest {
 
 	@Autowired
 	private CaldavCalendarDataDaoImpl calendarDataDao;
 	@Autowired
 	private IEventUtils eventUtils;
 	@Autowired
 	@Qualifier("owner1")
 	private MockCalendarAccount ownerCalendarAccount1;
 
 	@Autowired
 	@Qualifier("visitor1")
 	private MockCalendarAccount visitorCalendarAccount1;
 
 	@Autowired
 	@Qualifier("visitor2")
 	private MockCalendarAccount visitorCalendarAccount2;
 	
 	@Autowired
 	@Qualifier("resource1")
 	private MockDelegateCalendarAccount resourceCalendarAccount1;
 	
 	@Value("${caldav.reflectionEnabled:false}")
 	private String reflectionEnabled;
 
 	private Log log = LogFactory.getLog(this.getClass());
 
 	/**
 	 * Simple integration test to see if {@link ICalendarDataDao#getCalendar(org.jasig.schedassist.model.ICalendarAccount, Date, Date)}
 	 * returns data.
 	 * 
 	 * Before you run this test for the first time, import the event below into ownerCalendarAccount1's personal calendar:
 	 * <pre>
 	 * src/test/resources/vevent-examples/example-non-scheduling-assistant-conflict.ics
 	 * </pre>
 	 * 
 	 * This test will fail until the aforementioned event is imported into the ownerCalendarAccount1 personal calendar.
 	 * 
 	 * @throws InputFormatException 
 	 */
 	@Test
 	public void testGetCalendar() throws InputFormatException {
 		Date start = CommonDateOperations.parseDateTimePhrase("20110504-1300");
 		Date end = DateUtils.addHours(start, 1);
 		log.info("getCalendar test, " + start + " to " + end + " for account " + ownerCalendarAccount1);
 		Calendar calendar = calendarDataDao.getCalendar(ownerCalendarAccount1, start, end);
 		Assert.assertNotNull(calendar);
 		ComponentList events = calendar.getComponents(VEvent.VEVENT);
 		Assert.assertEquals(1, events.size());
 		VEvent event = (VEvent) events.get(0);
 		Summary summary = event.getSummary();
 		Assert.assertNotNull(summary);
 		Assert.assertEquals("dentist appointment", summary.getValue());
 	}
 
 	/**
 	 * Basic workflow integration test:
 	 * <ol>
 	 * <li>Create an individual appointment using mock owner and visitor ("owner1" and "visitor1")</li>
 	 * <li>Verify event retrieved via {@link ICalendarDataDao#getExistingAppointment(org.jasig.schedassist.model.IScheduleOwner, AvailableBlock)}</li>
 	 * <li>Verify event contains expected properties and parameters</li>
 	 * <li>Cancel appointment, verify removed</li>
 	 * </ol>
 	 */
 	@Test
 	public void testCreateAndCancelIndividualAppointment() {
 		// starts now
 		Date start = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
 		Date end = DateUtils.addHours(start, 1);
 		final DateTime ical4jstart = new DateTime(start);
 		final DateTime ical4jend = new DateTime(end);
 
 		AvailableBlock block = AvailableBlockBuilder.createBlock(start, end, 1);
 		MockScheduleOwner owner1 = new MockScheduleOwner(ownerCalendarAccount1, 1);
 		owner1.setPreference(Preferences.MEETING_PREFIX, "test appointment");
 		owner1.setPreference(Preferences.LOCATION, "meeting room 1a");
 
 		MockScheduleVisitor visitor1 = new MockScheduleVisitor(visitorCalendarAccount1);
 		VEvent event = this.calendarDataDao.createAppointment(visitor1, owner1, block, "testCreateAndCancelIndividualAppointment");
 		Assert.assertNotNull(event);
 
 		VEvent lookupResult = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNotNull(lookupResult);
 		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
 		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
 		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 		Assert.assertEquals(1, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
 
		Assert.assertEquals(1, lookupResult.getProperties(Attendee.ATTENDEE).size());
 
 		Property visitorAttendee = this.eventUtils.getAttendeeForUserFromEvent(event, visitorCalendarAccount1);
 		Assert.assertNotNull(visitorAttendee);
 		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
 
 		Property ownerAttendee = this.eventUtils.getAttendeeForUserFromEvent(event, ownerCalendarAccount1);
 		Assert.assertNotNull(ownerAttendee);
 		Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
 
 		this.calendarDataDao.cancelAppointment(visitor1, owner1, event);
 		VEvent lookupResultAfterCancel = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNull(lookupResultAfterCancel);
 	}
 	
 	/**
 	 * Basic workflow integration test:
 	 * <ol>
 	 * <li>Create an individual appointment using mock resource and visitor ("resource1" and "visitor1")</li>
 	 * <li>Verify event retrieved via {@link ICalendarDataDao#getExistingAppointment(org.jasig.schedassist.model.IScheduleOwner, AvailableBlock)}</li>
 	 * <li>Verify event contains expected properties and parameters</li>
 	 * <li>Cancel appointment, verify removed</li>
 	 * </ol>
 	 */
 	@Test
 	public void testCreateAndCancelIndividualAppointmentDelegateOwner() {
 		// starts now
 		Date start = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
 		Date end = DateUtils.addHours(start, 1);
 		final DateTime ical4jstart = new DateTime(start);
 		final DateTime ical4jend = new DateTime(end);
 
 		AvailableBlock block = AvailableBlockBuilder.createBlock(start, end, 1);
 		MockScheduleOwner owner1 = new MockScheduleOwner(resourceCalendarAccount1, 1);
 		owner1.setPreference(Preferences.MEETING_PREFIX, "test resource appointment");
 		owner1.setPreference(Preferences.LOCATION, "meeting room 1a");
 
 		MockScheduleVisitor visitor1 = new MockScheduleVisitor(visitorCalendarAccount1);
 		VEvent event = this.calendarDataDao.createAppointment(visitor1, owner1, block, "testCreateAndCancelIndividualAppointmentDelegateOwner");
 		Assert.assertNotNull(event);
 
 		VEvent lookupResult = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNotNull(lookupResult);
 		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
 		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
 		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 		Assert.assertEquals(1, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
 
		Assert.assertEquals(1, lookupResult.getProperties(Attendee.ATTENDEE).size());
 
 		Property visitorAttendee = this.eventUtils.getAttendeeForUserFromEvent(event, visitorCalendarAccount1);
 		Assert.assertNotNull(visitorAttendee);
 		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
 
 		Property ownerAttendee = this.eventUtils.getAttendeeForUserFromEvent(event, resourceCalendarAccount1);
 		Assert.assertNotNull(ownerAttendee);
 		Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
 
 		this.calendarDataDao.cancelAppointment(visitor1, owner1, event);
 		VEvent lookupResultAfterCancel = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNull(lookupResultAfterCancel);
 	}
 
 	/**
 	 * Integration test to create and manipulate a group appointment.
 	 * <ol>
 	 * <li>Visitor1 creates group appointment with Owner1</li>
 	 * <li>Visitor2 joins same appointment</li>
 	 * <li>Visitor1 leaves same appointment</li>
 	 * <li>Visitor2 cancels appointment</li>
 	 * </ol>
 	 * 
 	 * @throws SchedulingException 
 	 */
 	@Test
 	public void testGroupAppointmentWorkflow() {
 		// start an hour from now to avoid conflicts with individual apppointment
 		Date startDate = DateUtils.truncate(DateUtils.addHours(new Date(), 1), java.util.Calendar.MINUTE);
 		Date endDate = DateUtils.addHours(startDate, 1);
 		final DateTime ical4jstart = new DateTime(startDate);
 		final DateTime ical4jend = new DateTime(endDate);
 		AvailableBlock block = AvailableBlockBuilder.createBlock(startDate, endDate, 2);
 
 		MockScheduleOwner owner1 = new MockScheduleOwner(ownerCalendarAccount1, 1);
 		owner1.setPreference(Preferences.MEETING_PREFIX, "group appointment");
 		owner1.setPreference(Preferences.LOCATION, "meeting room 1b");
 		MockScheduleVisitor visitor1 = new MockScheduleVisitor(visitorCalendarAccount1);
 
 		VEvent original = this.calendarDataDao.createAppointment(visitor1, owner1, block, "testGroupAppointmentWorkflow");
 		Assert.assertNotNull(original);
 		VEvent lookup1= this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNotNull(lookup1);
 		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookup1.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 		Assert.assertEquals(2, Integer.parseInt(lookup1.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(1, lookup1.getProperties(Attendee.ATTENDEE).size());
 		Property visitorAttendee = this.eventUtils.getAttendeeForUserFromEvent(lookup1, visitor1.getCalendarAccount());
 		Assert.assertNotNull(visitorAttendee);
 		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
 		Property ownerAttendee = this.eventUtils.getAttendeeForUserFromEvent(lookup1, owner1.getCalendarAccount());
 		Assert.assertNotNull(ownerAttendee);
 		Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
 
 		Assert.assertEquals(ical4jstart, lookup1.getStartDate().getDate());
 		Assert.assertEquals(ical4jend, lookup1.getEndDate().getDate());
 		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookup1.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 		Assert.assertEquals(2, Integer.parseInt(lookup1.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(1, lookup1.getProperties(Attendee.ATTENDEE).size());
 
 
 		MockScheduleVisitor visitor2 = new MockScheduleVisitor(visitorCalendarAccount2);
 
 		// make 2nd visitor join
 		try {
 			this.calendarDataDao.joinAppointment(visitor2, owner1, lookup1);
 		} catch (SchedulingException e) {
 			Assert.fail("caught SchedulingException when visitor2 attempts join: " + e);
 		}
 		VEvent lookup2 = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNotNull(lookup2);
 		Assert.assertEquals(ical4jstart, lookup2.getStartDate().getDate());
 		Assert.assertEquals(ical4jend, lookup2.getEndDate().getDate());
 		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookup2.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 		Assert.assertEquals(2, Integer.parseInt(lookup2.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(2, lookup2.getProperties(Attendee.ATTENDEE).size());
 		PropertyList attendeeList = lookup2.getProperties(Attendee.ATTENDEE);
 		for(Object o : attendeeList) {
 			Attendee attendee = (Attendee) o;
 			Parameter participationStatus = attendee.getParameter(PartStat.PARTSTAT);
 			Assert.assertEquals(PartStat.ACCEPTED, participationStatus);
 			if(AppointmentRole.OWNER.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
 				Assert.assertEquals("mailto:" + owner1.getCalendarAccount().getEmailAddress(), attendee.getValue());
 			} else if (AppointmentRole.VISITOR.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
 				String value = attendee.getValue();
 				if(value.equals("mailto:" + visitor1.getCalendarAccount().getEmailAddress()) || value.equals("mailto:"+visitor2.getCalendarAccount().getEmailAddress()) ) {
 					// success
 				} else {
 					Assert.fail("unexpected visitor attendee value: " + value);
 				}
 			}
 		}
 
 		// now make visitor1 leave the appointment
 		try {
 			this.calendarDataDao.leaveAppointment(visitor1, owner1, lookup2);
 		} catch (SchedulingException e) {
 			Assert.fail("caught SchedulingException when visitor1 attempts leave: " + e);
 		}
 		VEvent lookup3 = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNotNull(lookup3);
 		Assert.assertEquals(ical4jstart, lookup3.getStartDate().getDate());
 		Assert.assertEquals(ical4jend, lookup3.getEndDate().getDate());
 		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookup3.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
 		Assert.assertEquals(2, Integer.parseInt(lookup3.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(1, lookup3.getProperties(Attendee.ATTENDEE).size());
 		attendeeList = lookup3.getProperties(Attendee.ATTENDEE);
 		for(Object o : attendeeList) {
 			Attendee attendee = (Attendee) o;
 			Parameter participationStatus = attendee.getParameter(PartStat.PARTSTAT);
 			Assert.assertEquals(PartStat.ACCEPTED, participationStatus);
 			if(AppointmentRole.OWNER.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
 				Assert.assertEquals("mailto:" + owner1.getCalendarAccount().getEmailAddress(), attendee.getValue());
 			} else if (AppointmentRole.VISITOR.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
 				String value = attendee.getValue();
 				if(value.equals("mailto:" + visitor2.getCalendarAccount().getEmailAddress())) {
 					// success
 				} else {
 					Assert.fail("unexpected visitor attendee value: " + value);
 				}
 			}
 		}
 
 		this.calendarDataDao.cancelAppointment(visitor2, owner1, lookup3);
 		VEvent lookupResultAfterCancel = this.calendarDataDao.getExistingAppointment(owner1, block);
 		Assert.assertNull(lookupResultAfterCancel);
 	}
 
 	/**
 	 * Reflect an {@link AvailableSchedule} into the owner's account and verify expected behavior.
 	 * 
 	 * @throws InputFormatException
 	 */
 	@Test
 	public void testReflectAvailabilitySchedule() throws InputFormatException {
 		System.setProperty("org.jasig.schedassist.impl.caldav.reflectionEnabled", reflectionEnabled);
 		if(Boolean.parseBoolean(reflectionEnabled)) {
 			MockScheduleOwner owner1 = new MockScheduleOwner(ownerCalendarAccount1, 1);
 
 			Date start = CommonDateOperations.parseDatePhrase("20110919");
 			Date end = DateUtils.addDays(start, 12);
 
 			Set<AvailableBlock> availableBlocks = AvailableBlockBuilder.createBlocks("9:00 AM", "3:00 PM", "MWF", start, end);
 			AvailableSchedule schedule = new AvailableSchedule(availableBlocks);
 			this.calendarDataDao.reflectAvailableSchedule(owner1, schedule);
 
 			List<CalendarWithURI> results = this.calendarDataDao.peekAtAvailableScheduleReflections(owner1, start, end);
 			Assert.assertEquals(1, results.size());
 			CalendarWithURI calendar = results.get(0);
 			VEvent event = (VEvent) calendar.getCalendar().getComponent(VEvent.VEVENT);
 			Assert.assertTrue(event.getProperties().contains(AvailabilityReflection.TRUE));
 			Assert.assertEquals("Available 9:00 AM - 3:00 PM", event.getSummary().getValue());
 			Assert.assertTrue(event.getProperties().contains(Transp.TRANSPARENT));
 			DtStart dtstart = event.getStartDate();
 			Assert.assertEquals(net.fortuna.ical4j.model.parameter.Value.DATE, dtstart.getParameter(net.fortuna.ical4j.model.parameter.Value.VALUE));
 			Assert.assertEquals("20110919", dtstart.getValue());
 			
 			PropertyList rdates = event.getProperties(RDate.RDATE);
 			Assert.assertEquals(5, rdates.size());
 			Set<String> rdateValues = new HashSet<String>();
 			for(Object o: rdates) {
 				Property rdate = (Property) o;
 				Assert.assertEquals(net.fortuna.ical4j.model.parameter.Value.DATE, rdate.getParameter(net.fortuna.ical4j.model.parameter.Value.VALUE));
 				rdateValues.add(rdate.getValue());
 			}
 			
 			Assert.assertTrue(rdateValues.contains("20110921"));
 			Assert.assertTrue(rdateValues.contains("20110923"));
 			Assert.assertTrue(rdateValues.contains("20110926"));
 			Assert.assertTrue(rdateValues.contains("20110928"));
 			Assert.assertTrue(rdateValues.contains("20110930"));
 			
 			this.calendarDataDao.purgeAvailableScheduleReflections(owner1, start, end);
 			results = this.calendarDataDao.peekAtAvailableScheduleReflections(owner1, start, end);
 			Assert.assertEquals(0, results.size());
 			
 		} else {
 			log.debug("testReflectAvailabilitySchedule disabled");
 		}
 	}
 
 }
