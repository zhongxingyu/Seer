 /**
  * 
  */
 package at.ac.tuwien.dslab2.domain;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * @author klaus
  * 
  */
 public class Auction {
	private final int id;
 	private final String description;
 	private final User owner;
 	private final SortedSet<Bid> bids;
 	private final int duration;
 	private final Date start;
 
 	/**
 	 * @param id
 	 * @param description
 	 * @param owner
 	 * @param duration
 	 */
	public Auction(int id, String description, User owner, int duration) {
 		this.id = id;
 		this.description = description;
 		this.owner = owner;
 		this.duration = duration;
 		this.start = new Date();
 		this.bids = Collections.synchronizedSortedSet(new TreeSet<Bid>());
 	}
 
 	public void addBid(Bid bid) {
 		if (bid == null)
 			throw new IllegalArgumentException("Bid is null");
 
 		this.bids.add(bid);
 	}
 
	public int getId() {
 		return this.id;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public User getOwner() {
 		return this.owner;
 	}
 
 	public int getDuration() {
 		return this.duration;
 	}
 
 	public Bid getHighestBid() {
 		if (bids == null || bids.isEmpty())
 			return null;
 		return bids.last();
 	}
 
 	public User getHighestBidder() {
 		Bid highest = getHighestBid();
 		if (highest == null)
 			return null;
 		return highest.getUser();
 	}
 
 	public Date getEndDate() {
 		if (start == null)
 			return null;
 
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(start);
 		cal.add(Calendar.SECOND, duration);
 
 		return cal.getTime();
 	}
 
 	public String getEndDateFormatted() {
 		if (getEndDate() == null)
 			return null;
 
 		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT,
 				DateFormat.LONG);
 		return df.format(this.getEndDate());
 	}
 
 	public boolean isExpired() {
 		Date now = new Date();
 		Date end = getEndDate();
 
 		return now.after(end);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
		result = prime * result + this.id;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Auction other = (Auction) obj;
 		if (this.id != other.id)
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 
 		return (this.id + ". ")
 				+ (this.description != null ? "'" + this.description + "' "
 						: "")
 				+ (this.owner != null ? this.owner + " " : "")
 				+ (this.getEndDateFormatted() != null ? this
 						.getEndDateFormatted() + " " : "")
 				+ (this.getHighestBid() != null ? this.getHighestBid() + " "
 						: "0.00 ")
 				+ (this.getHighestBidder() != null ? this.getHighestBidder()
 						: "none");
 	}
 }
