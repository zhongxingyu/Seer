 /**
  * Model for storing lists
  * 
  * @author		Edward Y. Chen
 * @since		04/25/2013 
  */
 
 package edu.mssm.pharm.maayanlab.Enrichr;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.annotations.DynamicInsert;
 import org.hibernate.annotations.DynamicUpdate;
 
 @Entity
 @DynamicInsert	// Don't need to include all fields in insert, don't know why not on by default
 @DynamicUpdate	// Don't need to update all fields in update, don't know why not on by default
 @Table(name = "lists", catalog = "enrichr")
 public class List implements Serializable {
 
 	private static final long serialVersionUID = -1387864947273228907L;
 	
 	private int listid;
 	private User user;
 	private String description;
 	private String passkey;
 	private Date created;
 
 	public List() {
 	}
 
 	public List(int listid) {
 		this.listid = listid;
 	}
 
 	public List(int listid, User user, String description) {
 		this(listid, user, description, null);
 	}
 	
 	public List(int listid, User user, String description, String passkey) {
 		this.listid = listid;
 		this.user = user;
 		this.description = description;
 		this.passkey = passkey;
 	}
 
 	@Id
 	@Column(name = "listid", unique = true, nullable = false)
 	public int getListid() {
 		return this.listid;
 	}
 
 	public void setListid(int listid) {
 		this.listid = listid;
 	}
 
 	@ManyToOne(fetch = FetchType.LAZY)
 	@JoinColumn(name = "ownerid")
 	public User getUser() {
 		return this.user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	@Column(name = "description", length = 200)
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Column(name = "passkey", length = 16)
 	public String getPasskey() {
 		return this.passkey;
 	}
 
 	public void setPasskey(String passkey) {
 		this.passkey = passkey;
 	}
 
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "created", nullable = false, length = 19)
 	public Date getCreated() {
 		return this.created;
 	}
 
 	// Shouldn't be used because it uses default timestamp by db
 	public void setCreated(Date created) {
 		this.created = created;
 	}
 }
