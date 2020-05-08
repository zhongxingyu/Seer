 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.exception.GradebookItemNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
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
         
         String dueDateText = null;
         
         if (!previewAsStudent && assignment.isRequiresSubmission() && 
                 assignment.getSubmissionType() != AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
             if (!submissionLogic.isSubmissionOpenForStudentForAssignment(assignmentSubmission.getUserId(), assignment.getId())) {
                 
                 // display error message indicating that submission is closed
                 // if submission is closed and:
                 // 1) student never made a submission -OR-
                 // 2) student had a submission in progress
                 if (!assignment.isRemoved()) {
                     if (assignmentSubmission.getCurrentSubmissionVersion() == null ||
                             assignmentSubmission.getCurrentSubmissionVersion().getId() == null ||
                             assignmentSubmission.getCurrentSubmissionVersion().isDraft()) {
                         UIOutput.make(joint, "submission_closed", messageLocator.getMessage("assignment2.student-submit.submission_closed"));
                     }
                 }
 
                 // if submission is closed and there is only one submission, we replace the
                 // due date text with "Submitted Jan 3, 2009 5:23 PM" info. If there are
                 // multiple submissions, this timestamp appears in the version history display
                 // so is not necessary here
                 List<AssignmentSubmissionVersion> history = submissionLogic.getVersionHistoryForSubmission(assignmentSubmission);
                 if (history != null && history.size() == 1) {
                     AssignmentSubmissionVersion version = history.get(0);
                     if (version.getSubmittedDate() != null) {
                         dueDateText = messageLocator.getMessage("assignment2.student-submit.submitted_info", 
                                 new Object[]{version.getSubmittedDate()});
                     }
                 }
             }
         }
 
         // if dueDateMessage has text already, we must be replacing the due date
         // with the submission info. otherwise, display the due date
         if (dueDateText == null) {
             if (assignment.getDueDate() == null) {
                 dueDateText = messageLocator.getMessage("assignment2.student-submit.no_due_date");
             } else {
                 dueDateText = messageLocator.getMessage("assignment2.student-submit.due_date", 
                         new Object[] {df.format(assignment.getDueDate())});
             }
         }
         
         UIOutput.make(joint, "due_date", dueDateText);
 
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
                 // make sure this gradebook item still exists
                 GradebookItem gradebookItem;
                 try {
                     gradebookItem = 
                         externalGradebookLogic.getGradebookItemById(curContext, 
                                 assignment.getGradebookItemId());
                 } catch (GradebookItemNotFoundException ginfe) {
                     if (log.isDebugEnabled()) log.debug("Student attempting to access assignment " + 
                             assignment.getId() + " but associated gb item no longer exists!");
                     gradebookItem = null;
                 }
                 // only display points possible if grade entry by points
                 if (gradebookItem != null && externalGradebookLogic.isGradingByPoints(assignment.getContextId())) {
                     UIOutput.make(joint, "points-possible-row");
                     
                     String pointsDisplay;
                     if (gradebookItem.getPointsPossible() == null) {
                         pointsDisplay = messageLocator.getMessage("assignment2.student-submit.points_possible.none");
                     } else {
                         pointsDisplay = gradebookItem.getPointsPossible().toString();
                     }
                     UIOutput.make(joint, "points-possible", pointsDisplay); 
                 }
 
                 // Render the graded information if it's available.
                 String grade = null;
                 String comment = null;
                 if (gradebookItem != null) {
                     GradeInformation gradeInfo = externalGradebookLogic.getGradeInformationForStudent(curContext, assignment.getGradebookItemId(), currentUser.getId());
                     if (gradeInfo != null) {
                         grade = gradeInfo.getGradebookGrade();
                         comment = gradeInfo.getGradebookComment();
                     }
                 }
                 
                 if (grade != null) {
                     UIOutput.make(joint, "grade-row");
                     UIOutput.make(joint, "grade", grade);
                 }
 
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
          * Only display the resubmission information if this assignment requires submission
          */
         if (assignment.isRequiresSubmission()) {
             UIOutput.make(joint, "resubmission-allowed-row");
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
             if (!previewAsStudent && resubmissionAllowed) {
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
     }
 
     public void fillComponents(UIContainer parent, String clientID) {
         // TODO Auto-generated method stub
 
     }
 
 
 }
