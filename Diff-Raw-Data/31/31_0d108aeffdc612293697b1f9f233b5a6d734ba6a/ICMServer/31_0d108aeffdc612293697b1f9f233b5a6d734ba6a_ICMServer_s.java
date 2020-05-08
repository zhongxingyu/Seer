 package icm.server;
 
 import icm.intent.Intent;
 import icm.intent.Intent.IntentType;
 import icm.server.intenthandler.BrowseInfoSystemsIntentHandler;
 import icm.server.intenthandler.CreateRequestIntentHandler;
 import icm.server.intenthandler.IntentHandler;
 import icm.server.intenthandler.LoginIntentHandler;
 import icm.server.intenthandler.UserMailBrowseIntentHandler;
 import icm.server.intenthandler.UserRequestBrowseIntentHandler;
 
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
 		intentHanlders.put(IntentType.BROWSE_INFO_SYSTEMS, new BrowseInfoSystemsIntentHandler());
 		intentHanlders.put(IntentType.CREATE_REQUEST, new CreateRequestIntentHandler());
 		intentHanlders.put(IntentType.USER_REQUEST_BROWSE, new UserRequestBrowseIntentHandler());
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
 				handler.execute(intent, client);
 			}
 		}
 	}
 	
 	@Override
 	protected void serverStarted()
 	{
 		System.out.println("Server listening for connections on port " + getPort()+")");
 	}
 	
 	@Override
 	protected void serverStopped()
 	{
 		System.out.println("Server has stopped listening for connections.");
 	}
 	
 	@Override
 	protected synchronized void clientException(ConnectionToClient client, Throwable exception)
 	{
 		exception.printStackTrace();
 		try 
 		{
 			client.sendToClient("unknown error occoured");
 		} 
 		catch (IOException e) {e.printStackTrace();}
 	}
 	
 	@Override
 	protected void clientConnected(ConnectionToClient client) 
 	{
 		System.out.println("new client connected: "+client.getInetAddress());
 	}
 	
 	@Override
 	protected synchronized void clientDisconnected(ConnectionToClient client) 
 	{
 		System.out.println("client disconnected: "+client.getInetAddress());
 	}
 }
