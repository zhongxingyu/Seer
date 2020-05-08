 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
 import static org.easymock.EasyMock.expect;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import static java.util.Arrays.asList;
 import java.util.List;
 
 /**
  * @author John Dzak
  */
 public class SearchActivitiesControllerTest extends ControllerTestCase {
     private SearchActivitiesController controller;
     private SearchActivitiesCommand command;
     private List<Activity> activities;
     private ActivityDao activityDao;
     private Source source0, source1;
     private SourceDao sourceDao;
 
     protected void setUp() throws Exception {
         super.setUp();
 
         command = registerMockFor(SearchActivitiesCommand.class);
         activityDao = registerDaoMockFor(ActivityDao.class);
         sourceDao = registerDaoMockFor(SourceDao.class);
 
         controller = new SearchActivitiesController() {
             @Override
             protected SearchActivitiesCommand getCommand(HttpServletRequest request) throws Exception {
                 return command;
             }
         };
         controller.setControllerTools(controllerTools);
         controller.setActivityDao(activityDao);
         controller.setSourceDao(sourceDao);
 
         source0 = setId(0, createNamedInstance("PSC Manual Entry Source", Source.class));
         source1 = setId(1, createNamedInstance("LOINK", Source.class));
         
         activities = asList(
                 createActivity("Activity A", "AA", source0, ActivityType.INTERVENTION),
                 createActivity("Activity B", "BB", source1, ActivityType.DISEASE_MEASURE)
         );
 
         request.setMethod("GET");
     }
 
     public void testHandle() throws Exception {
         expectSearch("", null, null);
         expectRefDataCalls();
         replayMocks();
 
         ModelAndView mv = controller.handleRequest(request, response);
         verifyMocks();
 
         assertEquals("template/ajax/activities", mv.getViewName());
     }
 
     public void testHandleSearchByFullName() throws Exception {
         expectSearch("Activity B", null, null);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 1, actual.size());
         assertEquals("Wrong search results", "Activity B", actual.get(0).getName());
     }
 
     public void testHandleSearchByFullNameLowerCase() throws Exception {
         expectSearch("activity b", null, null);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 1, actual.size());
         assertEquals("Wrong search results", "Activity B", actual.get(0).getName());
     }
 
     public void testHandleSearchByPartialName() throws Exception {
         expectSearch("Activity", null, null);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 2, actual.size());
         assertEquals("Wrong search results", "Activity A", actual.get(0).getName());
         assertEquals("Wrong search results", "Activity B", actual.get(1).getName());
     }
 
     public void testHandleSearchByCode() throws Exception {
         expectSearch("AA", null, null);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 1, actual.size());
         assertEquals("Wrong search results", "AA", actual.get(0).getCode());
     }
 
     public void testHandleSearchByCodeLowerCase() throws Exception {
         expectSearch("bb", null, null);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 1, actual.size());
         assertEquals("Wrong search results", "BB", actual.get(0).getCode());
     }
 
     public void testHandleFilterBySource() throws Exception {
         expectSearch("Activity", source0, null);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 1, actual.size());
         assertEquals("Wrong search results", "Activity A", actual.get(0).getName());
     }
 
     public void testHandleFilterByActivityType() throws Exception {
         expectSearch("Activity", null, ActivityType.DISEASE_MEASURE);
 
         List<Activity> actual = (List<Activity>) getRefData("activities");
 
         assertEquals("Wrong search results size", 1, actual.size());
         assertEquals("Wrong search results", "Activity B", actual.get(0).getName());
     }
 
     public void testHandleGetIsError() throws Exception {
         request.setMethod("POST");
         replayMocks();
 
         assertNull(controller.handleRequest(request, response));
         verifyMocks();
 
         assertEquals("Wrong HTTP status code", HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Wrong error message", "POST is the only valid method for this URL", response.getErrorMessage());
     }
 
     private void expectSearch(String searchText, Source source, ActivityType activityType) {
         expect(command.getSearchText()).andReturn(searchText).anyTimes();
         expect(command.getSource()).andReturn(source);
         expect(command.getActivityType()).andReturn(activityType);
     }
 
     public void expectRefDataCalls() {
         expect(activityDao.getAll()).andReturn(activities);
     }
 
     public Object getRefData(String refdataKey) throws Exception{
         expectRefDataCalls();
         replayMocks();
         ModelAndView mv = controller.handleRequest(request, response);
         verifyMocks();
 
         return mv.getModel().get(refdataKey);
     }
 }
