 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.nwu.bioinformatics.commons.DateUtils;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Collection;
 
 /**
  * @author Rhett Sutphin
  */
 public class ScheduleActivityCommandTest extends StudyCalendarTestCase {
     private static final String NEW_REASON = "New Reason";
     private static final Date NEW_DATE = DateUtils.createDate(2003, Calendar.MARCH, 14);
 
     private ScheduleActivityCommand command;
 
     private ScheduledCalendarDao scheduledCalendarDao;
     private ScheduledActivity event;
 
     protected void setUp() throws Exception {
         super.setUp();
         scheduledCalendarDao = registerMockFor(ScheduledCalendarDao.class);
         command = new ScheduleActivityCommand(scheduledCalendarDao);
 
         event = Fixtures.createScheduledActivity("ABC", 2003, Calendar.MARCH, 13);
         event.setScheduledStudySegment(new ScheduledStudySegment());
         event.getScheduledStudySegment().setScheduledCalendar(new ScheduledCalendar());
 
         command.setEvent(event);
         command.setNewReason(NEW_REASON);
         command.setNewDate(NEW_DATE);
     }
 
     public void testCreateCanceledState() throws Exception {
         command.setNewMode(ScheduledActivityMode.CANCELED);
         replayMocks();
 
         ScheduledActivityState created = command.createState();
         assertTrue(created instanceof Canceled);
         assertEquals(NEW_REASON, created.getReason());
     }
 
     public void testCreateScheduledState() throws Exception {
         command.setNewMode(ScheduledActivityMode.SCHEDULED);
         replayMocks();
 
         ScheduledActivityState created = command.createState();
         assertTrue(created instanceof Scheduled);
         assertEquals(NEW_REASON, created.getReason());
         assertEquals(NEW_DATE, (created).getDate());
     }
     
     public void testCreateOccurredState() throws Exception {
         command.setNewMode(ScheduledActivityMode.OCCURRED);
         replayMocks();
 
         ScheduledActivityState created = command.createState();
         assertTrue(created instanceof Occurred);
         assertEquals(NEW_REASON, created.getReason());
         assertEquals(NEW_DATE, (created).getDate());
     }
 
     public void testChangeState() throws Exception {
         assertEquals("Unexpeced number of initial states", 1, event.getAllStates().size());
         assertNull(event.getNotes());
 
         command.setNewMode(ScheduledActivityMode.OCCURRED);
         command.setNewNotes("Change-o");
         scheduledCalendarDao.save(event.getScheduledStudySegment().getScheduledCalendar());
 
         replayMocks();
         command.apply();
         verifyMocks();
         assertEquals("Wrong number of states", 2, event.getAllStates().size());
         assertEquals("Wrong mode for current state", ScheduledActivityMode.OCCURRED, event.getCurrentState().getMode());
         assertEquals("Wrong reason for current state", NEW_REASON, event.getCurrentState().getReason());
         assertEquals("Wrong notes", "Change-o", event.getNotes());
     }
 
     public void testEventSpecificModesForNonConditionalEvent() throws Exception {
         command.setNewMode(ScheduledActivityMode.SCHEDULED);
         replayMocks();
         Collection<ScheduledActivityMode> collection = command.getEventSpecificMode();
         System.out.println("collection " + collection);
         assertEquals("Wrong number of modes", 4, collection.size());
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.NOT_APPLICABLE);
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.CONDITIONAL);
     }
 
 
     public void testEventSpecificModesForConditionalEvent() throws Exception {
         ScheduledActivity conditionalEvent = Fixtures.createConditionalEvent("ABC", 2003, Calendar.MARCH, 13);
         conditionalEvent.changeState(new Scheduled("Schedule", DateUtils.createDate(2003, Calendar.MARCH, 13)));
         command.setEvent(conditionalEvent);
         command.setNewMode(ScheduledActivityMode.SCHEDULED);
         replayMocks();
         Collection<ScheduledActivityMode> collection = command.getEventSpecificMode();
         System.out.println("collection " + collection);
        assertEquals("Wrong number of modes", 4, collection.size());
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.CANCELED);
        assertNotContains("Wrong state available", collection, ScheduledActivityMode.CONDITIONAL);
     }
 
 }
