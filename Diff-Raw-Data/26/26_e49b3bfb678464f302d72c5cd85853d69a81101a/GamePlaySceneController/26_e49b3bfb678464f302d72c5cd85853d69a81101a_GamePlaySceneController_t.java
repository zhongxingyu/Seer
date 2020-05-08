 package com.secondhand.controller;
 
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.resource.HighScoreList;
 import com.secondhand.view.scene.AllScenes;
 import com.secondhand.view.scene.GamePlayScene;
 
 final class GamePlaySceneController extends Entity{
 
 	private final GameWorld gameWorld;
 	private final GamePlayScene gamePlayScene;
 	private final SceneController sceneController;
 	
 	public GamePlaySceneController(final GamePlayScene scene, final SceneController sceneController) {
 		
 		this.gamePlayScene = scene;
 		this.sceneController = sceneController;
 		
 		gameWorld = scene.getGameWorld();
 		
 		this.gamePlayScene.attachChild(this);
 		
 		scene.setOnSceneTouchListener(new GameSceneTouchListener());
 	
 		gameWorld.getPhysicsWorld().setContactListener(
 				new CollisionContactListener(gameWorld));
 	}
 	
 	private class GameSceneTouchListener implements IOnSceneTouchListener {
 		@Override
 		public boolean onSceneTouchEvent(final Scene pScene,
 				final TouchEvent pSceneTouchEvent) {
 			if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
 				final float posX = pSceneTouchEvent.getX();
 				final float posY = pSceneTouchEvent.getY();
 				gameWorld.updateWithTouchInput(new Vector2(posX, posY));
 				return true;
 			}
 			return false;
 		}
 	}
 	
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
 		if (gameWorld.isGameOver()) {
 			
 			if(!HighScoreList.getInstance().madeItToHighScoreList(
 					gameWorld.getPlayer().getScore())) {
 				// go to high score. 
 				this.sceneController.switchScene(AllScenes.HIGH_SCORE_SCENE);
 			}
 			
 			if (InputDialogManager.input != null) {
 
 				final HighScoreList.Entry newEntry = new HighScoreList.Entry(InputDialogManager.input, 
 						gameWorld.getPlayer().getScore());
 				HighScoreList.getInstance().insertInHighScoreList(newEntry);
 				InputDialogManager.showing = false;
 
 				InputDialogManager.input = null;
 
				this.sceneController.switchScene(AllScenes.HIGH_SCORE_SCENE);
 
 			} else if (HighScoreList.getInstance().madeItToHighScoreList(
					gameWorld.getPlayer().getScore())) {
 
 				InputDialogManager.showing = true;
 				InputDialogManager.getInstance().showDialog();
 
 			} else {
			}
 			
 		} else {
 			gameWorld.updateGameWorld();
 		}
 	}
 
 }
