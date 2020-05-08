 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
 import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
 import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.StudySubjectAssignmentXmlSerializer;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static org.easymock.classextension.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class ManagedSchedulesResourceTest extends AuthorizedResourceTestCase<ManagedSchedulesResource> {
     private PscUserService pscUserService;
    private StudySubjectAssignmentXmlSerializer ssaXmlSerializer;
 
     private PscUser jo, alice;
     private StudySubjectAssignment a_nu_1;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         pscUserService = registerMockFor(PscUserService.class);
        ssaXmlSerializer = registerNiceMockFor(StudySubjectAssignmentXmlSerializer.class);
 
         Site nu = createSite("NU", "IL036");
         Study a = createBasicTemplate("A");
         StudySite a_nu = createStudySite(a, nu);
         a_nu.approveAmendment(a.getAmendment(), new Date());
         a_nu_1 = createAssignment(a_nu, createSubject("First", "One"));
 
         StudySegment ae0s1 = a.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
         ScheduledStudySegment scheduledStudySegment = createScheduledStudySegment(ae0s1);
         a_nu_1.getScheduledCalendar().addStudySegment(scheduledStudySegment);
 
         jo = new PscUserBuilder("jo").
             add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().
             toUser();
         alice = new PscUserBuilder("alice").
             add(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().
             toUser();
 
         setCurrentUser(alice);
         UriTemplateParameters.USERNAME.putIn(request, "jo");
         expect(pscUserService.getAuthorizableUser("jo")).andStubReturn(jo);
         expect(pscUserService.getManagedAssignments(jo, alice)).
             andStubReturn(createExpectedUssars(jo, a_nu_1));
     }
 
     @Override
     protected ManagedSchedulesResource createAuthorizedResource() {
         ManagedSchedulesResource res = new ManagedSchedulesResource();
         res.setPscUserService(pscUserService);
        res.setXmlSerializer(ssaXmlSerializer);
         res.setTemplateService(new TestingTemplateService());
         StaticNowFactory nf = new StaticNowFactory();
         nf.setNowTimestamp(DateTools.createTimestamp(2008, Calendar.MAY, 5));
         res.setNowFactory(nf);
         return res;
     }
 
     public void testAllowsGetOnly() throws Exception {
         assertAllowedMethods("GET");
     }
 
     public void testAuthorizedForStudyTeams() throws Exception {
         assertRolesAllowedForMethod(Method.GET,
             PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.DATA_READER,
             PscRole.STUDY_TEAM_ADMINISTRATOR);
     }
 
     public void testRenderXml() throws Exception {
         setAccept(MediaType.TEXT_XML);
        expect(ssaXmlSerializer.createDocumentString(Arrays.asList(a_nu_1))).
             andReturn(MOCK_XML);
 
         doGet();
         assertResponseIsCreatedXml();
     }
 
     public void testRenderICS() throws Exception {
         setAccept(MediaType.TEXT_CALENDAR);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertEquals("Wrong response type",
             MediaType.TEXT_CALENDAR, response.getEntity().getMediaType());
     }
 
     public void testRenderJSON() throws Exception {
         setAccept(MediaType.APPLICATION_JSON);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertEquals("Wrong response type",
             MediaType.APPLICATION_JSON, response.getEntity().getMediaType());
     }
 
     public void test404IfNoAssignments() throws Exception {
         expectManagedAssignments(jo, alice);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     private void expectManagedAssignments(PscUser manager, PscUser viewer, StudySubjectAssignment... assignments) {
         expect(pscUserService.getManagedAssignments(manager, viewer)).
             andReturn(createExpectedUssars(viewer, assignments));
     }
 
     private List<UserStudySubjectAssignmentRelationship> createExpectedUssars(
         PscUser viewer, StudySubjectAssignment... assignments
     ) {
         List<UserStudySubjectAssignmentRelationship> expected =
             new ArrayList<UserStudySubjectAssignmentRelationship>();
         for (StudySubjectAssignment assignment : assignments) {
             expected.add(new UserStudySubjectAssignmentRelationship(viewer, assignment));
         }
         return expected;
     }
 }
