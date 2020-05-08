 
 package socialnetwork;
 
 import java.util.ArrayList;
 
 
 public class User {
 
 	public User(int id, String username, String password) {
             this.id = id;
             this.username = username;
             this.password = password;
             loggedIn = false;
 			subscriptions = new ArrayList();
 	}
 	
 	public void addSub(int subid){
		subscriptions.ensureCapacity(subscriptions.size());
 		subscriptions.add(subid);
 	}
 	
 	int id;
 	String username;
 	String password;
 	ArrayList<Integer> subscriptions;
 	boolean loggedIn;
 }
