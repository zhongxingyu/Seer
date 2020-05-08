 package edu.sjsu.cmpe.library.domain;
 
 public class Review {
 	
 	private long id;
     private int rating;
     private String comment;
    
 	public long getId() {
		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public int getRating() {
 		return rating;
 	}
 
 	public void setRating(int rating) {
 		this.rating = rating;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 	
 
 }
