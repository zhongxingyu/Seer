 /**********************************************************************************
  * $URL:https://source.sakaiproject.org/contrib/assignment2/trunk/impl/logic/src/java/org/sakaiproject/assignment2/logic/impl/AssignmentPermissionLogicImpl.java $
  * $Id:AssignmentPermissionLogicImpl.java 48274 2008-04-23 20:07:00Z wagnermr@iupui.edu $
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
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.SubmissionNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.site.api.Group;
 
 /**
  * This is the implementation for logic to answer common permissions questions
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentPermissionLogicImpl implements AssignmentPermissionLogic {
 
     private static Log log = LogFactory.getLog(AssignmentPermissionLogicImpl.class);
 
     public void init() {
     	if (log.isDebugEnabled()) log.debug("init");
     }
     
     private AssignmentDao dao;
     public void setDao(AssignmentDao dao) {
         this.dao = dao;
     }
     
 	private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
     
     private ExternalGradebookLogic gradebookLogic;
     public void setExternalGradebookLogic(ExternalGradebookLogic gradebookLogic) {
         this.gradebookLogic = gradebookLogic;
     }
     
     public boolean isCurrentUserAbleToEditAssignments(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("null contextId passed to isCurrentUserAbleToEditAssignments");
     	}
     	return gradebookLogic.isCurrentUserAbleToEdit(contextId);
     }
     
     public boolean isUserAbleToViewStudentSubmissionForAssignment(String studentId, Long assignmentId) {
     	if (studentId == null || assignmentId == null) {
     		throw new IllegalArgumentException("Null studentId or assignmentId passed to isUserAbleToViewStudentSubmissionForAssignment");
     	}
     	
     	boolean viewable = false;
     	
     	Assignment2 assignment = (Assignment2)dao.findById(Assignment2.class, assignmentId);
     	if (assignment == null) {
     		throw new AssignmentNotFoundException("No assignment found with id: " + assignmentId + " found in isUserAbleToViewStudentSubmissionForAssignment");
     	}
     	
     	String currUserId = externalLogic.getCurrentUserId();
 
     	// double check to see if this is a removed assignment. if so, only
     	// the student may view it
     	if (assignment.isRemoved()) {
     	    if (currUserId.equals(studentId)) {
     	        viewable = true;
     	    }
     	} else if (currUserId.equals(studentId)) {
     	    viewable = true;
     	} else if (gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
     	    viewable = true;
     	} else {
     	    if (!assignment.isGraded()) {
     	        viewable = isUserAbleToViewSubmissionForUngradedAssignment(studentId, assignment);
     	    } else {
     	        Long gbItemId = assignment.getGradebookItemId();
     	        if (gbItemId != null) {
     	            String function = gradebookLogic.getGradeViewPermissionForCurrentUserForStudentForItem(assignment.getContextId(), 
     	                    studentId, gbItemId);
     	            if (function != null && (function.equals(AssignmentConstants.GRADE) ||
     	                    function.equals(AssignmentConstants.VIEW))) {
     	                viewable = true;
     	            }
     			}
     		}
     	}
     	
     	return viewable;	
     }
     
     public boolean isUserAbleToProvideFeedbackForStudentForAssignment(String studentId, Assignment2 assignment) {
     	if (studentId == null || assignment == null) {
     		throw new IllegalArgumentException("null parameter passed to isUserAbleToProvideFeedbackForSubmission");
     	}
     	
     	boolean allowed = false;
     	
     	if (assignment != null) {
     		if (!assignment.isGraded()) {
     			allowed = isUserAbleToViewSubmissionForUngradedAssignment(studentId, assignment);
     		} else {
     			if (assignment.getGradebookItemId() != null) {
     				allowed = gradebookLogic.isCurrentUserAbleToGradeStudentForItem(externalLogic.getCurrentContextId(), 
     						studentId, assignment.getGradebookItemId());
     			}
     		}
     	}
     	
     	return allowed;
     }
     
     public boolean isUserAbleToProvideFeedbackForSubmission(Long submissionId) {
     	if (submissionId == null) {
     		throw new IllegalArgumentException("Null submissionId passed to isUserAbleToProvideFeedbackForSubmission");
     	}
     	
     	AssignmentSubmission submission = (AssignmentSubmission)dao.findById(AssignmentSubmission.class, submissionId);
     	if (submission == null) {
     		throw new SubmissionNotFoundException("No submission exists with id: " + submissionId);
     	}
     	
     	return isUserAbleToProvideFeedbackForStudentForAssignment(submission.getUserId(), submission.getAssignment());
     }
     
     public boolean isUserAbleToViewSubmissionForUngradedAssignment(String studentId, Assignment2 assignment) {
     	if (studentId == null || assignment == null) {
     		throw new IllegalArgumentException("null studentId or assignment passed to isUserAbleToViewSubmissionForUngradedAssignment");
     	}
     	
     	boolean viewable = false;
     	if (gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
     		viewable = true;
     	} else if (gradebookLogic.isCurrentUserAbleToGrade(assignment.getContextId())) {
     		String currContextId = externalLogic.getCurrentContextId();
     		List<String> currentUserMemberships = externalLogic.getUserMembershipGroupIdList(externalLogic.getCurrentUserId(), currContextId);
     		List<String> studentMemberships = externalLogic.getUserMembershipGroupIdList(studentId, currContextId);
     		if (userMembershipsOverlap(currentUserMemberships, studentMemberships)) {
     			viewable = true;
     		}
     	}
     	
     	return viewable;
     }
     
     public boolean isUserAbleToViewUngradedAssignment(Assignment2 assignment, Collection<String> groupMembershipIds) {
     	if (assignment == null) {
     		throw new IllegalArgumentException("Null assignment passed to isUserAbleToViewUngradedAssignment");
     	}
     	
     	if (assignment.isGraded()) {
     		throw new IllegalArgumentException("A graded assignment was passed to isUserAbleToViewUngradedAssignment");
     	}
     	
     	boolean viewable = false;
     	
     	// user can grade all
     	if (gradebookLogic.isCurrentUserAbleToGradeAll(assignment.getContextId())) {
     		viewable = true;
     		
     	// user has some grading privileges or they are a student and assign is open
     	} else if (gradebookLogic.isCurrentUserAbleToGrade(assignment.getContextId()) || 
     			(gradebookLogic.isCurrentUserAStudentInGb(assignment.getContextId()) && 
     					assignment.getOpenDate().before(new Date()))) {
     		
     		if (assignment.getAssignmentGroupSet() == null || assignment.getAssignmentGroupSet().isEmpty()) {
     			viewable = true;
     		} else {
     			// the user must be a member of a restricted group to view assignment
     			viewable = isUserAMemberOfARestrictedGroup(groupMembershipIds, assignment.getAssignmentGroupSet());
     		}
     	}
     	
     	return viewable;
     }
     
     public boolean isUserAbleToViewGradedAssignment(Assignment2 assignment, Collection<String> groupMembershipIds) {
     	if (assignment == null) {
     		throw new IllegalArgumentException("Null assignment passed to isUserAbleToViewGradedAssignment");
     	}
     	
     	if (!assignment.isGraded()) {
     		throw new IllegalArgumentException("An ungraded assignment was passed to isUserAbleToViewGradedAssignment");
     	}
     	
     	boolean viewable = false;
     	boolean userIsStudent = gradebookLogic.isCurrentUserAStudentInGb(assignment.getContextId());
     	boolean assignIsOpen = assignment.getOpenDate().before(new Date());
 
     	if (!userIsStudent) {
     	    // if the user is not a student, then we just check to see if the
     	    // current user has access to the associated gb item
     	    viewable = gradebookLogic.isCurrentUserAbleToViewGradebookItem(assignment.getContextId(), assignment.getGradebookItemId());
     	} else {
     	    // students may only view open assignments
     	    // students are allowed to view graded assignments regardless of
     	    // whether the gradebook item has been released. we just don't give
     	    // them access to the grade info
     	    if (assignIsOpen) {
     	        // check the group restrictions
     	        if (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()) {
     	            viewable = isUserAMemberOfARestrictedGroup(groupMembershipIds, assignment.getAssignmentGroupSet());
     	        } else {
     	            viewable = true;
     	        }
     	    }
     	}
 
     	return viewable;
     }
     
     public boolean isUserAMemberOfARestrictedGroup(Collection<String> groupMembershipIds, Collection<AssignmentGroup> assignmentGroupSet) {    	
     	if (assignmentGroupSet != null && !assignmentGroupSet.isEmpty()) {
         	if (groupMembershipIds != null) {
 	        	for (AssignmentGroup aGroup : assignmentGroupSet) {
 	        		if (aGroup != null) {
 	        			if (groupMembershipIds.contains(aGroup.getGroupId())) {
 	        				return true;
 	        			}
 	        		}
 	        	}
         	}
     	}
     	
     	return false;
     }
     
 	public boolean isUserAbleToAccessInstructorView(String contextId) {
 		if (contextId == null) {
 			throw new IllegalArgumentException("null contextId passed to isUserAbleToAccessInstructorView");
 		}
 		
 		boolean instructorView = false;
 		
 		if (gradebookLogic.isCurrentUserAbleToEdit(contextId) ||
 				gradebookLogic.isCurrentUserAbleToGrade(contextId)) {
 			instructorView = true;
 		}
 		
 		return instructorView;
 	}
 	
 	public boolean isUserAbleToMakeSubmissionForAssignment(String contextId, Assignment2 assignment) {
 		if (contextId == null || assignment == null) {
 			throw new IllegalArgumentException("null contextId or assignment passed to isUserAbleToMakeSubmission");
 		}
 		
 		if (assignment.getId() == null) {
 			throw new IllegalArgumentException("null data in not-null fields for assignment passed to isUserAbleToMakeSubmission");
 		} 
 		
 		// TODO - how do we handle certain roles that shouldn't be able to submit?
 		// ie guests? they don't have a section flag on their role
 		boolean userAbleToSubmit = false;
 	
 		if (!assignment.isGraded()) {
 			// TODO right now, we don't have any checks for ungraded assignments...
 			userAbleToSubmit = true;
 		} else {
 			// we must obey the gradebook permissions
 			if (gradebookLogic.isCurrentUserAStudentInGb(contextId)) {
 				userAbleToSubmit = true;
 			}
 		}
 		
 		// check to make sure any group restrictions have been upheld
 		if (userAbleToSubmit) {
 			List<AssignmentGroup> assignGroupRestrictions = 
 				dao.findByProperties(AssignmentGroup.class, new String[] {"assignment"}, new Object[] {assignment});
 			
 			List<String> groupMembershipIds = externalLogic.getUserMembershipGroupIdList(externalLogic.getCurrentUserId(), contextId);
 			if (assignGroupRestrictions != null && !assignGroupRestrictions.isEmpty()) {
 				if (!isUserAMemberOfARestrictedGroup(groupMembershipIds, assignGroupRestrictions)) {
 					userAbleToSubmit = false;
 				}
 			}
 		}
 		
 		return userAbleToSubmit;
 	}
 	
 	public Map<Assignment2, List<String>> getViewableStudentsForUserForAssignments(String userId, List<Assignment2> assignmentList) {
 	    if (userId == null) {
 	        throw new IllegalArgumentException("Null userId passed to getViewableStudentsForUserForAssignments");
 	    }
 	    
 	    Map<Assignment2, List<String>> assignToViewableStudentsMap = new HashMap<Assignment2, List<String>>();
 	    if (assignmentList != null) {
 	        assignToViewableStudentsMap = getAvailableStudentsForUserForAssignments(userId, assignmentList, AssignmentConstants.VIEW);
 	    }
 	    
 	    return assignToViewableStudentsMap;
 	}
 	
 	public List<String> getViewableStudentsForUserForItem(String userId, Assignment2 assignment) {
 		if (assignment == null || userId == null) {
 			throw new IllegalArgumentException("null assignment or userId passed to " +
 					"getViewableStudentsForUserForItem. userId: " + userId + " assignment: " + assignment);
 		}
 
 		List<Assignment2> assignmentList = new ArrayList<Assignment2>();
 		assignmentList.add(assignment);
 
 		Map<Assignment2, List<String>> assignIdAvailStudentsMap = getAvailableStudentsForUserForAssignments(userId, assignmentList, AssignmentConstants.VIEW);
 
 		List<String> availStudents = assignIdAvailStudentsMap.get(assignment);
 
 		return availStudents;
 	}
 	
 	public List<String> getGradableStudentsForUserForItem(String userId, Assignment2 assignment) {
 		if (assignment == null || userId == null) {
 			throw new IllegalArgumentException("null assignment or userId passed " +
 					"to getGradableStudentsForUserForItem. " +
 					"userId: " + userId + " assignment: " + assignment);
 		}
 		
 		List<Assignment2> assignmentList = new ArrayList<Assignment2>();
 		assignmentList.add(assignment);
 		
 		Map<Assignment2, List<String>> assignIdAvailStudentsMap = getAvailableStudentsForUserForAssignments(userId, assignmentList, AssignmentConstants.GRADE);
 		List<String> availStudents = assignIdAvailStudentsMap.get(assignment);
 		
 		return availStudents;
 	}
 	
 	/**
 	 * 
 	 * @param userId
 	 * @param assignmentList
 	 * @param gradeOrView - indicate whether you want all the students a user may view or all that the user may grade.
 	 * use {@link AssignmentConstants.GRADE} or {@link AssignmentConstants.VIEW}
 	 * @return a map of the Assignment2 object to a list of student uids that
 	 * the given user is allowed to view or grade (based upon the gradeOrView param).
 	 * if the user is not allowed to view any students for an assignment, the assignment
 	 * will be returned with an empty list
 	 */
 	private Map<Assignment2, List<String>> getAvailableStudentsForUserForAssignments(String userId, List<Assignment2> assignmentList, String gradeOrView) {
 	    if (userId == null) {
 	        throw new IllegalArgumentException("null userId passed to getAvailableStudentsForUserForItem. userId: " + userId);
 	    }
 
 	    if (gradeOrView == null || (!gradeOrView.equals(AssignmentConstants.GRADE) && !gradeOrView.equals(AssignmentConstants.VIEW))) {
 	        throw new IllegalArgumentException("Invalid gradeOrView " + gradeOrView + " passed to getAvailableStudentsForUserForItem");
 	    }
 
 	    Map<Assignment2, List<String>> assignToAvailStudentsMap = new HashMap<Assignment2, List<String>>();
 	    
         String contextId = externalLogic.getCurrentContextId();
         List<String> allStudentsInSite = externalLogic.getStudentsInSite(contextId);
         boolean userAbleToGradeAll = gradebookLogic.isUserAbleToGradeAll(contextId, userId);
         boolean userAbleToGrade = gradebookLogic.isUserAbleToGrade(contextId, userId);
 
 	    if (assignmentList != null && !assignmentList.isEmpty()) {
 	        
 	        for (Assignment2 assignment : assignmentList) {
                 List<String> availStudents = new ArrayList<String>();
                 
 	            if (!assignment.isRemoved()) {
 
 	                List<AssignmentGroup> assignGroupRestrictions = 
 	                    dao.findByProperties(AssignmentGroup.class, new String[] {"assignment"}, new Object[] {assignment});
 
 	                // if user may grade all, then return all of the students who have this assignment.
 	                // we need to check for group restrictions
 	                if (userAbleToGradeAll) {
 	                    if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 	                        availStudents.addAll(allStudentsInSite);
 	                    } else {
 	                        availStudents = getAllAvailableStudentsGivenGroupRestrictions(
 	                                contextId, assignGroupRestrictions);
 	                    }
 
 	                } else if(userAbleToGrade) {
 	                    if (!assignment.isGraded()) {
 	                        // if there are no restrictions, return students in user's group(s)
 	                        if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 	                            Set<String> sharedStudents = getStudentsInUsersGroups(userId, contextId);
 	                            if (sharedStudents != null) {
 	                                availStudents.addAll(sharedStudents);
 	                            }
 	                        } else {
 	                            // otherwise, only return students in his/her group if it is one
 	                            // of the group restrictions
 	                            List<String> memberships = externalLogic.getUserMembershipGroupIdList(userId, contextId);
 	                            if (memberships != null && !memberships.isEmpty()) {
 	                                for (AssignmentGroup group : assignGroupRestrictions) {
 	                                    if (group != null && memberships.contains(group.getGroupId())) {
 	                                        availStudents.addAll(externalLogic.getStudentsInGroup(group.getGroupId()));
 	                                    }
 	                                }
 	                            }
 	                        }
 
 	                    } else {
 	                        // we need to get the students that are viewable in the gb
 	                        List<String> viewableInGb = new ArrayList<String>();
 	                        if (assignment.getGradebookItemId() != null) {
 	                            Map<String, String> studentIdFunctionMap = 
 	                                gradebookLogic.getViewableStudentsForGradedItemMap(userId, contextId, assignment.getGradebookItemId());
 	                            if (studentIdFunctionMap != null) {
 	                                if (gradeOrView.equals(AssignmentConstants.VIEW)) {
 	                                    viewableInGb.addAll(studentIdFunctionMap.keySet());
 	                                } else {
 	                                    for (Map.Entry<String, String> entry : studentIdFunctionMap.entrySet()) {
 	                                        String studentId = entry.getKey();
 	                                        String function = entry.getValue();
 	                                        if (studentId != null && function != null
 	                                                && function.equals(AssignmentConstants.GRADE)) {
 	                                            viewableInGb.add(studentId);
 	                                        }
 	                                    }
 	                                }
 	                            }
 	                        }
 
 	                        // now we need to filter out the ones who aren't associated w/ this assignment
 	                        if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 	                            availStudents.addAll(viewableInGb);
 	                        } else {
 	                            List<String> availForAssign = getAllAvailableStudentsGivenGroupRestrictions(
 	                                    contextId, assignGroupRestrictions);
 	                            if (availForAssign != null && !availForAssign.isEmpty()) {
 	                                for (String studentId : availForAssign) {
 	                                    if (studentId != null && viewableInGb.contains(studentId)) {
 	                                        availStudents.add(studentId);
 	                                    }
 	                                }
 	                            }
 	                        }
 	                    }
 	                }     
 	            }
 	            
 	            assignToAvailStudentsMap.put(assignment, availStudents);
 	        }
 	    }
 
 	    return assignToAvailStudentsMap;
 	}
 	
 	/**
 	 * Given 2 lists of group ids, will return true if any of the group ids overlap
 	 * @param user1GroupIds
 	 * @param user2GroupIds
 	 * @return
 	 */
 	private boolean userMembershipsOverlap(List<String> user1GroupIds, List<String> user2GroupIds) {
 		if (user1GroupIds != null && user2GroupIds != null ) {
 			for (String user1Group : user1GroupIds) {
 				if (user1Group != null && user2GroupIds.contains(user1Group)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private Set<String> getStudentsInUsersGroups(String userId, String contextId) {
 		// get the user's memberships
 		List<String> groupMemberships = externalLogic.getUserMembershipGroupIdList(userId, contextId);
 		
 		// use a set to eliminate group overlap with duplicate students
 		Set<String> sharedStudents = new HashSet<String>();
 		if (groupMemberships != null) {
 			for (String groupId : groupMemberships) {
 				if (groupId != null) {
 					List<String> students = externalLogic.getStudentsInGroup(groupId);
 					if (students != null) {
 						sharedStudents.addAll(students);
 					}
 				}
 			}
 
 		}
 		
 		return sharedStudents;
 	}
 	
 	public boolean isUserAllowedToProvideFeedbackForAssignment(Assignment2 assignment) {
 		// returns true if user is authorized to grade at least one student for
 		// this assignment
 		if (assignment == null) {
 			throw new IllegalArgumentException("null assignment passed to isUserAllowedToProvideFeedbackForAssignment");
 		}
 		
 		boolean allowedToGrade = false;
 		
		String currUserId = externalLogic.getCurrentUserId();
		
		try {
			List<String> gradableStudents = getGradableStudentsForUserForItem(currUserId, assignment);
			if (gradableStudents != null && gradableStudents.size() > 0) {
				allowedToGrade = true;
			}
		} catch (SecurityException se) {
			// this user does not have grading privileges, so may not release
			allowedToGrade = false;
 		}
 		
 		return allowedToGrade;
 	}
 	
 	public boolean isCurrentUserAbleToSubmit(String contextId) {
 		return gradebookLogic.isCurrentUserAStudentInGb(contextId);
 	}
 	
 	/**
 	 * 
 	 * @param contextId
 	 * @param assignGroupRestrictions
 	 * @return a list of all of the students who are able to view this assignment
 	 * given any group restrictions
 	 */
 	private List<String> getAllAvailableStudentsGivenGroupRestrictions(String contextId, Collection<AssignmentGroup> assignGroupRestrictions) {
 		List<String> allStudentsForAssign = new ArrayList<String>();
 		
 		// if there are group restrictions, only a subset of all of the students in the
 		// class will be available for this assignment
 		if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty()) {
 			allStudentsForAssign = externalLogic.getStudentsInSite(contextId);
 		} else {
 			// use a set to make sure students only appear once, even if they
 			// are in multiple groups
 			Set<String> studentsInRestrictedGroups = new HashSet<String>();
 			for (AssignmentGroup group : assignGroupRestrictions) {
 				if (group != null) {
 					studentsInRestrictedGroups.addAll(externalLogic.getStudentsInGroup(group.getGroupId()));
 				}
 			}
 			
 			allStudentsForAssign.addAll(studentsInRestrictedGroups);
 		}
 		
 		return allStudentsForAssign;
 	}
 
 	public boolean isUserAbleToViewAssignment(String contextId, Long assignmentId) {
 		if (contextId == null || assignmentId == null) {
 			throw new IllegalArgumentException("Null contextId or assignmentId passed to " +
 					"isUserAbleToViewAssignment- contextId: " + contextId + " assignmentId: " + assignmentId);
 		}
 		boolean allowed = false;
 
 		// retrieve the assignment
 		Assignment2 assign = dao.getAssignmentByIdWithGroups(assignmentId);
 		if (assign == null) {
 		    throw new AssignmentNotFoundException("No assignment found with id " + assignmentId);
 		}
 
 		String currUserId = externalLogic.getCurrentUserId();
 
 		if (assign.isRemoved()) {
 		    // if it has been removed only students may view it if they have 
 		    // an existing submission
 		    Set<AssignmentSubmission> subForRemovedAssign = dao.getExistingSubmissionsForRemovedAssignments(currUserId, contextId);
 		    if (subForRemovedAssign != null && !subForRemovedAssign.isEmpty()) {
 		        for (AssignmentSubmission sub : subForRemovedAssign) {
 		            if (sub.getAssignment().getId().equals(assignmentId)) {
 		                allowed = true;
 		            }
 		        }
 		    }
 
 		} else {
 
 		    List<String> groupMembershipIds = new ArrayList<String>();
 		    if (assign.getAssignmentGroupSet() != null && !assign.getAssignmentGroupSet().isEmpty()) {
 		        groupMembershipIds = externalLogic.getUserMembershipGroupIdList(currUserId, contextId);
 		    }
 
 		    if (assign.isGraded()) {
 		        allowed = isUserAbleToViewGradedAssignment(assign, groupMembershipIds);
 		    } else {
 		        allowed = isUserAbleToViewUngradedAssignment(assign, groupMembershipIds);
 		    }
 		}
 		
 		return allowed;
 	}
 	
 	public List<String> getUsersAllowedToViewStudentForAssignment(String studentId, Assignment2 assignment) {
 	    if (studentId == null || assignment == null) {
 	        throw new IllegalArgumentException("Null studentId or assignmentId passed to getUsersAllowedToViewSubmission");
 	    }
 	    
 	    List<String> usersAllowedToViewStudent = new ArrayList<String>();
 
 	    // identify all of the instructors in this site
 	    Set<String> instructorsAndTas = new HashSet<String>();
 	    List<String> instructors = externalLogic.getInstructorsInSite(assignment.getContextId());
 	    List<String> tas = externalLogic.getTAsInSite(assignment.getContextId());
 	    
 	    if (instructors != null) {
 	        instructorsAndTas.addAll(instructors);
 	    }
 	    
 	    if (tas != null) {
 	        instructorsAndTas.addAll(tas);
 	    }
 	    
 	    for (String userId : instructorsAndTas) {
 	        List<String> viewableStudents = getViewableStudentsForUserForItem(userId, assignment);
 	        if (viewableStudents != null && viewableStudents.contains(studentId)) {
 	            usersAllowedToViewStudent.add(userId);
 	        }
 	    }
 	    
 	    return usersAllowedToViewStudent;
 	}
 	
 	public List<Group> getViewableGroupsForCurrUserForAssignment(Long assignmentId) {
 	    List<Group> viewableGroups = new ArrayList<Group>();
 
 	    Assignment2 assign = dao.getAssignmentByIdWithGroups(assignmentId);
 	    if (assign == null) {
 	        throw new AssignmentNotFoundException("getViewableGroupsForCurrUserForAssignment: " +
 	                "No assignment exists with id: " + assignmentId);
 	    }
 
 	    // first, we need to identify the viewable groups in the gb. then we will filter
 	    // these based upon whether or not this assignment is restricted to groups.
 	    List<Group> viewableGroupsInGB = gradebookLogic.getViewableGroupsInGradebook(assign.getContextId());
 	    if (viewableGroupsInGB != null && !viewableGroupsInGB.isEmpty()) {
 
 	        if (assign.getAssignmentGroupSet() == null || assign.getAssignmentGroupSet().isEmpty()) {
 	            // there are no group restrictions, so return all viewable
 	            viewableGroups = viewableGroupsInGB;
 	        } else {
 	            // put the groups in a map for easier processing
 	            Map<String, Group> gbGroupIdToGroupMap = new HashMap<String, Group>();
 	            for (Group group : viewableGroupsInGB) {
 	                gbGroupIdToGroupMap.put(group.getId(), group);
 	            }
 
 	            // check to see if this user may view any of the restricted groups
 	            for (AssignmentGroup assignGroup : assign.getAssignmentGroupSet()) {
 	                String groupId = assignGroup.getGroupId();
 	                
 	                if (gbGroupIdToGroupMap.containsKey(groupId)) {
 	                    viewableGroups.add(gbGroupIdToGroupMap.get(groupId));
 	                }
 	            }
 	        }
 	    }
 
 	    return viewableGroups;
 	}
 	
 	public boolean isUserAbleToProvideFeedbackForStudents(Collection<String> studentUids, Assignment2 assignment) {
 	    if (assignment == null) {
 	        throw new IllegalArgumentException("Null assignment passed to isUserAbleToProvideFeedbackForStudents");
 	    }
 	    boolean allowed = true;
 	    if (studentUids != null) {
 	        List<String> gradableStudents = getGradableStudentsForUserForItem(externalLogic.getCurrentUserId(), assignment);
 	        if (gradableStudents != null) {
 	            for (String studentUid : studentUids) {
 	                if (!gradableStudents.contains(studentUid)) {
 	                    allowed = false;
 	                    break;
 	                }
 	            }
 	        }
 	    }
 
 	    return allowed;    
 	}
 
 }
