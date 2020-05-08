 package com.adtworker.choose4u;
 
 import java.util.Random;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.Entity;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.andengine.entity.modifier.RotationModifier;
 import org.andengine.entity.scene.CameraScene;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleLayoutGameActivity;
 import org.andengine.util.modifier.IModifier;
 import org.andengine.util.modifier.ease.EaseQuadInOut;
 
 import android.graphics.Color;
 import android.view.KeyEvent;
 
 public class Choose4uActivity extends SimpleLayoutGameActivity {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 640;
 	private static final String TAG = "Choose4u";
 	private final Random mRandom = new Random(System.currentTimeMillis());
 	private int mFromRotation = 0;
 	private int mToRotation;
 
 	private Text mText;
 	private Font mFont;
 	private Camera mCamera;
 	private Scene mScene;
 	private Scene mRotateScene;
 	private boolean bRotating = false;
 	private boolean bFirstClick = true;
 	private BitmapTextureAtlas mBitmapTextureAtlas;
 	private ITextureRegion mPausedTextureRegion;
 	private ITextureRegion mButtonTextureRegion;
 	private ITextureRegion[] mRectagleTextureRegion = new ITextureRegion[4];
 	private CameraScene mPauseScene;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private boolean mMotionStreaking = true;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	protected int getLayoutID() {
 		return R.layout.main;
 	}
 
 	@Override
 	protected int getRenderSurfaceViewID() {
 		return R.id.rendersurfaceview;
 	}
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
 	}
 
 	@Override
 	public void onCreateResources() {
 		this.mFont = FontFactory.createFromAsset(this.getFontManager(),
 				this.getTextureManager(), 512, 512, TextureOptions.BILINEAR,
 				this.getAssets(), "Plok.ttf", 32, true, Color.WHITE);
 		this.mFont.load();
 
 		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 200, 870, TextureOptions.BILINEAR);
 		this.mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBitmapTextureAtlas, this, "paused.png",
 						0, 0);
 		this.mButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBitmapTextureAtlas, this, "next.png", 0,
 						50);
 		this.mRectagleTextureRegion[0] = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBitmapTextureAtlas, this, "red.png", 0,
 						150);
 		this.mRectagleTextureRegion[1] = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBitmapTextureAtlas, this, "green.png",
 						0, 330);
 		this.mRectagleTextureRegion[2] = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBitmapTextureAtlas, this, "blue.png", 0,
 						510);
 		this.mRectagleTextureRegion[3] = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mBitmapTextureAtlas, this, "yellow.png",
 						0, 690);
 
 		this.mBitmapTextureAtlas.load();
 	}
 
 	@Override
 	public Scene onCreateScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		this.mPauseScene = new CameraScene(this.mCamera);
 		/* Make the 'PAUSED'-label centered on the camera. */
 		final float centerX = (CAMERA_WIDTH - this.mPausedTextureRegion
 				.getWidth()) / 2;
 		final float centerY = (CAMERA_HEIGHT - this.mPausedTextureRegion
 				.getHeight()) / 2;
 		final Sprite pausedSprite = new Sprite(centerX, centerY,
 				this.mPausedTextureRegion, this.getVertexBufferObjectManager());
 		this.mPauseScene.attachChild(pausedSprite);
 		/* Makes the paused Game look through. */
 		this.mPauseScene.setBackgroundEnabled(false);
 
 		/* Create a nice scene with some rectangles. */
 		this.mScene = new Scene();
 		this.mRotateScene = new Scene();
 
 		final Entity rectangleGroup = new Entity(CAMERA_WIDTH / 2,
 				CAMERA_HEIGHT / 2);
 
 		addChoice(rectangleGroup, 0, -180, -180);
 		addChoice(rectangleGroup, 1, 0, -180);
 		addChoice(rectangleGroup, 2, 0, 0);
 		addChoice(rectangleGroup, 3, -180, 0);
 
 		mRotateScene.attachChild(rectangleGroup);
 
 		final Sprite buttonSprite = new Sprite(centerX
 				+ mButtonTextureRegion.getWidth() / 2, CAMERA_HEIGHT
 				- mButtonTextureRegion.getHeight(), this.mButtonTextureRegion,
 				this.getVertexBufferObjectManager()) {
 
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 
 				if (!bRotating) {
 
 					mToRotation = 7200 + mRandom.nextInt(8) * 90 + 45;
 
 					mRotateScene.getChild(0).registerEntityModifier(
 							new RotationModifier(10, mFromRotation,
 									mToRotation, pEntityModifierListener,
 									EaseQuadInOut.getInstance()));
 
 					if (bFirstClick) {
 						mRotateScene.getChild(1).registerEntityModifier(
 								new RotationModifier(2, 0, -90, EaseQuadInOut
 										.getInstance()));
 						bFirstClick = false;
 					}
 
 					mFromRotation = mToRotation - 7200;
 				}
 
 				return true;
 			}
 		};
 
 		this.mRotateScene.attachChild(buttonSprite);
 		this.mRotateScene.registerTouchArea(buttonSprite);
 		this.mRotateScene.setTouchAreaBindingOnActionDownEnabled(true);
 
 		mScene.setChildScene(mRotateScene);
 		// mScene.setTouchAreaBindingOnActionDownEnabled(true);
 
 		return mScene;
 	}
 	private IEntityModifierListener pEntityModifierListener = new IEntityModifierListener() {
 		@Override
 		public void onModifierStarted(IModifier<IEntity> pModifier,
 				IEntity pItem) {
 			bRotating = true;
 		}
 
 		@Override
 		public void onModifierFinished(IModifier<IEntity> pModifier,
 				IEntity pItem) {
 			bRotating = false;
 		}
 	};
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_MENU
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if (this.mEngine.isRunning()) {
 				this.mScene.getChildScene().setChildScene(this.mPauseScene,
 						false, true, true);
 				this.mEngine.stop();
 			} else {
 				this.mScene.getChildScene().clearChildScene();
 				this.mEngine.start();
 			}
 			return true;
 		} else {
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	private void addChoice(Entity parent, int i, float x, float y) {
 		final Sprite sprite = new Sprite(x, y, this.mRectagleTextureRegion[i],
 				this.getVertexBufferObjectManager()) {
 
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
 					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				switch (pSceneTouchEvent.getAction()) {
 					case TouchEvent.ACTION_DOWN :
 						this.setScale(1.5f);
 						break;
 
					default :
 						this.setScale(1.0f);
 						break;
 				}
 				return true;
 			}
 		};
 		parent.attachChild(sprite);
 		mRotateScene.registerTouchArea(sprite);
 
 	}
 
 }
