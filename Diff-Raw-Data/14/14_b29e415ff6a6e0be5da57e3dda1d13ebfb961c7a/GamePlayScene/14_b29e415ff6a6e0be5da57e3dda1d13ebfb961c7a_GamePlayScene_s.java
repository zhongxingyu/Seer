 package com.secondhand.scene;
 
 import java.util.List;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.entity.shape.IShape;
 
 import android.content.Context;
 import android.view.KeyEvent;
 
 import com.secondhand.controller.SceneManager;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Universe;
 import com.secondhand.opengl.StarsBackground;
 
 public class GamePlayScene extends GameScene {
 	
 	
 	private List<IShape> shapeList;
 	private IShape player;
 	
	private Universe universe = Universe.getInstance();
 	
	public GamePlayScene(Engine engine, Context context) {
 		super(engine, context);
 	}
 	
	public void setShapes(List<IShape> list){
 		shapeList = list;
 	}
 	
	public void setPlayer(IShape player){
 		this.player = player;
 	}
 
 	@Override
 	public void loadResources() {
 		// load the resources of this scene
 	}
 
 	@Override
 	public void loadScene() {
 	
 		// level width. it's the camera width and height for now.
 		final float width = universe.getLevel().getLevelWidth();
 		final float height = universe.getLevel().getLevelHeight();
 		
 		this.attachChild(new StarsBackground(50, 5.0f, width, height));
 		this.attachChild(new StarsBackground(100, 3.0f, width, height));
         this.attachChild(new StarsBackground(130, 1.0f, width, height));
 		
 		// now load the scene(attach all the entities)
 		
         player.detachSelf();
 		attachChild(player);
 		for(IShape shape : shapeList){
 			shape.detachSelf();
 			attachChild(shape);
 		}
 		
 		// set the level width here.
 		this.smoothCamera.setBounds(0, width, 0, height);
 		
 		this.smoothCamera.setBoundsEnabled(true);
 		
 		engine.getCamera().setChaseEntity(player);
 		
 	}
 	
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			AllScenes parent = getParentScene();
 			if (parent != null) {
 				SceneManager.getInstance().setCurrentSceneEnum(parent);
 				// Undo camera lock on player
 				smoothCamera.setChaseEntity(null);
 				engine.getCamera().setCenter(0, 0);
 				return true;
 			}
 			else
 				return false;
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public AllScenes getParentScene() {
 		// TODO: we should probably instead override onKeyDown and use custom back button handling,
 		// since we also need to somehow save the current state.
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 	
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed){
 		super.onManagedUpdate(pSecondsElapsed);
 		if(universe.isGameOver()){
 			MyDebug.d("GameOver");
 			
 			SceneManager.getInstance().setCurrentSceneEnum(AllScenes.GAME_OVER_SCENE);
 		}
 		
 	}
 }
