 package cl.own.usi.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class User implements Comparable<User>, Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	String userId;
 	String email;
 	String password;
 	String firstname;
 	String lastname;
 	int score = 0;
 
 	public String getUserId() {
 		return userId;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	List<Integer> answers = new ArrayList<Integer>();
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getFirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String firstname) {
 		this.firstname = firstname;
 	}
 
 	public String getLastname() {
 		return lastname;
 	}
 
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void setScore(int score) {
 		this.score = score;
 	}
 
 	// TODO NO !! move it to a UserComparator()
 	public int compareTo(User o) {
 		if (getScore() == o.getScore()) {
			return getEmail().compareTo(o.getEmail());
 		} else if (getScore() > o.getScore()) {
 			return -1;
 		} else {
 			return 1;
 		}
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 
 		if (obj == this) {
 			return true;
 		}
 
 		if (obj.getClass() != getClass()) {
 			return false;
 		}
 		//
 		// Customer rhs = (Customer) obj;
 		// return new EqualsBuilder()
 
 		if (obj instanceof User) {
 			User other = (User) obj;
 			return getEmail().equals(other.getEmail());
 		} else {
 			return false;
 		}
 	}
 }
