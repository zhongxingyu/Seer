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
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.exception.GradebookItemNotFoundException;
 import org.sakaiproject.assignment2.exception.InvalidGradeForAssignmentException;
 import org.sakaiproject.assignment2.exception.NoGradebookDataExistsException;
 import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.GradeInformation;
 import org.sakaiproject.assignment2.logic.GradebookItem;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
 import org.sakaiproject.service.gradebook.shared.Assignment;
 import org.sakaiproject.service.gradebook.shared.CommentDefinition;
 import org.sakaiproject.service.gradebook.shared.GradeDefinition;
 import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
 import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
 import org.sakaiproject.service.gradebook.shared.GradebookService;
 import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
 import org.sakaiproject.site.api.Group;
 
 
 /**
  * This is the implementation for logic to interact with the Sakai
  * Gradebook tool
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class ExternalGradebookLogicImpl implements ExternalGradebookLogic {
 
     private static Log log = LogFactory.getLog(ExternalGradebookLogicImpl.class);
 
     public void init() {
     	if (log.isDebugEnabled()) log.debug("init");
     }
     
     private GradebookService gradebookService;
     public void setGradebookService(GradebookService gradebookService) {
     	this.gradebookService = gradebookService;
     }
     
     private ExternalLogic externalLogic;
     public void setExternalLogic(ExternalLogic externalLogic) {
         this.externalLogic = externalLogic;
     }
 
     public List<Assignment2> getViewableGradedAssignments(List<Assignment2> gradedAssignments, String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("contextId is null in getViewableAssignmentsWithGbData");
     	}
     	
     	List<Assignment2> viewableGradedAssignments = new ArrayList<Assignment2>();
     	if (gradedAssignments == null || gradedAssignments.isEmpty()) {
     		return viewableGradedAssignments;
     	}
     	
     	List<Assignment> gbAssignments = gradebookService.getViewableAssignmentsForCurrentUser(contextId);
 		
 		Map<Long, Assignment> goIdGbAssignmentMap = new HashMap<Long, Assignment>();
 		if (gbAssignments != null) {
 			for (Assignment gbObject : gbAssignments) {
 				if (gbObject != null) {
 					goIdGbAssignmentMap.put(gbObject.getId(), gbObject);
 				}
 			}
 		}
 		
 		boolean userIsStudent = isCurrentUserAStudentInGb(contextId);
 		
 		// there are 2 situations in which the gradebook item associated with the assignment
 		// is not included in the list returned from the gradebook:
 		// 1) the user does not have permission to view that GO - if the user is a student,
 		//		we still want to display the assignment but don't want to display the
 		//		associated grade info b/c not released to students yet
 		// 2) the GO was deleted from the gb
 		
 		for (Assignment2 gradedAssignment : gradedAssignments) {
 			if (gradedAssignment != null && gradedAssignment.isGraded()) {
 				Long goId = gradedAssignment.getGradebookItemId();
 				if (goId != null) {
 					Assignment gbItem =	(Assignment)goIdGbAssignmentMap.get(goId);
 					if (gbItem != null) {
 						viewableGradedAssignments.add(gradedAssignment);
 					} else {
 						// check to see if this gradebook item exists anymore
 						if (!gradebookService.isGradableObjectDefined(goId)) {
 							viewableGradedAssignments.add(gradedAssignment);
 						} else {
 							// if it exists, then this user does not have perm to view it in the gb
 							if (userIsStudent) {
 								// if a student, we still need to return an assignment with gb item info
 								// this just means the grade info has not been released yet - we still
 								// let students view the assignment
 								viewableGradedAssignments.add(gradedAssignment);
 							}
 						}
 					}
 				} else {
 					// there is no gradebookItemId set for this assignment!
 					viewableGradedAssignments.add(gradedAssignment);
 				}
 			}
 		}
 		
 		return viewableGradedAssignments;
     }
 
     public void createGradebookDataIfNecessary(String contextId) {
     	// we need to check if there is gradebook data defined for this site. if not,
         // create it (but will not actually add the tool, just the backend)
     	
     	GradebookFrameworkService frameworkService = (org.sakaiproject.service.gradebook.shared.GradebookFrameworkService) 
         ComponentManager.get("org.sakaiproject.service.gradebook.GradebookFrameworkService");
         if (!frameworkService.isGradebookDefined(contextId)) {
         	if (log.isInfoEnabled()) 
         		log.info("Gradebook data being added to context " + contextId + " by Assignment2 tool");
         	frameworkService.addGradebook(contextId, contextId);
         }
     }
 
     public Map<Long, String> getViewableGradebookItemIdTitleMap(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("Null contextId passed to getViewableGradebookItemIdTitleMap");
     	}
     	
     	List<Assignment> viewableGbItems = gradebookService.getViewableAssignmentsForCurrentUser(contextId);
     	
     	Map<Long, String> idTitleMap = new HashMap<Long, String>();
     	if (viewableGbItems == null || viewableGbItems.isEmpty()) {
     		return idTitleMap;
     	}
     	
     	for (Assignment assign : viewableGbItems) {
     		if (assign != null) {
     			idTitleMap.put(assign.getId(), assign.getName());
     		}
     	}
     	
     	return idTitleMap;
     }
 
     public List<GradebookItem> getAllGradebookItems(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("null contextId passed to getAllGradebookItems");
     	}
 
     	List<GradebookItem> gradebookItems = new ArrayList<GradebookItem>();
 
     	try {
     		List<Assignment> allGbItems = gradebookService.getAssignments(contextId);
 
     		if (allGbItems != null) {
 
     			for (Assignment assign : allGbItems) {
     				if (assign != null) {
     					GradebookItem item = 
     						new GradebookItem(assign.getId(), assign.getName(), assign.getPoints(), assign.getDueDate(), assign.isReleased());
     					
     					if (assign.isExternallyMaintained()) {
     						item.setExternalId(assign.getExternalId());
     					}
     					
     					gradebookItems.add(item);
     				}
     			}
     		}
     	} catch (SecurityException se) {
     		throw new SecurityException("User without edit or grade perm attempted to access the list of all gb items.", se);
     	} catch (GradebookNotFoundException gnfe) {
     		throw new NoGradebookDataExistsException("No gradebook exists for the given contextId: " + contextId, gnfe);
     	}
 
     	return gradebookItems;
     }
 
     public List<Group> getViewableGroupsInGradebook(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("Null contextId passed to getViewableGroupIdToTitleMap");
     	}
     	
         List<Group> viewableGroups = new ArrayList<Group>();
         
     	Collection<Group> allGroupsInSite = externalLogic.getSiteGroups(contextId);
     	if (allGroupsInSite != null && !allGroupsInSite.isEmpty()) {
     	    // let's identify the groups that the current user is allowed to
     	    // view in the gradebook. this method returns the sectionUid (group reference)
     	    Map<String,String> sectionUidToNameMap =  gradebookService.getViewableSectionUuidToNameMap(contextId);
     	    if (sectionUidToNameMap != null && !sectionUidToNameMap.isEmpty()) {
     	        for (Group siteGroup : allGroupsInSite) {
     	            if (sectionUidToNameMap.containsKey(siteGroup.getReference())) {
     	                viewableGroups.add(siteGroup);
     	            }
     	        }
     	    }
     	}
     	
     	return viewableGroups;
     }
 
     public Map<String, String> getViewableStudentsForGradedItemMap(String userId, String contextId, Long gradebookItemId) {
         if (contextId == null || userId == null) {
             throw new IllegalArgumentException("Null contextId or userId passed to " +
                     "getViewableGroupIdToTitleMap. contextId: " + contextId +
                     " userId:" + userId);
         }
 
         Map<String, String> studentIdAssnFunctionMap = new HashMap<String, String>();
 
 
         Map<String, String> studentIdGbFunctionMap = gradebookService.getViewableStudentsForItemForUser(userId, contextId, gradebookItemId);
 
         if (studentIdGbFunctionMap != null) {
             for (Map.Entry<String, String> entry : studentIdGbFunctionMap.entrySet()) {
                 String studentId = entry.getKey();
                 String function = entry.getValue();
                 if (studentId != null && function != null) {
                     if (function != null) {
                         if (function.equals(GradebookService.gradePermission)) {
                             studentIdAssnFunctionMap.put(studentId, AssignmentConstants.GRADE);
                         } else {
                             studentIdAssnFunctionMap.put(studentId, AssignmentConstants.VIEW);
                         }
                     }
                 }
             }
         }
 
         return studentIdAssnFunctionMap;
     }
     
     public boolean isCurrentUserAbleToEdit(String contextId) {
     	return gradebookService.currentUserHasEditPerm(contextId);
     }
 	
 	public boolean isCurrentUserAbleToGradeAll(String contextId) {
     	return gradebookService.currentUserHasGradeAllPerm(contextId);
 	}
 	
 	public boolean isUserAbleToGradeAll(String contextId, String userId) {
 	    return gradebookService.isUserAllowedToGradeAll(contextId, userId); 
 	}
 	
 	public boolean isCurrentUserAbleToGrade(String contextId) {
     	return gradebookService.currentUserHasGradingPerm(contextId);
 	}
 	
 	public boolean isUserAbleToGrade(String contextId, String userId) {
         return gradebookService.isUserAllowedToGrade(contextId, userId); 
     }
 	
 	public boolean isCurrentUserAbleToViewOwnGrades(String contextId) {
     	return gradebookService.currentUserHasViewOwnGradesPerm(contextId);
 	}
 	
 	public boolean isCurrentUserAStudentInGb(String contextId) {
 		boolean userIsAStudentInGb = false;
 		
 		if (isCurrentUserAbleToViewOwnGrades(contextId) &&
 				!isCurrentUserAbleToGrade(contextId) &&
 				!isCurrentUserAbleToEdit(contextId)) {
 			userIsAStudentInGb = true;
 		}
 		
 		return userIsAStudentInGb;
 	}
     
     public String getGradeViewPermissionForCurrentUserForStudentForItem(String contextId, String studentId, Long gbItemId) {
     	if (contextId == null || studentId == null || gbItemId == null) {
     		throw new IllegalArgumentException("Null contextId or studentId or itemId passed to getGradeViewPermissionForCurrentUserForStudentForItem");
     	}
     	
     	String viewOrGrade = null;
     	
     	String function =
     		gradebookService.getGradeViewFunctionForUserForStudentForItem(contextId, gbItemId, studentId);
     	
     	if (function == null) {
     		viewOrGrade = null;
     	} else if (function.equals(GradebookService.gradePermission)) {
     		viewOrGrade = AssignmentConstants.GRADE;
     	} else if (function.equals(GradebookService.viewPermission)) {
     		viewOrGrade = AssignmentConstants.VIEW;
     	}
     	
     	return viewOrGrade;
     }
     
     public String getStudentGradeForItem(String contextId, String studentId, Long gradebookItemId) {
     	if (contextId == null || studentId == null || gradebookItemId == null) {
     		throw new IllegalArgumentException("Null contextId or studentId or gbItemId passed to getStudentGradeForItem");
     	}
     	
     	String grade = null;
     	
     	try {
     		GradeDefinition gradeDef = gradebookService.getGradeDefinitionForStudentForItem(contextId, gradebookItemId, studentId);
     		if (gradeDef != null) {
     			grade = gradeDef.getGrade();
     		}
     	} catch (AssessmentNotFoundException anfe) {
     		// this gradebook item no longer exists, so return a null grade
     		grade = null;
     	} catch (GradebookNotFoundException gnfe) {
     		throw new NoGradebookDataExistsException("No gradebook exists for the given contextId: " + contextId, gnfe);
     	} catch (SecurityException se) {
     		throw new SecurityException("User attempted to access the grade for student : " + 
     				studentId + " for gbItemId: " + gradebookItemId + " without authorization in gb", se);
     	}
     	
     	return grade;
     }
     
     public String getStudentGradeCommentForItem(String contextId, String studentId, Long gradebookItemId) {
     	if (contextId == null || studentId == null || gradebookItemId == null) {
     		throw new IllegalArgumentException("Null contextId or studentId or gbItemId passed to getStudentGradeCommentForItem");
     	}
     	
     	String comment = null;
     	
     	try {
     		CommentDefinition commentDef = gradebookService.getAssignmentScoreComment(contextId, gradebookItemId, studentId);
     		if (commentDef != null) {
     			comment = commentDef.getCommentText();
     		}
     	} catch (AssessmentNotFoundException anfe) {
     		// this gradebook item no longer exists, so return a null comment
     		comment = null;
     	} catch (GradebookNotFoundException gnfe) {
     		throw new NoGradebookDataExistsException(
 					"No gradebook exists for the given contextId: " + contextId, gnfe);
     	}
     	
     	return comment;
     }
     
     public boolean isCurrentUserAbleToGradeStudentForItem(String contextId, String studentId, Long gradebookItemId) {
     	if (contextId == null || studentId == null || gradebookItemId == null) {
     		throw new IllegalArgumentException("null contextId, studentId or gradebookItemId " +
     				"passed to isCurrentUserAbleToGradeStudentForItem. contextId:" + contextId +
     				" studentId:" + studentId + " gradebookItemId: " + gradebookItemId);
     	}
     	
     	return gradebookService.isUserAbleToGradeItemForStudent(contextId, gradebookItemId, studentId);
     }
     
     public GradeInformation getGradeInformationForSubmission(String contextId, AssignmentSubmission submission) {
     	if (contextId == null || submission == null) {
     		throw new IllegalArgumentException("null contextId or submission passed to populateAllGradeInfoForSubmission");
     	}
     	
     	Assignment2 assignment = submission.getAssignment();
     	if (assignment == null) {
     		throw new IllegalArgumentException("Null assignment associated with submission passed to getGradeInfoForSubmission");
     	}
     	
     	GradeInformation gradeInfo = new GradeInformation();
     	gradeInfo.setStudentId(submission.getUserId());
     	
     	if (assignment.isGraded() && assignment.getGradebookItemId() != null) {
     		try {
 				GradeDefinition gradeDef = gradebookService.getGradeDefinitionForStudentForItem(contextId, assignment.getGradebookItemId(), submission.getUserId());
 				
 				if (gradeDef != null) {
 					gradeInfo.setGradebookGrade(gradeDef.getGrade());
 					gradeInfo.setGradebookGradeReleased(gradeDef.isGradeReleased());
 					gradeInfo.setGradebookComment(gradeDef.getGradeComment());
 				}
 				
 			} catch (AssessmentNotFoundException anfe) {
 				// this gb item no longer exists, so there is no information to populate
 				if (log.isDebugEnabled()) log.debug("gb item with id " + assignment.getGradebookItemId() + " no longer exists, so returning null grade info");
 			} catch (SecurityException se) {
 				throw new SecurityException("User does not have authorization to access " +
 						"grade information for " + submission.getUserId() + " for gb item " + 
 						assignment.getGradebookItemId(), se);
 			}
     	}
     	
     	return gradeInfo;
     }
 	
 	public Long createGbItemInGradebook(String contextId, String title, Double pointsPossible, Date dueDate,
 			boolean releasedToStudents, boolean countedInCourseGrade) {
 		if (contextId == null || title == null) {
 			throw new IllegalArgumentException("Null contextId or title passed to createGbItemInGradebook");
 		}
 		
 		if (countedInCourseGrade && !releasedToStudents) {
 			throw new IllegalArgumentException("You may not count an item in course" +
 					" grade without releasing to students.");
 		}
 		
 		Long gradebookItemId = null;
 		Assignment newItem = new Assignment();
 		newItem.setCounted(countedInCourseGrade);
 		newItem.setDueDate(dueDate);
 		newItem.setName(title);
 		newItem.setPoints(pointsPossible);
 		newItem.setReleased(releasedToStudents);
 		
		if (pointsPossible == null) {
		    newItem.setUngraded(true);
		} else {
		    newItem.setUngraded(false);
		}
		
 		gradebookService.addAssignment(contextId, newItem);
 		if (log.isDebugEnabled()) log.debug("New gradebook item added to gb via assignment2 tool");
 		
 		// now let's retrieve the id of this newly created item
 		Assignment newlyCreatedAssign = gradebookService.getAssignment(contextId, title);
 		if (newlyCreatedAssign != null) {
 			gradebookItemId = newlyCreatedAssign.getId();
 		}
 		
 		return gradebookItemId;
 	}
 	
 	public GradebookItem getGradebookItemById(String contextId, Long gradebookItemId) {
 		if (contextId == null || gradebookItemId == null) {
 			throw new IllegalArgumentException ("Null contextId or gradebookItemId " +
 					"passed to getGradebookItemById. contextId:" +
 					contextId + " gradebookItemId:" + gradebookItemId);
 		}
 		
 		GradebookItem gradebookItem = new GradebookItem();
 		
 		try {
 			Assignment assign = gradebookService.getAssignment(contextId, gradebookItemId);
 			gradebookItem = new GradebookItem(assign.getId(), assign.getName(), 
 			        assign.getPoints(), assign.getDueDate(), assign.isReleased());
 		} catch (AssessmentNotFoundException anfe) {
 			throw new GradebookItemNotFoundException ("No gradebook item exists with gradebookItemId " 
 					+ gradebookItemId + " in context " + contextId, anfe);
 		}
 		
 		return gradebookItem;
 	}
 	
 	public void saveGradeAndCommentForStudent(String contextId, Long gradebookItemId, String studentId, String grade, String comment) {
 		if (contextId == null || gradebookItemId == null || studentId == null) {
 			throw new IllegalArgumentException("Null contextId or gradebookItemId " +
 					"or studentId passed to saveGradeAndCommentForStudent. contextId:" + 
 					contextId + " studentId:" + studentId + " gradebookItemId:" + gradebookItemId);
 		}
 		
 		try {
 			gradebookService.saveGradeAndCommentForStudent(contextId, gradebookItemId, studentId, grade, comment);
 			if(log.isDebugEnabled()) log.debug("Grade and comment for student " + studentId + 
 					" for gbItem " + gradebookItemId + "updated successfully");
 		} catch (GradebookNotFoundException gnfe) {
 			throw new NoGradebookDataExistsException("No gradebook exists in the given context "
 					+ contextId, gnfe);
 		} catch (AssessmentNotFoundException anfe) {
 			throw new GradebookItemNotFoundException("No gradebook item exists with the given id "
 					+ gradebookItemId, anfe);
 		} catch (InvalidGradeException ige) {
 			throw new InvalidGradeForAssignmentException("The grade: " + grade + 
 					" for gradebook " + contextId + " is invalid.", ige);
 		} catch (SecurityException se) {
 			throw new SecurityException(
 					"The current user attempted to saveGradeAndCommentForStudent "
 							+ "without authorization. Error: " + se.getMessage(), se);
 		}
 	}
 
 	public Map<String, GradeInformation> getGradeInformationForStudents(List<String> studentIdList, String contextId, Long gradebookItemId) {
 		if (contextId == null || gradebookItemId == null) {
 			throw new IllegalArgumentException("null contextId or gradebookItemId " +
 					"passed to getGradeInformationForStudents. contextId:" + 
 					contextId + " gradebookItemId:" + gradebookItemId);
 		}
 
 		Map<String, GradeInformation> studentIdToGradeInfoMap = new HashMap<String, GradeInformation>();
 
 		if (studentIdList != null && !studentIdList.isEmpty()) {
 			// let's retrieve a map of the students and their grades
 			Map<String, GradeDefinition> studentIdToGradeDefMap = new HashMap<String, GradeDefinition>();
 
 			try {
 				List<GradeDefinition>gradeDefs = gradebookService.getGradesForStudentsForItem(contextId, 
 						gradebookItemId, studentIdList);
 
 				if (gradeDefs != null) {
 					for (GradeDefinition gradeDef : gradeDefs) {
 						studentIdToGradeDefMap.put(gradeDef.getStudentUid(), gradeDef);
 					}
 				}
 			} catch (SecurityException se) {
 				throw new SecurityException("User attempted to access a list of student grades" +
 						" that he/she did not have authorization for in the gb.", se);
 			}
 
 			for (String studentId : studentIdList) {
 
 				GradeInformation gradeInfo = new GradeInformation();
 				gradeInfo.setStudentId(studentId);
 				gradeInfo.setGradebookItemId(gradebookItemId);
 
 				// get the GradeDefinition for this student and convert it to
 				// our local GradeInformation object
 				GradeDefinition gradeDef = studentIdToGradeDefMap.get(studentId);
 				if (gradeDef != null) {
 					gradeInfo.setGradebookComment(gradeDef.getGradeComment());
 					gradeInfo.setGradebookGrade(gradeDef.getGrade());
 					gradeInfo.setGradebookGradeReleased(gradeDef.isGradeReleased());
 				} 
 
 				studentIdToGradeInfoMap.put(studentId, gradeInfo);
 			}
 		}
 
 		return studentIdToGradeInfoMap;
 	}
 	
 	public void saveGradesAndComments(String contextId, Long gradebookItemId, 
 			List<GradeInformation> gradeInfoList) {
 		if (contextId == null || gradebookItemId == null) {
 			throw new IllegalArgumentException("Null contextId or gradebookItemId " +
 					"passed to saveGradesAndCommentsForSubmissions. contextId:" + contextId +
 					" gradebookItemId:" + gradebookItemId);
 		}
 		
 		List<GradeDefinition> gradeDefList = new ArrayList<GradeDefinition>();
 		
 		if (gradeInfoList != null) {
 			for (GradeInformation gradeInfo : gradeInfoList) {
 				if (gradeInfo != null) {
 					// double check that we weren't passed submissions for 
 					// different assignments - we don't want to update the wrong grades!
 					Long assignGo = gradeInfo.getGradebookItemId();
 					if (assignGo == null ||	!assignGo.equals(gradebookItemId)) {
 						throw new IllegalArgumentException("The given submission's gradebookItemId " + assignGo +
 							" does not match the goId passed to saveGradesAndCommentsForSubmissions: " + gradebookItemId);
 					}
 					
 					// convert the info into a GradeDefinition
 					GradeDefinition gradeDef = new GradeDefinition();
 					gradeDef.setGrade(gradeInfo.getGradebookGrade());
 					gradeDef.setGradeComment(gradeInfo.getGradebookComment());
 					gradeDef.setStudentUid(gradeInfo.getStudentId());
 					
 					gradeDefList.add(gradeDef);
 				} 
 			}
 			
 			// now try to save the new information
 			try {
 				gradebookService.saveGradesAndComments(contextId, gradebookItemId, gradeDefList);
 				if(log.isDebugEnabled()) log.debug("Grades and comments for contextId " + contextId + 
 						" for gbItem " + gradebookItemId + "updated successfully for " + gradeDefList.size() + " students");
 			} catch (GradebookNotFoundException gnfe) {
 				throw new NoGradebookDataExistsException(
 						"No gradebook exists in the given context " + contextId, gnfe);
 			} catch (AssessmentNotFoundException anfe) {
 				throw new GradebookItemNotFoundException(
 						"No gradebook item exists with the given id " + gradebookItemId, anfe);
 			} catch (InvalidGradeException ige) {
 				throw new InvalidGradeForAssignmentException("Attempted to save an invalid grade in gradebook " 
 						+ contextId + " via saveGradesAndCommentsForSubmissions. " +
 								"No grade information was updated.", ige);
 			} catch (SecurityException se) {
 				throw new SecurityException(
 						"The current user attempted to saveGradeAndCommentForStudent "
 								+ "without authorization. Error: " + se.getMessage(), se);
 			}
 		}
 	}
 
 	public boolean gradebookDataExistsInSite(String contextId) {
 		if (contextId == null) {
 			throw new IllegalArgumentException("Null contextId passed to gradebookDataExistsInSite");
 		}
 		
 		return gradebookService.isGradebookDefined(contextId);
 	}
 	
 	public boolean isGradeValid(String contextId, String grade) {
 		if (contextId == null) {
 			throw new IllegalArgumentException("Null contextId passed to isGradeValid");
 		}
 		
 		boolean valid = false;
 		
 		try {
 			valid = gradebookService.isGradeValid(contextId, grade);
 		} catch (GradebookNotFoundException gnfe) {
 			throw new NoGradebookDataExistsException("No gradebook exists in the given context: "
 					+ contextId, gnfe);
 		}
 		
 		return valid;
 	}
 	
 	public List<String> identifyStudentsWithInvalidGrades(String contextId, Map<String, String> studentIdToGradeMap) {
 		if (contextId == null) {
 			throw new IllegalArgumentException("Null contextId passed to getStudentsWithInvalidGrades");
 		}
 		
 		List<String> studentsWithInvalidGrades = new ArrayList<String>();
 		if (studentIdToGradeMap != null) {
 			try {
 				studentsWithInvalidGrades = gradebookService.identifyStudentsWithInvalidGrades(contextId, studentIdToGradeMap);
 			} catch (GradebookNotFoundException gnfe) {
 				throw new NoGradebookDataExistsException(
 						"No gradebook exists in the given context: " + contextId, gnfe);
 			}
 		}
 		
 		return studentsWithInvalidGrades;
 	}
 	
 	public boolean isCurrentUserAbleToViewGradebookItem(String contextId, Long gradebookItemId) {
 		if (contextId == null || gradebookItemId == null) {
 			throw new IllegalArgumentException ("Null parameter passed to " +
 					"sCurrentUserAbleToViewGradebookItem - contextId: " + contextId + 
 					", gradebookItemId: " + gradebookItemId);
 		}
 		
 		boolean allowed = false;
 		
 		if (isCurrentUserAbleToGradeAll(contextId)) {
 			allowed = true;
     	} else if (isCurrentUserAbleToGrade(contextId) || isCurrentUserAStudentInGb(contextId)) {
     		// check to see if this assignment is among the viewable assign for this user
     		Map<Long, String> gbItemIdToTitleMap = getViewableGradebookItemIdTitleMap(contextId);
     		if (gbItemIdToTitleMap.containsKey(gradebookItemId)) {
     			allowed = true;
     		}
     	} 
 		
 		return allowed;
 	}
 	
 	public void assignGradeToUngradedStudents(String contextId, Long gradebookItemId, List<String> studentIds, String grade) {
 	    if (contextId == null || gradebookItemId == null) {
 	        throw new IllegalArgumentException("Null contextId or gradebookItemId passed " +
 	        		"to assignGradeToUngradedSubmissions. contextId:" + contextId + " gradebookItemId:" + gradebookItemId);
 	    }
 
 	    // no need to continue if they didn't pass a new grade
 	    if (grade != null && grade.trim().length() > 0) {
 	        // first, let's validate the grade
 	        if (!isGradeValid(contextId, grade)) {
 	            throw new InvalidGradeForAssignmentException("Invalid grade passed to assignGradeToUngradedSubmissions: " + grade);
 	        }
 
 	        if (studentIds != null && !studentIds.isEmpty()) {
 
 	            // now determine which don't have a grade yet
 	            Map<String, GradeInformation> studentIdGradeInfoMap = getGradeInformationForStudents(studentIds, contextId, gradebookItemId);
 	            List<String> ungradedStudents = new ArrayList<String>();
 
 	            if (studentIdGradeInfoMap != null && !studentIdGradeInfoMap.isEmpty()) {
 	                for (Map.Entry<String, GradeInformation> entry : studentIdGradeInfoMap.entrySet()) {
 	                    String studentId = entry.getKey();
 	                    GradeInformation gradeInfo = entry.getValue();
 	                    if (studentId != null && gradeInfo != null) {
 	                        if (gradeInfo.getGradebookGrade() == null || gradeInfo.getGradebookGrade().trim().equals("")) {
 	                            ungradedStudents.add(studentId);
 	                        }
 	                    }
 	                }
 
 	                if (!ungradedStudents.isEmpty()) {
 	                    List<GradeInformation> gradeInfoToSave = new ArrayList<GradeInformation>();
 	                    for (String ungradedStudent : ungradedStudents) {
 	                        GradeInformation gradeInfo = studentIdGradeInfoMap.get(ungradedStudent);
 	                        gradeInfo.setGradebookGrade(grade);
 	                        gradeInfoToSave.add(gradeInfo);
 	                    }
 
 	                    saveGradesAndComments(contextId, gradebookItemId, gradeInfoToSave);
 	                }
 	            }
 	        }
 	    }
 	}
 	
 	public void releaseOrRetractGrades(String contextId, Long gradebookItemId, boolean release) {
 	    if (gradebookItemId == null || contextId == null) {
 	        throw new IllegalArgumentException("Null gradebookItemId passed to releaseOrRetractGrades." +
 	        		"contextId: " + contextId + " gradebookItemId: " + gradebookItemId);
 	    }
 	    
 	    try {
 	        Assignment gbAssign = gradebookService.getAssignment(contextId, gradebookItemId);
 	        gbAssign.setReleased(release);
 	        if (!release) {
 	            gbAssign.setCounted(false);
 	        }
 	        gradebookService.updateAssignment(contextId, gbAssign.getName(), gbAssign);
 	        if (log.isDebugEnabled()) log.debug("Gradebook setting released updated to " + release);
 	    } catch (AssessmentNotFoundException anfe) {
 	        throw new GradebookItemNotFoundException(
                     "No gradebook item exists with the given id " + gradebookItemId, anfe);
 	    }
 	}
 	
 	public boolean isGradingByPoints(String contextId) {
 	    if (contextId == null) {
 	        throw new IllegalArgumentException("Null contextId passed to isGradingByPoints");
 	    }
 	    
 	    boolean gradingByPoints = false;
 	    int gradeType = gradebookService.getGradeEntryType(contextId);
 	    if (gradeType == GradebookService.GRADE_TYPE_POINTS) {
 	        gradingByPoints = true;
 	    }
 	    
 	    return gradingByPoints;
 	}
 }
