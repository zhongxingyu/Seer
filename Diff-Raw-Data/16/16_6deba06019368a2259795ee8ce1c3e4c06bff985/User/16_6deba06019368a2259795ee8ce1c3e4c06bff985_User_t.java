 package Logic;
 
 import java.util.ArrayList;
 
 public class User {
 
 	private String name, email, username;
 	private ArrayList<Notification> notifications;
 
 	public User(String name, String email, String username) {
 		this.name = name;
 		this.email = email;
 		this.username = username;
 		notifications = new ArrayList<Notification>();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public void addNotification(Notification notification) {
 		notifications.add(notification);
 	}
 	
 	public void removeNotification(Notification notification) {
 		notifications.remove(notification);
 	}
 	public static User toUser(String arg){
 		String[] args = arg.split("-");
 		String name = args[0];
 		String email = args[1];
 		String username = args[2];
 		User user = new User(name, email, username);
 		return user;
 	}
 }
