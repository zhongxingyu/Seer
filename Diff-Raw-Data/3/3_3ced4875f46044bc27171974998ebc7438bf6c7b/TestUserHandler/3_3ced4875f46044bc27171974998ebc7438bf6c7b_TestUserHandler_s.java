 /*
  * Copyright (C) 2003-2008 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.services.tck.organization;
 
 import org.exoplatform.services.organization.Query;
 import org.exoplatform.services.organization.User;
 import org.exoplatform.services.organization.UserEventListener;
 import org.exoplatform.services.organization.UserEventListenerHandler;
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created by The eXo Platform SAS.
  * 
  * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
  * @version $Id: TestOrganizationService.java 111 2008-11-11 11:11:11Z $
  */
 public class TestUserHandler extends AbstractOrganizationServiceTest
 {
 
    /**
     * User authentication.
     */
    public void testAuthenticate() throws Exception
    {
       // authentication with existed user and correct password
       assertTrue(uHandler.authenticate("demo", "exo"));
 
       // unknown user authentication
       assertFalse(uHandler.authenticate("demo_", "exo"));
 
       // authentication with wrong password
       assertFalse(uHandler.authenticate("demo", "exo_"));
    }
 
    /**
     * Find user by name.
     */
    public void testFindUserByName() throws Exception
    {
       // try to find existed user
       User u = uHandler.findUserByName("demo");
 
       assertNotNull(u);
       assertEquals("demo@localhost", u.getEmail());
       assertEquals("Demo", u.getFirstName());
       assertEquals("exo", u.getLastName());
       assertEquals("exo", u.getPassword());
       assertEquals("demo", u.getUserName());
 
       // try to find not existed user. We are supposed to get "null" instead of Exception.
       try
       {
          assertNull(uHandler.findUserByName("not-existed-user"));
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Find users by query.
     */
    public void testFindUsersByQuery() throws Exception
    {
       createUser("tolik");
 
       Query query = new Query();
       query.setEmail("email@test");
 
       // try to find user by email
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name with mask
       query = new Query();
       query.setUserName("*tolik*");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name with mask
       query = new Query();
       query.setUserName("tol*");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name with mask
       query = new Query();
       query.setUserName("*lik");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name explicitly
       query = new Query();
       query.setUserName("tolik");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name explicitly, case sensitive search
       query = new Query();
       query.setUserName("Tolik");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by part of name without mask
       query = new Query();
       query.setUserName("tol");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by fist and last names, case sensitive search
       query = new Query();
       query.setFirstName("fiRst");
       query.setLastName("lasT");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       String skipDateTests = System.getProperty("orgservice.test.configuration.skipDateTests");
       if (!"true".equals(skipDateTests))
       {
          // try to find user by login date
          Calendar calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);
 
          query = new Query();
          query.setFromLoginDate(calc.getTime());
          query.setUserName("tolik");
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
          calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);
 
          query = new Query();
          query.setFromLoginDate(calc.getTime());
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 0);
 
          calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);
 
          query = new Query();
          query.setToLoginDate(calc.getTime());
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 0);
 
          calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);
 
          query = new Query();
          query.setToLoginDate(calc.getTime());
          query.setUserName("tolik");
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       }
    }
 
    /**
     * Find users.
     */
    public void testFindUsers() throws Exception
    {
       createUser("tolik");
 
       Query query = new Query();
       query.setEmail("email@test");
 
       // try to find user by email
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name with mask
       query = new Query();
       query.setUserName("*tolik*");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name with mask
       query = new Query();
       query.setUserName("tol*");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name with mask
       query = new Query();
       query.setUserName("*lik");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name explicitly
       query = new Query();
       query.setUserName("tolik");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by name explicitly, case sensitive search
       query = new Query();
       query.setUserName("Tolik");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by part of name without mask
       query = new Query();
       query.setUserName("tol");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       // try to find user by fist and last names, case sensitive search
       query = new Query();
       query.setFirstName("fiRst");
       query.setLastName("lasT");
       assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
       String skipDateTests = System.getProperty("orgservice.test.configuration.skipDateTests");
       if (!"true".equals(skipDateTests))
       {
          // try to find user by login date
          Calendar calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);
 
          query = new Query();
          query.setFromLoginDate(calc.getTime());
          query.setUserName("tolik");
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
 
          calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);
          
          query = new Query();
          query.setFromLoginDate(calc.getTime());
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 0);
 
          calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) - 1);
 
          query = new Query();
          query.setToLoginDate(calc.getTime());
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 0);
 
          calc = Calendar.getInstance();
          calc.set(Calendar.YEAR, calc.get(Calendar.YEAR) + 1);
 
          query = new Query();
          query.setToLoginDate(calc.getTime());
          query.setUserName("tolik");
          assertEquals(uHandler.findUsersByQuery(query).getSize(), 1);
       }
 
    }
 
    /**
     * Get users page list.
     */
    public void testGetUserPageList() throws Exception
    {
       assertEquals(uHandler.getUserPageList(10).getAll().size(), 4);
    }
 
    /**
     * Find all users.
     */
    public void testFindAllUsers() throws Exception
    {
       assertEquals(uHandler.findAllUsers().getSize(), 4);
    }
 
    /**
     * Remove user.
     */
    public void testRemoveUser() throws Exception
    {
       createMembership(userName, groupName2, membershipType);
 
       assertEquals("We expect to find single membership for user " + userName, 1,
          mHandler.findMembershipsByUser(userName).size());
 
       assertNotNull(uHandler.removeUser(userName, true));
 
       assertNull(upHandler.findUserProfileByName(userName));
       assertEquals("We expect to find no membership for user " + userName, 0, mHandler.findMembershipsByUser(userName)
          .size());
 
       // try to find user after remove. We are supposed to get "null" instead of exception
       try
       {
          assertNull(uHandler.findUserByName(userName + "_"));
       }
       catch (Exception e)
       {
          fail("Exception should not be thrown");
       }
    }
 
    /**
     * Save user.
     */
    public void testSaveUser() throws Exception
    {
       createUser(userName);
 
       String newEmail = "new@Email";
       String displayName = "name";
 
       // change email and check
       User u = uHandler.findUserByName(userName);
       u.setEmail(newEmail);
 
       uHandler.saveUser(u, true);
 
       u = uHandler.findUserByName(userName);
       assertEquals(newEmail, u.getEmail());
       assertEquals(u.getDisplayName(), u.getFirstName() + " " + u.getLastName());
 
       u.setDisplayName(displayName);
       uHandler.saveUser(u, true);
 
       u = uHandler.findUserByName(userName);
       assertEquals(u.getDisplayName(), displayName);
    }
 
    /**
     * Create user.
     */
    public void testCreateUser() throws Exception
    {
       User u = uHandler.createUserInstance(userName);
       u.setEmail("email@test");
       u.setFirstName("first");
       u.setLastName("last");
       u.setPassword("pwd");
       uHandler.createUser(u, true);
 
       // check if user exists
       assertNotNull(uHandler.findUserByName(userName));
    }
 
    /**
     * Test get listeners.
     */
    public void testGetListeners() throws Exception
    {
       if (uHandler instanceof UserEventListenerHandler)
       {
          List<UserEventListener> list = ((UserEventListenerHandler) uHandler).getUserListeners();
          try
          {
             // check if we able to modify the list of listeners
             list.clear();
             fail("Exception should not be thrown");
          }
          catch (Exception e)
          {
          }
       }
    }
 }
