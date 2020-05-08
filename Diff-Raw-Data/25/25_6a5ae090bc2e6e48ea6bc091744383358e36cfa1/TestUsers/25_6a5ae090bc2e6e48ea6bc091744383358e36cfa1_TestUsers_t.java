 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Account;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Test user creation and management.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 public class TestUsers {
     /**
      * System property that contains the enabled test users (or "all" for all users)
      */
     public static final String ENABLE_USERS_PROPERTY = "tests.users";
 
     public static final TestUser SUPERVISOR = new TestUser("SUPERVISOR");
     public static final TestUser MANDATOR_SUPERVISOR = new TestUser("MANDATOR-SUPERVISOR");
     public static final TestUser GUEST = new TestUser("GUEST");
     /** A "normal" test user who belongs to the shared test group but has no other roles/groups */
     public static final TestUser REGULAR = new TestUser("REGULAR");
 
     public static final List<TestUser> ALL_USERS = Collections.unmodifiableList(
             Arrays.asList(SUPERVISOR, MANDATOR_SUPERVISOR, GUEST, REGULAR));
     /**
      * Keeps track of user defined users
      */
     private static List<TestUser> USER_DEFINED_USERS = new ArrayList<TestUser>();
 
 
     private static final String MANDATOR_NAME = "UNIT_TEST_MANDATOR (automatically created)";
 
     private static boolean initialized = false;
     private static long mandatorId;
     private static long languageId;
     private static long sharedTestGroupId;
     private static long sharedInstanceAclId;
 
     public static synchronized void initializeUsers() throws FxApplicationException {
         if (initialized) {
             return;
         }
         initialized = true;
         try {
             FxContext.get().runAsSystem();
             languageId = EJBLookup.getLanguageEngine().load("en").getId();
             mandatorId = getTestMandator();
             removeOrphanedAccounts();
 
             // setup supervisor
             SUPERVISOR.createUser(mandatorId, languageId);
             EJBLookup.getAccountEngine().setRoleList(SUPERVISOR.getUserId(), Role.getList());
 
             // setup mandator supervisor
             MANDATOR_SUPERVISOR.createUser(mandatorId, languageId);
             EJBLookup.getAccountEngine().setRoles(MANDATOR_SUPERVISOR.getUserId(), Role.MandatorSupervisor.getId());
 
             // setup guest user
             GUEST.createUser(mandatorId, languageId);
 
             REGULAR.createUser(mandatorId, languageId);
 
 
             // create user-defined users
             for (TestUser user : USER_DEFINED_USERS) {
                 user.createUser(mandatorId, languageId);
             }
 
             // assign all test users except GUEST to this group
             sharedTestGroupId = EJBLookup.getUserGroupEngine().create("Test user group", "#000000", mandatorId);
             for (TestUser user : ALL_USERS) {
                 if (user != GUEST) {
                     EJBLookup.getAccountEngine().addGroup(user.getUserId(), sharedTestGroupId);
                 }
             }
             for (TestUser user : USER_DEFINED_USERS) {
                 EJBLookup.getAccountEngine().addGroup(user.getUserId(), sharedTestGroupId);
             }
             createTestGroupAssignments();
 
 
         } catch (Exception e) {
            try {
                deleteUsers();      // cleanup
            } catch (Exception e2) {
                // ignore, since setup probably wasn't complete
            }
             e.printStackTrace();
             throw new RuntimeException("Failed to initialize test users: " + e.getMessage());
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 
     private static synchronized void createTestGroupAssignments() throws FxApplicationException {
         // create a content ACL where test users have read/write access
         sharedInstanceAclId = EJBLookup.getAclEngine().create("test content ACL", new FxString(FxLanguage.ENGLISH,  "Test content ACL"),
                 getTestMandator(), "", "", ACL.Category.INSTANCE);
         // assign all permissions
         EJBLookup.getAclEngine().assign(sharedInstanceAclId, sharedTestGroupId, ACL.Permission.values());
 
         // update search test type ACLs to include edit&create&delete permissions for the test user
         final long searchTypeAclId = CacheAdmin.getEnvironment().getType("SEARCHTEST").getACL().getId();
         final long workflowAclId = CacheAdmin.getEnvironment().getDefaultACL(ACL.Category.WORKFLOW).getId();
         for (long aclId: new long[] { searchTypeAclId, workflowAclId }) {
             EJBLookup.getAclEngine().assign(aclId, sharedTestGroupId, true, true, false, true, false, true);
         }
     }
 
     /**
      * Get id of english
      *
      * @return english id
      */
     public static synchronized long getEnglishLanguageId() {
         return languageId;
     }
 
     public static synchronized TestUser createUser(String informalName, Role... roles) throws FxApplicationException {
         TestUser user = new TestUser(informalName);
         if (roles != null && roles.length > 0) {
             user.setRoles(roles);
         }
         USER_DEFINED_USERS.add(user);
         return user;
     }
 
     /**
      * Assign the requested acl and permissions to the given user
      *
      * @param user        TestUser
      * @param aclId       ACL to assign
      * @param permissions permissions to assign for the acl
      * @throws FxApplicationException on errors
      */
     public static synchronized void assignACL(TestUser user, long aclId, ACL.Permission... permissions) throws FxApplicationException {
         EJBLookup.getAclEngine().assign(aclId, user.getUserGroupId(), permissions);
     }
 
     public static synchronized long getTestMandator() throws FxApplicationException {
         try {
             return CacheAdmin.getEnvironment().getMandator(MANDATOR_NAME).getId();
         } catch (Exception e) {
             // ignore - try to create
             return EJBLookup.getMandatorEngine().create(MANDATOR_NAME, true);
         }
     }
 
     private static synchronized void removeOrphanedAccounts() throws FxApplicationException {
         // first remove all orphaned accounts created in previous runs
         for (Account account : EJBLookup.getAccountEngine().loadAll(mandatorId)) {
             if (account.getEmail().startsWith("TESTEMAIL_")) {
                 System.out.println("Removing orphaned test account: " + account.getLoginName());
                 EJBLookup.getAccountEngine().remove(account.getId());
             }
         }
     }
 
     public static synchronized void deleteUsers() throws FxApplicationException {
         try {
             FxContext.get().runAsSystem();
             List<TestUser> users = new ArrayList<TestUser>(ALL_USERS);
             users.addAll(USER_DEFINED_USERS);
             for (TestUser user : users) {
                 try {
                     user.deleteUser();
                 } catch (FxApplicationException e) {
                     System.out.println("Failed to remove test user from database: " + e.getMessage());
                     e.printStackTrace();
                 }
             }
             // remove shared test group
             EJBLookup.getUserGroupEngine().remove(sharedTestGroupId);
             EJBLookup.getAclEngine().remove(sharedInstanceAclId);
             // remove test mandator
             EJBLookup.getMandatorEngine().remove(mandatorId);
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 
     /**
      * Return the list of all test users as configured in the test environment.
      *
      * @return all test users as configured in the test environment.
      */
     public static synchronized List<TestUser> getConfiguredTestUsers() {
         String enableUsers = System.getProperty(ENABLE_USERS_PROPERTY);
         if (StringUtils.isBlank(enableUsers)) {
             return Collections.unmodifiableList(Arrays.asList(new TestUser[]{SUPERVISOR}));
         } else if ("all".equalsIgnoreCase(enableUsers)) {
             return ALL_USERS;
         } else {
             throw new RuntimeException("Invalid value for property " + ENABLE_USERS_PROPERTY + ": " + enableUsers);
         }
     }
 
     /**
      * Create a new ACL of category CONTENT
      *
      * @param name proposed name
      * @return id of the new ACL
      * @throws FxApplicationException on errors
      */
     public static long newContentACL(String name) throws FxApplicationException {
         return EJBLookup.getAclEngine().create("TEST_" + name + RandomStringUtils.random(16, true, true), new FxString(name + " (content)"), TestUsers.getTestMandator(),
                 "#112233", "", ACL.Category.INSTANCE);
     }
 
     /**
      * Create a new ACL of category STRUCTURE
      *
      * @param name proposed name
      * @return id of the new ACL
      * @throws FxApplicationException on errors
      */
     public static long newStructureACL(String name) throws FxApplicationException {
         return EJBLookup.getAclEngine().create("TEST_" + name + RandomStringUtils.random(16, true, true), new FxString(name + " (structure)"), TestUsers.getTestMandator(),
                 "#112233", "", ACL.Category.STRUCTURE);
     }
 
     /**
      * Create a new ACL of category WORKFLOW
      *
      * @param name proposed name
      * @return id of the new ACL
      * @throws FxApplicationException on errors
      */
     public static long newWorkflowACL(String name) throws FxApplicationException {
         return EJBLookup.getAclEngine().create("TEST_" + name + RandomStringUtils.random(16, true, true), new FxString(name + " (workflow)"), TestUsers.getTestMandator(),
                 "#112233", "", ACL.Category.WORKFLOW);
     }
 
     public static synchronized ACL getInstanceAcl() {
         return CacheAdmin.getEnvironment().getACL(sharedInstanceAclId);
     }
 }
