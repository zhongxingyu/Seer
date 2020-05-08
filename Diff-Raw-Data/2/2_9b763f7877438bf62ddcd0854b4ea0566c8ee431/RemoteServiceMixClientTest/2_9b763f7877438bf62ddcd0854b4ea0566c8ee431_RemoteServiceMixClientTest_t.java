 /*
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
 package org.apache.servicemix.client;
 
 import java.util.Set;
 
 import org.apache.activemq.transport.vm.VMTransportFactory;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 
 public class RemoteServiceMixClientTest extends ServiceMixClientTest {
 
     protected void tearDown() throws Exception {
         ((RemoteServiceMixClient) client).shutDown();
         super.tearDown();
        Set servers = VMTransportFactory.SERVERS.keySet();
         String[] serverNames = (String[]) servers.toArray(new String[servers.size()]);
         for (int i = 0; i < serverNames.length; i++) {
             VMTransportFactory.stopped(serverNames[i]);
         }
     }
     
     protected ServiceMixClient getClient() throws Exception {
         /*
         RemoteServiceMixClient client = new RemoteServiceMixClient("tcp://localhost:61616");
         client.start();
         return client;
         */
         return super.getClient();
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/client/remote.xml");
     }
 
     /*
     public void testSendUsingMapAndPOJOsUsingContainerRouting() throws Exception {
     }
 
     public void testRequestUsingPOJOWithXStreamMarshaling() throws Exception {
     }
     */
 
 }
