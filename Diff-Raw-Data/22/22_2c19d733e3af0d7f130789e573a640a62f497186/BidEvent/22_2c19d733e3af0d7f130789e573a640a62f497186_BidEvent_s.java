 package server.analytics;
 
 public class BidEvent extends Event{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private enum Type {BID_PLACED, BID_OVERBID, BID_WON;}
 	private long auctionId;
 	private String username;
 	private double price;
 
 	public BidEvent(){
 
 	}
 
 	public void setType(String type) {
 		if(type.equals(Type.BID_PLACED.toString()) || type.equals(Type.BID_OVERBID.toString()) || type.equals(Type.BID_WON.toString())) {
 			this.type = type;
 		}
 	}
 
 	public long getAuctionId() {
 		return auctionId;
 	}
 
 	public void setAuctionId(long auctionId) {
 		this.auctionId = auctionId;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public double getPrice() {
 		return price;
 	}
 
 	public void setPrice(double price) {
 		this.price = price;
 	}
 	
 	public String toString() {
 		String prefix = super.toString();
 		String suffix ="";
 		
 		if(this.type.equals("BID_PLACED")) {
 			suffix = "user " + username + " placed bid " + price + " on auction " + auctionId;
 		}
 		
 		if(this.type.equals("BID_OVERBID")) {
			suffix = "OVERBIDt";
 		}
 		
 		if(this.type.equals("BID_WON")) {
 			suffix = "WON";
 		}
 		
 		return prefix + suffix;
 	}
 }
