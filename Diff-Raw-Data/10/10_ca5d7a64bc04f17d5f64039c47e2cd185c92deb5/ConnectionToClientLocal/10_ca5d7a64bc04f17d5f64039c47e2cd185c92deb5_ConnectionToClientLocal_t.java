 package server;
 
 import action.Message;
 
 import clientData.Client;
 
 import server.ConnectionToClient;
 
 /**
  * Connection to a local player.
  * @author mahogny
  * @author Micket
  */
 public class ConnectionToClientLocal extends ConnectionToClient
 	{
 	public Client localClient;
 	private ServerThread thread;
 	
 	/**
 	 * Random unique ID obtained upon connection. It never changes during a session.
 	 */
 	//public int connectionID;
 	
 	public ConnectionToClientLocal(ServerThread thread)
 		{
 		this.thread=thread;
 		}
 	
 	
 	public void send(Message msg)
 		{
 		localClient.gotMessageFromServer(msg);
 		}
 	
 	@Override
 	public void run()
 		{
 		}
 
 
 	
 	}
