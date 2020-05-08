 package com.vaguehope.senkyou.twitter;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import twitter4j.Twitter;
 import twitter4j.http.AccessToken;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.vaguehope.senkyou.Config;
 
 public final class TwitterFactory {
 	
 	private TwitterFactory () {/* Static helper. */}
 	
 	private static final LoadingCache<String, Twitter> CACHE = CacheBuilder.newBuilder()
 			.maximumSize(Config.USER_COUNT_MAX)
 			.softValues()
 			.expireAfterAccess(Config.USER_AGE_MAX, TimeUnit.MINUTES)
 			.build(new CacheLoader<String, Twitter>() {
 				@Override
				public Twitter load (String username) throws Exception {
 					return makeTwitter(username);
 				}
 			});
 	
 	protected static Twitter makeTwitter (String username) throws IOException {
 		AccessToken appToken = TwitterConfigHelper.readAppAuthData();
 		AccessToken userToken = TwitterConfigHelper.readUserAuthData(username);
 		return new twitter4j.TwitterFactory().getOAuthAuthorizedInstance(appToken.getToken(), appToken.getTokenSecret(), userToken);
 	}
 	
 	public static Twitter getTwitter (String username) throws ExecutionException {
 		return CACHE.get(username);
 	}
 	
 }
