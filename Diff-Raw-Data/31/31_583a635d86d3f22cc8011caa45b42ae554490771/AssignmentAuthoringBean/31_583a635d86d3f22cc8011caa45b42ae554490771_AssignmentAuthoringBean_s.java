 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/tool/src/java/org/sakaiproject/assignment2/tool/beans/Assignment2Bean.java $
  * $Id: Assignment2Bean.java 55216 2008-11-21 22:16:59Z swgithen@mtu.edu $
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
 
 package org.sakaiproject.assignment2.tool.beans;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.exception.AnnouncementPermissionException;
 import org.sakaiproject.assignment2.exception.ContentReviewException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.tool.ErrorOptionalTargettedMessageList;
 import org.sakaiproject.assignment2.tool.WorkFlowResult;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.user.api.UserNotDefinedException;
 import org.sakaiproject.util.FormattedText;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.MessageLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 /**
  * This bean houses the action methods (Post, Draft, Preview, etc) for the 
  * Assignment Authoring Workflow. It uses a FlowScope bean, 
  * AssignmentAuthoringFlowBean, to hold the Assignment2 object being edited
  * and acted upon.
  * 
  * @author sgithens
  *
  */
 public class AssignmentAuthoringBean {
 
     // I don't think this needs to be public
     private Assignment2 assignment = new Assignment2();
 
     private static final Log LOG = LogFactory.getLog(AssignmentAuthoringBean.class);
 
     //private static final String REMOVE = "remove";
 
     // Assignment Authoring Flow Scope Bean
     private AssignmentAuthoringFlowBean assignmentAuthoringFlowBean;
     public void setAssignmentAuthoringFlowBean(AssignmentAuthoringFlowBean assignmentAuthoringFlowBean) {
         this.assignmentAuthoringFlowBean = assignmentAuthoringFlowBean;
     }
 
     // Assignment Authoring Flow Scope Bean
     private AssignmentAuthoringOptionsFlowBean options;
     public void setAssignmentAuthoringOptionsFlowBean(AssignmentAuthoringOptionsFlowBean assignmentAuthoringOptionsFlowBean) {
         this.options = assignmentAuthoringOptionsFlowBean;
     }
 
     // Request Scope Dependency
     private ErrorOptionalTargettedMessageList messages;
     public void setMessages(ErrorOptionalTargettedMessageList messages) {
         this.messages = messages;
     }
 
     // Service Application Scope Dependency
     private AssignmentLogic logic;
     public void setLogic(AssignmentLogic logic) {
         this.logic = logic;
     }
 
 
 
     //private Map<String, Assignment2> OTPMap;
     //@SuppressWarnings("unchecked")
     //public void setAssignment2EntityBeanLocator(EntityBeanLocator entityBeanLocator) {
     //    this.OTPMap = entityBeanLocator.getDeliveredBeans();
     //}
 
     // Service Application Scope Dependency
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     // Request Scope Dependency
     private MessageLocator messageLocator;
     public void setMessageLocator (MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 
     // Request Scope Dependency
     private NotificationBean notificationBean;
     public void setNotificationBean (NotificationBean notificationBean) {
         this.notificationBean = notificationBean;
     }
 
     public WorkFlowResult processActionPost() {
         WorkFlowResult result = WorkFlowResult.INSTRUCTOR_POST_ASSIGNMENT;
 
         Assignment2 assignment = assignmentAuthoringFlowBean.getAssignment(); //OTPMap.get(key);
         result = internalProcessPost(assignment, Boolean.FALSE);
 
         // Notify students
         if (result.equals(WorkFlowResult.INSTRUCTOR_POST_ASSIGNMENT))
         {
             try
             {
                 notificationBean.notifyStudentsOfNewAssignment(assignment);
             }catch (IdUnusedException e)
             {
                 messages.addMessage(new TargettedMessage("assignment2.student-submit.error.unexpected",
                         new Object[] {e.getLocalizedMessage()}, TargettedMessage.SEVERITY_ERROR));
             }catch (UserNotDefinedException e)
             {
                 messages.addMessage(new TargettedMessage("assignment2.student-submit.error.unexpected",
                         new Object[] {e.getLocalizedMessage()}, TargettedMessage.SEVERITY_ERROR));
             }
         }
         return result;
     }
 
     private boolean areMessagesErrorFree() {
         if (messages.isOriginalError()) {
             return false;
         }
         else {
             return true;
         }
     }
     
     private WorkFlowResult internalProcessPost(Assignment2 assignment, Boolean draft){
         Boolean errorFound = false;
 
         assignment.setDraft(draft);
 
         if (options.getRequireAcceptUntil() == null || options.getRequireAcceptUntil() == Boolean.FALSE) {
             assignment.setAcceptUntilDate(null);
         }
 
         if (options.getRequireDueDate() == null || options.getRequireDueDate() == Boolean.FALSE) {
             assignment.setDueDate(null);
         }
         
         // make sure all input from WYSIWYG editors has been cleaned
         errorFound = !cleanUpAssignment(assignment);
 
         //Since in the UI, the select box bound to the gradebookItemId is always present
         // we need to manually remove this value if the assignment is ungraded
         if (!assignment.isGraded()) {
             assignment.setGradebookItemId(null);
         }
         
         if (assignment.isModelAnswerEnabled() == Boolean.FALSE)
         {
             assignment.setModelAnswerText(null);
             assignment.setModelAnswerDisplayRule(AssignmentConstants.MODEL_NEVER);
         }
         else
         {
             if ("".equals(assignment.getModelAnswerText().trim()))
             {
                 // make sure the text is null if it is blank
                 assignment.setModelAnswerText(null);
             }
         }
 
         //do groups
         Set<AssignmentGroup> newGroups = new HashSet<AssignmentGroup>();
         if (options.getRestrictedToGroups() != null && options.getRestrictedToGroups().equals(Boolean.TRUE.toString())){
 
             List<String> existingGroups = assignment.getListOfAssociatedGroupReferences();
 
             //now add any new groups
             if (assignment.getAssignmentGroupSet() != null) {
                 newGroups.addAll(assignment.getAssignmentGroupSet());
             } 
 
             Set<AssignmentGroup> remGroups = new HashSet<AssignmentGroup>();
             for (String selectedId : options.getSelectedIds().keySet()) {
                 if (options.getSelectedIds().get(selectedId) == Boolean.TRUE) {
                     // if it isn't already associated with this assignment, add it
                     if (existingGroups == null || (existingGroups != null && !existingGroups.contains(selectedId))) {
                         AssignmentGroup newGroup = new AssignmentGroup();
                         newGroup.setAssignment(assignment);
                         newGroup.setGroupId(selectedId);
                         newGroups.add(newGroup);
                     }
                 } else if (options.getSelectedIds().get(selectedId) == Boolean.FALSE) {
                     //then remove the group
                     for (AssignmentGroup ag : newGroups) {
                         if (ag.getGroupId().equals(selectedId)) {
                             remGroups.add(ag);
                         }
                     }
                 }
             }
             newGroups.removeAll(remGroups);
         }
         
         // Turnitin options ASNN-516
         if (assignment.isContentReviewEnabled()) {
             if (!assignment.isRequiresSubmission()) {
                 // we need to turn off turnitin since assignment doesn't accept submissions. the
                 // turnitin section was hidden via javascript
                 assignment.setContentReviewEnabled(false);
             } else if (!assignment.acceptsAttachments()) {
                 // double check that this assignment is set up to accept attachments. if not, turn TII off
                 // (this is done via javascript in the UI)
                 assignment.setContentReviewEnabled(false);
             }
         }
     
         if (options.getRestrictedToGroups() != null && options.getRestrictedToGroups().equals(Boolean.TRUE.toString()) && newGroups.size() < 1){
             messages.addMessage(new TargettedMessage("assignment2.assignment_post.no_groups"));
             errorFound = true;
         } 
 
         // we also need to iterate through all of the existing group associations and
         // remove those whose associated groups no longer exist. these won't show up
         // in the UI
         if (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()) {
             Set<AssignmentGroup> remGroups = new HashSet<AssignmentGroup>();
 
             Map<String, String> siteGroupToNameMap = externalLogic.getGroupIdToNameMapForSite(assignment.getContextId());           
             Set<String> siteGroupIds = new HashSet<String>();
             if (siteGroupToNameMap != null) {
                 siteGroupIds = siteGroupToNameMap.keySet();
             }
 
             for (AssignmentGroup group : assignment.getAssignmentGroupSet()) {
                 if (!siteGroupIds.contains(group.getGroupId())) {
                     remGroups.add(group);
                     if (LOG.isDebugEnabled()) LOG.debug("Removing assignment group with id: " + 
                             group.getGroupId() + " because associated site group no longer exists");
                 }
             }
 
             newGroups.removeAll(remGroups);
         }
         assignment.setAssignmentGroupSet(newGroups);
         
         // Check the targetted messages list for hard errors from other 
         // components or evolvers such as the date widget.
         //System.out.println("About to check the targetted messages!");
         if (!areMessagesErrorFree()) {
             errorFound = true;
         }
 
         //start the validator
         Assignment2Validator validator = new Assignment2Validator();
         if (validator.validate(assignment, messages) && !errorFound){
             //Validation Passed!
            try {

                logic.saveAssignment(assignment);
 
             } catch (ContentReviewException cre) {
                 messages.addMessage(new TargettedMessage("assignment2.turnitin.error.unable_to_save_tii",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_ERROR));
             }
             
 
             //set Messages
             if (draft) {
                 messages.addMessage(new TargettedMessage("assignment2.assignment_save_draft",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
             } 
             else if (options.getOtpkey().startsWith(EntityBeanLocator.NEW_PREFIX)) {
                 messages.addMessage(new TargettedMessage("assignment2.assignment_post",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
             }
             else {
                 messages.addMessage(new TargettedMessage("assignment2.assignment_save",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
             }
         } else {
             return WorkFlowResult.INSTRUCTOR_ASSIGNMENT_VALIDATION_FAILURE;
         }
         return WorkFlowResult.INSTRUCTOR_POST_ASSIGNMENT;
     }
 
 
     public WorkFlowResult processActionPreview() {
         WorkFlowResult returnCode = WorkFlowResult.INSTRUCTOR_ASSIGNMENT_FAILURE;
 
         Assignment2 assignment = assignmentAuthoringFlowBean.getAssignment(); // OTPMap.get(key);
         if (options.getRequireAcceptUntil() == null || Boolean.FALSE.equals(options.getRequireAcceptUntil())) {
             assignment.setAcceptUntilDate(null);
         }
         if (options.getRequireDueDate() == null || options.getRequireDueDate() == Boolean.FALSE) {
             assignment.setDueDate(null);
         }
         
         // check for malicious tags before rendering the preview
         if (!cleanUpAssignment(assignment)) {
             return WorkFlowResult.INSTRUCTOR_CONTINUE_EDITING_ASSIGNMENT;
         }
         
         if (!areMessagesErrorFree()) {
             return WorkFlowResult.INSTRUCTOR_CONTINUE_EDITING_ASSIGNMENT;
         }
         
         // TODO FIXME ASNN-295
         // previewAssignmentBean.setAssignment(assignment);
         // reviewAssignmentBean.setOTPKey(key);
 
 
         // Validation
 
         // SWG Put this back in.
         // Assignment2Validator validator = new Assignment2Validator();
         // if (validator.validate(assignment, messages)) {
         //     returnCode = WorkFlowResult.INSTRUCTOR_PREVIEW_ASSIGNMENT;
         // }
 
         //return returnCode;
         return WorkFlowResult.INSTRUCTOR_PREVIEW_ASSIGNMENT;
     }
 
     public WorkFlowResult processActionEdit() {
         return WorkFlowResult.INSTRUCTOR_CONTINUE_EDITING_ASSIGNMENT;
     }
 
     public WorkFlowResult processActionSaveDraft() {
         WorkFlowResult result = WorkFlowResult.INSTRUCTOR_SAVE_DRAFT_ASSIGNMENT;
 
         Assignment2 assignment = assignmentAuthoringFlowBean.getAssignment();
         result = internalProcessPost(assignment, Boolean.TRUE);
 
         return result;
     }
 
     public WorkFlowResult processActionCancel() {
         return WorkFlowResult.INSTRUCTOR_CANCEL_ASSIGNMENT;
     }
     
     /**
      * 
      * @param version
      * @return true if the text supplied by WYSIWYG editors on the assignment is
      * valid. false if the text contains malicious tags or attributes that need
      * to be addressed before any action (ie preview, saving) can proceed. Strips the text of extraneous
      * whitespace, as well
      */
     private boolean cleanUpAssignment(Assignment2 assignment) {
         boolean textValid = true;
         if (assignment != null) {
             if (assignment.getInstructions() != null) {
                 StringBuilder alertMsg = new StringBuilder();
                 assignment.setInstructions(FormattedText.
                         processFormattedText(assignment.getInstructions(), alertMsg, true, true));
                 if (alertMsg != null && alertMsg.length() > 0) {
                     messages.addMessage(new TargettedMessage("assignment2.error.assignment_instructions", 
                             new Object[] {alertMsg.toString()}, TargettedMessage.SEVERITY_ERROR));
                     textValid = false;
                 }
             }
             if (assignment.getModelAnswerText() != null) {
                 StringBuilder alertMsg = new StringBuilder();
                 assignment.setModelAnswerText(FormattedText.
                         processFormattedText(assignment.getModelAnswerText(), alertMsg, true, true));
                 if (alertMsg != null && alertMsg.length() > 0) {
                     messages.addMessage(new TargettedMessage("assignment2.error.model_answer_text", 
                             new Object[] {alertMsg.toString()}, TargettedMessage.SEVERITY_ERROR));
                     textValid = false;
                 }
             }
         }
         
         return textValid;
     }
 
 }
