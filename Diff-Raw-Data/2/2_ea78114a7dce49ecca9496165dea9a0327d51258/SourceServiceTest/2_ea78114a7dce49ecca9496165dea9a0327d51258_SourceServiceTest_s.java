 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import static org.easymock.EasyMock.expect;
 
 import java.util.SortedSet;
 import java.util.HashSet;
 import java.util.TreeSet;
 
 /**
  * @author Saurabh Agrawal
  */
 public class SourceServiceTest extends StudyCalendarTestCase {
     private static final String TARGET_SOURCE = "targetSource";
     private static final String SOURCE = "source";
 
     private SourceService sourceService;
     private ActivityService activityService;
     private SourceDao sourceDao;
 
     private Source targetSource;
     private Source source;
 
     private Activity activity, anotherActivity, activityToUpdate, activityToDelete;
     private Activity revertTestAcvitity1, revertTestAcvitity2, revertTestAcvitity3, revertTestAcvitity4;
     private Activity referencedActivity;
 
     protected void setUp() throws Exception {
         super.setUp();
 
         sourceDao = registerDaoMockFor(SourceDao.class);
 
         activityService = registerMockFor(ActivityService.class);
         sourceService = new SourceService();
         sourceService.setActivityService(activityService);
 
         sourceService.setSourceDao(sourceDao);
         source = Fixtures.createSource(SOURCE);
         targetSource = Fixtures.createSource(TARGET_SOURCE);
 
         activity = Fixtures.createActivity("A", "1", null, Fixtures.createActivityType("LAB_TEST"));
 
         anotherActivity = Fixtures.createActivity("B", "2", null, Fixtures.createActivityType("LAB_TEST"));
         activityToUpdate = Fixtures.createActivity("C", "2", null, Fixtures.createActivityType("LAB_TEST"));
         activityToDelete = Fixtures.createActivity("D", "3", null, Fixtures.createActivityType("LAB_TEST"));
 
         revertTestAcvitity1 = Fixtures.createActivity("A", "1", null, Fixtures.createActivityType("LAB_TEST"));
         revertTestAcvitity2 = Fixtures.createActivity("B", "2", null, Fixtures.createActivityType("LAB_TEST"));
         revertTestAcvitity3 = Fixtures.createActivity("B", "1", null, Fixtures.createActivityType("LAB_TEST"));
         revertTestAcvitity4 = Fixtures.createActivity("A", "2", null, Fixtures.createActivityType("LAB_TEST"));
 
         referencedActivity = Fixtures.createActivity("E", "2", null, Fixtures.createActivityType("LAB_TEST"));
 
     }
 
     public void testUpdateSourceWhenSourceHasNoActivity() {
         sourceDao.save(source);
         replayMocks();
 
         sourceService.updateSource(source, targetSource);
 
         assertEquals(SOURCE, targetSource.getName());
         verifyMocks();
     }
 
     public void testUpdateSourceForNewActivity() {
         source.addActivity(activity);
         source.addActivity(anotherActivity);
         sourceDao.save(source);
         replayMocks();
 
         sourceService.updateSource(source, targetSource);
 
         assertEquals(SOURCE, targetSource.getName());
         verifyMocks();
         assertEquals(2, targetSource.getActivities().size());
         assertTrue(targetSource.getActivities().contains(activity));
         assertTrue(targetSource.getActivities().contains(anotherActivity));
     }
 
     public void testUpdateSourceForUpdateAndDeleteActivity() {
         source.addActivity(activity);
         source.addActivity(anotherActivity);
 
         targetSource.addActivity(activityToUpdate);
         targetSource.addActivity(activityToDelete);
 
         expect(activityService.deleteActivity(activityToDelete)).andReturn(true);
         sourceDao.save(source);
         replayMocks();
 
         sourceService.updateSource(source, targetSource);
         verifyMocks();
         assertEquals(SOURCE, targetSource.getName());
         assertEquals(2, targetSource.getActivities().size());
         assertTrue(targetSource.getActivities().contains(activity));
         assertTrue(targetSource.getActivities().contains(activityToUpdate));
         assertEquals(anotherActivity, activityToUpdate);
     }
 
     public void testUpdateSourceForUpdateAndDeleteActivityRevertCase() {
         source.addActivity(revertTestAcvitity1);
         source.addActivity(revertTestAcvitity2);
 
         targetSource.addActivity(revertTestAcvitity3);
         targetSource.addActivity(revertTestAcvitity4);
 
         expect(activityService.deleteActivity(revertTestAcvitity4)).andReturn(true);
         expect(activityService.deleteActivity(revertTestAcvitity3)).andReturn(true);
         sourceDao.save(source);
         replayMocks();
 
         sourceService.updateSource(source, targetSource);
         verifyMocks();
         assertEquals(SOURCE, targetSource.getName());
         assertEquals(2, targetSource.getActivities().size());
         assertTrue(targetSource.getActivities().contains(revertTestAcvitity1));
         assertTrue(targetSource.getActivities().contains(revertTestAcvitity2));
     }
 
     public void testUpdateSourceForUpdateAndDeleteActivityWithReference() {
         source.addActivity(activity);
         source.addActivity(anotherActivity);
 
         targetSource.addActivity(activityToUpdate);
         targetSource.addActivity(activityToDelete);
 
         SortedSet<PlannedActivity> activities = new TreeSet<PlannedActivity>();
 
         PlannedActivity refActivity = Fixtures.createPlannedActivity(activityToDelete, 9);
         activities.add(refActivity);
         activityToDelete.setPlannedActivities(activities);
         expect(activityService.deleteActivity(activityToDelete)).andReturn(false);
         replayMocks();
             try {
                 sourceService.updateSource(source, targetSource);
             } catch (StudyCalendarValidationException scve) {
                assertEquals("Import failed. Activity "+ activityToDelete.getName() + " with code " + activityToDelete.getCode() + " is referenced withing the study. " +
                         "Please remove those references manuall and try to import activity again " , scve.getMessage());
             }
         verifyMocks();
         assertEquals(SOURCE, targetSource.getName());
         assertEquals(2, targetSource.getActivities().size());
         assertTrue(targetSource.getActivities().contains(activityToUpdate));
         assertTrue(targetSource.getActivities().contains(activityToDelete));
     }
 }
