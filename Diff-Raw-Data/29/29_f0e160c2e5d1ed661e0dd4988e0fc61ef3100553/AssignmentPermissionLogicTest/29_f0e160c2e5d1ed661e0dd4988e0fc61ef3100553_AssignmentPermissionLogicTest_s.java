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
 package org.sakaiproject.assignment2.logic.test;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.sakaiproject.assignment2.exception.AssignmentNotFoundException;
 import org.sakaiproject.assignment2.model.Assignment2;
 import org.sakaiproject.assignment2.model.AssignmentGroup;
 import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
 import org.sakaiproject.assignment2.test.Assignment2TestBase;
 import org.sakaiproject.assignment2.test.AssignmentTestDataLoad;
 import org.sakaiproject.site.api.Group;
 
 
 public class AssignmentPermissionLogicTest extends Assignment2TestBase {
 
     /**
      * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
      */
     protected void onSetUpInTransaction() throws Exception {
         super.onSetUpInTransaction();
 
         // refresh the assign data before you begin the tests.  sometimes it gets cranky if you don't
         dao.evictObject(testData.a1);
         testData.a1 = dao.getAssignmentByIdWithGroupsAndAttachments(testData.a1Id);
 
         dao.evictObject(testData.a2);
         testData.a2 = dao.getAssignmentByIdWithGroupsAndAttachments(testData.a2Id);
 
         dao.evictObject(testData.a3);
         testData.a3 = dao.getAssignmentByIdWithGroupsAndAttachments(testData.a3Id);
 
         dao.evictObject(testData.a4);
         testData.a4 = dao.getAssignmentByIdWithGroupsAndAttachments(testData.a4Id);
     }
 
     public void testGetPermissionsForSite() {
         try {
             permissionLogic.getPermissionsForSite(null, null);
             fail("Did not catch null contextId passed to getPermissionsForSite");
         } catch (IllegalArgumentException iae) {}
 
         List<String> allSitePerms = authz.getSiteLevelPermissions();
 
         // if permissions is null, double check that they are all returned
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         Map<String, Boolean> sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, null);
         assertEquals(allSitePerms.size(), sitePerms.size());
         for (String perm : allSitePerms) {
             Boolean hasPerm = sitePerms.get(perm);
             if (perm.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 assertFalse(hasPerm);
             } else {
                 fail("Unknown permission returned from getPermissionsForSite:" + perm);
             }
         }
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, null);
         assertEquals(allSitePerms.size(), sitePerms.size());
         for (String perm : allSitePerms) {
             Boolean hasPerm = sitePerms.get(perm);
             if (perm.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                 assertTrue(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 assertFalse(hasPerm);
             } else {
                 fail("Unknown permission returned from getPermissionsForSite");
             }
         }
         
         // the TA with no groups doesn't have add, edit, etc
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_WITH_NO_GROUPS);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, null);
         assertEquals(allSitePerms.size(), sitePerms.size());
         for (String perm : allSitePerms) {
             Boolean hasPerm = sitePerms.get(perm);
             if (perm.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 assertFalse(hasPerm);
             } else {
                 fail("Unknown permission returned from getPermissionsForSite");
             }
         }
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, null);
         assertEquals(allSitePerms.size(), sitePerms.size());
         for (String perm : allSitePerms) {
             Boolean hasPerm = sitePerms.get(perm);
             if (perm.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 assertTrue(hasPerm);
             } else {
                 fail("Unknown permission returned from getPermissionsForSite");
             }
         }
         
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, null);
         assertEquals(allSitePerms.size(), sitePerms.size());
         for (String perm : allSitePerms) {
             Boolean hasPerm = sitePerms.get(perm);
             if (perm.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 assertTrue(hasPerm);
             } else {
                 fail("Unknown permission returned from getPermissionsForSite");
             }
         }
         
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, null);
         assertEquals(allSitePerms.size(), sitePerms.size());
         for (String perm : allSitePerms) {
             Boolean hasPerm = sitePerms.get(perm);
             if (perm.equals(AssignmentConstants.PERMISSION_ALL_GROUPS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                 assertFalse(hasPerm);
             } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                 assertTrue(hasPerm);
             } else {
                 fail("Unknown permission returned from getPermissionsForSite");
             }
         }
 
         // now let's double check that it handles individual permission properly
         List<String> permsToCheck = new ArrayList<String>();
         permsToCheck.add(AssignmentConstants.PERMISSION_ALL_GROUPS);
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, permsToCheck);
         assertEquals(1, sitePerms.size());
         assertTrue(sitePerms.containsKey(AssignmentConstants.PERMISSION_ALL_GROUPS));
         assertTrue(sitePerms.get(AssignmentConstants.PERMISSION_ALL_GROUPS));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, permsToCheck);
         assertEquals(1, sitePerms.size());
         assertTrue(sitePerms.containsKey(AssignmentConstants.PERMISSION_ALL_GROUPS));
         assertFalse(sitePerms.get(AssignmentConstants.PERMISSION_ALL_GROUPS));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         sitePerms = permissionLogic.getPermissionsForSite(AssignmentTestDataLoad.CONTEXT_ID, permsToCheck);
         assertEquals(1, sitePerms.size());
         assertTrue(sitePerms.containsKey(AssignmentConstants.PERMISSION_ALL_GROUPS));
         assertFalse(sitePerms.get(AssignmentConstants.PERMISSION_ALL_GROUPS));
 
     }
 
     public void testGetPermissionsForAssignments() {
 
         Map<Long, Map<String,Boolean>> assignPermsMap = permissionLogic.getPermissionsForAssignments(null, null);
         assertEquals(0, assignPermsMap.size());
 
         List<String> allAssignPerms = authz.getAssignmentLevelPermissions();
 
         List<Assignment2> allAssigns = new ArrayList<Assignment2>();
         allAssigns.add(testData.a1);
         allAssigns.add(testData.a2);
         allAssigns.add(testData.a3);
         allAssigns.add(testData.a4);
 
         // if permissions is null, double check that they are all returned
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assignPermsMap = permissionLogic.getPermissionsForAssignments(allAssigns, null);
         assertEquals(assignPermsMap.size(), 4);
         for (Long assignId : assignPermsMap.keySet()) {
             Map<String, Boolean> permMap = assignPermsMap.get(assignId);
             assertEquals(allAssignPerms.size(), permMap.size());
             for (String perm : permMap.keySet()) {
                 Boolean hasPerm = permMap.get(perm);
                 if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                     assertTrue(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                     assertTrue(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                     assertTrue(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                     assertTrue(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                     assertTrue(hasPerm);
                 } else {
                     fail("Unknown permission returned by getPermissionsForAssignments");
                 }
             }
         }
 
         // try the TA. These permissions will depend on the actual assignment. TA's can't
         // add/edit/delete assignments that aren't solely associated with the user's groups.
         // They can manage submissions, though, if the assignment has at least one of their groups
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assignPermsMap = permissionLogic.getPermissionsForAssignments(allAssigns, null);
         assertEquals(assignPermsMap.size(), 4);
         for (Long assignId : assignPermsMap.keySet()) {
             Map<String, Boolean> permMap = assignPermsMap.get(assignId);
             assertEquals(allAssignPerms.size(), permMap.size());
             for (String perm : permMap.keySet()) {
                 Boolean hasPerm = permMap.get(perm);
                 if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a2Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a3Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a2Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a3Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertTrue(hasPerm);
                     } else if (assignId.equals(testData.a2Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a3Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a2Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a3Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a2Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a3Id)) {
                         assertFalse(hasPerm);
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertTrue(hasPerm);
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm);
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm);
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else {
                     fail("Unknown permission returned by getPermissionsForAssignments");
                 }
             }
         }
 
         // student 1 is a member of group 1
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assignPermsMap = permissionLogic.getPermissionsForAssignments(allAssigns, null);
         assertEquals(assignPermsMap.size(), 4);
         for (Long assignId : assignPermsMap.keySet()) {
             Map<String, Boolean> permMap = assignPermsMap.get(assignId);
             assertEquals(allAssignPerms.size(), permMap.size());
             for (String perm : permMap.keySet()) {
                 Boolean hasPerm = permMap.get(perm);
                 if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {                    
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertTrue(hasPerm); // restricted to my group
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);  // i'm not in the restricted group
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertTrue(hasPerm); // restricted to my group
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);  // i'm not in the restricted group
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else {
                     fail("Unknown permission returned by getPermissionsForAssignments");
                 }
             }
         }
 
         // student 2 is a member of group 3
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assignPermsMap = permissionLogic.getPermissionsForAssignments(allAssigns, null);
         assertEquals(assignPermsMap.size(), 4);
         for (Long assignId : assignPermsMap.keySet()) {
             Map<String, Boolean> permMap = assignPermsMap.get(assignId);
             assertEquals(allAssignPerms.size(), permMap.size());
             for (String perm : permMap.keySet()) {
                 Boolean hasPerm = permMap.get(perm);
                 if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {                    
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                     if (assignId.equals(testData.a1Id)) { // restricted to group 1 and 3
                         assertTrue(hasPerm); // restricted to my group
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a4Id)) {
                         assertTrue(hasPerm);  // restricted to my group
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertTrue(hasPerm); // restricted to my group
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a4Id)) {
                         assertTrue(hasPerm);  // restricted to my group
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else {
                     fail("Unknown permission returned by getPermissionsForAssignments");
                 }
             }
         }
 
         // student 3 is not in any groups
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assignPermsMap = permissionLogic.getPermissionsForAssignments(allAssigns, null);
         assertEquals(assignPermsMap.size(), 4);
         for (Long assignId : assignPermsMap.keySet()) {
             Map<String, Boolean> permMap = assignPermsMap.get(assignId);
             assertEquals(allAssignPerms.size(), permMap.size());
             for (String perm : permMap.keySet()) {
                 Boolean hasPerm = permMap.get(perm);
                 if (perm.equals(AssignmentConstants.PERMISSION_ADD_ASSIGNMENTS)) {                    
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_EDIT_ASSIGNMENTS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_MANAGE_SUBMISSIONS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_REMOVE_ASSIGNMENTS)) {
                     assertFalse(hasPerm);
                 } else if (perm.equals(AssignmentConstants.PERMISSION_SUBMIT)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertFalse(hasPerm); // i'm not in the restricted group
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);  // i'm not in the restricted group
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else if (perm.equals(AssignmentConstants.PERMISSION_VIEW_ASSIGNMENTS)) {
                     if (assignId.equals(testData.a1Id)) {
                         assertFalse(hasPerm); // i'm not in the restricted group
                     } else if (assignId.equals(testData.a2Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a3Id)) {
                         assertTrue(hasPerm); // no group restrictions
                     } else if (assignId.equals(testData.a4Id)) {
                         assertFalse(hasPerm);  // i'm not in the restricted group
                     } else {
                         fail("Unknown assignment returned by getPermissionsForAssignments");
                     }
                 } else {
                     fail("Unknown permission returned by getPermissionsForAssignments");
                 }
             }
         }
 
     }
 
     public void testIsUserAllowedToEditAssignment() {
         // try passing a null assignment
         try {
             permissionLogic.isUserAllowedToEditAssignment(null, null, null, null);
             fail("did not catch null assignment passed to isUserAllowedToEditAssignment");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
 
         // only instructors should be able to edit assignments
         assertTrue(permissionLogic.isUserAllowedToEditAssignment(null, testData.a1, null, null));
 
         // TAs can only edit assignments that are only restricted to their own group(s)
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(null, testData.a1, null, null));
         // assignment2 doesn't have groups, so TA shouldn't have permission
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(null, testData.a2, null, null));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(null, testData.a1, null, null));
 
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToEditAssignment(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(
                 AssignmentTestDataLoad.TA_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(
                 AssignmentTestDataLoad.STUDENT1_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(
                 AssignmentTestDataLoad.STUDENT2_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToEditAssignment(
                 AssignmentTestDataLoad.STUDENT3_UID, testData.a1, null, null));
     }
 
     public void testIsUserAllowedToEditAllAssignments() {
         try {
             permissionLogic.isUserAllowedToEditAllAssignments(null, null);
             fail("Did not catch null contextId passed to isUserAllowedToEditAllAssignments");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToEditAllAssignments(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToEditAllAssignments(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToEditAllAssignments(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToEditAllAssignments(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToEditAllAssignments(
                 AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToEditAllAssignments(
                 AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToEditAllAssignments(
                 AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToEditAllAssignments(
                 AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.CONTEXT_ID));
     }
 
     public void testIsUserAllowedToAddAssignments() {
         // try passing a null contextId
         try {
             permissionLogic.isUserAllowedToAddAssignments(null, null, null);
             fail("did not catch null contextId passed to isUserAllowedToAddAssignments");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToAddAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToAddAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         
         // now let's fake that the TA isn't in any groups
         assertFalse(permissionLogic.isUserAllowedToAddAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, new ArrayList<String>()));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToAddAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToAddAssignments(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertTrue(permissionLogic.isUserAllowedToAddAssignments(
                 AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignments(
                 AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignments(
                 AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignments(
                 AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
     }
 
     public void testIsUserAllowedToAddAssignment() {
         // try passing a null contextId
         try {
             permissionLogic.isUserAllowedToAddAssignment(null, null, null, null);
             fail("did not catch null assignment passed to isUserAllowedToAddAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // test out all of the group restriction scenarios
         Assignment2 assignWithNoGroups = new Assignment2();
         assignWithNoGroups.setOpenDate(new Date());
 
         Assignment2 assignWithExtraGroups = new Assignment2();
         assignWithExtraGroups.setOpenDate(new Date());
         Set<AssignmentGroup> groups1and2 = new HashSet<AssignmentGroup>();
         groups1and2.add(new AssignmentGroup(assignWithExtraGroups, AssignmentTestDataLoad.GROUP1_NAME));
         groups1and2.add(new AssignmentGroup(assignWithExtraGroups, AssignmentTestDataLoad.GROUP2_NAME));
         assignWithExtraGroups.setAssignmentGroupSet(groups1and2);
 
         Assignment2 assignWithGroup1 = new Assignment2();
         assignWithGroup1.setOpenDate(new Date());
         Set<AssignmentGroup> group1 = new HashSet<AssignmentGroup>();
         group1.add(new AssignmentGroup(assignWithExtraGroups, AssignmentTestDataLoad.GROUP1_NAME));
         assignWithGroup1.setAssignmentGroupSet(group1);
 
         // instructors should have access to everything
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToAddAssignment(null, assignWithNoGroups, null, null));
         assertTrue(permissionLogic.isUserAllowedToAddAssignment(null, assignWithExtraGroups, null, null));
         assertTrue(permissionLogic.isUserAllowedToAddAssignment(null, assignWithGroup1, null, null));
 
         // TAs should only be able to add an assignment restricted to only group 1
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(null, assignWithNoGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(null, assignWithExtraGroups, null, null));
         assertTrue(permissionLogic.isUserAllowedToAddAssignment(null, assignWithGroup1, null, null));
 
         // students can't add at all
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(null, assignWithNoGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(null, assignWithExtraGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(null, assignWithGroup1, null, null));
         
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToAddAssignment(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, assignWithNoGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(
                 AssignmentTestDataLoad.TA_UID, assignWithNoGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(
                 AssignmentTestDataLoad.STUDENT1_UID, assignWithNoGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(
                 AssignmentTestDataLoad.STUDENT2_UID, assignWithNoGroups, null, null));
         assertFalse(permissionLogic.isUserAllowedToAddAssignment(
                 AssignmentTestDataLoad.STUDENT3_UID, assignWithNoGroups, null, null));
     }
 
     public void testIsUserAllowedToDeleteAssignment() {
         // try passing a null assignment
         try {
             permissionLogic.isUserAllowedToDeleteAssignment(null, null, null, null);
             fail("did not catch null assignment passed to isUserAllowedToDeleteAssignment");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToDeleteAssignment(null, testData.a1, null, null));
 
         // TAs can only delete assignments that are only available to his/her group(s)
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(null, testData.a1, null, null));
         // assignment2 doesn't have groups, so TA shouldn't have permission
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(null, testData.a2, null, null));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(null, testData.a1, null, null));
         
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToDeleteAssignment(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(
                 AssignmentTestDataLoad.TA_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(
                 AssignmentTestDataLoad.STUDENT1_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(
                 AssignmentTestDataLoad.STUDENT2_UID, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignment(
                 AssignmentTestDataLoad.STUDENT3_UID, testData.a1, null, null));
     }
 
     public void testIsUserAllowedToDeleteAssignments() {
         // try passing a null argument
         try {
             permissionLogic.isUserAllowedToDeleteAssignments(AssignmentTestDataLoad.INSTRUCTOR_UID, null, null);
             fail("Did not catch null contextId passed to isUserAllowedToDeleteAssignments");
         } catch (IllegalArgumentException iae) {}
 
         // first, let's check if specifying a userId is correct
         assertTrue(permissionLogic.isUserAllowedToDeleteAssignments(AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertTrue(permissionLogic.isUserAllowedToDeleteAssignments(AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignments(AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignments(AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignments(AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.CONTEXT_ID, null));
 
         // now let's try to leave userId null and set the current user
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToDeleteAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToDeleteAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToDeleteAssignments(null, AssignmentTestDataLoad.CONTEXT_ID, null));
     }
 
     public void testIsUserAllowedToViewAssignment() {
         try {
             permissionLogic.isUserAllowedToViewAssignment(null, null, null, null, null);
             fail("Did not catch null assignment passed to isUserAllowedToViewAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // make sure it works if we specify the user
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a4, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(AssignmentTestDataLoad.TA_UID, testData.a4, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a4, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(AssignmentTestDataLoad.STUDENT2_UID, testData.a4, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(AssignmentTestDataLoad.STUDENT3_UID, testData.a4, null, null, null));
 
         // start out as an instructor
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         // should be able to view all of the assignments
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a3, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a4, null, null, null));
 
         // TA may view all assignments but 4 b/c not in the group
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a3, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a4, null, null, null));
 
         // Student 1 may view 1,2,3 b/c of group memberships
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a3, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a4, null, null, null));
 
         // Student 2 may view 1,2,3,4 b/c of group memberships
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a3, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a4, null, null, null));
 
         // Student 3 may only view 2 & 3 b/c not in any groups
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a3, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a4, null, null, null));
 
         // now let's set an assignment to draft and make sure only select users may
         // view it
         testData.a1.setDraft(true);
         // instructor can still edit a1, so may view draft
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         // TA cannot edit a1 b/c is restricted to other groups, so can't view draft
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         // students may not view it
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
 
         testData.a1.setDraft(false);
 
         // now let's double check what happens if assignment is deleted
         testData.a1.setRemoved(true);
         // only students with submission should be able to view it
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         // students 1 and 2 have submissions to a1 so should see it
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
 
         testData.a1.setRemoved(false);
 
         // try an assignment with no group restrictions and no submissions
         testData.a2.setRemoved(true);
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a2, null, null, null));
 
         testData.a2.setRemoved(false);
 
         // let's see what happens if assignment isn't open
         Calendar cal = Calendar.getInstance();
         cal.set(2025, 10, 01);
 
         testData.a1.setOpenDate(cal.getTime());
         // instructor and ta may view it but not the students
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(null, testData.a1, null, null, null));
 
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a4, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(
                 AssignmentTestDataLoad.TA_UID, testData.a4, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(
                 AssignmentTestDataLoad.STUDENT1_UID, testData.a4, null, null, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(
                 AssignmentTestDataLoad.STUDENT2_UID, testData.a4, null, null, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(
                 AssignmentTestDataLoad.STUDENT3_UID, testData.a4, null, null, null));
     }
 
     public void testIsUserAllowedToViewAssignmentId() {
 
         // try passing a null assignmentId
         try {
             permissionLogic.isUserAllowedToViewAssignment(null, null);
             fail("Did not catch null assignmentId passed to isUserAbleToViewAssignment");
         } catch (IllegalArgumentException iae) {}
         
         // try a bogus assignmentId
         try {
             permissionLogic.isUserAllowedToViewAssignment(1234L, null);
             fail("did not catch bogus assignmentId passed to isUserAllowedToViewAssignment");
         } catch (AssignmentNotFoundException anfe) {}
 
         // instructors should be able to view all assignments 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a1Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a2Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a3Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a4Id, null));
 
         // TA should only be able to see assignments that he/she is a member of if restricted.
         // otherwise, should see all
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);   
         // try one that is restricted to a group that ta is a member of
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a1Id, null));
         // this one is not restricted, so should be ok
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a2Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a3Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(testData.a4Id, null));
 
         // Students will see assignments available to site and those available to groups they
         // are a member of
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         // student is a member of a restricted group, so ok
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a1Id, null));
         // this one is not restricted, so should be ok
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a2Id, null));
 
         // let's set the open date to the future.  student shouldn't be able to view anymore
         Calendar cal = Calendar.getInstance();
         cal.set(2025, 10, 01);
 
         // re-retrieve this assignment
         testData.a1 = (Assignment2)dao.findById(Assignment2.class, testData.a1Id);
         testData.a1.setOpenDate(cal.getTime());
         dao.save(testData.a1);
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(testData.a1Id, null));
 
         // Students will see assignments available to site and those available to groups they
         // are a member of. assign must be open
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         // student is not a member of the restricted group, so cannot view
         assertFalse(permissionLogic.isUserAllowedToViewAssignment(testData.a4Id, null));
         // this gb item hasn't been released yet, but student may still view it
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a3Id, null));
 
         // switch to student who is a member of group 3
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertTrue(permissionLogic.isUserAllowedToViewAssignment(testData.a4Id, null));
     }
 
     public void testIsUserAbleToViewStudentSubmissionForAssignment() {
         // try passing a null studentId
         try {
             permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, null, 12345L, null);
             fail("Did not catch null student passed to isUserAbleToViewStudentSubmissionForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // try passing a null assignment
         try {
             permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, AssignmentTestDataLoad.STUDENT1_UID, null, null);
             fail("Did not catch null assignment passed to isUserAbleToViewStudentSubmissionForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // Assignment 1 restricted to group 1 and group 2
         // Assignment 2 has no restrictions
         // Assignment 3 has no restrictions
         // Assignment 4 restricted to group 3
 
         // ta in group 1
         // student1 member of group 1
         // student 2 member of group 3
         // student 3 not in a group
 
         // instructor should be able to view any student
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a2Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a2Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a2Id, null));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a3Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a3Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a3Id, null));
 
         // switch to TA
         // ta may only view assignments restricted to his/her group 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a1Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a1Id, null));
 
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a2Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a2Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a2Id, null));
 
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a3Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a3Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a3Id, null));
 
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a4Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a4Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a4Id, null));
 
         // user may view their own submission
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
         // but they may not view others
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(null, AssignmentTestDataLoad.STUDENT2_UID, testData.a1Id, null));
 
         // make sure it works if we specify the user
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
         assertTrue(permissionLogic.isUserAllowedToViewSubmissionForAssignment(AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
         assertFalse(permissionLogic.isUserAllowedToViewSubmissionForAssignment(AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1Id, null));
     }
 
     public void testIsUserAllowedToManageSubmission() {
         // pass null studentId
         try {
             permissionLogic.isUserAllowedToManageSubmission(null, null, testData.a1);
             fail("did not catch null assignment passed to isUserAllowedToManageSubmission");
         } catch(IllegalArgumentException iae) {}
         // pass null assignment
         try {
             permissionLogic.isUserAllowedToManageSubmission(null, AssignmentTestDataLoad.STUDENT1_UID, null);
             fail("did not catch null assignment passed to isUserAllowedToManageSubmission");
         } catch(IllegalArgumentException iae) {}
 
         // instructor should be able to manage all student submissions
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a2));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a2));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a2));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a3));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a3));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a3));
 
         // switch to TA
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         // a1 is restricted to group 1, so ta has some privileges
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a1));
         // a2 and a3 are not restricted to groups, so ta does not have any privileges
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a2));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a2));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a2));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a3));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a3));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a3));
         // a4 is restricted to a different group, so TA has no privileges
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a4));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a4));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a4));
 
         // students should not be able to submit feedback at all
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT2_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, AssignmentTestDataLoad.STUDENT3_UID, testData.a1));
 
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.STUDENT1_UID, testData.a1));
     }
 
     public void testIsUserAllowedToManageSubmissionId() {
         // pass null submissionId
         try {
             permissionLogic.isUserAllowedToManageSubmission(null, null);
             fail("did not catch null submissionId passed to isUserAbleToProvideFeedbackForSubmission");
         } catch(IllegalArgumentException iae) {}
 
         // instructor should be able to submit feedback for any student
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st1a1Submission.getId()));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st2a1Submission.getId()));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st1a3Submission.getId()));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st2a3Submission.getId()));
 
         // switch to TA
         // ta may only submit feedback for members in his/her group for assignments
         // restricted to his/her group(s)
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st1a1Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st2a1Submission.getId()));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st1a1Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st1a3Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 null, testData.st2a3Submission.getId()));
 
         // students should not be able to submit feedback at all
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(null, testData.st1a1Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(null, testData.st2a1Submission.getId()));
 
         // make sure this still works if we pass in the userId
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.INSTRUCTOR_UID, testData.st1a1Submission.getId()));
         assertTrue(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.TA_UID, testData.st1a1Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.STUDENT1_UID, testData.st1a1Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.STUDENT2_UID, testData.st1a1Submission.getId()));
         assertFalse(permissionLogic.isUserAllowedToManageSubmission(
                 AssignmentTestDataLoad.STUDENT3_UID, testData.st1a1Submission.getId()));
     }
 
     public void testFilterViewableAssignments() {
         // try passing a null contextId
         try {
             permissionLogic.filterViewableAssignments(null, new ArrayList<Assignment2>());
             fail("Did not catch null contextId passed to filterViewableAssignments");
         } catch (IllegalArgumentException iae) {}
 
         // try passing an assignment with a diff contextId
         Assignment2 assign = new Assignment2();
         assign.setContextId(AssignmentTestDataLoad.BAD_CONTEXT);
         List<Assignment2> assignList = new ArrayList<Assignment2>();
         assignList.add(assign);
         try {
             permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
             fail("did not catch assignment in the list with a different contextId");
         } catch (IllegalArgumentException iae) {}
 
         // instructors should be able to view all assignments 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
 
         assignList = new ArrayList<Assignment2>();
         assignList.add(testData.a1);
         assignList.add(testData.a2);
 
         List<Assignment2> filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(2, filteredAssigns.size());
 
 
         // TA should only be able to see assignments that he/she is a member of if restricted
         // otherwise, should see all
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         // a1 is restricted to a group that ta is a member of
         // a2 is not restricted, so should be ok
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(2, filteredAssigns.size());
 
         // Students will see assignments available to site and those available to groups they
         // are a member of
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         // a1: student is a member of a restricted group, so ok
         // a2: is not restricted, so should be ok
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(2, filteredAssigns.size());
 
         // student 3 should only have access to assign 2 b/c of group memberships
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a2Id, filteredAssigns.get(0).getId());
 
         // let's set the open date to the future.  student shouldn't be able to view anymore
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         Calendar cal = Calendar.getInstance();
         cal.set(2025, 10, 01);
 
         testData.a1.setOpenDate(cal.getTime());
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a2Id, filteredAssigns.get(0).getId());
 
         // let's set the assignment to draft. only instructor should be able to view
         testData.a1.setDraft(true);
         // try student
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a2Id, filteredAssigns.get(0).getId());
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a2Id, filteredAssigns.get(0).getId());
 
         // instructor should be allowed
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(2, filteredAssigns.size());
 
         // let's try out some graded assignment logic
 
         // gb item 1 is not released - assoc with a3
         // a4 is restricted to group 3
 
         // instructors should be able to view all assignments 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assignList = new ArrayList<Assignment2>();
         assignList.add(testData.a3);
         assignList.add(testData.a4);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(2, filteredAssigns.size());
 
         // TAs can view all gb items since there are no grader perms, but he/she can't view
         // assignments that are restricted to groups they are not in. ta can't view assign 4
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a3Id, filteredAssigns.get(0).getId());
 
         // Students will see assignments available to site and those available to groups they
         // are a member of. assign must be open
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         // a4: student is not a member of the restricted group, so cannot view
         // a3: this gb item hasn't been released yet, but the student can still view it
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a3Id, filteredAssigns.get(0).getId());
 
         // switch to student who is a member of group 3
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(2, filteredAssigns.size());
 
         // let's set the open date to the future.  student shouldn't be able to view anymore
         cal.set(2025, 10, 01);
 
         testData.a4.setOpenDate(cal.getTime());
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
         assertEquals(testData.a3Id, filteredAssigns.get(0).getId());
 
         // now set a1 as removed and remove the group restrictions to test this. 
         // only a student w/ a submission should be able to view. st1 has a submission
         // but st3 does not
         testData.a4.setOpenDate(new Date());
         testData.a4.setRemoved(true);
         testData.a4.setAssignmentGroupSet(new HashSet<AssignmentGroup>());
 
         assignList = new ArrayList<Assignment2>();
         assignList.add(testData.a4);
 
         // instructor
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(0, filteredAssigns.size());
 
         // ta
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(0, filteredAssigns.size());
 
         // student w/o sub
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(0, filteredAssigns.size());
 
         // student with sub
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         filteredAssigns = permissionLogic.filterViewableAssignments(AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(1, filteredAssigns.size());
 
         testData.a1.setDraft(false);
         testData.a1.setOpenDate(new Date());
         testData.a4.setRemoved(false);
     }
 
     public void testIsUserAMemberOfARestrictedGroup() {
         List<String> groupMembershipIds = null;
         List<AssignmentGroup> assignmentGroupSet = null;
         // try with both null
         assertFalse(permissionLogic.isUserAMemberOfARestrictedGroup(groupMembershipIds, assignmentGroupSet));
 
         // add a group to groupMembershipIds - should still be false
         groupMembershipIds = new ArrayList<String>();
         groupMembershipIds.add(AssignmentTestDataLoad.GROUP1_NAME);
         assertFalse(permissionLogic.isUserAMemberOfARestrictedGroup(groupMembershipIds, assignmentGroupSet));
 
         // add a different AssignmentGroup to the assignmentGroups
         assignmentGroupSet = new ArrayList<AssignmentGroup>();
         assignmentGroupSet.add(new AssignmentGroup(null, AssignmentTestDataLoad.GROUP2_NAME));
         assertFalse(permissionLogic.isUserAMemberOfARestrictedGroup(groupMembershipIds, assignmentGroupSet));
 
         // now add an overlapping group to group membership
         groupMembershipIds.add(AssignmentTestDataLoad.GROUP2_NAME);
         assertTrue(permissionLogic.isUserAMemberOfARestrictedGroup(groupMembershipIds, assignmentGroupSet));
 
     }
 
     public void testIsUserAllowedToAccessInstructorView() {
         // pass in a null contextId
         try {
             permissionLogic.isUserAllowedToAccessInstructorView(null, null);
             fail("Did not catch null contextId passed to isUserAbleToAccessInstructorView");
         } catch (IllegalArgumentException iae) {
 
         }
         // only instructors and tas should have access to the non-student view
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToAccessInstructorView(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToAccessInstructorView(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToAccessInstructorView(null, AssignmentTestDataLoad.CONTEXT_ID));
         
         // make sure it works if we specify the user
         assertTrue(permissionLogic.isUserAllowedToAccessInstructorView(AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertTrue(permissionLogic.isUserAllowedToAccessInstructorView(AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToAccessInstructorView(AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToAccessInstructorView(AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedToAccessInstructorView(AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.CONTEXT_ID));
     }
 
     public void testGetViewableStudentsForAssignment() {
 
         // try a null assignment
         try {
             permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, null);
             fail("did not catch null assignment passed to getViewableStudentsForUserForItem");
         } catch(IllegalArgumentException iae) {}
 
         // this method should return 0 if a student calls it
 
         List<String> viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a1);
         assertEquals(0, viewableStudents.size());
 
         // instructor should get all students who have the assignment
         // a1 is restricted to groups, so will return all students in those groups
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a1);
         assertEquals(2, viewableStudents.size());
         // a2 and a3 are not restricted
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a2);
         assertEquals(3, viewableStudents.size());
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a3);
         assertEquals(3, viewableStudents.size());
         // a4 is restricted to group 3
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a4);
         assertEquals(1, viewableStudents.size());
 
         // the ta should have restrictions on a1
         // should only get student 1 b/c may only see students in his/her group
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.TA_UID, testData.a1);
         assertEquals(1, viewableStudents.size());
         // a2 and a3 aren't restricted to groups, so TA can't view any students
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.TA_UID, testData.a2);
         assertEquals(0, viewableStudents.size());
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.TA_UID, testData.a3);
         assertEquals(0, viewableStudents.size());
         // a4 is restricted to a group that the TA is not a member of, so should get 0 students
         viewableStudents = permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.TA_UID, testData.a4);
         assertEquals(0, viewableStudents.size());
         
         // the students should get empty lists
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a1).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a2).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a3).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a4).size());
         
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a1).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a2).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a3).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a4).size());
         
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a1).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a2).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a3).size());
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a4).size());
         
         // double check that it uses the current user if userId is null
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertEquals(2, permissionLogic.getViewableStudentsForAssignment(null, testData.a1).size());
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertEquals(1, permissionLogic.getViewableStudentsForAssignment(null, testData.a1).size());
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(null, testData.a1).size());
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(null, testData.a1).size());
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertEquals(0, permissionLogic.getViewableStudentsForAssignment(null, testData.a1).size());
     }
 
     public void testGetViewableStudentsForAssignments() {
 
         // try a null contextId
         try {
             permissionLogic.getViewableStudentsForAssignments(AssignmentTestDataLoad.TA_UID, null, new ArrayList<Assignment2>());
             fail("Did not catch null contextId passed to getViewableStudentsForUserForAssignments");
         } catch (IllegalArgumentException iae) {}
 
         // try a null assignment list - should return an empty map
         Map<Assignment2, List<String>> assignmentToStudentListMap = 
             permissionLogic.getViewableStudentsForAssignments(AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID, null);
 
         assertEquals(0, assignmentToStudentListMap.size());
 
         // this method should return all assignments with empty lists
         List<Assignment2> assignList = new ArrayList<Assignment2>();
         assignList.add(testData.a1);
         assignList.add(testData.a2);
         assignList.add(testData.a3);
         assignList.add(testData.a4);
         assignmentToStudentListMap = permissionLogic.getViewableStudentsForAssignments(AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(4, assignmentToStudentListMap.size());
         for (Map.Entry<Assignment2, List<String>> entry : assignmentToStudentListMap.entrySet()) {
             Assignment2 assign = entry.getKey();
             List<String> viewableStudents = entry.getValue();
             if (assign.equals(testData.a1)) {
                 assertEquals(0, viewableStudents.size());
             } else if (assign.equals(testData.a2)) {
                 assertEquals(0, viewableStudents.size());
             } else if (assign.equals(testData.a3)) {
                 assertEquals(0, viewableStudents.size());
             } else if (assign.equals(testData.a4)) {  
                 assertEquals(0, viewableStudents.size());
             } else {
                 fail("Unknown assignment returned by getViewableStudentsForUserForAssignments");
             }
         }
 
         // instructor should get all students who have the assignment
         // a1 is restricted to groups, so will return all students in those groups. a2 is not restricted
         // a3 (graded) is not restricted, so will return all students
         // a4 (graded) is restricted to group 3
         assignmentToStudentListMap = permissionLogic.getViewableStudentsForAssignments(AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(4, assignmentToStudentListMap.size());
         for (Map.Entry<Assignment2, List<String>> entry : assignmentToStudentListMap.entrySet()) {
             Assignment2 assign = entry.getKey();
             List<String> viewableStudents = entry.getValue();
             if (assign.equals(testData.a1)) {
                 assertEquals(2, viewableStudents.size());
             } else if (assign.equals(testData.a2)) {
                 assertEquals(3, viewableStudents.size());
             } else if (assign.equals(testData.a3)) {
                 assertEquals(3, viewableStudents.size());
             } else if (assign.equals(testData.a4)) {  
                 assertEquals(1, viewableStudents.size());
             } else {
                 fail("Unknown assignment returned by getViewableStudentsForUserForAssignments");
             }
         }
 
         // the ta should have restrictions on a1
         // should only get student 1 b/c may only see students in his/her group
         // should get no students for the other assignments
         assignmentToStudentListMap = permissionLogic.getViewableStudentsForAssignments(AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.CONTEXT_ID, assignList);
         assertEquals(4, assignmentToStudentListMap.size());
         for (Map.Entry<Assignment2, List<String>> entry : assignmentToStudentListMap.entrySet()) {
             Assignment2 assign = entry.getKey();
             List<String> viewableStudents = entry.getValue();
             if (assign.equals(testData.a1)) {
                 assertEquals(1, viewableStudents.size());
             } else if (assign.equals(testData.a2)) {
                 assertEquals(0, viewableStudents.size());
             } else if (assign.equals(testData.a3)) {
                 assertEquals(0, viewableStudents.size());
             } else if (assign.equals(testData.a4)) {  
                 assertEquals(0, viewableStudents.size());
             } else {
                 fail("Unknown assignment returned by getViewableStudentsForUserForAssignments");
             }
         }
     }
 
     public void testIsUserAbleToMakeSubmissionForAssignment() {
 
         // try passing a null assignment
         try {
             permissionLogic.isUserAllowedToMakeSubmissionForAssignmentId(null, null);
             fail("did not catch null assignment passed to isUserAbleToMakeSubmissionForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // instructor and TA should not be able to submit
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a2));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a3));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a4));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a1));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a2));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a3));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a4));
 
         // student 1 is a member of group 1
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         // should be able to submit for a1, a2, a3
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a1));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a2));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a3));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a4));
 
         // student 2 is a member of group 3
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         // should be able to submit for a1, a2, a3, a4
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a1));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a2));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a3));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a4));
 
         // student 3 is not a member of any groups
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         // should only be able to submit to 2,3
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a1));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a2));
         assertTrue(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a3));
         assertFalse(permissionLogic.isUserAllowedToMakeSubmissionForAssignment(null, testData.a4));
     }
 
     /*public void testIsUserAllowedToReleaseFeedbackForAssignment() {
         // try passing a null assignment
         try {
             permissionLogic.isUserAllowedToProvideFeedbackForAssignment(null);
             fail("Null assignment passed to isUserAllowedToReleaseFeedbackForAssignment was not caught");
         } catch (IllegalArgumentException iae) {}
 
         // instructor should be true for all
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a1));
         assertTrue(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a2));
         assertTrue(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a3));
         assertTrue(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a4));
 
         // ta should be true for a1 only. the ta does not have grading privileges for the
         // other assignments
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a1));
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a2));
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a3));
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a4));
 
         // double check that students are all false
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a1));
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a2));
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a3));
         assertFalse(permissionLogic.isUserAllowedToProvideFeedbackForAssignment(testData.a4));
     }*/
 
     public void testIsUserAllowedToSubmit() {
         // only students may submit
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertFalse(permissionLogic.isUserAllowedToSubmit(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToSubmit(null, AssignmentTestDataLoad.CONTEXT_ID, null));
 
         // now try the students
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertTrue(permissionLogic.isUserAllowedToSubmit(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertTrue(permissionLogic.isUserAllowedToSubmit(null, AssignmentTestDataLoad.CONTEXT_ID, null));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertTrue(permissionLogic.isUserAllowedToSubmit(null, AssignmentTestDataLoad.CONTEXT_ID, null));
     }
 
     public void testGetUsersAllowedToViewStudentForAssignment() {
         // try some null params
         try {
             permissionLogic.getUsersAllowedToViewStudentForAssignment(null, testData.a1);
             fail("Did not catch null studentId passed to getUsersAllowedToViewStudentForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         try {
             permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.TA_UID, null);
             fail("Did not catch null assignment passed to getUsersAllowedToViewStudentForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // instructor and ta passed as a student should return nothing
         List<String> usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.INSTRUCTOR_UID, testData.a1);
         assertEquals(0, usersAllowedToView.size());
 
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.TA_UID, testData.a1);
         assertEquals(0, usersAllowedToView.size());
 
         // ta only has access to group 1 - student1
         // STUDENT 1 should have inst and ta
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a1);
         assertEquals(2, usersAllowedToView.size());
         assertTrue(usersAllowedToView.contains(AssignmentTestDataLoad.TA_UID));
         assertTrue(usersAllowedToView.contains(AssignmentTestDataLoad.INSTRUCTOR_UID));
 
         // student 2 should only have instructor
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.STUDENT2_UID, testData.a1);
         assertEquals(1, usersAllowedToView.size());
         assertTrue(usersAllowedToView.contains(AssignmentTestDataLoad.INSTRUCTOR_UID));
 
         // student 3 does not have access to assign 1
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.STUDENT3_UID, testData.a1);
         assertEquals(0, usersAllowedToView.size());
 
         // all of the students can access assign 3 but the ta can't
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.STUDENT1_UID, testData.a3);
         assertEquals(1, usersAllowedToView.size());
         assertFalse(usersAllowedToView.contains(AssignmentTestDataLoad.TA_UID));
         assertTrue(usersAllowedToView.contains(AssignmentTestDataLoad.INSTRUCTOR_UID));
 
         // student 2 should only have instructor
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.STUDENT2_UID, testData.a3);
         assertEquals(1, usersAllowedToView.size());
         assertTrue(usersAllowedToView.contains(AssignmentTestDataLoad.INSTRUCTOR_UID));
 
         // student 3 should only have instructor
         usersAllowedToView = permissionLogic.getUsersAllowedToViewStudentForAssignment(AssignmentTestDataLoad.STUDENT3_UID, testData.a3);
         assertEquals(1, usersAllowedToView.size());
         assertTrue(usersAllowedToView.contains(AssignmentTestDataLoad.INSTRUCTOR_UID));
     }
 
     public void testGetViewableGroupsForCurrUserForAssignment() {
         // try passing a null
         try {
             permissionLogic.getViewableGroupsForAssignment(null, null);
             fail("did not catch null assignment passed to getViewableGroupsForCurrUserForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         // Assign 1 is restricted to groups 1 and 3
         // Assign 2 is not restricted
         // there are 3 groups defined 
 
         // start as instructor - should be allowed to view all available groups
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         List<Group> viewableGroups = permissionLogic.getViewableGroupsForAssignment(null, testData.a1);
         assertEquals(2, viewableGroups.size());  // restricted to group 1 and 3
         for (Group group : viewableGroups) {
             if (!group.getId().equals(AssignmentTestDataLoad.GROUP1_NAME) &&
                     !group.getId().equals(AssignmentTestDataLoad.GROUP3_NAME)) {
                 fail("Unknown group returned by getViewableGroupsForCurrUserForAssignment");
             }
         }
 
 
         viewableGroups = permissionLogic.getViewableGroupsForAssignment(null, testData.a2);
         assertEquals(3, viewableGroups.size());
         for (Group group : viewableGroups) {
             if (!group.getId().equals(AssignmentTestDataLoad.GROUP1_NAME) &&
                     !group.getId().equals(AssignmentTestDataLoad.GROUP2_NAME) &&
                     !group.getId().equals(AssignmentTestDataLoad.GROUP3_NAME)) {
                 fail("Unknown group returned by getViewableGroupsForCurrUserForAssignment");
             }
         }
 
         // now test TA - should only be able to view group 1
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         viewableGroups = permissionLogic.getViewableGroupsForAssignment(null, testData.a1);
         assertEquals(1, viewableGroups.size());  // restricted to group 1 and 3 and only 1 is viewable
         for (Group group : viewableGroups) {
             if (!group.getId().equals(AssignmentTestDataLoad.GROUP1_NAME)) {
                 fail("Unknown group returned by getViewableGroupsForCurrUserForAssignment");
             }
         }
 
         viewableGroups = permissionLogic.getViewableGroupsForAssignment(null, testData.a2);
         assertEquals(1, viewableGroups.size());
         for (Group group : viewableGroups) {
             if (!group.getId().equals(AssignmentTestDataLoad.GROUP1_NAME)) {
                 fail("Unknown group returned by getViewableGroupsForCurrUserForAssignment");
             }
         }
 
         // students should get their own group memberships
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         viewableGroups = permissionLogic.getViewableGroupsForAssignment(null, testData.a1);
         assertEquals(1, viewableGroups.size());  
 
         viewableGroups = permissionLogic.getViewableGroupsForAssignment(null, testData.a2);
         assertEquals(1, viewableGroups.size());
 
     }
 
     public void testIsUserAllowedToManageAllSubmissions() {
         try {
             permissionLogic.isUserAllowedToManageAllSubmissions(null, null);
             fail("did not catch null contextId passed to isUserAllowedToManageAllSubmissions");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToManageAllSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedToManageAllSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToManageAllSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertFalse(permissionLogic.isUserAllowedToManageAllSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedToManageAllSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID));
 
     }
 
     public void testIsUserAllowedToManageSubmissionsForAssignment() {
         try {
             permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, null, null, null);
             fail("Did not catch null assignment passed to isUserAllowedToManageSubmissionsForAssignment");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a1, null, null));
         assertTrue(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a2, null, null));
         assertTrue(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a3, null, null));
         assertTrue(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a4, null, null));
 
         // tas can only manage submissions for assignments that are restricted to group(s) they
         // are a member of
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a2, null, null));
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a3, null, null));
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a4, null, null));
 
         // double check that the students can't do anything
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a1, null, null));
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a2, null, null));
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a3, null, null));
         assertFalse(permissionLogic.isUserAllowedToManageSubmissionsForAssignment(null, testData.a4, null, null));
     }
 
     public void testIsUserAllowedToManageSubmissions() {
         try {
             permissionLogic.isUserAllowedToManageSubmissions(null, null, null);
             fail("Did not catch null contextId passed to isUserAllowedToManageSubmissions");
         } catch (IllegalArgumentException iae) {}
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID, null));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertTrue(permissionLogic.isUserAllowedToManageSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID, null));
 
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedToManageSubmissions(null, AssignmentTestDataLoad.CONTEXT_ID, null));
     }
     
     public void testGetSubmittersInSite() {
         try {
             permissionLogic.getSubmittersInSite(null);
             fail("did not catch null contextId passed to getSubmittersInSite");
         } catch (IllegalArgumentException iae) {}
         
         Set<String> submitters = permissionLogic.getSubmittersInSite(AssignmentTestDataLoad.CONTEXT_ID);
         assertEquals(3, submitters.size());
         
         for (String user : submitters) {
             if (user.equals(AssignmentTestDataLoad.STUDENT1_UID) ||
                     user.equals(AssignmentTestDataLoad.STUDENT2_UID) ||
                     user.equals(AssignmentTestDataLoad.STUDENT3_UID)) {
                 // these should be there
             } else {
                 fail("Non-submitter returned by getSubmittersInSite: " + user);
             }
         }
     }
     
     public void testIsUserAllowedForAllGroups() {
         try {
             permissionLogic.isUserAllowedForAllGroups(null, null);
             fail("Did not catch null contextId passed to isUserAllowedForAllGroups");
         } catch (IllegalArgumentException iae) {}
         
         // let's see if it works by passing in the userId
         assertTrue(permissionLogic.isUserAllowedForAllGroups(AssignmentTestDataLoad.INSTRUCTOR_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedForAllGroups(AssignmentTestDataLoad.TA_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedForAllGroups(AssignmentTestDataLoad.STUDENT1_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedForAllGroups(AssignmentTestDataLoad.STUDENT2_UID, AssignmentTestDataLoad.CONTEXT_ID));
         assertFalse(permissionLogic.isUserAllowedForAllGroups(AssignmentTestDataLoad.STUDENT3_UID, AssignmentTestDataLoad.CONTEXT_ID));
         
         // now use the current user
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.INSTRUCTOR_UID);
         assertTrue(permissionLogic.isUserAllowedForAllGroups(null, AssignmentTestDataLoad.CONTEXT_ID));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.TA_UID);
         assertFalse(permissionLogic.isUserAllowedForAllGroups(null, AssignmentTestDataLoad.CONTEXT_ID));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT1_UID);
         assertFalse(permissionLogic.isUserAllowedForAllGroups(null, AssignmentTestDataLoad.CONTEXT_ID));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT2_UID);
         assertFalse(permissionLogic.isUserAllowedForAllGroups(null, AssignmentTestDataLoad.CONTEXT_ID));
         externalLogic.setCurrentUserId(AssignmentTestDataLoad.STUDENT3_UID);
         assertFalse(permissionLogic.isUserAllowedForAllGroups(null, AssignmentTestDataLoad.CONTEXT_ID));
     }
 }
