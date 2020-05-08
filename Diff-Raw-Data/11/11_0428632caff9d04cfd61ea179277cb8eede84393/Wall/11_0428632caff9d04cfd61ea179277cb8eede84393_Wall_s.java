 package uk.org.cowgill.james.jircd.commands;
 
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.Command;
 import uk.org.cowgill.james.jircd.Message;
 import uk.org.cowgill.james.jircd.Permissions;
 import uk.org.cowgill.james.jircd.Server;
 
 /**
  * The WALL command - sends a message to all clients
  * 
  * @author James
  */
 public class Wall implements Command
 {
 	@Override
 	public void run(Client client, Message msg)
 	{
 		//Check permissions
 		if(client.hasPermission(Permissions.wall))
 		{
 			//Send WALLOPS message
			Message wMsg = new Message("WALL", client);
 			wMsg.appendParam(msg.getParam(0));
 			
 			Client.sendTo(Server.getServer().getRegisteredClients(), wMsg);
 		}
 		else
 		{
 			client.send(client.newNickMessage("481").appendParam("WALL: Permission Denied"));
 		}
 	}
 
 	@Override
 	public int getMinParameters()
 	{
 		return 1;
 	}
 
 	@Override
 	public String getName()
 	{
 		return "WALL";
 	}
 
 	@Override
 	public int getFlags()
 	{
 		return FLAG_NORMAL;
 	}
 }
