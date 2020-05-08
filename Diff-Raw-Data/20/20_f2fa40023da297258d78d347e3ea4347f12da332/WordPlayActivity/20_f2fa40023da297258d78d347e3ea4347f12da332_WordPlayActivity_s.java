 package edu.stanford.mobisocial.games.wordplay;
 
 import java.util.Calendar;
 import java.util.HashMap;
 
 import mobisocial.socialkit.User;
 import mobisocial.socialkit.musubi.DbObj;
 import mobisocial.socialkit.musubi.Musubi;
 import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
 import mobisocial.socialkit.musubi.multiplayer.TurnBasedMultiplayer;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.ZoomCamera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.input.touch.detector.ScrollDetector;
 import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.net.Uri;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.widget.Toast;
 import edu.stanford.mobisocial.games.wordplay.buttons.Button;
 import edu.stanford.mobisocial.games.wordplay.constants.BoardLayout;
 import edu.stanford.mobisocial.games.wordplay.players.Player;
 import edu.stanford.mobisocial.games.wordplay.tiles.BasicTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.DoubleLetterTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.DoubleWordTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.StartTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.TileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.TripleLetterTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.TripleWordTileSpace;
 import edu.stanford.mobisocial.games.wordplay.verifiers.Dictionary;
 import edu.stanford.mobisocial.games.wordplay.verifiers.TWLDictionary;
 
 public class WordPlayActivity extends BaseGameActivity  implements IScrollDetectorListener, IOnSceneTouchListener {
 	
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	static final int CAMERA_WIDTH = 320;
 	static final int CAMERA_HEIGHT = 480;
 	 
 	private static final String TAG = "WordPlayActivity";
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	public ZoomCamera mCamera;
 	
 	public HUD hud, tilePickerHud;
 	
 	private BitmapTextureAtlas mTexture;
 	private BitmapTextureAtlas  startTile, baseTile, doubleLetterTile, tripleLetterTile, doubleWordTile, tripleWordTile;
 	public TextureRegion startTileRegion, baseTileRegion, doubleLetterTileRegion, tripleLetterTileRegion, doubleWordTileRegion, tripleWordTileRegion;
 	
 	private BitmapTextureAtlas aTile, bTile, cTile, dTile, eTile, fTile, gTile, hTile, iTile, jTile, kTile, lTile, mTile, nTile, oTile, pTile, qTile, rTile, sTile, tTile, uTile, vTile, wTile, xTile, yTile, zTile, blankTile;
 	public HashMap<Character, TextureRegion> letterTileRegions;
 	
 	private BitmapTextureAtlas point0Tile, point1Tile, point2Tile, point3Tile, point4Tile, point5Tile, point6Tile, point7Tile, point8Tile, point9Tile, point10Tile;
 	public TextureRegion pointTileRegions[];
 	
 	private BitmapTextureAtlas tileOverlayTexture, confirmButtonTexture, cancelButtonTexture, playButtonTexture, shuffleButtonTexture, clearButtonTexture, swapButtonTexture, passButtonTexture;
 	private TiledTextureRegion tileOverlayRegion, confirmButtonRegion, cancelButtonRegion, playButtonRegion, shuffleButtonRegion, clearButtonRegion, swapButtonRegion, passButtonRegion;
 	
 	
 	private BitmapTextureAtlas horizontalCrosshairTexture, verticalCrosshairTexture;
 	private TextureRegion horizontalCrosshairRegion, verticalCrosshairRegion;
 	
 	public BitmapTextureAtlas activeOverlayTexture;
 	public TextureRegion activeOverlayRegion;
 	
     public TileSpace[][] tileSpaces;
     public TileRack tileRack;
 	private SurfaceScrollDetector mScrollDetector;
     
 	public Point startCoordinate;
 	
 	public TileBag bag;
 	
 	public Dictionary dictionary;
 	
 
     private BitmapTextureAtlas mFontTexture;
 	private Font mFont;
 
 	Scene scene;
 	
 	public boolean showingPicker;
 	public boolean gameOver;
 
 	public long lastTap;
 	
 	Button playButton, clearButton, shuffleButton, swapButton, passButton;
 	
 
 	Player players[];
 	int numPlayers;
 	int passCount;
 	
     Musubi mMusubi;
     //Feed mFeed;
     User musubiMe;
     
     String lastMove = "";
 
     private Sprite xCross, yCross;
     
     private WordPlayMultiplayer mMultiplayer;
     boolean isHost;
     ChangeableText titleText, player1Score, player2Score, player3Score, player4Score, tileCount;
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	 
 	// ===========================================================
 	// Getter &amp; Setter
 	// ===========================================================
 	 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public Engine onLoadEngine() {
 		//this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 1, 1, 5.0f);
 		hud = new HUD();
 		gameOver = false;
 		showingPicker = false;
 		startCoordinate = null;
 
 		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);  
 		//final int alturaTotal = CAMERA_HEIGHT*3;
 		this.mCamera.setBounds(0, CAMERA_WIDTH, 0, CAMERA_HEIGHT);
 		this.mCamera.setBoundsEnabled(true);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		ScreenOrientation deviceOrientation = ScreenOrientation.PORTRAIT;
 		int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
 		if (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
 		        screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
 		    deviceOrientation = ScreenOrientation.LANDSCAPE;
 		}
 		return new Engine(new EngineOptions(false, deviceOrientation, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
 		//return new Engine(new EngineOptions(false, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
 	}
 
 	@Override
 	public void onLoadResources() {
 		this.mTexture = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		
 		this.mEngine.getTextureManager().loadTexture(this.mTexture);
 		
 
 		this.startTile = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.baseTile = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.doubleLetterTile = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tripleLetterTile = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.doubleWordTile = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tripleWordTile = new BitmapTextureAtlas(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		
 		this.aTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.bTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.cTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.dTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.eTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.fTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.gTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.hTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.iTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.jTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.kTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.lTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.nTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.oTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.pTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.qTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.rTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.sTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.uTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.vTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.wTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.xTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.yTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.zTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.blankTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.point1Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point0Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point2Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point3Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point4Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point5Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point6Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point7Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point8Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point9Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point10Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.startTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.startTile, this, "gfx/start_tile.png", 0, 0);
 		this.baseTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.baseTile, this, "gfx/tile_base.png", 0, 0);
 		this.doubleLetterTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.doubleLetterTile, this, "gfx/double_letter_tile.png", 0, 0);
 		this.tripleLetterTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.tripleLetterTile, this, "gfx/triple_letter_tile.png", 0, 0);
 		this.doubleWordTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.doubleWordTile, this, "gfx/double_word_tile.png", 0, 0);
 		this.tripleWordTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.tripleWordTile, this, "gfx/triple_word_tile.png", 0, 0);
 
 		activeOverlayTexture = new BitmapTextureAtlas(64, 64, TextureOptions.DEFAULT);
 		activeOverlayRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(activeOverlayTexture, this, "gfx/active_tile.png", 0, 0);
 		
 		
 		horizontalCrosshairTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		horizontalCrosshairRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(horizontalCrosshairTexture, this, "gfx/horizontal_crosshair.png", 0, 0);
 		verticalCrosshairTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		verticalCrosshairRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(verticalCrosshairTexture, this, "gfx/vertical_crosshair.png", 0, 0);
 
 
 		tileOverlayTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		tileOverlayRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(tileOverlayTexture, this, "gfx/tile_overlay.png", 0, 0, 1, 1);
 
 		confirmButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		confirmButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(confirmButtonTexture, this, "gfx/confirm_button.png", 0, 0, 3, 1);
 
 		cancelButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		cancelButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(cancelButtonTexture, this, "gfx/cancel_button.png", 0, 0, 3, 1);
 
 		playButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		playButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(playButtonTexture, this, "gfx/play_button.png", 0, 0, 3, 1);
 		
 		passButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		passButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(passButtonTexture, this, "gfx/pass_button.png", 0, 0, 3, 1);
 		
 		swapButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		swapButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(swapButtonTexture, this, "gfx/swap_button.png", 0, 0, 3, 1);
 		
 		shuffleButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		shuffleButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(shuffleButtonTexture, this, "gfx/shuffle_button.png", 0, 0, 3, 1);
 		
 		clearButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		clearButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(clearButtonTexture, this, "gfx/clear_button.png", 0, 0, 3, 1);
 		
 		letterTileRegions = new HashMap<Character, TextureRegion>();
 		letterTileRegions.put('a', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.aTile, this, "gfx/a_tile.png", 0, 0));
 		letterTileRegions.put('b', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.bTile, this, "gfx/b_tile.png", 0, 0));
 		letterTileRegions.put('c', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.cTile, this, "gfx/c_tile.png", 0, 0));
 		letterTileRegions.put('d', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.dTile, this, "gfx/d_tile.png", 0, 0));
 		letterTileRegions.put('e', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.eTile, this, "gfx/e_tile.png", 0, 0));
 		letterTileRegions.put('f', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.fTile, this, "gfx/f_tile.png", 0, 0));
 		letterTileRegions.put('g', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.gTile, this, "gfx/g_tile.png", 0, 0));
 		letterTileRegions.put('h', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.hTile, this, "gfx/h_tile.png", 0, 0));
 		letterTileRegions.put('i', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.iTile, this, "gfx/i_tile.png", 0, 0));
 		letterTileRegions.put('j', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.jTile, this, "gfx/j_tile.png", 0, 0));
 		letterTileRegions.put('k', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.kTile, this, "gfx/k_tile.png", 0, 0));
 		letterTileRegions.put('l', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.lTile, this, "gfx/l_tile.png", 0, 0));
 		letterTileRegions.put('m', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mTile, this, "gfx/m_tile.png", 0, 0));
 		letterTileRegions.put('n', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.nTile, this, "gfx/n_tile.png", 0, 0));
 		letterTileRegions.put('o', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.oTile, this, "gfx/o_tile.png", 0, 0));
 		letterTileRegions.put('p', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.pTile, this, "gfx/p_tile.png", 0, 0));
 		letterTileRegions.put('q', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.qTile, this, "gfx/q_tile.png", 0, 0));
 		letterTileRegions.put('r', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.rTile, this, "gfx/r_tile.png", 0, 0));
 		letterTileRegions.put('s', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.sTile, this, "gfx/s_tile.png", 0, 0));
 		letterTileRegions.put('t', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.tTile, this, "gfx/t_tile.png", 0, 0));
 		letterTileRegions.put('u', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.uTile, this, "gfx/u_tile.png", 0, 0));
 		letterTileRegions.put('v', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.vTile, this, "gfx/v_tile.png", 0, 0));
 		letterTileRegions.put('w', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.wTile, this, "gfx/w_tile.png", 0, 0));
 		letterTileRegions.put('x', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.xTile, this, "gfx/x_tile.png", 0, 0));
 		letterTileRegions.put('y', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.yTile, this, "gfx/y_tile.png", 0, 0));
 		letterTileRegions.put('z', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.zTile, this, "gfx/z_tile.png", 0, 0));
 		letterTileRegions.put(' ', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.blankTile, this, "gfx/blank_tile.png", 0, 0));
 
 		pointTileRegions = new TextureRegion[11];
 		pointTileRegions[0] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point0Tile, this, "gfx/point_0.png", 0, 0);
 		pointTileRegions[1] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point1Tile, this, "gfx/point_1.png", 0, 0);
 		pointTileRegions[2] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point2Tile, this, "gfx/point_2.png", 0, 0);
 		pointTileRegions[3] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point3Tile, this, "gfx/point_3.png", 0, 0);
 		pointTileRegions[4] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point4Tile, this, "gfx/point_4.png", 0, 0);
 		pointTileRegions[5] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point5Tile, this, "gfx/point_5.png", 0, 0);
 		pointTileRegions[6] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point6Tile, this, "gfx/point_6.png", 0, 0);
 		pointTileRegions[7] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point7Tile, this, "gfx/point_7.png", 0, 0);
 		pointTileRegions[8] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point8Tile, this, "gfx/point_8.png", 0, 0);
 		pointTileRegions[9] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point9Tile, this, "gfx/point_9.png", 0, 0);
 		pointTileRegions[10] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point10Tile, this, "gfx/point_10.png", 0, 0);
 
 		this.mEngine.getTextureManager().loadTexture(activeOverlayTexture);
 
 		this.mEngine.getTextureManager().loadTexture(horizontalCrosshairTexture);
 		this.mEngine.getTextureManager().loadTexture(verticalCrosshairTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(confirmButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(cancelButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(playButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(passButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(shuffleButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(swapButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(clearButtonTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(this.startTile);
 		this.mEngine.getTextureManager().loadTexture(this.baseTile);
 		this.mEngine.getTextureManager().loadTexture(this.doubleLetterTile);
 		this.mEngine.getTextureManager().loadTexture(this.tripleLetterTile);
 		this.mEngine.getTextureManager().loadTexture(this.doubleWordTile);
 		this.mEngine.getTextureManager().loadTexture(this.tripleWordTile);
 
 		this.mEngine.getTextureManager().loadTexture(tileOverlayTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(aTile);
 		this.mEngine.getTextureManager().loadTexture(bTile);
 		this.mEngine.getTextureManager().loadTexture(cTile);
 		this.mEngine.getTextureManager().loadTexture(dTile);
 		this.mEngine.getTextureManager().loadTexture(eTile);
 		this.mEngine.getTextureManager().loadTexture(fTile);
 		this.mEngine.getTextureManager().loadTexture(gTile);
 		this.mEngine.getTextureManager().loadTexture(hTile);
 		this.mEngine.getTextureManager().loadTexture(iTile);
 		this.mEngine.getTextureManager().loadTexture(jTile);
 		this.mEngine.getTextureManager().loadTexture(kTile);
 		this.mEngine.getTextureManager().loadTexture(lTile);
 		this.mEngine.getTextureManager().loadTexture(mTile);
 		this.mEngine.getTextureManager().loadTexture(nTile);
 		this.mEngine.getTextureManager().loadTexture(oTile);
 		this.mEngine.getTextureManager().loadTexture(pTile);
 		this.mEngine.getTextureManager().loadTexture(qTile);
 		this.mEngine.getTextureManager().loadTexture(rTile);
 		this.mEngine.getTextureManager().loadTexture(sTile);
 		this.mEngine.getTextureManager().loadTexture(tTile);
 		this.mEngine.getTextureManager().loadTexture(uTile);
 		this.mEngine.getTextureManager().loadTexture(vTile);
 		this.mEngine.getTextureManager().loadTexture(wTile);
 		this.mEngine.getTextureManager().loadTexture(xTile);
 		this.mEngine.getTextureManager().loadTexture(yTile);
 		this.mEngine.getTextureManager().loadTexture(zTile);
 		this.mEngine.getTextureManager().loadTexture(blankTile);
 
 		this.mEngine.getTextureManager().loadTexture(point0Tile);
 		this.mEngine.getTextureManager().loadTexture(point1Tile);
 		this.mEngine.getTextureManager().loadTexture(point2Tile);
 		this.mEngine.getTextureManager().loadTexture(point3Tile);
 		this.mEngine.getTextureManager().loadTexture(point4Tile);
 		this.mEngine.getTextureManager().loadTexture(point5Tile);
 		this.mEngine.getTextureManager().loadTexture(point6Tile);
 		this.mEngine.getTextureManager().loadTexture(point7Tile);
 		this.mEngine.getTextureManager().loadTexture(point8Tile);
 		this.mEngine.getTextureManager().loadTexture(point9Tile);
 		this.mEngine.getTextureManager().loadTexture(point10Tile);
 		
 		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
         this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 16, true, Color.WHITE);
 
         this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
         this.mEngine.getFontManager().loadFont(this.mFont);
         
 
         tileSpaces = new TileSpace[15][15];
         
         tileRack = new TileRack(this, true);
         
         dictionary = new TWLDictionary(this);
         
 
     	lastTap = 0;
     	
 
         
         // All app code is in Board.
         
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		 
 		scene = new Scene();
 		scene.setBackground(new ColorBackground(0.1686f, 0.1686f, 0.1686f));
 		scene.setOnAreaTouchTraversalFrontToBack();
 		
 		
 		this.mScrollDetector = new SurfaceScrollDetector(this);
 		this.mScrollDetector.setEnabled(true);
 
 		bag = new TileBag();
 		
 		players = new Player[4];
 		players[0] = new Player("Player 1");
 		players[1] = new Player("Player 2");
 		players[2] = new Player("Player 3");
 		players[3] = new Player("Player 4");
 		
 		numPlayers = 0;
 
 		passCount = 0;
 		 
         for (int i = 0; i < 15; i++) {
         	for (int j = 0; j < 15; j++) {
         		if (BoardLayout.board[i][j].equals("DL")) {
         			tileSpaces[i][j] = new DoubleLetterTileSpace(this, 3+i*21, 23+j*21);
         		}
         		else if (BoardLayout.board[i][j].equals("TL")) {
         			tileSpaces[i][j] = new TripleLetterTileSpace(this, 3+i*21, 23+j*21);
         		}
         		else if (BoardLayout.board[i][j].equals("DW")) {
         			tileSpaces[i][j] = new DoubleWordTileSpace(this, 3+i*21, 23+j*21);
         		}
         		else if (BoardLayout.board[i][j].equals("TW")) {
         			tileSpaces[i][j] = new TripleWordTileSpace(this, 3+i*21, 23+j*21);
         		}
         		else if (BoardLayout.board[i][j].equals("ST")) {
         			tileSpaces[i][j] = new StartTileSpace(this, 3+i*21, 23+j*21);
         			startCoordinate = new Point(i, j);
         		}
         		else {
         			tileSpaces[i][j] = new BasicTileSpace(this, 3+i*21, 23+j*21);
         		}
         		tileSpaces[i][j].draw(scene);
         	}
         }
         /* Create the face and add it to the scene. */
         /*final Sprite face = new Sprite(centerX, centerY, this.mFaceTextureRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                     this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                     return true;
             }
         };*/
         
         /*tileSpaces[0][0].setLetter(WordPlayActivity.this, scene, 'a');
         tileSpaces[0][1].setLetter(WordPlayActivity.this, scene, 'a');
         tileSpaces[0][2].setLetter(WordPlayActivity.this, scene, 'a');
         tileSpaces[0][3].setLetter(WordPlayActivity.this, scene, 'a');
         tileSpaces[0][4].setLetter(WordPlayActivity.this, scene, 'a');*/
         //scene.attachChild(face);
         //scene.registerTouchArea(face);
         //scene.setTouchAreaBindingEnabled(true);
 
         titleText = new ChangeableText(20, 3, this.mFont, "Player 1 played 'quixotic' for 100 points.", "Player 1 played 'quixotic' for 100 points.".length());
         player1Score = new ChangeableText(200, 380, this.mFont, "Player 1: " + players[0].getScore(), "Player 1: XXX".length());
         player2Score = new ChangeableText(200, 400, this.mFont, "Player 2: " + players[1].getScore(), "Player 2: XXX".length());
         player3Score = new ChangeableText(200, 420, this.mFont, "Player 3: " + players[2].getScore(), "Player 3: XXX".length());
         player4Score = new ChangeableText(200, 440, this.mFont, "Player 4: " + players[3].getScore(), "Player 4: XXX".length());
         tileCount = new ChangeableText(200, 460, this.mFont, bag.tilesRemaining() + " Tiles left", (bag.tilesRemaining() + " Tiles left").length());
         
         hud.attachChild(titleText);
         hud.attachChild(player1Score);
         hud.attachChild(player2Score);
         hud.attachChild(player3Score);
         hud.attachChild(player4Score);
         hud.attachChild(tileCount);
         
 		playButton = new Button(5, 385, playButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				if (!mMultiplayer.isMyTurn() || gameOver) {
 					return true;
 				}
 
             	if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			TilePoint wordCoordinates[] = tileRack.placeTiles(scene, isFirstPlay());
         			if (wordCoordinates != null) {
         				Point[][] wordSet = calculateWordSet(wordCoordinates);
         				if (!verifyWordSet(wordSet)) {
         					tileRack.unsetLetters(scene);
 
                 			this.release();
         					return true;
         				}
 
         				int totalPoints = 0;
         				
         				if (tileRack.isBingo()) {
         					totalPoints += 50;
         				}
         				
         				tileRack.finalizeTiles(scene);
         				
         				for (int i = 0; i < wordSet.length; i++) {
         					if (wordSet[i] != null) {
         						totalPoints += calculatePointsForWord(wordSet[i]);
         					}
         				}
         				
         				for (int i = 0; i < wordCoordinates.length; i++) {
         					tileSpaces[wordCoordinates[i].x][wordCoordinates[i].y].setUsed();
         				}
         				while(tileRack.numTiles < 7 && bag.tilesRemaining() > 0) {
         					tileRack.addTile(scene, bag.getNextTile());
         				}
         				players[mMultiplayer.getLocalMemberIndex()].incrementScore(totalPoints);
     					player1Score.setText(players[0].getShortName() + ": " + players[0].getScore());
 
     					String word = "";
     					for (int j = 0; j < wordSet[0].length; j++) {
     						word += tileSpaces[wordSet[0][j].x][wordSet[0][j].y].getLetter();
     					}
     					passCount = 0;
     					lastMove = mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName() + " played '" + word.toLowerCase() + "' for " + totalPoints + "pts";
     					
     					if (tileRack.numTiles == 0) {
     						//lastMove = players[0].getShortName() + " won the game.";
     						String winners = "";
     						int topScore = 0;
     						for(int i = 0; i < numPlayers; i++) {
     							if(players[i].getScore() > topScore) {
     								topScore = players[i].getScore();
     							}
     						}
     						boolean tieGame = false;
     						for(int i = 0; i < numPlayers; i++) {
     							if(players[i].getScore() == topScore) {
     								if(winners == "") {
     									winners = players[i].getShortName();
     								}
     								else {
     									tieGame = true;
     									winners += ", " + players[i].getScore();
     								}
     							}
     						}
     						if(tieGame) {
     							lastMove = winners + " tied!";
     						}
     						else {
     							lastMove = winners + " won the game!";
     						}
     						gameOver = true;
     					}
     	        		mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
         			}
         			this.release();
         		}
         		return true;
             }
         };
         hud.attachChild(playButton);
         hud.registerTouchArea(playButton);
         
         
 
 		clearButton = new Button(65, 385, clearButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         		if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			this.release();
         			tileRack.clearTiles();
         		}
         		return true;
             }
         };
         hud.attachChild(clearButton);
         hud.registerTouchArea(clearButton);
         
 
 		shuffleButton = new Button(5, 430, shuffleButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         		if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			this.release();
         			tileRack.clearTiles();
         			tileRack.shuffleTiles(scene);
         		}
         		return true;
             }
         };
         hud.attachChild(shuffleButton);
         hud.registerTouchArea(shuffleButton);
         
 
         
         
 
 		swapButton = new Button(65, 430, swapButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
             	if (!mMultiplayer.isMyTurn() || gameOver) {
 					return true;
 				}
             	if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			//Log.w(TAG, "swapping");
         			showingPicker = true;
         			tileRack.clearTiles();
         			
 
         			tilePickerHud = new HUD();
         			
 
         			final boolean[] selection = new boolean[7];
         			
         			final Button cancelButton = new Button(5, 385, cancelButtonRegion){
         	            @Override
         	            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         	        		if (pSceneTouchEvent.isActionDown()) {
         	        			this.press();
         	        		}
         	        		if (pSceneTouchEvent.isActionUp()) {
         	        			this.release();
         	        			mCamera.setHUD(hud);
         	        		}
         	        		return true;
         	            }
         	        };
         	        tilePickerHud.attachChild(cancelButton);
         	        tilePickerHud.registerTouchArea(cancelButton);
         	        
         			final Button confirmSwapButton = new Button(65, 385, swapButtonRegion){
         	            @Override
         	            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         	        		if (pSceneTouchEvent.isActionDown()) {
         	        			this.press();
         	        		}
         	        		if (pSceneTouchEvent.isActionUp()) {
         	        			this.release();
         	        			
         	        			int numTiles = 0;
         	        			for (int i = 0; i < 7; i++) {
         	        				if (selection[i]) {
         	        					numTiles++;
         	        				}
         	        			}
         	        			
         	        			Character[] swapTiles = new Character[numTiles];
         	        			
         	        			int j = 0;
         	        			for (int i = 0; i < 7; i++) {
         	        				if (selection[i]) {
         	        					swapTiles[j] = tileRack.tiles[i].getLetter();
         	        					j++;
         	        				}
         	        			}
         	        			
         	        			if (numTiles > 0) {
         	        				Character[] newTiles = bag.swapTiles(swapTiles);
         	        				j = 0;
         	        				for (int i = 0; i < 7; i++) {
         	        					if (selection[i]) {
         	        						tileRack.replaceTile(scene, newTiles[j], i);
         	        						j++;
         	        					}
         	        				}
 
         	                		lastMove = mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName() + " swapped " + numTiles + " tiles";
         	    					mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
         	        			}
         	        			mCamera.setHUD(hud);
         	        		}
         	        		return true;
         	            }
         	        };
         	        tilePickerHud.attachChild(confirmSwapButton);
         	        tilePickerHud.registerTouchArea(confirmSwapButton);
         			
         			for (int i = 0; i < 7; i++) {
         				selection[i] = false;
         				if (tileRack.tiles[i] != null) {
         					final int pos = i;
             				int lastX = 3+45*i;
             				int lastY = 338;
             				Sprite letterTile = new Sprite(lastX, lastY, letterTileRegions.get(Character.valueOf(tileRack.tiles[i].getLetter())));
             				
             				Button letterSelection = new Button(lastX, lastY, tileOverlayRegion) {
             		            @Override
             		            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
             		            	if (!mMultiplayer.isMyTurn()) {
             							return true;
             						}
             		            	if(pSceneTouchEvent.isActionDown()) {
             		        			this.press();
             		        		}
             		        		if(pSceneTouchEvent.isActionUp()) {
             		        			this.release();
             		        			if (selection[pos]) {
             		        				this.setAlpha(.7f);
             		        			}
             		        			else {
             		        				this.setAlpha(0);
             		        			}
             		        			selection[pos] = !selection[pos];
             		        		}
             		        		return true;
             		            }
             				};
             				letterSelection.setAlpha(.7f);
             				tilePickerHud.attachChild(letterTile);
             				tilePickerHud.attachChild(letterSelection);
             				tilePickerHud.registerTouchArea(letterSelection);
         				}
         			}
         	        mCamera.setHUD(tilePickerHud);
         			this.release();
         		}
         		return true;
             }
         };
         hud.attachChild(swapButton);
         hud.registerTouchArea(swapButton);
         
 
 		passButton = new Button(125, 430, passButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
             	if (!mMultiplayer.isMyTurn() || gameOver) {
 					return true;
 				}
             	if(pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if(pSceneTouchEvent.isActionUp()) {
         			this.release();
         			passCount++;
             		lastMove = mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName() + " passed";
             		if (passCount >= numPlayers) {
 						//lastMove = players[0].getShortName() + " won the game.";
 						String winners = "";
 						int topScore = 0;
 						for(int i = 0; i < numPlayers; i++) {
 							if(players[i].getScore() > topScore) {
 								topScore = players[i].getScore();
 							}
 						}
 						boolean tieGame = false;
 						for(int i = 0; i < numPlayers; i++) {
 							if(players[i].getScore() == topScore) {
 								if(winners == "") {
 									winners = players[i].getShortName();
 								}
 								else {
 									tieGame = true;
 									winners += ", " + players[i].getShortName();
 								}
 							}
 						}
 						if(tieGame) {
 							lastMove = winners + " tied!";
 						}
 						else {
 							lastMove = winners + " won the game!";
 						}
 						gameOver = true;
 					}
 					mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
         		}
         		return true;
             }
         };
         hud.attachChild(passButton);
         hud.registerTouchArea(passButton);
         
 
         
         mCamera.setHUD(hud);
         scene.setOnSceneTouchListener(this);
         scene.setTouchAreaBindingEnabled(true);
         
         
 		return scene;
 	}
 
 	@Override
 	public void onLoadComplete() {
 		// TODO Auto-generated method stub
 		if (!Musubi.isMusubiInstalled(getApplicationContext())) {
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("WordPlay requires the Musubi Social Platform")
 			       .setCancelable(false)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
     			   			Intent updateIntent = Musubi.getMarketIntent();
     			   			startActivity(updateIntent); 
 			                WordPlayActivity.this.finish();
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			alert.show();
 			
 			
 		}
 		if (!Musubi.isMusubiIntent(getIntent())) {
             Toast.makeText(this, "Please launch with 2-players!", Toast.LENGTH_SHORT).show();
             finish();
             return;
         }
 		
		mMusubi = Musubi.getInstance(this);
		//mFeed = mMusubi.getFeed();
        //mFeed.registerStateObserver(mStateObserver);
        
         mMultiplayer = new WordPlayMultiplayer(mMusubi.getObj());
 
         if (mMultiplayer.getLocalMemberIndex() >= 0) {
         	players[mMultiplayer.getLocalMemberIndex()].setName(mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName());
         }
         else {
         	runOnUpdateThread(new Runnable() {
                 @Override
                 public void run() {
                 	hud.detachChild(playButton);
                 	hud.detachChild(clearButton);
                 	hud.detachChild(shuffleButton);
                 	hud.detachChild(swapButton);
                 	hud.detachChild(passButton);
 
                 	hud.unregisterTouchArea(playButton);
                 	hud.unregisterTouchArea(clearButton);
                 	hud.unregisterTouchArea(shuffleButton);
                 	hud.unregisterTouchArea(swapButton);
                 	hud.unregisterTouchArea(passButton);
                 }
         	});
         }
         for(int i = 0; i < mMultiplayer.getMembers().length; i++) {	
             	players[i].setName(mMultiplayer.getUser(i).getName());
         }
 
 		player1Score.setText(players[0].getShortName() + ": " + players[0].getScore());
 		player2Score.setText(players[1].getShortName() + ": " + players[1].getScore());
 		player3Score.setText(players[2].getShortName() + ": " + players[2].getScore());
 		player4Score.setText(players[3].getShortName() + ": " + players[3].getScore());
         
 		player1Score.setColor(255, 255, 255);
 		player2Score.setColor(255, 255, 255);
 		player3Score.setColor(255, 255, 255);
 		player4Score.setColor(255, 255, 255);
 		
 		if (mMultiplayer.getGlobalMemberCursor() == 0) {
 			player1Score.setColor(1f, .647f, .341f);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 1) {
 			player2Score.setColor(1f, .647f, .341f);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 2) {
 			player3Score.setColor(1f, .647f, .341f);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 3) {
 			player4Score.setColor(1f, .647f, .341f);
         }
 
 		if(numPlayers < 4) {
 			player4Score.setText("");
 		}
 		if(numPlayers < 3) {
 			player3Score.setText("");
 		}
 		
 		if (mMultiplayer.isMyTurn()) {
 			titleText.setText("Your turn");
 		}
 		else {
 			titleText.setText(players[mMultiplayer.getGlobalMemberCursor()].getShortName() + "'s turn");
 		}
 		
         if(mMultiplayer.getLatestState() == null) {
         	//Toast.makeText(this, "latest state is null", Toast.LENGTH_SHORT).show();
         	if(!mMultiplayer.isMyTurn()) {
         		tileRack = new TileRack(this, true);
         	}
         }
         else {
         	render(mMultiplayer.getLatestState(), true);
         }
         
 	}
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	public Point[][] calculateWordSet(TilePoint[] wordCoordinates) {
 		boolean horizontal = false;
 		
 		//length 1, arbitrarily pick horizontal as alignment
 		if (wordCoordinates.length == 1) {
 			horizontal = true;
 		}
 		//vertical alignment
 		else if (wordCoordinates[0].x == wordCoordinates[1].x) {
 			horizontal = false;
 		}
 		//horizontal alignment
 		else {
 			horizontal = true;
 		}
 		Point[][] wordSet = new Point[wordCoordinates.length+1][];
 		wordSet[0] = wordCoordinates;
 		
 		for (int i = 0; i < wordCoordinates.length; i++) {
 			//get orthogonally aligned word
 			if(wordCoordinates[i].played) {
 				Point[] altWord = getWordCoordinates(wordCoordinates[i], !horizontal);
 				//check that word is atleast 2 letters long
 				if (altWord.length > 1) {
 					wordSet[i+1] = altWord;
 				}
 			}
 		}
 		return wordSet;
 	}
 	
 	public Point[] getWordCoordinates(Point anchor, boolean horizontal) {
 		int maxPos, minPos;
 		Point[] wordCoordinates = null;
 		if (horizontal) {
 			minPos = anchor.x;
 			maxPos = anchor.x;
 			//get earliest letter coordinate
 			for (int i = anchor.x; i >= 0; i--) {
 				if (tileSpaces[i][anchor.y].getLetter() != '0') {
 					minPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//get latest letter coordinate
 			for (int i = anchor.x; i < 15; i++) {
 				if (tileSpaces[i][anchor.y].getLetter() != '0') {
 					maxPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//construct word coordinates
 			wordCoordinates = new Point[maxPos-minPos+1];
 			for (int i = minPos; i <= maxPos; i++) {
 				wordCoordinates[i-minPos] = new Point(i, anchor.y);
 			}
 		}
 		else {
 			minPos = anchor.y;
 			maxPos = anchor.y;
 			//get earliest letter coordinate
 			for (int i = anchor.y; i >= 0; i--) {
 				if (tileSpaces[anchor.x][i].getLetter() != '0') {
 					minPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//get latest letter coordinate
 			for (int i = anchor.y; i < 15; i++) {
 				if (tileSpaces[anchor.x][i].getLetter() != '0') {
 					maxPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//construct word coordinates
 			wordCoordinates = new Point[maxPos-minPos+1];
 			for (int i = minPos; i <= maxPos; i++) {
 				wordCoordinates[i-minPos] = new Point(anchor.x, i);
 			}
 		}
 		return wordCoordinates;
 	}
 	
 	public boolean verifyWordSet(Point[][] wordSet) {
 		for (int i = 0; i < wordSet.length; i++) {
 			if (wordSet[i] == null) {
 				continue;
 			}
 			String word = "";
 			for (int j = 0; j < wordSet[i].length; j++) {
 				word += tileSpaces[wordSet[i][j].x][wordSet[i][j].y].getLetter();
 			}
 			if (!dictionary.isWord(word)) {
 				Toast.makeText(WordPlayActivity.this, word + " is not a word.", Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public int calculatePointsForWord(Point[] wordCoordinates) {
 		int wordModifier = 1;
 		int wordPoints = 0;
 		String word = "";
 		
 		for(int i = 0; i < wordCoordinates.length; i++) {
 			TileSpace tile = tileSpaces[wordCoordinates[i].x][wordCoordinates[i].y];
 			wordPoints += tile.getPoints();
 			wordModifier *= tile.getMultiplier();
 			word += tile.getLetter();
 		}
 		
 		
 		int totalPoints = wordPoints * wordModifier;
 		//Log.w(TAG, word + " for " + totalPoints + " points");
 		return totalPoints;
 	}
 	
 	
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		// TODO Auto-generated method stub
 		if(showingPicker) {
 			return true;
 		}
 		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
 		if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN)
         {
 	    	Calendar rightNow = Calendar.getInstance();
 			long currentTap = rightNow.getTimeInMillis();
 			if (currentTap - lastTap < 500) {
 				lastTap = 0;
 				if (mCamera.getZoomFactor() == 1) {
         			mCamera.setZoomFactor(2);
         			mCamera.setCenter(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 				}
 				else {
         			mCamera.setZoomFactor(1);
 				}
 			}
 			else {
 				lastTap = currentTap;
 			}
         }
 		return true;
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
 			float pDistanceX, float pDistanceY) {
 		// TODO Auto-generated method stub
 		if (showingPicker) {
 			return;
 		}
 		Log.d(TAG, "Scroll {x:"+pDistanceX+", y: "+pDistanceY+"}");
 		this.mCamera.offsetCenter(-pDistanceX, -pDistanceY);
 	}
 	 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// =========================================================== 
 
     class WordPlayMultiplayer extends TurnBasedMultiplayer {
         public WordPlayMultiplayer(DbObj obj) {
             super(obj);
         }
 
         @Override
         protected FeedRenderable getFeedView(JSONObject arg0) {
             return FeedRenderable.fromHtml(getSnapshotHtml());
         }
 
         @Override
         protected JSONObject getInitialState() {
             for (int i = 0; i < getMembers().length; i++) {
                 players[i].setName(getUser(i).getName());
             }
             lastMove = getLocalUser().getName() + " started a game";
             return getApplicationState();
         }
 
         @Override
         protected void onStateUpdate(JSONObject state) {
             lastMove = state.optString("lastmove");
             if (state.optBoolean("initializing")) {
                 initializeState(state);
             }
             else {
                 render(state, false);
             }
         }
 
         private JSONObject getApplicationState() {
             JSONObject state = getLatestState() == null ? new JSONObject() : getLatestState();
             numPlayers = getMembers().length;
             JSONArray board = new JSONArray();
             try {
                 state.put("passcount", passCount);
                 state.put("gameover", gameOver);
                 if (mMultiplayer == null) {
                     //Log.w(TAG, "sending initial state");
                     state.put("initializing", true);
                 }
                 else {
                     state.put("initializing", false);
                 }
                 for (int i = 0; i < 15; i++) {
                     JSONArray row = new JSONArray();
                     for (int j = 0; j < 15; j++) {
                         row.put(tileSpaces[i][j].getLetter() + "");
                     }
                     board.put(row);
                 }
                 /*for (Button b : mmSquares) {
                     s.put(b.getText());
                 }*/
                 state.put("lastmove", lastMove);
                 state.put("board", board);
 
                 JSONArray racks = new JSONArray();
 
                 TileRack opponentRacks[] = new TileRack[numPlayers];// = new TileRack(this, false);
                 for(int i = 0; i < numPlayers-1; i++) {
                     opponentRacks[i] = new TileRack(WordPlayActivity.this, false);
                 }
                 
                 if(getLatestState() == null) {      
                     while(tileRack.numTiles < 7 && bag.tilesRemaining() > 0) {
                         tileRack.addTile(scene, bag.getNextTile());
                     }
                     for(int i = 0; i < numPlayers-1; i++) {
                         while(opponentRacks[i].numTiles < 7 && bag.tilesRemaining() > 0) {
                             //Log.w(TAG, "creating new tiles for opponent");
                             opponentRacks[i].addTile(scene, bag.getNextTile());
                         }
                     }
                 }
                 else {
                     JSONArray oldRacks = mMultiplayer.getLatestState().getJSONArray("racks");
                     /*Log.w(TAG, "getting tiles for opponent from state");
                     Log.w(TAG, oldRacks.toString());
                     Log.w(TAG, oldRacks.getJSONArray((mMultiplayer.getLocalMemberIndex()+1)%2).toString());
                     Log.w(TAG, (mMultiplayer.getLocalMemberIndex()+1)%2 + "");*/
                     //opponentRack.fromJson(scene, oldRacks.getJSONArray((mMultiplayer.getLocalMemberIndex()+1)%2));
                     
                     int j = 0;
                     for(int i = 0; i < numPlayers; i++) {
                         if (i != getLocalMemberIndex()) {
                             opponentRacks[j].fromJson(scene, oldRacks.getJSONArray(i));
                             j++;
                         }
                     }
                 }
 
                 tileCount.setText(bag.tilesRemaining() + " Tiles left");
                 state.put("bag", bag.toJson());
                 
                 int j = 0;
                 for(int i = 0; i < numPlayers; i++) {
                     if (i == getLocalMemberIndex()) {
                         racks.put(tileRack.toJson());
                     }
                     else {
                         racks.put(opponentRacks[j].toJson());
                     }
                 }
                 state.put("racks", racks);
                 Log.w(TAG, racks.toString());
                 
                 JSONArray scores = new JSONArray();
                 scores.put(players[0].getScore());
                 scores.put(players[1].getScore());
                 scores.put(players[2].getScore());
                 scores.put(players[3].getScore());
                 state.put("scores", scores);
                 
                 JSONArray jsonPlayers = new JSONArray();
                 
                 jsonPlayers.put(players[0].getName());
                 jsonPlayers.put(players[1].getName());
                 jsonPlayers.put(players[2].getName());
                 jsonPlayers.put(players[3].getName());
                 state.put("players", jsonPlayers);
                 
             } catch (JSONException e) {
                 //Log.wtf(TAG, "Failed to get board state", e);
             }
             return state;
         }
 
         public String getSnapshotHtml() {
             StringBuilder html = new StringBuilder("<html><head>");
             html.append("<body>");
             html.append("<span style=\"font-weight:bold;\">WordPlay Scoreboard</span>");
             html.append("<div style=\"border: 3px solid black; border-radius: 10px; padding: 5px; background:#4D5157;\">");
             html.append("<table>");
             for (int i = 0; i < numPlayers; i++) {
                 if ((getGlobalMemberCursor())%numPlayers == i) {
                     html.append("<tr><td><span style=\"font-weight:bold; color: #FF752B;\">").append(players[i].getShortName()).append("</span></td><td><span style=\"color: #ffffff;\">").append(players[i].getScore()).append(" pts</span></td></tr>");
                 }
                 else {
                     html.append("<tr><td><span style=\"font-weight:bold; color: #ffffff;\">").append(players[i].getShortName()).append("</span></td><td><span style=\"color: #ffffff;\">").append(players[i].getScore()).append(" pts</span></td></tr>");
                 }
             }
             html.append("</table>");
             html.append("</div>").append(lastMove).append("<div style=\"text-align: right;\">");
             html.append(bag.tilesRemaining()).append(" tiles remaining");
             html.append("</div></body>");
 
             html.append("</html>");
             return html.toString();
         }
     }
     
     private void initializeState(JSONObject state) {

     	//Log.w(TAG, "initializing state");
     	
         JSONArray board = state.optJSONArray("board");
         
         for (int i = 0; i < 15; i++) {
         	JSONArray row = board.optJSONArray(i);
         	for (int j = 0; j < 15; j++) {
         		char c = row.optString(j).charAt(0);
         		tileSpaces[i][j].setLetter(c);
         		if (c != '0') {
         			tileSpaces[i][j].setUsed();
             		tileSpaces[i][j].finalizeLetter(WordPlayActivity.this, scene);
         		}
         	}
         }
         bag.fromJson(state.optJSONArray("bag"));
         
     	tileRack.fromJson(scene, state.optJSONArray("racks").optJSONArray(mMultiplayer.getLocalMemberIndex()));
         
     }
     
     
     public void showTentativePoints() {
     	TilePoint wordCoordinates[] = tileRack.placeTiles(scene, isFirstPlay());
 		if (wordCoordinates != null) {
 			Point[][] wordSet = calculateWordSet(wordCoordinates);
 			
 			int totalPoints = 0;
 			
 			if (tileRack.isBingo()) {
 				totalPoints += 50;
 			}
 			
 			for (int i = 0; i < wordSet.length; i++) {
 				if (wordSet[i] != null) {
 					totalPoints += calculatePointsForWord(wordSet[i]);
 				}
 			}
 			titleText.setText("This play is worth " + totalPoints + "pts");
 			tileRack.unsetLetters(scene);
 		}
 		else {
 			Log.w(TAG, "not a valid placement");
 			titleText.setText(lastMove);
 		}
     }
     
     public void drawCrosshair(final int xPos, final int yPos) {
 
 		final int x = 3+xPos*21;
 		final int y = 23+yPos*21;
 		runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	if(xCross != null && yCross != null) {
 	        		if (scene.getChildIndex(xCross) >= 0) {
 	                	scene.detachChild(xCross);
 	                }
 	                if (scene.getChildIndex(yCross) >= 0) {
 	                	scene.detachChild(yCross);
 	                }
             	}
                 if (xPos >= 0 && xPos < 15 && yPos >= 0 && yPos < 15) {
                 	xCross = new Sprite(3, y, horizontalCrosshairRegion);
                 	yCross = new Sprite(x, 23, verticalCrosshairRegion);
                 	//xCross.setAlpha(.01f);
                 	//yCross.setAlpha(.01f);
                 	scene.attachChild(xCross);
                 	scene.attachChild(yCross);
                 }
             }
     	});
     }
     
     public void removeCrosshair() {
 
 		runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	if(xCross != null && yCross != null) {
 	        		if (scene.getChildIndex(xCross) >= 0) {
 	                	scene.detachChild(xCross);
 	                }
 	                if (scene.getChildIndex(yCross) >= 0) {
 	                	scene.detachChild(yCross);
 	                }
             	}
             }
     	});
     }
     
     private void render(JSONObject message, boolean firstLoad) {

     	//Log.w(TAG, "rendering normal state");
         gameOver = message.optBoolean("gameover");
        
         passCount = message.optInt("passcount");
     	
         JSONArray board = message.optJSONArray("board");
         for (int i = 0; i < 15; i++) {
         	JSONArray row = board.optJSONArray(i);
         	for (int j = 0; j < 15; j++) {
         		char c = row.optString(j).charAt(0);
         		tileSpaces[i][j].setLetter(c);
         		if (c != '0') {
         			tileSpaces[i][j].setUsed();
             		tileSpaces[i][j].finalizeLetter(WordPlayActivity.this, scene);
         		}
         	}
         }
         bag.fromJson(message.optJSONArray("bag"));
         for(int i = 0; i < numPlayers; i++) {
            players[i].setScore(message.optJSONArray("scores").optInt(i));	
         }
 
     	/*if (mMultiplayer.isMyTurn()) {
 			titleText.setText("Your turn");
 		}
 		else {
 			titleText.setText(players[mMultiplayer.getGlobalMemberCursor()].getShortName() + "'s turn");
 		}*/
         lastMove = message.optString("lastmove");
         titleText.setText(lastMove);
 		player1Score.setText(players[0].getShortName() + ": " + players[0].getScore());
 		player2Score.setText(players[1].getShortName() + ": " + players[1].getScore());
 		player3Score.setText(players[2].getShortName() + ": " + players[2].getScore());
 		player4Score.setText(players[3].getShortName() + ": " + players[3].getScore());
 		
 
 		player1Score.setColor(255, 255, 255);
 		player2Score.setColor(255, 255, 255);
 		player3Score.setColor(255, 255, 255);
 		player4Score.setColor(255, 255, 255);
 		
 		if (mMultiplayer.getGlobalMemberCursor() == 0) {
 			player1Score.setColor(1f, .647f, .341f);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 1) {
 			player2Score.setColor(1f, .647f, .341f);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 2) {
 			player3Score.setColor(1f, .647f, .341f);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 3) {
 			player4Score.setColor(1f, .647f, .341f);
         }
 
 		if(numPlayers < 4) {
 			player4Score.setText("");
 		}
 		if(numPlayers < 3) {
 			player3Score.setText("");
 		}
 
         tileCount.setText(bag.tilesRemaining() + " Tiles left");
         
         if(gameOver) {
         	int topScore = 0;
         	for(int i = 0; i < numPlayers; i++) {
         		if(players[i].getScore() > topScore) {
         			topScore = players[i].getScore();
         		}
         	}
         	int numWinners = 0;
         	for(int i = 0; i < numPlayers; i++) {
         		if(players[i].getScore() == topScore) {
         			numWinners++;
         		}
         	}
         	if (mMultiplayer.getLocalMemberIndex() >= 0) {
 	        	if(players[mMultiplayer.getLocalMemberIndex()].getScore() == topScore) {
 	        		if (numWinners == 1) {
 	        			titleText.setText("You won!");
 	            		Toast.makeText(WordPlayActivity.this, "You won!", Toast.LENGTH_SHORT).show();
 	        			
 	        		}
 	        		else {
 	        			titleText.setText("You tied!");
 	            		Toast.makeText(WordPlayActivity.this, "You tied!", Toast.LENGTH_SHORT).show();
 	        		}
 	        	}
 	        	else {
 	        		titleText.setText("You lost!");
 	        		Toast.makeText(WordPlayActivity.this, "You lost!", Toast.LENGTH_SHORT).show();
 	        	}
         	}
         }
 		
         
         if(firstLoad) {
         	if(mMultiplayer.getLocalMemberIndex() >= 0) {
         		//Log.w(TAG, "racks " + mMultiplayer.getLocalMemberIndex() + ": " + message.optJSONArray("racks").optJSONArray(mMultiplayer.getLocalMemberIndex()));
         		tileRack.fromJson(scene, message.optJSONArray("racks").optJSONArray(mMultiplayer.getLocalMemberIndex()));
         	}
     		//firstPlay = false;
         }
         
 
     	if (mMultiplayer.isMyTurn() && !gameOver) {
     		Toast.makeText(WordPlayActivity.this, "Your turn!", Toast.LENGTH_SHORT).show();
 		}
     }
     
     private boolean isFirstPlay() {
     	for(int i = 0; i < 15; i++) {
     		for(int j = 0; j < 15; j++) {
     			if (tileSpaces[i][j].getLetter() != '0') {
     				return false;
     			}
     		}
     	}
     	return true;
     }
     
 }
