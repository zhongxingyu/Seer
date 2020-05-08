 /*
  * Copyright 2010-2013 Ning, Inc.
  *
  *  Ning licenses this file to you under the Apache License, version 2.0
  *  (the "License"); you may not use this file except in compliance with the
  *  License.  You may obtain a copy of the License at:
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  *  License for the specific language governing permissions and limitations
  *  under the License.
  */
 
 package com.ning.killbill.zuora.zuora;
 
 import static com.google.common.collect.Iterables.getOnlyElement;
 import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.annotation.Nullable;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.WebServiceClient;
 
 import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;
 import com.ning.killbill.zuora.util.Either;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.frontend.ClientProxy;
 import org.apache.cxf.interceptor.LoggingInInterceptor;
 import org.apache.cxf.interceptor.LoggingOutInterceptor;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.zuora.api.DeleteResult;
 import com.zuora.api.Error;
 import com.zuora.api.ErrorCode;
 import com.zuora.api.LoginFault;
 import com.zuora.api.LoginResult;
 import com.zuora.api.QueryResult;
 import com.zuora.api.SaveResult;
 import com.zuora.api.SessionHeader;
 import com.zuora.api.Soap;
 import com.zuora.api.SubscribeRequest;
 import com.zuora.api.SubscribeResult;
 import com.zuora.api.UnexpectedErrorFault;
 import com.zuora.api.ZuoraService;
 import com.zuora.api.object.ZObject;
 
 public class ZuoraConnection {
     public static final String ZUORA_ACCOUNT_ID_KEY = "zuoraAccountId";
 
     private final ZuoraConfig config;
     private final ZuoraService zuoraService;
     private final Soap stub;
     private final com.zuora.api.ObjectFactory apiFactory;
     private final com.zuora.api.object.ObjectFactory objectFactory;
     private SessionHeader header;
 
     public ZuoraConnection(ZuoraConfig config) {
         this.config = config;
         try {
             URL wsdlLocation = ZuoraService.class.getClassLoader().getResource(ZuoraService.class.getAnnotation(WebServiceClient.class).wsdlLocation());
 
             this.zuoraService = new ZuoraService(wsdlLocation);
             this.stub = zuoraService.getSoap();
 
             BindingProvider bp = (BindingProvider) stub;
             Map<String, Object> context = bp.getRequestContext();
 
             context.put(ENDPOINT_ADDRESS_PROPERTY, config.getZuoraApiUrl());
 
             Client client = ClientProxy.getClient(stub);
 
             client.getInInterceptors().add(new LoggingInInterceptor());
             client.getOutInterceptors().add(new LoggingOutInterceptor());
 
             this.apiFactory = new com.zuora.api.ObjectFactory();
             this.objectFactory = new com.zuora.api.object.ObjectFactory();
             this.header = apiFactory.createSessionHeader();
             login();
         }
         catch (Exception ex) {
             throw new IllegalArgumentException("Could not login to Zuora", ex);
         }
     }
 
     private void login() throws UnexpectedErrorFault, LoginFault, IOException {
 
         final String user;
         final String password;
         final String zuoraPropertyLocaltion = config.getZuoraPropertyFileLocation();
         final File zuoraCredentialsFile = zuoraPropertyLocaltion != null ? new File(config.getZuoraPropertyFileLocation()) : null;
         if (zuoraCredentialsFile != null && zuoraCredentialsFile.exists()) {
             Properties zuoraCredentials = new Properties();
             zuoraCredentials.load(new FileReader(zuoraCredentialsFile));
             user = zuoraCredentials.getProperty("user");
             password = zuoraCredentials.getProperty("password");
         } else {
             user = config.getZuoraUserName();
             password = config.getZuoraPassword();
         }
 

         LoginResult session = stub.login(user, password);
 
         header.setSession(session.getSession());
     }
 
     public com.zuora.api.ObjectFactory getApiFactory() {
         return apiFactory;
     }
 
     public com.zuora.api.object.ObjectFactory getObjectFactory() {
         return objectFactory;
     }
 
     @SuppressWarnings("unchecked")
     public <T extends ZObject> Either<ZuoraError, List<T>> query(String queryString) {
         int numTries = 0;
 
         while (numTries < config.getMaxLoginRetries()) {
             numTries++;
             try {
                 List<T> result = null;
                 QueryResult queryResult;
 
                 do {
                     queryResult = stub.query(queryString, null, header);
                     if (result == null) {
                         int theSize = 0;
                         if (queryResult.getSize() > 0) {
                             theSize = queryResult.getSize();
                         }
                         result = (List<T>)Lists.newArrayList(queryResult.getRecords().subList(0, theSize));
                     }
                     else {
                         result.addAll((List<T>)Lists.newArrayList(queryResult.getRecords().subList(0, queryResult.getRecords().size())));
                     }
                 }
                 while (!queryResult.isDone());
 
                 // Crappy zuora API is returning a list with one null element when there is 0 records.
                 if (CollectionUtils.isEmpty(result) || ((result.size() == 1) && result.get(0) == null)) {
                     return Either.right(Collections.<T>emptyList());
                 }
                 else {
                     return Either.right(result);
                 }
             }
             catch (UnexpectedErrorFault fault) {
                 if (fault.getFaultInfo().getFaultCode() == ErrorCode.INVALID_SESSION) {
                     try {
                         login();
                         continue;
                     }
                     catch (Exception ex) {
                         return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
                     }
                 }
             }
             catch (Exception ex) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
             }
         }
         return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not establish a valid zuora session after " + numTries + " attempts"));
     }
 
     public <T extends ZObject> Either<ZuoraError, T> querySingle(String queryString) {
         Either<ZuoraError, List<T>> resultsOrError = query(queryString);
 
         if (resultsOrError.isLeft()) {
             return Either.left(resultsOrError.getLeft());
         }
         else {
             List<T> results = resultsOrError.getRight();
 
             return Either.right(CollectionUtils.isEmpty(results) ? null : getOnlyElement(results));
         }
     }
 
     private boolean isSessionInvalid(List<com.zuora.api.Error> errors) {
         if (CollectionUtils.isNotEmpty(errors)) {
             for (com.zuora.api.Error error : errors) {
                 if (error.getCode() == ErrorCode.INVALID_SESSION) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public <T extends ZObject> Either<ZuoraError, String> createWithId(T object) {
         int numTries = 0;
 
         while (numTries < config.getMaxLoginRetries()) {
             numTries++;
             try {
                 List<SaveResult> results = stub.create(Arrays.<ZObject>asList(object), header);
 
                 if (CollectionUtils.isEmpty(results)) {
                     return Either.left(new ZuoraError(ErrorCode.UNKNOWN_ERROR.toString(), "Did not get any result back"));
                 }
                 else {
                     List<Error> errors = results.get(0).getErrors();
 
                     if (errors.isEmpty()) {
                         return Either.right(results.get(0).getId());
                     }
                     else if (isSessionInvalid(errors)) {
                         login();
                         continue;
                     }
                     else {
                         return Either.left(new ZuoraError(errors.get(0).getCode().toString(), errors.get(0).getMessage()));
                     }
                 }
             }
             catch (Exception ex) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
             }
         }
         return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not establish a valid zuora session after " + numTries + " attempts"));
     }
 
     public <T extends ZObject> Either<ZuoraError, SaveResult> createWithResult(T object) {
         int numTries = 0;
 
         while (numTries < config.getMaxLoginRetries()) {
             numTries++;
 
             try {
                 List<SaveResult> results = stub.create(Arrays.<ZObject>asList(object), header);
 
                 if (CollectionUtils.isEmpty(results)) {
                     return Either.left(new ZuoraError(ErrorCode.UNKNOWN_ERROR.toString(), "Did not get any result back"));
                 }
                 else {
                     List<Error> errors = results.get(0).getErrors();
 
                     if (errors.isEmpty()) {
                         return Either.right(results.get(0));
                     }
                     else if (isSessionInvalid(errors)) {
                         login();
                         continue;
                     }
                     else {
                         return Either.left(new ZuoraError(results.get(0).getErrors().get(0).getCode().toString(),
                             results.get(0).getErrors().get(0).getMessage()));
                     }
                 }
             }
             catch (Exception ex) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
             }
         }
         return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not establish a valid zuora session after " + numTries + " attempts"));
     }
 
     public <T extends ZObject> Either<ZuoraError, String> update(T object) {
         int numTries = 0;
 
         while (numTries < config.getMaxLoginRetries()) {
             numTries++;
 
             try {
                 List<SaveResult> results = stub.update(Arrays.<ZObject>asList(object), header);
 
                 if (CollectionUtils.isEmpty(results)) {
                     return Either.left(new ZuoraError(ErrorCode.UNKNOWN_ERROR.toString(), "Did not get any result back"));
                 }
                 else {
                     List<Error> errors = results.get(0).getErrors();
 
                     if (errors.isEmpty()) {
                         return Either.right(results.get(0).getId());
                     }
                     else if (isSessionInvalid(errors)) {
                         login();
                         continue;
                     }
                     else {
                         return Either.left(new ZuoraError(results.get(0).getErrors().get(0).getCode().toString(),
                             results.get(0).getErrors().get(0).getMessage()));
                     }
                 }
             }
             catch (Exception ex) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
             }
         }
         return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not establish a valid zuora session after " + numTries + " attempts"));
     }
 
     public Either<ZuoraError, SubscribeResult> subscribe(SubscribeRequest zuoraSubscribeRequest) {
         int numTries = 0;
 
         while (numTries < config.getMaxLoginRetries()) {
             numTries++;
             try {
                 List<SubscribeResult> results = stub.subscribe(Arrays.asList(zuoraSubscribeRequest), header);
 
                 if (CollectionUtils.isEmpty(results)) {
                     return Either.left(new ZuoraError(ErrorCode.UNKNOWN_ERROR.toString(), "Did not get any result back"));
                 }
                 else {
                     List<Error> errors = results.get(0).getErrors();
 
                     if (errors.isEmpty()) {
                         return Either.right(results.get(0));
                     }
                     else if (isSessionInvalid(errors)) {
                         login();
                         continue;
                     }
                     else {
                         return Either.left(new ZuoraError(results.get(0).getErrors().get(0).getCode().toString(),
                             results.get(0).getErrors().get(0).getMessage()));
                     }
                 }
             }
             catch (Exception ex) {
                 return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
             }
         }
         return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, "Could not establish a valid zuora session after " + numTries + " attempts"));
     }
 
     public Either<ZuoraError, Void> delete(final List<? extends ZObject> objs) {
         int numTries = 0;
 
         while (numTries < config.getMaxLoginRetries()) {
             numTries++;
             if (!objs.isEmpty()) {
                 final String className = objs.get(0).getClass().getSimpleName();
 
                 final List<String> ids = Lists.transform(objs, new Function<ZObject, String>() {
                     @Override
                     public String apply(@Nullable ZObject from) {
                         return from.getId();
                     }
                 });
                 try {
                     List<DeleteResult> results = stub.delete(className, ids, header);
                     if (CollectionUtils.isEmpty(results)) {
                         return Either.left(new ZuoraError(ErrorCode.UNKNOWN_ERROR.toString(), "Did not get any result back"));
                     }
 
                     if (results.get(0).getErrors().isEmpty()) {
                         return Either.right(null);
                     } else {
                         if (isSessionInvalid(results.get(0).getErrors())) {
                             login();
                             continue;
                         }
 
                         return Either.left(new ZuoraError(results.get(0).getErrors().get(0).getCode().toString(),
                                 results.get(0).getErrors().get(0).getMessage()));
 
                     }
                 } catch (Exception ex) {
                     return Either.left(new ZuoraError(ZuoraError.ERROR_UNKNOWN, ex.getMessage()));
                 }
             }
         }
         return Either.right(null);
     }
 }
