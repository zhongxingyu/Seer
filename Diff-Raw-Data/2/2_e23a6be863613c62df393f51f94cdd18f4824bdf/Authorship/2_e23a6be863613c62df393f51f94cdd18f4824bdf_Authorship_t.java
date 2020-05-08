 package cz.cvut.fel.bupro.model;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 
 import javax.persistence.Column;
 import javax.persistence.Embeddable;
 import javax.persistence.ManyToOne;
 
 import cz.cvut.fel.bupro.TimeUtils;
 
 @Embeddable
 public class Authorship implements Serializable {
 	private static final long serialVersionUID = -6239486295089657373L;
 
 	@ManyToOne(optional = false)
 	private User author;
 	@Column(nullable = false)
 	private Timestamp creationTime;
 
 	public Authorship() {
 		// Default constructor JPA required
 	}
 
 	public Authorship(User author) {
 		this(author, TimeUtils.createCurrentTimestamp());
 	}
 
 	public Authorship(User author, Timestamp creationTime) {
 		this.author = author;
 		this.creationTime = creationTime;
 	}
 
 	public User getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(User author) {
 		this.author = author;
 	}
 
 	public Timestamp getCreationTime() {
 		return creationTime;
 	}
 
 	public void setCreationTime(Timestamp creationTime) {
 		this.creationTime = creationTime;
 	}
 
 	@Override
 	public String toString() {
		return getClass().getName() + " [creator=" + author + ", creationTime=" + creationTime + "]";
 	}
 }
