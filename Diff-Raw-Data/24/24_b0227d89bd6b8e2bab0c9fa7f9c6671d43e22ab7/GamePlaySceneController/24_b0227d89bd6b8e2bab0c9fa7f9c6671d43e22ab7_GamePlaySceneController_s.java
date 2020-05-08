 package com.secondhand.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
import java.util.List;
 
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.secondhand.debug.MyDebug;
import com.secondhand.model.EntityManager;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.Vector2;
 import com.secondhand.model.resource.HighScoreList;
import com.secondhand.view.physics.MyPhysicsEntity;
 import com.secondhand.view.scene.AllScenes;
 import com.secondhand.view.scene.GamePlayScene;
 
 final class GamePlaySceneController extends Entity implements
 		PropertyChangeListener {
 
 	private final GameWorld gameWorld;
 	private final GamePlayScene gamePlayScene;
 	private final SceneController sceneController;
 
 	public GamePlaySceneController(final GamePlayScene scene,
 			final SceneController sceneController) {
 		super();
 		
 		this.gamePlayScene = scene;
 		this.sceneController = sceneController;
 
 		gameWorld = scene.getGameWorld();
 
 		this.gamePlayScene.attachChild(this);
 
 		scene.setOnSceneTouchListener(new GameSceneTouchListener());
 
 		gameWorld.getPhysics().setContactListener();
 
 		// receive gameworld property change in controller.
 		gameWorld.addListener(this);
 
 		
 		gameWorld.getPhysics().setContactListener();
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
 
 			if (!HighScoreList.getInstance().madeItToHighScoreList(
 					gameWorld.getPlayer().getScore())) {
 				// go to high score.
 				this.sceneController.switchScene(AllScenes.HIGH_SCORE_SCENE);
 			}
 
 			if (InputDialogManager.input != null) {
 
 				final HighScoreList.Entry newEntry = new HighScoreList.Entry(
 						InputDialogManager.input, gameWorld.getPlayer()
								.getScore());
 				HighScoreList.getInstance().insertInHighScoreList(newEntry);
 				InputDialogManager.showing = false;
 
 				InputDialogManager.input = null;
 
 				this.sceneController.switchScene(AllScenes.HIGH_SCORE_SCENE);
 
 			} else if (HighScoreList.getInstance().madeItToHighScoreList(
 					gameWorld.getPlayer().getScore())) {
 
 				InputDialogManager.showing = true;
 				InputDialogManager.getInstance().showDialog();
 
 			}
 
 		} else {
 			gameWorld.updateGameWorld();
 		}
 	}
 
 	// not a very good solution bellow but it can do for now
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		final String eventName = event.getPropertyName();
		if (event.getSource().getClass() == EntityManager.class) {
/*
			List <com.secondhand.model.Entity> list = (List<com.secondhand.model.Entity>) event.getNewValue();
			for (int i = 0; i<list.size() ; i++){
					list.get(i).setPhysics(new MyPhysicsEntity(this.gamePlayScene.getPhysicsWorld()));
			}*/

		} else if (event.getSource().getClass() == GameWorld.class) {
 
 			if (eventName.equals(Player.INCREASE_SCORE)) {
 				this.gamePlayScene.updateScore((Integer) event.getNewValue());
 			} else if (eventName.equals(Player.INCREASE_LIFE)) {
 				this.gamePlayScene.updateLives((Integer) event.getNewValue());
 			} else if (eventName.equals("PlayerRadius")) {
 				// TODO: is never sent from player for some reason; fix.
 				MyDebug.d("eric");
 				this.gamePlayScene.apaptCameraToGrowingPlayer(
 						(Float) event.getNewValue(),
 						(Float) event.getOldValue());
 			} else if (eventName.equals("NextLevel")) {
 				this.gamePlayScene.newLevelStarted();
 				// now add listeners n stuff
 			} else if (eventName.equals("NextLevel")) {
 				this.gamePlayScene.newLevelStarted();
 			} else if (eventName.equals("PlayerWallCollision")) {
 				this.gamePlayScene.onPlayerWallCollision();
 			}
 		}
 	}
 
 }
