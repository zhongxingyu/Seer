 package com.secondhand.scene;
 
 import java.util.List;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 
 import android.content.Context;
 
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.secondhand.controller.SceneManager.AllScenes;
 import com.secondhand.opengl.StarsBackground;
 
 public class GamePlayScene extends GameScene {
 	
 	
 	private List<IShape> shapeList;
 	private IShape player;
 	
 	
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
 		// now load the scene(attach all the entities)
 		
 		attachChild(player);
 		/*for(IShape s : shapeList){
 			attachChild(s);
 		}*/
 		
 		// level width. it's the camera width and height for now.
 		final float width = this.smoothCamera.getWidth();
 		final float height = this.smoothCamera.getHeight();
 		
 		// set the level width here.
 		this.smoothCamera.setBounds(0, width, 0, height);
 		
 		this.smoothCamera.setBoundsEnabled(true);
 		
 		engine.getCamera().setChaseEntity(player);
 		
 		
 		this.attachChild(new StarsBackground(50, 5.0f, width, height));
 		this.attachChild(new StarsBackground(100, 3.0f, width, height));
         this.attachChild(new StarsBackground(130, 1.0f, width, height));
	}	
 	
 	@Override
 	public AllScenes getParentScene() {
 		// TODO: we should probably instead override onKeyDown and use custom back button handling,
 		// since we also need to somehow save the current state.
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 }
