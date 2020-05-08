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
 import java.util.Enumeration;
 import org.slf4j.LoggerFactory;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import com.cws.us.esolutions.Constants;
 import com.cws.us.esolutions.dto.LoginRequest;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.us.esolutions.ApplicationServiceBean;
 import com.cws.esolutions.security.dto.UserSecurity;
 import com.cws.us.esolutions.validators.LoginValidator;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.security.enums.SecurityRequestStatus;
 import com.cws.esolutions.security.dao.userauth.enums.LoginType;
 import com.cws.esolutions.security.processors.dto.AuthenticationRequest;
 import com.cws.esolutions.security.dao.userauth.enums.AuthenticationType;
 import com.cws.esolutions.security.processors.dto.AuthenticationResponse;
 import com.cws.esolutions.security.processors.impl.AuthenticationProcessorImpl;
 import com.cws.esolutions.security.processors.exception.AuthenticationException;
 import com.cws.esolutions.security.processors.interfaces.IAuthenticationProcessor;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.controllers
  * LoginController.java
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
 @RequestMapping("/login")
 public class LoginController
 {
     private String otpLoginPage = null;
     private boolean allowUserReset = true;
     private LoginValidator validator = null;
     private String combinedLoginPage = null;
     private String usernameLoginPage = null;
     private String passwordLoginPage = null;
     private String logoffCompleteString = null;
     private String messageUsernameEmpty = null;
     private ApplicationServiceBean appConfig = null;
 
     private static final String CNAME = LoginController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 
     public final void setCombinedLoginPage(final String value)
     {
         final String methodName = LoginController.CNAME + "#setCombinedLoginPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.combinedLoginPage = value;
     }
 
     public final void setUsernameLoginPage(final String value)
     {
         final String methodName = LoginController.CNAME + "#setUsernameLoginPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.usernameLoginPage = value;
     }
 
     public final void setPasswordLoginPage(final String value)
     {
         final String methodName = LoginController.CNAME + "#setPasswordLoginPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.passwordLoginPage = value;
     }
 
     public final void setOtpLoginPage(final String value)
     {
         final String methodName = LoginController.CNAME + "#setOtpLoginPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.otpLoginPage = value;
     }
 
     public final void setLogoffCompleteString(final String value)
     {
         final String methodName = LoginController.CNAME + "#setLogoffCompleteString(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.logoffCompleteString = value;
     }
 
     public final void setMessageUsernameEmpty(final String value)
     {
         final String methodName = LoginController.CNAME + "#setMessageUsernameEmpty(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageUsernameEmpty = value;
     }
 
     public final void setValidator(final LoginValidator value)
     {
         final String methodName = LoginController.CNAME + "#setValidator(final LoginValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.validator = value;
     }
 
     public final void setAppConfig(final ApplicationServiceBean value)
     {
         final String methodName = LoginController.CNAME + "#setAppConfig(final CoreServiceBean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.appConfig = value;
     }
 
     public final void setAllowUserReset(final boolean value)
     {
         final String methodName = LoginController.CNAME + "#setAllowUserReset(final boolean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.allowUserReset = value;
     }
 
     @RequestMapping(value = "/default", method = RequestMethod.GET)
     public final ModelAndView showDefaultPage()
     {
         final String methodName = LoginController.CNAME + "#showDefaultPage()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
         mView.addObject(Constants.ALLOW_RESET, this.allowUserReset);
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
 
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
 
         @SuppressWarnings("unchecked") Enumeration<String> sessionEnumeration = hRequest.getSession().getAttributeNames();
 
         while (sessionEnumeration.hasMoreElements())
         {
             String sessionElement = sessionEnumeration.nextElement();
 
             if (DEBUG)
             {
                 DEBUGGER.debug("sessionElement: {}", sessionElement);
             }
 
             Object sessionValue = hRequest.getSession().getAttribute(sessionElement);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("sessionValue: {}", sessionValue);
             }
 
             if (sessionValue instanceof UserAccount)
             {
                 UserAccount sessionAccount = (UserAccount) sessionValue;
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: {}", sessionAccount);
                 }
 
                 if (sessionAccount.getStatus() == null)
                 {
                     hRequest.getSession().invalidate();
 
                     break;
                 }
                 else
                 {
                     switch (sessionAccount.getStatus())
                     {
                         case SUCCESS:
                             mView = new ModelAndView(new RedirectView());
                             mView.setViewName(appConfig.getHomeRedirect());
 
                             return mView;
                         case EXPIRED:
                             mView = new ModelAndView(new RedirectView());
                             mView.setViewName(appConfig.getExpiredRedirect());
 
                             return mView;
                         default:
                             hRequest.getSession().invalidate();
 
                             break;
                     }
                 }
             }
         }
 
         switch (appConfig.getLogonType())
         {
             case COMBINED:
                 mView.setViewName(this.combinedLoginPage);
                 mView.addObject("command", new LoginRequest());
 
                 break;
             default:
                 // default to split
                 mView.setViewName(this.usernameLoginPage);
                 mView.addObject("command", new UserAccount());
 
                 break;
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/logout", method = RequestMethod.GET)
     public final ModelAndView performLogout()
     {
         final String methodName = LoginController.CNAME + "#performLogout()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
         mView.addObject(Constants.ALLOW_RESET, this.allowUserReset);
         mView.addObject(Constants.RESPONSE_MESSAGE, this.logoffCompleteString);
 
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
 
         hSession.removeAttribute(Constants.USER_ACCOUNT);
         hSession.invalidate();
 
         switch (appConfig.getLogonType())
         {
             case COMBINED:
                 mView.setViewName(this.combinedLoginPage);
                 mView.addObject("command", new LoginRequest());
 
                 break;
             default:
                 // default to split
                 mView.setViewName(this.usernameLoginPage);
                 mView.addObject("command", new UserAccount());
 
                 break;
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     // combined logon
     @RequestMapping(value = "/submit", method = RequestMethod.POST)
     public final ModelAndView doCombinedLogin(@ModelAttribute("loginRequest") final LoginRequest loginRequest, final BindingResult bindResult)
     {
         final String methodName = LoginController.CNAME + "#doCombinedLogin(@ModelAttribute(\"loginRequest\") final LoginRequest loginRequest, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("LoginRequest: {}", loginRequest);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
         mView.addObject(Constants.ALLOW_RESET, this.allowUserReset);
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
 
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
 
         validator.validate(loginRequest, bindResult);
 
         if (bindResult.hasErrors())
         {
             mView.addObject("errors", bindResult.getAllErrors());
             mView.addObject("command", new LoginRequest());
             mView.setViewName(this.combinedLoginPage);
 
             return mView;
         }
 
         try
         {
             // validate
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteHost());
             reqInfo.setHostName(hRequest.getRemoteAddr());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             UserAccount reqUser = new UserAccount();
             reqUser.setUsername(loginRequest.getLoginUser());
             reqUser.setSessionId(hRequest.getSession().getId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserAccount: {}", reqUser);
             }
 
             UserSecurity reqSecurity = new UserSecurity();
             reqSecurity.setPassword(loginRequest.getLoginPass());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserSecurity: {}", reqSecurity);
             }
 
             AuthenticationRequest authRequest = new AuthenticationRequest();
             authRequest.setAuthType(AuthenticationType.LOGIN);
             authRequest.setLoginType(LoginType.COMBINED);
             authRequest.setHostInfo(reqInfo);
             authRequest.setTimeoutValue(appConfig.getRequestTimeout());
             authRequest.setUserAccount(reqUser);
             authRequest.setUserSecurity(reqSecurity);
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
                 UserAccount userAccount = authResponse.getUserAccount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: {}", userAccount);
                 }
 
                 HttpSession hSession = null;
 
                 switch (userAccount.getStatus())
                 {
                     case SUCCESS:
                         hRequest.getSession().invalidate();
 
                         hSession = hRequest.getSession();
                         userAccount.setSessionId(hSession.getId());
                         hSession.setAttribute(Constants.USER_ACCOUNT, userAccount);
 
                         mView = new ModelAndView(new RedirectView());
                         mView.setViewName(appConfig.getHomeRedirect());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ModelAndView: {}", mView);
                         }
 
                         return mView;
                     case EXPIRED:
                         // password expired - redirect to change password page
                         hRequest.getSession().invalidate();
 
                         hSession = hRequest.getSession();
                         userAccount.setSessionId(hSession.getId());
                         hSession.setAttribute(Constants.USER_ACCOUNT, userAccount);
 
                         mView = new ModelAndView(new RedirectView());
                         mView.setViewName(appConfig.getExpiredRedirect());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ModelAndView: {}", mView);
                         }
 
                         return mView;
                     default:
                         // no dice (but its also an unspecified failure)
 						ERROR_RECORDER.error("An unspecified error occurred during authentication.");
 
                         mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
                         mView.addObject("command", new LoginRequest());
                         mView.setViewName(this.combinedLoginPage);
 
                         break;
                 }
             }
             else
             {
                 mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
                 mView.addObject("command", new LoginRequest());
                 mView.setViewName(this.combinedLoginPage);
             }
         }
         catch (AuthenticationException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
 
             mView.addObject("command", new LoginRequest());
             mView.setViewName(this.combinedLoginPage);
             mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     // username logon
     @RequestMapping(value = "/username", method = RequestMethod.POST)
     public final ModelAndView doUsernameLogin(@ModelAttribute("user") final UserAccount user, final BindingResult bindResult)
     {
         final String methodName = LoginController.CNAME + "#doUsernameLogin(@ModelAttribute(\"user\") final UserAccount, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("UserAccount: {}", user);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
         mView.addObject(Constants.ALLOW_RESET, this.allowUserReset);
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
 
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
 
         validator.validate(user, bindResult);
 
         if (bindResult.hasErrors())
         {
             // validation failed
             mView.addObject(Constants.ERROR_MESSAGE, this.messageUsernameEmpty);
             mView.addObject("command", new UserAccount());
 
             return mView;
         }
 
         try
         {
             // validate
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteHost());
             reqInfo.setHostName(hRequest.getRemoteAddr());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             UserAccount reqUser = new UserAccount();
             reqUser.setUsername(user.getUsername());
             reqUser.setSessionId(hRequest.getSession().getId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("UserAccount: {}", reqUser);
             }
 
             AuthenticationRequest authRequest = new AuthenticationRequest();
             authRequest.setAuthType(AuthenticationType.LOGIN);
             authRequest.setLoginType(LoginType.USERNAME);
             authRequest.setHostInfo(reqInfo);
             authRequest.setTimeoutValue(appConfig.getRequestTimeout());
             authRequest.setUserAccount(reqUser);
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
                 UserAccount userAccount = authResponse.getUserAccount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: ", userAccount);
                 }
 
                 switch (userAccount.getStatus())
                 {
                     case SUCCESS:
                         // username validated
                         // add auth
                         mView.addObject("command", new UserSecurity());
 
                         switch (appConfig.getLogonType())
                         {
                             case OTP:
                                 // send to OTP page
                                 mView.setViewName(this.otpLoginPage);
 
                                 return mView;
                             default:
                                 // send to password page
                                 mView.setViewName(this.passwordLoginPage);
 
                                 return mView;
                         }
                     default:
                         // no dice (but its also an unspecified failure)
                         mView.addObject("command", new UserAccount());
                         mView.setViewName(this.usernameLoginPage);
                         mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
                         
                         break;
                 }
             }
             else
             {
                 mView.addObject("command", new UserAccount());
                 mView.setViewName(this.usernameLoginPage);
                 mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
             }
         }
         catch (AuthenticationException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
 
             mView.addObject("command", new UserAccount());
             mView.setViewName(this.usernameLoginPage);
             mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     // password logon
     @RequestMapping(value = "/password", method = RequestMethod.POST)
     public final ModelAndView doUsernameLogin(@ModelAttribute("security") final UserSecurity security, final BindingResult bindResult)
     {
         final String methodName = LoginController.CNAME + "#doUsernameLogin(@ModelAttribute(\"security\") final UserSecurity security, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("UserSecurity: {}", security);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
         mView.addObject(Constants.ALLOW_RESET, this.allowUserReset);
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
 
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
             // validate
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteHost());
             reqInfo.setHostName(hRequest.getRemoteAddr());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             AuthenticationRequest authRequest = new AuthenticationRequest();
             authRequest.setAuthType(AuthenticationType.LOGIN);
             authRequest.setLoginType(LoginType.PASSWORD);
             authRequest.setHostInfo(reqInfo);
             authRequest.setTimeoutValue(appConfig.getRequestTimeout());
             authRequest.setUserAccount((UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT));
             authRequest.setUserSecurity(security);
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
                 UserAccount userAccount = authResponse.getUserAccount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: ", userAccount);
                 }
 
                 HttpSession hSession = null;
 
                 switch (userAccount.getStatus())
                 {
                     case SUCCESS:
                         // username validated
                         // check logon type
                         hRequest.getSession().invalidate();
 
                         hSession = hRequest.getSession();
                         userAccount.setSessionId(hSession.getId());
                         hSession.setAttribute(Constants.USER_ACCOUNT, userAccount);
 
                         mView.setViewName(appConfig.getHomeRedirect());
 
                         return mView;
                     case EXPIRED:
                         // password expired - redirect to change password page
                         hRequest.getSession().invalidate();
 
                         hSession = hRequest.getSession();
                         userAccount.setSessionId(hSession.getId());
                         hSession.setAttribute(Constants.USER_ACCOUNT, userAccount);
 
                         mView = new ModelAndView(new RedirectView());
                         mView.setViewName(appConfig.getExpiredRedirect());
                         mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessagePasswordExpired());
 
                         return mView;
                     default:
                         // no dice (but its also an unspecified failure)
                         mView.addObject("command", new UserSecurity());
                         mView.setViewName(this.passwordLoginPage);
                         mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
                         
                         break;
                 }
             }
             else
             {
                 mView.addObject("command", new UserSecurity());
                 mView.setViewName(this.passwordLoginPage);
                 mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
             }
         }
         catch (AuthenticationException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
 
             mView.addObject("command", new UserSecurity());
             mView.setViewName(this.passwordLoginPage);
             mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessageRequestProcessingFailure());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     // otp logon
     @RequestMapping(value = "/otp", method = RequestMethod.POST)
     public final ModelAndView doOtpLogon(@ModelAttribute("security") final UserSecurity security, final BindingResult bindResult)
     {
         final String methodName = LoginController.CNAME + "#doOtpLogon(@ModelAttribute(\"security\") final UserSecurity security, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("UserSecurity: {}", security);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
         mView.addObject(Constants.ALLOW_RESET, this.allowUserReset);
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
 
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
             // validate
             RequestHostInfo reqInfo = new RequestHostInfo();
             reqInfo.setHostAddress(hRequest.getRemoteHost());
             reqInfo.setHostName(hRequest.getRemoteAddr());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             }
 
             AuthenticationRequest authRequest = new AuthenticationRequest();
             authRequest.setAuthType(AuthenticationType.LOGIN);
             authRequest.setLoginType(LoginType.USERNAME);
             authRequest.setHostInfo(reqInfo);
             authRequest.setTimeoutValue(appConfig.getRequestTimeout());
            authRequest.setUserAccount((UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT));
             authRequest.setUserSecurity(security);
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
                 UserAccount userAccount = authResponse.getUserAccount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("UserAccount: ", userAccount);
                 }
 
                 HttpSession hSession = null;
 
                 switch (userAccount.getStatus())
                 {
                     case SUCCESS:
                         // username validated
                         // check logon type
                         hRequest.getSession().invalidate();
 
                         hSession = hRequest.getSession();
                         userAccount.setSessionId(hSession.getId());
                         hSession.setAttribute(Constants.USER_ACCOUNT, userAccount);
 
                         mView.setViewName(appConfig.getHomeRedirect());
 
                         return mView;
                     case EXPIRED:
                         // password expired - redirect to change password page
                         hRequest.getSession().invalidate();
 
                         hSession = hRequest.getSession();
                         userAccount.setSessionId(hSession.getId());
                         hSession.setAttribute(Constants.USER_ACCOUNT, userAccount);
 
                         mView = new ModelAndView(new RedirectView());
                         mView.setViewName(appConfig.getExpiredRedirect());
                         mView.addObject(Constants.ERROR_MESSAGE, appConfig.getMessagePasswordExpired());
 
                         return mView;
                     default:
                         mView.addObject("command", new UserSecurity());
                         mView.setViewName(this.otpLoginPage);
                         mView.addObject(Constants.ERROR_RESPONSE, authResponse.getResponse());
 
                         break;
                 }
             }
             else
             {
                 mView.addObject("command", new UserSecurity());
                 mView.setViewName(this.otpLoginPage);
                 mView.addObject(Constants.ERROR_RESPONSE, authResponse.getResponse());
             }
         }
         catch (AuthenticationException ax)
         {
             ERROR_RECORDER.error(ax.getMessage(), ax);
 
             mView.addObject("command", new UserSecurity());
             mView.setViewName(this.otpLoginPage);
             mView.addObject(Constants.ERROR_RESPONSE, appConfig.getMessageRequestProcessingFailure());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 }
