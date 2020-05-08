 package topplintowers.scenes;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
 
 import topplintowers.ResourceManager;
 import topplintowers.levels.Level;
 
 public class SceneManager
 {
     private BaseScene mSplashScene, mMenuScene, mGameScene, mPausedScene, mQuitPopupScene, mLevelSelectScene, mLoadingScene;
     
     private static final SceneManager INSTANCE = new SceneManager();
     private SceneType mCurrentSceneType = SceneType.SCENE_SPLASH;
     private BaseScene mCurrentScene;
     private Engine mEngine = ResourceManager.getInstance().mEngine;
     
     public enum SceneType {
     	SCENE_SPLASH,
     	SCENE_MAIN_MENU,
     	SCENE_LEVEL_SELECT,
     	SCENE_PAUSED,
     	SCENE_QUIT_POPUP,
     	SCENE_LOADING,
     	SCENE_GAME
     }
     
     public static SceneManager getInstance(){ return INSTANCE; }    
     public SceneType getCurrentSceneType()  { return mCurrentSceneType; }    
     public BaseScene getCurrentScene()		{ return mCurrentScene; }
     
     public void setScene(BaseScene scene)
     {
         mEngine.setScene(scene);
         mCurrentScene = scene;
         mCurrentSceneType = scene.getSceneType();
     }
     
     public void setScene(SceneType sceneType)
     {
         switch (sceneType)
         {
             case SCENE_MAIN_MENU:
                 setScene(mMenuScene);
                 break;
             case SCENE_GAME:
                 setScene(mGameScene);
                 break;
             case SCENE_SPLASH:
                 setScene(mSplashScene);
                 break;
             case SCENE_LEVEL_SELECT:
                 setScene(mLevelSelectScene);
                 break;
             case SCENE_PAUSED:
             	setScene(mPausedScene);
             	break;
             case SCENE_QUIT_POPUP:
             	setScene(mQuitPopupScene);
             	break;
             case SCENE_LOADING:
             	setScene(mLoadingScene);
             	break;
             default:
                 break;
         }
     }
     
     public void createSplashScene(OnCreateSceneCallback pOnCreateSceneCallback) {
     	ResourceManager.getInstance().loadSplashScreen();
     	mSplashScene = new SplashScene();
     	mCurrentScene = mSplashScene;
     	pOnCreateSceneCallback.onCreateSceneFinished(mSplashScene);
     }
     
     private void disposeSplashScene() {
     	ResourceManager.getInstance().unloadSplashScreen();
     	mSplashScene.disposeScene();
     	mSplashScene = null;
     }
     
     public void createMenuScene() {
     	ResourceManager.getInstance().loadMenuResources();
     	mMenuScene = new MainMenuScene();
     	mLoadingScene = new LoadingScene();
     	mQuitPopupScene = new QuitPopupScene();
     	setScene(mMenuScene);
     	disposeSplashScene();
     }
     
 //    public void loadGameScene(final Engine mEngine) {
 //    	setScene(mLoadingScene);    	
 //    	ResourceManager.getInstance().unloadMenuTextures();
 //    	mEngine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
 //    		public void onTimePassed(final TimerHandler pTimerHandler) {
 //    			mEngine.unregisterUpdateHandler(pTimerHandler);
 //    			ResourceManager.getInstance().loadGameResources();
 //    			mGameScene = new GameScene(new Level();
 //    			setScene(mGameScene);
 //    		}
 //    	}));
 //    }
     
     public void loadGameScene(final Engine mEngine, final Level level) {
     	((LoadingScene)mLoadingScene).setCrateTexture();
     	setScene(mLoadingScene);    	
     	ResourceManager.getInstance().unloadMenuTextures();
     	mEngine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
     		public void onTimePassed(final TimerHandler pTimerHandler) {
     			mEngine.unregisterUpdateHandler(pTimerHandler);
     			ResourceManager.getInstance().loadGameResources();
     			mGameScene = new GameScene(level);
     			mPausedScene = new PauseMenuScene();
     			
     			setScene(mGameScene);
     		}
     	}));
     }
     
     public void loadMenuScene(final Engine mEngine) {
    	((LoadingScene)mLoadingScene).setCrateTexture();
     	setScene(mLoadingScene);
     	
    	try {
     		mGameScene.disposeScene();
     		ResourceManager.getInstance().unloadGameTextures();
    	} catch (Exception e) {
     		mLevelSelectScene.disposeScene();
     		ResourceManager.getInstance().unloadLevelSelectGraphics();
    	}
    	
     	
     	mEngine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
     		public void onTimePassed(final TimerHandler pTimerHandler) {
     			mEngine.unregisterUpdateHandler(pTimerHandler);
     			ResourceManager.getInstance().loadMenuTextures();
     			
     			for (SpriteMenuItem button : ((MainMenuScene)mMenuScene).getButtons()) {
     				if (button.getX() >= mMenuScene.camera.getWidth())
     					SceneCommon.reenableButton(button);
     			}
     			
     			MenuScene childScene = ((MainMenuScene)mMenuScene).getMenuChildScene();
     			mMenuScene.setChildScene(childScene);		
     			setScene(mMenuScene);
     		}
     	}));
     }
     
 //    public void loadMenuScene(final Engine mEngine, LevelSelectScene lss) {
 //    	((LoadingScene)mLoadingScene).setCrateTexture();
 //    	setScene(mLoadingScene);
 //    	lss.disposeScene();
 //    	ResourceManager.getInstance().unloadLevelSelectGraphics();
 //    	mEngine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
 //    		public void onTimePassed(final TimerHandler pTimerHandler) {
 //    			mEngine.unregisterUpdateHandler(pTimerHandler);
 //    			ResourceManager.getInstance().loadMenuTextures();
 //    			
 //    			MenuScene childScene = ((MainMenuScene)mMenuScene).getMenuChildScene();
 //    			mMenuScene.setChildScene(childScene);		
 //    			setScene(mMenuScene);
 //    		}
 //    	}));
 //    }
     
     public void loadLevelSelect(final Engine mEngine) {
     	((LoadingScene)mLoadingScene).setCrateTexture();
     	setScene(mLoadingScene);
     	mMenuScene.disposeScene();
     	ResourceManager.getInstance().unloadMenuTextures();
     	mEngine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
     		public void onTimePassed(final TimerHandler pTimerHandler) {
     			mEngine.unregisterUpdateHandler(pTimerHandler);
     			
     			ResourceManager.getInstance().loadLevelSelectGraphics();
     			
     			mLevelSelectScene = new LevelSelectScene();
     			setScene(mLevelSelectScene);
     		}
     	}));
     }
     
     public void loadQuitPopup(final Engine mEngine) {
     	mCurrentScene.setChildScene(mQuitPopupScene);
     	QuitPopupScene qps = (QuitPopupScene) mQuitPopupScene;
     	SceneCommon.fadeIn(qps.getRectangle(), qps.getButtons(), qps.getText());
     }
     
     public void loadPauseScene(final Engine mEngine) {
     	GameScene currentScene = (GameScene)mCurrentScene;
     	currentScene.mHud.setVisible(false);
     	
     	PauseMenuScene pms = (PauseMenuScene) mPausedScene;
     	
     	pms.setBackgroundPosition(0, pms.getCamera().getCenterY() - 240);
     	pms.setTextPosition(10, pms.getCamera().getCenterY() - pms.getText().getHeight()/2);
     	
     	currentScene.setChildScene(mPausedScene, false, true, true);
     	
     	SceneCommon.fadeIn(pms.getRectangle(), pms.getButtons(), pms.getText());
     	
     }
 }
