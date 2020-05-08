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
 
 import java.util.UUID;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.sql.SQLException;
 import org.apache.commons.lang.StringUtils;
 
 import com.cws.esolutions.agent.Constants;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.core.processors.dto.Project;
 import com.cws.esolutions.core.processors.enums.ServiceStatus;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.processors.dto.ProjectManagementRequest;
 import com.cws.esolutions.core.processors.dto.ProjectManagementResponse;
 import com.cws.esolutions.core.processors.exception.ProjectManagementException;
 import com.cws.esolutions.core.processors.interfaces.IProjectManagementProcessor;
 import com.cws.esolutions.security.access.control.exception.UserControlServiceException;
import com.cws.esolutions.security.audit.dto.AuditEntry;
import com.cws.esolutions.security.audit.dto.AuditRequest;
import com.cws.esolutions.security.audit.dto.RequestHostInfo;
import com.cws.esolutions.security.audit.enums.AuditType;
import com.cws.esolutions.security.audit.exception.AuditServiceException;
 /**
  * eSolutionsCore
  * com.cws.esolutions.core.processors.impl
  * ServerManagementProcessorImpl.java
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
 public class ProjectManagementProcessorImpl implements IProjectManagementProcessor
 {
     @Override
     public ProjectManagementResponse addNewProject(final ProjectManagementRequest request) throws ProjectManagementException
     {
         final String methodName = IProjectManagementProcessor.CNAME + "#addNewProject(final ProjectManagementRequest request) throws ProjectManagementException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ProjectManagementRequest: {}", request);
         }
 
         ProjectManagementResponse response = new ProjectManagementResponse();
 
         final Project project = request.getProject();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Project: {}", project);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount.getGuid(), request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                 if (project == null)
                 {
                     throw new ProjectManagementException("No platform was provided. Cannot continue.");
                 }
 
                 // make sure all the platform data is there
                 List<String[]> validator = null;
 
                 try
                 {
                     validator = projectDao.getProjectsByAttribute(project.getProjectCode(), request.getStartPage());
                 }
                 catch (SQLException sqx)
                 {
                     // don't do anything with it
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Validator: {}", validator);
                 }
 
                 if ((validator == null) || (validator.size() == 0))
                 {
                     List<String> insertData = new ArrayList<String>(
                             Arrays.asList(
                                     (StringUtils.isNotEmpty(project.getProjectGuid())) ? project.getProjectGuid() : UUID.randomUUID().toString(),
                                     project.getProjectCode(),
                                     project.getPrimaryContact(),
                                     (StringUtils.isNotEmpty(project.getSecondaryContact())) ? project.getSecondaryContact() : Constants.NOT_SET,
                                     project.getContactEmail(),
                                     project.getIncidentQueue(),
                                     project.getChangeQueue(),
                                     project.getProjectStatus().name()));
 
                     if (DEBUG)
                     {
                         for (Object str : insertData)
                         {
                             DEBUGGER.debug("Value: {}", str);
                         }
                     }
 
                     boolean isComplete = projectDao.addNewProject(insertData);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isComplete: {}", isComplete);
                     }
 
                     if (isComplete)
                     {
                         response.setRequestStatus(CoreServicesStatus.SUCCESS);
                         response.setResponse("Successfully added " + project.getProjectCode() + " to the asset datasource");
                     }
                     else
                     {
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                         response.setResponse("Failed to add " + project.getProjectCode() + " to the asset datasource");
                     }
                 }
                 else
                 {
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                     response.setResponse("Platform " + project.getProjectCode() + " already exists in the asset datasource.");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ProjectManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ProjectManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.ADDPROJECT);
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
     public ProjectManagementResponse updateProjectData(final ProjectManagementRequest request) throws ProjectManagementException
     {
         final String methodName = IProjectManagementProcessor.CNAME + "#updateProjectData(final ProjectManagementRequest request) throws ProjectManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ProjectManagementRequest: {}", request);
         }
 
         ProjectManagementResponse response = new ProjectManagementResponse();
 
         final Project project = request.getProject();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Project: {}", project);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount.getGuid(), request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                 List<String> insertData = new ArrayList<String>(
                         Arrays.asList(
                                 (StringUtils.isNotEmpty(project.getProjectGuid())) ? project.getProjectGuid() : UUID.randomUUID().toString(),
                                 project.getProjectCode(),
                                 project.getPrimaryContact(),
                                 (StringUtils.isNotEmpty(project.getSecondaryContact())) ? project.getSecondaryContact() : Constants.NOT_SET,
                                 project.getContactEmail(),
                                 project.getIncidentQueue(),
                                 project.getChangeQueue(),
                                 project.getProjectStatus().name()));
 
                 if (DEBUG)
                 {
                     for (Object str : insertData)
                     {
                         DEBUGGER.debug("Value: {}", str);
                     }
                 }
 
                 boolean isComplete = projectDao.updateProjectData(insertData);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isComplete: {}", isComplete);
                 }
 
                 if (isComplete)
                 {
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setResponse("Successfully added " + project.getProjectCode() + " to the asset datasource");
                 }
                 else
                 {
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                     response.setResponse("Failed to add " + project.getProjectCode() + " to the asset datasource");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ProjectManagementException(ucsx.getMessage(), ucsx);
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ProjectManagementException(sqx.getMessage(), sqx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.UPDATEPROJECT);
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
     public ProjectManagementResponse listProjects(final ProjectManagementRequest request) throws ProjectManagementException
     {
         final String methodName = IProjectManagementProcessor.CNAME + "#listProjects(final ProjectManagementRequest request) throws ProjectManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ProjectManagementRequest: {}", request);
         }
 
         ProjectManagementResponse response = new ProjectManagementResponse();
 
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount.getGuid(), request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                 int count = projectDao.getProjectCount();
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("count: {}", count);
                 }
 
                 List<String[]> projectData = projectDao.listAvailableProjects(request.getStartPage());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("projectData: {}", projectData);
                 }
 
                 if ((projectData != null) && (projectData.size() != 0))
                 {
                     List<Project> projectList = new ArrayList<Project>();
 
                     for (String[] data : projectData)
                     {
                         Project resProject = new Project();
                         resProject.setProjectGuid(data[0]);
                         resProject.setProjectCode(data[1]);
                         resProject.setProjectStatus(ServiceStatus.valueOf(data[2]));
                         resProject.setPrimaryContact(data[3]);
                         resProject.setSecondaryContact(data[4]);
                         resProject.setContactEmail(data[5]);
                         resProject.setIncidentQueue(data[6]);
                         resProject.setChangeQueue(data[7]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Project: {}", resProject);
                         }
 
                         projectList.add(resProject);
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("projectList: {}", projectList);
                     }
 
                     response.setEntryCount(count);
                     response.setProjectList(projectList);
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setResponse("Successfully loaded project list");
                 }
                 else
                 {
                     throw new ProjectManagementException("No projects were located in the asset datasource.");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ProjectManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ProjectManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.LISTPROJECTS);
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
     public ProjectManagementResponse listProjectsByAttribute(final ProjectManagementRequest request) throws ProjectManagementException
     {
         final String methodName = IProjectManagementProcessor.CNAME + "#listProjectsByAttribute(final ProjectManagementRequest request) throws ProjectManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ProjectManagementRequest: {}", request);
         }
 
         ProjectManagementResponse response = new ProjectManagementResponse();
 
         final Project project = request.getProject();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Project: {}", project);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount.getGuid(), request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                 List<String[]> projectData = projectDao.getProjectsByAttribute(project.getProjectCode(), request.getStartPage());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("projectData: {}", projectData);
                 }
 
                 if ((projectData != null) && (projectData.size() != 0))
                 {
                     List<Project> projectList = new ArrayList<Project>();
 
                     for (String[] data : projectData)
                     {
                         Project resProject = new Project();
                         resProject.setProjectGuid(data[0]);
                         resProject.setProjectCode(data[1]);
                         resProject.setProjectStatus(ServiceStatus.valueOf(data[2]));
                         resProject.setPrimaryContact(data[3]);
                         resProject.setSecondaryContact(data[4]);
                         resProject.setContactEmail(data[5]);
                         resProject.setIncidentQueue(data[6]);
                         resProject.setChangeQueue(data[7]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Project: {}", resProject);
                         }
 
                         projectList.add(resProject);
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("projectList: {}", projectList);
                     }
 
                     response.setProjectList(projectList);
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setResponse("Successfully loaded project list");
                 }
                 else
                 {
                     throw new ProjectManagementException("No projects were located in the asset datasource.");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ProjectManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ProjectManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.LISTPROJECTS);
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
     public ProjectManagementResponse getProjectData(final ProjectManagementRequest request) throws ProjectManagementException
     {
         final String methodName = IProjectManagementProcessor.CNAME + "#getProjectData(final ProjectManagementRequest request) throws ProjectManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ProjectManagementRequest: {}", request);
         }
 
         ProjectManagementResponse response = new ProjectManagementResponse();
 
         final Project project = request.getProject();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Project: {}", project);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
         }
 
         try
         {
             boolean isServiceAuthorized = userControl.isUserAuthorizedForService(userAccount.getGuid(), request.getServiceId());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("isServiceAuthorized: {}", isServiceAuthorized);
             }
 
             if (isServiceAuthorized)
             {
                 if (project != null)
                 {
                     List<String> projectList = projectDao.getProjectData(project.getProjectGuid());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("projectList: {}", projectList);
                     }
 
                     if ((projectList != null) && (projectList.size() != 0))
                     {
                         project.setProjectGuid(projectList.get(0));
                         project.setProjectCode(projectList.get(1));
                         project.setProjectStatus(ServiceStatus.valueOf(projectList.get(2)));
                         project.setPrimaryContact(projectList.get(3));
                         project.setSecondaryContact(projectList.get(4));
                         project.setContactEmail(projectList.get(5));
                         project.setIncidentQueue(projectList.get(6));
                         project.setChangeQueue(projectList.get(7));
 
                         // get the list of applications associated with this project
                         List<String[]> appList = appDao.getApplicationsByAttribute(projectList.get(0), 0);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("List<String[]>: {}", appList);
                         }
 
                         if ((appList != null) && (appList.size() != 0))
                         {
                             List<Application> applicationList = new ArrayList<Application>();
 
                             // we're only loading the application, not the platform
                             // we'll link to the application info page, which will draw
                             // the platform for us. really we don't even need more than
                             // the name and the guid. and maybe the version. so that's
                             // all we're adding.
                             for (String[] appData : appList)
                             {
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("appData: {}", appData);
                                 }
 
                                 Application app = new Application();
                                 app.setApplicationGuid(appData[0]);
                                 app.setApplicationName(appData[1]);
                                 app.setApplicationVersion(appData[2]);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Application: {}", app);
                                 }
 
                                 applicationList.add(app);
                             }
 
                             project.setApplicationList(applicationList);
                         }
                                 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("Project: {}", project);
                         }
 
                         response.setRequestStatus(CoreServicesStatus.SUCCESS);
                         response.setResponse("Successfully loaded project data");
                         response.setProject(project);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ProjectManagementResponse: {}", response);
                         }
                     }
                     else
                     {
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                         response.setResponse("No projects were found with the provided information.");
                     }
                 }
                 else
                 {
                     throw new ProjectManagementException("No project data was provided. Cannot continue.");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ProjectManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ProjectManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.LOADPROJECT);
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
