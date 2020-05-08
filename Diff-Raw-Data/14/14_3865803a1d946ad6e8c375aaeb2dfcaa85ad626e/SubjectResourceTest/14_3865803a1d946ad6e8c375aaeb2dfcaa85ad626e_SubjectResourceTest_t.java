 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Gender;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.SubjectJsonRepresentation;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import gov.nih.nci.cabig.ctms.lang.DateTools;
 import org.easymock.EasyMock;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.ext.json.JsonRepresentation;
 
 import java.util.Arrays;
 import java.util.Calendar;
 
 import static org.easymock.classextension.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class SubjectResourceTest extends AuthorizedResourceTestCase<SubjectResource> {
     public static final String PERSON_ID = "XLC4";
 
     private Subject subject;
     private JSONObject subjectJson;
     private Site nu, tju;
 
     private SubjectDao subjectDao;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         subjectDao = registerDaoMockFor(SubjectDao.class);
 
         subject = Fixtures.createSubject(PERSON_ID, "Jo", "Carlson",
             DateTools.createDate(1963, Calendar.MARCH, 17), Gender.FEMALE);
         subject.setGridId("Eleventy");
         expect(subjectDao.getByGridIdOrPersonId(PERSON_ID)).andStubReturn(subject);
         UriTemplateParameters.SUBJECT_IDENTIFIER.putIn(request, PERSON_ID);
 
         subjectJson = new JSONObject(
             new MapBuilder<String, Object>().
                 put("first_name", "Jo").
                 put("last_name", "Carlson").
                 put("birth_date", "1962-03-17").
                 put("person_id", PERSON_ID).
                 put("gender", "Female").
                 toMap()
         );
 
         nu = Fixtures.createSite("NU", "IL702");
         tju = Fixtures.createSite("TJU", "PA036");
         Study study = Fixtures.createNamedInstance("Foo", Study.class);
         Fixtures.createAssignment(study, nu, subject);
         expect(subjectDao.getSiteParticipation(subject)).andStubReturn(Arrays.asList(nu));
 
         setCurrentUser(new PscUserBuilder().add(PscRole.SUBJECT_MANAGER).forAllSites().toUser());
     }
 
     @Override
     protected SubjectResource createAuthorizedResource() {
         SubjectResource sr = new SubjectResource();
         sr.setSubjectDao(subjectDao);
         return sr;
     }
 
     public void testGetAndPutAllowed() throws Exception {
         assertAllowedMethods("GET", "PUT");
     }
 
     ////// AUTHORIZATION
 
     public void testGetAllowedForDataReaderAndSubjectManager() throws Exception {
         replayMocks();
         assertRolesAllowedForMethod(Method.GET,
             PscRole.DATA_READER, PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.SUBJECT_MANAGER);
     }
 
     public void testPutAllowedForSubjectManager() throws Exception {
         replayMocks();
         assertRolesAllowedForMethod(Method.PUT, PscRole.SUBJECT_MANAGER);
     }
 
     public void testGetAllowedForDataReaderFromAllSites() throws Exception {
         assertStatusForAuthorization(
             PscRole.DATA_READER, Method.GET, Status.SUCCESS_OK);
     }
 
     public void testGetAllowedForDataReaderFromParticipationSite() throws Exception {
         assertStatusForAuthorization(
             PscRole.DATA_READER, nu, Method.GET, Status.SUCCESS_OK);
     }
 
     public void testGetNotAllowedForDataReaderFromOtherSite() throws Exception {
         assertStatusForAuthorization(
             PscRole.DATA_READER, tju, Method.GET, Status.CLIENT_ERROR_FORBIDDEN);
     }
 
     public void testGetAllowedForSubjectManagerFromAllSites() throws Exception {
         assertStatusForAuthorization(
             PscRole.SUBJECT_MANAGER, Method.GET, Status.SUCCESS_OK);
     }
 
     public void testGetAllowedForSubjectManagerFromParticipationSite() throws Exception {
         assertStatusForAuthorization(
             PscRole.SUBJECT_MANAGER, nu, Method.GET, Status.SUCCESS_OK);
     }
 
     public void testGetNotAllowedForSubjectManagerFromOtherSite() throws Exception {
         assertStatusForAuthorization(
             PscRole.SUBJECT_MANAGER, tju, Method.GET, Status.CLIENT_ERROR_FORBIDDEN);
     }
 
     public void testGetAllowedForStudySubjectCalendarManagerFromAllSites() throws Exception {
         assertStatusForAuthorization(
             PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, Method.GET, Status.SUCCESS_OK);
     }
 
     public void testGetAllowedForStudySubjectCalendarManagerFromParticipationSite() throws Exception {
         assertStatusForAuthorization(
             PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, nu, Method.GET, Status.SUCCESS_OK);
     }
 
     public void testGetNotAllowedForStudySubjectCalendarManagerFromOtherSite() throws Exception {
         assertStatusForAuthorization(
             PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, tju, Method.GET, Status.CLIENT_ERROR_FORBIDDEN);
     }
 
     public void testGetNotAllowedForOtherRoles() throws Exception {
         assertStatusForAuthorization(
             PscRole.STUDY_QA_MANAGER, Method.GET, Status.CLIENT_ERROR_FORBIDDEN);
     }
 
     public void testPutAllowedForSubjectManagerFromAllSites() throws Exception {
         request.setEntity(new SubjectJsonRepresentation(subject, request.getRootRef()));
         /* expect */ subjectDao.save(subject);
         assertStatusForAuthorization(
             PscRole.SUBJECT_MANAGER, Method.PUT, Status.SUCCESS_OK);
     }
 
     public void testPutAllowedForSubjectManagerFromParticipationSite() throws Exception {
         request.setEntity(new SubjectJsonRepresentation(subject, request.getRootRef()));
         /* expect */ subjectDao.save(subject);
         assertStatusForAuthorization(
             PscRole.SUBJECT_MANAGER, nu, Method.PUT, Status.SUCCESS_OK);
     }
 
     public void testPutNotAllowedForSubjectManagerFromOtherSite() throws Exception {
         request.setEntity(new SubjectJsonRepresentation(subject, request.getRootRef()));
         assertStatusForAuthorization(
             PscRole.SUBJECT_MANAGER, tju, Method.PUT, Status.CLIENT_ERROR_FORBIDDEN);
     }
 
     public void testPutNotAllowedForOtherRoles() throws Exception {
         request.setEntity(new SubjectJsonRepresentation(subject, request.getRootRef()));
         assertStatusForAuthorization(
             PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, Method.PUT, Status.CLIENT_ERROR_FORBIDDEN);
     }
 
     private void assertStatusForAuthorization(PscRole role, Method method, Status expectedStatus) {
         assertStatusForAuthorization(role, null, method, expectedStatus);
     }
 
     private void assertStatusForAuthorization(PscRole role, Site site, Method method, Status expectedStatus) {
         PscUserBuilder pub = new PscUserBuilder().add(role);
         if (site == null) {
             pub.forAllSites();
         } else {
             pub.forSites(site);
         }
         if (role.isStudyScoped()) {
             pub.forAllStudies();
         }
         setCurrentUser(pub.toUser());
 
         doRequest(method);
         assertResponseStatus(expectedStatus);
     }
 
     ////// GET
 
    public void testGet404sWithUnknownIdent() throws Exception {
         expect(subjectDao.getByGridIdOrPersonId(PERSON_ID)).andReturn(null);
 
         doGet();
        assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND);
     }
 
     public void testGetReturnsSubjectRepresentation() throws Exception {
         doGet();
 
         assertTrue(response.getEntity() instanceof SubjectJsonRepresentation);
         SubjectJsonRepresentation entity = (SubjectJsonRepresentation) response.getEntity();
         assertSame("Wrong subject", subject, entity.getSubject());
         assertNotNull("Test setup failure", request.getRootRef());
         assertEquals(request.getRootRef(), entity.getRootRef());
     }
 
     ////// PUT
 
     public void testPutUpdatesTheFirstNameInPlace() throws Exception {
         subjectJson.put("first_name", "P.");
         expectReceiveSubjectJsonAndSave();
 
         assertEquals("P.", subject.getFirstName());
     }
 
     public void testPutUpdatesTheLastNameInPlace() throws Exception {
         subjectJson.put("last_name", "Carlsson");
         expectReceiveSubjectJsonAndSave();
 
         assertEquals("Carlsson", subject.getLastName());
     }
 
     public void testPutUpdatesThePersonIdInPlace() throws Exception {
         subjectJson.put("person_id", "MR2001");
         expectReceiveSubjectJsonAndSave();
 
         assertEquals("MR2001", subject.getPersonId());
     }
 
     public void testPutUpdatesTheBirthDateInPlace() throws Exception {
         subjectJson.put("birth_date", "1962-10-14");
         expectReceiveSubjectJsonAndSave();
 
         assertDayOfDate(1962, Calendar.OCTOBER, 14, subject.getDateOfBirth());
     }
 
     public void testPutUpdatesTheGenderInPlace() throws Exception {
         subjectJson.put("gender", "Not reported");
         expectReceiveSubjectJsonAndSave();
 
         assertEquals(Gender.NOT_REPORTED, subject.getGender());
     }
 
     public void testPutUpdatesPropertiesInPlace() throws Exception {
         subjectJson.put("properties",
             new JSONArray("[ { 'name': 'Nickname', 'value': 'Josephine' } ]"));
         expectReceiveSubjectJsonAndSave();
 
         assertEquals("Wrong number of properties", 1, subject.getProperties().size());
         assertEquals("Wrong property name",  "Nickname",  subject.getProperties().get(0).getName());
         assertEquals("Wrong property value", "Josephine", subject.getProperties().get(0).getValue());
     }
 
     public void testUpdateLeavesGridIdAlone() throws Exception {
         expectReceiveSubjectJsonAndSave();
 
         assertEquals("Eleventy", subject.getGridId());
     }
 
     private void expectReceiveSubjectJsonAndSave() {
         request.setEntity(new JsonRepresentation(subjectJson));
         /* expect */ subjectDao.save(EasyMock.same(subject));
         doPut();
     }
 
     public void testPut400sForNewSubject() throws Exception {
         expect(subjectDao.getByGridIdOrPersonId(PERSON_ID)).andReturn(null);
         request.setEntity(new JsonRepresentation(
             new MapBuilder<String, Object>().
                 put("first_name", "Betty").put("person_id", PERSON_ID).
                 toMap()
         ));
 
         doPut();
 
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Update only");
         assertEquals(
             "This resource can not create new subjects. New subjects may only be created during registration.",
             request.getAttributes().get(PscStatusService.CLIENT_ERROR_REASON_KEY));
     }
 }
