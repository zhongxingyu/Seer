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
 package org.apache.servicemix.components.xfire;
 
 import javax.xml.namespace.QName;
 
 import org.apache.servicemix.tck.TestSupport;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.w3c.dom.Node;
 import org.xbean.spring.context.ClassPathXmlApplicationContext;
 
 public class XFireBindingTest extends TestSupport {
 
     public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
         Object answer = requestServiceWithFileRequest(new QName("http://xfire.components.servicemix.org", "Echo"),
                "/org/apache/servicemix/components/xfire/echo.xml");
         assertTrue("Shoud return a DOM Node: " + answer, answer instanceof Node);
         Node node = (Node) answer;
         System.out.println(transformer.toString(node));
         
         Echo echo = (Echo) context.getBean("xfireReceiverService");
         assertEquals(1, echo.getCount());
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext(new String[] {
                 "/org/apache/servicemix/components/xfire/xfire-inout.xml",
                 "/org/codehaus/xfire/spring/xfire.xml"
         });
     }
 }
