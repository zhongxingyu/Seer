 package burrito.controller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import taco.Controller;
 import burrito.services.FeedsSubscription;
 import burrito.services.FeedsSubscriptionMessage;
 
 import com.google.appengine.api.channel.ChannelMessage;
 import com.google.appengine.api.channel.ChannelService;
 import com.google.appengine.api.channel.ChannelServiceFactory;
 import com.google.gson.Gson;
 
 public class BroadcastMessageController implements Controller<Map<String, String>> {
 
 	private String feedId;
 	private String message;
	private String excludeSubscriptionId;
 	
 	private static final Logger log = Logger.getLogger(BroadcastMessageController.class.getName());
 	
 	
 	@Override
 	public Map<String, String> execute() {
 
 		log("Starting broadcast to feed \""+feedId+"\". Message: \n" + message);
 		
 		ChannelService channelService = ChannelServiceFactory
 				.getChannelService();
 		Map<String, String> map = new HashMap<String, String>();
 		map.put("message", message);
 		map.put("feedId", feedId);
 		String wrapped = new Gson().toJson(map);
 		// get a list of people (browser clients) to notify
 		List<FeedsSubscription> subs = FeedsSubscription.getSubscriptionsForFeed(feedId);
 		
 		log("Found " + subs.size() + " to send the message to");
 		
 		for (FeedsSubscription sub : subs) {
 			if (excludeSubscriptionId != null
 					&& excludeSubscriptionId.equals(sub.getId())) {
 				//don't push to this channel
 				continue;
 			}
 			if (sub.getChannelId() == null) {
 				//polling, store the update until next poll
 				FeedsSubscriptionMessage msg = new FeedsSubscriptionMessage();
 				msg.setFeedId(feedId);
 				msg.setMessage(message);
 				msg.setSubscriptionId(sub.getId());
 				msg.insert();
 				log("Message stored for polling client with subscription id: " + sub.getId());
 			} else {
 				try {
 					// Push to client
 					channelService.sendMessage(new ChannelMessage(sub
 							.getClientId(), wrapped));
 					log("Successful push to client with subscription id " + sub.getId());
 				} catch (Exception e) {
 					System.err.println("Failed: "
 							+ sub.toString()
 							+ ". Exception: " + e.getMessage());
 //					e.printStackTrace();
 				}
 			}
 		}
 		
 		Map<String, String> result = new HashMap<String, String>();
 		result.put("status", "ok");
 		return result;
 		
 		
 	}
 
 
 	private void log(String string) {
 		log.log(Level.INFO, string);
 	}
 
 
 	public String getFeedId() {
 		return feedId;
 	}
 
 
 	public void setFeedId(String feedId) {
 		this.feedId = feedId;
 	}
 
 
 
 	public String getMessage() {
 		return message;
 	}
 
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 
	public String getExcludeSubscriptionId() {
 		return excludeSubscriptionId;
 	}
 
 
	public void setExcludeSubscriptionId(String excludeSubscriptionId) {
 		this.excludeSubscriptionId = excludeSubscriptionId;
 	}
 
 
 
 	
 }
