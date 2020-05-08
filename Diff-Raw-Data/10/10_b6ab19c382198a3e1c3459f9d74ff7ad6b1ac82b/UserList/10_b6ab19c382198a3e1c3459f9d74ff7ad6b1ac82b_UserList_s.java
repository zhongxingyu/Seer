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
   
   public void print() {
     print(false);
   }
   public void print(boolean online) {
     int state = (online == true) ? 1 : 0;
     print(state);
   }
   public void print(int state) {
     String output = "";
     for (User user : this.userHash.values()) {
       if (((state == 0)||(state == 3))&&(!user.isOnline())) {
         output += String.format(" -%s\n",user.name);
         if (MyUtils.debugFlag)
           for (Session s : user.sessions.values())
             output+= String.format("   = %s:%s\n",s.address.getHostName(),s.address.getPort());
       }
       else if (((state == 1)||(state == 3))&&user.isOnline()) {
         output += String.format(" +%s\n",user.name);
         if (MyUtils.debugFlag)
           for (Session s : user.sessions.values())
             output+= String.format("   = %s:%s\n",s.address.getHostName(),s.address.getPort());
       }
     }
     if (output.equals(""))
       output = " (none)\n";
     System.out.printf(output);
   }
 	
 	public int seed()
 	{
 		int count = 0;
 //		User u = this.get("ahmet");
 //		u.meetUser(get("ben"));
 //		u.meetUser(get("harley"));
 //		u.meetUser(get("elaine"));
 //		u.putSession(new InetSocketAddress("128.36.171.168",6789), false);
 //		count++;
 //		u = this.get("nathan");
 //		u.meetUser(get("michelle"));
 //		u.meetUser(get("yang"));
 //		u.putSession(new InetSocketAddress("128.36.156.46",6789), false);
 //		//u.putSession(home);
 //		count++;
 //		u.meetUser(get("ahmet"));
 		User tick = get("tick");
 		//tick.putSession(new InetSocketAddress("tick.zoo.cs.yale.edu",6789));
 		User python = get("python");
 		//python.putSession(new InetSocketAddress("python.zoo.cs.yale.edu",6789));
 		User termite = get("termite");
 		User hornet = get("hornet");
 		User cobra = get("cobra");
 		tick.meetUser(termite);
 		tick.meetUser(cobra);
 		python.meetUser(termite);
 		termite.meetUser(hornet);
 		hornet.meetUser(cobra);
 		
 
 		return count;
 	}
 	
 	public UserList loadFromDatabase(){
 		UserList userList = new UserList(); // haha faked it there is no db yet
 		return userList;
 	}
 } // end of class
 