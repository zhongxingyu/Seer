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
 
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import org.apache.commons.lang.RandomStringUtils;
 
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.security.audit.dto.AuditEntry;
 import com.cws.esolutions.security.audit.enums.AuditType;
 import com.cws.esolutions.security.audit.dto.AuditRequest;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.core.processors.dto.ServiceMessage;
 import com.cws.esolutions.core.processors.dto.MessagingRequest;
 import com.cws.esolutions.core.processors.dto.MessagingResponse;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.dao.processors.interfaces.IMessagingDAO;
 import com.cws.esolutions.core.processors.interfaces.IMessagingProcessor;
 import com.cws.esolutions.security.audit.exception.AuditServiceException;
 import com.cws.esolutions.core.dao.processors.impl.ServiceMessagingDAOImpl;
 import com.cws.esolutions.core.processors.exception.MessagingServiceException;
 import com.cws.esolutions.security.access.control.exception.UserControlServiceException;
 /**
  * eSolutionsCore
  * com.cws.esolutions.core.processors.impl
  * ServiceMessagingProcessorImpl.java
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
  * 35033355 @ Apr 29, 2013 1:32:21 PM
  *     Created.
  */
 public class ServiceMessagingProcessorImpl implements IMessagingProcessor
 {
     private static final IMessagingDAO messageDAO = new ServiceMessagingDAOImpl();
 
     @Override
     public MessagingResponse addNewMessage(final MessagingRequest request) throws MessagingServiceException
     {
         final String methodName = IMessagingProcessor.CNAME + "#addNewMessage(final MessagingRequest request) throws MessagingServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("MessagingRequest: {}", request);
         }
 
         MessagingResponse response = new MessagingResponse();
 
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final ServiceMessage message = request.getServiceMessage();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("ServiceMessage: {}", message);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount, request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                String messageId = RandomStringUtils.randomAlphanumeric(appConfig.getMessageIdLength());

                if (DEBUG)
                {
                    DEBUGGER.debug("messageId: {}", messageId);
                }

                 List<Object> messageList = new ArrayList<Object>(
                         Arrays.asList(
                                messageId,
                                 message.getMessageTitle(),
                                 message.getMessageText(),
                                 userAccount.getUsername(),
                                 userAccount.getEmailAddr(),
                                 message.getIsActive(),
                                 message.getDoesExpire(),
                                 message.getExpiryDate()));
 
                 // submit it
                 boolean isSubmitted = messageDAO.insertMessage(messageList);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isSubmitted: {}", isSubmitted);
                 }
 
                 if (!(isSubmitted))
                 {
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                     response.setResponse("Failed to submit message into the datastore.");
                 }
                 else
                 {
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                    response.setMessageId(messageId);
                     response.setResponse("Successfully inserted contact message into datastore.");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requesting user was NOT authorized to perform the operation");
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessagingResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new MessagingServiceException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new MessagingServiceException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.ADDSVCMESSAGE);
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
     public MessagingResponse updateExistingMessage(final MessagingRequest request) throws MessagingServiceException
     {
         final String methodName = IMessagingProcessor.CNAME + "#updateExistingMessage(final MessagingRequest request) throws MessagingServiceException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("MessagingRequest: {}", request);
         }
         
         MessagingResponse response = new MessagingResponse();
 
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final ServiceMessage message = request.getServiceMessage();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("ServiceMessage: {}", message);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount, request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                 List<Object> messageList = new ArrayList<Object>(
                         Arrays.asList(
                                 message.getMessageTitle(),
                                 message.getMessageText(),
                                 message.getIsActive(),
                                 message.getDoesExpire(),
                                 message.getExpiryDate(),
                                 userAccount.getUsername()));
 
                 // submit it
                 boolean isUpdated = messageDAO.updateMessage(message.getMessageId(), messageList);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUpdated: {}", isUpdated);
                 }
 
                 if (isUpdated)
                 {
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setMessageId(message.getMessageId());
                     response.setResponse("Successfully inserted contact message into datastore.");
                 }
                 else
                 {
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                     response.setResponse("Failed to submit message into the datastore.");
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("MessagingResponse: {}", response);
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requesting user was NOT authorized to perform the operation");
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessagingResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new MessagingServiceException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new MessagingServiceException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.EDITSVCMESSAGE);
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
     public MessagingResponse showMessages(final MessagingRequest request) throws MessagingServiceException
     {
         final String methodName = IMessagingProcessor.CNAME + "#showMessages(final MessagingRequest request) throws MessagingServiceException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("MessagingRequest: {}", request);
         }
 
         MessagingResponse response = new MessagingResponse();
 
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             List<Object[]> data = messageDAO.retrieveMessages();
 
             if (DEBUG)
             {
                 DEBUGGER.debug("data: {}", data);
             }
 
             if ((data == null) || (data.isEmpty()))
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("No messages were located.");
             }
             else
             {
                 List<ServiceMessage> svcMessages = new ArrayList<>();
                 SimpleDateFormat sdf = new SimpleDateFormat(appConfig.getDateFormat());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SimpleDateFormat: {}", sdf);
                 }
 
                 for (Object[] object : data)
                 {
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Object: {}", object);
                     }
 
                     ServiceMessage message = new ServiceMessage();
                     message.setMessageId((String) object[0]); // svc_message_id
                     message.setMessageTitle((String) object[1]); // svc_message_title
                     message.setMessageText((String) object[2]); // svc_message_txt
                     message.setMessageAuthor((String) object[3]); // svc_message_author
                     message.setAuthorEmail((String) object[4]); // svc_message_email
                     message.setSubmitDate((Long) object[5]); // svc_message_submitdate
                     message.setIsActive((Boolean) object[6]); // svc_message_active
                     message.setDoesExpire((Boolean) object[7]); //svc_message_expires
                     message.setExpiryDate((Long) object[8]); // svc_message_expirydate
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServiceMessage: {}", message);
                     }
 
                     svcMessages.add(message);
                 }
 
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
                 response.setResponse("Successfully loaded service messages");
                 response.setSvcMessages(svcMessages);
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessageResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new MessagingServiceException(sqx.getMessage(), sqx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.SHOWMESSAGES);
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
     public MessagingResponse showMessage(final MessagingRequest request) throws MessagingServiceException
     {
         final String methodName = IMessagingProcessor.CNAME + "#showMessage(final MessagingRequest request) throws MessagingServiceException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("MessagingRequest: {}", request);
         }
         
         MessagingResponse response = new MessagingResponse();
 
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final ServiceMessage reqMessage = request.getServiceMessage();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Message: {}", reqMessage);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             List<Object> responseList = messageDAO.retrieveMessage(reqMessage.getMessageId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("Response list: {}", responseList);
             }
 
             if ((responseList == null) || (responseList.isEmpty()))
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("The requested message could not be loaded.");
             }
             else
             {
                 SimpleDateFormat sdf = new SimpleDateFormat(appConfig.getDateFormat());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SimpleDateFormat: {}", sdf);
                 }
 
                 ServiceMessage message = new ServiceMessage();
                 message.setMessageId((String) responseList.get(0)); // svc_message_id
                 message.setMessageTitle((String) responseList.get(1)); // svc_message_title
                 message.setMessageText((String) responseList.get(2)); // svc_message_txt
                 message.setMessageAuthor((String) responseList.get(3)); // svc_message_author
                 message.setAuthorEmail((String) responseList.get(4)); // svc_message_email
                 message.setSubmitDate((Long) responseList.get(5)); // svc_message_submitdate
                 message.setIsActive((Boolean) responseList.get(6)); // svc_message_active
                 message.setDoesExpire((Boolean) responseList.get(7)); //svc_message_expires
                 message.setExpiryDate((Long) responseList.get(8)); // svc_message_expirydate
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ServiceMessage: {}", message);
                 }
 
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
                 response.setResponse("Successfully retrieved message");
                 response.setServiceMessage(message);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("MessageResponse: {}", response);
                 }
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessagingResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new MessagingServiceException(sqx.getMessage(), sqx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.LOADMESSAGE);
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
 }
