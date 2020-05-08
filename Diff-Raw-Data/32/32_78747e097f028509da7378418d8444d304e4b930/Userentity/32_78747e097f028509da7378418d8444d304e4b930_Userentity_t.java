 package entity;
 
 
 import static javax.persistence.CascadeType.ALL;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 
 @Entity
 public class Userentity {
 	private int userID;
 	private String userName;
	private NewsGroupRightentity right;
     private MailBoxentity mailbox;
     
     @Id
 	@GeneratedValue(strategy=GenerationType.AUTO)  
 	public int getUserID() {
 		return userID;
 	}
 
 	public void setUserID(int userID) {
 		this.userID = userID;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 	@OneToOne(cascade = ALL, mappedBy = "userentity")
 	@JoinColumn(name = "rightentity_id")
	public NewsGroupRightentity getRight() {
 		return right;
 	}
 
	public void setRight(NewsGroupRightentity right) {
 		this.right = right;
 	}
 
 	@OneToOne(cascade = ALL, mappedBy = "userentity")
 	@JoinColumn(name = "mailboxentity_id")
 	public MailBoxentity getMailbox() {
 		return mailbox;
 	}
 
 	public void setMailbox(MailBoxentity mailbox) {
 		this.mailbox = mailbox;
 	}
     
 }
