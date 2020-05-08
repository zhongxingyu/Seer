 
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
		subscriptions.ensureCapacity(subscriptions.size() + 1);
 		subscriptions.add(subid);
 	}
 	
	public void addManySubs(int[] subids){
		subscriptions.ensureCapacity(subscriptions.size() + subids.length);
		for(int i=0; i<subids.length; i++){
			subscriptions.add(subids[i]);
		}
	}
	
 	int id;
 	String username;
 	String password;
 	ArrayList<Integer> subscriptions;
 	boolean loggedIn;
 }
