 /*
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
 package com.cws.esolutions.security.utils;
 /*
  * Project: eSolutionsCore
  * Package: com.cws.esolutions.security.utils
  * File: PasswordUtilsTest.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 import org.apache.commons.lang.RandomStringUtils;
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Assert;
 
 import com.cws.esolutions.security.SecurityServiceBean;
 import com.cws.esolutions.security.utils.PasswordUtils;
 import com.cws.esolutions.core.controllers.ResourceController;
 import com.cws.esolutions.core.exception.CoreServiceException;
 import com.cws.esolutions.security.listeners.SecurityServiceInitializer;
 
 public class PasswordUtilsTest
 {
     private static final SecurityServiceBean bean = SecurityServiceBean.getInstance();
 
     @Before
     public void setUp()
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
     public void testCreateHash()
     {
        final String salt = "XVyxWFeK4BjExoS7vzqiFSYa4j3EQeHtynHxto3SueqCboIwDVI7q7lOMz8Rm8ej";
        final String pass = "demo";
 
         System.out.println(salt);
         System.out.println(PasswordUtils.encryptText(pass, salt, bean.getConfigData().getSecurityConfig().getAuthAlgorithm(), bean.getConfigData().getSecurityConfig().getIterations()));
     }
 
     @Test
     public void testOneWayHash()
     {
         Assert.assertEquals("xdwcvNbTtdBkcxvtn3g5BTHz1naNiq3tZAn255ai1hZtRUPiA0TyoLPs3fP6lC9YcvyNcreuFqEuse10nnyHAg==",
                 PasswordUtils.encryptText("TestPasswordValue", "zHnDJVgtiJy3FNFDfSe9ZK1KW97zd1oDmA8awAoW7QnDR6i2wd9AfV2NmXOOVYJO",
                         bean.getConfigData().getSecurityConfig().getAuthAlgorithm(), bean.getConfigData().getSecurityConfig().getIterations()));
     }
 
     @Test
     public void testTwoWayHash()
     {
         Assert.assertEquals("bXUNMHkQsn0XYqKYQIqb5/rBcjqgP+iI9oaRqsFwZg6ksO6VXygs7Z1Dw08BaaecTteff8Knx8gnR+l6YoE41l2jLG4ZkFL4y4M6/pqP7fU=",
                 PasswordUtils.encryptText("TestTwoWayHash", "PWQKRN8J60s9gg7Qg3eM6o19ggPsLiIDfjCXohJ9qgfMMILnDmPl79ipeZ56TEJI"));
     }
 
     @Test
     public void testDecryptText()
     {
         Assert.assertEquals("TUX_t3st", PasswordUtils.decryptText("ivKO8kEZU3lOOgmdhp0PCgkn4FTs2yYr+XCbFpd7SRrUR1BjvOCTXpwEtFYcsjE6",
                 "VQNLG99rmhcij4lrWfJV3tahkUeWhVhD".length()));
     }
 
     @After
     public void tearDown()
     {
         try
         {
             ResourceController.closeAuthConnection(bean.getConfigData().getAuthRepo(), true, bean.getResourceBean());
         }
         catch (CoreServiceException csx)
         {
 
         }
     }
 }
