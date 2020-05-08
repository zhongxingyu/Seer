 /*
  * Copyright (c) 2009 - 2014 CaspersBox Web Services
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
 package com.cws.esolutions.security.services.interfaces;
 /*
  * Project: eSolutionsSecurity
  * Package: com.cws.esolutions.security.access.control.interfaces
  * File: IaccessControlService.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cws.esolutions.security.dto.UserAccount;
 import com.cws.esolutions.security.SecurityServiceConstants;
 import com.cws.esolutions.security.processors.impl.AuditProcessorImpl;
 import com.cws.esolutions.security.processors.interfaces.IAuditProcessor;
 import com.cws.esolutions.security.dao.reference.impl.SecurityReferenceDAOImpl;
 import com.cws.esolutions.security.dao.reference.interfaces.ISecurityReferenceDAO;
 import com.cws.esolutions.security.services.exception.AccessControlServiceException;
 /**
  * Interface for the Application Data DAO layer. Allows access
  * into the asset management database to obtain, modify and remove
  * application information.
  *
  * @author khuntly
  * @version 1.0
  */
 public interface IAccessControlService
 {
     static final String CNAME = IAccessControlService.class.getName();
     static final ISecurityReferenceDAO ref = new SecurityReferenceDAOImpl();
 
     static final Logger DEBUGGER = LoggerFactory.getLogger(SecurityServiceConstants.DEBUGGER);
     static final boolean DEBUG = DEBUGGER.isDebugEnabled();
 
     /**
      * Determines if the requested user has the proper level of authority to
      * access the requested resource. This method needs a little work - its
      * long-term goal is to allow both a servlet-based method as well as a
      * portlet service. It should also query an applicable user datastore,
      * in the event the session data may have been tampered.
      *
      * @param userAccount
      * @param serviceGuid
      * @return boolean
      * @throws AccessControlServiceException
      */
     boolean isUserAuthorized(final UserAccount userAccount, final String serviceGuid) throws AccessControlServiceException;
 }
