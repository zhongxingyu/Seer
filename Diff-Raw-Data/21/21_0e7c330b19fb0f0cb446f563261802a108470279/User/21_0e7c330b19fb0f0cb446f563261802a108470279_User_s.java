 package cs4520.server;
 
 import java.util.Calendar;
 
 /**
  * 
  * @author James Reed
  *
  */
 
 public class User {
 	
 	private String secret;
 	private int loginAttempts = 0;
	private final long timeout = 15*60;
 	private long lastTimeCheck = 0;
 	private boolean locked = false;
 	
 	public User(String secret)
 	{
 		this.secret = secret;
 	}
 	
 	public void incrementAttempts()
 	{
 		if(loginAttempts == 3)
 		{
 			locked = true;
 			lastTimeCheck = Calendar.getInstance().getTimeInMillis();
 		}
 		else
 			loginAttempts++;
 	}
 	
 	public boolean isLocked()
 	{
 		if(locked && ((Calendar.getInstance().getTimeInMillis() - lastTimeCheck) > timeout))
 		{
 			locked = false;
 			loginAttempts = 0;
 		}
 		return locked;
 	}
 	
 	public int getAttempts()
 	{
 		return loginAttempts;
 	}
 
 	
 	public String getSecret()
 	{
 		return secret;
 	}
 
 }
