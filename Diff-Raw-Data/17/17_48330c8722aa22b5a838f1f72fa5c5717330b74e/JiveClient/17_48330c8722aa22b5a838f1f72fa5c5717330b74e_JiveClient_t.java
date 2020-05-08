/**
 * Mule Jive Cloud Connector
 *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.modules.jive.api;
 
 import org.mule.modules.jive.api.xml.XmlMapper;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 public class JiveClient
 {
     private static final int serverMinAcceptableStatus = 300;
     
     private final XmlMapper mapper = new XmlMapper();
     private final WebResource resource;
     
     /**
      * @param gateway 
      */
     public JiveClient(final WebResource gateway)
     {
         this.resource = gateway;
     }
 
     public Map<String, Object> doRequestWithPayload(final String uri, final String method, final String payload) 
     {
         ClientResponse result = resource.path(uri).method(method, ClientResponse.class, payload);
         if (result.getStatus() > serverMinAcceptableStatus)
         {
             throw new JiveGenericException(StringUtils.substringAfterLast(
                 StringUtils.substringBefore(result.getEntity(String.class), "</ns1:faultstring>"), ">"));
         }
         return mapper.xml2map(result.getEntity(String.class));
     }
     
     public Map<String, Object> doRequest(final String uri, final String method, final String pathParams)
     {
         ClientResponse result = resource.path(JiveUris.getOperationUri(uri, pathParams)).method(method, ClientResponse.class);
         if (result.getStatus() > serverMinAcceptableStatus)
         {
             throw new JiveGenericException(StringUtils.substringAfterLast(
                 StringUtils.substringBefore(result.getEntity(String.class), "</ns1:faultstring>"), ">"));
         }
         return mapper.xml2map(result.getEntity(String.class));
     }
 
     /**
      * @param uri
      * @param method
      * @param string2
      * @return
      */
     public String doRequestAndExtractTagBetween(String uri, String method, String tagName)
     {
         final String response = resource.path(uri).method(method, String.class);
         return StringUtils.substringBetween(response, "<" + tagName + ">", "</" + tagName + ">");
     }
 }
 
 
