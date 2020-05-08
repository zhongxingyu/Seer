 package core.im;
 
 import org.jivesoftware.smack.packet.Presence;
 
 public class Buddy {
 	public String alias;
 	public String groupName;
 	public String mergeID;
 	public String userID;
 	public String client;
 	public String screenname;
 	public String status;
 	public String message;
 	
 	boolean isOffline;
 	
 	public Buddy()
 	{
 		status = "offline";
 		isOffline = true;
 	}
 
 	
 	public void setPresence(Presence p)
 	{
		if(p.getMode() == Presence.Mode.available || p.getMode() == Presence.Mode.chat || p.getStatus() != null)
 		{
 			status = "Available";
 			isOffline = false;
 		}
 		else if(p.getMode() == Presence.Mode.away || p.getMode() == Presence.Mode.dnd)
 		{
 			status = "Away";
 			isOffline = false;
 		}
 		else if (p.getMode() == Presence.Mode.xa) //No idea what the connection between xa and idle actually is
 		{
 			status = "Idle";
 			isOffline = false;
 		}
 		else
 		{
 			status = "offline";
 			isOffline = true;
 		}
 	}
 	
 	public boolean getOffline()
 	{
 		return isOffline;
 	}
 	
 	public void setStatusMessage(String s)
 	{
 		message = s;
 	}
 	
 }
