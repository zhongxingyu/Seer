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
 
 package com.janrain.backplane.server;
 
 import com.janrain.backplane.server.config.Backplane1Config;
 import com.janrain.backplane.server.dao.redis.RedisBackplaneMessageDAO;
 import com.janrain.backplane.server.dao.DaoFactory;
 import com.janrain.backplane2.server.dao.BackplaneMessageDAO;
 import com.janrain.commons.util.Pair;
 import com.janrain.redis.Redis;
 import com.netflix.curator.framework.CuratorFramework;
 import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
 import com.netflix.curator.framework.state.ConnectionState;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Histogram;
 import com.yammer.metrics.core.MetricName;
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPubSub;
 import redis.clients.jedis.Transaction;
 
 import java.util.*;
 
 /**
  * @author Tom Raney
  */
 public class MessageProcessor extends JedisPubSub implements LeaderSelectorListener {
 
     public MessageProcessor() {}
 
     @Override
     public void onMessage(String s, String s1) {
         logger.info("message received on channel: " + s + " with message: " + s1);
     }
 
     @Override
     public void onPMessage(String s, String s1, String s2) {
 
     }
 
     @Override
     public void onSubscribe(String s, int i) {
         logger.info("successfully subscribed to " + s);
     }
 
     @Override
     public void onUnsubscribe(String s, int i) {
 
     }
 
     @Override
     public void onPUnsubscribe(String s, int i) {
 
     }
 
     @Override
     public void onPSubscribe(String s, int i) {
 
     }
 
     public void subscribe() {
 
         Jedis jedis = null;
 
         try {
             jedis = Redis.getInstance().getJedis();
             //this call is blocking
             jedis.subscribe(this, "alerts");
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
 
     }
 
     /**
      * Processor to remove expired messages
      */
     public void cleanupMessages() {
         try {
             DaoFactory.getBackplaneMessageDAO().deleteExpiredMessages();
         } catch (Exception e) {
             logger.error(e);
         }
     }
 
     /**
      * Processor to pull messages off queue and make them available
      *
      */
     public void insertMessages(boolean loop) {
 
         try {
             logger.info("v1 message processor started");
 
             List<String> insertionTimes = new ArrayList<String>();
             do {
 
                 Jedis jedis = null;
 
                 try {
 
                 jedis = Redis.getInstance().getJedis();
 
                 // retrieve the latest 'live' message ID
                 String latestMessageId = null;
                 Set<String> latestMessageMetaSet = jedis.zrange(RedisBackplaneMessageDAO.V1_MESSAGES, -1, -1);
 
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
                         new Pair<String, Date>(latestMessageId, Backplane1Config.ISO8601.get().parse(latestMessageId.substring(0, latestMessageId.indexOf("Z") + 1)));
                 } catch (Exception e) {
                         //
                 }
 
                 // set watch on the messages sorted set of keys
                 jedis.watch(RedisBackplaneMessageDAO.V1_MESSAGES);
 
                 // retrieve a handful of messages (ten) off the queue for processing
                 List<byte[]> messagesToProcess = jedis.lrange(RedisBackplaneMessageDAO.V1_MESSAGE_QUEUE.getBytes(), 0, 9);
 
                 // only enter the next block if we have messages to process
                 if (messagesToProcess.size() > 0) {
 
                     Transaction transaction = jedis.multi();
 
                     insertionTimes.clear();
 
                     for (byte[] messageBytes : messagesToProcess) {
 
                         if (messageBytes != null) {
                             BackplaneMessage backplaneMessage = (BackplaneMessage) SerializationUtils.deserialize(messageBytes);
 
                             if (backplaneMessage != null) {
 
                                 // retrieve the expiration config per the bus
                                 BusConfig1 busConfig1 = DaoFactory.getBusDAO().get(backplaneMessage.getBus());
                                 int retentionTimeSeconds = 60;
                                 int retentionTimeStickySeconds = 3600;
                                 if (busConfig1 != null) {
                                     // should be here in normal flow
                                     retentionTimeSeconds = busConfig1.getRetentionTimeSeconds();
                                     retentionTimeStickySeconds = busConfig1.getRetentionTimeStickySeconds();
                                 }
 
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
                                 // save the individual message by key
                                 transaction.set(RedisBackplaneMessageDAO.getKey(newId), SerializationUtils.serialize(backplaneMessage));
                                 // set the message TTL
                                 if (backplaneMessage.isSticky()) {
                                     transaction.expire(RedisBackplaneMessageDAO.getKey(newId), retentionTimeStickySeconds);
                                 } else {
                                     transaction.expire(RedisBackplaneMessageDAO.getKey(newId), retentionTimeSeconds);
                                 }
 
                                 // add message id to channel list
                                 transaction.rpush(RedisBackplaneMessageDAO.getChannelKey(backplaneMessage.getChannel()), newId.getBytes());
 
                                 // add message id to sorted set of all message ids as an index
                                 String metaData = backplaneMessage.getBus() + " " + backplaneMessage.getChannel() + " " + newId;
                                 transaction.zadd(RedisBackplaneMessageDAO.V1_MESSAGES.getBytes(), messageTime, metaData.getBytes());
 
                                 // add message id to sorted set keyed by bus as an index
                                 transaction.zadd(RedisBackplaneMessageDAO.getBusKey(backplaneMessage.getBus()), messageTime, newId.getBytes());
 
                                 // make sure all subscribers get the update
                                 transaction.publish("alerts", newId);
 
                                 // pop one message off the queue - which will only happen if this transaction is successful
                                 transaction.lpop(RedisBackplaneMessageDAO.V1_MESSAGE_QUEUE);
                                 // </ATOMIC>
 
                                 logger.info("pipelined message " + oldId + " -> " + newId);
                             }
                         }
                     } // for messages
 
                     Thread.sleep(150);
 
                     logger.info("processing transaction with " + insertionTimes.size() + " message(s)");
                     if (transaction.exec() == null) {
                         // the transaction failed
                         continue;
                     }
 
                     logger.info("flushed " + insertionTimes.size() + " messages");
                     long now = System.currentTimeMillis();
                     for (String insertionId : insertionTimes) {
                         long diff = now - BackplaneMessage.getDateFromId(insertionId).getTime();
                         timeInQueue.update(diff);
                         if (diff < 0 || diff > 2880000) {
                             logger.warn("time diff is bizarre at: " + diff);
                         }
                     }
                 } // messagesToProcess > 0
 
                 } catch (Exception e) {
                    try {
                        logger.warn("error " + e.getMessage());
                        if (jedis != null) {
                            jedis.unwatch();
                            Redis.getInstance().releaseBrokenResourceToPool(jedis);
                            jedis = null;
                        }
                    } catch (Exception e1) {
                        //ignore
                     }
                     Thread.sleep(2000);
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
 
             } while (loop);
 
         } catch (Exception e) {
             logger.warn("exception thrown in message processor thread: " + e.getMessage());
         }
     }
 
     private static final Logger logger = Logger.getLogger(MessageProcessor.class);
 
     private final Histogram timeInQueue = Metrics.newHistogram(new MetricName("v1", this.getClass().getName().replace(".","_"), "time_in_queue"));
 
     @Override
     public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
         logger.info("v1 leader elected");
         insertMessages(true);
     }
 
     @Override
     public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
         logger.info("state changed");
     }
 }
