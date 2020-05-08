 package model;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class Conversation implements Comparable<Conversation>
 {
 	private DateFormat dateFormat;
 	private String phoneNumber;
 	private ArrayList<Message> outbox;
 	private ArrayList<Message> inbox;
 	private Contact contact;
 	private Date date;
 	
 	public Conversation(String phoneNumber) 
 	{
 		this.dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		this.phoneNumber = phoneNumber;
 		this.date = new Date();
 		this.inbox = new ArrayList<Message>();
 		this.outbox = new ArrayList<Message>();
 	}
 	public Conversation(Contact contact)
 	{
 		this.dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 		this.contact = contact;
 		this.phoneNumber = this.contact.getPhoneNumber();
 		this.date = new Date();
 		this.inbox = new ArrayList<Message>();
 		this.outbox = new ArrayList<Message>();
 	}
 
 	public void setPhoneNumber(String phoneNumber) 
 	{
 		this.phoneNumber = phoneNumber;
 	}
 
 	public String getPhoneNumber() 
 	{
 		return phoneNumber;
 	}
 	
 	public void setDate(Date date) 
 	{
 		this.date = date;
 	}
 	public String getDateString()
 	{
 		return this.dateFormat.format(this.date);
 	}
 	public Date getDate()
 	{
 		return date;
 	}
 	
 	public ArrayList<Message> getInbox()
 	{
 		return new ArrayList<Message>(inbox);
 	}
 	
 	public ArrayList<Message> getOutbox() 
 	{
 		return new ArrayList<Message>(outbox);	
 	}
 	
 	public Contact getContact() 
 	{
 		return contact;
 	}
 	
 	/**
 	 * Adds message to inbox
 	 * @param theMessage
 	 */
 	public void addToInbox(Message theMessage)
 	{
 		boolean caught = false;
 		for (int i = 0; i < inbox.size(); i++)
 			if (theMessage.compareTo(inbox.get(i)) < 0) {
 				inbox.add(i, theMessage);
 				caught = true;
 				break;
 			}
 		if (!caught)
 			inbox.add(theMessage);
 	}
 	/**
 	 * Adds message to outbox
 	 * @param theMessage
 	 */
 	public void addToOutbox(Message theMessage)
 	{
 		boolean caught = false;
 		for (int i = 0; i < outbox.size(); i++) 
 			if (theMessage.compareTo(outbox.get(i)) < 0) {
 				outbox.add(i, theMessage);
 				caught = true;
 				break;
 			}
 		if (!caught)
 			outbox.add(theMessage);
 	}
 	public Message createMessage(String content, String fromNumber, boolean outgoingMessage) 
 	{
 		Message newMessage = new Message(content, fromNumber);
 		if (outgoingMessage)
 			addToOutbox(newMessage);
 		else
 			addToInbox(newMessage);
 		return newMessage;
 	}
 	public Message createMessage(String content, Contact contact, boolean outgoingMessage)
 	{
 		System.out.println("her2!");
 		if (contact == null) {
 			System.out.println("Contact points to null.");
 			return null;
 		}
 		Message newMessage = new Message(content, contact);
 		if (outgoingMessage)
 			addToOutbox(newMessage);
 		else
 			addToInbox(newMessage);
 		return newMessage;
 	}
 	public Message getLatestMessage() {
 		if (!this.outbox.isEmpty() && !this.inbox.isEmpty()) {
 			Message latestOutbox = this.outbox.get(this.outbox.size()-1);
 			Message latestInbox = this.inbox.get(this.inbox.size()-1);
 			return (latestOutbox.getDateString().compareTo(latestInbox.getDateString()) > 0) ? latestOutbox : latestInbox;
 		} else {
 			return (!this.outbox.isEmpty()) ? this.outbox.get(this.outbox.size()-1) : this.inbox.get(this.inbox.size()-1);
 		}
 	}
 	
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	
 	@Override
 	public int compareTo(Conversation conversation) {
 		if (conversation == null) {
 			System.out.println("compareTo(): Conversation points to null.");
 			return 0;
 		}
 		return this.getDate().compareTo(conversation.getDate());
 	}
 	
 	public ArrayList<Message> getMessages() {
 		ArrayList<Message> newArray = new ArrayList<>();
 		int i1 = 0;
 		int i2 = 0;
 		while(i1<this.inbox.size() && i2<this.outbox.size()) {
 			if (this.inbox.get(i1).compareTo(this.outbox.get(i2)) <= 0) {
 				newArray.add(this.inbox.get(i1));
 				i1++;
 			}
 			else {
 				newArray.add(this.outbox.get(i2));
 				i2++;
 			}
 		}
 		while (i1 < this.inbox.size()) { 
 			newArray.add(this.inbox.get(i1));
 			i1++;
 		}
 		while (i2 < this.outbox.size()) { 
 			newArray.add(this.outbox.get(i2));
 			i2++;
 		}
 		return newArray;
 	}
 }
