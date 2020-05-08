 /*
  * JBoss, Home of Professional Open Source
  * 
  * Copyright 2012, Red Hat Middleware LLC, and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kie.tests.wb.eap.rest;
 
 import static org.kie.tests.wb.base.methods.TestConstants.*;
 
 import java.net.URL;
 
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.junit.InSequence;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.jboss.shrinkwrap.api.Archive;
 import org.junit.AfterClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kie.tests.wb.base.methods.RestIntegrationTestMethods;
 import org.kie.tests.wb.eap.deploy.KieWbWarJbossEapDeploy;
 
 @RunAsClient
 @RunWith(Arquillian.class)
 public class JbossEapBasicAuthRestIntegrationTest extends KieWbWarJbossEapDeploy {
 
     @Deployment(testable = false, name="kie-wb-basic-auth")
     public static Archive<?> createWar() {
        return createWarWithTestDeploymentLoader("eap-6_1");
     }
 
     @ArquillianResource
     URL deploymentUrl;
 
     private RestIntegrationTestMethods restTests = new RestIntegrationTestMethods(KJAR_DEPLOYMENT_ID, MediaType.APPLICATION_JSON);
     
     @AfterClass
     public static void waitForTxOnServer() throws InterruptedException { 
         Thread.sleep(1000);
     }
    
     @Test
     @InSequence(1)
     public void testDeployment() throws Exception { 
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.deployModule(deploymentUrl, requestFactory);
     }
     
     @Test
     @InSequence(2)
     public void testRestUrlStartHumanTaskProcess() throws Exception {
          ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, SALA_USER, SALA_PASSWORD);
         restTests.urlsStartHumanTaskProcess(deploymentUrl, requestFactory, requestFactory);
     }
     
     @Test
     @InSequence(2)
     public void testRestExecuteStartProcess() throws Exception { 
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.commandsStartProcess(deploymentUrl, requestFactory);
     }
     
     @Test
     @InSequence(2)
     public void testRestRemoteApiHumanTaskProcess() throws Exception {
         restTests.remoteApiHumanTaskProcess(deploymentUrl, USER, PASSWORD);
     }
     
     @Test
     @InSequence(2)
     public void testRestExecuteTaskCommands() throws Exception  {
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.commandsTaskCommands(deploymentUrl, requestFactory, USER, PASSWORD);
     }
     
     @Test
     @InSequence(2)
     public void testRestHistoryLogs() throws Exception {
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.urlsHistoryLogs(deploymentUrl, requestFactory);
     }
     
     @Test
     @InSequence(2)
     public void testRestDataServicesCoupling() throws Exception {
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.urlsDataServiceCoupling(deploymentUrl, requestFactory, USER);
     }
     
     @Test
     @InSequence(2)
     public void testJsonAndXmlStartProcess() throws Exception { 
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.urlsJsonJaxbStartProcess(deploymentUrl, requestFactory);
     }
     
     @Test
     @InSequence(2)
     public void testHumanTaskCompleteWithVariable() throws Exception { 
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.urlsHumanTaskWithFormVariableChange(deploymentUrl, requestFactory);
     }
 
     @Test
     @InSequence(2)
     public void testHttpURLConnection() throws Exception { 
         restTests.urlsHttpURLConnectionAcceptHeaderIsFixed(deploymentUrl, USER, PASSWORD);
     }
 
     @Test
     @InSequence(2)
     public void testRemoteApiProcessInstances() throws Exception { 
         restTests.remoteApiSerialization(deploymentUrl, USER, PASSWORD);
     }
     
     @Test
     @InSequence(2)
     public void testRemoteApiExtraJaxbClasses() throws Exception { 
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         restTests.remoteApiExtraJaxbClasses(deploymentUrl, requestFactory, USER, PASSWORD);
     }
 }
