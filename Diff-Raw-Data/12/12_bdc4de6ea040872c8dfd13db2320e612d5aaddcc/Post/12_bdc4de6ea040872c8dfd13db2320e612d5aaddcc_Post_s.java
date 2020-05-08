 package models;
 
import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
import play.Logger;

 public abstract class Post {
 	
 	private User owner;
 	private String content;
 	private Date timestamp;
 	private long id;
 	private LinkedList<Vote> votes;	
	
	private static long idCounter = 1;
	
 	
 	protected Post(User inUser, String inContent){
 		this.owner = inUser;
 		this.content = inContent;
 		this.timestamp = new Date(System.currentTimeMillis());
 		this.id = idCounter++;
 		this.votes = new LinkedList<Vote>();
 	}
 	
 	public User getOwner() {
 		return this.owner;
 	}
 	
 	public String getContent() {
 		return this.content;
 	}
 	
 	public Date getTimestamp(){
 		return timestamp;
 	}
 	
 	public long getId(){
 		return id;
 	}
 	
 	/**
 	 * override this method 
 	 */	
 	
 	protected abstract void doDelete();
 	
 	
 	/**
 	 * Rates an entry with +1 when up is true, -1 otherwise.
 	 * 
 	 * @param user
 	 *            - The User who rates the entry.
 	 */
 	private void rate(User user, boolean up) {
 		
 		if(user == null) return;
 		
 		int rateValue = up ? +1 : -1;
 		
 		//check if it is my own post
 		if(user.equals(getOwner()))
 			return;		
 		
 		//check if user already voted
 		for(Vote v : votes){
 			if(v.getUser() != null && v.getUser().equals(user)){
 				v.setValue(rateValue);
 				return;
 			}
 				
 		}		
 		//else new vote
 		votes.add(new Vote(rateValue, user));
 	}
 	
 	
 	/**
 	 * Rates an entry with +1.
 	 * 
 	 * @param user
 	 *            - The user who rates the entry.
 	 */
 	public void rateUp(User user) {
 		rate(user,true);
 	}
 
 	/**
 	 * Rates an entry with -1.
 	 * 
 	 * @param user
 	 *            - The user who rates the entry.
 	 */
 	public void rateDown(User user) {
 		rate(user,false);
 	}
 
 	/**
 	 * Calculates the average mark for the entry. 
 	 * 
 	 */
 	public int getVotation() {		
 		
 		int votation = 0;
 		for (Vote singleVote : votes) {
 			votation += singleVote.getValue();
 		}		
 		return votation;
 
 	}
 	
 	
 	public void setContent(String editedContent){
 		content = editedContent;
 	}
 	
 
 }
