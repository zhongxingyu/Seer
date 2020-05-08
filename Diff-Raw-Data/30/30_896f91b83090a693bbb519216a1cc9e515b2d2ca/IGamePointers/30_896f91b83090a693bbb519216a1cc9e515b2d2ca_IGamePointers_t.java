 package com.liongrid.infectomancer;
 
 import com.liongrid.infectomancer.crowd.situations.ISituationHandler;
 
 public class IGamePointers {
 	//Persistent variables:
 	public static int coins = 0; 
 	
	private static int mXP = 0;
	private static int mLevel = 0;
	
 	//Per game variables:
 	public static int NumberOfHumans = 15;
 	public static IGameObjectHandler gameObjectHandler;
 	public static int difficulty = -1;
 	public static IGameStatus gameStatus;
 	public static IGameActivity curGameActivity;
 	public static ISpawnPool spawnPool;
 	public static ISituationHandler situationHandler;
 	
 	
 	public static void resetGameVars() {
 		NumberOfHumans = -1;
 		gameObjectHandler = null;
 		difficulty = -1;
 		gameStatus = null;
 		curGameActivity = null;
 		coins = 0;
 		spawnPool = null;
 		situationHandler = null;
 		
 	}
	
	public static int getLevel(){
		return mLevel;
	}
	
	
 }
