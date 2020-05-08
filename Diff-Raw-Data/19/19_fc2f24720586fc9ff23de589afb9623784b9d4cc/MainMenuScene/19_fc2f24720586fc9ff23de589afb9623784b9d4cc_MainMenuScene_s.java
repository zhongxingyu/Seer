 package com.pigote.objectpool;
 
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.util.GLState;
 import org.andengine.engine.camera.Camera;
 import com.pigote.objectpool.SceneManager.SceneType;
 
 public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener{
 	
 	private MenuScene menuChildScene;
 	private final int MENU_PLAY = 0;
 	private final int MENU_OPTIONS = 1;
 	
 	@Override
 	public void createScene() {
 		createBackground();
 		createMenuChildScene();
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		System.exit(0);		
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_MENU;
 	}
 
 	@Override
 	public void disposeScene() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void createBackground()
 	{
 	    attachChild(new Sprite(0, 0, resourcesManager.menu_background_region, vbom)
 	    {
 	        @Override
 	        protected void preDraw(GLState pGLState, Camera pCamera) 
 	        {
 	            super.preDraw(pGLState, pCamera);
 	            pGLState.enableDither();
 	        }
 	    });
 	} 
 	
 	private void createMenuChildScene()
 	{
 	    menuChildScene = new MenuScene(camera);
 	    menuChildScene.setPosition(0, 0);
 	    
 	    final IMenuItem playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourcesManager.play_region, vbom), 0.8f, 0.7f);
 	    final IMenuItem optionsMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_OPTIONS, resourcesManager.options_region, vbom), 0.8f, 0.7f);
 	      
 	    menuChildScene.addMenuItem(playMenuItem);
 	    menuChildScene.addMenuItem(optionsMenuItem);
 	    
 	    menuChildScene.buildAnimations();
 	    menuChildScene.setBackgroundEnabled(false);
 	    
 	    playMenuItem.setPosition(60, CAMERA_HEIGHT/1.8f);
 	    optionsMenuItem.setPosition(playMenuItem.getX()+playMenuItem.getWidth()/1.3f,playMenuItem.getY());
 	    
 	    menuChildScene.setOnMenuItemClickListener(this);
 	    
 	    setChildScene(menuChildScene);
 	}
  
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
 			float pMenuItemLocalX, float pMenuItemLocalY) {
 			switch(pMenuItem.getID())
 	        {
 	        case MENU_PLAY:
 	        	SceneManager.getInstance().setScene(SceneType.SCENE_GAME);
 	            return true;
 	        case MENU_OPTIONS:
 	            return true;
 	        default:
 	            return false;
         }
 	}
 
 }
