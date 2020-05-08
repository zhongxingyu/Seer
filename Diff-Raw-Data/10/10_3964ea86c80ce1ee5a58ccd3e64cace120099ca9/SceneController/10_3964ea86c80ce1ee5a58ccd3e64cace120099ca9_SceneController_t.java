 package com.secondhand.controller;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.entity.scene.Scene;
 
 import android.content.Context;
 import android.view.KeyEvent;
 
 import com.secondhand.view.scene.AllScenes;
 
 /**
  * The controller for all the scenes. While the SceneManager is capable of switching between scenes,
  * this is the one who does the actual switching. This class also responsible for registering the 
  * controllers of all the scenes. 
  */
 class SceneController {
 
 	private final SceneManager sceneManager;
 	
 	public SceneManager getSceneManager() {
 		return this.sceneManager;
 	}
 	
 	public SceneController(final Engine engine, final Context context) {
		this.sceneManager = new SceneManager(engine, context);		
 	}
 	
 	private boolean isGameLoaded = false;
 	
 	public void setGameLoaded(final boolean isGameLoaded) {
 		this.isGameLoaded = isGameLoaded;
 	}
 	
 	public boolean isGameLoaded() {
 		return this.isGameLoaded;
 	}
 	
 	
 	public void switchScene(final AllScenes scene) {
 		
 		this.sceneManager.switchScene(scene);
 
 		// now register the controller for the scene.
 		
 		if(scene == AllScenes.LOADING_SCENE) {
 			new LoadingSceneController(this.sceneManager.getLoadingScene(), this,
 					AllScenes.MAIN_MENU_SCENE);	
 		}else if(scene == AllScenes.LEVEL_LOADING_SCENE) {
 			new LoadingSceneController(this.sceneManager.getLoadingScene(), this,
 					AllScenes.GAME_PLAY_SCENE);			
 		} else if(scene == AllScenes.MAIN_MENU_SCENE) {
 			new MainMenuSceneController(this.sceneManager.getMainMenuScene(), this);
 		} else if(scene == AllScenes.HIGH_SCORE_SCENE) {
 			new HighScoreSceneController(this.sceneManager.getHighScoreScene(), this);
 		} else if (scene == AllScenes.GAME_PLAY_SCENE) {
 			new GamePlaySceneController(this.sceneManager.getGamePlayScene(), this);
 		} 
 		 
 				
 	}
 	
 	public Scene getCurrentScene() {
 		return this.sceneManager.getCurrentScene().getScene();
 	}
 	
 	// called from MainActivity.
 	public boolean sendOnKeyDownToCurrentScene(final int pKeyCode,
 			final KeyEvent pEvent) {
 		
 		if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			
 			if(this.sceneManager.getCurrentScene().getParentScene() != null) {
 
 				this.switchScene(this.sceneManager.getCurrentScene().getParentScene());
 				
 				return true;
 			} else {
 				return false;
 			}
 		} else
 			return false;
 	}
 }
