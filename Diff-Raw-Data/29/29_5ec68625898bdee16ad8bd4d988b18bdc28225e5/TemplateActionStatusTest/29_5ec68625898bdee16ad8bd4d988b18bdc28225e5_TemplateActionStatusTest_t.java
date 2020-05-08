 package edu.northwestern.bioinformatics.studycalendar.service.presenter;
 
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
 
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.service.presenter.WorkflowStep.*;
 import static org.easymock.EasyMock.expect;
 
 public class TemplateActionStatusTest extends StudyCalendarTestCase {
     private StudyWorkflowStatus studyWorkflowStatus;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         studyWorkflowStatus = registerMockFor(StudyWorkflowStatus.class);
     }
 
    public void testReleaseAvailableWhenHasAssignedIdentifierAndRevisionComplete() {
         expect(studyWorkflowStatus.isRevisionComplete()).andReturn(true);
         List<TemplateAction> actions = getActionsWhenStudyWorkflowStatusIs(COMPLETE_AND_RELEASE_INITIAL_TEMPLATE);
        assertEquals("Wrong size", 1, actions.size());
        assertEquals("Wrong action", TemplateAction.RELEASE_REVISION, actions.get(0));
     }
 
    public void testReleaseNotAvailableWhenMissingAssignedIdentifier() {
         List<TemplateAction> actions = getActionsWhenStudyWorkflowStatusIs(SET_ASSIGNED_IDENTIFIER);
         assertTrue("Should be empty", actions.isEmpty());
     }
 
    public void testReleaseNotAvailableWhenRevisionIncomplete() {
         expect(studyWorkflowStatus.isRevisionComplete()).andReturn(false);
         List<TemplateAction> actions = getActionsWhenStudyWorkflowStatusIs(null);
         assertTrue("Should be empty", actions.isEmpty());
     }
 
     // Logic Helpers
     private List<TemplateAction> getActionsWhenStudyWorkflowStatusIs(WorkflowStep step) {
         expect(studyWorkflowStatus.getMessagesIgnoringRevisionMessages()).andReturn(createWorkflowMessage(step));
         TemplateActionStatus status = new TemplateActionStatus(studyWorkflowStatus, true);
         replayMocks();
         List<TemplateAction> actions = status.getActions();
         verifyMocks();
         return actions;
     }
 
     // Instance Creation Helpers
     private WorkflowMessage createWorkflowMessage(WorkflowStep step) {
         if (step == null) {return null;}
         return new WorkflowMessage(step, "/", true);
     }
 }
