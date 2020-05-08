 package com.googlecode.prmf;
 
 import com.googlecode.prmf.starter.IOThread;
 
 public class PostGame implements MafiaGameState {
 	IOThread inputThread;
 
 	public PostGame(IOThread inputThread) {
 		this.inputThread = inputThread;
 	}
 
 	public boolean receiveMessage(Game game, String line, IOThread inputThread)
 	{
		return true;
 	}	
 	
 	public void swapState(Game game, MafiaGameState newState)
 	{
 		game.setState(newState);
 	}
 	
 	public void status()
 	{
 		inputThread.sendMessage(inputThread.getChannel(), "The game is now over");
 	}
 }
