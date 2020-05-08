 package models;
 
 import play.*;
 import play.db.jpa.*;
 
 import javax.persistence.*;
 import java.util.*;
 
 @Entity
 public class Receipt extends Model
 {
 	public String title;
 	public Date created;
 	public Date cleared;
 
 	public int tip;
 
 	// Owning side
 	@ManyToOne
 	public User owner;
 
 	// Owning side
 	@ManyToMany(cascade = CascadeType.PERSIST)
 	public Set<User> members;
 
 	@Lob
 	public String description;
 
 	// Inverse side
 	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
 	public List<Comment> comments;
 
 	// Inverse side
 	@OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL)
 	public List<Subpot> subpots;
 
 	@ManyToMany(cascade=CascadeType.ALL)
 	public List<Payment> payments;
 
 	public Receipt(String title, User owner, String description)
 	{
 		this.title = title;
 		this.owner = owner;
 		this.description = description;
 		this.created = new Date();
 		this.comments = new ArrayList<Comment>();
 		this.members = new TreeSet<User>();
 		this.subpots = new ArrayList<Subpot>();
 	}
 
 	/**
 	 * @return Total amount of money on this receipt
 	 */
 	public int getTotal()
 	{
 		int amount = 0;
 		for (Subpot pot : subpots)
 		{
 			amount += pot.getTotal();
 			// Add restAmount here, since Subpot does not know number of members
 			amount += (members.size() - pot.cases.size()) * pot.restAmount;
			amount += tip;
 		}
 		return amount;
 	}
 	
 	/**
 	 * @param user
 	 * @return The amount of money user should pay
 	 */
 	public int getTotal(User user)
 	{
 		int amount = 0;
 		for (Subpot pot : subpots)
 		{
 			amount += pot.getTotal(user);
 		}
 		
 		// Calculate amount of tip user should pay
 		// if user has X% of non-tip debt, he should pay X% of the tip
 		int allUsers = getTotal() - tip;
 		double percentage = amount / (double) allUsers;
 		amount += tip * percentage;
 		
 		return amount;
 	}
 	
 	/**
 	 * Returns whether user has paid money for this receipt
 	 * @param user
 	 * @return True iff user has a payment associated with this receipt
 	 */
 	public boolean hasPayment(User user)
 	{
 		//TODO(dschlyter) later - verify if payments are for correct amount (turns into graph problem)
 		if (!members.contains(user)) return true;
 		
 		for (Payment p : payments) {
 			if (p.payer.equals(user)) return true;
 		}
 		
 		return false;
 	}
 
 	public String toString()
 	{
 		return "Receipt by " + owner + " for " + getTotal() + " SEK";
 	}
 	
 	/**
 	 * @param payer
 	 * @param receiver
 	 * @return all receipts from owner, where payer has made no payment
 	 */
 	public static List<Receipt> unpayedReceipts(User owner, User payer) {
 		List<Receipt> ret = new ArrayList<Receipt>();
 		
 		outer: for(Receipt r : owner.receipts) {
 			if(r.members.contains(payer)) {
 				for(Payment p : r.payments) {
 					if(p.payer == payer) continue outer;
 				}
 				ret.add(r);
 			}
 		}
 		
 		return ret;
 	}
 	
 
 
 }
