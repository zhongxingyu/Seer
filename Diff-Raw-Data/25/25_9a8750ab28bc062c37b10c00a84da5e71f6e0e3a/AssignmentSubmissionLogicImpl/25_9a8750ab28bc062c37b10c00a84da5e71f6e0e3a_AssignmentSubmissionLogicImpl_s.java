 /**********************************************************************************
  * $URL:https://source.sakaiproject.org/contrib/assignment2/trunk/impl/logic/src/java/org/sakaiproject/assignment2/logic/impl/AssignmentSubmissionLogicImpl.java $
  * $Id:AssignmentSubmissionLogicImpl.java 48274 2008-04-23 20:07:00Z wagnermr@iupui.edu $
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
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
 
 package org.sakaiproject.assignment2.logic.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.StaleObjectStateException;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.StaleObjectModificationException;
 import org.sakaiproject.assignment2.exception.SubmissionClosedException;
 import org.sakaiproject.assignment2.exception.SubmissionNotFoundException;
 import org.sakaiproject.assignment2.exception.VersionNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.AssignmentSubmissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalContentReviewLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.logic.utils.Assignment2Utils;
 import org.sakaiproject.assignment2.logic.utils.ComparatorsUtils;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 import org.sakaiproject.assignment2.model.SubmissionAttachmentBase;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.genericdao.api.search.Search;
 import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
 
 /**
  * This is the interface for interaction with the AssignmentSubmission object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentSubmissionLogicImpl implements AssignmentSubmissionLogic{
 
     private static Log log = LogFactory.getLog(AssignmentSubmissionLogicImpl.class);
 
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     private AssignmentDao dao;
     public void setDao(AssignmentDao dao) {
         this.dao = dao;
     }
 
     private ExternalGradebookLogic gradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
 
     private AssignmentPermissionLogic permissionLogic;
     public void setPermissionLogic(AssignmentPermissionLogic permissionLogic) {
         this.permissionLogic = permissionLogic;
     }
 
     private AssignmentLogic assignmentLogic;
     public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
         this.assignmentLogic = assignmentLogic;
     }
 
     private ExternalContentReviewLogic contentReviewLogic;
     public void setExternalContentReviewLogic(ExternalContentReviewLogic contentReviewLogic)
     {
         this.contentReviewLogic = contentReviewLogic;
     }
 
     public void init(){
         if (log.isDebugEnabled()) log.debug("init");
     }
 
     public AssignmentSubmission getAssignmentSubmissionById(Long submissionId, Map<String, Object> optionalParameters){
         if (submissionId == null) {
             throw new IllegalArgumentException("Null submissionId passed to getAssignmentSubmissionById");
         }
 
         AssignmentSubmission submission =  (AssignmentSubmission) dao.findById(AssignmentSubmission.class, submissionId);
         if (submission == null) {
             throw new SubmissionNotFoundException("No submission found with id: " + submissionId);
         }
 
         String currentUserId = externalLogic.getCurrentUserId();
 
         // if the submission rec exists, we need to grab the most current version
 
         Assignment2 assignment = submission.getAssignment();
 
         if (!permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, submission.getUserId(), assignment.getId(), optionalParameters)) {
             throw new SecurityException("user" + currentUserId + " attempted to view submission with id " + submissionId + " but is not authorized");
         }
 
         AssignmentSubmissionVersion currentVersion = dao.getCurrentSubmissionVersionWithAttachments(submission);
 
         if (currentVersion != null) {
             currentVersion = getFilteredVersion(currentVersion, currentUserId);
         }
 
         submission.setCurrentSubmissionVersion(currentVersion);
 
         return submission;
     }
 
     public AssignmentSubmissionVersion getSubmissionVersionById(Long submissionVersionId) {
         if (submissionVersionId == null) {
             throw new IllegalArgumentException("null submissionVersionId passed to getSubmissionVersionById");
         }
 
         AssignmentSubmissionVersion version = dao.getAssignmentSubmissionVersionByIdWithAttachments(submissionVersionId);
 
         if (version == null) {
             throw new VersionNotFoundException("No AssignmentSubmissionVersion exists with id: " + submissionVersionId);
         }
 
         String currentUserId = externalLogic.getCurrentUserId();
 
         if (version != null) {		
             AssignmentSubmission submission = version.getAssignmentSubmission();
             Assignment2 assignment = submission.getAssignment();
 
             // ensure that the current user is authorized to view this user for this assignment
             if (!permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, submission.getUserId(), assignment.getId(), null)) {
                 throw new SecurityException("User " + currentUserId + " attempted to access the version " + 
                         submissionVersionId + " for student " + submission.getUserId() + " without authorization");
             }
 
             version = getFilteredVersion(version, currentUserId);
             if (version == null) {
                 log.warn("Version with id:" + submissionVersionId + " for curr user: " + 
                         currentUserId + " was null after it was filtered for restricted information. This" +
                 "is possibly due to a submitter trying to access a feedback-only version that has not been released");
             }
         }
 
         return version;
     }
 
     public AssignmentSubmission getCurrentSubmissionByAssignmentIdAndStudentId(Long assignmentId, String studentId, Map<String, Object> optionalParameters) {
         if (assignmentId == null || studentId == null) {
             throw new IllegalArgumentException("Null assignmentId or userId passed to getCurrentSubmissionByAssignmentAndUser");
         }
 
         String currentUserId = externalLogic.getCurrentUserId();
 
         Assignment2 assignment = dao.getAssignmentByIdWithGroups(assignmentId);
 
         if (assignment == null) {
             throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
         }
 
         if (!permissionLogic.isUserAllowedToViewSubmissionForAssignment(currentUserId, studentId, assignment.getId(), optionalParameters)) {
             throw new SecurityException("Current user " + currentUserId + " is not allowed to view submission for " + studentId + " for assignment " + assignment.getId());
         }
 
         AssignmentSubmission submission = dao.getSubmissionWithVersionHistoryForStudentAndAssignment(studentId, assignment);
 
         if (submission == null) {
             // return an "empty" submission
             submission = new AssignmentSubmission(assignment, studentId);
         } else {
 
             filterOutRestrictedInfo(submission, currentUserId, true);
         } 
 
         return submission;
     }
 
     public void saveStudentSubmission(String userId, Assignment2 assignment, boolean draft, 
             String submittedText, Set<SubmissionAttachment> subAttachSet, boolean saveAsDraftIfClosed) {
         if (userId == null || assignment == null) {
             throw new IllegalArgumentException("null userId, or assignment passed to saveAssignmentSubmission");
         }
 
         if (assignment.getId() == null) {
             throw new IllegalArgumentException("assignment without an id passed to saveStudentSubmission");
         }
 
         Date currentTime = new Date();
         String currentUserId = externalLogic.getCurrentUserId();
 
         if (!currentUserId.equals(userId)) {
             throw new SecurityException("User " + currentUserId + " attempted to save a submission for " +
                     userId + ". You may only make a submission for yourself!");
         }
 
         if (!permissionLogic.isUserAllowedToMakeSubmissionForAssignment(currentUserId, assignment)) {
             log.warn("User " + currentUserId + " attempted to make a submission " +
                     "without authorization for assignment " + assignment.getId());
             throw new SecurityException("User " + currentUserId + " attempted to make a submission " +
                     "without authorization for assignment " + assignment.getId());
         }
 
         if (!isSubmissionOpenForStudentForAssignment(currentUserId, assignment.getId())) {
             if (saveAsDraftIfClosed) {
                 if (log.isDebugEnabled()) log.debug("Saving submission as draft since submission is closed for this student");
                 draft = true;
             } else {
                 log.warn("User " + currentUserId + " attempted to make a submission " +
                         "but submission for this user for assignment " + assignment.getId() + " is closed.");
                 throw new SubmissionClosedException("User " + currentUserId + " attempted to make a submission " +
                         "for closed assignment " + assignment.getId());
             }
         }
 
         // if there is no current version or the most recent version was submitted, we will need
         // to create a new version. If the current version is draft, we will continue to update
         // this version until it is submitted
 
         // retrieve the latest submission for this user and assignment
         AssignmentSubmission submission = getAssignmentSubmissionForStudentAndAssignment(assignment, userId);
         AssignmentSubmissionVersion version = null;
 
         // clean up submittedText
         submittedText = Assignment2Utils.cleanupHtmlText(submittedText);
 
         boolean isAnUpdate = false;
 
         if (submission == null) {
             // there is no submission for this user yet
             submission = new AssignmentSubmission(assignment, userId);
             submission.setCreatedBy(currentUserId);
             submission.setCreatedDate(currentTime);
             submission.setModifiedBy(currentUserId);
             submission.setModifiedDate(currentTime);
 
             // we need to create a new version for this submission
             version = new AssignmentSubmissionVersion();
         } else {
             // the submission exists, so we need to check the current version
             version = dao.getCurrentSubmissionVersionWithAttachments(submission);
 
             // if the current version hasn't been submitted yet, we will
             // update that one. otherwise, create a new version
             // note: the submittedVersionNumber = 0 is reserved for feedback-only
             // versions. if the current version is number 0, we need to 
             // create a new version
             if (version != null && version.getSubmittedDate() == null && 
                     version.getSubmittedVersionNumber() != AssignmentSubmissionVersion.FEEDBACK_ONLY_VERSION_NUMBER) {
 
                 isAnUpdate = true;
 
             } else {
                 // we need to create a new version for this submission
                 version = new AssignmentSubmissionVersion();
             }
         }
 
         // now let's set the new data
         version.setAssignmentSubmission(submission);
         version.setDraft(draft);
         version.setSubmittedText(submittedText);
 
         if (!isAnUpdate) {
             version.setCreatedBy(currentUserId);
             version.setCreatedDate(currentTime);
 
             // set the version number for this submission - if this submission
             // doesn't exist yet, set the version to 1 --> FEEDBACK_ONLY_VERSION_NUMBER is reserved for feedback before submission
             if (submission.getId() == null) {
                 version.setSubmittedVersionNumber(AssignmentSubmissionVersion.FEEDBACK_ONLY_VERSION_NUMBER + 1);
             } else {
                 version.setSubmittedVersionNumber(dao.getHighestSubmittedVersionNumber(submission) + 1);
             }
         }
 
         version.setModifiedBy(currentUserId);
         version.setModifiedDate(currentTime);
         version.setStudentSaveDate(currentTime);
 
         if (!version.isDraft()) {
             version.setSubmittedDate(currentTime);
 
             // if this isn't a draft, set the annotated text to be the submitted text
             // to allow instructor to provide inline comments for submitted text
             version.setAnnotatedText(submittedText);
             // mark this submission as completed
             submission.setCompleted(true);
             submission.setModifiedBy(currentUserId);
             submission.setModifiedDate(currentTime);
         }
 
         // identify any attachments that were deleted or need to be created
         // - we don't update attachments
         Set<SubmissionAttachmentBase> attachToDelete = identifyAttachmentsToDelete(version.getSubmissionAttachSet(), subAttachSet);
         Set<SubmissionAttachmentBase> attachToCreate = identifyAttachmentsToCreate(version.getSubmissionAttachSet(), subAttachSet);
 
         // make sure the version was populated on the SubmissionAttachments
         populateVersionForAttachmentSet(attachToCreate, version);
         populateVersionForAttachmentSet(attachToDelete, version);
 
         // double check that the attachments we are creating are valid for saving
         if (attachToCreate != null) {
             for (SubmissionAttachmentBase attach : attachToCreate) {
                 if (!attach.isAttachmentValid()) {
                     throw new IllegalArgumentException("The subAttachSet passed to saveStudentSubmission" +
                             " contained invalid attachment(s). Please ensure all required fields are populated " +
                     " before saving.");
                 }
             }
         }
 
         try {
 
             Set<AssignmentSubmissionVersion> versionSet = new HashSet<AssignmentSubmissionVersion>();
             versionSet.add(version);
 
             Set<AssignmentSubmission> submissionSet = new HashSet<AssignmentSubmission>();
             submissionSet.add(submission);
 
             // this allows us to avoid the "empty set so nothing to do" warning
             if (attachToCreate != null && !attachToCreate.isEmpty()) {
                 dao.saveMixedSet(new Set[] {submissionSet, versionSet, attachToCreate});
             } else {
                 dao.saveMixedSet(new Set[] {submissionSet, versionSet});
             }
 
             if (log.isDebugEnabled()) log.debug("Updated/Added student submission version " + 
                     version.getId() + " for user " + submission.getUserId() + " for assignment " + 
                     submission.getAssignment().getTitle()+ " ID: " + submission.getAssignment().getId());
             if (log.isDebugEnabled()) log.debug("New submission attachments created: " + attachToCreate.size());
 
             if (attachToDelete != null && !attachToDelete.isEmpty()) {
                 dao.deleteSet(attachToDelete);
                 if (log.isDebugEnabled()) log.debug("Removed " + attachToDelete.size() + 
                         "sub attachments deleted for updated version " + 
                         version.getId() + " by user " + currentUserId);
             }
         } catch (HibernateOptimisticLockingFailureException holfe) {
             if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
             throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), holfe);
         } catch (StaleObjectStateException sose) {
             if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
             throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), sose);
         }
         
         // ASNN-516 now let's run these attachments through the content review service, if appropriate
         if (!version.isDraft() && assignment.isContentReviewEnabled() && subAttachSet != null) {
             if (contentReviewLogic.isContentReviewAvailable(assignment.getContextId())) {
                 for (SubmissionAttachment att : subAttachSet) {
                     if (contentReviewLogic.isAttachmentAcceptableForReview(att.getAttachmentReference())) {
                         if (log.isDebugEnabled()) log.debug("Adding attachment " + att.getAttachmentReference() + " to review queue for student " + userId);
                         contentReviewLogic.reviewAttachment(userId, assignment, att.getAttachmentReference());
                     }
                 }
             }
         }
     }
 
     public Set<String> saveAllInstructorFeedback(Assignment2 assignment, Map<String, Collection<AssignmentSubmissionVersion>> studentUidVersionsMap) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment passed to saveInstructorFeedback");
         }
 
         Set<String> studentsUpdated = new HashSet<String>();
 
         if (studentUidVersionsMap != null && !studentUidVersionsMap.isEmpty()) {
             String currUserId = externalLogic.getCurrentUserId();
             Date currentTime = new Date();
 
             // check to see if user may manage submissions
             List<String> manageableStudents = permissionLogic.getViewableStudentsForAssignment(currUserId, assignment);
             if (manageableStudents == null || manageableStudents.isEmpty()) {
                 throw new SecurityException("User " + currUserId + " attempted to update feedback for students w/o permission");
             }
 
 
             for (String studentUid : studentUidVersionsMap.keySet()) {
                 if (!manageableStudents.contains(studentUid)) {
                     throw new SecurityException("User " + currUserId + " attempted to update " +
                             "feedback for student " + studentUid + " w/o permission");
                 }
             }
 
             // let's get all of the existing submissions with their version history
             // and throw them in a map for easy access
             Set<String> students = studentUidVersionsMap.keySet();
             Set<AssignmentSubmission> existingSubsWithHistory = dao.getSubmissionsWithVersionHistoryForStudentListAndAssignment(students, assignment);
             Map<String, AssignmentSubmission> studentUidSubmissionMap = new HashMap<String, AssignmentSubmission>();
             if (existingSubsWithHistory != null) {
                 for (AssignmentSubmission sub : existingSubsWithHistory) {
                     studentUidSubmissionMap.put(sub.getUserId(), sub);
                 }
             }
 
             // now iterate through all of the versions to update
             for (Map.Entry<String, Collection<AssignmentSubmissionVersion>> entry : studentUidVersionsMap.entrySet()) {
                 String studentUid = entry.getKey();
                 Collection<AssignmentSubmissionVersion> updatedVersionList = entry.getValue();
                 for (AssignmentSubmissionVersion updatedVersion : updatedVersionList) {
                     // evict this object from the session since we are re-retrieving the existing version
                     dao.evictObject(updatedVersion);
 
                     if (updatedVersion != null) {
                         AssignmentSubmission submission = studentUidSubmissionMap.get(studentUid);
 
                         if (submission != null && updatedVersion.getId() == null) {
                             Set<AssignmentSubmissionVersion> existingVersions = submission.getSubmissionHistorySet();
                             if (existingVersions != null && !existingVersions.isEmpty()) {
                                 // we need to check for a version with submittedVersionNumber = 0
                                 // this indicates feedback w/o submission
                                 for (AssignmentSubmissionVersion ver : existingVersions) {
                                     if (ver.getSubmittedVersionNumber() == AssignmentSubmissionVersion.FEEDBACK_ONLY_VERSION_NUMBER) {
                                         updatedVersion.setId(ver.getId());
                                     }
                                 }
                             }
                         }
 
                         if (submission == null) {
                             submission = new AssignmentSubmission(assignment, studentUid);
                             submission.setCreatedBy(currUserId);
                             submission.setCreatedDate(currentTime);
                         }
 
                         AssignmentSubmissionVersion version = null;
                         boolean newVersion = false;
 
                         // let's try to retrieve the version to update
                         if (updatedVersion.getId() != null) {
                             if (submission.getSubmissionHistorySet() != null) {
                                 for (AssignmentSubmissionVersion existingVersion : submission.getSubmissionHistorySet()) {
                                     if (existingVersion.getId().equals(updatedVersion.getId())) {
                                         version = existingVersion;
                                         break;
                                     }
                                 }
                             }
 
                             if (version == null) {
                                 throw new VersionNotFoundException("No version exists for the " +
                                         "given student and assignment with the versionId: " + updatedVersion.getId());
                             }
                         } else {
                             // if null, we should be creating a new version b/c the instructor
                             // wants to provide feedback but the student has not made a submission
                             version = new AssignmentSubmissionVersion();
                             newVersion = true;
                         }
 
                         // clean up the provided text
                         updatedVersion.setFeedbackNotes(Assignment2Utils.cleanupHtmlText(updatedVersion.getFeedbackNotes()));
                         updatedVersion.setAnnotatedText(Assignment2Utils.cleanupHtmlText(updatedVersion.getAnnotatedText()));
 
                         // identify any attachments that were deleted
                         Set<SubmissionAttachmentBase> attachToDelete = identifyAttachmentsToDelete(version.getFeedbackAttachSet(), updatedVersion.getFeedbackAttachSet());
                         Set<SubmissionAttachmentBase> attachToCreate = identifyAttachmentsToCreate(version.getFeedbackAttachSet(), updatedVersion.getFeedbackAttachSet());
 
                         // check to see if any changes were actually made
                         boolean needsSave = false;
                         if (valueUpdated(version.getFeedbackNotes(), updatedVersion.getFeedbackNotes(), true) ||
                                 valueUpdated(version.getAnnotatedText(), updatedVersion.getAnnotatedText(), true) ||
                                 valueUpdated(version.getFeedbackReleasedDate(), updatedVersion.getFeedbackReleasedDate(), false) ||
                                 (attachToDelete != null && !attachToDelete.isEmpty()) ||
                                 (attachToCreate != null && !attachToCreate.isEmpty())) {
                             needsSave = true;
                         }
 
                         // we won't proceed any further if there aren't any changes to save
                         if (needsSave) {
                             if (newVersion) {
                                 version.setCreatedBy(currUserId);
                                 version.setCreatedDate(currentTime);
                                 version.setDraft(false);
                                 // the only time an instructor can create a new version is if no
                                 // submitted version exists. in this case, we always set the
                                 // submittedVersionNumber to 0 to differentiate it
                                 version.setSubmittedVersionNumber(AssignmentSubmissionVersion.FEEDBACK_ONLY_VERSION_NUMBER);
                             }
 
                             version.setAssignmentSubmission(submission);
                             version.setAnnotatedText(updatedVersion.getAnnotatedText());
                             version.setFeedbackNotes(updatedVersion.getFeedbackNotes());
                             version.setFeedbackReleasedDate(updatedVersion.getFeedbackReleasedDate());
                             version.setLastFeedbackSubmittedBy(currUserId);
                             version.setLastFeedbackDate(currentTime);
                             version.setModifiedBy(currUserId);
                             version.setModifiedDate(currentTime);
 
                             // make sure the version was populated on the FeedbackAttachments
                             populateVersionForAttachmentSet(attachToCreate, version);
                             populateVersionForAttachmentSet(attachToDelete, version);
 
                             // double check that the attachments to create have valid data
                             if (attachToCreate != null) {
                                 for (SubmissionAttachmentBase attach : attachToCreate) {
                                     if (!attach.isAttachmentValid()) {
                                         throw new IllegalArgumentException("The feedbackAttachSet passed to saveInstructorFeedback" +
                                                 " contained invalid attachment(s). Please ensure all required fields are populated " +
                                         " before saving.");
                                     }
                                 }
                             }
 
                             try {
 
                                 Set<AssignmentSubmissionVersion> versionSet = new HashSet<AssignmentSubmissionVersion>();
                                 versionSet.add(version);
 
                                 Set<AssignmentSubmission> submissionSet = new HashSet<AssignmentSubmission>();
                                 submissionSet.add(submission);
 
                                 // this will allow us to avoid the warning "empty set so nothing to do"
                                 if (attachToCreate != null && !attachToCreate.isEmpty()) {
                                     dao.saveMixedSet(new Set[] {submissionSet, versionSet, attachToCreate});
                                 } else {
                                     dao.saveMixedSet(new Set[] {submissionSet, versionSet});
                                 }
 
                                 if (log.isDebugEnabled()) log.debug("Updated/Added feedback for version " + 
                                         version.getId() + " for user " + submission.getUserId() + " for assignment " + 
                                         submission.getAssignment().getTitle()+ " ID: " + submission.getAssignment().getId());
                                 if (log.isDebugEnabled()) log.debug("Created feedbackAttachments: " + attachToCreate.size());
 
                                 if (attachToDelete != null && !attachToDelete.isEmpty()) {
                                     dao.deleteSet(attachToDelete);
                                     if (log.isDebugEnabled()) log.debug("Removed feedback attachments deleted for updated version " + version.getId() + " by user " + currUserId);
                                 }
 
                                 studentsUpdated.add(submission.getUserId());
 
                                 // evict the original AssignmentSubmission object. for some reason,
                                 // in the unit tests this object's collections aren't refreshed
                                 // when they are retrieved after save
                                 for (AssignmentSubmission sub: submissionSet) {
                                     dao.evictObject(sub);
                                 }
 
                             } catch (HibernateOptimisticLockingFailureException holfe) {
                                 if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
                                 throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), holfe);
                             } catch (StaleObjectStateException sose) {
                                 if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
                                 throw new StaleObjectModificationException("An optimistic locking failure occurred while attempting to update submission version" + version.getId(), sose);
                             }
                         }
                     }
                 }
             }
         }
 
         return studentsUpdated;
     }
 
     public void saveInstructorFeedback(Long versionId, String studentId, Assignment2 assignment, 
             String annotatedText, String feedbackNotes, Date releasedDate, 
             Set<FeedbackAttachment> feedbackAttachSet) {
 
         if (studentId == null || assignment == null) {
             throw new IllegalArgumentException("Null studentId or assignment passed " +
                     "to saveInstructorFeedback. studentId: " + studentId + " assignment:" + assignment);
         }
 
         if (!permissionLogic.isUserAllowedToManageSubmission(null, studentId, assignment)) {
             throw new SecurityException("User " + externalLogic.getCurrentUserId() + 
                     " attempted to submit feedback for student " + studentId + " without authorization");
         }
 
         List<AssignmentSubmissionVersion> versionsToUpdate = new ArrayList<AssignmentSubmissionVersion>();
         AssignmentSubmissionVersion version = new AssignmentSubmissionVersion();
         version.setId(versionId);
         version.setAnnotatedText(annotatedText);
         version.setFeedbackAttachSet(feedbackAttachSet);
         version.setFeedbackNotes(feedbackNotes);
         version.setFeedbackReleasedDate(releasedDate);
 
         versionsToUpdate.add(version);
 
         Map<String, Collection<AssignmentSubmissionVersion>> studentIdVersionsMap = new HashMap<String, Collection<AssignmentSubmissionVersion>>();
         studentIdVersionsMap.put(studentId, versionsToUpdate);
 
         saveAllInstructorFeedback(assignment, studentIdVersionsMap);
     }
 
     public List<AssignmentSubmission> getViewableSubmissionsWithHistoryForAssignmentId(Long assignmentId, String filterGroupId) {
         return getViewableSubmissions(assignmentId, true, filterGroupId);
     }
 
     public List<AssignmentSubmission> getViewableSubmissionsForAssignmentId(Long assignmentId, String filterGroupId) {
         return getViewableSubmissions(assignmentId, false, filterGroupId);
     }
 
     private List<AssignmentSubmission> getViewableSubmissions(Long assignmentId, boolean includeVersionHistory, String filterGroupId) {
         if (assignmentId == null) {
             throw new IllegalArgumentException("null assignmentId passed to getViewableSubmissionsForAssignmentId");
         }
 
         List<AssignmentSubmission> viewableSubmissions = new ArrayList<AssignmentSubmission>();
 
         Assignment2 assignment = dao.getAssignmentByIdWithGroups(assignmentId);
         if (assignment == null) {
             throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId);
         }
 
         String currUserId = externalLogic.getCurrentUserId();
         
         if (!permissionLogic.isUserAllowedToManageSubmissionsForAssignment(currUserId, assignment)) {
             throw new SecurityException("A user without feedback privileges attempted to access submissions for assignment: " + assignment.getId());
         }
 
         // get a list of all the students that the current user may view for the given assignment
         List<String> viewableStudents = permissionLogic.getViewableStudentsForAssignment(currUserId, assignment);
 
         // filter by group, if a group id was supplied
         if (filterGroupId != null && filterGroupId.trim().length() > 0) {
             viewableStudents = filterStudentsByGroupMembership(assignment.getContextId(), viewableStudents, filterGroupId);
         }
 
         if (viewableStudents != null && !viewableStudents.isEmpty()) {
 
             // get the submissions for these students
             Set<AssignmentSubmission> existingSubmissions;
 
             if (includeVersionHistory) {
                 existingSubmissions = dao.getSubmissionsWithVersionHistoryForStudentListAndAssignment(viewableStudents, assignment);
             } else {
                 existingSubmissions = dao.getCurrentSubmissionsForStudentsForAssignment(viewableStudents, assignment);
             }
 
             Map<String, AssignmentSubmission> studentIdSubmissionMap = new HashMap<String, AssignmentSubmission>();
             if (existingSubmissions != null) {
                 for (AssignmentSubmission submission : existingSubmissions) {
                     if (submission != null) {
                         studentIdSubmissionMap.put(submission.getUserId(), submission);
                     }
                 }
             }
 
             // now, iterate through the students and create empty AssignmentSubmission recs
             // if no submission exists yet
             for (String studentId : viewableStudents) {
                 if (studentId != null) {
                     AssignmentSubmission thisSubmission = 
                         (AssignmentSubmission)studentIdSubmissionMap.get(studentId);
 
                     if (thisSubmission == null) {
                         // no submission exists for this student yet, so just
                         // add an empty rec to the returned list
                         thisSubmission = new AssignmentSubmission(assignment, studentId);
                     } else {
                         // we need to filter restricted info from instructor
                         // if this is draft
                         filterOutRestrictedInfo(thisSubmission, externalLogic.getCurrentUserId(), includeVersionHistory);
                     }
 
                     viewableSubmissions.add(thisSubmission);
                 }
             }
         }
 
         return viewableSubmissions;
     }
 
     public Map<Assignment2, Integer> getSubmissionStatusConstantForAssignments(List<Assignment2> assignments, String studentId) {
         if (studentId == null) {
             throw new IllegalArgumentException("Null studentId passed to setSubmissionStatusForAssignments");
         }
 
         Map<Assignment2, Integer> assignToStatusMap = new HashMap<Assignment2, Integer>();
 
         if (assignments != null) {
             // retrieve the associated submission recs with current version data populated
             List<AssignmentSubmission> submissions = dao.getCurrentAssignmentSubmissionsForStudent(assignments, studentId);
             Map<Long, AssignmentSubmission> assignmentIdToSubmissionMap = new HashMap<Long, AssignmentSubmission>();
             if (submissions != null) {
                 for (AssignmentSubmission submission : submissions) {
                     if (submission != null) {
                         Assignment2 assign = submission.getAssignment();
                         if (assign != null) {
                             assignmentIdToSubmissionMap.put(assign.getId(), submission);
                         }
                     }
                 }
             }
 
             for (Assignment2 assign : assignments) {
                 if (assign != null) {
                     AssignmentSubmission currSubmission = (AssignmentSubmission)assignmentIdToSubmissionMap.get(assign.getId());
                     AssignmentSubmissionVersion currVersion = currSubmission != null ? 
                             currSubmission.getCurrentSubmissionVersion() : null;
                     Date submissionDueDate = currSubmission != null ? currSubmission.getResubmitCloseDate() : null;
 
                     Integer status = getSubmissionStatusForVersion(currVersion, assign.getDueDate(), submissionDueDate);
                     assignToStatusMap.put(assign, status);
                 }
             }
         }
 
         return assignToStatusMap;
     }
     
     public Integer getSubmissionStatusForVersion(AssignmentSubmissionVersion version, Date assignDueDate, Date submissionDueDate) {
         
         int status = AssignmentConstants.SUBMISSION_NOT_STARTED;
         
         // the student's due date will be the assignment due date unless the instructor
         // has extended the due date for this single student
         Date studentDueDate;
         if (submissionDueDate != null) {
             studentDueDate = submissionDueDate;
         } else {
             studentDueDate = assignDueDate;
         }
         
         if (version == null) {
             status = AssignmentConstants.SUBMISSION_NOT_STARTED;
         } else if (version.getId() == null) {
             status = AssignmentConstants.SUBMISSION_NOT_STARTED;
         } else if (version.isDraft()) {
             status = AssignmentConstants.SUBMISSION_IN_PROGRESS;
         } else if (version.getSubmittedDate() != null) {
             if (studentDueDate != null && studentDueDate.before(version.getSubmittedDate())) {
                 status = AssignmentConstants.SUBMISSION_LATE;
             } else {
                 status = AssignmentConstants.SUBMISSION_SUBMITTED;
             }
         }
 
         return status;
     }
 
     private void populateVersionForAttachmentSet(Set<? extends SubmissionAttachmentBase> attachSet,
             AssignmentSubmissionVersion version)
     {
         if (attachSet != null && !attachSet.isEmpty())
             for (SubmissionAttachmentBase attach : attachSet)
                 if (attach != null)
                     attach.setSubmissionVersion(version);
     }
 
     /**
      * 
      * @param existingAttachSet
      * @param updatedAttachSet
      * @return a set of attachments from the given updatedAttachSet that need
      * to be deleted. this is determined by comparing the attachmentReferences
      * of the attachments in the existingAttachSet to those in the updatedAttachSet.
      * if it does not exist in the updatedAttachSet, it needs to be deleted
      */
     private Set<SubmissionAttachmentBase> identifyAttachmentsToDelete(
             Set<? extends SubmissionAttachmentBase> existingAttachSet,
             Set<? extends SubmissionAttachmentBase> updatedAttachSet)
             {
         Set<SubmissionAttachmentBase> attachToRemove = new HashSet<SubmissionAttachmentBase>();
 
         // make a set of attachment references in case the id wasn't populated
         // properly
         Set<String> updatedAttachSetRefs = new HashSet<String>();
         if (updatedAttachSet != null) {
             for (SubmissionAttachmentBase attach : updatedAttachSet) {
                 updatedAttachSetRefs.add(attach.getAttachmentReference());
             }
         }
 
         if (existingAttachSet != null)
             for (SubmissionAttachmentBase attach : existingAttachSet) {
                 if (attach != null) {
                     if (updatedAttachSet == null || !updatedAttachSetRefs.contains(attach.getAttachmentReference())) {
                         // we need to delete this attachment
                         attachToRemove.add(attach);
                     }
                 }
             }
 
         return attachToRemove;
             }
 
     /**
      * 
      * @param existingAttachSet
      * @param updatedAttachSet
      * @return a set of attachments from the given updatedAttachSet that do not
      * currently exist. this is determined by checking to see if there is
      * already an attachment in the given existingAttachSet with the same
      * attachmentReference as each attachment in the updatedAttachSet. 
      */
     private Set<SubmissionAttachmentBase> identifyAttachmentsToCreate(
             Set<? extends SubmissionAttachmentBase> existingAttachSet,
             Set<? extends SubmissionAttachmentBase> updatedAttachSet)
             {
         Set<SubmissionAttachmentBase> attachToCreate = new HashSet<SubmissionAttachmentBase>();
 
         // make a set of attachment references in case the id wasn't populated
         // properly
         Set<String> existingAttachSetRefs = new HashSet<String>();
         if (existingAttachSet != null) {
             for (SubmissionAttachmentBase attach : existingAttachSet) {
                 existingAttachSetRefs.add(attach.getAttachmentReference());
             }
         }
 
         if (updatedAttachSet != null) {
             for (SubmissionAttachmentBase attach : updatedAttachSet) {
                 if (attach != null) {
                     if (!existingAttachSetRefs.contains(attach.getAttachmentReference())) {
                         attachToCreate.add(attach);
                     }
                 }
             }
         }
 
         return attachToCreate;
             }
 
     public int getNumberOfRemainingSubmissionsForStudent(String studentId, Long assignmentId) {
         if (studentId == null || assignmentId == null) {
             throw new IllegalArgumentException("null parameter passed to studentAbleToSubmit");
         } 
 
         Assignment2 assignment = (Assignment2)dao.findById(Assignment2.class, assignmentId);
         if (assignment == null) {
             throw new AssignmentNotFoundException("No assignment exists with id " + assignmentId);
         }
 
         int numSubmissionsRemaining = 0;
 
         if (!assignment.isRemoved() 
                 && assignment.isRequiresSubmission() 
                 && assignment.getSubmissionType() != AssignmentConstants.SUBMIT_NON_ELECTRONIC) {
 
             // retrieve the submission history for this student for this assignment
             AssignmentSubmission submission = dao.getSubmissionWithVersionHistoryForStudentAndAssignment(studentId, assignment);
             List<AssignmentSubmissionVersion> versionHistory = null;
             if (submission != null) {
                 versionHistory = dao.getVersionHistoryForSubmission(submission);
             }
 
             // we need to determine if this is the first submission for the student
             int currNumSubmissions = 0;
             if (submission == null) {
                 currNumSubmissions = 0;
             } else if (versionHistory == null) {
                 currNumSubmissions = 0;
             } else {
                 // we need to look at the submission history to determine if there
                 // are any submission by the student (not drafts and not versions
                 // created by instructor feedback when no submission)
                 for (AssignmentSubmissionVersion version : versionHistory) {
                     if (version != null) {
                         if (version.getSubmittedDate() != null) {
                             currNumSubmissions++;
                         }
                     }
                 }
             }
 
             /* A student is allowed to submit if:
 		 	1) student has not made a submission yet and assignment is open -OR-
 		 	2) instructor has set resubmission settings on the submission level,
 		 		and the resubmission date has not passed and the limit on the num
 		 		resubmissions has not been reached -OR-
 		 	3) there are no submission-level settings but there are on the assignment level
 		 		the assignment is still open and the number submissions allowed on
 		 		the assignment level has not been reached
              */
 
 
             int numAllowedOnAssignLevel = assignment.getNumSubmissionsAllowed();
             Integer numAllowedOnSubLevel = submission != null ? submission.getNumSubmissionsAllowed() : null;
 
             boolean resubmitSettingsOnAssignLevel = numAllowedOnAssignLevel == AssignmentConstants.UNLIMITED_SUBMISSION
             || numAllowedOnAssignLevel > 0;
             boolean resubmitSettingsOnSubmissionLevel = numAllowedOnSubLevel != null 
             && (numAllowedOnSubLevel > 0 || numAllowedOnSubLevel == AssignmentConstants.UNLIMITED_SUBMISSION);
 
 
             if (currNumSubmissions == 0 && assignment.isSubmissionOpen()) {
                 numSubmissionsRemaining = determineNumSubmissionRemaining(numAllowedOnAssignLevel, 
                         numAllowedOnSubLevel, currNumSubmissions);
             } else if (resubmitSettingsOnSubmissionLevel) {
                 // these setting override any settings on the assignment level
                 if (submission.getResubmitCloseDate() == null || 
                         submission.getResubmitCloseDate().after(new Date())) {
                     numSubmissionsRemaining = determineNumSubmissionRemaining(numAllowedOnAssignLevel, 
                             numAllowedOnSubLevel, currNumSubmissions);
                 }
             } else if (resubmitSettingsOnAssignLevel) {
                 if (assignment.isSubmissionOpen()) { 
                     numSubmissionsRemaining = determineNumSubmissionRemaining(numAllowedOnAssignLevel, 
                             numAllowedOnSubLevel, currNumSubmissions);
                 } 
             }
         }
 
         return numSubmissionsRemaining;
     }
 
     public boolean isSubmissionOpenForStudentForAssignment(String studentId, Long assignmentId) {
         int numRemainingSubmissions = getNumberOfRemainingSubmissionsForStudent(studentId, assignmentId);
         boolean isOpen = false;
         if (numRemainingSubmissions == AssignmentConstants.UNLIMITED_SUBMISSION || numRemainingSubmissions > 0) {
             isOpen = true;
         }
 
         return isOpen;
     }
 
     /**
      * 
      * @param assignmentLevelNumAllowed
      * @param subLevelNumAllowed
      * @param numAlreadySubmitted
      * @return the number of submissions remaining based upon the submission level num
      * submissions allowed, assign level num submission allowed, and the number of
      * submissions that have already occurred.  does NOT account for the assignment
      * being open or resubmission deadlines having passed
      */
     private int determineNumSubmissionRemaining(int assignmentLevelNumAllowed, Integer subLevelNumAllowed, int numAlreadySubmitted) {
         int numRemaining = 0;
         // first, check settings on the submission level. these override any other setting
         if (subLevelNumAllowed != null) {
             if (subLevelNumAllowed.equals(-1)) {
                 numRemaining = -1;
             } else if (numAlreadySubmitted < subLevelNumAllowed ){
                 numRemaining = subLevelNumAllowed.intValue() - numAlreadySubmitted;
             } else {
                 numRemaining = 0;
             } 
         } else {
             // then check assignment level
             if (assignmentLevelNumAllowed == -1) {
                 numRemaining = -1;
             } else if (numAlreadySubmitted < assignmentLevelNumAllowed){
                 numRemaining = assignmentLevelNumAllowed - numAlreadySubmitted; 
             } else {
                 numRemaining = 0;
             }
         }
 
         return numRemaining;
     }
 
     public boolean isMostRecentVersionDraft(AssignmentSubmission submission) {
         if (submission == null) {
             throw new IllegalArgumentException("null submission passed to currentVersionIsDraft");
         }
 
         boolean currVersionIsDraft = false;
 
         // if the id is null, there is no current version
         if (submission.getId() != null) {
             AssignmentSubmissionVersion version = dao.getCurrentSubmissionVersionWithAttachments(submission);
 
             if (version != null && version.isDraft()) {
                 currVersionIsDraft = true;
             }
         }
 
         return currVersionIsDraft;
     }
 
     public void releaseOrRetractAllFeedback(Long assignmentId, boolean release) {
         if (assignmentId == null) {
             throw new IllegalArgumentException("null assignmentId passed to releaseAllFeedbackForAssignment");
         }
 
         String currUserId = externalLogic.getCurrentUserId();
         Date now = new Date();
 
         Assignment2 assignment = dao.getAssignmentByIdWithGroups(assignmentId);
         if (assignment == null) {
             throw new AssignmentNotFoundException("Assignment with id " + assignmentId + " does not exist");
         }
 
         if (!permissionLogic.isUserAllowedToManageSubmissionsForAssignment(currUserId, assignment)) {
             throw new SecurityException("User attempted to release feedback for assignment " + assignmentId + " without authorization");
         }
 
         List<String> gradableStudents = permissionLogic.getViewableStudentsForAssignment(currUserId, assignment);
         if (gradableStudents != null && !gradableStudents.isEmpty()) {
             Set<AssignmentSubmission> submissionList = dao.getSubmissionsWithVersionHistoryForStudentListAndAssignment(
                     gradableStudents, assignment);
 
             if (submissionList != null && !submissionList.isEmpty()) {
 
                 Set<AssignmentSubmissionVersion> versionsToUpdate = new HashSet<AssignmentSubmissionVersion>();
 
                 for (AssignmentSubmission submission : submissionList) {
                     if (submission != null) {
                         if (submission.getSubmissionHistorySet() != null &&
                                 !submission.getSubmissionHistorySet().isEmpty()) {
                             // we need to iterate through all of the versions and
                             // release them
                             for (AssignmentSubmissionVersion version : submission.getSubmissionHistorySet())
                             {
                                 if (version != null) {
                                     if (release) {
                                         version.setFeedbackReleasedDate(now);
                                     } else {
                                         version.setFeedbackReleasedDate(null);
                                     }
 
                                     version.setModifiedBy(currUserId);
                                     version.setModifiedDate(now);
 
                                     versionsToUpdate.add(version);
                                 }
                             }
                         }
                     }
                 }
 
                 try {
                     dao.saveMixedSet(new Set[] { versionsToUpdate });
                     if (log.isDebugEnabled()) log.debug("All versions for assignment " + assignmentId + " released by " + externalLogic.getCurrentUserId());
                 } catch (HibernateOptimisticLockingFailureException holfe) {
                     if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission versions for assignment " + assignmentId);
                     throw new StaleObjectModificationException("An optimistic locking " +
                             "failure occurred while attempting to update submission " +
                             "versions for assignment " + assignmentId, holfe);
                 } catch (StaleObjectStateException sose) {
                     if(log.isInfoEnabled()) log.info("An optimistic locking failure " +
                             "occurred while attempting to update submission versions for assignment " + assignmentId);
                     throw new StaleObjectModificationException("An optimistic locking " +
                             "failure occurred while attempting to update submission versions for assignment " + assignmentId, sose);
                 }
             }
         }
     }
 
     public void releaseOrRetractAllFeedbackForSubmission(Long submissionId, boolean release) {
         if (submissionId == null) {
             throw new IllegalArgumentException("null submissionId passed to releaseAllFeedbackForSubmission");
         }
 
         String currUserId = externalLogic.getCurrentUserId();
         Date now = new Date();
 
         AssignmentSubmission subWithHistory = dao.getSubmissionWithVersionHistoryById(submissionId);
 
         if (subWithHistory == null) {
             throw new SubmissionNotFoundException("No submission exists with id " + submissionId);
         }
 
         if (!permissionLogic.isUserAllowedToManageSubmission(currUserId, subWithHistory.getUserId(), subWithHistory.getAssignment())) {
             throw new SecurityException("User " + currUserId + " attempted to release feedback" +
                     " for student " + subWithHistory.getUserId() + " and assignment " + 
                     subWithHistory.getAssignment().getId() + "without authorization");
         }
 
         if (subWithHistory.getSubmissionHistorySet() != null &&
                 !subWithHistory.getSubmissionHistorySet().isEmpty()) {
             // we need to iterate through all of the versions and
             // release them
             Set<AssignmentSubmissionVersion> updatedVersions = new HashSet<AssignmentSubmissionVersion>();
             for (AssignmentSubmissionVersion version : subWithHistory.getSubmissionHistorySet()) {
                 if (version != null) {
                     if (release) {
                         version.setFeedbackReleasedDate(now);
                     } else {
                         version.setFeedbackReleasedDate(null);
                     }
                     version.setModifiedBy(currUserId);
                     version.setModifiedDate(now);
                     updatedVersions.add(version);
                 }
             }
 
             try {
                 dao.saveMixedSet(new Set[] { updatedVersions });
                 if (log.isDebugEnabled()) log.debug("All submission versions for submission " + submissionId + " released by " + externalLogic.getCurrentUserId());
             } catch (HibernateOptimisticLockingFailureException holfe) {
                 if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update release all version for submission " + submissionId);
 
                 throw new StaleObjectModificationException("An optimistic locking " +
                         "failure occurred while attempting to update release all version for submission " + submissionId, holfe);
             }
         }
     }
 
     public void releaseOrRetractFeedbackForVersion(Long submissionVersionId, boolean release) {
         if (submissionVersionId == null) {
             throw new IllegalArgumentException("Null submissionVersionId passed to releaseFeedbackForVersion");
         }
 
         String currUserId = externalLogic.getCurrentUserId();
         Date now = new Date();
 
         AssignmentSubmissionVersion version = (AssignmentSubmissionVersion)dao.findById(
                 AssignmentSubmissionVersion.class, submissionVersionId);
         if (version == null) {
             throw new VersionNotFoundException("No version " + submissionVersionId + " exists");
         }
 
         AssignmentSubmission submission = version.getAssignmentSubmission();
 
         if (!permissionLogic.isUserAllowedToManageSubmission(currUserId, submission.getUserId(), submission.getAssignment())) {
             throw new SecurityException("User " + externalLogic.getCurrentUserId() + " attempted to release feedback" +
                     " for student " + submission.getUserId() + " and assignment " + 
                     submission.getAssignment().getId() + "without authorization");
         }
 
         if (release) {
             version.setFeedbackReleasedDate(now);
         } else {
             version.setFeedbackReleasedDate(null);
         }
 
         version.setModifiedBy(currUserId);
         version.setModifiedDate(now);
 
         try {
             dao.update(version);
             if (log.isDebugEnabled()) log.debug("Version " + version.getId() + " released by " + externalLogic.getCurrentUserId());
         } catch (HibernateOptimisticLockingFailureException holfe) {
             if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update submission version" + version.getId());
             throw new StaleObjectModificationException("An optimistic locking failure occurred " +
                     "while attempting to update submission version" + version.getId(), holfe);
         }
     }
 
     /**
      * when retrieving a submission and/or version, some fields may be restricted
      * for the curr user. If curr user is the submitter and feedback has not been
      * released, we do not want to return feedback. If curr user is not the submitter
      * and the version is draft, we do not want to return the submission text and attach.
      * if the curr user is the submitter and the history contains a feedback-only
      * (submittedVersionNumber=0) version that is not released, this version is removed
      * from the history
      * this method will also evict the submission and version(s) from session since we do not want
      * to accidentally save or re-load these modified objects
      * @param submission 
      * @param currentUserId
      * @param includeHistory - true if the submissionHistorySet was populated and
      * needs to be filtered, as well
      */
     private void filterOutRestrictedInfo(AssignmentSubmission submission, String currentUserId, boolean includeHistory) {
         // if the current user is the submitter and feedback has not been 
         // released, do not return the feedback info
         // if the current user is not the submitter and a version is draft, 
         // do not return any of the submission info
 
         // evict the submission from the session since we are making modifications
         // that we don't want to be saved or re-loaded
         dao.evictObject(submission);
 
         // check the version history
         if (includeHistory) {
             if (submission.getSubmissionHistorySet() != null && !submission.getSubmissionHistorySet().isEmpty()) {
                 Set<AssignmentSubmissionVersion> filteredHistorySet = new LinkedHashSet<AssignmentSubmissionVersion>();
                 for (AssignmentSubmissionVersion version : submission.getSubmissionHistorySet()) {
                     version = getFilteredVersion(version, currentUserId);
                     if (version != null) {
                         filteredHistorySet.add(version);
                     }
                 }
 
                 submission.setSubmissionHistorySet(filteredHistorySet);
             }		
         }
 
         // also check the currentVersion
         AssignmentSubmissionVersion currVersion = submission.getCurrentSubmissionVersion();
         if (currVersion != null) {
             currVersion = getFilteredVersion(submission.getCurrentSubmissionVersion(), currentUserId);
         }
         submission.setCurrentSubmissionVersion(currVersion);
     }
 
     /**
      * Returns the given version with restricted fields removed. If submitter is
      * current user and version is "feedback-only" (submittedVersionNumber = 0)
      * that is not released, returns null.
      * when retrieving a submission and/or version, some fields may be restricted
      * for the curr user. If curr user is the submitter and feedback has not been
      * released, we do not want to return feedback. If curr user is not the submitter
      * and the version is draft, we do not want to return the submission text and attach.
      * This method will also evict the version from session since we do not want
      * to accidentally save this modified object. 
      * @param version 
      * @param currentUserId
      */
     private AssignmentSubmissionVersion getFilteredVersion(AssignmentSubmissionVersion version, String currentUserId) {
         if (version != null) {
 
             // since we may modify the versions before returning them to filter
             // out restricted info, we don't want to return the persistent objects.
             // we have no intention of saving the modified object
             dao.evictObject(version);
 
             // if the current user is the submitter
             if (version.getAssignmentSubmission().getUserId().equals(currentUserId)) {
                 if (!version.isFeedbackReleased()) {
                     if (version.getSubmittedVersionNumber() == 0) {
                         // this indicates "feedback-only" version and
                         // we don't want to return this to the submitter
                         // if it hasn't been released yet
                         version = null;
                     } else {
                         // do not populate the feedback since not released 
                         version.setFeedbackAttachSet(new HashSet<FeedbackAttachment>());
                         version.setFeedbackNotes("");
                         version.setAnnotatedText("");
                         if (log.isDebugEnabled()) log.debug("Not populating feedback-specific info " +
                         "b/c curr user is submitter and feedback not released");
                     }
                 }
             } else {
                 // do not populate submission info if still draft
                 if (version.isDraft()) {
                     version.setSubmittedText("");
                     version.setSubmissionAttachSet(new HashSet<SubmissionAttachment>());
                     if (log.isDebugEnabled()) log.debug("Not populating submission-specific info b/c draft status and current user is not submitter");
                 }
             }
         }
 
         return version;
     }
 
     public List<AssignmentSubmissionVersion> getVersionHistoryForSubmission(AssignmentSubmission submission) {
         if (submission == null) {
             throw new IllegalArgumentException("Null submission passed to getVersionHistoryForSubmission");
         }
 
         List<AssignmentSubmissionVersion> filteredVersionHistory = new ArrayList<AssignmentSubmissionVersion>();
 
         if (!permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, submission.getUserId(), submission.getAssignment().getId(), null)) {
             throw new SecurityException("User " + externalLogic.getCurrentUserId() +
                     " attempted to access version history for student " + submission.getUserId() +
             " without authorization!");
         }
 
         // if id is null, this submission does not exist yet - will return empty
         // version history
         if (submission.getId() != null) {
             String currentUserId = externalLogic.getCurrentUserId();
 
             List<AssignmentSubmissionVersion> historySet = dao.getVersionHistoryForSubmission(submission);
             if (historySet != null && !historySet.isEmpty()) {
                 for (AssignmentSubmissionVersion version : historySet) {
                     if (version != null) {					
                         version = getFilteredVersion(version, currentUserId);
                         if (version != null) {
                             filteredVersionHistory.add(version);
                         }
                     }
                 }
             }
         } else {
             if (log.isDebugEnabled()) log.debug("Submission does not exist so no version history retrieved");
         }
 
         if (filteredVersionHistory.size() > 0)
         {
         	Collections.sort(filteredVersionHistory, new ComparatorsUtils.VersionHistoryComparatorDesc());
         }
         return filteredVersionHistory;
     }
 
     private AssignmentSubmission getAssignmentSubmissionForStudentAndAssignment(Assignment2 assignment, String studentId) {
         AssignmentSubmission submission = null;
 
         List<AssignmentSubmission> subList = dao.findByProperties(AssignmentSubmission.class, 
                 new String[] {"assignment", "userId"}, new Object[] {assignment, studentId});
 
         if (subList != null && subList.size() > 0) {
             submission = (AssignmentSubmission) subList.get(0);
         }
 
         return submission;
     }
 
     public int getNumSubmittedVersions(String studentId, Long assignmentId) {
         if (studentId == null || assignmentId == null) {
             throw new IllegalArgumentException("Null studentId or assignmentId passed " +
             "to getTotalNumSubmissionsForStudentForAssignment");
         }
 
         return dao.getNumSubmittedVersions(studentId, assignmentId);
     }
 
     public int getNumStudentsWithASubmission(Assignment2 assignment, Collection<String> studentIds) {
         if (assignment == null) {
             throw new IllegalArgumentException ("Null assignment passed to getNumStudentsWithASubmission");
         }
 
         return dao.getNumStudentsWithASubmission(assignment, studentIds);
     }
 
     public int getNumNewSubmissions(Assignment2 assignment, Collection<String> studentUids) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment passed to getNumNewSubmissions");
         }
 
         // "new" is defined as a student whose most recent submitted version does not
         // have released feedback or, if assignment is graded, there is no grade yet
         Set<String> newSubmissions = new HashSet<String>();
 
         if (studentUids != null && !studentUids.isEmpty()) {
 
             // retrieve all of the most recently submitted versions
             List<AssignmentSubmissionVersion> currSubVersions = dao.getCurrentSubmittedVersions(studentUids, assignment);
             if (currSubVersions != null && !currSubVersions.isEmpty()) {
                 // identify potentially new versions
                 Set<String> studentsToCheckForGrade = new HashSet<String>();
                 for (AssignmentSubmissionVersion version : currSubVersions) {
                     if (!version.isFeedbackReleased()) {
                         newSubmissions.add(version.getAssignmentSubmission().getUserId());
                     } else {
                         // this version has feedback released, so we need to check
                         // if it has a grade, if applicable
                         studentsToCheckForGrade.add(version.getAssignmentSubmission().getUserId());
                     }
                 }
 
                 if (assignment.isGraded() && assignment.getGradebookItemId() != null &&
                         !studentsToCheckForGrade.isEmpty()) {
                     
                     // we need to filter this list to only include students the user is
                     // allowed to view or grade in the gradebook or we will hit a security exception
                     if (!gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
                         studentsToCheckForGrade = new HashSet<String>(gradebookLogic.getFilteredStudentsForGradebookItem(externalLogic.getCurrentUserId(), 
                                 assignment.getContextId(), assignment.getGradebookItemId(), 
                                 AssignmentConstants.VIEW, studentsToCheckForGrade));
                     }
                 
                     Map<String, GradeInformation> studentGradeInfo = gradebookLogic.getGradeInformationForStudents(studentsToCheckForGrade, assignment.getContextId(), assignment.getGradebookItemId());
                     if (studentGradeInfo != null) {
                         for (Map.Entry<String, GradeInformation> entry : studentGradeInfo.entrySet()) {
                             String studentUid = entry.getKey();
                             GradeInformation gradeInfo = entry.getValue();
                             if (gradeInfo.getGradebookGrade() == null || gradeInfo.getGradebookGrade().equals("")) {
                                 newSubmissions.add(studentUid);
                             }
                         }
                     }
                 }
             }
         }
 
         return newSubmissions.size();
     }
 
     public void markFeedbackAsViewed(Long submissionId, List<Long> versionIdList) {
         if (submissionId == null) {
             throw new IllegalArgumentException("Null submissionId passed to markFeedbackAsViewed");
         }
 
         if (versionIdList != null) {
             // retrieve the submission and version history
             AssignmentSubmission subWithHistory = dao.getSubmissionWithVersionHistoryById(submissionId);
             if (subWithHistory == null) {
                 throw new SubmissionNotFoundException("No submission exists with id " + submissionId);
             }
 
             String currentUserId = externalLogic.getCurrentUserId();
             if (!currentUserId.equals(subWithHistory.getUserId())) {
                 throw new SecurityException("User " + currentUserId + " attempted to mark " +
                         "feedback as viewed for student " + subWithHistory.getUserId());
             }
 
             if (subWithHistory.getSubmissionHistorySet() != null) {
                 Set<AssignmentSubmissionVersion> updatedVersions = new HashSet<AssignmentSubmissionVersion>();
                 Date now = new Date();
 
                 for (AssignmentSubmissionVersion existingVersion : subWithHistory.getSubmissionHistorySet()) {
                     if (versionIdList.contains(existingVersion.getId())) {
                         // double check that this feedback has actually been released
                         if (existingVersion.isFeedbackReleased()) {
                             // this version needs to be marked as viewed and updated
                             existingVersion.setFeedbackLastViewed(now);
                             existingVersion.setModifiedBy(currentUserId);
                             existingVersion.setModifiedDate(now);
 
                             updatedVersions.add(existingVersion);
                         } else {
                             if (log.isDebugEnabled()) log.debug("Version " + existingVersion.getId() +
                             " was not marked as read b/c feedback has not been released yet");
                         }
                     }
                 }
 
                 if (!updatedVersions.isEmpty()) {
                     dao.saveSet(updatedVersions);
                 }
 
                 if (log.isDebugEnabled()) log.debug(updatedVersions.size() + 
                 " versions marked as feedback viewed");
             }
         }
     }
 
     public void markAssignmentsAsCompleted(String studentId, Map<Long, Boolean> assignmentIdToCompletedMap) {
         if (studentId == null) {
             throw new IllegalArgumentException("Null studentId passed to markAssignmentAsCompleted");
         }
 
         String currentUserId = externalLogic.getCurrentUserId();
 
         if (!studentId.equals(currentUserId)) {
             throw new SecurityException("User " + currentUserId + " attempted to mark assignments as " +
                     "complete for student " + studentId);
         }
 
         if (assignmentIdToCompletedMap != null && !assignmentIdToCompletedMap.isEmpty()) {
             // first, retrieve the Assignment2 objects associated with the list
             Set<Assignment2> assignToMarkComplete = dao.getAssignmentsWithGroupsAndAttachmentsById(assignmentIdToCompletedMap.keySet());
             if (assignToMarkComplete != null) {
                 // now, let's retrieve the submissions
                 List<AssignmentSubmission> submissions = dao.getCurrentAssignmentSubmissionsForStudent(assignToMarkComplete, studentId);
                 Map<Long, AssignmentSubmission> assignmentIdToSubmissionMap = new HashMap<Long, AssignmentSubmission>();
                 if (submissions != null) {
                     for (AssignmentSubmission sub : submissions) {
                         assignmentIdToSubmissionMap.put(sub.getAssignment().getId(), sub);
                     }
                 }
 
                 // now, iterate through the assignments to see if any do not have
                 // a submission yet - we will need to create it
                 Set<AssignmentSubmission> submissionsToSave = new HashSet<AssignmentSubmission>();
                 for (Assignment2 assign : assignToMarkComplete) {
                     Boolean complete = assignmentIdToCompletedMap.get(assign.getId());
                     if (complete == null) {
                         throw new IllegalArgumentException("Null value for completed passed in map to markAssignmentsAsCompleted");
                     }
 
                     AssignmentSubmission submission = (AssignmentSubmission)assignmentIdToSubmissionMap.get(assign.getId());
                     if (submission == null) {
                         // we need to create this submission record
                         submission = new AssignmentSubmission(assign, studentId);
                         submission.setCreatedBy(currentUserId);
                         submission.setCreatedDate(new Date());
                     }
 
                     submission.setCompleted(complete);
                     submission.setModifiedBy(currentUserId);
                     submission.setModifiedDate(new Date());
 
                     submissionsToSave.add(submission);
                 }
 
                 if (!submissionsToSave.isEmpty()) {
                     dao.saveSet(submissionsToSave);
                     if (log.isDebugEnabled()) log.debug(submissionsToSave.size() + " submissions updated through markAssignmentsAsCompleted");
                 }
             }
         }
     }
 
     public void updateStudentResubmissionOptions(Collection<String> studentUids, Assignment2 assign, 
             Integer numSubmissionsAllowed, Date resubmitCloseDate) {
         if (studentUids == null || assign == null) {
             throw new IllegalArgumentException("Null studentUid or assign passed to updateStudentResubmissionOptions." +
                     " studentUid:" + studentUids + " assign:" + assign);
         }
 
         if (!permissionLogic.isUserAbleToProvideFeedbackForStudents(studentUids, assign)) {
             throw new SecurityException("User attempted to provide feedback for student: " + 
                     studentUids + " for assignment: " + assign.getId() + " without permission");
         }
 
         Search search = new Search(new String[] {"assignment", "userId"}, new Object[] {assign, studentUids.toArray()});
         List<AssignmentSubmission> submissions = dao.findBySearch(AssignmentSubmission.class, search);
 
         Map<String, AssignmentSubmission> studentUidSubmissionMap = new HashMap<String, AssignmentSubmission>();
         if (submissions != null) {
             for (AssignmentSubmission sub : submissions) {
                 studentUidSubmissionMap.put(sub.getUserId(), sub);
             }
         }
         String currUserId = externalLogic.getCurrentUserId();
         Date currDate = new Date();
 
         Set<AssignmentSubmission> subToUpdate = new HashSet<AssignmentSubmission>();
         for (String studentUid : studentUids) {
             AssignmentSubmission submission = studentUidSubmissionMap.get(studentUid);
             if (submission == null) {
                 submission = new AssignmentSubmission(assign, studentUid);
                 submission.setCreatedBy(currUserId);
                 submission.setCreatedDate(currDate);
             }
 
             Integer origNumAllowed = submission.getNumSubmissionsAllowed();
             Date origResubmitClose = submission.getResubmitCloseDate();
 
             // only save/update if they have actually changed
             if (valueUpdated(origNumAllowed, numSubmissionsAllowed, false) ||
                     valueUpdated(origResubmitClose, resubmitCloseDate, false)) {
                 submission.setNumSubmissionsAllowed(numSubmissionsAllowed);
                 submission.setResubmitCloseDate(resubmitCloseDate);
                 submission.setModifiedBy(currUserId);
                 submission.setModifiedDate(currDate);
 
                 subToUpdate.add(submission);
             }
         }
 
         if (!subToUpdate.isEmpty()) {
             dao.saveMixedSet(new Set[] {subToUpdate});
         }
     }
 
     public List<AssignmentSubmission> getSubmissionsForCurrentUser(String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to " + this);
         }
 
         List<AssignmentSubmission> userSubmissions = new ArrayList<AssignmentSubmission>();
 
         String currentUserId = externalLogic.getCurrentUserId();
 
         if (!permissionLogic.isUserAllowedToSubmit(currentUserId, contextId)) {
             throw new SecurityException("Attempt to retrieve submissions for a non-student user");
         }
 
         List<Assignment2> viewableAssignments = assignmentLogic.getViewableAssignments(contextId);
 
         if (viewableAssignments != null) {
             Set<AssignmentSubmission> existingSubmissions = dao.getSubmissionsForStudentWithVersionHistoryAndAttach(currentUserId, viewableAssignments);
 
             // put these submissions into a map so we can determine which assignments don't
             // have submissions yet
             Map<Long, AssignmentSubmission> assignIdSubmissionMap = new HashMap<Long, AssignmentSubmission>();
             if (existingSubmissions != null) {
                 for (AssignmentSubmission existingSub : existingSubmissions) {
                     assignIdSubmissionMap.put(existingSub.getAssignment().getId(), existingSub);
                 }
             }
 
             // now, let's iterate through all of the viewable assignments. we may need to
             // add filler recs for assignments with no submissions yet
             for (Assignment2 assign : viewableAssignments) {
                 // try to get the existing submission
                 AssignmentSubmission existingSub = assignIdSubmissionMap.get(assign.getId());
                 if (existingSub == null) {
                     // we need to add an "empty" submission to the list as a placeholder
                     // for this assignment
                     AssignmentSubmission newSub = new AssignmentSubmission(assign, currentUserId);
                     userSubmissions.add(newSub);
                 } else {
                     filterOutRestrictedInfo(existingSub, currentUserId, true);
                     userSubmissions.add(existingSub);
                 }
             }
         }
 
         // sort by completed, then by assignment sortIndex
         Collections.sort(userSubmissions, new ComparatorsUtils.SubmissionCompletedSortOrderComparator());
 
         return userSubmissions;
     }
     
     public AssignmentSubmissionVersion getCurrentVersionFromHistory(Collection<AssignmentSubmissionVersion> versionHistory) {
         AssignmentSubmissionVersion currVersion = null;
         if (versionHistory != null) {
             currVersion = dao.getCurrentVersionFromHistory(versionHistory);
         }
         
         return currVersion;
     }
 
     private List<String> filterStudentsByGroupMembership(String contextId, List<String> fullStudentIdList, String filterGroupId) {
         List<String> filteredStudentIdList = new ArrayList<String>();
         if (fullStudentIdList != null && filterGroupId != null) {
             List<String> studentsInGroup = externalLogic.getUsersInGroup(contextId, filterGroupId);
             if (studentsInGroup != null) {
                 for (String studentId : studentsInGroup) {
                     if (fullStudentIdList.contains(studentId)) {
                         filteredStudentIdList.add(studentId);
                     }
                 }
             }
         }
 
         return filteredStudentIdList;
     }
 
     /**
      * 
      * @param value1
      * @param value2
      * @param treatNullAsEmptyString treats null values as equal to "". useful
      * if you are comparing String properties to treat null and empty string as
      * identical values
      * @return true if value1 and value2 are not equal. 
      */
     private boolean valueUpdated(Object value1, Object value2, boolean treatNullAsEmptyString) {
         boolean valueChanged = false;
 
         // we want to make null equivalent to the empty string for comparison
         // in some cases
         if (treatNullAsEmptyString && value1 == null) {
             value1 = "";
         }
 
         if (treatNullAsEmptyString && value2 == null) {
             value2 = "";
         }
 
         if (value1 == null && value2 != null) {
             valueChanged = true;
         } else if (value1 != null && value2 == null) {
             valueChanged = true;
         } else if (value1 != null && value2 != null && !value1.equals(value2)) {
             valueChanged = true;
         }
 
         return valueChanged;
     }
 
 }
