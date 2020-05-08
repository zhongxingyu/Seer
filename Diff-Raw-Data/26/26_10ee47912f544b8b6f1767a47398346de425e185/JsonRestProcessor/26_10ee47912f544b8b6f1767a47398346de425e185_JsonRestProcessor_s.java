 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.fusesource.camel.component.salesforce.internal;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.http.Consts;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.fusesource.camel.component.salesforce.api.RestClient;
 import org.fusesource.camel.component.salesforce.api.RestException;
 import org.fusesource.camel.component.salesforce.api.dto.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executor;
 
 import static org.fusesource.camel.component.salesforce.SalesforceEndpointConfig.*;
 
 public class JsonRestProcessor extends AbstractRestProcessor {
 
     private static final Logger LOG = LoggerFactory.getLogger(JsonRestProcessor.class);
 
     private final ObjectMapper objectMapper;
     private static final String RESPONSE_TYPE = JsonRestProcessor.class.getName() + ".responseType";
     private static final String RESPONSE_CLASS = JsonRestProcessor.class.getName() + ".responseClass";
 
     public JsonRestProcessor(RestClient restClient,
                              RestClientHelper.ApiName apiName, Executor executor,
                              Map<String, String> endpointConfig) {
         super(restClient, apiName, executor, endpointConfig);
 
         this.objectMapper = new ObjectMapper();
     }
 
     @Override
     protected InputStream processRequest(Exchange exchange) {
         // TODO process JSON parameters
         InputStream request = null;
 
         try {
             switch (getApiName()) {
                 case GET_VERSIONS:
                     // handle in built response types
                     exchange.setProperty(RESPONSE_TYPE, new TypeReference<List<Version>>() {});
                     break;
 
                 case GET_RESOURCES:
                     // handle in built response types
                     exchange.setProperty(RESPONSE_CLASS, RestResources.class);
                     break;
 
                 case GET_GLOBAL_OBJECTS:
                     // handle in built response types
                     exchange.setProperty(RESPONSE_CLASS, GlobalObjects.class);
                     break;
 
                 case GET_SOBJECT_BASIC_INFO:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, USE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
 
                     // handle in built response types
                     exchange.setProperty(RESPONSE_CLASS, SObjectBasicInfo.class);
                     break;
 
                 case GET_SOBJECT_DESCRIPTION:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, USE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
 
                     // handle in built response types
                     exchange.setProperty(RESPONSE_CLASS, SObjectDescription.class);
                     break;
 
                 case GET_SOBJECT_BY_ID:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_ID, exchange, USE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
 
                     // get optional field list
                     String fieldsValue = getParameter(SOBJECT_FIELDS, exchange, IGNORE_IN_BODY, IS_OPTIONAL);
                     if (fieldsValue != null) {
                         String[] fields = fieldsValue.split(",");
                         exchange.setProperty(SOBJECT_FIELDS, fields);
                     }
 
                     // use custom response class property
                    if (setResponseClass(exchange)) {
                         return null;
                     }
                     break;
 
                 case CREATE_SOBJECT:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
 
                     // handle known response type
                     exchange.setProperty(RESPONSE_CLASS, CreateSObjectResult.class);
                     request = getRequest(exchange);
                     break;
 
                 case UPDATE_SOBJECT_BY_ID:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_ID, exchange, IGNORE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
 
                     request = getRequest(exchange);
                     break;
 
                 case DELETE_SOBJECT_BY_ID:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_ID, exchange, USE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
                     break;
 
                 case CREATE_OR_UPDATE_SOBJECT_BY_EXTERNAL_ID:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_EXT_ID_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_EXT_ID_VALUE, exchange, IGNORE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
 
                     // handle known response type
                     exchange.setProperty(RESPONSE_CLASS, CreateSObjectResult.class);
                     request = getRequest(exchange);
                     break;
 
                 case DELETE_SOBJECT_BY_EXTERNAL_ID:
                     // get parameters and set them in exchange
                     if (!setParameter(SOBJECT_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_EXT_ID_NAME, exchange, IGNORE_IN_BODY, NOT_OPTIONAL) ||
                         !setParameter(SOBJECT_EXT_ID_VALUE, exchange, USE_IN_BODY, NOT_OPTIONAL)) {
                         return null;
                     }
                     break;
 
                 case EXECUTE_QUERY:
                     break;
                 case EXECUTE_SEARCH:
                     break;
             }
         } catch (IOException e) {
             String msg = "Error marshaling request: " + e.getMessage();
             LOG.error(msg, e);
             exchange.setException(new RestException(msg, e));
         }
 
         return request;
     }
 
     private boolean setResponseClass(Exchange exchange) {
         Class sObjectClass;
         final String className = getParameter(SOBJECT_CLASS, exchange, IGNORE_IN_BODY, NOT_OPTIONAL);
         if (className == null) {
            return true;
         }
 
         try {
             sObjectClass = Thread.currentThread().getContextClassLoader().loadClass(className);
         } catch (ClassNotFoundException e) {
             String msg = String.format("Error loading class %s : %s", className, e.getMessage());
             LOG.error(msg, e);
             exchange.setException(new RestException(msg, e));
            return true;
         }
         exchange.setProperty(RESPONSE_CLASS, sObjectClass);
        return false;
     }
 
     // get request stream from In message
     private InputStream getRequest(Exchange exchange) throws IOException {
         InputStream request;
         Message in = exchange.getIn();
         request = in.getBody(InputStream.class);
         if (request == null) {
             AbstractSObjectBase sObject = in.getBody(AbstractSObjectBase.class);
             if (sObject != null) {
                 // marshall the SObject
                 ByteArrayOutputStream out = new ByteArrayOutputStream();
                 objectMapper.writeValue(out, sObject);
                 request = new ByteArrayInputStream(out.toByteArray());
             } else {
                 // if all else fails, get body as String
                 final String body = in.getBody(String.class);
                 if (null == body) {
                     String msg = "Unsupported request message body " +
                         (in.getBody() == null ? null : in.getBody().getClass());
                     LOG.error(msg);
                     exchange.setException(new RestException(msg, null));
                 } else {
                     request = new ByteArrayInputStream(body.getBytes(Consts.UTF_8));
                 }
             }
         }
         return request;
     }
 
     @Override
     protected void processResponse(Exchange exchange, InputStream responseEntity) {
         // process JSON response for TypeReference
         try {
             // do we need to un-marshal a response
             if (responseEntity != null) {
                 Object response = null;
                 Class<?> responseClass = exchange.getProperty(RESPONSE_CLASS, Class.class);
                 if (responseClass != null) {
                     response = objectMapper.readValue(responseEntity, responseClass);
                 } else {
                     TypeReference<?> responseType = exchange.getProperty(RESPONSE_TYPE, TypeReference.class);
                     response = objectMapper.readValue(responseEntity, responseType);
                 }
                 exchange.getOut().setBody(response);
             }
             // copy headers and attachments
             exchange.getOut().getHeaders().putAll(exchange.getIn().getHeaders());
             exchange.getOut().getAttachments().putAll(exchange.getIn().getAttachments());
         } catch (IOException e) {
             String msg = "Error parsing JSON response: " + e.getMessage();
             LOG.error(msg, e);
             exchange.setException(new RestException(msg, e));
         }
     }
 
 }
