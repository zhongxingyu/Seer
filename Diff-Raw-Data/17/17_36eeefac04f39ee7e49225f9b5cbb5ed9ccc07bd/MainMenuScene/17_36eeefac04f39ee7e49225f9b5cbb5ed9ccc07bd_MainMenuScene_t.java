 package se.chalmers.segway.scenes;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.options.MusicOptions;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.scene.menu.item.decorator.BaseMenuItemDecorator;
 import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.util.GLState;
 
 import android.R.integer;
 import se.chalmers.segway.game.PlayerData;
 import se.chalmers.segway.game.SaveManager;
 import se.chalmers.segway.game.Settings;
 import se.chalmers.segway.scenes.SceneManager.SceneType;
 
 public class MainMenuScene extends BaseScene implements
 		IOnMenuItemClickListener {
 	// If music is on default
 	private boolean sound = false;
 	IMenuItem shopMenuItem;
 	IMenuItem soundonMenuItem;
 	IMenuItem soundoffMenuItem;
 	IMenuItem playMenuItem;
 	private PlayerData playerData;
 	private Settings settings;
 
 	@Override
 	public void createScene() {
 		createBackground();
 		createMenuChildScene();
 		initMusic();
 		createHUD();
 	}
 
 	public void setPlayerData(PlayerData data) {
 		this.playerData = data;
 	}
 
 	/**
 	 * Starts music if none is running and makes it loop forever.
 	 */
 	private void initMusic() {
 		this.resourcesManager.musicManager.setMasterVolume(0);
 		this.resourcesManager.music.setLooping(true);
 		this.resourcesManager.music3.setLooping(true);
 		this.resourcesManager.music2.setLooping(true);
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		System.out.println("CLEAN EXIT BITCH");
 		System.out.println("BITCH CASH WRITTEN IS: " + playerData.getMoney());
 		if(playerData == null){
 			System.out.println("PLAYERDATA IS NULL BITCH");
 		}
 		SaveManager.saveSettings(settings);
 		SaveManager.savePlayerData(playerData);
 		SaveManager.saveUpgrades();
 		System.exit(0);
 
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_MENU;
 	}
 
 	@Override
 	public void disposeScene() {
		cookieCounter.setVisible(false);
		cookieAmount.setVisible(false);
 	}
 
 	/**
 	 * Adds a background to the scene.
 	 */
 	private void createBackground() {
 		attachChild(new Sprite(400, 240,
 				resourcesManager.menu_background_region, vbom) {
 			@Override
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			}
 		});
 	}
 
 	private MenuScene menuChildScene;
 	private HUD hud;
 	private final int MENU_PLAY = 0;
 	private final int MENU_OPTIONS = 1;
 	private final int MENU_SHOP = 2;
	private Sprite cookieCounter;
	private Text cookieAmount;
 
 	private void createHUD() {
 		hud = new HUD();
 		camera.setHUD(hud);
 	}
 
 	public void updateHUD() {
 		camera.setHUD(hud);
 		hud.detachChildren();
		cookieCounter = new Sprite(580, 453,
 				resourcesManager.cookieCounter_region, vbom);
		cookieAmount = new Text(620, 450,
 				resourcesManager.loadingFont, ":" + playerData.getMoney(), vbom);
 		cookieAmount.setPosition(
 				620 + (14 * Integer.toString(playerData.getMoney()).length()),
 				450);
 		hud.attachChild(cookieAmount);
 		hud.attachChild(cookieCounter);
 	}
 
 	private void createMenuChildScene() {
 		menuChildScene = new MenuScene(camera);
 		menuChildScene.setPosition(0, 0);
 
 		playMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY,
 				resourcesManager.play_region, vbom), 1.2f, 1);
 		soundonMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(
 				MENU_OPTIONS, resourcesManager.soundon_region, vbom), 1.2f, 1);
 		soundoffMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(
 				MENU_OPTIONS, resourcesManager.soundoff_region, vbom), 1.2f, 1);
 		shopMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_SHOP,
 				resourcesManager.shop_region, vbom), 1.2f, 1);
 
 		menuChildScene.addMenuItem(playMenuItem);
 		menuChildScene.addMenuItem(shopMenuItem);
 		menuChildScene.addMenuItem(soundoffMenuItem);
 		menuChildScene.addMenuItem(soundonMenuItem);
 
 		menuChildScene.buildAnimations();
 		menuChildScene.setBackgroundEnabled(false);
 
 		playMenuItem.setPosition(playMenuItem.getX(), playMenuItem.getY() - 70);
 		shopMenuItem.setPosition(shopMenuItem.getX(), shopMenuItem.getY() - 70);
 		soundoffMenuItem.setPosition(soundoffMenuItem.getX(), soundoffMenuItem.getY()-70);
 		soundonMenuItem.setPosition(soundoffMenuItem.getX(), soundoffMenuItem.getY());
 		
 		soundoffMenuItem.setVisible(!sound);
 		soundonMenuItem.setVisible(sound);
 
 		menuChildScene.setOnMenuItemClickListener(this);
 
 		setChildScene(menuChildScene);
 	}
 
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem,
 			float pMenuItemLocalX, float pMenuItemLocalY) {
 		switch (pMenuItem.getID()) {
 		case MENU_PLAY:
 			disposeScene();
 			SceneManager.getInstance().loadSelectionScene(engine);
 			return true;
 		case MENU_OPTIONS:
 			soundoffMenuItem.setVisible(sound);
 			soundonMenuItem.setVisible(!sound);
 			sound = !sound;
 			if (sound) {
 				resourcesManager.musicManager.setMasterVolume(1);
 				resourcesManager.music.resume();
 			} else {
 				resourcesManager.musicManager.setMasterVolume(0);
 				resourcesManager.music.pause();
 			}
 			return true;
 		case MENU_SHOP:
 			disposeScene();
 			SceneManager.getInstance().loadShopScene(engine);
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public void setSettings(Settings settings) {
 		this.settings = settings;
 		
 	}
 }
