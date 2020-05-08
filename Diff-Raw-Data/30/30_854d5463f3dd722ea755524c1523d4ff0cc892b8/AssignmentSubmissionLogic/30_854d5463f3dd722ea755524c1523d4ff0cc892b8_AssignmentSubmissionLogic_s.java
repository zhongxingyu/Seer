 
 /**********************************************************************************
  * $URL$
  * $Id$
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
 
 package org.sakaiproject.assignment2.logic;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.assignment2.model.FeedbackAttachment;
 import org.sakaiproject.assignment2.model.FeedbackVersion;
 import org.sakaiproject.assignment2.model.SubmissionAttachment;
 
 /**
  * This is the interface for the AssignmentSubmission object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public interface AssignmentSubmissionLogic {
 	
 	// sorting information
     public static final String SORT_BY_NAME = "name";
     public static final String SORT_BY_SUBMIT_DATE = "submitDate";
     public static final String SORT_BY_RELEASED = "released";
     
 	/**
 	 * 
 	 * @param submissionId
 	 * @return Returns the AssignmentSubmission based on its assignmentSubmissionId.
 	 * Populates current version information. If version is draft and current
 	 * user is not submitter, submittedText and submissionAttachments will not
 	 * be populated. If the curr user is the submitter but feedback has not 
 	 * been released, will not populate	feedback. Because of these 
 	 * changes that we don't want to save, the associated version was evicted 
 	 * from the session and is not persistent.
 	 * @throws SecurityException if current user is not allowed to view the
 	 * corresponding submission
 	 * @throws SubmissionNotFoundException if no submission exists with the given id
 	 */
 	public AssignmentSubmission getAssignmentSubmissionById(Long submissionId);
 	
 	/**
 	 * 
 	 * @param submissionVersionId
 	 * @return Returns the AssignmentSubmissionVersion with the given submissionVersionId.
 	 * If the version is draft and the submitter is not the current user, will not
 	 * populate the submissionText or submissionAttachmentSet. If the curr user is
 	 * the submitter but feedback has not been released, will not populate
 	 * feedback.  Because of these changes that we don't want to save, the 
 	 * returned version was evicted from the session and is not persistent.
 	 * @throws SecurityException if current user is not allowed to view the version
 	 * @throws VersionNotFoundException if no version exists with the given id
 	 */
 	public AssignmentSubmissionVersion getSubmissionVersionById(Long submissionVersionId);
 	
 	
 	/**
 	 * 
 	 * @param assignmentId
 	 * @param studentId
 	 * @return AssignmentSubmission associated with the given assignmentId and studentId with
 	 * the attachments and submission version history populated.
 	 * will return an empty record if there is no submission info for this student yet. If the curr version 
 	 * is draft and the submitter is not the current user, will not
 	 * populate the submissionText or submissionAttachmentSet. If the curr user is
 	 * the submitter but feedback has not been released, will not populate
 	 * feedback.  Because of these changes that we don't want to save, the 
 	 * returned submission was evicted from the session and is not persistent.
 	 * @throws SecurityException if current user not allowed to view student's submission
 	 * @throws AssignmentNotFoundException if no assignment exists with the given assignmentId
 	 */
 	public AssignmentSubmission getCurrentSubmissionByAssignmentIdAndStudentId(Long assignmentId, String studentId);
 	
 	/**
 	 * Create or update an AssignmentSubmission and AssignmentSubmissionVersion.
 	 * Will retrieve the current submission and determine whether a new submission
 	 * and/or version is required.  Versions are updated until they are submitted.
 	 * Each submission creates a new version. There will be one AssignmentSubmission
 	 * record per student per assignment.
 	 * @param userId - the submitter's userId
 	 * @param assignment - which assignment this submission is for
 	 * @param draft - true if this submission is draft
 	 * @param submittedText - the submitter's text
 	 * @param subAttachSet - the set of SubmissionAttachments associated with the
 	 * version. if this is an update, will delete any existing attachments associated
 	 * with the version that aren't included in this set
 	 * @throws SecurityException if current user is not allowed to make this submission
 	 */
 	public void saveStudentSubmission(String userId, Assignment2 assignment, boolean draft, 
 			String submittedText, Set<SubmissionAttachment> subAttachSet);
 	
 	
 	/**
 	 * Save instructor feedback for a particular version. If student has not made
 	 * a submission, will create the submission and version
 	 * @param versionId - id of the version that you want to update. if null, there must
 	 * not be a student submission yet
 	 * @param studentId
 	 * @param assignment
 	 * @param numSubmissionsAllowed
 	 * @param resubmitCloseDate
 	 * @param annotatedText
 	 * @param feedbackNotes
 	 * @param releasedDate
 	 * @param feedbackAttachSet
 	 * @throws SecurityException if user is not allowed to submit feedback for
 	 * the given student and assignment
 	 */
 	public void saveInstructorFeedback(Long versionId, String studentId, Assignment2 assignment, 
 			Integer numSubmissionsAllowed, Date resubmitCloseDate, String annotatedText, 
 			String feedbackNotes, Date releasedDate, Set<FeedbackAttachment> feedbackAttachSet);
 
 	/**
 	 * 
 	 * @param assignmentId
 	 * @return Non-null list.  All AssignmentSubmissions for this assignmentId that the current
 	 * user is allowed to view or grade with the currentVersion information. Version history is not populated.
 	 * If no submission exists yet, returns an empty AssigmentSubmission rec for the
 	 * student. If the curr version is draft, will not
 	 * populate the submissionText or submissionAttachmentSet. Because of these 
 	 * changes that we don't want to save, the returned submissions were evicted 
 	 * from the session and are not persistent.
 	 * @throws SecurityException if not allowed to provide feedback for the given assignment
 	 * @throws AssignmentNotFoundException if no assignment exists with the given assignmentId
 	 */
 	public List<AssignmentSubmission> getViewableSubmissionsForAssignmentId(Long assignmentId);
 	
 	/**
 	 * 
 	 * @param assignmentId
 	 * @return Non-null list.  All AssignmentSubmissions for this assignmentId that the current
 	 * user is allowed to view or grade with the currentVersion information and version history populated. If
 	 * no submission exists yet, returns an empty AssigmentSubmission rec for the
 	 * student. If the curr version is draft, will not
 	 * populate the submissionText or submissionAttachmentSet. Because of these 
 	 * changes that we don't want to save, the returned submissions were evicted 
 	 * from the session and are not persistent.
 	 * @throws SecurityException if not allowed to provide feedback for the given assignment
 	 * @throws AssignmentNotFoundException if no assignment exists with the given assignmentId
 	 */
 	public List<AssignmentSubmission> getViewableSubmissionsWithHistoryForAssignmentId(Long assignmentId);
 	
 	/**
 	 * @param assignments - list of assignments that you want this student's status for
 	 * @param studentId
 	 * @return a map of Assignment2 object to its associated submission status for the
 	 * given student (ie submitted, not started, draft, etc)
 	 */
 	public Map<Assignment2, Integer> getSubmissionStatusConstantForAssignments(List<Assignment2> assignments, String studentId);
 	
 	/**
 	 * 
 	 * @param currentVersion
 	 * @param dueDate - due date for the associated assignment. if null, assumes
 	 * no due date
 	 * @return the constant equivalent for the given submission's status 
 	 * ie In Progress, Submitted, etc based upon the passed currentVersion. will
 	 * return the constant for "Not started" if currentVersion is null
 	 */
 	public Integer getSubmissionStatusConstantForCurrentVersion(AssignmentSubmissionVersion currentVersion,
 			Date dueDate);
 	
 	/**
 	 * 
 	 * @param studentId
 	 * @param assignmentId
 	 * @return true if the student is still able to make a submission for the given
 	 * assignment at this time.  checks to see if assignment is open, if resubmission allowed,
 	 * etc to determine if submission is still open
 	 * @throws AssignmentNotFoundException if no assignment exists with given assignmentId
 	 */
 	public boolean isSubmissionOpenForStudentForAssignment(String studentId, Long assignmentId);
 	
 	/**
 	 * 
 	 * @param studentId
 	 * @param assignmentId
 	 * @return the number of submissions remaining for the given student and assignment.
 	 * will return AssignmentConstants.UNLIMITED_SUBMISSION constant
 	 *  if there are an unlimited number of submissions allowed
 	 */
 	public int getNumberOfRemainingSubmissionsForStudent(String studentId, Long assignmentId);
 	
 	/**
 	 * 
 	 * @param submission
 	 * @return true if the most recent AssignmentSubmissionVersion for this submission
 	 * is a draft
 	 */
 	public boolean isMostRecentVersionDraft(AssignmentSubmission submission);
 	
 	/**
 	 * set the version to "released" for all of the submissions that the current 
 	 * user is able to submit feedback for. if the user is only authorized for
 	 * a subset of the students for this assignment, the unauthorized students
 	 * will not be affected
 	 * @param assignmentId
 	 * @throws SecurityException if user is not allowed to submit feedback
 	 * @throws AssignmentNotFoundException if no assignment with the given assignmentId
 	 */
 	public void releaseAllFeedbackForAssignment(Long assignmentId);
 	
 	/**
 	 * set all of the non-draft versions for this submission to "released"
 	 * @param submissionId
 	 * @throws SecurityException if the current user is not authorized to 
 	 * submit feedback for the given submission
 	 * @throws SubmissionNotFoundException if no AssignmentSubmission with the given submissionId
 	 */
 	public void releaseAllFeedbackForSubmission(Long submissionId);
 	
 	/**
 	 * release the feedback for the given submission version to the submitter
 	 * @param submissionVersionId
 	 * @throws SecurityException if the current user is not authorized to 
 	 * submit feedback for the given submission
 	 * @throws VersionNotFoundException if no version exists with the given submissionVersionId
 	 */
 	public void releaseFeedbackForVersion(Long submissionVersionId);
 
 	/**
 	 * 
 	 * @param submission
 	 * @return a list of all of the AssignmentSubmissionVersions associated with
 	 * the given submission. 
 	 * if the passed submission does not have an id, will return an empty list. 
 	 * list is ordered by submittedVersionNumber
 	 * If the version is draft and the submitter is not 
 	 * the current user, will not populate the submissionText or 
 	 * submissionAttachmentSet. If the curr user is	the submitter but feedback 
 	 * has not been released, will not populate	feedback. Because of these 
 	 * changes that we don't want to save, the returned submissions were evicted 
 	 * from the session and are not persistent.
 	 * 
 	 */
 	public List<AssignmentSubmissionVersion> getVersionHistoryForSubmission(AssignmentSubmission submission);
 
 	/**
 	 * 
 	 * @param userId
 	 * @param submittedDate
 	 * @return
 	 */
 	public FeedbackVersion getFeedbackByUserIdAndSubmittedDate(String userId, Date submittedDate);
 
 	/**
 	 * Update the feedback aspects of a submission version. The submission version is expected to
 	 * exist prior to using this method.
 	 * 
 	 * @param feedback
 	 */
 	public void updateFeedbackForVersion(FeedbackVersion feedback);
 	
 	/**
 	 * 
 	 * @param studentId
 	 * @param assignmentId
 	 * @return the total number of versions that the given student has submitted
 	 * for the given assignment. does not count draft versions or feedback-only versions
 	 */
 	public int getNumSubmittedVersions(String studentId, Long assignmentId);
 	
 	/**
 	 * 
 	 * @param assignment
 	 * @param studentIds
 	 * @return the number of students from the given studentIds collection who have at least
 	 * one submission for the given assignment
 	 */
 	public int getNumStudentsWithASubmission(Assignment2 assignment, Collection<String> studentIds);
 	
 	/**
 	 * Will update the "feedbackLastViewed" property for each version in the given versionIdList
 	 * to the current date and time if feedback has been released for the version. 
 	 * This allows you to mark that the student has viewed the feedback for these versions
 	 * @param submissionId
 	 * @param versionIdList
 	 * @throws SecurityException if the current user is not the submitter 
 	 */
 	public void markFeedbackAsViewed(Long submissionId, List<Long> versionIdList);
 	
 	/**
 	 * method to update whether or not a student has completed the given assignment(s). 
 	 * will update the "completed" property for the submission associated with the
 	 * given assignmentId depending on the corresponding Boolean value in the 
 	 * assignmentIdToCompletedMap.
 	 * @param studentId
 	 * @param assignmentIdToCompletedMap - map of assignmentId to Boolean to indicate if
 	 * the assignment should be marked as completed (true) or not completed (false)
 	 * @throws SecurityException if current user is not the submitter
 	 * @throws IllegalArgumentException if the Boolean in the map is null - won't update
 	 * any records
 	 */
 	public void markAssignmentsAsCompleted(String studentId, Map<Long, Boolean> assignmentIdToCompletedMap);
 	
 	/**
 	 * 
 	 * @return Non-null list.  The AssignmentSubmissions for the current user
 	 * for all of the user's viewable assignments in the current site.
 	 * If no submission exists yet for an assignment that is available to the user, 
 	 * adds an empty AssigmentSubmission rec to the list for that assignment. 
	 * If a submission exists for an assignment in this site that was deleted,
 	 * the submission is still returned (so the student can still access their work).
 	 * Populates attachments and version history. 
 	 * If feedback has not been released, does not populate
 	 * feedback-related information.  Because of these changes that we don't want 
 	 * to save, the returned submissions were evicted from the session and are not persistent.
 	 * Ordered by completed, then by the assignment sort order
 	 * @throws SecurityException - if the current user is not allowed to make
 	 * submissions in the current site (ie non-students)
 	 */
 	public List<AssignmentSubmission> getSubmissionsForCurrentUser();
 }
