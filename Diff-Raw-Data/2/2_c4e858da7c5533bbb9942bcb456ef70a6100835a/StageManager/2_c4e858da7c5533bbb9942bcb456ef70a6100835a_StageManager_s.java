 package com.jpii.navalbattle.game;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.WorldSize;
 
 public class StageManager {
 	
 	private GameComponent game;
 	int stageNumber;
 	
 	public StageManager(){
		stageNumber = 0;
 	}
 	/**
 	 * @return the GameComponent, can be null
 	 */
 	public GameComponent getGameComponent(){
 		stageNumber++;
 		return newGameComponent(stageNumber);
 	}
 	
 	private GameComponent newGameComponent(int num){
 		if(game!=null)
 			game.dispose();
 		switch(num){
 			case 1: Game.Settings.resetSeed(0); game=new GameComponent(new NavalGame(WorldSize.WORLD_TINY)); break;
 			case 2: Game.Settings.resetSeed(10); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL));  break;
 			case 3: Game.Settings.resetSeed(15); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL));  break;
 			case 4: Game.Settings.resetSeed(20); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL));  break;
 			case 5: Game.Settings.resetSeed(25); game=new GameComponent(new NavalGame(WorldSize.WORLD_SMALL));  break;
 			default: Game.Settings.resetSeed(100); game=new GameComponent(new NavalGame(WorldSize.WORLD_MEDIUM));  break;
 		}
 		return game;
 	}
 	
 }
