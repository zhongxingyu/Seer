 package by.itransition.fanfic.domain;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 

 public class Vote {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int id;
 	
 	@ManyToOne
 	private User user;
 	
 	private int rating;
 
 	public int getId() {
 		return id;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public int getRating() {
 		return rating;
 	}
 
 	public void setRating(int rating) {
 		this.rating = rating;
 	}
 
 }
