 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/api/model/src/java/org/sakaiproject/assignment2/model/AssignmentSubmission.java $
  * $Id: AssignmentSubmission.java 12544 2006-05-03 15:06:26Z wagnermr@iupui.edu $
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
 
 package org.sakaiproject.assignment2.model;
 
 import java.util.Date;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The AssignmentSubmissionVersion object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentSubmissionVersion {
 	
 	private Long submissionVersionId;
 	private AssignmentSubmission assignmentSubmission;
 	private Date submittedTime;
 	private Date releasedTimeForUngraded;
 	private String feedbackText;
 	private String commentForUngraded;
 	private String submittedText;
 	private Boolean draft;
 	private String reviewReportUrl;
 	private int reviewReportScore;
 	private String reviewStatus;
 	private String reviewIconUrl;
 	private String createdBy;
 	private Date createdTime;
 	private String lastFeedbackSubmittedBy;
 	private Date lastFeedbackTime;
 	private Set<AssignmentFeedbackAttachment> feedbackAttachSet;
 	private Set<AssignmentSubmissionAttachment> submissionAttachSet;
 	
 	public AssignmentSubmissionVersion() {
 	}
 	
 	public Long getSubmissionVersionId() {
 		return submissionVersionId;
 	}
 
 	public void setSubmissionVersionId(Long submissionVersionId) {
 		this.submissionVersionId = submissionVersionId;
 	}
 	
 	/**
 	 * @return time this assignment was submitted. If null, assignment has not
 	 * been submitted for this user.
 	 */
 	public Date getSubmittedTime() {
 		return submittedTime;
 	}
 	
 	/**
 	 * Set the time the assignment was submitted. Null if no submission yet.
 	 * @param submittedTime
 	 */
 	public void setSubmittedTime(Date submittedTime) {
 		this.submittedTime = submittedTime;
 	}
 	
 	/**
 	 * 
 	 * @return text composed of the submission with grader-added annotation
 	 */
 	public String getFeedbackText() {
 		return feedbackText;
 	}
 	
 	/**
 	 * 
 	 * @return formated text composed of the submission with grader-added annotation
 	 */
 	public String getFeedbackTextFormatted() {		
    	Pattern p = Pattern.compile("\\{\\{(.+)\\}\\}");
     	Matcher m = p.matcher(feedbackText);
     	StringBuffer sb = new StringBuffer();
     	while(m.find()){
     		m.appendReplacement(sb, "<span class=\"highlight\">$1</span>");
     	}
     	m.appendTail(sb);
 		return sb.toString();
 	}
 
 	/**
 	 * set the feedback text
 	 * @param feedbackText
 	 */
 	public void setFeedbackText(String feedbackText) {
 		this.feedbackText = feedbackText;
 	}
 	
 	/**
 	 * 
 	 * @return the text of the submission
 	 */
 	public String getSubmittedText() {
 		return submittedText;
 	}
 
 	/**
 	 * set the text of the submission
 	 * @param submittedText
 	 */
 	public void setSubmittedText(String submittedText) {
 		this.submittedText = submittedText;
 	}
 	
 	/**
 	 * 
 	 * @return true if the submitter has started working on the submission
 	 * but has not yet submitted it for review
 	 */
 	public Boolean isDraft() {
 		return draft;
 	}
 
 	/**
 	 * set the draft status
 	 * @param draft
 	 */
 	public void setDraft(Boolean draft) {
 		this.draft = draft;
 	}
 	
 	/**
 	 * 
 	 * @return the URL of the content review report (ie from turnitin)
 	 */
 	public String getReviewReportUrl() {
 		return reviewReportUrl;
 	}
 
 	/**
 	 * set the URL of the content review report (ie from turnitin)
 	 * @param reviewReportUrl
 	 */
 	public void setReviewReportUrl(String reviewReportUrl) {
 		this.reviewReportUrl = reviewReportUrl;
 	}
 	
 	/**
 	 * 
 	 * @return the score from the content review service (ie from turnitin)
 	 */
 	public int getReviewReportScore() {
 		return reviewReportScore;
 	}
 
 	/**
 	 * set the score from the content review service (ie from turnitin)
 	 * @param reviewReportScore
 	 */
 	public void setReviewReportScore(int reviewReportScore) {
 		this.reviewReportScore = reviewReportScore;
 	}
 	
 	/**
 	 * 
 	 * @return the status of the content review (ie from turnitin)
 	 */
 	public String getReviewStatus() {
 		return reviewStatus;
 	}
 
 	/**
 	 * set the status of the content review (ie from turnitin)
 	 * @param reviewStatus
 	 */
 	public void setReviewStatus(String reviewStatus) {
 		this.reviewStatus = reviewStatus;
 	}
 	
 	/**
 	 * 
 	 * @return the URL of the content review icon associated with 
 	 * this submission (ie from turnitin)
 	 */
 	public String getReviewIconUrl() {
 		return reviewIconUrl;
 	}
 
 	/**
 	 * set the URL of the content review icon associated with this 
 	 * submission (ie from turnitin)
 	 * @param reviewIconUrl
 	 */
 	public void setReviewIconUrl(String reviewIconUrl) {
 		this.reviewIconUrl = reviewIconUrl;
 	}
 
 	/**
 	 * Ungraded assignments will not be linked to the gb, so this field
 	 * will be used in lieu of the gb comment
 	 * @return comment
 	 */
 	public String getCommentForUngraded() {
 		return commentForUngraded;
 	}
 
 	/**
 	 * Ungraded assignments will not be linked to the gb, so this field
 	 * will be used in lieu of the gb comment
 	 * @param commentForUngraded
 	 */
 	public void setCommentForUngraded(String commentForUngraded) {
 		this.commentForUngraded = commentForUngraded;
 	}
 
 	/**
 	 * 
 	 * @return the Date this submission was released to the submitter. only
 	 * used for ungraded assignments. graded assignment release is set in
 	 * the gradebook
 	 */
 	public Date getReleasedTimeForUngraded() {
 		return releasedTimeForUngraded;
 	}
 
 	/**
 	 * set the Date this submission was released to the submitter. only
 	 * used for ungraded assignments. graded assignment release is set in
 	 * the gradebook
 	 * @param releasedTimeForUngraded
 	 */
 	public void setReleasedTimeForUngraded(Date releasedTimeForUngraded) {
 		this.releasedTimeForUngraded = releasedTimeForUngraded;
 	}
 
 	/**
 	 * 
 	 * @return the userId of the person who made this modification to
 	 * the submission
 	 */
 	public String getCreatedBy() {
 		return createdBy;
 	}
 
 	/**
 	 * set the userId of the person who made this modification to
 	 * the submission
 	 * @param createdBy
 	 */
 	public void setCreatedBy(String createdBy) {
 		this.createdBy = createdBy;
 	}
 	
 	/**
 	 * 
 	 * @return the date this version was created. will be the same as the
 	 * submittedTime unless the instructor is giving feedback where there
 	 * was no submission
 	 */
 	public Date getCreatedTime() {
 		return createdTime;
 	}
 
 	/**
 	 * the date this version was created. will be the same as the
 	 * submittedTime unless the instructor is giving feedback where there
 	 * was no submission
 	 * @param createdTime
 	 */
 	public void setCreatedTime(Date createdTime) {
 		this.createdTime = createdTime;
 	}
 
 	/**
 	 * 
 	 * @return the parent submission record associated with this version
 	 */
 	public AssignmentSubmission getAssignmentSubmission() {
 		return assignmentSubmission;
 	}
 
 	/**
 	 * set the parent submission record associated with this version
 	 * @param assignmentSubmission
 	 */
 	public void setAssignmentSubmission(AssignmentSubmission assignmentSubmission) {
 		this.assignmentSubmission = assignmentSubmission;
 	}
 
 	/**
 	 * 
 	 * @return the userid who last submitted feedback on this version
 	 */
 	public String getLastFeedbackSubmittedBy() {
 		return lastFeedbackSubmittedBy;
 	}
 
 	/**
 	 *  the userid who last submitted feedback on this version
 	 * @param lastFeedbackSubmittedBy
 	 */
 	public void setLastFeedbackSubmittedBy(String lastFeedbackSubmittedBy) {
 		this.lastFeedbackSubmittedBy = lastFeedbackSubmittedBy;
 	}
 
 	/**
 	 * 
 	 * @return the date that the feedback for this version was last updated
 	 */
 	public Date getLastFeedbackTime() {
 		return lastFeedbackTime;
 	}
 
 	/**
 	 * the date that the feedback for this version was last updated
 	 * @param lastFeedbackTime
 	 */
 	public void setLastFeedbackTime(Date lastFeedbackTime) {
 		this.lastFeedbackTime = lastFeedbackTime;
 	}
 
 	/**
 	 * 
 	 * @return the AssignmentFeedbackAttachments associated with this submission
 	 * version
 	 */
 	public Set<AssignmentFeedbackAttachment> getFeedbackAttachSet() {
 		return feedbackAttachSet;
 	}
 
 	/**
 	 * 
 	 * @param feedbackAttachSet
 	 * the AssignmentFeedbackAttachments associated with this submission version
 	 */
 	public void setFeedbackAttachSet(
 			Set<AssignmentFeedbackAttachment> feedbackAttachSet) {
 		this.feedbackAttachSet = feedbackAttachSet;
 	}
 	
 	/**
 	 * 
 	 * @return the AssignmentSubmissionAttachments associated with this
 	 * submission version
 	 */
 	public Set<AssignmentSubmissionAttachment> getSubmissionAttachSet() {
 		return submissionAttachSet;
 	}
 
 	/**
 	 * 
 	 * @param submissionAttachSet
 	 * the AssignmentSubmissionAttachments associated with this
 	 * submission version
 	 */
 	public void setSubmissionAttachSet(
 			Set<AssignmentSubmissionAttachment> submissionAttachSet) {
 		this.submissionAttachSet = submissionAttachSet;
 	}
 	
 	@Override
 	public AssignmentSubmissionVersion clone() {
 		AssignmentSubmissionVersion newVersion = new AssignmentSubmissionVersion();
 		newVersion.setAssignmentSubmission(assignmentSubmission);
 		newVersion.setCommentForUngraded(commentForUngraded);
 		newVersion.setCreatedBy(createdBy);
 		newVersion.setCreatedTime(createdTime);
 		newVersion.setDraft(draft);
 		newVersion.setFeedbackAttachSet(feedbackAttachSet);
 		newVersion.setFeedbackText(feedbackText);
 		newVersion.setLastFeedbackSubmittedBy(lastFeedbackSubmittedBy);
 		newVersion.setLastFeedbackTime(lastFeedbackTime);
 		newVersion.setReleasedTimeForUngraded(releasedTimeForUngraded);
 		newVersion.setReviewIconUrl(reviewIconUrl);
 		newVersion.setReviewReportScore(reviewReportScore);
 		newVersion.setReviewReportUrl(reviewReportUrl);
 		newVersion.setReviewReportScore(reviewReportScore);
 		newVersion.setReviewStatus(reviewStatus);
 		newVersion.setSubmissionAttachSet(submissionAttachSet);
 		newVersion.setSubmittedText(submittedText);
 		newVersion.setSubmittedTime(submittedTime);
 		
 		return newVersion;
 	}
 
 }
