 package com.testgame.resource;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXProperties;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.extension.tmx.TMXTileProperty;
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
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 
 import org.andengine.util.debug.Debug;
 
 import android.content.res.AssetManager;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.util.Log;
 
 import com.example.testgame.MainActivity;
 
 public class ResourcesManager {
     
 	
 	/*
 	 * MAP INFORMATION
 	 */
 	
 	//Basic.tmx
 	Point[] basic1 = {};
 	Point[] basic2 = {new Point(5,6), new Point(6,5)};
 	Point[] classroom1 = {new Point(3,2), new Point(1,2), new Point(7, 2), new Point(9, 2), new Point(4, 0)};
 	Point[] classroom2 = {new Point(2,6), new Point(4,6), new Point(6,6), new Point(8,6), new Point(4, 7)};
 	Point[] defaultSpawns = {};
 	
 	
 	/*
 	 * END MAP INFORMATION
 	 */
 	
 	
 	
     public String userString;
     public String opponent;
     public String opponentString;
     public String mapString = "Default";
     public boolean turn;
     public boolean inGame;
     public boolean isLocal;
     public ArrayList<Integer> unitArray;
     public ArrayList<Integer> unitArray2;
     public String deviceID;
     public String opponentDeviceID;
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
     
     public Font cartoon_font_white;
     public Font cartoon_font_red;
     public Font cartoon_font_blue;
     
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
     public ITextureRegion newgame_region, options_region, continue_region, login_region, reset_region, blank_region, howtoplay_region, logout_region, play_region;
     public Music menu_background_music, select_sound;
 
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
                 this.menu_background_music = MusicFactory.createMusicFromAsset(this.engine.getMusicManager(), activity, "kickstarter.mp3");
                 this.menu_background_music.setLooping(true);
                 this.menu_background_music.setVolume(2f);
                 
                 this.select_sound = MusicFactory.createMusicFromAsset(engine.getMusicManager(), activity, "buttonpush.wav");
                 
                 touch_sound = MusicFactory.createMusicFromAsset(engine.getMusicManager(), activity, "touch.mp3");
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
 
     	howtoplay_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "howtoplaybutton.png");
 
     	logout_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "logoutbutton.png");
     	
     	play_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, activity, "playbutton.png");
     	
     	
 
     	try {
     	    this.menuTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
     	    this.menuTextureAtlas.load();
     	} 
     	catch (final TextureAtlasBuilderException e){
     	    Debug.e(e);
     	}
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
     	
     	dialog_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	dialog_background = BitmapTextureAtlasTextureRegionFactory.createFromAsset(dialog_atlas, activity, "dialogbackground.png", 0, 0);
     	dialog_atlas.load();
     	
     }
     
     private void loadMenuFonts()
     {
         FontFactory.setAssetBasePath("font/");
         final ITexture mainFontTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
         font = FontFactory.createStrokeFromAsset(activity.getFontManager(), mainFontTexture, activity.getAssets(), "Toony.ttf", 50, true, Color.BLACK, 2, Color.WHITE);
         font.load();  
         
         final ITexture whiteTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         final ITexture redTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         final ITexture blueTexture = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         
         
         cartoon_font_white = FontFactory.createStrokeFromAsset(activity.getFontManager(), whiteTexture, activity.getAssets(), "cartoonfont.ttf", 36, true, Color.WHITE, 1, Color.BLACK);
         cartoon_font_white.load();	
         
         cartoon_font_red = FontFactory.createStrokeFromAsset(activity.getFontManager(), redTexture, activity.getAssets(), "cartoonfont.ttf", 36, true, Color.RED, 1, Color.BLACK);
         cartoon_font_red.load();
         
         cartoon_font_blue = FontFactory.createStrokeFromAsset(activity.getFontManager(), blueTexture, activity.getAssets(), "cartoonfont.ttf", 36, true, Color.BLUE, 1, Color.BLACK);
         cartoon_font_blue.load();
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
     
     private BitmapTextureAtlas gear_atlas;
     public ITextureRegion gear_region;
     
     private BitmapTextureAtlas top_bar_atlas, bottom_bar_atlas;
     public ITextureRegion top_bar, bottom_bar;
     
 
     private BitmapTextureAtlas red_button_atlas, blue_button_atlas;
     public ITextureRegion red_button, blue_button;
     
 
     public TMXTiledMap selectedMap, tiledMap;
     
     public Font handwriting_font;
     
     public Music footsteps;
     public Music hit;
     
     public Music walking_sound, attack_sound, touch_sound;
     
     private BitmapTextureAtlas dialog_atlas;
     public ITextureRegion dialog_background;
     
     public BitmapTextureAtlas map_tiles_atlas;
     public TiledTextureRegion map_tiles;
     
     public void loadGameResources()
     {
         loadGameGraphics();
         loadGameFonts();
         loadGameAudio();
         //loadGameMap();
     }
     
     @SuppressWarnings("unused")
 	private void loadGameMap() {
     	
     	try {
             final TMXLoader tmxLoader = new TMXLoader(activity.getAssets(), activity.getTextureManager(), TextureOptions.NEAREST, vbom);
             this.tiledMap = tmxLoader.loadFromAsset("tmx/basic.tmx");
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
     	nerd_tileset = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(nerd_tileset_atlas, activity, "nerd_tileset.png", 0, 0, 10, 6);
 
     	jock_tileset_atlas.load();
     	ditz_tileset_atlas.load();
     	nerd_tileset_atlas.load();
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/hud/");
     	
     	gear_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	gear_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gear_atlas, activity, "settinggear.png", 0, 0);
     	gear_atlas.load();
     	
     	bottom_bar_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	bottom_bar = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bottom_bar_atlas, activity, "bottombar.png", 0, 0);
     	bottom_bar_atlas.load();
     	
     	top_bar_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	top_bar = BitmapTextureAtlasTextureRegionFactory.createFromAsset(top_bar_atlas, activity, "topbar.png", 0, 0);
     	top_bar_atlas.load();
     	
     	pause_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 500, 500, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	pause_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(pause_atlas, activity, "pausebutton.png", 0, 0);
     	pause_atlas.load();
     	
     	blue_button_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 200, 200, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	blue_button = BitmapTextureAtlasTextureRegionFactory.createFromAsset(blue_button_atlas, activity, "blue_button.png", 0, 0);
     	blue_button_atlas.load();
     	
     	red_button_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 200, 200, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	red_button = BitmapTextureAtlasTextureRegionFactory.createFromAsset(red_button_atlas, activity, "red_button.png", 0, 0);
     	red_button_atlas.load();
     	
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
     	
     	map_tiles_atlas = new BitmapTextureAtlas(activity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
     	map_tiles = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(map_tiles_atlas, activity, "maptiles.png", 0, 0, 11, 2);
     	map_tiles_atlas.load();
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
         MusicFactory.setAssetBasePath("mfx/");
         try {
 			walking_sound = MusicFactory.createMusicFromAsset(engine.getMusicManager(), activity, "running.wav");
 			walking_sound.setLooping(true);
 			walking_sound.setVolume(2f);
 			attack_sound = MusicFactory.createMusicFromAsset(engine.getMusicManager(), activity, "whack.wav");
 			
 		} catch (IllegalStateException e) {
 			Debug.e("Error", e);
 		} catch (IOException e) {
 			Debug.e("Error", e);
 		}
         
     }
     
     public void unloadGameTextures()
     {
     	jock_tileset_atlas.unload();
     	ditz_tileset_atlas.unload();
     	nerd_tileset_atlas.unload();
     	gear_atlas.unload();
     	pause_atlas.unload();
     	bottom_bar_atlas.unload();
     	top_bar_atlas.unload();
         
     }
 
 
 	public void play_music() {
 		if (menu_background_music != null) menu_background_music.play();	
 	}
 
 	public void pause_music() {
 		if (menu_background_music != null) menu_background_music.pause();
 	}
 	
 	public ArrayList<Point> obstacles;
 	
 	public void setMap(String mapName) {
 		Log.d("AndEngine", "[ResourcesManager] setting map");
 		this.mapString = mapName;
 		this.obstacles = new ArrayList<Point>();
     	try {
             final TMXLoader tmxLoader = new TMXLoader(activity.getAssets(), activity.getTextureManager(), TextureOptions.NEAREST, vbom, new ITMXTilePropertiesListener() {
 				@Override
 				public void onTMXTileWithPropertiesCreated(
 						TMXTiledMap pTMXTiledMap, TMXLayer pTMXLayer,
 						TMXTile pTMXTile,
 						TMXProperties<TMXTileProperty> pTMXTileProperties) {
 					// TODO make this work.
 					Log.d("AndEngine", "found tile property");
 					for (TMXTileProperty tp : pTMXTileProperties) {
 						Log.d("AndEngine", pTMXTile.getTileColumn()+"x"+pTMXTile.getTileRow()+" -> "+tp.getName() + " : " + tp.getValue());
 						obstacles.add(new Point(pTMXTile.getTileColumn(), pTMXTile.getTileRow()));
 					}
 					
 				}
             });
             
             this.tiledMap = tmxLoader.loadFromAsset("tmx/"+mapString);
             
             Log.d("AndEngine", "[ResourcesManager] successfully loaded map");
         } 
     	catch (final TMXLoadException e) {
              Debug.e(e);
         }
 			
 	}
 	
 	public void resetGame() {
 		unitArray = null;
 		gameId = null;
 		turn = false;
 		inGame = false;
 		opponent = null;
 		opponentString = null;
 		isLocal = false;
 	}
 
 	private boolean first;
 	
 	public String getLocalName() {
 		if (first) {
 			first = false;
 			return "Player 2";
 		} else {
 			first = true;
 			return "Player 1";
 		}
 	}
 	
 	public String[] maps() {
 		
 		AssetManager assets = activity.getAssets();
 		try {
 			Log.d("AndEngine", "[tmx maps] " + assets.list("tmx").length);
 			return assets.list("tmx");
 		} catch (IOException e) {
 			Log.d("Error", e.toString());
 			return null;
 		}
	}
 	
 	public int getNumber(String map){
 		if(map.equals("basic.tmx")){
 			return 10;
 		}
 		else if(map.equals("classroom.tmx")){
 				return 4;
 		}
 		return -1;
 						
 	}
 	
 	
 	public Point[] getSpawn1(String map){
 		if(map.equals("basic.tmx")){
 			return basic1;
 		}
 		else if(map.equals("classroom.tmx")){
 				return classroom1;
 		} else
 			return defaultSpawns;
 		
 						
 	}
 	
 	public Point[] getSpawn2(String map){
 		if(map.equals("basic.tmx")){
 			return basic2;
 		}
 		else if(map.equals("classroom.tmx")){
 				return classroom2;
 		}
 		return defaultSpawns;
 						
 	}
 
 }
