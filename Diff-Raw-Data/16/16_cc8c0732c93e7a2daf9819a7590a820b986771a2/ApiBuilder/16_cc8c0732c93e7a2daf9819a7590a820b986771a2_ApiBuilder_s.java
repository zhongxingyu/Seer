 /*
  *      Copyright (c) 2004-2012 Stuart Boston
  *
  *      This software is licensed under a Creative Commons License
  *      See the LICENCE.txt file included in this package
  *
  *      For any reuse or distribution, you must make clear to others the
  *      license terms of this work.
  */
 package com.omertron.subbabaapi.tools;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.omertron.subbabaapi.model.SearchFunction;
 import com.omertron.subbabaapi.model.SearchType;
 import com.omertron.subbabaapi.model.SubBabaContent;
 import com.omertron.subbabaapi.model.SubBabaMovie;
 import com.omertron.subbabaapi.model.SubBabaResult;
 import com.omertron.subbabaapi.wrapper.SubBabaWrapper;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 
 public final class ApiBuilder {
 
     private static final Logger logger = Logger.getLogger(ApiBuilder.class);
     private static final String LOGMESSAGE = "SubBabaApi: ";
     // API parts
     private static final String API_BASE = "http://www.sub-baba.com/api/";
     private static final String API_TYPE = "/json/";
     private static final String API_SLASH = "/";
     // Jackson JSON configuration
     private static ObjectMapper mapper = new ObjectMapper();
     private static String apiKey;
 
     private ApiBuilder() {
         throw new UnsupportedOperationException("Class cannot be initialised");
     }
 
     public static void setApiKey(String newApiKey) {
         apiKey = newApiKey;
     }
 
     public static List<SubBabaMovie> searchByEnglishName(String query, SearchType searchType) {
         SubBabaWrapper sbw = getWrapper(SubBabaWrapper.class, SearchFunction.NAME, query, searchType);
         if (sbw == null) {
             return Collections.EMPTY_LIST;
         }
 
         List<SubBabaMovie> movies = new ArrayList<SubBabaMovie>();
        for(SubBabaResult result : sbw.getResults()) {
             movies.add(result.getMovie());
         }
 
         return movies;
     }
 
     /**
      * Get the single image information using the Sub-Baba ID
      *
      * @param query
      * @return
      */
     public static SubBabaContent fetchInfoByContentId(String query) {
         SubBabaWrapper sbw = getWrapper(SubBabaWrapper.class, SearchFunction.SUBBABA, query);
 
         if (sbw == null) {
             return null;
         } else {
             return sbw.getContent();
         }
     }
 
     public static SubBabaMovie searchByImdbId(String query, SearchType searchType) {
         SubBabaWrapper sbw = getWrapper(SubBabaWrapper.class, SearchFunction.IMDB, query, searchType);
        if (sbw == null) {
            return null;
         }
 
         return sbw.getResults().get(0).getMovie();
     }
 
     private static URL buildUrl(SearchFunction function, String query, SearchType searchType) {
         StringBuilder sbURL = new StringBuilder(API_BASE);
         sbURL.append(apiKey);
         sbURL.append(API_TYPE);
         sbURL.append(function.getType()).append(API_SLASH);
 
         if (StringUtils.isNotBlank(query)) {
             try {
                 sbURL.append(URLEncoder.encode(query.toLowerCase(), "UTF-8"));
             } catch (UnsupportedEncodingException ignore) {
                 // Failed to encode the string, so try it un-encoded
                 sbURL.append(query);
             }
         }
 
         if (searchType != null) {
             sbURL.append(API_SLASH);
             sbURL.append(searchType.getType());
         }
 
         logger.trace(LOGMESSAGE + "URL = " + sbURL.toString());
         try {
             return new URL(sbURL.toString());
         } catch (MalformedURLException ex) {
             logger.trace(LOGMESSAGE + "Failed to convert string to URL: " + ex.getMessage());
             return null;
         }
     }
 
     private static <T> T getWrapper(Class<T> clazz, SearchFunction function, String query) {
         return getWrapper(clazz, function, query, null);
     }
 
     private static <T> T getWrapper(Class<T> clazz, SearchFunction function, String query, SearchType searchType) {
         try {
             String webPage = WebBrowser.request(buildUrl(function, query, searchType));
             Object response = mapper.readValue(webPage, clazz);
             return clazz.cast(response);
         } catch (JsonParseException ex) {
             logger.warn(LOGMESSAGE + "JsonParseException: " + ex.getMessage());
         } catch (JsonMappingException ex) {
             logger.warn(LOGMESSAGE + "JsonMappingException: " + ex.getMessage());
         } catch (IOException ex) {
             logger.warn(LOGMESSAGE + "IOException: " + ex.getMessage());
         }
         return null;
     }
 }
