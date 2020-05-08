 package com.evervoid.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.json.Json;
 import com.evervoid.network.LoadGameRequest;
 import com.evervoid.network.PlayerDefeatedMessage;
 import com.evervoid.network.PlayerVictoryMessage;
 import com.evervoid.network.ReadyMessage;
 import com.evervoid.network.RequestGameState;
 import com.evervoid.network.SaveGameStateReply;
 import com.evervoid.network.ServerChatMessage;
 import com.evervoid.network.StartGameMessage;
 import com.evervoid.network.TurnMessage;
 import com.evervoid.network.lobby.LobbyPlayer;
 import com.evervoid.network.lobby.LobbyState;
 import com.evervoid.state.EVGameState;
 import com.evervoid.state.action.Action;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.action.Turn;
 import com.evervoid.state.action.building.IncrementBuildingConstruction;
 import com.evervoid.state.action.building.IncrementShipConstruction;
 import com.evervoid.state.action.planet.RegeneratePlanet;
 import com.evervoid.state.action.player.ReceiveIncome;
 import com.evervoid.state.action.ship.BombPlanet;
 import com.evervoid.state.action.ship.CapturePlanet;
 import com.evervoid.state.action.ship.EnterCargo;
 import com.evervoid.state.action.ship.JumpShipIntoPortal;
 import com.evervoid.state.action.ship.LeaveCargo;
 import com.evervoid.state.action.ship.MoveShip;
 import com.evervoid.state.action.ship.RegenerateShip;
 import com.evervoid.state.action.ship.ShootShip;
 import com.evervoid.state.data.BadJsonInitialization;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.player.Player;
 import com.evervoid.state.prop.Planet;
 import com.evervoid.state.prop.Ship;
 import com.jme3.network.connection.Client;
 
 public class EVGameEngine implements EVServerMessageObserver
 {
 	private static final Logger aGameEngineLog = Logger.getLogger(EVGameEngine.class.getName());
 	public static final Class<?>[] sCombatActionTypes = { ShootShip.class, BombPlanet.class };
 	public static final Class<?>[] sMoveActionTypes = { MoveShip.class, JumpShipIntoPortal.class };
 	private final Map<Client, Player> aClientMap = new HashMap<Client, Player>();
 	private final GameData aGameData;
 	private HashSet<Client> aReadyMap;
 	protected EVServerEngine aServer;
 	private EVGameState aState;
 	private final Map<Player, Turn> aTurnMap = new HashMap<Player, Turn>();
 	private int aTurnNumber = 1;
 	private Timer aTurnTimer = new Timer();
 
 	EVGameEngine(final EVServerEngine server) throws BadJsonInitialization
 	{
 		aGameEngineLog.setLevel(Level.ALL);
 		aGameEngineLog.info("Game engine starting with server " + server);
 		aServer = server;
 		server.registerListener(this);
 		aGameData = new GameData();
 	}
 
 	private boolean addTurn(final Client client, final Turn turn)
 	{
 		final Player sender = aClientMap.get(client);
 		if (sender == null) {
 			return false; // Not even a registered player
 		}
 		for (final Action act : turn) {
 			if (!act.getSender().equals(sender)) {
 				return false; // Trying to impersonate another player
 			}
 		}
 		if (aTurnMap.get(client) != null) {
 			return false;
 		}
 		aTurnMap.put(sender, turn);
 		return true;
 	}
 
 	private List<Action> calculateIncome()
 	{
 		final List<Action> incomeActions = new ArrayList<Action>();
 		for (final Player p : aState.getPlayers()) {
 			if (p.isNullPlayer()) {
 				continue; // no point in giving it income
 			}
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
 		// compress all client turns into one
 		final Turn combinedTurn = new Turn();
 		for (final Player p : aTurnMap.keySet()) {
 			combinedTurn.addTurn(aTurnMap.get(p));
 		}
 		// inform
 		aGameEngineLog.info("Game engine building turn from original:\n" + combinedTurn.toJson().toPrettyString());
 		// start calculating turn
 		// !WARNING!: If you need to modify the order of actions, do not forget to edit res/action_order.txt and
 		// TurnSynchronizer to reflect the changes.
 		// 1: Regeneration (Ships, planets)
 		combinedTurn.reEnqueueAllActions(regenShips());
 		combinedTurn.reEnqueueAllActions(regenPlanets());
 		// 2: Combat (Shooting, bombing)
 		final List<Action> combatActions = combinedTurn.getActionsOfType(sCombatActionTypes).getActions();
 		for (final Action act : combatActions) {
 			if (act instanceof ShootShip) {
 				((ShootShip) act).rollDamage();
 				combinedTurn.reEnqueueAction(act);
 			}
 			else if (act instanceof BombPlanet) {
 				((BombPlanet) act).rollDamage();
 				combinedTurn.reEnqueueAction(act);
 			}
 		}
 		// 3: Docking ships
 		shakeUpActions(combinedTurn, EnterCargo.class);
 		// 4: Unloading ships
 		shakeUpActions(combinedTurn, LeaveCargo.class);
 		// 5: Jumps
 		shakeUpActions(combinedTurn, JumpShipIntoPortal.class);
 		// 6: Capturing planets
 		shakeUpActions(combinedTurn, CapturePlanet.class);
 		// 7: Movement
 		shakeUpActions(combinedTurn, sMoveActionTypes);
 		// 8: Building construction
 		shakeUpActions(combinedTurn, IncrementBuildingConstruction.class);
 		// 9: Ship construction
 		shakeUpActions(combinedTurn, IncrementShipConstruction.class);
 		// 10: Income
 		combinedTurn.reEnqueueAllActions(calculateIncome());
 		// Finally - send out turn
 		aServer.sendAll(new TurnMessage(aState.commitTurn(combinedTurn)));
 		aTurnNumber++;
 		// Check if some other players have lost
 		for (final Player p : aState.getPlayers()) {
 			if (aState.hasLost(p)) {
 				aServer.sendAll(new PlayerDefeatedMessage(p));
 				aServer.sendAll(new ServerChatMessage("Player \"" + p.getNickname() + "\" has been defeated."));
 			}
 		}
 		// Check if game is over
 		final Player winner = aState.getWinner();
 		if (winner != null) {
 			aServer.sendAll(new PlayerVictoryMessage(winner));
 			aServer.sendAll(new ServerChatMessage("Player \"" + winner.getNickname() + "\" has won the game."));
 		}
 		// Reset stuff
 		resetTurnMap();
 		resetTimer();
 	}
 
 	@Override
 	public void clientQuit(final Client client)
 	{
 		if (!aClientMap.containsKey(client)) {
 			return; // the player doesn't exist, why is this being called?
 		}
 		final Player leaving = aClientMap.remove(client);
 		aTurnMap.remove(leaving);
 		if (aClientMap.isEmpty()) {
 			// no more players, go ahead and stop server
 			EverVoidServer.stop();
 		}
 	}
 
 	@Override
 	public void messageReceived(final String type, final LobbyState lobby, final Client client, final Json content)
 	{
 		if (type.equals(TurnMessage.class.getName())) {
 			addTurn(client, new Turn(content, aState));
 			tryCalculateTurn();
 		}
 		else if (type.equals(StartGameMessage.class.getName())) {
 			final List<Player> playerList = new ArrayList<Player>();
 			for (final LobbyPlayer player : lobby) {
 				final Player p = new Player(player.getNickname(), player.getRace(), player.getColorName(), null);
 				playerList.add(p);
 				aClientMap.put(player.getClient(), p);
 				aTurnMap.put(p, null);
 			}
 			if (playerList.size() == 1) {
 				// there is only one player, add an "AI"
 				String randomColor;
 				boolean taken;
 				do {
 					taken = false;
 					randomColor = aGameData.getRandomColor();
 					for (final LobbyPlayer p : lobby) {
 						if (p.getColorName().equals(randomColor)) {
 							taken = true;
 						}
 					}
 				}
 				while (taken);
				playerList.add(Player.newRandomPlayer(aGameData.getRandomRace(), randomColor));
 			}
 			final EVGameState tempState = new EVGameState(playerList, aGameData);
 			aReadyMap = new HashSet<Client>(aClientMap.keySet());
 			aServer.sendAllState(tempState);
 			setState(tempState);
 		}
 		else if (type.equals(ReadyMessage.class.getName())) {
 			// wait until all players are ready
 			if (aReadyMap != null) {
 				aReadyMap.remove(client);
 			}
 			if (aReadyMap.isEmpty()) {
 				// actually start game
 				resetTimer();
 				aReadyMap = null;
 			}
 		}
 		else if (type.equals(LoadGameRequest.class.getName())) {
 			try {
 				setState(new EVGameState(content));
 			}
 			catch (final BadJsonInitialization e) {
 				// This should never happen, it is never called without checking
 				e.printStackTrace();
 			}
 			for (final LobbyPlayer player : lobby) {
 				final Player p = aState.getPlayerByNickname(player.getNickname());
 				if (p == null) {
 					// We're really in trouble here
 					aGameEngineLog.severe("Cannot find player from loaded game corresponding to LobbyPlayer "
 							+ player.getNickname());
 				}
 				aClientMap.put(player.getClient(), p);
 				aTurnMap.put(p, null);
 			}
 			aReadyMap = new HashSet<Client>(aClientMap.keySet());
 			aServer.sendAllState(aState);
 			resetTimer();
 		}
 		else if (type.equals(RequestGameState.class.getName())) {
 			aGameEngineLog.info("Got game state request from client " + client);
 			final String clientHash = content.getStringAttribute("gamehash");
 			final String thisHash = aState.toJson().getHash();
 			aGameEngineLog.info("Client hash is " + clientHash + "; server hash is " + thisHash);
 			aServer.send(client, new SaveGameStateReply(clientHash.equals(thisHash) ? null : aState));
 		}
 	}
 
 	private List<Action> regenPlanets()
 	{
 		final List<Action> actions = new ArrayList<Action>();
 		for (final Planet p : aState.getAllPlanets()) {
 			if (!p.getPlayer().isNullPlayer() && (!p.isAtMaxHealth() || !p.isAtMaxShields())) {
 				// needs to regenerate
 				try {
 					actions.add(new RegeneratePlanet(p, aState));
 				}
 				catch (final IllegalEVActionException e) {
 					// should never happen
 				}
 			}
 		}
 		return actions;
 	}
 
 	private List<Action> regenShips()
 	{
 		final List<Action> actions = new ArrayList<Action>();
 		for (final Ship s : aState.getAllShips()) {
 			if (!s.isAtMaxHealth() || !s.isAtMaxRadiation() || !s.isAtMaxShields()) {
 				try {
 					actions.add(new RegenerateShip(s, aState));
 				}
 				catch (final IllegalEVActionException e) {
 					// Should never happen
 				}
 			}
 		}
 		return actions;
 	}
 
 	public void resetTimer()
 	{
 		// Cancel current timer and remove the scheduled task from the list
 		aTurnTimer.cancel();
 		aTurnTimer = new Timer();
 		aTurnTimer.schedule(new TimerTask()
 		{
 			@Override
 			public void run()
 			{
 				calculateTurn();
 			}
 		}, 1000 * aGameData.getTurnLength() + Math.max(3000, (int) (3 * aServer.maxPingTime())));
 		// give some time for late turns to come in
 	}
 
 	private void resetTurnMap()
 	{
 		for (final Player p : aTurnMap.keySet()) {
 			aTurnMap.put(p, null);
 		}
 	}
 
 	protected void setState(final EVGameState state)
 	{
 		aState = state;
 	}
 
 	/**
 	 * Extract the action of the specified types from the specified turn, and reenqueues them in random order at the end of the
 	 * turn
 	 * 
 	 * @param turn
 	 *            The turn to extract from (and to reenqueue to)
 	 * @param types
 	 *            The types of action to extract
 	 */
 	private void shakeUpActions(final Turn turn, final Class<?>... types)
 	{
 		final List<Action> actionsCopy = new ArrayList<Action>(turn.getActionsOfType(types).getActions());
 		Collections.shuffle(actionsCopy); // Shake it up
 		for (final Action act : actionsCopy) {
 			turn.reEnqueueAction(act);
 		}
 	}
 
 	@Override
 	public void stop()
 	{
 		aTurnTimer.cancel();
 		aTurnTimer.purge();
 		aClientMap.clear();
 		aTurnMap.clear();
 	}
 
 	private boolean tryCalculateTurn()
 	{
 		for (final Player p : aTurnMap.keySet()) {
 			if (aTurnMap.get(p) == null) {
 				return false;
 			}
 		}
 		calculateTurn();
 		return true;
 	}
 }
