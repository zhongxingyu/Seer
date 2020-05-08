 package com.secondhand.controller;
 
 import org.anddev.andengine.engine.Engine;
 
 import android.content.Context;
 
 import com.secondhand.debug.MyDebug;
 import com.secondhand.view.scene.AllScenes;
 import com.secondhand.view.scene.GamePlayScene;
 import com.secondhand.view.scene.HighScoreScene;
 import com.secondhand.view.scene.IGameScene;
 import com.secondhand.view.scene.LoadingScene;
 import com.secondhand.view.scene.MainMenuScene;
 import com.secondhand.view.scene.SettingsMenuScene;
 
 /**
  * Is used to switch between different scenes. 
  */
 final class SceneManager {
 	private AllScenes currentSceneEnum;
 
 	private final Engine engine;
 	
	private final IGameScene settingsMenuScene;
 	
 	private final HighScoreScene highScoreScene;
 	private final MainMenuScene mainMenuScene;
 	private final GamePlayScene gamePlayScene;
 	private final LoadingScene loadingScene;
 
 	public SceneManager(final Engine engine, final Context context) {
 		this.engine = engine;
 		
 		// create all the scenes. 
 		this.loadingScene = new LoadingScene(this.engine, context);
 		this.mainMenuScene = new MainMenuScene(this.engine, context);
 		this.settingsMenuScene = new SettingsMenuScene(this.engine, context);
 		this.gamePlayScene = new GamePlayScene(this.engine, context);
 		this.highScoreScene = new HighScoreScene(this.engine, context);
 	}
 
 	public IGameScene getCurrentScene() {
 		return getScene(currentSceneEnum);
 	}
 	
 	public LoadingScene getLoadingScene() {
 		return this.loadingScene;
 	}
 	
 	public MainMenuScene getMainMenuScene() {
 		return this.mainMenuScene;
 	}
 
 	public HighScoreScene getHighScoreScene() {
 		return this.highScoreScene;
 	}
 
 	public GamePlayScene getGamePlayScene() {
 		return this.gamePlayScene;
 	}
 	
 	public IGameScene getScene(final AllScenes sceneEnum) {
 		IGameScene scene = null;
 
 		if (sceneEnum == AllScenes.LOADING_SCENE ||
 				sceneEnum == AllScenes.LEVEL_LOADING_SCENE) {
 			scene = this.loadingScene;
 		} else if (sceneEnum == AllScenes.MAIN_MENU_SCENE) {
 			scene = this.mainMenuScene;
 		} else if (sceneEnum == AllScenes.SETTINGS_MENU_SCENE) {
 			scene = this.settingsMenuScene;
 		} else if (sceneEnum == AllScenes.GAME_PLAY_SCENE) {
 			scene = this.gamePlayScene;
 		} else if (sceneEnum == AllScenes.HIGH_SCORE_SCENE) {
 			scene = this.highScoreScene;
 		}
 
 		return scene;
 	}
 
 	public IGameScene setCurrentSceneEnum(final AllScenes currentSceneEnum) {
 		this.currentSceneEnum = currentSceneEnum;
 		final IGameScene currentScene = getCurrentScene();	
 		
 		if(!currentScene.isLoaded()) {
 			MyDebug.d("scene not preloaded, loading!");
 			MyDebug.d("Scene: " + currentScene);
 			currentScene.loadScene();
 			MyDebug.d("Scene: " + currentScene);
 		}
 
 		this.engine.setScene(currentScene.getScene());
 
 		return currentScene;
 	}
 		
 	public void switchScene(final AllScenes scene) {
 		
 		if(this.getCurrentScene() != null) {
 			this.getCurrentScene().onSwitchScene();
 		}
 		
 		this.setCurrentSceneEnum(scene );
 	}
 	
 }
