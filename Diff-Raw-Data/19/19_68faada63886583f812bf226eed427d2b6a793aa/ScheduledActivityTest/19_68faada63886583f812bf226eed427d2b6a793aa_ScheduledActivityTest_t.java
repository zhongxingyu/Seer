 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivity;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
 import junit.framework.TestCase;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class ScheduledActivityTest extends TestCase {
     private ScheduledActivity scheduledActivity, sa0, sa1;
     private PlannedActivity plannedActivity;
     private Activity activityA = createActivity("A");
     private Activity activityB = createActivity("B");
 
     public void setUp() throws Exception {
         super.setUp();
         plannedActivity = new PlannedActivity();
         scheduledActivity = new ScheduledActivity();
         sa0 = new ScheduledActivity(); sa1 = new ScheduledActivity();
         sa0.setActivity(activityA);
         sa1.setActivity(activityB);
     }
 
     public void testGetActualDateCurrentDate() throws Exception {
         Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.setIdealDate(DateTools.createDate(2006, Calendar.AUGUST, 1));
         scheduledActivity.changeState(new Scheduled(null, expected));
         assertEquals(expected, scheduledActivity.getActualDate());
     }
 
    public void testGetActualDateIsPreviousDateIfCurrentNotSet() throws Exception {
         Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.setIdealDate(DateTools.createDate(2006, Calendar.AUGUST, 1));
         scheduledActivity.changeState(new Scheduled(null, expected));
        scheduledActivity.changeState(new Canceled(null, new Date()));
        // this shouldn't happen in new data, but there could still be some out there
        scheduledActivity.getCurrentState().setDate(null);
         assertEquals(expected, scheduledActivity.getActualDate());
     }
 
    public void testGetActualDateIsIdealDateWhenNothingElseAvailable() throws Exception {
         Date expected = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.setIdealDate(expected);
         assertEquals(expected, scheduledActivity.getActualDate());
     }
 
     public void testGetDayNumberWithRepetition() throws Exception {
         plannedActivity.setDay(1);
         scheduledActivity.setPlannedActivity(plannedActivity);
         scheduledActivity.setRepetitionNumber(null);
         assertEquals(null,scheduledActivity.getDayNumber());
     }
     
     public void testGetDayNumberWithPeriod() throws Exception {
         scheduledActivity.setPlannedActivity(null);
         scheduledActivity.setRepetitionNumber(3);
         assertEquals(null,scheduledActivity.getDayNumber());
     }
 
     public void testChangeStateWithNoEvents() throws Exception {
         scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         assertEquals("Wrong number of states in history", 1, scheduledActivity.getAllStates().size());
         assertTrue("Wrong curent state", scheduledActivity.getCurrentState() instanceof Canceled);
         assertEquals("Wrong curent state", "A", scheduledActivity.getCurrentState().getReason());
         assertEquals(0, scheduledActivity.getPreviousStates().size());
     }
 
     public void testChangeStateWithCurrentEventOnly() throws Exception {
         scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         scheduledActivity.changeState(new Canceled("B",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         assertEquals("Wrong number of states in history", 2, scheduledActivity.getAllStates().size());
         assertEquals("Wrong current state", "B", scheduledActivity.getCurrentState().getReason());
         assertEquals("Wrong number of previous states", 1, scheduledActivity.getPreviousStates().size());
         assertEquals("Wrong previous state", "A", scheduledActivity.getPreviousStates().get(0).getReason());
     }
     
     public void testChangeStateWithExistingHistory() throws Exception {
         scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         scheduledActivity.changeState(new Canceled("B",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         scheduledActivity.changeState(new Canceled("C",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         assertEquals("Wrong number of states in history", 3, scheduledActivity.getAllStates().size());
         assertEquals("Wrong current state", "C", scheduledActivity.getCurrentState().getReason());
         assertEquals("Wrong number of previous states", 2, scheduledActivity.getPreviousStates().size());
         assertEquals("Wrong previous state 0", "A", scheduledActivity.getPreviousStates().get(0).getReason());
         assertEquals("Wrong previous state 1", "B", scheduledActivity.getPreviousStates().get(1).getReason());
     }
 
     public void testGetAllWithNone() throws Exception {
         assertEquals(0, scheduledActivity.getAllStates().size());
     }
 
     public void testGetAllWithCurrentOnly() throws Exception {
         scheduledActivity.setPreviousStates(null); // paranoia
         scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         List<ScheduledActivityState> all = scheduledActivity.getAllStates();
         assertEquals(1, all.size());
         assertEquals("A", all.get(0).getReason());
     }
     
     public void testGetAllWithHistory() throws Exception {
         scheduledActivity.changeState(new Canceled("A",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         scheduledActivity.changeState(new Canceled("B",DateTools.createDate(2006, Calendar.AUGUST, 3)));
         scheduledActivity.changeState(new Canceled("C",DateTools.createDate(2006, Calendar.AUGUST, 3)));
 
         List<ScheduledActivityState> all = scheduledActivity.getAllStates();
         assertEquals(3, all.size());
         assertEquals("A", all.get(0).getReason());
         assertEquals("B", all.get(1).getReason());
         assertEquals("C", all.get(2).getReason());
     }
 
     public void testChangeStateCanceledToOccurredWithOffStudy() throws Exception {
         scheduledActivity.changeState(new Scheduled("New", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
         ScheduledCalendar calendar = new ScheduledCalendar();
         StudySubjectAssignment assignment = new StudySubjectAssignment();
         calendar.setAssignment(assignment);
         calendar.addStudySegment(new ScheduledStudySegment());
         calendar.getScheduledStudySegments().get(0).addEvent(scheduledActivity);
         scheduledActivity.changeState(new Canceled("Canceled",DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
         assignment.setEndDateEpoch(DateTools.createDate(2007, Calendar.SEPTEMBER, 1));
         
         //scheduledActivity.changeState(new Occurred());
         assertEquals("Wrong states size", 2, scheduledActivity.getAllStates().size());
         assertEquals("Wrong event state", ScheduledActivityMode.CANCELED, scheduledActivity.getCurrentState().getMode());
     }
 
     public void testScheduleConditional() throws Exception {
         scheduledActivity.changeState(new Conditional("New", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
         scheduledActivity.changeState(new Scheduled("Scheduled", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
         scheduledActivity.changeState(new Occurred("Occurred", DateTools.createDate(2007, Calendar.SEPTEMBER, 2)));
 
         assertEquals("Conditional flag not set", true, scheduledActivity.isConditionalEvent());
         assertEquals("Wrong previous state size", 2, scheduledActivity.getPreviousStates().size());
     }
 
     public void testIsConditionalPos() throws Exception {
         Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.changeState(new Conditional("Conditional", date));
         scheduledActivity.changeState(new Scheduled("Conditional", date));
         assertTrue("Event should be conditional", scheduledActivity.isConditionalEvent());
     }
 
     public void testIsConditionalNeg() throws Exception {
         Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.changeState(new Scheduled("Conditional", date));
         assertFalse("Event should not be conditional", scheduledActivity.isConditionalEvent());
     }
 
     public void testIsValidStateChangePos() throws Exception {
         Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.changeState(new Conditional("Conditional", date));
         assertTrue("Should be valid new state", scheduledActivity.isValidNewState(Scheduled.class));
     }
 
     public void testIsValidStateChangeNeg() throws Exception {
         Date date = DateTools.createDate(2006, Calendar.AUGUST, 3);
         scheduledActivity.changeState(new Conditional("Conditional", date));
         assertFalse("Should not be valid new state", scheduledActivity.isValidNewState(Canceled.class));
     }
 
     public void testUnscheduleScheduledActivity() throws Exception {
         Date scheduledOn = DateTools.createDate(2005, Calendar.MAY, 5);
         scheduledActivity.changeState(new Scheduled(null, scheduledOn));
         assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());
 
         scheduledActivity.unscheduleIfOutstanding("Testing");
         assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
         assertEquals("State not changed", ScheduledActivityMode.CANCELED,
             scheduledActivity.getCurrentState().getMode());
         assertEquals("New state has wrong reason", "Testing",
             scheduledActivity.getCurrentState().getReason());
         assertEquals("New state does not preserve previous date", scheduledOn,
             scheduledActivity.getCurrentState().getDate());
     }
 
     public void testUnscheduleConditionalActivity() throws Exception {
         Date conditionalFor = DateTools.createDate(2005, Calendar.MAY, 9);
         scheduledActivity.changeState(new Conditional(null, conditionalFor));
         assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());
 
         scheduledActivity.unscheduleIfOutstanding("Testing");
         assertEquals("State not changed", 2, scheduledActivity.getAllStates().size());
         assertEquals("State not changed", ScheduledActivityMode.NOT_APPLICABLE,
             scheduledActivity.getCurrentState().getMode());
         assertEquals("New state has wrong reason", "Testing",
             scheduledActivity.getCurrentState().getReason());
         assertEquals("New state does not preserve previous date", conditionalFor,
             scheduledActivity.getCurrentState().getDate());
     }
 
     public void testUnscheduleCanceledEvent() throws Exception {
         scheduledActivity.changeState(new Canceled());
         assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());
 
         scheduledActivity.unscheduleIfOutstanding("Testing");
         assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
         assertEquals("State changed", ScheduledActivityMode.CANCELED,
             scheduledActivity.getCurrentState().getMode());
     }
 
     public void testUnscheduleOccurredEvent() throws Exception {
         scheduledActivity.changeState(new Occurred());
         assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());
 
         scheduledActivity.unscheduleIfOutstanding("Testing");
         assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
         assertEquals("State changed", ScheduledActivityMode.OCCURRED,
             scheduledActivity.getCurrentState().getMode());
     }
 
     public void testUnscheduleNotApplicableEvent() throws Exception {
         scheduledActivity.changeState(new NotApplicable());
         assertEquals("Test setup failure", 1, scheduledActivity.getAllStates().size());
 
         scheduledActivity.unscheduleIfOutstanding("Testing");
         assertEquals("State changed", 1, scheduledActivity.getAllStates().size());
         assertEquals("State changed", ScheduledActivityMode.NOT_APPLICABLE,
             scheduledActivity.getCurrentState().getMode());
     }
 
     public void testIsOutstandingWhenOutstanding() throws Exception {
         scheduledActivity.changeState(new Conditional());
         assertTrue(scheduledActivity.isOutstanding());
     }
 
     public void testIsOutstandingWhenCompleted() throws Exception {
         scheduledActivity.changeState(new Canceled());
         assertFalse(scheduledActivity.isOutstanding());
     }
 
     public void testNaturalOrderConsidersStudy() throws Exception {
         sa0.setPlannedActivity(plannedActivityFromStudy("Y"));
         sa1.setPlannedActivity(plannedActivityFromStudy("X"));
 
         assertPositive(sa0.compareTo(sa1));
         assertNegative(sa1.compareTo(sa0));
     }
     
     public void testNaturalOrderConsidersPAWeight() throws Exception {
         PlannedActivity pa0 = plannedActivityFromStudy("X");
         PlannedActivity pa1 = plannedActivityFromStudy("X");
         pa1.setWeight(4);
         sa0.setPlannedActivity(pa0);
         sa1.setPlannedActivity(pa1);
 
         assertPositive(sa0.compareTo(sa1));
         assertNegative(sa1.compareTo(sa0));
     }
 
     private PlannedActivity plannedActivityFromStudy(String ident) {
         PlannedActivity pa = createPlannedActivity(activityA, 2);
         Period p = createPeriod(1, 7, 1); p.addPlannedActivity(pa);
         Study s = createSingleEpochStudy(ident, "E");
         s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).addPeriod(p);
         return pa;
     }
 
     public void testCompareWithNoPlannedActivityDoesNotErrorOut() throws Exception {
         sa0.setPlannedActivity(null); sa1.setPlannedActivity(null);
         assertNegative(sa0.compareTo(sa1));
     }
 
     public void testCompareWithDisassociatedPlannedActivityDoesNotErrorOut() throws Exception {
         sa0.setPlannedActivity(new PlannedActivity());
         sa1.setPlannedActivity(new PlannedActivity());
 
         assertNegative(sa0.compareTo(sa1));
     }
 }
