 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import java.text.DateFormat;
 import java.util.Locale;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.user.api.User;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.producers.BasicProducer;
 
 /**
  * This renders the Assignments Details that typically appear at the top of
  * the Student Submit/View Assignment page.
  * 
  * Usually it looks something like:
  * 
  * <h2>Professional Writing for Visual Media Submission for Earlene Arledge</h2>
  * <h3>DUE: 09 23, 2008 4:00pm</h3>
  * <h2>Assignment Details</h2>
  * <table>
  * <tr>
  *   <th>Graded?</th>	
  *   <td>Yes</td>
  * </tr>
  * <tr>
  *   <th>Points Possible</th>	
  *   <td>25</td>
  * </tr>
  * <tr>
  *   <th>Resubmissions Allowed</th>	
  *   <td>Yes</td>
  * </tr>
  * <tr>
  *   <th>Remaining Resubmissions Allowed</th>	
  *   <td>2</td>
  * </tr>
  * <tr>
  *   <th>Grade</th>	
  *   <td>22</td>
  * </tr>
  * </table>
  * 
  * @author sgithens
  *
  */
 public class AsnnSubmissionDetailsRenderer implements BasicProducer {
     private static Log log = LogFactory.getLog(AsnnSubmissionDetailsRenderer.class);
 
     // Dependency
     private User currentUser;
     public void setCurrentUser(User currentUser) {
         this.currentUser = currentUser;
     }
 
     // Dependency
     private Locale locale;
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     // Dependency
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     // Dependency
     private ExternalGradebookLogic externalGradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic externalGradebookLogic) {
         this.externalGradebookLogic = externalGradebookLogic;
     }  
 
     // Dependency
     private String curContext;
     public void setCurContext(String curContext) {
         this.curContext = curContext;
     }
 
     // Dependency
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     public void fillComponents(UIContainer parent, String clientID, AssignmentSubmission assignmentSubmission, boolean previewAsStudent) {
         fillComponents(parent, clientID, assignmentSubmission, previewAsStudent, false);
     }
     
     /**
      * Renders the assignment details at the top of the Student Submission
      * Page(s)
      * 
      * @param parent
      * @param clientID
      * @param assignmentSubmission
      * @param previewAsStudent
      * @param excludeDetails If this is true, we only render the Title/Name and
      * due date, but leave off the table with Graded, Submission Status etc.
      */
     public void fillComponents(UIContainer parent, String clientID, AssignmentSubmission assignmentSubmission, boolean previewAsStudent, boolean excludeDetails) {
         Assignment2 assignment = assignmentSubmission.getAssignment();
         String title = assignment.getTitle();
 
         UIJointContainer joint = new UIJointContainer(parent, clientID, "assn2-submission-details-widget:");
 
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         /***
          * Title and Due Date Information
          */
         if (!assignment.isRemoved()) {
            String submissionHeading;
            if (!assignment.isRequiresSubmission() || assignment.getSubmissionType() == AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
                submissionHeading = messageLocator.getMessage("assignment2.student-submit.heading.no_submission", new Object[]{ title, currentUser.getDisplayName() });
            } else {
                submissionHeading = messageLocator.getMessage("assignment2.student-submit.heading.submission", new Object[]{ title, currentUser.getDisplayName() });
            }
            UIOutput.make(joint, "heading_status", submissionHeading);
         } else {
            UIVerbatim.make(joint, "heading_status", messageLocator.getMessage("assignment2.student-submit.heading.submission.deleted", 
                     new Object[]{ title, currentUser.getDisplayName() }));
         }
 
         if (assignment.getDueDate() == null) {
             UIMessage.make(joint, "due_date",
             "assignment2.student-submit.no_due_date");
         }
         else {
             UIMessage.make(joint, "due_date",  
                     "assignment2.student-submit.due_date", 
                     new Object[] {df.format(assignment.getDueDate())});
         }
 
         if (!excludeDetails) {
             renderAssignmentDetails(assignmentSubmission, previewAsStudent,
                     assignment, joint);
         }
 
     }
 
     /**
      * @param assignmentSubmission
      * @param previewAsStudent
      * @param assignment
      * @param joint
      */
     private void renderAssignmentDetails(
             AssignmentSubmission assignmentSubmission,
             boolean previewAsStudent, Assignment2 assignment,
             UIJointContainer joint) {
         /***
          * Assignment Details including:
          *   - Graded?
          *   - Points Possible
          *   - Resubmissions Allowed
          *   - Remaining Resubmissions Allowed
          *   - Grade
          *   - Comments
          */
         // Title
         UIMessage.make(joint, "assignment-details-header", "assignment2.student-submit.details_title");
 
         // Details Table
         UIOutput.make(joint, "assignment-details-table");
         
         // Graded?
         if (assignment.isGraded()) {
             UIMessage.make(joint, "is_graded", "assignment2.student-submit.yes_graded");
         }
         else {
             UIMessage.make(joint, "is_graded", "assignment2.student-submit.no_graded");
         }
 
         /*
          * Points possible : Display if the assignment is 
          *
          *  1) graded 
          *  2) associated with a gradebook item
          *  3) gradebook entry type is "Points"
          *  
          *  TODO FIXME Needs checks for whether the graded item is point based 
          *  (rather than percentage, pass/fail, etc). We are still working on 
          *  the best way to integrate this with the gradebook a reasonable
          *  amount of coupling.
          */
         if (assignment.isGraded() && assignment.getGradebookItemId() != null) {
             try {
                 GradebookItem gradebookItem = 
                     externalGradebookLogic.getGradebookItemById(curContext, 
                             assignment.getGradebookItemId());
                 UIOutput.make(joint, "points-possible-row");
                 UIOutput.make(joint, "points-possible", gradebookItem.getPointsPossible().toString());      
 
                 // Render the graded information if it's available.
                 String grade = externalGradebookLogic.getStudentGradeForItem(
                         curContext, currentUser.getId(), assignment.getGradebookItemId());
                 if (grade != null) {
                     UIOutput.make(joint, "grade-row");
                     UIOutput.make(joint, "grade", grade);
                 }
 
                 String comment = externalGradebookLogic.getStudentGradeCommentForItem(
                         curContext, currentUser.getId(), assignment.getGradebookItemId());
 
                 if (comment != null) {
                     UIOutput.make(joint, "comment-row");
                     UIOutput.make(joint, "comment", comment);
                 }
 
             } catch (IllegalArgumentException iae) {
                 log.warn("Trying to look up grade object that doesn't exist" 
                         + "context: " + curContext 
                         + " gradeObjectId: " + assignment.getGradebookItemId() 
                         + "asnnId: " + assignment.getId());
             }
         }
 
         /*
          * Resubmissions Allowed
          */
         //if (!preview) {
         boolean resubmissionAllowed = false;
         Integer subLevelNumSubmissions = assignmentSubmission.getNumSubmissionsAllowed();
         if (subLevelNumSubmissions != null) {
             if (subLevelNumSubmissions.equals(AssignmentConstants.UNLIMITED_SUBMISSION) ||
                     subLevelNumSubmissions.intValue() > 1) {
                 resubmissionAllowed = true;
             }
         } else {
             if (assignment.getNumSubmissionsAllowed() > 1 || 
                     assignment.getNumSubmissionsAllowed() == AssignmentConstants.UNLIMITED_SUBMISSION) {
                 resubmissionAllowed = true;
             }
         }
 
         if (resubmissionAllowed) {
             UIMessage.make(joint, "resubmissions-allowed", "assignment2.student-submit.resubmissions_allowed");
         }
         else {
             UIMessage.make(joint, "resubmissions-allowed", "assignment2.student-submit.resubmissions_not_allowed");
         }
 
         /*
          * Remaining resubmissions allowed
          */
         if (!previewAsStudent) {
             UIOutput.make(joint, "remaining-resubmissions-row");
             int numSubmissionsAllowed = submissionLogic.getNumberOfRemainingSubmissionsForStudent(currentUser.getId(), assignment.getId());
             String numAllowedDisplay;
             if (numSubmissionsAllowed == AssignmentConstants.UNLIMITED_SUBMISSION) {
                 numAllowedDisplay = messageLocator.getMessage("assignment2.indefinite_resubmit");
             } else {
                 numAllowedDisplay = "" + numSubmissionsAllowed;
             }
             UIOutput.make(joint, "remaining-resubmissions", numAllowedDisplay);
         }
     }
 
     public void fillComponents(UIContainer parent, String clientID) {
         // TODO Auto-generated method stub
 
     }
 
 
 }
