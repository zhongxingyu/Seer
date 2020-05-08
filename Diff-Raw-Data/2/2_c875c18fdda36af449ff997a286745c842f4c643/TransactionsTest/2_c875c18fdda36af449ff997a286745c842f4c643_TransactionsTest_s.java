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
 package org.apache.servicemix.common;
 
 import javax.jbi.component.Component;
 import javax.jbi.component.ComponentContext;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.InOnly;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.transaction.Status;
 import javax.transaction.TransactionManager;
 import javax.xml.namespace.QName;
 
 import junit.framework.TestCase;
 
 import org.apache.activemq.broker.BrokerService;
 import org.apache.geronimo.transaction.context.GeronimoTransactionManager;
 import org.apache.geronimo.transaction.context.TransactionContextManager;
 import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
 import org.apache.geronimo.transaction.manager.XidFactoryImpl;
 import org.apache.servicemix.client.DefaultServiceMixClient;
 import org.apache.servicemix.client.ServiceMixClient;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 import org.apache.servicemix.jbi.nmr.flow.Flow;
 import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;
 import org.apache.servicemix.jbi.nmr.flow.seda.SedaFlow;
 
 public class TransactionsTest extends TestCase {
 
     private JBIContainer jbi;
     private BrokerService broker;
     private TransactionManagerImpl exTransactionManager;
     private TransactionContextManager transactionContextManager;
     private TransactionManager txManager;
     private Component component;
     private ServiceMixClient client;
     private Exception exceptionToThrow;
     private boolean exceptionShouldRollback;
     
     protected void setUp() throws Exception {
         exceptionToThrow = null;
         exceptionShouldRollback = false;
         
         broker = new BrokerService();
         broker.setPersistent(false);
         broker.addConnector("tcp://localhost:61616");
         broker.start();
         
         exTransactionManager = new TransactionManagerImpl(600, new XidFactoryImpl(), null, null);
         transactionContextManager = new TransactionContextManager(exTransactionManager, exTransactionManager);
         txManager = (TransactionManager) new GeronimoTransactionManager(transactionContextManager);
         
         JCAFlow jcaFlow = new JCAFlow();
         jcaFlow.setTransactionContextManager(transactionContextManager);
         
         jbi = new JBIContainer();
         jbi.setFlows(new Flow[] { new SedaFlow(), jcaFlow });
         jbi.setEmbedded(true);
         jbi.setUseMBeanServer(false);
         jbi.setTransactionManager(txManager);
         jbi.setAutoEnlistInTransaction(true);
         jbi.init();
         jbi.start();
         component = new TestComponent();
         jbi.activateComponent(component, "test");
         client = new DefaultServiceMixClient(jbi);
     }
     
     protected void tearDown() throws Exception {
         jbi.shutDown();
         broker.stop();
     }
     
     public void testTxOkAsync() throws Exception {
         txManager.begin();
         InOnly me = client.createInOnlyExchange();
         me.setService(new QName("service"));
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         client.send(me);
         assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
         assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
         txManager.commit();
         me = (InOnly) client.receive(1000);
         assertNotNull(me);
         assertEquals(ExchangeStatus.DONE, me.getStatus());
     }
     
     public void testTxOkSync() throws Exception {
         txManager.begin();
         InOnly me = client.createInOnlyExchange();
         me.setService(new QName("service"));
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         boolean ok = client.sendSync(me, 1000);
         assertTrue(ok);
         assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
         assertEquals(ExchangeStatus.DONE, me.getStatus());
         txManager.commit();
     }
     
     public void testTxExceptionAsync() throws Exception {
         exceptionToThrow = new Exception("Business exception");
         txManager.begin();
         InOnly me = client.createInOnlyExchange();
         me.setService(new QName("service"));
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         client.send(me);
         assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
         assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
         txManager.commit();
         me = (InOnly) client.receive(1000);
         assertNotNull(me);
         assertEquals(ExchangeStatus.ERROR, me.getStatus());
     }
     
     public void testTxExceptionSync() throws Exception {
         exceptionToThrow = new Exception("Business exception");
         txManager.begin();
         InOnly me = client.createInOnlyExchange();
         me.setService(new QName("service"));
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         boolean ok = client.sendSync(me, 1000);
         assertTrue(ok);
         assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
         assertEquals(ExchangeStatus.ERROR, me.getStatus());
         txManager.commit();
     }
     
     public void testTxExceptionRollbackAsync() throws Exception {
         exceptionToThrow = new Exception("Business exception");
         exceptionShouldRollback = true;
         txManager.begin();
         InOnly me = client.createInOnlyExchange();
         me.setService(new QName("service"));
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         client.send(me);
         assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
         assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
         txManager.commit();
         // if we always mark the transaction as rollback,
         // the exchange will be redelivered by the JCA flow,
         // until it is discarded, so we will never receive
         // it back
         me = (InOnly) client.receive(1000);
         assertNull(me);
     }
     
     public void testTxExceptionRollbackSync() throws Exception {
         exceptionToThrow = new RuntimeException("Runtime exception");
         exceptionShouldRollback = true;
         txManager.begin();
         InOnly me = client.createInOnlyExchange();
         me.setService(new QName("service"));
         me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
         boolean ok = client.sendSync(me, 1000);
         assertTrue(ok);
         assertEquals(Status.STATUS_MARKED_ROLLBACK, txManager.getStatus());
         assertEquals(ExchangeStatus.ERROR, me.getStatus());
        txManager.commit();
     }
     
     private class TestComponent extends BaseComponent {
         public TestComponent() {
             super();
         }
         protected BaseLifeCycle createLifeCycle() {
             return new TestLifeCycle();
         }
         
         protected class TestLifeCycle extends BaseLifeCycle {
             protected ServiceUnit su;
             public TestLifeCycle() {
                 super(TestComponent.this);
             }
             protected void doInit() throws Exception {
                 super.doInit();
                 su = new ServiceUnit();
                 su.setComponent(component);
                 TestEndpoint ep = new TestEndpoint();
                 ep.setService(new QName("service"));
                 ep.setEndpoint("endpoint");
                 ep.setServiceUnit(su);
                 su.addEndpoint(ep);
                 getRegistry().registerServiceUnit(su);
             }
             protected void doStart() throws Exception {
                 super.doStart();
                 su.start();
             }
             protected void doStop() throws Exception {
                 super.doStop();
                 su.stop();
             }
             protected boolean exceptionShouldRollbackTx(Exception e) {
                 return exceptionShouldRollback;
             }
         }
         
         protected class TestEndpoint extends Endpoint implements ExchangeProcessor {
             protected ServiceEndpoint activated;
             public void activate() throws Exception {
                 ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
                 activated = ctx.activateEndpoint(service, endpoint);
             }
             public void deactivate() throws Exception {
                 ComponentContext ctx = this.serviceUnit.getComponent().getComponentContext();
                 ctx.deactivateEndpoint(activated);
                 activated = null;
             }
             public ExchangeProcessor getProcessor() {
                 return this;
             }
             public Role getRole() {
                 return Role.PROVIDER;
             }
             public void process(MessageExchange exchange) throws Exception {
                 if (exceptionToThrow != null) {
                     throw exceptionToThrow;
                 }
                 exchange.setStatus(ExchangeStatus.DONE);
                 getComponentContext().getDeliveryChannel().send(exchange);
             }
             public void start() throws Exception {
             }
             public void stop() throws Exception {
             }
         }
     }
     
     
 }
