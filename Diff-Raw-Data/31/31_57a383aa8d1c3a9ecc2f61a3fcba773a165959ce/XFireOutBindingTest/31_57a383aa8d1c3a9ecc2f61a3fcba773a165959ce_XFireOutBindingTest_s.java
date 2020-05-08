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
 package org.apache.servicemix.components.xfire;
 
 import javax.jbi.JBIException;
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
 
 import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
 import org.apache.servicemix.tck.TestSupport;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 
 public class XFireOutBindingTest extends TestSupport {
     private OneWayService receiverService;
 
     protected void setUp() throws Exception {
         super.setUp();
         
         receiverService = (OneWayService) getBean("xfireReceiverService");
     }
     
     public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
         sendFile(new QName("http://xfire.components.servicemix.org", "OneWayService"),
                 "/org/apache/servicemix/components/xfire/oneway.xml"); 
 
        receiverService.getMessageList().assertMessagesReceived(1);
     }
 
     protected void sendFile(QName serviceName, String fileOnClassPath) throws JBIException {
         Source content = getSourceFromClassPath(fileOnClassPath);
 
         ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(serviceName);
 
         client.send(resolver, null, null, content);
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext(new String[] {
                 "/org/apache/servicemix/components/xfire/xfire-out.xml",
                 "/org/codehaus/xfire/spring/xfire.xml"
         });
     }
 }
