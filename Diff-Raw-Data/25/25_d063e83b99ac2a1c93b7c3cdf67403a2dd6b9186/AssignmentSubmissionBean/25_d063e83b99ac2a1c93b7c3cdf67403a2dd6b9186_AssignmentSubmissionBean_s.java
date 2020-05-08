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
 
 package org.sakaiproject.assignment2.tool.beans;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.user.api.UserNotDefinedException;
 
 import uk.org.ponder.beanutil.entity.EntityBeanLocator;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 public class AssignmentSubmissionBean {
 
     public static final String SUBMIT = "submit";
     public static final String PREVIEW = "preview";
     public static final String SAVE_DRAFT = "save_draft";
     private static final String EDIT = "edit";
     public static final String CANCEL = "cancel";
     private static final String FAILURE = "failure";
     private static final String RELEASE_ALL= "release_all";
     
     // for determining available student action(s)
     public static final int VIEW_DETAILS = 0;
     public static final int VIEW_AND_SUBMIT = 1;
     public static final int VIEW_AND_RESUBMIT = 2;
     public static final int VIEW_SUB = 3;
     public static final int VIEW_ALL_SUB = 4;
 
     public Map<String, Boolean> selectedIds = new HashMap<String, Boolean>();
     public Long assignmentId;
     public String ASOTPKey;
     public String userId;
     public Boolean releaseFeedback;
 
     private TargettedMessageList messages;
     public void setMessages(TargettedMessageList messages) {
         this.messages = messages;
     }
 
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     private AssignmentSubmissionLogic submissionLogic;
     public void setSubmissionLogic(AssignmentSubmissionLogic submissionLogic) {
         this.submissionLogic = submissionLogic;
     }
 
     private Map<String, AssignmentSubmission> OTPMap;
     private EntityBeanLocator asEntityBeanLocator;
     @SuppressWarnings("unchecked")
     public void setAssignmentSubmissionEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.OTPMap = entityBeanLocator.getDeliveredBeans();
         this.asEntityBeanLocator = entityBeanLocator;
     }
 
     private Map<String, AssignmentSubmissionVersion> asvOTPMap;
     public void setAsvEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
         this.asvOTPMap = entityBeanLocator.getDeliveredBeans();
     }
 
    private PreviewAssignmentSubmissionBean previewAssignmentSubmissionBean;
    public void setPreviewAssignmentSubmissionBean (PreviewAssignmentSubmissionBean previewAssignmentSubmissionBean) {
        this.previewAssignmentSubmissionBean = previewAssignmentSubmissionBean;
    }
 
     private Boolean honorPledge;
     public void setHonorPledge(Boolean honorPledge) {
         this.honorPledge = honorPledge;
     }
 
     private NotificationBean notificationBean;
     public void setNotificationBean(NotificationBean notificationBean) {
         this.notificationBean = notificationBean;
     }
     
     private Boolean overrideResubmissionSettings;
     public void setOverrideResubmissionSettings(Boolean overrideResubmissionSettings) {
         this.overrideResubmissionSettings = overrideResubmissionSettings;
     }
 
     private Boolean resubmitUntil;
     public void setResubmitUntil(Boolean resubmitUntil) {
         this.resubmitUntil = resubmitUntil;
     }
 
     /*
      * STUDENT FUNCTIONS
      */
     public String processActionSubmit(){
         if (assignmentId == null ) {
             return FAILURE;
         }
 
         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
         assignmentSubmission.setAssignment(assignment);
 
         for (String key : asvOTPMap.keySet()) {
             AssignmentSubmissionVersion asv = asvOTPMap.get(key);
 
             asv.setAssignmentSubmission(assignmentSubmission);
             asv.setDraft(Boolean.FALSE);
 
             //check whether honor pledge was added if required
             if (assignment.isHonorPledge() && !(this.honorPledge != null && Boolean.TRUE.equals(honorPledge))) {
                 messages.addMessage(new TargettedMessage("assignment2.student-submit.error.honor_pledge_required",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_ERROR));
                 return FAILURE;
             }else {
                 submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(), 
                         assignmentSubmission.getAssignment(), false, asv.getSubmittedText(), asv.getSubmissionAttachSet());
                 messages.addMessage(new TargettedMessage("assignment2.student-submit.info.submission_submitted",
                         new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
                 
                 // Send out notifications
                 if (assignment.isSendSubmissionNotifications()) {
                     AssignmentSubmission newSubmission = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(assignmentId, assignmentSubmission.getUserId());            
                     notificationBean.notifyStudentThatSubmissionWasAccepted(newSubmission);
                     notificationBean.notifyInstructorsOfSubmission(newSubmission);
                 }
 
             }
         }
         return SUBMIT;
     }
 
     public String processActionPreview(){
         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
        previewAssignmentSubmissionBean.setAssignmentSubmission(assignmentSubmission);
        for (String key : asvOTPMap.keySet()) {
            AssignmentSubmissionVersion asv = asvOTPMap.get(key);
            previewAssignmentSubmissionBean.setAssignmentSubmissionVersion(asv);
        }
         return PREVIEW;
     }
 
     public String processActionSaveDraft() {
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
         AssignmentSubmission assignmentSubmission = (AssignmentSubmission) asEntityBeanLocator.locateBean(ASOTPKey);
         if (assignmentId == null){
             return FAILURE;
         }
         assignmentSubmission.setAssignment(assignment);
         for (String key : asvOTPMap.keySet()) {
             AssignmentSubmissionVersion asv = (AssignmentSubmissionVersion) asvOTPMap.get(key);
 
             asv.setAssignmentSubmission(assignmentSubmission);
             asv.setDraft(Boolean.TRUE);
 
             submissionLogic.saveStudentSubmission(assignmentSubmission.getUserId(),
                     assignmentSubmission.getAssignment(), true, asv.getSubmittedText(),
                     asv.getSubmissionAttachSet());
             messages.addMessage(new TargettedMessage("assignment2.student-submit.info.submission_save_draft",
                     new Object[] { assignment.getTitle() }, TargettedMessage.SEVERITY_INFO));
         }
         return SAVE_DRAFT;
     }
 
     /*
      * INSTRUCTOR FUNCTIONS
      */
     public String processActionReleaseAllFeedbackForAssignment() {
         if (this.assignmentId != null) {
             submissionLogic.releaseAllFeedbackForAssignment(assignmentId);
         }
 
         return RELEASE_ALL;
     }
 
     public String processActionSaveAndReleaseAllFeedbackForSubmission(){
         this.releaseFeedback = true;
         processActionGradeSubmit();
 
         for (String key : OTPMap.keySet()) {
             AssignmentSubmission as = OTPMap.get(key);
             Long subId = as.getId();
             if (subId == null) {
                 // we need to retrieve the newly created submission
                 AssignmentSubmission sub = submissionLogic.getCurrentSubmissionByAssignmentIdAndStudentId(
                         as.getAssignment().getId(), as.getUserId());
                 subId = sub.getId();
             }
 
             submissionLogic.releaseAllFeedbackForSubmission(subId);
         }
 
         return SUBMIT;
     }
 
     public String processActionGradeSubmit(){
         if (assignmentId == null || userId == null){
             return FAILURE;
         }
         Assignment2 assignment = assignmentLogic.getAssignmentById(assignmentId);
         AssignmentSubmission assignmentSubmission = new AssignmentSubmission(assignment, userId);
 
         for (String key : OTPMap.keySet()){
             assignmentSubmission = OTPMap.get(key);
             assignmentSubmission.setAssignment(assignment);
             assignmentSubmission.setUserId(userId);
 
             if (resubmitUntil == null || Boolean.FALSE.equals(resubmitUntil)) {
                 assignmentSubmission.setResubmitCloseDate(null);
             }
             
             if (overrideResubmissionSettings == null || !overrideResubmissionSettings) {
                 assignmentSubmission.setNumSubmissionsAllowed(null);
                 assignmentSubmission.setResubmitCloseDate(null);
             }
         }
         for (String key : asvOTPMap.keySet()){
 
             AssignmentSubmissionVersion asv = asvOTPMap.get(key);
 
             asv.setAssignmentSubmission(assignmentSubmission);
             if (this.releaseFeedback != null && asv.getFeedbackReleasedDate() == null) {
                 asv.setFeedbackReleasedDate(new Date());
             }
 
             submissionLogic.saveInstructorFeedback(asv.getId(), assignmentSubmission.getUserId(),
                     assignmentSubmission.getAssignment(), assignmentSubmission.getNumSubmissionsAllowed(),
                     assignmentSubmission.getResubmitCloseDate(), asv.getAnnotatedText(), asv.getFeedbackNotes(),
                     asv.getFeedbackReleasedDate(), asv.getFeedbackAttachSet());
         }
         return SUBMIT;
     }
 
     public String processActionGradePreview(){
         for (String key : OTPMap.keySet()){
             AssignmentSubmission assignmentSubmission = OTPMap.get(key);
             Assignment2 assignment = assignmentLogic.getAssignmentByIdWithAssociatedData(assignmentId);
             assignmentSubmission.setAssignment(assignment);
            previewAssignmentSubmissionBean.setAssignmentSubmission(assignmentSubmission);
         }
         for (String key : asvOTPMap.keySet()){
             AssignmentSubmissionVersion asv = asvOTPMap.get(key);
            previewAssignmentSubmissionBean.setAssignmentSubmissionVersion(asv);
         }
         return PREVIEW;
     }
 
     public String processActionCancel() {
         return CANCEL;
     }
     
     public int determineStudentAction(String studentId, Long assignmentId) {
         boolean isOpenForSubmission = submissionLogic.isSubmissionOpenForStudentForAssignment(
                 studentId, assignmentId);
 
         int numSubmittedVersions = submissionLogic.getNumSubmittedVersions(studentId, assignmentId);
 
         int action = VIEW_DETAILS;
 
         // 1. View Details and Submit
         if (isOpenForSubmission && numSubmittedVersions < 1) {
             action = VIEW_AND_SUBMIT;
         }
         // 3. Resubmit
         else if (isOpenForSubmission && numSubmittedVersions >= 1) {
             action = VIEW_AND_RESUBMIT;
         }
         // 4a View Submission
         else if (numSubmittedVersions == 1) {
             action = VIEW_SUB;
         }
         // 4b View Submissions
         else if (numSubmittedVersions > 1) {
             action = VIEW_ALL_SUB;
         }
         // 2 View Details
         else {
             action = VIEW_DETAILS;
         }
 
         return action;
     }
 
 }
