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
 
 import org.slf4j.Logger;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import org.slf4j.LoggerFactory;
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.stereotype.Controller;
 import org.apache.commons.lang.RandomStringUtils;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import com.cws.us.esolutions.Constants;
 import com.cws.esolutions.core.utils.EmailUtils;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.us.esolutions.ApplicationServiceBean;
 import com.cws.esolutions.core.processors.dto.EmailMessage;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.us.esolutions.validators.EmailAddressValidator;
 import com.cws.us.esolutions.validators.EmailMessageValidator;
 import com.cws.esolutions.core.processors.dto.MessagingRequest;
 import com.cws.esolutions.core.processors.dto.MessagingResponse;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.processors.interfaces.IMessagingProcessor;
 import com.cws.esolutions.core.processors.impl.ServiceMessagingProcessorImpl;
 import com.cws.esolutions.core.processors.exception.MessagingServiceException;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.controllers
  * CommonController.java
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
 @RequestMapping("/common")
 public class CommonController
 {
     private String homePage = null;
     private String serviceId = null;
     private ApplicationServiceBean appConfig = null;
 
     private static final String CNAME = CommonController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 
     public final void setAppConfig(final ApplicationServiceBean value)
     {
         final String methodName = CommonController.CNAME + "#setAppConfig(final CoreServiceBean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.appConfig = value;
     }
 
     public final void setHomePage(final String value)
     {
         final String methodName = CommonController.CNAME + "#setHomePage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.homePage = value;
     }
 
     public final void setServiceId(final String value)
     {
         final String methodName = CommonController.CNAME + "#setServiceId(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.serviceId = value;
     }
 
     @RequestMapping(value = "/default", method = RequestMethod.GET)
     public final ModelAndView showDefaultPage()
     {
         final String methodName = CommonController.CNAME + "#showDefaultPage()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IMessagingProcessor msgProcessor = new ServiceMessagingProcessorImpl();
 
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
 
         try
         {
             // ensure authenticated access
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteAddr());
             reqInfo.setHostName(hRequest.getRemoteHost());
             reqInfo.setSessionId(hSession.getId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             MessagingRequest mRequest = new MessagingRequest();
             mRequest.setRequestInfo(reqInfo);
             mRequest.setUserAccount(userAccount);
             mRequest.setServiceId(this.serviceId);
             mRequest.setApplicationId(this.appConfig.getApplicationId());
             mRequest.setApplicationName(this.appConfig.getApplicationName());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessagingRequest: {}", mRequest);
             }
 
             MessagingResponse mResponse = msgProcessor.showMessages(mRequest);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessagingResponse: {}", mResponse);
             }
 
             if (mResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
             {
                 mView.addObject("dateFormat", this.appConfig.getDateFormat());
                 mView.addObject("messageList", mResponse.getSvcMessages());
             }
 
             MessagingResponse messageResponse = msgProcessor.showAlertMessages(new MessagingRequest());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("MessagingResponse: {}", messageResponse);
             }
 
             if (messageResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
             {
                 mView.addObject("alertMessages", messageResponse.getSvcMessages());
             }
 
             mView.setViewName(this.homePage);
         }
         catch (MessagingServiceException msx)
         {
             // log it, but dont redirect or anything
             ERROR_RECORDER.error(msx.getMessage(), msx);
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         // in here, we're going to get all the messages to display and such
         return mView;
     }
 
     @RequestMapping(value = "/unauthorized", method = RequestMethod.GET)
     public final ModelAndView showUnauthorizedPage()
     {
         final String methodName = CommonController.CNAME + "#showUnauthorizedPage()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
 
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
 
         mView.setViewName(this.appConfig.getUnauthorizedPage());
 
         return mView;
     }
 
     @RequestMapping(value = "/submit-contact", method = RequestMethod.GET)
     public final ModelAndView showContactPage()
     {
         final String methodName = CommonController.CNAME + "#showContactPage()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
 
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
 
         mView.addObject("command", new EmailMessage());
         mView.setViewName(this.appConfig.getContactAdminsPage());
 
         return mView;
     }
 
     @RequestMapping(value = "/submit-contact", method = RequestMethod.POST)
     public final ModelAndView doSubmitMessage(@ModelAttribute("message") final EmailMessage message, final BindingResult bindResult)
     {
         final String methodName = CommonController.CNAME + "#doSubmitMessage(@ModelAttribute(\"message\") final EmailMessage message, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("EmailMessage: {}", message);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final EmailMessageValidator messageValidator = this.appConfig.getMessageValidator();
         final EmailAddressValidator addressValidator = this.appConfig.getEmailValidator();
         final String emailId = RandomStringUtils.randomAlphanumeric(16);
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("EmailMessageValidator: {}", messageValidator);
             DEBUGGER.debug("EmailAddressValidator: {}", addressValidator);
             DEBUGGER.debug("emailId: {}", emailId);
 
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
 
         messageValidator.validate(message, bindResult);
 
         if (bindResult.hasErrors())
         {
             mView.addObject("errors", bindResult.getAllErrors());
             mView.addObject("command", message);
             mView.setViewName(this.appConfig.getContactAdminsPage());
 
             return mView;
         }
 
         addressValidator.validate(message.getEmailAddr().get(0), bindResult);
 
         if (bindResult.hasErrors())
         {
             mView.addObject("errors", bindResult.getAllErrors());
             mView.addObject("command", message);
             mView.setViewName(this.appConfig.getContactAdminsPage());
 
             return mView;
         }
 
         try
         {
             EmailMessage emailMessage = new EmailMessage();
             emailMessage.setIsAlert(false); // set this to alert so it shows as high priority
             emailMessage.setMessageBody(message.getMessageBody());
             emailMessage.setMessageId(RandomStringUtils.randomAlphanumeric(16));
             emailMessage.setMessageSubject("[ " + emailId + " ] - " + message.getMessageSubject());
             emailMessage.setMessageTo(new ArrayList<>(Arrays.asList(this.appConfig.getSecEmailAddr())));
             emailMessage.setEmailAddr(message.getEmailAddr());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("EmailMessage: {}", emailMessage);
             }
 
             EmailUtils.sendEmailMessage(emailMessage, true);
 
            mView = new ModelAndView(new RedirectView());
             mView.setViewName(this.appConfig.getContactAdminsPage());
            mView.addObject(Constants.RESPONSE_MESSAGE, this.appConfig.getMessageEmailSentSuccess());
         }
         catch (MessagingException mx)
         {
             ERROR_RECORDER.error(mx.getMessage(), mx);
 
            mView = new ModelAndView(new RedirectView());
             mView.setViewName(this.appConfig.getContactAdminsPage());
             mView.addObject(Constants.ERROR_MESSAGE, this.appConfig.getMessageRequestProcessingFailure());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 }
