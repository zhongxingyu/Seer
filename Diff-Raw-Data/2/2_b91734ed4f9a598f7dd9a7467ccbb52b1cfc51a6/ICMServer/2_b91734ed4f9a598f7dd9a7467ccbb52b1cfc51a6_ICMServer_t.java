 package icm.server;
 
 import icm.intent.Intent;
 import icm.intent.Intent.IntentType;
 import icm.server.intenthandler.IntentHandler;
 import icm.server.intenthandler.LoginIntentHandler;
 import icm.server.intenthandler.UserMailBrowseIntentHandler;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import ocsf.server.AbstractServer;
 import ocsf.server.ConnectionToClient;
 
 /**
  * OUR SERVER
  * @author Ni
  *
  */
 public class ICMServer extends AbstractServer
 {
 	// fields
 	//-------------------------------------------------------------------------
 	@SuppressWarnings("rawtypes")
 	HashMap<Intent.IntentType, IntentHandler> intentHanlders;
 	
 	// Constructors
 	//-------------------------------------------------------------------------
 	/**
 	 * created a new ICMServer instance to listen to port 'port'
 	 * @param port the server will attempt to listen to this port
 	 */
 	@SuppressWarnings("rawtypes")
 	public ICMServer(int port) 
 	{
 		super(port);
 		intentHanlders = new HashMap<Intent.IntentType, IntentHandler>();
 		intentHanlders.put(IntentType.LOGIN_REQUEST, new LoginIntentHandler());
 		intentHanlders.put(IntentType.USER_MAIL_BROWSE, new UserMailBrowseIntentHandler());
 	}
 	
 	// overrides
 	//-------------------------------------------------------------------------
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	protected void handleMessageFromClient(Object msg, ConnectionToClient client) 
 	{
 		if( msg instanceof Intent )
 		{
 			Intent intent = (Intent) msg;
 			IntentHandler handler = intentHanlders.get(intent.getType());
 			
 			if(handler!=null)
 			{
 				// the intent handler may have a response.
 				// the response will be sent back to the client.
 				Object response = handler.execute(intent);
 				if(response!=null)
 				{
 					try
 					{
						client.sendToClient(response);
 					} 
 					catch (IOException e) {e.printStackTrace();}
 				}
 			}
 		}
 	}
 	
 	@Override
 	protected void serverStarted()
 	{
 		System.out.println("Server listening for connections on port " + getPort());
 	}
 	
 	@Override
 	protected void serverStopped()
 	{
 		System.out.println("Server has stopped listening for connections.");
 	}
 	
 	@Override
 	protected synchronized void clientException(ConnectionToClient client,
 			Throwable exception)
 	{
 		exception.printStackTrace();
 	}
 }
