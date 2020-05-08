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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Date;
 import java.util.Map;
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
     private Long gradebookItemId;
     private String contextId;
     private String title;
     private boolean draft;
     private int sortIndex;
     private Date openDate;
     private Date acceptUntilDate;
     private boolean graded;
     private Date dueDate;
     private boolean honorPledge;
     private String instructions;
     private boolean requiresSubmission;
     private int submissionType;
     private boolean sendSubmissionNotifications;
     private boolean hasAnnouncement;
     private String announcementId;
     private boolean addedToSchedule;
     private String eventId;
     private int numSubmissionsAllowed;
     private boolean contentReviewEnabled;
     private String contentReviewRef;
     private String creator;
     private Date createDate;
     private String modifiedBy;
     private Date modifiedDate;
     private boolean removed;
     private int optimisticVersion;
     private Set<AssignmentSubmission> submissionsSet;
     private Set<AssignmentAttachment> attachmentSet;
     private Set<AssignmentGroup> assignmentGroupSet; 
     private Map properties;
 
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
      * @return Returns the id of the associated gradebook item in the Gradebook
      */
     public Long getGradebookItemId() {
         return gradebookItemId;
     }
 
     /**
      * set the id of the associated gradebook item in the Gradebook
      * @param gradebookItemId
      */
     public void setGradebookItemId(Long gradebookItemId) {
         this.gradebookItemId = gradebookItemId;
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
     public boolean isDraft() {
         return draft;
     }
 
     /**
      * draft status
      * @param draft
      */
     public void setDraft(boolean draft) {
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
      * @return The first date and time at which the assignment can be viewed
      */
     public Date getOpenDate() {
         return openDate;
     }
 
     /**
      * The first date and time at which the assignment can be viewed
      * @param openDate
      */
     public void setOpenDate(Date openDate) {
         this.openDate = openDate;
     }
 
     /**
      * @return The date and time after which this assignment is closed for submissions.
      */
     public Date getAcceptUntilDate() {
         return acceptUntilDate;
     }
 
     /**
      * The date and time after which this assignment is closed to submissions.
      * @param acceptUntilDate
      */
     public void setAcceptUntilDate(Date acceptUntilDate) {
         this.acceptUntilDate = acceptUntilDate;
     }
 
     /**
      * All assignments will be linked to the gradebook and store grade
      * information in the gradebook tables except ungraded assignments.  
      * @return true if this assignment is graded
      */
     public boolean isGraded() {
         return graded;
     }
 
     /**
      * All assignments will be linked to the gradebook and store grade
      * information in the gradebook tables except ungraded assignments.  
      * @param graded
      */
     public void setGraded(boolean graded) {
         this.graded = graded;
     }
 
     /**
      * The date and time after which responses to this assignment are considered late.
      * This is an optional setting.
      * @return due date
      */
     public Date getDueDate() {
         return dueDate;
     }
 
     /**
      * The date and time after which responses to this assignment are considered late.
      * This is an optional setting.
      * @param dueDate
      */
     public void setDueDate(Date dueDate) {
         this.dueDate = dueDate;
     }
 
     /**
      * @return Returns true if this assignment requires an honor pledge
      */
     public boolean isHonorPledge() {
         return honorPledge;
     }
 
     /**
      * true if this assignment requires an honor pledge
      * @param honorPledge
      */
     public void setHonorPledge(boolean honorPledge) {
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
      * 
      * @return true if this assignment requires submission.
      * for example, an assignment like "Read Chapter 1" would likely have
      * requiresSubmission = false
      */
     public boolean isRequiresSubmission()
     {
         return requiresSubmission;
     }
 
     /**
      * true if this assignment requires submission.
      * for example, an assignment like "Read Chapter 1" would likely have
      * requiresSubmission = false
      * @param requiresSubmission
      */
     public void setRequiresSubmission(boolean requiresSubmission)
     {
         this.requiresSubmission = requiresSubmission;
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
      * 
      * @return true if we should send notifications upon submission for this assignment
      */
     public boolean isSendSubmissionNotifications()
     {
         return sendSubmissionNotifications;
     }
 
     /**
      * true if we should send notifications upon submission for this assignment
      * @param sendSubmissionNotfications
      */
     public void setSendSubmissionNotifications(boolean sendSubmissionNotifications)
     {
         this.sendSubmissionNotifications = sendSubmissionNotifications;
     }
 
     /**
      * 
      * @return true if the user wants to add an announcement of the open
      * date. this field may be true and the announcementId field null if
      * the assignment is in draft status
      */
     public boolean getHasAnnouncement() {
         return hasAnnouncement;
     }
 
     /**
      * true if the user wants to add an announcement of the open
      * date. this field may be true and the announcementId field null if
      * the assignment is in draft status
      * @param hasAnnouncement
      */
     public void setHasAnnouncement(boolean hasAnnouncement) {
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
     public boolean getAddedToSchedule()
     {
         return addedToSchedule;
     }
 
     /**
      * true if the due date for this assignment should be
      * added to the Schedule (aka Calendar) tool
      * @param addedToSchedule
      */
     public void setAddedToSchedule(boolean addedToSchedule)
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
     public int getNumSubmissionsAllowed() {
         return numSubmissionsAllowed;
     }
 
     /**
      * the number of submissions allowed for this assignment. if -1,
      * unlimited submissions. 
      * @param numSubmissionsAllowed
      */
     public void setNumSubmissionsAllowed(int numSubmissionsAllowed) {
         this.numSubmissionsAllowed = numSubmissionsAllowed;
     }
     
     /**
      * 
      * @return true if plagiarism checking (via ContentReviewService) has
      * been enabled for this assignment
      */
     public boolean isContentReviewEnabled() {  
         return contentReviewEnabled;
     }
 
     /**
      * true if plagiarism checking (via ContentReviewService) has
      * been enabled for this assignment
      * @param contentReviewEnabled
      */
     public void setContentReviewEnabled(boolean contentReviewEnabled)
     {
         this.contentReviewEnabled = contentReviewEnabled;
     }
 
     /**
      * 
      * @return a reference for the content review service item associated with this
      *        assignment. if null, assumes no content review service has been enabled
      *        for this assignment
      */
     public String getContentReviewRef()
     {
         return contentReviewRef;
     }
 
     /**
      * a reference for the content review service item associated with this
      *        assignment. if null, assumes no content review service has been enabled
      *        for this assignment
      * @param contentReviewRef
      */
     public void setContentReviewRef(String contentReviewRef)
     {
         this.contentReviewRef = contentReviewRef;
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
      * @return Date and time this assignment was created
      */
     public Date getCreateDate() {
         return createDate;
     }
 
     /**
      * Date and time this assignment was created
      * @param createDate
      */
     public void setCreateDate(Date createDate) {
         this.createDate = createDate;
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
      * @return Date and time this assignment was last modified
      */
     public Date getModifiedDate() {
         return modifiedDate;
     }
 
     /**
      * Date and time this assignment was last modified
      * @param modifiedDate
      */
     public void setModifiedDate(Date modifiedDate) {
         this.modifiedDate = modifiedDate;
     }
 
     /**
      * 
      * @return true if this assignment was deleted
      */
     public boolean isRemoved() {
         return removed;
     }
 
     /**
      * true if this assignment was deleted
      * @param removed
      */
     public void setRemoved(boolean removed) {
         this.removed = removed;
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
 
 
     /**
      * Unstructured properties for the Assignments Object.  These may or may not
      * be persisted depending on their nature.  Some may be generated at 
      * runtime.
      * 
      * @return
      */
     public Map getProperties() {
         if (properties == null) {
             properties = new HashMap();
         }
         return properties;
     }
 
     /**
      * Unstructured properties for the Assignments Object.  These may or may not
      * be persisted depending on their nature.  Some may be generated at 
      * runtime.
      * 
      * @param properties
      */
     public void setProperties(Map properties) {
         this.properties = properties;
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
        sb.append(Entity.SEPARATOR);
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
         String[] refs = new String[0];
         if (attachmentSet != null) {
             refs = new String[attachmentSet.size()];
             int i = 0;
             for (AssignmentAttachment aa : attachmentSet) {
                 refs[i++] = aa.getAttachmentReference();
             }
         }
         return refs;
     }
 
     public void setAssignmentAttachmentRefs(String[] attachmentRefs)
     {
         Set<AssignmentAttachment> set = new HashSet<AssignmentAttachment>();
         for (int i = 0; i < attachmentRefs.length; i++) {
             if (attachmentRefs[i] != null && !attachmentRefs[i].equals("")) {
                 AssignmentAttachment aa = new AssignmentAttachment();
                 aa.setAssignment(this);
                 aa.setAttachmentReference(attachmentRefs[i]);
                 set.add(aa);
             }
         }
         this.attachmentSet = set;
     }
 
     /**
      * Convenience method for determining if assignment is currently open and
      * due date has not passed. Not persisted.
      * @return true if submission is open for this assignment and the due date
      * has not passed (if a due date was set). 
      * @see #isSubmissionOpen() isSubmissionOpen(): method used to determine if "submission is open"
      */
     public boolean isOpen() {
         boolean isOpen = false;
         if (isSubmissionOpen()) {
             // check to see if the due date has passed
             if (dueDate == null) {
                 // no due date was set
                 isOpen = true;
             } else if (dueDate.after(new Date())) {
                 // the due date has not passed
                 isOpen = true;
             }
         }
 
         return isOpen;
     }
 
     /**
      * Convenience method for determining if this assignment is open for submission.
      * Not persisted.
      * @return true if the current date is after the open date of this assignment
      * and the accept until date has not passed. if there is no accept until date,
      * checks to see if due date has passed 
      */
     public boolean isSubmissionOpen() {
         boolean isSubmissionOpen = false;
         if (openDate.before(new Date())) {
             if (acceptUntilDate == null) {
                 // no accept until date was set, check for due date
                 if (dueDate != null) {
                     if (dueDate.after(new Date())) {
                         isSubmissionOpen = true;
                     }
                 } else {
                     // if there is no due date and no accept until date
                     // allow submission indefinitely
                     isSubmissionOpen = true;
                 }
 
             } else if (acceptUntilDate.after(new Date())) {
                 // the accept until date has not passed
                 isSubmissionOpen = true;
             }
         }
 
         return isSubmissionOpen;
     }
     
     /**
      * 
      * @return true if this assignment accepts attachment submissions. This
      * is a convenience method to determine if the assignment accepts attachments
      * as an option for submission.  The assignment may also accept other forms
      * of submission.
      */
     public boolean acceptsAttachments() {
         boolean acceptsAttachments = false;
         if (this.submissionType == AssignmentConstants.SUBMIT_INLINE_AND_ATTACH ||
                 this.submissionType == AssignmentConstants.SUBMIT_ATTACH_ONLY) {
             acceptsAttachments = true;
         }
         
         return acceptsAttachments;
     }
 }
