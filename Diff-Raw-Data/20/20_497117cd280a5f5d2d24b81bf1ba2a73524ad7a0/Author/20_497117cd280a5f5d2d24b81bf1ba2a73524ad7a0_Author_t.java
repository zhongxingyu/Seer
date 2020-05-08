 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.Query;
 
 import play.db.jpa.JPA;
 import play.db.jpa.Model;
 
 @Entity
 public class Author extends Model {
 
 	/** The author's name **/
	public String name;
 	/** A foreign user id **/
 	private String uid;
 	/** The time of the author's last login **/
 	public Date lastLogin;
 	/** The time when the author was created **/
 	private Date creationDate;
 	/** The author's password. Only store the hash-code in here! **/
 	public int password;
 
 	public Author(String name) {
 		this.name = name;
 		this.creationDate = new Date();
 	}
 
 	public Author() {
 		this.creationDate = new Date();
 	}
 
 	/* Getters and setters */
 	public String getName() {
 		return name;
 	}
 
 	public String getUid() {
 		return uid;
 	}
 
 	public void setUid(String uid) {
 		this.uid = uid;
 	}
 
 	public Date getLastLogin() {
 		return lastLogin;
 	}
 
 	public void setLastLogin(Date lastLogin) {
 		this.lastLogin = lastLogin;
 	}
 
 	public Date getCreationDate() {
 		return this.creationDate;
 	}
 
 }
