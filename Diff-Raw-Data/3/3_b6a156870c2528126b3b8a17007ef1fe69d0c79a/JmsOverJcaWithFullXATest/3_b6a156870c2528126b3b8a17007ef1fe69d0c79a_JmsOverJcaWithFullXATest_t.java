 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.components.jca;
 
 import org.apache.servicemix.tck.TestSupport;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.xbean.spring.context.ClassPathXmlApplicationContext;
 
 import javax.xml.namespace.QName;
 
 /**
  * @version $Revision$
  */
 public class JmsOverJcaWithFullXATest extends TestSupport {
 
     public void testSendMessagesToJmsThenOutofJmsToReceiver() throws Exception {
         QName service = new QName("http://servicemix.org/cheese/", "myJmsSender");
 
        sendMessages(service, messageCount, false);
        assertMessagesReceived(messageCount);
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/components/jca/xa-on-jca-flow.xml");
     }
 }
