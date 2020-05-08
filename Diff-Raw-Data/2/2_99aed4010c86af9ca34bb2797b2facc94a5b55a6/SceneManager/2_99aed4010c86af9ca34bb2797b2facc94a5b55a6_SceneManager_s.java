 package se.chalmers.segway.scenes;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
 
 import android.app.Activity;
 import android.content.Context;
 import se.chalmers.segway.game.PlayerData;
 import se.chalmers.segway.game.SaveManager;
 import se.chalmers.segway.game.Upgrades;
 import se.chalmers.segway.resources.ResourcesManager;
 
 public class SceneManager {
 	// ---------------------------------------------
 	// SCENES
 	// ---------------------------------------------
 
 	private BaseScene splashScene;
 	private BaseScene menuScene;
 	private BaseScene gameScene;
 	private BaseScene loadingScene;
 	private BaseScene selectionScene;
 	private BaseScene shopScene;
 	private static boolean isCreated = false;
 	private PlayerData playerData;
 	// ---------------------------------------------
 	// VARIABLES
 	// ---------------------------------------------
 
 	private static SceneManager INSTANCE = new SceneManager();
 
 	private SceneType currentSceneType = SceneType.SCENE_SPLASH;
 
 	private BaseScene currentScene;
 
 	private Engine engine = ResourcesManager.getInstance().engine;
 
 	public enum SceneType {
 		SCENE_SPLASH, SCENE_MENU, SCENE_GAME, SCENE_LOADING, SCENE_SELECTION, SHOP_SCENE
 	}
 
 	// ---------------------------------------------
 	// CREATION AND DISPOSAL
 	// ---------------------------------------------
 
 	public void createSplashScene(OnCreateSceneCallback pOnCreateSceneCallback) {
 		ResourcesManager.getInstance().loadSplashScreen();
 		splashScene = new SplashScene();
 		currentScene = splashScene;
 		pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
 	}
 
 	private void disposeSplashScene() {
 		ResourcesManager.getInstance().unloadSplashScreen();
 		splashScene.disposeScene();
 		splashScene = null;
 	}
 
 	public void createMenuScene() {
 		ResourcesManager.getInstance().loadMenuResources();
 		menuScene = new MainMenuScene();
 		loadingScene = new LoadingScene();
 		shopScene = new ShopScene();
 		((MainMenuScene) menuScene).setPlayerData(playerData);
 		((MainMenuScene) menuScene).updateHUD();
 		setScene(menuScene);
 		disposeSplashScene();
 	}
 
 	// ---------------------------------------------
 	// CLASS LOGIC
 	// ---------------------------------------------
 
 	public void setScene(BaseScene scene) {
 		engine.setScene(scene);
 		currentScene = scene;
 		currentSceneType = scene.getSceneType();
 	}
 
 	public void setScene(SceneType sceneType) {
 		switch (sceneType) {
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
 		case SCENE_SELECTION:
 			setScene(selectionScene);
 			break;
 		case SHOP_SCENE:
 			setScene(shopScene);
 			break;
 		default:
 			break;
 		}
 	}
 
 	// TODO: Unfinished
 	public void loadSelectionScene(final Engine mEngine) {
 		if (currentScene == gameScene) {
 			gameScene.disposeScene();
 			ResourcesManager.getInstance().unloadGameTextures();
 		} else if (currentScene == menuScene) {
 			ResourcesManager.getInstance().unloadMenuTextures();
 		}
 		setScene(loadingScene);
 		mEngine.registerUpdateHandler(new TimerHandler(0.1f,
 				new ITimerCallback() {
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						mEngine.unregisterUpdateHandler(pTimerHandler);
 						ResourcesManager.getInstance().loadSelectionResources();
 						selectionScene = new LevelSelectionScene();
 						((LevelSelectionScene) selectionScene)
 								.setUnlockedLevels(playerData
 										.getHighestLevelCleared());
 						((LevelSelectionScene) selectionScene).updateScene();
 						setScene(selectionScene);
 					}
 				}));
 	}
 
 	public void loadGameScene(final Engine mEngine, final int level) {
 		setScene(loadingScene);
 		ResourcesManager.getInstance().unloadSelectionTextures();
 		mEngine.registerUpdateHandler(new TimerHandler(0.1f,
 				new ITimerCallback() {
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						mEngine.unregisterUpdateHandler(pTimerHandler);
 						ResourcesManager.getInstance().loadGameResources();
 						gameScene = new GameScene();
 						((GameScene) gameScene).setPlayerData(playerData);
 						((GameScene) gameScene).loadLevel(level);
 						setScene(gameScene);
 
 						if (ResourcesManager.getInstance().musicManager
 								.getMasterVolume() == 1) {
 							ResourcesManager.getInstance().music.pause();
 							if (level == 4) {
 								ResourcesManager.getInstance().music2.play();
							} else if (level == 5) {
 								ResourcesManager.getInstance().music3.play();
 							} else {
 								ResourcesManager.getInstance().music.resume();
 							}
 						}
 					}
 				}));
 	}
 
 	public void loadMenuScene(final Engine mEngine) {
 		if (currentScene == gameScene) {
 			gameScene.disposeScene();
 			ResourcesManager.getInstance().unloadGameTextures();
 			if (ResourcesManager.getInstance().musicManager.getMasterVolume() == 1) {
 				ResourcesManager.getInstance().music.resume();
 				if (ResourcesManager.getInstance().music2.isPlaying()) {
 					ResourcesManager.getInstance().music2.pause();
 				} else if (ResourcesManager.getInstance().music3.isPlaying()) {
 					ResourcesManager.getInstance().music3.pause();
 				}
 			}
 		}
 		setScene(loadingScene);
 		mEngine.registerUpdateHandler(new TimerHandler(0.1f,
 				new ITimerCallback() {
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						mEngine.unregisterUpdateHandler(pTimerHandler);
 						ResourcesManager.getInstance().loadMenuTextures();
 						((MainMenuScene) menuScene).setPlayerData(playerData);
 						((MainMenuScene) menuScene).updateHUD();
 						setScene(menuScene);
 					}
 				}));
 	}
 
 	private void initPlayerData() {
 		playerData = SaveManager.loadPlayerData();
 		if (playerData == null) {
 			playerData = new PlayerData("Plebian " + Math.random() * 1000);
 		}
 	}
 
 	public void loadShopScene(final Engine mEngine) {
 		if (currentScene == gameScene) {
 			gameScene.disposeScene();
 			ResourcesManager.getInstance().unloadGameTextures();
 		}
 		setScene(shopScene);
 		mEngine.registerUpdateHandler(new TimerHandler(0.1f,
 				new ITimerCallback() {
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						mEngine.unregisterUpdateHandler(pTimerHandler);
 						ResourcesManager.getInstance().loadMenuTextures();
 						setScene(shopScene);
 					}
 				}));
 
 	}
 
 	// ---------------------------------------------
 	// GETTERS AND SETTERS
 	// ---------------------------------------------
 
 	public static synchronized SceneManager getInstance() {
 		if (!isCreated) {
 			SaveManager.loadUpgrades();
 			isCreated = true;
 			INSTANCE = new SceneManager();
 			INSTANCE.initPlayerData();
 		}
 		return INSTANCE;
 	}
 
 	public SceneType getCurrentSceneType() {
 		return currentSceneType;
 	}
 
 	public BaseScene getCurrentScene() {
 		return currentScene;
 	}
 
 }
