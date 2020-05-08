 package com.secondhand.controller;
 
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.input.touch.TouchEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.GameWorld;
 import com.secondhand.scene.GamePlayScene;
 
 public final class GamePlaySceneController {
 
 	private final GameWorld gameWorld;
 	
 	public GamePlaySceneController(final GamePlayScene scene) {
 		gameWorld = scene.getGameWorld();
 		scene.setOnSceneTouchListener(new GameSceneTouchListener());
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
 }
