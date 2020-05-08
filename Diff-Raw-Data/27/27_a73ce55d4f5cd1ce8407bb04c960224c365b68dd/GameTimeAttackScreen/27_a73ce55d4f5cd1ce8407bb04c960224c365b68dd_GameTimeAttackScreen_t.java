 package com.tacoid.pweekmini.screens;
 
 import com.tacoid.pweek.ScoreManager.GameType;
 import com.tacoid.pweek.actors.TimeActor;
 import com.tacoid.pweek.logic.State;
 import com.tacoid.pweek.screens.IGameTimeAttackScreen;
 import com.tacoid.pweekmini.PweekMini;
 
 public class GameTimeAttackScreen extends GameScreenPortrait implements IGameTimeAttackScreen {
 	
 	private static GameTimeAttackScreen instance = null;
 
 	public void initGraphics() {
 		super.initGraphics();
 		TimeActor timeActor = new TimeActor(PweekMini.getInstance().manager, this, 76, 535);
 		stage.addActor(timeActor);
 	}
 
 	public static GameTimeAttackScreen getInstance() {
 		if (instance == null) {
 			instance = new GameTimeAttackScreen();
 		}
 		return instance;
 	}
 
 	protected boolean gameEnded() {
 		return gameLogic.getState() == State.LOST || elapsedTime >= 120;
 	}
 	
 	public float getTimeLeft() {
 		return 120 - elapsedTime;
 	}
 	
 	@Override
 	public GameType getGameType() {
 		return GameType.CHRONO;
 	}
 
 	@Override
 	public int getLevel() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }
