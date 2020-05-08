 package data;
 
 import java.io.Serializable;
 import java.net.InetSocketAddress;
 
 public class UserEntry implements Comparable<UserEntry>, Serializable {
 	private static final long	serialVersionUID	= 1L;
 
 	public enum Offer {
 		NO_OFFER, OFFER_MADE, OFFER_ACCEPTED, OFFER_REFUSED, OFFER_EXCEDED, TRANSFER_STARTED, TRANSFER_IN_PROGRESS, TRANSFER_COMPLETE, TRANSFER_FAILED, OFFER_DROP;
 
 		public String toString() {
 			switch (ordinal()) {
 			case 0:
 				return "No offer";
 			case 1:
 				return "Offer made";
 			case 2:
 				return "Offer accepted";
 			case 3:
 				return "Offer refused";
 			case 4:
 				return "Offer exceded";
 			case 5:
 				return "Transfer started";
 			case 6:
 				return "Transfer in progress";
 			case 7:
 				return "Transfer complete";
 			case 8:
 				return "Transfer failed";
 			default:
 				break;
 			}
 
 			return "";
 		};
 	}
 
	private String  username;
	private String	name;
	private Offer	offer;
	private Long	time;
	private Double	price;
	private Integer	progress;
 	// FIXME: Delete it
	private InetSocketAddress address; 
 
 	public UserEntry(String username, String name, Offer offer, Long time, Double price) {
 		this.username = username;
 		this.name = name;
 		this.offer = offer;
 		this.time = time;
 		this.price = price;
 	}
 
 	public UserEntry(UserEntry user) {
 		this.username = user.getUsername();
 		this.name = user.getName();
 		this.offer = user.getOffer();
 		this.time = user.getTime();
 		this.price = user.getPrice();
 	}
 
 	public UserEntry() {
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public Offer getOffer() {
 		return offer;
 	}
 
 	public void setOffer(Offer offer) {
 		this.offer = offer;
 	}
 
 	public Long getTime() {
 		return time;
 	}
 
 	public void setTime(Long time) {
 		this.time = time;
 	}
 
 	public Double getPrice() {
 		return price;
 	}
 
 	public void setPrice(Double price) {
 		this.price = price;
 	}
 
 	public Integer getProgress() {
 		return progress;
 	}
 
 	public void setProgress(Integer progress) {
 		this.progress = progress;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	@Override
 	public String toString() {
		return name;
 	}
 
 	@Override
 	public int compareTo(UserEntry o) {
 		return this.price.compareTo(o.getPrice());
 	}
 
 	@Override
 	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
 		UserEntry userEntry = (UserEntry) obj;
		return username.equals(userEntry.getName());
 	}
 
 	public InetSocketAddress getAddress() {
 		return address;
 	}
	
 	public void setAddress(InetSocketAddress address) {
 		this.address = address;
 	}
 }
