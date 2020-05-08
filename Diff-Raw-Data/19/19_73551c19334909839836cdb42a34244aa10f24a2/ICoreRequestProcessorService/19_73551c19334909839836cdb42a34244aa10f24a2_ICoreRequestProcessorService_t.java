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
 package com.cws.esolutions.core.webservice.interfaces;
 
 import org.slf4j.Logger;
 import javax.jws.WebService;
 import org.slf4j.LoggerFactory;
 
 import com.cws.esolutions.core.Constants;
 import com.cws.esolutions.core.CoreServiceBean;
 import com.cws.esolutions.core.processors.impl.SearchProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.ISearchProcessor;
 import com.cws.esolutions.core.processors.impl.KnowledgeBaseProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.IKnowledgeBaseProcessor;
 import com.cws.esolutions.core.processors.impl.ServerManagementProcessorImpl;
 import com.cws.esolutions.core.processors.impl.DNSServiceRequestProcessorImpl;
 import com.cws.esolutions.security.processors.impl.AuthenticationProcessorImpl;
 import com.cws.esolutions.core.processors.interfaces.IServerManagementProcessor;
 import com.cws.esolutions.core.processors.interfaces.IDNSServiceRequestProcessor;
 import com.cws.esolutions.security.processors.interfaces.IAuthenticationProcessor;
 /**
  * eSolutionsCore
  * com.cws.esolutions.core.webservice.interfaces
  * ICoreRequestProcessorService.java
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
  * kh05451 @ Jan 2, 2013 1:49:02 PM
  *     Created.
  */
@WebService(name="ICoreRequestProcessorService",
    targetNamespace = "http://esolutions.caspersbox.corp/s?q=esolutions",
    wsdlLocation = "WEB-INF/wsdl/CoreRequestProcessorService.wsdl")
 public interface ICoreRequestProcessorService extends IDNSServiceRequestProcessor, IKnowledgeBaseProcessor, ISearchProcessor, IAuthenticationProcessor
 {
     static final String CNAME = ICoreRequestProcessorService.class.getName();
 
     static final ISearchProcessor searchSvc = new SearchProcessorImpl();
     static final CoreServiceBean appBean = CoreServiceBean.getInstance();
     static final IDNSServiceRequestProcessor dnsSvc = new DNSServiceRequestProcessorImpl();
     static final IKnowledgeBaseProcessor kbase = new KnowledgeBaseProcessorImpl();
     static final IServerManagementProcessor sysMgr = new ServerManagementProcessorImpl();
     static final IAuthenticationProcessor authProcessor = new AuthenticationProcessorImpl();
 
     static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + CNAME);
 }
