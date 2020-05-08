 
 import java.util.HashMap;
 
 
 public class UserStore {
 	private HashMap<String, ClientInterface> userStore = new HashMap();
 	
 	public ClientInterface getClientByName(String name) {
 		return this.userStore.get(name);
 	}
 	
 	public String[] getAllUser() {
 		return this.userStore.keySet().toArray(new String[0]);
 	}
 	
 	public void addUser(String user, ClientInterface client) {
 		this.userStore.put(user, client);
 	}
 	
 	public void removeUser(String user) {
 		this.userStore.remove(user);
 	}
 }
