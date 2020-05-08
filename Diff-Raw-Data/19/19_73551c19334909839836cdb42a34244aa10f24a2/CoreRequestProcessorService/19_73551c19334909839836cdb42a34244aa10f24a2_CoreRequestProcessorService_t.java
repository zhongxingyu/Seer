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
 package com.cws.esolutions.core.webservice.impl;
 
 import javax.jws.WebService;
 
 import com.cws.esolutions.core.processors.dto.SearchRequest;
 import com.cws.esolutions.core.processors.dto.SearchResponse;
 import com.cws.esolutions.core.processors.dto.DNSServiceRequest;
 import com.cws.esolutions.core.processors.dto.DNSServiceResponse;
 import com.cws.esolutions.core.processors.dto.KnowledgeBaseRequest;
 import com.cws.esolutions.core.processors.dto.KnowledgeBaseResponse;
 import com.cws.esolutions.core.processors.exception.DNSServiceException;
 import com.cws.esolutions.security.processors.dto.AuthenticationRequest;
 import com.cws.esolutions.security.processors.dto.AuthenticationResponse;
 import com.cws.esolutions.core.processors.exception.KnowledgeBaseException;
 import com.cws.esolutions.core.processors.exception.SearchRequestException;
 import com.cws.esolutions.security.processors.exception.AuthenticationException;
 import com.cws.esolutions.core.webservice.interfaces.ICoreRequestProcessorService;
 /**
  * eSolutionsCore
  * com.cws.esolutions.core.webservice.impl
  * CoreRequestProcessorService.java
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
  * kh05451 @ Nov 21, 2012 3:47:42 PM
  *     Created.
  */
 @WebService(serviceName = "CoreRequestProcessorService",
     endpointInterface = "com.cws.esolutions.core.webservice.interfaces.ICoreRequestProcessorService",
    portName="CoreRequestProcessorServicePort",
    targetNamespace = "http://esolutions.caspersbox.corp/s?q=esolutions")
 public class CoreRequestProcessorService implements ICoreRequestProcessorService
 {
     /*
      * Authentication Processing
      */
     public AuthenticationResponse processAgentLogon(final AuthenticationRequest request) throws AuthenticationException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#processAgentLogon(final AuthenticationRequest request) throws AuthenticationException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuthenticationRequest: ", request);
         }
 
         AuthenticationResponse response = authProcessor.processAgentLogon(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("AuthenticationResponse: {}", response);
         }
 
         return response;
     }
 
     public AuthenticationResponse obtainUserSecurityConfig(final AuthenticationRequest request) throws AuthenticationException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#obtainUserSecurityConfig(final AuthenticationRequest request)";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuthenticationRequest: ", request);
         }
 
         AuthenticationResponse response = authProcessor.obtainUserSecurityConfig(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("AuthenticationResponse: {}", response);
         }
 
         return response;
     }
 
     public AuthenticationResponse verifyUserSecurityConfig(final AuthenticationRequest request) throws AuthenticationException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#verifyUserSecurityConfig(final AuthenticationRequest request)";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuthenticationRequest: ", request);
         }
 
         AuthenticationResponse response = authProcessor.verifyUserSecurityConfig(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("AuthenticationResponse: {}", response);
         }
 
         return response;
     }
 
     public DNSServiceResponse performLookup(final DNSServiceRequest request) throws DNSServiceException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#performLookup(final DNSServiceRequest request) throws DNSServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("DNSServiceRequest: ", request);
         }
 
         DNSServiceResponse response = dnsSvc.performLookup(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("DNSServiceResponse: ", response);
         }
 
         return response;
     }
 
     public DNSServiceResponse getDataFromDatabase(final DNSServiceRequest request) throws DNSServiceException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#getDataFromDatabase(final DNSServiceRequest request)";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("DNSServiceRequest: ", request);
         }
 
         DNSServiceResponse response = dnsSvc.performLookup(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("DNSServiceResponse: ", response);
         }
 
         return response;
     }
 
     public DNSServiceResponse createNewService(final DNSServiceRequest request) throws DNSServiceException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#createNewService(final DNSServiceRequest request) throws DNSServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("DNSServiceRequest: ", request);
         }
 
         DNSServiceResponse response = dnsSvc.createNewService(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("DNSServiceResponse: ", response);
         }
 
         return response;
     }
 
     public DNSServiceResponse pushNewService(final DNSServiceRequest request) throws DNSServiceException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#pushNewService(final DNSServiceRequest request) throws DNSServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("DNSServiceRequest: ", request);
         }
 
         DNSServiceResponse response = dnsSvc.pushNewService(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("DNSServiceResponse: {}", response);
         }
 
         return response;
     }
 
     public DNSServiceResponse performSiteTransfer(final DNSServiceRequest request) throws DNSServiceException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#performSiteTransfer(final DNSServiceRequest request) throws DNSServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("DNSServiceRequest: ", request);
         }
 
         DNSServiceResponse response = dnsSvc.performSiteTransfer(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("DNSServiceResponse: ", response);
         }
 
         return response;
     }
 
     public KnowledgeBaseResponse addNewArticle(final KnowledgeBaseRequest request) throws KnowledgeBaseException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#getArticle(final KnowledgeBaseRequest request) throws KnowledgeBaseException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("KnowledgeBaseRequest: ", request);
         }
 
         KnowledgeBaseResponse response = kbase.addNewArticle(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("KnowledgeBaseResponse: ", response);
         }
 
         return response;
     }
 
     public KnowledgeBaseResponse updateArticle(final KnowledgeBaseRequest request) throws KnowledgeBaseException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#updateArticle(final KnowledgeBaseRequest request) throws KnowledgeBaseException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("KnowledgeBaseRequest: ", request);
         }
 
         KnowledgeBaseResponse response = kbase.updateArticle(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("KnowledgeBaseResponse: ", response);
         }
 
         return response;
     }
 
     public KnowledgeBaseResponse updateArticleStatus(final KnowledgeBaseRequest request) throws KnowledgeBaseException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#updateArticleStatus(final KnowledgeBaseRequest request) throws KnowledgeBaseException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("KnowledgeBaseRequest: ", request);
         }
 
         KnowledgeBaseResponse response = kbase.updateArticleStatus(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("KnowledgeBaseResponse: ", response);
         }
 
         return response;
     }
 
     public KnowledgeBaseResponse getArticle(final KnowledgeBaseRequest request) throws KnowledgeBaseException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#getArticle(final KnowledgeBaseRequest request) throws KnowledgeBaseException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("KnowledgeBaseRequest: ", request);
         }
 
         KnowledgeBaseResponse response = kbase.getArticle(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("KnowledgeBaseResponse: ", response);
         }
 
         return response;
     }
 
     public KnowledgeBaseResponse getPendingArticles(final KnowledgeBaseRequest request) throws KnowledgeBaseException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#getPendingArticles(final KnowledgeBaseRequest request) throws KnowledgeBaseException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         KnowledgeBaseResponse response = kbase.getPendingArticles(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("KnowledgeBaseResponse: ", response);
         }
 
         return response;
     }
 
     public SearchResponse doArticleSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#doArticleSearch(final request request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("request: ", request);
         }
 
         SearchResponse response = searchSvc.doArticleSearch(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("SearchResponse: ", response);
         }
 
         return response;
     }
 
     public SearchResponse doServerSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#doServerSearch(final request request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("request: ", request);
         }
 
         SearchResponse response = searchSvc.doArticleSearch(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("SearchResponse: ", response);
         }
 
         return response;
     }
 
     public SearchResponse doMessageSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#doMessageSearch(final SearchRequest request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: ", request);
         }
 
         SearchResponse response = searchSvc.doArticleSearch(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("SearchResponse: ", response);
         }
 
         return response;
     }
 
     public SearchResponse doApplicationSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#doApplicationSearch(final SearchRequest request) throws SearchRequestException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: ", request);
         }
 
         SearchResponse response = searchSvc.doApplicationSearch(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("SearchResponse: ", response);
         }
 
         return response;
     }
 
     public SearchResponse doProjectSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ICoreRequestProcessorService.CNAME + "#doProjectSearch(final SearchRequest request) throws SearchRequestException";
         
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: ", request);
         }
 
         SearchResponse response = searchSvc.doProjectSearch(request);
 
         if (DEBUG)
         {
             DEBUGGER.debug("SearchResponse: ", response);
         }
 
         return response;
     }
}
