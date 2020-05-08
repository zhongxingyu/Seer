 package entity;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
import mailbox.Box;

 
 @Entity
 public class Messageentity {
 	private int messageID;
 	private String senderName;
 	private String receiverName;
 	private String sendingDate;
 	private String subject;
 	private String body;
 	private boolean alreadyRead=false;
     private Boxentity box;
     
     @Id
     @GeneratedValue(strategy=GenerationType.AUTO)	
     	public int getMessageID() {
     		return messageID;
     	}
 
     	public void setMessageID(int messageID) {
     		this.messageID = messageID;
     	}
 
     	public String getSenderName() {
     		return senderName;
     	}
 
     	public void setSenderName(String senderName) {
     		this.senderName = senderName;
     	}
 
     	public String getReceiverName() {
     		return receiverName;
     	}
 
     	public void setReceiverName(String receiverName) {
     		this.receiverName = receiverName;
     	}
 
     	public String getSendingDate() {
     		return sendingDate;
     	}
 
     	public void setSendingDate(String sendingDate) {
     		this.sendingDate = sendingDate;
     	}
 
     	public String getSubject() {
     		return subject;
     	}
 
     	public void setSubject(String subject) {
     		this.subject = subject;
     	}
 
     	public String getBody() {
     		return body;
     	}
 
     	public void setBody(String body) {
     		this.body = body;
     	}
 
     	public boolean isAlreadyRead() {
     		return alreadyRead;
     	}
 
     	public void setAlreadyRead(boolean alreadyRead) {
     		this.alreadyRead = alreadyRead;
     	}
 
     	@ManyToOne
     	@JoinColumn(name="boxentity_id")
     	public Boxentity getBox() {
     		return box;
     	}
 
     	public void setBox(Boxentity box) {
     		this.box = box;
     	}
     
     
     
 }
