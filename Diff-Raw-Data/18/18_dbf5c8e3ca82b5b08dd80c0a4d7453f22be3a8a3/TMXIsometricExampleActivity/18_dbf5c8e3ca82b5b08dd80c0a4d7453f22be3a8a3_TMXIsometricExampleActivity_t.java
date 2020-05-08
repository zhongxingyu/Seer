 package com.cyagamesocial.activity;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.andengine.audio.sound.Sound;
 import org.andengine.audio.sound.SoundFactory;
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.primitive.Line;
 import org.andengine.entity.scene.IOnAreaTouchListener;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.scene.background.RepeatingSpriteBackground;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLayerObjectTiles;
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.andengine.extension.tmx.TMXProperties;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.extension.tmx.TMXTileProperty;
 import org.andengine.extension.tmx.TMXTiledMap;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.PinchZoomDetector;
 import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.database.DataSetObserver;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.cyagamesocial.R;
 import com.cyagamesocial.log.IErrorLog;
 import com.cyagamesocial.log.Logging;
 import com.cyagamesocial.unity.MyCamera;
 import com.cyagamesocial.utils.Config;
 import com.cyagamesocial.utils.StateOfTile;
 
 public class TMXIsometricExampleActivity extends BaseGameActivity implements
 		IOnSceneTouchListener, IScrollDetectorListener,
 		IPinchZoomDetectorListener, ITMXTilePropertiesListener, StateOfTile {
 
 	private final String TAG = "TestIsometricMap";
 	
 	
 	private Dialog mDialog;
 	
 	// Camera stuff
 	
 	public int CAMERA_WIDTH = 800;
 	public int CAMERA_HEIGHT = 480;
 	public ZoomCamera mCamera;
 
 	private RepeatingSpriteBackground mGrassBackground;
 
 	public ScrollDetector mScrollDetector;
 	public PinchZoomDetector mPinchZoomDetector;
 	public float mPinchZoomStartedCameraZoomFactor;
 	private float maxZoom = 1;
 	private final float zoomDepth = 5; // Smaller this is, the less we zoom in?
 	private final float minZoom = 1f;
 	private boolean mClicked = false;
 
 	private HUD mHUD;
 	private Text mFPS;
 	private Text mXYLoc;
 	private Text mTileRowCol1;
 
 	public Engine mEngine;
 	public IErrorLog log = null;
 
 	// Map and current layer.
 	public TMXLayer currentLayer = null;
 	public TMXTiledMap mMap = null;
 
 	TMXLayer tmxLayer;
 
 	// menu scenes + colour for menus
 	// protected Background mMenuBackground = new Background(1f, 1f, 0.5f,
 	// 0.35f);
 
 	org.andengine.util.color.Color selected = new org.andengine.util.color.Color(
 			1f, 0f, 0f);
 	org.andengine.util.color.Color unselected = new org.andengine.util.color.Color(
 			0f, 0f, 0f);
 	org.andengine.util.color.Color objectLines = new org.andengine.util.color.Color(
 			0.4823f, 0.8313f, 0.3254f);
 
 	// fonts
 	private String mFontFile = "font/Vnidisneynornal.ttf";
 	private FontManager mFontManager;
 	private MapHandler mMapHandler;
 	// The zap!
 	private Sound mZap;
 	private final String mSound = "11152__jimpurbrick__polysixslowinglaser1.wav";
 	// Little friend..
 	private Vibrator mLittleFriend;
 
 	// Lines belong to tile hits, objects
 	public ArrayList<Line> mDrawnLines = new ArrayList<Line>();
 	public ArrayList<TMXLayerObjectTiles> mTileObject = new ArrayList<TMXLayerObjectTiles>();
 
 	// TMXFiles
 	public String TMXAssetsLocation = "tmx/map_pro/";
 	public String TMXFileTag = ".tmx";
 
 	public String TMXFileMap = "map_pro";
 	public boolean[][] TilesBlocked;
 
 	private TMXTiledMap mTMXTiledMap;
 
 	// State of Item Tile
 	/*
 	 * Kiem tra cac phan tu cua TileMap da duoc addSprite hay chua Tuy vao moi
 	 * loai Sprite co 1 State khac nhau State Defaut la 0: Chua duoc add Sprite
 	 * nao
 	 */
 
 	public int[][] StateofTile = new int[50][50];
 
 	// Bitmap
 	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
 	private TiledTextureRegion mCarTextureRegion;
 	private AnimatedSprite player;
 	private BitmapTextureAtlas mBitmapTextureAtlas2;
 	private ITextureRegion mCayTextureRegion;
 	
 	/*
 	 *HUD 
 	 */
 	//Icon Setting
 	private BitmapTextureAtlas mHUD_Icon_Setting_BitmapTextureAtlas;
 	private ITextureRegion mHUD_Icon_Setting_ITextureRegion;
 	private Sprite mHUD_Icon_Setting_Sprite;
 	
 	// Bg_House0
 	private BitmapTextureAtlas mBg_House0_BitmapTextureAtlas;
 	private ITextureRegion mBg_House0_ITextureRegion;
 	private Sprite mBg_House0_Sprite;
 	// Bg_House1
 	private BitmapTextureAtlas mBg_House1_BitmapTextureAtlas;
 	private ITextureRegion mBg_House1_ITextureRegion;
 	private Sprite mBg_House1_Sprite;
 
 	// House1
 	private BitmapTextureAtlas m_House1_BitmapTextureAtlas;
 	private ITextureRegion m_House1_ITextureRegion;
 	private Sprite m_House1_Sprite;
 
 	// Bg_House1_Rot
 	private BitmapTextureAtlas mBg_House1_Rot_BitmapTextureAtlas;
 	private ITextureRegion mBg_House1_Rot_ITextureRegion;
 	private Sprite mBg_House1_Rot_Sprite;
 
 	// Bg_House4
 	private BitmapTextureAtlas mBg_House4_BitmapTextureAtlas;
 	private ITextureRegion mBg_House4_ITextureRegion;
 	private Sprite mBg_House4_Sprite;
 
 	// House4
 	private BitmapTextureAtlas m_House4_BitmapTextureAtlas;
 	private ITextureRegion m_House4_ITextureRegion;
 	private Sprite m_House4_Sprite;
 	
 	// Kiem tra collidson cua new sprite voi old sprite
 	ArrayList<Sprite> mSpriteCollid = null;
 
 	private Sprite cay;
 	
 	
 	
 
 	@Override
 	protected void onCreate(Bundle pSavedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(pSavedInstanceState);
 	}
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.log = new Logging();
 		this.log.setTag(this.TAG);
 		this.log.enable(false);
 		this.log.i(0, "onCreateEngineOptions");
 		final DisplayMetrics displayMetrics = new DisplayMetrics();
 		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
 		CAMERA_WIDTH = displayMetrics.widthPixels;
 		CAMERA_HEIGHT = displayMetrics.heightPixels;
 		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		EngineOptions eOps = new EngineOptions(true,
 				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
 						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
 		eOps.getAudioOptions().setNeedsSound(true);
 
 		return eOps;
 	}
 
 	@Override
 	public Engine onCreateEngine(EngineOptions pEngineOptions) {
 
 		Config.BGHOUSE_SELECTED = 4;
 
 		this.log.i(0, "onCreateEngine");
 		this.mEngine = new Engine(pEngineOptions);
 		return this.mEngine;
 	}
 
 	@Override
 	public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws Exception {
 		this.log.i(0, "onCreateResources");
 		this.mFontManager = new FontManager(this, this.mFontFile);
 		this.mMapHandler = new MapHandler(this);
 
 		this.mScrollDetector = new SurfaceScrollDetector(this);
 		this.mPinchZoomDetector = new PinchZoomDetector(this);
 		this.mLittleFriend = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		SoundFactory.setAssetBasePath("mfx/");
 		
 		//LoadResource HUD
 		load_Icon_Setting_Resource();
 		
 		
 		// LoadResource
 		loadBg_House0Resource();
 		loadBg_House1Resource();
 		loadBg_House1_Rot_Resource();
 		load_House1Resource();
 		loadBg_House4_Resource();
 		load_House4_Resource();
 
 		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
 				this.getTextureManager(), 186, 154, TextureOptions.NEAREST);
 		try {
 			this.mBitmapTextureAtlas
 					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 							0, 0, 1));
 			this.mBitmapTextureAtlas.load();
 			this.mZap = SoundFactory.createSoundFromAsset(this.getEngine()
 					.getSoundManager(), this, this.mSound);
 		} catch (final IOException e) {
 			Debug.e(e);
 		}
 
 		pOnCreateResourcesCallback.onCreateResourcesFinished();
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws Exception {
 		this.log.i(0, "onCreateScene");
 		Scene mScene = new Scene();
 		final FPSLogger fpsLogger = new FPSLogger();
 		this.getEngine().registerUpdateHandler(fpsLogger);
 		mScene.setBackground(new Background(0.9686f, 0.8588f, 0.6941f));
 
 		this.mHUD = new HUD();
 		this.getEngine().getCamera().setHUD(this.mHUD);
 		this.mFPS = new Text(0, 0, this.mFontManager.Font_HUD, "FPS:",
 				"FPS: XXXXXXXXXXXXXXXXX".length(),
 				this.getVertexBufferObjectManager());
 		this.mXYLoc = new Text(0, this.mFPS.getY() + 1
 				+ this.mFPS.getFont().getLineHeight(),
 				this.mFontManager.Font_HUD, "Touch X: Y:",
 				"Touch X: XXXXXXXXXXXX Y: XXXXXXXXXX".length(),
 				this.getVertexBufferObjectManager());
 		this.mTileRowCol1 = new Text(0, this.mXYLoc.getY() + 1
 				+ this.mXYLoc.getFont().getLineHeight(),
 				this.mFontManager.Font_HUD, "Row: Col:",
 				"Row: Not in Bounds Col: Not in Bounds".length(),
 				this.getVertexBufferObjectManager());
 		float mHUD_Icon_Setting_X=CAMERA_WIDTH-this.mHUD_Icon_Setting_ITextureRegion.getWidth();
 		float mHUD_Icon_Setting_Y=CAMERA_HEIGHT-this.mHUD_Icon_Setting_ITextureRegion.getHeight();
 		
 		this.mHUD_Icon_Setting_Sprite=new Sprite(mHUD_Icon_Setting_X, mHUD_Icon_Setting_Y, this.mHUD_Icon_Setting_ITextureRegion, this.getVertexBufferObjectManager());
 		this.mHUD.attachChild(this.mHUD_Icon_Setting_Sprite);
 		this.mHUD.registerTouchArea(this.mHUD_Icon_Setting_Sprite);
 		this.mHUD.attachChild(this.mFPS);
 		this.mHUD.attachChild(this.mXYLoc);
 		this.mHUD.attachChild(this.mTileRowCol1);
 		this.mHUD.setOnAreaTouchListener(new IOnAreaTouchListener() {
 			
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 					ITouchArea pTouchArea, float pTouchAreaLocalX,
 					float pTouchAreaLocalY) {
 				// TODO Auto-generated method stub
 				Sprite sprite=(Sprite)pTouchArea;
 				if(pSceneTouchEvent.isActionDown()){
 					if(sprite.equals(TMXIsometricExampleActivity.this.mHUD_Icon_Setting_Sprite)){
 						TMXIsometricExampleActivity.this.runOnUiThread(new Runnable() {
 							
 							@Override
 							public void run() {
 								// TODO Auto-generated method stub
 		//						Toast.makeText(getBaseContext(), "Touched Setting", Toast.LENGTH_SHORT).show();
 								showDialog();
 								
 							}
 
 							
 							
 						});
 					}
 				}
 				return true;
 			}
 		});
 		mScene.registerUpdateHandler(new TimerHandler(.5f, true,
 				new ITimerCallback() {
 					@Override
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						mFPS.setText("FPS: " + fpsLogger.getFPS());
 					}
 				}));
 		
 		pOnCreateSceneCallback.onCreateSceneFinished(mScene);
 
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 		this.log.i(0, "onPopulateScene");
 		pScene.setOnSceneTouchListener(this);
 		pScene.setTouchAreaBindingOnActionMoveEnabled(true);
 		pScene.setOnAreaTouchTraversalFrontToBack();
 
 		this.mMapHandler.loadMap();
 
 		// Add sprite
 		
 		this.mMapHandler.attachAllObjectLayers();
 
 		pOnPopulateSceneCallback.onPopulateSceneFinished();
 	}
 
 	@Override
 	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
 			TouchEvent pSceneTouchEvent) {
 		this.log.i(1, "onPinchZoomStarted");
 		this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
 		this.mClicked = false;
 	}
 
 	@Override
 	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
 			TouchEvent pTouchEvent, float pZoomFactor) {
 		this.log.i(1, "onPinchZoom");
 		this.mCamera.setZoomFactor(Math.min(
 				Math.max(this.minZoom, this.mPinchZoomStartedCameraZoomFactor
 						* pZoomFactor), this.zoomDepth));
 		this.mClicked = false;
 	}
 
 	@Override
 	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
 			TouchEvent pTouchEvent, float pZoomFactor) {
 		this.log.i(1, "onPinchZoomFinished");
 		this.mClicked = false;
 	}
 
 	@Override
 	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
 			float pDistanceX, float pDistanceY) {
 		this.log.i(1, "onScrollStarted");
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
 			float pDistanceX, float pDistanceY) {
 		this.log.i(1, "onScroll");
 		final float zoomFactor = mCamera.getZoomFactor();
 		float xLocation = -pDistanceX / zoomFactor;
 		float yLocation = -pDistanceY / zoomFactor;
 		mCamera.offsetCenter(xLocation, yLocation);
 		this.mClicked = false;
 	}
 
 	@Override
 	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
 			float pDistanceX, float pDistanceY) {
 		this.log.i(1, "onScrollFinished");
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		this.log.i(2, "onSceneTouchEvent");
 
 		if (this.mPinchZoomDetector != null) {
 			this.log.i(0, "PinchZoomDetector");
 			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
 			if (this.mPinchZoomDetector.isZooming()) {
 				this.mScrollDetector.setEnabled(false);
 			} else {
 				if (pSceneTouchEvent.isActionDown()) {
 					this.mScrollDetector.setEnabled(true);
 				}
 				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
 			}
 		} else {
 			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
 		}
 
 		if (pSceneTouchEvent.isActionUp()) {
 			if (this.mClicked) {
 
 				this.handleActionDown(pScene, pSceneTouchEvent);
 
 			}
 			this.mClicked = true;
 		}
 		return true;
 	}
 
 	private void handleActionDown(final Scene pScene,
 			TouchEvent pSceneTouchEvent) {
 		this.log.i(4, String.format("Touch X: %f Y: %f",
 				pSceneTouchEvent.getX(), pSceneTouchEvent.getY()));
 		this.touchMap(pScene, pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 
 	}
 
 	public void resetCamera() {
 		this.log.i(4, "Reset camera");
 		this.mCamera.setZoomFactor(this.maxZoom);
 		this.mCamera.setCenter(0, 0);
 		this.mXYLoc.setText("Touch X:0 Y:0");
 		this.mTileRowCol1.setText("Row: Not in Bounds Col: Not in Bounds");
 	}
 
 	/**
 	 * Setup the Camera if the map is isometric.
 	 * 
 	 * @param height
 	 *            {@link Float} overall height of the map
 	 * @param width
 	 *            {@link Float} overall width of the map
 	 */
 	public void setupCameraIsometric(final float height, final float width) {
 		this.log.i(4, "Setup camera");
 
 		final float MAX_CAMERA_BOUND_ADDITION = 60;
 		final float halfTileWidth = this.mMap.getTileWidth() / 2;
 		final float xMin = this.mMap.getTileRows() * halfTileWidth;
 		final float xMax = this.mMap.getTileColumns() * halfTileWidth;
 		final float pBoundsXMin = halfTileWidth - xMin
 				- MAX_CAMERA_BOUND_ADDITION + 200;
 		final float pBoundsYMin = -MAX_CAMERA_BOUND_ADDITION + 300;
 		final float pBoundsXMax = halfTileWidth + xMax
 				+ MAX_CAMERA_BOUND_ADDITION - 200;
 		final float pBoundsYMax = height + MAX_CAMERA_BOUND_ADDITION - 300;
 		this.mCamera.setBounds(pBoundsXMin, pBoundsYMin, pBoundsXMax,
 				pBoundsYMax);
 		this.mCamera.setBoundsEnabled(true);
 		this.resetCamera();
 
 	}
 
 	public void touchMap(final Scene pScene, final float pX, final float pY) {
 		// Standard method of getting tile
 		final float[] pToTiles = this.getEngine().getScene()
 				.convertLocalToSceneCoordinates(pX, pY);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		final TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		if (tmxSelected != null) {
 			this.log.i(6, String.format(
 					"Standard getTMXTileAt - tile found Row: %d Column %d ",
 					tmxSelected.getTileRow(), tmxSelected.getTileColumn()));
 			this.mTileRowCol1.setText(String.format("Row: %d Col: %d",
 					tmxSelected.getTileColumn(), tmxSelected.getTileRow()));
 
 			if (Config.BGHOUSE_SELECTED == 0) {
 
 				if (StateofTile[tmxSelected.getTileColumn()][tmxSelected
 						.getTileRow()] == 0) {
 					addBg_House0(pScene, tmxSelected.getTileXIsoCentre(),
 							tmxSelected.getTileYIsoCentre());
 					StateofTile[tmxSelected.getTileColumn()][tmxSelected
 							.getTileRow()] = STATE_BGHOUSE0;
 				} else {
 					System.out.println("BGHouse0 has added this Tile");
 				}
 			} else if (Config.BGHOUSE_SELECTED == 1) {
 				if (StateofTile[tmxSelected.getTileColumn()][tmxSelected
 						.getTileRow()] == 0
 						&& StateofTile[tmxSelected.getTileColumn()][tmxSelected
 								.getTileRow() - 1] == 0) {
 					addBg_House1(pScene, tmxSelected.getTileXIsoCentre(),
 							tmxSelected.getTileYIsoCentre());
 					add_House1(pScene, tmxSelected.getTileXIsoCentre(),
 							tmxSelected.getTileYIsoCentre());
 					StateofTile[tmxSelected.getTileColumn()][tmxSelected
 							.getTileRow()] = 2;
 					StateofTile[tmxSelected.getTileColumn()][tmxSelected
 							.getTileRow() - 1] = 2;
 				} else {
 					System.out.println("You can't bulid House1 in this Tile");
 				}
 
 			} else if (Config.BGHOUSE_SELECTED == 2) {
 				if (StateofTile[tmxSelected.getTileColumn()][tmxSelected
 						.getTileRow()] == 0
 						&& StateofTile[tmxSelected.getTileColumn() + 1][tmxSelected
 								.getTileRow()] == 0) {
 					addBg_House1_Rot(pScene, tmxSelected.getTileXIsoCentre(),
 							tmxSelected.getTileYIsoCentre());
 					StateofTile[tmxSelected.getTileColumn()][tmxSelected
 							.getTileRow()] = 2;
 					StateofTile[tmxSelected.getTileColumn() + 1][tmxSelected
 							.getTileRow()] = 2;
 				} else {
 					System.out
 							.println("You can't bulid House1_Rot in this Tile");
 				}
 			} else if (Config.BGHOUSE_SELECTED == 4) {
 				if (StateofTile[tmxSelected.getTileColumn()][tmxSelected
 						.getTileRow()] == 0
 						&& StateofTile[tmxSelected.getTileColumn()][tmxSelected
 								.getTileRow() - 1] == 0
 						&& StateofTile[tmxSelected.getTileColumn() + 1][tmxSelected
 								.getTileRow()] == 0
 						&& StateofTile[tmxSelected.getTileColumn() + 1][tmxSelected
 								.getTileRow() - 1] == 0) {
 					// addBg_House4(pScene, tmxSelected.getTileXIsoCentre(),
 					// tmxSelected.getTileYIsoCentre());
 					add_House4(pScene, tmxSelected.getTileXIsoCentre(),
 							tmxSelected.getTileYIsoCentre());
 					 StateofTile[tmxSelected.getTileColumn()][tmxSelected.getTileRow()]=4;
 					 StateofTile[tmxSelected.getTileColumn()][tmxSelected.getTileRow()-1]=4;
 					 StateofTile[tmxSelected.getTileColumn()+1][tmxSelected.getTileRow()]=4;
 					 StateofTile[tmxSelected.getTileColumn()+1][tmxSelected.getTileRow()-1]=4;
 					//
 				} else {
 					this.runOnUiThread(new Runnable() {
 						
 						@Override
 						public void run() {
 							// TODO Auto-generated method stub
 							System.out.println("Touched House 4");
 //							Toast.makeText(getBaseContext(), "Touched House 4", Toast.LENGTH_SHORT).show();
 							mDialog=new Dialog(TMXIsometricExampleActivity.this);
 							mDialog.setContentView(R.layout.custom_dialog);
 							mDialog.getWindow().getAttributes().windowAnimations=R.style.PauseDialogAnimation;
 							mDialog.show();
 						}
 					});
 				}
 			}
 
 		} else {
 			this.mTileRowCol1.setText(String.format("Row: %s Col: %s",
 					"Not in Bounds", "Not in Bounds"));
 		}
 
 		this.mXYLoc.setText(String.format("Touch X: %f Y: %f", pX, pY));
 
 	}
 
 	@Override
 	public void onTMXTileWithPropertiesCreated(TMXTiledMap pTMXTiledMap,
 			TMXLayer pTMXLayer, TMXTile pTMXTile,
 			TMXProperties<TMXTileProperty> pTMXTileProperties) {
 		this.log.i(9, "onTMXTileWithPropertiesCreated");
 	}
 
 	// Bg_House0
 
 	public void loadBg_House0Resource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/map_pro/");
 		this.mBg_House0_BitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 64, 32, TextureOptions.BILINEAR);
 		this.mBg_House0_ITextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mBg_House0_BitmapTextureAtlas, this,
 						"bg_house0.png", 0, 0);
 		this.mBg_House0_BitmapTextureAtlas.load();
 
 	}
 
 	public void addBg_House0(Scene pScene, float x, float y) {
 		this.mBg_House0_Sprite = new Sprite(x - 32, y - 16,
 				mBg_House0_ITextureRegion, this.getVertexBufferObjectManager());
 		pScene.attachChild(mBg_House0_Sprite);
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(x ,y);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
		this.mBg_House0_Sprite.setZIndex(row+col);
		pScene.sortChildren();
		
 	}
 
 	// Bg_House1
 
 	public void loadBg_House1Resource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/map_pro/");
 		this.mBg_House1_BitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 96, 48, TextureOptions.BILINEAR);
 		this.mBg_House1_ITextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mBg_House1_BitmapTextureAtlas, this,
 						"bg_house1.png", 0, 0);
 		this.mBg_House1_BitmapTextureAtlas.load();
 
 	}
 
 	public void addBg_House1(Scene pScene, float x, float y) {
 		this.mBg_House1_Sprite = new Sprite(x - 32, y - 32,
 				mBg_House1_ITextureRegion, this.getVertexBufferObjectManager());
 		pScene.attachChild(mBg_House1_Sprite);
		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(x ,y);
		this.currentLayer = this.mMap.getTMXLayers().get(0);
		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
				pToTiles[1]);
		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
		int row = tmxSelected.getTileColumn();
		int col = tmxSelected.getTileRow();
		this.mBg_House1_Sprite.setZIndex(row+col);
		pScene.sortChildren();
 	}
 
 	// House1
 	public void load_House1Resource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/map_pro/");
 		this.m_House1_BitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 96, 125, TextureOptions.BILINEAR);
 		this.m_House1_ITextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(m_House1_BitmapTextureAtlas, this,
 						"house1.png", 0, 0);
 		this.m_House1_BitmapTextureAtlas.load();
 
 	}
 
 	public void add_House1(Scene pScene, float x, float y) {
 		this.m_House1_Sprite = new Sprite(x - 32, y - 32 - 76,
 				m_House1_ITextureRegion, this.getVertexBufferObjectManager());
 		pScene.attachChild(m_House1_Sprite);
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(x ,y);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
 		this.m_House1_Sprite.setZIndex(row+col);
		pScene.sortChildren();
 	}
 
 	// Bg_House2
 
 	public void loadBg_House1_Rot_Resource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/map_pro/");
 		this.mBg_House1_Rot_BitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 96, 48, TextureOptions.BILINEAR);
 		this.mBg_House1_Rot_ITextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mBg_House1_Rot_BitmapTextureAtlas, this,
 						"bg_house1_rot.png", 0, 0);
 		this.mBg_House1_Rot_BitmapTextureAtlas.load();
 
 	}
 
 	public void addBg_House1_Rot(Scene pScene, float x, float y) {
 		this.mBg_House1_Rot_Sprite = new Sprite(x - 32, y - 16,
 				mBg_House1_Rot_ITextureRegion,
 				this.getVertexBufferObjectManager());
 		pScene.attachChild(mBg_House1_Rot_Sprite);
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(x ,y);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
 		this.mBg_House1_Sprite.setZIndex(row+col);
		pScene.sortChildren();
 	}
 
 	// Bg_House4
 
 	public void loadBg_House4_Resource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/map_pro/");
 		this.mBg_House4_BitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 128, 64, TextureOptions.BILINEAR);
 		this.mBg_House4_ITextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mBg_House4_BitmapTextureAtlas, this,
 						"bg_house4.png", 0, 0);
 		this.mBg_House4_BitmapTextureAtlas.load();
 	}
 
 	public void addBg_House4(Scene pScene, float x, float y) {
 		this.mBg_House4_Sprite = new Sprite(x - 32, y - 32,
 				mBg_House4_ITextureRegion, this.getVertexBufferObjectManager());
 		pScene.attachChild(mBg_House4_Sprite);
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(x ,y);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
 		this.mBg_House4_Sprite.setZIndex(row+col);
 		pScene.sortChildren();
 	}
 
 	// House4
 
 	public void load_House4_Resource() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/map_pro/");
 		this.m_House4_BitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 128, 77, TextureOptions.BILINEAR);
 		this.m_House4_ITextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(m_House4_BitmapTextureAtlas, this,
 						"house4.png", 0, 0);
 		this.m_House4_BitmapTextureAtlas.load();
 
 	}
 
 	public void add_House4(final Scene pScene, float x, float y) {
 		this.m_House4_Sprite = new Sprite(x - 32, y - 32 - 12,
 				m_House4_ITextureRegion, this.getVertexBufferObjectManager());
 		this.m_House4_Sprite.setUserData("sprite_House4");
 		
 		pScene.attachChild(this.m_House4_Sprite);
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(x ,y);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
 		this.m_House4_Sprite.setZIndex(row+col);
 		pScene.sortChildren();
 		System.out.println("Zindex:"+this.m_House4_Sprite.getZIndex());
 	}
 	
 	
 	//Load resource icon_setting
 	public void load_Icon_Setting_Resource(){
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		this.mHUD_Icon_Setting_BitmapTextureAtlas=new BitmapTextureAtlas(this.getTextureManager(), 64, 64,TextureOptions.BILINEAR);
 		this.mHUD_Icon_Setting_ITextureRegion=BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHUD_Icon_Setting_BitmapTextureAtlas, this, "icon_setting.png",0,0);
 		this.mHUD_Icon_Setting_BitmapTextureAtlas.load();
 	} 
 	
 	
 	//Add Setting
 	
 
 	public ArrayList<Sprite> checkColision(Sprite sprite, Scene pScene) {
 		ArrayList<Sprite> collidsonSprite = new ArrayList<Sprite>();
 		int count = pScene.getChildCount();
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(sprite.getX() + 32,sprite.getY() + 32 + 12);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
 		for (int i = 1; i < count; i++) {
 			//Reset Zindex
 			IEntity entity = pScene.getChildByIndex(i);
 			
 			entity.setZIndex(0);
 			
 			if (entity instanceof Sprite) {
 				if (entity.getUserData().equals("sprite_House4")) {
 
 					if (((Sprite) entity).collidesWith(sprite)) {
 						System.out.println("Co va cham voi sprite da co");
 						float[] pToTiles_en = this.getEngine().getScene().convertLocalToSceneCoordinates(entity.getX() + 32,entity.getY() + 32 + 12);
 						this.currentLayer = this.mMap.getTMXLayers().get(0);
 						TMXTile tmxSelected_en = this.currentLayer
 								.getTMXTileAt(pToTiles_en[0], pToTiles_en[1]);
 						System.out.println("Get XY Row:"
 								+ tmxSelected_en.getTileColumn() + "Col:"
 								+ tmxSelected_en.getTileRow());
 						if (row < tmxSelected_en.getTileColumn()
 								|| col < tmxSelected_en.getTileRow()) {
 							collidsonSprite.add((Sprite) entity);
 							System.out.println("Zindex:"+entity.getZIndex());
 						}
 					}
 				}
 			}
 		}
 		return collidsonSprite;
 	}
 
 	public void checkColision_en(Sprite sprite, Scene pScene) {
 		int count = pScene.getChildCount();
 		ArrayList<Sprite> collidsonSprite = new ArrayList<Sprite>();
 		float[] pToTiles = this.getEngine().getScene().convertLocalToSceneCoordinates(sprite.getX() + 32,sprite.getY() + 32 + 12);
 		this.currentLayer = this.mMap.getTMXLayers().get(0);
 		final TMXTile tmxSelected = this.currentLayer.getTMXTileAt(pToTiles[0],
 				pToTiles[1]);
 		System.out.println("Get XY Row:" + tmxSelected.getTileColumn() + "Col:"+ tmxSelected.getTileRow());
 		int row = tmxSelected.getTileColumn();
 		int col = tmxSelected.getTileRow();
 
 		for (int i = 0; i < count; i++) {
 			IEntity entity = pScene.getChildByIndex(i);
 			if (entity instanceof Sprite) {
 				if (entity.getUserData().equals("sprite_House4")) {
 					if (((Sprite) entity).collidesWith(sprite)) {
 						System.out.println("Co va cham voi sprite da co");
 						float[] pToTiles_en = this.getEngine().getScene().convertLocalToSceneCoordinates(entity.getX() + 32,entity.getY() + 32 + 12);
 						this.currentLayer = this.mMap.getTMXLayers().get(0);
 						TMXTile tmxSelected_en = this.currentLayer.getTMXTileAt(pToTiles_en[0], pToTiles_en[1]);
 						System.out.println("Get XY Row:"+ tmxSelected_en.getTileColumn() + "Col:"+ tmxSelected_en.getTileRow());
 						if (row < tmxSelected_en.getTileColumn()|| col < tmxSelected_en.getTileRow()) {
 							System.out.println("Test va cham");
 							collidsonSprite.add((Sprite) entity);
 						}
 					}
 				}
 			}
 		}
 		if(collidsonSprite.size()>0){
 			pScene.detachChild(sprite);
 			pScene.attachChild(sprite);
 			for(int i=0;i<collidsonSprite.size();i++){
 				pScene.detachChild(collidsonSprite.get(i));
 				pScene.attachChild(collidsonSprite.get(i));
 			}
 		}else{
 			pScene.attachChild(sprite);
 		}
 	}
 	private void showDialog() {
 		// TODO Auto-generated method stub
 		mDialog=new Dialog(TMXIsometricExampleActivity.this);
 		mDialog.setContentView(R.layout.custom_dialog);
 		mDialog.getWindow().getAttributes().windowAnimations=R.style.PauseDialogAnimation;
 
 		final Context context=TMXIsometricExampleActivity.this.getApplicationContext();
 		ListView mListView=(ListView)mDialog.findViewById(R.id.listView1);
 		final String[] mStrings={"aaa","bbb","ccc","ddd","eee","fff","ggg"};
 		final Integer[] mIntegers={R.drawable.aaa,R.drawable.bbb,R.drawable.ccc,R.drawable.ddd,R.drawable.ddd,R.drawable.fff,R.drawable.ggg};
 		mListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				System.out.println("Touch Item");
 			}
 		});
 		mListView.setAdapter(new ListAdapter() {
 			
 			@Override
 			public void unregisterDataSetObserver(DataSetObserver observer) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void registerDataSetObserver(DataSetObserver observer) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public boolean isEmpty() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public boolean hasStableIds() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public int getViewTypeCount() {
 				// TODO Auto-generated method stub
 				return mStrings.length;
 			}
 			@Override
 			public View getView(final int position, View convertView, ViewGroup parent) {
 				// TODO Auto-generated method stub
 				if(convertView==null){
 					LayoutInflater layout=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 					convertView=layout.inflate(R.layout.row_item, parent,false);
 					TextView tv=(TextView)convertView.findViewById(R.id.textView1);
 					tv.setText(mStrings[position]);
 					tv.setOnClickListener(new OnClickListener() {
 						
 						@Override
 						public void onClick(View v) {
 							// TODO Auto-generated method stub
 							System.out.println("Touch Text View");
 							mDialog.dismiss();
 						}
 					});
 					ImageView img=(ImageView)convertView.findViewById(R.id.imageView1);
 					img.setImageResource(mIntegers[position]);
 					img.setOnClickListener(new OnClickListener() {
 						
 						@Override
 						public void onClick(View v) {
 							// TODO Auto-generated method stub
 							mDialog.dismiss();
 							Config.BGHOUSE_SELECTED=position;
 							
 						}
 					});
 				}
 				return convertView;
 			}
 			
 			@Override
 			public int getItemViewType(int position) {
 				// TODO Auto-generated method stub
 				return position;
 			}
 			
 			@Override
 			public long getItemId(int position) {
 				// TODO Auto-generated method stub
 				return position;
 			}
 			
 			@Override
 			public Object getItem(int position) {
 				// TODO Auto-generated method stub
 				return position;
 			}
 			
 			@Override
 			public int getCount() {
 				// TODO Auto-generated method stub
 				return mStrings.length;
 			}
 			
 			@Override
 			public boolean isEnabled(int position) {
 				// TODO Auto-generated method stub
 				return true;
 			}
 			
 			@Override
 			public boolean areAllItemsEnabled() {
 				// TODO Auto-generated method stub
 				return true;
 			}
 		});
 		mDialog.show();
 	}
 
 }
