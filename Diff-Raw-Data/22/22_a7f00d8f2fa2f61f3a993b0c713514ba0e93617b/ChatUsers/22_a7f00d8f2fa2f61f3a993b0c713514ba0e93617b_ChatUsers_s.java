 import java.util.ArrayList;
 
 
 public class ChatUsers {
 	
 	private ArrayList<String> users = new ArrayList<>();
 
 	public boolean contains(String username) {
 		return users.contains(username);
 	}
 
 	public void addUser(String username) {
 		users.add(username);
 	}
 
 	public void removeUser(String username) {
 		users.remove(username);
 	}
 
 }
