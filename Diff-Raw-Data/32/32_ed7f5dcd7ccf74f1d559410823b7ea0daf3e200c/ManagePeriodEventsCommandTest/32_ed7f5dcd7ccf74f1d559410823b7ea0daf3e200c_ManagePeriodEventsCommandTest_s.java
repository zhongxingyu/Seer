 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * @author Rhett Sutphin
  * @author Jaron Sampson
  */
 public class ManagePeriodEventsCommandTest extends StudyCalendarTestCase {
     private ManagePeriodEventsCommand command;
     private ActivityDao activityDao;
     private Period period;
 
     private List<Activity> activities;
 
     protected void setUp() throws Exception {
         super.setUp();
         period = createPeriod("src", 13, 7, 2);
         activityDao = registerMockFor(ActivityDao.class);
         initCommand();
         activities = new ArrayList<Activity>();
         activities.add(setId(3, createNamedInstance("Three", Activity.class)));
         activities.add(setId(4, createNamedInstance("Four", Activity.class)));
         activities.add(setId(5, createNamedInstance("Five", Activity.class)));
         activities.add(setId(6, createNamedInstance("Six", Activity.class)));
         activities.add(setId(7, createNamedInstance("Seven", Activity.class)));
     }
 
     public void testInitializeGrid() throws Exception {
         period.addPlannedEvent(createPlannedEvent( 0, 2, "Det A"));
         period.addPlannedEvent(createPlannedEvent( 0, 5, "Det B"));
         period.addPlannedEvent(createPlannedEvent( 2, 7));
         period.addPlannedEvent(createPlannedEvent( 3, 5));
         period.addPlannedEvent(createPlannedEvent( 4, 3, "Det C"));
         period.addPlannedEvent(createPlannedEvent( 4, 3, "Det C"));
         assertEquals(6, period.getPlannedEvents().size());
 
         initCommand();
         assertEquals("Wrong number of rows in grid", 5, command.getGrid().size());
 
         assertGridRow(command.getGrid().get(0), activities.get(0), "Det A", 0, 1, 0, 0, 0, 0, 0);
         assertGridRow(command.getGrid().get(1), activities.get(0), "Det B", 0, 0, 0, 0, 1, 0, 0);
         assertGridRow(command.getGrid().get(2), activities.get(2), null,    0, 0, 0, 0, 0, 0, 1);
         assertGridRow(command.getGrid().get(3), activities.get(3), null,    0, 0, 0, 0, 1, 0, 0);
         assertGridRow(command.getGrid().get(4), activities.get(4), "Det C", 0, 0, 2, 0, 0, 0, 0);
     }
 
     private void assertGridRow(
         ManagePeriodEventsCommand.GridRow actual, Activity expectedActivity, String expectedDetails, Integer... eventCounts
     ) {
         assertEquals("Wrong activity", expectedActivity, actual.getActivity());
         assertEquals("Wrong details", expectedDetails, actual.getDetails());
         assertEqualArrays("Wrong counts", eventCounts, actual.getCounts().toArray(new Integer[0]));
     }
 
     public void testApplyToBlank() throws Exception {
         command.getGrid().add(createGridRow(activities.get(0), "Det A", 0, 1, 0, 0, 1, 0, 0));
         command.getGrid().add(createGridRow(activities.get(1), null,    2, 0, 0, 0, 0, 0, 0));
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(4, period.getPlannedEvents().size());
     }
 
     public void testApplyDoesNotAddOverExisting() throws Exception {
         PlannedEvent expectedEvent = createPlannedEvent(3, 4);
 
         period.addPlannedEvent(expectedEvent);
         initCommand();
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(1, period.getPlannedEvents().size());
         assertSame(expectedEvent, period.getPlannedEvents().get(0));
     }
 
     public void testApplyAddSecond() throws Exception {
         PlannedEvent expectedEvent = createPlannedEvent(3, 4);
 
         period.addPlannedEvent(expectedEvent);
         initCommand();
 
         command.getGrid().get(0).incrementDay(4);
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(2, period.getPlannedEvents().size());
         assertSame(period.getPlannedEvents().get(0), expectedEvent);
         assertNotSame(period.getPlannedEvents().get(1), expectedEvent);
     }
 
     public void testApplyRemove() throws Exception {
         PlannedEvent existingEvent = createPlannedEvent(4, 2);
         period.addPlannedEvent(existingEvent);
         initCommand();
 
         command.getGrid().get(0).decrementDay(2);
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(0, period.getPlannedEvents().size());
     }
 
     public void testApplyChangeDetails() throws Exception {
         PlannedEvent existingEvent = createPlannedEvent(4, 2, "Det A");
         period.addPlannedEvent(existingEvent);
         initCommand();
 
         command.getGrid().get(0).decrementDay(2);
         command.getGrid().add(createGridRow(activities.get(4), "Det B", 0, 0, 1, 0, 0, 0, 0));
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(1, period.getPlannedEvents().size());
         PlannedEvent actual = period.getPlannedEvents().get(0);
         assertEquals("Details not updated", "Det B", actual.getDetails());
     }
 
     public void testApplyAnotherWithDifferentDetails() throws Exception {
         PlannedEvent existingEvent = createPlannedEvent(4, 2, "Det A");
         period.addPlannedEvent(existingEvent);
         initCommand();
 
         command.getGrid().add(createGridRow(activities.get(4), "Det B", 0, 0, 1, 0, 0, 0, 0));
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(2, period.getPlannedEvents().size());
         assertEquals("Details wrong on old event", "Det A", period.getPlannedEvents().get(0).getDetails());
         assertEquals("Details wrong on new event", "Det B", period.getPlannedEvents().get(1).getDetails());
     }
 
     private void initCommand() {
         command = new ManagePeriodEventsCommand(period);
     }
 
     private PlannedEvent createPlannedEvent(int activityId, int day) {
         return createPlannedEvent(activityId, day, null);
     }
 
     private PlannedEvent createPlannedEvent(int activityIndex, int day, String details) {
         Activity activity = activities.get(activityIndex);
         PlannedEvent evt = Fixtures.createPlannedEvent(activity.getName(), day);
         evt.setActivity(activity);
         evt.setDetails(details);
         return evt;
     }
 
     private ManagePeriodEventsCommand.GridRow createGridRow(Activity activity, String details, int... counts) {
         ManagePeriodEventsCommand.GridRow row = new ManagePeriodEventsCommand.GridRow(activity, details, counts.length);
         for (int i = 0; i < counts.length; i++) {
             row.getCounts().set(i, counts[i]);
         }
         return row;
     }
 }
