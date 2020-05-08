 package com.googlecode.prmf;
 
 import com.googlecode.prmf.starter.IOThread;
 
 public class Game{
 	private Player[] players;
 	private TimerThread timerThread;
 	private String gameStarter;
 	private IOThread inputThread;
 	private MafiaGameState state;
 	
 	Pregame pregame;
 	Day day;
 	Night night;
 	public PostGame postgame;
 	
 	public Game(String gameStarter, IOThread inputThread)	{
 		this.gameStarter = gameStarter;
 		this.inputThread = inputThread;
 		pregame = new Pregame(gameStarter, inputThread);
 		state = pregame;
 		timerThread = new TimerThread(inputThread);
 	}
 
 
 	public void receiveMessage(String line)
 	{
 		state.receiveMessage(this, line, inputThread);
 	}
 	
 	public Game(String gameStarter, IOThread inputThread, boolean dayStart, int numMafia)
 	{
 		this(gameStarter, inputThread);
 	}
 
 	public void swapState(MafiaGameState newState)
 	{
 		state.swapState(this, newState);
 	}
  
 	public boolean isOver() {
 		boolean result = false;
 		for(Player player: players) {
 			if(player.getRole().getTeam().hasWon(players))
 				result = true;
 		}
 		if(result){
 			postgame = new PostGame(inputThread);
			swapState(postgame);
			state.status();
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
 		getTimerThread().timer.interrupt();
 	}
 	
 	public MafiaGameState getState()
 	{
 		return state;
 	}
 	
 	public void setState(MafiaGameState state)
 	{
 		this.state = state;
 	}
 	
 	public MafiaGameState getDay()
 	{
 		if (day == null)
 			day = new Day(getPlayerList(), inputThread);
 		return day;
 	}
 	
 	public MafiaGameState getNight()
 	{
 		if (night == null)
 			night = new Night(getPlayerList(), inputThread);
 		return night;
 	}
 	
 	public Player[] getPlayerList()
 	{
 		if(players == null)
 			players = pregame.getPlayerArray();
 		return players;
 			
 	}
 	public void startTimer()
 	{
 		timerThread = new TimerThread(inputThread);
 		getTimerThread().timer.start();
 	}
 	
 }
