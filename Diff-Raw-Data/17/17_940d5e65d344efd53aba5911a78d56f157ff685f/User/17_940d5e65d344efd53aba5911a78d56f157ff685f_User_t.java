 package logic;
 import java.util.List;
 
 
 public class User {
 
 	private String username, password, surName, lastName;
 	private List<User> friends;
 	private List<Debt> debtList;
 	private boolean isOnline;
 	
 	public User(String username, String password) {
 		this.username = username;
 		this.password = password;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
	
	public boolean isOnline() {
		return isOnline;
	}
	
	public void setIsOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
 }
