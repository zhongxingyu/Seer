 package fr.midix.services.boireverre.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 /**
  *
  * @author midix
  */
 @Entity
@Table(name = "rendez-vous")
 public class RendezVous implements Serializable{
     
     private Place place;
     @ManyToMany
     private List<User> users;
     private int maxUser;
     private MyDate date;
     private int minAge;
     private int maxAge;
     @Id
     @GeneratedValue
     private long id;
     private User creatorUser;
 
 
 	public RendezVous() {
     	this.users = new ArrayList<User>();
     }
 
     public RendezVous(Place place, int maxUser, MyDate date, int minAge, int maxAge, User userCreator){
     	this();
     	this.place = place;
     	this.maxUser = maxUser;
     	this.date = date;
     	this.minAge = minAge;
     	this.maxAge = maxAge;
     	this.creatorUser = userCreator;
     	this.users.add(this.creatorUser);
     	
     }
     
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public Place getPlace() {
         return place;
     }
 
     public void setPlace(Place place) {
         this.place = place;
     }
 
     public List<User> getUsers() {
         return users;
     }
     
     public void addUser(User user){
         this.users.add(user);
     }
 
     public void setUsers(List<User> users) {
         this.users = users;
     }
 
     public int getMaxUser() {
         return maxUser;
     }
 
     public void setMaxUser(int maxUser) {
         this.maxUser = maxUser;
     }
 
 
 
 	public int getMinAge() {
 		return minAge;
 	}
 
 
 
 	public void setMinAge(int minAge) {
 		this.minAge = minAge;
 	}
 
 
 
 	public int getMaxAge() {
 		return maxAge;
 	}
 
 
 
 	public void setMaxAge(int maxAge) {
 		this.maxAge = maxAge;
 	}
 
 
 
 	public MyDate getDate() {
 		return date;
 	}
 
 
 
 	public void setDate(MyDate date) {
 		this.date = date;
 	}
     
 	public User getCreatorUser() {
 		return creatorUser;
 	}
 
 	public void setCreatorUser(User creatorUser) {
 		this.creatorUser = creatorUser;
 	}
 }
