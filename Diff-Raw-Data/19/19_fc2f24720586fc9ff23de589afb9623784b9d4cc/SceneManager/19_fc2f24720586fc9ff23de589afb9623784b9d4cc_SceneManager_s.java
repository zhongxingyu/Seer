 package com.pigote.objectpool;
 
 import org.andengine.engine.Engine;
 import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
 
 import com.pigote.objectpool.BaseScene;
 
 public class SceneManager
 {
 	//---------------------------------------------
     // SCENES
     //---------------------------------------------
     
 	private BaseScene splashScene;
     private BaseScene menuScene;
     private BaseScene gameScene;
     private BaseScene loadingScene;
     
     //---------------------------------------------
     // VARIABLES
     //---------------------------------------------
     
     private static final SceneManager INSTANCE = new SceneManager();
     
     private SceneType currentSceneType = SceneType.SCENE_SPLASH;
     
     private BaseScene currentScene;
     
     private Engine engine = ResourcesManager.getInstance().engine;
     
     public enum SceneType
     {
         SCENE_SPLASH,
         SCENE_MENU,
         SCENE_GAME,
         SCENE_LOADING,
     }
     
     //---------------------------------------------
     // CLASS LOGIC
     //---------------------------------------------
     
     public void setScene(BaseScene scene)
     {
         engine.setScene(scene);
         currentScene = scene;
         currentSceneType = scene.getSceneType();
     }
     
     public void setScene(SceneType sceneType)
     {
         switch (sceneType)
         {
             case SCENE_MENU:
                 setScene(menuScene);
                 break;
             case SCENE_GAME:
                 setScene(gameScene);
                 break;
             case SCENE_SPLASH:
                 setScene(splashScene);
                 break;
             case SCENE_LOADING:
                 setScene(loadingScene);
                 break;
             default:
                 break;
         }
     }
     
     public void createSplashScene(OnCreateSceneCallback pOnCreateSceneCallback)
     {
         ResourcesManager.getInstance().loadSplashScreen();
         splashScene = new SplashScene();
         pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
     }
     
     private void disposeSplashScene()
     {
         ResourcesManager.getInstance().unloadSplashScreen();
         splashScene.disposeScene();
         splashScene = null;
     }
     
     public void createMenuScene()
     {
         ResourcesManager.getInstance().loadMenuResources();
         menuScene = new MainMenuScene();
        loadingScene = new LoadingScene();
         //setScene(loadingScene);
         setScene(menuScene);
         disposeSplashScene();
     }
     
     public void createGameScene()
     {
         ResourcesManager.getInstance().loadGameResources();
         gameScene = new GameScene();
         setScene(gameScene);
     }
     //---------------------------------------------
     // GETTERS AND SETTERS
     //---------------------------------------------
     
     public static SceneManager getInstance()
     {
         return INSTANCE;
     }
     
     public SceneType getCurrentSceneType()
     {
         return currentSceneType;
     }
     
     public BaseScene getCurrentScene()
     {
         return currentScene;
     }
 }
