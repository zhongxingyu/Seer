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
 
 package org.sakaiproject.assignment2.logic;
 
 import java.util.List;
 import java.util.Map;
 
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
import org.sakaiproject.assignment2.exception.ConflictingAssignmentNameException;
 
 
 /**
  * This is the interface for the Assignment object
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public interface AssignmentLogic {
 	
 	/**
 	 * 
 	 * @param assignmentId
 	 * @return Returns the Assignment based on its assignmentId
 	 */
 	public Assignment2 getAssignmentById(Long assignmentId);
 	
 	/**
 	 * Create or update an assignment
 	 * @param assignment
 	 * the assignment to create or update
 	 * @throws SecurityException -
 	 * user must have "edit" permission to add or update an assignment
 	 * @throws ConflictingAssignmentNameException -
 	 * if it is a new assignment and the title already exists
 	 */
	public void saveAssignment(Assignment2 assignment) throws ConflictingAssignmentNameException;
 	
 	/**
 	 * Delete an Assignment 
 	 * note: no assignments are actually deleted; the "removed" property
 	 * will be set to true
 	 * @param assignment
 	 * 			the Assignment to delete
 	 * @throws SecurityException -
 	 * user must have "delete" permission
 	 */	
 	public void deleteAssignment(Assignment2 assignment);
 	
 	/**
 	 * Returns list of Assignment objects that the given user has permission
 	 * to view or grade. Assignments that the user does not have permission 
 	 * to view or grade will not be returned.
 	 * @return
 	 */
 	public List<Assignment2> getViewableAssignments();
 	
 	public List<Assignment2> getViewableAssignments(String userId, String sortProperty, boolean ascending, int start, int limit);
 	
 	public int getTotalCountViewableAssignments(String userId);
 	
 	public void setAssignmentSortIndexes(Long[] assignmentIds);
 	
 	/**
 	 * retrieve the Assignment2 object with the given id and populate associated
 	 * data (ie attachments, groups, any gradebook data). Does not include
 	 * student submission information
 	 * @param assignmentId
 	 * @return
 	 */
 	public Assignment2 getAssignmentByIdWithAssociatedData(Long assignmentId);
 	
 	/**
 	 * retrieve the Assignment2 object with the given id and populate the
 	 * associated AssignmentGroups
 	 * @param assignmentId
 	 * @return
 	 */
 	public Assignment2 getAssignmentByIdWithGroups(Long assignmentId);
 	
 	/**
 	 * 
 	 * @param groups
 	 * @return a comma-delimited String representation of the given list of
 	 * groups/section. will delete groups that have been deleted at the site
 	 * level
 	 */
 	public String getListOfGroupRestrictionsAsString(List<AssignmentGroup> restrictedGroups, Map<String, String> siteGroupIdNameMap);
 
 	/**
 	 * Uses the open, due, and accept until dates to determine the current status
 	 * of the given assignment
 	 * @param assignment
 	 * @return an Integer constant equivalent to the assignment's status
 	 */
 	public Integer getStatusForAssignment(Assignment2 assignment);
 }
