 package com.petrovdevelopment.killthemall;
 
 import java.util.List;

import com.petrovdevelopment.killthemall.GameLoopThread.Difficulty;

 import android.graphics.Canvas;
 
 /**
  * Game World, holding all other elements. Singleton.
  * 
  * @author andrey
  * 
  */
 public class World implements GameElement {
 	public static World mInstance;
 	
 	private GameState mGameState;
 	private Difficulty mDifficulty;
 	
 	public enum GameState {
 		RUNNING, PAUSE, READY, WIN, LOSS
 	}
 	public enum Difficulty {
 		EASY, MEDIUM, HARD
 	}
 	
 	private Background mBackground;
 	private List<Npc> mNpcs;
 
 	private World() {
 	}
 		
 	public static World getInstance() {
 		if (mInstance == null) {
 			mInstance = new World();
 		}
 		return mInstance;
 	}
 	
 	public void initialize(GameLoader gameLoader) {
 		mBackground = gameLoader.loadBackground();
 		mNpcs = gameLoader.loadNpcs();
 		gameLoader.loadDeathEffect();
 	}
 
 	
 	/**
 	 * Update all game elements. Consider update dependencies. Currently there are none.
 	 */
 	@Override
 	public void update() {
 		mBackground.update();
 		DeathEffect.updateAll();
 		for (Npc npc : mNpcs) {
 			npc.update();
 		}
 	}
 	
 	/**
 	 * Render all game elements. 
 	 * Order of rendering affects the order on the Z axis.
 	 * First things rendered are on the back. 
 	 * Last things rendered are on the front.
 	 */
 	@Override
 	public void render(Canvas canvas) {
 		mBackground.render(canvas);
 		DeathEffect.renderAll(canvas);
 
 		for (Npc npc : mNpcs) {
 			npc.render(canvas);
 		}
 	}
 
 	
 	
 	
	
	
	
	
	
	
	
	
	
	
 	//SETTERS AND GETTERS
 	
 	
 	private GameState getGameState() {
 		return mGameState;
 	}
 
 	private void setGameState(GameState gameState) {
 		this.mGameState = gameState;
 	}
 
 }
