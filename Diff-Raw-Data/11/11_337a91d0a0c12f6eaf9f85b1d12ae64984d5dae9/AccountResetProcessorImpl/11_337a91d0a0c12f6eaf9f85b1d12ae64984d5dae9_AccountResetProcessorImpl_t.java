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
 package com.cws.esolutions.security.processors.impl;
 
 import java.util.List;
 import java.util.Calendar;
 import java.sql.SQLException;

 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.RandomStringUtils;
 
 import com.cws.esolutions.security.enums.Role;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.security.dto.UserSecurity;
 import com.cws.esolutions.security.utils.PasswordUtils;
 import com.cws.esolutions.security.audit.dto.AuditEntry;
 import com.cws.esolutions.security.audit.enums.AuditType;
 import com.cws.esolutions.security.audit.dto.AuditRequest;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.security.enums.SecurityRequestStatus;
 import com.cws.esolutions.security.exception.SecurityServiceException;
 import com.cws.esolutions.security.processors.dto.AccountResetRequest;
 import com.cws.esolutions.security.processors.dto.AccountResetResponse;
 import com.cws.esolutions.security.dao.usermgmt.enums.SearchRequestType;
 import com.cws.esolutions.security.access.control.enums.AdminControlType;
 import com.cws.esolutions.security.audit.exception.AuditServiceException;
import com.cws.esolutions.security.processors.enums.LoginStatus;
 import com.cws.esolutions.security.processors.exception.AccountResetException;
 import com.cws.esolutions.security.processors.exception.AuthenticationException;
 import com.cws.esolutions.security.processors.interfaces.IAccountResetProcessor;
 import com.cws.esolutions.security.dao.userauth.exception.AuthenticatorException;
 import com.cws.esolutions.security.dao.usermgmt.exception.UserManagementException;
 import com.cws.esolutions.security.access.control.exception.AdminControlServiceException;
 /**
  * eSolutionsCore
  * com.cws.esolutions.security.processors.impl
  * AccountResetProcessorImpl.java
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
  * 35033355 @ Jul 10, 2013 3:38:34 PM
  *     Created.
  */
 public class AccountResetProcessorImpl implements IAccountResetProcessor
 {
     @Override
     public AccountResetResponse verifyResetRequest(final AccountResetRequest request) throws AccountResetException
     {
         final String methodName = IAccountResetProcessor.CNAME + "#verifyResetRequest(final AccountResetRequest request) throws AccountResetException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuthenticationRequest: {}", request);
         }
 
         UserAccount userAccount = null;
         AccountResetResponse response = new AccountResetResponse();
 
         final Calendar cal = Calendar.getInstance();
         final RequestHostInfo reqInfo = request.getHostInfo();
         final UserSecurity userSecurity = request.getUserSecurity();
                 
         if (DEBUG)
         {
             DEBUGGER.debug("Calendar: {}", cal);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserSecurity: {}", userSecurity);
         }
         
         try
         {
             cal.add(Calendar.MINUTE, secConfig.getResetTimeout());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("Reset expiry: {}", cal.getTimeInMillis());
             }
 
             // the request id should be in here, so lets make sure it exists
             List<String> resetData = userSec.getResetData(userSecurity.getResetRequestId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("resetData: {}", resetData);
             }
 
             final String commonName = resetData.get(0);
             final Long resetTimestamp = Long.valueOf(resetData.get(1));
 
             if (DEBUG)
             {
                 DEBUGGER.debug("commonName: {}", commonName);
                 DEBUGGER.debug("resetTimestamp: {}", resetTimestamp);
             }
 
             // make sure the timestamp is appropriate
             if (resetTimestamp <= cal.getTimeInMillis())
             {
                 if (StringUtils.isNotEmpty(commonName))
                 {
                     // good, now we have something we can look for
                     List<String[]> userList = userManager.searchUsers(SearchRequestType.GUID, commonName);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("userList: {}", userList);
                     }
 
                     // we expect back only one
                     if ((userList != null) && (userList.size() == 1))
                     {
                         // good, we can continue
                         String[] userData = userList.get(0);
 
                         if (DEBUG)
                         {
                             for (String str : userData)
                             {
                                 DEBUGGER.debug("userData: {}", str);
                             }
                         }
 
                         userAccount = new UserAccount();
                        userAccount.setStatus(LoginStatus.RESET);
                         userAccount.setGuid(userData[0]);
                         userAccount.setUsername(userData[1]);
                         userAccount.setGivenName(userData[2]);
                         userAccount.setSurname(userData[3]);
                         userAccount.setDisplayName(userData[4]);
                         userAccount.setEmailAddr(userData[5]);
                         userAccount.setRole(Role.valueOf(userData[8]));
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("UserAccount: {}", userAccount);
                         }
 
                         // remove the reset request
                         boolean isRemoved = userSec.removeResetData(userData[0], userSecurity.getResetRequestId());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("isRemoved: {}", isRemoved);
                         }
 
                         if (!(isRemoved))
                         {
                             ERROR_RECORDER.error("Failed to remove provided reset request from datastore");
                         }
 
                         response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                         response.setResponse("Successfully loaded user reset request");
                         response.setUserAccount(userAccount);
                     }
                     else
                     {
                         throw new AuthenticationException("Multiple user accounts were located for the provided information");
                     }
                 }
                 else
                 {
                     response.setRequestStatus(SecurityRequestStatus.FAILURE);
                     response.setResponse("The reset request either does not exist or has expired.");
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuthenticationResponse: {}", response);
                 }
             }
             else
             {
                 throw new AuthenticationException("Reset request has expired.");
             }
         }
         catch (SecurityServiceException ssx)
         {
             ERROR_RECORDER.error(ssx.getMessage(), ssx);
 
             throw new AccountResetException(ssx.getMessage(), ssx);
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new AccountResetException(sqx.getMessage(), sqx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.VERIFYRESET);
                 auditEntry.setUserAccount(userAccount);
                 auditEntry.setApplicationId(request.getApplicationId());
                 auditEntry.setApplicationName(request.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditEntry: {}", auditEntry);
                 }
 
                 AuditRequest auditRequest = new AuditRequest();
                 auditRequest.setAuditEntry(auditEntry);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditRequest: {}", auditRequest);
                 }
 
                 auditor.auditRequest(auditRequest);
             }
             catch (AuditServiceException asx)
             {
                 ERROR_RECORDER.error(asx.getMessage(), asx);
             }
         }
 
         return response;
     }
 
     @Override
     public AccountResetResponse resetUserPassword(final AccountResetRequest request) throws AccountResetException
     {
         final String methodName = IAccountResetProcessor.CNAME + "#resetUserPassword(final AccountResetRequest request) throws AccountResetException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountResetRequest: {}", request);
         }
 
         AccountResetResponse response = new AccountResetResponse();
 
         final Calendar calendar = Calendar.getInstance();
         final RequestHostInfo reqInfo = request.getHostInfo();
         final UserAccount reqAccount = request.getRequestor();
         final UserAccount userAccount = request.getUserAccount();
 
         calendar.add(Calendar.DATE, secConfig.getPasswordExpiration());
 
         if (DEBUG)
         {
             DEBUGGER.debug("Calendar: {}", calendar);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", reqAccount);
             DEBUGGER.debug("UserAccount: {}", userAccount);
         }
 
         try
         {
             if (!(StringUtils.equals(userAccount.getGuid(), reqAccount.getGuid())))
             {
                 // requesting user is not the same as the user being reset. authorize
                 boolean isAdminAuthorized = adminControl.adminControlService(reqAccount, AdminControlType.USER_ADMIN);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isAdminAuthorized: {}", isAdminAuthorized);
                 }
 
                 if (!(isAdminAuthorized))
                 {
                     // nope !
                     throw new AccountResetException("Unauthorized modification request attempted by " + reqAccount.getUsername() + " for user " + userAccount.getUsername());
                 }
             }
 
             // otherwise, keep going
             // this is a reset request, so we need to do a few things
             // 1, we need to generate a unique id that we can email off
             // to the user, that we can then look up to confirm
             // then once we have that we can actually do the reset
             // first, change the existing password
             // 128 character values - its possible that the reset is
             // coming as a result of a possible compromise
             String tmpPassword = PasswordUtils.encryptText(RandomStringUtils.randomAlphanumeric(secConfig.getPasswordMaxLength()), RandomStringUtils.randomAlphanumeric(secConfig.getSaltLength()),
                     secConfig.getAuthAlgorithm(), secConfig.getIterations());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("Value: {}", tmpPassword);
             }
 
             if (StringUtils.isNotEmpty(tmpPassword))
             {
                 // update the authentication datastore with the new password
                 // we never show the user the password, we're only doing this
                 // to prevent unauthorized access (or further unauthorized access)
                 // we get a return code back but we aren't going to use it really
                 boolean isComplete = authenticator.changeUserPassword(userAccount.getGuid(), tmpPassword, System.currentTimeMillis());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isComplete: {}", isComplete);
                 }
 
                 // now generate a temporary id to stuff into the database
                 // this will effectively replace the current salt value
                 String resetId = RandomStringUtils.randomAlphanumeric(secConfig.getResetIdLength());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("resetId: {}", resetId);
                 }
 
                 if (StringUtils.isNotEmpty(resetId))
                 {
                     isComplete = userSec.insertResetData(userAccount.getGuid(), resetId, request.getSmsCode());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isComplete: {}", isComplete);
                     }
 
                     if (isComplete)
                     {
                         // load the user account for the email response
                         List<Object> userData = userManager.loadUserAccount(userAccount.getGuid());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("UserData: {}", userData);
                         }
 
                         if ((userData != null) && (!(userData.isEmpty())))
                         {
                             UserAccount responseAccount = new UserAccount();
                             responseAccount.setGuid((String) userData.get(0));
                             responseAccount.setUsername((String) userData.get(1));
                             responseAccount.setGivenName((String) userData.get(2));
                             responseAccount.setSurname((String) userData.get(3));
                             responseAccount.setDisplayName((String) userData.get(4));
                             responseAccount.setEmailAddr((String) userData.get(5));
                             responseAccount.setPagerNumber((String) userData.get(6));
                             responseAccount.setTelephoneNumber((String) userData.get(7));
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("UserAccount: {}", responseAccount);
                             }
 
                             response.setResetId(resetId);
                             response.setUserAccount(responseAccount);
                             response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                             response.setResponse("Successfully generated password reset request");
                         }
                         else
                         {
                             throw new AuthenticatorException("Failed to locate user account in authentication repository. Cannot continue.");
                         }
                     }
                     else
                     {
                         throw new AccountResetException("Unable to insert password identifier into database. Cannot continue.");
                     }
                 }
                 else
                 {
                     throw new AccountResetException("Unable to generate a unique identifier. Cannot continue.");
                 }
             }
             else
             {
                 throw new AccountResetException("Failed to generate a temporary password. Cannot continue.");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
             
             throw new AccountResetException(sqx.getMessage(), sqx);
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
             
             throw new AccountResetException(acsx.getMessage(), acsx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountResetException(umx.getMessage(), umx);
         }
         catch (AuthenticatorException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
 
             throw new AccountResetException(ax.getMessage(), ax);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.RESETPASS);
                 auditEntry.setUserAccount(userAccount);
                 auditEntry.setApplicationId(request.getApplicationId());
                 auditEntry.setApplicationName(request.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditEntry: {}", auditEntry);
                 }
 
                 AuditRequest auditRequest = new AuditRequest();
                 auditRequest.setAuditEntry(auditEntry);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditRequest: {}", auditRequest);
                 }
 
                 auditor.auditRequest(auditRequest);
             }
             catch (AuditServiceException asx)
             {
                 ERROR_RECORDER.error(asx.getMessage(), asx);
             }
         }
 
         return response;
     }
 
     @Override
     public AccountResetResponse getSecurityQuestions(final AccountResetRequest request) throws AccountResetException
     {
         final String methodName = IAccountResetProcessor.CNAME + "#getSecurityQuestions(final AccountResetRequest request) throws AccountResetException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountResetRequest: {}", request);
         }
 
         AccountResetResponse response = new AccountResetResponse();
 
         final UserAccount reqUser = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getHostInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", reqUser);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             List<String> questionList = secRef.obtainSecurityQuestionList();
 
             if (DEBUG)
             {
                 DEBUGGER.debug("questionList: {}", questionList);
             }
 
             response.setQuestionList(questionList);
             response.setRequestStatus(SecurityRequestStatus.SUCCESS);
             response.setResponse("Successfully loaded available security questions");
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AccountResetResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
             
             throw new AccountResetException(sqx.getMessage(), sqx);
         }
 
         return response;
     }
 }
