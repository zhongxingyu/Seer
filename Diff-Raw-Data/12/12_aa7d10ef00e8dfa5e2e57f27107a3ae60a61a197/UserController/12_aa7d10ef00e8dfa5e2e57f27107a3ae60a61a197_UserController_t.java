 package pg13.persistence;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import pg13.models.User;
 
 /**
  * Similar idea as PuzzleController
  * @author PaymahnMoghadasian
  * 
  */
 public class UserController
 {
 	private final String GUEST_NAME = "Guest";
 	private static long guestPrimaryKey;
 	
 	private static UserController controller = null;
 	private ArrayList<User> users;
 	
 	private UserController()
 	{
 		users = new ArrayList<User>();
 		
 		User guest = new User(1l, GUEST_NAME);
 		UserController.guestPrimaryKey = guest.getPrimaryKey();
 		this.persist(guest);
 	}
 	
 	
 	public static UserController getInstance()
 	{
 		if(controller == null)
 			controller = new UserController();
 		
 		return controller;
 	}
 	
 	public static long getGuestPrimaryKey()
 	{
 		return guestPrimaryKey;
 	}
 	
 	public ArrayList<User> getUsers()
 	{
 		return this.users;
 	}
 	
 	public void persist(User toAdd)
 	{
 		for(User user: this.users)
 		{
 			if(user.getPrimaryKey() == toAdd.getPrimaryKey())
 				throw new IllegalArgumentException("User with primarykey " + toAdd.getPrimaryKey() + " already exists in the databse");
 		}
 		this.users.add(toAdd);
 	}
 
 	public User findUser(long primaryKey)
 	{
 		for(int i =0; i < this.users.size(); i++)
 		{
 			if(users.get(i).getPrimaryKey() == primaryKey)
 				return this.users.get(i);
 		}
 		
 		return null;
 	}
 	
	public ArrayList<Long> getPrimaryKeys()
 	{
 		ArrayList<Long> keys = new ArrayList<Long>();
 		
		for(User user: this.getUsers())
 		{
 			keys.add(user.getPrimaryKey());
 		}
 		
 		Collections.sort(keys);
 		return keys;
 	}
 }
