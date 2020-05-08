 package com.mmtzj.service;
 
 import com.mmtzj.util.RedisConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
 import org.springframework.stereotype.Service;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 
 import javax.annotation.Resource;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 @Service
 public class JedisService {
     private static Logger logger = LoggerFactory.getLogger(JedisService.class);
 
     @Autowired
     private RedisConfig redisConfig;
 
     @Resource
     private JedisPool jedisPool;
 
    @Resource
    private RedisTemplate redisTemplate;
 
     public Object get(String key) {
         if ("off".equals(redisConfig.getStatus())) {
             return null;
         }
         Object obj = null;
         Jedis jedis = jedisPool.getResource();
         try {
             obj = byte2Object(jedis.get(getKey(key)));
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return null;
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
         return obj;
     }
 
     public void lpush(String key, Object value) {
         Jedis jedis = jedisPool.getResource();
         try {
             jedis.lpush(getKey(key), object2Bytes(value));
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
     }
 
     public Object lpop(String key) {
         Jedis jedis = jedisPool.getResource();
         try {
             Object object = byte2Object(jedis.lpop(getKey(key)));
             return object;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
     }
 
     public Long zcount(String key, double min, double max) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.zcount(getKey(key), min, max);
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return null;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public Long zrank(String key, Object member) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.zrank(getKey(key), object2Bytes(member));
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return -1l;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public Long zrevrank(String key, Object member) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.zrevrank(getKey(key), object2Bytes(member)) == null ? -1 : jedis.zrevrank(getKey(key), object2Bytes(member));
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return -1l;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public List<Object> lrange(String key, int start, int end) {
         Jedis jedis = jedisPool.getResource();
         List<Object> ret = new ArrayList<Object>();
         try {
             List<byte[]> list = jedis.lrange(getKey(key), start, end);
             for (byte[] i : list) {
                 ret.add(byte2Object(i));
             }
             return ret;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
     }
 
     public void lset(String key, int index, Object value) {
         Jedis jedis = jedisPool.getResource();
         try {
             jedis.lset(getKey(key), index, object2Bytes(value));
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
     }
 
     public Object lindex(String key, int index) {
         Jedis jedis = jedisPool.getResource();
         try {
             return byte2Object(jedis.lindex(getKey(key), index));
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
     }
 
     public List<Object> zrevrange(String key, int start, int end) {
         Jedis jedis = jedisPool.getResource();
         List<Object> returnSet = new ArrayList<Object>();
         try {
             Set<byte[]> set = jedis.zrevrange(getKey(key), start, end);
             for (byte[] s : set) {
                 returnSet.add(byte2Object(s));
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (jedis != null)
                 this.jedisPool.returnResource(jedis);
         }
         return returnSet;
     }
 
     public Long zadd(String key, double score, Object member) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.zadd(getKey(key), score, object2Bytes(member));
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return null;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public Long zrem(String key, Object member) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.zrem(getKey(key), object2Bytes(member));
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return null;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public Long zcard(String key) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.zcard(getKey(key));
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return null;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public boolean isExist(String key) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.exists(key);
         } catch (Exception e) {
             logger.error(e.getLocalizedMessage());
             return false;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public void set(String key, Object value, int outTime) {
         if ("on".equals(redisConfig.getStatus())) {
             Jedis jedis = jedisPool.getResource();
             try {
                 jedis.set(getKey(key), object2Bytes(value));
                 if (outTime != 0) {
                     jedis.expire(getKey(key), outTime);
                 }
             } catch (Exception e) {
                 logger.error(e.getLocalizedMessage());
             } finally {
                 this.jedisPool.returnResource(jedis);
             }
         }
     }
 
     public Long del(String key) {
         Jedis jedis = jedisPool.getResource();
         try {
             return jedis.del(key);
         } catch (Exception e) {
             return null;
         } finally {
             this.jedisPool.returnResource(jedis);
         }
     }
 
     public Object byte2Object(byte[] bytes) {
         if (bytes == null || bytes.length == 0)
             return null;
         try {
             ObjectInputStream inputStream;
             inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
             Object obj = inputStream.readObject();
             return (Object) obj;
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public byte[] object2Bytes(Object value) {
         if (value == null)
             return null;
         ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
         ObjectOutputStream outputStream;
         try {
             outputStream = new ObjectOutputStream(arrayOutputStream);
             outputStream.writeObject(value);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 arrayOutputStream.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return arrayOutputStream.toByteArray();
     }
 
     public byte[] getKey(String key) {
         return key.getBytes();
     }
 
 }
