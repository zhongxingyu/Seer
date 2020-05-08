 package logic;
 
 import requests.xml.XMLSerializable;
 
 
 public class Debt extends XMLSerializable {
 	
 //	private boolean isDone, isConfirmed, isDeleted;
 	/**
 	 * Empty constructor used when restoring objects from XML
 	 */
 	public Debt() {}
 	
 	/**
 	 * Create a new Debt 
 	 * 
 	 * @param id
 	 * @param amount
 	 * @param what
 	 * @param from
 	 * @param to
 	 * @param comment
 	 * @param requestedBy
 	 */
 	public Debt(long id, double amount, String what, User from, User to, String comment, User requestedBy) {
		this(id, amount, what, from, to, comment, requestedBy, DebtStatus.REQUESTED);
 	}
 	
 	/**
 	 * Create a new Debt with a predefined status
 	 * 
 	 * @param id
 	 * @param amount
 	 * @param what
 	 * @param from
 	 * @param to
 	 * @param comment
 	 * @param requestedBy
 	 * @param status
 	 */
	public Debt(long id, double amount, String what, User from, User to, 
			String comment, User requestedBy, DebtStatus status) {
		setAmount(amount);
		setVariable("id", id);
		setWhat(what);
		setFrom(from);
		setTo(to);
		setComment(comment);
		setVariable("requestedBy", requestedBy);
 		setStatus(status);
 	}
 	
 	public DebtStatus getStatus() {
 		return (DebtStatus) getVariable("status");
 	}
 	
 	public void setStatus(DebtStatus status) {
 		setVariable("status", status);
 	}
 	
 //	public void setIsDeleted(boolean isDeleted) {
 //		this.isDeleted = isDeleted;
 //		addVariable("isDeleted", isDeleted);
 //	}
 //	
 //	public boolean isDeleted() {
 //		return isDeleted;
 //	}
 	
 	public long getId(){
 		return (Long) getVariable("id");
 	}
 	
 	public boolean isConfirmed() {
 //		return isConfirmed;
 		return getVariable("status") == DebtStatus.CONFIRMED;
 	}
 	
 //	public void setIsConfirmed(boolean isConfirmed) {
 //		this.isConfirmed = isConfirmed;
 //		addVariable("isConfirmed", isConfirmed);
 //	}
 	
 	public User getRequestedBy() {
 		return (User) getVariable("requestedBy");
 	}
 	
 	public String getComment() {
 		return (String) getVariable("comment");
 	}
 	
 	public void setComment(String comment) {
 		setVariable("comment", comment);
 	}
 	
 //	public boolean isDone() {
 //		return isDone;
 //	}
 //	
 //	public void setDone(boolean isDone) {
 //		this.isDone = isDone;
 //		addVariable("isDone", isDone);
 //	}
 	
 	public double getAmount() {
 		return (Double) getVariable("amount");
 	}
 
 	public String getWhat() {
 		return (String) getVariable("what");
 	}
 
 	public void setId(long id) {
 		setVariable("id", id);
 	}
 	
 	public void setWhat(String what) {
 		setVariable("what", what);
 	}
 
 	public User getFrom() {
 		return (User) getVariable("from");
 	}
 
 	public void setFrom(User from) {
 		setVariable("from", from);
 	}
 
 	public User getTo() {
 		return (User) getVariable("to");
 	}
 
 	public void setTo(User to) {
 		setVariable("to", to);
 	}
 
 	public void setAmount(double amount) {
 		setVariable("amount", amount);
 	}
 	
 	public String toString() {
 		return "Amount: " + getAmount() + ", what: " + getWhat() + ", from: " + getFrom().getUsername() + ", to: " + getTo().getUsername() + ", comment: " + getComment();
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(o instanceof Debt) {
 			return ((Debt) o).getId() == getId();
 		}
 		return false;
 	}
 }
