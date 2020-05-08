 package dk.stacktrace.risk.game_logic;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import dk.stacktrace.risk.game_logic.battle.Battle;
 import dk.stacktrace.risk.game_logic.enumerate.GamePhase;
 
 import android.util.Log;
 
 public class RiskGame {
 	private Board board;
 	private ArrayList<Player> players;
 	private Player activePlayer;
 	private GamePhase gamePhase;
 	private Territory sourceTerritory, targetTerritory;
 	private Battle battle;
 	
 	
 	
 	public RiskGame(ArrayList<Player> players) {
 		this.players = players;
 		board = new Board();
 		activePlayer = this.players.get(0);
 		gamePhase = null;
 		sourceTerritory = null;
 		targetTerritory = null;
 		battle = null;
 	}
 	
 	public void setInitialReinforcementForAllPlayers() {
 		int initialReinforcement = 0;
 		
 		switch (players.size()) {
 		case 2:
 			initialReinforcement = 2;
 			//initialReinforcement = 40;
 			break;
 		case 3:
 			initialReinforcement = 2;
 			//initialReinforcement = 35;
 			break;
 		case 4:
 			initialReinforcement = 30;
 			break;
 		case 5:
 			initialReinforcement = 25;
 			break;
 		case 6:
 			initialReinforcement = 20;
 			break;
 		}
 		
 		for (Player player : players) 
 		{
 			player.reinforcement(initialReinforcement);
 		}
 	}
 
 	public void dealTerritories()
 	{
 		ArrayList<Territory> allTerritories = board.getAllTerritories();
 		Collections.shuffle(allTerritories);
 		for (Territory territory : allTerritories) {
 			territory.setOwner(activePlayer);
 			activePlayer = nextPlayer();
 		}
 	}
 	
 	private boolean addPlayer(Player player)
 	{
 		// A maximum of 6 players is allowed
 		if (players.size() < 6)
 		{ 
 			players.add(player);
 			return true;
 		}
 		else 
 		{
 			return false;
 		}
 	}
 	
 	private Player nextPlayer()
 	{
 		for (int i = 0; i < players.size();i++)
 		{
 			if (players.get(i).equals(activePlayer))
 			{   
 				// the active player is not the last in the array
 				if (i != players.size() -1)
 				{
 					return players.get(i+1);
 				}
 				// the active player is the last in the array.
 				else
 				{
 					return players.get(0);
 				}
 			}
 		}
 		// Error
 		return null;
 	}
 
 	public Player getNextAlivePlayer()
 	{
 		do
 		{
			activePlayer = nextPlayer();
		}while(isDead(activePlayer));
		return activePlayer;
 	}
 	
 	public void endTurn()
 	{
 		//activePlayer = nextPlayer();
 		activePlayer = getNextAlivePlayer();
 		if(gamePhase != GamePhase.INITIAL_REINFORCEMENT)
 		{
 			activePlayer.reinforcement(board.calcReinforcementBonus(activePlayer));
 			gamePhase = GamePhase.REINFORCEMENT;
 			
 		}
 	}
 	
 	public Board getBoard() {
 		return board;
 	}
 	
 	public Player getActivePlayer() {
 		return activePlayer;
 	}
 	
 	public GamePhase getGamePhase() {
 		return gamePhase;
 	}
 	
 	public void setGamePhase(GamePhase gamePhase) {
 		this.gamePhase = gamePhase;
 	}
 	
 	public boolean initialReinforcementPhaseIsDone()
 	{
 		for (Player player : players) 
 		{
 			if (player.hasTroopsToDeploy())
 			{
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean reinforcementPhaseIsDone()
 	{
 		return !activePlayer.hasTroopsToDeploy();
 	}
 	
 	public void setSourceTerritory(Territory sourceTerritory) {
 		this.sourceTerritory = sourceTerritory;
 	}
 	
 	public Territory getSourceTerritory() {
 		return sourceTerritory;
 	}
 	
 	public void setTargetTerritory(Territory targetTerritory) {
 		this.targetTerritory = targetTerritory;
 	}
 	
 	public Territory getTargetTerritory() {
 		return targetTerritory;
 	}
 	
 	public Battle createBattle() {
 		if (sourceTerritory != null && targetTerritory != null)
 		{
 			battle = new Battle(sourceTerritory, targetTerritory);
 			return battle;
 		}
 		return null;
 	}
 	
 	public void endBattle()
 	{
 		battle = null;
 	}
 	
 	public Battle getBattle() {
 		return battle;
 	}
 	
 	public boolean isAlive(Player player)
 	{
 		return board.getNumberOfTerritoriesOwnedBy(player) > 0;
 	}
 	
 	public boolean isDead(Player player)
 	{
 		return board.getNumberOfTerritoriesOwnedBy(player) <= 0;
 	}
 	
 }
