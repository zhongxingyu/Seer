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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 import java.util.Set;
 import java.util.HashSet;
 
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.entity.api.Entity;
 
 /**
  * The Assignment object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class Assignment2 {
 
     private Long id;
     private Long gradableObjectId;
     private String contextId;
     private String title;
     private Boolean draft;
     private int sortIndex;
     private Date openTime;
     private Date acceptUntilTime;
     private Boolean ungraded;
     private Date dueDate;
     private Boolean honorPledge;
     private String instructions;
     private int submissionType;
     private int notificationType;
     private Boolean hasAnnouncement;
     private String announcementId;
     private Boolean addedToSchedule;
     private String eventId;
     private Integer numSubmissionsAllowed;
     private Boolean allowReviewService;
     private Boolean allowStudentViewReport;
     private String creator;
     private Date createTime;
     private String modifiedBy;
     private Date modifiedTime;
     private Boolean removed;
     private int revisionVersion;
     private Set<AssignmentSubmission> submissionsSet;
     private Set<AssignmentAttachment> attachmentSet;
     private Set<AssignmentGroup> assignmentGroupSet; 
 
 	public Assignment2(Long id, Long gradableObjectId,
 			String contextId, String title, Boolean draft, int sortIndex,
 			Date openTime, Date acceptUntilTime, Boolean ungraded,
 			Date dueDate, Boolean honorPledge, String instructions,
 			int submissionType, int notificationType, Boolean hasAnnouncement,
 			String announcementId, Boolean addedToSchedule, String scheduleId, Integer numSubmissionsAllowed,
 			Boolean allowReviewService,	Boolean allowStudentViewReport, 
 			String creator, Date createTime, String modifiedBy, Date modifiedTime, Boolean removed) {
 		this.id = id;
 		this.gradableObjectId = gradableObjectId;
 		this.contextId = contextId;
 		this.title = title;
 		this.draft = draft;
 		this.sortIndex = sortIndex;
 		this.openTime = openTime;
 		this.acceptUntilTime = acceptUntilTime;
 		this.ungraded = ungraded;
 		this.dueDate = dueDate;
 		this.honorPledge = honorPledge;
 		this.instructions = instructions;
 		this.submissionType = submissionType;
 		this.notificationType = notificationType;
 		this.hasAnnouncement = hasAnnouncement;
 		this.announcementId = announcementId;
 		this.addedToSchedule = addedToSchedule;
 		this.eventId = scheduleId;
 		this.numSubmissionsAllowed = numSubmissionsAllowed;
 		this.allowReviewService = allowReviewService;
 		this.allowStudentViewReport = allowStudentViewReport;
 		this.creator = creator;
 		this.createTime = createTime;
 		this.modifiedBy = modifiedBy;
 		this.modifiedTime = modifiedTime;
 		this.removed = removed;
 	}
 
 	/**
      * Default constructor
      */
     public Assignment2() {
     }
     
     /**
      * Getters and Setters
      */
     
     /**
      * @return Returns the assignment id
      */
     public Long getId() {
         return id;
     }
     
     /**
      * set the assignment id
      * @param id
      */
     public void setId(Long id) {
         this.id = id;
     }
     
     /**
      * @return Returns the id of the associated GradableObject in the Gradebook
      */
     public Long getGradableObjectId() {
         return gradableObjectId;
     }
     
     /**
      * set the GradableObject id
      * @param gradableObjectId
      */
     public void setGradableObjectId(Long gradableObjectId) {
         this.gradableObjectId = gradableObjectId;
     }
     
     /**
      * @return Returns the context id
      */
     public String getContextId() {
         return contextId;
     }
     
     /**
      * set the contextId
      * @param contextId
      */
     public void setContextId(String contextId) {
         this.contextId = contextId;
     }
     
     /**
      * 
      * @return the assignment's title
      */
     public String getTitle() {
     	return title;
     }
     
     /**
      * set the assignment title
      * @param title
      */
     public void setTitle(String title) {
     	this.title = title;
     }
     
     /**
      * @return Returns this assignment's draft status
      */
     public Boolean isDraft() {
         return draft;
     }
     
     /**
      * draft status
      * @param draft
      */
     public void setDraft(Boolean draft) {
         this.draft = draft;
     }
     
     /**
      * @return Returns the sort index that determines this assignment's ordering
      */
     public int getSortIndex() {
         return sortIndex;
     }
     
     /**
      * the sort index that determines this assignment's ordering
      * @param sortIndex
      */
     public void setSortIndex(int sortIndex) {
         this.sortIndex = sortIndex;
     }
     
     /**
      * @return The first time at which the assignment can be viewed
      */
     public Date getOpenTime() {
         return openTime;
     }
     
     /**
      * The first time at which the assignment can be viewed
      * @param openTime
      */
     public void setOpenTime(Date openTime) {
         this.openTime = openTime;
     }
 
     /**
      * @return The time after which this assignment is closed to submissions.
      */
     public Date getAcceptUntilTime() {
         return acceptUntilTime;
     }
     
     /**
      * The time after which this assignment is closed to submissions.
      * @param acceptUntilTime
      */
     public void setAcceptUntilTime(Date acceptUntilTime) {
         this.acceptUntilTime = acceptUntilTime;
     }
     
     /**
      * All assignments will be linked to the gradebook and store grade
      * information in the gradebook tables except ungraded assignments.  
      * @return true if this assignment is ungraded
      */
     public Boolean isUngraded() {
     	return ungraded;
     }
     
     /**
      * All assignments will be linked to the gradebook and store grade
      * information in the gradebook tables except ungraded assignments.  
      * @param ungraded
      */
     public void setUngraded(Boolean ungraded) {
     	this.ungraded = ungraded;
     }
     
     /**
      * The time after which responses to this assignment are considered late.
      * This is an optional setting.
      * @return due date
      */
     public Date getDueDate() {
     	return dueDate;
     }
     
     /**
      * The time after which responses to this assignment are considered late.
      * This is an optional setting.
      * @param dueDate
      */
     public void setDueDate(Date dueDate) {
     	this.dueDate = dueDate;
     }
     
     /**
      * @return Returns true if this assignment requires an honor pledge
      */
     public Boolean isHonorPledge() {
         return honorPledge;
     }
     
     /**
      * true if this assignment requires an honor pledge
      * @param honorPledge
      */
     public void setHonorPledge(Boolean honorPledge) {
         this.honorPledge = honorPledge;
     }
     
     /**
      * @return Instructions for this assignment
      */
     public String getInstructions() {
         return instructions;
     }
     
     /**
      * Instructions for this assignment
      * @param instructions
      */
     public void setInstructions(String instructions) {
         this.instructions = instructions;
     }
     
     /**
      * @return Returns equivalent int value of the submission type
      * ie inline only, inline and attachments, non-electronic, etc
      */
     public int getSubmissionType() {
         return submissionType;
     }
     
     /**
      * equivalent int value of the submission type
      * ie inline only, inline and attachments, non-electronic, etc
      * @param submissionType
      */
     public void setSubmissionType(int submissionType) {
         this.submissionType = submissionType;
     }
     
     /**
      * @return Returns the equivalent int value of the notification setting for this
      * assignment
      * ie Do not send me notifications of student submissions,
      * 		Send me notification for each student submission, etc
      */
     public int getNotificationType() {
         return notificationType;
     }
 
     /**
      * the equivalent int value of the notification setting for this
      * assignment
      * ie Do not send me notifications of student submissions,
      * 		Send me notification for each student submission, etc
      * @param notificationType
      */
     public void setNotificationType(int notificationType) {
         this.notificationType = notificationType;
     }
     
 	/**
 	 * 
 	 * @return true if the user wants to add an announcement of the open
 	 * date. this field may be true and the announcementId field null if
 	 * the assignment is in draft status
 	 */
 	public Boolean getHasAnnouncement() {
 		return hasAnnouncement;
 	}
 
 	/**
 	 * true if the user wants to add an announcement of the open
 	 * date. this field may be true and the announcementId field null if
 	 * the assignment is in draft status
 	 * @param hasAnnouncement
 	 */
 	public void setHasAnnouncement(Boolean hasAnnouncement) {
 		this.hasAnnouncement = hasAnnouncement;
 	}
     
     /**
      * @return Returns id of the announcement announcing the open date of this assignment.
      * If null, the announcement no announcement exists for this event in the 
      * announcements tool
      */
     public String getAnnouncementId() {
         return announcementId;
     }
     
     /**
      * id of the announcement announcing the open date of this assignment.
      * If null, the announcement no announcement exists for this event in the 
      * announcements tool
      * @param announcementId
      */
     public void setAnnouncementId(String announcementId) {
         this.announcementId = announcementId;
     }
     
     /**
      * 
      * @return true if the due date for this assignment should be
      * added to the Schedule (aka Calendar) tool
      */
 	public Boolean getAddedToSchedule()
 	{
 		return addedToSchedule;
 	}
 
 	/**
 	 * true if the due date for this assignment should be
      * added to the Schedule (aka Calendar) tool
 	 * @param addedToSchedule
 	 */
 	public void setAddedToSchedule(Boolean addedToSchedule)
 	{
 		this.addedToSchedule = addedToSchedule;
 	}
 
 	/**
 	 * 
 	 * @return the id of the event announcing the assignment's due date in
 	 * the Schedule (aka Calendar) tool. 
 	 */
 	public String getEventId()
 	{
 		return eventId;
 	}
 
 	/**
 	 * the id of the event announcing the assignment's due date in
 	 * the Schedule (aka Calendar) tool.
 	 * @param eventId
 	 */
 	public void setEventId(String eventId)
 	{
 		this.eventId = eventId;
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
      * @param numSubmissionsAllowed
      */
 	public void setNumSubmissionsAllowed(Integer numSubmissionsAllowed) {
 		this.numSubmissionsAllowed = numSubmissionsAllowed;
 	}
     
     /**
      * @return If true, this assignment allows a review service (ie TurnItIn)
      */
     public Boolean isAllowReviewService() {
         return allowReviewService;
     }
 
     /**
      * If true, this assignment allows a review service (ie TurnItIn)
      * @param allowReviewService
      */
     public void setAllowReviewService(Boolean allowReviewService) {
         this.allowReviewService = allowReviewService;
     }
     
     /**
      * @return If true, students are allowed to view the review service report
      */
     public Boolean isAllowStudentViewReport() {
         return allowStudentViewReport;
     }
 
     /**
      * If true, students are allowed to view the review service report
      * @param allowStudentViewReport
      */
     public void setAllowStudentViewReport(Boolean allowStudentViewReport) {
         this.allowStudentViewReport = allowStudentViewReport;
     }
     
     /**
      * @return User id of this assignment's creator
      */
     public String getCreator() {
         return creator;
     }
     
     /**
      * User id of this assignment's creator
      * @param creator
      */
     public void setCreator(String creator) {
         this.creator = creator;
     }
     
     /**
      * @return Time this assignment was created
      */
     public Date getCreateTime() {
         return createTime;
     }
 
     /**
      * Time this assignment was created
      * @param createTime
      */
     public void setCreateTime(Date createTime) {
         this.createTime = createTime;
     }
     
     /**
      * @return User id of the last modifier
      */
     public String getModifiedBy() {
         return modifiedBy;
     }
 
     /**
      * User id of the last modifier
      * @param modifiedBy
      */
     public void setModifiedBy(String modifiedBy) {
         this.modifiedBy = modifiedBy;
     }
     
     /**
      * @return Time this assignment was last modified
      */
     public Date getModifiedTime() {
         return modifiedTime;
     }
 
     /**
      * Time this assignment was last modified
      * @param modifiedTime
      */
     public void setModifiedTime(Date modifiedTime) {
         this.modifiedTime = modifiedTime;
     }
     
     /**
      * 
      * @return true if this assignment was deleted
      */
     public Boolean isRemoved() {
     	return removed;
     }
     
     /**
      * true if this assignment was deleted
      * @param removed
      */
     public void setRemoved(Boolean removed) {
     	this.removed = removed;
     }
     
     /**
      * 
      * @return the int value of the version number for this assignment. not
      * to be confused with submission version.
      */
     public int getRevisionVersion() {
     	return revisionVersion;
     }
     
     /**
      * the int value of the version number for this assignment. not
      * to be confused with submission version.
      * @param version
      */
     public void setRevisionVersion(int revisionVersion) {
     	this.revisionVersion = revisionVersion;
     }
     
     /**
      * 
      * @return Set of AssignmentAttachments associated with this assignment
      */
     public Set<AssignmentAttachment> getAttachmentSet() {
 		return attachmentSet;
 	}
 
     /**
      * 
      * @param attachmentSet
      * Set of AssignmentAttachments associated with this assignment
      */
 	public void setAttachmentSet(Set<AssignmentAttachment> attachmentSet) {
 		this.attachmentSet = attachmentSet;
 	}
 
 	/**
 	 * 
 	 * @return the AssignmentGroups that this assignment is restricted to
 	 */
 	public Set<AssignmentGroup> getAssignmentGroupSet() {
 		return assignmentGroupSet;
 	}
 
 	/**
 	 * 
 	 * @param assignmentGroupSet
 	 * the AssignmentGroups that this assignment is restricted to
 	 */
 	public void setAssignmentGroupSet(Set<AssignmentGroup> assignmentGroupSet) {
 		this.assignmentGroupSet = assignmentGroupSet;
 	}
 	
 	/**
 	 * 
 	 * @return the set of AssignmentSubmission recs associated with this assignment
 	 */
 	public Set<AssignmentSubmission> getSubmissionsSet() {
 		return submissionsSet;
 	}
 
 	/**
 	 * the set of AssignmentSubmission recs associated with this assignment
 	 * @param submissionsSet
 	 */
 	public void setSubmissionsSet(Set<AssignmentSubmission> submissionsSet) {
 		this.submissionsSet = submissionsSet;
 	}
 	
 	
 	// Convenience methods
 	
 	/**
 	 * 
 	 * @return a list of the group references (realms) for the AssignmentGroup
 	 * objects associated with this assignment
 	 */
 	public List<String> getListOfAssociatedGroupReferences() {
 		List<String> groupReferences = new ArrayList<String>();
 		if (assignmentGroupSet != null) {
 			for (AssignmentGroup group : assignmentGroupSet) {
 				if (group != null) {
 					groupReferences.add(group.getGroupId());
 				}
 			}
 		}
 		
 		return groupReferences;
 	}
 	
 	public String getReference()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(AssignmentConstants.REFERENCE_ROOT);
 		sb.append(Entity.SEPARATOR);
 		sb.append(AssignmentConstants.ASSIGNMENT_TYPE);
 		sb.append(Entity.SEPARATOR);
 		sb.append(contextId);
 		sb.append(Entity.SEPARATOR);
 		sb.append(Long.toString(id));
 		return sb.toString();
 	}
 
 	public String[] getAssignmentAttachmentRefs()
 	{
		String[] refs = new String[attachmentSet.size()];
		int i = 0;
		for (AssignmentAttachment aa : attachmentSet) {
			refs[i++] = aa.getAttachmentReference();
 		}
 		return refs;
 	}
 
 	public void setAssignmentAttachmentRefs(String[] attachmentRefs)
 	{
 		Set<AssignmentAttachment> set = new HashSet<AssignmentAttachment>();
 		for (int i = 0; i < attachmentRefs.length; i++) {
 			AssignmentAttachment aa = new AssignmentAttachment();
 			aa.setAssignment(this);
 			aa.setAttachmentReference(attachmentRefs[i]);
 			set.add(aa);
 		}
 		this.attachmentSet = set;
 	}
 }
