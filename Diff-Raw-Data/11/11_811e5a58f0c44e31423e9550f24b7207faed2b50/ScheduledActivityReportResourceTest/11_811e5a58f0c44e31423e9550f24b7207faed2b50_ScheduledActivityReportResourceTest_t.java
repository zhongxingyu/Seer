 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.service.ReportService;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Preference;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 
 import java.beans.PropertyDescriptor;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createActivityType;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static org.easymock.EasyMock.*;
 
 /**
  * @author Nataliya Shurupova
  */
 public class ScheduledActivityReportResourceTest extends AuthorizedResourceTestCase<ScheduledActivityReportResource> {
     private static final Collection<String> FILTER_PROPERTIES = new HashSet<String>();
     static {
         BeanWrapperImpl filter = new BeanWrapperImpl(new ScheduledActivitiesReportFilters());
         for (PropertyDescriptor descriptor : filter.getPropertyDescriptors()) {
             if (descriptor.getWriteMethod() != null) {
                 FILTER_PROPERTIES.add(descriptor.getName());
             }
         }
         FILTER_PROPERTIES.remove("actualActivityDate");
         FILTER_PROPERTIES.add("actualActivityDate.start");
         FILTER_PROPERTIES.add("actualActivityDate.stop");
     }
 
     private ActivityTypeDao activityTypeDao;
     private ReportService reportService;
     private AuthorizationManager csmAuthorizationManager;
 
     private List<ScheduledActivitiesReportRow> rows;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         reportService = registerMockFor(ReportService.class);
         activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
         csmAuthorizationManager = registerMockFor(AuthorizationManager.class);
 
         ScheduledActivitiesReportRow row1 = new ScheduledActivitiesReportRow(); row1.setId(1001);
         ScheduledActivitiesReportRow row2 = new ScheduledActivitiesReportRow(); row2.setId(1002);
         rows = Arrays.asList(row1, row2);
 
         SecurityContextHolderTestHelper.setUserAndReturnMembership("jo", PscRole.DATA_READER);
     }
 
     @Override
     @SuppressWarnings({ "unchecked" })
     protected ScheduledActivityReportResource createAuthorizedResource() {
         ScheduledActivityReportResource resource = new ScheduledActivityReportResource();
         resource.setActivityTypeDao(activityTypeDao);
         resource.setReportService(reportService);
         resource.setCsmAuthorizationManager(csmAuthorizationManager);
         return resource;
     }
 
     public void testGetAllowed() throws Exception {
         assertAllowedMethods("GET");
     }
 
     public void testGetWithAuthorizedRoles() {
         assertRolesAllowedForMethod(Method.GET,
             STUDY_SUBJECT_CALENDAR_MANAGER,
             STUDY_TEAM_ADMINISTRATOR,
             DATA_READER);
     }
 
     public void test200ForSupportedCSVMediaType() throws Exception {
         FilterParameters.STATE.putIn(request, "NA");
         expect(reportService.searchScheduledActivities((ScheduledActivitiesReportFilters) notNull())).
             andReturn(rows);
         makeRequestType(PscMetadataService.TEXT_CSV);
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
     }
 
     public void test200ForSupportedJSONMediaType() throws Exception {
         FilterParameters.STATE.putIn(request, "NA");
         expect(reportService.searchScheduledActivities((ScheduledActivitiesReportFilters) notNull())).
             andReturn(rows);
         makeRequestType(MediaType.APPLICATION_JSON);
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
     }
 
     // TODO: need tests for parse failures
     public void testGetFilterForRangeOfDates() throws Exception {
         FilterParameters.START_DATE.putIn(request, "2010-03-01");
         FilterParameters.END_DATE.putIn(request, "2010-03-08");
         ScheduledActivitiesReportFilters actualFilter = getResource().buildFilters();
         assertNotNull("No filters built", actualFilter);
         assertNotNull("Filter doesn't contain the actual activity date range", actualFilter.getActualActivityDate());
         assertDayOfDate("Incorrect start date", 2010, Calendar.MARCH, 1, actualFilter.getActualActivityDate().getStart());
         assertDayOfDate("Incorrect stop date", 2010, Calendar.MARCH, 8, actualFilter.getActualActivityDate().getStop());
     }
 
     public void testGetFilterForEndDate() throws Exception {
         FilterParameters.END_DATE.putIn(request, "2010-03-08");
         assertOnlyFilterIs("actualActivityDate.stop", DateTools.createDate(2010, Calendar.MARCH, 8, 0, 0, 0));
     }
 
     public void testGetFilterForStartDate() throws Exception {
         FilterParameters.START_DATE.putIn(request, "2010-03-01");
         assertOnlyFilterIs("actualActivityDate.start", DateTools.createDate(2010, Calendar.MARCH, 1, 0, 0, 0));
     }
 
     public void testGetFilterForLabel() throws Exception {
         FilterParameters.LABEL.putIn(request, "labelA");
         assertOnlyFilterIs("label", "labelA");
     }
 
     public void testGetFilterForResponsibleUser() throws Exception {
         User expectedUser = AuthorizationObjectFactory.createCsmUser("josephine");
         FilterParameters.RESPONSIBLE_USER.putIn(request, "josephine");
         expect(csmAuthorizationManager.getUser("josephine")).andReturn(expectedUser);
 
         replayMocks();
         assertOnlyFilterIs("responsibleUser", expectedUser);
         verifyMocks();
     }
 
     public void testGetFilterForResponsibleUserWhenUnknown() throws Exception {
         FilterParameters.RESPONSIBLE_USER.putIn(request, "josephine");
         expect(csmAuthorizationManager.getUser("josephine")).andReturn(null);
         replayMocks();
 
         try {
             getResource().buildFilters();
             fail("Resource exception not thrown");
         } catch (ResourceException re) {
             assertEquals("Wrong HTTP error code", 422, re.getStatus().getCode());
             assertEquals("Wrong message",
                "Unknown user for responsible-user filter: josephine",
                re.getStatus().getDescription());
         }
     }
 
     public void testGetFilterForCurrentState() throws Exception {
         FilterParameters.STATE.putIn(request, "SchEDulEd");
         assertOnlyFilterIs("currentStateMode", ScheduledActivityMode.SCHEDULED);
     }
 
     public void testGetFilterForCurrentStateWhenNotFound() throws Exception {
         try {
             FilterParameters.STATE.putIn(request, "Sparkly");
             getResource().buildFilters();
             fail("Exception not thrown");
         } catch (ResourceException re) {
             assertEquals("Wrong HTTP error code", 422, re.getStatus().getCode());
             assertEquals("Wrong message",
                 "Invalid scheduled activity state name for state filter: Sparkly",
                 re.getStatus().getDescription());
         }
     }
 
     public void testGetFilterForStudyAssignedIdentifier() throws Exception {
         FilterParameters.STUDY.putIn(request, "EG 1701");
         assertOnlyFilterIs("studyAssignedIdentifier", "EG 1701");
     }
 
     public void testGetFilterForSiteAssignedIdendtifier() throws Exception {
         FilterParameters.SITE.putIn(request, "Belgium, man");
         assertOnlyFilterIs("siteName", "Belgium, man");
     }
 
     public void testGetFilterForActivityType() throws Exception {
         // TODO: this is very wrong (#1143)
         ActivityType type = Fixtures.setId(111, createActivityType("Measure"));
         FilterParameters.ACTIVITY_TYPE.putIn(request, "111");
         expect(activityTypeDao.getById(type.getId())).andReturn(type);
         replayMocks();
         assertOnlyFilterIs("activityType", type);
         verifyMocks();
     }
 
     private void assertOnlyFilterIs(String filterProperty, Object expectedValue) throws ResourceException {
         BeanWrapper filterBean = new BeanWrapperImpl(getResource().buildFilters());
         assertEquals("Filter property " + filterProperty + " not set to expected value",
             expectedValue, filterBean.getPropertyValue(filterProperty));
         for (String otherProperty : FILTER_PROPERTIES) {
             if (!filterProperty.equals(otherProperty)) {
                 assertNull("Expected filter property " + otherProperty + " to be null",
                     filterBean.getPropertyValue(otherProperty));
             }
         }
     }
 
     public void testGetJSONRepresentation() throws Exception {
         FilterParameters.STATE.putIn(request, "NA");
 
         expect(reportService.searchScheduledActivities((ScheduledActivitiesReportFilters) notNull())).
             andReturn(rows);
 
         makeRequestType(MediaType.APPLICATION_JSON);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertEquals("Result is not of right content type",
             MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
     }
 
     private void makeRequestType(MediaType requestType) {
         request.getClientInfo().setAcceptedMediaTypes(Arrays.asList(new Preference<MediaType>(requestType)));
     }
 }
