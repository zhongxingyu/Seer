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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.params.FilePickerHelperViewParams;
 import org.sakaiproject.assignment2.tool.params.FragmentViewSubmissionViewParams;
 import org.sakaiproject.assignment2.tool.producers.AddAttachmentHelperProducer;
 import org.sakaiproject.assignment2.tool.producers.StudentAssignmentListProducer;
 import org.sakaiproject.assignment2.tool.producers.fragments.FragmentViewSubmissionProducer;
 import org.sakaiproject.assignment2.tool.producers.evolvers.AttachmentInputEvolver;
 import org.sakaiproject.tool.api.SessionManager;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.user.api.User;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIELBinding;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIJointContainer;
 import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
 import uk.org.ponder.rsf.components.UIMessage;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.components.decorators.DecoratorList;
 import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
 import uk.org.ponder.rsf.evolvers.TextInputEvolver;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 
 public class StudentViewAssignmentRenderer {
     private static Log log = LogFactory.getLog(StudentViewAssignmentRenderer.class);
 
     private Locale locale;
     public void setLocale(Locale locale) {
         this.locale = locale;
     }
 
     private MessageLocator messageLocator;
     public void setMessageLocator(MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     private AttachmentListRenderer attachmentListRenderer;
     public void setAttachmentListRenderer (AttachmentListRenderer attachmentListRenderer) {
         this.attachmentListRenderer = attachmentListRenderer;
     }
 
     private TextInputEvolver richTextEvolver;
     public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
         this.richTextEvolver = richTextEvolver;
     }
 
     private SessionManager sessionManager;
     public void setSessionManager(SessionManager sessionManager) {
         this.sessionManager = sessionManager;
     }
 
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     private AttachmentInputEvolver attachmentInputEvolver;
     public void setAttachmentInputEvolver(AttachmentInputEvolver attachmentInputEvolver){
         this.attachmentInputEvolver = attachmentInputEvolver;
     }
     
     private User currentUser;
     public void setCurrentUser(User currentUser) {
         this.currentUser = currentUser;
     }
     
     private ExternalGradebookLogic externalGradebookLogic;
     public void setExternalGradebookLogic(
             ExternalGradebookLogic externalGradebookLogic) {
         this.externalGradebookLogic = externalGradebookLogic;
     }
     
     private String curContext;
     public void setCurContext(String curContext) {
         this.curContext = curContext;
     }
 
     public void makeStudentView(UIContainer tofill, String divID, AssignmentSubmission assignmentSubmission, 
             Assignment2 assignment, ViewParameters params, String ASOTPKey, Boolean preview) {
         /**
          * Breadcrumbs
          */
         if (!preview) {
             UIInternalLink.make(tofill, "breadcrumb", 
                     messageLocator.getMessage("assignment2.student-assignment-list.heading"),
                     new SimpleViewParameters(StudentAssignmentListProducer.VIEW_ID));
         } else {
             UIMessage.make(tofill, "breadcrumb", "assignment2.student-assignment-list.heading");
         }
         
         String title = (assignment != null) ? assignment.getTitle() : "";
         
         String asvOTP = "AssignmentSubmissionVersion.";
         String asvOTPKey = "";
         if (assignmentSubmission != null && assignmentSubmission.getCurrentSubmissionVersion() != null 
                 && assignmentSubmission.getCurrentSubmissionVersion().isDraft() == Boolean.TRUE) {
             asvOTPKey += assignmentSubmission.getCurrentSubmissionVersion().getId();
         } else {
             asvOTPKey += EntityBeanLocator.NEW_PREFIX + "1";
         }
         asvOTP = asvOTP + asvOTPKey;
 
 
         if (assignmentSubmission != null) {
             assignmentSubmission.setAssignment(assignment);
         }
         UIJointContainer joint = new UIJointContainer(tofill, divID, "portletBody:", ""+1);
 
         // use a date which is related to the current users locale
         DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
 
         //For preview, get a decorated list of disabled="disabled"
         Map<String, String> disabledAttr = new HashMap<String, String>();
         disabledAttr.put("disabled", "disabled");
         DecoratorList disabledDecoratorList = new DecoratorList(new UIFreeAttributeDecorator(disabledAttr));
 
         Map<String, String> disabledLinkAttr = new HashMap<String, String>();
         disabledLinkAttr.put("onclick", "return false;");
         // DecoratorList disabledLinkDecoratorList = new DecoratorList(new UIFreeAttributeDecorator(disabledLinkAttr));
 
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
 
         /***
          * Title and Due Date Information
          */
         UIMessage.make(joint, "heading_status", "assignment2.student-submit.heading_status", 
                         new Object[]{ title, currentUser.getDisplayName() });
         
         if (assignment.getDueDate() == null) {
             UIMessage.make(joint, "due_date",
                     "assignment2.student-submit.no_due_date");
         }
         else {
             UIMessage.make(joint, "due_date",  
                     "assignment2.student-submit.due_date", 
                     new Object[] {df.format(assignment.getDueDate())});
         }
         
         /***
          * Assignment Details including:
          *   - Graded?
          *   - Points Possible
          *   - Resubmissions Allowed
          *   - Remaining Resubmissions Allowed
          *   - Grade
          *   - Comments
          */
         
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
         if (assignment.isGraded() && assignment.getGradableObjectId() != null) {
             try {
                 GradebookItem gradebookItem = 
                     externalGradebookLogic.getGradebookItemById(curContext, 
                             assignment.getGradableObjectId());
                 UIOutput.make(joint, "points-possible-row");
                 UIOutput.make(joint, "points-possible", gradebookItem.getPointsPossible().toString());      
                 
                 // Render the graded information if it's available.
                 String grade = externalGradebookLogic.getStudentGradeForItem(
                         curContext, currentUser.getId(), assignment.getGradableObjectId());
                 if (grade != null) {
                     UIOutput.make(joint, "grade-row");
                     UIOutput.make(joint, "grade", grade);
                 }
                 
                 String comment = externalGradebookLogic.getStudentGradeCommentForItem(
                         curContext, currentUser.getId(), assignment.getGradableObjectId());
                 
                 if (comment != null) {
                     UIOutput.make(joint, "comment-row");
                     UIOutput.make(joint, "comment", comment);
                 }
                 
             } catch (IllegalArgumentException iae) {
                 log.warn("Trying to look up grade object that doesn't exist" 
                         + "context: " + curContext 
                         + " gradeObjectId: " + assignment.getGradableObjectId() 
                         + "asnnId: " + assignment.getId());
             }
         }
         
         /*
          * Resubmissions Allowed
          */
         boolean resubmissionsAllowed = submissionLogic.submissionIsOpenForStudentForAssignment(
                 currentUser.getId(), assignment.getId());
         if (resubmissionsAllowed) {
             UIMessage.make(joint, "resubmissions-allowed", "assignment2.student-submit.resubmissions_allowed");
         }
         else {
             UIMessage.make(joint, "resubmissions-allowed", "assignment2.student-submit.resubmissions_not_allowed");
         }
         
         /*
          * Remaining resubmissions allowed
          */
         if (resubmissionsAllowed) {
             UIOutput.make(joint, "remaining-resubmissions-row");
             //TODO FIXME Michelle is adding in a Logic method to get this info.
             UIOutput.make(joint, "remaining-resubmissions", "This functionality in progress");
         }
         
         /*
          * Assignments Instructions
          * 
          * If there are no instructions show the "No Instructions... etc" 
          * message.
          * 
          * If there are attachments render the supporting materials header and
          * contents. Otherwise don't show any attachments stuff.
          * 
          * TODO FIXME At the moment there are ghost attachments showing up on
          * all the assignments, I think is related to ASNN-204
          * 
          */
         if (!preview && assignment.getInstructions() == null || assignment.getInstructions().equals("")) {
             UIMessage.make(joint, "instructions", "assignment2.student-submit.no_instructions");
         }
         else {
             UIVerbatim.make(joint, "instructions", assignment.getInstructions());
         }
 
         if (assignment.getAttachmentSet() != null && assignment.getAttachmentSet().size() > 0) {
             UIMessage.make(joint, "attachments-header", "assignment2.student-submit.additional_resources");
             
             if (!preview) {
                 Set<AssignmentAttachment> attachments = (assignment != null) ? assignment.getAttachmentSet() : null;
                 //If this is not a preview, then we need to just display the attachment set from the Assignment2 object
                 attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(joint, "attachment_list:", params.viewID, 
                         attachments);
             } 
             else {
                 attachmentListRenderer.makeAttachmentFromAssignmentAttachmentSet(joint, "attachment_list:", params.viewID, assignment.getAttachmentSet());
             }
         }
         
 
 
         UIForm form = UIForm.make(joint, "form");
         UIOutput.make(form, "submission_instructions"); //Fill in with submission type specific instructions
         UIVerbatim.make(form, "instructions", messageLocator.getMessage("assignment2.student-submit.instructions"));
 
         //Rich Text Input
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_ONLY || 
                 assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
 
             UIOutput.make(form, "submit_text");
             UIInput text = UIInput.make(form, "text:", asvOTP + ".submittedText");
             if (!preview) {
                 text.mustapply = Boolean.TRUE;
                 richTextEvolver.evolveTextInput(text);
             } else {
                 //disable textarea
 
                 UIInput text_disabled = UIInput.make(form, "text_disabled",asvOTP + ".submittedText");
                 text_disabled.decorators = disabledDecoratorList;
             }
 
         }
 
         //Attachment Stuff
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY ||
                 assignment.getSubmissionType() == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH){
             UIOutput.make(form, "submit_attachments");
 
             
             if (!preview) {
                 //Attachments
                 UIInputMany attachmentInput = UIInputMany.make(form, "attachment_list:", asvOTP + ".submittedAttachmentRefs", 
                         assignmentSubmission.getCurrentSubmissionVersion().getSubmittedAttachmentRefs());
                 attachmentInputEvolver.evolveAttachment(attachmentInput);
 
                 UIInternalLink.make(form, "add_submission_attachments", UIMessage.make("assignment2.student-submit.add_attachments"),
                         new FilePickerHelperViewParams(AddAttachmentHelperProducer.VIEWID, Boolean.TRUE, 
                                 Boolean.TRUE, 500, 700, ASOTPKey));
             }
         }
 
         // attachment only situations will not return any values in the OTP map; thus,
         // we won't enter the processing loop in processActionSubmit (and nothing will be saved)
         // this will force rsf to bind the otp mapping
         if (assignment.getSubmissionType() == AssignmentConstants.SUBMIT_ATTACH_ONLY) {
             UIInput.make(form, "submitted_text_for_attach_only", asvOTP + ".submittedText");
         }
 
         if (assignment.isHonorPledge()) {
             UIOutput.make(joint, "honor_pledge_fieldset");
             UIMessage honor_pledge_label = UIMessage.make(joint, "honor_pledge_label", "assignment2.student-submit.honor_pledge_text");
             UIBoundBoolean honor_pledge_checkbox = UIBoundBoolean.make(form, "honor_pledge", "#{AssignmentSubmissionBean.honorPledge}");
             UILabelTargetDecorator.targetLabel(honor_pledge_label, honor_pledge_checkbox);
         }
 
 
         //Begin Looping for previous submissions
         List<AssignmentSubmissionVersion> history = new ArrayList<AssignmentSubmissionVersion>();
         if (!preview) {
             history = submissionLogic.getVersionHistoryForSubmission(assignmentSubmission);
         }
 
         for (AssignmentSubmissionVersion asv : history){
             if (asv.isDraft()) { 
                 continue;
             }
 
             UIBranchContainer loop = UIBranchContainer.make(form, "previous_submissions:");
             UIOutput.make(loop, "previous_date", (asv.getSubmittedDate() != null ? df.format(asv.getSubmittedDate()) : ""));
             if (asvOTPKey.equals(asv.getId().toString())){
                 //we are editing this version
                 UIMessage.make(loop, "current_version", "assignment2.student-submit.current_version");
             } else {
                 //else add link to edit this submission
                 UIInternalLink.make(loop, "previous_link", 
                         messageLocator.getMessage("assignment2.assignment_grade.view_submission"),
                         new FragmentViewSubmissionViewParams(FragmentViewSubmissionProducer.VIEW_ID, asv.getId()));
             }
         }
         if (history == null || history.size() == 0) {
             //no history, add dialog
             UIMessage.make(form, "no_history", "assignment2.student-submit.no_history");
         }
 
         form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.ASOTPKey}", ASOTPKey));
         form.parameters.add( new UIELBinding("#{AssignmentSubmissionBean.assignmentId}", assignment.getId()));
 
         //Buttons
         UICommand submit_button = UICommand.make(form, "submit_button", UIMessage.make("assignment2.student-submit.submit"), 
         "#{AssignmentSubmissionBean.processActionSubmit}");
         UICommand preview_button = UICommand.make(form, "preview_button", UIMessage.make("assignment2.student-submit.preview"), 
         "#{AssignmentSubmissionBean.processActionPreview}");
         UICommand save_button = UICommand.make(form, "save_draft_button", UIMessage.make("assignment2.student-submit.save_draft"), 
         "#{AssignmentSubmissionBean.processActionSaveDraft}");
         UICommand cancel_button = UICommand.make(form, "cancel_button", UIMessage.make("assignment2.student-submit.cancel"), 
         "#{AssignmentSubmissionBean.processActionCancel}");
 
         if (preview) {
             submit_button.decorators = disabledDecoratorList;
             preview_button.decorators = disabledDecoratorList;
             save_button.decorators = disabledDecoratorList;
             cancel_button.decorators = disabledDecoratorList;
         }
     }
 }
