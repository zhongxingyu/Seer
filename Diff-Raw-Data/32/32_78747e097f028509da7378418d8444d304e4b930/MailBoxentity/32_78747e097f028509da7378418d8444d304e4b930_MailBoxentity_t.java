 package entity;
 
 import static javax.persistence.CascadeType.ALL;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 
 
 @Entity
 public class MailBoxentity {
    private Userentity user;
    
    @OneToOne(cascade = ALL, mappedBy = "mailboxentity")
    @JoinColumn(name = "userentity_id")
    	public Userentity getUser() {
    		return user;
    	}
 
    	public void setUser(Userentity user) {
    		this.user = user;
    	}
    
    
    
 }
