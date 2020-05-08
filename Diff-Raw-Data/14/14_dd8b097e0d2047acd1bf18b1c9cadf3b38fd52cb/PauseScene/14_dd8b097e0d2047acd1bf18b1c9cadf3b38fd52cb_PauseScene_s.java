 package ch.hsr.wa.entity.scenes;
 
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import android.util.Log;
 import ch.hsr.wa.SceneManager;
 import ch.hsr.wa.hud.GameHUD;
 import ch.hsr.wa.options.GameOptions;
 import ch.hsr.wa.util.TextureLoader;
 
 public class PauseScene extends GameScene {
 
 	private static final String GFX_MENU = "gfx/menu/menu_menu.png";
 	private static final String GFX_PLAY = "gfx/menu/play_menu.png";
 	private static final String GFX_BG = "gfx/menu/pause_bg.png";
 	private final TextureLoader mTextureLoader;
 	private final SceneManager mSceneManager;
 	private ButtonSprite mPlayButtonSprite;
 	private Sprite mBackgroundSprite;
 	private final GameHUD mHUD;
 	private ButtonSprite mMenuButtonSprite;
 	private ITextureRegion mBackgroundTextureRegion;
 	private ITextureRegion mPlayButtonTextureRegion;
 	private ITextureRegion mMenuButtonTextureRegion;
 
 	public PauseScene(SceneManager pSceneManager, TextureLoader pTextureLoader, GameHUD pHUD) {
 		this.mSceneManager = pSceneManager;
 		this.mTextureLoader = pTextureLoader;
 		this.mHUD = pHUD;
 		setBackgroundEnabled(false);

 	}
 
 	@Override
 	public void load() {
 		super.load();
 		loadTextures();
 		createMenu();
 	}
 
 	private void loadTextures() {
 		mBackgroundTextureRegion = mTextureLoader.loadTexture(GFX_BG);
 		mPlayButtonTextureRegion = mTextureLoader.loadTexture(GFX_PLAY);
 		mMenuButtonTextureRegion = mTextureLoader.loadTexture(GFX_MENU);
 
 	}
 
 	private void createMenu() {
 		addBackground();
 		addPlayButton();
 		addMenuButton();
 	}
 
 	private void addBackground() {
 		mBackgroundSprite = new Sprite((GameOptions.CAMERA_WIDTH - mBackgroundTextureRegion.getWidth()) / 2,
 				(GameOptions.CAMERA_HEIGHT - mBackgroundTextureRegion.getHeight()) / 2, mBackgroundTextureRegion,
 				mSceneManager.getVertexBufferObjectManager());
 		attachChild(mBackgroundSprite);
 		registerTouchArea(mBackgroundSprite);
 	}
 
 	private void addMenuButton() {
 		mMenuButtonSprite = new ButtonSprite((mBackgroundTextureRegion.getWidth() - mMenuButtonTextureRegion.getWidth()) / 2,
 				100 + mPlayButtonTextureRegion.getHeight(), mMenuButtonTextureRegion, mSceneManager.getVertexBufferObjectManager(),
 				mOnMenuClickListener);
 		mBackgroundSprite.attachChild(mMenuButtonSprite);
 		registerTouchArea(mMenuButtonSprite);
 	}
 
 	private void addPlayButton() {
 		mPlayButtonSprite = new ButtonSprite((mBackgroundTextureRegion.getWidth() - mPlayButtonTextureRegion.getWidth()) / 2, 100,
 				mPlayButtonTextureRegion, mSceneManager.getVertexBufferObjectManager(), mOnPlayClickListener);
 		mBackgroundSprite.attachChild(mPlayButtonSprite);
 		registerTouchArea(mPlayButtonSprite);
 	}
 
 	private OnClickListener mOnPlayClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 			Log.d("TOUCH", "PAUSE_MENU");
 			mHUD.clearChildScene();
 			mSceneManager.getEngine().getScene().setChildScene(mHUD);
 
 		}
 	};
 	private OnClickListener mOnMenuClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(ButtonSprite arg0, float arg1, float arg2) {
 			mSceneManager.showPreviousScene();
 
 		}
 	};
 
 }
