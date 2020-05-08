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
 package org.apache.servicemix.jbi.nmr.flow.seda;
 
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.framework.ComponentNameSpace;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.BaseLifeCycle;
 import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
 import org.apache.servicemix.jbi.util.BoundedLinkedQueue;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.MessageExchange;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.ObjectName;
 import javax.resource.spi.work.Work;
 import javax.resource.spi.work.WorkException;
 
 /**
  * A simple Straight through flow
  * 
  * @version $Revision$
  */
 public class SedaQueue extends BaseLifeCycle implements Work {
     private static final Log log = LogFactory.getLog(SedaQueue.class);
     protected SedaFlow flow;
     protected ComponentNameSpace name;
     protected BoundedLinkedQueue queue;
     protected AtomicBoolean started = new AtomicBoolean(false);
     protected AtomicBoolean running = new AtomicBoolean(false);
     protected ObjectName objectName;
     protected String subType;
     protected Thread thread;
 
     /**
      * SedaQueue name
      * 
      * @param name
      */
     public SedaQueue(ComponentNameSpace name) {
         this.name = name;
     }
 
     /**
      * Get the name
      * 
      * @return name
      */
     public String getName() {
         return "SedaQueue." + name.getName();
     }
 
     /**
      * @return Return the name
      */
     public ComponentNameSpace getComponentNameSpace() {
         return this.name;
     }
 
     /**
      * Get the description
      * 
      * @return description
      */
     public String getDescription() {
         return "bounded worker Queue for the NMR";
     }
 
     /**
      * Initialize the Region
      * 
      * @param flow
      * @param capacity
      */
     public void init(SedaFlow flow, int capacity) {
         this.flow = flow;
         this.queue = new BoundedLinkedQueue(capacity);
     }
 
     /**
      * Set the capacity of the Queue
      * 
      * @param value
      */
     public void setCapacity(int value) {
         int oldValue = queue.capacity();
         this.queue.setCapacity(value);
         super.firePropertyChanged("capacity", new Integer(oldValue), new Integer(value));
     }
 
     /**
      * @return the capacity of the Queue
      */
     public int getCapacity() {
         return this.queue.capacity();
     }
 
     /**
      * @return size of the Queue
      */
     public int getSize() {
         return this.queue.size();
     }
 
     /**
      * Enqueue a Packet for processing
      * 
      * @param packet
      * @throws InterruptedException
      */
     public void enqueue(MessageExchange me) throws InterruptedException {
         queue.put(me);
     }
 
     /**
      * start processing
      * 
      * @throws JBIException
      */
     public void start() throws JBIException {
         synchronized (running) {
             try {
                 started.set(true);
                 flow.getBroker().getWorkManager().startWork(this);
                 running.wait();
                 super.start();
             } catch (Exception e) {
                 throw new JBIException("Unable to start queue work", e);
             }
         }
     }
 
     /**
      * stop processing
      * 
      * @throws JBIException
      */
     public void stop() throws JBIException {
         started.set(false);
         if (thread != null && running.get()) {
             try {
                 synchronized (running) {
                     thread.interrupt();
                     running.wait();
                 }
             } catch (Exception e) {
                 log.warn("Error stopping thread", e);
             } finally {
                 thread = null;
             }
         }
         super.stop();
     }
 
     /**
      * shutDown the Queue
      * 
      * @throws JBIException
      */
     public void shutDown() throws JBIException {
         super.shutDown();
     }
 
     /**
      * Asked by the WorkManager to give up
      */
     public void release() {
         log.info("SedaQueue " + name + " asked to be released");
         try {
             shutDown();
         }
         catch (JBIException e) {
             log.warn("Caught an exception shutting down", e);
         }
         flow.release(this);
     }
     
     /**
      * do processing
      */
     public void run() {
         thread = Thread.currentThread();
         synchronized (running) { 
             running.set(true);
             running.notify();
         }
         while (started.get()) {
             final MessageExchangeImpl me;
             try {
                 me = (MessageExchangeImpl) queue.poll(1000);
                 if (me != null) {
                     flow.getBroker().getWorkManager().scheduleWork(new Work() {
                         public void release() {
                         }
                         public void run() {
                             try {
                                 if (log.isDebugEnabled()) {
                                     log.debug(this + " dequeued exchange: " + me);
                                 }
                                 flow.doRouting(me);
                             }
                             catch (Throwable e) {
                                log.error(this + " got error processing " + me, e);
                             }
                         }
                         
                     });
                 }
             }
             catch (InterruptedException e) {
                 if (!started.get()) {
                     break;
                 }
                 log.warn(this + " interrupted", e);
             } catch (WorkException e) {
                 log.error(this + " got error processing exchange", e);
             }
         }
         synchronized (running) { 
             running.set(false);
             running.notify();
         }
     }
 
     /**
      * @return pretty print
      */
     public String toString() {
         return "SedaQueue{" + name + "}";
     }
 
     /**
      * Get an array of MBeanAttributeInfo
      * 
      * @return array of AttributeInfos
      * @throws JMException
      */
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "capacity", "The capacity of the SedaQueue");
         helper.addAttribute(getObjectToManage(), "size", "The size (depth) of the SedaQueue");
         return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
     }
 
     /**
      * @return Returns the objectName.
      */
     public ObjectName getObjectName() {
         return objectName;
     }
 
     /**
      * @param objectName The objectName to set.
      */
     public void setObjectName(ObjectName objectName) {
         this.objectName = objectName;
     }
 }
