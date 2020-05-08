 /**
  * Copyright (c) 2009 - 2013 By: CWS, Inc.
  * 
  * All rights reserved. These materials are confidential and
  * proprietary to CaspersBox Web Services N.A and no part of
  * these materials should be reproduced, published in any form
  * by any means, electronic or mechanical, including photocopy
  * or any information storage or retrieval system not should
  * the materials be disclosed to third parties without the
  * express written authorization of CaspersBox Web Services, N.A.
  */
 package com.cws.us.esolutions.controllers;
 
 import java.util.Date;
 import org.slf4j.Logger;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.text.MessageFormat;
 import org.slf4j.LoggerFactory;
 import javax.mail.MessagingException;
 import org.apache.commons.io.IOUtils;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.stereotype.Controller;
 import org.apache.commons.lang.RandomStringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import com.cws.us.esolutions.Constants;
 import com.cws.esolutions.core.utils.EmailUtils;
 import com.cws.us.esolutions.dto.UserChangeRequest;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.security.dto.UserSecurity;
 import com.cws.us.esolutions.ApplicationServiceBean;
 import com.cws.esolutions.security.config.SecurityConfig;
 import com.cws.esolutions.core.processors.dto.EmailMessage;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.us.esolutions.validators.OnlineResetValidator;
 import com.cws.esolutions.core.controllers.ResourceController;
 import com.cws.esolutions.core.exception.CoreServiceException;
 import com.cws.esolutions.security.enums.SecurityRequestStatus;
 import com.cws.esolutions.security.processors.enums.ControlType;
 import com.cws.esolutions.security.processors.enums.LoginStatus;
 import com.cws.esolutions.security.dao.userauth.enums.LoginType;
 import com.cws.esolutions.security.processors.enums.ModificationType;
 import com.cws.esolutions.security.processors.dto.AccountResetRequest;
 import com.cws.esolutions.security.processors.dto.AccountResetResponse;
 import com.cws.esolutions.security.processors.dto.AuthenticationRequest;
 import com.cws.esolutions.security.processors.dto.AccountControlRequest;
 import com.cws.esolutions.security.dao.usermgmt.enums.SearchRequestType;
 import com.cws.esolutions.security.processors.dto.AccountControlResponse;
 import com.cws.esolutions.security.processors.dto.AuthenticationResponse;
 import com.cws.esolutions.security.dao.userauth.enums.AuthenticationType;
 import com.cws.esolutions.security.processors.impl.AccountResetProcessorImpl;
 import com.cws.esolutions.security.processors.exception.AccountResetException;
 import com.cws.esolutions.security.processors.impl.AccountControlProcessorImpl;
 import com.cws.esolutions.security.processors.impl.AuthenticationProcessorImpl;
 import com.cws.esolutions.security.processors.exception.AccountControlException;
 import com.cws.esolutions.security.processors.exception.AuthenticationException;
 import com.cws.esolutions.security.processors.interfaces.IAccountResetProcessor;
 import com.cws.esolutions.security.processors.interfaces.IAccountControlProcessor;
 import com.cws.esolutions.security.processors.interfaces.IAuthenticationProcessor;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.controllers
  * OnlineResetController.java
  *
  * $Id$
  * $Author$
  * $Date$
  * $Revision$
  * @author kh05451
  * @version 1.0
  *
  * History
  * ----------------------------------------------------------------------------
  * kh05451 @ Jan 16, 2013 11:53:26 AM
  *     Created.
  */
 @Controller
 @RequestMapping("/online-reset")
 public class OnlineResetController
 {
     private String resetURL = null;
     private String messageSource = null;
     private String userResetEmail = null;
     private SecurityConfig secConfig = null;
     private String submitAnswersPage = null;
     private String messageOlrComplete = null;
     private String submitUsernamePage = null;
     private String submitEmailAddrPage = null;
     private String forgotUsernameEmail = null;
     private String passwordResetSubject = null;
     private String messageValidationFailed = null;
     private OnlineResetValidator validator = null;
     private ApplicationServiceBean appConfig = null;
 
     private static final String CNAME = OnlineResetController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 
     public final void setResetURL(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setResetURL(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.resetURL = value;
     }
 
     public final void setMessageOlrComplete(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setMessageOlrComplete(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageOlrComplete = value;
     }
 
     public final void setSubmitAnswersPage(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setSubmitAnswersPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.submitAnswersPage = value;
     }
 
     public final void setSubmitUsernamePage(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setSubmitUsernamePage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.submitUsernamePage = value;
     }
 
     public final void setSubmitEmailAddrPage(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setSubmitEmailAddrPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.submitEmailAddrPage = value;
     }
 
     public final void setAppConfig(final ApplicationServiceBean value)
     {
         final String methodName = OnlineResetController.CNAME + "#setAppConfig(final CoreServiceBean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.appConfig = value;
     }
 
     public final void setSecConfig(final SecurityConfig value)
     {
         final String methodName = OnlineResetController.CNAME + "#setSecConfig(final SecurityConfig value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.secConfig = value;
     }
 
     public final void setValidator(final OnlineResetValidator value)
     {
         final String methodName = OnlineResetController.CNAME + "#setValidator(final OnlineResetValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.validator = value;
     }
 
     public final void setUserResetEmail(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setUserResetEmail(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.userResetEmail = value;
     }
 
     public final void setPasswordResetSubject(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setPasswordResetSubject(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.passwordResetSubject = value;
     }
 
     public final void setMessageSource(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setMessageSource(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageSource = value;
     }
 
     public final void setMessageValidationFailed(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setMessageValidationFailed(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageValidationFailed = value;
     }
 
     public final void setForgotUsernameEmail(final String value)
     {
         final String methodName = OnlineResetController.CNAME + "#setForgotUsernameEmail(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.forgotUsernameEmail = value;
     }
 
     @RequestMapping(value = "/forgot-username", method = RequestMethod.GET)
     public final ModelAndView showForgotUsername()
     {
         final String methodName = OnlineResetController.CNAME + "#showForgotUsername()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         mView.addObject("command", new UserChangeRequest());
         mView.setViewName(this.submitEmailAddrPage);
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/forgot-password", method = RequestMethod.GET)
     public final ModelAndView showForgottenPassword()
     {
         final String methodName = OnlineResetController.CNAME + "#showForgottenPassword()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         mView.addObject("command", new UserChangeRequest());
         mView.setViewName(this.submitUsernamePage);
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/forgot-password/{resetId}", method = RequestMethod.GET)
     public final ModelAndView showPasswordChange(@PathVariable(value = "resetId") final String resetId)
     {
         final String methodName = OnlineResetController.CNAME + "#showPasswordChange()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("resetId: {}", resetId);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         try
         {
             // ensure authenticated access
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteAddr());
             reqInfo.setHostName(hRequest.getRemoteHost());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             UserSecurity userSecurity = new UserSecurity();
             userSecurity.setResetRequestId(resetId);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserSecurity: {}", userSecurity);
             }
 
             AccountResetRequest resetReq = new AccountResetRequest();
             resetReq.setHostInfo(reqInfo);
             resetReq.setUserSecurity(userSecurity);
             resetReq.setApplicationId(appConfig.getApplicationId());
             resetReq.setApplicationName(appConfig.getApplicationName());
 
             IAccountResetProcessor resetProcessor = new AccountResetProcessorImpl();
             AccountResetResponse resetRes = resetProcessor.verifyResetRequest(resetReq);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AccountResetResponse: {}", resetRes);
             }
 
             if (resetRes.getRequestStatus() == SecurityRequestStatus.SUCCESS)
             {
                 UserAccount userAccount = resetRes.getUserAccount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: {}", userAccount);
                 }
 
                 if (userAccount.isSuspended())
                 {
                     // this account is suspended, we cant work on it
                     mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageUserNotLoggedIn());
                     mView.setViewName(appConfig.getLogonRedirect());
                 }
                 else
                 {
                     // add in the session id
                     userAccount.setSessionId(hSession.getId());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("UserAccount: {}", userAccount);
                     }
 
                     hSession.setAttribute(Constants.RESET_ACCOUNT, userAccount);
 
                     mView.setViewName(appConfig.getExpiredRedirect());
                 }
             }
             else
             {
                 // user not logged in, redirect
                 mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageUserNotLoggedIn());
                 mView.setViewName(appConfig.getLogonRedirect());
             }
         }
         catch (AccountResetException arx)
         {
             ERROR_RECORDER.error(arx.getMessage(), arx);
 
             mView.setViewName(appConfig.getErrorResponsePage());
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/cancel", method = RequestMethod.GET)
     public final ModelAndView doCancelRequest()
     {
         final String methodName = OnlineResetController.CNAME + "#doCancelRequest()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         hSession.invalidate(); // clear the http session
 
         mView = new ModelAndView(new RedirectView());
         mView.addObject(Constants.RESPONSE_MESSAGE, appConfig.getMessageRequestCanceled());
         mView.setViewName(appConfig.getLogonRedirect());
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/forgot-username", method = RequestMethod.POST)
     public final ModelAndView submitForgottenUsername(@ModelAttribute("request") final UserChangeRequest request, final BindingResult bindResult)
     {
         final String methodName = OnlineResetController.CNAME + "#submitForgottenUsername(@ModelAttribute(\"UserChangeRequest\") final UserChangeRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("UserChangeRequest: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final IAccountControlProcessor acctController = new AccountControlProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         // validate
         validator.validate(request, bindResult);
 
         if (bindResult.hasErrors())
         {
             ERROR_RECORDER.error("Failed to validate request");
 
             mView.addObject(Constants.ERROR_MESSAGE, this.messageValidationFailed);
             mView.addObject("command", new UserChangeRequest());
             mView.setViewName(this.submitUsernamePage);
 
             return mView;
         }
         
         try
         {
             // ensure authenticated access
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteAddr());
             reqInfo.setHostName(hRequest.getRemoteHost());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             UserAccount reqAccount = new UserAccount();
             reqAccount.setEmailAddr(request.getEmailAddr());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserAccount: {}", reqAccount);
             }
 
             AccountControlRequest controlReq = new AccountControlRequest();
             controlReq.setControlType(ControlType.LOOKUP);
             controlReq.setHostInfo(reqInfo);
             controlReq.setIsLogonRequest(false);
             controlReq.setModType(ModificationType.NONE);
             controlReq.setUserAccount(reqAccount);
             controlReq.setSearchType(SearchRequestType.FORGOTUID);
             controlReq.setApplicationId(appConfig.getApplicationId());
             controlReq.setApplicationName(appConfig.getApplicationName());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AccountControlRequest: {}", request);
             }
 
             AccountControlResponse response = acctController.searchAccounts(controlReq);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AccountControlResponse: {}", response);
             }
 
             if (response.getRequestStatus() == SecurityRequestStatus.SUCCESS)
             {
                 // this will return a single user account
                 UserAccount userAccount = response.getUserList().get(0);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: {}", userAccount);
                 }
 
                 // send an email with the username in it
                 // good, send email
                 String emailId = RandomStringUtils.randomAlphanumeric(16);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Message ID: {}", emailId);
                 }
 
                 String emailBody = MessageFormat.format(IOUtils.toString(
                         this.getClass().getClassLoader().getResourceAsStream(this.forgotUsernameEmail)), new Object[]
                 {
                     userAccount.getGivenName(),
                     new Date(System.currentTimeMillis()),
                     reqInfo.getHostName(),
                     userAccount.getUsername()
                 });
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Email body: {}", emailBody);
                 }
 
                 // good, now generate an email with the information
                 EmailMessage emailMessage = new EmailMessage();
                 emailMessage.setIsAlert(true); // set this to alert so it shows as high priority
                 emailMessage.setMessageBody(emailBody);
                 emailMessage.setMessageId(RandomStringUtils.randomAlphanumeric(16));
                 emailMessage.setMessageSubject("[ " + emailId + " ] - " + ResourceController.returnSystemPropertyValue(this.messageSource,
                         this.passwordResetSubject, this.getClass().getClassLoader()));
                 emailMessage.setMessageTo(new ArrayList<String>(Arrays.asList(userAccount.getEmailAddr())));
                 emailMessage.setMessageFrom(new ArrayList<String>(Arrays.asList(appConfig.getSecEmailAddr())));
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("EmailMessage: {}", emailMessage);
                 }
 
                 EmailUtils.sendEmailMessage(emailMessage);
 
                 mView.addObject(Constants.MESSAGE_RESPONSE, response.getResponse());
                 mView.setViewName(appConfig.getLogonRedirect());
             }
             else
             {
                 mView.addObject(Constants.ERROR_RESPONSE, response.getResponse());
                 mView.addObject("command", new UserChangeRequest());
                 mView.setViewName(this.submitUsernamePage);
             }
         }
         catch (AccountControlException acx)
         {
             ERROR_RECORDER.error(acx.getMessage(), acx);
             
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (MessagingException mx)
         {
             ERROR_RECORDER.error(mx.getMessage(), mx);
             
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (CoreServiceException csx)
         {
             ERROR_RECORDER.error(csx.getMessage(), csx);
             
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (IOException iox)
         {
             ERROR_RECORDER.error(iox.getMessage(), iox);
             
             mView.setViewName(appConfig.getErrorResponsePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/forgot-password", method = RequestMethod.POST)
     public final ModelAndView submitUsername(@ModelAttribute("request") final UserChangeRequest request, final BindingResult bindResult)
     {
         final String methodName = OnlineResetController.CNAME + "#submitUsername(@ModelAttribute(\"UserChangeRequest\") final UserChangeRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("UserChangeRequest: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         validator.validate(request, bindResult);
 
         if (bindResult.hasErrors())
         {
             ERROR_RECORDER.error("Request failed validation");
 
             mView.addObject(Constants.ERROR_MESSAGE, this.messageValidationFailed);
             mView.addObject("command", new UserChangeRequest());
             mView.setViewName(this.submitUsernamePage);
 
             return mView;
         }
 
         try
         {
             // ensure authenticated access
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteAddr());
             reqInfo.setHostName(hRequest.getRemoteHost());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             UserAccount reqAccount = new UserAccount();
             reqAccount.setUsername(request.getUsername());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserAccount: {}", reqAccount);
             }
 
             AuthenticationRequest authRequest = new AuthenticationRequest();
             authRequest.setHostInfo(reqInfo);
             authRequest.setUserAccount(reqAccount);
             authRequest.setAuthType(AuthenticationType.LOGIN);
             authRequest.setLoginType(LoginType.USERNAME);
             authRequest.setTimeoutValue(appConfig.getRequestTimeout());
             authRequest.setApplicationId(appConfig.getApplicationId());
             authRequest.setApplicationName(appConfig.getApplicationName());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuthenticationRequest: {}", authRequest);
             }
 
             AuthenticationResponse authResponse = authProcessor.processAgentLogon(authRequest);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuthenticationResponse: {}", authResponse);
             }
 
             if (authResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
             {
                 UserAccount resUser = authResponse.getUserAccount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: {}", resUser);
                 }
 
                 if (resUser.getStatus() != LoginStatus.SUSPENDED)
                 {
                     // account not suspended, we can continue
                     if (resUser.getOlrLocked())
                     {
                         // account olr locked
                         mView.addObject(Constants.ERROR_RESPONSE, authResponse.getResponse());
                         mView.setViewName(appConfig.getErrorResponsePage());
                     }
                     else
                     {
                         // continue
                         AuthenticationRequest secRequest = new AuthenticationRequest();
                         secRequest.setAuthType(AuthenticationType.RESET);
                         secRequest.setLoginType(LoginType.SECCONFIG);
                         secRequest.setHostInfo(reqInfo);
                         secRequest.setUserAccount(resUser);
                         secRequest.setApplicationId(appConfig.getApplicationId());
                         secRequest.setApplicationName(appConfig.getApplicationName());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("AuthenticationRequest: {}", secRequest);
                         }
 
                         AuthenticationResponse secResponse = authProcessor.obtainUserSecurityConfig(secRequest);
 
                         if (secResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                         {
                             // xlnt. set the user
                             hSession.setAttribute(Constants.RESET_ACCOUNT, resUser);
 
                             mView.addObject(Constants.USER_SECURITY, secResponse.getUserSecurity());
                             mView.addObject("command", new UserChangeRequest());
                             mView.setViewName(this.submitAnswersPage);
                         }
                         else
                         {
                             mView.addObject(Constants.ERROR_RESPONSE, secResponse.getResponse());
                             mView.setViewName(appConfig.getErrorResponsePage());
                         }
                     }
                 }
                 else
                 {
                     // account is suspended
                     mView.addObject("command", new UserChangeRequest());
                     mView.addObject(Constants.ERROR_RESPONSE, authResponse.getResponse());
                     mView.setViewName(this.submitUsernamePage);
                 }
             }
         }
         catch (AuthenticationException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
             
             mView.setViewName(appConfig.getErrorResponsePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/submit", method = RequestMethod.POST)
     public final ModelAndView submitSecurityResponse(@ModelAttribute("request") final UserChangeRequest request, final BindingResult bindResult)
     {
         final String methodName = OnlineResetController.CNAME + "#submitSecurityResponse(@ModelAttribute(\"request\") final UserChangeRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("UserChangeRequest: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.RESET_ACCOUNT);
         final IAccountResetProcessor resetProcess = new AccountResetProcessorImpl();
         final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             @SuppressWarnings("unchecked") Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             @SuppressWarnings("unchecked") Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         validator.validate(request, bindResult);
 
         if (bindResult.hasErrors())
         {
             ERROR_RECORDER.error("Request validation failed");
 
             mView.addObject(Constants.ERROR_MESSAGE, this.messageValidationFailed);
             mView.addObject("command", request);
             mView.setViewName(this.submitAnswersPage);
 
             return mView;
         }
 
         try
         {
             // ensure authenticated access
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteAddr());
             reqInfo.setHostName(hRequest.getRemoteHost());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             UserSecurity userSecurity = new UserSecurity();
             userSecurity.setSecAnswerOne(request.getSecAnswerOne());
             userSecurity.setSecAnswerTwo(request.getSecAnswerTwo());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserSecurity: {}", userSecurity);
             }
 
             AuthenticationRequest authRequest = new AuthenticationRequest();
             authRequest.setHostInfo(reqInfo);
             authRequest.setUserAccount(userAccount);
             authRequest.setUserSecurity(userSecurity);
             authRequest.setAuthType(AuthenticationType.RESET);
             authRequest.setLoginType(LoginType.SECCONFIG);
             authRequest.setTimeoutValue(appConfig.getRequestTimeout());
             authRequest.setApplicationId(appConfig.getApplicationId());
             authRequest.setApplicationName(appConfig.getApplicationName());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuthenticationRequest: {}", authRequest);
             }
 
             AuthenticationResponse response = authProcessor.verifyUserSecurityConfig(authRequest);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuthenticationResponse: {}", response);
             }
 
             if (response.getRequestStatus() == SecurityRequestStatus.SUCCESS)
             {
                 // ok, good - the user successfully passed this validation
                 // kick off the reset workflow
                 AccountResetRequest resetReq = new AccountResetRequest();
                 resetReq.setHostInfo(reqInfo);
                 resetReq.setRequestor(userAccount);
                 resetReq.setUserAccount(userAccount);
                 resetReq.setAlgorithm(secConfig.getAuthAlgorithm());
                 resetReq.setApplicationId(appConfig.getApplicationId());
                 resetReq.setApplicationName(appConfig.getApplicationName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AccountResetRequest: {}", resetReq);
                 }
 
                 if (secConfig.getSmsResetEnabled())
                 {
                     String smsCode = RandomStringUtils.randomAlphanumeric(8);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("smsCode: {}", smsCode);
                     }
 
                     resetReq.setSmsCode(smsCode);
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AccountResetRequest: {}", resetReq);
                 }
 
                 AccountResetResponse resetRes = resetProcess.resetUserPassword(resetReq);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AccountResetResponse: {}", resetRes);
                 }
 
                 if (resetRes.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                 {
                     // good, send email
                     UserAccount responseAccount = resetRes.getUserAccount();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("UserAccount: {}", responseAccount);
                     }
 
                     String emailId = RandomStringUtils.randomAlphanumeric(16);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Message ID: {}", emailId);
                     }
 
                     StringBuilder targetURL = new StringBuilder()
                         .append(hRequest.getScheme() + "://" + hRequest.getServerName())
                         .append((hRequest.getServerPort() == 443) ? null : ":" + hRequest.getServerPort())
                         .append(hRequest.getContextPath() + this.resetURL + resetRes.getResetId());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("targetURL: {}", targetURL);
                     }
                         
                     String emailBody = MessageFormat.format(IOUtils.toString(
                             this.getClass().getClassLoader().getResourceAsStream(this.userResetEmail)), new Object[]
                     {
                         responseAccount.getGivenName(),
                         new Date(System.currentTimeMillis()),
                         reqInfo.getHostName(),
                         targetURL.toString(),
                         secConfig.getPasswordMinLength(),
                         secConfig.getPasswordMaxLength()
                     });
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Email body: {}", emailBody);
                     }
 
                     // good, now generate an email with the information
                     EmailMessage emailMessage = new EmailMessage();
                     emailMessage.setIsAlert(true); // set this to alert so it shows as high priority
                     emailMessage.setMessageBody(emailBody);
                     emailMessage.setMessageId(RandomStringUtils.randomAlphanumeric(16));
                     emailMessage.setMessageSubject("[ " + emailId + " ] - " + ResourceController.returnSystemPropertyValue(this.messageSource,
                             this.passwordResetSubject, this.getClass().getClassLoader()));
                     emailMessage.setMessageFrom(new ArrayList<String>(Arrays.asList(appConfig.getSecEmailAddr())));
                     emailMessage.setMessageTo(new ArrayList<String>(Arrays.asList(responseAccount.getEmailAddr())));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("EmailMessage: {}", emailMessage);
                     }
 
                     EmailUtils.sendEmailMessage(emailMessage);
 
                     if (secConfig.getSmsResetEnabled())
                     {
                         // send an sms code
                         EmailMessage smsMessage = new EmailMessage();
                         smsMessage.setIsAlert(true); // set this to alert so it shows as high priority
                         smsMessage.setMessageBody(resetReq.getSmsCode());
                         emailMessage.setMessageTo(new ArrayList<String>(Arrays.asList(responseAccount.getPagerNumber())));
                         emailMessage.setMessageFrom(new ArrayList<String>(Arrays.asList(appConfig.getSecEmailAddr())));
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("EmailMessage: {}", smsMessage);
                         }
 
                         EmailUtils.sendSmsMessage(smsMessage);
                     }
 
                     // invalidate the session at this point
                     hSession.invalidate();
 
                     mView.addObject(Constants.RESPONSE_MESSAGE, this.messageOlrComplete);
                     mView.setViewName(appConfig.getLogonRedirect());
                 }
                 else
                 {
                     // some failure occurred
                     ERROR_RECORDER.error(resetRes.getResponse());
 
                     mView.addObject(Constants.ERROR_RESPONSE, resetRes.getResponse());
                     mView.addObject("command", new UserChangeRequest());
                     mView.setViewName(this.submitAnswersPage);
                 }
             }
             else
             {
                 // user not logged in, redirect
                 ERROR_RECORDER.error(response.getResponse());
 
                 mView.addObject(Constants.ERROR_RESPONSE, response.getResponse());
                 mView.addObject("command", new UserChangeRequest());
                 mView.setViewName(this.submitAnswersPage);
             }
         }
         catch (AuthenticationException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
 
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (MessagingException mx)
         {
             ERROR_RECORDER.error(mx.getMessage(), mx);
 
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (IOException iox)
         {
             ERROR_RECORDER.error(iox.getMessage(), iox);
 
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (CoreServiceException csx)
         {
             ERROR_RECORDER.error(csx.getMessage(), csx);
 
             mView.setViewName(appConfig.getErrorResponsePage());
         }
         catch (AccountResetException arx)
         {
             ERROR_RECORDER.error(arx.getMessage(), arx);
 
             mView.setViewName(appConfig.getErrorResponsePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 }
