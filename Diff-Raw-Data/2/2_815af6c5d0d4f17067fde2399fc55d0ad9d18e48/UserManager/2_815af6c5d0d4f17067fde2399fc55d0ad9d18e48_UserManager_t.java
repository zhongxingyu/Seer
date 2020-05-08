 package pg13.business;
 
 import java.util.ArrayList;
 
 import pg13.app.PG13;
 import pg13.app.Services;
 import pg13.models.User;
 import pg13.persistence.DataAccess;
 
 public class UserManager
 {
 	private DataAccess dataAccess;
 	
 	public UserManager()
 	{
 		dataAccess = Services.getDataAccess(PG13.dbName);
 	}
 	
 	public User addUser(String name)
 	{
 		User user = null;
 		if(isValidUsername(name))
 		{
 			ArrayList<Long> keys = dataAccess.getSortedUserPrimaryKeys();
			long primaryKey = keys.size() > 0 ? keys.get(keys.size() - 1).longValue() + 1 : 1;
 			user = new User(primaryKey, name);
 			this.dataAccess.saveUser(user);
 		}
 		return user;
 	}
 	
 	public User findUser(User user)
 	{
 		return this.findUser(user.getPrimaryKey());
 	}
 	
 	public User findUser(long primaryKey)
 	{
 		return this.dataAccess.findUser(primaryKey);
 	}
 	
 	public String getNameOfUser(long primaryKey)
 	{
 		User user = this.findUser(primaryKey);
 		
 		return user.getName();
 	}
 	
 	public ArrayList<String> getNamesOfAllUsers()
 	{
 		ArrayList<User> users = this.dataAccess.getUsers();
 		ArrayList<String> names = new ArrayList<String>();
 		
 		for(int i =0; i < users.size(); i++)
 		{
 			names.add(users.get(i).getName());
 		}
 		
 		return names;
 	}
 	
 	public ArrayList<User> getAllUsers()
 	{
 		return this.dataAccess.getUsers();
 	}
 	
 	public long getGuestPrimaryKey()
 	{
 		return this.dataAccess.getGuestPrimaryKey();
 	}
 	
 	private boolean isValidUsername(String name)
 	{
 		boolean valid = true;
 		
 		if(name.length() >= 1 && name.length() <= 15)
 		{
 			ArrayList <String> currentUsers = getNamesOfAllUsers();
 			
 			for(int i = 0; i < currentUsers.size() && valid; i++)
 			{
 				if(name.equalsIgnoreCase(currentUsers.get(i)))
 				{
 					valid = false;
 				}
 			}
 			
 			//valid characters
 			if(valid)
 			{
 				for(int i = 0; i < name.length(); i++)
 				{
 					if(!Character.isLetterOrDigit(name.charAt(i)))
 					{
 						valid = false;
 					}
 				}
 			}
 		}
 		else
 		{
 			valid = false;
 		}
 		
 		return valid;
 	}
 }
