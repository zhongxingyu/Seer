 /*
  * Ninja Trials is an old school style Android Game developed for OUYA & using
  * AndEngine. It features several minigames with simple gameplay.
  * Copyright 2013 Mad Gear Games <madgeargames@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.madgear.ninjatrials.managers;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.util.Log;
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
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
 import org.andengine.opengl.texture.bitmap.BitmapTexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.util.debug.Debug;
 
 import com.madgear.ninjatrials.NinjaTrials;
 
 
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
 
     // MAIN MENU:
     public static ITextureRegion mainTitleTR;
     public static ITextureRegion mainTitlePattern1TR;
 
     // MAIN OPTIONS MENU:
     public static ITextureRegion mainOptionsPatternTR;
     public static ITextureRegion mainOptionsSoundBarsActiveTR;
     public static ITextureRegion mainOptionsSoundBarsInactiveTR;
 
     // CONTROLLER OPTIONS MENU:
     public static ITextureRegion controllerOptionsPatternTR;
     public static ITextureRegion controllerOuyaTR;
     public static ITextureRegion controllerMarksTR;
 
     // HUD:
     public static ITextureRegion hudPowerBarCursorTR;
     public static ITextureRegion hudCursorTR;
     public static ITextureRegion hudPowerBarPushTR;
 
     public static ITextureRegion hudAngleBarCursorTR;
 
     public static ITextureRegion runLineBar;
     public static ITextureRegion runMarkP1;
     public static ITextureRegion runMarkP2;
     public static ITiledTextureRegion runHead;
 
 
     // JUMP TRIAL:
     public static ITextureRegion jumpStatueTR;
     
     // CUT TRIAL:
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
 
     // CUT SCENE SOUNDS:
     public Music cutMusic;
     public Sound cutEyesZoom;
     public Sound cutKatana1;
     public Sound cutKatana2;
     public Sound cutKatana3;
     public Sound cutKatanaWhoosh;
     public Sound cutThud;    
 
 
 	// RUN SCENE
     public static ITiledTextureRegion runSho;
     public static ITiledTextureRegion runRyoko;
     public static ITextureRegion runBgFloor;;
     public static ITextureRegion runBgTreesFront;
     public static ITextureRegion runBgTreesBack;
     public static ITextureRegion runDushStart;
     public static ITextureRegion runDushContinue;
 
 
     
     
     // FONTS:
     public Font fontSmall;        // pequeño
     public Font fontMedium;        // mediano
     public Font fontBig;        // grande
     public Font fontXBig;        // Extra grande
 
 
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
 
     /**
      * Loads the main menu resources.
      */
     public synchronized void loadMainMenuResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
 
         // Main Menu Ninja Trials Logo:
         if(mainTitleTR==null) {
             BitmapTextureAtlas mainTitleT = new BitmapTextureAtlas(
                     textureManager, 756, 495, mTransparentTextureOption);
             mainTitleTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     mainTitleT, activity, "menu_main_title.png", 0, 0);
             mainTitleT.load();
         }
 
         // Main Menu Pattern:
         if (mainTitlePattern1TR == null) {
             BuildableBitmapTextureAtlas mainTitlePattern1T = new BuildableBitmapTextureAtlas(
                     textureManager, 400, 300, TextureOptions.REPEATING_BILINEAR);
             mainTitlePattern1TR = BitmapTextureAtlasTextureRegionFactory
                     .createFromAsset(mainTitlePattern1T, activity, "menu_main_pattern_1.png");
             try {
                 mainTitlePattern1T.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
                 mainTitlePattern1T.load();
             } catch (TextureAtlasBuilderException e) {
                 Debug.e(e);
             }
         }
     }
 
     /**
      * Unloads the main menu resources.
      */
     public synchronized void unloadMainMenuResources() {
         if(mainTitleTR!=null) {
             if(mainTitleTR.getTexture().isLoadedToHardware()) {
                 mainTitleTR.getTexture().unload();
                 mainTitleTR = null;
             }
         }
         if(mainTitlePattern1TR!=null) {
             if(mainTitlePattern1TR.getTexture().isLoadedToHardware()) {
                 mainTitlePattern1TR.getTexture().unload();
                 mainTitlePattern1TR = null;
             }
         }
     }
 
     /**
      * Loads the main option menu resources.
      */
     public synchronized void loadOptionResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
 
         // Sound bars:
         BitmapTextureAtlas mainOptionsSoundBarsT = new BitmapTextureAtlas(textureManager, 575, 220,
                 mTransparentTextureOption);
         ITextureRegion mainOptionsSoundBarsTR = BitmapTextureAtlasTextureRegionFactory.
                 createFromAsset(mainOptionsSoundBarsT, activity, "menu_options_volume.png", 0, 0);
         mainOptionsSoundBarsT.load();
         mainOptionsSoundBarsActiveTR = TextureRegionFactory.
                 extractFromTexture(mainOptionsSoundBarsT, 0, 0, 575, 110, false);
         mainOptionsSoundBarsInactiveTR = TextureRegionFactory.
                 extractFromTexture(mainOptionsSoundBarsT, 0, 111, 575, 109, false);
 
         // Option Menu Pattern:
         if (mainOptionsPatternTR == null) {
             BuildableBitmapTextureAtlas mainOptionsPatternT = new BuildableBitmapTextureAtlas(
                     textureManager, 390, 361, TextureOptions.REPEATING_BILINEAR);
             mainOptionsPatternTR = BitmapTextureAtlasTextureRegionFactory
                     .createFromAsset(mainOptionsPatternT, activity, "menu_main_pattern_2.png");
             try {
                 mainOptionsPatternT.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
                 mainOptionsPatternT.load();
             } catch (TextureAtlasBuilderException e) {
                 Debug.e(e);
             }
         }
     }
 
     /**
      * Unloads the option menu resources.
      */
     public synchronized void unloadOptionResources() {
         if(mainOptionsSoundBarsActiveTR!=null) {
             if(mainOptionsSoundBarsActiveTR.getTexture().isLoadedToHardware()) {
                 mainOptionsSoundBarsActiveTR.getTexture().unload();
                 mainOptionsSoundBarsActiveTR = null;
             }
         }
         if(mainOptionsSoundBarsInactiveTR!=null) {
             if(mainOptionsSoundBarsInactiveTR.getTexture().isLoadedToHardware()) {
                 mainOptionsSoundBarsInactiveTR.getTexture().unload();
                 mainOptionsSoundBarsInactiveTR = null;
             }
         }
         if(mainOptionsPatternTR!=null) {
             if(mainOptionsPatternTR.getTexture().isLoadedToHardware()) {
                 mainOptionsPatternTR.getTexture().unload();
                 mainOptionsPatternTR = null;
             }
         }
     }
 
     /**
      * Loads the main option menu resources.
      *     public static ITextureRegion controllerOuyaTR;
     public static ITextureRegion controllerMarksTR;
      */
     public synchronized void loadControllerOptionResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
 
         // Controller ouya:
         if(controllerOuyaTR==null) {
             BitmapTextureAtlas controllerOuyaT = new BitmapTextureAtlas(textureManager, 1164, 791,
                     mTransparentTextureOption);
             controllerOuyaTR = BitmapTextureAtlasTextureRegionFactory.
                     createFromAsset(
                             controllerOuyaT, activity, "menu_options_controller_ouya.png", 0, 0);
             controllerOuyaT.load();
         }
 
         // Controller marks:
         if(controllerMarksTR==null) {
             BitmapTextureAtlas controllerMarksT = new BitmapTextureAtlas(textureManager, 1195, 717,
                     mTransparentTextureOption);
             controllerMarksTR = BitmapTextureAtlasTextureRegionFactory.
                     createFromAsset(
                             controllerMarksT, activity, "menu_options_controller_marks.png", 0, 0);
             controllerMarksT.load();
         }
 
         // Controller Option Pattern:
         if (controllerOptionsPatternTR == null) {
             BuildableBitmapTextureAtlas controllerOptionsPatternT = new BuildableBitmapTextureAtlas(
                     textureManager, 319, 319, TextureOptions.REPEATING_BILINEAR);
             controllerOptionsPatternTR = BitmapTextureAtlasTextureRegionFactory
                     .createFromAsset(controllerOptionsPatternT, activity,
                             "menu_main_pattern_3.png");
             try {
                 controllerOptionsPatternT.build(
                         new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                             BitmapTextureAtlas>(0, 0, 0));
                 controllerOptionsPatternT.load();
             } catch (TextureAtlasBuilderException e) {
                 Debug.e(e);
             }
         }
     }
 
     /**
      * Unloads the option menu resources.
      */
     public synchronized void unloadControllerOptionResources() {
         if(controllerOuyaTR!=null) {
             if(controllerOuyaTR.getTexture().isLoadedToHardware()) {
                 controllerOuyaTR.getTexture().unload();
                 controllerOuyaTR = null;
             }
         }
         if(controllerMarksTR!=null) {
             if(controllerMarksTR.getTexture().isLoadedToHardware()) {
                 controllerMarksTR.getTexture().unload();
                 controllerMarksTR = null;
             }
         }
         if(controllerOptionsPatternTR!=null) {
             if(controllerOptionsPatternTR.getTexture().isLoadedToHardware()) {
                 controllerOptionsPatternTR.getTexture().unload();
                 controllerOptionsPatternTR = null;
             }
         }
     }
 
     public synchronized void loadHUDResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/hud/");
 
         // Barra power cursor:
         if(hudPowerBarCursorTR==null) {
             BitmapTextureAtlas hudPowerBarCursorT = new BitmapTextureAtlas(
                     textureManager, 240, 120, mTransparentTextureOption);
             hudPowerBarCursorTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     hudPowerBarCursorT, activity, "hud_precision_indicator.png", 0, 0);
             hudPowerBarCursorT.load();
         }
         
         // Angle Bar:
         if (hudAngleBarCursorTR == null) {
         	BitmapTextureAtlas hudAngleBarCursorT = new BitmapTextureAtlas(
                     textureManager, 353, 257, mTransparentTextureOption);
             hudAngleBarCursorTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
             		hudAngleBarCursorT, activity, "hud_angle_indicator.png", 0, 0);
             hudAngleBarCursorT.load();
         }
 
         //TODO: import Cursor angle
         // Cursor:
         if(hudCursorTR==null) {
             BitmapTextureAtlas hudCursorT = new BitmapTextureAtlas(textureManager, 59, 52,
                     mTransparentTextureOption);
             hudCursorTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(hudCursorT,
                     activity, "hud_precision_cursor.png", 0, 0);
             hudCursorT.load();
         }
 
         // Barra power push:
         if(hudPowerBarPushTR==null) {
             BitmapTextureAtlas hudPowerBarPushT = new BitmapTextureAtlas(textureManager, 120, 240,
                     mTransparentTextureOption);
             hudPowerBarPushTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     hudPowerBarPushT, activity, "hud_power_indicator.png", 0, 0);
             hudPowerBarPushT.load();
         }
         // LineBar
         if (runLineBar == null) {
             BitmapTextureAtlas runLineBarBit = new BitmapTextureAtlas(textureManager, 1012, 80,
                     mTransparentTextureOption);
             runLineBar = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     runLineBarBit, activity, "run_line_bar.png", 0, 0);
             runLineBarBit.load();
         }
         // LineMark
         BitmapTextureAtlas runMarkBit = new BitmapTextureAtlas(textureManager, 140, 116,
                 mTransparentTextureOption);
         ITextureRegion runMark = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 runMarkBit, activity, "run_line_mark.png", 0, 0);
         runMarkBit.load();
         if (runMarkP1 == null)
             runMarkP1 = TextureRegionFactory.extractFromTexture(runMarkBit, 0, 0, 70, 116, false);
         if (runMarkP2 == null)
             runMarkP2 = TextureRegionFactory.extractFromTexture(runMarkBit, 70, 0, 140, 116, false);
         // RunHead
         if (runHead == null) {
             BuildableBitmapTextureAtlas runHeadBit = new BuildableBitmapTextureAtlas(
                     textureManager, 660, 440, mTransparentTextureOption);
             runHead = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     runHeadBit, context, "hud_head_run.png", 3, 2);
             try {
                 runHeadBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             runHeadBit.load();
         }
     }
 
     public synchronized void unloadHUDResources() {
         if(hudPowerBarCursorTR!=null) {
             if(hudPowerBarCursorTR.getTexture().isLoadedToHardware()) {
                 hudPowerBarCursorTR.getTexture().unload();
                 hudPowerBarCursorTR = null;
             }
         }
         if(hudAngleBarCursorTR!=null) {
             if(hudAngleBarCursorTR.getTexture().isLoadedToHardware()) {
             	hudAngleBarCursorTR.getTexture().unload();
             	hudAngleBarCursorTR = null;
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
         if (runLineBar != null && runLineBar.getTexture().isLoadedToHardware()) {
                 runLineBar.getTexture().unload();
                 runLineBar = null;
         }
         if (runMarkP1 != null && runMarkP1.getTexture().isLoadedToHardware()) {
                 runMarkP1.getTexture().unload();
                 runMarkP1 = null;
         }
         if (runMarkP2 != null && runMarkP2.getTexture().isLoadedToHardware()) {
                 runMarkP2.getTexture().unload();
                 runMarkP2 = null;
         }
         if (runHead != null && runHead.getTexture().isLoadedToHardware()) {
                 runHead.getTexture().unload();
                 runHead = null;
         }
     }
 
     public synchronized void loadJumpSceneResources() {
     	//Texturas:
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/trial_jump/");
     	
     	//Statue:
     	 BitmapTextureAtlas jumpStatueT = new BitmapTextureAtlas(textureManager, 442, 310,
                  mTransparentTextureOption);
          ITextureRegion jumpStatueAllTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                  jumpStatueT, activity, "jump_bg_1_stone_statues.png", 0, 0);
          jumpStatueT.load();
         // jumpStatueTR = TextureRegionFactory.extractFromTexture(jumpStatueT, 0, 0, 388, 380,
         //         false);
          jumpStatueTR = jumpStatueAllTR;
     }
     
    
    public synchronized void unloadJumpSceneResources() {
        if(jumpStatueTR != null){
            if(jumpStatueTR.getTexture().isLoadedToHardware()) {
                jumpStatueTR.getTexture().unload();
                jumpStatueTR = null;
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
 
         // Músic & Sounds:
         SoundFactory.setAssetBasePath("sounds/");
         MusicFactory.setAssetBasePath("music/");
         try {
             cutEyesZoom = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_eyes_zoom.ogg");
             cutKatana1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_cut1.ogg");
             cutKatana2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_cut2.ogg");
             cutKatana3 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_cut3.ogg");
             cutKatanaWhoosh = SoundFactory.createSoundFromAsset(
                    activity.getSoundManager(), context, "trial_cut_katana_whoosh1.ogg");
             cutThud = SoundFactory.createSoundFromAsset(
                    activity.getSoundManager(), context, "trial_cut_katana_whoosh2.ogg");
             cutMusic = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "trial_cut_music.ogg");
         } catch (final IOException e) {
             Log.v("Sounds Load","Exception:" + e.getMessage());
         }
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
 
         // Sounds:
         if(!cutEyesZoom.isReleased())
             cutEyesZoom.release();
         if(!cutKatana1.isReleased())
             cutKatana1.release();
         if(!cutKatana2.isReleased())
             cutKatana2.release();
         if(!cutKatana3.isReleased())
             cutKatana3.release();
         if(!cutKatanaWhoosh.isReleased())
             cutKatanaWhoosh.release();
         if(!cutEyesZoom.isReleased())
             cutThud.release();
         if(!cutMusic.isReleased())
             cutMusic.release();
 
         // Garbage Collector:
         System.gc();
     }
 
     public synchronized void loadRunSceneResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/trial_run/");
         // Background
         if (runBgFloor == null) {
             BitmapTextureAtlas RunBg1 = new BitmapTextureAtlas(textureManager, 1024, 326,
                     mTransparentTextureOption);
             runBgFloor = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     RunBg1, activity, "run_background_floor.png", 0, 0);
             RunBg1.load();
         }
         if (runBgTreesBack == null) {
             BitmapTextureAtlas RunBg2 = new BitmapTextureAtlas(textureManager, 1021, 510,
                     mTransparentTextureOption);
             runBgTreesBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     RunBg2, activity, "run_background_trees_back.png", 0, 0);
             RunBg2.load();
         }
         if (runBgTreesFront == null) {
             BitmapTextureAtlas RunBg3 = new BitmapTextureAtlas(textureManager, 1024, 754,
                     mTransparentTextureOption);
             runBgTreesFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     RunBg3, activity, "run_background_trees_front.png", 0, 0);
             RunBg3.load();
         }
 
         // Dush
         if (runDushStart == null) {
             BitmapTextureAtlas runDush = new BitmapTextureAtlas(textureManager, 1296, 1080,
                     mTransparentTextureOption);
             runDushStart = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     runDush, activity, "run_dust_start.png", 0, 0);
             runDush.load();
         }
         if (runDushContinue == null) {
             BitmapTextureAtlas runDush = new BitmapTextureAtlas(textureManager, 600, 600,
                     mTransparentTextureOption);
             runDushContinue = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     runDush, activity, "run_dust_continuous.png", 0, 0);
             runDush.load();
         }
 
         // Sho
         if (runSho == null) {
             BuildableBitmapTextureAtlas runShoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 2115, 2028, mTransparentTextureOption);
             runSho = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     runShoBit, context, "run_ch_sho.png", 5, 4);
             try {
                 runShoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             runShoBit.load();
         }
 
         // Ryoko
         if (runRyoko == null) {
             BuildableBitmapTextureAtlas runRyokoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 2115, 2028, mTransparentTextureOption);
             runRyoko = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     runRyokoBit, context, "run_ch_ryoko.png", 5, 4);
             try {
                 runRyokoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             runRyokoBit.load();
         }
     }
 
 
     public synchronized void unloadRunSceneResources() {
         if (runSho != null && runSho.getTexture().isLoadedToHardware()) {
                 runSho.getTexture().unload();
                 runSho = null;
         }
         if (runRyoko != null && runRyoko.getTexture().isLoadedToHardware()) {
                 runRyoko.getTexture().unload();
                 runRyoko = null;
         }
         if (runBgFloor != null && runBgFloor.getTexture().isLoadedToHardware()) {
                 runBgFloor.getTexture().unload();
                 runBgFloor = null;
         }
         if (runBgTreesFront != null && runBgTreesFront.getTexture().isLoadedToHardware()) {
                 runBgTreesFront.getTexture().unload();
                 runBgTreesFront = null;
         }
         if (runBgTreesBack != null && runBgTreesBack.getTexture().isLoadedToHardware()) {
                 runBgTreesBack.getTexture().unload();
                 runBgTreesBack = null;
         }
         if (runDushStart != null && runDushStart.getTexture().isLoadedToHardware()) {
                 runDushStart.getTexture().unload();
                 runDushStart = null;
         }
         if (runDushContinue != null && runDushContinue.getTexture().isLoadedToHardware()) {
                 runDushContinue.getTexture().unload();
                 runDushContinue = null;
         }
         // Garbage Collector:
         System.gc();
     }
 
 
     /* Loads fonts resources
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
 
         // XBig = 192
         fontXBig = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 1024, 1024, activity.getAssets(), "go3v2.ttf",
                 192f, true, android.graphics.Color.WHITE, 3, android.graphics.Color.RED);
         fontXBig.load();
     }
 
     /* If an unloadFonts() method is necessary, we can provide one
      */
     public synchronized void unloadFonts(){
         fontSmall.unload();
         fontMedium.unload();
         fontBig.unload();
         fontXBig.unload();
     }
 }
