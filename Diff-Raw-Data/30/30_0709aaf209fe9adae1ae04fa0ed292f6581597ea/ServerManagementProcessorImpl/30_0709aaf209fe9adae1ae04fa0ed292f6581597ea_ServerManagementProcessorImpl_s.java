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
 import com.cws.esolutions.core.utils.MQUtils;
 import com.cws.esolutions.security.enums.Role;
 import com.cws.esolutions.agent.dto.AgentRequest;
 import com.cws.esolutions.agent.dto.AgentResponse;
 import com.cws.esolutions.agent.enums.AgentStatus;
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.core.processors.dto.Server;
 import com.cws.esolutions.security.audit.dto.AuditEntry;
 import com.cws.esolutions.security.audit.enums.AuditType;
 import com.cws.esolutions.core.processors.dto.DataCenter;
 import com.cws.esolutions.security.audit.dto.AuditRequest;
 import com.cws.esolutions.core.processors.enums.ServerType;
 import com.cws.esolutions.security.audit.dto.RequestHostInfo;
 import com.cws.esolutions.core.processors.enums.ServerStatus;
 import com.cws.esolutions.core.processors.enums.ServiceStatus;
 import com.cws.esolutions.core.processors.enums.ServiceRegion;
 import com.cws.esolutions.security.enums.SecurityRequestStatus;
 import com.cws.esolutions.core.utils.exception.UtilityException;
 import com.cws.esolutions.core.processors.enums.NetworkPartition;
 import com.cws.esolutions.agent.processors.enums.SystemCheckType;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.agent.processors.dto.SystemManagerRequest;
 import com.cws.esolutions.agent.processors.dto.SystemManagerResponse;
 import com.cws.esolutions.agent.processors.enums.SystemManagementType;
 import com.cws.esolutions.core.processors.dto.ServerManagementRequest;
 import com.cws.esolutions.core.processors.dto.ServerManagementResponse;
 import com.cws.esolutions.security.dao.usermgmt.enums.SearchRequestType;
 import com.cws.esolutions.security.processors.dto.AccountControlRequest;
 import com.cws.esolutions.security.processors.dto.AccountControlResponse;
 import com.cws.esolutions.security.audit.exception.AuditServiceException;
 import com.cws.esolutions.core.processors.exception.ServerManagementException;
 import com.cws.esolutions.security.processors.impl.AccountControlProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.IServerManagementProcessor;
 import com.cws.esolutions.security.processors.exception.AccountControlException;
 import com.cws.esolutions.security.processors.interfaces.IAccountControlProcessor;
 import com.cws.esolutions.security.access.control.exception.UserControlServiceException;
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
 public class ServerManagementProcessorImpl implements IServerManagementProcessor
 {
     @Override
     public ServerManagementResponse addNewServer(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#addNewServer(final ServerManagementRequest request) throws ServerManagementException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: {}", request);
         }
 
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server requestServer = request.getTargetServer();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", requestServer);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
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
                 if (requestServer == null)
                 {
                     throw new ServerManagementException("No server was provided. Cannot continue.");
                 }
 
                 // make sure all the platform data is there
                 List<Object[]> validator = null;
 
                 try
                 {
                     validator = serverDAO.getServersByAttribute(requestServer.getOperHostName(), request.getStartPage());
                 }
                 catch (SQLException sqx)
                 {
                     ERROR_RECORDER.error(sqx.getMessage(), sqx);
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Validator: {}", validator);
                 }
 
                 if ((validator == null) || (validator.size() == 0))
                 {
                     // valid server
                     if ((requestServer.getServerType() == ServerType.VIRTUALHOST) || (requestServer.getServerType() == ServerType.DMGRSERVER))
                     {
                         if (StringUtils.isEmpty(requestServer.getMgrUrl()))
                         {
                             throw new ServerManagementException("Server type provided was " + requestServer.getServerType() + " but additional information is required.");
                         }
                         else if ((requestServer.getServerType() == ServerType.DMGRSERVER) && (requestServer.getDmgrPort() == 0))
                         {
                             throw new ServerManagementException("Server type provided was " + requestServer.getServerType() + " but additional information is required.");
                         }
                     }
 
                     List<Object> insertData = new ArrayList<Object>(
                             Arrays.asList(
                                     UUID.randomUUID().toString(),
                                     requestServer.getOsName(),
                                     requestServer.getServerStatus().name(),
                                     requestServer.getServerRegion().name(),
                                     requestServer.getNetworkPartition().name(),
                                     requestServer.getDatacenter().getDatacenterGuid(),
                                     requestServer.getServerType().name(),
                                     requestServer.getDomainName(),
                                     requestServer.getCpuType(),
                                     requestServer.getCpuCount(),
                                     requestServer.getServerModel(),
                                     requestServer.getSerialNumber(),
                                     requestServer.getInstalledMemory(),
                                     requestServer.getOperIpAddress(),
                                     requestServer.getOperHostName(),
                                     (StringUtils.isNotEmpty(requestServer.getMgmtIpAddress())) ? requestServer.getMgmtIpAddress() : Constants.NOT_SET,
                                     (StringUtils.isNotEmpty(requestServer.getMgmtHostName())) ? requestServer.getMgmtHostName() : Constants.NOT_SET,
                                     (StringUtils.isNotEmpty(requestServer.getBkIpAddress())) ? requestServer.getBkIpAddress() : Constants.NOT_SET,
                                     (StringUtils.isNotEmpty(requestServer.getBkHostName())) ? requestServer.getBkHostName() : Constants.NOT_SET,
                                     (StringUtils.isNotEmpty(requestServer.getNasIpAddress())) ? requestServer.getNasIpAddress() : Constants.NOT_SET,
                                     (StringUtils.isNotEmpty(requestServer.getNasHostName())) ? requestServer.getNasHostName() : Constants.NOT_SET,
                                     (StringUtils.isNotEmpty(requestServer.getNatAddress())) ? requestServer.getNatAddress() : Constants.NOT_SET,
                                     requestServer.getServerComments(),
                                    userAccount.getUsername(),
                                     (StringUtils.isNotBlank(requestServer.getMgrUrl())) ? requestServer.getMgrUrl() : Constants.NOT_SET,
                                     requestServer.getDmgrPort(),
                                     (StringUtils.isNotBlank(requestServer.getServerRack())) ? requestServer.getServerRack() : Constants.NOT_SET,
                                     (StringUtils.isNotBlank(requestServer.getRackPosition())) ? requestServer.getRackPosition() : Constants.NOT_SET,
                                     (requestServer.getOwningDmgr() != null) ? requestServer.getOwningDmgr().getServerGuid() : Constants.NOT_SET));
 
                     if (DEBUG)
                     {
                         for (Object str : insertData)
                         {
                             DEBUGGER.debug("Value: {}", str);
                         }
                     }
 
                     boolean isComplete = serverDAO.addNewServer(insertData);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("isComplete: {}", isComplete);
                     }
 
                     if (isComplete)
                     {
                         // install agent
                         response.setRequestStatus(CoreServicesStatus.SUCCESS);
                         response.setResponse("Successfully added " + requestServer.getOperHostName() + " to the asset datasource");
                     }
                     else
                     {
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                         response.setResponse("Failed to add " + requestServer.getOperHostName() + " to the asset datasource");
                     }
                 }
                 else
                 {
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                     response.setResponse("Server " + requestServer.getOperHostName() + " already exists in the asset datasource.");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ServerManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.ADDSERVER);
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
     public ServerManagementResponse updateServerData(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#updateServerData(final ServerManagementRequest request) throws ServerManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: {}", request);
         }
 
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server requestServer = request.getTargetServer();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", requestServer);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
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
                 if ((requestServer.getServerType() == ServerType.VIRTUALHOST) || (requestServer.getServerType() == ServerType.DMGRSERVER))
                 {
                     if (StringUtils.isEmpty(requestServer.getMgrUrl()))
                     {
                         throw new ServerManagementException("Server type provided was " + requestServer.getServerType() + " but additional information is required.");
                     }
                     else if ((requestServer.getServerType() == ServerType.DMGRSERVER) && (requestServer.getDmgrPort() == 0))
                     {
                         throw new ServerManagementException("Server type provided was " + requestServer.getServerType() + " but additional information is required.");
                     }
                 }
 
                 List<Object> insertData = new ArrayList<Object>(
                         Arrays.asList(
                                 requestServer.getOsName(),
                                 requestServer.getServerStatus().name(),
                                 requestServer.getServerRegion().name(),
                                 requestServer.getNetworkPartition().name(),
                                 requestServer.getDatacenter().getDatacenterGuid(),
                                 requestServer.getServerType().name(),
                                 requestServer.getDomainName(),
                                 requestServer.getCpuType(),
                                 requestServer.getCpuCount(),
                                 requestServer.getServerModel(),
                                 requestServer.getSerialNumber(),
                                 requestServer.getInstalledMemory(),
                                 requestServer.getOperIpAddress(),
                                 requestServer.getOperHostName(),
                                 (StringUtils.isNotEmpty(requestServer.getMgmtIpAddress())) ? requestServer.getMgmtIpAddress() : Constants.NOT_SET,
                                 (StringUtils.isNotEmpty(requestServer.getMgmtHostName())) ? requestServer.getMgmtHostName() : Constants.NOT_SET,
                                 (StringUtils.isNotEmpty(requestServer.getBkIpAddress())) ? requestServer.getBkIpAddress() : Constants.NOT_SET,
                                 (StringUtils.isNotEmpty(requestServer.getBkHostName())) ? requestServer.getBkHostName() : Constants.NOT_SET,
                                 (StringUtils.isNotEmpty(requestServer.getNasIpAddress())) ? requestServer.getNasIpAddress() : Constants.NOT_SET,
                                 (StringUtils.isNotEmpty(requestServer.getNasHostName())) ? requestServer.getNasHostName() : Constants.NOT_SET,
                                 (StringUtils.isNotEmpty(requestServer.getNatAddress())) ? requestServer.getNatAddress() : Constants.NOT_SET,
                                 requestServer.getServerComments(),
                                requestServer.getAssignedEngineer(),
                                 (StringUtils.isNotBlank(requestServer.getMgrUrl())) ? requestServer.getMgrUrl() : Constants.NOT_SET,
                                 requestServer.getDmgrPort(),
                                 (StringUtils.isNotBlank(requestServer.getServerRack())) ? requestServer.getServerRack() : Constants.NOT_SET,
                                 (StringUtils.isNotBlank(requestServer.getRackPosition())) ? requestServer.getRackPosition() : Constants.NOT_SET,
                                 (requestServer.getOwningDmgr() != null) ? requestServer.getOwningDmgr().getServerGuid() : Constants.NOT_SET));
 
                 if (DEBUG)
                 {
                     for (Object str : insertData)
                     {
                         DEBUGGER.debug("Value: {}", str);
                     }
                 }
 
                 boolean isComplete = serverDAO.modifyServerData(requestServer.getServerGuid(), insertData);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("isComplete: {}", isComplete);
                 }
 
                 if (isComplete)
                 {
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setResponse("Successfully added " + requestServer.getOperHostName() + " to the asset datasource");
                 }
                 else
                 {
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                     response.setResponse("Failed to add " + requestServer.getOperHostName() + " to the asset datasource");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ServerManagementException(sqx.getMessage(), sqx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.UPDATESERVER);
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
     public ServerManagementResponse listServersByDmgr(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#listServersByDmgr(final ServerManagementRequest request) throws ServerManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: {}", request);
         }
 
         UserAccount svcAccount = null;
         UserAccount searchAccount = null;
         AccountControlRequest searchRequest = null;
         AccountControlResponse searchResponse = null;
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server targetServer = request.getTargetServer(); // dmgr
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final IAccountControlProcessor acctControl = new AccountControlProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", targetServer);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
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
                 List<Object[]> serverData = serverDAO.getServersForDmgr(targetServer.getServerGuid());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("serverList: {}", serverData);
                 }
 
                 if ((serverData != null) && (serverData.size() != 0))
                 {
                     List<Server> serverList = new ArrayList<>();
 
                     // retrServersForDmgr
                     for (Object[] data : serverData)
                     {
                         List<String> datacenter = datactrDAO.getDatacenter((String) data[5]); // DATACENTER_GUID
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("List<String>: {}", datacenter);
                         }
 
                         if ((datacenter != null) && (datacenter.size() != 0))
                         {
                             DataCenter dataCenter = new DataCenter();
                             dataCenter.setDatacenterGuid(datacenter.get(0));
                             dataCenter.setDatacenterName(datacenter.get(1));
                             dataCenter.setDatacenterStatus(ServiceStatus.valueOf(datacenter.get(2)));
                             dataCenter.setDatacenterDesc(datacenter.get(3));
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("DataCenter: {}", dataCenter);
                             }
 
                             List<Object> dmgrData = serverDAO.getInstalledServer(targetServer.getServerGuid());
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("List<Object>: {}", dmgrData);
                             }
 
                             if ((dmgrData != null) && (dmgrData.size() != 0))
                             {
                                 Server dmgrServer = new Server();
                                 dmgrServer.setServerGuid((String) dmgrData.get(0)); // SYSTEM_GUID
                                 dmgrServer.setOsName((String) dmgrData.get(1)); // SYSTEM_OSTYPE
                                 dmgrServer.setServerStatus(ServerStatus.valueOf((String) dmgrData.get(2))); // SYSTEM_STATUS
                                 dmgrServer.setServerRegion(ServiceRegion.valueOf((String) dmgrData.get(3))); // SYSTEM_REGION
                                 dmgrServer.setNetworkPartition(NetworkPartition.valueOf((String) dmgrData.get(4))); // NETWORK_PARTITION
                                 dmgrServer.setDatacenter(dataCenter); // datacenter as earlier obtained
                                 dmgrServer.setServerType(ServerType.valueOf((String) dmgrData.get(6))); // SYSTEM_TYPE
                                 dmgrServer.setDomainName((String) dmgrData.get(7)); // DOMAIN_NAME
                                 dmgrServer.setCpuType((String) dmgrData.get(8)); // CPU_TYPE
                                 dmgrServer.setCpuCount((Integer) dmgrData.get(9)); // CPU_COUNT
                                 dmgrServer.setServerRack((String) dmgrData.get(10)); // SERVER_RACK
                                 dmgrServer.setRackPosition((String) dmgrData.get(11)); // RACK_POSITION
                                 dmgrServer.setServerModel((String) dmgrData.get(12)); // SERVER_MODEL
                                 dmgrServer.setSerialNumber((String) dmgrData.get(13)); // SERIAL_NUMBER
                                 dmgrServer.setInstalledMemory((Integer) dmgrData.get(14)); // INSTALLED_MEMORY
                                 dmgrServer.setOperIpAddress((String) dmgrData.get(15)); // OPER_IP
                                 dmgrServer.setOperHostName((String) dmgrData.get(16)); // OPER_HOSTNAME
                                 dmgrServer.setMgmtIpAddress((String) dmgrData.get(17)); // MGMT_IP
                                 dmgrServer.setMgmtHostName((String) dmgrData.get(18)); // MGMT_HOSTNAME
                                 dmgrServer.setBkIpAddress((String) dmgrData.get(19)); // BKUP_IP
                                 dmgrServer.setBkHostName((String) dmgrData.get(20)); // BKUP_HOSTNAME
                                 dmgrServer.setNasIpAddress((String) dmgrData.get(21)); // NAS_IP
                                 dmgrServer.setNasHostName((String) dmgrData.get(22)); // NAS_HOSTNAME
                                 dmgrServer.setNatAddress((String) dmgrData.get(23)); // NAT_ADDR
                                 dmgrServer.setServerComments((String) dmgrData.get(24)); // COMMENTS
                                 dmgrServer.setDmgrPort((Integer) dmgrData.get(28)); // DMGR_PORT
                                 dmgrServer.setMgrUrl((String) dmgrData.get(30)); // MGR_ENTRY
 
                                 searchAccount = new UserAccount();
                                 searchAccount.setGuid((String) dmgrData.get(25));
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("UserAccount: {}", searchAccount);
                                 }
 
                                 svcAccount = new UserAccount();
                                 svcAccount.setUsername(serviceAccount.get(0));
                                 svcAccount.setGuid(serviceAccount.get(1));
                                 svcAccount.setRole(Role.valueOf(serviceAccount.get(2)));
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("UserAccount: {}", svcAccount);
                                 }
 
                                 searchRequest = new AccountControlRequest();
                                 searchRequest.setHostInfo(request.getRequestInfo());
                                 searchRequest.setUserAccount(searchAccount);
                                 searchRequest.setApplicationName(request.getApplicationName());
                                 searchRequest.setApplicationId(request.getApplicationId());
                                 searchRequest.setSearchType(SearchRequestType.GUID);
                                 searchRequest.setRequestor(svcAccount);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("AccountControlRequest: {}", searchRequest);
                                 }
 
                                 try
                                 {
                                     searchResponse = acctControl.loadUserAccount(searchRequest);
 
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("AccountControlResponse: {}", searchResponse);
                                     }
 
                                     if (searchResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                                     {
                                         dmgrServer.setAssignedEngineer(searchResponse.getUserAccount()); // ASSIGNED_ENGINEER
                                     }
                                 }
                                 catch (AccountControlException acx)
                                 {
                                     ERROR_RECORDER.error(acx.getMessage(), acx);
                                 }
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Server: {}", dmgrServer);
                                 }
 
                                 Server server = new Server();
                                 server.setServerGuid((String) data[0]); // SYSTEM_GUID
                                 server.setOsName((String) data[1]); // SYSTEM_OSTYPE
                                 server.setServerStatus(ServerStatus.valueOf((String) data[2])); // SYSTEM_STATUS
                                 server.setServerRegion(ServiceRegion.valueOf((String) data[3])); // SYSTEM_REGION
                                 server.setNetworkPartition(NetworkPartition.valueOf((String) data[4])); // NETWORK_PARTITION
                                 server.setDatacenter(dataCenter); // datacenter as earlier obtained
                                 server.setServerType(ServerType.valueOf((String) data[6])); // SYSTEM_TYPE
                                 server.setDomainName((String) data[7]); // DOMAIN_NAME
                                 server.setCpuType((String) data[8]); // CPU_TYPE
                                 server.setCpuCount((Integer) data[9]); // CPU_COUNT
                                 server.setServerRack((String) data[10]); // SERVER_RACK
                                 server.setRackPosition((String) data[11]); // RACK_POSITION
                                 server.setServerModel((String) data[12]); // SERVER_MODEL
                                 server.setSerialNumber((String) data[13]); // SERIAL_NUMBER
                                 server.setInstalledMemory((Integer) data[14]); // INSTALLED_MEMORY
                                 server.setOperIpAddress((String) data[15]); // OPER_IP
                                 server.setOperHostName((String) data[16]); // OPER_HOSTNAME
                                 server.setMgmtIpAddress((String) data[17]); // MGMT_IP
                                 server.setMgmtHostName((String) data[18]); // MGMT_HOSTNAME
                                 server.setBkIpAddress((String) data[19]); // BKUP_IP
                                 server.setBkHostName((String) data[20]); // BKUP_HOSTNAME
                                 server.setNasIpAddress((String) data[21]); // NAS_IP
                                 server.setNasHostName((String) data[22]); // NAS_HOSTNAME
                                 server.setNatAddress((String) data[23]); // NAT_ADDR
                                 server.setServerComments((String) data[24]); // COMMENTS
                                 server.setOwningDmgr(dmgrServer);
 
                                 searchAccount = new UserAccount();
                                 searchAccount.setGuid((String) data[25]);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("UserAccount: {}", searchAccount);
                                 }
 
                                 svcAccount = new UserAccount();
                                 svcAccount.setUsername(serviceAccount.get(0));
                                 svcAccount.setGuid(serviceAccount.get(1));
                                 svcAccount.setRole(Role.valueOf(serviceAccount.get(2)));
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("UserAccount: {}", svcAccount);
                                 }
 
                                 searchRequest = new AccountControlRequest();
                                 searchRequest.setHostInfo(request.getRequestInfo());
                                 searchRequest.setUserAccount(searchAccount);
                                 searchRequest.setApplicationName(request.getApplicationName());
                                 searchRequest.setApplicationId(request.getApplicationId());
                                 searchRequest.setSearchType(SearchRequestType.GUID);
                                 searchRequest.setRequestor(svcAccount);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("AccountControlRequest: {}", searchRequest);
                                 }
 
                                 try
                                 {
                                     searchResponse = acctControl.loadUserAccount(searchRequest);
 
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("AccountControlResponse: {}", searchResponse);
                                     }
 
                                     if (searchResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                                     {
                                         server.setAssignedEngineer(searchResponse.getUserAccount()); // ASSIGNED_ENGINEER
                                     }
                                 }
                                 catch (AccountControlException acx)
                                 {
                                     ERROR_RECORDER.error(acx.getMessage(), acx);
                                 }
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Server: {}", server);
                                 }
 
                                 serverList.add(server);
                             }
                             else
                             {
                                 response.setRequestStatus(CoreServicesStatus.FAILURE);
                                 response.setResponse("Failed to load deployment manager data. Cannot continue.");
 
                                 return response;
                             }
                         }
                         else
                         {
                             ERROR_RECORDER.error("Server has no assigned datacenter");
 
                             continue;
                         }
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("serverList: {}", serverList);
                     }
 
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setResponse("Successfully loaded installed server information.");
                     response.setServerList(serverList);
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ServerManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.LISTSERVERS);
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
     public ServerManagementResponse listServersByType(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#listServersByType(final ServerManagementRequest request) throws ServerManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: {}", request);
         }
 
         UserAccount svcAccount = null;
         UserAccount searchAccount = null;
         AccountControlRequest searchRequest = null;
         AccountControlResponse searchResponse = null;
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server requestServer = request.getTargetServer();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final IAccountControlProcessor acctControl = new AccountControlProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", requestServer);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
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
                 List<Object[]> serverData = serverDAO.getServersByAttribute(requestServer.getServerType().name(), request.getStartPage());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("serverList: {}", serverData);
                 }
 
                 if ((serverData != null) && (serverData.size() != 0))
                 {
                     List<Server> serverList = new ArrayList<>();
 
                     for (Object[] data : serverData)
                     {
                         if (DEBUG)
                         {
                             for (Object obj : data)
                             {
                                 DEBUGGER.debug("Value: {}", obj);
                             }
                         }
 
                         ServerType serverType = ServerType.valueOf((String) data[6]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ServerType: {}", serverType);
                         }
 
                         if (serverType == requestServer.getServerType())
                         {
                             List<String> datacenter = datactrDAO.getDatacenter((String) data[5]);
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("List<String>: {}", datacenter);
                             }
 
                             if ((datacenter != null) && (datacenter.size() != 0))
                             {
                                 DataCenter dataCenter = new DataCenter();
                                 dataCenter.setDatacenterGuid(datacenter.get(0));
                                 dataCenter.setDatacenterName(datacenter.get(1));
                                 dataCenter.setDatacenterStatus(ServiceStatus.valueOf(datacenter.get(2)));
                                 dataCenter.setDatacenterDesc(datacenter.get(3));
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("DataCenter: {}", dataCenter);
                                 }
 
                                 Server server = new Server();
                                 server.setServerGuid((String) data[0]); // SYSTEM_GUID
                                 server.setOsName((String) data[1]); // SYSTEM_OSTYPE
                                 server.setServerStatus(ServerStatus.valueOf((String) data[2])); // SYSTEM_STATUS
                                 server.setServerRegion(ServiceRegion.valueOf((String) data[3])); // SYSTEM_REGION
                                 server.setNetworkPartition(NetworkPartition.valueOf((String) data[4])); // NETWORK_PARTITION
                                 server.setDatacenter(dataCenter); // datacenter as earlier obtained
                                 server.setServerType(ServerType.valueOf((String) data[6])); // SYSTEM_TYPE
                                 server.setDomainName((String) data[7]); // DOMAIN_NAME
                                 server.setCpuType((String) data[8]); // CPU_TYPE
                                 server.setCpuCount((Integer) data[9]); // CPU_COUNT
                                 server.setServerRack((String) data[10]); // SERVER_RACK
                                 server.setRackPosition((String) data[11]); // RACK_POSITION
                                 server.setServerModel((String) data[12]); // SERVER_MODEL
                                 server.setSerialNumber((String) data[13]); // SERIAL_NUMBER
                                 server.setInstalledMemory((Integer) data[14]); // INSTALLED_MEMORY
                                 server.setOperIpAddress((String) data[15]); // OPER_IP
                                 server.setOperHostName((String) data[16]); // OPER_HOSTNAME
                                 server.setMgmtIpAddress((String) data[17]); // MGMT_IP
                                 server.setMgmtHostName((String) data[18]); // MGMT_HOSTNAME
                                 server.setBkIpAddress((String) data[19]); // BKUP_IP
                                 server.setBkHostName((String) data[20]); // BKUP_HOSTNAME
                                 server.setNasIpAddress((String) data[21]); // NAS_IP
                                 server.setNasHostName((String) data[22]); // NAS_HOSTNAME
                                 server.setNatAddress((String) data[23]); // NAT_ADDR
                                 server.setServerComments((String) data[24]); // COMMENTS
 
                                 searchAccount = new UserAccount();
                                 searchAccount.setGuid((String) data[25]);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("UserAccount: {}", searchAccount);
                                 }
 
                                 svcAccount = new UserAccount();
                                 svcAccount.setUsername(serviceAccount.get(0));
                                 svcAccount.setGuid(serviceAccount.get(1));
                                 svcAccount.setRole(Role.valueOf(serviceAccount.get(2)));
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("UserAccount: {}", svcAccount);
                                 }
 
                                 searchRequest = new AccountControlRequest();
                                 searchRequest.setHostInfo(request.getRequestInfo());
                                 searchRequest.setUserAccount(searchAccount);
                                 searchRequest.setApplicationName(request.getApplicationName());
                                 searchRequest.setApplicationId(request.getApplicationId());
                                 searchRequest.setSearchType(SearchRequestType.GUID);
                                 searchRequest.setRequestor(svcAccount);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("AccountControlRequest: {}", searchRequest);
                                 }
 
                                 try
                                 {
                                     searchResponse = acctControl.loadUserAccount(searchRequest);
 
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("AccountControlResponse: {}", searchResponse);
                                     }
 
                                     if (searchResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                                     {
                                         server.setAssignedEngineer(searchResponse.getUserAccount()); // ASSIGNED_ENGINEER
                                     }
                                 }
                                 catch (AccountControlException acx)
                                 {
                                     ERROR_RECORDER.error(acx.getMessage(), acx);
                                 }
 
                                 switch (server.getServerType())
                                 {
                                     case APPSERVER:
                                         // set owning dmgr
                                         List<Object> dmgrData = serverDAO.getInstalledServer((String) data[29]);
 
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("dmgrData: {}", dmgrData);
                                         }
 
                                         if ((dmgrData != null) && (dmgrData.size() != 0))
                                         {
                                             Server dmgrServer = new Server();
                                             dmgrServer.setServerGuid((String) dmgrData.get(0)); // SYSTEM_GUID
                                             dmgrServer.setOsName((String) dmgrData.get(1)); // SYSTEM_OSTYPE
                                             dmgrServer.setServerStatus(ServerStatus.valueOf((String) dmgrData.get(2))); // SYSTEM_STATUS
                                             dmgrServer.setServerRegion(ServiceRegion.valueOf((String) dmgrData.get(3))); // SYSTEM_REGION
                                             dmgrServer.setNetworkPartition(NetworkPartition.valueOf((String) dmgrData.get(4))); // NETWORK_PARTITION
                                             dmgrServer.setDatacenter(dataCenter); // datacenter as earlier obtained
                                             dmgrServer.setServerType(ServerType.valueOf((String) dmgrData.get(6))); // SYSTEM_TYPE
                                             dmgrServer.setDomainName((String) dmgrData.get(7)); // DOMAIN_NAME
                                             dmgrServer.setCpuType((String) dmgrData.get(8)); // CPU_TYPE
                                             dmgrServer.setCpuCount((Integer) dmgrData.get(9)); // CPU_COUNT
                                             dmgrServer.setServerRack((String) dmgrData.get(10)); // SERVER_RACK
                                             dmgrServer.setRackPosition((String) dmgrData.get(11)); // RACK_POSITION
                                             dmgrServer.setServerModel((String) dmgrData.get(12)); // SERVER_MODEL
                                             dmgrServer.setSerialNumber((String) dmgrData.get(13)); // SERIAL_NUMBER
                                             dmgrServer.setInstalledMemory((Integer) dmgrData.get(14)); // INSTALLED_MEMORY
                                             dmgrServer.setOperIpAddress((String) dmgrData.get(15)); // OPER_IP
                                             dmgrServer.setOperHostName((String) dmgrData.get(16)); // OPER_HOSTNAME
                                             dmgrServer.setMgmtIpAddress((String) dmgrData.get(17)); // MGMT_IP
                                             dmgrServer.setMgmtHostName((String) dmgrData.get(18)); // MGMT_HOSTNAME
                                             dmgrServer.setBkIpAddress((String) dmgrData.get(19)); // BKUP_IP
                                             dmgrServer.setBkHostName((String) dmgrData.get(20)); // BKUP_HOSTNAME
                                             dmgrServer.setNasIpAddress((String) dmgrData.get(21)); // NAS_IP
                                             dmgrServer.setNasHostName((String) dmgrData.get(22)); // NAS_HOSTNAME
                                             dmgrServer.setNatAddress((String) dmgrData.get(23)); // NAT_ADDR
                                             dmgrServer.setServerComments((String) dmgrData.get(24)); // COMMENTS
                                             dmgrServer.setDmgrPort((Integer) dmgrData.get(28)); // DMGR_PORT
                                             dmgrServer.setMgrUrl((String) dmgrData.get(30)); // MGR_ENTRY
 
                                             searchAccount = new UserAccount();
                                             searchAccount.setGuid((String) dmgrData.get(25));
 
                                             if (DEBUG)
                                             {
                                                 DEBUGGER.debug("UserAccount: {}", searchAccount);
                                             }
 
                                             searchRequest = new AccountControlRequest();
                                             searchRequest.setHostInfo(request.getRequestInfo());
                                             searchRequest.setUserAccount(searchAccount);
                                             searchRequest.setApplicationName(request.getApplicationName());
                                             searchRequest.setApplicationId(request.getApplicationId());
                                             searchRequest.setSearchType(SearchRequestType.GUID);
                                             searchRequest.setRequestor(svcAccount);
 
                                             if (DEBUG)
                                             {
                                                 DEBUGGER.debug("AccountControlRequest: {}", searchRequest);
                                             }
 
                                             try
                                             {
                                                 searchResponse = acctControl.loadUserAccount(searchRequest);
 
                                                 if (DEBUG)
                                                 {
                                                     DEBUGGER.debug("AccountControlResponse: {}", searchResponse);
                                                 }
 
                                                 if (searchResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                                                 {
                                                     dmgrServer.setAssignedEngineer(searchResponse.getUserAccount()); // ASSIGNED_ENGINEER
                                                 }
                                             }
                                             catch (AccountControlException acx)
                                             {
                                                 ERROR_RECORDER.error(acx.getMessage(), acx);
                                             }
 
                                             if (DEBUG)
                                             {
                                                 DEBUGGER.debug("Server: {}", dmgrServer);
                                             }
 
                                             server.setOwningDmgr(dmgrServer); // OWNING_DMGR
                                         }
 
                                         break;
                                     case DMGRSERVER:
                                         server.setDmgrPort((Integer) data[28]); // DMGR_PORT
                                         server.setMgrUrl((String) data[30]); // MGR_ENTRY
 
                                         break;
                                     case VIRTUALHOST:
                                         server.setDmgrPort((Integer) data[28]); // DMGR_PORT
                                         server.setMgrUrl((String) data[30]); // MGR_ENTRY
 
                                         break;
                                     default:
                                         break;
                                 }
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Server: {}", server);
                                 }
 
                                 serverList.add(server);
                             }
                             else
                             {
                                 ERROR_RECORDER.error("Server " + data[0] + " has no associated datacenter");
                             }
                         }
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("serverList: {}", serverList);
                     }
 
                     response.setRequestStatus(CoreServicesStatus.SUCCESS);
                     response.setResponse("Successfully loaded installed server information.");
                     response.setServerList(serverList);
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ServerManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.LISTSERVERS);
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
     public ServerManagementResponse getServerData(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#getServerData(final ServerManagementRequest request) throws ServerManagementException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: {}", request);
         }
 
         UserAccount svcAccount = null;
         UserAccount searchAccount = null;
         AccountControlRequest searchRequest = null;
         AccountControlResponse searchResponse = null;
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server requestServer = request.getTargetServer();
         final UserAccount userAccount = request.getUserAccount();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final IAccountControlProcessor acctControl = new AccountControlProcessorImpl();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", requestServer);
             DEBUGGER.debug("UserAccount: {}", userAccount);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
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
                 if (requestServer != null)
                 {
                     List<Object> serverData = serverDAO.getInstalledServer(requestServer.getServerGuid());
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("serverData: {}", serverData);
                     }
 
                     if ((serverData != null) && (serverData.size() != 0))
                     {
                         List<String> datacenter = datactrDAO.getDatacenter((String) serverData.get(5)); // DATACENTER_GUID
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("List<String>: {}", datacenter);
                         }
 
                         if ((datacenter != null) && (datacenter.size() != 0))
                         {
                             DataCenter dataCenter = new DataCenter();
                             dataCenter.setDatacenterGuid(datacenter.get(0));
                             dataCenter.setDatacenterName(datacenter.get(1));
                             dataCenter.setDatacenterStatus(ServiceStatus.valueOf(datacenter.get(2)));
                             dataCenter.setDatacenterDesc(datacenter.get(3));
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("DataCenter: {}", dataCenter);
                             }
 
                             Server server = new Server();
                             server.setServerGuid((String) serverData.get(0)); // SYSTEM_GUID
                             server.setOsName((String) serverData.get(1)); // SYSTEM_OSTYPE
                             server.setServerStatus(ServerStatus.valueOf((String) serverData.get(2))); // SYSTEM_STATUS
                             server.setServerRegion(ServiceRegion.valueOf((String) serverData.get(3))); // SYSTEM_REGION
                             server.setNetworkPartition(NetworkPartition.valueOf((String) serverData.get(4))); // NETWORK_PARTITION
                             server.setDatacenter(dataCenter); // datacenter as earlier obtained
                             server.setServerType(ServerType.valueOf((String) serverData.get(6))); // SYSTEM_TYPE
                             server.setDomainName((String) serverData.get(7)); // DOMAIN_NAME
                             server.setCpuType((String) serverData.get(8)); // CPU_TYPE
                             server.setCpuCount((Integer) serverData.get(9)); // CPU_COUNT
                             server.setServerRack((String) serverData.get(10)); // SERVER_RACK
                             server.setRackPosition((String) serverData.get(11)); // RACK_POSITION
                             server.setServerModel((String) serverData.get(12)); // SERVER_MODEL
                             server.setSerialNumber((String) serverData.get(13)); // SERIAL_NUMBER
                             server.setInstalledMemory((Integer) serverData.get(14)); // INSTALLED_MEMORY
                             server.setOperIpAddress((String) serverData.get(15)); // OPER_IP
                             server.setOperHostName((String) serverData.get(16)); // OPER_HOSTNAME
                             server.setMgmtIpAddress((String) serverData.get(17)); // MGMT_IP
                             server.setMgmtHostName((String) serverData.get(18)); // MGMT_HOSTNAME
                             server.setBkIpAddress((String) serverData.get(19)); // BKUP_IP
                             server.setBkHostName((String) serverData.get(20)); // BKUP_HOSTNAME
                             server.setNasIpAddress((String) serverData.get(21)); // NAS_IP
                             server.setNasHostName((String) serverData.get(22)); // NAS_HOSTNAME
                             server.setNatAddress((String) serverData.get(23)); // NAT_ADDR
                             server.setServerComments((String) serverData.get(24)); // COMMENTS
 
                             searchAccount = new UserAccount();
                             searchAccount.setGuid((String) serverData.get(25));
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("UserAccount: {}", searchAccount);
                             }
 
                             svcAccount = new UserAccount();
                             svcAccount.setUsername(serviceAccount.get(0));
                             svcAccount.setGuid(serviceAccount.get(1));
                             svcAccount.setRole(Role.valueOf(serviceAccount.get(2)));
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("UserAccount: {}", svcAccount);
                             }
 
                             searchRequest = new AccountControlRequest();
                             searchRequest.setHostInfo(request.getRequestInfo());
                             searchRequest.setUserAccount(searchAccount);
                             searchRequest.setApplicationName(request.getApplicationName());
                             searchRequest.setApplicationId(request.getApplicationId());
                             searchRequest.setSearchType(SearchRequestType.GUID);
                             searchRequest.setRequestor(svcAccount);
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("AccountControlRequest: {}", searchRequest);
                             }
 
                             try
                             {
                                 searchResponse = acctControl.loadUserAccount(searchRequest);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("AccountControlResponse: {}", searchResponse);
                                 }
 
                                 if (searchResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                                 {
                                     server.setAssignedEngineer(searchResponse.getUserAccount()); // ASSIGNED_ENGINEER
                                 }
                             }
                             catch (AccountControlException acx)
                             {
                                 ERROR_RECORDER.error(acx.getMessage(), acx);
                             }
 
                             switch (server.getServerType())
                             {
                                 case APPSERVER:
                                     // set owning dmgr
                                     List<Object> dmgrData = serverDAO.getInstalledServer((String) serverData.get(29));
 
                                     if (DEBUG)
                                     {
                                         DEBUGGER.debug("dmgrData: {}", dmgrData);
                                     }
 
                                     if ((dmgrData != null) && (dmgrData.size() != 0))
                                     {
                                         Server dmgrServer = new Server();
                                         dmgrServer.setServerGuid((String) dmgrData.get(0)); // SYSTEM_GUID
                                         dmgrServer.setOsName((String) dmgrData.get(1)); // SYSTEM_OSTYPE
                                         dmgrServer.setServerStatus(ServerStatus.valueOf((String) dmgrData.get(2))); // SYSTEM_STATUS
                                         dmgrServer.setServerRegion(ServiceRegion.valueOf((String) dmgrData.get(3))); // SYSTEM_REGION
                                         dmgrServer.setNetworkPartition(NetworkPartition.valueOf((String) dmgrData.get(4))); // NETWORK_PARTITION
                                         dmgrServer.setDatacenter(dataCenter); // datacenter as earlier obtained
                                         dmgrServer.setServerType(ServerType.valueOf((String) dmgrData.get(6))); // SYSTEM_TYPE
                                         dmgrServer.setDomainName((String) dmgrData.get(7)); // DOMAIN_NAME
                                         dmgrServer.setCpuType((String) dmgrData.get(8)); // CPU_TYPE
                                         dmgrServer.setCpuCount((Integer) dmgrData.get(9)); // CPU_COUNT
                                         dmgrServer.setServerRack((String) dmgrData.get(10)); // SERVER_RACK
                                         dmgrServer.setRackPosition((String) dmgrData.get(11)); // RACK_POSITION
                                         dmgrServer.setServerModel((String) dmgrData.get(12)); // SERVER_MODEL
                                         dmgrServer.setSerialNumber((String) dmgrData.get(13)); // SERIAL_NUMBER
                                         dmgrServer.setInstalledMemory((Integer) dmgrData.get(14)); // INSTALLED_MEMORY
                                         dmgrServer.setOperIpAddress((String) dmgrData.get(15)); // OPER_IP
                                         dmgrServer.setOperHostName((String) dmgrData.get(16)); // OPER_HOSTNAME
                                         dmgrServer.setMgmtIpAddress((String) dmgrData.get(17)); // MGMT_IP
                                         dmgrServer.setMgmtHostName((String) dmgrData.get(18)); // MGMT_HOSTNAME
                                         dmgrServer.setBkIpAddress((String) dmgrData.get(19)); // BKUP_IP
                                         dmgrServer.setBkHostName((String) dmgrData.get(20)); // BKUP_HOSTNAME
                                         dmgrServer.setNasIpAddress((String) dmgrData.get(21)); // NAS_IP
                                         dmgrServer.setNasHostName((String) dmgrData.get(22)); // NAS_HOSTNAME
                                         dmgrServer.setNatAddress((String) dmgrData.get(23)); // NAT_ADDR
                                         dmgrServer.setServerComments((String) dmgrData.get(24)); // COMMENTS
                                         dmgrServer.setDmgrPort((Integer) serverData.get(28)); // DMGR_PORT
                                         dmgrServer.setMgrUrl((String) serverData.get(30)); // MGR_ENTRY
 
                                         searchAccount = new UserAccount();
                                         searchAccount.setGuid((String) dmgrData.get(25));
 
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("UserAccount: {}", searchAccount);
                                         }
 
                                         svcAccount = new UserAccount();
                                         svcAccount.setUsername(serviceAccount.get(0));
                                         svcAccount.setGuid(serviceAccount.get(1));
                                         svcAccount.setRole(Role.valueOf(serviceAccount.get(2)));
 
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("UserAccount: {}", svcAccount);
                                         }
 
                                         searchRequest = new AccountControlRequest();
                                         searchRequest.setHostInfo(request.getRequestInfo());
                                         searchRequest.setUserAccount(searchAccount);
                                         searchRequest.setApplicationName(request.getApplicationName());
                                         searchRequest.setApplicationId(request.getApplicationId());
                                         searchRequest.setSearchType(SearchRequestType.GUID);
                                         searchRequest.setRequestor(svcAccount);
 
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("AccountControlRequest: {}", searchRequest);
                                         }
 
                                         try
                                         {
                                             searchResponse = acctControl.loadUserAccount(searchRequest);
 
                                             if (DEBUG)
                                             {
                                                 DEBUGGER.debug("AccountControlResponse: {}", searchResponse);
                                             }
 
                                             if (searchResponse.getRequestStatus() == SecurityRequestStatus.SUCCESS)
                                             {
                                                 dmgrServer.setAssignedEngineer(searchResponse.getUserAccount()); // ASSIGNED_ENGINEER
                                             }
                                         }
                                         catch (AccountControlException acx)
                                         {
                                             ERROR_RECORDER.error(acx.getMessage(), acx);
                                         }
 
                                         if (DEBUG)
                                         {
                                             DEBUGGER.debug("Server: {}", dmgrServer);
                                         }
 
                                         server.setOwningDmgr(dmgrServer); // OWNING_DMGR
                                     }
 
                                     break;
                                 case DMGRSERVER:
                                     server.setDmgrPort((Integer) serverData.get(28)); // DMGR_PORT
                                     server.setMgrUrl((String) serverData.get(30)); // MGR_ENTRY
 
                                     break;
                                 case VIRTUALHOST:
                                     server.setDmgrPort((Integer) serverData.get(28)); // DMGR_PORT
                                     server.setMgrUrl((String) serverData.get(30)); // MGR_ENTRY
 
                                     break;
                                 default:
                                     break;
                             }
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("Server: {}", server);
                             }
 
                             response.setRequestStatus(CoreServicesStatus.SUCCESS);
                             response.setResponse("Successfully loaded installed server information.");
                             response.setServer(server);
                         }
                         else
                         {
                             throw new ServerManagementException("The server provided has no associated datacenter.");
                         }
                     }
                     else
                     {
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                         response.setResponse("No server was located with the provided information");
                     }
                 }
                 else
                 {
                     throw new ServerManagementException("No server search data was provided. Cannot continue");
                 }
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
                 response.setResponse("The requested user was not authorized to perform the operation");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new ServerManagementException(sqx.getMessage(), sqx);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
             
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.GETSERVER);
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
     public ServerManagementResponse runNetstatCheck(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#runNetstatCheck(final ServerManagementRequest request)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: ", request);
         }
 
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server sourceServer = request.getSourceServer();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final UserAccount userAccount = request.getUserAccount();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", sourceServer);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", userAccount);
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
                 SystemManagerRequest systemReq = new SystemManagerRequest();
                 systemReq.setMgmtType(SystemManagementType.SYSTEMCHECK);
                 systemReq.setRequestType(SystemCheckType.NETSTAT);
                 systemReq.setPortNumber(request.getPortNumber());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SystemManagerRequest: {}", request);
                 }
 
                 AgentRequest agentRequest = new AgentRequest();
                 agentRequest.setHostname(sourceServer.getOperHostName());
                 agentRequest.setAppName(appConfig.getAppName());
                 agentRequest.setRequestPayload(systemReq);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AgentRequest: {}", agentRequest);
                 }
 
                 // always make the tcp conn to the oper hostname - thats where the agent should be listening
                 String correlator = MQUtils.sendMqMessage(agentRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("correlator: {}", correlator);
                 }
 
                 if (StringUtils.isNotEmpty(correlator))
                 {
                     AgentResponse agentResponse = (AgentResponse) MQUtils.getMqMessage(correlator);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("AgentResponse: {}", agentResponse);
                     }
 
                     if (agentResponse.getRequestStatus() == AgentStatus.SUCCESS)
                     {
                         SystemManagerResponse systemRes = (SystemManagerResponse) agentResponse.getResponsePayload();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SystemManagerResponse: {}", systemRes);
                         }
 
                         response.setRequestStatus(CoreServicesStatus.valueOf(systemRes.getRequestStatus().name()));
                         response.setResponse(systemRes.getResponse());
                         response.setResponseObject(systemRes.getResponseData());
                     }
                     else
                     {
                         response.setResponse(agentResponse.getResponse());
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                     }
                 }
                 else
                 {
                     response.setResponse("Failed to send message to configured request queue for action");
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                 }
             }
             else
             {
                 response.setResponse("Requesting user was not authorized to perform the operation.");
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
             }
         }
         catch (UtilityException ux)
         {
             ERROR_RECORDER.error(ux.getMessage(), ux);
 
             throw new ServerManagementException(ux.getMessage(), ux);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.NETSTAT);
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
     public ServerManagementResponse runTelnetCheck(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#runTelnetCheck(final ServerManagementRequest request)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: ", request);
         }
 
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server sourceServer = request.getSourceServer();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final UserAccount userAccount = request.getUserAccount();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", sourceServer);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", userAccount);
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
                 SystemManagerRequest systemReq = new SystemManagerRequest();
                 systemReq.setMgmtType(SystemManagementType.SYSTEMCHECK);
                 systemReq.setRequestType(SystemCheckType.TELNET);
                 systemReq.setPortNumber(request.getPortNumber());
                 systemReq.setTargetServer(request.getTargetServer().getOperHostName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SystemManagerRequest: {}", request);
                 }
 
                 AgentRequest agentRequest = new AgentRequest();
                 agentRequest.setHostname(sourceServer.getOperHostName());
                 agentRequest.setAppName(appConfig.getAppName());
                 agentRequest.setRequestPayload(systemReq);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AgentRequest: {}", agentRequest);
                 }
 
                 // always make the tcp conn to the oper hostname - thats where the agent should be listening
                 String correlator = MQUtils.sendMqMessage(agentRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("correlator: {}", correlator);
                 }
 
                 if (StringUtils.isNotEmpty(correlator))
                 {
                     AgentResponse agentResponse = (AgentResponse) MQUtils.getMqMessage(correlator);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("AgentResponse: {}", agentResponse);
                     }
 
                     if (agentResponse.getRequestStatus() == AgentStatus.SUCCESS)
                     {
                         SystemManagerResponse systemRes = (SystemManagerResponse) agentResponse.getResponsePayload();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SystemManagerResponse: {}", systemRes);
                         }
 
                         response.setRequestStatus(CoreServicesStatus.valueOf(systemRes.getRequestStatus().name()));
                         response.setResponse(systemRes.getResponse());
                         response.setResponseObject(systemRes.getResponseData());
                     }
                     else
                     {
                         response.setResponse(agentResponse.getResponse());
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                     }
                 }
                 else
                 {
                     response.setResponse("Failed to send message to configured request queue for action");
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                 }
             }
             else
             {
                 response.setResponse("Requesting user was not authorized to perform the operation.");
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
             }
         }
         catch (UtilityException ux)
         {
             ERROR_RECORDER.error(ux.getMessage(), ux);
 
             throw new ServerManagementException(ux.getMessage(), ux);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.TELNET);
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
     public ServerManagementResponse runRemoteDateCheck(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#runRemoteDateCheck(final ServerManagementRequest request)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: ", request);
         }
 
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server sourceServer = request.getSourceServer();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final UserAccount userAccount = request.getUserAccount();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", sourceServer);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", userAccount);
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
                 SystemManagerRequest systemReq = new SystemManagerRequest();
                 systemReq.setMgmtType(SystemManagementType.SYSTEMCHECK);
                 systemReq.setRequestType(SystemCheckType.REMOTEDATE);
                 systemReq.setPortNumber(request.getPortNumber());
                 systemReq.setTargetServer(request.getTargetServer().getOperHostName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SystemManagerRequest: {}", request);
                 }
 
                 AgentRequest agentRequest = new AgentRequest();
                 agentRequest.setAppName(appConfig.getAppName());
                 agentRequest.setRequestPayload(systemReq);
                 agentRequest.setHostname(sourceServer.getOperHostName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AgentRequest: {}", agentRequest);
                 }
 
                 // always make the tcp conn to the oper hostname - thats where the agent should be listening
                 String correlator = MQUtils.sendMqMessage(agentRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("correlator: {}", correlator);
                 }
 
                 if (StringUtils.isNotEmpty(correlator))
                 {
                     AgentResponse agentResponse = (AgentResponse) MQUtils.getMqMessage(correlator);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("AgentResponse: {}", agentResponse);
                     }
 
                     if (agentResponse.getRequestStatus() == AgentStatus.SUCCESS)
                     {
                         SystemManagerResponse systemRes = (SystemManagerResponse) agentResponse.getResponsePayload();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SystemManagerResponse: {}", systemRes);
                         }
 
                         response.setRequestStatus(CoreServicesStatus.valueOf(systemRes.getRequestStatus().name()));
                         response.setResponse(systemRes.getResponse());
                         response.setResponseObject(systemRes.getResponseData());
                     }
                     else
                     {
                         response.setResponse(agentResponse.getResponse());
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                     }
                 }
                 else
                 {
                     response.setResponse("Failed to send message to configured request queue for action");
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                 }
             }
             else
             {
                 response.setResponse("Requesting user was not authorized to perform the operation.");
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
             }
         }
         catch (UtilityException ux)
         {
             ERROR_RECORDER.error(ux.getMessage(), ux);
 
             throw new ServerManagementException(ux.getMessage(), ux);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.REMOTEDATE);
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
     public ServerManagementResponse runProcessListCheck(final ServerManagementRequest request) throws ServerManagementException
     {
         final String methodName = IServerManagementProcessor.CNAME + "#runProcessListCheck(final ServerManagementRequest request)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServerManagementRequest: ", request);
         }
 
         ServerManagementResponse response = new ServerManagementResponse();
 
         final Server sourceServer = request.getSourceServer();
         final RequestHostInfo reqInfo = request.getRequestInfo();
         final UserAccount userAccount = request.getUserAccount();
 
         if (DEBUG)
         {
             DEBUGGER.debug("Server: {}", sourceServer);
             DEBUGGER.debug("RequestHostInfo: {}", reqInfo);
             DEBUGGER.debug("UserAccount: {}", userAccount);
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
                 SystemManagerRequest systemReq = new SystemManagerRequest();
                 systemReq.setMgmtType(SystemManagementType.SYSTEMCHECK);
                 systemReq.setRequestType(SystemCheckType.PROCESSLIST);
                 systemReq.setTargetServer(request.getTargetServer().getOperHostName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("SystemManagerRequest: {}", request);
                 }
 
                 AgentRequest agentRequest = new AgentRequest();
                 agentRequest.setAppName(appConfig.getAppName());
                 agentRequest.setRequestPayload(systemReq);
                 agentRequest.setHostname(sourceServer.getOperHostName());
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("AgentRequest: {}", agentRequest);
                 }
 
                 // always make the tcp conn to the oper hostname - thats where the agent should be listening
                 String correlator = MQUtils.sendMqMessage(agentRequest);
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("correlator: {}", correlator);
                 }
 
                 if (StringUtils.isNotEmpty(correlator))
                 {
                     AgentResponse agentResponse = (AgentResponse) MQUtils.getMqMessage(correlator);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("AgentResponse: {}", agentResponse);
                     }
 
                     if (agentResponse.getRequestStatus() == AgentStatus.SUCCESS)
                     {
                         SystemManagerResponse systemRes = (SystemManagerResponse) agentResponse.getResponsePayload();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SystemManagerResponse: {}", systemRes);
                         }
 
                         response.setRequestStatus(CoreServicesStatus.valueOf(systemRes.getRequestStatus().name()));
                         response.setResponse(systemRes.getResponse());
                         response.setResponseObject(systemRes.getResponseData());
                     }
                     else
                     {
                         response.setResponse(agentResponse.getResponse());
                         response.setRequestStatus(CoreServicesStatus.FAILURE);
                     }
                 }
                 else
                 {
                     response.setResponse("Failed to send message to configured request queue for action");
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
                 }
             }
             else
             {
                 response.setResponse("Requesting user was not authorized to perform the operation.");
                 response.setRequestStatus(CoreServicesStatus.UNAUTHORIZED);
             }
         }
         catch (UtilityException ux)
         {
             ERROR_RECORDER.error(ux.getMessage(), ux);
 
             throw new ServerManagementException(ux.getMessage(), ux);
         }
         catch (UserControlServiceException ucsx)
         {
             ERROR_RECORDER.error(ucsx.getMessage(), ucsx);
 
             throw new ServerManagementException(ucsx.getMessage(), ucsx);
         }
         finally
         {
             // audit
             try
             {
                 AuditEntry auditEntry = new AuditEntry();
                 auditEntry.setHostInfo(reqInfo);
                 auditEntry.setAuditType(AuditType.PROCESSLIST);
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
