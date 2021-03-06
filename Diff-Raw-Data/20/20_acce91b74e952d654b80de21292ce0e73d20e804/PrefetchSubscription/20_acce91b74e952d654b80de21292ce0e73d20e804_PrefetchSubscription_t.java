 /**
  * 
  * Copyright 2005-2006 The Apache Software Foundation
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.apache.activemq.broker.region;
 
 import org.apache.activemq.broker.Broker;
 import org.apache.activemq.broker.ConnectionContext;
 import org.apache.activemq.broker.region.policy.DeadLetterStrategy;
 import org.apache.activemq.command.ActiveMQDestination;
 import org.apache.activemq.command.ConsumerInfo;
 import org.apache.activemq.command.Message;
 import org.apache.activemq.command.MessageAck;
 import org.apache.activemq.command.MessageDispatch;
 import org.apache.activemq.command.MessageDispatchNotification;
 import org.apache.activemq.command.MessageId;
 import org.apache.activemq.transaction.Synchronization;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import javax.jms.InvalidSelectorException;
 import javax.jms.JMSException;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 /**
  * A subscription that honors the pre-fetch option of the ConsumerInfo.
  * 
  * @version $Revision: 1.15 $
  */
 abstract public class PrefetchSubscription extends AbstractSubscription{
     
     static private final Log log=LogFactory.getLog(PrefetchSubscription.class);
     final protected LinkedList matched=new LinkedList();
     final protected LinkedList dispatched=new LinkedList();
     protected int delivered=0;
     int preLoadLimit=1024*100;
     int preLoadSize=0;
     boolean dispatching=false;
     
     long enqueueCounter;
     long dispatchCounter;
     
     public PrefetchSubscription(Broker broker,ConnectionContext context,ConsumerInfo info)
                     throws InvalidSelectorException{
         super(broker,context,info);
     }
 
     synchronized public void add(MessageReference node) throws Throwable{
         enqueueCounter++;
         if(!isFull()&&!isSlaveBroker()){
             dispatch(node);
         }else{
             synchronized(matched){
                 matched.addLast(node);
             }
         }
     }
 
     public void processMessageDispatchNotification(MessageDispatchNotification mdn){
         synchronized(matched){
             for(Iterator i=matched.iterator();i.hasNext();){
                 MessageReference node=(MessageReference) i.next();
                 if(node.getMessageId().equals(mdn.getMessageId())){
                     i.remove();
                     try{
                         MessageDispatch md=createMessageDispatch(node,node.getMessage());
                         dispatched.addLast(node);
                         incrementPreloadSize(node.getSize());
                         node.decrementReferenceCount();
                     }catch(Exception e){
                         log.error("Problem processing MessageDispatchNotification: "+mdn,e);
                     }
                     break;
                 }
             }
         }
     }
 
     synchronized public void acknowledge(final ConnectionContext context,final MessageAck ack) throws Throwable{
         // Handle the standard acknowledgment case.
         boolean wasFull=isFull();
         if(ack.isStandardAck()){
             // Acknowledge all dispatched messages up till the message id of the acknowledgment.
             int index=0;
             boolean inAckRange=false;
             for(Iterator iter=dispatched.iterator();iter.hasNext();){
                 final MessageReference node=(MessageReference) iter.next();
                 MessageId messageId=node.getMessageId();
                 if(ack.getFirstMessageId()==null||ack.getFirstMessageId().equals(messageId)){
                     inAckRange=true;
                 }
                 if(inAckRange){
                     // Don't remove the nodes until we are committed.
                     if(!context.isInTransaction()){
                         iter.remove();
                     }else{
                         // setup a Synchronization to remove nodes from the dispatched list.
                         context.getTransaction().addSynchronization(new Synchronization(){
                             public void afterCommit() throws Throwable{
                                 synchronized(PrefetchSubscription.this){
                                    dispatched.remove(node);
                                    delivered--;
                                 }
                             }
                         });
                     }
                     index++;
                     acknowledge(context,ack,node);
                     if(ack.getLastMessageId().equals(messageId)){
                         if(context.isInTransaction())
                             delivered=Math.max(delivered,index+1);
                         else
                             delivered=Math.max(0,delivered-(index+1));
                         if(wasFull&&!isFull()){
                             dispatchMatched();
                         }
                         return;
                     }else{
                         // System.out.println("no match: "+ack.getLastMessageId()+","+messageId);
                     }
                 }
             }
             log.info("Could not correlate acknowledgment with dispatched message: "+ack);
         }else if(ack.isDeliveredAck()){
             // Message was delivered but not acknowledged: update pre-fetch counters.
             // Acknowledge all dispatched messages up till the message id of the acknowledgment.
             int index=0;
             for(Iterator iter=dispatched.iterator();iter.hasNext();index++){
                 final MessageReference node=(MessageReference) iter.next();
                 if(ack.getLastMessageId().equals(node.getMessageId())){
                     delivered=Math.max(delivered,index+1);
                     if(wasFull&&!isFull()){
                         dispatchMatched();
                     }
                     return;
                 }
             }
             throw new JMSException("Could not correlate acknowledgment with dispatched message: "+ack);
         }else if(ack.isPoisonAck()){
             // TODO: what if the message is already in a DLQ???
             // Handle the poison ACK case: we need to send the message to a DLQ
             if(ack.isInTransaction())
                 throw new JMSException("Poison ack cannot be transacted: "+ack);
             // Acknowledge all dispatched messages up till the message id of the acknowledgment.
             int index=0;
             boolean inAckRange=false;
             for(Iterator iter=dispatched.iterator();iter.hasNext();){
                 final MessageReference node=(MessageReference) iter.next();
                 MessageId messageId=node.getMessageId();
                 if(ack.getFirstMessageId()==null||ack.getFirstMessageId().equals(messageId)){
                     inAckRange=true;
                 }
                 if(inAckRange){
                     // Send the message to the DLQ
                     node.incrementReferenceCount();
                     try{
                         Message message=node.getMessage();
                         if(message!=null){
                             // The original destination and transaction id do not get filled when the message is first
                             // sent,
                             // it is only populated if the message is routed to another destination like the DLQ
                             if(message.getOriginalDestination()!=null)
                                 message.setOriginalDestination(message.getDestination());
                             if(message.getOriginalTransactionId()!=null)
                                 message.setOriginalTransactionId(message.getTransactionId());
                             DeadLetterStrategy deadLetterStrategy=node.getRegionDestination().getDeadLetterStrategy();
                             ActiveMQDestination deadLetterDestination=deadLetterStrategy.getDeadLetterQueueFor(message
                                             .getDestination());
                             message.setDestination(deadLetterDestination);
                             message.setTransactionId(null);
                             message.evictMarshlledForm();
                             boolean originalFlowControl=context.isProducerFlowControl();
                             try{
                                 context.setProducerFlowControl(false);
                                 context.getBroker().send(context,message);
                             }finally{
                                 context.setProducerFlowControl(originalFlowControl);
                             }
                         }
                     }finally{
                         node.decrementReferenceCount();
                     }
                     iter.remove();
                     index++;
                     acknowledge(context,ack,node);
                     if(ack.getLastMessageId().equals(messageId)){
                         delivered=Math.max(0,delivered-(index+1));
                         if(wasFull&&!isFull()){
                             dispatchMatched();
                         }
                         return;
                     }
                 }
             }
             throw new JMSException("Could not correlate acknowledgment with dispatched message: "+ack);
         }
         throw new JMSException("Invalid acknowledgment: "+ack);
     }
 
     protected boolean isFull(){
         return dispatched.size()-delivered>=info.getPrefetchSize()||preLoadSize>preLoadLimit;
     }
     
     public int pending(){
         return matched.size() - dispatched.size();
     }
     
     public int dispatched(){
         return dispatched.size();
     }
     
     public int delivered(){
         return delivered;
     }
 
     protected void dispatchMatched() throws IOException{
         if(!dispatching){
             dispatching=true;
             try{
                 for(Iterator iter=matched.iterator();iter.hasNext()&&!isFull();){
                     MessageReference node=(MessageReference) iter.next();
                     iter.remove();
                     dispatch(node);
                 }
             }finally{
                 dispatching=false;
             }
         }
     }
 
     private void dispatch(final MessageReference node) throws IOException{
         node.incrementReferenceCount();
         final Message message=node.getMessage();
         if(message==null){
             return;
         }
         // Make sure we can dispatch a message.
         if(canDispatch(node)&&!isSlaveBroker()){
             dispatchCounter++;
             MessageDispatch md=createMessageDispatch(node,message);
             dispatched.addLast(node);            
             incrementPreloadSize(node.getMessage().getSize());
             if(info.isDispatchAsync()){
                 md.setConsumer(new Runnable(){
                     public void run(){
                         // Since the message gets queued up in async dispatch, we don't want to
                         // decrease the reference count until it gets put on the wire.
                         onDispatch(node,message);
                     }
                 });
                 context.getConnection().dispatchAsync(md);
             }else{
                 context.getConnection().dispatchSync(md);
                 onDispatch(node,message);
             }
             // The onDispatch() does the node.decrementReferenceCount();
         }else{
             // We were not allowed to dispatch that message (an other consumer grabbed it before we did)
             node.decrementReferenceCount();
         }
     }
 
     synchronized private void onDispatch(final MessageReference node,final Message message){
         boolean wasFull=isFull();
         decrementPreloadSize(message.getSize());
         node.decrementReferenceCount();
         if(node.getRegionDestination()!=null){
             node.getRegionDestination().getDestinationStatistics().onMessageDequeue(message);
             context.getConnection().getStatistics().onMessageDequeue(message);
             if(wasFull&&!isFull()){
                 try{
                     dispatchMatched();
                 }catch(IOException e){
                     context.getConnection().serviceException(e);
                 }
             }
         }
     }
 
     private int incrementPreloadSize(int size){
         preLoadSize+=size;
         return preLoadSize;
     }
 
     private int decrementPreloadSize(int size){
         preLoadSize-=size;
         return preLoadSize;
     }
 
     /**
      * @param node
      * @param message
      *            TODO
      * @return
      */
     protected MessageDispatch createMessageDispatch(MessageReference node,Message message){
         MessageDispatch md=new MessageDispatch();
         md.setConsumerId(info.getConsumerId());
         md.setDestination(node.getRegionDestination().getActiveMQDestination());
         md.setMessage(message);
         md.setRedeliveryCounter(node.getRedeliveryCounter());
         return md;
     }
 
     /**
      * Use when a matched message is about to be dispatched to the client.
      * 
      * @param node
      * @return false if the message should not be dispatched to the client (another sub may have already dispatched it
      *         for example).
      * @throws IOException 
      */
     abstract protected boolean canDispatch(MessageReference node) throws IOException;
 
     /**
      * Used during acknowledgment to remove the message.
      * 
      * @throws IOException
      */
     protected void acknowledge(ConnectionContext context,final MessageAck ack,final MessageReference node)
                     throws IOException{}
 
 
     public long getDispatchCounter() {
         return dispatchCounter;
     }
 
     public long getEnqueueCounter() {
         return enqueueCounter;
     }
 }
