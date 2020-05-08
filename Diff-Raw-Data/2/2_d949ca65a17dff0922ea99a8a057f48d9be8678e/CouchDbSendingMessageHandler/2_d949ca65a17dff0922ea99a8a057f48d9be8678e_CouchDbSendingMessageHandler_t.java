 /*
  * Copyright 2011 the original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opencredo.couchdb;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.expression.BeanFactoryResolver;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.expression.Expression;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.expression.spel.support.StandardEvaluationContext;
 import org.springframework.expression.spel.support.StandardTypeConverter;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.MediaType;
 import org.springframework.integration.Message;
 import org.springframework.integration.handler.AbstractMessageHandler;
 import org.springframework.util.Assert;
 import org.springframework.web.client.RestOperations;
 import org.springframework.web.client.RestTemplate;
 
 /**
  * A message handler that creates new CouchDB documents from SI messages.
  * The id of the created documents is by default that of the Spring Integration message, but this can be customized using a Spel expression.
  * This handler relies on a RestTemplate to communicate with CouchDB. By default a plain RestTemplate
  * is created but a custom one can be provided using the appropriate constructor.
  *
  * @author Tareq Abedrabbo (tareq.abedrabbo@opencredo.com)
  * @since 11/01/2011
  */
 public class CouchDbSendingMessageHandler extends AbstractMessageHandler {
 
     protected transient final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private static final ExpressionParser expressionParser = new SpelExpressionParser();
 
     private final RestOperations restOperations;
     private final String databaseUrl;
     private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
     private Expression documentIdExpression;
 
 
     /**
      * Creates a handler instance with a database URL and a RestTemplate
      */
     public CouchDbSendingMessageHandler(String databaseUrl, RestOperations restOperations) {
         Assert.hasText(databaseUrl, "databaseUrl cannot be empty");
         Assert.notNull(restOperations, "restTemplate cannot be null");
         this.databaseUrl = CouchDbUtils.addId(databaseUrl);
         this.restOperations = restOperations;
     }
 
     /**
      * Creates a handler instance with the database URL 
      */
     public CouchDbSendingMessageHandler(String databaseUrl) {
         this(databaseUrl, new RestTemplate());
     }
 
 
     @Override
     protected void onInit() {
         BeanFactory beanFactory = this.getBeanFactory();
         if (beanFactory != null) {
             this.evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
         }
         ConversionService conversionService = this.getConversionService();
         if (conversionService != null) {
             this.evaluationContext.setTypeConverter(new StandardTypeConverter(conversionService));
         }
     }
 
 
     @Override
     protected final void handleMessageInternal(Message<?> message) throws Exception {
         String documentId = createDocumentId(message);
         HttpEntity<?> httpEntity = createHttpEntity(message);
 
        logger.debug("sending message to CouchDB [{}]", httpEntity.getBody());
         restOperations.put(databaseUrl, httpEntity, documentId);
     }
 
     private HttpEntity<?> createHttpEntity(Message<?> message) {
         Object payload = message.getPayload();
 
         if (payload instanceof HttpEntity) {
             HttpEntity httpEntity = (HttpEntity) payload;
             Assert.isTrue(httpEntity.getHeaders().getContentType().equals(MediaType.APPLICATION_JSON),
                     "HttpEntity payload with non application/json content type found.");
             return httpEntity;
         }
 
         HttpHeaders httpHeaders = new HttpHeaders();
         httpHeaders.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity<Object> httpEntity = new HttpEntity<Object>(payload, httpHeaders);
 
         return httpEntity;
     }
 
     private String createDocumentId(Message<?> message) {
         String documentId;
         if (documentIdExpression == null) {
             documentId = message.getHeaders().getId().toString();
         } else {
             documentId = documentIdExpression.getValue(evaluationContext, message, String.class);
         }
         logger.debug("created document id [{}]", documentId);
         return documentId;
     }
 
     /** Sets the Spel expression used to create document ids. If not specified, the default behavior is to use the id of the handled message */
     public void setDocumentIdExpression(String documentIdExpression) {
         this.documentIdExpression = expressionParser.parseExpression(documentIdExpression);
     }
 }
