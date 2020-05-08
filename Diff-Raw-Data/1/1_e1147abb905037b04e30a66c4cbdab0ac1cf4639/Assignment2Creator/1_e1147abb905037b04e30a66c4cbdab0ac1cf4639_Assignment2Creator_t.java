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
 
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 
 import uk.org.ponder.messageutil.MessageLocator;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Set;
 import java.util.HashSet;
 
 public class Assignment2Creator {
 
     //public static final String DEFAULT_TITLE = "";
     private ExternalLogic externalLogic;
     private MessageLocator messageLocator;
 
     public Assignment2 create() {
 
         Assignment2 togo = new Assignment2();
         togo.setTitle("");
         togo.setContextId(externalLogic.getCurrentContextId());
         togo.setHonorPledge(false);
         togo.setGraded(false);
         togo.setRequiresSubmission(true);
         togo.setHasAnnouncement(false);
         togo.setAddedToSchedule(false);
         togo.setSubmissionType(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH);
         togo.setAttachmentSet(new HashSet<AssignmentAttachment>());
        togo.setNumSubmissionsAllowed(1);
 
         //Setting up Dates
         Calendar cal = Calendar.getInstance();
         //cal.set(Calendar.HOUR_OF_DAY, 12);
         //cal.set(Calendar.MINUTE, 0);
         Date openDate = cal.getTime();
         cal.add(Calendar.DAY_OF_YEAR, 7);
         cal.set(Calendar.HOUR_OF_DAY, 17);
 
         togo.setOpenDate(openDate);
         //togo.setDueDate(closeDate);
         //togo.setAcceptUntilDate(closeDate);
 
         return togo;
     }
 
     public Assignment2 createDuplicate(Assignment2 assignment) {
         Assignment2 dup = new Assignment2();
 
         String newTitle = messageLocator.getMessage("Assignment2Creator.duplicate.title", assignment.getTitle());
 
         dup.setGradableObjectId(assignment.getGradableObjectId());
         dup.setContextId(assignment.getContextId());
         dup.setTitle(newTitle);
         dup.setDraft(true);
         dup.setSortIndex(assignment.getSortIndex());
         dup.setOpenDate(assignment.getOpenDate());
         dup.setAcceptUntilDate(assignment.getAcceptUntilDate());
         dup.setGraded(assignment.isGraded());
         dup.setDueDate(assignment.getDueDate());
         dup.setHonorPledge(assignment.isHonorPledge());
         dup.setInstructions(assignment.getInstructions());
         dup.setSubmissionType(assignment.getSubmissionType());
         dup.setNotificationType(assignment.getNotificationType());
         dup.setHasAnnouncement(assignment.getHasAnnouncement());
         dup.setAddedToSchedule(assignment.getAddedToSchedule());
         dup.setRemoved(false);
         dup.setNumSubmissionsAllowed(assignment.getNumSubmissionsAllowed());
         dup.setRequiresSubmission(assignment.isRequiresSubmission());
 
         // let's duplicate the attachments and group restrictions
         Set<AssignmentGroup> assignGroupSet = new HashSet<AssignmentGroup>();
         if (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()) {
             for (AssignmentGroup group : assignment.getAssignmentGroupSet()) {
                 if (group != null) {
                     AssignmentGroup newGroup = new AssignmentGroup(dup, group.getGroupId());
                     assignGroupSet.add(newGroup);
                 }
             }
         }
 
         Set<AssignmentAttachment> attachSet = new HashSet<AssignmentAttachment>();
         if (assignment.getAttachmentSet() != null && !assignment.getAttachmentSet().isEmpty()) {
             for (AssignmentAttachment attach : assignment.getAttachmentSet()) {
                 if (attach != null) {
                     AssignmentAttachment newGroup = new AssignmentAttachment(dup, attach.getAttachmentReference());
                     attachSet.add(newGroup);
                 }
             }
         }
 
         dup.setAssignmentGroupSet(assignGroupSet);
         dup.setAttachmentSet(attachSet);
 
         return dup;
     }
 
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public void setMessageLocator (MessageLocator messageLocator) {
         this.messageLocator = messageLocator;
     }
 }
