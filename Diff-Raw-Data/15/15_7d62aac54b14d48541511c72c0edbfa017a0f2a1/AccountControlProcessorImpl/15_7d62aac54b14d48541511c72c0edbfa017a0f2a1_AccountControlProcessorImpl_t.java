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
 
 import java.util.Map;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.sql.SQLException;
 import java.lang.reflect.Field;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.RandomStringUtils;
 
 import com.cws.esolutions.security.enums.Role;
 import com.cws.esolutions.security.enums.SaltType;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.security.dto.UserSecurity;
 import com.cws.esolutions.security.SecurityConstants;
 import com.cws.esolutions.security.utils.PasswordUtils;
 import com.cws.esolutions.security.audit.dto.AuditEntry;
 import com.cws.esolutions.security.audit.enums.AuditType;
 import com.cws.esolutions.security.audit.dto.AuditRequest;
 import com.cws.esolutions.security.audit.dto.AuditResponse;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.security.enums.SecurityRequestStatus;
 import com.cws.esolutions.security.keymgmt.dto.KeyManagementRequest;
 import com.cws.esolutions.security.keymgmt.dto.KeyManagementResponse;
 import com.cws.esolutions.security.dao.usermgmt.enums.SearchRequestType;
 import com.cws.esolutions.security.processors.dto.AccountControlRequest;
 import com.cws.esolutions.security.audit.exception.AuditServiceException;
 import com.cws.esolutions.security.processors.dto.AccountControlResponse;
 import com.cws.esolutions.security.access.control.enums.AdminControlType;
 import com.cws.esolutions.security.keymgmt.exception.KeyManagementException;
 import com.cws.esolutions.security.processors.exception.AccountControlException;
 import com.cws.esolutions.security.dao.usermgmt.exception.UserManagementException;
 import com.cws.esolutions.security.processors.interfaces.IAccountControlProcessor;
 import com.cws.esolutions.security.access.control.exception.AdminControlServiceException;
 /**
  * SecurityService
  * com.cws.esolutions.security.processors.impl
  * AccountControlProcessorImpl.java
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
  * kh05451 @ Oct 29, 2012 9:44:46 AM
  *     Created.
  */
 public class AccountControlProcessorImpl implements IAccountControlProcessor
 {
     @Override
     public AccountControlResponse createNewUser(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#createNewUser(final CreateUserRequest createReq) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final RequestHostInfo reqInfo = request.getHostInfo();
         final UserAccount userAccount = request.getUserAccount();
         final UserSecurity userSecurity = request.getUserSecurity();
         final String newUserSalt = RandomStringUtils.randomAlphanumeric(secConfig.getSaltLength());
         final StringBuilder userDN = new StringBuilder()
             .append(authData.getUserId() + "=" + userAccount.getUsername() + ",")
             .append(authRepo.getRepositoryUserBase());
 
         if (DEBUG)
         {
             DEBUGGER.debug("Requestor: {}", requestor);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("UserSecurity: {}", userSecurity);
             DEBUGGER.debug("newUserSalt: {}", newUserSalt);
             DEBUGGER.debug("userDN: {}", userDN);
         }
 
         try
         {
             boolean isAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isAuthorized: {}", isAuthorized);
             }
 
             if (isAuthorized)
             {
                 String userGuid = (StringUtils.isNotEmpty(userAccount.getGuid())) ? userAccount.getGuid() : UUID.randomUUID().toString();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Value: {}", userGuid);
                 }
 
                 int x = 0;
 
                 while (true)
                 {
                     if (x == 10)
                     {
                         throw new AccountControlException("Failed to generate a unique user GUID");
                     }
 
                     try
                     {
                         userManager.validateUserAccount(userAccount.getUsername(), userGuid);
 
                         break;
                     }
                     catch (UserManagementException umx)
                     {
                         ERROR_RECORDER.error(umx.getMessage(), umx);
 
                         if (StringUtils.contains(umx.getMessage(), "UUID"))
                         {
                             userGuid = UUID.randomUUID().toString();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("Value: {}", userGuid);
                             }
 
                             x++;
 
                             continue;
                         }
                         else
                         {
                             response.setRequestStatus(SecurityRequestStatus.FAILURE);
                             response.setResponse("A user already exists with the provided username.");
 
                             return response;
                         }
                     }
                 }
 
                 // insert the user salt
                 boolean isSaltInserted = userSec.addUserSalt(userGuid, newUserSalt, SaltType.LOGON.name());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isSaltInserted: {}", isSaltInserted);
                 }
 
                 if (isSaltInserted)
                 {
                     String newPassword = PasswordUtils.encryptText(RandomStringUtils.randomAlphanumeric(secConfig.getPasswordMaxLength()), newUserSalt,
                             secConfig.getAuthAlgorithm(), secConfig.getIterations());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Value: {}", newPassword);
                     }
 
                     List<String> accountData = new ArrayList<String>(
                         Arrays.asList(
                                 userAccount.getUsername(),
                                 newPassword,
                                 userAccount.getRole().name(),
                                 userAccount.getSurname(),
                                 userAccount.getGivenName(),
                                 userAccount.getEmailAddr(),
                                 userGuid,
                                 userAccount.getDisplayName()));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("accountData: {}", accountData);
                     }
 
                     boolean isUserCreated = userManager.addUserAccount(userDN.toString(), accountData, request.getApplicationName());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isUserCreated: {}", isUserCreated);
                     }
 
                     if (isUserCreated)
                     {
                         // generate a key for the user
                         KeyManagementRequest keyRequest = new KeyManagementRequest();
                         keyRequest.setKeyAlgorithm(keyConfig.getKeyAlgorithm());
                         keyRequest.setGuid(userGuid);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("KeyManagementRequest: {}", keyRequest);
                         }
 
                         KeyManagementResponse keyResponse = keyManager.createKeys(keyRequest);
 
                         if (keyResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                         {
                             response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                             response.setResponse("New user successfully created");
 
                             // assign services (if any)
                             if ((request.getServicesList() != null) && (request.getServicesList().size() != 0))
                             {
                                 try
                                 {
                                     for (String svcId : request.getServicesList())
                                     {
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("Service: {}", svcId);
                                         }
 
                                         boolean isServiceAdded = userSvcs.addServiceToUser(userAccount.getGuid(), svcId);
 
                                         if (!(isServiceAdded))
                                         {
                                             throw new SQLException("Failed to provision service " + svcId + " for the provided user");
                                         }
                                     }
                                 }
                                 catch (SQLException sqx)
                                 {
                                     ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
                                     response.setResponse("Successfully added the new user account, but failed to provision services. Please review application logs.");
                                 }
                             }
                         }
                         else
                         {
                             // failed to generate user keypair
                             userManager.removeUserAccount(userAccount.getUsername(), userGuid);
                             userSec.removeUserData(userGuid);
 
                             throw new AccountControlException("Failed to provision new user account: failed to generate keys for the provided user");
                         }
                     }
                     else
                     {
                         // failed to add the user to the repository
                         throw new AccountControlException("Failed to add user to the account repository");
                     }
                 }
                 else
                 {
                     // failed to insert salt
                     throw new AccountControlException("Failed to provision new user account: failed to insert the generated salt value");
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("The requesting user was NOT authorized to perform the operation");
             }
         }
         catch (AccountControlException acx)
         {
             ERROR_RECORDER.error(acx.getMessage(), acx);
 
             throw new AccountControlException(acx.getMessage(), acx);
         }
         catch (AdminControlServiceException acx)
         {
             ERROR_RECORDER.error(acx.getMessage(), acx);
 
             throw new AccountControlException(acx.getMessage(), acx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new AccountControlException(sqx.getMessage(), sqx);
         }
         catch (KeyManagementException kmx)
         {
             ERROR_RECORDER.error(kmx.getMessage(), kmx);
             
             throw new AccountControlException(kmx.getMessage(), kmx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.CREATEUSER);
                 auditEntry.setUserAccount(requestor);
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
     public AccountControlResponse removeUserAccount(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#removeUserAccount(final AccountControlRequest) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final UserAccount account = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getHostInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount reqUser: {}", requestor);
             DEBUGGER.debug("UserAccount account: {}", account);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isAuthorized: {}", isAuthorized);
             }
 
             if (isAuthorized)
             {
                 
                 // delete account
                 boolean isComplete = userManager.removeUserAccount(account.getUsername(), account.getGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isComplete: {}", isComplete);
                 }
 
                 if (isComplete)
                 {
                     response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                     response.setResponse("User account was successfully deleted");
                 }
                 else
                 {
                     response.setRequestStatus(SecurityRequestStatus.FAILURE);
                     response.setResponse("Failed to delete user account");
                 }
             }
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.DELETEUSER);
                 auditEntry.setUserAccount(requestor);
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
     public AccountControlResponse modifyUserSuspension(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#modifyUserSuspension(final AccountControlRequest) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final UserAccount account = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getHostInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount reqUser: {}", requestor);
             DEBUGGER.debug("UserAccount account: {}", account);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isAuthorized: {}", isAuthorized);
             }
 
             if (isAuthorized)
             {
                 // we will only have a guid here - so we need to load the user account
                 List<Object> userData = userManager.loadUserAccount(account.getGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("List<Object>: {}", userData);
                 }
 
                 if ((userData != null) && (userData.size() != 0))
                 {
                    boolean isComplete = userManager.modifyUserSuspension((String) userData.get(1), (String) userData.get(0), account.isSuspended());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isComplete: {}", isComplete);
                     }
 
                     if (isComplete)
                     {
                         response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                         response.setResponse("Successfully performed suspension modification.");
                     }
                     else
                     {
                         response.setRequestStatus(SecurityRequestStatus.FAILURE);
                         response.setResponse("Failed to perform suspension modification for requested user.");
                     }
                 }
                 else
                 {
                     throw new AccountControlException("Failed to locate user account for given GUID");
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("The requesting user was NOT authorized to perform the operation");
             }
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.SUSPENDUSER);
                 auditEntry.setUserAccount(requestor);
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
     public AccountControlResponse modifyUserLockout(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#modifyUserLockout(final AccountControlRequest) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final UserAccount account = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getHostInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount reqUser: {}", requestor);
             DEBUGGER.debug("UserAccount account: {}", account);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isAuthorized: {}", isAuthorized);
             }
 
             if (isAuthorized)
             {
                 // we will only have a guid here - so we need to load the user account
                 List<Object> userData = userManager.loadUserAccount(account.getGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("List<Object>: {}", userData);
                 }
 
                 if ((userData != null) && (userData.size() != 0))
                 {
                     Map<String, Object> requestMap = new HashMap<String, Object>()
                     {
                         private static final long serialVersionUID = -4501815670500496164L;
 
                         {
                             put(authData.getLockCount(), account.getFailedCount());
                         }
                     };
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("requestMap: {}", requestMap);
                     }
 
                    boolean isComplete = userManager.modifyUserInformation((String) userData.get(1), (String) userData.get(0), requestMap);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isComplete: {}", isComplete);
                     }
 
                     if (isComplete)
                     {
                         response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                         response.setResponse("Successfully performed suspension modification.");
                     }
                     else
                     {
                         response.setRequestStatus(SecurityRequestStatus.FAILURE);
                         response.setResponse("Failed to perform suspension modification for requested user.");
                     }
                 }
                 else
                 {
                     throw new AccountControlException("Failed to locate user account for given GUID");
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("The requesting user was NOT authorized to perform the operation");
             }
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.SUSPENDUSER);
                 auditEntry.setUserAccount(requestor);
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
     public AccountControlResponse modifyUserRole(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#modifyUserRole(final AccountControlRequest) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final UserAccount account = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getHostInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount reqUser: {}", requestor);
             DEBUGGER.debug("UserAccount account: {}", account);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isAuthorized: {}", isAuthorized);
             }
 
             if (isAuthorized)
             {
                 // we will only have a guid here - so we need to load the user account
                 List<Object> userData = userManager.loadUserAccount(account.getGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("List<Object>: {}", userData);
                 }
 
                 if ((userData != null) && (userData.size() != 0))
                 {
                     Map<String, Object> requestMap = new HashMap<String, Object>()
                     {
                         private static final long serialVersionUID = -4501815670500496164L;
 
                         {
                             put(authData.getUserRole(), account.getRole());
                         }
                     };
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("requestMap: {}", requestMap);
                     }
 
                     boolean isComplete = userManager.modifyUserInformation((String) userData.get(1), (String) userData.get(0), requestMap);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isComplete: {}", isComplete);
                     }
 
                     if (isComplete)
                     {
                         response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                         response.setResponse("Successfully performed role modification.");
                     }
                     else
                     {
                         response.setRequestStatus(SecurityRequestStatus.FAILURE);
                         response.setResponse("Failed to perform role modification for requested user.");
                     }
                 }
                 else
                 {
                     throw new AccountControlException("Failed to locate user account for given GUID");
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("The requesting user was NOT authorized to perform the operation");
             }
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.MODIFYUSER);
                 auditEntry.setUserAccount(requestor);
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
     public AccountControlResponse searchAccounts(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#searchAccounts(final AccountControlRequest request) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final RequestHostInfo reqInfo = request.getHostInfo();
         final UserAccount searchUser = request.getUserAccount();
         final SearchRequestType searchType = request.getSearchType();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", requestor);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", searchUser);
             DEBUGGER.debug("SearchRequestType: {}", searchType);
         }
 
         try
         {
             boolean isUserAuthorized = false;
 
             if (searchType != SearchRequestType.FORGOTUID)
             {
                 isUserAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
             }
             else
             {
                 isUserAuthorized = true;
             }
 
             if (isUserAuthorized)
             {
                 List<String[]> userList = null;
 
                 switch (searchType)
                 {
                     case FORGOTUID:
                         userList = userManager.searchUsers(SearchRequestType.EMAILADDR, searchUser.getEmailAddr());
 
                         break;
                     default:
                         String searchData = null;
                         SearchRequestType requestType = null;
 
                         for (Field field : searchUser.getClass().getDeclaredFields())
                         {
                             field.setAccessible(true); // private fields, make them accessible
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("field: {}", field);
                             }
 
                             if (!(field.getName().equals("methodName")) &&
                                     (!(field.getName().equals("CNAME"))) &&
                                     (!(field.getName().equals("DEBUGGER"))) &&
                                     (!(field.getName().equals("DEBUG"))) &&
                                     (!(field.getName().equals("serialVersionUID"))))
                             {
                                 try
                                 {
                                     if (field.get(searchUser) != null)
                                     {
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("Value: {}", field.get(searchUser));
                                         }
 
                                         searchData = (String) field.get(searchUser);
                                         requestType = SearchRequestType.valueOf(field.getName().toUpperCase());
 
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("requestType: {}", requestType);
                                             DEBUGGER.debug("searchData: {}", searchData);
                                         }
 
                                         break;
                                     }
                                 }
                                 catch (IllegalAccessException iax)
                                 {
                                     ERROR_RECORDER.error(iax.getMessage(), iax);
                                 }
                             }
                         }
 
                         userList = userManager.searchUsers(requestType, searchData);
 
                         break;
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("userList: {}", userList);
                 }
 
                 if ((userList != null) && (userList.size() != 0))
                 {
                     if ((searchType == SearchRequestType.FORGOTUID) && (userList.size() != 1))
                     {
                         response.setRequestStatus(SecurityRequestStatus.FAILURE);
                         response.setResponse("Multiple accounts were located with the provided information.");
                     }
                     else
                     {
                         List<UserAccount> userAccounts = new ArrayList<UserAccount>();
 
                         for (String[] userData : userList)
                         {
                             UserAccount userInfo = new UserAccount();
                             userInfo.setGuid(userData[0]);
                             userInfo.setUsername(userData[1]);
                             userInfo.setGivenName(userData[2]);
                             userInfo.setSurname(userData[3]);
                             userInfo.setEmailAddr(userData[5]);
                             userInfo.setDisplayName(userData[4]);
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("UserAccount: {}", userInfo);
                             }
 
                             userAccounts.add(userInfo);
                         }
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("userAccounts: {}", userAccounts);
                         }
 
                         response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                         response.setResponse("Successfully loaded matching accounts");
                         response.setUserList(userAccounts);
                     }
                 }
                 else
                 {
                     response.setRequestStatus(SecurityRequestStatus.FAILURE);
                     response.setResponse("No accounts were found with the provided data");
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("Requesting user account was NOT authorized to perform the operation");
             }
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         finally
         {
             if (searchType != SearchRequestType.FORGOTUID)
             {
                 // audit
                 try
                 {
                     AuditEntry auditEntry = new AuditEntry();
                     auditEntry.setHostInfo(reqInfo);
                     auditEntry.setAuditType(AuditType.SEARCHACCOUNTS);
                     auditEntry.setUserAccount(requestor);
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
         }
 
         return response;
     }
 
     @Override
     public AccountControlResponse loadUserAccount(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#loadUserAccount(final AccountControlRequest request) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AccountControlRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final RequestHostInfo reqInfo = request.getHostInfo();
         final UserAccount searchUser = request.getUserAccount();
         final SearchRequestType searchType = request.getSearchType();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", requestor);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", searchUser);
             DEBUGGER.debug("SearchRequestType: {}", searchType);
         }
 
         try
         {
             boolean isUserAuthorized = false;
 
             if (searchType != SearchRequestType.FORGOTUID)
             {
                 isUserAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
             }
             else
             {
                 isUserAuthorized = true;
             }
 
             if (isUserAuthorized)
             {
                 List<Object> userData = userManager.loadUserAccount(searchUser.getGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("List<Object>: {}", userData);
                 }
 
                 if ((userData != null) && (!(userData.isEmpty())))
                 {
                     UserAccount userAccount = new UserAccount();
                     userAccount.setGuid((String) userData.get(0));
                     userAccount.setUsername((String) userData.get(1));
                     userAccount.setGivenName((String) userData.get(2));
                     userAccount.setSurname((String) userData.get(3));
                     userAccount.setDisplayName((String) userData.get(4));
                     userAccount.setEmailAddr((String) userData.get(5));
                     userAccount.setPagerNumber((userData.get(6) == null) ? SecurityConstants.NOT_SET : (String) userData.get(6));
                     userAccount.setTelephoneNumber((userData.get(7) == null) ? SecurityConstants.NOT_SET : (String) userData.get(7));
                     userAccount.setRole(Role.valueOf((String) userData.get(8)));
                     userAccount.setFailedCount(((userData.get(9) == null) ? 0 : (Integer) userData.get(9)));
                     userAccount.setLastLogin(((userData.get(10) == null) ? new Date(1L) : new Date((Long) userData.get(10))));
                     userAccount.setExpiryDate(((userData.get(11) == null) ? 1L : (Long) userData.get(11)));
                     userAccount.setSuspended(((userData.get(12) == null) ? Boolean.FALSE : (Boolean) userData.get(12)));
                     userAccount.setOlrSetup(((userData.get(13) == null) ? Boolean.FALSE : (Boolean) userData.get(13)));
                     userAccount.setOlrLocked(((userData.get(14) == null) ? Boolean.FALSE : (Boolean) userData.get(14)));
                     userAccount.setTcAccepted(((userData.get(15) == null) ? Boolean.FALSE : (Boolean) userData.get(15)));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("UserAccount: {}", userAccount);
                     }
 
                     response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                     response.setResponse("Successfully loaded user account");
                     response.setUserAccount(userAccount);
                 }
                 else
                 {
                     response.setRequestStatus(SecurityRequestStatus.FAILURE);
                     response.setResponse("No accounts were found with the provided data");
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("Requesting user account was NOT authorized to perform the operation");
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AccountControlResponse: {}", response);
             }
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         finally
         {
             if (searchType != SearchRequestType.FORGOTUID)
             {
                 // audit
                 try
                 {
                     AuditEntry auditEntry = new AuditEntry();
                     auditEntry.setHostInfo(reqInfo);
                     auditEntry.setAuditType(AuditType.LOADACCOUNT);
                     auditEntry.setUserAccount(requestor);
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
         }
 
         return response;
     }
 
     @Override
     public AccountControlResponse loadUserAudit(final AccountControlRequest request) throws AccountControlException
     {
         final String methodName = IAccountControlProcessor.CNAME + "#loadUserAuditTrail(final AccountControlRequest request) throws AccountControlException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuditRequest: {}", request);
         }
 
         AccountControlResponse response = new AccountControlResponse();
 
         final UserAccount requestor = request.getRequestor();
         final RequestHostInfo reqInfo = request.getHostInfo();
         final UserAccount searchUser = request.getUserAccount();
         final SearchRequestType searchType = request.getSearchType();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", requestor);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", searchUser);
             DEBUGGER.debug("SearchRequestType: {}", searchType);
         }
 
         try
         {
             boolean isUserAuthorized = adminControl.adminControlService(requestor, AdminControlType.USER_ADMIN);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
             }
 
             if (isUserAuthorized)
             {
                 // get the user info
                 UserAccount userAccount = null;
                 List<Object> userData = userManager.loadUserAccount(searchUser.getGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("List<Object>: {}", userData);
                 }
 
                 if ((userData != null) && (!(userData.isEmpty())))
                 {
                     userAccount = new UserAccount();
                     userAccount.setGuid((String) userData.get(0));
                     userAccount.setUsername((String) userData.get(1));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("UserAccount: {}", userAccount);
                     }
                 }
 
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setUserAccount(searchUser);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditEntry: {}", auditEntry);
                 }
 
                 AuditRequest auditRequest = new AuditRequest();
                 auditRequest.setAuditEntry(auditEntry);
                 auditRequest.setStartRow(request.getStartPage());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditRequest: {}", auditRequest);
                 }
 
                 AuditResponse auditResponse = auditor.getAuditEntries(auditRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AuditResponse: {}", auditResponse);
                 }
 
                 if (auditResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                 {
                     List<AuditEntry> auditEntries = auditResponse.getAuditList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("List<AuditEntry>: {}", auditEntries);
                     }
 
                     if ((auditEntries != null) && (auditEntries.size() != 0))
                     {
                         // add to the response
                         response.setUserAccount(userAccount);
                         response.setEntryCount(auditResponse.getEntryCount());
                         response.setRequestStatus(SecurityRequestStatus.SUCCESS);
                         response.setResponse("Successfully loaded audit trail");
                         response.setAuditEntries(auditEntries);
                     }
                     else
                     {
                         response.setRequestStatus(SecurityRequestStatus.FAILURE);
                         response.setResponse("No audit history was located for the provided user.");
                     }
                 }
             }
             else
             {
                 response.setRequestStatus(SecurityRequestStatus.UNAUTHORIZED);
                 response.setResponse("Requesting user account was NOT authorized to perform the operation");
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuditResponse: {}", response);
             }
         }
         catch (AdminControlServiceException acsx)
         {
             ERROR_RECORDER.error(acsx.getMessage(), acsx);
 
             throw new AccountControlException(acsx.getMessage(), acsx);
         }
         catch (AuditServiceException asx)
         {
             ERROR_RECORDER.error(asx.getMessage(), asx);
 
             throw new AccountControlException(asx.getMessage(), asx);
         }
         catch (UserManagementException umx)
         {
             ERROR_RECORDER.error(umx.getMessage(), umx);
 
             throw new AccountControlException(umx.getMessage(), umx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.SHOWAUDIT);
                 auditEntry.setUserAccount(requestor);
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
 }
