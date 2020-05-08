 /**
  * This work was created by the National Center for Ecological Analysis and Synthesis
  * at the University of California Santa Barbara (UCSB).
  *
 *   Copyright 2011 Regents of the University of California
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package edu.ucsb.nceas.ezid;
 
 import java.util.HashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A simple client application for the EZID Service that allows calling applications
  * to set up a connection to EZID and maintain that connection across a series of
  * service invocations.  Service call requests are maintained in a queue and submitted
  * to EZID asynchronously, allowing the rate of requests to the EZID service to be
  * controlled by the calling application.  Callback methods are provided to allow
  * the calling application to be notified of the error status of service invocations.
  * This client should be mainly useful to calling applications that need to issue many
  * requests to the EZID service.
  * 
  * @author Matthew Jones, NCEAS, UC Santa Barbara
  */
 public class EZIDClient  {
     private String USERNAME = "apitest";
     private String PASSWORD = "apitest";
     private EZIDService ezid = null;
     private ExecutorService executor = null;
 
     protected static Log log = LogFactory.getLog(EZIDClient.class);
 
     public EZIDClient(String baseURL) {
         ezid = new EZIDService(baseURL);
         startExecutorLoop();
     }
     
     public EZIDClient() {
         this(null);
     }
         
     public boolean login(String username, String password) {
         boolean loginSuccess = false;
         USERNAME = username;
         PASSWORD = password;
         try {
             ezid.login(USERNAME, PASSWORD);
             loginSuccess = true;
         } catch (EZIDException e) {
             loginSuccess = false;
         }        
         return loginSuccess;
     }
     
     public void create(String identifier, HashMap<String, String> metadata) throws InterruptedException {
         EZIDServiceRequest request = new EZIDServiceRequest(ezid, EZIDServiceRequest.CREATE, identifier, metadata);
         executor.execute(request);
     }
 
     public void delete(String identifier) throws InterruptedException {
         EZIDServiceRequest request = new EZIDServiceRequest(ezid, EZIDServiceRequest.DELETE, identifier);
         executor.execute(request);
     }
 
     public void setMetadata(String identifier, HashMap<String, String> metadata) throws InterruptedException {
         EZIDServiceRequest request = new EZIDServiceRequest(ezid, EZIDServiceRequest.SETMETADATA, identifier, metadata);
         executor.execute(request);
 
     }
     
     private void startExecutorLoop() {
         // Query the runtime to see how many CPUs are available, and configure that many threads
         Runtime runtime = Runtime.getRuntime();        
         int numCores = runtime.availableProcessors();
         log.debug("Number of cores available: " + numCores);
         executor = Executors.newFixedThreadPool(numCores);
     }
     
     public void shutdown() {
         log.debug("Shutting down executor...");
         // Stop the executor from accepting new requests and finishing existing Runnables
         executor.shutdown();
         // Wait until all Runnables are finished
         while (!executor.isTerminated()) {
 //            log.debug("...");
         }
     }
 }
