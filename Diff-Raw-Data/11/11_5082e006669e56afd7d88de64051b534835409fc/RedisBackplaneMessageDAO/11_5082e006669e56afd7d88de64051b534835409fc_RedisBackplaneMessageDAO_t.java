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
 
 package com.janrain.backplane2.server.dao.redis;
 
 import com.janrain.backplane2.server.*;
 import com.janrain.backplane2.server.dao.BackplaneMessageDAO;
 import com.janrain.commons.supersimpledb.message.Message;
 import com.janrain.crypto.ChannelUtil;
 import com.janrain.oauth2.TokenException;
 import com.janrain.redis.Redis;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.jboss.netty.util.internal.StringUtil;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import redis.clients.jedis.*;
 
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 /**
  * @author Tom Raney, Johnny Bufu
  */
 public class RedisBackplaneMessageDAO implements BackplaneMessageDAO {
 
     final public static String V2_MESSAGE_QUEUE = "v2_message_queue";
     final public static String V2_MESSAGES = "v2_messages";
 
     public static byte[] getBusKey(String bus) {
         return ("v2_bus_idx_" + bus).getBytes();
     }
 
     public static byte[] getChannelKey(String channel) {
         return ("v2_channel_idx_" + channel).getBytes();
     }
 
     public static byte[] getKey(String key) {
         return ("v2_message_" + key).getBytes();
     }
 
 
     @Override
     public BackplaneMessage getLatestMessage() throws BackplaneServerException {
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
 
             Set<byte[]> bytesList = jedis.zrange(V2_MESSAGES.getBytes(), -1, -1);
             if (! bytesList.isEmpty()) {
                 String args[] = new String(bytesList.iterator().next()).split(" ");
                 byte[] bytes = jedis.get(getKey(args[2]));
                 if (bytes != null) {
                     return (BackplaneMessage) SerializationUtils.deserialize(bytes);
                 }
             }
             return null;
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @NotNull
     @Override
     public BackplaneMessage retrieveBackplaneMessage(@NotNull String messageId, @NotNull Token token)
             throws BackplaneServerException, TokenException {
 
         BackplaneMessage message = get(messageId);
 
         if ( message == null || ! token.getScope().isMessageInScope(message)) {
             // don't disclose that the messageId exists if not in scope
             throw new TokenException("Message id '" + messageId + "' not found", HttpServletResponse.SC_NOT_FOUND);
         } else {
             return message;
         }
     }
 
     @Override
     public long getMessageCount(String channel) {
         return (int) Redis.getInstance().zcard(getChannelKey(channel));
     }
 
     @Override
     public long countMessages() throws BackplaneServerException {
         return Redis.getInstance().llen(V2_MESSAGES.getBytes());
     }
 
     @Override
     public void retrieveMessagesPerScope(@NotNull MessagesResponse bpResponse, @NotNull Token token) throws BackplaneServerException {
         final Scope scope = token.getScope();
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
             Transaction t = jedis.multi();
             List<String> unions = new ArrayList<String>();
 
             Set<String> channelScopes = scope.getScopeFieldValues(BackplaneMessage.Field.CHANNEL);
             if (channelScopes != null) {
                 String channelUnion = "scope_req_" + ChannelUtil.randomString(10);
                 unions.add(channelUnion);
                 for(String channel : channelScopes) {
                     t.zunionstore( channelUnion.getBytes(), new ZParams() {{aggregate(Aggregate.MAX);}},
                             channelUnion.getBytes(), getChannelKey(channel) );
                 }
             }
             Set<String> busScopes = scope.getScopeFieldValues(BackplaneMessage.Field.BUS);
             if (busScopes != null) {
                 String busUnion = "scope_req_" + ChannelUtil.randomString(10);
                 unions.add(busUnion);
                 for(String bus : busScopes) {
                     t.zunionstore( busUnion.getBytes(), new ZParams() {{aggregate(Aggregate.MAX);}},
                             busUnion.getBytes(), getBusKey(bus));
                 }
             }
             String channelBusIntersection = null;
             for(String union : unions) {
                 if (channelBusIntersection == null) {
                     channelBusIntersection = union;
                 } else {
                     t.zinterstore( channelBusIntersection.getBytes(), new ZParams() {{aggregate(Aggregate.MAX);}},
                                    channelBusIntersection.getBytes(), union.getBytes());
                 }
             }
 
             Response<Set<byte[]>> lastResponse = t.zrange(V2_MESSAGES.getBytes(), -1, -1);
             List<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
 
             if (channelBusIntersection != null) {
                 Date lastMessageDate = BackplaneMessage.getDateFromId(bpResponse.getLastMessageId());
                 long lastMessageTime = lastMessageDate == null ? 0 : lastMessageDate.getTime();
                 Response<Set<String>> busChannelMessageIds = t.zrangeByScore(channelBusIntersection,
                         lastMessageTime+1, Double.MAX_VALUE);
                 for(String union : unions) t.del(union);
                 t.exec();
                 if (! busChannelMessageIds.get().isEmpty()) {
                     List<byte[]> idBytes = new ArrayList<byte[]>();
                     for(String msgId : busChannelMessageIds.get()) {
                         idBytes.add(getKey(msgId));
                     }
                     for(byte[] messageBytes : jedis.mget(idBytes.toArray(new byte[idBytes.size()][]))) {
                         if (messageBytes != null) messages.add((BackplaneMessage) SerializationUtils.deserialize(messageBytes));
                     }
 
                 }
             } else {
                 t.exec();
             }
 
             if ( ! messages.isEmpty()) {
                 filterMessagesPerScope(messages, scope, bpResponse);
             } else {
                 Set<byte[]> lastBytes = lastResponse.get();
                 if (lastBytes.isEmpty()) {
                     bpResponse.setLastMessageId("");
                 } else {
                     String lastMessageId = new String(lastBytes.iterator().next()).split(" ")[2];
                     bpResponse.setLastMessageId(lastMessageId);
                 }
             }
         } catch (Exception e) {
             logger.error(e);
             throw new BackplaneServerException(e.getMessage());
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public List<BackplaneMessage> retrieveMessagesNoScope(@Nullable String sinceIso8601timestamp) throws BackplaneServerException {
         Jedis jedis = null;
 
         try {
 
             jedis = Redis.getInstance().getJedis();
 
             double sinceInMs = 0;
             if (StringUtils.isNotBlank(sinceIso8601timestamp)) {
                 sinceInMs = BackplaneMessage.getDateFromId(sinceIso8601timestamp).getTime();
             }
 
             // every message has a unique timestamp - which serves as a key for indexing
             Set<byte[]> messageIdBytes = jedis.zrangeByScore(V2_MESSAGES.getBytes(), sinceInMs+1, Double.POSITIVE_INFINITY);
 
             List<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
 
             Pipeline pipeline = jedis.pipelined();
             List<Response<byte[]>> responses = new ArrayList<Response<byte[]>>();
 
             if (messageIdBytes != null) {
                 for (byte[] b: messageIdBytes) {
                     String[] args = new String(b).split(" ");
                     byte[] key = getKey(args[2]);
                     responses.add(pipeline.get(key));
                 }
                 pipeline.sync();
                 for (Response<byte[]> response : responses) {
                     if (response.get() != null) {
                         BackplaneMessage backplaneMessage = (BackplaneMessage) SerializationUtils.deserialize(response.get());
                         messages.add(backplaneMessage);
                     } else {
                         logger.warn("failed to retrieve a message");
                     }
                 }
             }
 
             Collections.sort(messages, new Comparator<BackplaneMessage>() {
                 @Override
                 public int compare(BackplaneMessage backplaneMessage, BackplaneMessage backplaneMessage1) {
                     return backplaneMessage.getIdValue().compareTo(backplaneMessage1.getIdValue());
                 }
             });
 
             return messages;
 
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public List<BackplaneMessage> retrieveMessagesByChannel(String channel) throws BackplaneServerException {
 
         Jedis jedis = null;
 
         try {
 
             jedis = Redis.getInstance().getJedis();
 
             List<BackplaneMessage> messages = new ArrayList<BackplaneMessage>();
             Set<byte[]> messageIdBytes = jedis.zrange(getChannelKey(channel), 0, -1);
 
             Pipeline pipeline = jedis.pipelined();
             List<Response<byte[]>> responses = new ArrayList<Response<byte[]>>();
 
             if (messageIdBytes != null) {
                 for (byte[] b: messageIdBytes) {
                     responses.add(pipeline.get(getKey(new String(b))));
                 }
                 pipeline.sync();
                 for (Response<byte[]> response : responses) {
                     if (response.get() != null) {
                         BackplaneMessage backplaneMessage = (BackplaneMessage) SerializationUtils.deserialize(response.get());
                         messages.add(backplaneMessage);
                     } else {
                         logger.warn("failed to retrieve a message");
                     }
                 }
             }
 
             Collections.sort(messages, new Comparator<BackplaneMessage>() {
                 @Override
                 public int compare(BackplaneMessage backplaneMessage, BackplaneMessage backplaneMessage1) {
                     return backplaneMessage.getIdValue().compareTo(backplaneMessage1.getIdValue());
                 }
             });
 
             return messages;
 
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
 
     }
 
     @Override
     public void deleteExpiredMessages() throws BackplaneServerException {
 
         Jedis jedis = null;
 
         try {
 
             logger.info("preparing to cleanup v2 messages");
 
             jedis = Redis.getInstance().getJedis();
 
             Set<byte[]> messageMetaBytes = jedis.zrangeByScore(V2_MESSAGES.getBytes(), 0, Double.MAX_VALUE);
             if (messageMetaBytes != null) {
                 for (byte[] b : messageMetaBytes) {
                     String metaData = new String(b);
                     String[] segs = metaData.split(" ");
                     if (jedis.get(getKey(segs[2])) == null) {
                         delete(segs[2]);
                     }
                 }
             }
         } catch (Exception e) {
             logger.error(e);
         } finally {
             logger.info("exiting v2 message cleanup");
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     @Override
     public BackplaneMessage get(String id) throws BackplaneServerException {
         byte[] messageBytes = Redis.getInstance().get(getKey(id));
         if (messageBytes != null) {
             return (BackplaneMessage) SerializationUtils.deserialize(messageBytes);
         }
         return null;
     }
 
     @Override
     public List<BackplaneMessage> getAll() throws BackplaneServerException {
         return retrieveMessagesNoScope(null);
     }
 
     @Override
     public void persist(BackplaneMessage obj) throws BackplaneServerException {
         // the messages will not be immediately available for reading until they
         // are inserted by the message processing thread.
         Redis.getInstance().rpush(V2_MESSAGE_QUEUE.getBytes(), SerializationUtils.serialize(obj));
     }
 
     @Override
     public void delete(String id) throws BackplaneServerException, TokenException {
         Jedis jedis = null;
         try {
             jedis = Redis.getInstance().getJedis();
             Date d = BackplaneMessage.getDateFromId(id);
            if (d == null) {
                logger.warn("cannot retrieve date from " + id + ": aborting delete");
                return;
            }
             Set<String> sortedSetBytes = jedis.zrangeByScore(V2_MESSAGES, d.getTime(), d.getTime());
 
             if (!sortedSetBytes.isEmpty()) {
                 String key = sortedSetBytes.iterator().next();
                 Transaction t = jedis.multi();
 
                 Response<Long> del1 = t.zrem(V2_MESSAGES, key);
                 String[] args = key.split(" ");
                 Response<Long> del2 = t.zrem(getChannelKey(args[1]), args[2].getBytes());
                 Response<Long> del3 = t.zrem(getBusKey(args[0]), args[2].getBytes());
                 Response<Long> del4 = t.del(getKey(id));
 
                 t.exec();
 
                 if (del1.get() == 0) {
                     logger.warn("could not remove message " + id + " from " + V2_MESSAGES);
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
                 logger.info("v2 message " + id + " deleted");
             } else {
                 logger.warn("v2 message " + id + " not found in " + V2_MESSAGES);
             }
         } finally {
             Redis.getInstance().releaseToPool(jedis);
         }
     }
 
     // PRIVATE
 
     private static final int MAX_MSGS_IN_FRAME = 25;
 
     private static final Logger logger = Logger.getLogger(RedisBackplaneMessageDAO.class);
 
     private void filterMessagesPerScope(List<BackplaneMessage> unfilteredMessages, Scope scope, MessagesResponse bpResponse) {
         // Filter and add to results
         List<BackplaneMessage> filteredMessages = new ArrayList<BackplaneMessage>();
         for (BackplaneMessage unfilteredMessage : unfilteredMessages) {
             if (scope.isMessageInScope(unfilteredMessage)) {
                 if (filteredMessages.size() >= MAX_MSGS_IN_FRAME) {
                     bpResponse.moreMessages(true);
                     bpResponse.setLastMessageId(filteredMessages.get(filteredMessages.size() - 1).getIdValue());
                     break;
                 }
                 filteredMessages.add(unfilteredMessage);
             }
         }
 
         // update lastMessageId to point to last message in this unfiltered result
         if (unfilteredMessages.size() > 0 && !bpResponse.moreMessages()) {
             bpResponse.setLastMessageId(unfilteredMessages.get(unfilteredMessages.size() - 1).getIdValue());
         }
 
         bpResponse.addMessages(filteredMessages);
     }
 }
