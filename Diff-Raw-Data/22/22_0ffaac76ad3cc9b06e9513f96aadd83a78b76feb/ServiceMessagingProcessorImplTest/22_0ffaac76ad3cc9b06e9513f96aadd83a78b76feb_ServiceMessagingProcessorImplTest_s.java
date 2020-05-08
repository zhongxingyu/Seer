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
 package com.cws.esolutions.core.processors.impl;
 
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.apache.commons.lang.RandomStringUtils;
 
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.security.dto.UserSecurity;
 import com.cws.esolutions.core.processors.dto.ServiceMessage;
 import com.cws.esolutions.core.processors.dto.MessagingRequest;
 import com.cws.esolutions.security.enums.SecurityRequestStatus;
 import com.cws.esolutions.core.processors.dto.MessagingResponse;
 import com.cws.esolutions.security.dao.userauth.enums.LoginType;
 import com.cws.esolutions.security.processors.enums.LoginStatus;
 import com.cws.esolutions.core.listeners.CoreServiceInitializer;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.security.listeners.SecurityServiceInitializer;
 import com.cws.esolutions.security.processors.dto.AuthenticationRequest;
 import com.cws.esolutions.core.processors.interfaces.IMessagingProcessor;
 import com.cws.esolutions.security.dao.userauth.enums.AuthenticationType;
 import com.cws.esolutions.security.processors.dto.AuthenticationResponse;
 import com.cws.esolutions.core.processors.exception.MessagingServiceException;
 import com.cws.esolutions.security.processors.impl.AuthenticationProcessorImpl;
 import com.cws.esolutions.security.processors.interfaces.IAuthenticationProcessor;
 /**
  * eSolutionsCore
  * com.cws.esolutions.core.processors.impl
  * ServiceMessagingProcessorTest.java
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
  * 35033355 @ Apr 29, 2013 2:51:21 PM
  *     Created.
  */
 public class ServiceMessagingProcessorImplTest
 {
     private UserAccount userAccount = new UserAccount();
     private RequestHostInfo hostInfo = new RequestHostInfo();
 
     private static final IMessagingProcessor processor = new ServiceMessagingProcessorImpl();
 
     private static final String SERVICEID = "5C0B0A54-2456-45C9-A435-B485ED36FAC7";
 
     @Before
     public final void setUp() throws Exception
     {
         try
         {
             CoreServiceInitializer.initializeService("eSolutionsCore/config/ServiceConfig.xml", "logging/logging.xml");
 
             SecurityServiceInitializer.initializeService("SecurityService/config/ServiceConfig.xml", "SecurityService/config/SecurityLogging.xml");
 
             IAuthenticationProcessor agentAuth = new AuthenticationProcessorImpl();
             hostInfo.setHostAddress("127.0.0.1");
             hostInfo.setHostName("localhost");
 
             UserAccount account = new UserAccount();
             account.setUsername("khuntly");
             hostInfo.setSessionId(RandomStringUtils.randomAlphanumeric(32));
 
             try
             {
                 AuthenticationRequest userRequest = new AuthenticationRequest();
                 userRequest.setApplicationName("esolutions");
                 userRequest.setAuthType(AuthenticationType.LOGIN);
                 userRequest.setLoginType(LoginType.USERNAME);
                 userRequest.setUserAccount(account);
                 userRequest.setApplicationId("B760E92F-827A-42E7-9E8D-64334657BA83");
 
                 AuthenticationResponse userResponse = agentAuth.processAgentLogon(userRequest);
 
                 if (userResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                 {
                     UserAccount authUser = userResponse.getUserAccount();
 
                     if (authUser.getStatus() == LoginStatus.SUCCESS)
                     {
                         UserSecurity userSecurity = new UserSecurity();
                        userSecurity.setPassword("Ariana16*");
 
                         AuthenticationRequest passRequest = new AuthenticationRequest();
                         passRequest.setApplicationName("esolutions");
                         passRequest.setAuthType(AuthenticationType.LOGIN);
                         passRequest.setLoginType(LoginType.PASSWORD);
                         passRequest.setUserAccount(authUser);
                         passRequest.setUserSecurity(userSecurity);
                         passRequest.setApplicationId("B760E92F-827A-42E7-9E8D-64334657BA83");
 
                         AuthenticationResponse passResponse = agentAuth.processAgentLogon(passRequest);
 
                         if (passResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                         {
                             userAccount = passResponse.getUserAccount();
                             hostInfo.setSessionId(RandomStringUtils.randomAlphanumeric(32));
                         }
                         else
                         {
                             Assert.fail("Account login failed");
                         }
                     }
                     else
                     {
                         Assert.fail("Account login failed");
                     }
                 }
                 else
                 {
                     Assert.fail("Account login failed");
                 }
             }
             catch (Exception e)
             {
                 Assert.fail(e.getMessage());
             }
         }
         catch (Exception ex)
         {
             Assert.fail(ex.getMessage());
 
             System.exit(-1);
         }
     }
 
     @Test
     public final void addNewMessage()
     {
         ServiceMessage message = new ServiceMessage();
         message.setMessageId(RandomStringUtils.randomAlphanumeric(16));
         message.setMessageTitle("Test Message");
         message.setMessageText("This is a test");
         message.setMessageAuthor(userAccount.getUsername());
         message.setAuthorEmail(userAccount.getEmailAddr());
         message.setDoesExpire(false);
         message.setIsActive(true);
 
         MessagingRequest request = new MessagingRequest();
         request.setRequestInfo(hostInfo);
         request.setServiceId(ServiceMessagingProcessorImplTest.SERVICEID);
         request.setUserAccount(userAccount);
         request.setSvcMessage(message);
 
         try
         {
             MessagingResponse response = processor.addNewMessage(request);
 
             Assert.assertEquals(CoreServicesStatus.SUCCESS, response.getRequestStatus());
         }
         catch (MessagingServiceException msx)
         {
             Assert.fail(msx.getMessage());
         }
     }
 
     @Test
     public final void deleteExistingMessage()
     {
         Assert.fail("Not yet implemented");
     }
 
     public final void updateExistingMessage()
     {
         Assert.fail("Not yet implemented");
     }
 
     @Test
     public final void showMessages()
     {
         MessagingRequest request = new MessagingRequest();
         request.setRequestInfo(hostInfo);
         request.setUserAccount(userAccount);
         request.setServiceId(ServiceMessagingProcessorImplTest.SERVICEID);
 
         try
         {
             MessagingResponse response = processor.showMessages(request);
 
             Assert.assertEquals(CoreServicesStatus.SUCCESS, response.getRequestStatus());
         }
         catch (MessagingServiceException msx)
         {
             Assert.fail(msx.getMessage());
         }
     }
 
     @Test
     public final void showMessage()
     {
         ServiceMessage message = new ServiceMessage();
         message.setMessageId("muUlODU6k1kA0L3q");
 
         MessagingRequest request = new MessagingRequest();
         request.setRequestInfo(hostInfo);
         request.setServiceId(ServiceMessagingProcessorImplTest.SERVICEID);
         request.setUserAccount(userAccount);
         request.setSvcMessage(message);
 
         try
         {
             MessagingResponse response = processor.showMessage(request);
 
             Assert.assertEquals(CoreServicesStatus.SUCCESS, response.getRequestStatus());
         }
         catch (MessagingServiceException msx)
         {
             Assert.fail(msx.getMessage());
         }
     }
 
     @After
     public void tearDown()
     {
         SecurityServiceInitializer.shutdown();
     }
 }
