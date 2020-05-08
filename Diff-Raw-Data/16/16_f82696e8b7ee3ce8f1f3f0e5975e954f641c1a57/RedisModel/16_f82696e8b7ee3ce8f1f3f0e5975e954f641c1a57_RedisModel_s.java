 package com.wk.rivers;
 
 import java.net.URI;
 import java.util.Set;
 
 import redis.clients.jedis.Jedis;
 
 public class RedisModel {
 	private int mRedisDB;
 	private String mRedisURI;
 	private String mRiver;
 	private String mKeySpace;
 	private String mType;
 	private Jedis mJedis;
 	
 	public RedisModel(String river, String type, String redisURI) {
 		mRiver = river;
 		mType = type;
 		mRedisURI = redisURI;
 		mKeySpace = String.format("%s:%s", mRiver, mType);
 	}
 	
 	public Set<String> all() {
 		return getClient().zrevrange(mKeySpace, 0, -1);
 	}
 	
 	public boolean contains(String id) {		
 			return getClient().sismember(mKeySpace+":ids", id);	
 	}
 	
 	public void add(Double rank, String member, String id){		
 		if (getClient().sadd(mKeySpace+":ids", id) != 0) {
 			getClient().zadd(mKeySpace, rank, member);
			broadcastCount();
 		}
 	}
 	
	public void broadcastCount() {
		getClient().publish(mKeySpace, len().toString());
 	}
 	
 	public Long len() {
 		return getClient().zcard(mKeySpace);		
 	}
 	
 	/**
 	 * Get a Redis client
 	 * @return
 	 */
 	private Jedis getClient() {
 		if (mJedis == null || !mJedis.isConnected()) {		
 			URI uri = URI.create(mRedisURI);
 			if (uri.getScheme() != null && uri.getScheme().equals("redis")) {
 				String host = uri.getHost();				
 				int port = uri.getPort();				
 				String userInfo = uri.getUserInfo();				
 				mRedisDB = Integer.parseInt(uri.getPath().split("/", 2)[1]);				
 				
 				mJedis = new Jedis(host, port);
 				if (userInfo != null){
 					String password = userInfo.split(":", 2)[1];					
 					mJedis.auth(password);
 				}
 				mJedis.select(mRedisDB);
 			}
 		}
 		return mJedis;
 	}
 }
