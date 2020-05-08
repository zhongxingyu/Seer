 package com.hungry_bubbles;
 
 import android.app.Application;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 public class AppInfo extends Application
 {
 	public static final int MAX_RADIUS = 100;
 	public static final int MIN_RADIUS = 20; 
 	public static final int PLAYER_STARTING_RADIUS = 50;
 	public static final int MAX_BUBBLES = 4;
 	private static final String GAME_DATA = "GAME_DATA";
 	private static final String[] matrixKeys = {"wins", "games"};
 	private SharedPreferences gameData;
 	int wins;
 	int games;
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		SharedPreferences settings = getSharedPreferences(GAME_DATA, 0);
 	       wins = settings.getInt("Wins", 0);
	       games = settings.getInt("Games", 0);
 		// TODO: Load past game data (if any) here 
 	}
 	
 	@Override
 	public void onTerminate()
 	{
 		super.onTerminate();
 		
 		// TODO: Make sure any persistent data gets stored
 	}
 	public void endGame(boolean win){
 	      SharedPreferences settings = getSharedPreferences(GAME_DATA, 0);
 	      SharedPreferences.Editor editor = settings.edit();
		editor.putInt("Games", settings.getInt("Games", 0) + 1 );
 		games++;
 		if(win){
			editor.putInt("Wins", settings.getInt("Wins", 0) + 1 );
 			wins++;
 		}
 		editor.commit();
 	}
 	
 	
 }
