 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
 import edu.northwestern.bioinformatics.studycalendar.domain.*;
 import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
 import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
 import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import org.easymock.IArgumentMatcher;
 import static org.easymock.classextension.EasyMock.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.MapBindingResult;
 import org.springframework.validation.ObjectError;
 
 import java.util.*;
 
 public class CreateUserCommandTest extends StudyCalendarTestCase {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private Study study;
     private Site mayo, nu;
     private StudySite mayoSS, nuSS;
 
     private UserService userService;
     private SiteDao siteDao;
     private UserRoleService userRoleService;
     private UserDao userDao;
     private Errors errors;
     private AuthenticationSystemConfiguration authenticationSystemConfiguration;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         mayo = setId(1, createNamedInstance("Mayo Clinic", Site.class));
         nu = setId(2, createNamedInstance("Northwestern Clinic", Site.class));
         study = createNamedInstance("Study A", Study.class);
         mayoSS = createStudySite(study, mayo);
         nuSS = createStudySite(study, nu);
 
         siteDao = new SiteDaoStub(Arrays.asList(mayo, nu));
         userDao = registerDaoMockFor(UserDao.class);
         userService = registerMockFor(UserService.class);
         userRoleService = registerMockFor(UserRoleService.class);
         authenticationSystemConfiguration = registerMockFor(AuthenticationSystemConfiguration.class);
         expect(authenticationSystemConfiguration.isLocalAuthenticationSystem()).andReturn(true).anyTimes();
 
         errors = new MapBindingResult(new HashMap(), "?");
     }
 
     public void testBuildRolesGrid() throws Exception {
         User expectedUser = createUser(-1, "Joe", -1L, true, STUDY_ADMIN);
         expectedCsmUser(expectedUser);
         replayMocks();
         CreateUserCommand command = createCommand(expectedUser);
         verifyMocks();
         Map<Site, Map<Role, CreateUserCommand.RoleCell>> rolesGrid = command.getRolesGrid();
         assertEquals("Roles grid has wrong number of sites", 2, rolesGrid.size());
         assertEquals("Wrong number checked for site specific Role", values().length, rolesGrid.get(mayo).size());
         assertTrue("Role should be true for all sites", rolesGrid.get(mayo).get(STUDY_ADMIN).isSelected());
         assertTrue("Role should be true for all sites", rolesGrid.get(nu).get(STUDY_ADMIN).isSelected());
         assertFalse("Role should not be true for this site", rolesGrid.get(mayo).get(SUBJECT_COORDINATOR).isSelected());
     }
 
     private void expectedCsmUser(final User expectedUser) {
         gov.nih.nci.security.authorization.domainobjects.User csmUser = new gov.nih.nci.security.authorization.domainobjects.User();
         csmUser.setEmailId("user@email.com");
         expect(userService.getCsmUserByCsmUserId(expectedUser.getId())).andReturn(csmUser);
     }
 
     public void testInterpretRolesGrid() throws Exception {
         User expectedUser = createUser(-1, "Joe", -1L, true);
         expectedCsmUser(expectedUser);
         List<UserRole> expectedUserRoles = Arrays.asList(
                 createUserRole(expectedUser, STUDY_ADMIN),
                 createUserRole(expectedUser, SUBJECT_COORDINATOR, mayo, nu)
         );
         expectedUser.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
 
         userRoleService.assignUserRole(expectedUser, SUBJECT_COORDINATOR, mayo);
         userRoleService.assignUserRole(expectedUser, SUBJECT_COORDINATOR, nu);
 
         userRoleService.removeUserRoleAssignment(expectedUser, SITE_COORDINATOR, mayo);
         userRoleService.removeUserRoleAssignment(expectedUser, SITE_COORDINATOR, nu);
 
         userRoleService.assignUserRole(expectedUser, STUDY_ADMIN);
         userRoleService.removeUserRoleAssignment(expectedUser, STUDY_COORDINATOR);
         userRoleService.removeUserRoleAssignment(expectedUser, SYSTEM_ADMINISTRATOR);
 
         replayMocks();
 
         CreateUserCommand command = createCommand(expectedUser);
         expectedUser.setUserRoles(Collections.<UserRole>emptySet());        // empty user roles so we can test the assign method
         command.assignUserRolesFromRolesGrid();
         verifyMocks();
     }
 
     public void testInterpretRolesGridRemoveRole() throws Exception {
         User expectedUser = createUser(-1, "Joe", -1L, true);
 
         List<UserRole> expectedUserRoles = Arrays.asList(
                 createUserRole(expectedUser, STUDY_ADMIN),
                 createUserRole(expectedUser, SUBJECT_COORDINATOR, mayo, nu)
         );
         expectedUser.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
 
         userRoleService.assignUserRole(expectedUser, SUBJECT_COORDINATOR, mayo);
         userRoleService.assignUserRole(expectedUser, SUBJECT_COORDINATOR, nu);
 
         userRoleService.removeUserRoleAssignment(expectedUser, SITE_COORDINATOR, mayo);
         userRoleService.removeUserRoleAssignment(expectedUser, SITE_COORDINATOR, nu);
 
         userRoleService.removeUserRoleAssignment(expectedUser, STUDY_ADMIN);
         userRoleService.removeUserRoleAssignment(expectedUser, STUDY_COORDINATOR);
         userRoleService.removeUserRoleAssignment(expectedUser, SYSTEM_ADMINISTRATOR);
         expectedCsmUser(expectedUser);
 
         replayMocks();
 
         CreateUserCommand command = createCommand(expectedUser);
         command.getRolesGrid().get(mayo).get(STUDY_ADMIN).setSelected(false);
         command.assignUserRolesFromRolesGrid();
         verifyMocks();
     }
 
     public void testInterpretRolesGridAddRole() throws Exception {
         User expectedUser = createUser(-1, "Joe", -1L, true);
 
         List<UserRole> expectedUserRoles = Arrays.asList(
                 createUserRole(expectedUser, SUBJECT_COORDINATOR, mayo, nu)
         );
         expectedUser.setUserRoles(new HashSet<UserRole>(expectedUserRoles));
 
         userRoleService.assignUserRole(expectedUser, SUBJECT_COORDINATOR, mayo);
         userRoleService.assignUserRole(expectedUser, SUBJECT_COORDINATOR, nu);
 
         userRoleService.removeUserRoleAssignment(expectedUser, SITE_COORDINATOR, mayo);
         userRoleService.removeUserRoleAssignment(expectedUser, SITE_COORDINATOR, nu);
 
         userRoleService.assignUserRole(expectedUser, STUDY_ADMIN);
         userRoleService.removeUserRoleAssignment(expectedUser, STUDY_COORDINATOR);
         userRoleService.removeUserRoleAssignment(expectedUser, SYSTEM_ADMINISTRATOR);
         expectedCsmUser(expectedUser);
 
         replayMocks();
 
         CreateUserCommand command = createCommand(expectedUser);
         command.getRolesGrid().get(mayo).get(STUDY_ADMIN).setSelected(true);
         command.assignUserRolesFromRolesGrid();
         verifyMocks();
     }
 
     public void testUserDefaultsToNewWhenNotSet() throws Exception {
         CreateUserCommand command = createCommand(null);
         assertNotNull(command.getUser());
         assertNull(command.getUser().getId());
         assertNull(command.getUser().getName());
     }
 
     public void testValidateRejectsRemovalOfLastSiteCoordinatorForSite() throws Exception {
         User lastSiteCoord = setId(3, createUser("jimbo", SITE_COORDINATOR));
         expectedCsmUser(lastSiteCoord);
         replayMocks();
         CreateUserCommand command = createCommand(lastSiteCoord);
        resetMocks();
         lastSiteCoord.getUserRole(SITE_COORDINATOR).addSite(mayo);
 
         mayoSS.addStudySubjectAssignment(new StudySubjectAssignment());
 
         command.getRolesGrid().get(mayo).get(SITE_COORDINATOR).setSelected(false);
 
         expect(userDao.getSiteCoordinators(mayo)).andReturn(Arrays.asList(lastSiteCoord));
         replayMocks();
         command.validate(errors);
         verifyMocks();
 
         assertEquals("Wrong number of errors", 1, errors.getFieldErrorCount());
         FieldError actualError = (FieldError) errors.getFieldErrors().get(0);
         assertFieldError(actualError, "rolesGrid[1][SITE_COORDINATOR].selected",
                 "error.user.last-site-coordinator", "jimbo", "Mayo Clinic");
     }
 
     public void testValidateDoesNotRejectLastSiteCoordinatorForUnusedSite() throws Exception {
         User lastSiteCoord = setId(3, createUser("jimbo", SITE_COORDINATOR));
         lastSiteCoord.getUserRole(SITE_COORDINATOR).addSite(mayo);
         expectedCsmUser(lastSiteCoord);
 
         replayMocks();
 
         CreateUserCommand command = createCommand(lastSiteCoord);
         verifyMocks();
         command.getRolesGrid().get(mayo).get(SITE_COORDINATOR).setSelected(false);
 
 
         command.validate(errors);
         verifyMocks();
 
         assertEquals("Wrong number of errors: " + errors.getFieldErrors(), 0, errors.getFieldErrorCount());
     }
 
     public void testValidateDoesNotRequirePasswordForNonLocalAuthenticationSystem() throws Exception {
         reset(authenticationSystemConfiguration);
         expect(authenticationSystemConfiguration.isLocalAuthenticationSystem()).andReturn(false);
         User newUser = new User();
         newUser.setName("anything");
 
         CreateUserCommand command = createCommand(newUser);
         command.setEmailAddress("user@email.com");
 
         expect(userDao.getByName(newUser.getName())).andReturn(null);
 
         replayMocks();
 
         command.validate(errors);
 
         assertEquals("Wrong number of errors: " + errors.getFieldErrors(), 0, errors.getFieldErrorCount());
         verifyMocks();
     }
 
     public void testValidateEmailAddressIsMandatory() throws Exception {
         User newUser = new User();
         newUser.setName("anything");
 
         CreateUserCommand command = createCommand(newUser);
         command.setPassword("password");
         command.setRePassword("password");
         expect(userDao.getByName(newUser.getName())).andReturn(null);
 
         replayMocks();
 
         command.validate(errors);
 
         assertEquals("Wrong number of errors: " + errors.getFieldErrors(), 1, errors.getFieldErrorCount());
         verifyMocks();
     }
 
     public void testValidateEmailAddressforInvalidFormat() throws Exception {
         User newUser = new User();
         newUser.setName("anything");
 
         CreateUserCommand command = createCommand(newUser);
         command.setPassword("password");
         command.setRePassword("password");
         command.setEmailAddress("invalid email address");
         expect(userDao.getByName(newUser.getName())).andReturn(null);
 
         replayMocks();
 
         command.validate(errors);
 
         assertEquals("Wrong number of errors: " + errors.getFieldErrors(), 1, errors.getFieldErrorCount());
         verifyMocks();
     }
 
     public void testValidateEmailAddressforValidFormat() throws Exception {
         User newUser = new User();
         newUser.setName("anything");
         CreateUserCommand command = createCommand(newUser);
         command.setPassword("password");
         command.setRePassword("password");
         command.setEmailAddress("valid@email.com");
         expect(userDao.getByName(newUser.getName())).andReturn(null);
 
         replayMocks();
 
         command.validate(errors);
 
         assertEquals("Wrong number of errors: " + errors.getFieldErrors(), 0, errors.getFieldErrorCount());
         verifyMocks();
     }
 
     public void testRandomPasswordIsGeneratedForNewUserWithNonLocalAuthenticationSystem() throws Exception {
         // stub calls
         userRoleService.removeUserRoleAssignment((User) notNull(), (Role) notNull(), (Site) notNull());
         expectLastCall().anyTimes();
         userRoleService.removeUserRoleAssignment((User) notNull(), (Role) notNull());
         expectLastCall().anyTimes();
 
         reset(authenticationSystemConfiguration);
         expect(authenticationSystemConfiguration.isLocalAuthenticationSystem()).andReturn(false);
         User newUser = new User();
         expect(userService.saveUser(eq(newUser), randomPassword(), null)).andReturn(newUser);
 
         CreateUserCommand command = createCommand(newUser);
         replayMocks();
 
         command.apply();
         verifyMocks();
     }
 
     private String randomPassword() {
         reportMatcher(new IArgumentMatcher() {
             public boolean matches(Object object) {
                 String candidate = (String) object;
                 if (candidate == null) return false;
                 boolean lengthOkay = 16 <= candidate.length() && candidate.length() <= 32;
                 if (!lengthOkay) log.error("Length not in range [16, 32]");
                 boolean charsOkay = true;
                 for (int i = 0; i < candidate.length(); i++) {
                     boolean thisCharOkay = (' ' <= candidate.charAt(i) && candidate.charAt(i) <= '~');
                     if (!thisCharOkay) log.error("Character {} ({}) out of range", i, candidate.charAt(i));
                     charsOkay = charsOkay && thisCharOkay;
                 }
 
                 return lengthOkay && charsOkay;
             }
 
             public void appendTo(StringBuffer stringBuffer) {
                 stringBuffer.append("[random password]");
             }
         });
         return null;
     }
 
     private static void assertGlobalError(
             ObjectError actual, String expectedCode, Object... expectedArgs
     ) {
         assertEquals("Wrong code", expectedCode, actual.getCode());
         assertEquals("Wrong number of args", expectedArgs.length, actual.getArguments().length);
         for (int i = 0; i < expectedArgs.length; i++) {
             Object expectedParameter = expectedArgs[i];
             assertEquals("Wrong parameter " + i, expectedParameter, actual.getArguments()[i]);
         }
     }
 
     private static void assertFieldError(
             FieldError error, String expectedField, String expectedCode, Object... expectedArgs
     ) {
         assertEquals("Wrong field", expectedField, error.getField());
         assertGlobalError(error, expectedCode, expectedArgs);
     }
 
     private CreateUserCommand createCommand(User user) {
         return new CreateUserCommand(user, siteDao, userService, userDao, userRoleService, authenticationSystemConfiguration);
     }
 
     private static class SiteDaoStub extends SiteDao {
         private List<Site> all;
 
         public SiteDaoStub(List<Site> all) {
             this.all = all;
         }
 
         @Override
         public List<Site> getAll() {
             return all;
         }
 
         public void setAll(List<Site> all) {
             this.all = all;
         }
 
         @Override
         public int getCount() {
             return getAll().size();
         }
 
         ////// UNUSED STUB METHODS
 
         @Override
         public Site getByName(final String name) {
             throw new UnsupportedOperationException("getByName not implemented");
         }
 
         @Override
         public Site getByAssignedIdentifier(final String assignedIdentifier) {
             throw new UnsupportedOperationException("getByAssignedIdentifier not implemented");
         }
 
         @Override
         public Site getByGridId(String gridId) {
             throw new UnsupportedOperationException("getByGridId not implemented");
         }
 
         @Override
         public Site getByGridId(Site template) {
             throw new UnsupportedOperationException("getByGridId not implemented");
         }
 
         @Override
         public void save(Site site) {
             throw new UnsupportedOperationException("save not implemented");
         }
 
         @Override
         public Site getById(int i) {
             throw new UnsupportedOperationException("getById not implemented");
         }
     }
 }
