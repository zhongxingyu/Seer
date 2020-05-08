 package com.dogonyalki;
 
 import java.util.Random;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.WakeLockOptions;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import android.content.Context;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 
 import com.dogonyalki.elements.Player;
 import com.dogonyalki.managers.ScreenManager;
 import com.dogonyalki.network.Network;
 
 /**
  * @author Tooflya.com
  * @since
  */
 public class GameActivity extends BaseGameActivity {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	/** Instance of engine */
 	public static Engine instance;
 
 	/** Context of main activity */
 	public static Context context;
 
 	/** Instance of screen manager */
 	public static ScreenManager screens;
 
 	/** Instance of this activity */
 	public static GameActivity activity;
 
 	/** Camera of the game */
 	public static Camera camera;
 
 	/** Network worker */
 	public static Network connection;
 
 	/** Texture Atlases */
 	public static BitmapTextureAtlas mResourcesAtlas = new BitmapTextureAtlas(1024, 1024,
 			TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 	/** Global random */
 	public static Random random = new Random();
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Virtual methods
 	// ===========================================================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.IGameInterface#onLoadEngine()
 	 */
 	@Override
 	public Engine onLoadEngine() {
 		/** Let's remember Context of this activity */
 		context = getApplicationContext();
 
 		/** Set the position and resolution of camera */
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 
 		Options.cameraWidth = 800;
 		Options.cameraHeight = 480;
 
 		/** Init game camera */
 		camera = new Camera(-Options.cameraWidth / 2, -Options.cameraHeight / 2, Options.cameraWidth,
 				Options.cameraHeight);
 
 		/** Initialize the configuration of engine */
 		final EngineOptions options = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(
 				dm.widthPixels, dm.heightPixels), camera).setWakeLockOptions(WakeLockOptions.SCREEN_BRIGHT)
 				.setWakeLockOptions(WakeLockOptions.SCREEN_ON).setNeedsMusic(true).setNeedsSound(true);
 
 		/**
 		 * Disable extension vertex buffer objects. This extension usually has a
 		 * problems with HTC phones
 		 */
 		// options.getRenderOptions().disableExtensionVertexBufferObjects();
 
 		/** Auto setRunOnUpdateThread for touch events */
 		options.getTouchOptions().setRunOnUpdateThread(true);
 
 		/** Try to init our engine */
		instance = new Engine(options);
 
 		/** Save pointer instance */
 		activity = this;
 
 		// connection = new Network();
 		//
 		// new Thread(new Runnable() {
 		// @Override
 		// public void run() {
 		// connection.listen();
 		// }
 		// }).start();
 
 		return instance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.IGameInterface#onLoadComplete()
 	 */
 	@Override
 	public void onLoadComplete() {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.IGameInterface#onLoadResources()
 	 */
 	@Override
 	public void onLoadResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		this.mEngine.getTextureManager().loadTextures(mResourcesAtlas);
 	}
 
 	public static Sprite[] sprites;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.IGameInterface#onLoadScene()
 	 */
 	@Override
 	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

 		/** Initialization of screen manager */
 		screens = new ScreenManager();
 
 		Scene scene = screens.get(Screen.MAIN);
 		TextureRegion textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mResourcesAtlas,
 				getApplicationContext(), "box.jpg", 0, 400);
 		sprites = new Sprite[10];
 		for (int i = sprites.length - 1; i >= 0; --i) {
 			sprites[i] = new Sprite(random.nextInt(Options.cameraWidth - textureRegion.getWidth())
 					- Options.cameraWidth / 2, random.nextInt(Options.cameraHeight - textureRegion.getHeight())
 					- Options.cameraHeight / 2, textureRegion);
 			scene.attachChild(sprites[i]);
 		}
 
 		/** Try to play :) */
 		new Player(BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mResourcesAtlas,
 				getApplicationContext(), "p1.png", 0, 0, 4, 4)).setCenterPosition(0, 0);
 
 		return screens.get(Screen.MAIN);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.activity.BaseGameActivity#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		/**
 		 * Here we need to correctly shutdown our application and unload all
 		 * resources
 		 */
 
 		getTextureManager().unloadTextures(mResourcesAtlas);
 
 		/** Closing socket connection */
 		// connection.closeConnection();
 
 		/*
 		 * Notify the system to finalize and collect all objects of the
 		 * application on exit so that the process running the application can
 		 * be killed by the system without causing issues. NOTE: If this is set
 		 * to true then the process will not be killed until all of its threads
 		 * have closed.
 		 */
 		System.runFinalizersOnExit(true);
 
 		/*
 		 * Force the system to close the application down completely instead of
 		 * retaining it in the background. The process that runs the application
 		 * will be killed. The application will be completely created as a new
 		 * application in a new process if the user starts the application
 		 * again.
 		 */
 		System.exit(0);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.activity.BaseGameActivity#onResumeGame()
 	 */
 	@Override
 	public void onResumeGame() {
 		super.onResumeGame();
 
 		/** Release all music, update handlers and other active things */
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.anddev.andengine.ui.activity.BaseGameActivity#onPauseGame()
 	 */
 	@Override
 	public void onPauseGame() {
 		super.onPauseGame();
 
 		/** Stop all music, update handlers and other active things */
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
 		/**
 		 * if (Screen.screen < 0) { return false; }
 		 */
 
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return screens.get(Screen.screen).onBackPressed();
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 }
