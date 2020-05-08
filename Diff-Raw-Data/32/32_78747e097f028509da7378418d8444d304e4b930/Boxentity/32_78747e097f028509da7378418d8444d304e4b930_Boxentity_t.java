 package entity;
 
 import static javax.persistence.CascadeType.ALL;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Inheritance;
 import javax.persistence.OneToMany;
 
 
 @Entity
 //@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
 public class Boxentity {
 	
 	private int boxID;
 	private String boxName;
 	private Collection<Messageentity> Messages = new ArrayList<Messageentity>(); 
 
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	public int getBoxID() {
 		return boxID;
 	}
 
 	public void setBoxID(int boxID) {
 		this.boxID = boxID;
 	}
 
 	public String getBoxName() {
 		return boxName;
 	}
 
 	public void setBoxName(String boxName) {
 		this.boxName = boxName;
 	}
 	@OneToMany(cascade = ALL, mappedBy = "boxentity")
 	public Collection<Messageentity> getMessages() {
 		return Messages;
 	}
 
 	public void setMessages(Collection<Messageentity> messages) {
 		Messages = messages;
 	}
 }
