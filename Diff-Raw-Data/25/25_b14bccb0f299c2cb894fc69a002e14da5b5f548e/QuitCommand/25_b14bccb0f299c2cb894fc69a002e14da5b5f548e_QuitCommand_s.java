 
 package com.lofibucket.yotris.util.command;
 
 import com.lofibucket.yotris.logic.GameLogic;
 
 /**
  * Command that quits the game.
  */
 public class QuitCommand extends Command {
 
 	public QuitCommand() {
 	}
 
 	@Override
 	public void apply(GameLogic logic) {
 		logic.quitGame();
 	}
 
 	@Override
 	public boolean overrideGameOver() {
 		return true;
 	}
 
 }
