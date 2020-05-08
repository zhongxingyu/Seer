 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import static org.easymock.classextension.EasyMock.expect;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
 * @author Jaron Sampson
  */
 public class ManagePeriodEventsCommandTest extends StudyCalendarTestCase {
     private ManagePeriodEventsCommand command;
     private ActivityDao activityDao;
    private ActivityTypeDao activityTypeDao;
     private Period period;
 
     protected void setUp() throws Exception {
         super.setUp();
         period = Fixtures.createPeriod("src", 13, 7, 2);
         activityDao = registerMockFor(ActivityDao.class);
         initCommand();
     }
 
     public void testInitializeGrid() throws Exception {
         period.addPlannedEvent(createPlannedEvent( 1, 2));
         period.addPlannedEvent(createPlannedEvent( 1, 5));
         period.addPlannedEvent(createPlannedEvent( 7, 7));
         period.addPlannedEvent(createPlannedEvent( 2, 5));
         period.addPlannedEvent(createPlannedEvent(10, 3));
         period.addPlannedEvent(createPlannedEvent(10, 3));
         assertEquals(6, period.getPlannedEvents().size());
 
         initCommand();
         assertTrue(command.getGrid().containsKey(1));
         assertTrue(command.getGrid().containsKey(7));
         assertTrue(command.getGrid().containsKey(2));
         assertTrue(command.getGrid().containsKey(10));
 
         assertGridValue( 1, 2, 1);
         assertGridValue( 1, 1, 0);
         assertGridValue( 1, 5, 1);
         assertGridValue( 7, 7, 1);
         assertGridValue( 2, 5, 1);
         assertGridValue(10, 3, 2);
 
         for (Map.Entry<Integer, List<Integer>> entry : command.getGrid().entrySet()) {
             assertEquals("Wrong number of days for activity " + entry.getKey(),
                 7, entry.getValue().size());
         }
     }
 
     private void assertGridValue(int activityId, int dayNumber, int eventCount) {
         assertEquals(eventCount, (int) command.getGrid().get(activityId).get(dayNumber - 1));
     }
 
     public void testApplyToBlank() throws Exception {
         command.getGrid().get(4).set(3, 1);
         command.getGrid().get(8).set(0, 2);
 
         expectGetActivity(4);
         expectGetActivity(8);
         expectGetActivity(8);
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(3, period.getPlannedEvents().size());
     }
 
     public void testApplyDoesNotAddOverExisting() throws Exception {
         PlannedEvent expectedEvent = createPlannedEvent(5, 4);
 
         period.addPlannedEvent(expectedEvent);
         initCommand();
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(1, period.getPlannedEvents().size());
         assertSame(expectedEvent, period.getPlannedEvents().get(0));
     }
 
     public void testApplyAddSecond() throws Exception {
         PlannedEvent expectedEvent = createPlannedEvent(5, 4);
 
         period.addPlannedEvent(expectedEvent);
         initCommand();
 
         command.getGrid().get(5).set(3, 2);
         expectGetActivity(5);
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(2, period.getPlannedEvents().size());
         assertSame(period.getPlannedEvents().get(0), expectedEvent);
         assertNotSame(period.getPlannedEvents().get(1), expectedEvent);
     }
 
     private void expectGetActivity(int activityId) {
         expect(activityDao.getById(activityId)).andReturn(new Activity());
     }
 
     public void testApplyRemove() throws Exception {
         PlannedEvent existingEvent = createPlannedEvent(6, 2);
         period.addPlannedEvent(existingEvent);
         initCommand();
 
         command.getGrid().get(6).set(1, 0);
 
         replayMocks();
         command.apply();
         verifyMocks();
 
         assertEquals(0, period.getPlannedEvents().size());
     }
 
     private void initCommand() {
        command = new ManagePeriodEventsCommand(period, activityDao, activityTypeDao);
     }
 
     private static PlannedEvent createPlannedEvent(int activityId, int day) {
         PlannedEvent evt = Fixtures.createPlannedEvent("A" + activityId, day);
         evt.getActivity().setId(activityId);
         return evt;
     }
 }
