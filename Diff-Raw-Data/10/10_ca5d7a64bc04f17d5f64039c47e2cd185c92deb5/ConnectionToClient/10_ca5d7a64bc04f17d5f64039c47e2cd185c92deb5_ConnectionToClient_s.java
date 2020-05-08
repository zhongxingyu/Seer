 package server;
 
 import action.Message;
 
 /**
  * Player - Either a remote connection or an AI
  * @author mahogny
  *
  */
 abstract public class ConnectionToClient extends Thread
 	{
 
 	public String nick="<unset>";
 	abstract public void send(Message msg);
 	
 	@Override
 	abstract public void run();
 
 	public void doFinalHandshake(ServerThread thread)
 		{
 		//Update list of connections
 		thread.broadcastUserlistToClients();
 		send(thread.createMessageGameTypesToClients());
 		send(thread.createMessageGameSessionsToClients());
 		}
 	}
