 package com.synovel.social;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpResponseException;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson.JacksonFactory;
 import com.google.api.client.http.json.JsonHttpParser;
 import com.google.api.client.util.Key;
 
 public class SocialConnector
 {
 	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
 	static final JsonFactory JSON_FACTORY = new JacksonFactory();
 	static final String SHARED_SECRET = "ABCDE";
 
 	private final Logger logger;
 	
 	private String baseUrl;
 	private String sessionUrlFormat;
 	private String subscribeUrlFormat;
 	private String disconnectUrlFormat;
 	
 	private String userNotifyChannelFormat;
 	private String userPresenceChannelFormat;
 	private String orgPresenceChannelFormat;
 
 	public static class AuthData {
 		@Key
 		public String user;		
 		@Key
 		public String org;
 	}
 	
 	public static class ResultData {
 		@Key
 		public String status;
 		@Key
 		public String reason;
 		
 		public boolean isSuccess() {
 			return status.equals("SUCCESS");
 		}
 	}
 	
 	public SocialConnector() {
 		logger = LoggerFactory.getLogger(getClass().getName());
 
         Properties p = new Properties();
         try {
           p.load(this.getClass().getResourceAsStream("/social.properties"));
         } catch (Exception ex) {
         }
 
     	logger.debug("base url: " + p.getProperty("url.base"));
     	
     	baseUrl = p.getProperty("url.base");
     	sessionUrlFormat 	= baseUrl + p.getProperty("url.session.format");
     	subscribeUrlFormat 	= baseUrl + p.getProperty("url.subscribe.format");
     	disconnectUrlFormat = baseUrl + p.getProperty("url.disconnect.format");
     	
     	userNotifyChannelFormat = p.getProperty("channel.user.notify.format");
     	userPresenceChannelFormat = p.getProperty("channel.user.presence.format");
     	orgPresenceChannelFormat = p.getProperty("channel.org.presence.format");
 	}
 	
 	public AuthData validateSession(String appSessionId) throws IOException {
 		if (appSessionId.equals(SHARED_SECRET)) {
 			AuthData authData = new AuthData();
 			authData.user = "server";
 			authData.org = "server";
 			return authData;
 		}
 
 		HttpRequestFactory requestFactory = 
 				HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
 					public void initialize(HttpRequest request) {
 						request.addParser(new JsonHttpParser(JSON_FACTORY));
 					}
 				});
 		
 		String url = String.format(sessionUrlFormat, appSessionId);
 		HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
 		
 		return request.execute().parseAs(AuthData.class);
 	}
 	
 	public ResultData validateSubscribe(String appSessionId, String channelId, String userId, String orgId) throws IOException {
 		ResultData resultData = new ResultData();
 		
 		if (appSessionId.equals(SHARED_SECRET)) {
 			resultData.status = "SUCCESS";
 			resultData.reason = "Passed with shared secret";
 			return resultData;
 		}
 		
 		String userNotifyChannel = String.format(userNotifyChannelFormat, userId);
 		String userPresenceChannel = String.format(userPresenceChannelFormat, userId);
		String orgPresenceChannel = String.format(orgPresenceChannelFormat, userId);
 		
 		if (channelId.equals(userNotifyChannel) ||
 			channelId.equals(userPresenceChannel) || 
 			channelId.equals(orgPresenceChannel)) {
 			resultData.status = "SUCCESS";
 			resultData.reason = "Subscription to own/org notify/presence channel allowed by default";
 			return resultData; 
 		}
 		
 		HttpRequestFactory requestFactory = 
 			HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
 				public void initialize(HttpRequest request) {
 					request.addParser(new JsonHttpParser(JSON_FACTORY));
 				}
 			});
 		
 		String url = String.format(subscribeUrlFormat, appSessionId, channelId);
 		HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
 		
 		try {
 			return request.execute().parseAs(ResultData.class);
 		} catch (HttpResponseException ex) {
 			resultData = ex.getResponse().parseAs(ResultData.class);
 			logger.debug("SocialConnector request failed. Reason: " + resultData.reason);
 			throw ex;
 		}
 	}
 	
 	public ResultData validatePublish(String appSessionId, String channelId) throws IOException {
 		ResultData resultData = new ResultData();
 		resultData.status = "FAILURE";
 		resultData.reason = "Only apps with shared secret can publish";
 		
 		if (appSessionId.equals(SHARED_SECRET)) {
 			resultData.status = "SUCCESS";
 			resultData.reason = "Passed with shared secret";
 		}
 		
 		return resultData;
 	}
 	
 	public void removeSession(String appSessionId) throws IOException {
 		HttpRequestFactory requestFactory = 
 			HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
 				public void initialize(HttpRequest request) {
 					request.addParser(new JsonHttpParser(JSON_FACTORY));
 				}
 			});
 		
 		String url = String.format(disconnectUrlFormat, appSessionId);
 		HttpRequest request = requestFactory.buildGetRequest(new GenericUrl(url));
 		
 		request.execute();
 	}
 }
