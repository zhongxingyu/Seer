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
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.client.ClientHttpResponse;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.web.client.DefaultResponseErrorHandler;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import static org.junit.Assume.assumeNoException;
 import static org.junit.Assume.assumeTrue;
 import static org.springframework.http.HttpStatus.OK;
 
 /**
  * Base class for CouchDB integration tests. Checks whether CouchDB is available before running each test,
  * in which case the test is executed. If CouchDB is not available, tests are ignored.
  *
  * @author Tareq Abedrabbo (tareq.abedrabbo@opencredo.com)
  * @since 13/01/2011
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 public abstract class CouchDbIntegrationTest {
 
 
     protected static final Logger logger = LoggerFactory.getLogger(CouchDbIntegrationTest.class);
 
     public static final String COUCHDB_URL = "http://127.0.0.1:5984/";
     public static final String TEST_DATABASE_URL = COUCHDB_URL + "si_couchdb_test/";
 
     protected static final RestTemplate restTemplate = new RestTemplate();
 
     /**
      * This methods ensures that the database is running. Otherwise, the test is ignored.
      */
     @BeforeClass
     public static void assumeDatabaseIsUpAndRunning() {
         try {
             ResponseEntity<String> responseEntity = restTemplate.getForEntity(COUCHDB_URL, String.class);
             assumeTrue(responseEntity.getStatusCode().equals(OK));
             logger.debug("CouchDB is running on {} with status {} ", COUCHDB_URL, responseEntity.getStatusCode());
         } catch (RestClientException e) {
             logger.debug("CouchDB is not running on {}", COUCHDB_URL);
             assumeNoException(e);
         }
     }
 
     @Before
     public void setUpTestDatabase() throws Exception {
         RestTemplate template = new RestTemplate();
         template.setErrorHandler(new DefaultResponseErrorHandler(){
             @Override
             public void handleError(ClientHttpResponse response) throws IOException {
                 // do nothing, error status will be handled in the switch statement
             }
         });
        ResponseEntity<String> response = template.getForEntity(TEST_DATABASE_URL, String.class);
         HttpStatus statusCode = response.getStatusCode();
         switch (statusCode) {
             case NOT_FOUND:
                 createNewTestDatabase();
                 break;
             case OK:
                 deleteExisitingTestDatabase();
                 createNewTestDatabase();
                 break;
             default:
                 throw new IllegalStateException("Unsupported http status [" + statusCode + "]");
         }
     }
 
     private void deleteExisitingTestDatabase() {
         restTemplate.delete(TEST_DATABASE_URL);
     }
 
     private void createNewTestDatabase() {
         restTemplate.put(TEST_DATABASE_URL, null);
     }
 
     /**
      * Reads a CouchDB document and converts it to the expected type.
      */
     protected <T> T getDocument(String id, Class<T> expectedType) {
         String url = TEST_DATABASE_URL + "{id}";
         return restTemplate.getForObject(url, expectedType, id);
     }
 
     /**
      * Writes a CouchDB document
      */
     protected String putDocument(Object document) {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);
         HttpEntity request = new HttpEntity(document, headers);
         String id = UUID.randomUUID().toString();
         restTemplate.put(TEST_DATABASE_URL + "{id}", request, id);
         return id;
     }
 }
