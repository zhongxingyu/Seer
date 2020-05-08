 package uk.ac.cam.dashboard.models;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.JoinColumn;
 import javax.persistence.OneToOne;
 
 import org.hibernate.annotations.GenericGenerator;
 
 @Entity
 public class Settings {
 
     @Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy="increment")
 	private int id;
 	
 	private boolean signupsOptIn = true;
 	private boolean questionsOptIn = true;
 	private boolean handinsOptIn = true;
 	
	@OneToOne (mappedBy="settings")
	@JoinColumn(name="USER_CRSID")
 	private User user;
 	
 	public Settings() {}
 	
 	// Setters and getters
 	public boolean isSignupsOptIn() {return signupsOptIn;}
 	public void setSignupsOptIn(boolean signupsOptIn) {this.signupsOptIn = signupsOptIn;}
 	
 	public boolean isQuestionsOptIn() {return questionsOptIn;}
 	public void setQuestionsOptIn(boolean questionsOptIn) {this.questionsOptIn = questionsOptIn;}
 	
 	public boolean isHandinsOptIn() {return handinsOptIn;}
 	public void setHandinsOptIn(boolean handinsOptIn) {this.handinsOptIn = handinsOptIn;}
 	
 }
