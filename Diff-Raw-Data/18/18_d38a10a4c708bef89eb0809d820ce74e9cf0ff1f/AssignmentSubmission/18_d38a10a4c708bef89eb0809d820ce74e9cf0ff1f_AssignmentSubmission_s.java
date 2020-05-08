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
 
 package org.sakaiproject.assignment2.model;
 
 import java.util.Date;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.entity.api.Entity;
 
 /**
  * The AssignmentSubmission object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentSubmission {
 	
 	private Long id;
 	private Assignment2 assignment;
 	private String userId;
 	private Date resubmitCloseDate;
 	private boolean completed;
 	private Integer numSubmissionsAllowed;
 	private String createdBy;
 	private Date createdDate;
 	private String modifiedBy;
 	private Date modifiedDate;
 	private int optimisticVersion;
	private Set<AssignmentSubmissionVersion> submissionHistorySet;
 	
 	/**
 	 * the current submission version must be populated manually b/c we want
 	 * to retrieve the most recent version
 	 */
 	private AssignmentSubmissionVersion currentSubmissionVersion;
 
 	public AssignmentSubmission() {
 	}
 	
 	public AssignmentSubmission(Assignment2 assignment, String userId) {
 		this.assignment = assignment;
 		this.userId = userId;
 	}
 	
 	/**
 	 * 
 	 * @return assignment submission id
 	 */
 	public Long getId() {
 		return id;
 	}
 	
 	/**
 	 * set assignment submission id
 	 * @param id
 	 */
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	/**
 	 * 
 	 * @return	the parent assignment
 	 */
 	public Assignment2 getAssignment() {
 		return assignment;
 	}
 
 	/**
 	 * set the parent assignment
 	 * @param assignment
 	 */
 	public void setAssignment(Assignment2 assignment) {
 		this.assignment = assignment;
 	}
 	
 	/**
 	 * 
 	 * @return user id associated with this submission
 	 */
 	public String getUserId() {
 		return userId;
 	}
 	
 	/**
 	 * set the user id associated with this submission
 	 * @param userId
 	 */
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	
 	/**
 	 * 
 	 * @return date and time after which the submitter may no longer submit this assignment. This
 	 * value overrides the acceptUntilDate on the assignment level if numResubmissionsAllowed is
 	 * populated. if null and resubmission allowed on submission level, may resubmit indefinitely
 	 */
 	public Date getResubmitCloseDate() {
 		return resubmitCloseDate;
 	}
 
 	/**
 	 * date and time after which the submitter may no longer submit this assignment. This
 	 * value overrides the acceptUntilDate on the assignment level if numResubmissionsAllowed is
 	 * populated. if null and resubmission allowed on submission level, may resubmit indefinitely
 	 * @param resubmitCloseDate
 	 */
 	public void setResubmitCloseDate(Date resubmitCloseDate) {
 		this.resubmitCloseDate = resubmitCloseDate;
 	}
 	
 	/**
 	 * 
 	 * @return the number of submissions allowed for this assignment. if -1,
      * unlimited submissions.
 	 */
 	public Integer getNumSubmissionsAllowed() {
 		return numSubmissionsAllowed;
 	}
 
 	/**
 	 * the number of submissions allowed for this assignment. if -1,
      * unlimited submissions.
 	 * @param numResubmissionsAllowed
 	 */
 	public void setNumSubmissionsAllowed(Integer numSubmissionsAllowed) {
 		this.numSubmissionsAllowed = numSubmissionsAllowed;
 	}
 	
 	/**
 	 * 
 	 * @return true if this submission has been marked as "completed". this is
 	 * set to true when the student submits. it may also be changed to true or
 	 * false by the student
 	 */
 	public boolean isCompleted()
 	{
 		return completed;
 	}
 
 	/**
 	 * true if this submission has been marked as "completed". this is
 	 * set to true when the student submits. it may also be changed to true or
 	 * false by the student
 	 * @param completed
 	 */
 	public void setCompleted(boolean completed)
 	{
 		this.completed = completed;
 	}
 	
 	/**
 	 * 
 	 * @return the userId of this submission record's creator
 	 */
 	public String getCreatedBy()
 	{
 		return createdBy;
 	}
 
 	/**
 	 * the userId of this submission record's creator
 	 * @param createdBy
 	 */
 	public void setCreatedBy(String createdBy)
 	{
 		this.createdBy = createdBy;
 	}
 
 	/**
 	 * 
 	 * @return the date and time this submission was created
 	 */
 	public Date getCreatedDate()
 	{
 		return createdDate;
 	}
 
 	/**
 	 * the date and time this submission was created
 	 * @param createdDate
 	 */
 	public void setCreatedDate(Date createdDate)
 	{
 		this.createdDate = createdDate;
 	}
 
 	/**
 	 * 
 	 * @return the userId of the person who last modified this record
 	 */
 	public String getModifiedBy()
 	{
 		return modifiedBy;
 	}
 
 	/**
 	 * the userId of the person who last modified this record
 	 * @param modifiedBy
 	 */
 	public void setModifiedBy(String modifiedBy)
 	{
 		this.modifiedBy = modifiedBy;
 	}
 
 	/**
 	 * 
 	 * @return the date and time this submission was last modified
 	 */
 	public Date getModifiedDate()
 	{
 		return modifiedDate;
 	}
 
 	/**
 	 * the date and time this submission was last modified
 	 * @param modifiedDate
 	 */
 	public void setModifiedDate(Date modifiedDate)
 	{
 		this.modifiedDate = modifiedDate;
 	}
 	
 	/**
 	 * 
 	 * @return a set of AssignmentSubmissionVersion recs that represent
 	 * the submission history for this user
 	 */
 	public Set<AssignmentSubmissionVersion> getSubmissionHistorySet() {
 		return submissionHistorySet;
 	}
 
 	/**
 	 * the set of AssignmentSubmissionVersion recs that represent
 	 * the submission history for this user
 	 * @param submissionHistorySet
 	 */
 	public void setSubmissionHistorySet(Set<AssignmentSubmissionVersion> submissionHistorySet) {
 		this.submissionHistorySet = submissionHistorySet;
 	}
     
     /**
      * 
      * @return version stored for hibernate's automatic optimistic concurrency control.
      * this is not related to any of the submission version data for assignment2
      */
     public int getOptimisticVersion() {
     	return optimisticVersion;
     }
     
     /**
      * version stored for hibernate's automatic optimistic concurrency control.
      * this is not related to any of the submission version data for assignment2
      * @param optimisticVersion
      */
     public void setOptimisticVersion(int optimisticVersion) {
     	this.optimisticVersion = optimisticVersion;
     }
 	
 	// not persisted but convenient to have
 	/**
 	 * <b>Note</b> This is not a persisted field but must be handled specially
 	 * when you want to retrieve or update this information
 	 * @return The most recent AssignmentSubmissionVersion for this submission. Each
 	 * modification to the submission will result in a new AssignmentSubmissionVersion
 	 * record so we maintain a history. 
 	 */
 	public AssignmentSubmissionVersion getCurrentSubmissionVersion() {
 		return currentSubmissionVersion;
 	}
 
 	/**
 	 * <b>Note</b> This is not a persisted field but must be handled specially
 	 * when you want to retrieve or update this information
 	 * 
 	 * Set the most recent AssignmentSubmissionVersion for this submission. Each
 	 * modification to the submission will result in a new AssignmentSubmissionVersion
 	 * record so we maintain a history. 
 	 * @param currentSubmissionVersion
 	 */
 	public void setCurrentSubmissionVersion(AssignmentSubmissionVersion currentSubmissionVersion) {
 		this.currentSubmissionVersion = currentSubmissionVersion;
 	}
 	
 	public String getReference()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(AssignmentConstants.REFERENCE_ROOT);
 		sb.append(Entity.SEPARATOR);
 		sb.append(AssignmentConstants.SUBMISSION_TYPE);
 		sb.append(Entity.SEPARATOR);
 		sb.append(getAssignment().getContextId());
 		sb.append(Entity.SEPARATOR);
 		sb.append(Long.toString(id));
 		return sb.toString();
 	}
 
 }
