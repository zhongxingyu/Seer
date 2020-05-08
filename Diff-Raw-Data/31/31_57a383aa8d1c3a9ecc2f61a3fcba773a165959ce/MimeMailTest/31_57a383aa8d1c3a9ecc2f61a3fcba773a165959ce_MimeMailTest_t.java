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
 package org.apache.servicemix.components.email;
 
 import java.util.Date;
import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 import javax.activation.DataHandler;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.mail.Part;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
 
 import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
 import org.apache.servicemix.jbi.util.ByteArrayDataSource;
 import org.apache.servicemix.tck.TestSupport;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 
 /**
  * @version $Revision$
  */
 public class MimeMailTest extends TestSupport {
 
     public void testSendUsingMessageProperties() throws Exception {
 
         // START SNIPPET: email
         InOnly exchange = client.createInOnlyExchange();
         NormalizedMessage message = exchange.getInMessage();
 
         message.setProperty("org.apache.servicemix.email.to", "scm@servicemix.org");
         message.setProperty("org.apache.servicemix.email.from", "junit@servicemix.org");
         message.setProperty("org.apache.servicemix.email.subject", "Hello from JUnit!");
         message.setProperty("org.apache.servicemix.email.text", "Hi from test case: " + getName() + " running at: " + new Date());
 
         client.send(exchange);
         // END SNIPPET: email
 
         // lest find the test sender
         StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
         sender.assertMessagesReceived(1);
         List messages = sender.getMessages();
         assertEquals("message size: " + messages, 1, messages.size());
 
         MimeMessage mail = (MimeMessage) messages.get(0);
 
         System.out.println("Created message: " + message);
 
         assertEquals("subject", "Hello from JUnit!", mail.getSubject());
     }
     
     public void testSendUsingMessagePropertiesWithMultipleRecipients() throws Exception {
 
         // START SNIPPET: email
         InOnly exchange = client.createInOnlyExchange();
         NormalizedMessage message = exchange.getInMessage();
 
         message.setProperty("org.apache.servicemix.email.to", "scm@servicemix.org,foo@servicemix.org");
         message.setProperty("org.apache.servicemix.email.cc", "bar@servicemix.org,baz@servicemix.org");
         message.setProperty("org.apache.servicemix.email.bcc", "guinness@servicemix.org,harper@servicemix.org");
         message.setProperty("org.apache.servicemix.email.from", "junit@servicemix.org");
         message.setProperty("org.apache.servicemix.email.subject", "Hello from JUnit!");
         message.setProperty("org.apache.servicemix.email.text", "Hi from test case: " + getName() + " running at: " + new Date());
 
         client.send(exchange);
         // END SNIPPET: email
 
         // lest find the test sender
         StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
         sender.assertMessagesReceived(1);
         List messages = sender.getMessages();
         assertEquals("message size: " + messages, 1, messages.size());
 
         MimeMessage mail = (MimeMessage) messages.get(0);
 
         System.out.println("Created message: " + message);
 
         assertEquals("subject", "Hello from JUnit!", mail.getSubject());
     }
 
     public void testUsingXPathExpressionsInEmail() throws Exception {
 
         // START SNIPPET: xpath
         QName xpathSender = new QName("http://servicemix.org/cheese/", "emailSenderWithExpressions");
         ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(xpathSender);
 
         InOnly exchange = client.createInOnlyExchange(resolver);
 
         Source source = getSourceFromClassPath("request.xml");
         exchange.getInMessage().setContent(source);
 
         client.send(exchange);
         // END SNIPPET: xpath
 
         // lest find the test sender
         StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
         sender.assertMessagesReceived(1);
         List messages = sender.getMessages();
         assertEquals("message size: " + messages, 1, messages.size());
 
         MimeMessage message = (MimeMessage) messages.get(0);
 
         System.out.println("Created message: " + message);
 
         assertEquals("subject", "Drink a beer James", message.getSubject());
     }
 
     public void testUsingXPathExpressionsInEmailWithAttachment() throws Exception {
 
         // START SNIPPET: xpath
         QName xpathSender = new QName("http://servicemix.org/cheese/", "emailSenderWithExpressionsAndAttachment");
         ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(xpathSender);
 
         InOnly exchange = client.createInOnlyExchange(resolver);
 
         Source source = getSourceFromClassPath("request.xml");
         exchange.getInMessage().setContent(source);
         ByteArrayDataSource ds = new ByteArrayDataSource("hello".getBytes(), "text/plain");
         ds.setName("id");
         exchange.getInMessage().addAttachment("id", new DataHandler(ds));
 
         client.send(exchange);
         // END SNIPPET: xpath
 
         // lest find the test sender
         StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
         sender.assertMessagesReceived(1);
         List messages = sender.getMessages();
         assertEquals("message size: " + messages, 1, messages.size());
 
         MimeMessage message = (MimeMessage) messages.get(0);
 
         System.out.println("Created message: " + message);
         Object content = message.getContent();
         assertTrue(content instanceof MimeMultipart);
         MimeMultipart contentMP = (MimeMultipart) content;
         assertEquals(contentMP.getCount(), 3); // first attachement for text body and second for file attached
         Part part = contentMP.getBodyPart(0);
         assertTrue(part.isMimeType("text/plain"));
        Set names = new HashSet();
         part = contentMP.getBodyPart(1);
         String disposition = part.getDisposition();
         assertTrue(disposition.equalsIgnoreCase(Part.ATTACHMENT));
         DataHandler att = part.getDataHandler();
        names.add(att.getName().toLowerCase());
         part = contentMP.getBodyPart(2);
         disposition = part.getDisposition();
         assertTrue(disposition.equalsIgnoreCase(Part.ATTACHMENT));
         att = part.getDataHandler();
        names.add(att.getName().toLowerCase());
        assertTrue(names.contains("example.xml"));
        assertTrue(names.contains("id"));
         assertEquals("subject", "Drink a beer James", message.getSubject());
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/components/email/mimeMail.xml");
     }
 }
