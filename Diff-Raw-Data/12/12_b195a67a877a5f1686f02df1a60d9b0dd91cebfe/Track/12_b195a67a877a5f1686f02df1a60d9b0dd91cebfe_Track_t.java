 package org.footware.server.db;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import org.footware.shared.dto.CommentDTO;
 import org.footware.shared.dto.TrackDTO;
 import org.hibernate.annotations.ManyToAny;
 
 @Entity
 public class Track implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.IDENTITY)
 	private long id;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
	//@Column(nullable=false)
 	private User user;
 	
 	@Column(length=128,nullable=false)
 	private String filename;
 	
 	@Column(length=128)
 	private String path;
 	
 	@Column(length=256)
 	private String notes;
 	
 	//@Enumerated()
 	private int publicity;
 	
 	private boolean commentsEnabled;
 	private int trackpoints;
 	private int length;
 	private Date startTime;
 	
 	@ManyToAny(metaColumn=@Column(name="comment_id"), fetch=FetchType.LAZY)
 	private List<Comment> comments;
 	
 	protected Track() {}
 	
 	public Track(User u, String filename, String path) {
 		this.user = u;
 		this.filename = filename;
 		this.path = path;
 	}
 	
 	public Track(TrackDTO track) {
 		this.id = track.getId();
 		this.user = new User(track.getUser());
 		this.filename = track.getFilename();
 		this.path = track.getPath();
 		this.notes = track.getNotes();
 		this.publicity = track.getPublicity();
 		this.commentsEnabled = track.isCommentsEnabled();
 		this.trackpoints = track.getTrackpoints();
 		this.length = track.getLength();
 		this.startTime = track.getStartTime();
 		List<CommentDTO> comments = track.getComments();
 		if (comments != null) {
 			this.comments = new ArrayList<Comment>();
 			for (CommentDTO comment : comments)
 				this.comments.add(new Comment(comment));
 		}
 	}
 	
 	public long getId() {
 		return id;
 	}
 	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 	public String getFilename() {
 		return filename;
 	}
 	public void setFilename(String filename) {
 		this.filename = filename;
 	}
 	public String getPath() {
 		return path;
 	}
 	public void setPath(String path) {
 		this.path = path;
 	}
 	public String getNotes() {
 		return notes;
 	}
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 	public int getPublicity() {
 		return publicity;
 	}
 	public void setPublicity(int publicity) {
 		this.publicity = publicity;
 	}
 	public boolean isCommentsEnabled() {
 		return commentsEnabled;
 	}
 	public void setCommentsEnabled(boolean commentsEnabled) {
 		this.commentsEnabled = commentsEnabled;
 	}
 	public int getTrackpoints() {
 		return trackpoints;
 	}
 	public void setTrackpoints(int trackpoints) {
 		this.trackpoints = trackpoints;
 	}
 	public int getLength() {
 		return length;
 	}
 	public void setLength(int length) {
 		this.length = length;
 	}
 	public Date getStartTime() {
 		return startTime;
 	}
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 	
 	public List<Comment> getComments() {
 		return comments;
 	}
 	public void addComment(Comment comment) {
 		if (comments == null)
 			comments = new ArrayList<Comment>();
 		comments.add(comment);
 	}
 	
 }
