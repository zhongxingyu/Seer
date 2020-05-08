 package com.evervoid.server;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.evervoid.json.Json;
 import com.evervoid.network.GameStateMessage;
 import com.evervoid.network.TurnMessage;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.Turn;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.player.Player;
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
 
 	private final GameData aGameData = new GameData();
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
 		if (type.equals("handshake")) {
 			// FIXME: Server should not handle handshake messages at all
 			aServer.send(client, new GameStateMessage(aState));
 		}
 		else if (type.equals("turn")) {
 			calculateTurn(new Turn(content, aState));
 		}
 		else if (type.equals("startgame")) {
 			final List<Player> playerList = new ArrayList<Player>();
 			for (final Json elem : content.getList()) {
 				final LobbyPlayer p = new LobbyPlayer(elem);
				// FIXME - make "p.getRace()" instead of "round"
				playerList.add(new Player(p.getNickname(), "round", aGameData));
 			}
 			setState(new EVGameState(playerList, aGameData));
 			aServer.sendAll(new GameStateMessage(aState));
 		}
 	}
 
 	protected void setState(final EVGameState state)
 	{
 		aState = state;
 	}
 }
