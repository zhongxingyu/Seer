 /**
  * Mule Coupa Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  *
  * Coupa Connector com.coupa package contains portions of code based on Coupa4j
  * http://code.google.com/p/coupa4j/, under a MIT license:
  * http://www.opensource.org/licenses/mit-license.php.
  */
 
 
 package com.coupa.api.impl;
 
 import javax.ws.rs.core.MultivaluedMap;
 import javax.xml.bind.JAXBElement;
 
 import net.sf.staccatocommons.collections.stream.Streams;
 
 import com.coupa.api.Client;
 import com.coupa.api.RESTException;
 import com.coupa.resources.Errors;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.WebResource.Builder;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.representation.Form;
 
 /**
  * Coupa {@link Client} based on WS-RS and Jersey
  *
  * @author flbulgarelli
  */
 public class JerseyClient implements Client
 {
     private static final String COUPA_API_KEY_NAME = "X_COUPA_API_KEY";
 
     private final String apiKey;
 
     private WebResource resource;
 
     public JerseyClient(String baseUrl, String apiKey)
     {
         this.apiKey = apiKey;
         this.resource = com.sun.jersey.api.client.Client.create(new DefaultClientConfig()).resource(baseUrl);
     }
 
     @Override
     public <T> T get(String uri, Class<T> clazz)
     {
         return get(uri, new Form(), clazz);
     }
 
     @Override
     public <T> T get(String uri, MultivaluedMap<String, String> urlParams, Class<T> clazz)
     {
         return fetch(uri, urlParams, "GET", clazz);
     }
 
     @Override
     public <T> T put(String url, Object content, Class<T> clazz)
     {
         return uploadAndFetch(url, content, "PUT", clazz);
     }
 
     @Override
     public <T> T post(String url, Object content, Class<T> clazz)
     {
         return uploadAndFetch(url, content, "POST", clazz);
     }
 
     protected <T> T uploadAndFetch(String url, Object content, String method, Class<T> clazz)
     {
         return parseResponse(newRequestBuilder(url).method(method, ClientResponse.class, content), clazz);
     }
 
     protected <T> T fetch(String url,
                           MultivaluedMap<String, String> params,
                           String method,
                           Class<T> responseClazz)
     {
         return parseResponse(newRequestBuilder(url, params).method(method, ClientResponse.class),
             responseClazz);
     }
 
     @SuppressWarnings("unchecked")
     protected <T> T parseResponse(ClientResponse response, Class<T> responseType)
     {
        if (response.getStatus() >= 300)
         {
            throw new RESTException(Streams.from(response.getEntity(Errors.class).getError())
                .joinStrings(","));
         }
         T entity = response.getEntity(responseType);
         if (entity instanceof JAXBElement)
         {
             return ((JAXBElement<T>) entity).getValue();
         }
         return entity;
     }
 
     protected Builder newRequestBuilder(String url)
     {
         return newRequestBuilder(url, new Form());
     }
 
     protected Builder newRequestBuilder(String url, MultivaluedMap<String, String> params)
     {
         return resource.path(url)
             .queryParams(params)
            .accept("application/xml")
            .type("text/xml")
             .header(COUPA_API_KEY_NAME, apiKey);
     }
 }
