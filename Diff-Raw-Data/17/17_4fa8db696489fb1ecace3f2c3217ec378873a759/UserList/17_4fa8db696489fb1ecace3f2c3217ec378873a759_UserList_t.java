 /*  Ahmet Aktay and Nathan Griffith
  *  DarkChat
  *  CS435: Final Project
  */
import java.net.InetSocketAddress;
 import java.util.HashMap;
 
 public class UserList {
 	public HashMap<String, User> userHash;
 	
 	public UserList() {
 		this.userHash = new HashMap<String, User>(); 
 	}
 	
 	public User get(String name){
 		return get(name, false);
 	}
 	public User get(String name, Boolean getOnly)
 	{
 		User user = userHash.get(name);
 		if ((user==null) && !getOnly) {
 			user = new User(name);
 			put(user);
 		}
 		return user;
 	}
 	public User put(User user){
 		return userHash.put(user.name, user);
 	}
 	
 	public int seed()
 	{
		InetSocketAddress home = new InetSocketAddress("localhost",6789);
 		int count = 0;
 		User u = new User("ahmet");
 		userHash.put(u.name, u);
		u.putSession(home);
 		count++;
 		u = new User("nathan");
 		userHash.put(u.name, u);
		u.putSession(home);
		u.putSession(home);
 		count++;
 		return count;
 	}
 	
 	public UserList loadFromDatabase(){
 		UserList userList = new UserList(); // haha faked it there is no db yet
 		return userList;
 	}
 } // end of class
 