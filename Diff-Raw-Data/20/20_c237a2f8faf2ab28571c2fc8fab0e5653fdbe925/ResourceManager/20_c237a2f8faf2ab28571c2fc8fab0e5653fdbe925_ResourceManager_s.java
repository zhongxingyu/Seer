 package com.codefest2013.game;
 
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.Engine;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.util.debug.Debug;
 
 import android.content.Context;
 import android.util.DisplayMetrics;
 
 public class ResourceManager extends Object {
 	
 	public final static float REAL_WORLD_WIDTH = 2048.0f;
 	public final static float REAL_WORLD_HEIGHT = 990.0f;
 	
     public float CAMERA_WIDTH;
     public float CAMERA_HEIGHT;
     public float WORLD_WIDTH;
     public float WORLD_HEIGHT;
     
 	public Engine engine;
 	public Context context;
     
     public float WORLD_SCALE_CONSTANT;
 
     /**
      * Goblin animation
      */
     public ITiledTextureRegion goblinTiledLeftWalk;
     public ITiledTextureRegion goblinTiledRightWalk;
     
     public ITextureRegion[] foxLeftWalk = new ITextureRegion[12];
     public ITextureRegion[] foxRightWalk = new ITextureRegion[12];
        
     /**
      * Parts of background image
      */
     public ITextureRegion bgLB;
     public ITextureRegion bgLT;
     public ITextureRegion bgRB;
     public ITextureRegion bgRT;
     
     /**
      * Fireplace animation
      */
     public ITextureRegion fireplace1;
     public ITextureRegion fireplace2;
     
     /**
      * Tree texture
      */
     public ITextureRegion tree;
     
     /**
      * Clock animation
      */
     public ITextureRegion[] clock = new ITextureRegion[10];
     
     /**
      * Lamp texture
      */
     public ITextureRegion lamp;
     
     /**
      * Stocking texture
      */
     public ITextureRegion stocking;
     
     /**
      * Music
      */
     public Music fireplaceMusic;
     public Music tickTookMusic;
     
 	private static final ResourceManager INSTANCE = new ResourceManager();
 	
 	//====================================================
 	// CONSTRUCTOR
 	//====================================================
 	private ResourceManager(){
 	}
 
 	//====================================================
 	// GETTERS & SETTERS
 	//====================================================
 	// Retrieves a global instance of the ResourceManager
 	public static ResourceManager getInstance(){
 		return INSTANCE;
 	}
 	
 	//====================================================
 	// PUBLIC METHODS
 	//====================================================
 	// Setup the ResourceManager
 	public void setupContext(final Context pContext){
 		context = pContext;
 		
         final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
 
         CAMERA_WIDTH = displayMetrics.widthPixels;
         CAMERA_HEIGHT = displayMetrics.heightPixels;
 
         WORLD_WIDTH = CAMERA_HEIGHT * (REAL_WORLD_WIDTH/REAL_WORLD_HEIGHT);
         WORLD_HEIGHT = CAMERA_HEIGHT;
 
         WORLD_SCALE_CONSTANT = WORLD_HEIGHT/REAL_WORLD_HEIGHT;
 	}
 	
 	public void setupEngine(final Engine pEngine)
 	{
 		engine = pEngine;
 	}
 	
 	// Loads all game resources.
 	public static void loadResources() {
 		getInstance().loadTextures();
 		getInstance().loadAudio();
 		getInstance().loadFonts();
 	}
 	
 	// Unloads all game resources.
 	public static void unloadResources() {
 		getInstance().unloadGameTextures();
 		getInstance().unloadAudio();
 		getInstance().unloadFonts();
 	}
 	
 	// ============================ LOAD TEXTURES (GAME) ================= //
 	private void loadTextures(){
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
     	BitmapTextureAtlas textureAtlas = null;
 
     	textureAtlas = new BitmapTextureAtlas(engine.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         bgLB = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/lb.png", 0, 0);
         bgLT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/lt.png", 0, 496);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(engine.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         bgRB = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/rb.png", 0, 0);
         bgRT = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/rt.png", 0, 496);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(engine.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         fireplace1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/fireplace1.png", 0, 0);
         fireplace2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/fireplace2.png", 0, 512);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(engine.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         tree = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/tree.png", 0, 0);
         lamp = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/lamp.png", 500, 0);
         stocking = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, "background/stocking.png", 500, 180);
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(engine.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=0; i<5; ++i)
         {
         	String name = "background/clock" + (i+1) + ".png";
         	clock[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, name, 150*i, 0);
         }
         textureAtlas.load();
         
         textureAtlas = new BitmapTextureAtlas(engine.getTextureManager(), 1024, 1024,
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=5; i<10; ++i)
         {
         	String name = "background/clock" + (i+1) + ".png";
         	clock[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, context, name, 150*(i-5), 0);
         }
         textureAtlas.load();
         
         BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         
         // leftWalk fox
         mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=0; i<12; ++i)
         {
        	String name = "fox/leftWalk/" + i + ".png";
         	foxLeftWalk[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, context, name);
         }
         try {
             mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
             mBuildableBitmapTextureAtlas.load();
         } catch (final TextureAtlasBuilderException e) {
             Debug.e(e);
         }
         
         // rightWalk fox
         mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         for(int i=0; i<12; ++i)
         {
        	String name = "fox/rightWalk/" + i + ".png";
         	foxRightWalk[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, context, name);
         }
         try {
             mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
             mBuildableBitmapTextureAtlas.load();
         } catch (final TextureAtlasBuilderException e) {
             Debug.e(e);
         }
         
         mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         goblinTiledRightWalk = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, context, 
         		"goblin/rightWalk/rightWalk.png", 3, 4);
         try {
             mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
             mBuildableBitmapTextureAtlas.load();
         } catch (final TextureAtlasBuilderException e) {
             Debug.e(e);
         }        
         
         mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(engine.getTextureManager(), 1024, 1024, 
         		BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
         goblinTiledLeftWalk = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, context, 
         		"goblin/leftWalk/leftWalk.png", 3, 4);
         try {
             mBuildableBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
             mBuildableBitmapTextureAtlas.load();
         } catch (final TextureAtlasBuilderException e) {
             Debug.e(e);
         }
 	}
 	
 	// ============================ UNLOAD TEXTURES (GAME) =============== //
 	private void unloadGameTextures(){
 		unloadTexture(goblinTiledLeftWalk);
 		unloadTexture(goblinTiledRightWalk);
 		unloadTexture(bgLB);
 		unloadTexture(bgLT);
 		unloadTexture(bgRB);
 		unloadTexture(bgRT);
 		unloadTexture(fireplace1);
 		unloadTexture(fireplace2);
 		unloadTexture(tree);
 		unloadTexture(lamp);
 		unloadTexture(stocking);
 		unloadTextureArray(clock);
 		unloadTextureArray(foxLeftWalk);
 		unloadTextureArray(foxRightWalk);
 	}
 	
 	private void unloadTexture(ITextureRegion texture)
 	{
 		if(texture!=null && texture.getTexture().isLoadedToHardware() )
 		{
 			texture.getTexture().unload();
 			texture = null;
 		}
 	}
 	
 	private void unloadTextureArray(ITextureRegion[] textures)
 	{
 		for(int i=0; i<textures.length; ++i)
 		{
 			unloadTexture(textures[i]);
 		}
 	}	
 	
 	// =========================== LOAD AUDIO ======================== //
 	private void loadAudio(){
         MusicFactory.setAssetBasePath("afx/");
         try {
         	tickTookMusic = MusicFactory.createMusicFromAsset(engine.getMusicManager(), context, "ticktook.mp3");
         	fireplaceMusic = MusicFactory.createMusicFromAsset(engine.getMusicManager(), context, "fireplace.mp3");
         } catch (IOException e) {
         	e.printStackTrace();
         }
 	}
 	
 	// =========================== UNLOAD AUDIO ====================== //
 	private void unloadAudio(){
 		unloadMusic(tickTookMusic);
 		unloadMusic(fireplaceMusic);
 	}
 	
 	private void unloadMusic(Music music)
 	{
 		if(music!=null && !music.isReleased()) {
 			music.stop();
 			engine.getMusicManager().remove(music);
 			music = null;
 		}
 	}
 
 	// ============================ LOAD FONTS ========================== //
 	private void loadFonts(){
 	}
 	
 	// ============================ UNLOAD FONTS ======================== //
 	private void unloadFonts(){
 	}
 }
