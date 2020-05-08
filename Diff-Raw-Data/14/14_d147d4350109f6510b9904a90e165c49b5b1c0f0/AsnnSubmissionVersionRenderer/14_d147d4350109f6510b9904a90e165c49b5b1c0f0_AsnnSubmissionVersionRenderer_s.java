 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.producers.BasicProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /**
  * Renders a single submission version from a Student. You'll see this on the
  * Student View Assn Details page, or as part of an aggregated drop down when
  * there are multiple submissions.  It includes the title, student name, 
  * submission date, and then the text/attachments of the submission.
  * 
  * If there is feedback from the Instructor for this version that will be 
  * rendered as well.
  * 
  * 
  * @author sgithens
  *
  */
 public class AsnnSubmissionVersionRenderer implements BasicProducer {
     private static Log log = LogFactory.getLog(AsnnSubmissionVersionRenderer.class);
 
     // Dependency
     private ViewParameters viewParameters;
     public void setViewParameters(ViewParameters viewParameters) {
         this.viewParameters = viewParameters;
     }
 
     // Dependency
     private AttachmentListRenderer attachmentListRenderer;
     public void setAttachmentListRenderer (AttachmentListRenderer attachmentListRenderer) {
         this.attachmentListRenderer = attachmentListRenderer;
     }
 
     /**
      * Renders the Submission Version in the parent container in element with 
      * the client id. Returns the new UIContainer that holds this rendered
      * version.
      * 
      * @param parent
      * @param clientID
      * @param asnnSubVersion
      * @param multipleVersionDisplay true if this version is being displayed in a multi-version scenario.
   	 * this will prevent unnecessary repeated headers
      * @param singleVersionDisplay - true if being used in the context of a single
      * version. the heading information is different for multi-version display (ie the history)
      * @return
      */
     public UIContainer fillComponents(UIContainer parent, String clientID, AssignmentSubmissionVersion asnnSubVersion, boolean multipleVersionDisplay) {
         UIJointContainer joint = new UIJointContainer(parent, clientID, "asnn2-submission-version-widget:");
 
         AssignmentSubmission assignmentSubmssion = asnnSubVersion.getAssignmentSubmission();
         Assignment2 assignment = assignmentSubmssion.getAssignment();
         int submissionType = assignment.getSubmissionType();
 
         /*
          * Render the headers
          */
         if (!multipleVersionDisplay) {
             UIMessage.make(joint, "submission-header", "assignment2.student-submission.submission.header");
         }
 
         //TODO FIXME time and date of submission here
         
         /*
          * Render the Students Submission Materials
          * These are not displayed if the version number is 0 - this indicates
          * feedback without a submission
          */
         if (asnnSubVersion.getSubmittedVersionNumber() != 0) {
             if (submissionType == AssignmentConstants.SUBMIT_ATTACH_ONLY || submissionType == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH) {
                 // TODO FIXME if the student didn't actually submit any attachments
                 // what should we say
                 UIMessage.make(joint, "submission-attachments-header", "assignment2.student-submit.submitted_attachments");
                 
                 if (asnnSubVersion.getSubmissionAttachSet() != null && !asnnSubVersion.getSubmissionAttachSet().isEmpty()){
                     attachmentListRenderer.makeAttachmentFromSubmissionAttachmentSet(joint, "submission-attachment-list:", viewParameters.viewID, 
                             asnnSubVersion.getSubmissionAttachSet());
                 } else {
                     UIMessage.make(joint, "no_submitted_attachments", "assignment2.student-submit.no_attachments_submitted");
                 }
             }
             
            if (submissionType == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH || submissionType == AssignmentConstants.SUBMIT_INLINE_ONLY) {
                UIMessage.make(joint, "submission-text-header", "assignment2.student-submit.submission_text");
                UIVerbatim.make(joint, "submission-text", asnnSubVersion.getSubmittedText());
             }
         }
 
         /* 
          * Render the Instructor's Feedback Materials
          */
         if (asnnSubVersion.isFeedbackReleased()) {
             UIMessage.make(joint, "feedback-header", "assignment2.student-submission.feedback.header");
             String feedbackText = asnnSubVersion.getFeedbackNotes();
             
             if (feedbackText != null && feedbackText.trim().length() > 0) {
                 UIVerbatim.make(joint, "feedback-text", asnnSubVersion.getFeedbackNotes());
             } else {
                 UIMessage.make(joint, "feedback-text", "assignment2.student-submission.feedback.none");
             }
             
             if (asnnSubVersion.getFeedbackAttachSet() != null && 
                     asnnSubVersion.getFeedbackAttachSet().size() > 0) {
                 UIMessage.make(joint, "feedback-attachments-header", "assignment2.student-submission.feedback.materials.header");
                 attachmentListRenderer.makeAttachmentFromFeedbackAttachmentSet(joint, 
                         "feedback-attachment-list:", viewParameters.viewID, 
                         asnnSubVersion.getFeedbackAttachSet());
             }
         }
         
         return joint;
     }
 
     public void fillComponents(UIContainer parent, String clientID) {
 
     }
 
 }
