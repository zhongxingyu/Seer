 package com.jjaz.aetherflames;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
 import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
 import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
 import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
 import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
 import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
 import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
 import org.andengine.extension.multiplayer.protocol.util.MessagePool;
 import org.andengine.extension.multiplayer.protocol.util.WifiUtils;
 import org.andengine.util.debug.Debug;
 
 import com.jjaz.aetherflames.AetherFlamesConstants;
 import com.jjaz.aetherflames.messages.phone.*;
 import com.jjaz.aetherflames.messages.client.ConnectionEstablishClientMessage;
 import com.jjaz.aetherflames.messages.matchmaker.*;
 
 public class MatchmakerClient implements AetherFlamesConstants {
 
 	// Networking variables
	private static final String MATCHMAKER_IP_ADDRESS = "206.76.108.25";
 	private static final int MATCHMAKER_PORT = 5555;
 	private static ServerConnector<SocketConnection> mMatchmakerConnector;
 	private static MessagePool<IMessage> mMessagePool;
 	static boolean matchmakerFound;
 	static ReentrantLock talkingToMatchmakerLock = new ReentrantLock();
 	static Condition doneTalkingToMatchmaker = talkingToMatchmakerLock.newCondition();
 	static boolean matchmakerCommunicationComplete;
 	
 	static GameServer server;
 	static HashMap<String, GameServer> serverList;
 	
 	public MatchmakerClient() {
 		matchmakerFound = false;
 		MatchmakerClient.mMessagePool = new MessagePool<IMessage>();
 		
 		initMessagePool();
 		
 		try
 		{
 			MatchmakerClient.mMatchmakerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(MATCHMAKER_IP_ADDRESS, MATCHMAKER_PORT)), new MatchmakerConnectorListener());
 		}
 		catch (UnknownHostException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		registerMessageHandlers();
 	}
 	
 	/**
 	 * Initializes the message pool
 	 */
 	private static void initMessagePool() {
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_CONNECTION_CLOSE, ConnectionClosePhoneMessage.class);
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_CONNECTION_ESTABLISH, ConnectionEstablishPhoneMessage.class);
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_CURRENT_PLAYER_COUNT, CurrentPlayerCountPhoneMessage.class);
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_GET_FIRST_SERVER, GetFirstServerPhoneMessage.class);
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_GET_SERVER_LIST, GetServerListPhoneMessage.class);
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_START_SERVER, StartServerPhoneMessage.class);
 		MatchmakerClient.mMessagePool.registerMessage(FLAG_MESSAGE_PHONE_GAME_START, GameStartPhoneMessage.class);
 	}
 	
 
 	/**
 	 * Registers all matchmaker messages to be handled by this client
 	 */
 	private static void registerMessageHandlers() {
 		try {
 			/*this.mMatchmakerConnector.registerServerMessage(FLAG_MESSAGE_MATCHMAKER_CONNECTION_CLOSE, ConnectionCloseMatchmakerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final ConnectionCloseMatchmakerMessage gameStateMessage = (ConnectionCloseMatchmakerMessage)pServerMessage;
 					DistributedFixedStepPhysicsWorld.this.handleGameStateMessage(gameStateMessage);
 				}
 			});*/
 			MatchmakerClient.mMatchmakerConnector.registerServerMessage(FLAG_MESSAGE_MATCHMAKER_CONNECTION_REJECTED_PROTOCOL_MISMATCH, ConnectionRejectedProtocolMismatchMatchmakerMessage.class, new IServerMessageHandler<SocketConnection>() {
 			@Override
 			public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 				final ConnectionRejectedProtocolMismatchMatchmakerMessage connectionRejectedProtocalMismatch = (ConnectionRejectedProtocolMismatchMatchmakerMessage)pServerMessage;
 				// do something about this later
 			}
 			});
 			MatchmakerClient.mMatchmakerConnector.registerServerMessage(FLAG_MESSAGE_MATCHMAKER_CONNECTION_ESTABLISH, ConnectionEstablishMatchmakerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					MatchmakerClient.matchmakerFound = true; // connection established
 				}
 			});
 			MatchmakerClient.mMatchmakerConnector.registerServerMessage(FLAG_MESSAGE_MATCHMAKER_FREE_SERVER, FreeServerMatchmakerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final FreeServerMatchmakerMessage freeServerMessage = (FreeServerMatchmakerMessage)pServerMessage;
 					System.out.println("Got free server message.");
 					MatchmakerClient.handleFreeServerMessage(freeServerMessage);
 				}
 			});
 			MatchmakerClient.mMatchmakerConnector.registerServerMessage(FLAG_MESSAGE_MATCHMAKER_SERVER_LIST, ServerListMatchmakerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final ServerListMatchmakerMessage serverListMessage = (ServerListMatchmakerMessage)pServerMessage;
 					MatchmakerClient.handleServerListMessage(serverListMessage);
 				}
 			});
 			MatchmakerClient.mMatchmakerConnector.registerServerMessage(FLAG_MESSAGE_MATCHMAKER_NO_SERVER_FOUND, NoServerFoundMatchmakerMessage.class, new IServerMessageHandler<SocketConnection>() {
 				@Override
 				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
 					final NoServerFoundMatchmakerMessage noServerFoundMessage = (NoServerFoundMatchmakerMessage)pServerMessage;
 					MatchmakerClient.handleNoServerFoundMessage();
 				}
 			});
 			
 			MatchmakerClient.mMatchmakerConnector.getConnection().start();
 
 		} catch (final Throwable t) {
 			Debug.e(t);
 		}
 	}
 
 	/**
 	 * Only returned if a server or a server list was requested. Let the client know
 	 * that the matchmaker could not find its request.
 	 */
 	protected static void handleNoServerFoundMessage() {
 		server = null;
 		serverList = null;
 		
 		try
 		{
 			talkingToMatchmakerLock.lock();
 			matchmakerCommunicationComplete = true;
 			doneTalkingToMatchmaker.signalAll();
 		}
 		finally
 		{
 			talkingToMatchmakerLock.unlock();
 		}		
 	}
 
 	/**
 	 * Handle free server messages. This message contains a single server name/IP that
 	 * the client can try to connect to.
 	 * 
 	 * @param message The message to handle
 	 */
 	public static void handleFreeServerMessage(FreeServerMatchmakerMessage message) {
 		server = message.mServer;
 		
 		try
 		{
 			talkingToMatchmakerLock.lock();
 			matchmakerCommunicationComplete = true;
 			doneTalkingToMatchmaker.signalAll();
 		}
 		finally
 		{
 			talkingToMatchmakerLock.unlock();
 		}
 	}
 
 	/**
 	 * Handle server list messages. This message contains a serialized list of
 	 * servers.
 	 * 
 	 * @param message The message to handle
 	 */
 	public static void handleServerListMessage(ServerListMatchmakerMessage message) {
 		serverList = message.mServerList;
 		
 		try
 		{
 			talkingToMatchmakerLock.lock();
 			matchmakerCommunicationComplete = true;
 			doneTalkingToMatchmaker.signalAll();
 		}
 		finally
 		{
 			talkingToMatchmakerLock.unlock();
 		}
 	}
 
 	/**
 	 * Should return a single server, however that is structured.
 	 */
 	public static GameServer requestSingleServer(int numPlayersDesired) {
 		GetFirstServerPhoneMessage message = (GetFirstServerPhoneMessage)MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_GET_FIRST_SERVER);
 		message.SetDesiredPlayers((short) numPlayersDesired);
 		matchmakerCommunicationComplete = false;
 		// send the message
 		try {
 			MatchmakerClient.mMatchmakerConnector.sendClientMessage(message);
 			System.out.println("Sent out a single server request.");
 			MatchmakerClient.mMessagePool.recycleMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 		}
 		
 		// SHOULD WAIT HERE FOR THE RESPONSE AND RETURN THAT SERVER
 		//try
 		{
 			//talkingToMatchmakerLock.lock();
 			while(!matchmakerCommunicationComplete)
 			{
 				//doneTalkingToMatchmaker.await();
 			}
 		}
 		//catch (InterruptedException e)
 		{
 			//e.printStackTrace();
 		}
 		//finally
 		{
 			//talkingToMatchmakerLock.unlock();
 		}
 		return server;
 	}
 	
 	/**
 	 * Should return a server list, however that is structured.
 	 */
 	public static HashMap<String, GameServer> requestServerList() {
 		GetServerListPhoneMessage message = (GetServerListPhoneMessage)MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_GET_SERVER_LIST);
 		
 		// send the message
 		try {
 			MatchmakerClient.mMatchmakerConnector.sendClientMessage(message);
 			MatchmakerClient.mMessagePool.recycleMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 		}
 		
 		// SHOULD WAIT HERE FOR THE RESPONSE AND RETURN THAT LIST
 		try
 		{
 			talkingToMatchmakerLock.lock();
 			matchmakerCommunicationComplete = false;
 			while(!matchmakerCommunicationComplete)
 			{
 				doneTalkingToMatchmaker.await();
 			}
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			talkingToMatchmakerLock.unlock();
 		}
 		
 		return serverList;
 	}
 	
 	/**
 	 * Inform the matchmaker that this phone is a server.
 	 * @param numPlayersDesired 
 	 */
 	public static void startServer(GameServer gs) {
 		StartServerPhoneMessage message = (StartServerPhoneMessage)MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_START_SERVER);
 		
 		message.mServer = gs;
 		MatchmakerClient.server = gs;
 		
 		// send the message
 		try {
 			MatchmakerClient.mMatchmakerConnector.sendClientMessage(message);
 			MatchmakerClient.mMessagePool.recycleMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 		}
 	}	
 	
 	public class MatchmakerConnectorListener implements ISocketConnectionServerConnectorListener {
 		@Override
 		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
 			final ConnectionEstablishPhoneMessage connectionEstablishPhoneMessage = (ConnectionEstablishPhoneMessage)MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_CONNECTION_ESTABLISH);
 			connectionEstablishPhoneMessage.setProtocolVersion(PROTOCOL_VERSION);
 			try {
 				System.out.println("OnStarted called.");
 				mMatchmakerConnector.sendClientMessage(connectionEstablishPhoneMessage);
 			} catch (IOException e) {
 				Debug.e(e);
 			}
 			MatchmakerClient.mMessagePool.recycleMessage(connectionEstablishPhoneMessage);
 		}
 
 		@Override
 		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
			AetherFlamesActivity.afa.toast("Couldn't find matchmaker!");
 			//AetherFlamesActivity.this.finish();
 		}
 	}
 
 	public static void sendGameStartPhoneMessage(int players) {
 		GameStartPhoneMessage message = (GameStartPhoneMessage) MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_GAME_START);
 		MatchmakerClient.server.setNumPlayers(players); // update the number of players
 		message.setServer(MatchmakerClient.server);
 		try {
 			mMatchmakerConnector.sendClientMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 		} finally {
 			MatchmakerClient.mMessagePool.recycleMessage(message);
 		}		
 	}
 
 	public static void sendCurrentPlayerCountPhoneMessage(int players) {
 		if (players != 1) { // don't send this message if we just started the server
 			CurrentPlayerCountPhoneMessage message = (CurrentPlayerCountPhoneMessage) MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_CURRENT_PLAYER_COUNT);
 			MatchmakerClient.server.setNumPlayers(players); // update the number of players
 			message.setServer(MatchmakerClient.server);
 			try {
 				mMatchmakerConnector.sendClientMessage(message);
 			} catch (IOException e) {
 				Debug.e(e);
 			} finally {
 				MatchmakerClient.mMessagePool.recycleMessage(message);
 			}
 		}
 	}
 
 	public static void closeConnection() {
 		ConnectionClosePhoneMessage message = (ConnectionClosePhoneMessage) MatchmakerClient.mMessagePool.obtainMessage(FLAG_MESSAGE_PHONE_CONNECTION_CLOSE);
 		try {
 			mMatchmakerConnector.sendClientMessage(message);
 		} catch (IOException e) {
 			Debug.e(e);
 		} finally {
 			MatchmakerClient.mMessagePool.recycleMessage(message);
 		}
 		mMatchmakerConnector.terminate(); // kill the connection
 	}
 }
