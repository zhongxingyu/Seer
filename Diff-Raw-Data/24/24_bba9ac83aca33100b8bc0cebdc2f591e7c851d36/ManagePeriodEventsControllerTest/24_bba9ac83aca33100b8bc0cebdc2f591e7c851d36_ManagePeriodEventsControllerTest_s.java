 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Period;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import static org.easymock.classextension.EasyMock.expect;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  * @author Jaron Sampson
  */
 public class ManagePeriodEventsControllerTest extends ControllerTestCase {
     private ManagePeriodEventsController controller = new ManagePeriodEventsController();
     private PeriodDao periodDao;
     private ActivityDao activityDao;
     private Period period = createPeriod("7th", 10, 8, 4);
     private List<Activity> activities = new LinkedList<Activity>();
     private ManagePeriodEventsCommand command;
 
     protected void setUp() throws Exception {
         super.setUp();
         Study parent = createNamedInstance("Root", Study.class);
         parent.setPlannedCalendar(new PlannedCalendar());
         parent.getPlannedCalendar().addEpoch(createEpoch("Holocene", "Middle"));
         parent.getPlannedCalendar().getEpochs().get(0).getArms().get(0).addPeriod(period);
 
         periodDao = registerMockFor(PeriodDao.class);
         activityDao = registerMockFor(ActivityDao.class);
 
         controller.setPeriodDao(periodDao);
         controller.setActivityDao(activityDao);
 
         request.setMethod("GET");
         request.addParameter("id", "15");
         expect(periodDao.getById(15)).andReturn(period).anyTimes();
         expect(activityDao.getAll()).andReturn(activities).anyTimes();
 
         command = new ManagePeriodEventsCommand(period, activityDao);
     }
 
     public void testFormBackingObject() throws Exception {
         replayMocks();
 
         Object command = controller.formBackingObject(request);
         verifyMocks();
         assertTrue(command instanceof ManagePeriodEventsCommand);
         assertSame(period, ((ManagePeriodEventsCommand) command).getPeriod());
     }
 
     public void testBindingGrid() throws Exception {
         request.addParameter("grid[7][4]", "2");
         replayMocks();
 
         ManagePeriodEventsCommand command
             = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
         assertEquals("Value not bound", (Integer) 2, command.getGrid().get(7).get(4));
     }
 
     public void testBindingGridNull() throws Exception {
         request.addParameter("grid[7][4]", "");
         replayMocks();
 
         ManagePeriodEventsCommand command
             = (ManagePeriodEventsCommand) controller.handleRequest(request, response).getModel().get("command");
         Integer actual = command.getGrid().get(7).get(4);
         assertNull("Value not bound", actual);
     }
 
     public void testFormView() throws Exception {
         replayMocks();
         assertEquals("managePeriod", controller.handleRequest(request, response).getViewName());
         verifyMocks();
     }
 
     public void testFormModel() throws Exception {
         replayMocks();
         Map<String, Object> model = controller.handleRequest(request, response).getModel();
         verifyMocks();
 
         assertEquals(period, model.get("period"));
         assertEquals(activities, model.get("activities"));
         assertNotNull(model.get("activitiesById"));
         assertEquals(period.getArm().getEpoch().getPlannedCalendar().getStudy(), model.get("study"));
     }
 
     public void testReferenceDataIncludesNewActivityIfParamPresent() throws Exception {
         request.addParameter("selectedActivity", "43");
         Activity expectedActivity = setId(43, new Activity());
         expect(activityDao.getById(43)).andReturn(expectedActivity);
         replayMocks();
 
         Map<String, Object> refdata = controller.referenceData(request, command, null);
         assertSame(expectedActivity, refdata.get("selectedActivity"));
         verifyMocks();
     }
 
     public void testReferenceDataEmptyIfNoNewActivity() throws Exception {
         request.removeParameter("selectedActivity");
         replayMocks();
 
         assertNotContains(controller.referenceData(request, command, null).keySet(), "selectedActivity");
         verifyMocks();
     }
 }
