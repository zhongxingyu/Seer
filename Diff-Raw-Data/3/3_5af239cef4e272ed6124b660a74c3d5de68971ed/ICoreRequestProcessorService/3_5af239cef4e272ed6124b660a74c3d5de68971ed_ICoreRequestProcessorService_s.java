 /*
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
 package com.cws.esolutions.core.ws.interfaces;
 /*
  * Project: eSolutionsCore
  * Package: com.cws.esolutions.core.ws.interfaces
  * File: ICoreRequestProcessorService.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 import org.slf4j.Logger;
 import javax.jws.WebService;
 import org.slf4j.LoggerFactory;
 
import com.cws.esolutions.core.CoreServiceConstants;
 import com.cws.esolutions.core.CoreServiceBean;
 import com.cws.esolutions.core.processors.impl.ServerManagementProcessorImpl;
 import com.cws.esolutions.core.processors.impl.DNSServiceRequestProcessorImpl;
 import com.cws.esolutions.security.processors.impl.AuthenticationProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.IServerManagementProcessor;
 import com.cws.esolutions.core.processors.interfaces.IDNSServiceRequestProcessor;
 import com.cws.esolutions.security.processors.interfaces.IAuthenticationProcessor;
 /**
  * Interface for the Application Data DAO layer. Allows access
  * into the asset management database to obtain, modify and remove
  * application information.
  *
  * @author khuntly
  * @version 1.0
  */
 @WebService(targetNamespace = "http://esolutions.caspersbox.com/s?q=esolutions",
     portName = "CoreRequestProcessorServicePort",
     serviceName = "CoreRequestProcessorService")
 public interface ICoreRequestProcessorService extends IDNSServiceRequestProcessor, IAuthenticationProcessor
 {
     static final String CNAME = ICoreRequestProcessorService.class.getName();
 
     static final CoreServiceBean appBean = CoreServiceBean.getInstance();
     static final IDNSServiceRequestProcessor dnsSvc = new DNSServiceRequestProcessorImpl();
     static final IServerManagementProcessor sysMgr = new ServerManagementProcessorImpl();
     static final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
     static final Logger DEBUGGER = LoggerFactory.getLogger(CoreServiceConstants.DEBUGGER);
     static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     static final Logger ERROR_RECORDER = LoggerFactory.getLogger(CoreServiceConstants.ERROR_LOGGER + CNAME);
 }
