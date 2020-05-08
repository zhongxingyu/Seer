 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane.server2;
 
 import com.janrain.backplane.common.BpSerialUtils;
 import com.janrain.backplane.common.DateTimeUtils;
 import com.janrain.backplane.redis.Redis;
 import com.janrain.backplane.server2.dao.BP2DAOs;
 import com.janrain.backplane.server2.dao.redis.RedisBackplaneMessageDAO;
 import com.janrain.commons.util.Pair;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
 import com.netflix.curator.framework.state.ConnectionState;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Histogram;
 import com.yammer.metrics.core.MetricName;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Transaction;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Tom Raney
  */
 public class V2MessageProcessor implements LeaderSelectorListener {
 
     /**
      * Processor to remove expired messages
      */
     public void cleanupMessages() {
         try {
             BP2DAOs.getBackplaneMessageDAO().deleteExpiredMessages();
         } catch (Exception e) {
             logger.error(e);
         }
     }
 
     /**
      * Processor to pull messages off queue and make them available
      *
      */
     public void insertMessages() {
 
         try {
             logger.info("v2 message processor started");
             while (true) {
                 try {
                     processSingleBatchOfPendingMessages();
                     Thread.sleep(150);
                 } catch (Exception e) {
                     logger.warn(e);
                     try {
                         Thread.sleep(2000);
                     } catch (InterruptedException e1) {
                         // ignore
                     }
                 }
             }
         } finally {
             // very bad if we get here...
             logger.error("method exited but it should NEVER do so");
         }
     }
 
     private void processSingleBatchOfPendingMessages() throws Exception {
 
         Jedis jedis = null;
 
         try {
 
             jedis = Redis.getInstance().getWriteJedis();
 
             List<String> insertionTimes = new ArrayList<String>();
 
             // retrieve the latest 'live' message ID
             String latestMessageId = null;
             Set<String> latestMessageMetaSet = jedis.zrange(RedisBackplaneMessageDAO.V2_MESSAGES, -1, -1);
             if (latestMessageMetaSet != null && !latestMessageMetaSet.isEmpty()) {
                 String[] segs = latestMessageMetaSet.iterator().next().split(" ");
                 if (segs.length == 3) {
                     latestMessageId = segs[2];
                 }
             }
 
             Pair<String, Date> lastIdAndDate =  new Pair<String, Date>("", new Date(0));
             try {
                 lastIdAndDate = StringUtils.isEmpty(latestMessageId) ?
                     new Pair<String, Date>("", new Date(0)) :
                     new Pair<String, Date>(latestMessageId, DateTimeUtils.ISO8601.get().parse(latestMessageId.substring(0, latestMessageId.indexOf("Z") + 1)));
             } catch (Exception e) {
                 //
             }
 
             jedis.watch(RedisBackplaneMessageDAO.V2_MESSAGES);
 
             // retrieve a handful of messages (ten) off the queue for processing
             List<byte[]> messagesToProcess = jedis.lrange(RedisBackplaneMessageDAO.V2_MESSAGE_QUEUE.getBytes(), 0, 9);
 
             // only enter the next block if we have messages to process
             if (messagesToProcess.size() > 0) {
 
                 Transaction transaction = jedis.multi();
 
                 insertionTimes.clear();
 
                 for (byte[] messageBytes : messagesToProcess) {
 
                     if (messageBytes != null) {
                         BackplaneMessage backplaneMessage = (BackplaneMessage) BpSerialUtils.deserialize(messageBytes);
 
                         if (backplaneMessage != null) {
                             processSingleMessage(backplaneMessage, transaction, insertionTimes, lastIdAndDate);
                         }
                     }
                 }
 
                 logger.info("processing transaction with " + insertionTimes.size() + " v2 message(s)");
                 List<Object> results = transaction.exec();
                 if (results == null || results.size() == 0) {
                     // the transaction failed
                     logger.warn("transaction failed! - halting work for now");
                     return;
                 }
 
                 logger.info("flushed " + insertionTimes.size() + " v2 messages");
                 long now = System.currentTimeMillis();
                 for (String insertionId : insertionTimes) {
                    long diff = now - com.janrain.backplane.server1.BackplaneMessage.getDateFromId(insertionId).getTime();
                     timeInQueue.update(diff);
                     if (diff < 0 || diff > 2880000) {
                         logger.warn("time diff is bizarre at: " + diff);
                     }
                 }
             }
         } catch (Exception e) {
             // if we get here, something bonked, like a connection to the redis server
             logger.warn("an error occurred while trying to process v2 message batch: " + e.getMessage());
             Redis.getInstance().releaseBrokenResourceToPool(jedis);
             jedis = null;
             throw e;
         } finally {
             try {
                 if (jedis != null) {
                     jedis.unwatch();
                     Redis.getInstance().releaseToPool(jedis);
                 }
             } catch (Exception e) {
                 // ignore
             }
         }
     }
 
     private void processSingleMessage(BackplaneMessage backplaneMessage,
                                  Transaction transaction, List<String> insertionTimes,
                                  Pair<String, Date> lastIdAndDate) throws Exception {
 
         try {
             String oldId = backplaneMessage.getIdValue();
             insertionTimes.add(oldId);
 
             // TOTAL ORDER GUARANTEE
             // verify that the date portion of the new message ID is greater than all existing message ID dates
             // if not, uptick id by 1 ms and insert
             // this means that all message ids have unique time stamps, even if they
             // arrived at the same time.
 
             lastIdAndDate = backplaneMessage.updateId(lastIdAndDate);
             String newId = backplaneMessage.getIdValue();
 
             // messageTime is guaranteed to be a unique identifier of the message
             // because of the TOTAL ORDER mechanism above
             long messageTime = BackplaneMessage.getDateFromId(newId).getTime();
 
             // <ATOMIC>
             // save the individual message by key & TTL
             transaction.setex(RedisBackplaneMessageDAO.getKey(newId),
                     DateTimeUtils.getExpireSeconds(backplaneMessage.getIdValue(), backplaneMessage.get(BackplaneMessage.Field.EXPIRE), backplaneMessage.isSticky()),
                     BpSerialUtils.serialize(backplaneMessage));
 
             // channel and bus sorted set index
             transaction.zadd(RedisBackplaneMessageDAO.getChannelKey(backplaneMessage.getChannel()), messageTime,
                     backplaneMessage.getIdValue().getBytes());
             transaction.zadd(RedisBackplaneMessageDAO.getBusKey(backplaneMessage.getBus()), messageTime,
                     backplaneMessage.getIdValue().getBytes());
 
             // add message id to sorted set of all message ids as an index
             String metaData = backplaneMessage.getBus() + " " + backplaneMessage.getChannel() + " " +
                     backplaneMessage.getIdValue();
 
             transaction.zadd(RedisBackplaneMessageDAO.V2_MESSAGES.getBytes(), messageTime, metaData.getBytes());
 
             // add message id to sorted set keyed by bus as an index
 
             // make sure all subscribers get the update
             transaction.publish("alerts", newId);
 
             // pop one message off the queue - which will only happen if this transaction is successful
             transaction.lpop(RedisBackplaneMessageDAO.V2_MESSAGE_QUEUE);
             // </ATOMIC>
 
             logger.info("pipelined v2 message " + oldId + " -> " + newId);
         } catch (Exception e) {
             throw e;
         }
 
     }
 
     private static final Logger logger = Logger.getLogger(V2MessageProcessor.class);
 
     private final Histogram timeInQueue = Metrics.newHistogram(new MetricName("v2", this.getClass().getName().replace(".","_"), "time_in_queue"));
 
     @Override
     public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
         logger.info("v2 leader elected");
         insertMessages();
     }
 
     @Override
     public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
         logger.info("state changed");
     }
 }
