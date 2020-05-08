 package se.chalmers.segway.resources;
 
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.debug.Debug;
 
 import se.chalmers.segway.main.MainActivity;
 import android.graphics.Color;
 
 public class ResourcesManager {
 	// ---------------------------------------------
 	// VARIABLES
 	// ---------------------------------------------
 
 	private static final ResourcesManager INSTANCE = new ResourcesManager();
 	private BitmapTextureAtlas splashTextureAtlas;
 
 	public Engine engine;
 	public MainActivity activity;
 	public BoundCamera camera;
 	public VertexBufferObjectManager vbom;
 	public Font loadingFont;
 	public Font fancyFont;
 	public Font tipFont;
 	public Music music;
 	public Music music2;
 
 	// ---------------------------------------------
 	// TEXTURES & TEXTURE REGIONS
 	// ---------------------------------------------
 	// Game Texture
 	public BuildableBitmapTextureAtlas gameTextureAtlas;
 
 	// Level Complete Window
 	public ITextureRegion complete_window_region;
 
 	// Death Window
 	public ITextureRegion death_window_region;
 
 	// Game Texture Regions
 	public ITiledTextureRegion player_region;
 	public ITiledTextureRegion player_backwards_region;
 	public ITiledTextureRegion cookies_region;
 	public ITextureRegion platform1_region;
 	public ITextureRegion platform2_region;
 	public ITextureRegion platform3_region;
 	public ITextureRegion curvyPlatform1_region;
 	public ITextureRegion coin_region;
 	public ITextureRegion golden_cookie;
 
 	// Selection Texture Regions
 	public BuildableBitmapTextureAtlas selectionTextureAtlas;
 	public ITextureRegion level_button;
 	public ITextureRegion selection_background_region;
 
 	// Other
 	private BuildableBitmapTextureAtlas menuTextureAtlas;
 	public ITextureRegion menu_background_region;
 	public ITextureRegion play_region;
 	public ITiledTextureRegion playerRegion;
 	// public ITextureRegion options_region;
 	public ITextureRegion soundon_region;
 	public ITextureRegion soundoff_region;
 	public ITextureRegion splash_region;
	private ITexture tipFontTexture;
 
 	// ---------------------------------------------
 	// CLASS LOGIC
 	// ---------------------------------------------
 
 	public void unloadGameTextures() {
 		gameTextureAtlas.unload();
 	}
 
 	public void unloadMenuTextures() {
 		menuTextureAtlas.unload();
 	}
 
 	public void loadMenuTextures() {
 		menuTextureAtlas.load();
 	}
 
 	public void unloadSelectionTextures() {
 		selectionTextureAtlas.unload();
 	}
 
 	public void loadMenuResources() {
 		loadMenuGraphics();
 		loadMenuAudio();
 		loadMenuFonts();
 	}
 
 	public void loadGameResources() {
 		loadGameGraphics();
 		loadGameFonts();
 		loadGameAudio();
 	}
 
 	public void loadSelectionResources() {
 		loadSelectionGraphics();
 	}
 
 	public void loadSelectionGraphics() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
 		selectionTextureAtlas = new BuildableBitmapTextureAtlas(
 				activity.getTextureManager(), 1024, 1024,
 				TextureOptions.BILINEAR);
 		selection_background_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(selectionTextureAtlas, activity,
 						"levelselect_background.png");
 		level_button = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				selectionTextureAtlas, activity, "levelselect_button.png");
 		try {
 			this.selectionTextureAtlas
 					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 							0, 1, 0));
 			this.selectionTextureAtlas.load();
 		} catch (final TextureAtlasBuilderException e) {
 			Debug.e(e);
 		}
 	}
 
 	private void loadMenuGraphics() {
 
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
 		menuTextureAtlas = new BuildableBitmapTextureAtlas(
 				activity.getTextureManager(), 1024, 1024,
 				TextureOptions.BILINEAR);
 		menu_background_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(menuTextureAtlas, activity,
 						"menu_background.png");
 		play_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				menuTextureAtlas, activity, "play.png");
 		soundon_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(menuTextureAtlas, activity, "soundon.png");
 		soundoff_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(menuTextureAtlas, activity, "soundoff.png");
 
 		try {
 			this.menuTextureAtlas
 					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 							0, 1, 0));
 			this.menuTextureAtlas.load();
 		} catch (final TextureAtlasBuilderException e) {
 			Debug.e(e);
 		}
 
 	}
 
 	private void loadMenuFonts() {
 		FontFactory.setAssetBasePath("font/");
 		final ITexture loadFontTexture = new BitmapTextureAtlas(
 				activity.getTextureManager(), 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		final ITexture fancyFontTexture = new BitmapTextureAtlas(
 				activity.getTextureManager(), 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		final ITexture tipFontTexture = new BitmapTextureAtlas(
 				activity.getTextureManager(), 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		// loadingFont
 		loadingFont = FontFactory.createStrokeFromAsset(
 				activity.getFontManager(), loadFontTexture,
 				activity.getAssets(), "start2p.ttf", 32, true, Color.WHITE, 2,
 				Color.BLACK);
 		loadingFont.load();
 
 		// fancyFont
 		fancyFont = FontFactory.createStrokeFromAsset(
 				activity.getFontManager(), fancyFontTexture,
 				activity.getAssets(), "start2p.ttf", 40, true, Color.YELLOW, 2,
 				Color.BLACK);
 		fancyFont.load();
 
 		// tipFont
 		tipFont = FontFactory.createStrokeFromAsset(activity.getFontManager(),
 				tipFontTexture, activity.getAssets(), "start2p.ttf", 30, true,
 				Color.GREEN, 2, Color.BLACK);
 		tipFont.load();
 	}
 
 	private void loadMenuAudio() {
 		try {
 			music = MusicFactory.createMusicFromAsset(engine.getMusicManager(),
 					activity, "sfx/bigblue.ogg");
 			music2 = MusicFactory.createMusicFromAsset(
 					engine.getMusicManager(), activity, "sfx/shepard_tone.ogg");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void loadGameGraphics() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
 		gameTextureAtlas = new BuildableBitmapTextureAtlas(
 				activity.getTextureManager(), 2048, 2048,
 				TextureOptions.BILINEAR);
 
 		platform1_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(gameTextureAtlas, activity, "platform1.png");
 		platform2_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(gameTextureAtlas, activity, "platform1.png");
 		platform3_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(gameTextureAtlas, activity, "platform1.png");
 		curvyPlatform1_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(gameTextureAtlas, activity,
 						"curvyPlatform1.png");
 		coin_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				gameTextureAtlas, activity, "cookie.png");
 		player_region = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(gameTextureAtlas, activity, "segway.png",
 						3, 1);
 		player_backwards_region = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(gameTextureAtlas, activity,
 						"segwayBackwards.png", 3, 1);
 		complete_window_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(gameTextureAtlas, activity, "complete.png");
 		death_window_region = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(gameTextureAtlas, activity, "youdied2.png");
 		golden_cookie = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				gameTextureAtlas, activity, "goldcookie2.png");
 		cookies_region = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(gameTextureAtlas, activity,
 						"cookie_sheet.png", 8, 1);
 		try {
 			this.gameTextureAtlas
 					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 							0, 1, 0));
 			this.gameTextureAtlas.load();
 		} catch (final TextureAtlasBuilderException e) {
 			Debug.e(e);
 		}
 	}
 
 	private void loadGameFonts() {
 
 	}
 
 	private void loadGameAudio() {
 
 	}
 
 	public void loadSplashScreen() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		splashTextureAtlas = new BitmapTextureAtlas(
 				activity.getTextureManager(), 1024, 1024,
 				TextureOptions.BILINEAR);
 		splash_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				splashTextureAtlas, activity, "splash.png", 0, 0);
 		splashTextureAtlas.load();
 	}
 
 	public void unloadSplashScreen() {
 		splashTextureAtlas.unload();
 		splash_region = null;
 	}
 
 	/**
 	 * @param engine
 	 * @param activity
 	 * @param camera
 	 * @param vbom
 	 * <br>
 	 * <br>
 	 *            We use this method at beginning of game loading, to prepare
 	 *            Resources Manager properly, setting all needed parameters, so
 	 *            we can latter access them from different classes (eg. scenes)
 	 */
 	public static void prepareManager(Engine engine, MainActivity activity,
 			BoundCamera camera, VertexBufferObjectManager vbom) {
 		getInstance().engine = engine;
 		getInstance().activity = activity;
 		getInstance().camera = camera;
 		getInstance().vbom = vbom;
 
 	}
 
 	// ---------------------------------------------
 	// GETTERS AND SETTERS
 	// ---------------------------------------------
 
 	public static ResourcesManager getInstance() {
 		return INSTANCE;
 	}
 }
