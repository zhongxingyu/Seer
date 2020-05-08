 package com.agileteam.biddingbidding;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.agileteam.biddingbidding.AuctionEventListener.PriceSource;
 
 
 public class AuctionEvent {
 
 	private final Map<String, String> fields = new HashMap<String, String>();
 
 	public String type() {
 		return get("Event");
 	}
 
 	public int currentPrice() {
 		return getInt("CurrentPrice");
 	}
 
 	public int increment() {
 		return getInt("Increment");
 	}
 
 	private int getInt(String fieldName) {
 		return Integer.parseInt(get(fieldName));
 	}
 
 	private String get(String fieldName) {
 		return fields.get(fieldName);
 	}
 
 	private void addField(String field) {
 		String[] pair = field.split(":");
 		fields.put(pair[0].trim(), pair[1].trim());
 	}
 
 	static AuctionEvent from(String messageBody) {
 		AuctionEvent event = new AuctionEvent();
 		for (String field : fieldsIn(messageBody)) {
 			event.addField(field);
 		}
 
 		return event;
 	}
 
 	static String[] fieldsIn(String messageBody) {
 		return messageBody.split(";");
 	}
 	
 	public PriceSource isFrom(String sniperId){
		return sniperId.equals(bidder()) ? PriceSource.FromSelf : PriceSource.FromOtherBidder;
 	}
 	
 	private String bidder(){
 		return get("Bidder");
 	}
 }
