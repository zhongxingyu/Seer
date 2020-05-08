 package cs4253;
 
 import java.util.TreeMap;
 
 public class UserList {
     private TreeMap<String, IRCUser> users;
 
     public UserList(){
         users = new TreeMap<String, IRCUser>();
     }
 
     public synchronized void add(IRCUser u){
         users.put(u.getUsername(),u);
     }
     
     public synchronized void remove(IRCUser u){
     	users.remove(u.getUsername());
     }
     
     public synchronized void remove(String u){
     	users.remove(u);
     }
     
    public void sendMessage(String message, String username) throws NullPointerException{
     	users.get(username).sendMessage(message);
     }
     
     public void sendMessageToAll(String message){
     	for(String u : users.keySet())
     		users.get(u).sendMessage(message);
     }
     
     public void sendMessageToAllExcept(String message, String username){
     	for(String u : users.keySet())
     		if(!users.get(u).getUsername().equalsIgnoreCase(username))
     			users.get(u).sendMessage(message);
     }
 }
