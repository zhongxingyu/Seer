 package com.vaguehope.senkyou.twitter;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.vaguehope.senkyou.Config;
 
 public final class TweetCacheFactory {
 	
 	private TweetCacheFactory () {/* Static helper. */}
 	
 	private static final LoadingCache<String, TweetCache> CACHE = CacheBuilder.newBuilder()
 			.maximumSize(Config.USER_COUNT_MAX)
 			.softValues()
 			.expireAfterAccess(Config.USER_AGE_MAX, TimeUnit.MINUTES)
 			.build(new CacheLoader<String, TweetCache>() {
 				@Override
				public TweetCache load (String username) throws ExecutionException {
 					return new TweetCache(username);
 				}
 				
 			});
 	
 	public static TweetCache getTweetCache (String username) throws ExecutionException {
 		return CACHE.get(username);
 	}
 	
 }
