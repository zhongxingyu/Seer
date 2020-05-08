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
 
 package com.janrain.backplane2.server;
 
 import com.janrain.backplane.server.config.Backplane1Config;
 import com.janrain.backplane2.server.config.BusConfig2;
 import com.janrain.backplane2.server.dao.redis.RedisBackplaneMessageDAO;
 import com.janrain.commons.util.Pair;
 import com.janrain.redis.Redis;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Histogram;
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
 public class V2MessageProcessor extends JedisPubSub {
 
     public V2MessageProcessor() {}
 
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
 
     public synchronized void subscribe() {
 
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
 
         Jedis jedis = null;
 
         try {
 
             logger.info("preparing to cleanup v2 messages");
 
             jedis = Redis.getInstance().getJedis();
 
             Set<byte[]> messageMetaBytes = jedis.zrangeByScore(RedisBackplaneMessageDAO.V2_MESSAGES.getBytes(), 0, Double.MAX_VALUE);
             if (messageMetaBytes != null) {
                 for (byte[] b : messageMetaBytes) {
                     String metaData = new String(b);
                     String[] segs = metaData.split(" ");
                     if (jedis.get(segs[2]) == null) {
                         // remove this key from indexes
                         logger.info("removing message " + segs[2]);
                         jedis.zrem(RedisBackplaneMessageDAO.getBusKey(segs[0]), segs[2].getBytes());
                         jedis.lrem(RedisBackplaneMessageDAO.getChannelKey(segs[1]), 0, segs[2].getBytes());
                         jedis.zrem(RedisBackplaneMessageDAO.V2_MESSAGES.getBytes(), segs[2].getBytes());
                         //todo: remove the empty set?
                     }
                 }
             }
         } catch (Exception e) {
             logger.error(e);
         } finally {
             logger.info("exiting message cleanup");
             Redis.getInstance().releaseToPool(jedis);
         }
 
 
     }
 
     /**
      * Processor to pull messages off queue and make them available
      *
      */
     public void insertMessages(boolean loop) {
 
         String uuid = UUID.randomUUID().toString();
         Jedis jedis = null;
 
         try {
             logger.info("v2 message processor waiting for exclusive write lock");
 
             // TRY forever to get lock to do work
             String lock = Redis.getInstance().getLock(V2_WRITE_LOCK, uuid, -1, LOCK_SECONDS);
 
             // if we lose our lock sometime between this point and the lock refresh, the
             // transaction will fail
 
             if (lock != null) {
                 logger.info("v2 message processor got lock " + lock);
             } else {
                 logger.warn("something went terribly wrong");
                 return;
             }
 
             List<String> insertionTimes = new ArrayList<String>();
             do {
 
                 try {
 
                     jedis = Redis.getInstance().getJedis();
 
                     // retrieve the latest 'live' message ID
                     String latestMessageId = null;
                     Set<String> latestMessageMetaSet = jedis.zrange(RedisBackplaneMessageDAO.V2_MESSAGES, -1, -1);
                     if (latestMessageMetaSet != null && !latestMessageMetaSet.isEmpty()) {
                         String[] segs = latestMessageMetaSet.iterator().next().split(" ");
                         if (segs.length == 3) {
                             latestMessageId = segs[2];
                         }
                     }
 
                     Pair<String, Date> lastIdAndDate = StringUtils.isEmpty(latestMessageId) ?
                             new Pair<String, Date>("", new Date(0)) :
                             new Pair<String, Date>(latestMessageId, Backplane1Config.ISO8601.get().parse(latestMessageId.substring(0, latestMessageId.indexOf("Z") + 1)));
 
                     // retrieve a handful of messages (ten) off the queue for processing
                     List<byte[]> messagesToProcess = jedis.lrange(RedisBackplaneMessageDAO.V2_MESSAGE_QUEUE.getBytes(), 0, 9);
 
                     // only enter the next block if we have messages to process
                     if (messagesToProcess.size() > 0) {
 
                         // set watch on the lock key - this will allow the transaction to abort if
                         // the lock is reset by another process
                         jedis.watch(V2_WRITE_LOCK);
 
                         Transaction transaction = jedis.multi();
 
                         insertionTimes.clear();
 
                         for (byte[] messageBytes : messagesToProcess) {
 
                             if (messageBytes != null) {
                                 BackplaneMessage backplaneMessage = (BackplaneMessage) SerializationUtils.deserialize(messageBytes);
 
                                 if (backplaneMessage != null) {
 
                                     // retrieve the expiration config per the bus
                                     BusConfig2 busConfig2 = null;//DAOFactory.getInstance().getBusDAO().get(backplaneMessage.getBus());
                                     int retentionTimeSeconds = 60;
                                     int retentionTimeStickySeconds = 3600;
                                     if (busConfig2 != null) {
                                         // should be here in normal flow
                                         retentionTimeSeconds = busConfig2.getRetentionTimeSeconds();
                                         retentionTimeStickySeconds = busConfig2.getRetentionTimeStickySeconds();
                                     }
 
                                     String oldId = backplaneMessage.getIdValue();
                                     insertionTimes.add(oldId);
 
                                     // TOTAL ORDER GUARANTEE
                                     // verify that the new message ID is greater than all existing message IDs
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
 
                                     // append entire message to list of messages in a channel for retrieval efficiency
                                     transaction.rpush(RedisBackplaneMessageDAO.getChannelKey(backplaneMessage.getChannel()),
                                             SerializationUtils.serialize(backplaneMessage));
 
                                     // add message id to sorted set of all message ids as an index
                                    String metaData = backplaneMessage.getBus() + " " + backplaneMessage.getChannel() + " " + new String(RedisBackplaneMessageDAO.getKey(backplaneMessage.getIdValue()));

                                     transaction.zadd(RedisBackplaneMessageDAO.V2_MESSAGES.getBytes(), messageTime, metaData.getBytes());
 
                                     // add message id to sorted set keyed by bus as an index
                                     transaction.zadd(RedisBackplaneMessageDAO.getBusKey(backplaneMessage.getBus()), messageTime, newId.getBytes());
 
                                     // make sure all subscribers get the update
                                     transaction.publish("alerts", newId);
 
                                     // pop one message off the queue - which will only happen if this transaction is successful
                                     transaction.lpop(RedisBackplaneMessageDAO.V2_MESSAGE_QUEUE);
                                     // </ATOMIC>
 
                                     logger.info("pipelined v2 message " + oldId + " -> " + newId);
                                 }
                             }
                         }
 
                         logger.info("processing transaction with " + insertionTimes.size() + " v2 message(s)");
                         if (transaction.exec() == null) {
                             // the transaction failed, which likely means the lock was lost
                             logger.warn("transaction failed! - halting work for now");
                             return;
                         }
 
                         logger.info("flushed " + insertionTimes.size() + " v2 messages");
                         long now = System.currentTimeMillis();
                         for (String insertionId : insertionTimes) {
                             long diff = now - com.janrain.backplane.server.BackplaneMessage.getDateFromId(insertionId).getTime();
                             timeInQueue.update(diff);
                             if (diff >= 0 && diff < 2880000) {
                                 logger.warn("time diff is bizarre at: " + diff);
                             }
                         }
                     }
 
                     if (!Redis.getInstance().refreshLock(V2_WRITE_LOCK, uuid, LOCK_SECONDS)) {
                         logger.warn("lost lock! - halting work for now");
                         return;
                     }
 
                 } finally {
                     Redis.getInstance().releaseToPool(jedis);
                 }
 
             } while (loop);
 
         } catch (Exception e) {
             logger.warn("exception thrown in message processor thread", e);
         } finally {
             logger.info("v2 message processor releasing lock");
             // we may have already lost the lock, but if we exit for any other reason, good to release it
             Redis.getInstance().releaseLock(V2_WRITE_LOCK, uuid);
         }
 
     }
 
     private static final String V2_WRITE_LOCK = "v2_write_lock";
     private static final int LOCK_SECONDS = 30;
 
     private static final Logger logger = Logger.getLogger(V2MessageProcessor.class);
 
     private final Histogram timeInQueue = Metrics.newHistogram(V2MessageProcessor.class, "time_in_queue");
 }
