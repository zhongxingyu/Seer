 package org.tedxuba.social.events;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpResponse;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.JsonObjectParser;
 import com.google.api.client.json.jackson2.JacksonFactory;
 
 public abstract class SocialEventListener extends Observable {
 	private final Logger logger = LoggerFactory.getLogger(SocialEventListener.class);
 	
 	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
 	static final JsonFactory JSON_FACTORY = new JacksonFactory();
 	
 	private Map<String,Integer> lastCountMap = new HashMap<String, Integer>();
 	private Map<String,Integer> countDiffMap = new HashMap<String, Integer>();
 	
 	public abstract String getSocialNetworkName();
 	public abstract List<String> getEventNames();
 	public abstract String getRequestUrl(HttpRequestFactory requestFactory);
 	public abstract void parseResponseAndUpdateCount(HttpResponse response);
 	
 	public void setLastCount(String eventName, Integer lastCount) {
 		this.lastCountMap.put(eventName, lastCount);
 	}
 	
 	public Integer getLastCount(String eventName) {
 		return lastCountMap.get(eventName);
 	}
 	
 	public Integer getCountDiff(String eventName) {
 		return countDiffMap.get(eventName);
 	}
 	public void setCountDiff(String eventName, Integer countDiff) {
 		this.countDiffMap.put(eventName, countDiff);
 	}
 	
 	public void start() {
 		Thread thread = new Thread(new Runnable() {
 			
 			public void run() {
 				HttpRequestFactory requestFactory = HTTP_TRANSPORT
 						.createRequestFactory(new HttpRequestInitializer() {
 							public void initialize(HttpRequest request)
 									throws IOException {
 								request.setParser(new JsonObjectParser(JSON_FACTORY));
 							}
 						});
 				String requestUrl = SocialEventListener.this.getRequestUrl(requestFactory);
 				GenericUrl url = new GenericUrl(requestUrl);
 				try {
 					HttpRequest request = requestFactory.buildGetRequest(url);
 				
 					while (true) {
 						logger.debug("Requesting to " + url.toString());
						HttpResponse response = request.execute();
						parseResponseAndUpdateCount(response);
						
						Thread.sleep(1000);
 					}
 				} catch (Exception e) {
 					logger.error(e.getMessage());
 				}
 			}
 			
 		});
 		thread.start();
 	}
 }
