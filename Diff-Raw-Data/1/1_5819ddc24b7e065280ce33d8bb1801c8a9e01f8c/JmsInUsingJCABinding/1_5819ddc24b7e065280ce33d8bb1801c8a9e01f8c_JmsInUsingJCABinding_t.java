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
 package org.apache.servicemix.components.jms;
 
 import javax.jbi.JBIException;
 import javax.resource.spi.ActivationSpec;
 import javax.transaction.TransactionManager;
 
 import org.jencks.JCAConnector;
 import org.jencks.JCAContainer;
 import org.jencks.SingletonEndpointFactory;
 
 /**
  * Uses the JCA Container for better inbound subscription.
  *
  * @version $Revision$
  */
 public class JmsInUsingJCABinding extends JmsInBinding {
 
     private JCAContainer jcaContainer;
     private ActivationSpec activationSpec;
     private TransactionManager transactionManager;
     private JCAConnector jcaConnector;
 
     protected void init() throws JBIException {
         if (jcaContainer == null) {
             throw new IllegalArgumentException("Must specify a jcaContainer property");
         }
         if (activationSpec == null) {
             throw new IllegalArgumentException("Must specify an activationSpec property");
         }
         jcaConnector = jcaContainer.addConnector();
         jcaConnector.setActivationSpec(activationSpec);
         if (transactionManager == null) {
         	transactionManager = (TransactionManager) getContext().getTransactionManager();
         }
         if (transactionManager != null) {
             jcaConnector.setTransactionManager(transactionManager);
         }
         jcaConnector.setEndpointFactory(new SingletonEndpointFactory(this, transactionManager));
         try {
         	jcaConnector.afterPropertiesSet();
            jcaConnector.start();
         } catch (Exception e) {
         	throw new JBIException("Unable to start jca connector", e);
         }
     }
 
     public JCAContainer getJcaContainer() {
         return jcaContainer;
     }
 
     public void setJcaContainer(JCAContainer jcaContainer) {
         this.jcaContainer = jcaContainer;
     }
 
     public ActivationSpec getActivationSpec() {
         return activationSpec;
     }
 
     public void setActivationSpec(ActivationSpec activationSpec) {
         this.activationSpec = activationSpec;
     }
 
     public TransactionManager getTransactionManager() {
         return transactionManager;
     }
 
     public void setTransactionManager(TransactionManager transactionManager) {
         this.transactionManager = transactionManager;
     }
 
     public JCAConnector getJcaConnector() {
         return jcaConnector;
     }
 }
