 package uk.co.eelpieconsulting.busroutes.services;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import uk.co.eelpieconsulting.busroutes.model.Message;
 import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
 import uk.co.eelpieconsulting.busroutes.parsing.CountdownService;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
 import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
 
 @Component
 public class MessageService {
 	
 	private CountdownService countdownService;
 	private StopsService stopsService;
 
 	public MessageService() {
 	}
 	
 	@Autowired
 	public MessageService(CountdownService countdownService, StopsService stopsService) {
 		this.countdownService = countdownService;
 		this.stopsService = stopsService;
 	}
 	
 	public Object getMessages(int stopId) {
 		List<Message> stopMessages = countdownService.getStopMessages(stopId);
 		return filterMessages(stopMessages);		
 	}
 		
 	public List<MultiStopMessage> getMessages(int[] stopIds) throws HttpFetchException, ParsingException {				
 		final List<Message> allStopMessages = countdownService.getMultipleStopMessages(stopIds);		
 		return filterMessages(allStopMessages);
 	}
 	
 	private List<MultiStopMessage> filterMessages(final List<Message> allStopMessages) {
 		final List<Message> currentMessages = filterCurrentMessages(allStopMessages);
 		
 		Map<String, MultiStopMessage> uniqueMessages = new HashMap<String, MultiStopMessage>();
 		for (Message message : currentMessages) {
 			final String hash = getMessageHash(message);
 			MultiStopMessage multiStopMessage = uniqueMessages.get(hash);
 			if (multiStopMessage == null) {
 				multiStopMessage = new MultiStopMessage(hash, message);
 			}
 			multiStopMessage.addStop(stopsService.getStopById(message.getStopId()));
 			uniqueMessages.put(hash, multiStopMessage);
 		}
 		
 		List<MultiStopMessage> results = new ArrayList<MultiStopMessage>(uniqueMessages.values());
 		Collections.sort(results, new MessageStartDateComparator());		
 		return results;
 	}
 
 	private List<Message> filterCurrentMessages(List<Message> allStopMessages) {
 		List<Message> currentMessages = new ArrayList<Message>();
 		for (Message message : allStopMessages) {
 			final boolean isCurrent = message.getStartDate() < (System.currentTimeMillis()) && message.getEndDate() > (System.currentTimeMillis());
 			if (isCurrent) {
 				currentMessages.add(message);			
 			}
 		}
 		return currentMessages;		
 	}
 	
 	private String getMessageHash(Message message) {
 		return DigestUtils.md5Hex(message.getMessage());
 	}
 
 }
