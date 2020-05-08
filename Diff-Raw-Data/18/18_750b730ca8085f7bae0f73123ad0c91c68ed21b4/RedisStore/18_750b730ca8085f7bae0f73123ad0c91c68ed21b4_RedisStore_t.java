 package com.vreco.boomerang.datastore;
 
 import com.vreco.boomerang.message.Message;
 import com.vreco.boomerang.message.ResponseMessage;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Set;
 import org.codehaus.jackson.map.ObjectMapper;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Response;
 import redis.clients.jedis.Transaction;
 
 /**
  * Store messages in Redis.
  *
  * @author Ben Aldrich
  */
 public class RedisStore implements DataStore, AutoCloseable {
 
   final Jedis jedis;
   final String appName;
   final ObjectMapper mapper = new ObjectMapper();
   final protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
 
   /**
    * Initialize data store.
    *
    * @param connectionHostName
    * @param appName
    */
   public RedisStore(String connectionHostName, String appName) {
     jedis = new Jedis(connectionHostName);
     this.appName = appName;
   }
 
 //  @Override
 //  public void batchSet(Map<String, String> batch) {
 //    Pipeline pipe = jedis.pipelined();
 //    ArrayList<Response<Long>> responses = new ArrayList();
 //    for (Map.Entry<String, String> message : batch.entrySet()) {
 //      //responses.add(pipe.hset(getHashKey(appName, date), message.getKey(), message.getValue()));
 //    }
 //
 //    pipe.sync();
 //
 //    for (Response<Long> r : responses) {
 //      if (r.get().intValue() != 1) {
 //        //throw new IOException("Failed to insert batch!");
 //      }
 //    }
 //
 //  }
   @Override
   public Set<String> getKeys(Date date) {
 //    final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     return jedis.hkeys(getHashKey(appName, "broke", date));
   }
 
   /**
    * Get our stored message from redis.
    *
    * @param msg
    * @return
    * @throws ParseException
    */
   @Override
   public String get(final ResponseMessage msg) throws ParseException {
     final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     return jedis.hget(key, msg.getUuid());
   }
 
   /**
    * Get our stored message from redis.
    *
    * @param msg
    * @return
    * @throws ParseException
    */
   @Override
   public String get(final Message msg) throws ParseException {
     final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     return jedis.hget(key, msg.getUuid());
   }
 
   /**
    * Check to see if a message exists in the data store.
    *
    * @param msg
    * @return
    * @throws ParseException
    */
   @Override
   public boolean exists(final ResponseMessage msg) throws ParseException {
     final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     final String zkey = getZKey();
     final String zvalue = getZValue(msg.getDate(), msg.getUuid());
    final boolean hexists = jedis.hexists(key, msg.getUuid());
     final boolean zexists = jedis.zscore(zkey, zvalue) != null;
    if (hexists || zexists) {
       return true;
    }
    else {
       return false;
     }
   }
 
   /**
    * Check to see if a message exists in the data store.
    *
    * @param msg
    * @return
    */
   @Override
   public boolean exists(final Message msg) {
     final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     final String zkey = getZKey();
     final String zvalue = getZValue(msg.getDate(), msg.getUuid());
    final boolean hexists = jedis.hexists(key, msg.getUuid());
    final boolean zexists = jedis.zscore(zkey, zvalue) != null;
    if (hexists || zexists) {
       return true;
    }
    else {
       return false;
     }
   }
 
   /**
    * Add a message to the data store.
    *
    * @param msg
    * @throws IOException
    */
   @Override
   public void set(final Message msg) throws IOException {
     try {
       ArrayList<Response<Long>> responses = new ArrayList();
       final Date date = msg.getDate();
       final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
       final String zValue = getZValue(date, msg.getUuid());
       final String zKey = getZKey();
 
       Transaction t = jedis.multi();
       System.out.println("setting: " + key);
       System.out.println("zkey: " + zKey);
       System.out.println("zValue: " + zValue);
       responses.add(t.hset(key, msg.getUuid(), mapper.writeValueAsString(msg.getMsg())));
       responses.add(t.zadd(zKey.getBytes("UTF8"), 0, zValue.getBytes("UTF8")));
       t.exec();
       for (Response<Long> r : responses) {
         System.out.println("response:" + r.get());
       }
     } catch (RuntimeException e) {
       throw new IOException("Failed to set message in store.", e);
     }
   }
 
   /**
    * Add a collection of messages in the data store.
    *
    * @param msgs
    */
   @Override
   public void batchSet(Collection<Message> msgs) {
     throw new UnsupportedOperationException("Not supported yet.");
   }
 
   /**
    * Delete our stored message.
    *
    * @param msg
    * @throws ParseException
    */
   @Override
   public void delete(ResponseMessage msg) throws ParseException {
     final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     final String zkey = getZKey();
     final String zvalue = getZValue(msg.getDate(), msg.getUuid());
     jedis.zrem(zkey, zvalue);
     jedis.hdel(key, msg.getUuid());
   }
 
   /**
    * Delete our stored message.
    *
    * @param msg
    * @throws ParseException
    */
   @Override
   public void delete(Message msg) throws ParseException {
     final String key = getHashKey(appName, msg.getUuid(), msg.getDate());
     final String zkey = getZKey();
     final String zvalue = getZValue(msg.getDate(), msg.getUuid());
     jedis.zrem(zkey, zvalue);
     jedis.hdel(key, msg.getUuid());
   }
 
   /**
    * Delete all data in the db.
    */
   public void deleteAll() {
     jedis.flushDB();
   }
   
   protected long zSize() {
     return jedis.zcard(getZKey());
   }
 
   protected String getZKey() {
     StringBuilder sb = new StringBuilder(appName);
     sb.append(":uuidByDate");
     return sb.toString();
   }
 
   protected String getZValue(Date date, String uuid) {
     StringBuilder sb = new StringBuilder();
     sb.append(date.getTime());
     sb.append(":");
     sb.append(uuid);
     return sb.toString();
   }
 
   /**
    * Build hash key.
    *
    * @param prefix
    * @param date
    * @return new hash key.
    */
   protected String getHashKey(String prefix, String uuid, Date date) {
     StringBuilder sb = new StringBuilder(prefix);
     sb.append(":");
     sb.append(uuid);
     sb.append(":");
     sb.append(dateFormat.format(date));
     return sb.toString();
   }
 
   /**
    * Close / disconnect from our data store.
    */
   @Override
   public void close() {
     jedis.disconnect();
   }
 }
