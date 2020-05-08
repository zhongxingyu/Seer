 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/assignment2/trunk/impl/logic/src/java/org/sakaiproject/assignment2/dao/AssignmentDaoImpl.java $
  * $Id: AssignmentDaoImpl.java 12544 2006-05-03 15:06:26Z wagnermr@iupui.edu $
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
 
 package org.sakaiproject.assignment2.dao.impl;
 
 import java.lang.Integer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Iterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Query;
 
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.AssignmentSubmissionVersion;
 import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;
 
 /**
  * Implementations of any specialized DAO methods from the specialized DAO that allows the developer to extend the functionality of the
  * generic dao package
  * 
  * @author <a href="mailto:wagnermr@iupui.edu">michelle wagner</a>
  */
 public class AssignmentDaoImpl extends HibernateCompleteGenericDao implements AssignmentDao {
 
     private static Log log = LogFactory.getLog(AssignmentDaoImpl.class);
     
     public static final int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;
 
     public void init() {
         log.debug("init");
     }
     
     public Integer getHighestSortIndexInSite(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("contextId cannot be null");
     	}
     	String hql = "select max(assignment.sortIndex) from Assignment2 as assignment where assignment.contextId = :contextId and assignment.removed != true";
     	
     	Query query = getSession().createQuery(hql);
     	query.setParameter("contextId", contextId);
     	
     	Integer highestIndex = (Integer)query.uniqueResult();
     	if (highestIndex == null) {
     		highestIndex = 0;
     	}
 
         return highestIndex; 
     }
     
     public Set<Assignment2> getAssignmentsWithGroupsAndAttachments(String contextId) {
     	if (contextId == null) {
     		throw new IllegalArgumentException("Null contextId passed to getAssignmentsWithGroupsAndAttachments");
     	}
     	Query query = getSession().getNamedQuery("findAssignmentsWithGroupsAndAttachments");
     	query.setParameter("contextId", contextId);
     	
     	List<Assignment2> assignmentList = query.list();
     	
     	Set<Assignment2> assignmentSet = new HashSet();
     	
     	if (assignmentList != null) {
     		assignmentSet = new HashSet(assignmentList);
     	}
     	
     	return assignmentSet;
     }
     
     public Assignment2 getAssignmentByIdWithGroupsAndAttachments(Long assignmentId) {
     	if (assignmentId == null) {
     		throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithGroupsAndAttachments");
     	}
     	Query query = getSession().getNamedQuery("findAssignmentByIdWithGroupsAndAttachments");
     	query.setParameter("assignmentId",assignmentId);
     	
     	return (Assignment2) query.uniqueResult();
     }
 
 	public Assignment2 getAssignmentByIdWithGroups(Long assignmentId) {
 		if (assignmentId == null) {
     		throw new IllegalArgumentException("Null assignmentId passed to getAssignmentByIdWithGroups");
     	}
     	Query query = getSession().getNamedQuery("findAssignmentByIdWithGroups");
     	query.setParameter("assignmentId",assignmentId);
     	
     	return (Assignment2) query.uniqueResult();
 	}
 	
 	
 	// Assignment Submissions DAO
     
     public List<AssignmentSubmission> findCurrentSubmissionsForAssignment(Assignment2 assignment) {
     	if (assignment == null) {
     		throw new IllegalArgumentException("Null assignment passed to findCurrentSubmissionsForAssignment");
     	}
     	Query query = getSession().getNamedQuery("findCurrentSubmissionsForAssignment");
     	query.setParameter("assignment", assignment);
     	
     	return query.list();
     }
     
     public AssignmentSubmissionVersion getCurrentSubmissionVersionWithAttachments(AssignmentSubmission submission, boolean ignoreDrafts) {
     	if (submission == null) {
     		throw new IllegalArgumentException("null submission passed to getSubmissionVersionForUserIdWithAttachments");
     	}
     	
     	AssignmentSubmissionVersion currentVersion = null;
     	
     	String hqlGetVersionNoDraft = "select max(submissionVersion.submissionVersionId) " +
 		"from AssignmentSubmissionVersion as submissionVersion " +
 		"where submissionVersion.assignmentSubmission = :submission " +
 		"and submissionVersion.draft = false";
 	
     	String hqlGetVersionWithDraft = "select max(submissionVersion.submissionVersionId) " +
 		"from AssignmentSubmissionVersion as submissionVersion " +
 		"where submissionVersion.assignmentSubmission = :submission";
     	
     	String queryToUse = ignoreDrafts ? hqlGetVersionNoDraft : hqlGetVersionWithDraft;
     	
     	Query query = getSession().createQuery(queryToUse);
     	query.setParameter("submission", submission);
     	
     	Long submissionVersionId = (Long) query.uniqueResult();
     	
     	if (submissionVersionId != null) {
     		currentVersion = getAssignmentSubmissionVersionByIdWithAttachments(submissionVersionId);
     	}
     	
     	return currentVersion;
     }
     
    private List<Long> getCurrentVersionIdsForSubmissions(List<AssignmentSubmission> submissionList, boolean ignoreDrafts) {    	
     	List<Long> versionIdList = new ArrayList();
 
     	if (submissionList != null && !submissionList.isEmpty()) {
     		
    		Query query;
    		if (ignoreDrafts) {
    			query = getSession().getNamedQuery("findCurrentVersionIdsWithoutDrafts");
    		} else {
    			query = getSession().getNamedQuery("findCurrentVersionIdsWithoutDrafts");
    		}
 
     		// TODO if submission list is > than the max length allowed in sql, we need
     		// to cycle through the list
 
     		query.setParameterList("submissionList", submissionList);
 
     		versionIdList = query.list();
     	}
 
     	return versionIdList;
     }
     
    public List <AssignmentSubmission> getAssignmentSubmissionsWithCurrentVersionDataNoAttach(List<Long> submissionIdList, boolean includeDraft) {
    	return null;
    }
    
     public List<AssignmentSubmission> getCurrentAssignmentSubmissionsForStudent(List<Assignment2> assignments, String studentId) {
 		if (studentId == null) {
 			throw new IllegalArgumentException("null studentId passed to getAllSubmissionRecsForStudentWithVersionData");
 		}
 		
 		List<AssignmentSubmission> submissions = new ArrayList();
 		
 		if (assignments != null && !assignments.isEmpty()) {
 			// retrieve the submissions
 			Query query = getSession().getNamedQuery("findSubmissionsForStudentForAssignments");
 	    	query.setParameter("studentId",studentId);
 	    	query.setParameterList("assignmentList", assignments);
 	    	
 	    	submissions = query.list();
 	    	
 	    	// now, populate the version information
 	    	
 			if (submissions != null && !submissions.isEmpty()) {
 				// then, we will populate the version data
 				
 				// first, retrieve the ids of the current versions
				List<Long> versionIds = getCurrentVersionIdsForSubmissions(submissions, Boolean.FALSE);
 				
 				// now retrieve the associated AssignmentSubmissionVersion recs
 				List<AssignmentSubmissionVersion> currentVersions = getAssignmentSubmissionVersionsById(versionIds);
 				
 				if (currentVersions != null) {
 					Map submissionIdVersionMap = new HashMap();
 					for (Iterator versionIter = currentVersions.iterator(); versionIter.hasNext();) {
 						AssignmentSubmissionVersion version = (AssignmentSubmissionVersion) versionIter.next();
 						if (version != null) {
 							submissionIdVersionMap.put(version.getAssignmentSubmission().getSubmissionId(), version);
 						}
 					}
 					
 					for (Iterator submissionIter = submissions.iterator(); submissionIter.hasNext();) {
 						AssignmentSubmission submission = (AssignmentSubmission) submissionIter.next();
 						if (submission != null) {
 							AssignmentSubmissionVersion currVersion = 
 								(AssignmentSubmissionVersion)submissionIdVersionMap.get(submission.getSubmissionId());
 							if (currVersion != null) {
 								submission.setCurrentSubmissionVersion(currVersion);
 							}
 						}
 					}
 				}
 				
 			}
 		}
 		
 		return submissions;
     }
     
     private AssignmentSubmissionVersion getAssignmentSubmissionVersionByIdWithAttachments(Long submissionVersionId) {
     	if (submissionVersionId == null) {
     		throw new IllegalArgumentException("Null submissionVersionId passed to getAssignmentSubmissionVersionByIdWithAttachments");
     	}
     	
     	Query query = getSession().getNamedQuery("findSubmissionVersionByIdWithAttachments");
     	query.setParameter("submissionVersionId", submissionVersionId);
     	
     	return (AssignmentSubmissionVersion) query.uniqueResult();
     }
     
     private List<AssignmentSubmissionVersion> getAssignmentSubmissionVersionsById(List<Long> versionIds) {
     	List<AssignmentSubmissionVersion> versions = new ArrayList();
     	
     	if (versionIds != null && !versionIds.isEmpty()) {
     		String hql = "from AssignmentSubmissionVersion as version where version.submissionVersionId in (:versionIdList)";
     		Query query = getSession().createQuery(hql);
     		query.setParameterList("versionIdList", versionIds);
     		
     		versions = query.list();
     	}
     	
     	return versions;
     }
 
 }
