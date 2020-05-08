 package models;
 
 import java.sql.Timestamp;
 
 public class Comment {
 	
 	private User owner;
 	private Votable commentedVotable;
 	private String content;
 	private Timestamp timestamp;
 	private int id;
 	
	public Comment(User owner,Votable commentedVotable,String content,Timestamp timestamp, int id) {
 		this.owner = owner;
 		this.commentedVotable = commentedVotable;
 		this.content = content;
		this.timestamp = timestamp;
		this.id = id;
 	}
 	
 	public void setOwner(User owner) {
 		this.owner = owner;
 	}
 	public User getOwner() {
 		return owner;
 	}
 	public void setCommentedVotable(Votable commentedVotable) {
 		this.commentedVotable = commentedVotable;
 	}
 	public Votable getCommentedVotable() {
 		return commentedVotable;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public String getContent() {
 		return content;
 	}
 	public void setTimestamp(Timestamp timestamp) {
 		this.timestamp = timestamp;
 	}
 	public Timestamp getTimestamp() {
 		return timestamp;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public int getId() {
 		return id;
 	}
 	
 	
 	
 }
