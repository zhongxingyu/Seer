 /*
  * (C) 2007-2012 Alibaba Group Holding Limited.
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
  * Authors:
  *   wuhua <wq163@163.com> , boyan <killme2008@gmail.com>
  */
 package com.taobao.metamorphosis.client.consumer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.RejectedExecutionException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.taobao.gecko.core.util.ConcurrentHashSet;
 import com.taobao.gecko.service.exception.NotifyRemotingException;
 import com.taobao.metamorphosis.Message;
 import com.taobao.metamorphosis.MessageAccessor;
 import com.taobao.metamorphosis.cluster.Partition;
 import com.taobao.metamorphosis.consumer.ConsumerMessageFilter;
 import com.taobao.metamorphosis.exception.InvalidMessageException;
 import com.taobao.metamorphosis.exception.MetaClientException;
 import com.taobao.metamorphosis.utils.MetaStatLog;
 import com.taobao.metamorphosis.utils.StatConstants;
 
 
 /**
  * Ϣץȡʵ
  * 
  * @author boyan(boyan@taobao.com)
  * @date 2011-9-13
  * 
  */
 public class SimpleFetchManager implements FetchManager {
 
     private volatile boolean shutdown = false;
 
     private Thread[] fetchThreads;
 
     private FetchRequestRunner[] requestRunners;
 
     private volatile int fetchRequestCount;
 
     private FetchRequestQueue requestQueue;
 
     private final ConsumerConfig consumerConfig;
 
     private static final ThreadLocal<TopicPartitionRegInfo> currentTopicRegInfo =
             new ThreadLocal<TopicPartitionRegInfo>();
 
     private final InnerConsumer consumer;
 
     public static final Byte PROCESSED = (byte) 1;
 
     private final int CACAHE_SIZE = Integer.parseInt(System
         .getProperty("metaq.consumer.message.ids.cache.size", "1024"));
 
    private final ConcurrentLRUHashMap<String, Byte> messageIdCache = new ConcurrentLRUHashMap<String, Byte>(
             this.CACAHE_SIZE);
 
 
     public SimpleFetchManager(final ConsumerConfig consumerConfig, final InnerConsumer consumer) {
         super();
         this.consumerConfig = consumerConfig;
         this.consumer = consumer;
     }
 
 
     /**
      * Returns current thread processing message's TopicPartitionRegInfo.
      * 
      * @since 1.4.6
      * @return
      */
     public static TopicPartitionRegInfo currentTopicRegInfo() {
         return currentTopicRegInfo.get();
     }
 

     @Override
     public int getFetchRequestCount() {
         return this.fetchRequestCount;
     }
 
 
     @Override
     public boolean isShutdown() {
         return this.shutdown;
     }
 
 
     @Override
     public void stopFetchRunner() throws InterruptedException {
         this.shutdown = true;
         // ж
         if (this.fetchThreads != null) {
             for (int i = 0; i < this.fetchThreads.length; i++) {
                 Thread thread = this.fetchThreads[i];
                 FetchRequestRunner runner = this.requestRunners[i];
                 if (thread != null) {
                     runner.shutdown();
                     thread.interrupt();
                     runner.interruptExecutor();
                     try {
                         thread.join(100);
                     }
                     catch (final InterruptedException e) {
                         Thread.currentThread().interrupt();
                     }
                 }
 
             }
         }
         // // ȴ
         // if (this.requestQueue != null) {
         // while (this.requestQueue.size() != this.fetchRequestCount) {
         // Thread.sleep(50);
         // }
         // }
         this.fetchRequestCount = 0;
     }
 
 
     @Override
     public void resetFetchState() {
         this.requestQueue = new FetchRequestQueue();
         this.fetchThreads = new Thread[this.consumerConfig.getFetchRunnerCount()];
         this.requestRunners = new FetchRequestRunner[this.consumerConfig.getFetchRunnerCount()];
         for (int i = 0; i < this.fetchThreads.length; i++) {
             FetchRequestRunner runner = new FetchRequestRunner();
             this.requestRunners[i] = runner;
             this.fetchThreads[i] = new Thread(runner);
             this.fetchThreads[i].setName(this.consumerConfig.getGroup() + "Fetch-Runner-" + i);
         }
 
     }
 
 
     @Override
     public void startFetchRunner() {
         // ĿֹͣʱҪ
         this.fetchRequestCount = this.requestQueue.size();
         this.shutdown = false;
         for (final Thread thread : this.fetchThreads) {
             thread.start();
         }
 
     }
 
 
     @Override
     public void addFetchRequest(final FetchRequest request) {
         this.requestQueue.offer(request);
 
     }
 
 
     FetchRequest takeFetchRequest() throws InterruptedException {
         return this.requestQueue.take();
     }
 
     static final Log log = LogFactory.getLog(SimpleFetchManager.class);
 
     class FetchRequestRunner implements Runnable {
 
         private static final int DELAY_NPARTS = 10;
 
         private volatile boolean stopped = false;
 
 
         void shutdown() {
             this.stopped = true;
         }
 
 
         @Override
         public void run() {
             while (!this.stopped) {
                 try {
                     final FetchRequest request = SimpleFetchManager.this.requestQueue.take();
                     this.processRequest(request);
                 }
                 catch (final InterruptedException e) {
                     // takeӦжϣ
                 }
 
             }
         }
 
 
         void processRequest(final FetchRequest request) {
             try {
                 final MessageIterator iterator = SimpleFetchManager.this.consumer.fetch(request, -1, null);
                 final MessageListener listener =
                         SimpleFetchManager.this.consumer.getMessageListener(request.getTopic());
                 final ConsumerMessageFilter filter =
                         SimpleFetchManager.this.consumer.getMessageFilter(request.getTopic());
                 this.notifyListener(request, iterator, listener, filter, SimpleFetchManager.this.consumer
                     .getConsumerConfig().getGroup());
             }
             catch (final MetaClientException e) {
                 this.updateDelay(request);
                 this.LogAddRequest(request, e);
             }
             catch (final InterruptedException e) {
                 this.reAddFetchRequest2Queue(request);
             }
             catch (final Throwable e) {
                 this.updateDelay(request);
                 this.LogAddRequest(request, e);
             }
         }
 
         private long lastLogNoConnectionTime;
 
 
         private void LogAddRequest(final FetchRequest request, final Throwable e) {
             if (e instanceof MetaClientException && e.getCause() instanceof NotifyRemotingException
                     && e.getMessage().contains("޿")) {
                 // 30ӡһ
                 final long now = System.currentTimeMillis();
                 if (this.lastLogNoConnectionTime <= 0 || now - this.lastLogNoConnectionTime > 30000) {
                     log.error("ȡϢʧ,topic=" + request.getTopic() + ",partition=" + request.getPartition(), e);
                     this.lastLogNoConnectionTime = now;
                 }
             }
             else {
                 log.error("ȡϢʧ,topic=" + request.getTopic() + ",partition=" + request.getPartition(), e);
             }
             this.reAddFetchRequest2Queue(request);
         }
 
 
         private void getOffsetAddRequest(final FetchRequest request, final InvalidMessageException e) {
             try {
                 final long newOffset = SimpleFetchManager.this.consumer.offset(request);
                 request.resetRetries();
                 if (!this.stopped) {
                     request.setOffset(newOffset, request.getLastMessageId(), request.getPartitionObject().isAutoAck());
                 }
             }
             catch (final MetaClientException ex) {
                 log.error("ѯoffsetʧ,topic=" + request.getTopic() + ",partition=" + request.getPartition(), e);
             }
             finally {
                 this.reAddFetchRequest2Queue(request);
             }
         }
 
 
         public void interruptExecutor() {
             for (Thread thread : this.executorThreads) {
                 if (!thread.isInterrupted()) {
                     thread.interrupt();
                 }
             }
         }
 
         private final ConcurrentHashSet<Thread> executorThreads = new ConcurrentHashSet<Thread>();
 
 
         private void notifyListener(final FetchRequest request, final MessageIterator it,
                 final MessageListener listener, final ConsumerMessageFilter filter, final String group) {
             if (listener != null) {
                 if (listener.getExecutor() != null) {
                     try {
                         listener.getExecutor().execute(new Runnable() {
                             @Override
                             public void run() {
                                 Thread currentThread = Thread.currentThread();
                                 FetchRequestRunner.this.executorThreads.add(currentThread);
                                 try {
                                     FetchRequestRunner.this.receiveMessages(request, it, listener, filter, group);
                                 }
                                 finally {
                                     FetchRequestRunner.this.executorThreads.remove(currentThread);
                                 }
                             }
                         });
                     }
                     catch (final RejectedExecutionException e) {
                         log.error(
                             "MessageListener̳߳طæ޷Ϣ,topic=" + request.getTopic() + ",partition="
                                     + request.getPartition(), e);
                         this.reAddFetchRequest2Queue(request);
                     }
 
                 }
                 else {
                     this.receiveMessages(request, it, listener, filter, group);
                 }
             }
         }
 
 
         private void reAddFetchRequest2Queue(final FetchRequest request) {
             if (!this.stopped) {
                 SimpleFetchManager.this.addFetchRequest(request);
             }
         }
 
 
         /**
          * Ϣ̣<br>
          * <ul>
          * <li>1.жǷϢԴûϢݵԴжǷҪmaxSize</li>
          * <li>2.жϢǷԶΣ趨ϢߡϢڱԻ߽notifyͶ</li>
          * <li>3.Ϣ̣ǷԶackд:
          * <ul>
          * <li>(1)ϢԶackѷ쳣޸offsetӳѵȴ</li>
          * <li>(2)ϢԶackoffset</li>
          * <li>(3)ϢԶackackoffset޸Ϊtmp offsettmp offset</li>
          * <li>(4)ϢԶackrollbackoffsettmp offset</li>
          * <li>(5)ϢԶackackҲrollbackoffsettmp offset</li>
          * </ul>
          * </li>
          * </ul>
          * 
          * @param request
          * @param it
          * @param listener
          */
         private void receiveMessages(final FetchRequest request, final MessageIterator it,
                 final MessageListener listener, final ConsumerMessageFilter filter, final String group) {
             if (it != null && it.hasNext()) {
                 if (this.processWhenRetryTooMany(request, it)) {
                     return;
                 }
                 final Partition partition = request.getPartitionObject();
                 if (this.processReceiveMessage(request, it, listener, filter, partition, group)) {
                     return;
                 }
                 this.postReceiveMessage(request, it, partition);
             }
             else {
 
                 // Զ޷ȡݣҪmaxSize
                 if (SimpleFetchManager.this.isRetryTooManyForIncrease(request) && it != null && it.getDataLength() > 0) {
                     request.increaseMaxSize();
                     log.warn("棬" + request.getRetries() + "޷ȡtopic=" + request.getTopic() + ",partition="
                             + request.getPartitionObject() + "ϢmaxSize=" + request.getMaxSize() + " Bytes");
                 }
 
                 // һҪжitǷΪnull,βʱ(null)ҲRetries,ᵼԺϢʱrecover
                 if (it != null) {
                     request.incrementRetriesAndGet();
                 }
 
                 this.updateDelay(request);
                 this.reAddFetchRequest2Queue(request);
             }
         }
 
 
         /**
          * ǷҪĴ
          * 
          * @param request
          * @param it
          * @param listener
          * @param partition
          * @return
          */
         private boolean processReceiveMessage(final FetchRequest request, final MessageIterator it,
                 final MessageListener listener, final ConsumerMessageFilter filter, final Partition partition,
                 final String group) {
             int count = 0;
             List<Long> inTransactionMsgIds = new ArrayList<Long>();
             while (it.hasNext()) {
                 final int prevOffset = it.getOffset();
                 try {
                     final Message msg = it.next();
                     // If the message is processed before,don't process it
                     // again.
                    if (this.isProcessed(msg.getId(), group)) {
                         continue;
                     }
                     MessageAccessor.setPartition(msg, partition);
                     boolean accept = this.isAcceptable(request, filter, group, msg);
                     if (accept) {
                         currentTopicRegInfo.set(request.getTopicPartitionRegInfo().clone(it));
                         try {
                             listener.recieveMessages(msg);
                         }
                         finally {
                             currentTopicRegInfo.remove();
                         }
                     }
                     // rollback message if it is in rollback only state.
                     if (MessageAccessor.isRollbackOnly(msg)) {
                         it.setOffset(prevOffset);
                         break;
                     }
                     if (partition.isAutoAck()) {
                         count++;
                        this.markProcessed(msg.getId(), group);
                     }
                     else {
                         // ύ߻عѭ
                         if (partition.isAcked()) {
                             count++;
                             // mark all in transaction messages were processed.
                             for (Long msgId : inTransactionMsgIds) {
                                this.markProcessed(msgId, group);
                             }
                            this.markProcessed(msg.getId(), group);
                             break;
                         }
                         else if (partition.isRollback()) {
                             break;
                         }
                         else {
                             inTransactionMsgIds.add(msg.getId());
                             // ύҲǻع
                             count++;
                         }
                     }
                 }
                 catch (InterruptedException e) {
                     // Receive messages thread is interrupted
                     it.setOffset(prevOffset);
                     log.error("Process messages thread was interrupted,topic=" + request.getTopic() + ",partition="
                             + request.getPartition(), e);
                     break;
                 }
                 catch (final InvalidMessageException e) {
                     MetaStatLog.addStat(null, StatConstants.INVALID_MSG_STAT, request.getTopic());
                     // ϢǷȡЧoffset·ѯ
                     this.getOffsetAddRequest(request, e);
                     return true;
                 }
                 catch (final Throwable e) {
                     // ָƵһϢ
                     it.setOffset(prevOffset);
                     log.error(
                         "Process messages failed,topic=" + request.getTopic() + ",partition=" + request.getPartition(),
                         e);
                     // ѭϢ쳣Ϊֹ
                     break;
                 }
             }
             MetaStatLog.addStatValue2(null, StatConstants.GET_MSG_COUNT_STAT, request.getTopic(), count);
             return false;
         }
 
 
        private boolean isProcessed(final Long id, String group) {
            return SimpleFetchManager.this.messageIdCache.get(this.cacheKey(id, group)) != null;
        }


        private String cacheKey(final Long id, String group) {
            return group + id;
         }
 
 
        private void markProcessed(final Long msgId, String group) {
            SimpleFetchManager.this.messageIdCache.put(this.cacheKey(msgId, group), PROCESSED);
         }
 
 
         private boolean isAcceptable(final FetchRequest request, final ConsumerMessageFilter filter,
                 final String group, final Message msg) {
             if (filter == null) {
                 return true;
             }
             else {
                 try {
                     return filter.accept(group, msg);
                 }
                 catch (Exception e) {
                     log.error("Filter message failed,topic=" + request.getTopic() + ",group=" + group + ",filterClass="
                             + filter.getClass().getCanonicalName());
                     // If accept throw exception,we think we can't accept
                     // this message.
                     return false;
                 }
             }
         }
 
 
         private boolean processWhenRetryTooMany(final FetchRequest request, final MessageIterator it) {
             if (SimpleFetchManager.this.isRetryTooMany(request)) {
                 try {
                     final Message couldNotProecssMsg = it.next();
                     MessageAccessor.setPartition(couldNotProecssMsg, request.getPartitionObject());
                     MetaStatLog.addStat(null, StatConstants.SKIP_MSG_COUNT, couldNotProecssMsg.getTopic());
                     SimpleFetchManager.this.consumer.appendCouldNotProcessMessage(couldNotProecssMsg);
                 }
                 catch (final InvalidMessageException e) {
                     MetaStatLog.addStat(null, StatConstants.INVALID_MSG_STAT, request.getTopic());
                     // ϢǷȡЧoffset·ѯ
                     this.getOffsetAddRequest(request, e);
                     return true;
                 }
                 catch (final Throwable t) {
                     this.LogAddRequest(request, t);
                     return true;
                 }
 
                 request.resetRetries();
                 // ܴϢ
                 if (!this.stopped) {
                     request.setOffset(request.getOffset() + it.getOffset(), it.getPrevMessage().getId(), true);
                 }
                 // ǿӳΪ0
                 request.setDelay(0);
                 this.reAddFetchRequest2Queue(request);
                 return true;
             }
             else {
                 return false;
             }
         }
 
 
         private void postReceiveMessage(final FetchRequest request, final MessageIterator it, final Partition partition) {
             // offsetȻûǰԴ
             if (it.getOffset() == 0) {
                 request.incrementRetriesAndGet();
             }
             else {
                 request.resetRetries();
             }
 
             // Զackģʽ
             if (!partition.isAutoAck()) {
                 // ǻع,عoffsetٴη
                 if (partition.isRollback()) {
                     request.rollbackOffset();
                     partition.reset();
                     this.addRequst(request);
                 }
                 // ύʱoffset洢
                 else if (partition.isAcked()) {
                     partition.reset();
                     this.ackRequest(request, it, true);
                 }
                 else {
                     // ǣʱoffset
                     this.ackRequest(request, it, false);
                 }
             }
             else {
                 // Զackģʽ
                 this.ackRequest(request, it, true);
             }
         }
 
 
         private void ackRequest(final FetchRequest request, final MessageIterator it, final boolean ack) {
             if (!this.stopped) {
                 long msgId = it.getPrevMessage() != null ? it.getPrevMessage().getId() : -1;
                 request.setOffset(request.getOffset() + it.getOffset(), msgId, ack);
             }
             this.addRequst(request);
         }
 
 
         private void addRequst(final FetchRequest request) {
             final long delay = this.getRetryDelay(request);
             request.setDelay(delay);
             this.reAddFetchRequest2Queue(request);
         }
 
 
         private long getRetryDelay(final FetchRequest request) {
             final long maxDelayFetchTimeInMills = SimpleFetchManager.this.getMaxDelayFetchTimeInMills();
             final long nPartsDelayTime = maxDelayFetchTimeInMills / DELAY_NPARTS;
             // ӳʱΪӳʱ/10*Դ
             long delay = nPartsDelayTime * request.getRetries();
             if (delay > maxDelayFetchTimeInMills) {
                 delay = maxDelayFetchTimeInMills;
             }
             return delay;
         }
 
 
         // ʱѯ
         private void updateDelay(final FetchRequest request) {
             final long delay = this.getNextDelay(request);
             request.setDelay(delay);
         }
 
 
         private long getNextDelay(final FetchRequest request) {
             final long maxDelayFetchTimeInMills = SimpleFetchManager.this.getMaxDelayFetchTimeInMills();
             // ÿ1/10,MaxDelayFetchTimeInMills
             final long nPartsDelayTime = maxDelayFetchTimeInMills / DELAY_NPARTS;
             long delay = request.getDelay() + nPartsDelayTime;
             if (delay > maxDelayFetchTimeInMills) {
                 delay = maxDelayFetchTimeInMills;
             }
             return delay;
         }
 
     }
 
 
     boolean isRetryTooMany(final FetchRequest request) {
         return request.getRetries() > this.consumerConfig.getMaxFetchRetries();
     }
 
 
     boolean isRetryTooManyForIncrease(final FetchRequest request) {
         return request.getRetries() > this.consumerConfig.getMaxIncreaseFetchDataRetries();
     }
 
 
     long getMaxDelayFetchTimeInMills() {
         return this.consumerConfig.getMaxDelayFetchTimeInMills();
     }
 
 }
