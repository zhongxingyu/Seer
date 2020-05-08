 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package services;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import play.Logger;
 import play.Play;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisShardInfo;
 import redis.clients.jedis.exceptions.JedisConnectionException;
 
 /**
  *
  * @author luciano
  */
 public class RedisImpl implements Redis {
 
	private static final Pattern REDIS_URL_PATTERN = Pattern.compile("^redis://([^:]*):([^@]*)@([^:]*):([^/]*)(/)?");
 	
 	static Jedis jedis;
 
     public Jedis connect() {
         if (jedis == null) {
         	
         	// TODO: Make this into a Play! module
         	JedisShardInfo jedisShardInfo;
         	String redisUrl;
         	Matcher redisUrlMatcher;
         	if ((redisUrl = Play.configuration.getProperty("redis.url")) != null
         			&& (redisUrlMatcher = REDIS_URL_PATTERN.matcher(redisUrl)).matches()) {
         		
        		Logger.info("Connecting to redis using url: " + redisUrl);
        		
         		String name = redisUrlMatcher.group(1);
         		String password = redisUrlMatcher.group(2);
         		String host = redisUrlMatcher.group(3);
         		int port = Integer.parseInt(redisUrlMatcher.group(4));
        		Logger.info("Parsed redis url: name -> " + name + ", password -> " + password + ", host -> " + host + ", port -> " + port);
         		
         		jedisShardInfo = new JedisShardInfo(host, port, name);
         		jedisShardInfo.setPassword(password);
         		
         	} else if (Play.configuration.containsKey("redis.host")) {
         		String host = Play.configuration.getProperty("redis.host");
         		
         		if (Play.configuration.containsKey("redis.port")) {
         			int port = Integer.parseInt(Play.configuration.getProperty("redis.port"));
         			jedisShardInfo = new JedisShardInfo(host, port);
        			Logger.info("Connecting to redis using: host -> " + host + ", port -> " + port);
         		} else {
         			jedisShardInfo = new JedisShardInfo(host);
        			Logger.info("Connecting to redis using: host -> " + host);
         		}
         		
         		if (Play.configuration.containsKey("redis.password")) {
         			jedisShardInfo.setPassword(Play.configuration.getProperty("redis.password"));
         		}
         		
         	} else {
         		throw new JedisConnectionException("No redis configuration found.");
         	}
         	
         	if (Play.configuration.containsKey("redis.timeout")) {
         		int timeout = Integer.parseInt(Play.configuration.getProperty("redis.timeout"));
         		jedisShardInfo.setTimeout(timeout);
         	}
         	
         	
         	jedis = new Jedis(jedisShardInfo);
         	//jedis.ping();
         }
         
         return this.jedis;
     }
     
     
 }
