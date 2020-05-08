 package edu.northwestern.bioinformatics.studycalendar.domain;
 
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
 import edu.northwestern.bioinformatics.studycalendar.tools.Range;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertDayOfDate;
 import junit.framework.TestCase;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public class ScheduledStudySegmentTest extends TestCase {
     private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
     private ScheduledStudySegment scheduledStudySegment = new ScheduledStudySegment();
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         scheduledCalendar.addStudySegment(scheduledStudySegment);
     }
 
     public void testNameWithMultiArmEpoch() throws Exception {
         Epoch multi = Epoch.create("Treatment", "A", "B", "C");
         scheduledStudySegment.setStudySegment(multi.getStudySegments().get(1));
         assertName("Treatment: B");
     }
 
     public void testNameWithZeroStudySegmentEpoch() throws Exception {
         Epoch single = Epoch.create("Screening");
         scheduledStudySegment.setStudySegment(single.getStudySegments().get(0));
         assertName("Screening");
     }
 
     public void testNameWhenRepeated() throws Exception {
         Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
         scheduledCalendar.getScheduledStudySegments().clear();
         scheduledCalendar.addStudySegment(createScheduledStudySegment(epoch.getStudySegments().get(1)));
         scheduledCalendar.addStudySegment(createScheduledStudySegment(epoch.getStudySegments().get(0)));
         scheduledCalendar.addStudySegment(scheduledStudySegment);
         scheduledStudySegment.setStudySegment(epoch.getStudySegments().get(1));
 
         List<ScheduledStudySegment> studySegments = scheduledCalendar.getScheduledStudySegments();
         assertName("Treatment: B (1)", studySegments.get(0));
         assertName("Treatment: A", studySegments.get(1));
         assertName("Treatment: B (2)", studySegments.get(2));
     }
 
     private void assertName(String expectedName) {
         assertName(expectedName, this.scheduledStudySegment);
     }
 
     private static void assertName(String expectedName, ScheduledStudySegment scheduledStudySegment) {
         assertEquals("Wrong name", expectedName, scheduledStudySegment.getName());
     }
 
     public void testEventsByDay() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity("One", 2006, Calendar.SEPTEMBER, 20));
         scheduledStudySegment.addEvent(createScheduledActivity("Two", 2006, Calendar.SEPTEMBER, 20));
         scheduledStudySegment.addEvent(createScheduledActivity("Three", 2006, Calendar.SEPTEMBER, 18));
         scheduledStudySegment.addEvent(createScheduledActivity("Two", 2006, Calendar.SEPTEMBER, 24));
 
         Map<Date, List<ScheduledActivity>> byDate = scheduledStudySegment.getActivitiesByDate();
         assertEquals(3, byDate.size());
         Iterator<Map.Entry<Date, List<ScheduledActivity>>> entries = byDate.entrySet().iterator();
 
         assertTrue(entries.hasNext());
         assertEventDayRecord(entries.next(), 2006, Calendar.SEPTEMBER, 18, "Three");
         assertTrue(entries.hasNext());
         assertEventDayRecord(entries.next(), 2006, Calendar.SEPTEMBER, 20, "One", "Two");
         assertTrue(entries.hasNext());
         assertEventDayRecord(entries.next(), 2006, Calendar.SEPTEMBER, 24, "Two");
         assertFalse(entries.hasNext());
     }
 
     public void testEventsByDayWithWeightedPlannedActivity() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity(setId(1,createPlannedActivity("ActivityOne", 20, -5)), 2006, Calendar.SEPTEMBER, 20));
         scheduledStudySegment.addEvent(createScheduledActivity(setId(2, createPlannedActivity("ActivityTwo", 20, 0)), 2006, Calendar.SEPTEMBER, 20));
         scheduledStudySegment.addEvent(createScheduledActivity(setId(3, createPlannedActivity("ActivityThree", 20, 10)), 2006, Calendar.SEPTEMBER, 20));
         scheduledStudySegment.addEvent(createScheduledActivity(setId(4, createPlannedActivity("ActivityFour", 20, 2)), 2006, Calendar.SEPTEMBER, 20));
 
         Map<Date, List<ScheduledActivity>> byDate = scheduledStudySegment.getActivitiesByDate();
 
         Iterator<Map.Entry<Date, List<ScheduledActivity>>> entries = byDate.entrySet().iterator();
 
         List<ScheduledActivity> sa = entries.next().getValue();
         assertEquals("First scheduledActivity in the list is not sorted by weight ", (int) sa.get(0).getPlannedActivity().getWeight(), 10);
         assertEquals("First scheduledActivity in the list is not sorted by weight ", (int) sa.get(1).getPlannedActivity().getWeight(), 2);
         assertEquals("First scheduledActivity in the list is not sorted by weight ", (int) sa.get(2).getPlannedActivity().getWeight(), 0);
         assertEquals("First scheduledActivity in the list is not sorted by weight ", (int) sa.get(3).getPlannedActivity().getWeight(), -5);
     }
 
     private void assertEventDayRecord(
         Map.Entry<Date, List<ScheduledActivity>> actual, int year, int month, int day, String... expectedActivities
     ) {
         assertDayOfDate("Wrong key", year, month, day, actual.getKey());
         List<ScheduledActivity> actualEvents = actual.getValue();
         assertEquals("Wrong number of activities", expectedActivities.length, actualEvents.size());
         for (int i = 0; i < actualEvents.size(); i++) {
             ScheduledActivity actualEvent = actualEvents.get(i);
             assertEquals("Event mismatch at " + i, expectedActivities[i], actualEvent.getActivity().getName());
         }
     }
 
     public void testGetNextStudySegmentPerProtocolStartDate() throws Exception {
         StudySegment studySegment = Epoch.create("Screening").getStudySegments().get(0);
         Period period = createPeriod("P1", 4, 7, 3);
         PlannedActivity plannedActivity = createPlannedActivity("ABC", 4);
         period.addPlannedActivity(plannedActivity);
         studySegment.addPeriod(period); // studySegment length is 21 days
 
         scheduledStudySegment.setStartDay(4);
         scheduledStudySegment.setStartDate(DateTools.createDate(2004, Calendar.JANUARY, 4));
         scheduledStudySegment.setStudySegment(studySegment);
 
         assertDayOfDate(2004, Calendar.JANUARY, 25, scheduledStudySegment.getNextStudySegmentPerProtocolStartDate());
     }
 
     public void testIsNotCompleteIfAnyEventInScheduledState() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 4, new Canceled()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 9, new Occurred()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 18));
 
         assertFalse(scheduledStudySegment.isComplete());
     }
 
     public void testIsCompleteIfAllEventsAreCanceled() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 4, new Canceled()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 5, new Canceled()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 6, new Canceled()));
 
         assertTrue(scheduledStudySegment.isComplete());
     }
 
     public void testIsCompleteIfAllEventsAreOccurred() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 4, new Occurred()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 5, new Occurred()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 6, new Occurred()));
 
         assertTrue(scheduledStudySegment.isComplete());
     }
 
     public void testIsCompleteIfNoEventsAreScheduled() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 4, new Occurred()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 5, new Canceled()));
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", 2005, Calendar.OCTOBER, 6, new Occurred()));
 
         assertTrue(scheduledStudySegment.isComplete());
     }
 
   /*  public void testGetNextScheduledDate() {
         Calendar now = Calendar.getInstance();
         Calendar calendar = Calendar.getInstance();
 
 
         calendar.add(Calendar.MONTH, -1);
         scheduledStudySegment.addEvent(createScheduledActivity("ABC", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Scheduled()));
         calendar.add(Calendar.MONTH, 2);
         scheduledStudySegment.addEvent(createScheduledActivity("DEF", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Occurred()));
         calendar.add(Calendar.MONTH, 1);
         scheduledStudySegment.addEvent(createScheduledActivity("GHI", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Scheduled()));
         calendar.add(Calendar.MONTH, 1);
         scheduledStudySegment.addEvent(createScheduledActivity("JKL", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Scheduled()));
 
         Date d = scheduledStudySegment.getNextScheduledActivity(now.getTime()).getActualDate();
 
         assertTrue(scheduledStudySegment.getEvents().get(2).getActualDate() == d);
     }   */
 
     public void testUnscheduleAllOutstandingEvents() throws Exception {
         scheduledStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 1));
         scheduledStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 2,
             new Occurred(null, DateTools.createDate(2005, Calendar.AUGUST, 4))));
         scheduledStudySegment.addEvent(createScheduledActivity("CBC", 2005, Calendar.AUGUST, 3,
             new Canceled()));
         scheduledStudySegment.addEvent(createScheduledActivity("Maybe CBC", 2005, Calendar.AUGUST, 4,
             new Conditional()));
         scheduledStudySegment.addEvent(createScheduledActivity("Maybe CBC", 2005, Calendar.AUGUST, 5,
             new NotApplicable()));
 
        scheduledStudySegment.unscheduleOutstandingEvents("Testing", null);
 
         assertEquals("Scheduled event not changed", 2, scheduledStudySegment.getActivities().get(0).getAllStates().size());
         assertEquals("Scheduled not changed to canceled", ScheduledActivityMode.CANCELED,
             scheduledStudySegment.getActivities().get(0).getCurrentState().getMode());
         assertEquals("Scheduled new mode has wrong reason", "Testing",
             scheduledStudySegment.getActivities().get(0).getCurrentState().getReason());
 
         assertEquals("Conditional event not changed", 3, scheduledStudySegment.getActivities().get(3).getAllStates().size());
         assertEquals("Conditional not changed to NA", ScheduledActivityMode.NOT_APPLICABLE,
             scheduledStudySegment.getActivities().get(3).getCurrentState().getMode());
         assertEquals("Conditional new mode has wrong reason", "Testing",
             scheduledStudySegment.getActivities().get(3).getCurrentState().getReason());
 
         assertEquals("Occurred event changed", 2, scheduledStudySegment.getActivities().get(1).getAllStates().size());
         assertEquals("Canceled event changed", 2, scheduledStudySegment.getActivities().get(2).getAllStates().size());
         assertEquals("NA event changed", 2, scheduledStudySegment.getActivities().get(4).getAllStates().size());
     }
 
     public void testDateRange() throws Exception {
         ScheduledStudySegment segment = Fixtures.createScheduledStudySegment(DateTools.createDate(2007, Calendar.NOVEMBER, 3), 17);
         assertDateRange(2007, Calendar.NOVEMBER,  3,
                         2007, Calendar.NOVEMBER, 19, segment.getDateRange());
     }
 
     public void testDateRangeCoversAllCurrentActualDatesEvenIfTheyAreOutsideTheIdealRange() throws Exception {
         ScheduledStudySegment segment = Fixtures.createScheduledStudySegment(DateTools.createDate(2007, Calendar.MARCH, 3), 15);
         assertDateRange(2007, Calendar.MARCH,  3,
                         2007, Calendar.MARCH, 17, segment.getDateRange());
 
         segment.addEvent(createScheduledActivity("H", 2006, Calendar.APRIL, 6));
         assertDateRange(2006, Calendar.APRIL,  6, 
                         2007, Calendar.MARCH, 17, segment.getDateRange());
 
         segment.addEvent(createScheduledActivity("L", 2008, Calendar.APRIL, 19));
         assertDateRange(2006, Calendar.APRIL,  6,
                         2008, Calendar.APRIL, 19, segment.getDateRange());
     }
 
     private static void assertDateRange(
         int expectedStartYear, int expectedStartMonth, int expectedStartDay,
         int expectedStopYear, int expectedStopMonth, int expectedStopDay,
         Range<Date> actual
     ) {
         assertDayOfDate("Wrong start of range", expectedStartYear, expectedStartMonth, expectedStartDay, actual.getStart());
         assertDayOfDate("Wrong end of range", expectedStopYear, expectedStopMonth, expectedStopDay, actual.getStop());
     }
 }
