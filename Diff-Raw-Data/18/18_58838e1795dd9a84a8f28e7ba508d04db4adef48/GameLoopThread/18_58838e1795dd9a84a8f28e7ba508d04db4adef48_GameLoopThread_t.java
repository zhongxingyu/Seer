 package com.detour.raw;
 
 import android.content.Context;
 
 public class GameLoopThread extends Thread{
 	
 	public static final int TICKS_PER_SECOND = 25; //UPS
 	public static final long TICK_DURARTION = 1000000000 / TICKS_PER_SECOND;
 	public static final int MAX_FRAMESKIP = 5;
 	
 	long next_game_tick;
     int loops;
     float interpolation;
     boolean game_is_running;
     
     //Context mContext;
 
     private GameView gameView;
     private GameManager gameManager;
     private FPSCounter fps;
 	
 	public GameLoopThread(GameView gv){
 		super();
		//next_game_tick = System.nanoTime();
 		gameView = gv;
 		//mContext = context;
 		gameManager = GameManager.getGameManager();
 		
 		game_is_running = false;
 		
 		fps = new FPSCounter();
 	}
 	
 	@Override
 	public void run(){
 		
		next_game_tick = System.nanoTime();
		
 	    while(game_is_running){
 	        loops = 0;
 	        while(System.nanoTime() > next_game_tick && loops < MAX_FRAMESKIP) {
 	            updateGame();
 	            next_game_tick += TICK_DURARTION;
 	            loops++;
 	        }
 	        interpolation = ((float)(System.nanoTime() + TICK_DURARTION - next_game_tick))/((float)TICK_DURARTION); //TODO change floats to longs
 	        displayGame(interpolation);
 	    }
 	    System.gc();
 	}
 	
 	private void updateGame(){
 		gameManager.update();
 	}
 	
 	private void displayGame(float interpol){
 		gameView.requestRender();
 		//when running at FPS faster than UPS, 
 		//use the interpolation value to update the view
 		//without updating the game logic between scheduled ticks.
 		
 		//view_position = position + (speed * interpol);
 		
 		//fps.calculate();
 	}
 	
 	public void setGameRunning(boolean b){
 		game_is_running = b;
 	}
 
 }
