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
 package org.apache.servicemix.jbi.messaging;
 
 import java.util.ArrayList;
import java.util.List;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.InOut;
 import javax.jbi.messaging.MessageExchange;
import javax.xml.namespace.QName;
 
 import junit.framework.TestCase;
import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.api.ServiceMixClient;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 
 public class ExchangeListenerTest extends TestCase {
 
     protected JBIContainer container;
 
     @Override
     protected void setUp() throws Exception {
         // Create jbi container
         container = new JBIContainer();
         container.setEmbedded(true);
         container.setFlowName("st");
         container.init();
         container.start();
     }
 
     public void test() throws JBIException {
         TestListener listener = new TestListener();
         container.addListener(listener);
 
         EchoComponent echo = new EchoComponent(new QName("echo"), "endpoint");
         container.activateComponent(echo, "echo");
 
         ServiceMixClient c = container.getClientFactory().createClient();
         InOut me = c.createInOutExchange();
         me.getInMessage().setContent(new StringSource("<hello/>"));
         me.setService(new QName("echo"));
         c.sendSync(me);
         c.done(me);
 
         assertEquals(6, listener.events.size());
     }
 
     public static class TestListener implements ExchangeListener {
 
        List<ExchangeEvent> events = new ArrayList<ExchangeEvent>();
 
         public void exchangeSent(ExchangeEvent e) {
            System.err.println((e.getType() == ExchangeEvent.EXCHANGE_ACCEPTED ? "accepted" : "sent    ") 
                                + " " + e.getExchange().getStatus() + " "
                                + (e.getExchange().getRole() == MessageExchange.Role.CONSUMER ? "consumer" : "provider"));
             events.add(e);
         }
 
         public void exchangeAccepted(ExchangeEvent e) {
            System.err.println((e.getType() == ExchangeEvent.EXCHANGE_ACCEPTED ? "accepted" : "sent    ")
                                + " " + e.getExchange().getStatus() + " "
                                + (e.getExchange().getRole() == MessageExchange.Role.CONSUMER ? "consumer" : "provider"));
             events.add(e);
         }
     }
 }
