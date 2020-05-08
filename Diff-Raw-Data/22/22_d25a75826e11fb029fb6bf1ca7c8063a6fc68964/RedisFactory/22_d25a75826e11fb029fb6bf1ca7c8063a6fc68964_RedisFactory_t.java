 package iie.mm.client;
 
 import redis.clients.jedis.Jedis;
 
 public class RedisFactory {
 	//专门用来操作redis中数据库0,它里面存储的是md5与存储时的返回值的映射
 	
	public static Jedis getNewInstance(String host, int port) {
 		Jedis jedis = new Jedis(host,port);
 		return jedis;
 	}
 	
 }
