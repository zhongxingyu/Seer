 package edu.northwestern.bioinformatics.studycalendar.web.admin;
 
 import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
 import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
 import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
 import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
 import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
 import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Arrays;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
 import static org.easymock.EasyMock.*;
 
 /**
  * Tests for BaseUserProvisioningCommand.
  *
  * @author Rhett Sutphin
  */
 public class BaseUserProvisioningCommandTest extends WebTestCase {
     private BaseUserProvisioningCommand command;
 
     private PscUser pscUser;
     private Site austin, sanAntonio;
     private Study studyA, studyB, studyC;
 
     private ProvisioningSession pSession;
     private ProvisioningSessionFactory psFactory;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         pscUser = createPscUser("jo", 15L);
 
         pSession = registerMockFor(ProvisioningSession.class);
         psFactory = registerMockFor(ProvisioningSessionFactory.class);
         expect(psFactory.createSession(15L)).andStubReturn(pSession);
 
         austin = Fixtures.createSite("A", "i-a");
         sanAntonio = Fixtures.createSite("SA", "i-sa");
 
         studyA = Fixtures.createBasicTemplate("A");
         studyB = Fixtures.createBasicTemplate("B");
         studyC = Fixtures.createBasicTemplate("C");
 
         PscUser principal = new PscUserBuilder("zelda").setCsmUserId(99L).
             add(PscRole.USER_ADMINISTRATOR).forAllSites().toUser();
         SecurityContextHolderTestHelper.setSecurityContext(principal);
 
         command = create(pscUser);
     }
 
     private TestCommand create(PscUser user) {
         TestCommand command = new TestCommand(user, psFactory, applicationSecurityManager);
         command.setCanProvisionAllSites(true);
         command.setCanProvisionManagingAllStudies(true);
         command.setCanProvisionManagingAllStudies(true);
         command.setProvisionableRoles(SuiteRole.values());
         command.setProvisionableSites(Arrays.asList(austin, sanAntonio));
         command.setProvisionableManagedStudies(Arrays.asList(studyA, studyB));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyC, studyB));
         return command;
     }
 
     ////// configuration
 
     public void testSetProvisionableRolesSortsTheRoles() throws Exception {
         command.setProvisionableRoles(
             SuiteRole.AE_EXPEDITED_REPORT_REVIEWER,
             SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER,
             SuiteRole.USER_ADMINISTRATOR,
             SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER);
 
         assertEquals("Wrong number of roles", 4, command.getProvisionableRoles().size());
         assertEquals("Wrong 1st role", "study_calendar_template_builder",
             command.getProvisionableRoles().get(0).getKey());
         assertEquals("Wrong 2nd role", "study_subject_calendar_manager",
             command.getProvisionableRoles().get(1).getKey());
         assertEquals("Wrong 3rd role", "user_administrator",
             command.getProvisionableRoles().get(2).getKey());
         assertEquals("Wrong 4th role", "ae_expedited_report_reviewer",
             command.getProvisionableRoles().get(3).getKey());
     }
 
     ////// apply
 
     public void testApplyAppliesAddAllSiteScope() throws Exception {
         expectRoleChange("data_reader", "add", "site", "__ALL__");
 
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Membership not made for all sites", srm.isAllSites());
     }
 
     public void testApplyAppliesAddSingleSiteScope() throws Exception {
         expectRoleChange("data_reader", "add", "site", sanAntonio.getAssignedIdentifier());
 
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Membership not made for specified site",
             srm.getSiteIdentifiers().contains(sanAntonio.getAssignedIdentifier()));
     }
 
     public void testApplyAppliesAddAllStudyScope() throws Exception {
         expectRoleChange("data_reader", "add", "study", "__ALL__");
 
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Membership not made for all sites", srm.isAllStudies());
     }
 
     public void testApplyAppliesAddSingleStudyScope() throws Exception {
         expectRoleChange("data_reader", "add", "study", studyB.getAssignedIdentifier());
 
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Membership not made for specified study",
             srm.getStudyIdentifiers().contains(studyB.getAssignedIdentifier()));
     }
 
     public void testApplyAppliesAddGroupOnly() throws Exception {
         expectRoleChange("business_administrator", "add");
 
         expectGetAndReplaceMembership(SuiteRole.BUSINESS_ADMINISTRATOR);
         replayMocks();
 
         command.apply();
         verifyMocks();
     }
 
     public void testApplyRemoveGroupDeletesRole() throws Exception {
         expectRoleChange("system_administrator", "remove");
 
         /* expect */ pSession.deleteRole(SuiteRole.SYSTEM_ADMINISTRATOR);
         replayMocks();
 
         command.apply();
         verifyMocks();
     }
 
     public void testApplyAppliesRemoveSingleSiteScope() throws Exception {
         expectRoleChange("data_reader", "remove", "site", sanAntonio.getAssignedIdentifier());
 
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER).
             forSites(sanAntonio, austin);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertEquals("Removed site not removed", 1, srm.getSiteIdentifiers().size());
         assertEquals("Wrong site removed",
             austin.getAssignedIdentifier(), srm.getSiteIdentifiers().get(0));
     }
 
     public void testApplyAppliesRemoveSingleStudyScope() throws Exception {
         expectRoleChange("data_reader", "remove", "study", "C");
 
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER).
             forStudies(studyB, studyC);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertEquals("Removed study not removed", 1, srm.getStudyIdentifiers().size());
         assertEquals("Wrong studye removed",
             studyB.getAssignedIdentifier(), srm.getStudyIdentifiers().get(0));
     }
 
     public void testApplySetsStaleIfModifyingSelf() throws Exception {
         PscUser existingUser = createPscUser("zelda", 99L);
 
         TestCommand selfMod = create(existingUser);
         replayMocks();
 
         selfMod.apply();
         verifyMocks();
 
         assertTrue("Security context principal should be stale",
             applicationSecurityManager.getUser().isStale());
     }
 
     public void testApplyDoesNotSetStaleIfNotModifyingSelf() throws Exception {
         replayMocks();
 
         command.apply();
         verifyMocks();
 
         assertFalse("Security context principal should not be stale",
             applicationSecurityManager.getUser().isStale());
     }
 
     public void testApplyDoesNotSetStaleIfNoApplicationSecurityManager() throws Exception {
         TestCommand setup = new TestCommand(pscUser, psFactory, null);
         replayMocks();
 
         setup.apply();
         verifyMocks();
         // no exceptions
     }
 
     private SuiteRoleMembership expectGetAndReplaceMembership(SuiteRole expectedRole) {
         SuiteRoleMembership srm =
             AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.valueOf(expectedRole));
         expect(pSession.getProvisionableRoleMembership(expectedRole)).andReturn(srm);
         /* expect */ pSession.replaceRole(srm);
         return srm;
     }
 
     ////// authorization
 
     public void testRoleChangesForUnallowedUsersIgnored() throws Exception {
         expectRoleChange(command, "harvey", "system_administrator", "add", null, null);
         replayMocks(); // nothing expected
 
         command.apply();
         verifyMocks();
     }
 
     public void testRoleChangesForUnallowedRolesIgnored() throws Exception {
         command.setProvisionableRoles(SuiteRole.USER_ADMINISTRATOR);
 
         expectRoleChange(command, "system_administrator", "add", null, null);
         replayMocks(); // nothing expected
 
         command.apply();
         verifyMocks();
     }
 
     public void testSiteChangeWhenDisallowedIgnored() throws Exception {
         command.setProvisionableSites(Arrays.asList(sanAntonio));
 
         expectRoleChange(command, "data_reader", "add", "site", austin.getAssignedIdentifier());
         replayMocks(); // nothing expected
 
         command.apply();
         verifyMocks();
     }
 
     public void testAllSiteChangesWhenDisallowedIgnored() throws Exception {
         command.setCanProvisionAllSites(false);
 
         expectRoleChange(command, "data_reader", "add", "site", "__ALL__");
         replayMocks(); // nothing expected
 
         command.apply();
         verifyMocks();
     }
 
     public void testProvisioningAllowedForManagedStudyWithTemplateManagerRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "study_calendar_template_builder", "add", "study", "A");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertContains("Study not added", srm.getStudyIdentifiers(), "A");
     }
 
     public void testProvisioningNotAllowedForParticipatingStudyWithTemplateManagerRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "study_calendar_template_builder", "add", "study", "B");
         replayMocks();  // expect nothing
 
         command.apply();
         verifyMocks();
     }
 
     public void testProvisioningAllowedForParticipatingStudyWithParticipationRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "B");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertContains("Study not added", srm.getStudyIdentifiers(), "B");
     }
 
     public void testProvisioningNotAllowedForManagedStudyWithParticipationRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "A");
         replayMocks();  // expect nothing
 
         command.apply();
         verifyMocks();
     }
 
     public void testProvisioningAllowedForParticipatingStudyWithTemplateManagerAndParticipationRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "study_qa_manager", "add", "study", "B");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_QA_MANAGER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertContains("Study not added", srm.getStudyIdentifiers(), "B");
     }
 
     public void testProvisioningAllowedForManagedStudyWithTemplateManagerAndParticipationRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "study_qa_manager", "add", "study", "A");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_QA_MANAGER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertContains("Study not added", srm.getStudyIdentifiers(), "A");
     }
 
     public void testProvisioningNotAllowedForManagingStudyWithAccessoryRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "lab_data_user", "add", "study", "A");
         replayMocks(); // expect nothing
 
         command.apply();
         verifyMocks();
     }
 
     public void testProvisioningAllowedForParticipatingStudyWithAccessoryRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "lab_data_user", "add", "study", "B");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.LAB_DATA_USER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertContains("Study not added", srm.getStudyIdentifiers(), "B");
     }
 
     public void testProvisioningNotAllowedForManagingStudyWithGridServiceRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "registrar", "add", "study", "A");
         replayMocks(); // expect nothing
 
         command.apply();
         verifyMocks();
     }
 
     public void testProvisioningAllowedForParticipatingStudyWithGridServiceRole() throws Exception {
         command.setProvisionableManagedStudies(Arrays.asList(studyA));
         command.setProvisionableParticipatingStudies(Arrays.asList(studyB));
 
         expectRoleChange(command, "registrar", "add", "study", "B");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.REGISTRAR);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertContains("Study not added", srm.getStudyIdentifiers(), "B");
     }
 
     public void testAllStudiesProvisioningAllowedWithAllManagedPermissionAndTemplateManagerRole() throws Exception {
         command.setCanProvisionManagingAllStudies(true);
         command.setCanProvisionParticipateInAllStudies(false);
 
         expectRoleChange(command, "study_calendar_template_builder", "add", "study", "__ALL__");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Not added", srm.isAllStudies());
     }
 
     public void testAllStudiesProvisioningNotAllowedWithAllParticipatingPermissionAndTemplateManagerRole() throws Exception {
         command.setCanProvisionManagingAllStudies(false);
         command.setCanProvisionParticipateInAllStudies(true);
 
         expectRoleChange(command, "study_calendar_template_builder", "add", "study", "__ALL__");
         replayMocks(); // expect nothing
 
         command.apply();
         verifyMocks();
     }
 
     public void testAllStudiesProvisioningAllowedWithAllParticipatingPermissionAndParticipationRole() throws Exception {
         command.setCanProvisionManagingAllStudies(false);
         command.setCanProvisionParticipateInAllStudies(true);
 
         expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "__ALL__");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Not added", srm.isAllStudies());
     }
 
     public void testAllStudiesProvisioningNotAllowedWithAllManagingPermissionAndParticipationRole() throws Exception {
         command.setCanProvisionManagingAllStudies(true);
         command.setCanProvisionParticipateInAllStudies(false);
 
         expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "__ALL__");
         replayMocks(); // expect nothing
 
         command.apply();
         verifyMocks();
     }
 
     public void testAllStudiesProvisioningAllowedWithAllParticipatingPermissionAndBothRole() throws Exception {
         command.setCanProvisionManagingAllStudies(false);
         command.setCanProvisionParticipateInAllStudies(true);
 
         expectRoleChange(command, "data_reader", "add", "study", "__ALL__");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Not added", srm.isAllStudies());
     }
 
     public void testAllStudiesProvisioningAllowedWithAllManagingPermissionAndBothRole() throws Exception {
         command.setCanProvisionManagingAllStudies(true);
         command.setCanProvisionParticipateInAllStudies(false);
 
         expectRoleChange(command, "data_reader", "add", "study", "__ALL__");
         SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
         replayMocks();
 
         command.apply();
         verifyMocks();
         assertTrue("Not added", srm.isAllStudies());
     }
 
     ////// javascript state init
 
     public void testJavaScriptUserForEmptyUserIsCorrect() throws Exception {
        assertEquals("new psc.admin.ProvisionableUser('jo', {})", command.getJavaScriptProvisionableUser());
     }
 
     public void testJavaScriptUserForUnnamedUserHasNoUsername() throws Exception {
         command.getUser().getCsmUser().setLoginName(null);
        assertEquals("new psc.admin.ProvisionableUser(null, {})",
             command.getJavaScriptProvisionableUser());
     }
 
     public void testJavaScriptUserWithOneGroupOnlyRole() throws Exception {
         TestCommand actual = create(new PscUserBuilder("jo").add(PscRole.SYSTEM_ADMINISTRATOR).toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"system_administrator\": {}})",
             actual.getJavaScriptProvisionableUser());
     }
 
     public void testJavaScriptUserWithOneSiteScopedRole() throws Exception {
         TestCommand actual = create(
             new PscUserBuilder("jo").add(PscRole.USER_ADMINISTRATOR).forSites(austin).toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"user_administrator\": {\"sites\": [\"i-a\"]}})",
             actual.getJavaScriptProvisionableUser());
     }
 
     public void testJavaScriptUserWithOneSitePlusStudyScopedRole() throws Exception {
         TestCommand actual = create(
             new PscUserBuilder("jo").add(PscRole.DATA_READER).forSites(sanAntonio).forAllStudies().toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"data_reader\": {\n    \"sites\": [\"i-sa\"],\n    \"studies\": [\"__ALL__\"]\n}})",
             actual.getJavaScriptProvisionableUser());
     }
 
     public void testJavaScriptUserWithMultipleRoles() throws Exception {
         TestCommand actual = create(
             new PscUserBuilder("jo").
                 add(PscRole.USER_ADMINISTRATOR).forSites(austin).
                 add(PscRole.DATA_READER).forAllSites().
                     forStudies(Fixtures.createBasicTemplate("T"), Fixtures.createBasicTemplate("Q")).
                 toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n    \"data_reader\": {\n        \"sites\": [\"__ALL__\"],\n        \"studies\": [\n            \"T\",\n            \"Q\"\n        ]\n    },\n    \"user_administrator\": {\"sites\": [\"i-a\"]}\n})",
             actual.getJavaScriptProvisionableUser());
     }
 
     ////// HELPERS
 
     private void expectRoleChange(String roleKey, String changeKind) throws JSONException {
         expectRoleChange(roleKey, changeKind, null, null);
     }
 
     private void expectRoleChange(
         String roleKey, String changeKind, String scopeType, String scopeIdent
     ) throws JSONException {
         expectRoleChange(this.command, roleKey, changeKind, scopeType, scopeIdent);
     }
 
     private void expectRoleChange(
         BaseUserProvisioningCommand cmd,
         String roleKey, String changeKind, String scopeType, String scopeIdent
     ) throws JSONException {
         expectRoleChange(cmd, cmd.getUser().getUsername(),
             roleKey, changeKind, scopeType, scopeIdent);
     }
 
     private void expectRoleChange(
         BaseUserProvisioningCommand cmd, String username,
         String roleKey, String changeKind, String scopeType, String scopeIdent
     ) throws JSONException {
         MapBuilder<String, String> mb = new MapBuilder<String, String>().
             put("role", roleKey).put("kind", changeKind);
         if (scopeType != null) {
             mb.put("scopeType", scopeType).put("scopeIdentifier", scopeIdent);
         }
 
         cmd.getRoleChanges().put(username, new JSONArray(Arrays.asList(new JSONObject(mb.toMap()))));
     }
 
     ////// INNER CLASSES
 
     private static class TestCommand extends BaseUserProvisioningCommand {
         private TestCommand(
             PscUser user, ProvisioningSessionFactory provisioningSessionFactory,
             ApplicationSecurityManager applicationSecurityManager
         ) {
             super(user, provisioningSessionFactory, applicationSecurityManager);
         }
     }
 }
