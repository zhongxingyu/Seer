 package com.matimdev.manager;
 
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
 
 import android.graphics.Color;
 
 import com.matimdev.GameActivity;
 
 public class ResourcesManager
 {
 	//---------------------------------------------
 	// VARIABLES
 	//---------------------------------------------
 	
 	private static final ResourcesManager INSTANCE = new ResourcesManager();
 	
 	public Engine engine;
 	public GameActivity activity;
 	public BoundCamera camera;
 	public VertexBufferObjectManager vbom;
 	
 	public Font font;
 	public Font timeFont;
 	
 	//---------------------------------------------
 	// TEXTURES & TEXTURE REGIONS
 	//---------------------------------------------
 	
 	public ITextureRegion splash_region;
 	public ITextureRegion menu_background_region;
 	public ITextureRegion play_region;
 	public ITextureRegion options_region;
 	
 	// Game Texture
 	public BuildableBitmapTextureAtlas gameTextureAtlas;
 	
 	// Game Texture Regions
 	public ITextureRegion platform1_region;
 	public ITextureRegion platform2_region;
 	public ITextureRegion platform3_region;
 	public ITextureRegion ground_region;
 	public ITextureRegion coin_region;
 	public ITiledTextureRegion player_region;
 	public ITiledTextureRegion big_player_region;
 	public ITiledTextureRegion kinoko_region;
 	public ITiledTextureRegion enemy1_region;
 	public ITiledTextureRegion enemy2_region;
 	public ITextureRegion dokan_region;
 	public ITextureRegion iphone_region;
 	public ITextureRegion degitalClock_region;
 	
 	private BitmapTextureAtlas splashTextureAtlas;
 	private BuildableBitmapTextureAtlas menuTextureAtlas;
 	
 	// Level Complete Window
 	public ITextureRegion complete_window_region;
 	public ITiledTextureRegion complete_stars_region;
 	
 	//---------------------------------------------
 	// CLASS LOGIC
 	//---------------------------------------------
 
 	public void loadMenuResources()
 	{
 		loadMenuGraphics();
 		loadMenuAudio();
 		loadMenuFonts();
 	}
 	
 	public void loadGameResources()
 	{
 		loadGameGraphics();
 		loadGameFonts();
 		loadGameAudio();
 	}
 	
 	private void loadMenuGraphics()
 	{
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
         menuTextureAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 1000, 800, TextureOptions.BILINEAR);
         menu_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "menu_background.png");
         play_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "play.png");
         options_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "options.png");
        
     	try 
     	{
 			this.menuTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
 			this.menuTextureAtlas.load();
 		} 
     	catch (final TextureAtlasBuilderException e)
     	{
 			Debug.e(e);
 		}
 	}
 	
 	private void loadMenuAudio()
 	{
 		
 	}
 	
 	private void loadMenuFonts()
 	{
 		FontFactory.setAssetBasePath("font/");
 		final ITexture mainFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		font = FontFactory.createStrokeFromAsset(activity.getFontManager(), mainFontTexture, activity.getAssets(), "font.ttf", 50, true, Color.WHITE, 2, Color.BLACK);
 		font.load();
 	}
 
 	private void loadGameGraphics()
 	{
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
         gameTextureAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 1100, 800, TextureOptions.BILINEAR);
         
        	platform1_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "platform4.png");
        	platform2_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "platform5.png");
        	platform3_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "platform6.png");
        	ground_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "ground.png");
        	dokan_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "dokan.png");
        	iphone_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "iphone.png");
        	degitalClock_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "degitalClock.png");
         coin_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "coin.png");
        player_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "player.png", 7, 1);
         big_player_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "big_player.png", 3, 1);
         enemy1_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "enemy1.png", 3, 1);
         enemy2_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "enemy2.png", 3, 1);
         kinoko_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "kinoko.png", 3, 1);
         complete_window_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, activity, "levelCompleteWindow.png");
         complete_stars_region = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, activity, "star.png", 2, 1);
 
     	try 
     	{
 			this.gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
 			this.gameTextureAtlas.load();
 		} 
     	catch (final TextureAtlasBuilderException e)
     	{
 			Debug.e(e);
 		}
 	}
 	
 	private void loadGameFonts()
 	{
 		FontFactory.setAssetBasePath("font/");
 		final ITexture mainFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		timeFont = FontFactory.createStrokeFromAsset(activity.getFontManager(), mainFontTexture, activity.getAssets(), "font.ttf", 20, true, Color.WHITE, 2, Color.BLACK);
 		timeFont.load();
 	}
 	
 	private void loadGameAudio()
 	{
 		
 	}
 	
 	public void unloadGameTextures()
 	{
 		// TODO (Since we did not create any textures for game scene yet)
 	}
 	
 	public void loadSplashScreen()
 	{
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         splashTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 800, 500, TextureOptions.BILINEAR);
         splash_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, activity, "splash.png", 0, 0);
         splashTextureAtlas.load();	
 	}
 	
 	public void unloadSplashScreen()
 	{
 		splashTextureAtlas.unload();
 		splash_region = null;
 	}
 	
 	public void unloadMenuTextures()
 	{
 		menuTextureAtlas.unload();
 	}
 	
 	public void loadMenuTextures()
 	{
 		menuTextureAtlas.load();
 	}
 	
 	/**
 	 * @param engine
 	 * @param activity
 	 * @param camera
 	 * @param vbom
 	 * <br><br>
 	 * We use this method at beginning of game loading, to prepare Resources Manager properly,
 	 * setting all needed parameters, so we can latter access them from different classes (eg. scenes)
 	 */
 	public static void prepareManager(Engine engine, GameActivity activity, BoundCamera camera, VertexBufferObjectManager vbom)
 	{
 		getInstance().engine = engine;
 		getInstance().activity = activity;
 		getInstance().camera = camera;
 		getInstance().vbom = vbom;
 	}
 	
 	//---------------------------------------------
 	// GETTERS AND SETTERS
 	//---------------------------------------------
 	
 	public static ResourcesManager getInstance()
 	{
 		return INSTANCE;
 	}
 }
