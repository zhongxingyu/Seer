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
 import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.command.ActiveMQQueue;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.jencks.factory.TransactionContextManagerFactoryBean;
import org.jencks.factory.TransactionManagerFactoryBean;
 import org.jencks.factory.WorkManagerFactoryBean;
 import org.logicblaze.lingo.jms.Requestor;
 import org.logicblaze.lingo.jms.JmsProducerConfig;
 import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;
 
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.Message;
 import javax.resource.spi.work.Work;
 
 /**
  * @version $Revision$
  */
 public class JMSClient implements Work {
     
     private static ConnectionFactory factory;
     private static CountDownLatch latch;
     private static Requestor requestor;
     
     /**
      * main ...
      * 
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         System.out.println("Connecting to JMS server.");
         factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
         Destination inQueue = new ActiveMQQueue("demo.org.servicemix.source");
         Destination outQueue = new ActiveMQQueue("demo.org.servicemix.output" + (int)(1000*Math.random()));
         requestor = MultiplexingRequestor.newInstance(factory, new JmsProducerConfig(), inQueue, outQueue); 
         
         if (args.length == 0) {
             new JMSClient().run();
         } else {
             int nb = Integer.parseInt(args[0]);
             int th = 30;
             if (args.length > 1) {
                 th = Integer.parseInt(args[1]);
             }
             GeronimoWorkManager wm = createWorkManager(th);
             latch = new CountDownLatch(nb);
             for (int i = 0; i < nb; i++) {
                 wm.scheduleWork(new JMSClient());
             }
             latch.await();
             wm.doStop();
         }
         System.out.println("Closing.");
         requestor.close();
     }
     
     protected static GeronimoWorkManager createWorkManager(int poolSize) throws Exception {
        TransactionManagerFactoryBean tmfb = new TransactionManagerFactoryBean();
        tmfb.afterPropertiesSet();
        TransactionContextManagerFactoryBean tcmfb = new TransactionContextManagerFactoryBean();
        tcmfb.setTransactionManager((ExtendedTransactionManager) tmfb.getObject());
        tcmfb.afterPropertiesSet();
         WorkManagerFactoryBean wmfb = new WorkManagerFactoryBean();
        wmfb.setTransactionContextManager((TransactionContextManager) tcmfb.getObject());
         wmfb.setThreadPoolSize(poolSize);
         wmfb.afterPropertiesSet();
         GeronimoWorkManager wm = wmfb.getWorkManager();
         return wm;
     }
 
     public void run() {
         try {
             System.out.println("Sending request.");
             Message out = requestor.getSession().createMapMessage();
             out.setStringProperty("ssn", "012-24532-53254");
             out.setDoubleProperty("amount", Math.random() * 100000);
             out.setIntProperty("duration", (int) Math.random() * 48);
             Message in = requestor.request(null, out);
             if (in == null) {
                 System.out.println("Response timed out.");
             }
             else {
                 System.out.println("Response was: " + in.getDoubleProperty("rate") + " from " + in.getStringProperty("bank"));
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (latch != null) {
                 latch.countDown();
             }
         }
     }
 
     public void release() {
     }
 }
