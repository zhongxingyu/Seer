 package com.testgame.resource;
 
 //hullo
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXTiledMap;
 import org.andengine.extension.tmx.util.exception.TMXLoadException;
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
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import org.andengine.util.debug.Debug;
 
 import android.graphics.Color;
 
 import com.example.testgame.MainActivity;
 
 public class ResourcesManager {
     
     // TODO: I think maybe this info shouldn't be stored here. This is just for graphics/music/fonts. -Carrie
     public String userString;
     public String opponent;
     public String opponentString;
     public boolean turn;
     public boolean inGame;
     public ArrayList<Integer> unitArray;
     private static final ResourcesManager INSTANCE = new ResourcesManager();
     public String gameId;
 	
 	public Engine engine;
 	public MainActivity activity;
     public Camera camera;
     public VertexBufferObjectManager vbom;
     
     /**
      * @param engine
      * @param activity
      * @param camera
      * @param vbom
      * <br><br>
      * We use this method at beginning of game loading, to prepare Resources Manager properly,
      * setting all needed parameters, so we can latter access them from different classes (eg. scenes)
      */
     public static void prepareManager(Engine engine,  MainActivity activity, Camera camera, VertexBufferObjectManager vbom)
     {
         getInstance().engine = engine;
         getInstance().activity = activity;
         getInstance().camera = camera;
         getInstance().vbom = vbom;
     }
     
     public static ResourcesManager getInstance()
     {
         return INSTANCE;
     }
     
     //==============================
     // Splash Resources
     //==============================
     
     public ITextureRegion splash_region;
 	private BitmapTextureAtlas splashTextureAtlas;
     
     public void loadSplashScreen()
     {
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
     	splashTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
     	splash_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, activity, "splash.png", 0, 0);
     	splashTextureAtlas.load();
     }
     
     public void unloadSplashScreen()
     {
     	splashTextureAtlas.unload();
     	splash_region = null;
     }
     
     //==============================
     // Tutorial Resources
     //==============================
     
     private BitmapTextureAtlas tutorialAtlas;
     public ITextureRegion tutorial_background_region;
     
     private BitmapTextureAtlas units_atlas, controls_atlas, basics_atlas;
     public ITextureRegion units_region, controls_region, basics_region;
     
     public void loadTutorialResources() {
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/tutorial/");
     	tutorialAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
     	tutorial_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tutorialAtlas, activity, "blankmenu.png", 0, 0);
     	tutorialAtlas.load();
     	
     	units_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR);
     	units_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(units_atlas, activity, "unitsbutton.png", 0, 0);
     	units_atlas.load();
     	
     	controls_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR);
     	controls_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(controls_atlas, activity, "controlsbutton.png", 0, 0);
     	controls_atlas.load();
     	
     	basics_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR);
     	basics_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(basics_atlas, activity, "basicsbutton.png", 0, 0);
     	basics_atlas.load();
     	
     	final ITexture secFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         
         handwriting_font = FontFactory.createStrokeFromAsset(activity.getFontManager(), secFontTexture, activity.getAssets(), "Gilles_Handwriting.ttf", 36, true, Color.WHITE, 1, Color.BLACK);
         handwriting_font.load();	
     }
     
     public void unloadTutorialResources() {
     	tutorialAtlas.unload();
     }
     
     //==============================
     // Menu Resources
     //==============================
     
     public ITextureRegion menu_background_region;
     // Button texture regions
     private BuildableBitmapTextureAtlas menuTextureAtlas;
     public ITextureRegion newgame_region, options_region, continue_region, login_region, reset_region, blank_region, logout_region;
     public Music menu_background_music;
     public Font font;
     
     public void loadMenuResources()
     {
         loadMenuGraphics();
         loadMenuFonts();
         loadMenuMusic();
     }
     
     private void loadMenuMusic() {
    	 	MusicFactory.setAssetBasePath("mfx/");
         try {
                 this.menu_background_music = MusicFactory.createMusicFromAsset(this.engine.getMusicManager(), activity, "school.wav");
                 this.menu_background_music.setLooping(true);
                 this.menu_background_music.setVolume(.25f);
         } catch (final IOException e) {
                 Debug.e("Error", e);
         }
    }
     
     private void loadMenuGraphics(){
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
     	menuTextureAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	
     	menu_background_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "menu.png");
     	
     	newgame_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "newgamebutton.png");
     	options_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "optionsbutton.png");
     	login_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "loginbutton.png");
     	continue_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "continuebutton.png");
     	reset_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "resetbutton.png");
     	blank_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "blankbutton.png");
    	logout_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "logoutmenubutton.png");
 
     	try {
     	    this.menuTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
     	    this.menuTextureAtlas.load();
     	} 
     	catch (final TextureAtlasBuilderException e){
     	    Debug.e(e);
     	}
     	
     }
     
     private void loadMenuFonts()
     {
         FontFactory.setAssetBasePath("font/");
         final ITexture mainFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
         font = FontFactory.createStrokeFromAsset(activity.getFontManager(), mainFontTexture, activity.getAssets(), "Toony.ttf", 50, true, Color.BLACK, 2, Color.WHITE);
         font.load();        
     }
 
     public void unloadMenuTextures()
     {
         menuTextureAtlas.unload();
     }
     
     //==============================
     // Setup Resources
     //==============================
     
     private BuildableBitmapTextureAtlas setup_atlas;
     public ITextureRegion jock, ditz, nerd;
     
     private BitmapTextureAtlas setup_backbround_atlas;
     public ITextureRegion setup_background;
     
     public void loadSetupResources() {
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/sprites/");
     	setup_atlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	
     	jock = BitmapTextureAtlasTextureRegionFactory.createFromAsset(setup_atlas, activity, "jock.png");
     	nerd = BitmapTextureAtlasTextureRegionFactory.createFromAsset(setup_atlas, activity, "nerd.png");
     	ditz = BitmapTextureAtlasTextureRegionFactory.createFromAsset(setup_atlas, activity, "ditz.png");
     	
     	try {
     	    setup_atlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
     	    setup_atlas.load();
     	} 
     	catch (final TextureAtlasBuilderException e){
     	    Debug.e(e);
     	}
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
     	setup_backbround_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
     	setup_background = BitmapTextureAtlasTextureRegionFactory.createFromAsset(setup_backbround_atlas, activity, "menu.png", 0, 0);
     	setup_backbround_atlas.load();
     	
     }
     
     public void unloadSetupResources() {
     	setup_atlas.unload();
     }
     
     //==============================
     // Game Resources
     //==============================
     
     private BitmapTextureAtlas pause_atlas;
     public ITextureRegion pause_region;
 
     private BitmapTextureAtlas jock_tileset_atlas, ditz_tileset_atlas, nerd_tileset_atlas;
     public TiledTextureRegion jock_tileset, ditz_tileset, nerd_tileset;
     
     private BitmapTextureAtlas question_atlas;
     public ITextureRegion question_region;
     
     private BitmapTextureAtlas top_bar_atlas, bottom_bar_atlas;
     public ITextureRegion top_bar, bottom_bar;
     
     public TMXTiledMap tiledMap;
     
     public Font handwriting_font;
     
     public Music footsteps;
     public Music hit;
     
     public void loadGameResources()
     {
         loadGameGraphics();
         loadGameFonts();
         loadGameAudio();
         loadGameMap();
     }
     
     private void loadGameMap() {
     	
     	try {
             final TMXLoader tmxLoader = new TMXLoader(activity.getAssets(), activity.getTextureManager(), TextureOptions.NEAREST, vbom);
             this.tiledMap = tmxLoader.loadFromAsset("tmx/basic.tmx");
             //this.tiledMap.setOffsetCenter(0, 0);
         } catch (final TMXLoadException e) {
              Debug.e(e);
         }
     }    
 
     private void loadGameGraphics()
     {
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/sprites/");
     	
     	jock_tileset_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	ditz_tileset_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	nerd_tileset_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	
     	jock_tileset = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(jock_tileset_atlas, activity, "jock_tileset.png", 0, 0, 10, 6);
     	ditz_tileset = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(ditz_tileset_atlas, activity, "ditz_tileset.png", 0, 0, 10, 6);
     	nerd_tileset = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(nerd_tileset_atlas, activity, "nerd_tileset.png", 0, 0, 9, 6);
 
     	jock_tileset_atlas.load();
     	ditz_tileset_atlas.load();
     	nerd_tileset_atlas.load();
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
     	
     	question_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	question_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(question_atlas, activity, "gear.png", 0, 0);
     	question_atlas.load();
     	
     	bottom_bar_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	bottom_bar = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bottom_bar_atlas, activity, "bottombar.png", 0, 0);
     	bottom_bar_atlas.load();
     	
     	top_bar_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	top_bar = BitmapTextureAtlasTextureRegionFactory.createFromAsset(top_bar_atlas, activity, "topbar.png", 0, 0);
     	top_bar_atlas.load();
     	
     	pause_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	pause_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(pause_atlas, activity, "pause.png", 0, 0);
     	pause_atlas.load();
     }
     
     private void loadGameFonts()
     {
     	FontFactory.setAssetBasePath("font/");
         final ITexture mainFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
         font = FontFactory.createStrokeFromAsset(activity.getFontManager(), mainFontTexture, activity.getAssets(), "Toony.ttf", 50, true, Color.BLACK, 3, Color.WHITE);
         font.load();   
         
         final ITexture secFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         
         handwriting_font = FontFactory.createStrokeFromAsset(activity.getFontManager(), secFontTexture, activity.getAssets(), "Gilles_Handwriting.ttf", 36, true, Color.WHITE, 1, Color.BLACK);
         handwriting_font.load();
         
     }
     
     private void loadGameAudio()
     {
         
     }
     
     public void unloadGameTextures()
     {
     	jock_tileset_atlas.unload();
     	ditz_tileset_atlas.unload();
     	nerd_tileset_atlas.unload();
     	question_atlas.unload();
     	pause_atlas.unload();
     	bottom_bar_atlas.unload();
     	top_bar_atlas.unload();
         
     }
 
 	public void resetGame() {
 		unitArray = null;
 		gameId = null;
 		turn = false;
 		inGame = false;
 		opponent = null;
 		opponentString = null;
 		
 	}
 
 }
