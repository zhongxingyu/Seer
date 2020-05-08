 package com.madgear.ninjatrials;
 
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.sound.Sound;
 import org.andengine.audio.sound.SoundFactory;
 import org.andengine.engine.Engine;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.util.debug.Debug;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.Log;
 
 
 public class ResourceManager {
 
     private static final TextureOptions mTransparentTextureOption = TextureOptions.BILINEAR;
 
     // ResourceManager Singleton instance
     private static ResourceManager INSTANCE;
 
     /* The variables listed should be kept public, allowing us easy access
        to them when creating new Sprites, Text objects and to play sound files */
     public NinjaTrials activity;
     public Engine engine;
     public Context context;
     public float cameraWidth;
     public float cameraHeight;
     public TextureManager textureManager;
 
     // HUD:
     public static ITextureRegion hudPowerBarCursorTR;
     public static ITextureRegion hudCursorTR;
     public static ITextureRegion hudPowerBarPushTR;
 
     // CUT SCENE:
     public static ITiledTextureRegion cutShoTR;
     public static ITextureRegion cutTreeTopTR;
     public static ITextureRegion cutTreeBottomTR;
     public static ITextureRegion cutCandleTopTR;
     public static ITextureRegion cutCandleBottomTR;
     public static ITextureRegion cutCandleLightTR;
     public static ITextureRegion cutEyesTR;
     public static ITextureRegion cutBackgroundTR;
     public static ITextureRegion cutSweatDropTR;
     public static ITextureRegion cutSwordSparkle1TR;
     public static ITiledTextureRegion cutSwordSparkle2TR;
     public static ITextureRegion cutHudBarTR;
     public static ITextureRegion cutHudCursorTR;
 
     // FONTS:
     public Font fontSmall;        // pequeño
     public Font fontMedium;        // mediano
     public Font fontBig;        // grande
 
     //public BuildableBitmapTextureAtlas mBitmapTextureAtlas;
     public ITiledTextureRegion mTiledTextureRegion;
     //public ITextureRegion mSpriteTextureRegion;
 
     public Music music;
     public Sound mSound;
 
     public float cameraScaleFactorX = 1;
     public float cameraScaleFactorY = 1;
 
     // Inicializa el manejador:
     public static void setup(NinjaTrials pActivity, Engine pEngine, Context pContext,
             float pCameraWidth, float pCameraHeight){
         getInstance().activity = pActivity;
         getInstance().engine = pEngine;
         getInstance().context = pContext;
         getInstance().cameraWidth = pCameraWidth;
         getInstance().cameraHeight = pCameraHeight;
         getInstance().textureManager = pActivity.getTextureManager();
     }
 
     // Constructor:
     ResourceManager(){
         // The constructor is of no use to us
     }
 
     public synchronized static ResourceManager getInstance(){
         if(INSTANCE == null){
             INSTANCE = new ResourceManager();
         }
         return INSTANCE;
     }
 
     // Cada escena debe tener sus métodos para cargar y descargar recursos (metodo load y unload).
     // tanto en gráficos como música y sonido.
     // Deben ser "synchronized".
     public synchronized void loadHUDResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/hud/");
 
         // Barra power cursor:
         if(hudPowerBarCursorTR==null) {
             BitmapTextureAtlas hudPowerBarCursorT = new BitmapTextureAtlas(
                     textureManager, 240, 120, mTransparentTextureOption);
             hudPowerBarCursorTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     hudPowerBarCursorT, activity, "power_bar_cursor.png", 0, 0);
             hudPowerBarCursorT.load();
         }
 
         // Cursor:
         if(hudCursorTR==null) {
             BitmapTextureAtlas hudCursorT = new BitmapTextureAtlas(textureManager, 59, 52,
                     mTransparentTextureOption);
             hudCursorTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(hudCursorT,
                     activity, "h_cursor.png", 0, 0);
             hudCursorT.load();
         }
 
         // Barra power push:
         if(hudPowerBarPushTR==null) {
             BitmapTextureAtlas hudPowerBarPushT = new BitmapTextureAtlas(textureManager, 120, 240,
                     mTransparentTextureOption);
             hudPowerBarPushTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     hudPowerBarPushT, activity, "power_bar_push.png", 0, 0);
             hudPowerBarPushT.load();
         }
     }
 
     public synchronized void unloadHUDResources() {
         if(hudPowerBarCursorTR!=null) {
             if(hudPowerBarCursorTR.getTexture().isLoadedToHardware()) {
                 hudPowerBarCursorTR.getTexture().unload();
                 hudPowerBarCursorTR = null;
             }
         }
         if(hudCursorTR!=null) {
             if(hudCursorTR.getTexture().isLoadedToHardware()) {
                 hudCursorTR.getTexture().unload();
                 hudCursorTR = null;
             }
         }
         if(hudPowerBarPushTR!=null) {
             if(hudPowerBarPushTR.getTexture().isLoadedToHardware()) {
                 hudPowerBarPushTR.getTexture().unload();
                 hudPowerBarPushTR = null;
             }
         }
     }
 
     // Recursos para la escena de corte:
     public synchronized void loadCutSceneResources() {
         // Texturas:
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/trial_cut/");
 
         // Sho:
         if(cutShoTR==null) {
             BuildableBitmapTextureAtlas cutShoT = new BuildableBitmapTextureAtlas(
                     textureManager, 1742, 1720, mTransparentTextureOption);
             cutShoTR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     cutShoT, context, "cut_ch_sho_cut_anim.png", 2, 2);
             try {
                 cutShoT.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             } catch (TextureAtlasBuilderException e) { e.printStackTrace(); }
             cutShoT.load();
         }
 
         // Arbol:
         BitmapTextureAtlas cutTreeT = new BitmapTextureAtlas(textureManager, 640, 950,
                 mTransparentTextureOption);
         ITextureRegion cutTreeTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 cutTreeT, activity, "cut_breakable_tree.png", 0, 0);
         cutTreeT.load();
         cutTreeTopTR = TextureRegionFactory.extractFromTexture(cutTreeT, 0, 0, 640, 403, false);
         cutTreeBottomTR = TextureRegionFactory.extractFromTexture(cutTreeT, 0, 404, 640, 546,
                 false);
 
         // Farol:
         BitmapTextureAtlas cutCandleT = new BitmapTextureAtlas(textureManager, 310, 860,
                 mTransparentTextureOption);
         ITextureRegion cutCandleTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 cutCandleT, activity, "cut_breakable_candle_base.png", 0, 0);
         cutCandleT.load();
         cutCandleTopTR = TextureRegionFactory.extractFromTexture(cutCandleT, 0, 0, 310, 515, false);
         cutCandleBottomTR = TextureRegionFactory.extractFromTexture(cutCandleT, 0, 516, 310, 344,
                 false);
 
         // Luz del farol:
         BitmapTextureAtlas cutCandleLightT = new BitmapTextureAtlas(textureManager, 760, 380,
                 mTransparentTextureOption);
         ITextureRegion cutCandleLightAllTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 cutCandleLightT, activity, "cut_breakable_candle_light.png", 0, 0);
         cutCandleLightT.load();
         cutCandleLightTR = TextureRegionFactory.extractFromTexture(cutCandleLightT, 0, 0, 388, 380,
                 false);
 
         // Espada 2:
         if(cutSwordSparkle2TR==null) {
             BuildableBitmapTextureAtlas cutSword2T = new BuildableBitmapTextureAtlas(
                     textureManager, 1358, 1034, mTransparentTextureOption);
             cutSwordSparkle2TR = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     cutSword2T, context, "cut_sword_sparkle2.png", 2, 2);
             try {
                 cutSword2T.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             } catch (TextureAtlasBuilderException e) { e.printStackTrace(); }
             cutSword2T.load();
         }
 
         // Ojos:
         if(cutEyesTR==null) {
             BitmapTextureAtlas cutEyesT =  new BitmapTextureAtlas(textureManager, 1416, 611,
                     mTransparentTextureOption);
             cutEyesTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutEyesT, activity, "cut_ch_sho_eyes.png", 0, 0);
             cutEyesT.load();
         }
 
         // Fondo:
         if(cutBackgroundTR==null) {
             BitmapTextureAtlas cutBackgroundT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             cutBackgroundTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutBackgroundT, activity, "cut_background.png", 0, 0);
             cutBackgroundT.load();
         }
 
         // Gota:
         if(cutSweatDropTR==null) {
             BitmapTextureAtlas cutSweatDropT = new BitmapTextureAtlas(textureManager, 46, 107,
                     mTransparentTextureOption);
             cutSweatDropTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutSweatDropT, activity, "cut_ch_sweatdrop.png", 0, 0);
             cutSweatDropT.load();
         }
 
         // Espada 1:
         if(cutSwordSparkle1TR==null) {
             BitmapTextureAtlas cutSword1T = new BitmapTextureAtlas(textureManager, 503, 345,
                     mTransparentTextureOption);
             cutSwordSparkle1TR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutSword1T, activity, "cut_sword_sparkle1.png", 0, 0);
             cutSword1T.load();
         }
 
        // Barra power
        if(cutHudBarTR==null) {
            BitmapTextureAtlas cutHudBarT = new BitmapTextureAtlas(textureManager, 240, 120,
                    mTransparentTextureOption);
            cutHudBarTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    cutHudBarT, activity, "cut_hud_bar.png", 0, 0);
            cutHudBarT.load();
        }

        // Cursor power:
        if(cutHudCursorTR==null) {
            BitmapTextureAtlas cutHudCursorT = new BitmapTextureAtlas(textureManager, 59, 52,
                    mTransparentTextureOption);
            cutHudCursorTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                    cutHudCursorT, activity, "cut_hud_cursor.png", 0, 0);
            cutHudCursorT.load();
        }

         // Sonido:
     }
 
     // Liberamos los recursos de la escena de corte:
     public synchronized void unloadCutSceneResources() {
         if(cutShoTR != null) {
             if(cutShoTR.getTexture().isLoadedToHardware()) {
                 cutShoTR.getTexture().unload();
                 cutShoTR = null;
             }
         }
         if(cutTreeTopTR!=null) {
             if(cutTreeTopTR.getTexture().isLoadedToHardware()) {
                 cutTreeTopTR.getTexture().unload();
                 cutTreeTopTR = null;
             }
         }
         if(cutTreeBottomTR!=null) {
             if(cutTreeBottomTR.getTexture().isLoadedToHardware()) {
                 cutTreeBottomTR.getTexture().unload();
                 cutTreeBottomTR = null;
             }
         }
         if(cutCandleTopTR!=null) {
             if(cutCandleTopTR.getTexture().isLoadedToHardware()) {
                 cutCandleTopTR.getTexture().unload();
                 cutCandleTopTR = null;
             }
         }
         if(cutCandleBottomTR!=null) {
             if(cutCandleBottomTR.getTexture().isLoadedToHardware()) {
                 cutCandleBottomTR.getTexture().unload();
                 cutCandleBottomTR = null;
             }
         }
         if(cutCandleLightTR!=null) {
             if(cutCandleLightTR.getTexture().isLoadedToHardware()) {
                 cutCandleLightTR.getTexture().unload();
                 cutCandleLightTR = null;
             }
         }
         if(cutEyesTR!=null) {
             if(cutEyesTR.getTexture().isLoadedToHardware()) {
                 cutEyesTR.getTexture().unload();
                 cutEyesTR = null;
             }
         }
         if(cutBackgroundTR!=null) {
             if(cutBackgroundTR.getTexture().isLoadedToHardware()) {
                 cutBackgroundTR.getTexture().unload();
                 cutBackgroundTR = null;
             }
         }
         if(cutSweatDropTR!=null) {
             if(cutSweatDropTR.getTexture().isLoadedToHardware()) {
                 cutSweatDropTR.getTexture().unload();
                 cutSweatDropTR = null;
             }
         }
         if(cutSwordSparkle1TR!=null) {
             if(cutSwordSparkle1TR.getTexture().isLoadedToHardware()) {
                 cutSwordSparkle1TR.getTexture().unload();
                 cutSwordSparkle1TR = null;
             }
         }
         if(cutSwordSparkle2TR!=null) {
             if(cutSwordSparkle2TR.getTexture().isLoadedToHardware()) {
                 cutSwordSparkle2TR.getTexture().unload();
                 cutSwordSparkle2TR = null;
             }
         }
        if(cutHudBarTR!=null) {
            if(cutHudBarTR.getTexture().isLoadedToHardware()) {
                cutHudBarTR.getTexture().unload();
                cutHudBarTR = null;
            }
        }
        if(cutHudCursorTR!=null) {
            if(cutHudCursorTR.getTexture().isLoadedToHardware()) {
                cutHudCursorTR.getTexture().unload();
                cutHudCursorTR = null;
            }
        }
 
         // Garbage Collector:
         System.gc();
     }
 
     /* Each scene within a game should have a loadTextures method as well
      * as an accompanying unloadTextures method. This way, we can display
      * a loading image during scene swapping, unload the first scene's textures
      * then load the next scenes textures.
      */
     public synchronized void loadGameTextures(Engine pEngine, Context pContext){
         // Set our game assets folder in "assets/gfx/game/"
         //BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
         /* Create the bitmap texture atlas for the sprite's texture
         region */
         BuildableBitmapTextureAtlas mBitmapTextureAtlas =
                 new BuildableBitmapTextureAtlas(pEngine.getTextureManager(), 1548, 332,
                         TextureOptions.BILINEAR);
 
         /* Create the TiledTextureRegion object, passing in the usual
         parameters, as well as the number of rows and columns in our sprite sheet
         for the final two parameters */
         // 6 = nº de imágenes que tiene la animación :D
         mTiledTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                 mBitmapTextureAtlas, pContext, "sprite1.png", 6, 1);
 
         /* Build the bitmap texture atlas */
         try {
             mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                     BitmapTextureAtlas>(0, 0, 0));
         } catch (TextureAtlasBuilderException e) {
             e.printStackTrace();
         }
 
         /* Load the bitmap texture atlas into the device's gpu memory
         */
         mBitmapTextureAtlas.load();
     }
 
     /* All textures should have a method call for unloading once
      * they're no longer needed; ie. a level transition. */
     public synchronized void unloadGameTextures(){
         // call unload to remove the corresponding texture atlas from memory
         BuildableBitmapTextureAtlas mBitmapTextureAtlas =
                 (BuildableBitmapTextureAtlas) mTiledTextureRegion.getTexture();
         mBitmapTextureAtlas.unload();
 
         // ... Continue to unload all textures related to the 'Game' scene
 
         // Once all textures have been unloaded, attempt to invoke the Garbage Collector
         System.gc();
     }
 
     // Se crea un método de load/unload para cada escena:
 
     /* Similar to the loadGameTextures(...) method, except this method will be
      * used to load a different scene's textures
 
     public synchronized void loadMenuTextures(Engine pEngine, Context pContext){
         // Set our menu assets folder in "assets/gfx/menu/"
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
 
         BuildableBitmapTextureAtlas mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
                 pEngine.getTextureManager() ,800 , 480);
 
         mMenuBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 mBitmapTextureAtlas, pContext, "menu_background.png");
 
         try {
             mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                     BitmapTextureAtlas>(0, 1, 1));
             mBitmapTextureAtlas.load();
         } catch (TextureAtlasBuilderException e) {
             Debug.e(e);
         }
     }
 
     // Once again, this method is similar to the 'Game' scene's for unloading
     public synchronized void unloadMenuTextures(){
         // call unload to remove the corresponding texture atlas from memory
         BuildableBitmapTextureAtlas mBitmapTextureAtlas =
                 (BuildableBitmapTextureAtlas) mMenuBackgroundTextureRegion.getTexture();
         mBitmapTextureAtlas.unload();
 
         // ... Continue to unload all textures related to the 'Game' scene
 
         // Once all textures have been unloaded, attempt to invoke the Garbage Collector
         System.gc();
     }*/
 
     /* As with textures, we can create methods to load sound/music objects
      * for different scene's within our games.
      */
     public synchronized void loadSounds(Engine pEngine, Context pContext){
         // Set the SoundFactory's base path
         SoundFactory.setAssetBasePath("sounds/");
          try {
              // Create mSound object via SoundFactory class
              mSound = SoundFactory.createSoundFromAsset(pEngine.getSoundManager(), pContext,
                      "sound.mp3");
          } catch (final IOException e) {
              Log.v("Sounds Load","Exception:" + e.getMessage());
          }
     }
 
     /* In some cases, we may only load one set of sounds throughout
      * our entire game's life-cycle. If that's the case, we may not
      * need to include an unloadSounds() method. Of course, this all
      * depends on how much variance we have in terms of sound
      */
     public synchronized void unloadSounds(){
         // we call the release() method on sounds to remove them from memory
         if(!mSound.isReleased())mSound.release();
     }
 
     /* Lastly, we've got the loadFonts method which, once again,
      * tends to only need to be loaded once as Font's are generally
      * used across an entire game, from menu to shop to game-play.
      */
     public synchronized void loadFonts(Engine pEngine){
         FontFactory.setAssetBasePath("fonts/");
 
         // Small = 64
         fontSmall = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 512, 512, activity.getAssets(), "go3v2.ttf",
                 64f, true, android.graphics.Color.WHITE, 3, android.graphics.Color.RED);
         fontSmall.load();
 
         // Medium = 96
         fontMedium = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 1024, 1024, activity.getAssets(), "go3v2.ttf",
                 96f, true, android.graphics.Color.WHITE, 3, android.graphics.Color.RED);
         fontMedium.load();
 
         // Big = 128
         fontBig = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 1024, 1024, activity.getAssets(), "go3v2.ttf",
                 128f, true, android.graphics.Color.WHITE, 3, android.graphics.Color.RED);
         fontBig.load();
     }
 
     /* If an unloadFonts() method is necessary, we can provide one
      */
     public synchronized void unloadFonts(){
         fontSmall.unload();
         fontMedium.unload();
         fontBig.unload();
     }
 }
