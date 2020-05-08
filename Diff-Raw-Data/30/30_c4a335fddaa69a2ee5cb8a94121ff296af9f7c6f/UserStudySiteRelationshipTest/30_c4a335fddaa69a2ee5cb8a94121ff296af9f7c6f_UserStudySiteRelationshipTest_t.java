 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationValidationException;
 import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
 
 import java.util.Date;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationScopeMappings.*;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 import static org.easymock.EasyMock.expect;
 
 /**
  * @author Rhett Sutphin
  */
 public class UserStudySiteRelationshipTest extends StudyCalendarTestCase {
     private Site nu, mayo;
     private StudySite nuF;
     private Study study, otherStudy;
     private Configuration configuration;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
 
         study = createBasicTemplate("F");
         otherStudy = createBasicTemplate("O");
 
         nu = createSite("NU", "IL099");
         mayo = createSite("Mayo", "MN008");
 
         StudySite mayoF = study.addSite(mayo);
         mayoF.approveAmendment(study.getAmendment(), new Date());
         nuF = study.addSite(nu);
         nuF.approveAmendment(study.getAmendment(), new Date());
         nuF.addStudySubjectAssignment(createAssignment(nuF, createSubject("A", "B")));
 
         configuration = registerMockFor(Configuration.class);
     }
 
     ////// canAssignSubjects
 
     public void testCanAssignIfAllScopesStudySubjectCalendarManager() throws Exception {
         expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
         replayMocks();
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
                 getCanAssignSubjects());
         verifyMocks();
     }
 
     public void testCanAssignIfSpecificStudyStudySubjectCalendarManager() throws Exception {
         expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
         replayMocks();
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(study)).
                 getCanAssignSubjects());
         verifyMocks();
     }
 
     public void testCannotAssignIfWrongStudySubjectCalendarManager() throws Exception {
         expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
         replayMocks();
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forStudies(otherStudy)).
                 getCanAssignSubjects());
         verifyMocks();
     }
 
     public void testCanAssignIfSpecificSiteStudySubjectCalendarManager() throws Exception {
         expect(configuration.get(Configuration.ENABLE_ASSIGNING_SUBJECT)).andReturn(Boolean.TRUE).anyTimes();
         replayMocks();
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forAllStudies()).
                 getCanAssignSubjects());
         verifyMocks();
     }
 
     public void testCannotAssignIfWrongSiteSubjectCalendarManager() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(mayo).forAllStudies()).
                 getCanAssignSubjects());
     }
 
     public void testCannotAssignIfNoApprovals() throws Exception {
         nuF.getAmendmentApprovals().clear();
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
                 getCanAssignSubjects());
     }
 
     public void testCannotAssignIfOtherRole() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forAllStudies()).
                 getCanAssignSubjects());
     }
 
     ////// canCreateSubjects
 
     public void testCanCreateSubjectIfAllScopesSubjectManager() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(SUBJECT_MANAGER).forAllSites()).
                 getCanCreateSubjects());
     }
 
     public void testCanCreateSubjectIfSpecificSiteSubjectManager() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(SUBJECT_MANAGER).forSites(nu)).
                 getCanCreateSubjects());
     }
 
     public void testCannotCreateSubjectIfWrongSiteSubjectManager() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(SUBJECT_MANAGER).forSites(mayo)).
                 getCanCreateSubjects());
     }
 
     public void testCannotCreateSubjectIfOtherRole() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forAllStudies()).
                 getCanCreateSubjects());
     }
 
     ////// visible
 
     public void testVisibleToAppropriatelyScopedStudySubjectCalendarManager() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forStudies(study).forSites(nu)).
                 isVisible());
     }
 
     public void testIsNotVisibleToInappropriatelyScopedStudySubjectCalendarManager() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forStudies(otherStudy).forSites(mayo)).
                 isVisible());
     }
 
     public void testVisibleToAppropriatelyScopedStudyTeamAdministrator() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(nu)).
                 isVisible());
     }
 
     public void testIsNotVisibleToInappropriatelyScopedStudyTeamAdministrator() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(mayo)).
                 isVisible());
     }
 
     public void testVisibleToAppropriatelyScopedDataReader() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(DATA_READER).forSites(nu).forAllStudies()).
                 isVisible());
     }
 
     public void testIsNotVisibleToInappropriatelyScopedDataReader() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forSites(mayo).forAllStudies()).
                 isVisible());
     }
 
     public void testVisibleToManagingStudySiteParticipationAdministrator() throws Exception {
         study.addManagingSite(mayo);
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SITE_PARTICIPATION_ADMINISTRATOR).forSites(mayo)).
                 isVisible());
     }
 
     public void testIsNotVisibleToNonManagingStudySiteParticipationAdministrator() throws Exception {
         study.addManagingSite(mayo);
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_SITE_PARTICIPATION_ADMINISTRATOR).forSites(nu)).
                 isVisible());
     }
 
     public void testIsNotVisibleToManagingStudyQaManager() throws Exception {
         study.addManagingSite(mayo);
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(mayo)).
                 isVisible());
     }
 
     public void testIsVisibleToParticipatingStudyQaManager() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                 isVisible());
     }
 
     public void testIsNotVisibleToSubjectManager() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(SUBJECT_MANAGER).forAllSites()).
                 isVisible());
     }
 
     public void testIsNotVisibleToStudyCalendarTemplateBuilder() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER).forAllStudies().forAllSites()).
                 isVisible());
     }
 
     public void testIsNotVisibleToDataImporter() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_IMPORTER)).
                 isVisible());
     }
 
     ////// canApproveAmendments
 
     public void testCannotApproveWhenThereAreNoUnapprovedAmendments() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forAllSites()).
                 getCanApproveAmendments());
     }
 
     public void testCannotApproveAsQaFromManagingSite() throws Exception {
         study.pushAmendment(new Amendment());
         study.addManagingSite(mayo);
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(mayo)).
                 getCanApproveAmendments());
     }
 
     public void testCanApproveAsQaFromMatchingSite() throws Exception {
         study.pushAmendment(new Amendment());
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forSites(nu)).
                 getCanApproveAmendments());
     }
 
     public void testCannotApproveAsOtherRole() throws Exception {
         study.pushAmendment(new Amendment());
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forSites(nu).forAllStudies()).
                 getCanApproveAmendments());
     }
 
     ////// canSeeSubjectInformation
 
     public void testCanSeeSubjectsAsStudyTeamAdmin() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(nu)).
                 getCanSeeSubjectInformation());
     }
 
     public void testCanSeeSubjectsAsStudySubjectCalendarManager() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study)).
                 getCanSeeSubjectInformation());
     }
 
     public void testCanSeeSubjectsAsDataReader() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forStudies(study)).
                 getCanSeeSubjectInformation());
     }
 
     public void testCannotSeeSubjectsAsStudyQaManager() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_QA_MANAGER).forAllSites()).
                 getCanSeeSubjectInformation());
     }
 
     public void testCannotSeeSubjectsWhenThereAreNone() throws Exception {
         nuF.getStudySubjectAssignments().clear();
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forAllStudies()).
                 getCanSeeSubjectInformation());
     }
 
    ////// couldSeeSubjectInformation

    public void testCouldSeeSubjectsInformationWhenThereAreNone() throws Exception {
        nuF.getStudySubjectAssignments().clear();
        assertTrue(
            actual(createSuiteRoleMembership(DATA_READER).forAllSites().forAllStudies()).
                getCouldSeeSubjectInformation());
    }


     ////// canAdministerUsers
 
     public void testCanAdministerUsersIfAllScopesUserAdministrator() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(USER_ADMINISTRATOR).forAllSites()).
                 getCanAdministerUsers());
     }
 
     public void testCanAdministerUsersIfSpecificSiteUserAdministrator() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(USER_ADMINISTRATOR).forSites(nu)).
                 getCanAdministerUsers());
     }
 
     public void testCannotAdministerUsersIfWrongSiteUserAdministrator() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(USER_ADMINISTRATOR).forSites(mayo)).
                 getCanAdministerUsers());
     }
 
     public void testCannotAdministerUsersIfOtherRole() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forAllStudies()).
                 getCanAdministerUsers());
     }
 
     ////// canAdministerTeam
 
     public void testCanAdministerTeamIfAllScopesStudyTeamAdministrator() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forAllSites()).
                 getCanAdministerTeam());
     }
 
     public void testCanAdministerTeamIfSpecificSiteStudyTeamAdministrator() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(nu)).
                 getCanAdministerTeam());
     }
 
     public void testCannotAdministerTeamIfWrongSiteStudyTeamAdministrator() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_TEAM_ADMINISTRATOR).forSites(mayo)).
                 getCanAdministerTeam());
     }
 
     public void testCannotAdministerTeamIfOtherRole() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forAllStudies()).
                 getCanAdministerTeam());
     }
 
     ////// canTakeSubjectOffStudy
 
     public void testCanTakeSubjectOffStudy() throws Exception {
         assertTrue(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forSites(nu).forStudies(study)).
                 getCanTakeSubjectOffStudy());
     }
 
     public void testCannotTakeSubjectOffStudyAsOther() throws Exception {
         assertFalse(
             actual(createSuiteRoleMembership(DATA_READER).forAllSites().forStudies(study)).
                 getCanTakeSubjectOffStudy());
     }
 
     public void testCannotTakeSubjectOffStudyWhenThereAreNone() throws Exception {
         nuF.getStudySubjectAssignments().clear();
         assertFalse(
             actual(createSuiteRoleMembership(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies()).
                 getCanTakeSubjectOffStudy());
     }
 
     ////// HELPERS
 
     private UserStudySiteRelationship actual(SuiteRoleMembership membership) {
         try {
             membership.checkComplete();
         } catch (SuiteAuthorizationValidationException save) {
             fail("Test membership is incomplete.  " + save.getMessage());
         }
         try {
             membership.validate();
         } catch (SuiteAuthorizationValidationException save) {
             fail("Test membership is invalid.  " + save.getMessage());
         }
         return new UserStudySiteRelationship(createPscUser("jo", membership), nuF, configuration);
     }
 }
