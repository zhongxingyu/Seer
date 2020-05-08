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
 
 package com.janrain.backplane.server.dao.redis;
 
 import com.janrain.backplane.server.BackplaneMessage;
 import com.janrain.backplane.server.BackplaneServerException;
 import com.janrain.backplane.server.dao.DAO;
 import com.janrain.redis.Redis;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Histogram;
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Pipeline;
 import redis.clients.jedis.Response;
 import redis.clients.jedis.Transaction;
 
 import java.util.*;
 
 /**
  * @author Tom Raney
  */
 public class RedisBackplaneMessageDAO extends DAO<BackplaneMessage> {
 
     final public static String V1_MESSAGE_QUEUE = "v1_message_queue";
     final public static String V1_MESSAGES = "v1_messages";
 
     public static byte[] getBusKey(String bus) {
         return ("v1_bus_idx_" + bus).getBytes();
     }
 
     public static byte[] getChannelKey(String channel) {
         return ("v1_channel_idx_" + channel).getBytes();
     }
 
     public static byte[] getKey(String key) {
         return ("v1_message_" + key).getBytes();
     }
 
     /**
      * Add message to work queue - any node may add since it is an atomic operation
      * However, the message ID will be determined later by the message processor
      * @param message
      */
 
     @Override
     public void persist(BackplaneMessage message) throws BackplaneServerException {
         Redis.getInstance().rpush(V1_MESSAGE_QUEUE.getBytes(), SerializationUtils.serialize(message));
     }
 
     @Override
     public void delete(String id) throws BackplaneServerException {
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
             Date d = BackplaneMessage.getDateFromId(id);
             Set<String> sortedSetBytes = jedis.zrangeByScore(V1_MESSAGES, d.getTime(), d.getTime());
 
             if (!sortedSetBytes.isEmpty()) {
                 String key = sortedSetBytes.iterator().next();
                 Transaction t = jedis.multi();
 
                 Response<Long> del1 = t.zrem(V1_MESSAGES, key);
                 String[] args = key.split(" ");
                 Response<Long> del2 = t.lrem(getChannelKey(args[1]), 0, args[2].getBytes());
                 Response<Long> del3 = t.zrem(getBusKey(args[0]), args[2].getBytes());
                 Response<Long> del4 = t.del(getKey(id));
 
                 t.exec();
 
                 if (del1.get() == 0) {
                     logger.warn("could not remove message " + id + " from " + V1_MESSAGES);
                 }
                 if (del2.get() == 0) {
                     logger.warn("could not remove message " + id + " from " + new String(getChannelKey(args[1])));
                 }
                 if (del3.get() == 0) {
                     logger.warn("could not remove message " + id + " from " + new String(getBusKey(args[0])));
                 }
                 if (del4.get() == 0) {
                    logger.warn("could not remove message " + id + " from " + new String(getKey(id)) + " but it may have expired");
                 }
                 logger.info("v1 message " + id + " deleted");
             } else {
                 logger.warn("v1 message " + id + " not found in " + V1_MESSAGES);
             }
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public BackplaneMessage get(String key) {
         byte[] messageBytes = Redis.getInstance().get(key.getBytes());
         if (messageBytes != null) {
             return (BackplaneMessage) SerializationUtils.deserialize(messageBytes);
         }
         return null;
     }
 
     @Override
     public List<BackplaneMessage> getAll() throws BackplaneServerException {
         throw new NotImplementedException();
     }
 
     public int getMessageCount(String bus, String channel) {
         return (int) Redis.getInstance().llen(getChannelKey(channel));
     }
 
     /**
      * Fetch a list (possibly empty) of backplane messages that exist on the channel
      * and return them in order by message id
      * @param channel
      * @return
      */
 
     public List<BackplaneMessage> getMessagesByChannel(String bus, String channel, String since, String sticky) throws SimpleDBException, BackplaneServerException {
 
         Jedis jedis = null;
 
         try {
 
             jedis = Redis.getInstance().getJedis();
 
             double sinceInMs = 0;
             if (StringUtils.isNotBlank(since)) {
                 sinceInMs = BackplaneMessage.getDateFromId(since).getTime();
             }
 
             // every message has a unique timestamp - which serves as a key for indexing
             List<byte[]> messageIdBytes = jedis.lrange(getChannelKey(channel), 0, -1);
 
             List<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
 
             Pipeline pipeline = jedis.pipelined();
             List<Response<byte[]>> responses = new ArrayList<Response<byte[]>>();
 
             if (messageIdBytes != null) {
                 for (byte[] b: messageIdBytes) {
                     responses.add(pipeline.get(getKey(new String(b))));
                 }
                 pipeline.sync();
                 for (Response<byte[]> response: responses) {
                     byte[] bytes = response.get();
                     if (bytes != null) {
                         messages.add((BackplaneMessage) SerializationUtils.deserialize(bytes));
                     }
                 }
             }
 
             filterAndSort(messages, since, sticky);
             return messages;
 
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage());
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
 
     }
 
     public List<String> getMessageIds(List<BackplaneMessage> messages) {
         List<String> ids = new ArrayList<String>();
         for (BackplaneMessage message : messages) {
             ids.add(message.getIdValue());
         }
         return ids;
     }
 
     public List<BackplaneMessage> getMessagesByBus(String bus, String since, String sticky) throws SimpleDBException, BackplaneServerException {
 
         Jedis jedis = null;
 
         try {
 
             jedis = Redis.getInstance().getJedis();
 
             double sinceInMs = 0;
             if (StringUtils.isNotBlank(since)) {
                 sinceInMs = BackplaneMessage.getDateFromId(since).getTime();
             }
 
             // every message has a unique timestamp - which serves as a key for indexing
             Set<byte[]> messageIdBytes = Redis.getInstance().zrangebyscore(RedisBackplaneMessageDAO.getBusKey(bus), sinceInMs, Double.POSITIVE_INFINITY);
 
             List<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
 
             Pipeline pipeline = jedis.pipelined();
             List<Response<byte[]>> responses = new ArrayList<Response<byte[]>>();
 
             if (messageIdBytes != null) {
                 for (byte[] b: messageIdBytes) {
                     responses.add(pipeline.get(getKey(new String(b))));
                 }
                 pipeline.sync();
                 for (Response<byte[]> response: responses) {
                     byte[] bytes = response.get();
                     if (bytes != null) {
                         BackplaneMessage backplaneMessage = (BackplaneMessage) SerializationUtils.deserialize(bytes);
                         messages.add(backplaneMessage);
                     }
                 }
             }
 
             filterAndSort(messages, since, sticky);
             return messages;
 
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
 
     }
 
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(RedisBackplaneMessageDAO.class);
 
     private final Histogram messagesPerChannel = Metrics.newHistogram(RedisBackplaneMessageDAO.class, "v1_messages_per_channel");
 
     private void filterAndSort(List<BackplaneMessage> messages, String since, String sticky) {
 
         // filter per sticky flag
         if (StringUtils.isNotBlank(sticky)) {
             Iterator<BackplaneMessage> iterator = messages.iterator();
             while(iterator.hasNext()) {
                 BackplaneMessage message = iterator.next();
                 if (!message.get(BackplaneMessage.Field.STICKY.getFieldName()).equals(sticky)) {
                     iterator.remove();
                 }
             }
         }
 
         // filter per since flag
         if (StringUtils.isNotBlank(since)) {
             Iterator<BackplaneMessage> iterator = messages.iterator();
             while(iterator.hasNext()) {
                 BackplaneMessage message = iterator.next();
                 if (message.getIdValue().compareTo(since) <= 0) {
                     iterator.remove();
                 }
             }
         }
 
         Collections.sort(messages, new Comparator<BackplaneMessage>() {
             @Override
             public int compare(BackplaneMessage backplaneMessage, BackplaneMessage backplaneMessage1) {
                 return backplaneMessage.getIdValue().compareTo(backplaneMessage1.getIdValue());
             }
         });
     }
 
     private String genBusKey(String key) {
         return "v1bus" + key;
     }
 
     private String genChannelKey(String key) {
         return "v1channel" + key;
     }
 
 
 }
