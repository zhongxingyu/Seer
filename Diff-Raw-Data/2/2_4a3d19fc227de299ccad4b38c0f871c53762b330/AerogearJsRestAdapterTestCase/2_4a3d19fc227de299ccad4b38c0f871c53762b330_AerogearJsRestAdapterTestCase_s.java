 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors.
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
 package org.jboss.aerogear.js.pipeline.rest;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.InSequence;
 import org.jboss.arquillian.qunit.junit.QUnitRunner;
 import org.jboss.arquillian.qunit.junit.annotations.QUnitResources;
 import org.jboss.arquillian.qunit.junit.annotations.QUnitTest;
 import org.jboss.shrinkwrap.api.Archive;
 import org.junit.runner.RunWith;
 
 @RunWith(QUnitRunner.class)
 @QUnitResources("src/main/webapp")
 @RunAsClient
 public class AerogearJsRestAdapterTestCase {
 
     @Deployment
     public static Archive<?> createDeployment() {
         return null;
     }
 
     @QUnitTest("rest-cors-jsonp-pipe-tests.html")
     @InSequence(1)
     public void qunitRestPipeTests() {
         // intentionally left empty
     }
 
    @QUnitTest("rest-cors-jsonp-pipe-secured-endpoint-tests.html")
     @InSequence(2)
     public void qunitRestPipeSecureEndpointTests() {
         // intentionally left empty
     }
 
 }
