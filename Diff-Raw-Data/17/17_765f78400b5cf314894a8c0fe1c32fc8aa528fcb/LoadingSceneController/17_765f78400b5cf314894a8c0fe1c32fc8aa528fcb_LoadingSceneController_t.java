 package com.secondhand.controller;
 
 import org.anddev.andengine.entity.Entity;
 
 import com.secondhand.model.resource.HighScoreList;
 import com.secondhand.model.resource.LocalizationStrings;
 import com.secondhand.view.resource.Sounds;
 import com.secondhand.view.resource.TextureRegions;
 import com.secondhand.view.scene.AllScenes;
 import com.secondhand.view.scene.LoadingScene;
 import com.secondhand.view.scene.LoadingSceneCallback;
 
 class LoadingSceneController extends Entity {
 	
 	private final SceneController sceneController;
 	private final AllScenes nextScene;
 	
 	LoadingSceneController(final LoadingScene loadingScene, 
 			final SceneController sceneController,
 			final AllScenes nextScene) {
 		super();
 		
 		this.nextScene = nextScene;
 		this.sceneController = sceneController;
 		
 		// attach to scene. 
 		loadingScene.attachChild(this);
 		
 		// get view loading text view and attach it to it's controller. 
 		loadingScene.attachChild(new LoadingTextController(loadingScene.getLoadingTextView()));
 		
 		if(this.nextScene == AllScenes.MAIN_MENU_SCENE) {
 			loadingScene.getLoadingTextView().startAnimation(LocalizationStrings.getInstance().getLocalizedString("loading"));
 		} else if(this.nextScene == AllScenes.GAME_PLAY_SCENE) {
 			loadingScene.getLoadingTextView().startAnimation(LocalizationStrings.getInstance().getLocalizedString("loading_level"));
 		}
			
 		loadingScene.load(new LoadingDoneCallBack());
 	}
 	
 	private class LoadingDoneCallBack implements LoadingSceneCallback {
 
 		@Override
 		public void onLoadComplete() {
 			if(nextScene == AllScenes.MAIN_MENU_SCENE)
 				sceneController.setGameLoaded(true);
 			sceneController.switchScene(nextScene);
 		}
 
 		@Override
 		public void doWork() {
 
 			// this is the main loading scene.
 			if(nextScene == AllScenes.MAIN_MENU_SCENE) {
 				// model resources
 				HighScoreList.getInstance();
 				LocalizationStrings.getInstance();
 
 				// view resources. 
 				TextureRegions.getInstance().load();
 				Sounds.getInstance().load();
 			} else if(nextScene == AllScenes.GAME_PLAY_SCENE) {
 				
 			
 				// find some better place to register this.
 				//sceneController.getSceneManager().getGamePlayScene().setPhysics(new Physics());
 				
 				// preoload gameplay scene. 
 				sceneController.getSceneManager().getScene(AllScenes.GAME_PLAY_SCENE).loadScene();
 			}
 
 		}
 		
 	}
 }
