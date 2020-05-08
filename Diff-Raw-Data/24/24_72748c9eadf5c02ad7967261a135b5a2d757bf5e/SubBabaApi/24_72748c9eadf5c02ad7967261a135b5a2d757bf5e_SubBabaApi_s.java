 /*
  *      Copyright (c) 2004-2013 Stuart Boston
  *
  *      This file is part of the SubBaba API.
  *
  *      The SubBaba API is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      any later version.
  *
  *      The SubBaba API is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU General Public License for more details.
  *
  *      You should have received a copy of the GNU General Public License
  *      along with the SubBaba API.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.omertron.subbabaapi;
 
 import com.omertron.subbabaapi.enumerations.SearchType;
 import com.omertron.subbabaapi.model.SubBabaContent;
 import com.omertron.subbabaapi.model.SubBabaMovie;
 import com.omertron.subbabaapi.tools.ApiBuilder;
 import java.util.List;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yamj.api.common.http.CommonHttpClient;
 import org.yamj.api.common.http.DefaultPoolingHttpClient;
 
 public class SubBabaApi {
 
     private static final Logger LOG = LoggerFactory.getLogger(SubBabaApi.class);
     private CommonHttpClient httpClient;
 
     public SubBabaApi(String apiKey) {
         // Use a default pooling client if one is not provided
         this(apiKey, new DefaultPoolingHttpClient());
     }
 
     public SubBabaApi(String apiKey, CommonHttpClient httpClient) {
         if (StringUtils.isBlank(apiKey)) {
            return;
         }
 
         ApiBuilder.setApiKey(apiKey);
         ApiBuilder.setHttpClient(httpClient);
     }
 
     /**
      * Use the English name of a movie to search for posters
      *
      * @param movieName
      * @param searchType
      * @return
      */
     public List<SubBabaMovie> searchByEnglishName(String movieName, SearchType searchType) {
         return ApiBuilder.searchByEnglishName(movieName, searchType);
     }
 
     /**
      * Use the IMDb ID to get the movie information
      *
      * @param imdbId
      * @param searchType
      * @return
      */
     public SubBabaMovie searchByImdbId(String imdbId, SearchType searchType) {
         return ApiBuilder.searchByImdbId(imdbId, searchType);
     }
 
     /**
      * Get the specific content associated with an ID
      *
      * @param contentId
      * @return
      */
     public SubBabaContent fetchInfoByContentId(String contentId) {
         SubBabaContent sbc = ApiBuilder.fetchInfoByContentId(contentId);
 
         if (sbc != null) {
             LOG.info(sbc.toString());
         }
         return sbc;
     }
 
     /**
      * Set the web browser proxy information
      *
      * @param host
      * @param port
      * @param username
      * @param password
      */
     public void setProxy(String host, int port, String username, String password) {
        httpClient.setProxy(host, port, username, password);
     }
 
     /**
      * Set the web browser timeout settings
      *
      * @param webTimeoutConnect
      * @param webTimeoutRead
      */
     public void setTimeout(int webTimeoutConnect, int webTimeoutRead) {
        httpClient.setTimeouts(webTimeoutConnect, webTimeoutRead);
     }
 }
