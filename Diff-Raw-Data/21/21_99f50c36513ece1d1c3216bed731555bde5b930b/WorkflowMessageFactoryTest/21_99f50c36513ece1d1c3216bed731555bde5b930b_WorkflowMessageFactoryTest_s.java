 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.domain.Study;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
 import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
 
 import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
 import static org.easymock.EasyMock.*;
 
 /**
  * @author Rhett Sutphin
  */
 public class WorkflowMessageFactoryTest extends StudyCalendarTestCase {
     private WorkflowMessageFactory factory;
 
     private UserTemplateRelationship template;
     private Study study;
     private StudySegment segmentA;
 
     private UserStudySiteRelationship participation;
 
     @Override
     public void setUp() throws Exception {
         super.setUp();
         study = createSingleEpochStudy("Aleph", "Treatment", "A", "B");
         assignIds(study);
 
         template = registerMockFor(UserTemplateRelationship.class);
         expect(template.getStudy()).andStubReturn(study);
 
         segmentA = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
         segmentA.addPeriod(setId(77, createPeriod("P7", 1, 1, 5)));
 
         StudySite ss = setId(9, study.addSite(createSite("NU", "IL441")));
         participation = registerMockFor(UserStudySiteRelationship.class);
         expect(participation.getStudySite()).andStubReturn(ss);
 
         factory = new WorkflowMessageFactory();
         factory.setWebContextPath("/psc-prod");
     }
 
     public void testMessageForStudyIdentWhenCanPerform() throws Exception {
        expect(template.getCanDevelop()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.SET_ASSIGNED_IDENTIFIER, template);
         verifyMocks();
 
         assertEquals("Wrong message", "needs the assigned identifier set.", msg.getHtml());
     }
 
     public void testMessageForStudyIdentWhenCannotPerform() throws Exception {
        expect(template.getCanDevelop()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.SET_ASSIGNED_IDENTIFIER, template);
         verifyMocks();
 
         assertEquals("Wrong message",
            "needs the assigned identifier set.  A <em>Study Calendar Template Builder</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForAddEpochWhenCanPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.ADD_AT_LEAST_ONE_EPOCH, template);
         verifyMocks();
 
         assertEquals("Wrong message", "Please add at least one epoch.", msg.getHtml());
     }
 
     public void testMessageForAddEpochWhenCannotPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.ADD_AT_LEAST_ONE_EPOCH, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please add at least one epoch.  A <em>Study Calendar Template Builder</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForUnnamedEpochWhenCanPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.UNNAMED_EPOCH, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please name all epochs.",
             msg.getHtml());
     }
 
     public void testMessageForUnnamedEpochWhenCannotPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.UNNAMED_EPOCH, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please name all epochs.  A <em>Study Calendar Template Builder</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForUnnamedStudySegmentWhenCanPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.UNNAMED_STUDY_SEGMENT, template, segmentA);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please name all the study segments in epoch Treatment.",
             msg.getHtml());
     }
 
     public void testMessageForUnnamedStudySegmentWhenCannotPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.UNNAMED_STUDY_SEGMENT, template, segmentA);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please name all the study segments in epoch Treatment.  A <em>Study Calendar Template Builder</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForSegmentNoPeriodWhenCanPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.STUDY_SEGMENT_NO_PERIODS,
                 template, segmentA);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Study segment Treatment: A does not have any periods.  <a href=\"/psc-prod/pages/cal/newPeriod?studySegment=5050\" class=\"control\">Add one.</a>",
             msg.getHtml());
     }
 
     public void testMessageForSegmentNoPeriodWhenCannotPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.STUDY_SEGMENT_NO_PERIODS, template, segmentA);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Study segment Treatment: A does not have any periods.  Add one.  A <em>Study Calendar Template Builder</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForPeriodNoActivitiesWhenCanPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES, template,
                 segmentA.getPeriods().first());
         verifyMocks();
 
         assertEquals("Wrong message",
             "Period P7 in Treatment: A does not have any planned activities.  " +
                 "<a href=\"/psc-prod/pages/cal/managePeriodActivities?period=77\" class=\"control\">Add some.</a>",
             msg.getHtml());
     }
 
     public void testMessageForPeriodNoActivitiesWhenCannotPerform() throws Exception {
         expect(template.getCanDevelop()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.
             createMessage(WorkflowStep.PERIOD_NO_PLANNED_ACTIVITIES, template,
                 segmentA.getPeriods().first());
         verifyMocks();
 
         assertEquals("Wrong message",
             "Period P7 in Treatment: A does not have any planned activities.  Add some.  " +
                 "A <em>Study Calendar Template Builder</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForReleaseRevisionWhenCanPerformForInitial() throws Exception {
         expect(template.getCanRelease()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.RELEASE_REVISION, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "When the initial template is complete, it will need to be " +
                 "<a href=\"/psc-prod/pages/cal/template/release?study=0\" class=\"control\">released</a>.",
             msg.getHtml());
     }
 
     public void testMessageForReleaseRevisionWhenCanPerformForSubsequent() throws Exception {
         expect(template.getCanRelease()).andReturn(true);
         study.setAmendment(new Amendment());
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.RELEASE_REVISION, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "When the amendment is complete, it will need to be " +
                 "<a href=\"/psc-prod/pages/cal/template/release?study=0\" class=\"control\">released</a>.",
             msg.getHtml());
     }
 
     public void testMessageForReleaseRevisionWhenCannotPerform() throws Exception {
         expect(template.getCanRelease()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.RELEASE_REVISION, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "When the initial template is complete, it will need to be released.  " +
                 "A <em>Study QA Manager</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForReleasedTemplateWhenCanPerform() throws Exception {
         expect(template.getCanRelease()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.COMPLETE_AND_RELEASE_INITIAL_TEMPLATE, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "needs at least one revision completed and released.",
             msg.getHtml());
     }
 
     public void testMessageForReleasedTemplateWhenCannotPerform() throws Exception {
         expect(template.getCanRelease()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.COMPLETE_AND_RELEASE_INITIAL_TEMPLATE, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "needs at least one revision completed and released.  A <em>Study QA Manager</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForAssignSiteWhenCanPerform() throws Exception {
         expect(template.getCanSetParticipation()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.ASSIGN_SITE, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "needs at least one site <a href=\"/psc-prod/pages/cal/assignSite?id=0\" class=\"control\">assigned</a> for participation.",
             msg.getHtml());
     }
 
     public void testMessageForAssignSiteWhenCannotPerform() throws Exception {
         expect(template.getCanSetParticipation()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.ASSIGN_SITE, template);
         verifyMocks();
 
         assertEquals("Wrong message",
             "needs at least one site assigned for participation.  A <em>Study Site Participation Administrator</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForApprovalWhenCanPerform() throws Exception {
         expect(participation.getCanApproveAmendments()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.APPROVE_AMENDMENT, participation);
         verifyMocks();
 
         assertEquals("Wrong message",
             "needs to be <a href=\"/psc-prod/pages/cal/template/approve?studySite=9\" class=\"control\">approved</a>.",
             msg.getHtml());
     }
 
     public void testMessageForApprovalWhenCannotPerform() throws Exception {
         expect(participation.getCanApproveAmendments()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.APPROVE_AMENDMENT, participation);
         verifyMocks();
 
         assertEquals("Wrong message",
             "needs to be approved.  A <em>Study QA Manager</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForAddCoordinatorForSiteWhenCanPerform() throws Exception {
         expect(participation.getCanAdministerUsers()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.ADD_SSCM_FOR_SITE, participation);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please <a href=\"/psc-prod/pages/admin/manage/listUsers\" class=\"control\">add</a>"
                 + " at least one Study Subject Calendar Manager for NU.",
             msg.getHtml());
     }
 
     public void testMessageForAddCoordinatorForSiteWhenCannotPerform() throws Exception {
         expect(participation.getCanAdministerUsers()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.ADD_SSCM_FOR_SITE, participation);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please add at least one Study Subject Calendar Manager for NU.  " +
                 "A <em>User Administrator</em> can do this.",
             msg.getHtml());
     }
 
     public void testMessageForAddCoordinatorForStudyWhenCanPerform() throws Exception {
         expect(participation.getCanAdministerTeam()).andReturn(true);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.ADD_SSCM_FOR_STUDY, participation);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please <a href=\"/psc-prod/pages/cal/team/manage?studySite=9\" class=\"control\">add</a>"
                 + " at least one Study Subject Calendar Manager.",
             msg.getHtml());
     }
 
     public void testMessageForAddCoordinatorForStudyWhenCannotPerform() throws Exception {
         expect(participation.getCanAdministerTeam()).andReturn(false);
 
         replayMocks();
         WorkflowMessage msg = factory.createMessage(WorkflowStep.ADD_SSCM_FOR_STUDY, participation);
         verifyMocks();
 
         assertEquals("Wrong message",
             "Please add at least one Study Subject Calendar Manager.  " +
                 "A <em>Study Team Administrator</em> can do this.",
             msg.getHtml());
     }
 }
