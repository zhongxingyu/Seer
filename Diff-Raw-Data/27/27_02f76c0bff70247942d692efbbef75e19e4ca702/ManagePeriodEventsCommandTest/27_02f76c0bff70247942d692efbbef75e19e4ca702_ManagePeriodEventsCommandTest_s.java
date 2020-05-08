 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Remove;
 import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import static gov.nih.nci.cabig.ctms.lang.ComparisonTools.nullSafeEquals;
 import org.easymock.IArgumentMatcher;
 import org.easymock.classextension.EasyMock;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  * @author Jaron Sampson
  */
 public class ManagePeriodEventsCommandTest extends StudyCalendarTestCase {
     private ManagePeriodEventsCommand command;
     private Period period;
     private PlannedActivityDao plannedActivityDao;
     private AmendmentService amendmentService;
 
     private List<Activity> activities;
     private Study study;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         period = createPeriod("src", 13, 7, 2);
         plannedActivityDao = registerMockFor(PlannedActivityDao.class);
         amendmentService = registerMockFor(AmendmentService.class);
 
         study = Fixtures.createBasicTemplate();
         study.getPlannedCalendar().getEpochs().get(1).getStudySegments().get(0).addPeriod(period);
         study.setDevelopmentAmendment(new Amendment("Dev"));
         initCommand();
         activities = new ArrayList<Activity>();
         activities.add(setId(3, createNamedInstance("Three", Activity.class)));
         activities.add(setId(4, createNamedInstance("Four", Activity.class)));
         activities.add(setId(5, createNamedInstance("Five", Activity.class)));
         activities.add(setId(6, createNamedInstance("Six", Activity.class)));
         activities.add(setId(7, createNamedInstance("Seven", Activity.class)));
     }
 
     public void testInitializeGrid() throws Exception {
        period.addPlannedActivity(createPlannedActivity( 0, 2, "Det A", 2, null));
        period.addPlannedActivity(createPlannedActivity( 0, 5, "Det B", 5, null));
         period.addPlannedActivity(createPlannedActivity( 2, 7, 27));
         period.addPlannedActivity(createPlannedActivity( 3, 5, 35));
         period.addPlannedActivity(createPlannedActivity( 4, 3, "Det C", 43, null));
         period.addPlannedActivity(createPlannedActivity( 4, 3, "Det C", 143, null));
         period.addPlannedActivity(createPlannedActivity( 4, 4, "Det C", 44, null));
         assertEquals(7, period.getPlannedActivities().size());
 
         initCommand();
         assertEquals("Wrong number of rows in grid", 6, command.getGrid().size());
 
        assertGridRow(command.getGrid().get(0), activities.get(0), "Det A", null, 2, null, null, null, null, null);
        assertGridRow(command.getGrid().get(1), activities.get(0), "Det B", null, null, null, null, 5, null, null);
         assertGridRow(command.getGrid().get(2), activities.get(2), null, null, null, null, null, null, null, 27);
         assertGridRow(command.getGrid().get(3), activities.get(3), null, null, null, null, null, 35, null, null);
         assertGridRow(command.getGrid().get(5), activities.get(4), "Det C", null, null, 143, null, null, null, null);
         assertGridRow(command.getGrid().get(4), activities.get(4), "Det C", null, null, 43, 44, null, null, null);
     }
 
     private void assertGridRow(
         ManagePeriodEventsCommand.GridRow actual, Activity expectedActivity, String expectedDetails,
        Integer... eventIds
     ) {
         assertEquals("Wrong activity", expectedActivity, actual.getActivity());
         assertEquals("Wrong details", expectedDetails, actual.getDetails());
        assertEquals("Wrong number of planned activities", eventIds.length, actual.getEventIds().size());
        for (int i = 0; i < eventIds.length; i++) {
            Integer eventId = eventIds[i];
            if (eventId != null) {
                assertEquals("Wrong planned activity " + i, eventId, actual.getEventIds().get(0).getId());
             } else {
                assertNull("Expected no planned activity at " + i);
             }
         }
     }
 
     private static abstract class AbstractPlannedActivityMatcher implements IArgumentMatcher {
         protected PlannedActivity expectedPlannedActivity;
 
         public AbstractPlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
             this.expectedPlannedActivity = expectedPlannedActivity;
         }
 
         protected boolean plannedActivityMatches(PlannedActivity actual) {
             return nullSafeEquals(expectedPlannedActivity.getActivity(), actual.getActivity())
                     && nullSafeEquals(expectedPlannedActivity.getDetails(), actual.getDetails())
                     && nullSafeEquals(expectedPlannedActivity.getCondition(), actual.getCondition());
         }
     }
 
     public static PlannedActivity eqPlannedActivity(PlannedActivity event) {
         EasyMock.reportMatcher(new PlannedActivityMatcher(event));
         return null;
     }
 
     private static class PlannedActivityMatcher extends AbstractPlannedActivityMatcher {
         public PlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
             super(expectedPlannedActivity);
         }
 
         public boolean matches(Object object) {
             if (!(object instanceof PlannedActivity)) return false;
 
             return plannedActivityMatches((PlannedActivity) object);
         }
 
         public void appendTo(StringBuffer sb) {
             sb.append("PlannedActivity activity=").append(expectedPlannedActivity.getActivity());
         }
     }
 
     public static Add addFor(PlannedActivity event) {
         EasyMock.reportMatcher(new AddForPlannedActivityMatcher(event));
         return null;
     }
 
     private static class AddForPlannedActivityMatcher extends AbstractPlannedActivityMatcher {
         public AddForPlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
             super(expectedPlannedActivity);
         }
 
         public boolean matches(Object object) {
             if (!(object instanceof Add)) return false;
             // Double cast to work around a javac bug
             return plannedActivityMatches((PlannedActivity) ((Add) object).getChild());
         }
 
         public void appendTo(StringBuffer sb) {
             sb.append("Add for PlannedActivity activity=").append(expectedPlannedActivity.getActivity());
         }
     }
 
     public static Add removeFor(PlannedActivity event) {
         EasyMock.reportMatcher(new RemoveForPlannedActivityMatcher(event));
         return null;
     }
 
     private static class RemoveForPlannedActivityMatcher extends AbstractPlannedActivityMatcher {
         public RemoveForPlannedActivityMatcher(PlannedActivity expectedPlannedActivity) {
             super(expectedPlannedActivity);
         }
 
         public boolean matches(Object object) {
             if (!(object instanceof Remove)) return false;
             // Double cast to work around a javac bug
             return plannedActivityMatches((PlannedActivity) ((Remove) object).getChild());
         }
 
         public void appendTo(StringBuffer sb) {
             sb.append("Remove for PlannedActivity activity=").append(expectedPlannedActivity.getActivity());
         }
     }
 
     private void initCommand() {
         command = new ManagePeriodEventsCommand(period);
     }
 
     private PlannedActivity createPlannedActivity(int activityId, int day, Integer id) {
         return createPlannedActivity(activityId, day, null, id, null);
     }
 
     private PlannedActivity createPlannedActivity(
         int activityIndex, int day, String details, Integer id, String conditionalDetails
     ) {
         Activity activity = activities.get(activityIndex);
         PlannedActivity evt = Fixtures.createPlannedActivity(activity.getName(), day);
         evt.setId(id);
         evt.setActivity(activity);
         evt.setDetails(details);
         evt.setCondition(conditionalDetails);
         return evt;
     }
 
     private ManagePeriodEventsCommand.GridRow createGridRow(
         Activity activity, String details, String conditionalDetails, Integer... eventIds
     ) {
         ManagePeriodEventsCommand.GridRow row = new ManagePeriodEventsCommand.GridRow(activity, details, eventIds.length, conditionalDetails);
         for (int i = 0; i < eventIds.length; i++) {
             row.getEventIds().set(i, setId(eventIds[i], new PlannedActivity()));
         }
         return row;
     }
 }
