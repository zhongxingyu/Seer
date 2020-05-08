 package ch.unibe.ese.calendar;
 
import java.util.HashSet;
 import java.util.List;
import java.util.Set;
 
 
 /**
  * Represents a User. Users have a unique names identifying them.
  * 
  *
  */
 public class User {
 	
 	public static final User ADMIN = new User("admin");
 
 	private String userName;
 	private Object password;
	private Set<User> myContacts = new HashSet<User>();
 
 	/**
 	 * creates a user with the specified username and password
 	 * @param userName
 	 * @param password
 	 */
 	public User(String userName, String password) {
 		this.userName = userName;
 		this.password = password;
 	}
 	
 	/**
 	 * creates a user with the specified username and a random password
 	 * @param userName
 	 */
 	public User(String userName) {
 		this(userName, Integer.toString((int)(Math.random()*1000)));
 	}
 	
 	public void addToMyContacts(User userToAdd) {
 		if (!myContacts.contains(userToAdd)) {
 			myContacts.add(userToAdd);
 		}
 	}
 	
 	public void removeFromMyContacts(User userToRemove) {
 		myContacts.remove(userToRemove);
 	}
 	
	public Set<User> getMyContacts() {
 		return myContacts;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((userName == null) ? 0 : userName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (userName == null) {
 			if (other.userName != null)
 				return false;
 		} else if (!userName.equals(other.userName))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "User [userName=" + userName + "]";
 	}
 
 	public String getName() {
 		return userName;
 	}
 
 	public Object getPassword() {
 		return password;
 	}
 
 }
