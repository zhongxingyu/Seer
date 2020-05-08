 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import org.restlet.data.Status;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.createSuiteRoleMembership;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Jalpa Patel
  */
 public class UserRoleResourceTest extends AuthorizedResourceTestCase<UserRoleResource> {
     private PscUserService pscUserService;
     private static final String USERNAME = "TestUser";
     private SuiteRoleMembership mem;
     private PscUser user;
 
     public void setUp() throws Exception {
         super.setUp();
         pscUserService = registerMockFor(PscUserService.class);
         user = createPscUser(USERNAME);
         request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), USERNAME);
     }
 
     @Override
     @SuppressWarnings({ "unchecked" })
     protected UserRoleResource createAuthorizedResource() {
         UserRoleResource resource = new UserRoleResource();
         resource.setPscUserService(pscUserService);
         resource.setXmlSerializer(xmlSerializer);
         return resource;
     }
     
     public void testGetAllowed() throws Exception {
         assertAllowedMethods("GET");
     }
 
     public void test400ForNoUserName() throws Exception {
         request.getAttributes().put(UriTemplateParameters.USERNAME.attributeName(), null);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No user name in request");
     }
 
     public void test400ForNoRoleName() throws Exception {
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), null);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No role name in request");
     }
 
     public void test400ForUnknownUser() throws Exception {
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), PscRole.STUDY_CREATOR.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(null);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user TestUser");
     }
 
     public void testGetUserRoleForUser() throws Exception {
         setCurrentUser(createPscUser(USERNAME,PscRole.STUDY_CREATOR));
         mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR);
         user.getMemberships().put(SuiteRole.STUDY_CREATOR, mem);
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), PscRole.STUDY_CREATOR.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
         expect(xmlSerializer.createDocumentString(mem)).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
     public void testGetUserRoleForUserAdmin() throws Exception {
         setCurrentUser(createPscUser("userAdmin",PscRole.USER_ADMINISTRATOR));
         mem = createSuiteRoleMembership(PscRole.STUDY_CREATOR);
         user.getMemberships().put(SuiteRole.STUDY_CREATOR, mem);
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), PscRole.STUDY_CREATOR.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
         expect(xmlSerializer.createDocumentString(mem)).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
     public void testGetUserRoleForSystemAdminWhenUserIsUserAdmin() throws Exception {
         setCurrentUser(createPscUser("systemAdmin",PscRole.SYSTEM_ADMINISTRATOR));
         mem = createSuiteRoleMembership(PscRole.USER_ADMINISTRATOR);
         user.getMemberships().put(SuiteRole.USER_ADMINISTRATOR, mem);
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), PscRole.USER_ADMINISTRATOR.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
         expect(xmlSerializer.createDocumentString(mem)).andReturn(MOCK_XML);
 
         doGet();
         assertResponseStatus(Status.SUCCESS_OK);
         assertResponseIsCreatedXml();
     }
 
     public void test404ForNonExistentRoleForUser() throws Exception {
         setCurrentUser(createPscUser(USERNAME,PscRole.STUDY_CREATOR));
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(),
                 PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND,
                 "The server has not found anything matching the request URI");
     }
 
     public void test404ForNonExistentRoleForUserAdmin() throws Exception {
         setCurrentUser(createPscUser("userAdmin",PscRole.USER_ADMINISTRATOR));
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(),
                 PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_NOT_FOUND,
                 "The server has not found anything matching the request URI");
     }
 
     public void testGetUserRoleForSystemAdminWhenUserIsNotUserAdmin() throws Exception {
         setCurrentUser(createPscUser("systemAdmin",PscRole.SYSTEM_ADMINISTRATOR));
         user.getMemberships().put(SuiteRole.STUDY_CREATOR, mem);
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(), PscRole.STUDY_CREATOR.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN, "systemAdmin has insufficient privilege");
     }
 
     public void test403ForUnauthorisedUser() throws Exception {
         setCurrentUser(createPscUser("otherUser",PscRole.STUDY_CREATOR));
         request.getAttributes().put(UriTemplateParameters.ROLENAME.attributeName(),
                 PscRole.STUDY_CREATOR.getDisplayName());
         expect(pscUserService.loadUserByUsername(USERNAME)).andReturn(user);
 
         doGet();
         assertResponseStatus(Status.CLIENT_ERROR_FORBIDDEN, "otherUser has insufficient privilege");
     }
 }
