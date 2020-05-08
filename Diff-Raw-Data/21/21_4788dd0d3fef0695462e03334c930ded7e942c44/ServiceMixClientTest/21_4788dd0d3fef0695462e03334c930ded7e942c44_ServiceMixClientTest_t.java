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
 package org.apache.servicemix.client;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.client.ServiceMixClient;
 import org.apache.servicemix.jbi.container.SpringJBIContainer;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.resolver.EndpointResolver;
 import org.apache.servicemix.tck.Receiver;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.InOut;
 import javax.jbi.messaging.NormalizedMessage;
 import javax.xml.namespace.QName;
 import javax.xml.transform.stream.StreamSource;
 
 import java.io.StringReader;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 /**
  * @version $Revision$
  */
 public class ServiceMixClientTest extends TestCase {
     private static final transient Log log = LogFactory.getLog(ServiceMixClientTest.class);
 
     protected AbstractXmlApplicationContext context;
     protected ServiceMixClient client;
     protected Receiver receiver;
 
     protected SourceTransformer transformer = new SourceTransformer();
 
 
     // Send methods
     //-------------------------------------------------------------------------
     public void testSendUsingJbiAPIs() throws Exception {
 
         InOnly exchange = client.createInOnlyExchange();
 
         NormalizedMessage message = exchange.getInMessage();
         message.setProperty("name", "James");
         message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));
 
         QName service = new QName("http://servicemix.org/cheese/", "receiver");
         exchange.setService(service);
         client.send(exchange);
 
         receiver.getMessageList().assertMessagesReceived(1);
     }
 
     public void testSendUsingMapAndPOJOsByServiceName() throws Exception {
 
         Map properties = new HashMap();
         properties.put("name", "James");
 
         QName service = new QName("http://servicemix.org/cheese/", "receiver");
         EndpointResolver resolver = client.createResolverForService(service);
         client.send(resolver, null, properties, "<hello>world</hello>");
 
         receiver.getMessageList().assertMessagesReceived(1);
     }
 
     public void testSendUsingMapAndPOJOsUsingContainerRouting() throws Exception {
 
         ServiceMixClient clientNoRouting = (ServiceMixClient) context.getBean("clientWithRouting");
 
         Map properties = new HashMap();
         properties.put("name", "James");
 
         clientNoRouting.send(null, null, properties, "<hello>world</hello>");
 
         receiver.getMessageList().assertMessagesReceived(1);
     }
 
     public void testSendUsingMapAndPOJOsUsingContainerRoutingWithNoConfiguration() throws Exception {
 
 
         try {
             Map properties = new HashMap();
             properties.put("name", "James");
 
             client.send(null, null, properties, "<hello>world</hello>");
             fail("Should have thrown an exception as we have not wired in any container routing information to this client");
         }
         catch (JBIException e) {
             log.info("Caught expected exception as we have specified no endpoint resolver: " + e);
             assertNotNull(e);
         }
     }
 
 
     // Request methods
     //-------------------------------------------------------------------------
     public void testRequestUsingJbiAPIsByServiceName() throws Exception {
         QName service = new QName("http://servicemix.org/cheese/", "myService");
         assertRequestUsingJBIAPIs(service);
     }
 
     public void testRequestUsingMapAndPOJOsByServiceName() throws Exception {
         QName service = new QName("http://servicemix.org/cheese/", "myService");
         assertRequestUsingMapAndPOJOByServiceName(service);
     }
 
     public void testRequestUsingPOJOWithXStreamMarshaling() throws Exception {
         QName service = new QName("http://servicemix.org/cheese/", "myService");
 
         ServiceMixClient client = (ServiceMixClient) context.getBean("clientWithXStream");
 
         Map properties = new HashMap();
         properties.put("name", "James");
 
         EndpointResolver resolver = client.createResolverForService(service);
         TestBean bean = new TestBean();
         bean.setName("James");
         bean.setLength(12);
         bean.getAddresses().addAll(Arrays.asList(new String[] {"London", "LA"}));
 
         Object response = client.request(resolver, null, properties, bean);
 
         assertNotNull("Should have returned a non-null response!", response);
 
         log.info("Received result: " + response);
     }
 
 
 
     // Implementation methods
     //-------------------------------------------------------------------------
     protected void assertRequestUsingJBIAPIs(QName service) throws Exception {
         InOut exchange = client.createInOutExchange();
 
         NormalizedMessage inMessage = exchange.getInMessage();
         inMessage.setProperty("name", "James");
         inMessage.setContent(new StreamSource(new StringReader("<hello>world</hello>")));
 
         exchange.setService(service);
         boolean answer = client.sendSync(exchange);
         assertTrue("Should have successed", answer);
 
         NormalizedMessage outMessage = exchange.getOutMessage();
         assertNotNull("outMessage is null!", outMessage);
 
         assertEquals("foo header", "hello", outMessage.getProperty("foo"));
         log.info("Received result: " + outMessage.getContent());
         log.info("XML is: " + transformer.toString(outMessage.getContent()));
     }
 
     protected void assertRequestUsingMapAndPOJOByServiceName(QName service) throws Exception {
         Map properties = new HashMap();
         properties.put("name", "James");
 
         EndpointResolver resolver = client.createResolverForService(service);
         Object response = client.request(resolver, null, properties, "<hello>world</hello>");
 
         assertNotNull("Should have returned a non-null response!", response);
         
         log.info("Received result: " + response);
     }
 
     protected void setUp() throws Exception {
         context = createBeanFactory();
         //context.setXmlValidating(false);
 
        client = getClient();
 
         // TODO
         //receiver = (Receiver) getBean("receiver");
 
         SpringJBIContainer jbi = (SpringJBIContainer) getBean("jbi");
         receiver = (Receiver) jbi.getBean("receiver");
         assertNotNull("receiver not found in JBI container", receiver);
     }
    
    protected ServiceMixClient getClient() throws Exception {
        return (ServiceMixClient) getBean("client");
    }
 
     protected void tearDown() throws Exception {
         super.tearDown();
 
         if (context != null) {
             context.close();
         }
     }
 
     protected Object getBean(String name) {
         Object answer = context.getBean(name);
         assertNotNull("Could not find object in Spring for key: " + name, answer);
         return answer;
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/client/example.xml");
     }
 }
