 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
  * contributors by the @authors tag. See the copyright.txt in the
  * distribution for a full listing of individual contributors.
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
 package org.jboss.as.quickstarts.cmt.jts.ejb;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.transaction.*;
 import java.rmi.RemoteException;
 
 @RemoteHome(InvoiceManagerEJBHome.class)
 @Stateless
 @TransactionManagement(TransactionManagementType.BEAN)
 public class InvoiceManagerEJBImpl {
 
     @Resource
     private UserTransaction utx;
 
    @EJB(lookup = "corbaname:iiop:localhost:3628#jts-quickstart/DummyEnlisterEJBImpl")
     private DummyEnlisterEJBHome dummyEnlisterHome;
 
     @Resource(mappedName = "java:/JmsXA")
     private ConnectionFactory connectionFactory;
 
     @Resource(mappedName = "java:/queue/jts-quickstart")
     private Queue queue;
 
     @Resource(lookup = "java:jboss/TransactionManager")
     private TransactionManager transactionManager;
 
     public void createInvoice(String name) throws JMSException, SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException, RemoteException {
 
         utx.begin();
 
         if (name.startsWith("fault:")) {
             if (transactionManager == null)
                 throw new IllegalArgumentException("injected transaction manager is null");
 
             String[] spec = name.split(":");
             String where = spec.length > 1 ? spec[1].toUpperCase() : "";
             String how = spec.length > 2 ? spec[2].toUpperCase() : "";
             ASFailureType type = ASFailureType.NONE;
             // example fault:PREPARE:XA_HEURRB
 
             if ("START".equals(where))
                 type = ASFailureType.XARES_START;
             else if ("END".equals(where))
                 type = ASFailureType.XARES_END;
             else if ("PREPARE".equals(where))
                 type = ASFailureType.XARES_PREPARE;
             else if ("ROLLBACK".equals(where))
                 type = ASFailureType.XARES_ROLLBACK;
             else if ("COMMIT".equals(where))
                 type = ASFailureType.XARES_COMMIT;
             else if ("RECOVER".equals(where))
                 type = ASFailureType.XARES_RECOVER;
             else if ("FORGET".equals(where))
                 type = ASFailureType.XARES_FORGET;
             else if ("BEFORE".equals(where))
                 type = ASFailureType.SYNCH_BEFORE;
             else if ("AFTER".equals(where))
                 type = ASFailureType.SYNCH_AFTER;
 
             ASFailureSpec fault = new ASFailureSpec("fault", ASFailureMode.XAEXCEPTION, how, type);
 
             try {
 
                 transactionManager.getTransaction().enlistResource(new DummyXAResource(fault));
             } catch (RollbackException e) {
                 e.printStackTrace();
             } catch (SystemException e) {
                 e.printStackTrace();
             }
         }
 
         Connection connection = connectionFactory.createConnection();
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         MessageProducer messageProducer = session.createProducer(queue);
         connection.start();
         TextMessage message = session.createTextMessage();
         message.setText("Created invoice for customer named: " + name);
         messageProducer.send(message);
         connection.close();
 
 
         final DummyEnlisterEJB dummyEnlister = dummyEnlisterHome.create();
         dummyEnlister.enlistDummy();
 
         if (":ROLLBACK".equalsIgnoreCase(name))
             utx.rollback();
         else if (":ROLLBACKONLY".equalsIgnoreCase(name))
             utx.setRollbackOnly();
 
         utx.commit();
     }
 }
