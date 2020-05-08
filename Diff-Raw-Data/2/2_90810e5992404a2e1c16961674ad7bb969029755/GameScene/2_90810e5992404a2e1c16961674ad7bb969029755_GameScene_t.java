 package com.secondhand.scene;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.SmoothCamera;
 import org.anddev.andengine.entity.scene.Scene;
 
 import android.content.Context;
 import android.view.KeyEvent;
 
 import com.secondhand.controller.SceneManager;
 
 /**
  * Credit for the main idea behind this class goes to:
  * http://andengine.wikidot.com
  * /loading-resources-in-the-background-with-a-loading-screen
  */
 public abstract class GameScene extends Scene implements IGameScene {
 
 	protected final SmoothCamera smoothCamera;
 	protected final Engine engine;
 	protected final Context context;
 
 	public GameScene(final Engine engine, final Context context) {
 		super();
 		this.smoothCamera = (SmoothCamera)engine.getCamera();
 		this.engine = engine;
 		this.context = context;
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			final AllScenes parent = getParentScene();
 			if (parent != null) {
 				// TODO needs to save game and have a continue option on the
 				// menuScene if the current scene is gamePlayScene
 				// but HighScoreScene also extends from GameScene, so we will probably want
 				// override this method in GamePlayScene and use custom handling for 
 				// that specific scene.
 				setScene(parent);
 				return true;
 			}
 			else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public Scene getScene() {
 		return this;
 	}
 	
 	public void setScene(AllScenes sceneEnum){
		SceneManager.getInstance().setCurrentSceneEnum(sceneEnum);
 	}
 }
