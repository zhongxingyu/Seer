 package edu.northwestern.bioinformatics.studycalendar.web.schedule;
 
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.DatedScheduledEventState;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 
 import java.util.*;
 
 /**
  * @author Rhett Sutphin
  */
 public class BatchRescheduleCommandTest extends StudyCalendarTestCase {
     private BatchRescheduleCommand command;
     private ScheduledCalendarDao scheduledCalendarDao;
     private ScheduledCalendar calendar;
     private ScheduledEvent e1, e2, e3, e4, e5, e6;
 
     protected void setUp() throws Exception {
         super.setUp();
         scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
         command = new BatchRescheduleCommand(scheduledCalendarDao);
         calendar = setId(6, new ScheduledCalendar());
         calendar.addArm(new ScheduledArm());
         calendar.addArm(new ScheduledArm());
 
         e1 =  createScheduledEvent("C", 2005, Calendar.APRIL, 1, new Canceled());
         e2 =  createScheduledEvent("O", 2005, Calendar.APRIL, 3, new Occurred());
         e3 =  createScheduledEvent("S", 2005, Calendar.APRIL, 4);
 
         addEvents(
             calendar.getScheduledArms().get(0),
             e1 ,
             e2,
             e3
         );
 
 
         e4 = createScheduledEvent("C", 2005, Calendar.APRIL, 10, new Canceled());
         e5 = createScheduledEvent("O", 2005, Calendar.APRIL, 13, new Occurred());
         e6 = createScheduledEvent("S", 2005, Calendar.APRIL, 14);
 
         addEvents(
             calendar.getScheduledArms().get(1),
             e4,
             e5,
             e6
         );
 
         command.setScheduledCalendar(calendar);
     }
     public void testApplyCancel() throws Exception {
         command.setNewMode(ScheduledEventMode.CANCELED);
         command.setNewReason("Died");
         Set events = new HashSet();
         Collections.addAll(events, e3, e6);
         command.setEvents(events);
 
         doApply();
 
         ScheduledEvent prevSched0 = calendar.getScheduledArms().get(0).getEvents().get(2);
         assertEquals("New state not added to scheduled event in arm 0", 2, prevSched0.getAllStates().size());
         assertEquals("Wrong new state for previously scheduled event", ScheduledEventMode.CANCELED, prevSched0.getCurrentState().getMode());
         assertEquals("Wrong new state for previously scheduled event", "Batch change: Died", prevSched0.getCurrentState().getReason());
 
         ScheduledEvent prevSched1 = calendar.getScheduledArms().get(1).getEvents().get(2);
         assertEquals("New state not added to scheduled event in arm 1", 2, prevSched1.getAllStates().size());
         assertEquals("Wrong new state for previously scheduled event", ScheduledEventMode.CANCELED, prevSched1.getCurrentState().getMode());
         assertEquals("Wrong new state for previously scheduled event", "Batch change: Died", prevSched1.getCurrentState().getReason());
     }
 
     public void testApplyReschedule() throws Exception {
         command.setNewMode(ScheduledEventMode.SCHEDULED);
         command.setNewReason("Vacation");
         command.setDateOffset(7);
         Set events = new HashSet();
         Collections.addAll(events, e3, e6);
         command.setEvents(events);
 
         doApply();
 
         ScheduledEvent prevSched0 = calendar.getScheduledArms().get(0).getEvents().get(2);
         assertEquals("New state not added to scheduled event in arm 0", 2, prevSched0.getAllStates().size());
         assertEquals("Wrong new state for previously scheduled event", ScheduledEventMode.SCHEDULED, prevSched0.getCurrentState().getMode());
         assertEquals("Wrong new state for previously scheduled event", "Batch change: Vacation", prevSched0.getCurrentState().getReason());
         assertDayOfDate("Wrong new date for previously scheduled event", 2005, Calendar.APRIL, 11,
             ((DatedScheduledEventState) prevSched0.getCurrentState()).getDate());
 
         ScheduledEvent prevSched1 = calendar.getScheduledArms().get(1).getEvents().get(2);
         assertEquals("New state not added to scheduled event in arm 1", 2, prevSched1.getAllStates().size());
         assertEquals("Wrong new state for previously scheduled event", ScheduledEventMode.SCHEDULED, prevSched1.getCurrentState().getMode());
         assertEquals("Wrong new state for previously scheduled event", "Batch change: Vacation", prevSched1.getCurrentState().getReason());
         assertDayOfDate("Wrong new date for previously scheduled event", 2005, Calendar.APRIL, 21,
             ((DatedScheduledEventState) prevSched1.getCurrentState()).getDate());
     }
 
     public void testReasonWhenNewReasonBlank() throws Exception {
         command.setNewMode(ScheduledEventMode.CANCELED);
         command.setNewReason(null);
         Set events = new HashSet();
         Collections.addAll(events, e3, e6);
         command.setEvents(events);
 
         doApply();
 
         ScheduledEvent prevSched0 = calendar.getScheduledArms().get(0).getEvents().get(2);
         assertEquals("New state not added to scheduled event in arm 0", 2, prevSched0.getAllStates().size());
         assertEquals("Wrong new state for previously scheduled event", "Batch change", prevSched0.getCurrentState().getReason());
     }
     
     public void testDoesNothingWithNoMode() throws Exception {
         command.setNewMode(null);
         doApply(false);
         ScheduledEvent prevSched0 = calendar.getScheduledArms().get(0).getEvents().get(2);
         assertEquals("New state added to scheduled event in arm 0", 1, prevSched0.getAllStates().size());
     }
 
     private void assertUnbatchableEventsNotChanged() {
         List<ScheduledEvent> arm0Events = calendar.getScheduledArms().get(0).getEvents();
         assertEquals("New state added to canceled event in arm 0", 2, arm0Events.get(0).getAllStates().size());
         assertEquals("New state added to occurred event in arm 0", 2, arm0Events.get(1).getAllStates().size());
         List<ScheduledEvent> arm1Events = calendar.getScheduledArms().get(0).getEvents();
         assertEquals("New state added to canceled event in arm 1", 2, arm1Events.get(0).getAllStates().size());
         assertEquals("New state added to occurred event in arm 1", 2, arm1Events.get(1).getAllStates().size());
     }
 
     private void doApply() {
         doApply(true);
     }
 
     private void doApply(boolean expectSave) {
         if (expectSave) scheduledCalendarDao.save(calendar);
         replayMocks();
 
         command.apply();
         verifyMocks();
         
         assertUnbatchableEventsNotChanged();
     }
 }
