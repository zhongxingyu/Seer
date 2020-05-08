 package com.evervoid.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.json.Json;
 import com.evervoid.network.GameStateMessage;
 import com.evervoid.network.TurnMessage;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.network.lobby.LobbyState;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.Action;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.action.Turn;
 import com.evervoid.state.action.player.ReceiveIncome;
 import com.evervoid.state.action.ship.BombPlanet;
 import com.evervoid.state.action.ship.JumpShipIntoPortal;
 import com.evervoid.state.action.ship.MoveShip;
 import com.evervoid.state.action.ship.ShootShip;
 import com.evervoid.state.data.BadJsonInitialization;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.player.Player;
 import com.jme3.network.connection.Client;
 
 public class EVGameEngine implements EVServerMessageObserver
 {
 	private static final Logger aGameEngineLog = Logger.getLogger(EVGameEngine.class.getName());
 	private static final String[] sCombatActionTypes = { ShootShip.class.getName(), BombPlanet.class.getName() };
 	private static final String[] sMoveActionTypes = { MoveShip.class.getName(), JumpShipIntoPortal.class.getName() };
 	private final GameData aGameData;
 	protected EVServerEngine aServer;
 	private EVGameState aState;
 	private final Map<Client, Turn> aTurnMap;
 	private Timer aTurnTimer;
 
 	EVGameEngine(final EVServerEngine server) throws BadJsonInitialization
 	{
 		aGameEngineLog.setLevel(Level.ALL);
 		aGameEngineLog.info("Game engine starting with server " + server);
 		aServer = server;
 		server.registerListener(this);
 		aGameData = new GameData();
 		aTurnMap = new HashMap<Client, Turn>();
 		aTurnTimer = new Timer();
 	}
 
 	private boolean addTurn(final Client client, final Turn turn)
 	{
 		if (aTurnMap.get(client) != null) {
 			return false;
 		}
 		aTurnMap.put(client, turn);
 		return true;
 	}
 
 	private List<Action> calculateIncome()
 	{
 		final List<Action> incomeActions = new ArrayList<Action>();
 		for (final Player p : aState.getPlayers()) {
 			try {
 				incomeActions.add(new ReceiveIncome(p, aState, p.getCurrentIncome()));
 			}
 			catch (final IllegalEVActionException e) {
 				// hopefully this doesn't happen, we're trying to give players resources
 			}
 		}
 		return incomeActions;
 	}
 
 	private void calculateTurn()
 	{
 		// inform
 		aGameEngineLog.info("Game engine building turn");
 		// compress all client turns into on
 		final Turn turn = new Turn();
 		for (final Client c : aTurnMap.keySet()) {
 			turn.addTurn(aTurnMap.get(c));
 		}
 		// start calculating turn
 		// First: Combat actions
 		final List<Action> combatActions = turn.getActionsOfType(sCombatActionTypes);
 		for (final Action act : combatActions) {
 			if (act instanceof ShootShip) {
 				((ShootShip) act).rollDamage();
 			}
 		}
 		// Second: Movement actions
 		final List<Action> moveActions = turn.getActionsOfType(sMoveActionTypes);
 		Collections.shuffle(moveActions); // Shake it up
 		for (final Action act : moveActions) {
 			turn.reEnqueueAction(act);
 		}
 		// Third: Calculate income (Should be last)
 		turn.addAllActions(calculateIncome());
 		// Finally - send out turn
 		aServer.sendAll(new TurnMessage(aState.commitTurn(turn)));
 		resetTurnMap();
 		resetTimer();
 	}
 
 	@Override
 	public void messageReceived(final String type, final Client client, final Json content)
 	{
 		if (!aTurnMap.containsKey(client)) {
 			aTurnMap.put(client, null);
 		}
 		if (type.equals("handshake")) {
 			// FIXME: Server should not handle handshake messages at all
 			// aServer.send(client, new GameStateMessage(aState));
 		}
 		else if (type.equals("turn")) {
 			addTurn(client, new Turn(content, aState));
 			tryCalculateTurn();
 		}
 		else if (type.equals("startgame")) {
 			LobbyState lobby = null;
 			try {
 				lobby = new LobbyState(content);
 			}
 			catch (final BadJsonInitialization e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			final List<Player> playerList = new ArrayList<Player>();
 			for (final LobbyPlayer player : lobby) {
 				playerList.add(new Player(player.getNickname(), player.getRace(), player.getColorName(), null));
 			}
 			setState(new EVGameState(playerList, aGameData));
 			aServer.sendAll(new GameStateMessage(aState));
 			resetTimer();
 		}
 	}
 
 	public void resetTimer()
 	{
 		// cancel current timer and remove the scheduled task from the list
 		aTurnTimer.cancel();
 		aTurnTimer = new Timer();
 		aTurnTimer.schedule(new TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				calculateTurn();
 			}
		}, 60000 * aGameData.getTurnLength());
 	}
 
 	private void resetTurnMap()
 	{
 		for (final Client c : aTurnMap.keySet()) {
 			aTurnMap.put(c, null);
 		}
 	}
 
 	protected void setState(final EVGameState state)
 	{
 		aState = state;
 	}
 
 	@Override
 	public void stop()
 	{
 		System.out.println("stopped");
 		aTurnTimer.cancel();
 		aTurnTimer.purge();
 	}
 
 	private boolean tryCalculateTurn()
 	{
 		for (final Client c : aTurnMap.keySet()) {
 			if (aTurnMap.get(c) == null) {
 				return false;
 			}
 		}
 		calculateTurn();
 		return true;
 	}
 }
