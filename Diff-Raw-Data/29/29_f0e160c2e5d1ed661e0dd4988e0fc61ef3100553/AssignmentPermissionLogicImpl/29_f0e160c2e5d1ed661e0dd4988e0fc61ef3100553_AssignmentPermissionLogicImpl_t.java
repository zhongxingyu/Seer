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
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.exception.SubmissionNotFoundException;
 import org.sakaiproject.assignment2.logic.AssignmentAuthzLogic;
 import org.sakaiproject.assignment2.logic.AssignmentBundleLogic;
 import org.sakaiproject.assignment2.logic.AssignmentPermissionLogic;
 import org.sakaiproject.assignment2.logic.ExternalLogic;
 import org.sakaiproject.assignment2.logic.ExternalTaggableLogic;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.AssignmentSubmission;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.site.api.Group;
 import org.sakaiproject.util.ResourceLoader;
 
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
     
     private AssignmentAuthzLogic authz;
     public void setAssignmentAuthzLogic(AssignmentAuthzLogic authz) {
         this.authz = authz;
     }
     
     private ExternalTaggableLogic taggableLogic;
     public void setExternalTaggableLogic(ExternalTaggableLogic taggableLogic) {
         this.taggableLogic = taggableLogic;
     }
     
     public boolean isUserAllowedForAllGroups(String userId, String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToEditAllAssignments");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return authz.userHasAllGroupsPermission(userId, contextId);
     }
     
     public boolean isUserAllowedToEditAllAssignments(String userId, String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToEditAllAssignments");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return authz.userHasEditPermission(userId, contextId) && 
         authz.userHasAllGroupsPermission(userId, contextId);
     }
     
     public boolean isUserAllowedToEditAssignment(String userId, Assignment2 assignment, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment or contextId passed to isUserAllowedToEditAssignment");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionOnAssignment(userId, assignment, AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS, null, authzPermissions);
     }
     
     public boolean isUserAllowedToAddAssignments(String userId, String contextId, List<String> groupMemberships) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToAddAssignments");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionInSite(userId, contextId, AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS, groupMemberships);
     }
     
     public boolean isUserAllowedToAddAssignment(String userId, Assignment2 assignment, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment passed to isUserAllowedToAddAssignment");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionOnAssignment(userId, assignment, 
                 AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS, null, authzPermissions);
     }
     
     public boolean isUserAllowedToDeleteAssignments(String userId, String contextId, List<String> groupMembershipIds) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToDeleteAssignments");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionInSite(userId, contextId, AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS, groupMembershipIds);
     }
     
     public boolean isUserAllowedToDeleteAssignment(String userId, Assignment2 assignment, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment passed to isUserAllowedToDeleteAssignment");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionOnAssignment(userId, assignment, AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS, null, authzPermissions);
     }
     
     public boolean isUserAllowedToManageAllSubmissions(String userId, String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToManageAllSubmissions");
         }
         
         return authz.userHasManageSubmissionsPermission(userId, contextId) && 
         authz.userHasAllGroupsPermission(userId, contextId);
     }
     
     public boolean isUserAllowedToManageSubmissions(String userId, String contextId, List<String> groupMembershipIds) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToManageAllSubmissions");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionInSite(userId, contextId, AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS, groupMembershipIds);
     }
     
     public boolean isUserAllowedToManageSubmissionsForAssignment(String userId, Assignment2 assignment, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment passed to isUserAllowedToManageSubmissionsForAssignment");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionOnAssignment(userId, assignment, 
                 AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS, groupMembershipIds, authzPermissions);
     }
     
     public boolean isUserAllowedToViewAssignment(String userId, Assignment2 assignment, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions, Map<String, Object> optionalParameters) {
         if (assignment == null) {
             throw new IllegalArgumentException("Null assignment passed to isUserAllowedToViewAssignment");
         }
         
         boolean allowed = false;
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         // viewing an assignment is a little more complicated because you may view the assignment
         // if you have view, submit, manage submissions, delete, or edit perm for it
         boolean hasViewPerm =  isUserAllowedToTakeActionOnAssignment(userId, assignment, 
                 AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS, groupMembershipIds, authzPermissions);
         if (hasViewPerm) {
             allowed = true;
         } else if (isUserAllowedToManageSubmissionsForAssignment(userId, assignment, groupMembershipIds, authzPermissions)) {
             allowed = true;
         } else if (isUserAllowedToEditAssignment(userId, assignment, groupMembershipIds, authzPermissions)) {
             allowed = true;
         } else if (isUserAllowedToTakeActionOnAssignment(userId, assignment, AssignmentConstants.PERMISSION_SUBMIT, groupMembershipIds, authzPermissions)) {
             // student has submission privileges for this assignment.
             // BUT this does not indicate that submission is actually open.
             allowed = true;
         } else if (isUserAllowedToDeleteAssignment(userId, assignment, groupMembershipIds, authzPermissions)){
             allowed = true;
         } else {
             allowed = false;
         }
         
         // if still not allowed, let's check for the optional parameters that may
         // override our default permission questions
         if (!allowed) {
             if (optionalParameters != null) {
                 // check for a tag
                 String tagReference = (String)optionalParameters.get(AssignmentConstants.TAGGABLE_REF_KEY);
                 if (tagReference != null) {
                     if (taggableLogic.isTaggable() && taggableLogic.isSiteAssociated(assignment.getContextId())) {
                         allowed = taggableLogic.canGetActivity(assignment.getReference(), userId, tagReference);
                     }
                 }
             }
         }
         
         return allowed;
     }
     
 
     public boolean isUserAllowedToViewAssignment(Long assignmentId, Map<String, Object> optionalParameters) {
         if (assignmentId == null) {
             throw new IllegalArgumentException("Null assignmentId passed to isUserAbleToViewAssignment");
         }
 
         // retrieve the assignment
         Assignment2 assign = dao.getAssignmentByIdWithGroups(assignmentId);
         if (assign == null) {
             throw new AssignmentNotFoundException("No assignment found with id " + assignmentId);
         }
 
         return isUserAllowedToViewAssignment(null, assign, null, null, optionalParameters);
     }
     
     public Map<String, Boolean> getPermissionsForSite(String contextId, List<String> permissions) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to getPermissionsForSite");
         }
         
         Map<String, Boolean> sitePerms = new HashMap<String, Boolean>();
         
         List<String> allSitePerms = authz.getSiteLevelPermissions();
         
         // if passed permissions list is null, default to return all site-level permissions
         if (permissions == null) {
             permissions = allSitePerms;
         }
         
         if (permissions != null) {
             String currUserId = externalLogic.getCurrentUserId();
             
             List<String> groupMembershipIds = new ArrayList<String>();
             if (!authz.userHasAllGroupsPermission(currUserId, contextId)) {
                 groupMembershipIds = externalLogic.getUserMembershipGroupIdList(currUserId, contextId);
             }
             
             for (String permission : permissions) {
                 if (allSitePerms.contains(permission)) {
                     boolean hasPerm;
                     if (permission.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                         hasPerm = authz.userHasAllGroupsPermission(currUserId, contextId);
                     } else {
                         hasPerm = isUserAllowedToTakeActionInSite(currUserId, contextId, permission, groupMembershipIds);
                     }
                     
                     sitePerms.put(permission, hasPerm);
                 }
             }
         }
         
         return sitePerms;
     }
     
     public Map<Long, Map<String, Boolean>> getPermissionsForAssignments(Collection<Assignment2> assignments, Collection<String> permissions) {
         
         Map<Long, Map<String, Boolean>> permissionMap = new HashMap<Long, Map<String,Boolean>>();
         
         if (assignments != null && !assignments.isEmpty()) {
             String currUserId = externalLogic.getCurrentUserId();
             String contextId = ((List<Assignment2>)assignments).get(0).getContextId();
             List<String> groupMembershipIds = externalLogic.getUserMembershipGroupIdList(currUserId, contextId);
 
             // if permissions is null, we return all assignment-level permissions
             List<String> assignPermissions = authz.getAssignmentLevelPermissions();
             if (permissions == null) {
                 permissions = assignPermissions;
             }
             
             // to save on repeated calls to the AssignmentAuthzLogic, let's make a map
             // of this user's assignment permissions
             Map<String, Boolean> authzPermMap = new HashMap<String, Boolean>();
             for (String permission : assignPermissions) {
                 boolean hasPerm = authz.userHasPermission(contextId, permission);
                 authzPermMap.put(permission, hasPerm);
             }
             
             // look up the allgroups perm too
             authzPermMap.put(AssignmentConstants.PERMISSION_ALL_GROUPS, 
                     authz.userHasAllGroupsPermission(currUserId, contextId));
             
             // now let's find the perms for the individual assignments
             for (Assignment2 assign : assignments) {
                 Map<String, Boolean> assignPerms = new HashMap<String, Boolean>();
                 for (String permission : permissions) {
                     if (permission != null) {
                         if (assignPermissions.contains(permission)) {
                             // we are checking perm for this assignment
                             boolean hasPermission = false;
                             
                             // we need to determine if the user has this permission
                             // generally. if they don't, we won't proceed. if they do
                             // have permission, we don't need to check it later
                             boolean authzPermission = authzPermMap.get(permission);
 
                             
                             if (authzPermission) {
                                 // the ability to view an assignment isn't straightforward, so call the specific method
                                 if (permission.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                                     hasPermission = isUserAllowedToViewAssignment(currUserId, assign, groupMembershipIds, authzPermMap, null);
                                 } else {
                                     hasPermission = isUserAllowedToTakeActionOnAssignment(currUserId, assign, 
                                             permission, groupMembershipIds, authzPermMap);
                                 }
                             }
                             assignPerms.put(permission, hasPermission);
                         }
                     }
                 }
                 
                 permissionMap.put(assign.getId(), assignPerms);
             }
         }
         
         return permissionMap;
     }
     
     private boolean isUserAllowedToTakeActionInSite(String userId, String contextId, 
             String permission, List<String> groupMembershipIds) {
         if (permission == null || contextId == null) {
             throw new IllegalArgumentException("Null contextId or permission passed to " +
                     "isUserAllowedToTakeActionInSite. contextId:" + contextId + " permission:" + permission);
         }
         boolean allowed = false;
 
         // depending on the permission, the user may need to be a member of at least one
         // group to have general permission in this site.  for example, a user only has
         // "add assignments" privileges if he/she has "all groups" or is a member of at
         // least one group
 
         if (authz.userHasPermission(userId, contextId, permission)) {
             // in some scenarios, the user's group membership will come into play
             if (authz.getPermissionsThatRequireAllGroups().contains(permission) ||
                     authz.getPermissionsThatRequireOneGroup().contains(permission)) {
                 if (authz.userHasAllGroupsPermission(userId, contextId)) {
                     allowed = true;
                 } else {
                     if (groupMembershipIds == null) {
                         groupMembershipIds = externalLogic.getUserMembershipGroupIdList(userId, contextId);
                     }
 
                     if (groupMembershipIds != null && !groupMembershipIds.isEmpty()) {
                         allowed = true;
                     }
                 }
             } else if (authz.getPermissionsForAtLeastOneOrNoGroups().contains(permission)) {
                 allowed = true;
             }
         }
 
         return allowed; 
     }
     
     /**
      * 
      * @param userId
      * @param assignment
      * @param permission the realm permission that you are checking for this assignment. ie {@link AssignmentConstants#PERMISSION_EDIT_ASSIGNMENTS}
      * @param groupMembershipIds if null, will assume this method needs to look them up. pass in an
      * empty list to avoid this call.
      * @param authzPermissions optional - if you have already looked up the authz permissions
      * for this user, you can optionally pass them here. this will avoid extra look-ups
      * @return true if the user has permission to take the given action (described by
      * the permission parameter) for the given assignment. If the user does have permission,
      * will also ensure the user has this permission for all groups. If not, will check to
      * see if user is a member of a restricted group
      */
     private boolean isUserAllowedToTakeActionOnAssignment(String userId, Assignment2 assignment, 
             String permission, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions) {
         if (assignment == null || permission == null) {
             throw new IllegalArgumentException("Null assignment or permission passed to " +
                     "isUserAllowedToTakeActionOnAssignment. assignment:" + " permission:" + permission);
         }
         
         // only users with edit permission may view drafts
         if (assignment.isDraft()) {
             if (!permission.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 return false;
             }
         }
         
         // the submit and view permission can only see assignments if they are open
         if (assignment.getOpenDate().after(new Date())) {
             if (permission.equals(AssignmentConstants.PERMISSION_SUBMIT) ||
                     permission.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                 return false;
             } 
         }
         
         // only the submit permission may view removed assignments
         if (assignment.isRemoved()) {
             if (!permission.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 return false;
             }
         }
         
         boolean allowed = false;
 
         boolean userHasPermission;
         // first, let's check to see if the caller already provided this information to avoid
         // extra calls to retrieve this information
         if (authzPermissions != null && authzPermissions.containsKey(permission)) {
             userHasPermission = authzPermissions.get(permission);
         } else {
             userHasPermission = authz.userHasPermission(userId, assignment.getContextId(), permission);
         }
 
         if (userHasPermission) {
             // we need to see if this permission applies given the group restrictions and all groups permission
             if (userHasGroupPermission(userId, permission, assignment, groupMembershipIds, authzPermissions)) {
                 // now we just need to check that if the assignment was removed,
                 // the user can only see if if he/she has a submission
                 if (assignment.isRemoved()) {
                     int numSubmissions = dao.getNumSubmittedVersions(userId, assignment.getId());
                     if (numSubmissions > 0) {
                         allowed = true;
                     } else {
                         allowed = false;
                     }
                 } else {
                     allowed = true;
                 }
             }
         }
 
         return allowed;
     }
 
     
     /**
      * 
      * @param userId
      * @param permission
      * @param assignment
      * @param groupMembershipIds leave null if you want this method to retrieve the group memberships for this user for you
      * @param authzPermissions optional - if you have already looked up the authz permissions
      * for this user, you can optionally pass them here. this will avoid extra look-ups
      * @return true if the user has group permission for the given assignment. the
      * permissions grant different privileges based upon whether or not the user
      * has the {@link AssignmentConstants#PERMISSION_ALL_GROUPS} permission and
      * the group restrictions that may or may not exist for the given assignment.
      * This method will figure that out for you.
      */
     private boolean userHasGroupPermission(String userId, String permission, Assignment2 assignment, List<String> groupMembershipIds, Map<String, Boolean> authzPermissions) {
         boolean hasPermissionForGroup = false;
         
         boolean userHasAllGroups;
         // first, check to see if this has already been retrieved in the authzPermissions map
         if (authzPermissions != null && authzPermissions.containsKey(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
             userHasAllGroups = authzPermissions.get(AssignmentConstants.PERMISSION_ALL_GROUPS);
         } else {
             userHasAllGroups = authz.userHasAllGroupsPermission(userId, assignment.getContextId());
         }
         
         if (userHasAllGroups) {
             hasPermissionForGroup = true;
         } else if (authz.getPermissionsForAtLeastOneOrNoGroups().contains(permission) &&
                 (assignment.getAssignmentGroupSet() == null || assignment.getAssignmentGroupSet().isEmpty())) {
             // user has permission if there are no groups or they are a member of at least one group
             hasPermissionForGroup = true;
             
         } else if (assignment.getAssignmentGroupSet() != null && !assignment.getAssignmentGroupSet().isEmpty()){
             // if this assignment is restricted to groups, you must be a member of the
             // group to take action on it
             if (groupMembershipIds == null) {
                 groupMembershipIds = externalLogic.getUserMembershipGroupIdList(userId, assignment.getContextId());
             }
             
             if (authz.getPermissionsThatRequireOneGroup().contains(permission) ||
                     authz.getPermissionsForAtLeastOneOrNoGroups().contains(permission)) {
                 // the user only needs to be a member of one of the groups
                 hasPermissionForGroup = isUserAMemberOfARestrictedGroup(groupMembershipIds, assignment.getAssignmentGroupSet());
             
             } else if (authz.getPermissionsThatRequireAllGroups().contains(permission)){
                 // you must be a member of every associated group
                 boolean memberOfAllGroups = true;
                 for (AssignmentGroup assignGroup : assignment.getAssignmentGroupSet()) {
                     if (!groupMembershipIds.contains(assignGroup.getGroupId())) {
                         memberOfAllGroups = false;
                     }
                 }
                 
                 if (memberOfAllGroups) {
                     hasPermissionForGroup = true;
                 }
             } else {
                 // unknown permission if we get to this point, so return false
                 hasPermissionForGroup = false;
             }
         } else {
             // unknown scenario so return false
             hasPermissionForGroup = false;
         }
         
         return hasPermissionForGroup;
     }
     
     public boolean isUserAllowedToManageSubmission(String userId, String studentId, Assignment2 assignment) {
         if (studentId == null || assignment == null) {
             throw new IllegalArgumentException("Null contextId, studentId or assignmentId passed to" +
                     " isUserAllowedToViewSubmissionForAssignment. studentId:" + studentId + " assignment:" + assignment);
         }
         
         boolean allowed = false;
         
        if (userId == null) {
            userId = externalLogic.getCurrentUserId();
        }
        
         if (isUserAllowedToManageAllSubmissions(userId, assignment.getContextId())) {
             allowed = true;
         } else {
            List<String> currentUserMemberships = externalLogic.getUserMembershipGroupIdList(userId, assignment.getContextId());
             if (isUserAllowedToManageSubmissionsForAssignment(userId, assignment, currentUserMemberships, null)) {
                 // double check that the student is in one of the curr user's groups
                 List<String> studentMemberships = externalLogic.getUserMembershipGroupIdList(studentId, assignment.getContextId());
                 if (listMembershipsOverlap(currentUserMemberships, studentMemberships)) {
                     allowed = true;
                 } else {
                     allowed = false;
                 }
             } else {
                 // user does not have permission to manage submissions for this assignment
                 allowed = false;
             }
         }
 
         return allowed;
     }
 
     public boolean isUserAllowedToViewSubmissionForAssignment(String userId, String studentId, Long assignmentId, Map<String, Object> optionalParameters) {
         if (studentId == null || assignmentId == null) {
             throw new IllegalArgumentException("Null studentId or assignmentId passed to isUserAbleToViewStudentSubmissionForAssignment");
         }
 
         boolean viewable = false;
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
 
         Assignment2 assign = null;
         
         if (userId.equals(studentId)) {
             viewable = true;
         } else {
             // we need to make sure this assignment wasn't deleted
             assign = dao.getAssignmentByIdWithGroups(assignmentId);
             viewable = isUserAllowedToManageSubmission(userId, studentId, assign);
         }
         
         // if still not viewable, check the optionalParameters to see if this
         // is a special situation with expanded permissions
         if (!viewable && optionalParameters != null) {
             // check for a tag
             String tagReference = (String) optionalParameters.get(AssignmentConstants.TAGGABLE_REF_KEY);
             if (tagReference != null) {
                 if (taggableLogic.isTaggable()) {
                     if (assign == null) {
                         assign = dao.getAssignmentByIdWithGroups(assignmentId);
                     }
                     if (taggableLogic.isSiteAssociated(assign.getContextId())) {
                         //Get the submission for the reference
                         String submissionRef = dao.getSubmissionReference(studentId, assign);
                         viewable = taggableLogic.canGetItem(assign.getReference(), submissionRef, userId, tagReference);
                     }
                 }
             }
         }
 
         return viewable;	
     }
 
     public boolean isUserAllowedToManageSubmission(String userId, Long submissionId) {
         if (submissionId == null) {
             throw new IllegalArgumentException("Null submissionId passed to isUserAbleToProvideFeedbackForSubmission");
         }
 
         AssignmentSubmission submission = (AssignmentSubmission)dao.findById(AssignmentSubmission.class, submissionId);
         if (submission == null) {
             throw new SubmissionNotFoundException("No submission exists with id: " + submissionId);
         }
 
         return isUserAllowedToManageSubmission(userId, submission.getUserId(), submission.getAssignment());
     }
     
     public boolean isUserAllowedToSubmit(String userId, String contextId, List<String> groupMembershipIds) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToSubmit");
         }
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
         
         return isUserAllowedToTakeActionInSite(userId, contextId, AssignmentConstants.PERMISSION_SUBMIT, groupMembershipIds);
     }
 
 
     public List<Assignment2> filterViewableAssignments(String contextId, Collection<Assignment2> assignmentList) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to filterViewableAssignments");
         }
 
         List<Assignment2> filteredAssignments = new ArrayList<Assignment2>();
         if (assignmentList != null && !assignmentList.isEmpty()) {
 
             for (Assignment2 assign : assignmentList) {               
                 // first, make sure all of these assignments belong to the given contextId
                 if (!assign.getContextId().equals(contextId)) {
                     throw new IllegalArgumentException("User attempted to filterViewableAssignments " +
                             "but assignmentList contained assignment with an associated contextId that " +
                             "did not match the contextIdParameter. contextId: " + contextId + 
                             " assignment.getContextId:" + assign.getContextId());
                 }      
             }
             
             List<String> permList = new ArrayList<String>();
             permList.add(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS);
             Map<Long, Map<String, Boolean>> assignPermMap = getPermissionsForAssignments(assignmentList, permList);
 
             for (Assignment2 assign : assignmentList) {
                 Map<String, Boolean> permMap = assignPermMap.get(assign.getId());
                 if (permMap != null && permMap.containsKey(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                     boolean hasViewPerm = permMap.get(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS);
                     if (hasViewPerm) {
                         filteredAssignments.add(assign);
                     }
                 }
             }
             
         }
 
         return filteredAssignments;
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
 
     public boolean isUserAllowedToAccessInstructorView(String userId, String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("null contextId passed to isUserAbleToAccessInstructorView");
         }
 
         boolean instructorView = false;
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
 
         if (authz.userHasEditPermission(userId, contextId) || 
                 authz.userHasAddPermission(userId, contextId) ||
                 authz.userHasDeletePermission(userId, contextId) || 
                 authz.userHasManageSubmissionsPermission(userId, contextId)) {
             instructorView = true;
         }
 
         return instructorView;
     }
 
     public boolean isUserAllowedToMakeSubmissionForAssignment(String userId, Assignment2 assignment) {
         if (assignment == null) {
             throw new IllegalArgumentException("null assignment passed to isUserAbleToMakeSubmission");
         }
        
        if (userId == null) {
            userId = externalLogic.getCurrentUserId();
        }
 
        return isUserAllowedToTakeActionOnAssignment(userId, assignment, AssignmentConstants.PERMISSION_SUBMIT, null, null);
     }
     
     public boolean isUserAllowedToMakeSubmissionForAssignmentId(String userId, Long assignmentId) {
         if (assignmentId == null) {
             throw new IllegalArgumentException("Null assignmentId passed to isUserAllowedToMakeSubmissionForAssignment");
         }
         
         Assignment2 assign = dao.getAssignmentByIdWithGroups(assignmentId);
         return isUserAllowedToMakeSubmissionForAssignment(userId, assign);
     }
 
     public List<String> getViewableStudentsForAssignment(String userId, Assignment2 assignment) {
         if (assignment == null) {
             throw new IllegalArgumentException("null assignment passed to getViewableStudentsForAssignment");
         }
 
         List<Assignment2> assignmentList = new ArrayList<Assignment2>();
         assignmentList.add(assignment);
 
         Map<Assignment2, List<String>> assignIdAvailStudentsMap = getViewableStudentsForAssignments(userId, assignment.getContextId(), assignmentList);
 
         List<String> availStudents = assignIdAvailStudentsMap.get(assignment);
 
         return availStudents;
     }
 
     public Map<Assignment2, List<String>> getViewableStudentsForAssignments(String userId, String contextId, List<Assignment2> assignmentList) {
         if (contextId == null) {
             throw new IllegalArgumentException("null contextId passed to getAvailableStudentsForUserForItem.");
         }
 
         Map<Assignment2, List<String>> assignToAvailStudentsMap = new HashMap<Assignment2, List<String>>();
         
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
 
         if (assignmentList != null && !assignmentList.isEmpty()) {
             
             if (!authz.userHasManageSubmissionsPermission(userId, contextId)) {
                 // user can't view any students, so let's send them back with empty lists
                 for (Assignment2 assign : assignmentList) {
                     assignToAvailStudentsMap.put(assign, new ArrayList<String>());
                 }
                 
             } else {
                 // first, we will retrieve all of the users with submission privileges in this site
                 Set<String> usersWithSubmitPerm = getSubmittersInSite(contextId);
 
                 if (usersWithSubmitPerm != null && !usersWithSubmitPerm.isEmpty()) {
 
                     boolean userMayManageAll = isUserAllowedToManageAllSubmissions(userId, contextId);
 
                     List<String> userGroupMemberships = new ArrayList<String>();
                     // if the user does not have the "manage all" perm, we are going
                     // to need their group memberships
                     if (!userMayManageAll) {
                         userGroupMemberships = externalLogic.getUserMembershipGroupIdList(userId, contextId);
                     }
 
                     // once we retrieve the group membership, put it in a map so we can reuse it
                     Map<String, List<String>> groupToMembershipMap = new HashMap<String, List<String>>();
                     
                     // let's retrieve the general authz permissions for this user for re-use
                     Map<String, Boolean> authzPermMap = new HashMap<String, Boolean>();
                     authzPermMap.put(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS, authz.userHasManageSubmissionsPermission(userId, contextId));
                     authzPermMap.put(AssignmentConstants.PERMISSION_ALL_GROUPS, authz.userHasAllGroupsPermission(userId, contextId));
 
                     for (Assignment2 assign : assignmentList) {
                         List<String> availStudents = new ArrayList<String>();
 
                         if (!assign.isRemoved()) {
                             if (userMayManageAll) {
                                 // we need to filter the available submitters based
                                 // on group restrictions, if applicable
                                 List<String> filteredSubmitters = filterSubmittersGivenGroupRestrictions(
                                         contextId, usersWithSubmitPerm, assign.getAssignmentGroupSet(), groupToMembershipMap);
                                 if (filteredSubmitters != null) {
                                     availStudents.addAll(filteredSubmitters);
                                 }
 
                             } else {
                                 // user doesn't have manage all perm
                                 if (isUserAllowedToManageSubmissionsForAssignment(userId, assign, userGroupMemberships, authzPermMap)) {
                                     // we need to filter the available submitters based
                                     // on group restrictions, if applicable
                                     List<String> filteredSubmitters = filterSubmittersGivenGroupRestrictions(
                                             contextId, usersWithSubmitPerm, assign.getAssignmentGroupSet(), groupToMembershipMap);
                                     if (filteredSubmitters != null) {
                                         // start with the assignment groups that the user is a member of
                                         Set<String> groupsToCheck = getSharedListMembers(
                                                 assign.getListOfAssociatedGroupReferences(), userGroupMemberships);
 
                                         // now retrieve all of the users in those groups
                                         Set<String> usersInGroups = getUsersInGroups(contextId, groupsToCheck, groupToMembershipMap);
 
                                         // now let's filter that based upon our filteredSubmitters
                                         Set<String> viewableStudents = getSharedListMembers(usersInGroups, filteredSubmitters);
                                         availStudents.addAll(viewableStudents);
                                     }
                                 }
 
                             }
                         }
 
                         assignToAvailStudentsMap.put(assign, availStudents);
                     }
                 }
             }
         }
 
         return assignToAvailStudentsMap;
     }
     
  
 
     /**
      * Given 2 lists, will return true if any of the elements in the two lists overlap
      * @param list1
      * @param list2
      * @return
      */
     private boolean listMembershipsOverlap(List<String> list1, List<String> list2) {
         if (list1 != null && list2 != null ) {
             for (String element : list1) {
                 if (element != null && list2.contains(element)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     private Set<String> getUsersInGroups(String contextId, Collection<String> groupIds, Map<String, List<String>> groupMembershipMap) {
         // use a set to eliminate group overlap with duplicate users
         Set<String> sharedMembers = new HashSet<String>();
         if (groupIds != null) {
             for (String groupId : groupIds) {
                 if (groupId != null) {
                     List<String> members;
                     if (groupMembershipMap.containsKey(groupId)) {
                         members = groupMembershipMap.get(groupId);
                     } else {
                         members = externalLogic.getUsersInGroup(contextId, groupId);
                     }
                     
                     sharedMembers.addAll(members);
                 }
             }
         }
 
         return sharedMembers;
     }
     
     private Set<String> getSharedListMembers(Collection<String> list1, Collection<String> list2) {
         // use a set to get rid of duplicates
         Set<String> sharedMembers = new HashSet<String>();
         if (list1 != null && list2 != null) {
             for (String member : list1) {
                 if (list2.contains(member)) {
                     sharedMembers.add(member);
                 }
             }
         }
         
         return sharedMembers;
     }
 
     /**
      * 
      * @param siteSubmitters userIds of submitters in the site
      * @param assignGroupRestrictions {@link AssignmentGroup} group restrictions for the assignment
      * @param groupIdMembershipMap a map to cut down on calls to retrieve memberships.
      * will check here first to see if we already have found the group membership
      * @return a list of userIds for users who have access to the assignment based upon
      * the given assignGroupRestrictions
      */
     private List<String> filterSubmittersGivenGroupRestrictions(String contextId, Collection<String> siteSubmitters, 
             Collection<AssignmentGroup> assignGroupRestrictions, Map<String, List<String>> groupIdMembershipMap) {
         List<String> assignmentSubmitters = new ArrayList<String>();
 
         // if there are group restrictions, only a subset of all of the submitters in the
         // site will be available for this assignment
         if (assignGroupRestrictions == null || assignGroupRestrictions.isEmpty() && siteSubmitters != null) {
             assignmentSubmitters.addAll(siteSubmitters);
         } else {
             // use a Set to make sure users only appear once, even if they
             // are in multiple groups
             Set<String> groupUsers = new HashSet<String>();
             
             for (AssignmentGroup group : assignGroupRestrictions) {
                 List<String> groupMembers;
                 if (groupIdMembershipMap.containsKey(group.getGroupId())) {
                     groupMembers = groupIdMembershipMap.get(group.getGroupId());
                 } else {
                     groupMembers = externalLogic.getUsersInGroup(contextId, group.getGroupId());
                     groupIdMembershipMap.put(group.getGroupId(), groupMembers);
                 }
                 if (groupMembers != null) {
                     groupUsers.addAll(groupMembers);
                 }
             }
 
             assignmentSubmitters.addAll(getSharedListMembers(groupUsers, siteSubmitters));
         }
 
         return assignmentSubmitters;
     }
 
     public List<String> getUsersAllowedToViewStudentForAssignment(String studentId, Assignment2 assignment) {
         if (studentId == null || assignment == null) {
             throw new IllegalArgumentException("Null studentId or assignmentId passed to getUsersAllowedToViewSubmission");
         }
 
         List<String> usersAllowedToViewStudent = new ArrayList<String>();
 
         // identify all of the users who are able to manage submissions
         Set<String> submissionManagers = authz.getUsersWithPermission(assignment.getContextId(), AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS);
 
         for (String userId : submissionManagers) {
             List<String> viewableStudents = getViewableStudentsForAssignment(userId, assignment);
             if (viewableStudents != null && viewableStudents.contains(studentId)) {
                 usersAllowedToViewStudent.add(userId);
             }
         }
 
         return usersAllowedToViewStudent;
     }
 
     public List<Group> getViewableGroupsForAssignment(String userId, Assignment2 assign) {
         if (assign == null) {
             throw new IllegalArgumentException("Null assignmentId passed to getViewableGroupsForAssignment");
         }
 
         if (userId == null) {
             userId = externalLogic.getCurrentUserId();
         }
 
         List<Group> viewableGroups = new ArrayList<Group>();
         
         Collection<Group> allGroupsInSite = externalLogic.getSiteGroups(assign.getContextId());
         Map<String, Group> siteGroupIdGroupMap = new HashMap<String, Group>();
         if (allGroupsInSite != null) {
             for (Group group : allGroupsInSite) {
                 siteGroupIdGroupMap.put(group.getId(), group);
             }
 
             List<String> userGroupIds = new ArrayList<String>();
             // the user may view all groups
             if (authz.userHasAllGroupsPermission(userId, assign.getContextId())) {
                 userGroupIds.addAll(siteGroupIdGroupMap.keySet());
             } else {
                 // the user may only view his/her own groups
                 userGroupIds.addAll(externalLogic.getUserMembershipGroupIdList(userId, assign.getContextId()));
             }
 
             // now let's filter these if assignment is restricted to groups
             if (assign.getAssignmentGroupSet() == null || assign.getAssignmentGroupSet().isEmpty()) {
                 for (String groupId : userGroupIds) {
                     viewableGroups.add(siteGroupIdGroupMap.get(groupId));
                 }
             } else {
                 for (AssignmentGroup assignGroup : assign.getAssignmentGroupSet()) {
                     if (userGroupIds.contains(assignGroup.getGroupId())) {
                         viewableGroups.add(siteGroupIdGroupMap.get(assignGroup.getGroupId()));
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
             String currUserId = externalLogic.getCurrentUserId();
             List<String> gradableStudents = getViewableStudentsForAssignment(currUserId, assignment);
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
     
     public Set<String> getSubmittersInSite(String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to getSubmittersInSite");
         }
         
         return authz.getUsersWithPermission(contextId, AssignmentConstants.PERMISSION_SUBMIT);
     }
     
     public boolean isUserAllowedToUpdateSite(String contextId) {
         if (contextId == null) {
             throw new IllegalArgumentException("Null contextId passed to isUserAllowedToUpdateSite");
         }
         
         return authz.userHasPermission(contextId, "site.upd");
     }
     
     public List<String> getAssignment2PermissionFunctions() {
         return authz.getAllPermissions();
     }
     
     public Map<String, String> getAssignment2PermissionDescriptions() {
         // get the resource loader object
         ResourceLoader pRb = new ResourceLoader(AssignmentBundleLogic.ASSIGNMENT2_BUNDLE_PERMISSIONS);
         HashMap<String, String> pRbValues = new HashMap<String, String>();
         for (Iterator iKeys = pRb.keySet().iterator();iKeys.hasNext();)
         {
             String key = (String) iKeys.next();
             pRbValues.put(key, (String) pRb.get(key));
             
         }
         
         return pRbValues;
     }
 }
