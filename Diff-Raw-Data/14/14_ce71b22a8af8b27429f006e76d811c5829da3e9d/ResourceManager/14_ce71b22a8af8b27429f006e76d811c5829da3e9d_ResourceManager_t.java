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
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.util.Log;
 import java.io.IOException;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.audio.sound.Sound;
 import org.andengine.audio.sound.SoundFactory;
 import org.andengine.engine.Engine;
 import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
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
     public static ITextureRegion mainTitle;
     public static ITextureRegion mainTitlePattern1;
 
     // MAIN OPTIONS MENU:
     public static ITextureRegion mainOptionsPattern;
     public static ITextureRegion mainOptionsSoundBarsActive;
     public static ITextureRegion mainOptionsSoundBarsInactive;
 
     // CONTROLLER OPTIONS MENU:
     public static ITextureRegion controllerOptionsPattern;
     public static ITextureRegion controllerOuya;
     public static ITextureRegion controllerMarks;
 
     // HUD:
     public static ITextureRegion hudPowerBarCursor;
     public static ITextureRegion hudCursor;
     public static ITextureRegion hudPowerBarPush;
 
     public static ITextureRegion hudAngleBarCursor;
 
     public static ITextureRegion runLineBar;
     public static ITextureRegion runMarkP1;
     public static ITextureRegion runMarkP2;
     public static ITiledTextureRegion cutHead;
     public static ITiledTextureRegion jumpHead;
     public static ITiledTextureRegion runHead;
     public static ITiledTextureRegion shurikenHead;
 
 
     // JUMP TRIAL:
     public static ITextureRegion jumpBg1Bamboo; // borrable
     public static ITextureRegion jumpBg1BambooTop;
     public static ITextureRegion jumpBg1BambooMiddle;
     public static ITextureRegion jumpBg1BambooBottom;
     
     public static ITextureRegion jumpBg1StoneStatues;
     
     public static ITextureRegion jumpBg2BambooForest1; //borrable
     public static ITextureRegion jumpBg2BambooForest1Top;
     public static ITextureRegion jumpBg2BambooForest1Middle;
     public static ITextureRegion jumpBg2BambooForest1Bottom;
     
     public static ITextureRegion jumpBg3BambooForest2; //borrable
     public static ITextureRegion jumpBg3BambooForest2Top;
     public static ITextureRegion jumpBg3BambooForest2Middle;
     public static ITextureRegion jumpBg3BambooForest2Bottom;
     
     public static ITextureRegion jumpBg4Mount;
     public static ITextureRegion jumpBg5Pagoda;
     public static ITextureRegion jumpBg6Clouds;
     public static ITextureRegion jumpBg7Lake;
     public static ITextureRegion jumpBg8MountFuji;
     public static ITextureRegion jumpBg9Sky;
     public static ITiledTextureRegion jumpChRyoko;
     public static ITiledTextureRegion jumpChSho;
     public static ITiledTextureRegion jumpEffectPreparation;
     public static ITiledTextureRegion jumpEffectWallKick;
 
     // CUT TRIAL:
     public static ITiledTextureRegion cutSho;
     public static ITextureRegion cutTreeTop;
     public static ITextureRegion cutTreeBottom;
     public static ITextureRegion cutCandleTop;
     public static ITextureRegion cutCandleBottom;
     public static ITextureRegion cutCandleLight;
     public static ITextureRegion cutEyes;
     public static ITextureRegion cutBackground;
     public static ITextureRegion cutSweatDrop;
     public static ITiledTextureRegion cutCharSparkle;
     public static ITextureRegion cutSwordSparkle1;
     public static ITiledTextureRegion cutSwordSparkle2;
     public static ITextureRegion cutHudBar;
     public static ITextureRegion cutHudCursor;
 
 
     // CUT SCENE SOUNDS:
     public static Music trialCut;
     public static Sound trialCutEyesZoom;
     public static Sound trialCutKatana1;
     public static Sound trialCutKatana2;
     public static Sound trialCutKatana3;
     public static Sound trialCutKatanaWhoosh;
     public static Sound trialCutKatanaWhoosh2;
 
 
     // RUN SCENE
     public static ITiledTextureRegion runSho;
     public static ITiledTextureRegion runRyoko;
     public static ITextureRegion runBgFloor;
     public static ITextureRegion runBgTreesFront;
     public static ITextureRegion runBgTreesBack;
     public static ITextureRegion runDushStart;
     public static ITextureRegion runDushContinue;
 
     // SHURIKEN SCENE
     public static ITextureRegion shurikenBackground;
     public static ITiledTextureRegion shurikenRyokoHands;
     public static ITiledTextureRegion shurikenRyokoLose;
     public static ITiledTextureRegion shurikenRyokoWin;
     public static ITiledTextureRegion shurikenShoHands;
     public static ITiledTextureRegion shurikenShoLose;
     public static ITiledTextureRegion shurikenShoWin;
     public static ITextureRegion shurikenShuriken;
     public static ITextureRegion[] shurikenShurikens;
     public static ITiledTextureRegion shurikenStrawman1;
     public static ITiledTextureRegion shurikenStrawman2;
     public static ITextureRegion shurikenStrawman3;
     public static ITextureRegion shurikenTempShuriken;
     public static ITextureRegion shurikenTempStrawman;
 
     // HOW TO PLAY
     public static ITextureRegion howToPlayArrow;
     public static ITextureRegion howToPlayButton;
     public static ITextureRegion howToPlayDigitalPad;
 
     // CHARACTER PROFILE
     public static ITextureRegion characterProfileBackground1;
     public static ITextureRegion characterProfileBackground2;
     public static ITextureRegion characterProfileRyoko;
     public static ITextureRegion characterProfileSho;
 
     // MENU ACHIEVEMENTS
     public static ITextureRegion menuAchievementsContainerDescription;
     public static ITextureRegion menuAchievementsContainerIcons;
     public static ITiledTextureRegion menuAchievementsIconsBig;
     public static ITextureRegion menuAchievementsIconsSmall;
     public static ITextureRegion menuAchievementsIngameContainer;
     public static ITextureRegion menuAchievementsSuccessStamp;
     public static ITiledTextureRegion[][] menuAchievementsIconsArray;
     public static ITextureRegion[][] menuAchievementsIconsBigArray;
     public static final int MENU_ACHIEV_COLS = 7;
     public static final int MENU_ACHIEV_ROWS = 5;
     public static final int MENU_ACHIEV_ICON_SIZE = 136;
     public static final int MENU_ACHIEV_ICON_BIG_SIZE = 190;
     public static ITextureRegion menuAchievementsSelectionMark;
     public static final int MENU_ACHIEV_BIG_COLS = 6;
     public static final int MENU_ACHIEV_BIG_ROWS = 6;
 
     // MENU RECORDS
     public static ITextureRegion menuRecordsShoHead;
     public static ITextureRegion menuRecordsShoHeadGold;
     public static ITextureRegion menuRecordsRyokoHead;
     public static ITextureRegion menuRecordsRyokoHeadGold;
 
     // MENU MAP
     public static ITiledTextureRegion menuMapBackgroundMarks;
     public static ITextureRegion menuMapBackground;
     public static ITiledTextureRegion menuMapChRyoko;
     public static ITiledTextureRegion menuMapChSho;
     public static ITiledTextureRegion menuMapDrawings;
     public static ITiledTextureRegion menuMapScroll;
 
     // MENU PAUSE
     public static ITextureRegion menuPauseBambooFrame;
 
     // MENU SELECTED
     public static ITextureRegion menuSelectChRyoko;
     public static ITextureRegion menuSelectChRyokoOutline;
     public static ITextureRegion menuSelectChSho;
     public static ITextureRegion menuSelectChShoOutline;
     public static ITextureRegion menuSelectClouds1;
     public static ITextureRegion menuSelectClouds2;
     public static ITextureRegion menuSelectDifficulty;
     public static ITextureRegion menuSelectMoon;
     public static ITextureRegion menuSelectRoof;
     public static ITextureRegion menuSelectSky;
 
     // RESULTS SCENE LOSE
     public static ITextureRegion loseCharRyoko;
     public static ITextureRegion loseCharSho;
     public static ITextureRegion loseBg;
 
     // RESULTS SCENE LOSE SOUNDS
     public static Music loseMusic;
     public static Sound loseYouLose;
 
 
     // RESULTS SCENE WIN
     public static ITextureRegion winBg;
     public static ITextureRegion winScroll;
     public static ITiledTextureRegion winDrawings;
     public static ITextureRegion winCharSho;
     public static ITextureRegion winCharRyoko;
     public static ITiledTextureRegion winStampRanking;
     public static final int WIN_STAMP_INDEX_THUG = 0;
     public static final int WIN_STAMP_INDEX_NINJA = 1;
     public static final int WIN_STAMP_INDEX_NINJA_MASTER = 2;
     public static final int WIN_STAMP_INDEX_GRAND_MASTER = 3;
 
 
     // RESULTS SCENE WIN SOUNDS
     public static Music winMusic;
     public static Sound winPointsSum;
     public static Sound winYouWin;
     public static Sound winPointsTotal;
 
     // SPLASH INTRO (MADGEAR LOGO)
     public static ITextureRegion splashLogo;
 
     // SPLASH INTRO (MADGEAR LOGO) SOUND
     public static Sound menuLogoMadgear;
 
     // INTRO1
     public static ITextureRegion intro1Gradient;
     public static ITextureRegion intro1Logo;
     public static ITextureRegion intro1Ryoko;
     public static ITextureRegion intro1Shapes;
     public static ITextureRegion intro1Sho;
     public static ITextureRegion intro1TrialCut;
     public static ITextureRegion intro1TrialJump;
     public static ITextureRegion intro1TrialRun;
     public static ITextureRegion intro1TrialShuriken;
     public static ITextureRegion intro1WordmaskNinja;
     public static ITextureRegion intro1WordmaskTrials;
 
     // INTRO2
     public static ITextureRegion intro2CommonBg;
     public static ITiledTextureRegion intro2CommonMaster;
     public static ITextureRegion intro2CommonMasterTextBalloon;
     public static ITiledTextureRegion intro2CommonRyoko;
     public static ITextureRegion intro2CommonRyokoTextBalloon;
     public static ITiledTextureRegion intro2CommonSho;
     public static ITextureRegion intro2CommonShoTextBalloon;
     public static ITextureRegion intro2RyokoBalloonText;
     public static ITextureRegion intro2RyokoBg;
     public static ITextureRegion intro2Ryoko;
     public static ITextureRegion intro2ShoBalloonText;
     public static ITextureRegion intro2ShoBg;
     public static ITextureRegion intro2Sho;
 
 
     // GAME OVER SOUNDS
     public static Music gameOverMusic;
     public static Sound gameOver;
 
     // ENDING (placeholders)
     public static ITextureRegion endingRyokoEasyBg;
     public static ITextureRegion endingRyokoEasy;
     public static ITextureRegion endingShoEasyBg;
     public static ITextureRegion endingShoEasy;
 
     // CREDITS
     public static ITextureRegion endingCreditsBackground;
     public static ITiledTextureRegion endingCreditsCategories;
     public static ITextureRegion endingCreditsLogoAndengine;
     public static ITextureRegion endingCreditsLogoEstudioevergreen;
 
     // FONTS
     public Font fontSmall;        // pequeño
     public Font fontMedium;        // mediano
     public Font fontBig;        // grande
     public Font fontXBig;        // Extra grande
     public Font fontLatinChrName; // CharacterIntroScene
     public Font fontJPChrName; // CharacterIntroScene
     public Font fontLatinChrInfo; // CharacterIntroScene
 
 
     //public BuildableBitmapTextureAtlas mBitmapTextureAtlas;
     public ITiledTextureRegion mTiledTextureRegion;
     //public ITextureRegion mSpriteTextureRegion;
 
     public Music music;
     public Sound mSound;
 
     public float cameraScaleFactorX = 1;
     public float cameraScaleFactorY = 1;
     
     
     // MUSICS
     public static Music credits;
     public static Music ending;
     public static Music intro1;
     public static Music intro2;
     public static Music map;
     public static Music records;
     public static Music trialJump;
     public static Music trialRun;
     public static Music trialShurikens;
 
     // SOUNDS
     public static Sound effectEyeGleam;
     public static Sound effectMasterHit;
     public static Sound effectSweatDrop;
     public static Sound judge1;
     public static Sound judge2;
     public static Sound judge3;
     public static Sound judge4;
     public static Sound judge5;
     public static Sound judge6;
     public static Sound judge7;
     public static Sound judge8;
     public static Sound judge9;
     public static Sound judgeExcellent;
     public static Sound judgeGood;
     public static Sound judgeGo;
     public static Sound judgeGreat;
     public static Sound judgeReady;
     public static Sound menuAchievement;
     public static Sound menuActivate;
     public static Sound menuBack;
     public static Sound menuFocus;
     public static Sound menuIntro1;
     public static Sound menuRank;
     public static Sound ryokoCutCut;
     public static Sound ryokoCutLose;
     public static Sound ryokoCutWin;
     public static Sound ryokoJumpCharge;
     public static Sound ryokoJumpFall;
     public static Sound ryokoJumpHop;
     public static Sound ryokoJumpLose;
     public static Sound ryokoJumpWin;
     public static Sound ryokoMenuContinue;
     public static Sound ryokoMenuGameOver;
     public static Sound ryokoRunCharge;
     public static Sound ryokoRunLose;
     public static Sound ryokoRunStart;
     public static Sound ryokoRunWin;
     public static Sound ryokoShurikenLose;
     public static Sound ryokoShurikenThrow;
     public static Sound ryokoShurikenWin;
     public static Sound shoCutCut;
     public static Sound shoCutLose;
     public static Sound shoCutWin;
     public static Sound shoJumpCharge;
     public static Sound shoJumpFall;
     public static Sound shoJumpHop;
     public static Sound shoJumpLose;
     public static Sound shoJumpWin;
     public static Sound shoMenuContinue;
     public static Sound shoMenuGameOver;
     public static Sound shoRunCharge;
     public static Sound shoRunLose;
     public static Sound shoRunStart;
     public static Sound shoRunWin;
     public static Sound shoShurikenLose;
     public static Sound shoShurikenThrow;
     public static Sound shoShurikenWin;
     public static Sound trialCutCandleBlowOut;
     public static Sound trialCutCandleShowingCut;
     public static Sound trialCutCandleThud;
     public static Sound trialCutCandleWobble;
     public static Sound trialCutCandleWoobleThud;
     public static Sound trialCutEyesZoomV2;
     public static Sound trialCutKatanaWhoosh3;
     public static Sound trialJumpFall;
     public static Sound trialJumpReach;
     public static Sound trialJumpSlip;
     public static Sound trialJumpTap1;
     public static Sound trialJumpTap2;
     public static Sound trialJumpThud;
     public static Sound trialJumpWhoosh1;
     public static Sound trialJumpWhoosh2;
     public static Sound trialJumpWhoosh3;
     public static Sound trialJumpWobble;
     public static Sound trialRunTap1;
     public static Sound trialRunTap2;
     public static Sound trialRunTap3;
     public static Sound trialRunWind1Start;
     public static Sound trialRunWind2Running;
     public static Sound trialRunWind3End;
     public static Sound trialShurikenStrawmanAscend;
     public static Sound trialShurikenStrawmanDescend;
     public static Sound trialShurikenStrawmanDestroyed;
     public static Sound trialShurikenStrawmanHit;
     public static Sound trialShurikenStrawmanMove;
     public static Sound trialShurikenThrowing;
 
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
         if(mainTitle==null) {
             BitmapTextureAtlas mainTitleT = new BitmapTextureAtlas(
                     textureManager, 756, 495, mTransparentTextureOption);
             mainTitle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     mainTitleT, activity, "menu_main_title.png", 0, 0);
             mainTitleT.load();
         }
 
         // Main Menu Pattern:
         if (mainTitlePattern1 == null) {
             BuildableBitmapTextureAtlas mainTitlePattern1T = new BuildableBitmapTextureAtlas(
                     textureManager, 400, 300, TextureOptions.REPEATING_BILINEAR);
             mainTitlePattern1 = BitmapTextureAtlasTextureRegionFactory
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
         if(mainTitle!=null) {
             if(mainTitle.getTexture().isLoadedToHardware()) {
                 mainTitle.getTexture().unload();
                 mainTitle = null;
             }
         }
         if(mainTitlePattern1!=null) {
             if(mainTitlePattern1.getTexture().isLoadedToHardware()) {
                 mainTitlePattern1.getTexture().unload();
                 mainTitlePattern1 = null;
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
         mainOptionsSoundBarsActive = TextureRegionFactory.
                 extractFromTexture(mainOptionsSoundBarsT, 0, 0, 575, 110, false);
         mainOptionsSoundBarsInactive = TextureRegionFactory.
                 extractFromTexture(mainOptionsSoundBarsT, 0, 111, 575, 109, false);
 
         // Option Menu Pattern:
         if (mainOptionsPattern == null) {
             BuildableBitmapTextureAtlas mainOptionsPatternT = new BuildableBitmapTextureAtlas(
                     textureManager, 390, 361, TextureOptions.REPEATING_BILINEAR);
             mainOptionsPattern = BitmapTextureAtlasTextureRegionFactory
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
         if(mainOptionsSoundBarsActive!=null) {
             if(mainOptionsSoundBarsActive.getTexture().isLoadedToHardware()) {
                 mainOptionsSoundBarsActive.getTexture().unload();
                 mainOptionsSoundBarsActive = null;
             }
         }
         if(mainOptionsSoundBarsInactive!=null) {
             if(mainOptionsSoundBarsInactive.getTexture().isLoadedToHardware()) {
                 mainOptionsSoundBarsInactive.getTexture().unload();
                 mainOptionsSoundBarsInactive = null;
             }
         }
         if(mainOptionsPattern!=null) {
             if(mainOptionsPattern.getTexture().isLoadedToHardware()) {
                 mainOptionsPattern.getTexture().unload();
                 mainOptionsPattern = null;
             }
         }
     }
 
     /**
      * Loads the main option menu resources.
      *     public static ITextureRegion controllerOuya;
     public static ITextureRegion controllerMarks;
      */
     public synchronized void loadControllerOptionResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
 
         // Controller ouya:
         if(controllerOuya==null) {
             BitmapTextureAtlas controllerOuyaT = new BitmapTextureAtlas(textureManager, 1164, 791,
                     mTransparentTextureOption);
             controllerOuya = BitmapTextureAtlasTextureRegionFactory.
                     createFromAsset(
                             controllerOuyaT, activity, "menu_options_controller_ouya.png", 0, 0);
             controllerOuyaT.load();
         }
 
         // Controller marks:
         if(controllerMarks==null) {
             BitmapTextureAtlas controllerMarksT = new BitmapTextureAtlas(textureManager, 1195, 717,
                     mTransparentTextureOption);
             controllerMarks = BitmapTextureAtlasTextureRegionFactory.
                     createFromAsset(
                             controllerMarksT, activity, "menu_options_controller_marks.png", 0, 0);
             controllerMarksT.load();
         }
 
         // Controller Option Pattern:
         if (controllerOptionsPattern == null) {
             BuildableBitmapTextureAtlas controllerOptionsPatternT = new BuildableBitmapTextureAtlas(
                     textureManager, 319, 319, TextureOptions.REPEATING_BILINEAR);
             controllerOptionsPattern = BitmapTextureAtlasTextureRegionFactory
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
         if(controllerOuya!=null) {
             if(controllerOuya.getTexture().isLoadedToHardware()) {
                 controllerOuya.getTexture().unload();
                 controllerOuya = null;
             }
         }
         if(controllerMarks!=null) {
             if(controllerMarks.getTexture().isLoadedToHardware()) {
                 controllerMarks.getTexture().unload();
                 controllerMarks = null;
             }
         }
         if(controllerOptionsPattern!=null) {
             if(controllerOptionsPattern.getTexture().isLoadedToHardware()) {
                 controllerOptionsPattern.getTexture().unload();
                 controllerOptionsPattern = null;
             }
         }
     }
 
     public synchronized void loadHUDResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/hud/");
 
         // Barra power cursor:
         if(hudPowerBarCursor==null) {
             BitmapTextureAtlas hudPowerBarCursorT = new BitmapTextureAtlas(
                     textureManager, 240, 120, mTransparentTextureOption);
             hudPowerBarCursor = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     hudPowerBarCursorT, activity, "hud_precision_indicator.png", 0, 0);
             hudPowerBarCursorT.load();
         }
         
         // Angle Bar:
         if (hudAngleBarCursor == null) {
             BitmapTextureAtlas hudAngleBarCursorT = new BitmapTextureAtlas(
                     textureManager, 353, 257, mTransparentTextureOption);
             hudAngleBarCursor = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     hudAngleBarCursorT, activity, "hud_angle_indicator.png", 0, 0);
             hudAngleBarCursorT.load();
         }
 
         if(hudCursor==null) {
             BitmapTextureAtlas hudCursorT = new BitmapTextureAtlas(textureManager, 59, 52,
                     mTransparentTextureOption);
             hudCursor = BitmapTextureAtlasTextureRegionFactory.createFromAsset(hudCursorT,
                     activity, "hud_angle_cursor.png", 0, 0);
             hudCursorT.load();
         }
 
         // Cursor:
         if(hudCursor==null) {
             BitmapTextureAtlas hudCursorT = new BitmapTextureAtlas(textureManager, 59, 52,
                     mTransparentTextureOption);
             hudCursor = BitmapTextureAtlasTextureRegionFactory.createFromAsset(hudCursorT,
                     activity, "hud_precision_cursor.png", 0, 0);
             hudCursorT.load();
         }
 
         // Barra power push:
         if(hudPowerBarPush==null) {
             BitmapTextureAtlas hudPowerBarPushT = new BitmapTextureAtlas(textureManager, 120, 240,
                     mTransparentTextureOption);
             hudPowerBarPush = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
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
         // CutHead
         if (cutHead == null) {
             BuildableBitmapTextureAtlas cutHeadBit = new BuildableBitmapTextureAtlas(
                     textureManager, 660, 440, mTransparentTextureOption);
             cutHead = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     cutHeadBit, context, "hud_head_cut.png", 3, 2);
             try {
                 cutHeadBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             cutHeadBit.load();
         }
         // JumpHead
         if (jumpHead == null) {
             BuildableBitmapTextureAtlas jumpHeadBit = new BuildableBitmapTextureAtlas(
                     textureManager, 660, 440, mTransparentTextureOption);
             jumpHead = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     jumpHeadBit, context, "hud_head_jump.png", 3, 2);
             try {
                 jumpHeadBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             jumpHeadBit.load();
         }
         // ShurikenHead
         if (shurikenHead == null) {
             BuildableBitmapTextureAtlas shurikenHeadBit = new BuildableBitmapTextureAtlas(
                     textureManager, 660, 440, mTransparentTextureOption);
             shurikenHead = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenHeadBit, context, "hud_head_shuriken.png", 3, 2);
             try {
                 shurikenHeadBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenHeadBit.load();
         }
     }
 
     public synchronized void unloadHUDResources() {
         if(hudPowerBarCursor!=null) {
             if(hudPowerBarCursor.getTexture().isLoadedToHardware()) {
                 hudPowerBarCursor.getTexture().unload();
                 hudPowerBarCursor = null;
             }
         }
         if(hudAngleBarCursor!=null) {
             if(hudAngleBarCursor.getTexture().isLoadedToHardware()) {
                 hudAngleBarCursor.getTexture().unload();
                 hudAngleBarCursor = null;
             }
         }
         if(hudCursor!=null) {
             if(hudCursor.getTexture().isLoadedToHardware()) {
                 hudCursor.getTexture().unload();
                 hudCursor = null;
             }
         }
         if(hudPowerBarPush!=null) {
             if(hudPowerBarPush.getTexture().isLoadedToHardware()) {
                 hudPowerBarPush.getTexture().unload();
                 hudPowerBarPush = null;
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
         if (cutHead != null && cutHead.getTexture().isLoadedToHardware()) {
             cutHead.getTexture().unload();
             cutHead = null;
         }
         if (jumpHead != null && jumpHead.getTexture().isLoadedToHardware()) {
             jumpHead.getTexture().unload();
             jumpHead = null;
         }
         if (shurikenHead != null && shurikenHead.getTexture().isLoadedToHardware()) {
             shurikenHead.getTexture().unload();
             shurikenHead = null;
         }
     }
 
     public synchronized void loadJumpSceneResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/trial_jump/");
 
         if (jumpBg1StoneStatues == null) {
             BitmapTextureAtlas jumpBg1StoneStatuesT = new BitmapTextureAtlas(textureManager, 442, 310,
                     mTransparentTextureOption);
             jumpBg1StoneStatues = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg1StoneStatuesT, activity, "jump_bg_1_stone_statues.png", 0, 0);
             jumpBg1StoneStatuesT.load();
         }
 
         if (jumpBg1Bamboo == null) { // borrable si no me equivoco
             BitmapTextureAtlas jumpBg1BambooT = new BitmapTextureAtlas(textureManager, 89, 1080,
                     mTransparentTextureOption);
             jumpBg1Bamboo = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg1BambooT, activity, "jump_bg_1_bamboo.png", 0, 0);
             jumpBg1BambooT.load();
         }
         if (jumpBg2BambooForest1 == null) { // borrable
             BitmapTextureAtlas jumpBg2BambooForest1T = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             jumpBg2BambooForest1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg2BambooForest1T, activity, "jump_bg_2_bamboo_forest_1.png", 0, 0);
             jumpBg2BambooForest1T.load();
         }
         if (jumpBg3BambooForest2 == null) {// borrable
             BitmapTextureAtlas jumpBg3BambooForest2T = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             jumpBg3BambooForest2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg3BambooForest2T, activity, "jump_bg_3_bamboo_forest_2.png", 0, 0);
             jumpBg3BambooForest2T.load();
         }
 
         // Bamboo in which the characters rebound (3 pieces)
          // ^ 91 px
          // | 921 px
          // v 68 px
         BitmapTextureAtlas bTA_Bamboo = new BitmapTextureAtlas(textureManager, 89, 1080,
                 mTransparentTextureOption);
         ITextureRegion iTR_Bamboo = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 bTA_Bamboo, activity, "jump_bg_1_bamboo.png", 0, 0);
         bTA_Bamboo.load();
         jumpBg1BambooTop = TextureRegionFactory.extractFromTexture(bTA_Bamboo, 0, 0, 89, 91);
         jumpBg1BambooMiddle = TextureRegionFactory.extractFromTexture(bTA_Bamboo, 0, 91, 89, 921);
         jumpBg1BambooBottom = TextureRegionFactory.extractFromTexture(bTA_Bamboo, 0, 1012, 89, 68);
 
         // Nearest bamboo forest
          // ^ 44 px
          // | 718 px
          // v 318 px
         BitmapTextureAtlas bTABambooForest1 = new BitmapTextureAtlas(textureManager, 1920, 1080,
                 mTransparentTextureOption);
         ITextureRegion iTRBambooForest1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 bTABambooForest1, activity, "jump_bg_2_bamboo_forest_1.png", 0, 0);
         bTABambooForest1.load();
         jumpBg2BambooForest1Top = TextureRegionFactory.extractFromTexture(bTABambooForest1, 0, 0, 1920, 44);
         jumpBg2BambooForest1Middle = TextureRegionFactory.extractFromTexture(bTABambooForest1, 0, 44, 1920, 718);
         jumpBg2BambooForest1Bottom = TextureRegionFactory.extractFromTexture(bTABambooForest1, 0, 763, 1920, 318);
 
         // Farthest bamboo forest
          // ^ 80 px
          // | 536 px
          // v 464 px
          BitmapTextureAtlas bTABambooForest2 = new BitmapTextureAtlas(textureManager, 1920, 1080,
                 mTransparentTextureOption);
         ITextureRegion iTRBambooForest2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 bTABambooForest2, activity, "jump_bg_3_bamboo_forest_2.png", 0, 0);
         bTABambooForest2.load();
         jumpBg3BambooForest2Top = TextureRegionFactory.extractFromTexture(bTABambooForest2, 0, 0, 1920, 80);
         jumpBg3BambooForest2Middle = TextureRegionFactory.extractFromTexture(bTABambooForest2, 0, 80, 1920, 536);
         jumpBg3BambooForest2Bottom = TextureRegionFactory.extractFromTexture(bTABambooForest2, 0, 80+536, 1920, 464);
 
         if (jumpBg4Mount == null) {
             BitmapTextureAtlas jumpBg4MountT = new BitmapTextureAtlas(textureManager, 1920, 794,
                     mTransparentTextureOption);
             jumpBg4Mount = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg4MountT, activity, "jump_bg_4_mount.png", 0, 0);
             jumpBg4MountT.load();
         }
         if (jumpBg5Pagoda == null) {
             BitmapTextureAtlas jumpBg5PagodaT = new BitmapTextureAtlas(textureManager, 650, 952,
                     mTransparentTextureOption);
             jumpBg5Pagoda = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg5PagodaT, activity, "jump_bg_5_pagoda.png", 0, 0);
             jumpBg5PagodaT.load();
         }
         if (jumpBg6Clouds == null) {
             BitmapTextureAtlas jumpBg6CloudsT = new BitmapTextureAtlas(textureManager, 1920, 503,
                     mTransparentTextureOption);
             jumpBg6Clouds = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg6CloudsT, activity, "jump_bg_6_clouds.png", 0, 0);
             jumpBg6CloudsT.load();
         }
         if (jumpBg7Lake == null) {
             BitmapTextureAtlas jumpBg7LakeT = new BitmapTextureAtlas(textureManager, 1920, 550,
                     mTransparentTextureOption);
             jumpBg7Lake = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg7LakeT, activity, "jump_bg_7_lake.png", 0, 0);
             jumpBg7LakeT.load();
         }
         if (jumpBg8MountFuji == null) {
             BitmapTextureAtlas jumpBg8MountFujiT = new BitmapTextureAtlas(textureManager, 1920, 806,
                     mTransparentTextureOption);
             jumpBg8MountFuji = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg8MountFujiT, activity, "jump_bg_8_mount_fuji.png", 0, 0);
             jumpBg8MountFujiT.load();
         }
         if (jumpBg9Sky == null) {
             BitmapTextureAtlas jumpBg9SkyT = new BitmapTextureAtlas(textureManager, 1920, 1471,
                     mTransparentTextureOption);
             jumpBg9Sky = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     jumpBg9SkyT, activity, "jump_bg_9_sky.png", 0, 0);
             jumpBg9SkyT.load();
         }
 
         //if (jumpChRyoko == null) {
         //   BuildableBitmapTextureAtlas jumpChRyokoBit = new BuildableBitmapTextureAtlas(
         //            textureManager, 2000, 2982, mTransparentTextureOption);
         //    jumpChRyoko = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
         //            jumpChRyokoBit, context, "jump_ch_ryoko.png", 4, 6);
         //    try {
         //        jumpChRyokoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
         //                BitmapTextureAtlas>(0, 0, 0));
         //    }
         //    catch (TextureAtlasBuilderException e) {
         //        e.printStackTrace();
         //    }
         //    jumpChRyokoBit.load();
         //}
 
         if (jumpChSho == null) {
             BuildableBitmapTextureAtlas jumpChShoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 687, 1024, mTransparentTextureOption);
             jumpChSho = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     jumpChShoBit, context, "jump_ch_sho_mini.png", 4, 6);
             try {
                 jumpChShoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             jumpChShoBit.load();
         }
 
         if (jumpEffectPreparation == null) {
             BuildableBitmapTextureAtlas jumpEffectPreparationBit = new BuildableBitmapTextureAtlas(
                     textureManager, 590, 406, mTransparentTextureOption);
             jumpEffectPreparation = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     jumpEffectPreparationBit, context, "jump_effect_preparation.png", 2, 2);
             try {
                 jumpEffectPreparationBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             jumpEffectPreparationBit.load();
         }
 
         //if (jumpEffectWallKick == null) {
         //    BuildableBitmapTextureAtlas jumpEffectWallKickBit = new BuildableBitmapTextureAtlas(
         //            textureManager, 406, 590, mTransparentTextureOption);
         //    jumpEffectWallKick = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
         //            jumpEffectWallKickBit, context, "jump_effect_wall_kick.png", 2, 2);
         //    try {
         //        jumpEffectWallKickBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
         //                BitmapTextureAtlas>(0, 0, 0));
         //    }
         //    catch (TextureAtlasBuilderException e) {
         //        e.printStackTrace();
         //    }
         //    jumpEffectWallKickBit.load();
         //}
     }
 
 
     public synchronized void unloadJumpSceneResources() {
         if (jumpBg1StoneStatues != null && jumpBg1StoneStatues.getTexture().isLoadedToHardware()) {
                 jumpBg1StoneStatues.getTexture().unload();
                 jumpBg1StoneStatues = null;
         }
         if (jumpBg1Bamboo != null && jumpBg1Bamboo.getTexture().isLoadedToHardware()) {
             jumpBg1Bamboo.getTexture().unload();
             jumpBg1Bamboo = null;
         }
         if (jumpBg2BambooForest1 != null && jumpBg2BambooForest1.getTexture().isLoadedToHardware()) {
                 jumpBg2BambooForest1.getTexture().unload();
                 jumpBg2BambooForest1 = null;
         }
         if (jumpBg3BambooForest2 != null && jumpBg3BambooForest2.getTexture().isLoadedToHardware()) {
                 jumpBg3BambooForest2.getTexture().unload();
                 jumpBg3BambooForest2 = null;
         }
 
         // CAMBIOS JJ ******************************* INICIO
         if (jumpBg1BambooTop != null && jumpBg1BambooTop.getTexture().isLoadedToHardware()) {
             jumpBg1BambooTop.getTexture().unload();
             jumpBg1BambooTop = null;
         }
         if (jumpBg1BambooMiddle != null && jumpBg1BambooMiddle.getTexture().isLoadedToHardware()) {
             jumpBg1BambooMiddle.getTexture().unload();
             jumpBg1BambooMiddle = null;
         }
         if (jumpBg1BambooBottom != null && jumpBg1BambooBottom.getTexture().isLoadedToHardware()) {
             jumpBg1BambooBottom.getTexture().unload();
             jumpBg1BambooBottom = null;
         }
         if (jumpBg2BambooForest1Top != null && jumpBg2BambooForest1Top.getTexture().isLoadedToHardware()) {
             jumpBg2BambooForest1Top.getTexture().unload();
             jumpBg2BambooForest1Top = null;
         }
         if (jumpBg2BambooForest1Middle != null && jumpBg2BambooForest1Middle.getTexture().isLoadedToHardware()) {
             jumpBg2BambooForest1Middle.getTexture().unload();
             jumpBg2BambooForest1Middle = null;
         }
         if (jumpBg2BambooForest1Bottom != null && jumpBg2BambooForest1Bottom.getTexture().isLoadedToHardware()) {
             jumpBg2BambooForest1Bottom.getTexture().unload();
             jumpBg2BambooForest1Bottom = null;
         }
         if (jumpBg3BambooForest2Top != null && jumpBg3BambooForest2Top.getTexture().isLoadedToHardware()) {
             jumpBg3BambooForest2Top.getTexture().unload();
             jumpBg3BambooForest2Top = null;
         }
         if (jumpBg3BambooForest2Middle != null && jumpBg3BambooForest2Middle.getTexture().isLoadedToHardware()) {
             jumpBg3BambooForest2Middle.getTexture().unload();
             jumpBg3BambooForest2Middle = null;
         }
         if (jumpBg3BambooForest2Bottom != null && jumpBg3BambooForest2Bottom.getTexture().isLoadedToHardware()) {
             jumpBg3BambooForest2Bottom.getTexture().unload();
             jumpBg3BambooForest2Bottom = null;
         }
         // CAMBIOS JJ ******************************* FIN
 
         if (jumpBg4Mount != null && jumpBg4Mount.getTexture().isLoadedToHardware()) {
                 jumpBg4Mount.getTexture().unload();
                 jumpBg4Mount = null;
         }
         if (jumpBg5Pagoda != null && jumpBg5Pagoda.getTexture().isLoadedToHardware()) {
                 jumpBg5Pagoda.getTexture().unload();
                 jumpBg5Pagoda = null;
         }
         if (jumpBg6Clouds != null && jumpBg6Clouds.getTexture().isLoadedToHardware()) {
                 jumpBg6Clouds.getTexture().unload();
                 jumpBg6Clouds = null;
         }
         if (jumpBg7Lake != null && jumpBg7Lake.getTexture().isLoadedToHardware()) {
                 jumpBg7Lake.getTexture().unload();
                 jumpBg7Lake = null;
         }
         if (jumpBg8MountFuji != null && jumpBg8MountFuji.getTexture().isLoadedToHardware()) {
                 jumpBg8MountFuji.getTexture().unload();
                 jumpBg8MountFuji = null;
         }
         if (jumpBg9Sky != null && jumpBg9Sky.getTexture().isLoadedToHardware()) {
                 jumpBg9Sky.getTexture().unload();
                 jumpBg9Sky = null;
         }
         if (jumpChRyoko != null && jumpChRyoko.getTexture().isLoadedToHardware()) {
                 jumpChRyoko.getTexture().unload();
                 jumpChRyoko = null;
         }
         if (jumpChSho != null && jumpChSho.getTexture().isLoadedToHardware()) {
                 jumpChSho.getTexture().unload();
                 jumpChSho = null;
         }
         if (jumpEffectPreparation != null && jumpEffectPreparation.getTexture().isLoadedToHardware()) {
                 jumpEffectPreparation.getTexture().unload();
                 jumpEffectPreparation = null;
         }
         if (jumpEffectWallKick != null && jumpEffectWallKick.getTexture().isLoadedToHardware()) {
                 jumpEffectWallKick.getTexture().unload();
                 jumpEffectWallKick = null;
         }
     }
 
     // Recursos para la escena de corte:
     public synchronized void loadCutSceneResources() {
         // Texturas:
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/trial_cut/");
 
         // Sho:
         if(cutSho==null) {
             BuildableBitmapTextureAtlas cutShoT = new BuildableBitmapTextureAtlas(
                     textureManager, 1742, 1720, mTransparentTextureOption);
             cutSho = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
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
         cutTreeTop = TextureRegionFactory.extractFromTexture(cutTreeT, 0, 0, 640, 403, false);
         cutTreeBottom = TextureRegionFactory.extractFromTexture(cutTreeT, 0, 404, 640, 546,
                 false);
 
         // Farol:
         BitmapTextureAtlas cutCandleT = new BitmapTextureAtlas(textureManager, 310, 860,
                 mTransparentTextureOption);
         ITextureRegion cutCandleTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 cutCandleT, activity, "cut_breakable_candle_base.png", 0, 0);
         cutCandleT.load();
         cutCandleTop = TextureRegionFactory.extractFromTexture(cutCandleT, 0, 0, 310, 515, false);
         cutCandleBottom = TextureRegionFactory.extractFromTexture(cutCandleT, 0, 516, 310, 344,
                 false);
 
         // Luz del farol:
         BitmapTextureAtlas cutCandleLightT = new BitmapTextureAtlas(textureManager, 760, 380,
                 mTransparentTextureOption);
         ITextureRegion cutCandleLightAllTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 cutCandleLightT, activity, "cut_breakable_candle_light.png", 0, 0);
         cutCandleLightT.load();
         cutCandleLight = TextureRegionFactory.extractFromTexture(cutCandleLightT, 0, 0, 388, 380,
                 false);
 
         // Espada 2:
         if(cutSwordSparkle2==null) {
             BuildableBitmapTextureAtlas cutSword2T = new BuildableBitmapTextureAtlas(
                     textureManager, 1358, 1034, mTransparentTextureOption);
             cutSwordSparkle2 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     cutSword2T, context, "cut_sword_sparkle2.png", 2, 2);
             try {
                 cutSword2T.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             } catch (TextureAtlasBuilderException e) { e.printStackTrace(); }
             cutSword2T.load();
         }
 
         // Ojos:
         if(cutEyes==null) {
             BitmapTextureAtlas cutEyesT =  new BitmapTextureAtlas(textureManager, 1416, 611,
                     mTransparentTextureOption);
             cutEyes = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutEyesT, activity, "cut_ch_sho_eyes.png", 0, 0);
             cutEyesT.load();
         }
 
         // Fondo:
         if(cutBackground==null) {
             BitmapTextureAtlas cutBackgroundT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             cutBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutBackgroundT, activity, "cut_background.png", 0, 0);
             cutBackgroundT.load();
         }
 
         // Gota:
         if(cutSweatDrop==null) {
             BitmapTextureAtlas cutSweatDropT = new BitmapTextureAtlas(textureManager, 46, 107,
                     mTransparentTextureOption);
             cutSweatDrop = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutSweatDropT, activity, "cut_ch_sweatdrop.png", 0, 0);
             cutSweatDropT.load();
         }
 
         // Character eye sparkle:
         if(cutCharSparkle==null) {
             BuildableBitmapTextureAtlas cutCharSparkleT = new BuildableBitmapTextureAtlas(
                     textureManager, 300, 100, mTransparentTextureOption);
             cutCharSparkle = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     cutCharSparkleT, context, "cut_ch_sparkle.png", 3, 1);
             try {
                 cutCharSparkleT.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             } catch (TextureAtlasBuilderException e) { e.printStackTrace(); }
             cutCharSparkleT.load();
         }
 
         // Espada 1:
         if(cutSwordSparkle1==null) {
             BitmapTextureAtlas cutSword1T = new BitmapTextureAtlas(textureManager, 503, 345,
                     mTransparentTextureOption);
             cutSwordSparkle1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     cutSword1T, activity, "cut_sword_sparkle1.png", 0, 0);
             cutSword1T.load();
         }
     }
 
 
     // Liberamos los recursos de la escena de corte:
     public synchronized void unloadCutSceneResources() {
         if(cutSho != null) {
             if(cutSho.getTexture().isLoadedToHardware()) {
                 cutSho.getTexture().unload();
                 cutSho = null;
             }
         }
         if(cutTreeTop!=null) {
             if(cutTreeTop.getTexture().isLoadedToHardware()) {
                 cutTreeTop.getTexture().unload();
                 cutTreeTop = null;
             }
         }
         if(cutTreeBottom!=null) {
             if(cutTreeBottom.getTexture().isLoadedToHardware()) {
                 cutTreeBottom.getTexture().unload();
                 cutTreeBottom = null;
             }
         }
         if(cutCandleTop!=null) {
             if(cutCandleTop.getTexture().isLoadedToHardware()) {
                 cutCandleTop.getTexture().unload();
                 cutCandleTop = null;
             }
         }
         if(cutCandleBottom!=null) {
             if(cutCandleBottom.getTexture().isLoadedToHardware()) {
                 cutCandleBottom.getTexture().unload();
                 cutCandleBottom = null;
             }
         }
         if(cutCandleLight!=null) {
             if(cutCandleLight.getTexture().isLoadedToHardware()) {
                 cutCandleLight.getTexture().unload();
                 cutCandleLight = null;
             }
         }
         if(cutEyes!=null) {
             if(cutEyes.getTexture().isLoadedToHardware()) {
                 cutEyes.getTexture().unload();
                 cutEyes = null;
             }
         }
         if(cutBackground!=null) {
             if(cutBackground.getTexture().isLoadedToHardware()) {
                 cutBackground.getTexture().unload();
                 cutBackground = null;
             }
         }
         if(cutSweatDrop!=null) {
             if(cutSweatDrop.getTexture().isLoadedToHardware()) {
                 cutSweatDrop.getTexture().unload();
                 cutSweatDrop = null;
             }
         }
         if(cutCharSparkle!=null) {
             if(cutCharSparkle.getTexture().isLoadedToHardware()) {
                 cutCharSparkle.getTexture().unload();
                 cutCharSparkle = null;
             }
         }
         if(cutSwordSparkle1!=null) {
             if(cutSwordSparkle1.getTexture().isLoadedToHardware()) {
                 cutSwordSparkle1.getTexture().unload();
                 cutSwordSparkle1 = null;
             }
         }
         if(cutSwordSparkle2!=null) {
             if(cutSwordSparkle2.getTexture().isLoadedToHardware()) {
                 cutSwordSparkle2.getTexture().unload();
                 cutSwordSparkle2 = null;
             }
         }
 
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
 
     public synchronized void loadShurikenSceneResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/trial_shuriken/");
         if (shurikenBackground == null) {
             BitmapTextureAtlas shurikenBackgroundT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             shurikenBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     shurikenBackgroundT, activity, "shuriken_background.png", 0, 0);
             shurikenBackgroundT.load();
         }
 
         if (shurikenRyokoHands == null) {
             BuildableBitmapTextureAtlas shurikenRyokoHandsBit = new BuildableBitmapTextureAtlas(
                     textureManager, 740, 960, mTransparentTextureOption);
             shurikenRyokoHands = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenRyokoHandsBit, context, "shuriken_ryoko_hands.png", 1, 3);
             try {
                 shurikenRyokoHandsBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenRyokoHandsBit.load();
         }
 
         if (shurikenRyokoLose == null) {
             BuildableBitmapTextureAtlas shurikenRyokoLoseBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1500, 978, mTransparentTextureOption);
             shurikenRyokoLose = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenRyokoLoseBit, context, "shuriken_ryoko_lose.png", 2, 1);
             try {
                 shurikenRyokoLoseBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenRyokoLoseBit.load();
         }
 
         if (shurikenRyokoWin == null) {
             BuildableBitmapTextureAtlas shurikenRyokoWinBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1400, 1036, mTransparentTextureOption);
             shurikenRyokoWin = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenRyokoWinBit, context, "shuriken_ryoko_win.png", 2, 1);
             try {
                 shurikenRyokoWinBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenRyokoWinBit.load();
         }
 
         if (shurikenShoHands == null) {
             BuildableBitmapTextureAtlas shurikenShoHandsBit = new BuildableBitmapTextureAtlas(
                     textureManager, 740, 960, mTransparentTextureOption);
             shurikenShoHands = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenShoHandsBit, context, "shuriken_sho_hands.png", 1, 3);
             try {
                 shurikenShoHandsBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenShoHandsBit.load();
         }
 
         if (shurikenShoLose == null) {
             BuildableBitmapTextureAtlas shurikenShoLoseBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1500, 978, mTransparentTextureOption);
             shurikenShoLose = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenShoLoseBit, context, "shuriken_sho_lose.png", 2, 1);
             try {
                 shurikenShoLoseBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenShoLoseBit.load();
         }
 
         if (shurikenShoWin == null) {
             BuildableBitmapTextureAtlas shurikenShoWinBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1400, 1036, mTransparentTextureOption);
             shurikenShoWin = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenShoWinBit, context, "shuriken_sho_win.png", 2, 1);
             try {
                 shurikenShoWinBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenShoWinBit.load();
         }
 
         if (shurikenShuriken == null) {
             BitmapTextureAtlas shurikenShurikenT = new BitmapTextureAtlas(textureManager, 196, 418,
                     mTransparentTextureOption);
             shurikenShuriken = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     shurikenShurikenT, activity, "shuriken_shuriken.png", 0, 0);
             shurikenShurikenT.load();
             shurikenShurikens = new ITextureRegion[6];
             shurikenShurikens[0] = TextureRegionFactory.extractFromTexture(shurikenShurikenT, 0, 0, 54, 26-0, false);
             shurikenShurikens[1] = TextureRegionFactory.extractFromTexture(shurikenShurikenT, 0, 29, 58, 66-29, false);
             shurikenShurikens[2] = TextureRegionFactory.extractFromTexture(shurikenShurikenT, 0, 67, 102, 121-67, false);
             shurikenShurikens[3] = TextureRegionFactory.extractFromTexture(shurikenShurikenT, 0, 122, 114, 195-122, false);
             shurikenShurikens[4] = TextureRegionFactory.extractFromTexture(shurikenShurikenT, 0, 197, 196, 303-197, false);
             shurikenShurikens[5] = TextureRegionFactory.extractFromTexture(shurikenShurikenT, 0, 304, 196, 418-304, false);
         }
 
         if (shurikenStrawman1 == null) {
             BuildableBitmapTextureAtlas shurikenStrawman1Bit = new BuildableBitmapTextureAtlas(
                     textureManager, 1023, 640, mTransparentTextureOption);
             shurikenStrawman1 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenStrawman1Bit, context, "shuriken_strawman_1.png", 3, 1);
             try {
                 shurikenStrawman1Bit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenStrawman1Bit.load();
         }
 
         if (shurikenStrawman2 == null) {
             BuildableBitmapTextureAtlas shurikenStrawman2Bit = new BuildableBitmapTextureAtlas(
                     textureManager, 1688, 1056, mTransparentTextureOption);
             shurikenStrawman2 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     shurikenStrawman2Bit, context, "shuriken_strawman_2.png", 3, 1);
             try {
                 shurikenStrawman2Bit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             shurikenStrawman2Bit.load();
         }
         if (shurikenStrawman3 == null) {
             BitmapTextureAtlas shurikenStrawman3T = new BitmapTextureAtlas(textureManager, 1068, 1635,
                     mTransparentTextureOption);
             shurikenStrawman3 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     shurikenStrawman3T, activity, "shuriken_strawman_3.png", 0, 0);
             shurikenStrawman3T.load();
         }
     }
 
     public synchronized void unloadShurikenSceneResources() {
         if (shurikenBackground != null && shurikenBackground.getTexture().isLoadedToHardware()) {
                 shurikenBackground.getTexture().unload();
                 shurikenBackground = null;
         }
         if (shurikenRyokoHands != null && shurikenRyokoHands.getTexture().isLoadedToHardware()) {
                 shurikenRyokoHands.getTexture().unload();
                 shurikenRyokoHands = null;
         }
         if (shurikenRyokoLose != null && shurikenRyokoLose.getTexture().isLoadedToHardware()) {
                 shurikenRyokoLose.getTexture().unload();
                 shurikenRyokoLose = null;
         }
         if (shurikenRyokoWin != null && shurikenRyokoWin.getTexture().isLoadedToHardware()) {
                 shurikenRyokoWin.getTexture().unload();
                 shurikenRyokoWin = null;
         }
         if (shurikenShoHands != null && shurikenShoHands.getTexture().isLoadedToHardware()) {
                 shurikenShoHands.getTexture().unload();
                 shurikenShoHands = null;
         }
         if (shurikenShoLose != null && shurikenShoLose.getTexture().isLoadedToHardware()) {
                 shurikenShoLose.getTexture().unload();
                 shurikenShoLose = null;
         }
         if (shurikenShoWin != null && shurikenShoWin.getTexture().isLoadedToHardware()) {
                 shurikenShoWin.getTexture().unload();
                 shurikenShoWin = null;
         }
         if (shurikenShuriken != null && shurikenShuriken.getTexture().isLoadedToHardware()) {
                 shurikenShuriken.getTexture().unload();
                 shurikenShuriken = null;
         }
         if (shurikenShurikens != null) {
             for (ITextureRegion shuriken: shurikenShurikens) {
                 if (shuriken != null && shuriken.getTexture().isLoadedToHardware()) {
                     shuriken.getTexture().unload();
                     shuriken = null;
                 }
             }
             shurikenShurikens = null;
         }
         if (shurikenStrawman1 != null && shurikenStrawman1.getTexture().isLoadedToHardware()) {
                 shurikenStrawman1.getTexture().unload();
                 shurikenStrawman1 = null;
         }
         if (shurikenStrawman2 != null && shurikenStrawman2.getTexture().isLoadedToHardware()) {
                 shurikenStrawman2.getTexture().unload();
                 shurikenStrawman2 = null;
         }
         if (shurikenStrawman3 != null && shurikenStrawman3.getTexture().isLoadedToHardware()) {
                 shurikenStrawman3.getTexture().unload();
                 shurikenStrawman3 = null;
         }
         if (shurikenTempShuriken != null && shurikenTempShuriken.getTexture().isLoadedToHardware()) {
                 shurikenTempShuriken.getTexture().unload();
                 shurikenTempShuriken = null;
         }
         if (shurikenTempStrawman != null && shurikenTempStrawman.getTexture().isLoadedToHardware()) {
                 shurikenTempStrawman.getTexture().unload();
                 shurikenTempStrawman = null;
         }
     }
 
     /**
      * Loads the splash intro resources.
      */
     public synchronized void loadSplashIntroResources() {
         // Texture of MadGear SVG Logo
         SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/splash/");
         if (splashLogo == null){
             BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas;
             mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
                     textureManager, 1024, 1024, TextureOptions.NEAREST);
             splashLogo = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     mBuildableBitmapTextureAtlas, context, "splash_logo_madgear.svg", 800, 800);
             try {
                     mBuildableBitmapTextureAtlas.build(
                             new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                             BitmapTextureAtlas>(0, 1, 0));
                     mBuildableBitmapTextureAtlas.load();
             } catch (final TextureAtlasBuilderException e) {
                     Debug.e(e);
             }
         }
     }
 
     /**
      * Unloads the splash intro resources.
      */
     public synchronized void unloadSplashIntroResources() {
         // Texture of MadGear SVG logo
         if(splashLogo!=null) {
             if(splashLogo.getTexture().isLoadedToHardware()) {
                 splashLogo.getTexture().unload();
                 splashLogo = null;
             }
         }
     }
 
     public synchronized void loadIntro1Resources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/intro_1/");
 
         if (intro1Gradient == null) {
             BitmapTextureAtlas intro1GradientT = new BitmapTextureAtlas(textureManager, 1900, 1651,
                     mTransparentTextureOption);
             intro1Gradient = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1GradientT, activity, "intro1_gradient.jpg", 0, 0);
             intro1GradientT.load();
         }
         if (intro1Logo == null) {
             BitmapTextureAtlas intro1LogoT = new BitmapTextureAtlas(textureManager, 756, 495,
                     mTransparentTextureOption);
             intro1Logo = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1LogoT, activity, "intro1_logo.png", 0, 0);
             intro1LogoT.load();
         }
         if (intro1Ryoko == null) {
             BitmapTextureAtlas intro1RyokoT = new BitmapTextureAtlas(textureManager, 706, 1563,
                     mTransparentTextureOption);
             intro1Ryoko = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1RyokoT, activity, "intro1_ryoko.png", 0, 0);
             intro1RyokoT.load();
         }
         if (intro1Shapes == null) {
             BitmapTextureAtlas intro1ShapesT = new BitmapTextureAtlas(textureManager, 1900, 1651,
                     mTransparentTextureOption);
             intro1Shapes = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1ShapesT, activity, "intro1_shapes.png", 0, 0);
             intro1ShapesT.load();
         }
         if (intro1Sho == null) {
             BitmapTextureAtlas intro1ShoT = new BitmapTextureAtlas(textureManager, 981, 1734,
                     mTransparentTextureOption);
             intro1Sho = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1ShoT, activity, "intro1_sho.png", 0, 0);
             intro1ShoT.load();
         }
         if (intro1TrialCut == null) {
             BitmapTextureAtlas intro1TrialCutT = new BitmapTextureAtlas(textureManager, 1260, 641,
                     mTransparentTextureOption);
             intro1TrialCut = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1TrialCutT, activity, "intro1_trial_cut.jpg", 0, 0);
             intro1TrialCutT.load();
         }
         if (intro1TrialJump == null) {
             BitmapTextureAtlas intro1TrialJumpT = new BitmapTextureAtlas(textureManager, 1240, 637,
                     mTransparentTextureOption);
             intro1TrialJump = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1TrialJumpT, activity, "intro1_trial_jump.jpg", 0, 0);
             intro1TrialJumpT.load();
         }
         if (intro1TrialRun == null) {
             BitmapTextureAtlas intro1TrialRunT = new BitmapTextureAtlas(textureManager, 1258, 643,
                     mTransparentTextureOption);
             intro1TrialRun = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1TrialRunT, activity, "intro1_trial_run.jpg", 0, 0);
             intro1TrialRunT.load();
         }
         if (intro1TrialShuriken == null) {
             BitmapTextureAtlas intro1TrialThrowT = new BitmapTextureAtlas(textureManager, 1242, 643,
                     mTransparentTextureOption);
             intro1TrialShuriken = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro1TrialThrowT, activity, "intro1_trial_shuriken.jpg", 0, 0);
             intro1TrialThrowT.load();
         }
         // SVG images
         SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/intro_1/");
         if (intro1WordmaskNinja == null){
             BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas;
             mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
                     textureManager, 1900, 1080, TextureOptions.NEAREST);
             intro1WordmaskNinja = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     mBuildableBitmapTextureAtlas, context, "intro1_wordmask_ninja.svg", 1800, 1080);
             try {
                     mBuildableBitmapTextureAtlas.build(
                             new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                             BitmapTextureAtlas>(0, 1, 0));
                     mBuildableBitmapTextureAtlas.load();
             } catch (final TextureAtlasBuilderException e) {
                     Debug.e(e);
             }
         }
 
         if (intro1WordmaskTrials == null){
             BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas;
             mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
                     textureManager, 1400, 1024, TextureOptions.NEAREST);
             intro1WordmaskTrials = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     mBuildableBitmapTextureAtlas, context, "intro1_wordmask_trials.svg", 1400, 800);
             try {
                     mBuildableBitmapTextureAtlas.build(
                             new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                             BitmapTextureAtlas>(0, 1, 0));
                     mBuildableBitmapTextureAtlas.load();
             } catch (final TextureAtlasBuilderException e) {
                     Debug.e(e);
             }
         }
     }
 
     public synchronized void unloadIntro1Resources() {
         if (intro1Gradient != null && intro1Gradient.getTexture().isLoadedToHardware()) {
                 intro1Gradient.getTexture().unload();
                 intro1Gradient = null;
         }
         if (intro1Logo != null && intro1Logo.getTexture().isLoadedToHardware()) {
                 intro1Logo.getTexture().unload();
                 intro1Logo = null;
         }
         if (intro1Ryoko != null && intro1Ryoko.getTexture().isLoadedToHardware()) {
                 intro1Ryoko.getTexture().unload();
                 intro1Ryoko = null;
         }
         if (intro1Shapes != null && intro1Shapes.getTexture().isLoadedToHardware()) {
                 intro1Shapes.getTexture().unload();
                 intro1Shapes = null;
         }
         if (intro1Sho != null && intro1Sho.getTexture().isLoadedToHardware()) {
                 intro1Sho.getTexture().unload();
                 intro1Sho = null;
         }
         if (intro1TrialCut != null && intro1TrialCut.getTexture().isLoadedToHardware()) {
                 intro1TrialCut.getTexture().unload();
                 intro1TrialCut = null;
         }
         if (intro1TrialJump != null && intro1TrialJump.getTexture().isLoadedToHardware()) {
                 intro1TrialJump.getTexture().unload();
                 intro1TrialJump = null;
         }
         if (intro1TrialRun != null && intro1TrialRun.getTexture().isLoadedToHardware()) {
                 intro1TrialRun.getTexture().unload();
                 intro1TrialRun = null;
         }
         if (intro1TrialShuriken != null && intro1TrialShuriken.getTexture().isLoadedToHardware()) {
                 intro1TrialShuriken.getTexture().unload();
                 intro1TrialShuriken = null;
         }
         if (intro1WordmaskNinja != null && intro1WordmaskNinja.getTexture().isLoadedToHardware()) {
             intro1WordmaskNinja.getTexture().unload();
             intro1WordmaskNinja = null;
         }
         if (intro1WordmaskTrials != null && intro1WordmaskTrials.getTexture().isLoadedToHardware()) {
             intro1WordmaskTrials.getTexture().unload();
             intro1WordmaskTrials = null;
         }
         // Garbage Collector:
         System.gc();
     }
 
     public synchronized void loadIntro2Resources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/intro_2/");
 
         if (intro2CommonBg == null) {
             BitmapTextureAtlas intro2CommonBgT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             intro2CommonBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2CommonBgT, activity, "intro2_common_bg.png", 0, 0);
             intro2CommonBgT.load();
         }
 
         if (intro2CommonMaster == null) {
             BuildableBitmapTextureAtlas intro2CommonMasterBit = new BuildableBitmapTextureAtlas(
                     textureManager, 698, 374, mTransparentTextureOption);
             intro2CommonMaster = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     intro2CommonMasterBit, context, "intro2_common_master.png", 2, 1);
             try {
                 intro2CommonMasterBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             intro2CommonMasterBit.load();
         }
         if (intro2CommonMasterTextBalloon == null) {
             BitmapTextureAtlas intro2CommonMasterTextBalloonT = new BitmapTextureAtlas(textureManager, 502, 236,
                     mTransparentTextureOption);
             intro2CommonMasterTextBalloon = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2CommonMasterTextBalloonT, activity, "intro2_common_master_text_balloon.png", 0, 0);
             intro2CommonMasterTextBalloonT.load();
         }
 
         if (intro2CommonRyoko == null) {
             BuildableBitmapTextureAtlas intro2CommonRyokoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 586, 764, mTransparentTextureOption);
             intro2CommonRyoko = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     intro2CommonRyokoBit, context, "intro2_common_ryoko.png", 1, 2);
             try {
                 intro2CommonRyokoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             intro2CommonRyokoBit.load();
         }
         if (intro2CommonRyokoTextBalloon == null) {
             BitmapTextureAtlas intro2CommonRyokoTextBalloonT = new BitmapTextureAtlas(textureManager, 598, 436,
                     mTransparentTextureOption);
             intro2CommonRyokoTextBalloon = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2CommonRyokoTextBalloonT, activity, "intro2_common_ryoko_text_balloon.png", 0, 0);
             intro2CommonRyokoTextBalloonT.load();
         }
 
         if (intro2CommonSho == null) {
             BuildableBitmapTextureAtlas intro2CommonShoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 586, 764, mTransparentTextureOption);
             intro2CommonSho = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     intro2CommonShoBit, context, "intro2_common_sho.png", 1, 2);
             try {
                 intro2CommonShoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             intro2CommonShoBit.load();
         }
         if (intro2CommonShoTextBalloon == null) {
             BitmapTextureAtlas intro2CommonShoTextBalloonT = new BitmapTextureAtlas(textureManager, 598, 436,
                     mTransparentTextureOption);
             intro2CommonShoTextBalloon = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2CommonShoTextBalloonT, activity, "intro2_common_sho_text_balloon.png", 0, 0);
             intro2CommonShoTextBalloonT.load();
         }
         if (intro2RyokoBalloonText == null) {
             BitmapTextureAtlas intro2RyokoBalloonTextT = new BitmapTextureAtlas(textureManager, 987, 505,
                     mTransparentTextureOption);
             intro2RyokoBalloonText = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2RyokoBalloonTextT, activity, "intro2_ryoko_balloon_text.png", 0, 0);
             intro2RyokoBalloonTextT.load();
         }
         if (intro2RyokoBg == null) {
             BitmapTextureAtlas intro2RyokoBgT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             intro2RyokoBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2RyokoBgT, activity, "intro2_ryoko_bg.png", 0, 0);
             intro2RyokoBgT.load();
         }
         if (intro2Ryoko == null) {
             BitmapTextureAtlas intro2RyokoT = new BitmapTextureAtlas(textureManager, 633, 989,
                     mTransparentTextureOption);
             intro2Ryoko = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2RyokoT, activity, "intro2_ryoko.png", 0, 0);
             intro2RyokoT.load();
         }
         if (intro2ShoBalloonText == null) {
             BitmapTextureAtlas intro2ShoBalloonTextT = new BitmapTextureAtlas(textureManager, 987, 505,
                     mTransparentTextureOption);
             intro2ShoBalloonText = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2ShoBalloonTextT, activity, "intro2_sho_balloon_text.png", 0, 0);
             intro2ShoBalloonTextT.load();
         }
         if (intro2ShoBg == null) {
             BitmapTextureAtlas intro2ShoBgT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             intro2ShoBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2ShoBgT, activity, "intro2_sho_bg.png", 0, 0);
             intro2ShoBgT.load();
         }
         if (intro2Sho == null) {
             BitmapTextureAtlas intro2ShoT = new BitmapTextureAtlas(textureManager, 813, 1049,
                     mTransparentTextureOption);
             intro2Sho = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     intro2ShoT, activity, "intro2_sho.png", 0, 0);
             intro2ShoT.load();
         }
     }
 
 
     public synchronized void unloadIntro2Resources() {
         if (intro2CommonBg != null && intro2CommonBg.getTexture().isLoadedToHardware()) {
                 intro2CommonBg.getTexture().unload();
                 intro2CommonBg = null;
         }
         if (intro2CommonMaster != null && intro2CommonMaster.getTexture().isLoadedToHardware()) {
                 intro2CommonMaster.getTexture().unload();
                 intro2CommonMaster = null;
         }
         if (intro2CommonMasterTextBalloon != null && intro2CommonMasterTextBalloon.getTexture().isLoadedToHardware()) {
                 intro2CommonMasterTextBalloon.getTexture().unload();
                 intro2CommonMasterTextBalloon = null;
         }
         if (intro2CommonRyoko != null && intro2CommonRyoko.getTexture().isLoadedToHardware()) {
                 intro2CommonRyoko.getTexture().unload();
                 intro2CommonRyoko = null;
         }
         if (intro2CommonRyokoTextBalloon != null && intro2CommonRyokoTextBalloon.getTexture().isLoadedToHardware()) {
                 intro2CommonRyokoTextBalloon.getTexture().unload();
                 intro2CommonRyokoTextBalloon = null;
         }
         if (intro2CommonSho != null && intro2CommonSho.getTexture().isLoadedToHardware()) {
                 intro2CommonSho.getTexture().unload();
                 intro2CommonSho = null;
         }
         if (intro2CommonShoTextBalloon != null && intro2CommonShoTextBalloon.getTexture().isLoadedToHardware()) {
                 intro2CommonShoTextBalloon.getTexture().unload();
                 intro2CommonShoTextBalloon = null;
         }
         if (intro2RyokoBalloonText != null && intro2RyokoBalloonText.getTexture().isLoadedToHardware()) {
                 intro2RyokoBalloonText.getTexture().unload();
                 intro2RyokoBalloonText = null;
         }
         if (intro2RyokoBg != null && intro2RyokoBg.getTexture().isLoadedToHardware()) {
                 intro2RyokoBg.getTexture().unload();
                 intro2RyokoBg = null;
         }
         if (intro2Ryoko != null && intro2Ryoko.getTexture().isLoadedToHardware()) {
                 intro2Ryoko.getTexture().unload();
                 intro2Ryoko = null;
         }
         if (intro2ShoBalloonText != null && intro2ShoBalloonText.getTexture().isLoadedToHardware()) {
                 intro2ShoBalloonText.getTexture().unload();
                 intro2ShoBalloonText = null;
         }
         if (intro2ShoBg != null && intro2ShoBg.getTexture().isLoadedToHardware()) {
                 intro2ShoBg.getTexture().unload();
                 intro2ShoBg = null;
         }
         if (intro2Sho != null && intro2Sho.getTexture().isLoadedToHardware()) {
                 intro2Sho.getTexture().unload();
                 intro2Sho = null;
         }
         // Garbage Collector:
         System.gc();
     }
 
     // TODO Divide into several (probably 9) loadEndingResources() and one loadCreditsResources()
     public synchronized void loadEndingResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/endings/");
         if (endingCreditsBackground == null) {
             BitmapTextureAtlas endingCreditsBackgroundT = new BitmapTextureAtlas(textureManager,
                     1920, 1080, mTransparentTextureOption);
             endingCreditsBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingCreditsBackgroundT, activity, "ending_credits_background.png", 0, 0);
             endingCreditsBackgroundT.load();
         }
         if (endingCreditsCategories == null) {
             BuildableBitmapTextureAtlas endingCredCategBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1200, 1020, mTransparentTextureOption);
             endingCreditsCategories = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     endingCredCategBit, context, "ending_credits_categories.png", 3, 3);
             try {
                 endingCredCategBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             endingCredCategBit.load();
         }
         if (endingCreditsLogoAndengine == null) {
             BitmapTextureAtlas endingCreditsLogoAndengineT = new BitmapTextureAtlas(textureManager, 389, 389,
                     mTransparentTextureOption);
             endingCreditsLogoAndengine = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingCreditsLogoAndengineT, activity, "ending_credits_logo_andengine.png", 0, 0);
             endingCreditsLogoAndengineT.load();
         }
         if (endingCreditsLogoEstudioevergreen == null) {
             BitmapTextureAtlas endingCreditsLogoEstudioevergreenT = new BitmapTextureAtlas(textureManager, 389, 389,
                     mTransparentTextureOption);
             endingCreditsLogoEstudioevergreen = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingCreditsLogoEstudioevergreenT, activity, "ending_credits_logo_estudioevergreen.png", 0, 0);
             endingCreditsLogoEstudioevergreenT.load();
         }
         if (endingRyokoEasyBg == null) {
             BitmapTextureAtlas endingRyokoEasyBgT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             endingRyokoEasyBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingRyokoEasyBgT, activity, "ending_ryoko_easy_bg.png", 0, 0);
             endingRyokoEasyBgT.load();
         }
         if (endingRyokoEasy == null) {
             BitmapTextureAtlas endingRyokoEasyT = new BitmapTextureAtlas(textureManager, 633, 989,
                     mTransparentTextureOption);
             endingRyokoEasy = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingRyokoEasyT, activity, "ending_ryoko_easy.png", 0, 0);
             endingRyokoEasyT.load();
         }
         if (endingShoEasyBg == null) {
             BitmapTextureAtlas endingShoEasyBgT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             endingShoEasyBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingShoEasyBgT, activity, "ending_sho_easy_bg.png", 0, 0);
             endingShoEasyBgT.load();
         }
         if (endingShoEasy == null) {
             BitmapTextureAtlas endingShoEasyT = new BitmapTextureAtlas(textureManager, 813, 1049,
                     mTransparentTextureOption);
             endingShoEasy = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     endingShoEasyT, activity, "ending_sho_easy.png", 0, 0);
             endingShoEasyT.load();
         }
     }
 
     public synchronized void unloadEndingResources() {
         if (endingCreditsBackground != null && endingCreditsBackground.getTexture().isLoadedToHardware()) {
                 endingCreditsBackground.getTexture().unload();
                 endingCreditsBackground = null;
         }
         if (endingCreditsCategories != null && endingCreditsCategories.getTexture().isLoadedToHardware()) {
                 endingCreditsCategories.getTexture().unload();
                 endingCreditsCategories = null;
         }
         if (endingCreditsLogoAndengine != null && endingCreditsLogoAndengine.getTexture().isLoadedToHardware()) {
                 endingCreditsLogoAndengine.getTexture().unload();
                 endingCreditsLogoAndengine = null;
         }
         if (endingCreditsLogoEstudioevergreen != null && endingCreditsLogoEstudioevergreen.getTexture().isLoadedToHardware()) {
                 endingCreditsLogoEstudioevergreen.getTexture().unload();
                 endingCreditsLogoEstudioevergreen = null;
         }
         if (endingRyokoEasyBg != null && endingRyokoEasyBg.getTexture().isLoadedToHardware()) {
                 endingRyokoEasyBg.getTexture().unload();
                 endingRyokoEasyBg = null;
         }
         if (endingRyokoEasy != null && endingRyokoEasy.getTexture().isLoadedToHardware()) {
                 endingRyokoEasy.getTexture().unload();
                 endingRyokoEasy = null;
         }
         if (endingShoEasyBg != null && endingShoEasyBg.getTexture().isLoadedToHardware()) {
                 endingShoEasyBg.getTexture().unload();
                 endingShoEasyBg = null;
         }
         if (endingShoEasy != null && endingShoEasy.getTexture().isLoadedToHardware()) {
                 endingShoEasy.getTexture().unload();
                 endingShoEasy = null;
         }
         // Garbage Collector:
         System.gc();
     }
 
     public synchronized void loadHowToPlayResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         if (howToPlayArrow == null) {
             BitmapTextureAtlas howToPlayArrowT = new BitmapTextureAtlas(textureManager, 149, 203,
                     mTransparentTextureOption);
             howToPlayArrow = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     howToPlayArrowT, activity, "how_to_play_arrow.png", 0, 0);
             howToPlayArrowT.load();
         }
 
         if (howToPlayButton == null) {
             BitmapTextureAtlas howToPlayButtonT = new BitmapTextureAtlas(textureManager, 182, 254,
                     mTransparentTextureOption);
             howToPlayButton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     howToPlayButtonT, activity, "how_to_play_button.png", 0, 0);
             howToPlayButtonT.load();
         }
 
         if (howToPlayDigitalPad == null) {
             BitmapTextureAtlas howToPlayDigitalPadT = new BitmapTextureAtlas(textureManager, 471, 334,
                     mTransparentTextureOption);
             howToPlayDigitalPad = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     howToPlayDigitalPadT, activity, "how_to_play_digital_pad.png", 0, 0);
             howToPlayDigitalPadT.load();
         }
     }
 
     public synchronized void unloadHowToPlayResources() {
         if (howToPlayArrow != null && howToPlayArrow.getTexture().isLoadedToHardware()) {
                 howToPlayArrow.getTexture().unload();
                 howToPlayArrow = null;
         }
 
         if (howToPlayButton != null && howToPlayButton.getTexture().isLoadedToHardware()) {
                 howToPlayButton.getTexture().unload();
                 howToPlayButton = null;
         }
 
         if (howToPlayDigitalPad != null && howToPlayDigitalPad.getTexture().isLoadedToHardware()) {
                 howToPlayDigitalPad.getTexture().unload();
                 howToPlayDigitalPad = null;
         }
     }
 
     public synchronized void loadCharacterProfileResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         if (characterProfileBackground1 == null) {
             BitmapTextureAtlas characterProfileBackground1T = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             characterProfileBackground1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     characterProfileBackground1T, activity, "character_profile_background_1.png", 0, 0);
             characterProfileBackground1T.load();
         }
 
         if (characterProfileBackground2 == null) {
             BitmapTextureAtlas characterProfileBackground2T = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             characterProfileBackground2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     characterProfileBackground2T, activity, "character_profile_background_2.png", 0, 0);
             characterProfileBackground2T.load();
         }
 
         if (characterProfileRyoko == null) {
             BitmapTextureAtlas characterProfileRyokoT = new BitmapTextureAtlas(textureManager, 706, 1563,
                     mTransparentTextureOption);
             characterProfileRyoko = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     characterProfileRyokoT, activity, "character_profile_ryoko.png", 0, 0);
             characterProfileRyokoT.load();
         }
 
         if (characterProfileSho == null) {
             BitmapTextureAtlas characterProfileShoT = new BitmapTextureAtlas(textureManager, 981, 1734,
                     mTransparentTextureOption);
             characterProfileSho = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     characterProfileShoT, activity, "character_profile_sho.png", 0, 0);
             characterProfileShoT.load();
         }
     }
 
     public synchronized void unloadCharacterProfileResources() {
         if (characterProfileBackground1 != null && characterProfileBackground1.getTexture().isLoadedToHardware()) {
                 characterProfileBackground1.getTexture().unload();
                 characterProfileBackground1 = null;
         }
 
         if (characterProfileBackground2 != null && characterProfileBackground2.getTexture().isLoadedToHardware()) {
                 characterProfileBackground2.getTexture().unload();
                 characterProfileBackground2 = null;
         }
 
         if (characterProfileRyoko != null && characterProfileRyoko.getTexture().isLoadedToHardware()) {
                 characterProfileRyoko.getTexture().unload();
                 characterProfileRyoko = null;
         }
 
         if (characterProfileSho != null && characterProfileSho.getTexture().isLoadedToHardware()) {
                 characterProfileSho.getTexture().unload();
                 characterProfileSho = null;
         }
     }
 
     public synchronized void loadMenuAchievementsResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
         
         if (menuAchievementsContainerDescription == null) {
             BitmapTextureAtlas menuAchievementsContainerDescriptionT = new BitmapTextureAtlas(textureManager, 438, 285,
                     mTransparentTextureOption);
             menuAchievementsContainerDescription = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuAchievementsContainerDescriptionT, activity, "menu_achievements_container_description.png", 0, 0);
             menuAchievementsContainerDescriptionT.load();
         }
 
         if (menuAchievementsContainerIcons == null) {
             BitmapTextureAtlas menuAchievementsContainerIconsT = new BitmapTextureAtlas(textureManager, 1063, 820,
                     mTransparentTextureOption);
             menuAchievementsContainerIcons = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuAchievementsContainerIconsT, activity, "menu_achievements_container_icons.png", 0, 0);
             menuAchievementsContainerIconsT.load();
         }
 
         // Icons Big
         if(menuAchievementsIconsBig == null) {
             BuildableBitmapTextureAtlas menuAchievementsIconsBigT = new BuildableBitmapTextureAtlas(
                     textureManager, 1140, 1140, mTransparentTextureOption);
             menuAchievementsIconsBig = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuAchievementsIconsBigT, context, "menu_achievements_icons_big.png", 6, 6);
             try {
                 menuAchievementsIconsBigT.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             } catch (TextureAtlasBuilderException e) { e.printStackTrace(); }
             menuAchievementsIconsBigT.load();
         }
         
         // Icons Small
         if (menuAchievementsIconsSmall == null) {
         BitmapTextureAtlas menuAchievementsIconsSmallT = new BitmapTextureAtlas(textureManager,
                 952, 1360, mTransparentTextureOption);
         
         ITextureRegion menuAchievementsIconsSmall = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 menuAchievementsIconsSmallT, activity, "menu_achievements_icons_small.png", 0, 0);
         menuAchievementsIconsSmallT.load();
 
         // Fill TiledSprites matrix :)
         menuAchievementsIconsArray = new ITiledTextureRegion[MENU_ACHIEV_COLS][MENU_ACHIEV_ROWS];
         for(int i = 0; i < MENU_ACHIEV_ROWS; i++)
             for(int j = 0; j < MENU_ACHIEV_COLS; j++) {                
                 menuAchievementsIconsArray[j][i] = TextureRegionFactory.extractTiledFromTexture(
                         menuAchievementsIconsSmall.getTexture(),
                         j * MENU_ACHIEV_ICON_SIZE,
                         i * MENU_ACHIEV_ICON_SIZE * 2,
                         MENU_ACHIEV_ICON_SIZE,
                         MENU_ACHIEV_ICON_SIZE * 2,
                         1, 2);
             }
         }
 
         // Selection Mark:
         if(menuAchievementsSelectionMark == null) {
             BitmapTextureAtlas menuAchievementsSelectionMarkT = new BitmapTextureAtlas(textureManager, 136, 136,
                     mTransparentTextureOption);
             menuAchievementsSelectionMark = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuAchievementsSelectionMarkT, activity, "menu_achievements_icons_small_selection_mark.png", 0, 0);
             menuAchievementsSelectionMarkT.load();
         }
         
         if (menuAchievementsIngameContainer == null) {
             BitmapTextureAtlas menuAchievementsIngameContainerT = new BitmapTextureAtlas(textureManager, 806, 192,
                     mTransparentTextureOption);
             menuAchievementsIngameContainer = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuAchievementsIngameContainerT, activity, "menu_achievements_ingame_container.png", 0, 0);
             menuAchievementsIngameContainerT.load();
         }
 
         if (menuAchievementsSuccessStamp == null) {
             BitmapTextureAtlas menuAchievementsSuccessStampT = new BitmapTextureAtlas(textureManager, 260, 260,
                     mTransparentTextureOption);
             menuAchievementsSuccessStamp = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuAchievementsSuccessStampT, activity, "menu_achievements_success_stamp.png", 0, 0);
             menuAchievementsSuccessStampT.load();
         }
     }
 
     public synchronized void unloadMenuAchievementsResources() {
         // TODO: new textures added
         if (menuAchievementsContainerDescription != null && menuAchievementsContainerDescription.getTexture().isLoadedToHardware()) {
                 menuAchievementsContainerDescription.getTexture().unload();
                 menuAchievementsContainerDescription = null;
         }
 
         if (menuAchievementsContainerIcons != null && menuAchievementsContainerIcons.getTexture().isLoadedToHardware()) {
                 menuAchievementsContainerIcons.getTexture().unload();
                 menuAchievementsContainerIcons = null;
         }
 
         if (menuAchievementsIconsBig != null && menuAchievementsIconsBig.getTexture().isLoadedToHardware()) {
                 menuAchievementsIconsBig.getTexture().unload();
                 menuAchievementsIconsBig = null;
         }
 
         if (menuAchievementsIconsSmall != null && menuAchievementsIconsSmall.getTexture().isLoadedToHardware()) {
                 menuAchievementsIconsSmall.getTexture().unload();
                 menuAchievementsIconsSmall = null;
         }
 
         if (menuAchievementsIngameContainer != null && menuAchievementsIngameContainer.getTexture().isLoadedToHardware()) {
                 menuAchievementsIngameContainer.getTexture().unload();
                 menuAchievementsIngameContainer = null;
         }
 
         if (menuAchievementsSuccessStamp != null && menuAchievementsSuccessStamp.getTexture().isLoadedToHardware()) {
                 menuAchievementsSuccessStamp.getTexture().unload();
                 menuAchievementsSuccessStamp = null;
         }
     }
     
     
     public synchronized void loadRecordsResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/results/");
 
         BitmapTextureAtlas recordHeadsT = new BitmapTextureAtlas(textureManager, 100, 100,
                 mTransparentTextureOption);
         ITextureRegion recordHeadsTR = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 recordHeadsT, activity, "results_records_winner_faces.png", 0, 0);
         recordHeadsT.load();
         
         menuRecordsRyokoHead = TextureRegionFactory.
                 extractFromTexture(recordHeadsT, 0, 0, 50, 50, false);
         
         menuRecordsRyokoHeadGold = TextureRegionFactory.
                 extractFromTexture(recordHeadsT, 0, 50, 50, 50, false);
         
         menuRecordsShoHead = TextureRegionFactory.
                 extractFromTexture(recordHeadsT, 50, 0, 50, 50, false);
         
         menuRecordsShoHeadGold = TextureRegionFactory.
                 extractFromTexture(recordHeadsT, 50, 50, 50, 50, false);
     }
     
     
     public synchronized void unloadRecordsResources() { 
         if (menuRecordsRyokoHead != null && menuRecordsRyokoHead.getTexture().isLoadedToHardware()) {
             menuRecordsRyokoHead.getTexture().unload();
             menuRecordsRyokoHead = null;
         }
         if (menuRecordsRyokoHeadGold != null && menuRecordsRyokoHeadGold.getTexture().isLoadedToHardware()) {
             menuRecordsRyokoHeadGold.getTexture().unload();
             menuRecordsRyokoHeadGold = null;
         }
         if (menuRecordsShoHead != null && menuRecordsShoHead.getTexture().isLoadedToHardware()) {
             menuRecordsShoHead.getTexture().unload();
             menuRecordsShoHead = null;
         }
         if (menuRecordsShoHeadGold != null && menuRecordsShoHeadGold.getTexture().isLoadedToHardware()) {
             menuRecordsShoHeadGold.getTexture().unload();
             menuRecordsShoHeadGold = null;
         }
     }
 
     public synchronized void loadMenuMapResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
 
         if (menuMapBackgroundMarks == null) {
             BuildableBitmapTextureAtlas menuMapBackgroundMarksBit = new BuildableBitmapTextureAtlas(
                     textureManager, 94, 152, mTransparentTextureOption);
             menuMapBackgroundMarks = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuMapBackgroundMarksBit, context, "menu_map_background_marks.png", 1, 4);
             try {
                 menuMapBackgroundMarksBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuMapBackgroundMarksBit.load();
         }
 
         if (menuMapBackground == null) {
             BitmapTextureAtlas menuMapBackgroundT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             menuMapBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuMapBackgroundT, activity, "menu_map_background.png", 0, 0);
             menuMapBackgroundT.load();
         }
 
 
         if (menuMapChRyoko == null) {
             BuildableBitmapTextureAtlas menuMapChRyokoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 192, 330, mTransparentTextureOption);
             menuMapChRyoko = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuMapChRyokoBit, context, "menu_map_ch_ryoko.png", 2, 2);
             try {
                 menuMapChRyokoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuMapChRyokoBit.load();
         }
 
 
         if (menuMapChSho == null) {
             BuildableBitmapTextureAtlas menuMapChShoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 192, 330, mTransparentTextureOption);
             menuMapChSho = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuMapChShoBit, context, "menu_map_ch_sho.png", 2, 2);
             try {
                 menuMapChShoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuMapChShoBit.load();
         }
 
 
         if (menuMapDrawings == null) {
             BuildableBitmapTextureAtlas menuMapDrawingsBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1106, 962, mTransparentTextureOption);
             menuMapDrawings = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuMapDrawingsBit, context, "menu_map_drawings.png", 2, 2);
             try {
                 menuMapDrawingsBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuMapDrawingsBit.load();
         }
 
         if (menuMapScroll == null) {
             BuildableBitmapTextureAtlas menuMapScrollBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1568, 1632, mTransparentTextureOption);
             menuMapScroll = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuMapScrollBit, context, "menu_map_scroll.png", 2, 2);
             try {
                 menuMapScrollBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuMapScrollBit.load();
         }
     }
 
     public synchronized void unloadMenuMapResources() {
         if (menuMapBackgroundMarks != null && menuMapBackgroundMarks.getTexture().isLoadedToHardware()) {
                 menuMapBackgroundMarks.getTexture().unload();
                 menuMapBackgroundMarks = null;
         }
 
         if (menuMapBackground != null && menuMapBackground.getTexture().isLoadedToHardware()) {
                 menuMapBackground.getTexture().unload();
                 menuMapBackground = null;
         }
 
         if (menuMapChRyoko != null && menuMapChRyoko.getTexture().isLoadedToHardware()) {
                 menuMapChRyoko.getTexture().unload();
                 menuMapChRyoko = null;
         }
 
         if (menuMapChSho != null && menuMapChSho.getTexture().isLoadedToHardware()) {
                 menuMapChSho.getTexture().unload();
                 menuMapChSho = null;
         }
 
         if (menuMapDrawings != null && menuMapDrawings.getTexture().isLoadedToHardware()) {
                 menuMapDrawings.getTexture().unload();
                 menuMapDrawings = null;
         }
 
         if (menuMapScroll != null && menuMapScroll.getTexture().isLoadedToHardware()) {
                 menuMapScroll.getTexture().unload();
                 menuMapScroll = null;
         }
     }
 
     public synchronized void loadMenuPauseResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
         if (menuPauseBambooFrame == null) {
             BitmapTextureAtlas menuPauseBambooFrameT = new BitmapTextureAtlas(textureManager, 1192, 717,
                     mTransparentTextureOption);
             menuPauseBambooFrame = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuPauseBambooFrameT, activity, "menu_pause_bamboo_frame.png", 0, 0);
             menuPauseBambooFrameT.load();
         }
     }
 
     public synchronized void unloadMenuPauseResources() {
         if (menuPauseBambooFrame != null && menuPauseBambooFrame.getTexture().isLoadedToHardware()) {
                 menuPauseBambooFrame.getTexture().unload();
                 menuPauseBambooFrame = null;
         }
     }
 
     public synchronized void loadMenuSelectedResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menus/");
      
         // Ryoko:
         BitmapTextureAtlas menuSelectChRyokoBit =
                 new BitmapTextureAtlas(textureManager, 870, 1028, mTransparentTextureOption);
         ITextureRegion menuSelectChRyokoTR = BitmapTextureAtlasTextureRegionFactory.
                 createFromAsset(menuSelectChRyokoBit, activity, "menu_select_ch_ryoko.png", 0, 0);
         menuSelectChRyokoBit.load();
         menuSelectChRyoko =
             TextureRegionFactory.extractFromTexture(menuSelectChRyokoBit, 0, 0, 435, 1028, false);
         menuSelectChRyokoOutline =
             TextureRegionFactory.extractFromTexture(menuSelectChRyokoBit, 435, 0, 435, 1028, false);
         
         // Sho:
         BitmapTextureAtlas menuSelectChShoBit =
                 new BitmapTextureAtlas(textureManager, 1310, 1120, mTransparentTextureOption);
         ITextureRegion menuSelectChShoTR = BitmapTextureAtlasTextureRegionFactory.
                 createFromAsset(menuSelectChShoBit, activity, "menu_select_ch_sho.png", 0, 0);
         menuSelectChShoBit.load();
         menuSelectChSho =
             TextureRegionFactory.extractFromTexture(menuSelectChShoBit, 0, 0, 655, 1028, false);
         menuSelectChShoOutline =
             TextureRegionFactory.extractFromTexture(menuSelectChShoBit, 655, 0, 655, 1028, false);
         
         // Clouds:
         BitmapTextureAtlas menuSelectCloudsBit =
                 new BitmapTextureAtlas(textureManager, 1422, 537, mTransparentTextureOption);
         ITextureRegion menuSelectCloudsTR = BitmapTextureAtlasTextureRegionFactory.
                 createFromAsset(menuSelectCloudsBit, activity, "menu_select_clouds.png", 0, 0);
         menuSelectCloudsBit.load();
         menuSelectClouds1 =
             TextureRegionFactory.extractFromTexture(menuSelectCloudsBit, 0, 0, 711, 537, false);
         menuSelectClouds2 =
             TextureRegionFactory.extractFromTexture(menuSelectCloudsBit, 711, 0, 711, 537, false);
         
 /*        if (menuSelectChRyoko == null) {
             BuildableBitmapTextureAtlas menuSelectChRyokoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 870, 1028, mTransparentTextureOption);
             menuSelectChRyoko = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuSelectChRyokoBit, context, "menu_select_ch_ryoko.png", 2, 1);
             try {
                 menuSelectChRyokoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuSelectChRyokoBit.load();
         }
         if (menuSelectChSho == null) {
             BuildableBitmapTextureAtlas menuSelectChShoBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1310, 1120, mTransparentTextureOption);
             menuSelectChSho = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     menuSelectChShoBit, context, "menu_select_ch_sho.png", 2, 1);
             try {
                 menuSelectChShoBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             menuSelectChShoBit.load();
         }
         
         if (menuSelectClouds == null) {
             BitmapTextureAtlas menuSelectCloudsT = new BitmapTextureAtlas(textureManager, 1422, 537,
                     mTransparentTextureOption);
             menuSelectClouds = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuSelectCloudsT, activity, "menu_select_clouds.png", 0, 0);
             menuSelectCloudsT.load();
         }
         
         */
         
         if (menuSelectDifficulty == null) {
             BitmapTextureAtlas menuSelectDifficultyT = new BitmapTextureAtlas(textureManager, 1649, 633,
                     mTransparentTextureOption);
             menuSelectDifficulty = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuSelectDifficultyT, activity, "menu_select_difficulty.png", 0, 0);
             menuSelectDifficultyT.load();
         }
         if (menuSelectMoon == null) {
             BitmapTextureAtlas menuSelectMoonT = new BitmapTextureAtlas(textureManager, 940, 905,
                     mTransparentTextureOption);
             menuSelectMoon = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuSelectMoonT, activity, "menu_select_moon.png", 0, 0);
             menuSelectMoonT.load();
         }
         if (menuSelectRoof == null) {
             BitmapTextureAtlas menuSelectRoofT = new BitmapTextureAtlas(textureManager, 1585, 385,
                     mTransparentTextureOption);
             menuSelectRoof = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuSelectRoofT, activity, "menu_select_roof.png", 0, 0);
             menuSelectRoofT.load();
         }
         if (menuSelectSky == null) {
             BitmapTextureAtlas menuSelectSkyT = new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             menuSelectSky = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     menuSelectSkyT, activity, "menu_select_sky.png", 0, 0);
             menuSelectSkyT.load();
         }
     }
 
     public synchronized void unloadMenuSelectedResources() {
         if (menuSelectChRyoko != null && menuSelectChRyoko.getTexture().isLoadedToHardware()) {
             menuSelectChRyoko.getTexture().unload();
             menuSelectChRyoko = null;
         }
         
         if (menuSelectChRyokoOutline != null && menuSelectChRyokoOutline.getTexture().isLoadedToHardware()) {
             menuSelectChRyokoOutline.getTexture().unload();
             menuSelectChRyokoOutline = null;
         }
         
         if (menuSelectChSho != null && menuSelectChSho.getTexture().isLoadedToHardware()) {
                 menuSelectChSho.getTexture().unload();
                 menuSelectChSho = null;
         }        
         
         if (menuSelectChShoOutline != null && menuSelectChShoOutline.getTexture().isLoadedToHardware()) {
             menuSelectChShoOutline.getTexture().unload();
             menuSelectChShoOutline = null;
         }
         
         if (menuSelectClouds1 != null && menuSelectClouds1.getTexture().isLoadedToHardware()) {
                 menuSelectClouds1.getTexture().unload();
                 menuSelectClouds1 = null;
         }
         
         if (menuSelectClouds2 != null && menuSelectClouds2.getTexture().isLoadedToHardware()) {
             menuSelectClouds2.getTexture().unload();
             menuSelectClouds2 = null;
         }
         if (menuSelectDifficulty != null && menuSelectDifficulty.getTexture().isLoadedToHardware()) {
                 menuSelectDifficulty.getTexture().unload();
                 menuSelectDifficulty = null;
         }
 
         if (menuSelectMoon != null && menuSelectMoon.getTexture().isLoadedToHardware()) {
                 menuSelectMoon.getTexture().unload();
                 menuSelectMoon = null;
         }
 
         if (menuSelectRoof != null && menuSelectRoof.getTexture().isLoadedToHardware()) {
                 menuSelectRoof.getTexture().unload();
                 menuSelectRoof = null;
         }
 
         if (menuSelectSky != null && menuSelectSky.getTexture().isLoadedToHardware()) {
                 menuSelectSky.getTexture().unload();
                 menuSelectSky = null;
         }
     }
 
     public synchronized void loadResultLoseSceneResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/results/");
 
         // Bg:
         if(loseBg==null) {
             BitmapTextureAtlas loseBgT =  new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             loseBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     loseBgT, activity, "results_lose_background.png", 0, 0);
             loseBgT.load();
         }
 
         // Sho:
         if(loseCharSho==null) {
             BitmapTextureAtlas loseCharShoT =  new BitmapTextureAtlas(textureManager, 797, 440,
                     mTransparentTextureOption);
             loseCharSho = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     loseCharShoT, activity, "results_lose_ch_sho.png", 0, 0);
             loseCharShoT.load();
         }
 
         // Ryoko:
         if(loseCharRyoko==null) {
             BitmapTextureAtlas loseCharRyokoT =  new BitmapTextureAtlas(textureManager, 797, 440,
                     mTransparentTextureOption);
             loseCharRyoko = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     loseCharRyokoT, activity, "results_lose_ch_ryoko.png", 0, 0);
             loseCharRyokoT.load();
         }
     }
 
     public synchronized void unloadResultLoseSceneResources() {
         if(loseBg!=null) {
             if(loseBg.getTexture().isLoadedToHardware()) {
                 loseBg.getTexture().unload();
                 loseBg = null;
             }
         }
         if(loseCharSho!=null) {
             if(loseCharSho.getTexture().isLoadedToHardware()) {
                 loseCharSho.getTexture().unload();
                 loseCharSho = null;
             }
         }
         if(loseCharRyoko!=null) {
             if(loseCharRyoko.getTexture().isLoadedToHardware()) {
                 loseCharRyoko.getTexture().unload();
                 loseCharRyoko = null;
             }
         }
 
         // Garbage Collector:
         System.gc();
     }
 
     
     public synchronized void loadResultWinResources() {
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/results/");
 
         // Bg:
         if(winBg==null) {
             BitmapTextureAtlas winBgT =  new BitmapTextureAtlas(textureManager, 1920, 1080,
                     mTransparentTextureOption);
             winBg = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     winBgT, activity, "results_win_background.png", 0, 0);
             winBgT.load();
         }
 
         // Scroll:
         if(winScroll==null) {
             BitmapTextureAtlas winScrollT =  new BitmapTextureAtlas(textureManager, 1064, 1029,
                     mTransparentTextureOption);
             winScroll = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     winScrollT, activity, "results_win_scroll.png", 0, 0);
             winScrollT.load();
         }
 
         // Sho:
         if(winCharSho==null) {
             BitmapTextureAtlas winCharShoT =  new BitmapTextureAtlas(textureManager, 437, 799,
                     mTransparentTextureOption);
             winCharSho = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     winCharShoT, activity, "results_win_ch_sho.png", 0, 0);
             winCharShoT.load();
         }
 
         // Ryoko:
         if(winCharRyoko==null) {
             BitmapTextureAtlas winCharRyokoT =  new BitmapTextureAtlas(textureManager, 395, 767,
                     mTransparentTextureOption);
             winCharRyoko = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                     winCharRyokoT, activity, "results_win_ch_ryoko.png", 0, 0);
             winCharRyokoT.load();
         }
 
         // Drawings:
         if (winDrawings == null) {
             BuildableBitmapTextureAtlas winDrawingsBit = new BuildableBitmapTextureAtlas(
                     textureManager, 1106, 962, mTransparentTextureOption);
             winDrawings = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     winDrawingsBit, context, "results_win_drawings.png", 2, 2);
             try {
                 winDrawingsBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             winDrawingsBit.load();
         }
 
         // Stamps:
         if (winStampRanking == null) {
             BuildableBitmapTextureAtlas winStampRankingBit = new BuildableBitmapTextureAtlas(
                     textureManager, 780, 400, mTransparentTextureOption);
             winStampRanking = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
                     winStampRankingBit, context, "results_win_stamp_ranking.png", 2, 2);
             try {
                 winStampRankingBit.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource,
                         BitmapTextureAtlas>(0, 0, 0));
             }
             catch (TextureAtlasBuilderException e) {
                 e.printStackTrace();
             }
             winStampRankingBit.load();
         }
     }
 
 
     public synchronized void unloadResultWinResources() {
         if(winBg!=null) {
             if(winBg.getTexture().isLoadedToHardware()) {
                 winBg.getTexture().unload();
                 winBg = null;
             }
         }
         if(winScroll!=null) {
             if(winScroll.getTexture().isLoadedToHardware()) {
                 winScroll.getTexture().unload();
                 winScroll = null;
             }
         }
         if(winCharSho!=null) {
             if(winCharSho.getTexture().isLoadedToHardware()) {
                 winCharSho.getTexture().unload();
                 winCharSho = null;
             }
         }
         if(winCharRyoko!=null) {
             if(winCharRyoko.getTexture().isLoadedToHardware()) {
                 winCharRyoko.getTexture().unload();
                 winCharRyoko = null;
             }
         }
         if(winDrawings!=null) {
             if(winDrawings.getTexture().isLoadedToHardware()) {
                 winDrawings.getTexture().unload();
                 winDrawings = null;
             }
         }
         if(winStampRanking!=null) {
             if(winStampRanking.getTexture().isLoadedToHardware()) {
                 winStampRanking.getTexture().unload();
                 winStampRanking = null;
             }
         }
         // Garbage Collector:
         System.gc();
     }
     
     
     public synchronized void loadGameOverResources() {
     }
 
 
     public synchronized void unloadGameOverResources() {
     }
 
     public synchronized void loadMusicsResources() {
         MusicFactory.setAssetBasePath("music/");
         try {
             credits = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "credits.ogg");
             ending = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "ending.ogg");
             intro1 = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "intro1.ogg");
             intro2 = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "intro2.ogg");
             map = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "map.ogg");
             records = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "records.ogg");
             trialJump = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "trial_jump.ogg");
             trialCut = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "trial_cut_music.ogg");
             trialRun = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "trial_run.ogg");
             trialShurikens = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "trial_shurikens.ogg");
             loseMusic = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "result_lose.ogg");
             winMusic = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "result_win.ogg");
             gameOverMusic = MusicFactory.createMusicFromAsset(
                     activity.getMusicManager(), context, "game_over.ogg");
         }
         catch (final IOException e) {
             Log.v("Sounds Load","Exception:" + e.getMessage());
         }
     }
 
     public synchronized void unloadMusicsResources() {
         if (!credits.isReleased())
             credits.release();
         if (!ending.isReleased())
             ending.release();
         if (!intro1.isReleased())
             intro1.release();
         if (!intro2.isReleased())
             intro2.release();
         if (!map.isReleased())
             map.release();
         if (!records.isReleased())
             records.release();
         if (!trialJump.isReleased())
             trialJump.release();
         if (!trialCut.isReleased())
             trialCut.release();
         if (!trialRun.isReleased())
             trialRun.release();
         if (!trialShurikens.isReleased())
             trialShurikens.release();
         if (!loseMusic.isReleased())
             loseMusic.release();
         if(winMusic != null && !winMusic.isReleased())
             winMusic.release();
         if(!gameOverMusic.isReleased())
             gameOverMusic.release();
     }
 
     public synchronized void loadSoundsResources() {
         SoundFactory.setAssetBasePath("sounds/");
         try {
             effectEyeGleam = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "effect_eye_gleam.ogg");
             effectMasterHit = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "effect_master_hit.ogg");
             effectSweatDrop = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "effect_sweat_drop.ogg");
             judge1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_1.ogg");
             judge2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_2.ogg");
             judge3 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_3.ogg");
             judge4 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_4.ogg");
             judge5 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_5.ogg");
             judge6 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_6.ogg");
             judge7 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_7.ogg");
             judge8 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_8.ogg");
             judge9 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_9.ogg");
             judgeExcellent = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_excellent.ogg");
             judgeGood = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_good.ogg");
             judgeGo = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_go.ogg");
             judgeGreat = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_great.ogg");
             judgeReady = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_ready.ogg");
             menuAchievement = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_achievement.ogg");
             menuActivate = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_activate.ogg");
             menuBack = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_back.ogg");
             menuFocus = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_focus.ogg");
             menuIntro1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_intro1.ogg");
             menuLogoMadgear = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_logo_madgear.ogg");
             menuRank = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_rank.ogg");
             ryokoCutCut = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_cut_cut.ogg");
             ryokoCutLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_cut_lose.ogg");
             ryokoCutWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_cut_win.ogg");
             ryokoJumpCharge = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_jump_charge.ogg");
             ryokoJumpFall = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_jump_fall.ogg");
             ryokoJumpHop = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_jump_hop.ogg");
             ryokoJumpLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_jump_lose.ogg");
             ryokoJumpWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_jump_win.ogg");
             ryokoMenuContinue = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_menu_continue.ogg");
             ryokoMenuGameOver = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_menu_game_over.ogg");
             ryokoRunCharge = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_run_charge.ogg");
             ryokoRunLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_run_lose.ogg");
             ryokoRunStart = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_run_start.ogg");
             ryokoRunWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_run_win.ogg");
             ryokoShurikenLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_shuriken_lose.ogg");
             ryokoShurikenThrow = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_shuriken_throw.ogg");
             ryokoShurikenWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "ryoko_shuriken_win.ogg");
             shoCutCut = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_cut_cut.ogg");
             shoCutLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_cut_lose.ogg");
             shoCutWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_cut_win.ogg");
             shoJumpCharge = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_jump_charge.ogg");
             shoJumpFall = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_jump_fall.ogg");
             shoJumpHop = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_jump_hop.ogg");
             shoJumpLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_jump_lose.ogg");
             shoJumpWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_jump_win.ogg");
             shoMenuContinue = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_menu_continue.ogg");
             shoMenuGameOver = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_menu_game_over.ogg");
             shoRunCharge = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_run_charge.ogg");
             shoRunLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_run_lose.ogg");
             shoRunStart = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_run_start.ogg");
             shoRunWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_run_win.ogg");
             shoShurikenLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_shuriken_lose.ogg");
             shoShurikenThrow = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_shuriken_throw.ogg");
             shoShurikenWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "sho_shuriken_win.ogg");
             trialCutCandleBlowOut = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_candle_blow_out.ogg");
             trialCutCandleShowingCut = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_candle_showing_cut.ogg");
             trialCutCandleThud = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_candle_thud.ogg");
             trialCutCandleWobble = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_candle_wobble.ogg");
             trialCutCandleWoobleThud = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_candle_wooble_thud.ogg");
             trialCutEyesZoomV2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_eyes_zoom_v2.ogg");
             trialCutEyesZoom = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_eyes_zoom.ogg");
             trialCutKatana1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_cut1.ogg");
             trialCutKatana2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_cut2.ogg");
             trialCutKatana3 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_cut3.ogg");
             trialCutKatanaWhoosh = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_whoosh1.ogg");
             trialCutKatanaWhoosh2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_whoosh2.ogg");
             trialCutKatanaWhoosh3 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_cut_katana_whoosh3.ogg");
             trialJumpFall = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_fall.ogg");
             trialJumpReach = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_reach.ogg");
             trialJumpSlip = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_slip.ogg");
             trialJumpTap1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_tap1.ogg");
             trialJumpTap2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_tap2.ogg");
             trialJumpThud = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_thud.ogg");
             trialJumpWhoosh1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_whoosh1.ogg");
             trialJumpWhoosh2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_whoosh2.ogg");
             trialJumpWhoosh3 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_whoosh3.ogg");
             trialJumpWobble = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_jump_wobble.ogg");
             trialRunTap1 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_run_tap1.ogg");
             trialRunTap2 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_run_tap2.ogg");
             trialRunTap3 = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_run_tap3.ogg");
             trialRunWind1Start = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_run_wind_1_start.ogg");
             trialRunWind2Running = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_run_wind_2_running.ogg");
             trialRunWind3End = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_run_wind_3_end.ogg");
             trialShurikenStrawmanAscend = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_shuriken_strawman_ascend.ogg");
             trialShurikenStrawmanDescend = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_shuriken_strawman_descend.ogg");
             trialShurikenStrawmanDestroyed = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_shuriken_strawman_destroyed.ogg");
             trialShurikenStrawmanHit = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_shuriken_strawman_hit.ogg");
             trialShurikenStrawmanMove = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_shuriken_strawman_move.ogg");
             trialShurikenThrowing = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "trial_shuriken_throwing.ogg");
             loseYouLose = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_you_lose.ogg");
             winYouWin = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_you_win.ogg");
             winPointsSum = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_points_sum.ogg");
             winPointsTotal = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "menu_points_total.ogg");
             gameOver = SoundFactory.createSoundFromAsset(
                     activity.getSoundManager(), context, "judge_game_over.ogg");
         }
         catch (final IOException e) {
             Log.v("Sounds Load","Exception:" + e.getMessage());
         }
     }
 
     public synchronized void unloadSoundsResources() {
         if (!effectEyeGleam.isReleased())
             effectEyeGleam.release();
         if (!effectMasterHit.isReleased())
             effectMasterHit.release();
         if (!effectSweatDrop.isReleased())
             effectSweatDrop.release();
         if (!judge1.isReleased())
             judge1.release();
         if (!judge2.isReleased())
             judge2.release();
         if (!judge3.isReleased())
             judge3.release();
         if (!judge4.isReleased())
             judge4.release();
         if (!judge5.isReleased())
             judge5.release();
         if (!judge6.isReleased())
             judge6.release();
         if (!judge7.isReleased())
             judge7.release();
         if (!judge8.isReleased())
             judge8.release();
         if (!judge9.isReleased())
             judge9.release();
         if (!judgeExcellent.isReleased())
             judgeExcellent.release();
         if (!judgeGood.isReleased())
             judgeGood.release();
         if (!judgeGo.isReleased())
             judgeGo.release();
         if (!judgeGreat.isReleased())
             judgeGreat.release();
         if (!judgeReady.isReleased())
             judgeReady.release();
         if (!menuAchievement.isReleased())
             menuAchievement.release();
         if (!menuActivate.isReleased())
             menuActivate.release();
         if (!menuBack.isReleased())
             menuBack.release();
         if (!menuFocus.isReleased())
             menuFocus.release();
         if (!menuIntro1.isReleased())
             menuIntro1.release();
         if (!menuLogoMadgear.isReleased())
             menuLogoMadgear.release();
         if (!menuRank.isReleased())
             menuRank.release();
         if (!ryokoCutCut.isReleased())
             ryokoCutCut.release();
         if (!ryokoCutLose.isReleased())
             ryokoCutLose.release();
         if (!ryokoCutWin.isReleased())
             ryokoCutWin.release();
         if (!ryokoJumpCharge.isReleased())
             ryokoJumpCharge.release();
         if (!ryokoJumpFall.isReleased())
             ryokoJumpFall.release();
         if (!ryokoJumpHop.isReleased())
             ryokoJumpHop.release();
         if (!ryokoJumpLose.isReleased())
             ryokoJumpLose.release();
         if (!ryokoJumpWin.isReleased())
             ryokoJumpWin.release();
         if (!ryokoMenuContinue.isReleased())
             ryokoMenuContinue.release();
         if (!ryokoMenuGameOver.isReleased())
             ryokoMenuGameOver.release();
         if (!ryokoRunCharge.isReleased())
             ryokoRunCharge.release();
         if (!ryokoRunLose.isReleased())
             ryokoRunLose.release();
         if (!ryokoRunStart.isReleased())
             ryokoRunStart.release();
         if (!ryokoRunWin.isReleased())
             ryokoRunWin.release();
         if (!ryokoShurikenLose.isReleased())
             ryokoShurikenLose.release();
         if (!ryokoShurikenThrow.isReleased())
             ryokoShurikenThrow.release();
         if (!ryokoShurikenWin.isReleased())
             ryokoShurikenWin.release();
         if (!shoCutCut.isReleased())
             shoCutCut.release();
         if (!shoCutLose.isReleased())
             shoCutLose.release();
         if (!shoCutWin.isReleased())
             shoCutWin.release();
         if (!shoJumpCharge.isReleased())
             shoJumpCharge.release();
         if (!shoJumpFall.isReleased())
             shoJumpFall.release();
         if (!shoJumpHop.isReleased())
             shoJumpHop.release();
         if (!shoJumpLose.isReleased())
             shoJumpLose.release();
         if (!shoJumpWin.isReleased())
             shoJumpWin.release();
         if (!shoMenuContinue.isReleased())
             shoMenuContinue.release();
         if (!shoMenuGameOver.isReleased())
             shoMenuGameOver.release();
         if (!shoRunCharge.isReleased())
             shoRunCharge.release();
         if (!shoRunLose.isReleased())
             shoRunLose.release();
         if (!shoRunStart.isReleased())
             shoRunStart.release();
         if (!shoRunWin.isReleased())
             shoRunWin.release();
         if (!shoShurikenLose.isReleased())
             shoShurikenLose.release();
         if (!shoShurikenThrow.isReleased())
             shoShurikenThrow.release();
         if (!shoShurikenWin.isReleased())
             shoShurikenWin.release();
         if (!trialCutCandleBlowOut.isReleased())
             trialCutCandleBlowOut.release();
         if (!trialCutCandleShowingCut.isReleased())
             trialCutCandleShowingCut.release();
         if (!trialCutCandleThud.isReleased())
             trialCutCandleThud.release();
         if (!trialCutCandleWobble.isReleased())
             trialCutCandleWobble.release();
         if (!trialCutCandleWoobleThud.isReleased())
             trialCutCandleWoobleThud.release();
         if (!trialCutEyesZoomV2.isReleased())
             trialCutEyesZoomV2.release();
         if (!trialCutKatanaWhoosh3.isReleased())
             trialCutKatanaWhoosh3.release();
         if (!trialJumpFall.isReleased())
             trialJumpFall.release();
         if (!trialJumpReach.isReleased())
             trialJumpReach.release();
         if (!trialJumpSlip.isReleased())
             trialJumpSlip.release();
         if (!trialJumpTap1.isReleased())
             trialJumpTap1.release();
         if (!trialJumpTap2.isReleased())
             trialJumpTap2.release();
         if (!trialJumpThud.isReleased())
             trialJumpThud.release();
         if (!trialJumpWhoosh1.isReleased())
             trialJumpWhoosh1.release();
         if (!trialJumpWhoosh2.isReleased())
             trialJumpWhoosh2.release();
         if (!trialJumpWhoosh3.isReleased())
             trialJumpWhoosh3.release();
         if (!trialJumpWobble.isReleased())
             trialJumpWobble.release();
         if(!trialCutEyesZoom.isReleased())
             trialCutEyesZoom.release();
         if(!trialCutKatana1.isReleased())
             trialCutKatana1.release();
         if(!trialCutKatana2.isReleased())
             trialCutKatana2.release();
         if(!trialCutKatana3.isReleased())
             trialCutKatana3.release();
         if(!trialCutKatanaWhoosh.isReleased())
             trialCutKatanaWhoosh.release();
         if(!trialCutEyesZoom.isReleased())
             trialCutKatanaWhoosh2.release();
         if (!trialRunTap1.isReleased())
             trialRunTap1.release();
         if (!trialRunTap2.isReleased())
             trialRunTap2.release();
         if (!trialRunTap3.isReleased())
             trialRunTap3.release();
         if (!trialRunWind1Start.isReleased())
             trialRunWind1Start.release();
         if (!trialRunWind2Running.isReleased())
             trialRunWind2Running.release();
         if (!trialRunWind3End.isReleased())
             trialRunWind3End.release();
         if (!trialShurikenStrawmanAscend.isReleased())
             trialShurikenStrawmanAscend.release();
         if (!trialShurikenStrawmanDescend.isReleased())
             trialShurikenStrawmanDescend.release();
         if (!trialShurikenStrawmanDestroyed.isReleased())
             trialShurikenStrawmanDestroyed.release();
         if (!trialShurikenStrawmanHit.isReleased())
             trialShurikenStrawmanHit.release();
         if (!trialShurikenStrawmanMove.isReleased())
             trialShurikenStrawmanMove.release();
         if (!trialShurikenThrowing.isReleased())
             trialShurikenThrowing.release();
         if(!loseYouLose.isReleased())
             loseYouLose.release();
         if(winYouWin != null && !winYouWin.isReleased())
             winYouWin.release();
         if(winPointsSum != null && !winPointsSum.isReleased())
             winPointsSum.release();
         if(winPointsTotal != null && !winPointsTotal.isReleased())
             winPointsTotal.release();
         if(!gameOver.isReleased())
             gameOver.release();
     }
 
     /* Loads Android resources from the "res" directory
      */
     public Resources loadAndroidRes(){
         return context.getResources();
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
         
         // CharacterIntroScene fonts
         fontLatinChrName = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 1024, 1024, activity.getAssets(), "DejaVuSans.ttf",
                 160f, true, android.graphics.Color.BLACK, 3, android.graphics.Color.WHITE);
         fontLatinChrName.load();
         fontJPChrName = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 1024, 1024, activity.getAssets(), "sazanami-gothic.ttf",
                 128f, true, android.graphics.Color.BLUE, 3, android.graphics.Color.WHITE);
         fontJPChrName.load();
         fontLatinChrInfo = FontFactory.createStrokeFromAsset(pEngine.getFontManager(),
                 pEngine.getTextureManager(), 1024, 1024, activity.getAssets(), "DejaVuSans.ttf",
                 80f, true, android.graphics.Color.BLACK, 3, android.graphics.Color.WHITE);
         fontLatinChrInfo.load();
     }
 
     /* If an unloadFonts() method is necessary, we can provide one
      */
     public synchronized void unloadFonts(){
         fontSmall.unload();
         fontMedium.unload();
         fontBig.unload();
         fontXBig.unload();
         fontLatinChrName.unload();
         fontJPChrName.unload();
         fontLatinChrInfo.unload();
     }
 }
         //if (AAA == null) {
         //    BitmapTextureAtlas BBB = new BitmapTextureAtlas(textureManager, 1920, 1080,
         //            mTransparentTextureOption);
         //    AAA = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
         //            BBB, activity, "CCC", 0, 0);
         //    BBB.load();
         //}
 
         //if (AAA != null && AAA.getTexture().isLoadedToHardware()) {
         //        AAA.getTexture().unload();
         //        AAA = null;
         //}
