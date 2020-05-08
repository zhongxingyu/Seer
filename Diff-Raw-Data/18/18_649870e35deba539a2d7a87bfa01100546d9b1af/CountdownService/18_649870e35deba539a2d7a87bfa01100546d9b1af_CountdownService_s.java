 package uk.co.eelpieconsulting.busroutes.parsing;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import uk.co.eelpieconsulting.busroutes.model.Message;
 import uk.co.eelpieconsulting.busroutes.services.caching.MemcachedCache;
 import uk.co.eelpieconsulting.countdown.api.CountdownApi;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
 import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
 import uk.co.eelpieconsulting.countdown.model.StopBoard;
 
 @Component
 public class CountdownService {
 	
 	private static final int ONE_HOUR = 3600;
 
 	private static Logger log = Logger.getLogger(CountdownService.class);
 	
 	private static final String MESSAGES_CACHEKEY_PREFIX = "busroutesstopmessages";
 	private MemcachedCache memcachedCache;
 	private CountdownApi countdownApi;
 
 	@Autowired
 	public CountdownService(MemcachedCache memcachedCache) {
 		this.memcachedCache = memcachedCache;
 		countdownApi = new CountdownApi("http://countdown.api.tfl.gov.uk");	// TODO inject
 	}
 	
 	public StopBoard getStopBoard(int stopId) {		
 		try {
 			return countdownApi.getStopBoard(stopId);
 		} catch (HttpFetchException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParsingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		return null;
 	}
 	
 	public List<Message> getStopMessages(int stopId) {		
 		@SuppressWarnings("unchecked")
 		List<Message> stopMessages = (List<Message>) memcachedCache.get(getCacheKeyForStopMessages(stopId));
 		if (stopMessages != null) {
 			log.info("Cache hit for stop messages: " + stopId);
 			return stopMessages;
 		}
 		
 		try {
 			log.info("Fetching stop messages from countdown api: " + stopId);
 			stopMessages = countdownApi.getStopMessages(stopId);
 			if (stopMessages != null) {
 				memcachedCache.put(getCacheKeyForStopMessages(stopId), ONE_HOUR, stopMessages);
 			}
 			return stopMessages;
 			
 		} catch (HttpFetchException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParsingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public List<Message> getMultipleStopMessages(int[] stopIds) {
 		final List<Message> messages = new ArrayList<Message>();
 		for (int stopId : stopIds) {
 			messages.addAll(getStopMessages(stopId));
 		}
 		return messages;
 	}
 	
 	private String getCacheKeyForStopMessages(int stopId) {
 		return MESSAGES_CACHEKEY_PREFIX + stopId;
 	}
 }
