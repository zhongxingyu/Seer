 package com.googlecode.prmf;
 
 import com.googlecode.prmf.starter.IOThread;
 import java.util.LinkedList;
 
 public class Game{
 	private Player[] players;
 	private TimerThread timerThread;
 	private String gameStarter;
 	private IOThread inputThread;
 	private MafiaGameState state;
 	
 	private Pregame pregame;
 	private Postgame postgame;
 	
 	public Game(String gameStarter, IOThread inputThread)	{
 		this.gameStarter = gameStarter;
 		this.inputThread = inputThread;
 		pregame = new Pregame(gameStarter, inputThread);
 		state = getPregame();
 		timerThread = new TimerThread(inputThread);
 	}
 
 
 	public void receiveMessage(String line)
 	{
 		state.receiveMessage(this, line, inputThread);
 	}
     //TODO: why is this receiving an IO thread? one was given in the constructor	
 	public Game(String gameStarter, IOThread inputThread, boolean dayStart, int numMafia)
 	{
 		this(gameStarter, inputThread);
 	}
  
 	public boolean isOver() {
 		boolean result = false;
 		LinkedList<String> teamsWon = new LinkedList<String>();
 		for(Player player : getPlayerList()) {
 			if(player.getRole().getTeam().hasWon(players)){
 				result = true;
 				String teamName = player.getRole().getTeam().getName();
 				if(!teamsWon.contains(teamName))
 					teamsWon.add(teamName);
 			}
 		}
 		if(result){
 			postgame = new Postgame(inputThread);
 			StringBuilder ret = new StringBuilder();
 			ret.append("Team:");
 			for(String teamName : teamsWon)
 					ret.append(" " + teamName);
 			ret.append(" has won");
 			inputThread.sendMessage(inputThread.getChannel(),ret.toString());
 		}
 		return result;
 	}
 
 	public String getGameStarter() {
 		return gameStarter;
 	}
 	
 	public TimerThread getTimerThread()
 	{
 		return timerThread;
 	}
 
 	public void stopTimer() {
 		getTimerThread().getTimer().interrupt();
 	}
 	
 	public MafiaGameState getState()
 	{
 		return state;
 	}
 	
 	public void setState(MafiaGameState state)
 	{
 		this.state = state;
		if(isOver())
			this.state = new Postgame(inputThread);
		this.state.status();	
 	}
 	
 	public Player[] getPlayerList()
 	{
 		if(players == null)
 			players = getPregame().getPlayerArray();
 		return players;
 			
 	}
 	public void startTimer()
 	{
 		timerThread = new TimerThread(inputThread);
 		getTimerThread().getTimer().start();
 	}
 	
 	public Pregame getPregame()
 	{
 		return pregame;
 	}
 	
 	public Postgame getPostgame()
 	{
 		return postgame;
 	}
 	
 }
