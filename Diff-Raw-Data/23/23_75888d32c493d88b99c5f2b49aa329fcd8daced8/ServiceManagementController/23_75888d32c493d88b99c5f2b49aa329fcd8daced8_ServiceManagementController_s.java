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
 
 import java.util.Map;
 import java.util.List;
 import org.slf4j.Logger;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import org.slf4j.LoggerFactory;
 import javax.servlet.http.HttpSession;
 import org.apache.commons.lang.StringUtils;
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
 import com.cws.us.esolutions.dto.PlatformRequest;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.us.esolutions.ApplicationServiceBean;
 import com.cws.esolutions.core.processors.dto.Server;
 import com.cws.esolutions.core.processors.dto.Project;
 import com.cws.esolutions.core.processors.dto.Platform;
 import com.cws.esolutions.core.processors.dto.DataCenter;
 import com.cws.us.esolutions.validators.PlatformValidator;
 import com.cws.esolutions.core.processors.dto.SearchResult;
 import com.cws.esolutions.core.processors.enums.ServerType;
 import com.cws.us.esolutions.validators.DatacenterValidator;
 import com.cws.esolutions.core.processors.dto.SearchRequest;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.core.processors.dto.SearchResponse;
 import com.cws.esolutions.security.processors.enums.LoginStatus;
 import com.cws.esolutions.core.processors.impl.SearchProcessorImpl;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.processors.interfaces.ISearchProcessor;
 import com.cws.esolutions.core.processors.dto.ServerManagementRequest;
 import com.cws.esolutions.core.processors.dto.ServerManagementResponse;
 import com.cws.esolutions.core.processors.dto.ProjectManagementRequest;
 import com.cws.esolutions.core.processors.dto.PlatformManagementRequest;
 import com.cws.esolutions.core.processors.dto.ProjectManagementResponse;
 import com.cws.esolutions.core.processors.dto.PlatformManagementResponse;
 import com.cws.esolutions.core.processors.dto.DatacenterManagementRequest;
 import com.cws.esolutions.core.processors.exception.SearchRequestException;
 import com.cws.esolutions.core.processors.dto.DatacenterManagementResponse;
 import com.cws.esolutions.core.processors.impl.ServerManagementProcessorImpl;
 import com.cws.esolutions.core.processors.impl.ProjectManagementProcessorImpl;
 import com.cws.esolutions.core.processors.exception.ServerManagementException;
 import com.cws.esolutions.core.processors.exception.ProjectManagementException;
 import com.cws.esolutions.core.processors.impl.PlatformManagementProcessorImpl;
 import com.cws.esolutions.core.processors.exception.PlatformManagementException;
 import com.cws.esolutions.core.processors.interfaces.IServerManagementProcessor;
 import com.cws.esolutions.core.processors.impl.DatacenterManagementProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.IProjectManagementProcessor;
 import com.cws.esolutions.core.processors.interfaces.IPlatformManagementProcessor;
 import com.cws.esolutions.core.processors.exception.DatacenterManagementException;
 import com.cws.esolutions.core.processors.interfaces.IDatacenterManagementProcessor;
 /**
  * eSolutions_java_source
  * com.cws.us.esolutions.controllers
  * ServiceManagementController.java
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
 @RequestMapping("/service-management")
 public class ServiceManagementController
 {
     private String dcService = null;
     private String systemMgmt = null;
     private String projectMgmt = null;
     private String serviceName = null;
     private String defaultPage = null;
     private String platformMgmt = null;
     private String addProjectPage = null;
     private String selectDmgrPage = null;
     private String addPlatformPage = null;
     private String viewProjectPage = null;
     private String viewPlatformPage = null;
     private String viewProjectsList = null;
     private String viewPlatformList = null;
     private String addDatacenterPage = null;
     private String messageNoDmgrFound = null;
     private String messageNoWebServersFound = null;
     private String messageNoAppServersFound = null;
     private ApplicationServiceBean appConfig = null;
     private PlatformValidator platformValidator = null;
     private String messageRequestValidationFailed = null;
     private DatacenterValidator datacenterValidator = null;
     private String messageDatacenterSuccessfullyAdded = null;
 
     private static final String CNAME = ServiceManagementController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 
     public final void setAppConfig(final ApplicationServiceBean value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setAppConfig(final CoreServiceBean value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.appConfig = value;
     }
 
     public final void setPlatformValidator(final PlatformValidator value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setPlatformValidator(final PlatformValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.platformValidator = value;
     }
 
     public final void setDatacenterValidator(final DatacenterValidator value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setDatacenterValidator(final DatacenterValidator value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.datacenterValidator = value;
     }
 
     public final void setServiceName(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setServiceName(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.serviceName = value;
     }
 
     public final void setDcService(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setDcService(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.dcService = value;
     }
 
     public final void setDefaultPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setDefaultPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.defaultPage = value;
     }
 
     public final void setAddProjectPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setAddProjectPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addProjectPage = value;
     }
 
     public final void setSelectDmgrPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setSelectDmgrPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.selectDmgrPage = value;
     }
 
     public final void setAddPlatformPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setAddPlatformPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addPlatformPage = value;
     }
 
     public final void setViewProjectPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setViewProjectPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewProjectPage = value;
     }
 
     public final void setViewPlatformPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setViewPlatformPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewPlatformPage = value;
     }
 
     public final void setViewProjectsList(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setViewProjectsList(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewProjectsList = value;
     }
 
     public final void setViewPlatformList(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setViewPlatformList(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.viewPlatformList = value;
     }
 
     public final void setAddDatacenterPage(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setAddDatacenterPage(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.addDatacenterPage = value;
     }
 
     public final void setProjectMgmt(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setProjectMgmt(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.projectMgmt = value;
     }
 
     public final void setPlatformMgmt(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setPlatformMgmt(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.platformMgmt = value;
     }
 
     public final void setSystemMgmt(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setSystemMgmt(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.systemMgmt = value;
     }
 
     public final void setMessageRequestValidationFailed(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setMessageRequestValidationFailed(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageRequestValidationFailed = value;
     }
 
     public final void setMessageNoWebServersFound(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setMessageNoWebServersFound(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoWebServersFound = value;
     }
 
     public final void setMessageNoAppServersFound(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setMessageNoAppServersFound(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageNoAppServersFound = value;
     }
 
     public final void setMessageDatacenterSuccessfullyAdded(final String value)
     {
         final String methodName = ServiceManagementController.CNAME + "#setMessageDatacenterSuccessfullyAdded(final String value)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", value);
         }
 
         this.messageDatacenterSuccessfullyAdded = value;
     }
 
     @RequestMapping(value = "/default", method = RequestMethod.GET)
     public final ModelAndView showDefaultPage()
     {
         final String methodName = ServiceManagementController.CNAME + "#showDefaultPage()";
 
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
             mView.setViewName(this.defaultPage);
         }
         else
         {
             mView.setViewName(appConfig.getUnavailablePage());
         }
 
         if (DEBUG)
         {
             DEBUGGER.debug("ModelAndView: {}", mView);
         }
 
         // in here, we're going to get all the messages to display and such
         return mView;
     }
 
     @RequestMapping(value = "/project/{projectName}", method = RequestMethod.GET)
     public final ModelAndView showProject(@PathVariable("projectName") final String projectName)
     {
         final String methodName = ServiceManagementController.CNAME + "#showProject(@PathVariable(\"projectName\") final String projectName)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("projectName: {}", projectName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IProjectManagementProcessor projectMgr = new ProjectManagementProcessorImpl();
 
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
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Project reqProject = new Project();
                 reqProject.setProjectGuid(projectName);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Project: {}", reqProject);
                 }
 
                 // get a list of available servers
                 ProjectManagementRequest request = new ProjectManagementRequest();
                 request.setRequestInfo(reqInfo);
                 request.setUserAccount(userAccount);
                 request.setServiceId(this.projectMgmt);
                 request.setProject(reqProject);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementRequest: {}", request);
                 }
 
                 ProjectManagementResponse response = projectMgr.getProjectData(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementResponse: {}", response);
                 }
 
                 if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Project resProject = response.getProject();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Project: {}", resProject);
                     }
 
                     mView.addObject("project", resProject);
                     mView.setViewName(this.viewProjectPage);
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, response.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (ProjectManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
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
 
     @RequestMapping(value = "/platform/{platformName}", method = RequestMethod.GET)
     public final ModelAndView showPlatform(@PathVariable("platformName") final String platformName)
     {
         final String methodName = ServiceManagementController.CNAME + "#showPlatform(@PathVariable(\"platformName\") final String platformName)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("platformName: {}", platformName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
 
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
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Platform reqPlatform = new Platform();
                 reqPlatform.setPlatformGuid(platformName);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Platform: {}", reqPlatform);
                 }
 
                 // get a list of available servers
                 PlatformManagementRequest request = new PlatformManagementRequest();
                 request.setRequestInfo(reqInfo);
                 request.setUserAccount(userAccount);
                 request.setServiceId(this.projectMgmt);
                 request.setPlatform(reqPlatform);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementRequest: {}", request);
                 }
 
                 PlatformManagementResponse response = platformMgr.getPlatformData(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementResponse: {}", response);
                 }
 
                 if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Platform resPlatform = response.getPlatformData();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Platform: {}", resPlatform);
                     }
 
                     mView.addObject("platform", resPlatform);
                     mView.setViewName(this.viewPlatformPage);
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, response.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (PlatformManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
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
 
     @RequestMapping(value = "/add-project", method = RequestMethod.GET)
     public final ModelAndView showAddProject()
     {
         final String methodName = ServiceManagementController.CNAME + "#showAddProject()";
 
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
             mView.addObject("command", new Project());
             mView.setViewName(this.addProjectPage);
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
 
     @RequestMapping(value = "/add-datacenter", method = RequestMethod.GET)
     public final ModelAndView showAddDatacenter()
     {
         final String methodName = ServiceManagementController.CNAME + "#showAddDatacenter()";
 
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
             mView.addObject("command", new DataCenter());
             mView.setViewName(this.addDatacenterPage);
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
 
     @RequestMapping(value = "/add-platform", method = RequestMethod.GET)
     public final ModelAndView showAddPlatform()
     {
         final String methodName = ServiceManagementController.CNAME + "#showAddPlatform()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final IServerManagementProcessor serverMgr = new ServerManagementProcessorImpl();
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
                 dmgrRequest.setServiceId(this.systemMgmt);
                 dmgrRequest.setTargetServer(dmgrServer);
                 dmgrRequest.setUserAccount(userAccount);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ServerManagementRequest: {}", dmgrRequest);
                 }
 
                 ServerManagementResponse dmgrResponse = serverMgr.listServersByType(dmgrRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ServerManagementResponse: {}", dmgrResponse);
                 }
 
                 if (dmgrResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<Server> listing = dmgrResponse.getServerList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("listing: {}", listing);
                     }
 
                     Map<String, String> dmgrList = new HashMap<String, String>();
 
                     for (Server server : listing)
                     {
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Server: {}", server);
                         }
 
                         dmgrList.put(server.getServerGuid(), server.getOperHostName());
                     }
 
                     mView.addObject("dmgrList", dmgrList);
                 }
 
                 mView.addObject("command", new PlatformRequest());
                 mView.setViewName(this.selectDmgrPage);
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
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
 
     @RequestMapping(value = "/list-platforms", method = RequestMethod.GET)
     public final ModelAndView showPlatformList()
     {
         final String methodName = ServiceManagementController.CNAME + "#showPlatformList()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IPlatformManagementProcessor platformMgr = new PlatformManagementProcessorImpl();
 
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
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 PlatformManagementRequest request = new PlatformManagementRequest();
                 request.setRequestInfo(reqInfo);
                 request.setUserAccount(userAccount);
                 request.setServiceId(this.platformMgmt);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementRequest: {}", request);
                 }
 
                 PlatformManagementResponse response = platformMgr.listPlatforms(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("PlatformManagementResponse: {}", response);
                 }
 
                 if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<Platform> platformList = response.getPlatformList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("platformList: {}", platformList);
                     }
 
                     mView.addObject("platformList", platformList);
                     mView.setViewName(this.viewPlatformList);
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, response.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (PlatformManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
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
 
     @RequestMapping(value = "/list-projects", method = RequestMethod.GET)
     public final ModelAndView showProjectList()
     {
         final String methodName = ServiceManagementController.CNAME + "#showProjectList()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IProjectManagementProcessor projectMgr = new ProjectManagementProcessorImpl();
 
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
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 ProjectManagementRequest request = new ProjectManagementRequest();
                 request.setRequestInfo(reqInfo);
                 request.setUserAccount(userAccount);
                 request.setServiceId(this.platformMgmt);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementRequest: {}", request);
                 }
 
                 ProjectManagementResponse response = projectMgr.listProjects(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementResponse: {}", response);
                 }
 
                 if (response.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     List<Project> projectList = response.getProjectList();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("projectList: {}", projectList);
                     }
 
                     mView.addObject("projectList", projectList);
                     mView.setViewName(this.viewProjectsList);
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, response.getResponse());
                     mView.setViewName(this.defaultPage);
                 }
             }
             catch (ProjectManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
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
 
     @RequestMapping(value = "/validate-dmgr", method = RequestMethod.POST)
     public final ModelAndView validateSelectedManager(@ModelAttribute("request") final PlatformRequest request, final BindingResult bindResult)
     {
         final String methodName = ServiceManagementController.CNAME + "#validateSelectedManager(@ModelAttribute(\"request\") final PlatformRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Project: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IServerManagementProcessor processor = new ServerManagementProcessorImpl();
 
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
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 if (StringUtils.isNotEmpty(request.getPlatformDmgr()))
                 {
                     Server dmgrServer = new Server();
                     dmgrServer.setServerGuid(request.getPlatformDmgr());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", dmgrServer);
                     }
 
                     ServerManagementRequest dmgrServerRequest = new ServerManagementRequest();
                     dmgrServerRequest.setTargetServer(dmgrServer);
                     dmgrServerRequest.setRequestInfo(reqInfo);
                     dmgrServerRequest.setUserAccount(userAccount);
                     dmgrServerRequest.setServiceId(this.systemMgmt);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementRequest: {}", dmgrServerRequest);
                     }
 
                     ServerManagementResponse dmgrServerResponse = processor.getServerData(dmgrServerRequest);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ServerManagementResponse: {}", dmgrServerResponse);
                     }
 
                     if (dmgrServerResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                     {
                         Server targetDmgr = dmgrServerResponse.getServer();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Server: {}", targetDmgr);
                         }
 
                         PlatformRequest platformRequest = new PlatformRequest();
                         platformRequest.setPlatformDmgr(targetDmgr.getServerGuid());
                         platformRequest.setPlatformDmgrName(targetDmgr.getOperHostName());
                         platformRequest.setPlatformRegion(targetDmgr.getServerRegion());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("PlatformRequest: {}", platformRequest);
                         }
 
                         ServerManagementRequest appServerRequest = new ServerManagementRequest();
                         appServerRequest.setTargetServer(targetDmgr);
                         appServerRequest.setRequestInfo(reqInfo);
                         appServerRequest.setUserAccount(userAccount);
                         appServerRequest.setServiceId(this.systemMgmt);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementRequest: {}", appServerRequest);
                         }
 
                         // get the list of appservers associated with this dmgr
                         ServerManagementResponse appServerResponse = processor.listServersByType(appServerRequest);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementResponse: {}", appServerResponse);
                         }
 
                         if (appServerResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             // xlnt
                             List<Server> appServerList = appServerResponse.getServerList();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("List<Server>: {}", appServerList);
                             }
 
                             if ((appServerList != null) && (appServerList.size() != 0))
                             {
                                 List<String> appServers = new ArrayList<String>();
 
                                 for (Server appserver : appServerList)
                                 {
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("Server: {}", appserver);
                                     }
 
                                     appServers.add(appserver.getServerGuid());
                                 }
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("List<String>: {}", appServers);
                                 }
 
                                 platformRequest.setAppServers(appServers);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("PlatformRequest: {}", platformRequest);
                                 }
                             }
                         }
 
                         // get the list of webservers associated with this dmgr
                         Server webserver = new Server();
                         webserver.setServerType(ServerType.WEBSERVER);
                         webserver.setDatacenter(targetDmgr.getDatacenter());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Server: {}", webserver);
                         }
 
                         ServerManagementRequest webRequest = new ServerManagementRequest();
                         webRequest.setTargetServer(webserver);
                         webRequest.setRequestInfo(reqInfo);
                         webRequest.setUserAccount(userAccount);
                         webRequest.setServiceId(this.systemMgmt);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementRequest: {}", webRequest);
                         }
 
                         ServerManagementResponse webResponse = processor.listServersByType(webRequest);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementResponse: {}", webResponse);
                         }
 
                         if (webResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             // xlnt
                             List<Server> webServerList = webResponse.getServerList();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("List<Server>: {}", webServerList);
                             }
 
                             if ((webServerList != null) && (webServerList.size() != 0))
                             {
                                 List<String> webServers = new ArrayList<String>();
 
                                 for (Server websrvr : webServerList)
                                 {
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("Server: {}", websrvr);
                                     }
 
                                     webServers.add(websrvr.getServerGuid());
                                 }
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("List<String>: {}", webServers);
                                 }
 
                                 platformRequest.setWebServers(webServers);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("PlatformRequest: {}", platformRequest);
                                 }
                             }
                         }
 
                         mView.addObject("command", platformRequest);
                         mView.setViewName(this.addPlatformPage);
                     }
                     else
                     {
                         mView.addObject(Constants.ERROR_MESSAGE, this.messageNoDmgrFound);
                         mView.setViewName(appConfig.getErrorResponsePage());
                     }
                 }
                 else
                 {
                     // no dmgr
                     mView.addObject(Constants.ERROR_MESSAGE, this.messageNoDmgrFound);
                     mView.setViewName(appConfig.getErrorResponsePage());
                 }
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
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
 
     @RequestMapping(value = "/submit-platform", method = RequestMethod.POST)
     public final ModelAndView doAddPlatform(@ModelAttribute("request") final PlatformRequest request, final BindingResult bindResult)
     {
         final String methodName = ServiceManagementController.CNAME + "#doAddPlatform(@ModelAttribute(\"request\") final PlatformRequest request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Project: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ISearchProcessor search = new SearchProcessorImpl();
         final IServerManagementProcessor serverMgr = new ServerManagementProcessorImpl();
 
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
             platformValidator.validate(request, bindResult);
 
             if (bindResult.hasErrors())
             {
                 // something was missing from the request
                 mView.addObject(Constants.ERROR_MESSAGE, this.messageRequestValidationFailed);
                 mView.addObject("command", new PlatformRequest());
                 mView.setViewName(this.selectDmgrPage);
 
                 return mView;
             }
 
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 Server requestedDmgr = new Server();
                 String dmgrGuid = request.getPlatformDmgr();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("dmgrGuid: {}", dmgrGuid);
                 }
 
                 if (StringUtils.isNotEmpty(dmgrGuid))
                 {
                     requestedDmgr.setServerGuid(request.getPlatformDmgr());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, this.messageNoDmgrFound);
                     mView.addObject("command", new PlatformRequest());
                     mView.setViewName(this.selectDmgrPage);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("ModelAndView: {}", mView);
                     }
 
                     return mView;
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Server: {}", requestedDmgr);
                 }
 
                 ServerManagementRequest dmgrRequest = new ServerManagementRequest();
                 dmgrRequest.setRequestInfo(reqInfo);
                 dmgrRequest.setUserAccount(userAccount);
                 dmgrRequest.setServiceId(this.systemMgmt);
                 dmgrRequest.setTargetServer(requestedDmgr);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ServerManagementRequest: {}", dmgrRequest);
                 }
 
                 ServerManagementResponse dmgrResponse = serverMgr.getServerData(dmgrRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ServerManagementResponse: {}", dmgrResponse);
                 }
 
                 if (dmgrResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     Server dmgrResServer = dmgrResponse.getServer();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Server: {}", dmgrResServer);
                     }
 
                     if ((request.getAppServers() == null) || (request.getAppServers().size() == 0))
                     {
                         SearchRequest appServerSearch = new SearchRequest();
                         appServerSearch.setRequestInfo(reqInfo);
                         appServerSearch.setUserAccount(userAccount);
                         appServerSearch.setSearchTerms(dmgrResServer.getServerGuid());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchRequest: {}", appServerSearch);
                         }
 
                         SearchResponse appServerResponse = search.doServerSearch(appServerSearch);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResponse: {}", appServerResponse);
                         }
 
                         if (appServerResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             List<SearchResult> searchList = appServerResponse.getResults();
                             Map<String, String> appServerList = new HashMap<String, String>();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("searchList: {}", searchList);
                             }
 
                             for (SearchResult result : searchList)
                             {
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("SearchResult: {}", result);
                                 }
 
                                 if (!(StringUtils.equals(request.getPlatformDmgr(), result.getPath())))
                                 {
                                     appServerList.put(result.getPath(), result.getTitle());
                                 }
                             }
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("appServerList: {}", appServerList);
                             }
 
                             mView.addObject("appServerList", appServerList);
                         }
                     }
 
                     if ((request.getWebServers() == null) || (request.getWebServers().size() == 0))
                     {
                         Server webServerSearch = new Server();
                         webServerSearch.setServerType(ServerType.WEBSERVER);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Server: {}", webServerSearch);
                         }
 
                         ServerManagementRequest webRequest = new ServerManagementRequest();
                         webRequest.setRequestInfo(reqInfo);
                         webRequest.setUserAccount(userAccount);
                         webRequest.setServiceId(this.systemMgmt);
                         webRequest.setTargetServer(webServerSearch);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementRequest: {}", webRequest);
                         }
 
                         ServerManagementResponse webResponse = serverMgr.listServersByType(webRequest);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerManagementResponse: {}", webResponse);
                         }
 
                         if (webResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                         {
                             List<Server> webServers = webResponse.getServerList();
                             Map<String, String> webServerList = new HashMap<String, String>();
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("webServers: {}", webServers);
                             }
 
                             for (Server server : webServers)
                             {
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Server: {}", server);
                                 }
 
                                 if (server.getServerRegion() == dmgrResServer.getServerRegion())
                                 {
                                     webServerList.put(server.getServerGuid(), server.getOperHostName());
                                 }
                             }
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("webServerList: {}", webServerList);
                             }
 
                             mView.addObject("webServerList", webServerList);
                         }
                     }
 
                     request.setPlatformDmgr(request.getPlatformDmgr());
                     request.setPlatformRegion(dmgrResServer.getServerRegion());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("PlatformRequest: {}", request);
                     }
 
                     mView.addObject("command", request);
                     mView.setViewName(this.addPlatformPage);
                 }
                 else
                 {
                     // no valid dmgr
                     mView.addObject(Constants.ERROR_MESSAGE, this.messageNoDmgrFound);
                     mView.addObject("command", new PlatformRequest());
                     mView.setViewName(this.selectDmgrPage);
                 }
             }
             catch (ServerManagementException smx)
             {
                 ERROR_RECORDER.error(smx.getMessage(), smx);
 
                 mView.setViewName(appConfig.getErrorResponsePage());
             }
             catch (SearchRequestException srx)
             {
                 ERROR_RECORDER.error(srx.getMessage(), srx);
 
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
 
     @RequestMapping(value = "/submit-datacenter", method = RequestMethod.POST)
     public final ModelAndView doAddDatacenter(@ModelAttribute("request") final DataCenter request, final BindingResult bindResult)
     {
         final String methodName = ServiceManagementController.CNAME + "#doAddDatacenter(@ModelAttribute(\"request\") final DataCenter request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Value: {}", request);
             DEBUGGER.debug("Value: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IDatacenterManagementProcessor processor = new DatacenterManagementProcessorImpl();
 
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
             datacenterValidator.validate(request, bindResult);
 
             if (bindResult.hasErrors())
             {
                 // go back
                 return mView;
             }
 
             try
             {
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 DatacenterManagementRequest dcRequest = new DatacenterManagementRequest();
                 dcRequest.setDataCenter(request);
                 dcRequest.setRequestInfo(reqInfo);
                 dcRequest.setServiceId(this.dcService);
                 dcRequest.setUserAccount(userAccount);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("DatacenterManagementRequest: {}", dcRequest);
                 }
 
                 DatacenterManagementResponse dcResponse = processor.addNewDatacenter(dcRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("DatacenterManagementResponse: {}", dcResponse);
                 }
 
                 if (dcResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     // return to add dc page
                     mView.addObject(Constants.RESPONSE_MESSAGE, this.messageDatacenterSuccessfullyAdded);
                     mView.addObject("command", new DataCenter());
                     mView.setViewName(this.addDatacenterPage);
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, dcResponse.getResponse());
                     mView.addObject("command", request);
                     mView.setViewName(this.addDatacenterPage);
                 }
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
 
     @RequestMapping(value = "/add-project", method = RequestMethod.POST)
     public final ModelAndView doAddProject(@ModelAttribute("request") final Project request, final BindingResult bindResult)
     {
         final String methodName = ServiceManagementController.CNAME + "#doAddProject(@ModelAttribute(\"request\") final Project request, final BindingResult bindResult)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("Project: {}", request);
             DEBUGGER.debug("BindingResult: {}", bindResult);
         }
 
         ModelAndView mView = new ModelAndView();
 
         final ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
         final HttpServletRequest hRequest = requestAttributes.getRequest();
         final HttpSession hSession = hRequest.getSession();
         final UserAccount userAccount = (UserAccount) hSession.getAttribute(Constants.USER_ACCOUNT);
         final IProjectManagementProcessor projectMgr = new ProjectManagementProcessorImpl();
 
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
                 RequestHostInfo reqInfo = new RequestHostInfo();
                 reqInfo.setHostName(hRequest.getRemoteHost());
                 reqInfo.setHostAddress(hRequest.getRemoteAddr());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
                 }
 
                 ProjectManagementRequest projectRequest = new ProjectManagementRequest();
                 projectRequest.setRequestInfo(reqInfo);
                 projectRequest.setUserAccount(userAccount);
                 projectRequest.setServiceId(this.projectMgmt);
                 projectRequest.setProject(request);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementRequest: {}", projectRequest);
                 }
 
                 ProjectManagementResponse projectResponse = projectMgr.addNewProject(projectRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("ProjectManagementResponse: {}", projectResponse);
                 }
 
                 if (projectResponse.getRequestStatus() == CoreServicesStatus.SUCCESS)
                 {
                     mView.addObject(Constants.RESPONSE_MESSAGE, projectResponse.getResponse());
                 }
                 else
                 {
                     mView.addObject(Constants.ERROR_MESSAGE, projectResponse.getResponse());
                 }
 
                 mView.addObject("command", new Project());
                 mView.setViewName(this.addProjectPage);
             }
             catch (ProjectManagementException pmx)
             {
                 ERROR_RECORDER.error(pmx.getMessage(), pmx);
 
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
 }
 
