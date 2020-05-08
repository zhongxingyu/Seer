 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.AccountEngine;
 import com.flexive.shared.interfaces.UserGroupEngine;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.security.*;
 import static com.flexive.tests.embedded.FxTestUtils.*;
 import org.apache.commons.lang.StringUtils;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.fail;
 import static org.testng.Assert.assertTrue;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * Account test cases.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"ejb"})
 public class AccountTest {
 
     private static final String USERNAME = "CACTUS_TEST_USER";
     private static final String LOGINNAME = StringUtils.reverse(USERNAME);
     private static final String PASSWORD = "123456";
     private static final String EMAIL = "test@cactus-user.com";
 
     private AccountEngine accountEngine = null;
     private ContentEngine contentEngine = null;
 
     @BeforeClass
     public void beforeClass() throws Exception {
         accountEngine = EJBLookup.getAccountEngine();
         contentEngine = EJBLookup.getContentEngine();
         long userCount = accountEngine.getActiveUserTickets().size();
         if (userCount > 0) {
             for (UserTicket t : accountEngine.getActiveUserTickets())
                 System.out.println("Logged in user: " + t);
         }
         login(TestUsers.SUPERVISOR);
     }
 
     @AfterClass
     public void afterClass() throws FxLogoutFailedException {
         logout();
     }
 
     /**
      * Test account creation.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void createAccount() throws Exception {
         long accountId = createAccount(0, 100, true, false);
         try {
             if (!getUserTicket().isInRole(Role.AccountManagement)) {
                 assert false : "Test user is not in role user management.";
             }
             Account account = accountEngine.load(accountId);
             assert USERNAME.equals(account.getName().substring(1)) : "Username not stored correctly.";
             assert LOGINNAME.equals(account.getLoginName().substring(1)) : "Login not stored correctly.";
             assert account.isActive() : "Activation flag not set correctly.";
             assert !account.isValidated() : "Confirmation flag not set correctly.";
         } finally {
             // cleanup
             accountEngine.remove(accountId);
         }
     }
 
     /**
      * Try to create an account with an activation date set in the past.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void createPastAccount() throws Exception {
         long accountId = createAccount(-1, 2, true, true);
         try {
             Account account = accountEngine.load(accountId);
             assert new Date().after(account.getValidFrom()) : "Validation date not set correctly.";
             assert new Date().before(account.getValidTo()) : "Validation date not set correctly.";
         } finally {
             accountEngine.remove(accountId);
         }
     }
 
     /**
      * Try to create an account with an expiration date set in the past.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void createExpiredAccount() throws Exception {
         long accountId = -1;
         try {
             accountId = createAccount(-10, 2, true, true);
             Account account = accountEngine.load(accountId);
             assert false : "Able to create expired account (from = " + account.getValidFromString()
                     + ", to = " + account.getValidToString() + ")";
         } catch (Exception e) {
             // passed test
         } finally {
             if (accountId != -1) {
                 accountEngine.remove(accountId);
             }
         }
     }
 
     /**
      * Try to create an account with an invalid date.
      *
      * @throws Exception if an error occured
      */
     @Test
     public void createInvalidDateAccount() throws Exception {
         long accountId = -1;
         try {
             accountId = createAccount(1, -2, true, true);
             Account account = accountEngine.load(accountId);
             assert false : "Able to create account with invalid dates (from = "
                     + account.getValidFromString() + ", to = " + account.getValidToString() + ")";
         } catch (Exception e) {
             // passed test
         } finally {
             if (accountId != -1) {
                 accountEngine.remove(accountId);
             }
         }
     }
 
     private static int COUNT = 0;
 
     /**
      * Create a test account
      *
      * @param deltaDays day difference from the current date
      * @param validDays validity for days
      * @param active    if the account should be active
      * @param confirmed if the account should be confirmed
      * @return a test account
      * @throws FxApplicationException on errors
      */
     private long createAccount(int deltaDays, int validDays, boolean active, boolean confirmed)
             throws FxApplicationException {
         Date begin = new Date(System.currentTimeMillis() + (long) deltaDays * 24 * 3600 * 1000);
         Date end = new Date(System.currentTimeMillis() + ((long) deltaDays + validDays) * 24 * 3600 * 1000);
         final AccountEdit account = new AccountEdit()
                 .setName(COUNT + USERNAME)
                 .setLoginName(COUNT + LOGINNAME)
                 .setEmail((COUNT++) + EMAIL)
                 .setActive(active)
                 .setValidated(confirmed)
                 .setValidFrom(begin)
                 .setValidTo(end);
         return accountEngine.create(account, COUNT + PASSWORD);
     }
 
     @Test
     /**
      * This method tests both the #addRole(), #getRoles(), #updateUser() and #update functionality
      * This test and the following tests assume that the account creation tests went ok
      * @throws FxApplicationException if an error occurrs
      */
     public void roleAndUpdateTest() throws FxApplicationException {
         String passwd = "$123456";
         String name = "A_NEW_CACTUS_NAME";
         String login = "TOTALLY_NEW_CACTUS";
         String email = "cactus@galore.com";
         long lang = FxLanguage.ENGLISH;
         long accountId = createAccount(0, 100, true, true);
         accountEngine.updateUser(accountId, passwd, name, login, email, lang);
         AccountEdit acc = accountEngine.load(accountId).asEditable();
 
         // account change verification
         assertEquals(acc.getName(), name);
         assertEquals(acc.getLoginName(), login);
         assertEquals(acc.getEmail(), email);
         assertEquals(acc.getLanguage().getId(), lang);
 
         // update with null values (shouldn't change anything)
         accountEngine.updateUser(accountId, passwd, null, null, null, null);
         acc = accountEngine.load(accountId).asEditable();
 
         // verification
         assertEquals(acc.getName(), name);
         assertEquals(acc.getLoginName(), login);
         assertEquals(acc.getEmail(), email);
         assertEquals(acc.getLanguage().getId(), lang);
 
         // test the #update(..) method
         name = "AN_EVEN_NEWER_CACTUS_NAME";
         accountEngine.update(accountId, passwd, null, name, null, null, null, null, null, null, null, null, null, acc.getContactDataId());
         acc = accountEngine.load(accountId).asEditable();
 
         // verification
         assertEquals(acc.getName(), name);
         assertEquals(acc.getLoginName(), login);
         assertEquals(acc.getEmail(), email);
         assertEquals(acc.getLanguage().getId(), lang);
 
         // new pw verification
         try { // login
             FxContext.get().login(login, passwd, true);
 
         } catch (FxLoginFailedException e) {
             // this shouldn't happen
         } catch (FxAccountInUseException e) {
             // this shouldn't happen either
         }
         try { // logout
             FxContext.get().logout();
         } catch (FxLogoutFailedException exc) {
             // this shouldn't happen
         }
 
         try { // try and update the user w/o the necessary privileges
             accountEngine.updateUser(accountId, passwd, null, null, null, null);
             fail("Shouldn't be able to update the user account");
         } catch (FxNoAccessException e) {
             // expected
         }
 
         try { // try and load the roles w/o the necessary privileges
             accountEngine.addRole(accountId, Role.MandatorSupervisor.getId());
             fail("Should'nt be able to add the role to the account");
         } catch (FxNoAccessException e) {
             // expected
         }
 
         try { //re-login the supervisor
             login(TestUsers.SUPERVISOR);
         } catch (FxLoginFailedException e) {
             // shouldn't happen
         } catch (FxAccountInUseException e) {
             // shouldn't happen
         }
 
         // add a role and verify the account has that privilege
         accountEngine.addRole(accountId, Role.MandatorSupervisor.getId());
        List<Role> roleList = accountEngine.getRoles(accountId, RoleLoadMode.FROM_USER_ONLY);
         for (Role r : roleList) {
             assertEquals(r.getId(), Role.MandatorSupervisor.getId());
         }
 
         // clean up
         accountEngine.remove(accountId);
     }
 
     @Test
     /**
      * This method tests both the #getAssignedUsers() as well as the #getAssignedUsersCount() methods
      * @throws FxApplicationException if an error occurrs
      */
     public void assignedUsersTest() throws FxApplicationException {
         long accountId_1 = createAccount(0, 100, true, true);
         long accountId_2 = createAccount(0, 100, true, true);
         // create a test group and assign the 2 test users to them
         UserGroupEngine uge = EJBLookup.getUserGroupEngine();
         final String groupName = "TESTICUS_123_TEST";
         long groupId = uge.create(groupName, null, TestUsers.getTestMandator());
         // assign the users to the groups
         accountEngine.addGroup(accountId_1, groupId);
         accountEngine.addGroup(accountId_2, groupId);
 
         List<Account> assignedAccs = accountEngine.getAssignedUsers(groupId, 0, 10);
         // returned list size should be = the assignedUsersCount
         assertEquals((long) assignedAccs.size(), accountEngine.getAssignedUsersCount(groupId, true));
         // check if the returned users are the same as the ones we assigned to the group
         for (Account a : assignedAccs) {
             if (a.getId() == accountId_1) {
                 assertEquals(a.getId(), accountId_1);
             } else if (a.getId() == accountId_2) {
                 assertEquals(a.getId(), accountId_2);
             } else {
                 fail("No other account should be assigned to the group " + groupName);
             }
         }
 
         // clean up
         accountEngine.remove(accountId_1);
         accountEngine.remove(accountId_2);
         uge.remove(groupId);
     }
 
     @Test
     /**
      * Tests the #getAccountMatches(...) and loadAll() methods by creating 2 accounts
      */
     public void accountMatchesTest() throws FxApplicationException {
         // create two testaccounts
         final int count = 2;
         int verify = 0;
         long accountId_1 = createAccount(0, 100, true, true);
         long accountId_2 = createAccount(0, 100, true, true);
         Account acc1 = accountEngine.load(accountId_1);
         String email = acc1.getEmail().substring(acc1.getEmail().indexOf("@"), acc1.getEmail().length());
         int result = accountEngine.getAccountMatches(acc1.getName().substring(2), acc1.getLoginName().substring(2), email, null, null, null, null, null);
 
         assertEquals(result, count);
 
         List<Account> allAccounts = accountEngine.loadAll();
 
         // see if we get anything in return and if the just created accounts are part of it
         assertTrue(allAccounts.size() > 0);
         for (Account a : allAccounts) {
             if (a.getId() == accountId_1 || a.getId() == accountId_2) {
                 verify++;
             }
         }
         assertEquals(count, verify);
 
         // clean up
         accountEngine.remove(accountId_1);
         accountEngine.remove(accountId_2);
     }
 
     @Test
     /**
      * This tests the #loadForContactData(FxPK contactPK) and the #fixContactData methods
      */
     public void loadForContactDataTest() throws FxApplicationException {
         Account accIdLoad, accFxPKLoad;
         long accountId = createAccount(0, 100, true, true);
         accIdLoad = accountEngine.load(accountId);
         FxPK accountPK = accIdLoad.getContactData();
         accFxPKLoad = accountEngine.loadForContactData(accountPK);
 
         // test equality
         compareAccounts(accIdLoad, accFxPKLoad);
 
         // remove the contact data, then fix it
         contentEngine.remove(accountPK);
 
         accountEngine.fixContactData();
         accIdLoad = accountEngine.load(accountId);
         accountPK = accIdLoad.getContactData();
         accFxPKLoad = accountEngine.loadForContactData(accountPK);
         compareAccounts(accIdLoad, accFxPKLoad);
         
         // clean up
         accountEngine.remove(accountId);
     }
 
     /**
      * Tests the equality of two accounts' attributes
      * @param acc1 the first Account to compare
      * @param acc2 the second Account to compare
      */
     private void compareAccounts(Account acc1, Account acc2) {
         // test equality
         assertEquals(acc1.getId(), acc2.getId());
         assertEquals(acc1.getName(), acc2.getName());
         assertEquals(acc1.getLoginName(), acc2.getLoginName());
         assertEquals(acc1.getEmail(), acc2.getEmail());
         assertEquals(acc1.getLanguage(), acc2.getLanguage());
         assertEquals(acc1.getValidFrom(), acc2.getValidFrom());
         assertEquals(acc1.getValidTo(), acc2.getValidTo());
         assertEquals(acc1.isActive(), acc2.isActive());
         assertEquals(acc1.isAllowMultiLogin(), acc2.isAllowMultiLogin());
         assertEquals(acc1.getMandatorId(), acc2.getMandatorId());
     }
 }
