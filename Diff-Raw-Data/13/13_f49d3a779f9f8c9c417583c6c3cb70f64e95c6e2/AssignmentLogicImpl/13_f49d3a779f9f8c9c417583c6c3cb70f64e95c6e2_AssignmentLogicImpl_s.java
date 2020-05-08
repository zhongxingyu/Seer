 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/api/logic/src/java/org/sakaiproject/assignment2/dao/AssignmentDao.java $
  * $Id: AssignmentDao.java 12544 2006-05-03 15:06:26Z wagnermr@iupui.edu $
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
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentAttachment;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.logic.AssignmentLogic;
 import org.sakaiproject.assignment2.logic.ExternalAnnouncementLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.ConflictingAssignmentNameException;
 import org.sakaiproject.genericdao.api.finders.ByPropsFinder;
 import org.sakaiproject.assignment2.logic.utils.ComparatorsUtils;
 
 
 /**
  * This is the interface for the Assignment object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentLogicImpl implements AssignmentLogic{
 	
 	private static Log log = LogFactory.getLog(AssignmentLogicImpl.class);
 	
 	private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     private ExternalGradebookLogic gradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
     
     private ExternalAnnouncementLogic announcementLogic;
     public void setExternalAnnouncementLogic(ExternalAnnouncementLogic announcementLogic) {
         this.announcementLogic = announcementLogic;
     }
     
     private AssignmentDao dao;
     public void setDao(AssignmentDao dao) {
         this.dao = dao;
     }
     
 	public void init(){
 		log.debug("init");
 	}
 	/**
 	 * 
 	 * @param assignmentId
 	 * @return Returns the Assignment based on its assignmentId
 	 */
 	public Assignment2 getAssignmentById(Long assignmentId)
 	{
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentById");
 		}
 		
 		return (Assignment2) dao.findById(Assignment2.class, assignmentId);
     }
 	
 	public Assignment2 getAssignmentByIdWithAssociatedData(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithAssociatedData");
 		}
 		// first, retrieve Assignment2 object
 		Assignment2 assign = (Assignment2) dao.getAssignmentByIdWithGroupsAndAttachments(assignmentId);
 		
 		if (assign != null) {
 			// populate any non-persisted fields that are applicable to this view
 			boolean restrictedToGroups = assign.getAssignmentGroupSet() != null &&
 				!assign.getAssignmentGroupSet().isEmpty();
 			assign.setRestrictedToGroups(restrictedToGroups);
 		}
 		
 		return assign;
 	}
 	
 	public Assignment2 getAssignmentByIdWithGroups(Long assignmentId) {
 		if (assignmentId == null) {
 			throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithGroups");
 		}
 		
 		return (Assignment2) dao.getAssignmentByIdWithGroups(assignmentId);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.sakaiproject.assignment2.logic.AssignmentLogic#saveAssignment(org.sakaiproject.assignment2.model.Assignment2)
 	 */
 	public void saveAssignment(Assignment2 assignment) throws SecurityException, ConflictingAssignmentNameException
 	{
 		if (assignment == null) {
 			throw new IllegalArgumentException("Null assignment passed to saveAssignment");
 		}
 		
 		if (!gradebookLogic.isCurrentUserAbleToEdit(externalLogic.getCurrentContextId())) {
 			throw new SecurityException("Current user may not save assignment " + assignment.getTitle()
                     + " because they do not have edit permission");
 		}
 		
 		boolean isNewAssignment = true;
 		Assignment2 existingAssignment = null;
 		
 		// determine if this is a new assignment
 		if (assignment.getAssignmentId() != null) {
 			// check to see if assignment exists
 			existingAssignment = (Assignment2)dao.getAssignmentByIdWithGroupsAndAttachments(assignment.getAssignmentId());	
 			if (existingAssignment != null) {
 				isNewAssignment = false;
 			}
 		}
 		
 		if (isNewAssignment) {
         	// check to ensure it is not a duplicate title
         	if (assignmentNameExists(assignment.getTitle())) {
         		throw new ConflictingAssignmentNameException("An assignment with the title " + assignment.getTitle() + " already exists");
         	}
         	// identify the next sort index to be used
         	Integer highestIndex = dao.getHighestSortIndexInSite(externalLogic.getCurrentContextId());
         	if (highestIndex != null) {
         		assignment.setSortIndex(highestIndex + 1);
         	} else {
         		assignment.setSortIndex(0);
         	}
         	
         	// the attachment and group recs do not have assignmentId data yet,
         	// so we need to handle it after we do the creation
         	Set<AssignmentAttachment> attachSet = assignment.getAttachmentSet();
         	Set<AssignmentGroup> assignGroupSet = assignment.getAssignmentGroupSet();
         	
         	assignment.setAttachmentSet(new HashSet());
         	assignment.setAssignmentGroupSet(new HashSet());
         	
         	dao.create(assignment);
             log.debug("Created assignment: " + assignment.getTitle());
             
             // now that we have an assignmentId, we can add the associated groups and attachments
             assignment.setAttachmentSet(attachSet);
             updateAttachments(existingAssignment, assignment);     
             
             assignment.setAssignmentGroupSet(assignGroupSet);
             updateAssignmentGroups(existingAssignment, assignment);
               
 		} else {
 			if (!assignment.getTitle().equals(existingAssignment.getTitle())) {
 				// check to see if this new title already exists
 				if (assignmentNameExists(assignment.getTitle())) {
 	        		throw new ConflictingAssignmentNameException("An assignment with the title " + assignment.getTitle() + " already exists");
 	        	}
 			}
 			
 			updateAttachments(existingAssignment, assignment);
 			updateAssignmentGroups(existingAssignment, assignment);
 			
         	dao.update(assignment);
             log.debug("Updated assignment: " + assignment.getTitle() + "with id: " + assignment.getAssignmentId());
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.sakaiproject.assignment2.logic.AssignmentLogic#deleteAssignment(org.sakaiproject.assignment2.model.Assignment2)
 	 */
 	public void deleteAssignment(Assignment2 assignment) throws SecurityException, IllegalArgumentException
 	{
 		if (assignment == null) {
 			throw new IllegalArgumentException("Null assignment passed to deleteAssignment");
 		}
 		
 		if (!gradebookLogic.isCurrentUserAbleToEdit(externalLogic.getCurrentContextId())) {
 			throw new SecurityException("Current user may not delete assignment " + assignment.getTitle()
                     + " because they do not have edit permission");
 		}
 		
     	assignment.setRemoved(true);
     	dao.update(assignment);
         log.debug("Deleted assignment: " + assignment.getTitle() + " with id " + assignment.getAssignmentId());
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.sakaiproject.assignment2.logic.AssignmentLogic#getViewableAssignments(String)
 	 */
 	public List<Assignment2> getViewableAssignments()
 	{
 		List<Assignment2> viewableAssignments = new ArrayList();
 		String contextId = externalLogic.getCurrentContextId();
 
 		Set<Assignment2> allAssignments = dao.getAssignmentsWithGroupsAndAttachments(contextId);
 
 		if (allAssignments != null && !allAssignments.isEmpty()) {
 
 			List<Assignment2> gradedAssignments = new ArrayList();
 
 			// users may view ungraded items if:
 			//  a) it is not restricted to groups
 			//  b) it is restricted, but user has grade all perm
 			//  c) it is restricted, but user is a member of restricted group
 			//  d) it is not draft or user has edit perm
 
 			List<String> userGroupIds = externalLogic.getCurrentUserGroupIdList();	
 
 			for (Iterator asnIter = allAssignments.iterator(); asnIter.hasNext();) {
 				Assignment2 assignment = (Assignment2) asnIter.next();
 				
 				boolean restrictedToGroups = assignment.getAssignmentGroupSet() != null
 					&& !assignment.getAssignmentGroupSet().isEmpty();
 
 				if (!assignment.isDraft() || gradebookLogic.isCurrentUserAbleToEdit(contextId)) {
 					if (assignment.isUngraded()) {
						if (restrictedToGroups || gradebookLogic.isCurrentUserAbleToGradeAll(contextId)) {
 							viewableAssignments.add(assignment);
 						} else if (userGroupIds != null && restrictedToGroups) {
 							// we need to filter out the section-based assignments if not authorized for all
 							// check to see if user is a member of an associated section
 							Set<AssignmentGroup> groupRestrictions = assignment.getAssignmentGroupSet();
 							if (groupRestrictions != null) {
 								boolean allowedToView = false;
 								for (Iterator groupIter = groupRestrictions.iterator(); groupIter.hasNext();) {
 									AssignmentGroup group = (AssignmentGroup) groupIter.next();
 									if (group != null && userGroupIds.contains(group.getGroupId())) {
 										allowedToView = true;
 									}
 								}
 								
 								if (allowedToView) {
 									viewableAssignments.add(assignment);
 								}
 							}	
 						} 
 
 					} else {
 						gradedAssignments.add(assignment);
 					}
 				}
 			}
 
 			if (gradedAssignments != null && !gradedAssignments.isEmpty()) {
 				// now, we need to filter the assignments that are associated with
 				// the gradebook according to grader permissions and populate the
 				// gradebook data
 				List viewableGbAssignments = gradebookLogic.getViewableAssignmentsWithGbData(gradedAssignments, externalLogic.getCurrentContextId());
 				if (viewableGbAssignments != null) {
 
 					for (Iterator gradedIter = viewableGbAssignments.iterator(); gradedIter.hasNext();) {
 						Assignment2 assignment = (Assignment2) gradedIter.next();
 						
 						boolean restrictedToGroups = assignment.getAssignmentGroupSet() != null
 						&& !assignment.getAssignmentGroupSet().isEmpty();
 						
 						// if user is a "student" in terms of the gb, we need to filter the view
 						// by AssignmentGroup restrictions.
 						if (restrictedToGroups && gradebookLogic.isCurrentUserAStudentInGb(contextId)) {
 							Set<AssignmentGroup> groupRestrictions = assignment.getAssignmentGroupSet();
 							if (groupRestrictions != null) {
 								boolean allowedToView = false;
 								for (Iterator groupIter = groupRestrictions.iterator(); groupIter.hasNext();) {
 									AssignmentGroup group = (AssignmentGroup) groupIter.next();
 									if (group != null && userGroupIds.contains(group.getGroupId())) {
 										allowedToView = true;
 									}
 								}
 								
 								if (allowedToView) {
 									viewableAssignments.add(assignment);
 								}
 							}	
 						} else {
 							viewableAssignments.add(assignment);
 						}
 					}
 				}
 			}
 		}
 		
 		return viewableAssignments;
 	}
 	
 	public List<Assignment2> getViewableAssignments(String userId, String sortProperty, boolean ascending, int start, int limit) {
 		if (!ascending) {
             sortProperty += ByPropsFinder.DESC;
         }
 
 		List<Assignment2> assignments = 
 			dao.findByProperties(Assignment2.class, new String[] {"contextId", "removed"}, new Object[] {externalLogic.getCurrentContextId(), Boolean.FALSE},
 					new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS }, new String[] { sortProperty }, start, limit);
 		
 		return assignments;
 	}
 	
 	//TODO this needs to consider permissions!
 	public int getTotalCountViewableAssignments(String userId) {
 
 		int result = dao.countByProperties(Assignment2.class, new String[] {"contextId", "removed"}, 
 				new Object[] {externalLogic.getCurrentContextId(), Boolean.FALSE});
 		return result;
 	}
 	
 	public void setAssignmentSortIndexes(Long[] assignmentIds)
 	{
 		if (assignmentIds != null) {
 			//Assume array of longs is in correct order now
 			//so that the index of the array is the new 
 			//sort index
 			for (int i=0; i < assignmentIds.length; i++){
 				//get Assignment
 	    		Assignment2 assignment = getAssignmentById(assignmentIds[i]);
 	    		if (assignment != null){
 	    			//check if we need to update
 	    			if (assignment.getSortIndex() != i){
 	    				//update and save
 		    			assignment.setSortIndex(i);
 		    			dao.save(assignment);
 	    			}
 	    		}
 	    	}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param assignmentName
 	 * @return true if there is an existing assignment (removed = false) with
 	 * the given title
 	 */
 	private boolean assignmentNameExists(String assignmentName) {
 		int count = dao.countByProperties(Assignment2.class, 
 	               new String[] {"contextId", "title", "removed", "draft"}, 
 	               new Object[] {externalLogic.getCurrentContextId(), assignmentName, Boolean.FALSE, Boolean.FALSE});
 		
 		return count > 0;
 	}
 	
 
 	/**
 	 * 
 	 * @param assignment
 	 * @return the number of submissions to date for the given assignment. this
 	 * will take permissions into account to return the number that the current
 	 * user is authorized to view 
 	 */
 	public int getTotalNumSubmissionsForAssignment(Assignment2 assignment) {
 		if (assignment == null) {
 			throw new IllegalArgumentException("null assignment passed to getTotalNumSubmissionsForAssignment");
 		}
 		return 0;
 	}
 	
 	/**
 	 * 
 	 * @param assignment
 	 * @return the number of ungraded submissions for the given assignment.  this
 	 * will take permissions into account to return the number that the current
 	 * user is authorized to view 
 	 */
 	public int getNumUngradedSubmissionsForAssignment(Assignment2 assignment) {
 		if (assignment == null) {
 			throw new IllegalArgumentException("null assignment passed to getNumUngradedSubmissionsForAssignment");
 		}
 		return 0;
 	}
 	
 	public int getStatusForAssignment(Assignment2 assignment) {
 		if (assignment == null){
 			throw new IllegalArgumentException("Null assignment passed to check status");
 		}
 		if (assignment.isDraft())
 			return AssignmentConstants.STATUS_DRAFT;
 		
 		Date currDate = new Date();
 		
 		if (currDate.before(assignment.getOpenTime()))
 			return AssignmentConstants.STATUS_NOT_OPEN;
 		
 		if (currDate.after(assignment.getOpenTime()) && currDate.before(assignment.getAcceptUntilTime())) {
 			if (assignment.isUngraded()) {
 				if (currDate.after(assignment.getDueDateForUngraded()))
 					return AssignmentConstants.STATUS_DUE;
 			}
 			else if (assignment.getDueDate() != null) {
 				if (currDate.after(assignment.getDueDate()))
 					return AssignmentConstants.STATUS_DUE;				
 			}
 			
 			return AssignmentConstants.STATUS_OPEN;
 		}
 		
 		return AssignmentConstants.STATUS_CLOSED;
 		
 	}
 	
 	/**
 	 * 
 	 * @param groups
 	 * @return a comma-delimited String representation of the given list of
 	 * groups/section. will delete groups that have been deleted at the site
 	 * level
 	 */
 	public String getListOfGroupRestrictionsAsString(List<AssignmentGroup> restrictedGroups, Map<String, String> siteGroupIdNameMap) {
 		if (restrictedGroups == null || restrictedGroups.isEmpty())
 			return null;
 	
 		StringBuilder sb = new StringBuilder();
 		
 		for (int i=0; i<restrictedGroups.size(); i++) {
 			
 			AssignmentGroup group = (AssignmentGroup) restrictedGroups.get(i);
 			if (group != null) {
 				if (i != 0) {
 					sb.append(", ");
 				}
 				
 				if (siteGroupIdNameMap.containsKey(group.getGroupId())) {
 					String groupName = (String)siteGroupIdNameMap.get(group.getGroupId());
 					sb.append(groupName);
 				} else {
 					// this group has been deleted from the site, so we need
 					// to delete this AssignmentGroup object
 					log.info("AssignmentGroup associated with group id " 
 							+ group.getAssignmentGroupId() + "and assignment " 
 							+ group.getAssignment().getAssignmentId()
 							+ " deleted b/c associated site group was deleted");
 					dao.delete(AssignmentGroup.class, group.getAssignmentGroupId());
 				}
 			}
 		}
 		
 		return sb.toString();
 	}
 	
 	/**
 	 * add or delete AssignmentAttachments by comparing the existingAssignment
 	 * from db to the new version
 	 * @param existingAssignment
 	 * @param newAssignment
 	 * @throws IllegalArgumentException if the existingAssignment is null or newAssignment
 	 * is not already persisted in db
 	 */
 	private void updateAttachments(Assignment2 existingAssignment, Assignment2 newAssignment) {
 		if (newAssignment == null) {
 			throw new IllegalArgumentException("Null newAssignment passed to updateAttachments");
 		}
 		
 		if (newAssignment.getAssignmentId() == null) {
 			throw new IllegalArgumentException("newAssignment passed to updateAttachments is not currently defined in db");
 		}
 		
 		Set<AssignmentAttachment> revisedAttachSet = new HashSet();
 		
 		if (newAssignment.getAttachmentSet() != null && !newAssignment.getAttachmentSet().isEmpty()) {
         	for (Iterator attachIter = newAssignment.getAttachmentSet().iterator(); attachIter.hasNext();) {
         		AssignmentAttachment attach = (AssignmentAttachment) attachIter.next();
         		if (attach != null && attach.getAssignAttachId() == null) {
         			// this is a new attachment and needs to be created
         			attach.setAssignment(newAssignment);
         			dao.save(attach);
         			log.debug("New attachment created: " + attach.getAttachmentReference() + "with attach id " + attach.getAssignAttachId());
         			revisedAttachSet.add(attach);
         		}
         	}
         }
 		
 		// now we need to handle the case in which existing attachments were removed
 		if (existingAssignment != null) {
 			for (Iterator existingIter = existingAssignment.getAttachmentSet().iterator(); existingIter.hasNext();) {
 				AssignmentAttachment attach = (AssignmentAttachment) existingIter.next();
 				if (attach != null) {
 					if (newAssignment.getAttachmentSet() == null ||
 							!newAssignment.getAttachmentSet().contains(attach)) {
 						// we need to delete this attachment
 						dao.delete(attach);
 						log.debug("Attachment deleted with id: " + attach.getAssignAttachId());
 					} else if (newAssignment.getAttachmentSet() != null &&
 								newAssignment.getAttachmentSet().contains(attach)) {
 						revisedAttachSet.add(attach);
 					}
 				}
 			}
 		}
 		
 		newAssignment.setAttachmentSet(revisedAttachSet);
 	}
 	
 	/**
 	 * This method will add or delete AssignmentGroups by comparing the existingAssignment
 	 * from db to the new version
 	 * @param existingAssignment
 	 * @param newAssignment
 	 * @throws IllegalArgumentException if the existingAssignment is null or newAssignment
 	 * is not already persisted in db
 	 */
 	private void updateAssignmentGroups(Assignment2 existingAssignment, Assignment2 newAssignment) {
 		if (newAssignment == null) {
 			throw new IllegalArgumentException("Null newAssignment passed to updateAssignmentGroups");
 		}
 		
 		if (newAssignment.getAssignmentId() == null) {
 			throw new IllegalArgumentException("newAssignment passed to updateAssignmentGroups is not currently defined in db");
 		}
 		
 		Set<AssignmentGroup> revisedGroupSet = new HashSet();
 		
 		if (newAssignment.getAssignmentGroupSet() != null && !newAssignment.getAssignmentGroupSet().isEmpty()) {
         	for (Iterator groupIter = newAssignment.getAssignmentGroupSet().iterator(); groupIter.hasNext();) {
         		AssignmentGroup group = (AssignmentGroup) groupIter.next();
         		if (group != null && group.getAssignmentGroupId() == null) {
         			// this is a new AssignmentGroup and needs to be created
         			group.setAssignment(newAssignment);
         			dao.save(group);
         			log.debug("New AssignmentGroup created: " + group.getAssignmentGroupId() + "with id " + group.getAssignmentGroupId());
         			revisedGroupSet.add(group);
         		}
         	}
         }
 		
 		// now we need to handle the case in which existing AssignmentGroups were removed
 		if (existingAssignment != null) {
 			for (Iterator existingIter = existingAssignment.getAssignmentGroupSet().iterator(); existingIter.hasNext();) {
 				AssignmentGroup group = (AssignmentGroup) existingIter.next();
 				if (group != null) {
 					if (newAssignment.getAssignmentGroupSet() == null ||
 							!newAssignment.getAssignmentGroupSet().contains(group)) {
 						// we need to delete this AssignmentGroup
 						dao.delete(group);
 						log.debug("AssignmentGroup deleted with id: " + group.getAssignmentGroupId());
 					}
 				} else if (newAssignment.getAssignmentGroupSet() != null &&
 						newAssignment.getAssignmentGroupSet().contains(group)) {
 					revisedGroupSet.add(group);
 				}
 			}
 		} 
 		
 		newAssignment.setAssignmentGroupSet(revisedGroupSet);
 	}
 	
 	public void sortAssignments(List<Assignment2> assignmentList, String sortBy, boolean ascending) {
 		Comparator<Assignment2> comp;
 		if(AssignmentLogic.SORT_BY_TITLE.equals(sortBy)) {
 			comp = new ComparatorsUtils.Assignment2TitleComparator();
 		} else if(AssignmentLogic.SORT_BY_DUE.equals(sortBy)) {
 			comp = new ComparatorsUtils.Assignment2DueDateComparator();
 		} else if(AssignmentLogic.SORT_BY_FOR.equals(sortBy)) {
 			comp = new ComparatorsUtils.Assignment2ForComparator();
 		}else if(AssignmentLogic.SORT_BY_NUM_UNGRADED.equals(sortBy)){
 			comp = new ComparatorsUtils.Assignment2NumUngradedComparator();
 		} else if(AssignmentLogic.SORT_BY_OPEN.equals(sortBy)){
 			comp = new ComparatorsUtils.Assignment2OpenDateComparator();
 		} else if(AssignmentLogic.SORT_BY_STATUS.equals(sortBy)){
 			comp = new ComparatorsUtils.Assignment2StatusComparator();
 		} else {
 			comp = new ComparatorsUtils.Assignment2SortIndexComparator();
 		}
 
 		Collections.sort(assignmentList, comp);
 		if(!ascending) {
 			Collections.reverse(assignmentList);
 		}
 	}
 	
 }
