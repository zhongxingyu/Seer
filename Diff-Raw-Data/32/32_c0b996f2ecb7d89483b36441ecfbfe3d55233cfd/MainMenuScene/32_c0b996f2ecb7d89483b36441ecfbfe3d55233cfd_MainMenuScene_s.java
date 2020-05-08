 package dragonsreign.scene;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.opengl.util.GLState;
 
 import dragonsreign.scene.BaseScene;
 import dragonsreign.scene.DragonsReignActivity;
 import dragonsreign.manager.SceneManager;
 import dragonsreign.manager.SceneManager.SceneType;
 import dragonsreign.manager.SoundManager;
 
 public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener
 {
 
 	private MenuScene menuChildScene;
 	private DragonsReignActivity main;
 	private final int MENU_PLAY = 0;
 	private final int MENU_EXIT = 1;
 	
 	private Text playButtonText, exitButtonText;
 	private Sprite title;
 	
 	private void createMenuChildScene()
 	{
 	    menuChildScene = new MenuScene(camera);
 	    menuChildScene.setPosition(0, 0);
 	    
 	    final IMenuItem playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourcesManager.playButton, vbom), 1.2f, 1);
 	    final IMenuItem exitMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_EXIT, resourcesManager.optionsButton, vbom), 1.2f, 1);
 	    
 	    playButtonText = new Text(40,10, resourcesManager.font, "" ,150, new TextOptions(), vbom);
 	    playButtonText.setText("Play");
 		
 	    exitButtonText = new Text(40,10, resourcesManager.font, "" ,150, new TextOptions(), vbom);
 	    exitButtonText.setText("Exit");
 		
 	    menuChildScene.addMenuItem(playMenuItem);
 	    menuChildScene.addMenuItem(exitMenuItem);
 	    
 	    playMenuItem.attachChild(playButtonText);
 	    exitMenuItem.attachChild(exitButtonText);
 	    
 	    menuChildScene.buildAnimations();
 	    menuChildScene.setBackgroundEnabled(false);
 	    
 	    playMenuItem.setPosition(200, 225);
 	    exitMenuItem.setPosition(200, 325);
 	    
 	    menuChildScene.setOnMenuItemClickListener(this);
 	    
 	    setChildScene(menuChildScene);
 	}
	private void createBackground()
	{
		camera.setChaseEntity(null);
		title = new Sprite(0, 0, resourcesManager.menuBackgroundRegion, vbom)
		{
    	    @Override
    	    protected void preDraw(GLState pGLState, Camera pCamera) 
    	    {
    	       super.preDraw(pGLState, pCamera);
    	       pGLState.enableDither();
    	    }
    	};
    	title.setPosition(0, 0);
    	attachChild(title);
	}
 
 	@Override
 	public void createScene() 
 	{
		createBackground();
 		createMenuChildScene();
 		//SoundManager.mMenuMusic.play();
 	}
 
 	@Override
 	public void onBackKeyPressed() 
 	{
 		System.exit(0);
 	}
 
 	@Override
 	public SceneType getSceneType() 
 	{
 		return SceneType.SCENE_MENU;
 	}
 
 	@Override
 	public void disposeScene() 
 	{
 		//SoundManager.mMenuMusic.stop();
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
 	{
 		switch(pMenuItem.getID())
 		{
 	        case MENU_PLAY:
 	        	//SoundManager.mPunchSound.play();
 	        	//Load Game Scene!
 	            SceneManager.getInstance().setScene(SceneManager.SceneType.SCENE_CHARACTER);
 	            return true;
 	        case MENU_EXIT:
 	        	System.exit(0);
 	            return true;
 	        default:
 	            return false;
 	    }
 	}
 	
 }
