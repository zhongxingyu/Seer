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
 
 package org.sakaiproject.assignment2.logic.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment.api.Assignment;
 import org.sakaiproject.assignment.api.AssignmentContent;
 import org.sakaiproject.assignment.api.AssignmentService;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AnnouncementPermissionException;
 import org.sakaiproject.assignment2.exception.CalendarPermissionException;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.ExternalContentLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.logic.ImportExportLogic;
 import org.sakaiproject.assignment2.logic.entity.AssignmentDefinition;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.site.api.Group;
 
 
 /**
  * This is the implementation of methods related to the import/export of
  * Assignment2 data.
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class ImportExportLogicImpl implements ImportExportLogic {
 
 	private static Log log = LogFactory.getLog(ImportExportLogicImpl.class);
 
 	private ExternalLogic externalLogic;
 	public void setExternalLogic(ExternalLogic externalLogic) {
 		this.externalLogic = externalLogic;
 	}
 
 	private ExternalGradebookLogic gradebookLogic;
 	public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
 		this.gradebookLogic = gradebookLogic;
 	}
 
 	private AssignmentLogic assignmentLogic;
 	public void setAssignmentLogic(AssignmentLogic assignmentLogic) {
 		this.assignmentLogic = assignmentLogic;
 	}
 
 	private AssignmentDao dao;
 	public void setDao(AssignmentDao dao) {
 		this.dao = dao;
 	}
 
 	private AssignmentService assignmentService;
 	public void setAssignmentService(AssignmentService assignmentService) {
 		this.assignmentService = assignmentService;
 	}
 
 	private ExternalContentLogic contentLogic;
 	public void setExternalContentLogic(ExternalContentLogic contentLogic) {
 		this.contentLogic = contentLogic;
 	}
 
 	public void init(){
 		if(log.isDebugEnabled()) log.debug("init");
 	}
 
 	public String getAssignmentToolDefinitionXML(String contextId) {
 		AssignmentToolDefinition toolDefinition = new AssignmentToolDefinition();
 		toolDefinition.setAssignments(getAssignmentDefinitionsInContext(contextId));
 
 		return VersionedExternalizable.toXml(toolDefinition);
 	}
 
 	public List<AssignmentDefinition> getAssignmentDefinitionsInContext(String contextId) {
 
 		if (contextId == null) {
 			throw new IllegalArgumentException("Null contextId passed to getAssignmentDefinitionsInContext");
 		}
 		List<AssignmentDefinition> assignList = new ArrayList<AssignmentDefinition>();
 
 		List<Assignment2> allAssignments = dao.getAssignmentsWithGroupsAndAttachments(contextId);
 		if (allAssignments != null && !allAssignments.isEmpty()) {
 			List<GradebookItem> allGbItems = gradebookLogic.getAllGradebookItems(contextId);
 			Map<Long, GradebookItem> gbIdItemMap = new HashMap<Long, GradebookItem>();
 			if (allGbItems != null) {
 				for (GradebookItem item : allGbItems) {
 					gbIdItemMap.put(item.getGradebookItemId(), item);
 				}
 			}
 
 			Map<String, String> groupIdToTitleMap = new HashMap<String, String>();
 			Collection<Group> groups = externalLogic.getSiteGroups(contextId);
 			if (groups != null) {
 				for (Group group : groups) {
 					if (group != null) {
 						groupIdToTitleMap.put(group.getId(), group.getTitle());
 					}
 				}
 			}
 
 			for (Assignment2 assign : allAssignments) {
 				if (assign != null) {
 					AssignmentDefinition assignDef = getAssignmentDefinition(assign, gbIdItemMap, groupIdToTitleMap);
 					assignList.add(assignDef);
 				}
 			}
 		}
 
 		return assignList;
 	}
 
 	private AssignmentDefinition getAssignmentDefinition(Assignment2 assignment, Map<Long, GradebookItem> gbIdItemMap,
 			Map<String, String> groupIdToTitleMap) {
 		if (assignment == null) {
 			throw new IllegalArgumentException("Null assignment passed to getAssignmentDefinition");
 		}
 
 		AssignmentDefinition assignDef = new AssignmentDefinition();
 		assignDef.setAcceptUntilDate(assignment.getAcceptUntilDate());
 		assignDef.setDraft(assignment.isDraft());
 		assignDef.setDueDate(assignment.getDueDate());
 		assignDef.setHasAnnouncement(assignment.getHasAnnouncement());
 		assignDef.setHonorPledge(assignment.isHonorPledge());
 		assignDef.setInstructions(assignment.getInstructions());
 		assignDef.setSendSubmissionNotifications(assignment.isSendSubmissionNotifications());
 		assignDef.setNumSubmissionsAllowed(assignment.getNumSubmissionsAllowed());
 		assignDef.setOpenDate(assignment.getOpenDate());
 		assignDef.setSortIndex(assignment.getSortIndex());
 		assignDef.setSubmissionType(assignment.getSubmissionType());
 		assignDef.setTitle(assignment.getTitle());
 		assignDef.setGraded(assignment.isGraded());
 		assignDef.setRequiresSubmission(assignment.isRequiresSubmission());
 
 		// if it is graded, we need to retrieve the name of the associated gb item
 		if (assignment.isGraded() && assignment.getGradebookItemId() != null &&
 				gbIdItemMap != null) {
 			GradebookItem gbItem = (GradebookItem)gbIdItemMap.get(assignment.getGradebookItemId());
 			if (gbItem != null) {
 				assignDef.setAssociatedGbItemName(gbItem.getTitle());
 				assignDef.setAssociatedGbItemPtsPossible(gbItem.getPointsPossible());
 			}
 		}
 
 		// we need to make a list of the attachment references
 		List<String> attachRefList = new ArrayList<String>();
 		if (assignment.getAttachmentSet() != null) {
 			for (AssignmentAttachment attach : assignment.getAttachmentSet()) {
 				if (attach != null) {
 					attachRefList.add(attach.getAttachmentReference());
 				}
 			}
 		}
 		assignDef.setAttachmentReferences(attachRefList);
 
 		// we need to make a list of the group names
 		List<String> associatedGroupNames = new ArrayList<String>();
 		if (assignment.getAssignmentGroupSet() != null && groupIdToTitleMap != null) {
 			for (AssignmentGroup aGroup : assignment.getAssignmentGroupSet()) {
 				if (aGroup != null) {
 					String groupName = (String)groupIdToTitleMap.get(aGroup.getGroupId());
 					if (groupName != null) {
 						associatedGroupNames.add(groupName);
 					}
 				}
 			}
 		}
 		assignDef.setGroupRestrictionGroupTitles(associatedGroupNames);
 
 		return assignDef;
 	}
 
 	public void mergeAssignmentToolDefinitionXml(String toContext, String fromAssignmentToolXml) {
 		AssignmentToolDefinition toolDefinition = 
 			(AssignmentToolDefinition)VersionedExternalizable.fromXml(fromAssignmentToolXml);
 
 		if (toolDefinition != null) {
 			if(toolDefinition.getAssignments() != null) {
 
 				// retrieve a list of all of the gb items in this site
 				List<GradebookItem> currGbItems = gradebookLogic.getAllGradebookItems(toContext);
 				// make a map of item title to item
 				Map<String, GradebookItem> gbTitleToItemMap = new HashMap<String, GradebookItem>();
 				if (currGbItems != null) {
 					for (GradebookItem item : currGbItems) {
 						if (item != null) {
 							gbTitleToItemMap.put(item.getTitle(), item);
 						}
 					}
 				}
 
 				// make a map of all of the titles of groups in the new site
 				Collection<Group> currGroups = externalLogic.getSiteGroups(toContext);
 				Map<String, Group> groupTitleGroupMap = new HashMap<String, Group>();
 				for (Group group : currGroups) {
 					if (group != null) {
 						groupTitleGroupMap.put(group.getTitle(), group);
 					}
 				}
 
 				// we will iterate through all of the assignments to be imported
 				for (AssignmentDefinition assignDef : toolDefinition.getAssignments()) {
 					if (assignDef != null) {
 						Assignment2 newAssignment = new Assignment2();
 						newAssignment.setAcceptUntilDate(assignDef.getAcceptUntilDate());
 						newAssignment.setContextId(toContext);
 						newAssignment.setDraft(assignDef.isDraft());
 						newAssignment.setHonorPledge(assignDef.isHonorPledge());
 						newAssignment.setInstructions(assignDef.getInstructions());
 						newAssignment.setSendSubmissionNotifications(assignDef.isSendSubmissionNotifications());
 						newAssignment.setNumSubmissionsAllowed(assignDef.getNumSubmissionsAllowed());
 						newAssignment.setOpenDate(assignDef.getOpenDate());
 						newAssignment.setDueDate(assignDef.getDueDate());
 						newAssignment.setRequiresSubmission(assignDef.isRequiresSubmission());
 
 						// we don't set the sort index here. the sort index will
 						// be generated upon saving. so make sure that the assignments
 						// in the xml are in the order you want them to appear
 						// in the UI
 
 						newAssignment.setSubmissionType(assignDef.getSubmissionType());
 						newAssignment.setGraded(assignDef.isGraded());
 						newAssignment.setHasAnnouncement(assignDef.isHasAnnouncement());
 						newAssignment.setAddedToSchedule(assignDef.isAddedToSchedule());
 
 						// title doesn't have to be unique
 						newAssignment.setTitle(assignDef.getTitle());
 
 						// if this item is graded, we need to link it up to a 
 						// corresponding gb item. first, we will check to see if
 						// the gb item already exists. if there is an item with
 						// the same name and points possible value, we will link
 						// our assignment to this item. Otherwise, we will create
 						// a new gradebook item
 						if (assignDef.isGraded() && assignDef.getAssociatedGbItemName() != null) {
 							Long associatedGbItemId = null;
							GradebookItem existingItem = gbTitleToItemMap.get(assignDef.getAssociatedGbItemName().trim());
 							if (existingItem != null) {
 								// an item exists with this title already - check the points possible
 								if (existingItem.getPointsPossible() == null && assignDef.getAssociatedGbItemPtsPossible() == null) {
 									associatedGbItemId = existingItem.getGradebookItemId();
 								} else if (existingItem.getPointsPossible() != null && 
 										assignDef.getAssociatedGbItemPtsPossible() != null &&
 										existingItem.getPointsPossible().equals(assignDef.getAssociatedGbItemPtsPossible())) {
 									associatedGbItemId = existingItem.getGradebookItemId();
 								} else {
 									// we need to create a new item
 									// be careful b/c multiple assignments may be associated with
 									// the same gb item. we don't want to try to create it more than once
 									String gbItemTitle = getNewTitle(assignDef.getAssociatedGbItemName(), 
 											new ArrayList<String>(gbTitleToItemMap.keySet()));
 									associatedGbItemId = gradebookLogic.createGbItemInGradebook(toContext, 
 											gbItemTitle, assignDef.getAssociatedGbItemPtsPossible(), 
 											assignDef.getDueDate(), false, false);
 									if (log.isDebugEnabled()) log.debug("New gb item created via import!");
 
 									// now let's retrieve it and add it to our map of existing gb items
 									// so we don't try to create it again
 									GradebookItem newItem = gradebookLogic.getGradebookItemById(toContext, associatedGbItemId);
 									if (newItem != null) {
 										gbTitleToItemMap.put(newItem.getTitle(), newItem);
 									}
 								}
 							} else {
 								// this is a new item
 								associatedGbItemId = gradebookLogic.createGbItemInGradebook(toContext, 
 										assignDef.getAssociatedGbItemName(), assignDef.getAssociatedGbItemPtsPossible(), 
 										assignDef.getDueDate(), false, false);
 								if (log.isDebugEnabled()) log.debug("New gb item created via import!");
 								// now let's retrieve it and add it to our map of existing gb items
 								// so we don't try to create it again
 								GradebookItem newItem = gradebookLogic.getGradebookItemById(toContext, associatedGbItemId);
 								if (newItem != null) {
 									gbTitleToItemMap.put(newItem.getTitle(), newItem);
 								}
 							}
 
 							newAssignment.setGradebookItemId(associatedGbItemId);
 						}
 
 						// we need to copy any associated attachments 
 						if (assignDef.getAttachmentReferences() != null && 
 								!assignDef.getAttachmentReferences().isEmpty()) {
 
 							Set<AssignmentAttachment> attachSet = new HashSet<AssignmentAttachment>();
 							for (String attRef : assignDef.getAttachmentReferences()) {
 								String newAttId = contentLogic.copyAttachment(attRef, toContext);
 								if (newAttId != null) {
     								AssignmentAttachment newAA = new AssignmentAttachment(newAssignment, newAttId);
     								attachSet.add(newAA);
 								} else {
 								    log.warn("A copy of attachment with ref:" + 
 								            attRef + " was NOT created because an error occurred.");
 								}
 							}
 							newAssignment.setAttachmentSet(attachSet);
 						}
 
 						// if a group with the same name exists in the new site,
 						// associate the assignment with that group
 						if (assignDef.getGroupRestrictionGroupTitles() != null &&
 								!assignDef.getGroupRestrictionGroupTitles().isEmpty() &&
 								groupTitleGroupMap != null) {
 
 							Set<AssignmentGroup> assignGroupSet = new HashSet<AssignmentGroup>();
 							// now iterate through the groups from the old site
 							// to see if a group with that name exists in the
 							// new site
 							for (String groupTitle : assignDef.getGroupRestrictionGroupTitles()) {
 								Group group = groupTitleGroupMap.get(groupTitle);
 								if (group != null) {
 									// the group exists, so create AssignmentGroup
 									AssignmentGroup ag = new AssignmentGroup(newAssignment, group.getId());
 									assignGroupSet.add(ag);
 								}
 							}
 
 							newAssignment.setAssignmentGroupSet(assignGroupSet);
 
 						}
 
 						try {
 							assignmentLogic.saveAssignment(newAssignment);
 							if (log.isDebugEnabled()) log.debug("New assignment " + 
 									newAssignment.getTitle() + " added in site " + toContext);
 						} catch (AnnouncementPermissionException ape) {
 							log.warn("No announcements were added because the user does not have permission in the announcements tool");
 						} catch (CalendarPermissionException cpe) {
 							log.warn("No events were added because the user does not have permission in the Schedule tool");
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	public String getAssignmentToolDefinitionXmlFromOriginalAssignmentsTool(String fromContext, String toContext) {
 
 		List<AssignmentDefinition> assignmentDefs = new ArrayList<AssignmentDefinition>();	
 		Collection<Group> siteGroups = externalLogic.getSiteGroups(fromContext);
 
 		// to identify assignments that act as external maintainers of a gb item,
 		// we need to retrieve all of the gb items in the old site
 		boolean gbDataExists = gradebookLogic.gradebookDataExistsInSite(fromContext);
 		List<GradebookItem> allGbItems = new ArrayList<GradebookItem>();
 		
 		if (gbDataExists) {
 			allGbItems = gradebookLogic.getAllGradebookItems(fromContext);
 		}
 		
 		Iterator<Assignment> origAssignIter = assignmentService.getAssignmentsForContext(fromContext);
 		while (origAssignIter.hasNext()) {
 			Assignment oAssignment = (Assignment)origAssignIter.next();
 			AssignmentContent oContent = oAssignment.getContent();
 			ResourceProperties oProperties = oAssignment.getProperties();
 
 			AssignmentDefinition newAssnDef = new AssignmentDefinition();
 
 			Date openDate = new Date(oAssignment.getOpenTime().getTime());
 			newAssnDef.setOpenDate(openDate);
 
 			if (oAssignment.getCloseTime() != null) {
 				Date closeDate = new Date(oAssignment.getCloseTime().getTime());
 				newAssnDef.setAcceptUntilDate(closeDate);
 			}
 			
 			if (oAssignment.getDueTime() != null) {
 				Date dueDate = new Date(oAssignment.getDueTime().getTime());
 				newAssnDef.setDueDate(dueDate);
 			}
 
 			newAssnDef.setTitle(oAssignment.getTitle());
 			newAssnDef.setDraft(oAssignment.getDraft());
 			newAssnDef.setInstructions(oContent.getInstructions());
 			newAssnDef.setHonorPledge(oContent.getHonorPledge() == Assignment.HONOR_PLEDGE_ENGINEERING);
 			// set submission type
 			if (oContent.getTypeOfSubmission() == Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION) {
 				newAssnDef.setSubmissionType(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH);
 			} else if (oContent.getTypeOfSubmission() == Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION) {
 				newAssnDef.setSubmissionType(AssignmentConstants.SUBMIT_ATTACH_ONLY);
 			} else if (oContent.getTypeOfSubmission() == Assignment.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
 				newAssnDef.setSubmissionType(AssignmentConstants.SUBMIT_NON_ELECTRONIC);
 			} else if (oContent.getTypeOfSubmission() == Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION) {
 				newAssnDef.setSubmissionType(AssignmentConstants.SUBMIT_INLINE_ONLY);
 			} else {
 				// default to text and attachments
 				newAssnDef.setSubmissionType(AssignmentConstants.SUBMIT_INLINE_AND_ATTACH);
 			}
 
 			// retrieve the notification setting
 			String notifProperty = oProperties.getProperty(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_VALUE);
 			if (notifProperty == null) {
 				newAssnDef.setSendSubmissionNotifications(false);
 			} else if (notifProperty.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_DIGEST)) {
 				newAssnDef.setSendSubmissionNotifications(true);
 			} else if (notifProperty.equals(Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_EACH)) {
 			    newAssnDef.setSendSubmissionNotifications(true);
 			} else { //Assignment.ASSIGNMENT_INSTRUCTOR_NOTIFICATIONS_NONE and default
 			    newAssnDef.setSendSubmissionNotifications(false);
 			}
 
 			// is there an announcement?
 			String openDateAnnc = oProperties.getProperty(ResourceProperties.PROP_ASSIGNMENT_OPENDATE_ANNOUNCEMENT_MESSAGE_ID);
 			if (openDateAnnc != null) {
 				newAssnDef.setHasAnnouncement(true);
 			} else {
 				newAssnDef.setHasAnnouncement(false);
 			}
 			
 			// is there an associated Schedule event?
 			String hasEvent = oProperties.getProperty(ResourceProperties.PROP_ASSIGNMENT_DUEDATE_CALENDAR_EVENT_ID);
 			if (hasEvent != null) {
 				newAssnDef.setAddedToSchedule(true);
 			} else {
 				newAssnDef.setAddedToSchedule(false);
 			}
 
 			// the old tool didn't support a resubmission option on the assignment level,
 			// so just allow 1 submission
 			newAssnDef.setNumSubmissionsAllowed(1);
 			
 			// the old tool didn't support the concept of optionally requiring submission
 			// so we will always require
 			newAssnDef.setRequiresSubmission(true);
 
 			// handle attachments
 			List<Reference> oAttachments = oContent.getAttachments();
 			List<String> attachRefList = new ArrayList<String>();
 			if (oAttachments != null && !oAttachments.isEmpty()) {
 				for (Reference attach : oAttachments) {
 					if (attach != null) {
 						attachRefList.add(attach.getId());
 					}
 				}
 			}
 			newAssnDef.setAttachmentReferences(attachRefList);
 
 			// handle any group restrictions
 			List<String> groupTitleList = new ArrayList<String>();
 			if (oAssignment.getAccess() == Assignment.AssignmentAccess.GROUPED &&
 					oAssignment.getGroups() != null && !oAssignment.getGroups().isEmpty()) {
 				if (siteGroups != null) {
 					// iterate through this assignment's groups and find the name
 					for (Group group : siteGroups) {
 						if (group != null) {
 							if (oAssignment.getGroups().contains(group.getReference())) {
 								groupTitleList.add(group.getTitle());
 							}
 						}
 					}
 				}
 			}
 			newAssnDef.setGroupRestrictionGroupTitles(groupTitleList);
 
 			// now let's handle the graded/ungraded stuff
 			if (oContent.getTypeOfGrade() == Assignment.UNGRADED_GRADE_TYPE) {
 				newAssnDef.setGraded(false);
 
 			} else {
 				String grading = oProperties.getProperty(AssignmentService.NEW_ASSIGNMENT_ADD_TO_GRADEBOOK);
 				String associateAssignment = oProperties.getProperty(AssignmentService.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
 				if (grading != null && grading.equals(AssignmentService.GRADEBOOK_INTEGRATION_ASSOCIATE)) {
 					// we need to figure out which gb item this was associated with, so iterate
 					// through the gb items
 					// either this is assignment "added" a gb item as an externally maintained item and
 					// the associateAssignment will match the externalId of the gb item, or it was
 					// associated with an existing gb item, in which case the associatedAssignment
 					// will be the gbItem title
 					if (associateAssignment != null && allGbItems != null) {
 						for (GradebookItem gbItem : allGbItems) {
 							if (gbItem.getExternalId() != null) {
 								if (associateAssignment.equals(gbItem.getExternalId())) {
 									newAssnDef.setGraded(true);
 									newAssnDef.setAssociatedGbItemName(gbItem.getTitle());
 									newAssnDef.setAssociatedGbItemPtsPossible(gbItem.getPointsPossible());
 								}
 							} else {
 								if (associateAssignment.equals(gbItem.getTitle())) {
 									newAssnDef.setGraded(true);
 									newAssnDef.setAssociatedGbItemName(gbItem.getTitle());
 									newAssnDef.setAssociatedGbItemPtsPossible(gbItem.getPointsPossible());
 								}
 							}
 						}
 					}
 				} else {
 					// if the assignment is graded but not associated w/ a gb item,
 					// the only time it will stay graded is if it is graded by points
 					// all other grading options will translate to an ungraded item
 					// in the new tool
 					if (oContent.getTypeOfGrade() == Assignment.SCORE_GRADE_TYPE) {
 						// we will add a gb item for this assignment
 						try {
 							newAssnDef.setGraded(true);
 							newAssnDef.setAssociatedGbItemName(oAssignment.getTitle());
 							newAssnDef.setAssociatedGbItemPtsPossible(new Double(oContent.getMaxGradePointDisplay()));
 						} catch (NumberFormatException nfe) {
 							// set this one as ungraded b/c points possible was invalid
 							newAssnDef.setGraded(false);
 						}
 
 					} else {
 						newAssnDef.setGraded(false);
 					}
 				}
 			}
 
 			assignmentDefs.add(newAssnDef);
 		}
 
 		AssignmentToolDefinition assignmentToolDef = new AssignmentToolDefinition();
 		assignmentToolDef.setAssignments(assignmentDefs);
 
 		return VersionedExternalizable.toXml(assignmentToolDef);
 	}
 
 	private String getNewTitle(String originalTitle, List<String> existingTitles) {
 		int increment = 1;
 		String newTitle = originalTitle + "_" + increment;
 		while (existingTitles.contains(newTitle)) {
 			increment++;
 			newTitle = originalTitle + "_" + increment;
 		}
 
 		return newTitle;
 	}
 	
 	public void cleanToolForImport(String contextId) {
 		if (contextId == null) {
 			throw new IllegalArgumentException("Null contextId passed to cleanAssignments");
 		}
 		
 		List<Assignment2> allAssignments = dao.getAssignmentsWithGroupsAndAttachments(contextId);
 		if (allAssignments != null) {
 			for (Assignment2 assign : allAssignments) {
 				assignmentLogic.deleteAssignment(assign);
 				if (log.isDebugEnabled()) log.debug("Assignment " + assign.getId() + " removed in preparation for import");
 			}
 		}
 	}
 }
