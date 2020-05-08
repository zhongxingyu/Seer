 package org.footware.server.db;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import org.footware.shared.dto.CommentDTO;
 
 @Entity
 public class Comment implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private long id;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
	@Column(nullable=false)
 	private Track track;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
	@Column(nullable=false)
 	private User user;
 	
 	@Column(length=256,nullable=false)
 	private String text;
 	
 	@Column(nullable=false)
 	private Date time;
 	
 	protected Comment() {}
 	
 	public Comment(CommentDTO comment) {
 		this.track = new Track(comment.getTrack());
 		this.user = new User(comment.getUser());
 		this.text = comment.getText();
 		this.time = comment.getTime();
 	}
 	
 	public Comment(String comment, User user) {
 		this.text = comment;
 		this.user = user;
 	}
 	
 	protected long getId() {
 		return id;
 	}
 	
 	public Track getTrack() {
 		return track;
 	}
 //	public void setTrack(Track track) {
 //		this.track = track;
 //	}
 	public User getUser() {
 		return user;
 	}
 //	public void setUser(User user) {
 //		this.user = user;
 //	}
 	public String getText() {
 		return text;
 	}
 	public void setText(String text) {
 		this.text = text;
 	}
 	public Date getTime() {
 		return time;
 	}
 	public void setTime(Date time) {
 		this.time = time;
 	}
 
 }
