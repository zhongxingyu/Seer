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
 package com.cws.esolutions.core.processors.impl;
 /**
  * @see com.cws.esolutions.core.processors.interfaces.ISearchProcessor
  */
 import java.util.List;
 import java.util.ArrayList;
 import java.sql.SQLException;
 
 import com.cws.esolutions.core.processors.dto.SearchResult;
 import com.cws.esolutions.core.processors.dto.SearchRequest;
 import com.cws.esolutions.core.processors.dto.SearchResponse;
 import com.cws.esolutions.core.processors.enums.CoreServicesStatus;
 import com.cws.esolutions.core.dao.processors.impl.SiteSearchDAOImpl;
 import com.cws.esolutions.core.dao.processors.impl.ServerDataDAOImpl;
 import com.cws.esolutions.core.processors.interfaces.ISearchProcessor;
 import com.cws.esolutions.core.dao.processors.impl.PlatformDataDAOImpl;
 import com.cws.esolutions.core.dao.processors.interfaces.IMessagingDAO;
 import com.cws.esolutions.core.dao.processors.interfaces.IServerDataDAO;
 import com.cws.esolutions.core.dao.processors.interfaces.ISiteSearchDAO;
 import com.cws.esolutions.core.dao.processors.impl.DatacenterDataDAOImpl;
 import com.cws.esolutions.core.dao.processors.interfaces.IPlatformDataDAO;
 import com.cws.esolutions.core.dao.processors.impl.ApplicationDataDAOImpl;
 import com.cws.esolutions.core.processors.exception.SearchRequestException;
 import com.cws.esolutions.core.dao.processors.impl.ServiceMessagingDAOImpl;
 import com.cws.esolutions.core.dao.processors.interfaces.IDatacenterDataDAO;
 import com.cws.esolutions.core.dao.processors.interfaces.IApplicationDataDAO;
 /*
  * Project: eSolutionsCore
  * Package: com.cws.esolutions.core.processors.impl
  * File: SearchProcessorImpl.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 public class SearchProcessorImpl implements ISearchProcessor
 {
     /**
      * @see com.cws.esolutions.core.processors.interfaces.ISearchProcessor#doMessageSearch(com.cws.esolutions.core.processors.dto.SearchRequest)
      */
     @Override
     public SearchResponse doMessageSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ISearchProcessor.CNAME + "#doMessageSearch(final SearchRequest request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
         }
 
         SearchResponse response = new SearchResponse();
 
         try
         {
             IMessagingDAO dao = new ServiceMessagingDAOImpl();
             List<Object[]> messageList = dao.getMessagesByAttribute(request.getSearchTerms());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("messageList: {}", messageList);
             }
 
             if ((messageList != null) && (messageList.size() != 0))
             {
                 List<SearchResult> responseList = new ArrayList<>();
 
                 for (Object[] data : messageList)
                 {
                     if (DEBUG)
                     {
                         if (data != null)
                         {
                             for (Object obj : data)
                             {
                                 DEBUGGER.debug("data: {}", obj);
                             }
                         }
                     }
 
                     if ((data != null) && (data.length >= 2))
                     {
                         SearchResult searchResult = new SearchResult();
                         searchResult.setPath((String) data[0]);
                         searchResult.setTitle((String) data[1]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResult: {}", searchResult);
                         }
 
                         responseList.add(searchResult);
                     }
                     else
                     {
                         throw new SearchRequestException("No results were located for the provided data");
                     }
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("responseList: {}", responseList);
                 }
 
                 response.setResults(responseList);
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("SearchResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new SearchRequestException(sqx.getMessage(), sqx);
         }
 
         return response;
     }
 
     /**
      * @see com.cws.esolutions.core.processors.interfaces.ISearchProcessor#doServerSearch(com.cws.esolutions.core.processors.dto.SearchRequest)
      */
     @Override
     public SearchResponse doServerSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ISearchProcessor.CNAME + "#doServerSearch(final SearchRequest request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
         }
 
         SearchResponse response = new SearchResponse();
 
         try
         {
             IServerDataDAO dao = new ServerDataDAOImpl();
             List<String[]> serverList = dao.getServersByAttribute(request.getSearchTerms(), request.getStartRow());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("serverList: {}", serverList);
             }
 
             if ((serverList != null) && (serverList.size() != 0))
             {
                 List<SearchResult> responseList = new ArrayList<>();
 
                 for (Object[] data : serverList)
                 {
                     if (DEBUG)
                     {
                         if (data != null)
                         {
                             for (Object obj : data)
                             {
                                 DEBUGGER.debug("Value: {}", obj);
                             }
                         }
                     }
 
                     if ((data != null) && (data.length >= 16))
                     {
                         SearchResult searchResult = new SearchResult();
                         searchResult.setPath((String) data[0]);
                        searchResult.setTitle((String) data[3]); // proper ordinal for oper hostname
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResult: {}", searchResult);
                         }
 
                         responseList.add(searchResult);
                     }
                     else
                     {
                         throw new SearchRequestException("No results were located for the provided data");
                     }
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("responseList: {}", responseList);
                 }
 
                 response.setResults(responseList);
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("SearchResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new SearchRequestException(sqx.getMessage(), sqx);
         }
 
         return response;
     }
 
     /**
      * @see com.cws.esolutions.core.processors.interfaces.ISearchProcessor#doApplicationSearch(com.cws.esolutions.core.processors.dto.SearchRequest)
      */
     @Override
     public SearchResponse doApplicationSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ISearchProcessor.CNAME + "#doApplicationSearch(final SearchRequest request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
         }
 
         SearchResponse response = new SearchResponse();
 
         try
         {
             IApplicationDataDAO dao = new ApplicationDataDAOImpl();
             List<String[]> applicationList = dao.getApplicationsByAttribute(request.getSearchTerms(), request.getStartRow());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("applicationList: {}", applicationList);
             }
 
             if ((applicationList != null) && (applicationList.size() != 0))
             {
                 List<SearchResult> responseList = new ArrayList<>();
 
                 for (String[] data : applicationList)
                 {
                     if (DEBUG)
                     {
                         if (data != null)
                         {
                             for (String str : data)
                             {
                                 DEBUGGER.debug("data: {}", str);
                             }
                         }
                     }
 
                     if ((data != null) && (data.length >= 2))
                     {
                         SearchResult searchResult = new SearchResult();
                         searchResult.setPath(data[0]);
                         searchResult.setTitle(data[1]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResult: {}", searchResult);
                         }
 
                         responseList.add(searchResult);
                     }
                     else
                     {
                         throw new SearchRequestException("No results were located for the provided data");
                     }
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("responseList: {}", responseList);
                 }
 
                 response.setResults(responseList);
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("SearchResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new SearchRequestException(sqx.getMessage(), sqx);
         }
 
         return response;
     }
 
     /**
      * @see com.cws.esolutions.core.processors.interfaces.ISearchProcessor#doServiceSearch(com.cws.esolutions.core.processors.dto.SearchRequest)
      */
     @Override
     public SearchResponse doServiceSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ISearchProcessor.CNAME + "#doServiceSearch(final SearchRequest request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
         }
 
         List<String[]> resultsList = null;
         SearchResponse response = new SearchResponse();
 
         try
         {
             switch (request.getSearchType())
             {
                 case PLATFORM:
                     IPlatformDataDAO platformDao = new PlatformDataDAOImpl();
                     resultsList = platformDao.listPlatformsByAttribute(request.getSearchTerms(), request.getStartRow());
 
                     break;
                 case DATACENTER:
                     IDatacenterDataDAO datacenterDao = new DatacenterDataDAOImpl();
                     resultsList = datacenterDao.getDataCenterByAttribute(request.getSearchTerms(), request.getStartRow());
 
                     break;
                 default:
                     response.setRequestStatus(CoreServicesStatus.FAILURE);
 
                     return response;
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("resultsList: {}", resultsList);
             }
 
             if ((resultsList != null) && (resultsList.size() != 0))
             {
                 List<SearchResult> responseList = new ArrayList<>();
 
                 for (String[] data : resultsList)
                 {
                     if (DEBUG)
                     {
                         if (data != null)
                         {
                             for (String str : data)
                             {
                                 DEBUGGER.debug("data: {}", str);
                             }
                         }
                     }
 
                     if ((data != null) && (data.length >= 2))
                     {
                         SearchResult searchResult = new SearchResult();
                         searchResult.setPath(data[0]);
                         searchResult.setTitle(data[1]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResult: {}", searchResult);
                         }
 
                         responseList.add(searchResult);
                     }
                 }
 
                 response.setResults(responseList);
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("SearchResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new SearchRequestException(sqx.getMessage(), sqx);
         }
 
         return response;
     }
 
     /**
      * @see com.cws.esolutions.core.processors.interfaces.ISearchProcessor#doSiteSearch(com.cws.esolutions.core.processors.dto.SearchRequest)
      */
     @Override
     public SearchResponse doSiteSearch(final SearchRequest request) throws SearchRequestException
     {
         final String methodName = ISearchProcessor.CNAME + "#doSiteSearch(final SearchRequest request) throws SearchRequestException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("SearchRequest: {}", request);
         }
 
         SearchResponse response = new SearchResponse();
 
         try
         {
             ISiteSearchDAO dao = new SiteSearchDAOImpl();
             List<Object[]> responseData = dao.getPagesByAttribute(request.getSearchTerms(), request.getStartRow());
 
             if (DEBUG)
             {
                 DEBUGGER.debug("responseData: {}", responseData);
             }
 
             if ((responseData != null) && (responseData.size() != 0))
             {
                 List<SearchResult> responseList = new ArrayList<>();
 
                 for (Object[] data : responseData)
                 {
                     if (DEBUG)
                     {
                         if (data != null)
                         {
                             for (Object str : data)
                             {
                                 DEBUGGER.debug("data: {}", str);
                             }
                         }
                     }
 
                     if ((data != null) && (data.length >= 2))
                     {
                         SearchResult searchResult = new SearchResult();
                         searchResult.setPath((String) data[1]);
                         searchResult.setTitle((String) data[5]);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SearchResult: {}", searchResult);
                         }
 
                         responseList.add(searchResult);
                     }
                     else
                     {
                         throw new SearchRequestException("No results were located for the provided data");
                     }
                 }
 
                 if (DEBUG)
                 {
                     DEBUGGER.debug("responseList: {}", responseList);
                 }
 
                 response.setResults(responseList);
                 response.setRequestStatus(CoreServicesStatus.SUCCESS);
             }
             else
             {
                 response.setRequestStatus(CoreServicesStatus.FAILURE);
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("SearchResponse: {}", response);
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
 
             throw new SearchRequestException(sqx.getMessage(), sqx);
         }
 
         return response;
     }
 }
