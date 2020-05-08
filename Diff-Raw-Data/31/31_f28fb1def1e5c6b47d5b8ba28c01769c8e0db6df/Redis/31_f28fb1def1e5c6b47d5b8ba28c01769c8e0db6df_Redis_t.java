 package com.thalesgroup.tcs.tai.gwcalipso;
 
 import redis.clients.jedis.Jedis;
 
 import java.util.Set;
 
 public class Redis {
 
     public static String redis_lookup(String key){
         Jedis jedis = new Jedis("localhost");
         Boolean key_exist = jedis.exists(key);
         if (key_exist) {
             return jedis.get(key);
         }
         else {
             return "";
         }
     }
 
     public static void redis_init() {
         Jedis jedis = new Jedis("localhost");
         jedis.del("coap_servers");
         jedis.sadd("coap_servers", "coap://coap.me");
         jedis.sadd("coap_servers", "coap://localhost");
        jedis.sadd("coap_servers", "coap://sky2:5683");
        System.out.println("Added");
     }
 
     public static void redis_publish(String channel, String message){
         Jedis jedis = new Jedis("localhost");
         jedis.publish(channel, message);
     }
 
     public static Set<String> smembers(String key){
         Jedis jedis = new Jedis("localhost");
         return jedis.smembers(key);
     }
 
     public static void setex(String key, int expire_time, String value){
         Jedis jedis = new Jedis("localhost");
         jedis.setex(key, expire_time, value);
     }
 
 }
