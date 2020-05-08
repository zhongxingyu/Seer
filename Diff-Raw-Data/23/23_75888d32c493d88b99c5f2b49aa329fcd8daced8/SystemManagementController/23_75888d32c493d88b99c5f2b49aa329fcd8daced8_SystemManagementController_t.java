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
 
 import java.util.List;
 import java.util.UUID;
 import org.slf4j.Logger;
 import java.util.Enumeration;
 import org.slf4j.LoggerFactory;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.stereotype.Controller;
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
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.us.esolutions.dto.ManagementRequest;
 import com.cws.us.esolutions.ApplicationServiceBean;
 import com.cws.esolutions.core.processors.dto.Server;
 import com.cws.us.esolutions.validators.ServerValidator;
 import com.cws.esolutions.core.processors.dto.DataCenter;
 import com.cws.esolutions.core.processors.dto.SearchResult;
 import com.cws.esolutions.core.processors.enums.ServerType;
 import com.cws.esolutions.core.processors.dto.SearchRequest;
 import com.cws.esolutions.core.processors.dto.SearchResponse;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
import com.cws.esolutions.core.processors.enums.ServerStatus;
import com.cws.esolutions.core.processors.enums.ServiceRegion;
 import com.cws.esolutions.security.processors.enums.LoginStatus;
import com.cws.esolutions.core.processors.enums.NetworkPartition;
 import com.cws.esolutions.core.processors.impl.SearchProcessorImpl;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.processors.interfaces.ISearchProcessor;
 import com.cws.esolutions.core.processors.dto.ServerManagementRequest;
 import com.cws.esolutions.core.processors.dto.ServerManagementResponse;
 import com.cws.esolutions.core.processors.dto.DatacenterManagementRequest;
 import com.cws.esolutions.core.processors.dto.DatacenterManagementResponse;
 import com.cws.esolutions.core.processors.exception.SearchRequestException;
 import com.cws.esolutions.core.processors.impl.ServerManagementProcessorImpl;
 import com.cws.esolutions.core.processors.exception.ServerManagementException;
 import com.cws.esolutions.security.access.control.impl.UserControlServiceImpl;
 import com.cws.esolutions.core.processors.interfaces.IServerManagementProcessor;
 import com.cws.esolutions.security.access.control.interfaces.IUserControlService;
 import com.cws.esolutions.core.processors.impl.DatacenterManagementProcessorImpl;
 import com.cws.esolutions.core.processors.exception.DatacenterManagementException;
 import com.cws.esolutions.core.processors.interfaces.IDatacenterManagementProcessor;
 import com.cws.esolutions.security.access.control.exception.UserControlServiceException;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.controllers
  * SystemManagementController.java
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
 @RequestMapping("/system-management")
 public class SystemManagementController
 {
     private String serviceId = null;
     private String dcService = null;
     private String requestUrl = null;
     private String serviceName = null;
     private String defaultPage = null;
     private String defaultDomain = null;
     private String systemService = null;
     private String addServerPage = null;
     private String searchPostUrl = null;
     private String viewServerPage = null;
     private String addServerFailed = null;
     private String adminConsolePage = null;
     private ServerValidator serverValidator = null;
     private ApplicationServiceBean appConfig = null;
 
     private static final String CNAME = SystemManagementController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 
     public final void setServiceId(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setServiceId(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.serviceId = value;
     }
 
     public final void setServiceName(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setServiceName(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.serviceName = value;
     }
 
     public final void setDefaultPage(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setDefaultPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.defaultPage = value;
     }
 
     public final void setAddServerPage(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setAddServerPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addServerPage = value;
     }
 
     public final void setViewServerPage(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setViewServerPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewServerPage = value;
     }
 
     public final void setAddServerFailed(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setAddServerFailed(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addServerFailed = value;
     }
 
     public final void setAdminConsolePage(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setAdminConsolePage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.adminConsolePage = value;
     }
 
     public final void setAppConfig(final ApplicationServiceBean value)
     {
         final String methodName = SystemManagementController.CNAME + "#setAppConfig(final CoreServiceBean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.appConfig = value;
     }
 
     public final void setSystemService(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setSystemService(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.systemService = value;
     }
 
     public final void setDcService(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setDcService(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.dcService = value;
     }
 
     public final void setServerValidator(final ServerValidator value)
     {
         final String methodName = SystemManagementController.CNAME + "#setServerValidator(final ServerValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.serverValidator = value;
     }
 
     public final void setDefaultDomain(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setDefaultDomain(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.defaultDomain = value;
     }
 
     public final void setSearchPostUrl(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setSearchPostUrl(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.searchPostUrl = value;
     }
 
     public final void setRequestUrl(final String value)
     {
         final String methodName = SystemManagementController.CNAME + "#setRequestUrl(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.requestUrl = value;
     }
 
     @RequestMapping(value = "/default", method = RequestMethod.GET)
     public final ModelAndView showDefaultPage()
     {
         final String methodName = SystemManagementController.CNAME + "#showDefaultPage()";
 
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
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     mView.setViewName(this.defaultPage);
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/consoles", method = RequestMethod.GET)
     public final ModelAndView showAdminConsoles()
     {
         final String methodName = SystemManagementController.CNAME + "#showAdminConsoles()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IServerManagementProcessor serverMgr = new ServerManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     RequestHostInfo reqInfo = new RequestHostInfo();
                     reqInfo.setHostName(hRequest.getRemoteHost());
                     reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                     }
 
                     Server sourceServer = new Server();
                     sourceServer.setServerType(ServerType.DMGRSERVER);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", sourceServer);
                     }
 
                     ServerManagementRequest serviceReq = new ServerManagementRequest();
                     serviceReq.setRequestInfo(reqInfo);
                     serviceReq.setUserAccount(userAccount);
                     serviceReq.setServiceId(this.systemService);
                     serviceReq.setSourceServer(sourceServer);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementRequest: {}", serviceReq);
                     }
 
                     ServerManagementResponse serviceRes = serverMgr.listServersByType(serviceReq);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementResponse: {}", serviceRes);
                     }
 
                     if (serviceRes.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         List<Server> serverList = (List<Server>) serviceRes.getServerList();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("serverList: {}", serverList);
                         }
 
                         if ((serverList != null) && (serverList.size() != 0))
                         {
                             mView.addObject("serverList", serverList);
                             mView.setViewName(this.adminConsolePage);
                         }
                         else
                         {
                             mView.setViewName(appConfig.getUnavailablePage());
                         }
                     }
                     else
                     {
                         mView.setViewName(appConfig.getUnavailablePage());
                     }
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
                 mView.setViewName(appConfig.getUnavailablePage());
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/server/{serverGuid}", method = RequestMethod.GET)
     public final ModelAndView showServerDetail(@PathVariable("serverGuid") final String serverGuid)
     {
         final String methodName = SystemManagementController.CNAME + "#showServerDetail(@PathVariable(\"serverGuid\") final String serverGuid)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("serverName: {}", serverGuid);
         }
 
         Server server = new Server();
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IServerManagementProcessor serverMgr = new ServerManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                  // ensure authenticated access
                     RequestHostInfo reqInfo = new RequestHostInfo();
                     reqInfo.setHostAddress(hRequest.getRemoteAddr());
                     reqInfo.setHostName(hRequest.getRemoteHost());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                     }
 
                     server.setServerGuid(serverGuid);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", server);
                     }
 
                     ServerManagementRequest guidRequest = new ServerManagementRequest();
                     guidRequest.setRequestInfo(reqInfo);
                     guidRequest.setUserAccount(userAccount);
                     guidRequest.setServiceId(this.systemService);
                     guidRequest.setSourceServer(server);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementRequest: {}", guidRequest);
                     }
 
                     ServerManagementResponse guidResponse = serverMgr.getServerData(guidRequest);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementResponse: {}", guidResponse);
                     }
 
                     if (guidResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         // yay
                         mView.addObject("server", guidResponse.getServer());
                         mView.setViewName(this.viewServerPage);
                     }
                     else
                     {
                         // maybe we have a hostname
                         server.setOperHostName(serverGuid);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Server: {}", server);
                         }
 
                         ServerManagementRequest hostRequest = new ServerManagementRequest();
                         hostRequest.setRequestInfo(reqInfo);
                         hostRequest.setUserAccount(userAccount);
                         hostRequest.setServiceId(this.systemService);
                         hostRequest.setSourceServer(server);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementRequest: {}", hostRequest);
                         }
 
                         ServerManagementResponse hostResponse = serverMgr.listServersByType(hostRequest);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementResponse: {}", hostResponse);
                         }
 
                         if (hostResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             // yay
                             mView.addObject("server", hostResponse.getServer());
                             mView.setViewName(this.viewServerPage);
                         }
                         else
                         {
                             // boo
                             mView.addObject(Constants.ERROR_MESSAGE, hostResponse.getResponse());
                             mView.setViewName(this.defaultPage);
                         }
                     }
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
                 mView.setViewName(appConfig.getErrorResponsePage());
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/search", method = RequestMethod.GET)
     public final ModelAndView showSearchPage()
     {
         final String methodName = SystemManagementController.CNAME + "#showSearchPage()";
 
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
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     mView.addObject("postUrl", this.searchPostUrl);
                     mView.addObject("command", new SearchRequest());
                     mView.setViewName(appConfig.getSearchRequestPage());
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/server-control", method = RequestMethod.GET)
     public final ModelAndView showServerControl()
     {
         final String methodName = SystemManagementController.CNAME + "#showServerControl()";
 
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
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             // TODO
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     mView.setViewName(this.defaultPage);
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/add-server", method = RequestMethod.GET)
     public final ModelAndView showAddNewServer()
     {
         final String methodName = SystemManagementController.CNAME + "#showAddNewServer()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IServerManagementProcessor processor = new ServerManagementProcessorImpl();
         final IDatacenterManagementProcessor dcProcessor = new DatacenterManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     RequestHostInfo reqInfo = new RequestHostInfo();
                     reqInfo.setHostName(hRequest.getRemoteHost());
                     reqInfo.setHostAddress(hRequest.getRemoteAddr());
     
                     if (DEBUG)
                     {
                         DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                     }
 
                     Server dmgrServer = new Server();
                     dmgrServer.setServerType(ServerType.DMGRSERVER);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", dmgrServer);
                     }
 
                     ServerManagementRequest dmgrRequest = new ServerManagementRequest();
                     dmgrRequest.setRequestInfo(reqInfo);
                     dmgrRequest.setUserAccount(userAccount);
                     dmgrRequest.setServiceId(this.systemService);
                     dmgrRequest.setTargetServer(dmgrServer);
 
                     ServerManagementResponse dmgrResponse = processor.listServersByType(dmgrRequest);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementResponse: {}", dmgrResponse);
                     }
 
                     if (dmgrResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         List<Server> dmgrServers = dmgrResponse.getServerList();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("List<Server>: {}", dmgrServers);
                         }
 
                         mView.addObject("dmgrServers", dmgrServers);
                     }
 
                     // list datacenters
                     DatacenterManagementRequest dcRequest = new DatacenterManagementRequest();
                     dcRequest.setRequestInfo(reqInfo);
                     dcRequest.setServiceId(this.dcService);
                     dcRequest.setUserAccount(userAccount);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("DatacenterManagementRequest: {}", dcRequest);
                     }
 
                     DatacenterManagementResponse dcResponse = dcProcessor.listDatacenters(dcRequest);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("DatacenterManagementResponse: {}", dcResponse);
                     }
 
                     if (dcResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         List<DataCenter> datacenters = dcResponse.getDatacenterList();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("List<DataCenter>: {}", datacenters);
                         }
 
                         mView.addObject("datacenters", datacenters);
                     }
 
                     Server server = new Server();
                     server.setDomainName(this.defaultDomain);
                     server.setAssignedEngineer(userAccount.getDisplayName());
         
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", server);
                     }

                    mView.addObject("serverTypes", ServerType.values());
                    mView.addObject("serverStatuses", ServerStatus.values());
                    mView.addObject("serverRegions", ServiceRegion.values());
                    mView.addObject("networkPartitions", NetworkPartition.values());
                     mView.addObject("command", server);
                     mView.setViewName(this.addServerPage);
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
                 mView.setViewName(appConfig.getErrorResponsePage());
             }
             catch (DatacenterManagementException dmx)
             {
                 ERROR_RECORDER.error(dmx.getMessage(), dmx);
 
                 mView.setViewName(appConfig.getErrorResponsePage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/add-server", method = RequestMethod.POST)
     public final ModelAndView addNewServer(@ModelAttribute("request") final Server request, final BindingResult binding)
     {
         final String methodName = SystemManagementController.CNAME + "#addNewServer(@ModelAttribute(\"request\") final Server request, final BindingResult binding)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Server: {}", request);
             DEBUGGER.debug("BindingResult: {}", binding);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IServerManagementProcessor serverMgr = new ServerManagementProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("ServletRequestAttributes: {}", requestAttributes);
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpSession: {}", hSession);
             DEBUGGER.debug("Session ID: {}", hSession.getId());
             DEBUGGER.debug("UserAccount: {}", userAccount);
 
             DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     RequestHostInfo reqInfo = new RequestHostInfo();
                     reqInfo.setHostName(hRequest.getRemoteHost());
                     reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                     }
 
                     serverValidator.validate(request, binding);
 
                     if (binding.hasErrors())
                     {
                         ERROR_RECORDER.error("Request failed validation: {}", binding.getAllErrors());
 
                         // send back to page
                         mView.addObject("command", new Server());
                         mView.setViewName(this.addServerPage);
                     }
 
                     request.setServerGuid(UUID.randomUUID().toString());
                     request.setAssignedEngineer(userAccount.getDisplayName());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", request);
                     }
 
                     ServerManagementRequest serverReq = new ServerManagementRequest();
                     serverReq.setRequestInfo(reqInfo);
                     serverReq.setUserAccount(userAccount);
                     serverReq.setServiceId(this.systemService);
                     serverReq.setTargetServer(request);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementRequest: {}", request);
                     }
 
                     ServerManagementResponse response = serverMgr.addNewServer(serverReq);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementResponse: {}", response);
                     }
 
                     if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         // all set
                         // at this point we should be kicking off a request to install the agent
                         /*ServerManagementRequest installRequest = new ServerManagementRequest();
                         installRequest.setInstallAgent(true);
                         installRequest.setRequestInfo(reqInfo);
                         installRequest.setUserAccount(userAccount);
                         installRequest.setTargetServer(request);
                         installRequest.setServiceId(this.serviceId);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementRequest: {}", installRequest);
                         }
 
                         serverMgr.installSoftwarePackage(installRequest);*/ // we are NOT waiting for a response here
 
                         mView.addObject(Constants.RESPONSE_MESSAGE, response.getResponse());
                     }
                     else
                     {
                         // nooo
                         Server addNewServer = new Server();
                         addNewServer.setDomainName(this.defaultDomain);
                         addNewServer.setAssignedEngineer(userAccount.getDisplayName());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Server: {}", addNewServer);
                         }
 
                         mView.addObject(Constants.ERROR_MESSAGE, response.getResponse());
                         mView.addObject("command", addNewServer);
                     }
 
                     mView.setViewName(this.addServerPage);
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
                 mView.addObject(Constants.ERROR_MESSAGE, this.addServerFailed);
                 mView.setViewName(this.addServerPage);
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     // TODO
     @RequestMapping(value = "/server-control", method = RequestMethod.POST)
     public final ModelAndView runServerControlOperation(@ModelAttribute("request") final ManagementRequest request, final BindingResult binding)
     {
         final String methodName = SystemManagementController.CNAME + "#runServerControlOperation(@ModelAttribute(\"request\") final ManagementRequest request, final BindingResult binding)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ManagementRequest: {}", request);
             DEBUGGER.debug("BindingResult: {}", binding);
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
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     RequestHostInfo reqInfo = new RequestHostInfo();
                     reqInfo.setHostName(hRequest.getRemoteHost());
                     reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                     }
 
                     // TODO
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 
     @RequestMapping(value = "/search", method = RequestMethod.POST)
     public final ModelAndView doServerSearch(@ModelAttribute("request") final SearchRequest request, final BindingResult binding)
     {
         final String methodName = SystemManagementController.CNAME + "#runServerControlOperation(@ModelAttribute(\"request\") final SearchRequest request, final BindingResult binding)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
             DEBUGGER.debug("BindingResult: {}", binding);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ISearchProcessor searchProcessor = new SearchProcessorImpl();
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
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         if (userAccount.getStatus() == LoginStatus.EXPIRED)
         {
             // redirect to password page
             mView = new ModelAndView(new RedirectView());
             mView.setViewName(appConfig.getExpiredRedirect());
             mView.addObject(Constants.ERROR_MESSAGE, Constants.PASSWORD_EXPIRED);
 
             if (DEBUG)
             {
                 DEBUGGER.debug("ModelAndView: {}", mView);
             }
 
             return mView;
         }
 
         if (appConfig.getServices().get(this.serviceName))
         {
             try
             {
                 IUserControlService control = new UserControlServiceImpl();
 
                 boolean isUserAuthorized = control.isUserAuthorizedForService(userAccount.getGuid(), this.serviceId);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isUserAuthorized: {}", isUserAuthorized);
                 }
 
                 if (isUserAuthorized)
                 {
                     RequestHostInfo reqInfo = new RequestHostInfo();
                     reqInfo.setHostName(hRequest.getRemoteHost());
                     reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                     }
 
                     try
                     {
                         // a source server is *required*
                         request.setRequestInfo(reqInfo);
                         request.setUserAccount(userAccount);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchRequest: {}", request);
                         }
 
                         SearchResponse response = searchProcessor.doServerSearch(request);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResponse: {}", response);
                         }
 
                         if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             List<SearchResult> results = response.getResults();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("SearchResults: {}", results);
                             }
 
                             mView.addObject("requestUrl", this.requestUrl);
                             mView.addObject(Constants.SEARCH_RESULTS, results);
                         }
                         else
                         {
                             mView.addObject(Constants.ERROR_MESSAGE, response.getResponse());
                         }
 
                         // regardless of what happens we still allow the user to
                         // make the request
                         mView.addObject("command", new SearchRequest());
                         mView.setViewName(appConfig.getSearchRequestPage());
                     }
                     catch (SearchRequestException srx)
                     {
                         ERROR_RECORDER.error(srx.getMessage(), srx);
 
                         mView.setViewName(appConfig.getErrorResponsePage());
                     }
                 }
                 else
                 {
                     mView.setViewName(appConfig.getUnauthorizedPage());
                 }
             }
             catch (UserControlServiceException ucsx)
             {
                 ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
                 mView.setViewName(appConfig.getUnauthorizedPage());
             }
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         return mView;
     }
 }
