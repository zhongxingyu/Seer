 package com.evervoid.server;
 
 import com.evervoid.json.Json;
 import com.evervoid.network.GameStateMessage;
 import com.evervoid.network.JoinAcknowledge;
 import com.evervoid.network.TurnMessage;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.Turn;
 import com.jme3.network.connection.Client;
 
 public class EVGameEngine implements EVServerMessageObserver
 {
 	private static EVGameEngine sInstance;
 
 	protected static EVGameEngine getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVGameEngine();
 		}
 		return sInstance;
 	}
 
 	protected EVServerEngine aServer;
 	private EVGameState aState;
 
 	private EVGameEngine()
 	{
 		sInstance = this;
 		aServer = EVServerEngine.getInstance();
 		EVServerEngine.registerListener(this);
 	}
 
 	private void calculateTurn(final Turn turn)
 	{
 		// TODO - magic
 		aServer.sendAll(new TurnMessage(turn));
 	}
 
 	@Override
 	public void messageReceived(final String type, final Client client, final Json content)
 	{
 		// FIXME: Server should not send game state directly after client connection. This boolean makes it do it anyway.
		final boolean haaaax = true;
 		if (type.equals("handshake")) {
 			if (haaaax) {
 				aServer.send(client, new GameStateMessage(aState));
 			}
 			else {
 				aServer.send(client, new JoinAcknowledge());
 			}
 		}
 		else if (type.equals("turn")) {
 			calculateTurn(new Turn(content));
 		}
 	}
 
 	protected void setState(final EVGameState state)
 	{
 		aState = state;
 	}
 }
