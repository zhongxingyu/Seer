 package org.sakaiproject.assignment2.tool.beans;
 
 import org.sakaiproject.assignment2.exception.SubmissionClosedException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ScheduledNotification;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.tool.WorkFlowResult;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 
 public class StudentSubmissionBean {
 
     // Service Application Scope Dependency
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     // Service Application Scope Dependency
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
     
     // Service Application Scope Dependency
     private ScheduledNotification scheduledNotification;
     public void setScheduledNotification(ScheduledNotification scheduledNotification) {
         this.scheduledNotification = scheduledNotification;
     }
 
     // Flow Scope Bean for Student Submission
     private StudentSubmissionVersionFlowBean studentSubmissionVersionFlowBean;
     public void setStudentSubmissionVersionFlowBean(StudentSubmissionVersionFlowBean studentSubmissionVersionFlowBean) {
         this.studentSubmissionVersionFlowBean = studentSubmissionVersionFlowBean;
     }
 
     // Property Binding
     public Long assignmentId;
 
     // Property Binding
     private Boolean honorPledge;
     public void setHonorPledge(Boolean honorPledge) {
         this.honorPledge = honorPledge;
     }
 
     // Property Binding
     public String ASOTPKey;
 
     // Request Scope Dependency
     private TargettedMessageList messages;
     public void setMessages(TargettedMessageList messages) {
         this.messages = messages;
     }
 
     // Request Scope Dependency (sort of even though it's declared in App Scope)
     private EntityBeanLocator asEntityBeanLocator;
     @SuppressWarnings("unchecked")
     public void setAssignmentSubmissionEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.asEntityBeanLocator = entityBeanLocator;
     }
 
     /*
      * STUDENT FUNCTIONS
      */
     public WorkFlowResult processActionSubmit(){
         if (assignmentId == null ) {
             return WorkFlowResult.STUDENT_SUBMISSION_FAILURE;
         }
 
         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
 
         AssignmentSubmissionVersion asv = studentSubmissionVersionFlowBean.getAssignmentSubmissionVersion();
 
         //check whether honor pledge was added if required
         if (assignment.isHonorPledge() && !(this.honorPledge != null && Boolean.TRUE.equals(honorPledge))) {
             messages.addMessage(new TargettedMessage("assignment2.student-submit.error.honor_pledge_required",
                     new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_ERROR));
             return WorkFlowResult.STUDENT_SUBMISSION_FAILURE;
         }else {
 
             submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(), assignment, false, 
                     asv.getSubmittedText(), asv.getSubmissionAttachSet(), true);
 
             // just in case submission closed while the student was working on
             // it, double check that the current submission isn't still
             // draft before we take the "success" actions. if the submission was
             // closed when they hit "submit", the backend saved their submission as draft
             AssignmentSubmission newSubmission = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(assignmentId, assignmentSubmission.getUserId());
 
             if (!newSubmission.getCurrentSubmissionVersion().isDraft()) {
                 messages.addMessage(new TargettedMessage("assignment2.student-submit.info.submission_submitted",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
 
                 // Send out notifications
                 if (assignment.isSendSubmissionNotifications()) {                 
                     scheduledNotification.notifyInstructorsOfSubmission(newSubmission);
                 }
                 
                 // students always get a notification
                 scheduledNotification.notifyStudentThatSubmissionWasAccepted(newSubmission);
             } else {
                 messages.addMessage(new TargettedMessage("assignment2.student-submit.error.submission_save_draft",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_ERROR));
                 return WorkFlowResult.STUDENT_SUBMISSION_FAILURE;
             }
         }
 
         return WorkFlowResult.STUDENT_SUBMIT_SUBMISSION;
     }
 
     public WorkFlowResult processActionPreview(){
         // save this submission as draft if submission is closed so we don't
         // lose the student's work. this may happen if the user was working
         // on their submission in the UI when the assignment closed and then
         // the student hit "preview"
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
         if (assignmentId != null) {
             AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
             assignmentSubmission.setAssignment(assignment);
 
             if (!submissionLogic.isSubmissionOpenForStudentForAssignment(assignmentSubmission.getUserId(), assignmentId)) {
                 AssignmentSubmissionVersion asv = (AssignmentSubmissionVersion) studentSubmissionVersionFlowBean.getAssignmentSubmissionVersion();
 
                 submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(),
                         assignmentSubmission.getAssignment(), true, asv.getSubmittedText(),
                         asv.getSubmissionAttachSet(), true);
             }
         }
 
         return WorkFlowResult.STUDENT_PREVIEW_SUBMISSION;
     }
 
     public WorkFlowResult processActionBackToEdit() {
         return WorkFlowResult.STUDENT_CONTINUE_EDITING_SUBMISSION;
     }
 
     public WorkFlowResult processActionSaveDraft() {
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
         if (assignmentId == null){
             return WorkFlowResult.STUDENT_SUBMISSION_FAILURE;
         }
 
         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
         assignmentSubmission.setAssignment(assignment);
 
         AssignmentSubmissionVersion asv = (AssignmentSubmissionVersion) studentSubmissionVersionFlowBean.getAssignmentSubmissionVersion();
 
         asv.setAssignmentSubmission(assignmentSubmission);
         asv.setDraft(Boolean.TRUE);
 
         try {
             submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(),
                     assignmentSubmission.getAssignment(), true, asv.getSubmittedText(),
                     asv.getSubmissionAttachSet(), true);
             messages.addMessage(new TargettedMessage("assignment2.student-submit.info.submission_save_draft",
                     new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
         } catch (SubmissionClosedException sce) {
             messages.addMessage(new TargettedMessage("assignment2.student-submit.error.submission_closed",
                     new Object[] {}, TargettedMessage.SEVERITY_ERROR));
             return WorkFlowResult.STUDENT_SUBMISSION_FAILURE;
         }
 
         return WorkFlowResult.STUDENT_SAVE_DRAFT_SUBMISSION;
     }
 
 }
