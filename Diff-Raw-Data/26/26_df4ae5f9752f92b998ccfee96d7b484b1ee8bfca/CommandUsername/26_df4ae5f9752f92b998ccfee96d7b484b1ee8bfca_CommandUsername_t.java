 package aidancbrady.server.commands;
 
 import java.io.PrintWriter;
 
 import aidancbrady.server.FileHandler;
 import aidancbrady.server.ICommand;
import aidancbrady.server.ServerConnection;
 import aidancbrady.server.ServerCore;
 import aidancbrady.server.SocketConnection;
 
 public class CommandUsername implements ICommand
 {
 	@Override
 	public void handle(SocketConnection connection, String[] params, PrintWriter printWriter)
 	{
 		try {
 			if(params.length != 2 || params[1] == null || params[1].equals(""))
 			{
 				throw new Exception();
 			}
 			
 			if(connection.getServerConnection().isAuthenticated())
 			{
 				if(ServerCore.instance().displayName.equals(params[1]))
 				{
 					printWriter.println("You cannot use the same username as the moderator!");
 					return;
 				}
 				
 				if(ServerCore.instance().cachedUsers.get(params[1]) != null)
 				{
 					printWriter.println("There is already a user with that username!");
 					return;
 				}
 				
				String oldName = connection.getServerConnection().user.username;
				
				ServerCore.instance().distributeMessage("<" + oldName + "'s username was changed to " + params[1] + ">");
				ServerCore.instance().theGui.appendChat("<" + oldName + "'s username was changed to " + params[1] + ">");
 				
 				ServerCore.instance().cachedUsers.remove(connection.getServerConnection().user.username);
 				connection.getServerConnection().user.username = params[1];
 				ServerCore.instance().cachedUsers.put(connection.getServerConnection().user.username, connection.getServerConnection().user);
 				
 				printWriter.println("/user:" + connection.getServerConnection().user.username);
 				
				for(ServerConnection conn : ServerCore.instance().connections.values())
				{
					if(conn.isAuthenticated())
					{
						if(conn.socketConnection.printWriter != null)
						{
							conn.socketConnection.printWriter.println("/namechange:" + oldName + ":" + params[1]);
						}
					}
				}
				
 				FileHandler.saveCaches();
 			}
 		} catch(Exception e) {
 			printWriter.println("Invalid command usage.");
 		}
 	}
 }
