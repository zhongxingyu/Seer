 package com.secondhand.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.secondhand.model.entity.IGameWorld;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.entity.PowerUpList;
 import com.secondhand.model.physics.Vector2;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.view.andengine.entity.RocketEmitter;
 import com.secondhand.view.resource.HighScoreList;
 import com.secondhand.view.scene.AllScenes;
 import com.secondhand.view.scene.GamePlayScene;
 
 final class GamePlaySceneController extends Entity implements PropertyChangeListener {
 
 	private IGameWorld gameWorld;
 	private final SceneController sceneController;
 	private final GamePlayScene gamePlayScene;
 
 	public GamePlaySceneController(final GamePlayScene scene,
 			final SceneController sceneController) {
 		super();
 		
 
 		this.gamePlayScene = scene;
 		this.sceneController = sceneController;
 		gamePlayScene.setOnSceneTouchListener(new GameSceneTouchListener());
 
 		registerController();
 	}
 	
 	// done every time the level changes.
 	private void registerController() {
 		gameWorld = gamePlayScene.getGameWorld();
 
 		gamePlayScene.attachChild(this);
 		
 		gameWorld.addListener(this);
 
 		//PlayerUtil adds this Controller as a listener
 		this.gameWorld.getPlayer().addListener(this);
 		
 		// TODO: potential memory leak:
 		gameWorld.getPowerUpList().addListener(this);
 		gameWorld.getPowerUpList().addListener(gamePlayScene);
 	
 		gameWorld.getPhysics().setContactListener();
 	}
 	
 	// we must unregister the listeners, elsewise the program will leak memory.
 	public void unregisterController() {
 		gamePlayScene.detachChild(this);
 		
 		this.gameWorld.removeListener(this);
 		
 		this.gameWorld.getPlayer().removeListener(this);
 		
 		this.gameWorld.getPowerUpList().removeListener(this);
 		this.gameWorld.getPowerUpList().removeListener(gamePlayScene);
 		
 		gameWorld.getPhysics().unsetContactListener();
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
 				
 				this.unregisterController();
 				this.sceneController.switchScene(AllScenes.HIGH_SCORE_SCENE);
 			}
 
 			if (InputDialogManager.input != null) {
 
 				final HighScoreList.Entry newEntry = new HighScoreList.Entry(
 						InputDialogManager.input, gameWorld.getPlayer()
 						.getScore());
 				HighScoreList.getInstance().insertInHighScoreList(newEntry);
 				InputDialogManager.showing = false;
 
 				InputDialogManager.input = null;
 				
 				this.unregisterController();
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
 	
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		final String name = event.getPropertyName();
 		
 		if (name.equals(PowerUpList.ADD_POWERUP)) {
 			this.sceneController.getSceneManager().registerUpdateHander(TimerFactory.createTimer(this.sceneController.getSceneManager(), gameWorld, (PowerUp)event.getNewValue()));
 		} else if (name.equals("NextLevel")) {
 			this.unregisterController();
			this.gamePlayScene.newLevelStarted();
 			this.registerController();
 		} else if (name.equals(Player.MOVE)) {
 			final Vector2 touchPosition = (Vector2) event.getNewValue();
 			final RocketEmitter emitter = this.gamePlayScene.attachRocketEmitter(new com.badlogic.gdx.math.Vector2(touchPosition.x,touchPosition.y));
 			this.sceneController.getSceneManager().registerUpdateHander(TimerFactory.createRocketTimer(this.sceneController.getSceneManager(), this.gamePlayScene, emitter));
 		}
 	}
 }
