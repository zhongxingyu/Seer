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
 package com.cws.esolutions.agent.processors.interfaces;
 /*
  * Project: eSolutionsAgent
  * Package: com.cws.esolutions.agent.processors.interfaces
  * File: IFileManagerProcessor.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cws.esolutions.agent.AgentBean;
 import com.cws.esolutions.agent.AgentConstants;
 import com.cws.esolutions.agent.processors.dto.FileManagerRequest;
 import com.cws.esolutions.agent.processors.dto.FileManagerResponse;
 import com.cws.esolutions.agent.processors.exception.FileManagerException;
 /**
  * Interface for the Application Data DAO layer. Allows access
  * into the asset management database to obtain, modify and remove
  * application information.
  *
  * @author khuntly
  * @version 1.0
  */
 public interface IFileManagerProcessor
 {
     static final AgentBean appBean = AgentBean.getInstance();
 
     static final byte buffer[] = new byte[1024];
     static final String CNAME = IFileManagerProcessor.class.getName();
 
     static final Logger DEBUGGER = LoggerFactory.getLogger(AgentConstants.DEBUGGER);
     static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     static final Logger ERROR_RECORDER = LoggerFactory.getLogger(AgentConstants.ERROR_LOGGER + CNAME);
 
     FileManagerResponse retrieveFile(final FileManagerRequest request) throws FileManagerException;
 
     FileManagerResponse deployFile(final FileManagerRequest request) throws FileManagerException;
 
     FileManagerResponse deleteFile(final FileManagerRequest request) throws FileManagerException;
 }
