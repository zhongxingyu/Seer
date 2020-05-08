 /**
  * Copyright (c) 2009 - 2013 By: CWS, Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cws.esolutions.security.dao.usermgmt.impl;
 
 import java.util.Map;
 import java.util.List;
 import java.util.UUID;
 import org.junit.Test;
 import org.junit.After;
 import java.util.Arrays;
 import org.junit.Before;
 import org.junit.Assert;
 import java.util.HashMap;
 import java.util.Calendar;
 import java.util.ArrayList;
 import org.apache.commons.lang.RandomStringUtils;
 
 import com.cws.esolutions.security.dao.usermgmt.interfaces.UserManager;
 import com.cws.esolutions.security.listeners.SecurityServiceInitializer;
 import com.cws.esolutions.security.dao.usermgmt.enums.SearchRequestType;
 import com.cws.esolutions.security.dao.usermgmt.exception.UserManagementException;
 /**
  * eSolutionsCore
  * com.cws.esolutions.security.dao.usermgmt.impl
  * LDAPUserManagerTest.java
  *
  *
  *
  * $Id: $
  * $Author: $
  * $Date: $
  * $Revision: $
  * @author kmhuntly@gmail.com
  * @version 1.0
  *
  * History
  * ----------------------------------------------------------------------------
  * 35033355 @ Apr 5, 2013 2:20:02 PM
  *     Created.
  */
 public class LDAPUserManagerTest
 {
     private static final UserManager userManager = new LDAPUserManager();
 
     @Before
     public final void setUp()
     {
         try
         {
             SecurityServiceInitializer.initializeService("SecurityService/config/ServiceConfig.xml", "SecurityService/config/SecurityLogging.xml");
         }
         catch (Exception e)
         {
             Assert.fail(e.getMessage());
             System.exit(1);
         }
     }
 
     @Test
     public final void testAddAccountNoGroup()
     {
         List<String> list = new ArrayList<String>(
             Arrays.asList(
                 "testuser",
                 RandomStringUtils.randomAlphanumeric(64),
                 "USER",
                 "Test",
                 "User",
                 "test@test.com",
                 UUID.randomUUID().toString(),
                 "Test User"));
 
         try
         {
             Assert.assertTrue(userManager.addUserAccount(null, list, null));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     public final void testAddAccountWithGroup()
     {
         List<String> list = new ArrayList<String>(
                 Arrays.asList(
                     "testuser",
                     RandomStringUtils.randomAlphanumeric(64),
                     "USER",
                     "Test",
                     "User",
                     "test@test.com",
                     UUID.randomUUID().toString(),
                     "Test User"));
 
         try
         {
             Assert.assertTrue(userManager.addUserAccount(null, list, "esolutions"));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @Test
     public final void testSearchUsers()
     {
         try
         {
             List<String[]> searchList = userManager.searchUsers(SearchRequestType.GUID, "74d9729b-7fb2-4fef-874b-c9ee5d7a5a95");
 
             Assert.assertNotNull(searchList);
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @Test
     public final void testModifyUserSuspension()
     {
         try
         {
             Assert.assertTrue(userManager.modifyUserSuspension("khuntly", "74d9729b-7fb2-4fef-874b-c9ee5d7a5a95", false));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @Test
     public final void testModifyUserLockout()
     {
         try
         {
            Assert.assertTrue(userManager.modifyUserInformation("test", "99504de2-ac12-486a-b835-12c8c164d8bc", new HashMap<String, Object>()
                     {
                         private static final long serialVersionUID = 602188777075148683L;
 
                         {
                            put("cwsfailedpwdcount", 5);
                         }
                     }));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @Test
     public final void testModifyUserPassword()
     {
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DATE, 45);
 
         Map<String, Object> change = new HashMap<String, Object>();
         change.put("userPassword", "6TPeXOxpCKce2wPZMM3nIGtbN2BRk31guOO7utNwfvtjmGxqvLhBi/Atd0ZzxDlR2/5l0cKJgqiMTHCoXPjhwQ==");
         // change.put("cwsexpirydate", cal.getTimeInMillis());
 
         try
         {
             Assert.assertTrue(userManager.modifyUserInformation("khuntly", "74d9729b-7fb2-4fef-874b-c9ee5d7a5a95", change));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @Test
     public final void testModifyUserAnswers()
     {
         try
         {
             Assert.assertTrue(userManager.modifyUserInformation("khuntly", "74d9729b-7fb2-4fef-874b-c9ee5d7a5a95", new HashMap<String, Object>()
                     {
                         private static final long serialVersionUID = 602188777075148683L;
 
                         {
                             put("cwssecans1", "VxqLnzg918cdevQdl+aut3+o2UW40O3ozfz2iUWkOYBjTeRsiJBppeHlyuofEJw+");
                             put("cwssecans2", "VxqLnzg918cdevQdl+aut3+o2UW40O3ozfz2iUWkOYCiHy4/Zkct4Dsf1KnqTbZE");
                         }
                     }));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @Test
     public final void testModifyUserContact()
     {
         try
         {
             Assert.assertTrue(userManager.modifyUserInformation("khuntly", "74d9729b-7fb2-4fef-874b-c9ee5d7a5a95", new HashMap<String, Object>()
                     {
                         private static final long serialVersionUID = 602188777075148683L;
 
                         {
                             put("pager", "716-341-5669");
                             put("telephoneNumber", "716-341-5669");
                         }
                     }));
         }
         catch (UserManagementException umx)
         {
             Assert.fail(umx.getMessage());
         }
     }
 
     @After
     public void tearDown()
     {
         SecurityServiceInitializer.shutdown();
     }
 }
