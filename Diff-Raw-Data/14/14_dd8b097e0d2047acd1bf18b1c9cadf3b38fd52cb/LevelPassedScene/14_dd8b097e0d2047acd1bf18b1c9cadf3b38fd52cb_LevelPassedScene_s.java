 package ch.hsr.wa.entity.scenes;
 
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import android.util.Log;
 import ch.hsr.wa.SceneManager;
 import ch.hsr.wa.hud.GameHUD;
 import ch.hsr.wa.levels.Level;
 import ch.hsr.wa.options.GameOptions;
 import ch.hsr.wa.util.LevelLoaderTask;
 import ch.hsr.wa.util.TextureLoader;
 
 public class LevelPassedScene extends GameScene {
 
 	private static final String GFX_LEVEL_SELECTION = "gfx/menu/level_selection_menu.png";
 	private static final String GFX_RETRY = "gfx/menu/retry_menu.png";
 	private static final String GFX_HELP = "gfx/menu/help_menu2.png";
 	private static final String GFX_SOUND = "gfx/menu/sound_menu.png";
 	private static final String GFX_PLAY_NEXT_LEVEL = "gfx/menu/play_next_level_menu.png";
 	private static final String GFX_BG = "gfx/menu/level_passed_bg.png";
 	private final TextureLoader mTextureLoader;
 	private final SceneManager mSceneManager;
 
 	private Sprite mBackgroundSprite;
 	private ButtonSprite mLevelSelectionButtonSprite;
 	private ButtonSprite mRetryButtonSprite;
 	private ButtonSprite mHelpButtonSprite;
 	private ButtonSprite mSoundButtonSprite;
 	private ButtonSprite mPlayNextLevelButtonSprite;
 
 	private ITextureRegion mBackgroundTextureRegion;
 	private ITextureRegion mLevelSelectionButtonTextureRegion;
 	private ITextureRegion mRetryButtonTextureRegion;
 	private ITextureRegion mHelpButtonTextureRegion;
 	private ITextureRegion mSoundButtonTextureRegion;
 	private ITextureRegion mPlayNextLevelButtonTextureRegion;
 	private final GameHUD mHUD;
 	private Level mCurrentLevel;
 
 	public LevelPassedScene(SceneManager pSceneManager, TextureLoader pTextureLoader, GameHUD pHUD) {
 		this.mSceneManager = pSceneManager;
 		this.mTextureLoader = pTextureLoader;
 		this.mHUD = pHUD;
 		this.mCurrentLevel = pHUD.getCurrentLevel();
 		setBackgroundEnabled(false);
 	}
 
 	@Override
 	public void load() {
 		loadTextures();
 		createMenu();
 	}
 
 	private void loadTextures() {
 		mBackgroundTextureRegion = mTextureLoader.loadTexture(GFX_BG);
 		mLevelSelectionButtonTextureRegion = mTextureLoader.loadTexture(GFX_LEVEL_SELECTION);
 		mRetryButtonTextureRegion = mTextureLoader.loadTexture(GFX_RETRY);
 		mHelpButtonTextureRegion = mTextureLoader.loadTexture(GFX_HELP);
 		mSoundButtonTextureRegion = mTextureLoader.loadTexture(GFX_SOUND);
 		mPlayNextLevelButtonTextureRegion = mTextureLoader.loadTexture(GFX_PLAY_NEXT_LEVEL);
 	}
 
 	private void createMenu() {
 		mBackgroundSprite = new Sprite((GameOptions.CAMERA_WIDTH - mBackgroundTextureRegion.getWidth()) / 2,
 				(GameOptions.CAMERA_HEIGHT - mBackgroundTextureRegion.getHeight()) / 2, mBackgroundTextureRegion,
 				mSceneManager.getVertexBufferObjectManager());
 
 		attachChild(mBackgroundSprite);
 		addLevelSelectionButton();
 		addRetryButton();
 		addHelpButton();
 		addSoundButton();
 		if (mCurrentLevel.hasNextLevel())
 			addPlayNextLevelButton();
 
 	}
 
 	private void addLevelSelectionButton() {
 		mLevelSelectionButtonSprite = new ButtonSprite(0, 100, mLevelSelectionButtonTextureRegion, mSceneManager.getVertexBufferObjectManager());
 		mLevelSelectionButtonSprite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 				Log.d("TOUCH", "LEVEL_SELECTION_MENU");
 
 			}
 		});
 		mBackgroundSprite.attachChild(mLevelSelectionButtonSprite);
 		registerTouchArea(mLevelSelectionButtonSprite);
 	}
 
 	private void addRetryButton() {
 		mRetryButtonSprite = new ButtonSprite(100, 100, mRetryButtonTextureRegion, mSceneManager.getVertexBufferObjectManager());
 		mRetryButtonSprite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 				Log.d("TOUCH", "RETRY_MENU");
 				new LevelLoaderTask(mCurrentLevel, false, mSceneManager, mTextureLoader).execute();
 
 			}
 		});
 		mBackgroundSprite.attachChild(mRetryButtonSprite);
 		registerTouchArea(mRetryButtonSprite);
 	}
 
 	private void addHelpButton() {
 		mHelpButtonSprite = new ButtonSprite(200, 100, mHelpButtonTextureRegion, mSceneManager.getVertexBufferObjectManager());
 		mHelpButtonSprite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 				Log.d("TOUCH", "HELP_MENU");
 
 			}
 		});
 		mBackgroundSprite.attachChild(mHelpButtonSprite);
 		registerTouchArea(mHelpButtonSprite);
 	}
 
 	private void addSoundButton() {
 		mSoundButtonSprite = new ButtonSprite(300, 100, mSoundButtonTextureRegion, mSceneManager.getVertexBufferObjectManager());
 		mSoundButtonSprite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 				Log.d("TOUCH", "SOUND_MENU");
 
 			}
 		});
 		mBackgroundSprite.attachChild(mSoundButtonSprite);
 		registerTouchArea(mSoundButtonSprite);
 	}
 
 	private void addPlayNextLevelButton() {
 		mPlayNextLevelButtonSprite = new ButtonSprite((mBackgroundTextureRegion.getWidth() / 2) - (mPlayNextLevelButtonTextureRegion.getWidth() / 2),
 				180, mPlayNextLevelButtonTextureRegion, mSceneManager.getVertexBufferObjectManager());
 		mPlayNextLevelButtonSprite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 				Log.d("TOUCH", "PLAY_NEXT_LEVEL_MENU");
 				new LevelLoaderTask(mCurrentLevel.getNextLevel(), false, mSceneManager, mTextureLoader).execute();
 			}
 		});
 		mBackgroundSprite.attachChild(mPlayNextLevelButtonSprite);
 		registerTouchArea(mPlayNextLevelButtonSprite);
 	}
 
 }
