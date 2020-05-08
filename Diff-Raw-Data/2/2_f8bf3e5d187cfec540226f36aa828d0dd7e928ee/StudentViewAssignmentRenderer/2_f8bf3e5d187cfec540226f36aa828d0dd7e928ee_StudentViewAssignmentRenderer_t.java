 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007, 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.assignment2.tool.producers.renderers;
 
 import java.text.DateFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Locale;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.StudentAction;
 import org.sakaiproject.assignment2.tool.beans.AssignmentSubmissionBean;
 import org.sakaiproject.assignment2.tool.producers.StudentAssignmentListProducer;
 import org.sakaiproject.user.api.User;
 
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 
 /**
  * Renders the Students view of an assignments details and instructions, as well
  * as completing the material and viewing previous submissions and feedback.
  * 
  * @author rjlowe
  * @author sgithens
  *
  */
 public class StudentViewAssignmentRenderer {
     private static Log log = LogFactory.getLog(StudentViewAssignmentRenderer.class);
 
     private static final String STUDENT_SUBMISSION_DIVID = "student-view-assignment-area:";
     
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
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
     
     // Dependency
     private AsnnSubmissionDetailsRenderer asnnSubmissionDetailsRenderer;
     public void setAsnnSubmissionDetailsRenderer(AsnnSubmissionDetailsRenderer asnnSubmissionDetailsRenderer) {
         this.asnnSubmissionDetailsRenderer = asnnSubmissionDetailsRenderer;
     }
     
     // Dependency
     private AsnnInstructionsRenderer asnnInstructionsRenderer;
     public void setAsnnInstructionsRenderer(AsnnInstructionsRenderer asnnInstructionsRenderer) {
         this.asnnInstructionsRenderer = asnnInstructionsRenderer;
     }
 
     // Dependency
     private AsnnSubmitEditorRenderer asnnSubmitEditorRenderer;
     public void setAsnnSubmitEditorRenderer(AsnnSubmitEditorRenderer asnnSubmitEditorRenderer) {
         this.asnnSubmitEditorRenderer = asnnSubmitEditorRenderer;
     }
     
     // Dependency
     private AsnnSubmissionHistoryRenderer asnnSubmissionHistoryRenderer;
     public void setAsnnSubmissionHistoryRenderer(AsnnSubmissionHistoryRenderer asnnSubmissionHistoryRenderer) {
         this.asnnSubmissionHistoryRenderer = asnnSubmissionHistoryRenderer;
     }
     
     // Dependency
     private AsnnSubmissionVersionRenderer asnnSubmissionVersionRenderer;
     public void setAsnnSubmissionVersionRenderer(
             AsnnSubmissionVersionRenderer asnnSubmissionVersionRenderer) {
         this.asnnSubmissionVersionRenderer = asnnSubmissionVersionRenderer;
     }
     
     // Dependency
     private User currentUser;
     public void setCurrentUser(User currentUser) {
         this.currentUser = currentUser;
     }
     
     // Dependency
     private AssignmentSubmissionBean submissionBean;
     public void setAssignmentSubmissionBean(AssignmentSubmissionBean submissionBean) {
         this.submissionBean = submissionBean;
     }
     
     /**
      * It's important to note that the boolean preview on this method is for
      * previewing what the assignment will look like to a student. It is not the
      * Preview Submission view for when the student is about to submit.
      * 
      * @param tofill
      * @param divID
      * @param assignmentSubmission
      * @param assignment
      * @param params
      * @param ASOTPKey
      * @param previewAsStudent
      */
     public void makeStudentView(UIContainer tofill, String divID, AssignmentSubmission assignmentSubmission, 
             Assignment2 assignment, ViewParameters params, String ASOTPKey, Boolean previewAsStudent, Boolean studentSubmissionPreview) {
         /**
          * Breadcrumbs
          */
         if (!previewAsStudent) {
             UIInternalLink.make(tofill, "breadcrumb", 
                     messageLocator.getMessage("assignment2.student-assignment-list.heading"),
                     new SimpleViewParameters(StudentAssignmentListProducer.VIEW_ID));
         } else {
             UIMessage.make(tofill, "breadcrumb", "assignment2.student-assignment-list.heading");
         }
         
         if (!previewAsStudent) {
             StudentAction studentAction = submissionBean.determineStudentAction(assignmentSubmission.getUserId(), assignment.getId());
            UIOutput.make(tofill, "student-submit-heading", messageLocator.getMessage("assignment2.student-assignment-list.action." + studentAction.toString().toLowerCase()));
         }
             
         if (assignmentSubmission != null) {
             assignmentSubmission.setAssignment(assignment);
         }
         UIJointContainer joint = new UIJointContainer(tofill, divID, STUDENT_SUBMISSION_DIVID, ""+1);
 
         // use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //For preview, get a decorated list of disabled="disabled"
         Map<String, String> disabledAttr = new HashMap<String, String>();
         disabledAttr.put("disabled", "disabled");
 
         Map<String, String> disabledLinkAttr = new HashMap<String, String>();
         disabledLinkAttr.put("onclick", "return false;");
 
         // set the textual representation of the submission status
         String status = "";
         int statusConstant = AssignmentConstants.SUBMISSION_NOT_STARTED;
         if (assignmentSubmission != null) {
             statusConstant = submissionLogic.getSubmissionStatusConstantForCurrentVersion(
                     assignmentSubmission.getCurrentSubmissionVersion(), assignment.getDueDate());
             status = messageLocator.getMessage(
                     "assignment2.student-submit.status." + 
                     statusConstant);
         }
         
         /* 
          * If the Student is previewing their submission, only want to show the
          * text and attachments of that submission.
          */
         if (!studentSubmissionPreview) {
             asnnSubmissionDetailsRenderer.fillComponents(joint, "assignment-details:", assignmentSubmission, previewAsStudent);
         
             asnnInstructionsRenderer.fillComponents(joint, "assignment-instructions:", assignment);
         
             // Submission History
             if (!previewAsStudent) {
                 asnnSubmissionHistoryRenderer.fillComponents(joint, "assignment-previous-submissions:", assignmentSubmission);
             }
         }
         else {
             asnnSubmissionDetailsRenderer.fillComponents(joint, "assignment-details:", assignmentSubmission, previewAsStudent, true);
         }
             
         if (previewAsStudent) {
             asnnSubmitEditorRenderer.fillComponents(joint, "assignment-edit-submission:", assignmentSubmission, true, false);
         }
         else if (submissionLogic.isSubmissionOpenForStudentForAssignment(currentUser.getId(), assignment.getId())) {
             asnnSubmitEditorRenderer.fillComponents(joint, "assignment-edit-submission:", assignmentSubmission, previewAsStudent, studentSubmissionPreview);
         }
         
     }
 }
