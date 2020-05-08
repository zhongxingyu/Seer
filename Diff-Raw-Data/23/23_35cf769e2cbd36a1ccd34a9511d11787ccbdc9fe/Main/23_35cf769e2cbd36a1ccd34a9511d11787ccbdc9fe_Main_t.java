 /*
  * Copyright 2012 Adrian Micayabas <deepspace30@gmail.com>
  * This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package STG;
 
 import STG.States.OpenSplashState;
 import STG.States.StateNode;
 import STG.filters.FadeFilterConst;
 import STG.shot.BendShot;
 import STG.shot.Bullet;
 import STG.shot.CurveShot1;
 import STG.shot.S3S2Shot;
 import STG.shot.S3S4Shot;
 import STG.shot.ScaleShot;
 import STG.shot.Shrinker;
 import STG.shot.StraightShot;
 import STG.shot.UncannySealShot;
 import STG.shot.VarSpeedShot;
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.LoopMode;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.audio.AudioNode;
 import com.jme3.collision.CollisionResult;
 import com.jme3.collision.CollisionResults;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.effect.shapes.EmitterBoxShape;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapFont.Align;
 import com.jme3.font.BitmapText;
 import com.jme3.font.Rectangle;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.PointLight;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.material.RenderState.FaceCullMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.BloomFilter;
 import com.jme3.post.filters.DepthOfFieldFilter;
 import com.jme3.post.filters.FadeFilter;
 import com.jme3.post.filters.LightScatteringFilter;
 import com.jme3.post.filters.RadialBlurFilter;
 import com.jme3.renderer.Camera;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Mesh;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.Spatial.CullHint;
 import com.jme3.scene.VertexBuffer;
 import com.jme3.scene.shape.Box;
 import com.jme3.system.AppSettings;
 import com.jme3.texture.Texture;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Main extends SimpleApplication {
     OpenSplashState openSplashState;
     
     AudioNode music;
     float timescale = 1;
     //Music:
     //menu: IN 00
     //s1:IN 07
     //s2:PCB 01
     //s3:PCB 10
     //s4:PCB 09
     Geometry mark;
     Geometry menuMark;
     //CONSTANTS
     public enum BULLET {
         TALISMAN_R, TALISMAN_B, TALISMAN_W,
         BALLSHOT_R, BALLSHOT_B, BALLSHOT_W, BALLSHOT_P,
         BEAMSHOT_R, BEAMSHOT_B,
         PILLSHOT_R, PILLSHOT_W,
         PETALSHOT_R,
         ARROWSHOT_R, ARROWSHOT_B, ARROWSHOT_P, ARROWSHOT_T, ARROWSHOT_G, ARROWSHOT_W, ARROWSHOT_O, ARROWSHOT_Y,
         KNIFE_B, KNIFE_W, KNIFE_K;
     }
     //5 State nodes
     //StateNode openSplashState;
     StateNode mainMenuState;
     StateNode gameStartState;
     StateNode gameState;
     StateNode endGameState;
 
     //Button nodes.  Buttons should be attached to menus
     ButtonNode startButton;
     ButtonNode optionsButton;
     ButtonNode exitButton;
     
     //Panel nodes
     //  Open Splash
     //PanelNode openSplashPanel;
     //  Main Menu
     PanelNode titlePanel;
     GameObject titleBackground;
     Node titleUIElements;
     //  Game Start
     PanelNode gameStartLoadingPanel;
     PanelNode gameStartBgPanel;
 
     //Filters
     BloomFilter bloomFilter;
     FadeFilterConst fadeFilter;
     RadialBlurFilter radialBlur;
     LightScatteringFilter scatterFilter;
     FilterPostProcessor filtPostProc;
 
     //State Constants
     enum STATE {
         PREGAME, OPENSPLASH, MAINMENU, START, GAME, END;
         public String next() {
             switch(this) {
                 case PREGAME: return "OPENSPLASH";
                 case OPENSPLASH: return "MAINMENU";
                 case MAINMENU: return "START";
                 case START: return "GAME";
                 case GAME: return "END";
                 case END: return "MAINMENU";
                 default: return "PREGAME";
             }
         }
         public STATE nextState() {
             switch(this) {
                 case PREGAME: return OPENSPLASH;
                 case OPENSPLASH: return MAINMENU;
                 case MAINMENU: return START;
                 case START: return GAME;
                 case GAME: return END;
                 case END: return MAINMENU;
                 default: return PREGAME;
             }
         }
     }
     /*final int STATE_OPENSPLASH=0;
     final int STATE_MAINMENU=1;
     final int STATE_START=2;
     final int STATE_GAME=3;
     final int STATE_END=4;*/
 
     //Main menu variables
     final static int mainMenuSize = 1; //Index of last item in menu
     final static int MAINMENU_NULL = -1;
     final static int MAINMENU_START = 0; //int codes for each button
     final static int MAINMENU_EXIT = 1;
     int curMainMenuItem = 0;
     //Main menu background
     Texture[] mainMenuFrame;
     final static int MAINMENU_FRAMES = 26;
     float mainMenuFrameRate = 0.15f;
     float mainMenuTime;
     int mainMenuCurFrame = 1;
 
     //Game indicators and switches
     //int currentGameState;  //Current game state, out of 5 possible states.
     STATE currentGameState;
     boolean advanceEventTime;   //Event line paused
     boolean spellcardActive;    //Stores whether a spellcard is active
     boolean stageActive;        //Like spellcardActive, but only for stagen_0
    boolean debug = false; //skips to game
     boolean mute = true; //mutes
     boolean gamePause = false;      //Stores whether game paused
     boolean gameOverFlag = false;   //Stores whether game over
     boolean gameMenuActive = false; //In game menu is active
     boolean showDialogue = false;
     boolean dialogueActive = false; //Someone is talking
     boolean dialoguePlayer = false; //Player is talking
     boolean dialogueEnemy = false;  //Enemy is talking
     boolean gameWin = false;        //Game beaten
     
     //Player variables
     boolean MOVE_STAND = true;  //Not moving
     boolean MOVE_UP = false;    //Moving up
     boolean MOVE_RIGHT = false; //Moving right
     boolean MOVE_DOWN = false;  //Moving down
     boolean MOVE_LEFT = false;  //Moving left
     boolean playerFocus;    //Stores whether or not in focus mode
     Spatial hitboxModel;      //Hitbox mesh data
     GameObject hitbox;        //Hitbox Geometry
     Spatial grazeboxModel;    //Grazebox mesh data
     GameObject grazebox;      //Grazebox Geometry
     Material grazeboxMat;   //Grazebox Material
     Material hitboxMat;     //Hitbox Material
 
     Spatial enemyHitboxModel;
     GameObject enemyHitbox;
     Material enemyHitboxMat;
 
     //Game variables
     int maxLife = 100;   //Life: how many times you can get hit
     int graze = 0;  //Graze: how many bullets pass nearby
     int grazeChange = 0;
     int heat;   //Heat:  Gained by graze, lost over time.  Used to attack
     int heatState = 0; //HEAT: 0, 1, 2, 3
     int heatMax = 2000;
     float heatLossRate = 70f;
     float heatGainRate = 0.10f;
     int stage = 1;
     int spell = 1;
     
     int timerCount = 128;
     float timer[] = new float[timerCount];
 
     final int T_EVENT_TIME = 0;  //Timeline: events
     final int T_CARD_TIME = 1;  //Timeline: spell card
     final int T_AFTER_STATE_TIME = 2;//Stores the amount of time passed after a given state.
     final int T_AFTER_DIALOGUE_TIME = 3;  //Stores the amount of time passed after dialogue.
     final int T_INTRO_TIME = 4; //Intro timer
     final int T_MAINMENU_TIME = 5;
     final float MAINMENU_MIN_TIME = 2;
 
     boolean stateFade;  //Stores whether or not the screen is faded.
     float windowScale = 1f;
     float transitionTime = 0.6f; //Transition time between game states.
     int screenWidth = Math.round(1280/windowScale);  //Horizontal res
     int screenHeight = Math.round(720/windowScale); //Vertical res
 
     final static float playerMaxDistance = 90;
     final static float playerMinDistance = -50;
     final static float playerMaxSide = 55;
 
     //GameStart variables
     float loadTime = 0;
 
     //Pause menu variables
     int pauseMenuSize = 2; //Index of last item in menu
     final int GAMEMENU_NULL = -1;
     final int GAMEMENU_RETRY = 1; //int codes for each button
     final int GAMEMENU_CONTINUE = 0;
     final int GAMEMENU_RETURN = 2;
     float pauseButtonIndentation = 0.2f;
     float pauseRetryIndent = 0f;
     float pauseContinueIndent = 0f;
     float pauseReturnIndent = 0f;
     int curGameMenuItem = -1;
     int continueCount = 0;
     int MAX_CONTINUE = 2;
 
     //  Playfield bounds
     float maxBulletDistance = 150;
     float camOffset = 0;
     float camDistance = 30;
     float camMaxDistance = 20;
     float camMinDistance = 12;
     float camHeight = 30;
     float camSpeed = 0.001f;
 
     Vector3f gameFocalPoint = new Vector3f(0, 0, -90);
     Vector3f gameCamLoc = new Vector3f(0,80,180);
     Vector3f gameMouseLoc = new Vector3f();
     Vector3f playerLoc = new Vector3f();
     final static Vector3f StartPosVector = new Vector3f(0,70,0);
     Box gamePlane = new Box(200,300,-1);
     Geometry gamePlaneGeom = new Geometry("gamePlane", gamePlane);
     
     //Loaded game assets
     //  Main Menu
     Spatial startButtonModel;
     Material startButtonMat;
     Spatial exitButtonModel;
     Material exitButtonMat;
 
     float[] titleAlpha;
     //1: main title
     //2: start
     //3: exit
     
     //  Game
     Spatial skySphereModel;
     Material skySphereMat;
     Spatial backdropModel;
     Material backdropMat;
 
     Spatial moonModel;
     Material moonMat;
 
     Spatial playerModel;
     Material playerMat;
     Spatial enemyModel;
     Material enemyMat;
 
     //Pause Menu assets
     
     float[] menuAlpha = new float[9];
     //1: pause 2: gameover 3: continue 4: retry 5: return
     GuiImage menuPause;
     Box menuPauseModel;
     
     GuiImage menuGameOver;
     Box menuGameOverModel;
     
     GuiImage menuContinue;
     Box menuContinueModel;
     
     GuiImage menuRetry;
     Box menuRetryModel;
     
     GuiImage menuReturn;
     Box menuReturnModel;
     
     //Bullets
     Spatial talismanW;
     Material talismanWMat;
     Spatial talismanR;
     Material talismanRMat;
 
     Spatial ballShotW;
     Material ballShotWMat;
     Spatial ballShotR;
     Material ballShotRMat;
     Spatial ballShotB;
     Material ballShotBMat;
     Spatial ballShotP;
     Material ballShotPMat;
     
     Spatial pillShotR;
     Material pillShotRMat;
 
     Spatial petalShotR;
     Material petalShotRMat;
 
     //Arrows
     Spatial arrowShotR;
     Material arrowShotRMat;
     Spatial arrowShotB;
     Material arrowShotBMat;
     Spatial arrowShotG;
     Material arrowShotGMat;
     Spatial arrowShotP;
     Material arrowShotPMat;
     Spatial arrowShotT;
     Material arrowShotTMat;
     Spatial arrowShotW;
     Material arrowShotWMat;
     Spatial arrowShotO;
     Material arrowShotOMat;
     Spatial arrowShotY;
     Material arrowShotYMat;
     
     //Knifes
     Spatial knifeB;
     Material knifeBMat;
     Spatial knifeK;
     Material knifeKMat;
     Spatial knifeW;
     Material knifeWMat;
     
     //Familiars
     Spatial hourai;
     Material houraiMat;
     Spatial hourailance;
     Spatial houraisword;
     float houraiAlpha;
     
     //In-game objects
     //Background
     GameObject skySphere;
     GameObject backdrop;
     GameObject moon;
     GameObject ground1;
     GameObject ground2;
     GameObject ground3;
     GameObject camFocalPoint;
     GameObject camLoc;
 
     //Foreground
     Player player;
     Enemy enemy;
     Node bulletNode;  //Bullet node to which we will affix all bullets shot by enemies
     Node shotNode;  //Shot node: player shots are attached here. [03 27 2011]
     Node objectNode;  //Object node, where game objects go.  For now,
                         //just the player and enemy.
     Node backNode;  //Where the sky/ground goes.
 
     //Animation
     AnimChannel playerAnimChan;
     AnimControl playerAnimCont;
 
     AnimChannel enemyAnimChan;
     AnimControl enemyAnimCont;
 
     //Text
     BitmapText dialogue;
     Rectangle dialogueBounds;
     BitmapText scoreReading;
 
     BitmapText lifeDisplay;
     BitmapText lifeReading;
 
     BitmapText grazeDisplay;
     BitmapText grazeReading;
 
     BitmapText timerDisplay;
     BitmapText timerDisplaySeconds;
 
     BitmapText heatDisplay;
     BitmapText heatReading;
     
     BitmapText enemyDisplay;
     BitmapText enemyReading;
     BitmapText stageDisplay;
     BitmapText stageReading;
     BitmapText spellDisplay;
     BitmapText spellReading;
     /*BitmapText stageClearDisplay1;
     BitmapText stageClearDisplay2;
     BitmapText stageClearDisplay3;*/
     float displayAlpha = 0;
     ColorRGBA displayColor;
 
     //GUI
     GuiImage dialogueNode;
     Box dialogueNodeModel;
     Material dialoguePaneMat;
     
     GuiImage portraitPlayer;
     GuiImage portraitEnemy;
     Box portraitPlayerModel;
     Box portraitEnemyModel;
 
     GuiImage cutPlayer;
     GuiImage cutEnemy;
     Box cutEnemyModel;
     Material cutEnemyMat;
     float cutEnemyAlpha = 0;
 
     GuiImage introBanner;
     Box introBannerModel;
     Material introBannerMat;
     float introBannerAlpha;
 
     Geometry spellcardBanner;
     Box spellcardBannerModel;
     Material spellcardBannerMat;
     float spellcardBannerAlpha = 0;
 
     GuiImage screenFadeOverlay;
     Box screenFadeOverlayModel;
     Material screenFadeOverlayMat;
     float screenFadeOverlayAlpha = 0;
     
     //Controls
     BackdropControl ground1Control;
     BackdropControl ground2Control;
     BackdropControl ground3Control;
 
     DirectionalLight bulletLight;
 
     ParticleEmitter enemyDeathEmitter;
     ParticleEmitter playerDeathEmitter;
     ParticleEmitter playerHeatEmitter;
     ParticleEmitter playerFocusEmitter;
     float focusTimer = 0;
     float focusLimit = 200;
     //---------------------------------------------------------
 
     //Game end
     GuiImage endgameImage;
     Box endgameImageModel;
     Material endgameImageMat;
     
     BitmapText endgameText;
     Rectangle endgameTextBounds;
     
     PanelNode endBackground;
     Spatial endBackgroundModel;
     Material endBackgroundMat;
     
     public static void main(String[] args) {
         Main app = new Main();
         app.setShowSettings(false);
         app.setDisplayFps(false);
         app.setDisplayStatView(false);
         
         AppSettings settings = new AppSettings(true);
         settings.setFrameRate(60);
         Logger.getLogger("com.jme3").setLevel(Level.SEVERE);
         settings.put("Width", app.screenWidth);
         settings.put("Height", app.screenHeight);
         settings.put("Title", "midnight sun v0.1");
         app.setSettings(settings);
         app.start();
     }
 
     //Main init method.  Initialized the application.
     public void simpleInitApp() {
         flyCam.setEnabled(false);
         currentGameState = STATE.PREGAME;
         //currentGameState = STATE.END;
                 
         //inputManager.clearMappings();
         //inputManager.removeListener(flyCam);
         inputManager.setCursorVisible(true);
         guiNode.detachAllChildren();
         filtPostProc = new FilterPostProcessor(assetManager);
 
         fadeFilter = new FadeFilterConst();
        fadeFilter.setDuration(transitionTime);
         filtPostProc.addFilter(fadeFilter);
         /*
         DepthOfFieldFilter dofFilter = new DepthOfFieldFilter();
         dofFilter.setFocusDistance(0);
         dofFilter.setFocusRange(150);
         dofFilter.setBlurScale(1.2f);
         filtPostProc.addFilter(dofFilter);*/
         viewPort.addProcessor(filtPostProc);
         
         //Prepare main menu background movie frames
         mainMenuFrame = new Texture[MAINMENU_FRAMES+1];
         for(int i = 1; i <= MAINMENU_FRAMES; i++) {
             TextureKey key = new TextureKey("Textures/mainMenu/frame" + i + ".png", false);
             mainMenuFrame[i] = assetManager.loadTexture(key);
         }
         initMark();
         if(debug) {
             currentGameState = STATE.START;
             initGameStart();
             return;
         }
         fadeFilter.fadeOut();
     }
 
     //Init methods for each state.  Entail attaching and detaching state nodes
     //  from rootnode, and managing key binds specific to each state.
     public void initOpenSplash() {
         System.out.println("initializing state " + currentGameState);
         //Object organization:
         //  root -> state -> Panel -> spatial
         openSplashState = new OpenSplashState("openSplashNode", rootNode, assetManager, inputManager, cam);
         openSplashState.init();
     }
 
     private void setBackgroundMusic(String file) {
         if(music != null) {
             music.stop();
         }
         if(!mute) {
         try {
             music = new AudioNode(assetManager, file, false);
             music.setLooping(true);
             rootNode.attachChild(music);
             music.setVolume(0.5f);
             music.play();
         } catch(Exception ex) {}
         }
     }
     //Initiates main menu state.
     public void initMainMenu() {
         //Clear root node children before starting.
         //Object organization:
         //  root -> state -> title -> spatial
         //  root>state>button>spatial
         //  root>state>button>spatial
         //  root>state>background>spatial
         //inputManager.setCursorVisible(false);
         System.out.println("initializing state " + currentGameState);
         rootNode.detachAllChildren();
         guiNode.detachAllChildren();
         
         //Moving Camera
         cam.setLocation(new Vector3f(0f,0f,15f));
         cam.setRotation(Quaternion.IDENTITY);
         cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
         mainMenuState = new StateNode("mainMenuNode");
         mainMenuState.detachAllChildren();
         setBackgroundMusic("Sounds/menu.ogg");
         titleAlpha = new float[9];
 
         titleBackground = new PanelNode("titleBackgroundPanel");
         titleBackground.setModel(assetManager.loadModel("Models/mainMenu/movie.j3o"));
         titleBackground.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         titleBackground.getMat().setTexture("ColorMap", assetManager.loadTexture("Textures/mainMenu/frame1.png"));
         //titleBackgroundMat.getAdditionalRenderState().setBlendMode(BlendMode.Off);
         titleBackground.getMat().getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         titleBackground.setLocalTranslation(0,0,0);
         titleBackground.move(0,0,-8f);
         titleUIElements = new Node("titleUIElements");
         titleUIElements.move(0,-2,0);
         mainMenuState.attachChild(titleBackground);
         titleBackground.getMat().setTexture("ColorMap", mainMenuFrame[1]);
 
         titlePanel = new PanelNode("titleLable");
         titlePanel.setModel(assetManager.loadModel("Models/mainMenu/title.j3o"));
         titlePanel.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         TextureKey titlePanelMatTextureKey = new TextureKey("Textures/mainMenu/titleTex.png", false);
         Texture titlePanelMatTex = assetManager.loadTexture(titlePanelMatTextureKey);
         titlePanel.getMat().setTexture("ColorMap", titlePanelMatTex);
         titlePanel.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         titlePanel.getMat().setColor("Color", new ColorRGBA(1,1,1, titleAlpha[0]));
         titlePanel.move(-0.5f,0,0);
 
         titleUIElements.attachChild(titlePanel);
 
         startButton = new ButtonNode("startButton");
         startButtonModel = assetManager.loadModel("Models/mainMenu/startButton.j3o");
         startButton.move(-0.5f,2,0);
         startButtonMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         startButtonMat.setTexture("ColorMap", titlePanelMatTex);
         startButtonMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         startButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[1]));
 
         startButtonModel.setMaterial(startButtonMat);
         startButton.attachChild(startButtonModel);
         titleUIElements.attachChild(startButton);
 
         exitButton = new ButtonNode("exitButton");
         exitButton.move(-0.5f,3,0);
         exitButtonModel = assetManager.loadModel("Models/mainMenu/exitButton.j3o");
         exitButtonMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         exitButtonMat.setTexture("ColorMap", titlePanelMatTex);
         exitButtonMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         exitButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[2]));
 
         exitButtonModel.setMaterial(exitButtonMat);
         exitButton.attachChild(exitButtonModel);
         titleUIElements.attachChild(exitButton);
 
         titleUIElements.move(-4,1,0);
         titleUIElements.setQueueBucket(Bucket.Translucent);
         mainMenuState.attachChild(titleUIElements);
         DirectionalLight mainMenuLight = new DirectionalLight();
         mainMenuLight.setDirection(new Vector3f(0f, 0f, -1.0f));
         mainMenuLight.setColor(ColorRGBA.White);
         DirectionalLight sun = new DirectionalLight();
         Vector3f lightDir = new Vector3f(0,0,1);
         sun.setDirection(lightDir);
         sun.setColor(ColorRGBA.White.clone().multLocal(2));
         //mainMenuState.addLight(mainMenuLight);
         mainMenuState.addLight(sun);
         //cam.lookAt(mainMenuState.getLocalTranslation(),Vector3f.UNIT_Y);
         rootNode.attachChild(mainMenuState);
 
         lastX = inputManager.getCursorPosition().x;
         lastY = inputManager.getCursorPosition().y;
         menuMark.setLocalTranslation(screenWidth/2, screenHeight / 2, 0);
         inputManager.setCursorVisible(false);
         
        //filtPostProc.removeFilter(radialBlur);
         initMainMenuBindings();
     }
 
     public void initMainMenuBindings() {
         //Set up key binds for main menu
         inputManager.addMapping("select", new KeyTrigger(KeyInput.KEY_Z));
         inputManager.addMapping("mselect", new MouseButtonTrigger(0));
         inputManager.addListener(mainMenuListener, new String[]{"select","mselect"});
     }
 
     public void initGameStart() {
         System.out.println("initializing state " + currentGameState + " (gamestart)");
         rootNode.detachAllChildren();
         guiNode.detachAllChildren();
         gameStartState = new StateNode("gameStartState");
         setBackgroundMusic("");
         
         //Create and attach 'loading' text
         gameStartLoadingPanel = new PanelNode("gamestartloading");
         gameStartLoadingPanel.setModel(assetManager.loadModel("Models/gameStart/logo.j3o"));
         gameStartLoadingPanel.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         gameStartLoadingPanel.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Models/gameStart/logo.png",false)));
         gameStartLoadingPanel.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
 
         gameStartState.attachChild(gameStartLoadingPanel);
        gameStartLoadingPanel.move(7,3,0);
         
         //Create and attach background panel
         gameStartBgPanel = new PanelNode("gamestartbg");
         gameStartBgPanel.setModel(assetManager.loadModel("Models/mainMenu/movie.j3o"));
         gameStartBgPanel.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         gameStartBgPanel.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Models/gameStart/gameStartBg.png", false)));
         
         gameStartState.attachChild(gameStartBgPanel);
         
         gameStartBgPanel.getMat().setColor("Color", new ColorRGBA(0.2f,0.2f,0.3f, 0));
         gameStartBgPanel.move(-2,0,-6);
         
         //Add lighting
         DirectionalLight mainMenuLight = new DirectionalLight();
         mainMenuLight.setDirection(new Vector3f(0f, -0f, -1.0f));
         mainMenuLight.setColor(ColorRGBA.White);
 
         gameStartState.addLight(mainMenuLight);
         
         cam.lookAt(gameStartState.getLocalTranslation(),Vector3f.UNIT_Y);
         
         //Attach game start state to root node
         rootNode.attachChild(gameStartState);
 
         //Set up key binds for game start
         inputManager.addMapping("advance", new MouseButtonTrigger(0));
         inputManager.addListener(gameStartListener, new String[]{"advance"});
         
         //Particle emitter for game start screen
         ParticleEmitter gameStartEmitter = new ParticleEmitter();
         gameStartEmitter = new ParticleEmitter("gameStartEmitter", ParticleMesh.Type.Triangle, 100);
         Material mat_red = new Material(assetManager, "MatDefs/Particle.j3md");
         mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/leaf.png"));
         mat_red.getAdditionalRenderState().setBlendMode(BlendMode.Additive);
         gameStartEmitter.setQueueBucket(Bucket.Translucent);
         gameStartEmitter.setMaterial(mat_red);
         gameStartEmitter.setParticlesPerSec(20);
         gameStartEmitter.setImagesX(2); gameStartEmitter.setImagesY(1);
         gameStartEmitter.setEndColor(new ColorRGBA(0.05f, 0f, 0f, 0.1f));   // red
         gameStartEmitter.setStartColor(new ColorRGBA(0.2f, 0.1f, 0.3f, 0.1f)); // yellow
         gameStartEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(-0.2f,-0.6f,0));
         gameStartEmitter.setGravity(2,5,0);
         gameStartEmitter.setRotateSpeed(6);
         gameStartEmitter.setShape(new EmitterBoxShape(new Vector3f(0,0,0), new Vector3f(5,0,0)));
         gameStartEmitter.getParticleInfluencer().setVelocityVariation(0.5f);
         gameStartEmitter.setStartSize(0.2f);
         gameStartEmitter.setEndSize(0.1f);
         gameStartEmitter.setLowLife(0.5f);
         gameStartEmitter.setHighLife(0.9f);
         gameStartEmitter.setVelocityVariation(0.1f);
        gameStartEmitter.move(-1.7f,0.8f,0);
         gameStartState.attachChild(gameStartEmitter);
         
     }
 
     private void loadAssets() {
         hourai = assetManager.loadModel("/Models/game/hourai.j3o");
         houraiMat = new Material(assetManager, "/MatDefs/Unshaded.j3md");
         houraiMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("/Textures/game/hourai.png",false)));
         hourai.setMaterial(houraiMat);
         
         hourailance = assetManager.loadModel("Models/game/hourailance.j3o");
         hourailance.setMaterial(houraiMat);
         
         houraisword = assetManager.loadModel("Models/game/houraisword.j3o");
         houraisword.setMaterial(houraiMat);
         houraiMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         houraiMat.setColor("Color", new ColorRGBA(1,1,1, 0));
         hourai.setMaterial(houraiMat);
         
         talismanW = assetManager.loadModel("Models/game/cardshot.j3o");
         //talismanWMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         //talismanWMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/bullets/talismanW.png",false)));
         talismanWMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         talismanWMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/bullets/talismanW.png"));
         talismanWMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/talismanW.png"));
         talismanWMat.setColor("m_GlowColor", ColorRGBA.White);
         talismanWMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         talismanW.setMaterial(talismanWMat);
 
         talismanR = assetManager.loadModel("Models/game/cardshot.j3o");
         talismanRMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         talismanRMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/bullets/talismanR.png"));
         talismanRMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/talismanR.png"));
         talismanRMat.setColor("m_GlowColor", ColorRGBA.White);
         talismanRMat.setColor("m_Ambient", ColorRGBA.White);
         talismanRMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         talismanR.setMaterial(talismanRMat);
 
         ballShotW = assetManager.loadModel("/Models/game/ballshot.j3o");
         ballShotWMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         ballShotWMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/roundShotW.png"));
         ballShotWMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/roundShotW.png"));
         ballShotWMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
 
         ballShotR = assetManager.loadModel("Models/game/ballshot.j3o");
         ballShotRMat = new Material(assetManager, "/MatDefs/Lighting.j3md");
         ballShotRMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/roundShotR.png"));
         ballShotRMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/roundShotR.png"));
         ballShotRMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
 
         ballShotB = assetManager.loadModel("Models/game/ballshot.j3o");
         ballShotBMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         ballShotBMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/roundShotB.png"));
         ballShotBMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/roundShotB.png"));
         ballShotBMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
 
         ballShotP = assetManager.loadModel("Models/game/ballshot.j3o");
         ballShotPMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         ballShotPMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/bullets/arrowP.png"));
         ballShotPMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowP.png"));
         ballShotPMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         pillShotR = assetManager.loadModel("Models/game/pillShot.j3o");
         pillShotRMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         pillShotRMat.setTexture("m_DiffuseMap", assetManager.loadTexture("Textures/game/roundShotR.png"));
         pillShotRMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/roundShotR.png"));
 
         petalShotR = assetManager.loadModel("Models/game/petalshotR.j3o");
         petalShotRMat = talismanRMat;
 
         arrowShotR = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotRMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotRMatTextureKey = new TextureKey("Textures/game/bullets/arrowR.png", false);
         arrowShotRMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotRMatTextureKey));
         arrowShotRMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowR.png"));
         arrowShotRMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
 
         arrowShotB = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotBMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotBMatTextureKey = new TextureKey("Textures/game/bullets/arrowB.png", false);
         arrowShotBMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotBMatTextureKey));
         arrowShotBMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowB.png"));
         arrowShotBMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         arrowShotP = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotPMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotPMatTextureKey = new TextureKey("Textures/game/bullets/arrowP.png", false);
         arrowShotPMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotPMatTextureKey));
         arrowShotPMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowP.png"));
         arrowShotPMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         arrowShotT = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotTMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotTMatTextureKey = new TextureKey("Textures/game/bullets/arrowT.png", false);
         arrowShotTMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotTMatTextureKey));
         arrowShotTMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowT.png"));
         arrowShotTMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         arrowShotG = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotGMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotGMatTextureKey = new TextureKey("Textures/game/bullets/arrowG.png", false);
         arrowShotGMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotGMatTextureKey));
         arrowShotGMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowG.png"));
         arrowShotGMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         arrowShotW = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotWMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotWMatTextureKey = new TextureKey("Textures/game/bullets/arrowW.png", false);
         arrowShotWMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotWMatTextureKey));
         arrowShotWMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowW.png"));
         arrowShotWMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         arrowShotO = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotOMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotOMatTextureKey = new TextureKey("Textures/game/bullets/arrowO.png", false);
         arrowShotOMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotOMatTextureKey));
         arrowShotOMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowO.png"));
         arrowShotOMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         arrowShotY = assetManager.loadModel("Models/game/arrow.j3o");
         arrowShotYMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey arrowShotYMatTextureKey = new TextureKey("Textures/game/bullets/arrowY.png", false);
         arrowShotYMat.setTexture("m_DiffuseMap", assetManager.loadTexture(arrowShotYMatTextureKey));
         arrowShotYMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/arrowY.png"));
         arrowShotYMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         knifeB = assetManager.loadModel("Models/game/bullets/knife.j3o");
         knifeBMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey knifeBMatTextureKey = new TextureKey("Textures/game/bullets/knifeB.png", false);
         knifeBMat.setTexture("m_DiffuseMap", assetManager.loadTexture(knifeBMatTextureKey));
         knifeBMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/knifeB.png"));
         knifeBMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
                 knifeK = assetManager.loadModel("Models/game/bullets/knife.j3o");
                 
         knifeKMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey knifeKMatTextureKey = new TextureKey("Textures/game/bullets/knifeK.png", false);
         knifeKMat.setTexture("m_DiffuseMap", assetManager.loadTexture(knifeKMatTextureKey));
         knifeKMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/knifeK.png"));
         knifeKMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
                 knifeW = assetManager.loadModel("Models/game/bullets/knife.j3o");
                 
         knifeWMat = new Material(assetManager, "MatDefs/Lighting.j3md");
         TextureKey knifeWMatTextureKey = new TextureKey("Textures/game/bullets/knifeW.png", false);
         knifeWMat.setTexture("m_DiffuseMap", assetManager.loadTexture(knifeWMatTextureKey));
         knifeWMat.setTexture("m_GlowMap", assetManager.loadTexture("Textures/game/bullets/knifeW.png"));
         knifeWMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         knifeB.setMaterial(knifeBMat);
         knifeK.setMaterial(knifeKMat);
         knifeW.setMaterial(knifeWMat);
         
         ballShotW.setMaterial(ballShotWMat);
         ballShotR.setMaterial(ballShotRMat);
         ballShotB.setMaterial(ballShotBMat);
         ballShotP.setMaterial(ballShotPMat);
         
         pillShotR.setMaterial(pillShotRMat);
         petalShotR.setMaterial(petalShotRMat);
         
         arrowShotR.setMaterial(arrowShotRMat);
         arrowShotB.setMaterial(arrowShotBMat);
         arrowShotP.setMaterial(arrowShotPMat);
         arrowShotT.setMaterial(arrowShotTMat);
         arrowShotG.setMaterial(arrowShotGMat);
         arrowShotW.setMaterial(arrowShotWMat);
         arrowShotO.setMaterial(arrowShotOMat);
         arrowShotY.setMaterial(arrowShotYMat);
         
         cutEnemyModel = new Box(100, 200, 0);
         cutEnemy = new GuiImage("cutEnemy", cutEnemyModel);
         cutEnemy.setWidth((int) cutEnemyModel.getXExtent());
         cutEnemy.setHeight((int) cutEnemyModel.getYExtent());
         cutEnemyMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         cutEnemyMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait1.png"));
         cutEnemyMat.setColor("Color", new ColorRGBA(1, 1, 1, cutEnemyAlpha));
         cutEnemyMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         //portraitEnemy.setQueueBucket(Bucket.Transparent);
         cutEnemy.setMaterial(cutEnemyMat);
         //portraitEnemy.move(screenWidth + portraitEnemy.getWidth(), portraitEnemy.getHeight()/2, 0);
         cutEnemy.move(screenWidth - 100, -200, 0);
         cutEnemy.scale(2);
         cutEnemyAlpha = 0;
         guiNode.attachChild(cutEnemy);
         
         spellcardBannerModel = new Box(64,1024,1);
         spellcardBanner = new Geometry("spellcardBanner", spellcardBannerModel);
         spellcardBannerMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         Texture spellcardBannerTex = assetManager.loadTexture(new TextureKey("Textures/game/text/banner.png"));
         spellcardBannerMat.setTexture("ColorMap", spellcardBannerTex);
         spellcardBannerAlpha = 0;
         spellcardBannerMat.setColor("Color", new ColorRGBA(1,1,1,spellcardBannerAlpha));
         spellcardBannerMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         spellcardBanner.setMaterial(spellcardBannerMat);
         guiNode.attachChild(spellcardBanner);
         spellcardBanner.setLocalTranslation(230,500,0);
         spellcardBanner.setCullHint(CullHint.Never);
 
         screenFadeOverlayModel = new Box(screenWidth, screenHeight, 0);
         screenFadeOverlay = new GuiImage("screenFadeOverlay", screenFadeOverlayModel);
         screenFadeOverlayMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         screenFadeOverlayMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/black.png"));
         screenFadeOverlayMat.setColor("Color", new ColorRGBA(0,0,0,screenFadeOverlayAlpha));
         screenFadeOverlayMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         screenFadeOverlay.setMaterial(screenFadeOverlayMat);
         screenFadeOverlay.move(screenWidth/2, screenHeight/2, 10);
         guiNode.attachChild(screenFadeOverlay);
         System.out.println("Assets Loaded");
     }
 
     public void initGameResetVars() {
         //Reset game variables
         //Make more convenient later.
         for(int i = 0; i < timerCount; i++) {
             timer[i] = 0;
         }
         for(int i = 0; i < flagCount; i++) {
             gameFlag[i] = false;
         }
         spawnDone = false;
         gamePause = false;
         gameOverFlag = false;
         gameMenuActive = false;
         dialogueActive = false;
         showDialogue = false;
         dialoguePlayer = false;
         dialogueEnemy = false;
         spawnDone = false;
         gameWin = false;
         for(int i = 0; i < dialogueCount; i++) {
             dialogueFlag[i] = false;
         }
         spellcardActive = false;
         stageActive = false;
         advanceEventTime = false;
 
         introDone = false;
         //Spellcard vars
         squareCreated = false;
         resetCardVars();
         try {
             player.setLife(player.MAX_LIFE);
         } catch(Exception ex) {
             //Player doesn't exist yet
         }
     }
 
     public void initGame() {
         System.out.println("initializing state " + currentGameState  + " (game)");
         rootNode.detachAllChildren();
         guiNode.detachAllChildren();
         loadAssets();
         continueCount = 0;
         cam.setFrustumFar(0);
         cam.setFrustumNear(0);
         inputManager.setCursorVisible(false);
         initGameResetVars();
         initMark();
         introBannerAlpha = 0;
         gameState = new StateNode("gameState");
         objectNode = new Node("objectNode");
         bulletNode = new Node("bulletNode");
         shotNode = new Node("shotNode");
         backNode = new Node("backNode");
         //Object organization:
         //  root -> state -> skysphere -> assets
         //  state -> backdrop -> assets
         //  state -> player -> assets
         //  state -> enemy -> assets
         //  state -> bullets -> bullet -> assets
         //Used for setting up the game's 'stage'.
         //SET UP SKYSPHERE
         /*
         skySphere = new GameObject("skySphere");
         skySphereModel = assetManager.loadModel("Models/game/skySphere.j3o");
         skySphereMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         skySphere.setMaterial(skySphereMat);
         skySphere.attachChild(skySphereModel);
         backNode.attachChild(skySphere);
          */
         //SET UP MOON
         createMoon();
 
         //SET UP GROUND
         createGround();
 
         //LIGHTING
         PointLight gameLight = new PointLight();
         gameLight.setPosition(new Vector3f(0,30,250));
         gameLight.setColor(ColorRGBA.White);
 
         DirectionalLight sun = new DirectionalLight();
         Vector3f lightDir = new Vector3f(0,1,-1);
         sun.setDirection(lightDir);
         sun.setColor(ColorRGBA.White.clone().multLocal(2));
 
         objectNode.addLight(gameLight);
         backNode.addLight(gameLight);
         backNode.addLight(sun);
         objectNode.addLight(sun);
         moon.addLight(gameLight);
         
         bulletLight = new DirectionalLight();
         bulletLight.setDirection(new Vector3f(0,0,-1));
         bulletLight.setDirection(lightDir);
         bulletLight.setColor(ColorRGBA.White);
 
         bulletNode.addLight(bulletLight);
         //bulletNode.addLight(gameLight);
 
         gameState.attachChild(objectNode);
         gameState.attachChild(backNode);
         rootNode.attachChild(gameState);
         gameState.attachChild(bulletNode);
         gameState.attachChild(shotNode);
 
         //Set up pause menu.
         
         for(int i = 0; i < 9; i++) {
             if(menuAlpha[i] < 0) {
                 menuAlpha[i] = 0;
             }
         }
         try{guiNode.detachChild(menuPause);guiNode.detachChild(menuGameOver);
         guiNode.detachChild(menuRetry);guiNode.detachChild(menuContinue);guiNode.detachChild(menuReturn);}catch(Exception ex){}
         menuPauseModel = new Box(64,14,0.5f);
         menuPause = new GuiImage("menuPause",menuPauseModel);
         menuPause.move(screenWidth/2,370,20);
         menuPause.scale(1.8f);
         menuPause.setMat(new Material(assetManager,"MatDefs/Unshaded.j3md"));
         menuPause.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/pause/pause.png", true)));
         menuPause.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[1]));
         menuPause.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         guiNode.attachChild(menuPause);
         
         menuGameOverModel = new Box(64,14,0.5f);
         menuGameOver = new GuiImage("menuGameOver",menuGameOverModel);
         menuGameOver.move(screenWidth/2,370,20);
         menuGameOver.scale(1.8f);
         menuGameOver.setMat(new Material(assetManager,"MatDefs/Unshaded.j3md"));
         menuGameOver.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/pause/gameover.png", true)));
         menuGameOver.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[2]));
         menuGameOver.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         guiNode.attachChild(menuGameOver);
         
         menuRetryModel = new Box(64,14,0.5f);
         menuRetry = new GuiImage("menuRetry",menuRetryModel);
         menuRetry.move(screenWidth/2,340,20);
         menuRetry.setMat(new Material(assetManager,"MatDefs/Unshaded.j3md"));
         menuRetry.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/pause/retry.png", true)));
         menuRetry.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[3]));
         menuRetry.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         guiNode.attachChild(menuRetry);
         
         menuContinueModel = new Box(64,14,0.5f);
         menuContinue = new GuiImage("menuContinue",menuContinueModel);
         menuContinue.move(screenWidth/2,320,20);
         menuContinue.setMat(new Material(assetManager,"MatDefs/Unshaded.j3md"));
         menuContinue.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/pause/continue.png", true)));
         menuContinue.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[4]));
         menuContinue.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         guiNode.attachChild(menuContinue);
         
         menuReturnModel = new Box(64,14,0.5f);
         menuReturn = new GuiImage("menuReturn",menuReturnModel);
         menuReturn.move(screenWidth/2,300,20);
         menuReturn.setMat(new Material(assetManager,"MatDefs/Unshaded.j3md"));
         menuReturn.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/pause/returntomenu.png", true)));
         menuReturn.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[5]));
         menuReturn.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         guiNode.attachChild(menuReturn);
 
         //Set up dialogue pane and portraits
         portraitPlayerModel = new Box(100,200,0);
         portraitPlayer = new GuiImage("portraitPlayer", portraitPlayerModel);
         portraitPlayer.setWidth(100);
         portraitPlayer.setHeight(200);
         portraitPlayer.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         portraitPlayer.getMat().setTexture("ColorMap", assetManager.loadTexture("Textures/game/playerPortrait.png"));
         portraitPlayer.getMat().getAdditionalRenderState().setAlphaFallOff(0.1f);
         portraitPlayer.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         portraitPlayer.move(-200, portraitPlayer.getHeight(), 0);
         guiNode.attachChild(portraitPlayer);
 
         portraitEnemyModel = new Box(100,200,0);
         portraitEnemy = new GuiImage("portraitEnemy", portraitEnemyModel);
         portraitEnemy.setWidth(100);
         portraitEnemy.setHeight(200);
         portraitEnemy.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         portraitEnemy.getMat().setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait1.png"));
         portraitEnemy.getMat().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         //portraitEnemy.move(screenWidth + portraitEnemy.getWidth(), portraitEnemy.getHeight()/2, 0);
         portraitEnemy.move(screenWidth + 100, portraitEnemy.getHeight(), 0);
         guiNode.attachChild(portraitEnemy);
 
         dialogueNodeModel = new Box(screenWidth, 46,0);
         dialogueNode = new GuiImage("DialogueNode", dialogueNodeModel);
         dialogueNode.setWidth(screenWidth);
         dialogueNode.setHeight(46);
         dialoguePaneMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         dialoguePaneMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/black.png"));
         dialoguePaneMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         dialogueNode.setMat(dialoguePaneMat);
         guiNode.attachChild(dialogueNode);
         dialogueNode.setLocalTranslation(0, -100, 0);
         
         //Create a new player object just to avoid null pointers for now
         player = new Player();
         enemy = new Enemy();
         advanceEventTime = true;
         //Set up the dialogue text
         guiFont = assetManager.loadFont("Textures/font/DroidSans.fnt");
 
         dialogue = new BitmapText(guiFont,false);
         dialogue.setSize(guiFont.getCharSet().getRenderedSize() * 0.5f);
         dialogue.setLocalTranslation(0, guiFont.getCharSet().getRenderedSize(), 0);
         dialogueBounds = new Rectangle(0,0,screenWidth, guiFont.getCharSet().getRenderedSize());
         dialogue.setBox(dialogueBounds);
         dialogue.setAlignment(BitmapFont.Align.Center);
         dialogue.setText("");
         guiNode.attachChild(dialogue);
 
         player.setLife(player.MAX_LIFE);
         graze = 0;
         heat = 0;
 
         stageDisplay = new BitmapText(guiFont, false);
         stageDisplay.setColor(new ColorRGBA(1,1,1,displayAlpha));
         stageDisplay.setSize(guiFont.getCharSet().getRenderedSize()/2.7f/windowScale);
         stageDisplay.setLocalTranslation(screenWidth - 180/windowScale, screenHeight - 19/windowScale,0);
         stageDisplay.setText("STAGE");
         guiNode.attachChild(stageDisplay);
         stageReading = new BitmapText(guiFont, false);
         stageReading.setColor(new ColorRGBA(1,1,1,displayAlpha));
         stageReading.setSize(guiFont.getCharSet().getRenderedSize()/1.2f/windowScale);
         stageReading.setLocalTranslation(screenWidth - 180/windowScale, screenHeight - 19/windowScale - stageDisplay.getLineHeight()/windowScale,0);
         guiNode.attachChild(stageReading);
 
         spellDisplay = new BitmapText(guiFont, false);
         spellDisplay.setColor(new ColorRGBA(1,1,1,displayAlpha));
         spellDisplay.setSize(guiFont.getCharSet().getRenderedSize()/2.7f/windowScale);
         spellDisplay.setLocalTranslation(screenWidth - 260/windowScale, screenHeight - 19/windowScale,0);
         spellDisplay.setText("SPELL");
         guiNode.attachChild(spellDisplay);
         spellReading = new BitmapText(guiFont, false);
         spellReading.setColor(new ColorRGBA(1,1,1,displayAlpha));
         spellReading.setSize(guiFont.getCharSet().getRenderedSize()/1.2f/windowScale);
         spellReading.setLocalTranslation(screenWidth - 260/windowScale, screenHeight - 19/windowScale - spellDisplay.getLineHeight()/windowScale,0);
         guiNode.attachChild(spellReading);
 
         lifeDisplay = new BitmapText(guiFont,false);
         lifeDisplay.setColor(new ColorRGBA(1,1,1,displayAlpha));
         lifeDisplay.setSize(guiFont.getCharSet().getRenderedSize()/2.7f/windowScale);
         lifeDisplay.setLocalTranslation(screenWidth - 90/windowScale, screenHeight - 68/windowScale,0);
         lifeDisplay.setText("LIFE");
         guiNode.attachChild(lifeDisplay);
         lifeReading = new BitmapText(guiFont, false);
         lifeReading.setColor(new ColorRGBA(1,1,1,displayAlpha));
         lifeReading.setSize(guiFont.getCharSet().getRenderedSize()/1.2f/windowScale);
         lifeReading.setLocalTranslation(screenWidth - 90/windowScale, screenHeight - 68/windowScale - lifeDisplay.getLineHeight()/windowScale,0);
         guiNode.attachChild(lifeReading);
 
         grazeDisplay = new BitmapText(guiFont, false);
         grazeDisplay.setColor(new ColorRGBA(1,1,1,displayAlpha));
         grazeDisplay.setSize(guiFont.getCharSet().getRenderedSize()/2.7f/windowScale);
         grazeDisplay.setLocalTranslation(screenWidth - 90/windowScale, screenHeight - 110/windowScale,0);
         grazeDisplay.setText("GRAZE");
         guiNode.attachChild(grazeDisplay);
         grazeReading = new BitmapText(guiFont, false);
         grazeReading.setColor(new ColorRGBA(1,1,1,displayAlpha));
         grazeReading.setSize(guiFont.getCharSet().getRenderedSize()/1.2f/windowScale);
         grazeReading.setLocalTranslation(screenWidth - 90/windowScale, screenHeight - 110/windowScale - grazeDisplay.getLineHeight()/windowScale,0);
         guiNode.attachChild(grazeReading);
 
         heatDisplay = new BitmapText(guiFont, false);
         heatDisplay.setColor(new ColorRGBA(1,1,1,displayAlpha));
         heatDisplay.setSize(guiFont.getCharSet().getRenderedSize()/2.7f/windowScale);
         heatDisplay.setLocalTranslation(screenWidth - 90/windowScale, screenHeight - 152/windowScale,0);
         heatDisplay.setText("HEAT");
         guiNode.attachChild(heatDisplay);
         heatReading = new BitmapText(guiFont, false);
         heatReading.setColor(new ColorRGBA(1,1,1,displayAlpha));
         heatReading.setSize(guiFont.getCharSet().getRenderedSize()/1.2f/windowScale);
         heatReading.setLocalTranslation(screenWidth - 90/windowScale, screenHeight - 152/windowScale - heatDisplay.getLineHeight()/windowScale,0);
         guiNode.attachChild(heatReading);
 
         scoreReading = new BitmapText(guiFont,false);
         scoreReading.setSize(guiFont.getCharSet().getRenderedSize()/windowScale);
         scoreReading.setLocalTranslation(0,dialogue.getLineHeight()*2.5f/windowScale,0);
         guiNode.attachChild(scoreReading);
 
         timerDisplay = new BitmapText(guiFont,false);
         timerDisplay.setColor(new ColorRGBA(1f,1f,1f, displayAlpha));
         timerDisplay.setSize(guiFont.getCharSet().getRenderedSize()*1.9f/windowScale);
         timerDisplay.setBox(new Rectangle(screenWidth - 140/windowScale,screenHeight - 7/windowScale,60/windowScale,10/windowScale));
         timerDisplay.setAlignment(Align.Right);
         guiNode.attachChild(timerDisplay);
         timerDisplaySeconds = new BitmapText(guiFont, false);
         timerDisplaySeconds.setColor(new ColorRGBA(1f,1f,1f, displayAlpha));
         timerDisplaySeconds.setSize(guiFont.getCharSet().getRenderedSize()/1.7f/windowScale);
         timerDisplaySeconds.setLocalTranslation(screenWidth - 75/windowScale,screenHeight-36/windowScale,0);
         guiNode.attachChild(timerDisplaySeconds);
 
         enemyDisplay = new BitmapText(guiFont, false);
         enemyDisplay.setColor(new ColorRGBA(1,1,1,displayAlpha));
         enemyDisplay.setSize(guiFont.getCharSet().getRenderedSize()/2.7f/windowScale);
         enemyDisplay.setLocalTranslation(screenWidth - 340/windowScale, screenHeight - 19/windowScale,0);
         enemyDisplay.setText("ENEMY");
         guiNode.attachChild(enemyDisplay);
         enemyReading = new BitmapText(guiFont, false);
         enemyReading.setColor(new ColorRGBA(1,1,1,displayAlpha));
         enemyReading.setSize(guiFont.getCharSet().getRenderedSize()/1.2f/windowScale);
         enemyReading.setLocalTranslation(screenWidth - 340/windowScale, screenHeight - 19/windowScale - enemyDisplay.getLineHeight()/windowScale,0);
         guiNode.attachChild(enemyReading);
 
         /*stageClearDisplay1 = new BitmapText(guiFont, false);
         stageClearDisplay1.setColor(new ColorRGBA(1f,1f,1f,1));
         stageClearDisplay1.setSize(guiFont.getCharSet().getRenderedSize()/3);
         stageClearDisplay1.setLocalTranslation(-screenWidth/2, screenHeight/2, 11);
         guiNode.attachChild(stageClearDisplay1);*/
 
         //Set up Intro Banner
         introBannerModel = new Box(120 / windowScale, 60 / windowScale,0);
         introBanner = new GuiImage("introBanner", introBannerModel);
         introBannerMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         introBannerMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/introbanner.png"));
         introBannerMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         introBanner.setMaterial(introBannerMat);
         introBannerAlpha = 0;
         introBannerMat.setColor("Color", new ColorRGBA(1, 1, 1, introBannerAlpha));
         introBanner.setLocalTranslation(screenWidth - 450 / windowScale, (350 / windowScale),0);
         try {guiNode.detachChildNamed("introBanner");} catch(Exception ex){}
         guiNode.attachChild(introBanner);
         try {
             filtPostProc.removeFilter(bloomFilter);
             filtPostProc.removeFilter(radialBlur);
         } catch(Exception ex) {}
         //BLOOM CODE
         bloomFilter=new BloomFilter(BloomFilter.GlowMode.Objects);
         bloomFilter.setBlurScale(4.2f);
         bloomFilter.setBloomIntensity(5.2f);
         filtPostProc.addFilter(bloomFilter);
 
         initGameBindings();
 
         radialBlur = new RadialBlurFilter();
         radialBlur.setSampleStrength(0.5f);
         radialBlur.setSampleDist(1);
         filtPostProc.addFilter(radialBlur);
 
        // scatterFilter = new LightScatteringFilter(new Vector3f(0,500,500));
         //filtPostProc.addFilter(scatterFilter);
 
         viewPort.addProcessor(filtPostProc);
 
         camFocalPoint = new GameObject("camFocalPoint");
         camFocalPoint.setLocalTranslation(new Vector3f(-50,0,100));
         objectNode.attachChild(camFocalPoint);
 
         camLoc = new GameObject("camLoc");
         camLoc.setLocalTranslation(new Vector3f(camOffset, 70 + camDistance, camHeight));
         objectNode.attachChild(camLoc);
 
         //Draw play boundaries
         Mesh lineMesh = new Mesh();
         lineMesh.setMode(Mesh.Mode.Lines);
         lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ playerMaxSide, playerMaxDistance, 0, playerMaxSide, playerMinDistance, 0, -playerMaxSide, playerMinDistance, 0, -playerMaxSide, playerMaxDistance, 0});
         lineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1, 1,2,2,3,3,0 });
         //lineMesh.updateBound();
         //lineMesh.updateCounts();
         Geometry lineGeometry = new Geometry("line", lineMesh);
         Material lineMaterial = new Material(assetManager, "MatDefs/Unshaded.j3md");
         lineMaterial.setColor("Color", new ColorRGBA(0.1f,0.1f,0.1f,0.2f));
         lineGeometry.setMaterial(lineMaterial);
         rootNode.attachChild(lineGeometry);
 
         gamePlaneGeom.setMaterial(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         gamePlaneGeom.setCullHint(CullHint.Always);
         
         objectNode.attachChild(gamePlaneGeom);
         
         gameFaded = false;
         gameUnfaded = true;
         screenFadeOverlayAlpha = 0;
     }
 
     private void createMoon() {
         moon = new GameObject("moon");
         moonModel = assetManager.loadModel("Models/game/moon.j3o");
         moon.attachChild(moonModel);
         moonMat = new Material(assetManager, "MatDefs/LightBlow/LightBlow.j3md");
         moonMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/game/backdrop/moon.png"));
         //moonMat.setTexture("GlowMap", assetManager.loadTexture("Textures/game/backdrop/moonGlow.png"));
         moonModel.setMaterial(moonMat);
         //moon.setCullHint(Spatial.CullHint.Never);
         moon.move(-50,-1300,780);
         moon.scale(0.4f);
         backNode.attachChild(moon);
         //moon.setQueueBucket(Bucket.Translucent);
         moonMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
     }
     private void createGround() {
         ground1 = new GameObject("ground");
         ground2 = new GameObject("ground2");
         ground3 = new GameObject("ground3");
         float offset = -2000;
         ground1.move(0, offset, 0);
         ground2.move(0, offset + 930, 0);
         ground3.move(0, offset + 1860, 0);
 
         ground1.setModel(assetManager.loadModel("Models/game/ground.j3o"));
         ground2.setModel(assetManager.loadModel("Models/game/ground.j3o"));
         ground3.setModel(assetManager.loadModel("Models/game/ground.j3o"));
 
         //Adjust Ground position
         ground1.getModel().rotateUpTo(Vector3f.UNIT_Z);
         ground1.getModel().move(0, 0, -60f);
 
         ground2.getModel().rotateUpTo(Vector3f.UNIT_Z);
         ground2.getModel().move(0, 0, -60f);
 
         ground3.getModel().rotateUpTo(Vector3f.UNIT_Z);
         ground3.getModel().move(0, 0, -60f);
 
 
         Material groundMat = assetManager.loadMaterial("Materials/LightBlow/Fog_System/LightBlow_Fog.j3m");
         groundMat.setTexture("DiffuseMap", assetManager.loadTexture(new TextureKey("Textures/game/groundTex.png",false)));
         //groundMat.setTexture("GlowMap", assetManager.loadTexture(new TextureKey("Textures/game/groundTex.png",false)));
         groundMat.setBoolean("Alpha_A_Dif", true);
         groundMat.setFloat("AlphaDiscardThreshold", 0.1f);
         groundMat.setColor("FogColor", new ColorRGBA(0.01f,0f,0.02f,750));
         groundMat.setBoolean("Fog", true);
         //groundMat.getAdditionalRenderState().setBlendMode(BlendMode.Off);
         groundMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         
         ground1.setMat(groundMat);
         ground2.setMat(groundMat);
         ground3.setMat(groundMat);
         
         ground1.setQueueBucket(Bucket.Translucent);
         ground2.setQueueBucket(Bucket.Translucent);
         ground3.setQueueBucket(Bucket.Translucent);
 
         //Set up ground control
         float groundLength = 930;
         float groundSpeed = 200;
         float groundCycle = groundLength*3/groundSpeed;
         ground1Control = new BackdropControl(ground1, 0, groundLength, groundSpeed, 0, groundCycle);
         ground2Control = new BackdropControl(ground2, 0, groundLength, groundSpeed, groundCycle/3, groundCycle);
         ground3Control = new BackdropControl(ground3, 0, groundLength, groundSpeed, (2*groundCycle)/3, groundCycle);
         ground1.addControl(ground1Control);
         ground2.addControl(ground2Control);
         ground3.addControl(ground3Control);
         backNode.attachChild(ground1);
         backNode.attachChild(ground2);
         backNode.attachChild(ground3);
     }
 
     public void initGameBindings() {
         //Set up key binds
         inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_LEFT));
         inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_RIGHT));
         inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_UP));
         inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_DOWN));/*
         inputManager.addMapping("shoot", new KeyTrigger(KeyInput.KEY_Z));
         inputManager.addMapping("focus", new KeyTrigger(KeyInput.KEY_LSHIFT));*/
         inputManager.addMapping("shoot", new MouseButtonTrigger(0));
         inputManager.addMapping("focus", new MouseButtonTrigger(1));
         inputManager.addMapping("pause", new KeyTrigger(KeyInput.KEY_P));
         inputManager.addListener(focusListener, new String[]{"left","right","up","down","focus"});
         inputManager.addListener(gameListener, new String[]{"left","right","up","down","focus","pause","shoot"});
     }
 
     PanelNode endgamePanel;
     ColorRGBA endgameTextColor;
     ColorRGBA endgameImageColor;
     public void initEndGame() {
         System.out.println("Initializing State " + currentGameState);
         rootNode.detachAllChildren();
         //guiNode.detachAllChildren();
         //guiNode.
         endgameImageModel = new Box(1280, 5000, 0);
         endgameImage = new GuiImage("screenFadeOverlay", screenFadeOverlayModel);
         endgameImage.setWidth(1280);
         endgameImage.setHeight(5000);
         endgameImageMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         endgameImageColor = new ColorRGBA(1,1,1,0);
         endgameImageMat.setTexture("ColorMap", assetManager.loadTexture("Textures/endgame/background.png"));
         endgameImageMat.setColor("Color", endgameImageColor);
         endgameImageMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         endgameImage.setMaterial(endgameImageMat);
         endgameImage.move(screenWidth/2, screenHeight, -1);
         //guiNode.attachChild(endgameImage);
         
         endgameText = new BitmapText(guiFont,false);
         endgameText.setSize(guiFont.getCharSet().getRenderedSize() * 0.5f);
         endgameTextBounds = new Rectangle(0,0,screenWidth, screenHeight);
         endgameText.setBox(endgameTextBounds);
         endgameText.setAlignment(BitmapFont.Align.Center);
         endgameTextColor = new ColorRGBA(1,1,1,0);
         endgameText.setText("and so a long night concludes\nthe trial of guts has ended\n\ncongratulations! demo clear!");
         endgameText.setLocalTranslation(0,screenHeight/2,0);
         endgameText.setColor(endgameTextColor);
         guiNode.attachChild(endgameText);
         
         endBackground = new PanelNode("endBackgroundPanel");
         endBackground.setModel(assetManager.loadModel("Models/end/endpanel.j3o"));
         endBackground.setMat(new Material(assetManager, "MatDefs/Unshaded.j3md"));
         endBackground.getMat().setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/endgame/background.png",false)));
         //endBackgroundMat.getAdditionalRenderState().setBlendMode(BlendMode.Off);
         endBackground.getMat().getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
         //55 to -64
         endBackground.setLocalTranslation(0,55f,0);
         endBackground.scale(6.2f);
         //endBackground.getMat().setTexture("ColorMap", nedBackgroundMat);
         rootNode.attachChild(endBackground);
         /*
         DirectionalLight openSplashLight = new DirectionalLight();
         openSplashLight.setDirection(new Vector3f(0f, 0f, -1.0f));
         openSplashLight.setColor(ColorRGBA.White);
 
         rootNode.addLight(openSplashLight);*/
         //cam.setParallelProjection(true);
     }
 
     public void cleanupOpenSplash() {
         currentGameState = STATE.MAINMENU;  //Advance the game state
         initMainMenu(); //Initialize the next state
         timer[T_AFTER_STATE_TIME] = 0; //reset the after state timer.
     }
 
     @Override
     public void simpleUpdate(float tpf) {
         tpf *= timescale;
         //Call different update method depending on the current game state.
         //  Check for state completion for each.
         switch(currentGameState) {
             case PREGAME: //Pre-everything state.
                 timer[T_AFTER_STATE_TIME] += 1/60f;
                 if(fadeFilter.getValue() < 0.95f) {
                     fadeFilter.fadeIn();
                     timer[T_AFTER_STATE_TIME] = 0;
                     currentGameState = STATE.OPENSPLASH;
                     initOpenSplash();
                 }
                 break;
             case OPENSPLASH://State == 0 : The open-splash screen state.
                 if(openSplashState.isComplete()) {  //If open splash is done
                     timer[T_AFTER_STATE_TIME] += 1/60f;  //Start counting time after state
                     if(!stateFade) {  //If the screen isn't faded yet,
                         fadeFilter.fadeOut();   //fade it now,
                         stateFade = true;   //and tell the rest of the game
                     }
                     if(fadeFilter.getValue() < 0.05f) {  //After fade time
                         cleanupOpenSplash();
                     }
                 } else {
                     //If it isn't done, then just update it normally.
                     updateOpenSplash(tpf);
                 }
                 break;
                 //Repeat for all states.  Probably should streamline this somehow,
                 //  it looks terrible as it is.
 
             case MAINMENU://State == 1 : The main menu screen state.
                 handleMainMenuMouse(tpf);
                 if(stateFade) {
                     stateFade = false;
                     fadeFilter.fadeIn();
                 }
                 if(mainMenuState.isComplete()) {
                     timer[T_AFTER_STATE_TIME] += 1/60f;
                     if(!stateFade) {
                         fadeFilter.fadeOut();
                         stateFade = true;
                     }
                     if(fadeFilter.getValue() < 0.05f) {
                         currentGameState = STATE.START;
                         initGameStart();
                         timer[T_AFTER_STATE_TIME] = 0;
                     }
                 } else {
                     updateMainMenu(tpf);
                 }
                 break;
 
             case START://State == 2 : This is the state between main menu and game.
                 if(stateFade) {
                     stateFade = false;
                     fadeFilter.fadeIn();
                 }
                 if(gameStartState.isComplete()) {
                     timer[T_AFTER_STATE_TIME] += 1/60f;
                     if(!stateFade) {
                         fadeFilter.fadeOut();
                         stateFade = true;
                     }
                     if(fadeFilter.getValue() < 0.05f) {
                         currentGameState = STATE.GAME;
                         initGame();
                         timer[T_AFTER_STATE_TIME] = 0;
                     }
                 } else {
                     updateGameStart(tpf);
                 }
                 break;
 
             case GAME://State == 4 : The actual game.  Just update.
                 handleGameMouse();
                 if(stateFade) {  //As always, fade in if faded out.
                     stateFade = false;
                     fadeFilter.fadeIn();
                 }
 
                 if(gameState.isComplete()) {
                     //Complete game depending on whether it's a death
                     //or if the game was beaten
                     if(gameWin) {
                         timer[T_AFTER_STATE_TIME] += 1/60f;
                         if(!stateFade) {
                             fadeFilter.setDuration(10);
                             fadeFilter.fadeOut();
                             stateFade = true;
                         }
                         if(fadeFilter.getValue() < 0.05f) {  //return to main
                             fadeFilter.setDuration(transitionTime);
                             unpauseGame();
                             currentGameState = STATE.END;
                             initEndGame();
                             timer[T_AFTER_STATE_TIME] = 0;
                             System.out.println("Going to end state");
                         }
                     } else {
                         timer[T_AFTER_STATE_TIME] += 1/60f;
                         if(!stateFade) {
                             fadeFilter.fadeOut();
                             stateFade = true;
                         }
                         if(fadeFilter.getValue() < 0.05f) {  //return to main
                             unpauseGame();
                             currentGameState = STATE.MAINMENU;
                             initMainMenu();
                             timer[T_AFTER_STATE_TIME] = 0;
                             System.out.println("Returning to main menu");
                         }
                     }
                 }
 
                 if(!gamePause) {
                     updateGame(tpf);
                     if(!gameUnfaded && !gameFlag[GFLAG_SCORE] && introDone) {
                         unfadeGame(tpf,true);
                     }
                     if(player.getLife() < 0) {
                         System.out.println("Game Over");
                         gameOverFlag = true;
                     }
                     if(gameOverFlag) {
                         pauseGame();
                     }
                     if(gameMenuActive) {
                         gameMenuActive = false;
                     }
                 } else {
                     if(!gameMenuActive) {
                         gameMenuActive = true;
                         curGameMenuItem = GAMEMENU_CONTINUE;
                     }
                     fadeGame(tpf,true);
                 }
                 break;
             case END:
                 //Timer
                 timer[T_EVENT_TIME] += 1/60f;
                 if(stateFade) {  //As always, fade in if faded out.
                     stateFade = false;
                     fadeFilter.setDuration(5);
                     fadeFilter.fadeIn();
                 }
                 
                 if(timer[T_EVENT_TIME] > 30) {  //If open splash is done
                     if(!stateFade) {  //If the screen isn't faded yet,
                         fadeFilter.fadeOut();   //fade it now,
                         stateFade = true;   //and tell the rest of the game
                     }
                     if(fadeFilter.getValue() < 0.05f) {  //After fade time
                         currentGameState = STATE.MAINMENU;  //Advance the game state
                         initMainMenu(); //Initialize the next state
                         timer[T_AFTER_STATE_TIME] = 0; //reset the after state timer.
                         timer[T_EVENT_TIME] = 0;
                         fadeFilter.setDuration(transitionTime);
                     }
                 } else {
                     //If it isn't done, then just update it normally.
                     
                 }
                 updateEndGame(tpf);
                 break;
         }
     }
 
     //Update methods, one for each state.
     public void updateOpenSplash(float tpf) {
         //Update code for opening splash.
         openSplashState.update(tpf);
     }
 
     boolean updateMainMenuFlags[];
     //scrolling: 0: title, 1: start, 2: end
     public void updateMainMenu(float tpf) {
         timer[T_MAINMENU_TIME] += 1/60f;
         cam.setLocation(new Vector3f(0f,0f,15f));
         cam.setRotation(Quaternion.IDENTITY);
         cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
         if(timer[T_MAINMENU_TIME] < 1.4f) {
             updateMainMenuFlags = new boolean[9];
             for(int i = 0; i < 9; i++) {
                 updateMainMenuFlags[i] = false;
             }
         }
         mainMenuTime += 1/60f;
         if(mainMenuTime > mainMenuFrameRate) {
             titleBackground.getMat().setTexture("ColorMap", mainMenuFrame[mainMenuCurFrame]);
             mainMenuCurFrame++;
             if(mainMenuCurFrame > 26) {
                 mainMenuCurFrame = 1;
             }
             mainMenuTime = 0;
         }
         if(timer[T_MAINMENU_TIME] > 1.4 && !updateMainMenuFlags[0]) {
             if(titlePanel.getLocalTranslation().x < 0) {
                 titlePanel.move(1/60f * 3f,0,0);
                 titleAlpha[0] += 6f * 1/60f;
                 titlePanel.getMat().setColor("Color", new ColorRGBA(1,1,1, titleAlpha[0]));
             } else {
                 updateMainMenuFlags[0] = true;
             }
         }
         if(timer[T_MAINMENU_TIME] > 1.5 && !updateMainMenuFlags[1]) {
             if(startButton.getLocalTranslation().x < 0) {
                 startButton.move(1/60f * 3f,0,0);
                 if(titleAlpha[1] < 0.6) {
                     titleAlpha[1] += 4f * 1/60f;
                     startButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[1]));
                 }
             } else {
                 updateMainMenuFlags[1] = true;
             }
         }
         if(timer[T_MAINMENU_TIME] > 1.6 && !updateMainMenuFlags[2]) {
             if(exitButton.getLocalTranslation().x < 0) {
                 exitButton.move(1/60f * 3f,0,0);
                 if(titleAlpha[2] < 0.6) {
                     titleAlpha[2] += 4f * 1/60f;
                     exitButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[2]));
                 }
             } else {
                 updateMainMenuFlags[2] = true;
             }
         }
     }
     public void updateGameStart(float tpf) {
         loadTime += tpf;
         if(loadTime > 3) {
             gameStartState.complete();
             System.out.println("Advancing to state " + (currentGameState.next()));
             inputManager.deleteMapping("advance");
         }
        
     }
 
     public void updateGame(float tpf) {
         player.distance = player.getLocalTranslation().y;
         if(debug) {
             cam.setLocation(new Vector3f(0, 100, 120));
             cam.lookAt(Vector3f.ZERO.add(0,30,0), Vector3f.UNIT_Z);
         } else {
             cam.setLocation(camLoc.getPos());
             cam.lookAt(camFocalPoint.getLocalTranslation(), Vector3f.UNIT_Z);
             //cam.lookAt(new Vector3f(cam.getLocation().x, cam.getLocation().y - 0.2f, -1), Vector3f.UNIT_Z);
         }
         moon.lookAt(cam.getLocation(),Vector3f.UNIT_Z);
         if(gameFlag[GFLAG_MOVE_ENABLED]) playerLoc.set(gameMouseLoc.x,gameMouseLoc.y,0);
         if(playerLoc.x > playerMaxSide) {
            playerLoc.setX(playerMaxSide);
         }
         if(playerLoc.x < -playerMaxSide) {
            playerLoc.setX(-playerMaxSide);
         }
         if(playerLoc.y < playerMinDistance) {
            playerLoc.setY(playerMinDistance);
         }
         if(playerLoc.y > playerMaxDistance) {
            playerLoc.setY(playerMaxDistance);
         }
         player.moveTo(playerLoc,player.getMoveSpeed());
         player.update(tpf);
 
         camFocalPoint.update(tpf);
         camLoc.update(tpf);
         //Setting the positions for the hud elements.
         //dialogueNode.setLocalTranslation(cam.getLocation().add(cam.getDirection().mult(5)));
         //dialogueNode.lookAt(cam.getLocation(), Vector3f.UNIT_Z);
 
         updateGamePortraits(tpf);
         updateTimeline(tpf);
         updateGameObjects(tpf);
         float timerToHundreths = Math.round(spellTimer[0] * 100);
         timerDisplay.setText(Integer.toString(Math.round(timerToHundreths / 100)));
         int fraction = Math.round(timerToHundreths % 100);
         if(fraction >= 10) {
             timerDisplaySeconds.setText(":"+Integer.toString(fraction));
         } else {
             timerDisplaySeconds.setText(":"+Integer.toString(fraction) + "0");
         }
         lifeReading.setText(Integer.toString(player.getLife()));
         grazeReading.setText(Integer.toString(graze));
         heatReading.setText(Integer.toString(heat));
         enemyReading.setText(Integer.toString(Math.round(enemy.life)));
         spellReading.setText(Integer.toString(spell));
         stageReading.setText(Integer.toString(stage));
         //HIT DETECTION: Check for bullet to player collisions here.
         grazeChange = graze;
         checkBulletCollisions(tpf);
         grazeChange = graze - grazeChange;
         heat += grazeChange * heatGainRate;
 
         if(heat > 0 + tpf*heatLossRate) {
             if(!playerFocus) {
                 heat -= tpf*heatLossRate;
             } else {
                 heat += tpf*heatGainRate;
             }
         } else {
             heat = 0;
         }
 
         if(heat > heatMax) {
             heat = heatMax;
         } else if(heat < 0) {
             heat = 0;
         }
         
         if(displayAlpha <= 1 && spellcardActive) {
                 displayAlpha += tpf;
                 if(displayAlpha > 1) displayAlpha = 1;
                 displayColor = new ColorRGBA(1f,1f,1f, displayAlpha);
                 enemyDisplay.setColor(displayColor);
                 enemyReading.setColor(displayColor);
                 lifeDisplay.setColor(displayColor);
                 lifeReading.setColor(displayColor);
                 timerDisplay.setColor(displayColor);
                 timerDisplaySeconds.setColor(displayColor);
                 grazeDisplay.setColor(displayColor);
                 grazeReading.setColor(displayColor);
                 heatDisplay.setColor(displayColor);
                 heatReading.setColor(displayColor);
                 stageDisplay.setColor(displayColor);
                 stageReading.setColor(displayColor);
                 spellDisplay.setColor(displayColor);
                 spellReading.setColor(displayColor);
         } else if(!spellcardActive) {
             if(displayAlpha > 0) {
                 displayAlpha -= tpf;
                 if(displayAlpha < 0) displayAlpha = 0;
                 displayColor = new ColorRGBA(1f,1f,1f, displayAlpha);
                 enemyDisplay.setColor(displayColor);
                 enemyReading.setColor(displayColor);
                 lifeDisplay.setColor(displayColor);
                 lifeReading.setColor(displayColor);
                 timerDisplay.setColor(displayColor);
                 timerDisplaySeconds.setColor(displayColor);
                 grazeDisplay.setColor(displayColor);
                 grazeReading.setColor(displayColor);
                 heatDisplay.setColor(displayColor);
                 heatReading.setColor(displayColor);
                 stageDisplay.setColor(displayColor);
                 stageReading.setColor(displayColor);
                 spellDisplay.setColor(displayColor);
                 spellReading.setColor(displayColor);
             }
         }
         updatePlayer(tpf);
     }
 
     public void checkBulletCollisions(float tpf) {
         //Finally got it working with iterator/list. [07 03 2011]
         //Changed this to check for graze.  Finished [24 03 2011]
         Iterator bulletIterator = bulletNode.getChildren().iterator();
         Iterator shotIterator;
         StaticBullet currBullet;
         StaticBullet currShot;
         
         while(bulletIterator.hasNext()) {
             try{
                 currBullet = (StaticBullet)bulletIterator.next();
                 if(currBullet.getLocalTranslation().length() > maxBulletDistance) {
                     //Remove bullets too far away
                     bulletIterator.remove();
                     currBullet.detachAllChildren();
                     currBullet.removeControl(currBullet.getControl(0));
                     currBullet.removeFromParent();
                 } else {
                     shotIterator = shotNode.getChildren().iterator();
                     while(shotIterator.hasNext()) {
                         try {
                             currShot = (StaticBullet)shotIterator.next();
                             if(currBullet.getLocalTranslation().subtract(currShot.getLocalTranslation()).length() < 5) {
                                 //bulletIterator.remove();
                                 //currBullet.removeFromParent();
                                 //currBullet.detachAllChildren();
                                 Shrinker sh = new Shrinker();
                                 sh.setSpatial(currBullet);
                                 currBullet.addControl(sh);
                                 currBullet.kill();
                             }
                         } catch(ClassCastException CCE) {}
                     }
                     
                     //Use distances instead of hitboxes.
                     //Only check bullets within the play bounds
                     if(currBullet.getPos().x > playerMaxSide || currBullet.getPos().x < -playerMaxSide || currBullet.getPos().y > playerMaxDistance || currBullet.getPos().y < playerMinDistance) {
                         //Skip bullets outside of play bounds
                     } else {
                         float dist = currBullet.getPos().distance(player.getPos());
                         if(dist < player.getGrazeSize()+currBullet.getHitSize()) {
                             graze += tpf * 300;
                             if(dist < player.getHitSize() + currBullet.getHitSize()) {
                                 bulletIterator.remove();
                                 playerHit();
                                 currBullet.removeFromParent();
                                 currBullet.detachAllChildren();
                             }
                         }
                     }
                 }
             }catch(Exception ex) {}
         }
 
         //Check player shot collisions with enemy.
         shotIterator = shotNode.getChildren().iterator();
         currBullet = null;
         while(shotIterator.hasNext()) {
             try{
                 currBullet = (StaticBullet)shotIterator.next();
 
                 if(currBullet.getLocalTranslation().length() > maxBulletDistance) {
                     //Remove shots too far away
                     shotIterator.remove();
                     currBullet.detachAllChildren();
                     currBullet.removeFromParent();
                     shotCount--;
                 } else {
                     //Use distances instead of hitboxes
                     if(enemy.getPos().distance(currBullet.getPos()) < enemy.getHitSize()+currBullet.getHitSize()) {
                         System.out.println("Enemy hit");
                         enemy.life -= 70*tpf;
                         System.out.println(enemy.life);
                     }
                 }
             } catch(Exception ex) {}
         }
     }
 
     public void playerHit() {
         if(!spellFlag[SFLAG_CSPELL]) {
             player.changeLife(-5);
             heat -= 300;
             playerDeathEmitter.emitAllParticles();
             System.out.println("LIFE: " + player.getLife());
         }
     }
     public void updateGamePortraits(float tpf) {
         if(showDialogue) {
             timer[T_AFTER_DIALOGUE_TIME] = 0;
             if(dialoguePlayer) {
                 if(portraitPlayer.getLocalTranslation().x + 13 < portraitPlayer.getWidth() - 25) {
                     portraitPlayer.move(13f,0,0);
                 } else {
                     portraitPlayer.setLocalTranslation(portraitPlayer.getWidth() - 25,portraitPlayer.getHeight(),0);
                 }
             } else {
                 if(portraitPlayer.getLocalTranslation().x - 18 > 20) {
                     portraitPlayer.move(-18f,0,0);
                 } else {
                     portraitPlayer.setLocalTranslation(20,portraitPlayer.getHeight(),0);
                 }
             }
 
             if(dialogueEnemy) {
                 if(portraitEnemy.getLocalTranslation().x > screenWidth - portraitEnemy.getWidth() + 25) {
                     portraitEnemy.move(-13f,0,0);
                 }
             } else {
                 if(portraitEnemy.getLocalTranslation().x < screenWidth - 50) {
                     portraitEnemy.move(18f,0,0);
                 }
             }
 
             if(dialogueNode.getLocalTranslation().y + 15 < 0) {
                 dialogueNode.move(0,15f,0);
             } else {
                 dialogueNode.setLocalTranslation(0, 0, 0);
             }
         } else { //Dialogue inactive
             timer[T_AFTER_DIALOGUE_TIME] += tpf;
             if(timer[T_AFTER_DIALOGUE_TIME] > 0.3) {
                 if(portraitPlayer.getLocalTranslation().x > -portraitPlayer.getWidth()) {
                     portraitPlayer.move(-25f,0,0);
                 }
                 if(portraitEnemy.getLocalTranslation().x < screenWidth + portraitEnemy.getWidth()) {
                     portraitEnemy.move(5f,0,0);
                 }
                 if(!dialogueEnemy) {
                     if(portraitEnemy.getLocalTranslation().x < screenWidth) {
                         portraitEnemy.move(5f,0,0);
                     }
                 }
                 if(dialogueNode.getLocalTranslation().y -15 > -100) {
                     dialogueNode.move(0,-15f,0);
                 } else {
                     dialogueNode.setLocalTranslation(0, -100, 0);
                 }
             }
         }
     }
     public void updateGameObjects(float tpf) {
         if(!MOVE_LEFT && !MOVE_RIGHT && !MOVE_UP && !MOVE_DOWN & !MOVE_STAND) {
             MOVE_STAND = true;
             playerAnimChan.setAnim("down",0.7f);
         }
         //Reset animation manually
         if(playerAnimChan.getTime() >= 0.4) {
             playerAnimChan.setTime(0);
         }
         try {
             if(timescale != 1) {
                 Iterator bulletIterator = bulletNode.getChildren().iterator();
                 while(bulletIterator.hasNext()) {
                     GameObject curBullet = (GameObject) bulletIterator.next();
                     Bullet bulCon = curBullet.getControl(Bullet.class);
                     bulCon.setTimescale(timescale);
                 }
                 ground1Control.setTimescale(timescale);
                 ground2Control.setTimescale(timescale);
                 ground3Control.setTimescale(timescale);
             } else {
                 Iterator bulletIterator = bulletNode.getChildren().iterator();
                 while(bulletIterator.hasNext()) {
                     GameObject curBullet = (GameObject) bulletIterator.next();
                     Bullet bulCon = curBullet.getControl(Bullet.class);
                     bulCon.setTimescale(1);
                 }
                 ground1Control.setTimescale(1);
                 ground2Control.setTimescale(1);
                 ground3Control.setTimescale(1);
             }
         }catch(Exception ex) {
         }
         enemy.update(tpf);
         if(enemyAnimChan.getAnimationName().equals("spell") || enemyAnimChan.getAnimationName().equals("up") || enemyAnimChan.getAnimationName().equals("back")) {
             enemy.lookAt(player.getPos(), Vector3f.UNIT_Z);
         }
     }
 
     public void updatePlayer(float tpf) {
         player.lookAt(gameMouseLoc.add(0,0,100),Vector3f.UNIT_Z);
         if(heat < 100 && heatState != 0) {
             playerHeatEmitter.setParticlesPerSec(0);
             heatState = 0;
         }
         if(heat > 150 && heatState != 1) {
             playerHeatEmitter.setParticlesPerSec(50);
             heatState = 1;
         }
         if(heat > 500 && heatState != 2) {
             playerHeatEmitter.setParticlesPerSec(90);
             heatState = 2;
         }
         if(heat > 1000 && heatState != 3) {
             playerHeatEmitter.setParticlesPerSec(220);
             heatState = 3;
         }
         if(playerFocus) {
             enemy.life = 0;
             focusTimer += tpf*160;
             if(focusTimer > focusLimit){
                 focusTimer = focusLimit;
             }
             playerFocusEmitter.setParticlesPerSec(Math.round(focusTimer));
             player.turnSpeed = 0.5f;
             player.moveSpeed = 1;
         } else {
             focusTimer -= tpf*400;
             if(focusTimer < 0) {
                 focusTimer = 0;
             }
             playerFocusEmitter.setParticlesPerSec(Math.round(focusTimer));
             player.turnSpeed = 1.2f;
             player.moveSpeed = 25;
         }
     }
     //----------------------------------------------------------------------------------
     //UTILITY METHODS
     //----------------------------------------------------------------------------------
     public void setUpPlayer() {
         playerModel = (Spatial) assetManager.loadModel("Models/game/player.j3o");
         playerMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         playerMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/moko.png",false)));
         playerModel.setMaterial(playerMat);
         player.attachChild(playerModel);
 
         player.scale(1f);
         player.distance = 70;
         player.setLocalTranslation(0, player.distance, 0);
         player.angle = 0;
 
         playerHeatEmitter = new ParticleEmitter("playerHeatEmitter", ParticleMesh.Type.Triangle, 100);
         Material mat_red = new Material(assetManager, "MatDefs/Particle.j3md");
         mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/wing.png"));
         mat_red.getAdditionalRenderState().setBlendMode(BlendMode.Additive);
         playerHeatEmitter.setQueueBucket(Bucket.Translucent);
         playerHeatEmitter.setMaterial(mat_red);
         playerHeatEmitter.setNumParticles(20);
         playerHeatEmitter.setParticlesPerSec(10);
         playerHeatEmitter.setImagesX(1); playerHeatEmitter.setImagesY(1);
         playerHeatEmitter.setEndColor(new ColorRGBA(0.02f, 0f, 0f, 0.1f));   // red
         playerHeatEmitter.setStartColor(new ColorRGBA(0.06f, 0.06f, 0f, 0.1f)); // yellow
         playerHeatEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0,4));
         playerHeatEmitter.setGravity(0,0,-9);
         //playerHeatEmitter.setRotateSpeed(20);
         //playerHeatEmitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 5));
         playerHeatEmitter.setParticlesPerSec(0);
         playerHeatEmitter.getParticleInfluencer().setVelocityVariation(0.5f);
         playerHeatEmitter.setStartSize(14);
         playerHeatEmitter.setEndSize(9);
         playerHeatEmitter.setLowLife(0.2f);
         playerHeatEmitter.setHighLife(0.4f);
         playerHeatEmitter.setVelocityVariation(0.5f);
         playerHeatEmitter.move(0,0,2);
         player.attachChild(playerHeatEmitter);
         
         playerFocusEmitter = new ParticleEmitter("playerFocusEmitter", ParticleMesh.Type.Triangle, 100);
         Material focusEmitter = new Material(assetManager, "MatDefs/Particle.j3md");
         focusEmitter.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/dashH.png"));
         focusEmitter.getAdditionalRenderState().setBlendMode(BlendMode.Additive);
         playerFocusEmitter.setQueueBucket(Bucket.Translucent);
         playerFocusEmitter.setMaterial(focusEmitter);
         playerFocusEmitter.setNumParticles(40);
         playerFocusEmitter.setParticlesPerSec(0);
         playerFocusEmitter.setImagesX(1); playerFocusEmitter.setImagesY(1);
         playerFocusEmitter.setEndColor(new ColorRGBA(0.3f, 0f, 0f, 0.1f));   // red
         playerFocusEmitter.setStartColor(new ColorRGBA(0.4f, 0.4f, 0.3f, 0.4f)); // yellow
         playerFocusEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(0,0,70));
         playerFocusEmitter.setStartSize(1);
         playerFocusEmitter.setEndSize(0.2f);
         playerFocusEmitter.setLowLife(0.1f);
         playerFocusEmitter.setHighLife(0.2f);
         playerFocusEmitter.setFacingVelocity(true);
         playerFocusEmitter.getParticleInfluencer().setVelocityVariation(1);
         playerFocusEmitter.move(0,0,0);
         player.attachChild(playerFocusEmitter);
         
         playerDeathEmitter = new ParticleEmitter("playerDeathEmitter", ParticleMesh.Type.Triangle, 10);
 
         Material deathParticle = new Material(assetManager, "MatDefs/Particle.j3md");
         //mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/spark.png"));
         deathParticle.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/leaf.png"));
         deathParticle.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         playerDeathEmitter.setQueueBucket(Bucket.Translucent);
         //Leaf explosion
         playerDeathEmitter.setMaterial(deathParticle);
         playerDeathEmitter.setNumParticles(40);
         playerDeathEmitter.setImagesX(2); playerDeathEmitter.setImagesY(1); // 2x2 texture animation
         playerDeathEmitter.setEndColor(new ColorRGBA(1f, 0f, 0f, 0.4f));   // red
         playerDeathEmitter.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.2f)); // yellow
         playerDeathEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(60,0,0));
         playerDeathEmitter.setGravity(0,0,20);
         playerDeathEmitter.setRotateSpeed(10);
         
         playerDeathEmitter.setParticlesPerSec(0);
         playerDeathEmitter.getParticleInfluencer().setVelocityVariation(1);
         playerDeathEmitter.setStartSize(4f);
         playerDeathEmitter.setEndSize(0.2f);
         playerDeathEmitter.setLowLife(0.1f);
         playerDeathEmitter.setHighLife(0.4f);
         //playerDeathEmitter.setVelocityVariation(0f);
         playerDeathEmitter.move(0,0,0);
         player.attachChild(playerDeathEmitter);
     }
     
     public void setUpEnemy(int stage) {
         try{enemy.detachChild(enemyModel);}catch(Exception ex){}
         portraitEnemy.getMat().setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait"+stage+".png"));
         cutEnemyMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait"+stage+".png"));
         cutEnemyMat.setColor("Color", new ColorRGBA(1, 1, 1, cutEnemyAlpha));
         switch(stage) {
             case 1:
                 enemyModel = assetManager.loadModel("Models/game/enemy.j3o");
                 enemyMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
                 enemyMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/hada01.png", false)));
                 break;
             case 2:
                 enemyModel = assetManager.loadModel("Models/game/alice.j3o");
                 enemyMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
                 enemyMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/alice.png", false)));
                 break;
             case 3:
                 enemyModel = assetManager.loadModel("Models/game/sakuya.j3o");
                 enemyMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
                 enemyMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/sakuya.png", false)));
                 break;
             case 4:
                 enemyModel = assetManager.loadModel("Models/game/youmu.j3o");
                 enemyMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
                 enemyMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/youmu.png", false)));
                 /*
                  * enemy lighting material
                 enemyMat = new Material(assetManager, "MatDefs/Lighting.j3md");
                 enemyMat.setTexture("m_DiffuseMap", assetManager.loadTexture(new TextureKey("Textures/game/youmu.png", false)));
                 enemyMat.setTexture("m_GlowMap", assetManager.loadTexture(new TextureKey("Textures/game/youmu.png", false)));
                 enemyMat.setColor("m_GlowColor", new ColorRGBA(20,150,20,0.3f));*/
                 break;
             case 5:
                 enemyModel = assetManager.loadModel("Models/game/yuyuko.j3o");
                 enemyMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
                 enemyMat.setTexture("ColorMap", assetManager.loadTexture(new TextureKey("Textures/game/yuyuko.png", false)));
                 break;
         }
         enemy.attachChild(enemyModel);
         enemy.setMaterial(enemyMat);
         enemy.scale(1f);
         enemyAnimCont = enemyModel.getControl(AnimControl.class);
         System.out.println(enemyAnimCont.getAnimationNames());
         try {
             enemyAnimCont.removeListener(enemyAnimListener);
         } catch(Exception ex) {}
         enemyAnimCont.addListener(enemyAnimListener);
         enemyAnimChan = enemyAnimCont.createChannel();
         enemyAnimChan.setAnim("up");
     }
     //----------------------------------------------------------------------------------
     //TIMELINE FLAGS
     //----------------------------------------------------------------------------------
     boolean spawnDone;
     boolean introDone;
     boolean introBannerEnterDone;
     boolean introBannerExitDone;
     int dialogueCount = 256;
     boolean dialogueFlag[] = new boolean[dialogueCount];
 
     int varCount = 32;
     int flagCount = 64;
     boolean spellFlag[] = new boolean[varCount];
     final int SFLAG_SCORE = 0;
     final int SFLAG_ZWAIT = 1;
     final int SFLAG_CSPELL = 31;
     
     boolean gameFlag[] = new boolean[flagCount];
     final int STAGE1 = 0;
     final int STAGE1_1 = 1;
     final int STAGE1_2 = 2;
     final int STAGE1_3 = 3;
     final int STAGE1_4 = 4;
     final int STAGE1_5 = 5;
     final int STAGE1_6 = 6;
     final int STAGE1_L = 7;
     
     final int STAGE2 = 27;
     final int STAGE2_0 = 8;
     final int STAGE2_0_1 = 10;
     final int STAGE2_1 = 9;
     final int GFLAG_SCORE = 11;
     final int STAGE2_2 = 12;
     final int STAGE2_3 = 13;
     final int STAGE2_4 = 14;
     final int STAGE2_5 = 15;
     final int STAGE2_6 = 16;
     final int STAGE2_L = 17;
     
     final int STAGE3 = 28;
     final int STAGE3_0 = 18;
     final int STAGE3_0_1 = 19;
     final int STAGE3_1 = 20;
     final int STAGE3_2 = 21;
     final int STAGE3_3 = 22;
     final int STAGE3_4 = 23;
     final int STAGE3_5 = 24;
     final int STAGE3_6 = 25;
     final int STAGE3_L = 26;
     
     final int STAGE4 = 29;
     final int STAGE4_0 = 30;
     final int STAGE4_0_1 = 31;
     final int STAGE4_1 = 32;
     final int STAGE4_2 = 33;
     final int STAGE4_3 = 34;
     final int STAGE4_4 = 35;
     final int STAGE4_5 = 36;
     final int STAGE4_6 = 37;
     final int STAGE4_L = 38;
     
     final int STAGE5 = 39;
     final int STAGE5_0 = 40;
     final int STAGE5_0_1 = 41;
     final int STAGE5_1 = 42;
     final int STAGE5_2 = 43;
     final int STAGE5_3 = 44;
     final int STAGE5_4 = 45;
     final int STAGE5_5 = 46;
     final int STAGE5_6 = 47;
     final int STAGE5_L = 48;
     
     final int STAGE6 = 49;
     final int STAGE6_0 = 50;
     final int STAGE6_0_1 = 51;
     final int STAGE6_1 = 52;
     final int STAGE6_2 = 53;
     final int STAGE6_3 = 54;
     final int STAGE6_4 = 55;
     final int STAGE6_5 = 56;
     final int STAGE6_6 = 57;
     final int STAGE6_L = 58;
     
     final int STAGE7 = 59;
     final int STAGE7_0 = 60;
     final int STAGE7_0_1 = 61;
     final int STAGE7_1 = 62;
     final int STAGE7_2 = 63;
     final int STAGE7_3 = 64;
     final int STAGE7_4 = 65;
     final int STAGE7_5 = 66;
     final int STAGE7_6 = 67;
     final int STAGE7_L = 68;
     
     final int STAGE8 = 69;
     final int STAGE8_0 = 70;
     final int STAGE8_0_1 = 71;
     final int STAGE8_1 = 72;
     final int STAGE8_2 = 73;
     final int STAGE8_3 = 74;
     final int STAGE8_4 = 75;
     final int STAGE8_5 = 76;
     final int STAGE8_6 = 77;
     final int STAGE8_L = 78;
     
     final static int GFLAG_MOVE_ENABLED = 40;
     
     float spellTimer[] = new float[varCount];
     final int T_SPELL_MAIN = 0;
     final int T_SPELL_FADE = 30;
     float bulletFade = 1;
     //----------------------------------------------------------------------------------
     //TIMELINE METHODS
     //----------------------------------------------------------------------------------
     private void resetCardVars() {
         for(int i = 0; i < varCount; i++) {
             spellFlag[i] = false;
             spellTimer[i] = 0;
         }
     }
     //SPAWNING AT THE START OF THE GAME
     private void spawn() {
         setUpPlayer();
         playerAnimCont = playerModel.getControl(AnimControl.class);
         try {
             playerAnimCont.removeListener(playerAnimListener);
         } catch(Exception ex) {
             //Probably the first run.
         }
         
         //Set player position to initial static vector constant
         //  0 70 0
         gameMouseLoc.set(StartPosVector);
         playerLoc.set(StartPosVector);
         
         //Set player animation
         playerAnimCont.addListener(playerAnimListener);
         playerAnimChan = playerAnimCont.createChannel();
         playerAnimChan.setLoopMode(LoopMode.DontLoop);
         playerAnimChan.setAnim("down");
         playerAnimChan.setSpeed(0.5f);
         
         setUpEnemy(1);
         
         objectNode.attachChild(player);
         playerModel.rotate(FastMath.HALF_PI,0,0);
         objectNode.attachChild(enemy);
         
         enemy.setLocalTranslation(20,-160,0);
         enemy.rotate(-FastMath.HALF_PI,0,0);
         enemy.moveTo(new Vector3f(0,0,0), 1f);
     }
 
     private void say(String text, int speaker) {
         //If speaker is 1, it's the player.  If it's 2, it's the enemy
         if(speaker == 1) {
             dialoguePlayer = true;
             dialogue.setColor(new ColorRGBA(0.9f, 0.6f, 0.6f, 1));
         } else if(speaker == 2) {
             dialogueEnemy = true;
             dialogue.setColor(new ColorRGBA(0.6f, 0.6f, 0.9f, 1));
         }
         
         advanceEventTime = false;
         dialogueActive = true;
         dialogue.setText(text);
         dialogue.setSize(guiFont.getCharSet().getRenderedSize() * 0.5f);
         dialogue.setLocalTranslation(0, guiFont.getCharSet().getRenderedSize(), 0);
         dialogue.setBox(dialogueBounds);
         dialogue.setAlignment(BitmapFont.Align.Center);
         //dialogue.setSize(guiFont.getCharSet().getRenderedSize() * 0.5f);
     }
 
     //----------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------
     boolean temp = false;
     public void updateTimeline(float tpf) {
         if((!spellcardActive && !stageActive) && advanceEventTime && !dialogueActive) {
             timer[T_EVENT_TIME] += 1/60f;
         }
         if(!spawnDone) {
             spawn();
             System.out.println("Spawning");
             spawnDone = true;
             timer[T_INTRO_TIME] = 0;
             setBackgroundMusic("Sounds/stage1.ogg");
         }
 
         if(spawnDone && !introDone) {
             cam.setFrustumFar(10000);
             cam.setFrustumNear(1);
             advanceEventTime = false;
             introSequence(tpf);
         }
         if(!temp) {
             //timer[T_EVENT_TIME] = 62.2f;
             //gameFlag[GFLAG_MOVE_ENABLED] = true;
             temp = true;
         }
         if(timer[T_EVENT_TIME] > 3.2 && !dialogueFlag[1]) {
             gameFlag[GFLAG_MOVE_ENABLED] = true;
             say("There are idiots who would go out on a night like this?",1);
             System.out.println("Dialogue 1.");
             dialogueFlag[1] = true;
             showDialogue = true;
         }
         if(timer[T_EVENT_TIME] > 3.4 && !dialogueFlag[2]) {
             say("Who?",2);
             System.out.println("Reply");
             dialogueFlag[2] = true;
         }
         if(timer[T_EVENT_TIME] > 3.6 && !dialogueFlag[3]) {
             say("I mean you.",1);
             dialogueFlag[3] = true;
         }
         if(timer[T_EVENT_TIME] > 3.8 && !dialogueFlag[4]) {
             say("No, I didn't mean who's an idiot...",2);
             dialogueFlag[4] = true;
         }
         if(timer[T_EVENT_TIME] > 4 && !gameFlag[STAGE1_1]) {
             showDialogue = false;
             stage1spell1(tpf);
             //stage2spell2(tpf);
             //stage2spell1(tpf);
         }
          
         //Demo end block
         if(timer[T_EVENT_TIME] > 5) {
             //DEMO END
             gameState.complete();
             gameWin = true;
             //currentGameState = STATE.MAINMENU;
             //guiNode.detachAllChildren();
             showDialogue = false;
             timer[T_EVENT_TIME] = 5;
         }
         
         if(timer[T_EVENT_TIME] > 6 && !gameFlag[STAGE1_2]) {
             stage1spell2(tpf);
         }
         if(timer[T_EVENT_TIME] > 8 && !gameFlag[STAGE1_3]) {
             stage1spell3(tpf);
         }
         if(timer[T_EVENT_TIME] > 10 && !gameFlag[STAGE1_4]) {
             stage1spell4(tpf);
         }
         if(timer[T_EVENT_TIME] > 12 && !gameFlag[STAGE1_5]) {
             stage1spell5(tpf);
         }
         if(timer[T_EVENT_TIME] > 14 && !gameFlag[STAGE1_6]) {
             stage1spell6(tpf);
         }
         if(timer[T_EVENT_TIME] > 14 && gameFlag[STAGE1_6] && !gameFlag[STAGE1_L]) {
             stage1spellL(tpf);
         }
 
         if(timer[T_EVENT_TIME] > 18 && !dialogueFlag[5]) {
             say("Finished",1);
             dialogueFlag[5] = true;
             showDialogue = true;
         }
         if(timer[T_EVENT_TIME] > 18.2 && !dialogueFlag[6]) {
             say("Ok",2);
             dialogueFlag[6] = true;
             gameFlag[STAGE1] = true;
         }
         
         if(timer[T_EVENT_TIME] > 20 && !gameFlag[STAGE2_0] && gameFlag[STAGE1]) {
             //DEMO END
             gameState.complete();
             filtPostProc.removeFilter(radialBlur);
             fadeFilter.fadeOut();
             guiNode.detachAllChildren();
             
             showDialogue = false;
             stage2(tpf);
         }
 
         if(timer[T_EVENT_TIME] > 22  && !dialogueFlag[7]) {
             showDialogue = true;
             dialogueFlag[7] = true;
             say("A magician too?", 1);
             setBackgroundMusic("Sounds/stage2.ogg");
         }
 
         if(timer[T_EVENT_TIME] > 22.2 && !dialogueFlag[8]) {
             dialogueFlag[8] = true;
             say("You'll die here.", 2);
         }
 
         if(timer[T_EVENT_TIME] > 22.4 && !dialogueFlag[9]) {
             dialogueFlag[9] = true;
             say("If someone dies here it's you", 1);
         }
 
         if(timer[T_EVENT_TIME] > 24 && !gameFlag[STAGE2_1]) {
             showDialogue = false;
             stage2spell1(tpf);
         }
         if(timer[T_EVENT_TIME] > 26 && !gameFlag[STAGE2_2])  {
             stage2spell2(tpf);
         }
         if(timer[T_EVENT_TIME] > 28 && !gameFlag[STAGE2_3]) {
             stage2spell3(tpf);
         }
         if(timer[T_EVENT_TIME] > 30 && !gameFlag[STAGE2_4]) {
             stage2spell4(tpf);
         }
         if(timer[T_EVENT_TIME] > 32 && !gameFlag[STAGE2_5])  {
             stage2spell5(tpf);
         }
         if(timer[T_EVENT_TIME] > 34 && !gameFlag[STAGE2_6]) {
             stage2spell6(tpf);
         }
         if(timer[T_EVENT_TIME] > 34 && gameFlag[STAGE2_6] && !gameFlag[STAGE2_L]) {
             stage2spellL(tpf);
         }
         if(timer[T_EVENT_TIME] > 36 && !dialogueFlag[10]) {
             showDialogue = true;
             say("I'm out",1);
             dialogueFlag[10] = true;
         }
         if(timer[T_EVENT_TIME] > 36.2 && !dialogueFlag[11]) {
             say("Yeah later",2);
             dialogueFlag[11] = true;
             gameFlag[STAGE2] = true;
         }
 
         if(timer[T_EVENT_TIME] > 38 && !gameFlag[STAGE3_0]) {
             showDialogue = false;
             stage3(tpf);
         }
 
         if(timer[T_EVENT_TIME] > 40  && !dialogueFlag[12]) {
             showDialogue = true;
             dialogueFlag[12] = true;
             say("Nice hair.", 1);
             setBackgroundMusic("Sounds/stage3.ogg");
         }
 
         if(timer[T_EVENT_TIME] > 40.2 && !dialogueFlag[13]) {
             dialogueFlag[13] = true;
             say("Let's finish this quickly.", 2);
         }
 
         if(timer[T_EVENT_TIME] > 40.4 && !dialogueFlag[14]) {
             dialogueFlag[14] = true;
             say("That helps, I'm busy right now", 1);
         }
 
         if(timer[T_EVENT_TIME] > 42 && !gameFlag[STAGE3_1]) {
             showDialogue = false;
             stage3spell1(tpf);
         }
         if(timer[T_EVENT_TIME] > 43 && !gameFlag[STAGE3_2]) {
             stage3spell2(tpf);
         }
         if(timer[T_EVENT_TIME] > 44 && !gameFlag[STAGE3_3]) {
             stage3spell3(tpf);
         }
         if(timer[T_EVENT_TIME] > 45 && !gameFlag[STAGE3_4]) {
             stage3spell4(tpf);
         }
         if(timer[T_EVENT_TIME] > 46 && !gameFlag[STAGE3_5]) {
             stage3spell5(tpf);
         }
         if(timer[T_EVENT_TIME] > 47 && !gameFlag[STAGE3_6]) {
             stage3spell6(tpf);
         }
         if(timer[T_EVENT_TIME] > 48 && !gameFlag[STAGE3_L]) {
             stage3spellL(tpf);
         }
         if(timer[T_EVENT_TIME] > 49 && !dialogueFlag[15]) {
             showDialogue = true;
             say("Ahh..",2);
             dialogueFlag[15] = true;
         }
         if(timer[T_EVENT_TIME] > 49.2 && !dialogueFlag[16]) {
             say("Not bad right?",1);
             dialogueFlag[16] = true;
             gameFlag[STAGE3] = true;
         }
 
         if(timer[T_EVENT_TIME] > 49 && !gameFlag[STAGE4_0]) {
             showDialogue = false;
             stage4(tpf);
         }
 
         if(timer[T_EVENT_TIME] > 53  && !dialogueFlag[17]) {
             showDialogue = true;
             dialogueFlag[17] = true;
             say("What are you so mad about?", 1);
             setBackgroundMusic("Sounds/stage4.ogg");
         }
 
         if(timer[T_EVENT_TIME] > 53.2 && !dialogueFlag[18]) {
             dialogueFlag[18] = true;
             say("Just die quickly!", 2);
         }
 
         if(timer[T_EVENT_TIME] > 53.4 && !dialogueFlag[19]) {
             dialogueFlag[19] = true;
             say("You realize.. nevermind.", 1);
         }
 
         if(timer[T_EVENT_TIME] > 55 && !gameFlag[STAGE4_1]) {
             showDialogue = false;
             stage4spell1(tpf);
         }
         if(timer[T_EVENT_TIME] > 56 && !gameFlag[STAGE4_2]) {
             stage4spell2(tpf);
         }
         if(timer[T_EVENT_TIME] > 57 && !gameFlag[STAGE4_3]) {
             stage4spell3(tpf);
         }
         if(timer[T_EVENT_TIME] > 58 && !gameFlag[STAGE4_4]) {
             stage4spell4(tpf);
         }
         if(timer[T_EVENT_TIME] > 59 && !gameFlag[STAGE4_5]) {
             stage4spell5(tpf);
         }
         if(timer[T_EVENT_TIME] > 60 && !gameFlag[STAGE4_6]) {
             stage4spell6(tpf);
         }
         if(timer[T_EVENT_TIME] > 61 && !gameFlag[STAGE4_L]) {
             stage4spellL(tpf);
         }
         if(timer[T_EVENT_TIME] > 62 && !dialogueFlag[20]) {
             showDialogue = true;
             say("!!",2);
             dialogueFlag[20] = true;
         }
         if(timer[T_EVENT_TIME] > 62.2 && !dialogueFlag[21]) {
             showDialogue = false;
             say("Come back a century from now.",1);
             dialogueFlag[21] = true;
             gameFlag[STAGE4] = true;
         }
         if(timer[T_EVENT_TIME] > 63 && !gameFlag[STAGE5_0]) {
             stage5(tpf);
         }
         
         if(timer[T_EVENT_TIME] > 63.5  && !dialogueFlag[22]) {
             dialogueFlag[22] = true;
             showDialogue = true;
             say("What are you so mad about?", 1);
             setBackgroundMusic("Sounds/stage4.ogg");
         }
 
         if(timer[T_EVENT_TIME] > 63.7 && !dialogueFlag[23]) {
             dialogueFlag[23] = true;
             say("Just die quickly!", 2);
         }
 
         if(timer[T_EVENT_TIME] > 63.9 && !dialogueFlag[24]) {
             dialogueFlag[24] = true;
             say("You realize.. nevermind.", 1);
         }
 
         if(timer[T_EVENT_TIME] > 64 && !gameFlag[STAGE5_1]) {
             showDialogue = false;
             gameFlag[GFLAG_MOVE_ENABLED] = true;
             stage5spell1(tpf);
         }
     }
 
     float cutEnemyTime = 0;
     boolean cutEnemyEnterDone, cutEnemyExitDone = false;
     
     private void cutEnemy(float tpf, int stage) {
         cutEnemyTime += tpf;
         if(cutEnemyAlpha > 1) {
             cutEnemyAlpha = 1;
         } else if (cutEnemyAlpha < 0) {
             cutEnemyAlpha = 0;
         }
 
         cutEnemyMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait"+ stage+".png"));
 
         if(cutEnemyTime > 0 && !cutEnemyEnterDone) {
             if(cutEnemy.getLocalTranslation().y < (300 / windowScale)) {
                 cutEnemyAlpha += 0.7f * tpf;
                 cutEnemy.move(0, 300*tpf, 0);
                 cutEnemyMat.setColor("Color", new ColorRGBA(1, 1, 1, cutEnemyAlpha));
             } else {
                 cutEnemyEnterDone = true;
                 cutEnemyExitDone = false;
             }
         }
         if(!cutEnemyExitDone && cutEnemyEnterDone) {
             if(cutEnemy.getLocalTranslation().y < (900 / windowScale)) {
                 cutEnemyAlpha -= 2 * tpf;
                 cutEnemy.move(0, 300*tpf, 0);
                 cutEnemyMat.setColor("Color", new ColorRGBA(1, 1, 1, cutEnemyAlpha));
             } else {
                 cutEnemyExitDone = true;
                 cutEnemyEnterDone = false;
                 cutEnemyDone = true;
                 cutEnemyTime = 0;
                 cutEnemy.setLocalTranslation(screenWidth - 100, -200, 0);
             }
         }
     }
 
     float spellcardBannerTime = 0;
     boolean spellcardBannerEnterDone, spellcardBannerExitDone = false;
     boolean spellcardBannerDone = false;
     private void spellcardBanner(float tpf) {
         spellcardBannerTime += 1/60f;
         if(spellcardBannerAlpha > 1) {
             spellcardBannerAlpha = 1;
         } else if(spellcardBannerAlpha < 0) {
             spellcardBannerAlpha = 0;
         }
         if(spellcardBannerTime > 0 && !spellcardBannerEnterDone) {
             if(spellcardBannerAlpha < 1) {
                 spellcardBannerAlpha += 0.7f * 1/60f;
                 spellcardBanner.move(0, -200f*1/60f, 0);
                 spellcardBannerMat.setColor("Color", new ColorRGBA(1,1,1, spellcardBannerAlpha));
                 spellcardBanner.setMaterial(spellcardBannerMat);
             } else {
                 spellcardBannerEnterDone = true;
             }
         }
         if(!spellcardBannerExitDone && spellcardBannerEnterDone) {
             if(spellcardBannerAlpha > 0) {
                 spellcardBannerAlpha -= tpf;
                 spellcardBanner.move(0, -200f*1/60f, 0);
                 spellcardBannerMat.setColor("Color", new ColorRGBA(1, 1, 1, spellcardBannerAlpha));
                 spellcardBanner.setMaterial(spellcardBannerMat);
             } else {
                 spellcardBannerExitDone = true;
                 spellcardBannerDone = true;
                 spellcardBannerTime = 0;
             }
         }
     }
     
     float introBannerSpeed;
     final static float introSequenceTime1 = 2;
     final static float introSequenceTime2 = 4;
     final static float introSequenceTime3 = 6;
     final static float introSequenceTime4 = 7;
     
     private void introSequence(float tpf) {
         timer[T_INTRO_TIME] += 1/60f;
         if(introBannerAlpha > 1) {
             introBannerAlpha = 1;
         } else if (introBannerAlpha < 0) {
             introBannerAlpha = 0;
         }
         for(int i = 0; i < 6; i++) {
             menuAlpha[i] = 0;
         }
         menuPause.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[1]));
         menuGameOver.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[2]));
         menuContinue.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[3]));
         menuRetry.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[4]));
         menuReturn.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[5]));
         
         if(timer[T_INTRO_TIME] < introSequenceTime1) {
             screenFadeOverlayAlpha = 1;
             screenFadeOverlayMat.setColor("Color", new ColorRGBA(0,0,0,screenFadeOverlayAlpha));
             gameFaded = true;
             gameUnfaded = false;
             introBannerSpeed = -240f;
         } else {
             unfadeGame(1/60f*0.1f, true);
         }
         if(debug) {
             timer[T_INTRO_TIME] = 10;
         }
         if(timer[T_INTRO_TIME] > introSequenceTime2 && !introBannerEnterDone) {
             if(introBanner.getLocalTranslation().x < (900 / windowScale)) {
                 introBannerAlpha += 1/60f;
                 introBanner.move(-introBannerSpeed*1/60f,0,0);
                 introBannerMat.setColor("Color", new ColorRGBA(1, 1, 1, introBannerAlpha));
             } else {
                 introBannerEnterDone = true;
             }
         }
         if(timer[T_INTRO_TIME] > introSequenceTime2 && introBannerEnterDone && timer[T_INTRO_TIME] < introSequenceTime3) {
             introBanner.move(-introBannerSpeed*1/60f,0,0);
             if(introBannerSpeed < -30) introBannerSpeed += 5;
         }
         if(timer[T_INTRO_TIME] > introSequenceTime3 && !introBannerExitDone && introBannerEnterDone) {
             if(introBannerSpeed > -480) introBannerSpeed -= 10;
             if(introBanner.getLocalTranslation().x < 1400) {
                 introBannerAlpha -= 1/60f;
                 introBanner.move(-introBannerSpeed*1/60f,0,0);
                 introBannerMat.setColor("Color", new ColorRGBA(1, 1, 1, introBannerAlpha));
             } else {
                 introBannerExitDone = true;
             }
         }
         if(timer[T_INTRO_TIME] > introSequenceTime4) {
             timer[T_INTRO_TIME] = 0;
             introBannerEnterDone = false;
             introBannerExitDone = false;
             introBannerAlpha = 0;
             introBannerMat.setColor("Color", new ColorRGBA(1, 1, 1, introBannerAlpha));
             camFocalPoint.moveTo(gameFocalPoint, 1f);
             camLoc.moveTo(gameCamLoc,1f);
             introDone = true;
             advanceEventTime = true;
             introBanner.setLocalTranslation(screenWidth - 450 / windowScale, (350 / windowScale),0);
         }
     }
     //----------------------------------------------------------------------------------
     //SPELLCARD VARIABLES
     //----------------------------------------------------------------------------------
     int reps;
     int bulletCount;
     int shotCount;
     boolean bulletsCleared = false;
     
     float spellcard1Length = 60;
     float spellcard2Length = 60;
     float spellcard3Length = 60;
 
     GameObject spellCircle;
     float spellCircleAlpha = 0;
     Spatial spellCircleModel;
     Material spellCircleMat;
 
     //----------------------------------------------------------------------------------
     //SPELLCARD METHODS
     //----------------------------------------------------------------------------------
 
     //Spellcard 1: Duplex Barrier
     boolean squareCreated = false;
     boolean spellCircleCreated = false;
     float circleScale = 0;
     float circleScaleSpeed = 4f;
 
     GameObject reimuSquare;
     Spatial squareModel;
     Texture squareTexture;
     Material reimuSquareMat;
 
     BULLET colorSwitch = BULLET.ARROWSHOT_R;
     boolean interrupt = false;
     boolean spellcard1_1;
 
     //Border Animation Variables
     float scrollAmt = 0;
     //float scrollSpeed = 0.0333333f;  //Field type 1
     float scrollSpeed = 0.2f;
     int counter = 0;
     //float frequency = 0.03f;  //Field type 1
     float frequency = 0.05f;
     float squareTimer = 0;
 
     float fieldScale = 0;
     float fieldScaleSpeed = 2;
     boolean fieldDeployed = false;
 
     GameObject familiarTest;
     GameObject familiarTest2;
 
     float interruptCycleTime = 0;
     boolean cutEnemyDone = false;
     private void stage1spell1(float tpf) {
         openSpell(1, 1, 6, 250, tpf);
         if(!spellFlag[9]) {
             familiarTest = new GameObject("familiarTest");
             objectNode.attachChild(familiarTest);
             familiarTest2 = new GameObject("familiarTest");
             objectNode.attachChild(familiarTest2);
             spellFlag[9] = true;
         }
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             familiarTest.setPos(-FastMath.cos(spellTimer[0])*15,FastMath.sin(spellTimer[0])*15,0);
             familiarTest2.setPos(FastMath.cos(spellTimer[0])*15,FastMath.sin(spellTimer[0])*15,0);
 
             interruptCycleTime += tpf;
 
             if(interruptCycleTime > 0.2) {
                 interrupt = !interrupt;
                 interruptCycleTime = 0;
             }
             if(spellTimer[2] > 2) {
                 spellcard1_1 = false;
                 spellTimer[2] = 0;
             }
             if(spellTimer[4] > 5) {
                 spellTimer[4] = 0;
                 spellFlag[4] = !spellFlag[4];
             }
             if(spellTimer[1] > 0.02 && !interrupt && spellFlag[0] && spellFlag[4]) {
                 fireStraightCircle(familiarTest.getLocalTranslation(), 12, 1, spellTimer[0] * 0.5f, 20f, 1f, BULLET.TALISMAN_R);
                 fireStraightCircle(familiarTest2.getLocalTranslation(), 12, 1, -spellTimer[0] * 0.5f, 20f, 1f, BULLET.TALISMAN_B);
             }
 
             if(spellTimer[5] > 0.5 && spellFlag[0] && !spellFlag[4]) {
                 fireSpeedCircle(enemy.getLocalTranslation(), 8, 1, -spellTimer[0], 10, 2,10f, 2f, BULLET.TALISMAN_B);
                 fireSpeedCircle(enemy.getLocalTranslation(), 8, 1, spellTimer[0], 10, 2,10f, 2f, BULLET.TALISMAN_B);
                 fireSpeedCircle(enemy.getLocalTranslation().add(30,0,0), 8, 1, spellTimer[0], 10f,1,30, 1f, BULLET.TALISMAN_R);
                 fireSpeedCircle(enemy.getLocalTranslation().add(-30,0,0), 8, 1, -spellTimer[0], 10f,1,30, 1f, BULLET.TALISMAN_R);
                 spellTimer[5] = 0;
             }
         } else {
             if(!spellFlag[8]) {
                 familiarTest.removeFromParent();
                 familiarTest2.removeFromParent();
                 spellFlag[8] = true;
             }
         }
         closeSpell(STAGE1_1, 60, 150, tpf);
     }
    private void duplex(float tpf) {
         bulletsCleared = false;
         if(!cutEnemyDone) {
             enemy.life = 250;
             cutEnemy(tpf,1);
             stage = 1;
             spell = 2;
         }
         if(spellTimer[0] > 4) {
             spellFlag[0] = true;
         }
         if(!spellFlag[9]) {
             familiarTest = new GameObject("familiarTest");
             objectNode.attachChild(familiarTest);
             familiarTest2 = new GameObject("familiarTest");
             objectNode.attachChild(familiarTest2);
             spellFlag[9] = true;
         }
         familiarTest.setPos(FastMath.cos(-spellTimer[0])*15,FastMath.sin(spellTimer[0])*15,0);
         familiarTest2.setPos(FastMath.cos(spellTimer[0])*15,FastMath.sin(spellTimer[0])*15,0);
         //First card, so initiate spell circle
         if(!spellCircleCreated) {
             System.out.println("Stage 1 Spell 1");
 
             spellCircle = new PanelNode("spellCircle");
             spellCircleModel = assetManager.loadModel("Models/game/spellCircle.j3o");
             spellCircleMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
             TextureKey spellCircleMatTextureKey = new TextureKey("Textures/game/spellCircle1.png", false);
             Texture spellCircleMatTex = assetManager.loadTexture(spellCircleMatTextureKey);
             spellCircleMat.setTexture("ColorMap", spellCircleMatTex);
             spellCircleMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
             spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
             spellCircleModel.setMaterial(spellCircleMat);
             spellCircle.attachChild(spellCircleModel);
             spellCircle.setQueueBucket(Bucket.Translucent);
             shotNode.attachChild(spellCircle);
 
             spellCircle.scale(0.1f);
             spellCircleCreated = true;
         }
         if(spellCircleAlpha < 1) {
             spellCircleAlpha += tpf;
             spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
         }
 
         spellCircle.rotate(0,0,tpf * 3);
         //Create Hakurei Border square
         if(!squareCreated) {
             reimuSquare = new GameObject("ReimuSquare");
             squareModel = assetManager.loadModel("/Models/game/field2.j3o");
             reimuSquareMat = new Material(assetManager, "MatDefs/scrollShader.j3md");
             squareTexture = assetManager.loadTexture("Textures/game/field2.png");
             reimuSquareMat.setTexture("ColorMap", squareTexture);
             squareModel.setMaterial(reimuSquareMat);
             reimuSquare.attachChild(squareModel);
             squareTexture.setWrap(Texture.WrapMode.Repeat);
             bulletNode.attachChild(reimuSquare);
             squareCreated = true;
             reimuSquare.scale(0.01f);
         }
 
         //Get one side of the square scale to get the scale of the field
         fieldScale = reimuSquare.getLocalScale().x;
 
         //If the scale is smaller than one, keep upscaling.
         if(fieldScale < 1) {
             reimuSquare.scale(1 + tpf*fieldScaleSpeed);
         } else {
             fieldDeployed = true;
         }
 
         //Do the same for the spell Circle.
         circleScale = spellCircle.getLocalScale().x;
 
         if(circleScale < 2) {
             spellCircle.scale(1 + tpf*circleScaleSpeed);
         }
 
         squareTimer += tpf;
         if(squareTimer > frequency) {
             scrollAmt += scrollSpeed;
             reimuSquareMat.setFloat("m_ScrollX", scrollAmt);
             squareTimer = 0;
         }
         if(scrollAmt > 0.9333) {
             scrollAmt = 0;
         }
 
         spellcardActive=true;
         updateSpellTimer(tpf, 5);
         interruptCycleTime += tpf;
 
         if(interruptCycleTime > 1 && fieldDeployed) {
             interrupt = !interrupt;
             interruptCycleTime = 0;
         }
         if(spellTimer[2] > 2 && fieldDeployed) {
             spellcard1_1 = false;
             spellTimer[2] = 0;
         }
         if(spellTimer[1] > 0.1 && !interrupt && fieldDeployed && spellFlag[0]) {
             fireReflectCircle(player.getLocalTranslation(),2,1,FastMath.sin(spellTimer[0])*1.4f,16f, BULLET.TALISMAN_B);
             fireReflectCircle(player.getLocalTranslation(),2,1,FastMath.sin(spellTimer[0]+1.07f)*1.4f,16f, BULLET.TALISMAN_B);
         }
 
         if(spellTimer[1] > 0.1 && fieldDeployed) {
             fireReflectCircle(player.getLocalTranslation(),2,1,FastMath.cos(spellTimer[0])*2,8f, BULLET.TALISMAN_R);
             fireReflectCircle(player.getLocalTranslation(),2,1,FastMath.cos(spellTimer[0]+1.7f)*2,8f, BULLET.TALISMAN_R);
             spellTimer[1] = 0;
         }
 
         if(spellTimer[2] > 1 && !spellcard1_1 && fieldDeployed) {
             fireReflectLine(player.getLocalTranslation(), 16, 16f, BULLET.TALISMAN_R);
             fireReflectCircle(player.getLocalTranslation(),64,3,FastMath.sin(spellTimer[0]+1.07f)*1.4f,2f, BULLET.TALISMAN_R);
             spellcard1_1 = true;
         }
 
         if((spellTimer[0] > spellcard1Length || enemy.life <= 200) && !bulletsCleared) {  //Clean up spellcard
             resetCardVars();
             spellcardActive = false;
             spellCircleCreated = false;
             fadeBullets(tpf);
             if(bulletFade <= 0) {
                 cutEnemyAlpha = 0;
                 cutEnemyTime = 0;
                 bulletFade = 1;
                 bulletsCleared = true;
                 clearBullets();
                 gameFlag[STAGE1_1] = true;
                 bulletLight.setColor(new ColorRGBA(1, 1, 1, 1));
                 cutEnemyDone = false;
             }
         }
     }
     boolean spellcard2_1, spellcard2_2, spellcard2_3, spellcard2_3_1, spellcard2_3_2, moved = false;
 
     Vector3f s1s2movevec;
     private void stage1spell2(float tpf) {
         openSpell(1,2,10,250, tpf);
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             //Move around a bit.
             if(spellTimer[1] > 0 && !moved) {
                 s1s2movevec = new Vector3f(FastMath.nextRandomFloat() * 10, FastMath.nextRandomFloat() * 10, 0);
                 moved = true;
             }
             enemy.moveTo(s1s2movevec, 20 * tpf);
             spellcardActive = true;
             updateSpellTimer(tpf, 2);
             if(spellTimer[1] > 2 && !spellcard2_1) {
                 fireSpeedCircle(enemy.getLocalTranslation(), 64, 8, 0, 18f, 2, 8, 1f, colorSwitch);
                     if(colorSwitch == BULLET.TALISMAN_R) {
                         colorSwitch = BULLET.TALISMAN_W;
                     } else if(colorSwitch == BULLET.TALISMAN_W) {
                         colorSwitch = BULLET.TALISMAN_R;
                     } else {
                         colorSwitch = BULLET.TALISMAN_R;
                     }
                 spellcard2_1 = true;
             }
 
             if(spellTimer[1] > 3 && !spellcard2_2) {
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.00f, 14.0f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.02f, 14.5f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.04f, 15f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.06f, 15.5f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.08f, 16.0f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.10f, 16.5f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.12f, 17.0f, 2, 8, 1f, colorSwitch);
                 fireSpeedCircle(enemy.getLocalTranslation(), 48, 1, 0.14f, 17.5f, 2, 8, 1f, colorSwitch);
                 spellcard2_2 = true;
             }
             if(spellTimer[1] > 5 && !spellcard2_3) {
                 fireStraightLine(enemy.getLocalTranslation(),player.getLocalTranslation(), 26, 0, 9.2f,1,BULLET.TALISMAN_R);
                 spellcard2_3 = true;
             }
             if(spellTimer[1] > 5.4 && !spellcard2_3_1) {
                 fireStraightLine(enemy.getLocalTranslation(),player.getLocalTranslation(), 26, 0, 9.2f,1,BULLET.TALISMAN_R);
                 spellcard2_3_1 = true;
             }
             if(spellTimer[1] > 5.6 && !spellcard2_3_2) {
                 fireStraightLine(enemy.getLocalTranslation(),player.getLocalTranslation(), 26, 0, 9.2f,1,BULLET.TALISMAN_R);
                 spellcard2_3_2 = true;
             }
             //Restart cycle
             if(spellTimer[1] > 5.8) {
                 spellTimer[1] = 0;
                 moved = false;
                 spellcard2_1 = false;
                 spellcard2_2 = false;
                 spellcard2_3 = false;
                 spellcard2_3_1 = false;
                 spellcard2_3_2 = false;
             }
         }
         closeSpell(STAGE1_2, 60, 150, tpf);
     }
 
     boolean spellcard3_1,spellcard3_1_2, spellcard3_1_3,spellcard3_2, spellcard3_3, unMoved;
 
     private void fadeBullets(float tpf) {
         bulletFade -= tpf/2;
         spellCircleAlpha -= tpf;
         spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
         spellCircle.setMaterial(spellCircleMat);
     }
 
     private void clearBullets() {
         bulletNode.detachAllChildren();
         bulletCount = 0;
     }
 
     private void stage1spell3(float tpf) {
         openSpell(1,3,10, 250,tpf);
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             //Reset position
             if(!unMoved) {
                 enemy.moveTo(Vector3f.ZERO, 0.01f);
             }
             enemy.moveTo(new Vector3f(FastMath.nextRandomFloat()*10,FastMath.nextRandomFloat()*10,0f),0.001f);
             if(spellTimer[1] > 1.5 && !spellcard3_2) {
                 spellcard3_2 = true;
                 fireStraightLine(enemy.getLocalTranslation(),player.getLocalTranslation(), 4,0, 8,2,BULLET.TALISMAN_R);
             }
             if(spellTimer[1] > 3 && !spellcard3_1) {
                 fireStraightCircle(enemy.getLocalTranslation(),92,4,FastMath.nextRandomFloat(),35, 2f, BULLET.TALISMAN_R);
                 fireStraightCircle(enemy.getLocalTranslation(),86,3,FastMath.nextRandomFloat(),27, 1.5f, BULLET.TALISMAN_R);
 
                 fireStraightCircle(enemy.getLocalTranslation(),3,1,FastMath.nextRandomFloat(),28, 2f, BULLET.BALLSHOT_R);
                 fireStraightCircle(enemy.getLocalTranslation(),55,2,FastMath.nextRandomFloat(),34f, 1.7f, BULLET.TALISMAN_R);
                 fireStraightCircle(enemy.getLocalTranslation(),48,3,FastMath.nextRandomFloat(),35, 1.7f, BULLET.TALISMAN_R);
 
                 fireStraightCircle(enemy.getLocalTranslation(),8,1,FastMath.nextRandomFloat(),36, 2f, BULLET.BALLSHOT_W);
                 fireStraightCircle(enemy.getLocalTranslation(),55,2,FastMath.nextRandomFloat(),37, 1.7f, BULLET.TALISMAN_R);
                 fireStraightCircle(enemy.getLocalTranslation(),48,3,FastMath.nextRandomFloat(),19, 1.7f, BULLET.TALISMAN_R);
                spellcard3_1 = true;
             }
             if(spellTimer[1] > 3.1 && !spellcard3_1_2) {
                 fireStraightCircle(enemy.getLocalTranslation(),75,3,FastMath.nextRandomFloat(),42.5f,2f,BULLET.TALISMAN_R);
                 fireStraightCircle(enemy.getLocalTranslation(),6,1,FastMath.nextRandomFloat(),40.5f,1.5f,BULLET.BALLSHOT_W);
                 fireStraightCircle(enemy.getLocalTranslation(),45,2,FastMath.nextRandomFloat(),39,2f,BULLET.TALISMAN_R);
                spellcard3_1_2 = true;
             }
             if(spellTimer[1] > 3.2 && !spellcard3_1_3) {
                 fireStraightCircle(enemy.getLocalTranslation(),7,1,FastMath.nextRandomFloat(),39.5f,2f,BULLET.BALLSHOT_R);
                 fireStraightCircle(enemy.getLocalTranslation(),55,2,FastMath.nextRandomFloat(),31.5f,1.7f,BULLET.TALISMAN_R);
                 fireStraightCircle(enemy.getLocalTranslation(),48,3,FastMath.nextRandomFloat(),21,1.7f,BULLET.TALISMAN_R);
                spellcard3_1_3 = true;
             }
             if(spellTimer[1] > 4) {
                 spellTimer[1] = 0;
                 spellcard3_2 = false;
                 spellcard3_1 = false;
                 spellcard3_1_2 = false;
                 spellcard3_1_3 = false;
             }
         }
         closeSpell(STAGE1_3,60,150,tpf);
     }
 
     boolean spell4familiarCreated = false;
     Vector3f spell4familiarVector = new Vector3f();
     Vector3f spell4familiarVector2 = new Vector3f();
     GameObject spell4familiar1 = new GameObject("spell4familiar");
     GameObject spell4familiar2 = new GameObject("spell4familiar2");
     private void stage1spell4(float tpf) {
         openSpell(1, 4, 9, 250, tpf);
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             if(!spellFlag[9]) {
                 spell4familiar1.attachChild(ballShotR.clone().scale(2));
                 spell4familiar2.attachChild(ballShotB.clone().scale(2));
                 bulletNode.attachChild(spell4familiar1);
                 bulletNode.attachChild(spell4familiar2);
                 spellFlag[9] = true;
             }
 
             spell4familiar1.setX(FastMath.cos(spellTimer[0]*3)*15);
             spell4familiar1.setY(FastMath.sin(spellTimer[0]*3)*15);
             spell4familiar2.setLocalTranslation(new Vector3f(FastMath.cos(-(spellTimer[0] + 3.14f)*3) * 15, FastMath.sin(-(spellTimer[0] + 3.14f)*3) * 15, 0).add(enemy.getPos()));
             spell4familiar1.setZ(0);
             spell4familiar1.setPos(spell4familiar1.getPos().add(enemy.getPos()));
             spell4familiarVector.set(spell4familiar1.getLocalTranslation().subtract(enemy.getLocalTranslation()).mult(3));
             spell4familiarVector2.set(spell4familiar2.getLocalTranslation().subtract(enemy.getLocalTranslation()).mult(3));
             if(spellTimer[1] > 0.04) {
                 fireStraightLine(spell4familiar1.getPos(), spell4familiarVector, 4, 1.7f, 26f, 0.7f, BULLET.TALISMAN_R);
                 fireStraightLine(spell4familiar2.getPos(), spell4familiarVector2, 4, -1.7f, 26f, 0.7f, BULLET.TALISMAN_B);
                 spellTimer[1] = 0;
             }
             if(spellTimer[2] > 3 && !spellFlag[7]) {/*
                 fireCurveShot1(enemy.getPos(), enemy.getPos().add(-10,0,0), player.getPos().add(10,0,0), 24f, 1f, BALLSHOT_W);
                 fireCurveShot1(enemy.getPos(), enemy.getPos().add(10,0,0), player.getPos().add(-10,0,0), 24f, 1f, BALLSHOT_R);
                 fireCurveShot1(enemy.getPos(), enemy.getPos().add(-10,0,0), player.getPos().add(10,0,0), 32f, 1f, BALLSHOT_W);
                 fireCurveShot1(enemy.getPos(), enemy.getPos().add(10,0,0), player.getPos().add(-10,0,0), 32f, 1f, BALLSHOT_R);
                 fireCurveShot1(enemy.getPos(), enemy.getPos().add(-10,0,0), player.getPos().add(10,0,0), 48f, 1f, BALLSHOT_W);
                 fireCurveShot1(enemy.getPos(), enemy.getPos().add(10,0,0), player.getPos().add(-10,0,0), 48f, 1f, BALLSHOT_R);*/
                 fireStraightLine(enemy.getPos(), player.getPos(), 6, 0.1f, 64f, 1.5f, BULLET.TALISMAN_B);
                 fireStraightLine(enemy.getPos(), player.getPos(), 6, 0.05f, 58f, 1.5f, BULLET.TALISMAN_R);
                 fireStraightLine(enemy.getPos(), player.getPos(), 6, -0.1f, 52f, 1.5f, BULLET.TALISMAN_B);
                 fireStraightLine(enemy.getPos(), player.getPos(), 6, -0.05f, 46f, 1.5f, BULLET.TALISMAN_R);
                 fireStraightLine(enemy.getPos(), player.getPos(), 6, 0, 40f, 1.5f, BULLET.TALISMAN_B);
                 spellFlag[7] = true;
             }
             if(spellTimer[2] > 3 && !spellFlag[1]) {
                 spellFlag[1] = true;
                 fireStraightCircle(enemy.getLocalTranslation(), 48, 3, FastMath.nextRandomFloat(), 21, 1.7f, BULLET.TALISMAN_B);
             }
             if(spellTimer[2] > 4 && !spellFlag[2]) {
                 //fireStraightCircle(enemy.getLocalTranslation(), 48, 3, FastMath.nextRandomFloat(), 21, 1.7f, BALLSHOT_W);
                 spellTimer[2] = 0;
                 spellFlag[7] = false;
                 spellFlag[1] = false;
                 spellFlag[2] = false;
             }
         } else {
             spell4familiar1.removeFromParent();
             spell4familiar2.removeFromParent();
         }
         closeSpell(STAGE1_4, 60, 90, tpf);
     }
 
     private void stage1spell5(float tpf) {
         openSpell(1,5,5,250,tpf);
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)){
             if(!spellFlag[3]) {
                 System.out.println("Stage 1 Spell 5");
                 spellFlag[3] = true;
             }
             if(spellTimer[1] > 0.3) {
                 fireSpeedCircle(enemy.getLocalTranslation(), 36, 1, spellTimer[0] * tpf * 20+FastMath.rand.nextFloat(), 20, 4f, 4, 1, BULLET.TALISMAN_R);
                 //fireSpeedCircle(enemy.getPos(), 16, 1, spellTimer[0], 30, 1f, 20, 1, TALISMAN_R);
                 spellTimer[1] = 0;
             }
             if(spellTimer[2] > 6) {
                //fireStraightCircle(enemy.getPos(), (int)(64 + spellTimer[0] * 5), 1, FastMath.rand.nextFloat(), 40, 1, TALISMAN_B);
                 spellFlag[3] = true;
             }
             if(spellFlag[3] && spellTimer[3] > 0.2) {
                 fireUncannySealCircle(enemy.getPos(), 4, 1, 2, 2, 1, true, spellTimer[0], 20, 1, BULLET.TALISMAN_B);
                 fireUncannySealCircle(enemy.getPos(), 4, 1, 2, 2, 1, false, spellTimer[0], 20, 1, BULLET.TALISMAN_B);
                 spellTimer[3] = 0;
             }
         }
         closeSpell(STAGE1_5,60,150,tpf);
     }
 
     boolean spell6_1;
     float t1 = 1.2f;
     float t2 = 1.9f;
     float s1s6speed = 25;
     private void stage1spell6(float tpf) {
         openSpell(1, 6, 5, 250, tpf);
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             //Spell vars:
             //4 : offset time
             //5 : offset control
             if(spellTimer[4] > 9) {
                 spellTimer[4] = 0;
                 spellFlag[5] = !spellFlag[5];
             }
             //bloomFilter.setBloomIntensity(spellTimer[0]);
             if(spellFlag[5]) {
                 spellTimer[5] += tpf*0.1f;
             } else {
                 spellTimer[5] -= tpf*0.1f;
             }
             if(spellTimer[1] > 0.1) {
                 fireUncannySealCircle(enemy.getLocalTranslation(),8,t1,t2, 0.1f,-0.4f, true, spellTimer[5],  s1s6speed, 1,BULLET.TALISMAN_R);
                 fireUncannySealCircle(enemy.getLocalTranslation(),8,t1,t2, 0.1f,-0.4f, false, spellTimer[5],  s1s6speed, 1,BULLET.TALISMAN_R);
                 fireUncannySealCircle(enemy.getLocalTranslation(),8,t1,t2, 1f,-1f, true, spellTimer[5],  s1s6speed, 1,BULLET.TALISMAN_B);
                 fireUncannySealCircle(enemy.getLocalTranslation(),8,t1,t2, 1f,-1f, false, spellTimer[5],  s1s6speed, 1,BULLET.TALISMAN_B);
                 fireStraightCircle(enemy.getPos(), 8, 1, spellTimer[5],  s1s6speed, 1, BULLET.TALISMAN_B);
                 spellTimer[1] = 0;
             }
             if(spellTimer[2] > 0.3) {
                 fireSpeedCircle(enemy.getPos(), 32, 1, spellTimer[0], 60, 0.5f, 16, 1, BULLET.PILLSHOT_R);
                 spellTimer[2] = 0;
             }
         }
         closeSpell(STAGE1_6,90,50,tpf);
     }
     private void stageClear(float tpf, int stage, int stageflag) {
         stageActive = true;
         updateSpellTimer(tpf, 1);
         if(!spellFlag[3]) {
             System.out.println("Stage "+stage+" Spell L");
             spellFlag[3] = true;
             gameFlag[80] = false;
             gameFlag[81] = false;
             gameFlag[82] = false;
             gameFlag[83] = false;
         }
         if(spellTimer[0] > 0 && !gameFlag[80]) {
             clearBullets();
             gameFlag[80] = true;
             enemy.moveTo(new Vector3f(50,-200,0), 0.2f);
             enemyDeathSequence();
         }
         if(spellTimer[0] > 3 && !gameFlag[82]) {
             System.out.println("Stage "+stage+" Closing");
             enemyDeathEmitter.emitAllParticles();
             enemyDeathEmitter.setParticlesPerSec(0);
             
             gameFlag[82] = true;
             gameFlag[GFLAG_SCORE] = true;
             //stageClearDisplay1.setText("STAGE "+stage+" CLEAR");
         }
         if(spellTimer[0] > 5 && !gameFlag[81]) {
             //fadeGame(tpf,false);
             //if(stageClearDisplay1.getLocalTranslation().x < (screenWidth)/2) {
             //    stageClearDisplay1.move(7,0,0);
             //}
             if(spellTimer[0] > 6) {
                 gameFlag[81] = true;
             }
         }
         if((spellTimer[0] > 8 && !gameFlag[83])) {
             //if(stageClearDisplay1.getLocalTranslation().x > -screenWidth/2) {
                 //stageClearDisplay1.move(-12,0,0);
             //}
             if(spellTimer[0] > 8.7) {
                 //stageClearDisplay1.setLocalTranslation(-screenWidth/2, screenHeight/2, 11);
                 gameFlag[83] = true;
                 spellFlag[SFLAG_ZWAIT] = true;
                 gameFlag[GFLAG_SCORE] = false;
             }
         }
         if(spellTimer[0] > 16) {
             spellFlag[SFLAG_SCORE] = true;
         }
         if(spellFlag[SFLAG_SCORE] && !gameFlag[stageflag]) {
              resetCardVars();
              spellcardActive = false;
              stageActive = false;
              gameFlag[GFLAG_SCORE] = false;
              gameFlag[stageflag] = true;
         }
     }
     private void stage1spellL(float tpf) {
         stageClear(tpf, 1, STAGE1_L);
     }
     
     private void stage2(float tpf) {
         stageActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE2_0_1]) {
             System.out.println("Stage 2 Start");
             setUpEnemy(2);
             enemy.moveTo(Vector3f.ZERO, 4f);
             gameFlag[STAGE2_0_1] = true;
         }
         if(spellTimer[0] > 3 && !gameFlag[STAGE2_0]) {
             stageActive = false;
             gameFlag[STAGE2_0] = true;
             resetCardVars();
         }
     }
 
     Vector3f stage2spell1FocalPoint = new Vector3f(0,90,0);
     int s2s1counter = 0;
     private void stage2spell1(float tpf) {
         openSpell(2, 1, 5, 250, tpf);
         spellCircle.rotate(0,0,tpf);
         
         if(spellTimer[1] > 0.05) {
             int size = 2;
             s2s1counter++;
             if(s2s1counter == 20) {
                 size = 4;
             } else if(s2s1counter == 21) {
                 size = 5;
             } else if(s2s1counter ==22) {
                 size = 4;
                 s2s1counter = 0;
             }
             fireStraightLine(enemy.getPos().add(6,0,0), stage2spell1FocalPoint, 1,  0.08f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(6,0,0), stage2spell1FocalPoint, 1, -0.08f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(6,0,0), stage2spell1FocalPoint, 1, 00.18f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(6,0,0), stage2spell1FocalPoint, 1, -0.18f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(6,0,0), stage2spell1FocalPoint, 1, 00.27f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(6,0,0), stage2spell1FocalPoint, 1, -0.27f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
 
             fireStraightLine(enemy.getPos().add(-6,0,0), stage2spell1FocalPoint, 1,  0.08f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(-6,0,0), stage2spell1FocalPoint, 1, -0.08f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(-6,0,0), stage2spell1FocalPoint, 1, 00.18f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(-6,0,0), stage2spell1FocalPoint, 1, -0.18f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(-6,0,0), stage2spell1FocalPoint, 1, 00.27f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             fireStraightLine(enemy.getPos().add(-6,0,0), stage2spell1FocalPoint, 1, -0.27f*4 + FastMath.sin(spellTimer[0]/4)/6, 60, size, BULLET.BALLSHOT_B);
             spellTimer[1] = 0;
         }
         if(spellTimer[0] > 3) {
             if(spellTimer[2] > 0.4) {
                 int type = FastMath.nextRandomInt(0,3);
                 BULLET btype = BULLET.BALLSHOT_W;
                 switch(type) {
                     case 0:btype = BULLET.BALLSHOT_W;break;
                     case 1:btype = BULLET.BALLSHOT_R;break;
                     case 2:btype = BULLET.BALLSHOT_P;break;
                     case 3:btype = BULLET.BALLSHOT_B;break;
                 }
                 fireSpeedCircle(enemy.getPos(), 16, 1, spellTimer[0] * 0.1f, 5,3,18, 1, btype);
                 fireSpeedCircle(enemy.getPos(), 16, 1, spellTimer[0] * -0.1f, 5,3,18, 1, btype);
                 spellTimer[2] = 0;
             }
         }
         if(spellTimer[0] > 7) {
             if(spellTimer[3] > 1) {
                 for(int i = 0; i < spellTimer[0]/2; i++) {
                     fireStraightLine(enemy.getPos().add(FastMath.nextRandomInt(-10,10), FastMath.nextRandomInt(0,4), 0), player.getPos(), 1, FastMath.nextRandomFloat() - 0.5f, 14, FastMath.nextRandomInt(2,4), BULLET.BALLSHOT_R);
                 }
                 spellTimer[3] = 0;
             }
         }
         closeSpell(STAGE2_1,60,50,tpf);
     }
 
     GameObject s2s2familiar1;
     GameObject s2s2familiar2;
     float s2s2distance = 15;
     int arrowColor = 0;
     BULLET arrowType = BULLET.BALLSHOT_W;
     private void stage2spell2(float tpf)  {
         openSpell(2, 2, 12, 250, tpf);
         if(!spellFlag[10]) {
             spellFlag[10] = true;
             s2s2familiar1 = new GameObject("s2s2familiar1");
             s2s2familiar2 = new GameObject("s2s2familiar2");/*
             s2s2familiar1.attachChild(ballShotR.clone().scale(2));
             s2s2familiar2.attachChild(ballShotR.clone().scale(2));*/
             s2s2familiar1.setPos(enemy.getPos().add(s2s2distance,0,0));
             s2s2familiar2.setPos(enemy.getPos().add(-s2s2distance,0,0));
             bulletNode.attachChild(s2s2familiar1);
             bulletNode.attachChild(s2s2familiar2);
         }
         spellCircle.rotate(0,0,tpf);
         //Spell timer/flags:
         //0 Primary timer
         //1 Cycle timer
         //2 fam1 active
         //3 fam2 active
         //4 move
         //5 Shot active
         //6 Shot2 active
         //7 Shot type counter
         //8 phase
         //9 
         if(!spellFlag[7]) {
             arrowColor++;
             arrowColor = arrowColor % 5;
             if(arrowColor == 0) {
                 arrowType = BULLET.BALLSHOT_W;
             } else if(arrowColor == 1) {
                 arrowType = BULLET.BALLSHOT_R;
             } else if(arrowColor == 2) {
                 arrowType = BULLET.BALLSHOT_B;
             } else if(arrowColor == 3) {
                 arrowType = BULLET.BALLSHOT_P;
             } else if(arrowColor == 4) {
                 arrowType = BULLET.BALLSHOT_B;
             }
 
             spellFlag[7] = true;
         }
 
         if(spellTimer[5] > 0.1 && spellFlag[5]) {
             spellTimer[5] = 0;
             fireSpeedCircle(s2s2familiar1.getPos(), 8, 1, spellTimer[0], 10,1,10, 2f, BULLET.BALLSHOT_W);
             fireSpeedCircle(s2s2familiar1.getPos(), 8, 1, spellTimer[0], 10,2,-10, 1.7f, arrowType);
             fireSpeedCircle(s2s2familiar1.getPos(), 8, 1, spellTimer[0], 10,1,-10, 1f, arrowType);
         }
         if(spellTimer[6] > 0.1 && spellFlag[6]) {
             spellTimer[6] = 0;
             fireSpeedCircle(s2s2familiar2.getPos(), 8, 1, -spellTimer[0], 10,1,10, 2f, BULLET.BALLSHOT_W);
             fireSpeedCircle(s2s2familiar2.getPos(), 8, 1, spellTimer[0], 10,2,-10, 1.7f, arrowType);
             fireSpeedCircle(s2s2familiar2.getPos(), 8, 1, -spellTimer[0], 10,1,-10, 1f, arrowType);
         }
         if(spellTimer[2] > 1 && !spellFlag[2]) {
             float angle = -(spellTimer[2] - 1)*3;
             float cos = FastMath.cos(angle*2) * s2s2distance + enemy.getX();
             float sin = -FastMath.sin(angle*2) * s2s2distance + enemy.getY();
             s2s2familiar1.setPos(cos,sin,0);
             spellFlag[5] = true;
         }
         if(spellTimer[2] > 2 && !spellFlag[2]) {
             spellFlag[7] = false;
             spellFlag[5] = false;
             spellFlag[2] = true;
             s2s2familiar1.setPos(s2s2distance,0,0);
         }
         if(spellTimer[3] > 3 && !spellFlag[3]) {
             spellFlag[6] = true;
             float angle = -(spellTimer[3] - 3)*3;
             float cos = -FastMath.cos(angle*2) * s2s2distance + enemy.getX();
             float sin = -FastMath.sin(angle*2) * s2s2distance + enemy.getY();
             s2s2familiar2.setPos(cos,sin,0);
         }
         if(spellTimer[3] > 4 && !spellFlag[3]) {
             spellFlag[7] = false;
             spellFlag[6] = false;
             spellFlag[3] = true;
             s2s2familiar2.setPos(-s2s2distance,0,0);
         }
         if(spellTimer[4] > 4 && !spellFlag[4]) {
             float newX = FastMath.rand.nextFloat()*40 - 20;
             float newY = FastMath.rand.nextFloat()*20 - 10;
             enemy.moveTo(newX,newY,0,0.1f);
             spellFlag[4] = true;
         }
 
         if(spellTimer[1] > 6) {
             spellTimer[1] = 0;
             spellTimer[2] = 0;
             spellTimer[3] = 0;
             spellTimer[4] = 0;
             spellFlag[2] = false;
             spellFlag[3] = false;
             spellFlag[4] = false;
         }
 
         closeSpell(STAGE2_2,60,50,tpf);
     }
 
     GameObject[] s2s3familiarCircle1;
     GameObject[] s2s3familiarCircle2;
     GameObject s2s3familiarHub1;
     GameObject s2s3familiarHub2;
     
     float circle1X[];
     float circle2X[];
     float circle1Y[];
     float circle2Y[];
     int familiars = 8;
     float s2s3alpha;
     Material s2s3mat;
     private void stage2spell3(float tpf)  {
         openSpell(2,3,9,250,tpf);
 
         if(!spellFlag[8]) {
             spellFlag[8] = true;
 
             s2s3familiarCircle1 = new GameObject[familiars];
             s2s3familiarCircle2 = new GameObject[familiars];
             s2s3familiarHub1 = new GameObject("familiarHub1");
             s2s3familiarHub2 = new GameObject("familiarHub2");
             s2s3alpha = 0;
             s2s3mat = houraiMat.clone();
             s2s3mat.setColor("Color", new ColorRGBA(1,1,1,s2s3alpha));
             for(int i = 0; i < familiars; i++) {
                 s2s3familiarCircle1[i] = new GameObject("familiarCircle1_" + i);
                 s2s3familiarCircle1[i].attachChild(hourai.clone());
                 s2s3familiarCircle1[i].setMaterial(s2s3mat);
                 s2s3familiarHub1.attachChild(s2s3familiarCircle1[i]);
                 
                 s2s3familiarCircle2[i] = new GameObject("familiarCircle2_" + i);
                 s2s3familiarCircle2[i].attachChild(hourai.clone());
                 s2s3familiarCircle2[i].setMaterial(s2s3mat);
                 s2s3familiarHub2.attachChild(s2s3familiarCircle2[i]);
             }
 
             bulletNode.attachChild(s2s3familiarHub1);
             bulletNode.attachChild(s2s3familiarHub2);
         }
         s2s3familiarHub1.setPos(enemy.getPos());
         s2s3familiarHub2.setPos(enemy.getPos());
         //Move enemy
         
         if(spellTimer[2] > 1 && !spellFlag[1]) {
             spellFlag[1] = true;
             enemy.moveTo(FastMath.nextRandomInt(-40,40), FastMath.nextRandomInt(-20,20), 0, 0.1f);
         }
         if(spellTimer[2] > 2 && spellFlag[1]) {
             spellFlag[1] = false;
             spellTimer[2] = 0;
         }
 
         if(spellTimer[3] > 10) {
             spellFlag[3] = !spellFlag[3];
             spellTimer[3] = 0;
         }
 
         for(int i = 0; i < familiars; i++) {
            s2s3familiarCircle2[i].setPos(new Vector3f(FastMath.cos((0.7854f * i) + spellTimer[0] * 0.2f) * 10,FastMath.sin((0.7854f * i) + spellTimer[0] * 0.2f) * 10,0));
            s2s3familiarCircle1[i].setPos(new Vector3f(FastMath.cos(-(0.7854f * i) - spellTimer[0] * 0.2f) * 10,FastMath.sin(-(0.7854f * i) - spellTimer[0] * 0.2f) * 10,0));
         }
         if(s2s3alpha < 0.5f) {
             s2s3alpha += tpf*0.3f;
             s2s3mat.setColor("Color", new ColorRGBA(1,1,1,s2s3alpha));
         }
         //Don't start shooting until dolls are spinning and visible
         if(spellTimer[0] > 3) {
             if(spellTimer[1] > 0.2 && !spellFlag[3]) {
                 for(int i = 0; i < familiars; i++) {
                     fireStraightLine(s2s3familiarCircle1[i].getPos().add(s2s3familiarHub1.getPos()),s2s3familiarCircle1[i].getPos().subtract(s2s3familiarHub1.getPos()).mult(3),1,spellTimer[0],15,FastMath.nextRandomInt(1,2), BULLET.BALLSHOT_W);
                     fireStraightLine(s2s3familiarCircle2[i].getPos().add(s2s3familiarHub1.getPos()),s2s3familiarCircle2[i].getPos().subtract(s2s3familiarHub2.getPos()).mult(3),1,-spellTimer[0],15,FastMath.nextRandomInt(1,2), BULLET.BALLSHOT_B);
                 }
                 spellTimer[1] = 0;
             } else if(spellTimer[1] > 0.1 && spellFlag[3]) {
                 for(int i = 0; i < familiars; i++) {
                     enemy.moveTo(0,0,0, 0.1f);
                     fireSpeedLine(s2s3familiarCircle1[i].getPos().add(s2s3familiarHub1.getPos()),new Vector3f(player.getPos()),1,spellTimer[0],6,10-spellTimer[3],-30,FastMath.nextRandomInt(1,2),BULLET.BALLSHOT_W);
                     fireSpeedLine(s2s3familiarCircle2[i].getPos().add(s2s3familiarHub1.getPos()),new Vector3f(player.getPos()),1,-spellTimer[0],6,10-spellTimer[3],-30,FastMath.nextRandomInt(1,2),BULLET.BALLSHOT_B);
                 }
                 spellTimer[1] = 0;
             }
         }
         closeSpell(STAGE2_3, 60, 50, tpf);
     }
     
     public void closeSpell(int spell, int timeLimit, int lifeLimit,float tpf) {
         if((spellTimer[T_SPELL_MAIN] > timeLimit || enemy.life < lifeLimit) && !bulletsCleared){
             if(spellcardActive) {
                 spellcardActive = false;
                 stageActive = true;
             }
             //Set enemy animation to hurt animation if life is under lifelimit
             if(enemy.life < lifeLimit && !spellFlag[SFLAG_CSPELL]) {
                 try {
                     enemyAnimChan.setAnim("back");
                     enemyAnimChan.setSpeed(0.7f);
                     enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                 } catch(Exception ex) {
                     //No back animation
                 }
                 spellTimer[T_SPELL_FADE] = 0;
                 spellFlag[SFLAG_CSPELL] = true;
             }
             fadeBullets(tpf);
             spellTimer[T_SPELL_FADE] += FastMath.sqrt(tpf);
             //Shrink bullets from the inside out
             Iterator bI = bulletNode.getChildren().iterator();
             while(bI.hasNext()) {
                 Node cn = (Node)bI.next();
                 if(cn instanceof StaticBullet) {
                     StaticBullet cb = (StaticBullet)cn;
                     if(cb.getPos().length() < spellTimer[T_SPELL_FADE]*30) {
                         cb.scale(1-tpf*7);
                         //cb.update(tpf);
                     }
                 }
             }
             //bulletNode.scale(1-tpf);
             if(spellCircleAlpha > 0) {
                 spellCircleAlpha -= tpf;
                 spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
             }
             if(bulletFade <= 0) {
                 enemyAnimChan.setAnim("up",2);
                 enemyAnimChan.setLoopMode(LoopMode.Loop);
                 bulletNode.setLocalScale(1);
                 clearBullets();
                 bulletFade = 1;
                 cutEnemyAlpha = 0;
                 cutEnemyTime = 0;
                 spellCircleAlpha = 0;
                 spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
                 cutEnemyDone = false;
                 resetCardVars();
                 spellcardActive = false;
                 stageActive = false;
                 bulletLight.setColor(new ColorRGBA(1, 1, 1, 1));
                 gameFlag[spell] = true;
                 spellCircleCreated = false;
                 System.out.println("Closing spell");
             }
         }
     }
     GameObject[] s2s4familiars;
     float[] s2s4alpha = new float[12];
     Material[] s2s4mat = new Material[12];
     int clusterVariance = 2;
     int bulletSpeed = 6;
     private void stage2spell4(float tpf) {
         openSpell(2,4,23,250,tpf);
 
         if(!spellFlag[13]) {
             enemy.moveTo(0,-20,0,2f);
             spellFlag[13] = true;
             s2s4familiars = new GameObject[12];
             for(int i = 0; i < 12; i++) {
                 s2s4familiars[i] = new GameObject("stage2spell4familiar" + i);
                 s2s4familiars[i].attachChild(hourai.clone());
                 bulletNode.attachChild(s2s4familiars[i]);
                 s2s4familiars[i].move(0,-100,0);
                 s2s4mat[i] = houraiMat.clone();
                 s2s4familiars[i].setMaterial(s2s4mat[i]);
             }
         }
         if(spellTimer[1] > 0.1) {
             spellTimer[1] = 0;
         }
         if(spellTimer[20] > 3) {
             if(spellTimer[2] > 1) {
                 if(!spellFlag[2]) {
                     spellTimer[15] = 0;
                     spellTimer[14] = 0;
                     spellTimer[13] = 0;
                     spellTimer[12] = 0;
                     spellTimer[11] = 0;
                     spellTimer[10] = 0;
                     spellTimer[9] = 0;
                     spellTimer[8] = 0;
                     spellTimer[7] = 0;
                     spellTimer[6] = 0;
                     spellTimer[5] = 0;
                     spellTimer[4] = 0;
                     spellFlag[2] = true;
                     spellFlag[3] = !spellFlag[3];
                 }
                 
                 //Control fading in and out
                 for(int i = 0; i < 12; i++){
                     if(spellTimer[i+4] > 0.3*i) {
                         s2s4alpha[i] += tpf*0.3;
                         if(s2s4alpha[i] > 1) {
                             s2s4alpha[i] = 1;
                         }
                         s2s4mat[i].setColor("Color", new ColorRGBA(1,1,1,s2s4alpha[i]));
                     } else {
                         s2s4alpha[i] -= tpf*0.7f;
                         if(s2s4alpha[i] < 0) {
                             s2s4alpha[i] = 0;
                         }
                         s2s4mat[i].setColor("Color", new ColorRGBA(1,1,1,s2s4alpha[i]));
                     }
                 }
                 
                 //Move dolls
                 float spacing  = 0.3f;
                 for(int i = 0; i < 12; i++) {
                     if(spellTimer[i+4] > spacing*(i)) {
                         if(!spellFlag[i+4]) {
                             float randX = FastMath.nextRandomInt(i*10-5,i*10+5);
                             if(randX > 50) {
                                 randX -= i*10-50;
                             }
                             float randY = FastMath.nextRandomInt((i-4)*10-5,(i-4)*10+5);
                             if(spellFlag[3]) {
                                 s2s4familiars[i].setPos(-randX,randY, 0);
                             } else {
                                 s2s4familiars[i].setPos(randX, randY, 0);
                             }
                             spellFlag[i+4] = true;
                         }
                     }
                     if(spellTimer[i+4] > spacing*(i+9)) {
                         for(int ii = 0; ii < 4; ii++) {
                             fireStraightCircle(s2s4familiars[i].getPos().add(FastMath.nextRandomInt(-clusterVariance,clusterVariance),FastMath.nextRandomInt(-clusterVariance,clusterVariance),0), 12, 1,ii*0.1f, bulletSpeed, FastMath.nextRandomFloat()*2.5f+1, BULLET.BALLSHOT_R);
                         }
                         spellTimer[i+4] = -70;
                         if(i == 11) {
                         spellFlag[1] = true;
                         }
                     }
                 }
                 if(spellFlag[1]) {
                     for(int i = 0; i < 12; i++) {
                         spellFlag[i+4] = false;
                     }
                     spellTimer[2] = 0;
                     spellTimer[9] = 0;
                     spellTimer[8] = 0;
                     spellTimer[7] = 0;
                     spellTimer[6] = 0;
                     spellTimer[5] = 0;
                     spellTimer[4] = 0;
                     spellTimer[10] = 0;
                     spellTimer[11] = 0;
                     spellTimer[12] = 0;
                     spellTimer[13] = 0;
                     spellTimer[14] = 0;
                     spellTimer[15] = 0;
                     spellFlag[1] = false;
                     spellFlag[2] = false;
                 }
             }
         }
         closeSpell(STAGE2_4,60,50,tpf);
     }
     
     int s2s5famCount = 12;
     GameObject[] s2s5shot = new GameObject[s2s5famCount];
     float[] s2s5shotAlpha = new float[s2s5famCount];
     Material[] s2s5shotMat = new Material[s2s5famCount];
     
     GameObject[] s2s5circle = new GameObject[s2s5famCount];
     float[] s2s5circleAlpha = new float[s2s5famCount];;
     Material[] s2s5circleMat = new Material[s2s5famCount];
     
     GameObject s2s5laser = new GameObject("s2s5laser");
     int s2s5laserAlpha = 0;
     Material s2s5laserMat;
    
     int s2s5phase;
     
     private void stage2spell5(float tpf) {
         openSpell(2,5,12,250,tpf);
         if(!spellFlag[10]) {
             s2s5phase = 0;
             spellFlag[10] = true;
             for(int i = 0; i < s2s5famCount; i++) {
                 s2s5shot[i] = new GameObject("s2s5shot" + i);
                 s2s5shot[i].attachChild(hourai.clone().scale(1.7f).rotate(0,0,FastMath.PI));
                 bulletNode.attachChild(s2s5shot[i]);
                 s2s5shotMat[i] = houraiMat.clone();
                 s2s5shotAlpha[i] = 0;
                 s2s5shot[i].setMaterial(s2s5shotMat[i]);
                 
                 s2s5circle[i] = new GameObject("s2s6circle" + i);
                 s2s5circle[i].attachChild(hourai.clone().scale(1.7f).rotate(0,0,FastMath.PI));
                 bulletNode.attachChild(s2s5circle[i]);
                 s2s5circleAlpha[i] = 0;
                 s2s5circleMat[i] = houraiMat.clone();
                 s2s5circle[i].setMaterial(s2s5circleMat[i]);
                 float length = FastMath.abs(playerMaxDistance - playerMinDistance);
                 if(i % 2 == 1) {
                     s2s5circle[i].setPos(-playerMaxSide,playerMinDistance + i * (length/s2s5famCount),0);
                 } else {
                     s2s5circle[i].setPos(playerMaxSide,playerMinDistance + (i+1) * (length/s2s5famCount),0);
                 }
                 
             }
             s2s5laser = new GameObject("s2s5laser");
             s2s5laser.attachChild(hourai.clone().scale(1.7f).rotate(0,0,FastMath.PI));
             bulletNode.attachChild(s2s5laser);
             s2s5laserMat = houraiMat.clone();
             s2s5laser.setMaterial(s2s5laserMat);
             s2s5laser.setPos(0,-20,0);
             enemy.moveTo(new Vector3f(0,-40,0), 3f);
         }
         //Timers/Flags
         //0: Card
         //1: Phase control
         //2: Rotary shot phase timer
         //3: Rotary shot bullet timer
         //4: Circle doll phase timer
         //5: Circle appearance timer
         //6: Laser shot timer
         //Phases: 
         //0: Rotary
         //1: 
         
         if(spellTimer[1] > 10) {
             spellTimer[1] = 0;
             s2s5phase++;
             if(s2s5phase > 1) {
                 s2s5phase = 0;
             }
         }
         for(int i = 0; i < s2s5famCount; i++) {
             float x = FastMath.cos(spellTimer[2]+i/FastMath.TWO_PI*s2s5famCount)*60;
             float y = FastMath.sin(spellTimer[2]+i/FastMath.TWO_PI*s2s5famCount)*10 - 40;
             s2s5shot[i].setLocalTranslation(x,y,0);
         }
         
         //Fade out inactives
         if(s2s5phase != 0) {
             for(int i = 0; i < s2s5famCount; i++) {
                 if(s2s5shotAlpha[i] > 0) {
                     s2s5shotAlpha[i] -= tpf*0.5f;
                     s2s5shotMat[i].setColor("Color", new ColorRGBA(1,1,1, s2s5shotAlpha[i]));
                     s2s5shot[i].setMaterial(s2s5shotMat[i]);
                 }
             }
         }
         if(s2s5phase != 1) {
             for(int i = 0; i < s2s5famCount; i++) {
                 if(s2s5circleAlpha[i] > 0) {
                     s2s5circleAlpha[i] -= tpf*0.5f;
                     s2s5circleMat[i].setColor("Color", new ColorRGBA(1,1,1, s2s5circleAlpha[i]));
                     s2s5circle[i].setMaterial(s2s5circleMat[i]);
                 }
             }
             s2s5laserAlpha -= tpf*0.5f;
             s2s5laserMat.setColor("Color", new ColorRGBA(1,1,1, s2s5laserAlpha));
             s2s5laser.setMaterial(s2s5laserMat);
             spellTimer[4] = 0;
         }
         
         
         //Phase updates
         if(s2s5phase == 0) {
             for(int i = 0; i < s2s5famCount; i++) {
                 //Make dolls fade in/out according to position
                 if(s2s5shot[i].getY() < -40) {
                     if(s2s5shotAlpha[i] < 0.5) {
                         s2s5shotAlpha[i] += tpf*0.5f;
                         s2s5shotMat[i].setColor("Color", new ColorRGBA(1,1,1, s2s5shotAlpha[i]));
                         s2s5shot[i].setMaterial(s2s5shotMat[i]);
                     }
                 } else {
                     if(s2s5shotAlpha[i] > 0) {
                         s2s5shotAlpha[i] -= tpf*0.2f;
                         s2s5shotMat[i].setColor("Color", new ColorRGBA(1,1,1, s2s5shotAlpha[i]));
                         s2s5shot[i].setMaterial(s2s5shotMat[i]);
                     }
                 }
             }
             //Let dolls spin around for a bit
             if(spellTimer[3] > 0.1 & spellTimer[0] > 3) {
                 for(int i = 0; i < s2s5famCount; i++) {
                     fireSpeedLine(s2s5shot[i].getLocalTranslation(),s2s5shot[i].getLocalTranslation().add(0,1,0),1,s2s5shot[i].getX()*FastMath.rand.nextFloat()/100,10,1,30,FastMath.nextRandomInt(1,5), BULLET.BALLSHOT_B);
                 }
                 spellTimer[3] = 0;
             }
         }
         if(s2s5phase == 1) {
             for(int i = 0; i < s2s5famCount; i++) {
                 //Make dolls fade in/out according to position
                 if(s2s5circle[i].getY() < spellTimer[4]*130-40) {
                     if(s2s5circleAlpha[i] < 0.6) {
                         s2s5circleAlpha[i] += tpf;
                         s2s5circleMat[i].setColor("Color", new ColorRGBA(1,1,1, s2s5circleAlpha[i]));
                         s2s5circle[i].setMaterial(s2s5circleMat[i]);
                     }
                 }
             }
             if(spellTimer[6] > 5 ) {
                 if(!spellFlag[6]) {
                     enemy.moveTo(0,0,0,1f);
                 } else {
                     enemy.moveTo(0,-40,0,1f);
                 }
                 spellFlag[6] = !spellFlag[6];
                 spellTimer[6] = 0;
             }
             if(spellTimer[5] > 0.5 && spellTimer[4] > 1.5) {
                 if(spellFlag[6]) {
                     for(int i = 0; i < 8; i++) {
                         fireSpeedLine(enemy.getPos(), player.getPos(),1, FastMath.nextRandomFloat()-0.5f, 2, 2, 30, 1, BULLET.BALLSHOT_W);
                     }
                 }
                 int shooters = Math.round((spellTimer[4] - 2) % 5)*2;
                 fireSpeedCircle(s2s5circle[shooters].getLocalTranslation(),5,1,spellTimer[0],40,1,20,FastMath.nextRandomInt(1,4),BULLET.BALLSHOT_W);
                 fireSpeedCircle(s2s5circle[shooters+1].getLocalTranslation(),5,1,spellTimer[0],40,1,20,FastMath.nextRandomInt(1,4),BULLET.BALLSHOT_W);
                 fireSpeedCircle(s2s5circle[shooters].getLocalTranslation(),5,1,spellTimer[0]*2,40,1,20,FastMath.nextRandomInt(1,4),BULLET.BALLSHOT_W);
                 fireSpeedCircle(s2s5circle[shooters+1].getLocalTranslation(),5,1,spellTimer[0]*2,40,1,20,FastMath.nextRandomInt(1,4),BULLET.BALLSHOT_W);
                 spellTimer[5] = 0;
             }
         }
         closeSpell(STAGE2_5,60,150,tpf);
     }
     private void createSpellCircle(int stageNum) {
         if(!spellCircleCreated) {
             if(spellCircle != null) {
                 spellCircle.removeFromParent();
                 spellCircle.detachAllChildren();
             }
             spellCircle = new PanelNode("spellCircle");
             spellCircleModel = assetManager.loadModel("Models/game/spellCircle.j3o");
             spellCircleMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
             TextureKey spellCircleMatTextureKey = new TextureKey("Textures/game/spellCircle"+stageNum+".png", false);
             Texture spellCircleMatTex = assetManager.loadTexture(spellCircleMatTextureKey);
             spellCircleMat.setTexture("ColorMap", spellCircleMatTex);
             spellCircleMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
             spellCircleAlpha = 0;
             spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
             spellCircleModel.setMaterial(spellCircleMat);
             spellCircle.attachChild(spellCircleModel);
             spellCircle.setQueueBucket(Bucket.Translucent);
             shotNode.attachChild(spellCircle);
             spellCircle.scale(2);
             spellCircleCreated = true;
         }
     }
     
     int s2s6phase = 0;
     int s2s6famCount = 8;
     float s2s6phase1offset = 0;
     GameObject[] s2s6lance = new GameObject[s2s6famCount];
     Material s2s6lanceMat;
     float lanceAlpha;
     GameObject[] s2s6sword = new GameObject[s2s6famCount];
     Material s2s6swordMat;
     float swordAlpha;
     GameObject[] s2s6laser = new GameObject[s2s6famCount];
     Material s2s6laserMat;
     float laserAlpha;
     
     private void stage2spell6(float tpf) {
         openSpell(2,6, 12,250,tpf);
         if(!spellFlag[11]) {
             spellFlag[11] = true;
             enemy.moveTo(0,0,0, 1);
             for(int i = 0; i < s2s6famCount; i++) {
                 s2s6lance[i] = new GameObject("s2s6lance" + i);
                 s2s6lance[i].attachChild(hourailance.clone().scale(1.7f));
                 bulletNode.attachChild(s2s6lance[i]);
                 s2s6lanceMat = houraiMat.clone();
                 
                 s2s6sword[i] = new GameObject("s2s6sword" + i);
                 s2s6sword[i].attachChild(houraisword.clone().scale(1.7f).rotate(0,0,FastMath.PI));
                 bulletNode.attachChild(s2s6sword[i]);
                 s2s6swordMat = houraiMat.clone();
                 
                 s2s6laser[i] = new GameObject("s2s6laser" + i);
                 s2s6laser[i].attachChild(hourai.clone().scale(1.7f).rotate(0,0,FastMath.PI));
                 s2s6laser[i].move(300,300,0);
                 bulletNode.attachChild(s2s6laser[i]);
                 s2s6laserMat = houraiMat.clone();
                 s2s6laser[i].setMaterial(s2s6laserMat);
             }
         }
         //Timer/Flags:
         //1: Phase (3 phases, lance, swords, lasers
         //2: Phase 1 lance timer 
         //3: Phase 1 bullet timer
         //4: Phase 2 sword timer
         //5: Phase 2 bullet timer
         //6: Phase 3 timer
         //7: Phase 3 bullets
         //s2s6phase = 1;
         
         //Change phases with time
         //Phases:
         // 0 = 1,2
         // 1 = 2,3
         // 2 = 3,1
         if(spellTimer[1] > 6) {
             s2s6phase++;
             if(s2s6phase > 2) {
                 s2s6phase = 0;
             }
             spellTimer[1] = 0;
 
         }
         //Fade out inactive dolls
         if(s2s6phase == 1) {
             lanceAlpha -= tpf;
             if(lanceAlpha < 0) {
                 lanceAlpha = 0;
             }
             s2s6lanceMat.setColor("Color", new ColorRGBA(1,1,1,lanceAlpha));
         }
         if(s2s6phase == 2) {
             swordAlpha -= tpf;
             if(swordAlpha < 0) {
                 swordAlpha = 0;
             }
             s2s6swordMat.setColor("Color", new ColorRGBA(1,1,1,swordAlpha));
         }
         if(s2s6phase == 0) {
             laserAlpha -= tpf;
             if(laserAlpha < 0) {
                 laserAlpha = 0;
             }
             s2s6laserMat.setColor("Color", new ColorRGBA(1,1,1,laserAlpha));
             for(int i = 0; i < s2s6famCount; i++) {
                 bulletNode.getChild("s2s6laser" + i).setMaterial(s2s6laserMat);
             }
         }
         //Move swords regardless of phase.
         float offset = 2*FastMath.PI/s2s6famCount;
         for(int i = 0; i < s2s6famCount; i++) {
             s2s6sword[i].rotate(0,0, 0.05f);
             float t = spellTimer[0];
             float angle = (0.1f*t+i*offset);
             s2s6sword[i].setLocalTranslation((15*FastMath.cos(angle) - 10*FastMath.cos(9*(angle)))*2,(15*FastMath.sin(angle) - 10*FastMath.sin(9*(angle)))*2,0);
         }
             
         //Update dolls based on phase
         if(s2s6phase == 0 || s2s6phase == 2) {
             //Phase 1
             //Fade in from 0, fade out from 1.5
             if(lanceAlpha < 0.5 && spellTimer[2] < 1.5) {
                 lanceAlpha += tpf;
                 s2s6lanceMat.setColor("Color", new ColorRGBA(1,1,1,lanceAlpha));
                 for(int i = 0; i < s2s6famCount; i++) {
                     bulletNode.getChild("s2s6lance" + i).setMaterial(s2s6lanceMat);
                 }
             } else {
                 lanceAlpha -= tpf;
                 s2s6lanceMat.setColor("Color", new ColorRGBA(1,1,1,lanceAlpha));
             }
             
             for(int i = 0; i < s2s6famCount; i++) {
                 s2s6lance[i].setLocalTranslation(FastMath.cos(i*2*FastMath.PI/s2s6famCount + s2s6phase1offset)*70*spellTimer[2],FastMath.sin(i*2*FastMath.PI/s2s6famCount + s2s6phase1offset)*70*spellTimer[2],0);
             }
             if(!spellFlag[2]) {
                 for(int i = 0; i < s2s6famCount; i++) {
                     float angle = (i*2*FastMath.PI/s2s6famCount + s2s6phase1offset) % (FastMath.PI*2);
                     s2s6lance[i].setLocalRotation(new Quaternion());
                     s2s6lance[i].rotate(0,0,  angle  + FastMath.HALF_PI);
                 }
                 spellFlag[2] = true;
             }
             if(spellTimer[2] > 2) {
                 spellFlag[2] = false;
                 spellTimer[2] = 0;
                 s2s6phase1offset += 0.3f;
             }
             if(spellTimer[3] > 0.1) {
                 for(int i = 0; i < s2s6famCount; i++) {
                     fireSpeedShot(s2s6lance[i].getPos(), s2s6lance[i].getPos().add(s2s6lance[i].getPos().cross(Vector3f.UNIT_Z)), 1, 2, 12, 1, BULLET.BALLSHOT_B);
                     fireSpeedShot(s2s6lance[i].getPos(), s2s6lance[i].getPos().add(s2s6lance[i].getPos().mult(-1).cross(Vector3f.UNIT_Z)), 1, 2, 12, 1, BULLET.BALLSHOT_W);
                 }
                 spellTimer[3] = 0;
             }
             
             
         } 
         if(s2s6phase == 1 || s2s6phase == 0) {
             //Phase 2
             //Fade in from 0, fade out from 1.5
             if(swordAlpha < 0.5) {
                 swordAlpha += tpf;
                 s2s6swordMat.setColor("Color", new ColorRGBA(1,1,1,swordAlpha));
                 for(int i = 0; i < s2s6famCount; i++) {
                     bulletNode.getChild("s2s6sword" + i).setMaterial(s2s6swordMat);
                 }
             } else {
                 swordAlpha -= tpf;
                 s2s6swordMat.setColor("Color", new ColorRGBA(1,1,1,swordAlpha));
             }
             
             
             if(!spellFlag[4]) {
                 for(int i = 0; i < s2s6famCount; i++) {
                     s2s6sword[i].rotate(0,0, (i*offset));
                 }
                 spellFlag[4] = true;
             } 
 
 
             if(spellTimer[5] > 0.3f) {
                 for(int i = 0; i < s2s6famCount; i++) {
                     fireSpeedShot(s2s6sword[i].getPos(), s2s6sword[i].getPos().add(s2s6sword[i].getPos().cross(Vector3f.UNIT_Z)), 1, 3, 30, 2, BULLET.BALLSHOT_B);
                     fireSpeedShot(s2s6sword[i].getPos(), s2s6sword[i].getPos().add(s2s6sword[i].getPos().mult(-1).cross(Vector3f.UNIT_Z)), 1, 3, 30, 2, BULLET.BALLSHOT_W);
                 }
                 spellTimer[5] = 0;
             }
             
         }
         if(s2s6phase == 2 || s2s6phase == 1) {
             //Phase 3
             
             //Fade in from 0, fade out from 1.5
             if(laserAlpha < 0.5 && spellTimer[6] < 2) {
                 laserAlpha += tpf*2;
                 
                 s2s6laserMat.setColor("Color", new ColorRGBA(1,1,1,laserAlpha));
                 for(int i = 0; i < s2s6famCount; i++) {
                     bulletNode.getChild("s2s6laser" + i).setMaterial(s2s6laserMat);
                 }
             } else {
                 laserAlpha -= tpf;
                 if(laserAlpha < 0) {
                     laserAlpha = 0;
                 }
                 s2s6laserMat.setColor("Color", new ColorRGBA(1,1,1,laserAlpha));
             }
             
             if(spellTimer[6] > 3) {
                 for(int i = 0; i < s2s6famCount; i++) {
                     s2s6laser[i].setPos(FastMath.rand.nextFloat()*80-40,FastMath.rand.nextFloat()*80-40,0);
                     Vector3f targ = new Vector3f(FastMath.rand.nextFloat()*60-15,FastMath.rand.nextFloat()*30,0);
                     for(int add = 0; add < 12; add++) {
                         fireStraightLine(s2s6laser[i].getPos(), targ, 1, 0, 30 + add*2, 1.5f, BULLET.BEAMSHOT_R);
                     }
                 }
                 spellTimer[6] = 0;
             }
         }
         
         if(houraiAlpha < 0.5) {
             houraiAlpha += tpf/5;
             houraiMat.setColor("Color", new ColorRGBA(1,1,1,houraiAlpha));
             for(int i = 0; i < s2s6famCount; i++) {
                 bulletNode.getChild("s2s6lance" + i).setMaterial(houraiMat);
                 bulletNode.getChild("s2s6sword" + i).setMaterial(houraiMat);
                 bulletNode.getChild("s2s6laser" + i).setMaterial(houraiMat);
             }
         }
 
         closeSpell(STAGE2_6,90,150,tpf);
     }
 
     private void stage2spellL(float tpf) {
         stageClear(tpf,2,STAGE2_L);
     }
     private void stage3(float tpf) {
         stageActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE3_0_1]) {
             System.out.println("Stage 3 Start");
             enemy.detachChild(enemyModel);
             portraitEnemy.getMat().setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait2.png"));
             cutEnemyMat.setTexture("ColorMap", assetManager.loadTexture("Textures/game/enemyPortrait2.png"));
             cutEnemyMat.setColor("Color", new ColorRGBA(1, 1, 1, cutEnemyAlpha));
             setUpEnemy(3);
             enemy.moveTo(new Vector3f(0, 0, 0), 6f);
             gameFlag[STAGE3_0_1] = true;
         }
         if(spellTimer[0] > 3 && !gameFlag[STAGE3_0]) {
             stageActive = false;
             gameFlag[STAGE3_0] = true;
             resetCardVars();
         }
     }
     private void stage3spell1(float tpf){
         openSpell(3,1,5,250,tpf);
 
         if(spellTimer[1] > 0.1) {
             fireSpeedCircle(enemy.getPos(), 12, 1, spellTimer[T_SPELL_MAIN], 22,5,5, 1, BULLET.KNIFE_K);
             fireSpeedCircle(enemy.getPos(), 12, 1, -spellTimer[T_SPELL_MAIN], 22,5,5, 1, BULLET.KNIFE_K);
             spellTimer[1] = 0;
         }
         if(spellTimer[2] > 0.6) {
             fireStraightLine(enemy.getPos(), Vector3f.UNIT_Z, 1, Math.round(spellTimer[T_SPELL_MAIN]*100)/200, 180, 2, BULLET.KNIFE_W);
             fireStraightLine(enemy.getPos(), Vector3f.UNIT_Z, 1, spellTimer[T_SPELL_MAIN]*10/100, 180, 3, BULLET.KNIFE_W);
             fireStraightLine(enemy.getPos(), Vector3f.UNIT_Z, 1, spellTimer[T_SPELL_MAIN]/20, 180, 4, BULLET.KNIFE_W);
         }
         closeSpell(STAGE3_1,60,150,tpf);
     }
 
     private void openSpell(int stage, int spell, int vars, int life, float tpf) {
         bulletsCleared = false;
         spellcardActive = true;
         
         if(!cutEnemyDone && spell >= 5) {
             cutEnemy(1/60f,stage);
         }
         if(!spellcardBannerDone) {
             spellcardBanner(1/60f);
         }
         createSpellCircle(stage);
         
         if(spellCircleAlpha < 0.4f) { 
             spellCircleAlpha += 1/60f;
             spellCircleMat.setColor("Color", new ColorRGBA(1,1,1, spellCircleAlpha));
         }
         
         spellCircle.rotate(0,0,tpf);
         updateSpellTimer(tpf, vars);
         
         if(!spellFlag[0]) {
             timescale = 1;
             enemyAnimChan.setAnim("spell",1);
             enemyAnimChan.setLoopMode(LoopMode.DontLoop);
             bulletNode.setLocalScale(1);
             System.out.println("Stage "+stage+" Spell "+spell);
             this.stage = stage;
             this.spell = spell;
             enemy.life = life;
             spellcardBanner.setLocalTranslation(230,500,0);
             spellcardBannerDone = false;
             spellcardBannerAlpha = 0;
             spellcardBannerEnterDone = false;
             spellcardBannerExitDone = false;
             spellcardBannerTime = 0;
             spellFlag[0] = true;
         }
     }
     private void stage3spell2(float tpf){
         openSpell(3,2,5,250,tpf);
         //2 phases: spawn redirecting knives, then wait for knifes to close in (do nothing)
         //1: spawn for 6s
         //2: spawn timer delay
         //3: wait for redirection
         //Repeat.
         if(spellTimer[1] < 6) {
             if(spellTimer[2] > 0.3 && !spellFlag[1]) {
                 for(int i = 0; i < 4; i++) {
                     fireS3S2Shot(FastMath.nextRandomFloat()*FastMath.TWO_PI, player.getPos(),0.2f,26,10*spellTimer[1] + FastMath.nextRandomFloat()*50, 5-spellTimer[1], 8-spellTimer[1], spellFlag[2], 2,BULLET.KNIFE_K);
                     fireS3S2Shot(FastMath.nextRandomFloat()*FastMath.TWO_PI, player.getPos(),0.2f,26,10*spellTimer[1] + FastMath.nextRandomFloat()*50, 5-spellTimer[1], 8-spellTimer[1], spellFlag[2], 2,BULLET.KNIFE_B);
                     fireS3S2Shot(FastMath.nextRandomFloat()*FastMath.TWO_PI, player.getPos(),0.2f,26,10*spellTimer[1] + FastMath.nextRandomFloat()*50, 5-spellTimer[1], 8-spellTimer[1], spellFlag[2], 2,BULLET.KNIFE_W);
                     fireS3S2Shot(FastMath.nextRandomFloat()*FastMath.TWO_PI, player.getPos(),0.2f,26,10*spellTimer[1] + FastMath.nextRandomFloat()*50, 5-spellTimer[1], 8-spellTimer[1], spellFlag[2], 2,BULLET.KNIFE_K);
                 }
                 spellTimer[2] = 0;
             }
         } else {
             spellTimer[1] = 0;
             //For every shot phase, reverse circle orientation
             if(!spellFlag[1]) {
                 spellFlag[2] = !spellFlag[2];
             }
             spellFlag[1] = !spellFlag[1];
         }
 
 
         closeSpell(STAGE3_2,60,150,tpf);
     }
     float s3s3loopTime;
     ParticleEmitter s3s3dashEmitter;
     Vector3f s3s3tracker;
     private void stage3spell3(float tpf){
         openSpell(3,3,5,250,tpf);
         //0: MAIN
         //1: Loop timer (Dash -> throw)
         //2: Dash timer
         //3: Throw timer
         //4: Throw small timer
         //Init variables
         if(!spellFlag[1]) {
             s3s3loopTime = 3;
             s3s3dashEmitter = new ParticleEmitter("s3s3dashEmitter",ParticleMesh.Type.Triangle,300);
             Material dashMat = new Material(assetManager,"MatDefs/Particle.j3md");
             dashMat.setTexture("m_Texture", assetManager.loadTexture("Textures/game/shockwave.png"));
             s3s3dashEmitter.setMaterial(dashMat);
             s3s3dashEmitter.setQueueBucket(Bucket.Translucent);
             s3s3dashEmitter.setImagesX(1);
             s3s3dashEmitter.setImagesY(1);
             s3s3dashEmitter.setLowLife(0.01f);
             s3s3dashEmitter.setHighLife(0.05f);
             s3s3dashEmitter.setStartSize(0.1f);
             s3s3dashEmitter.setEndSize(5f);
             s3s3dashEmitter.setEndColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 0.1f));   // red
             s3s3dashEmitter.setStartColor(new ColorRGBA(0f, 0f, 1f, 0.2f)); // yellow
             s3s3dashEmitter.setParticlesPerSec(0);
             enemy.attachChild(s3s3dashEmitter);
             
             s3s3tracker = new Vector3f();
             spellFlag[1] = true;
         }
         if(spellTimer[1] < s3s3loopTime) {
             if(!spellFlag[2]) {
                 fireStraightCircle(enemy.getPos(), 3,1, spellTimer[T_SPELL_MAIN], 15, 2, BULLET.KNIFE_W);
                 spellFlag[2] = true;
             }
             if(spellTimer[2] > s3s3loopTime/2) {
                 //Create a weighted average position so that the enemy
                 //dashes 80% of the distance to the player.
                 //Player's location is weighted
                 s3s3dashEmitter.setParticlesPerSec(40);
                 
                 float meanX = (player.getX()*4 + enemy.getX())/5;
                 float meanY = (player.getY()*4 + enemy.getY())/5;
                 enemy.moveTo(meanX, meanY, 0, 20);
                 enemyAnimChan.setAnim("slash");
                 enemyAnimChan.setSpeed(0.2f*spellTimer[0]*0.2f);
                 enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                 s3s3tracker.set(player.getPos());
                 s3s3dashEmitter.setInitialVelocity(new Vector3f(1,0,0));
                 s3s3dashEmitter.setFacingVelocity(true);
                 spellTimer[2] = -20;
             }
             if(spellTimer[3] > s3s3loopTime*3/4) {
                 s3s3dashEmitter.setParticlesPerSec(0);
                 //Fire a flurry
                 for(int i = 0; i < 16/s3s3loopTime; i++) {
                     fireStraightLine(enemy.getPos(), s3s3tracker,1,i*0.05f, 20 / s3s3loopTime, 1, BULLET.KNIFE_B);
                     fireStraightLine(enemy.getPos(), s3s3tracker,1,i*0.05f, 20 / s3s3loopTime, 1, BULLET.KNIFE_B);
                 }
                 spellTimer[3] = -20;
             }
         } else {
             if(s3s3loopTime > 0.5) {
                 s3s3loopTime -= 0.2;
             }
             //Reset every loop
             spellTimer[1] = 0;
             spellFlag[2] = false;
             spellTimer[2] = 0;
             spellTimer[3] = 0;
         }
         
         closeSpell(STAGE3_3,60,150,tpf);
         //Remove emitter
         if(gameFlag[STAGE3_3]) {
             s3s3dashEmitter.removeFromParent();
             s3s3dashEmitter = null;
         }
     }
     
     Vector3f s3s4tracker;
     private void stage3spell4(float tpf){
         openSpell(3,4,9,250,tpf);
         
         //Timers:
         //1: Cycle
         //2: First swipe
         //3: first swipe bullets
         //4: Second swipe
         //5: second swipe 1st set
         //6: second swipe 2nd set
         //7: third swipe
         enemy.moveTo(player.getPos(), 0.05f);
         if(spellTimer[2] < 0.4) {
             if(spellTimer[3] > 0.05) {
                 fireS3S4Line(enemy.getPos(), player.getPos(),2,spellTimer[2]*2,50,3,BULLET.KNIFE_K);
                 spellTimer[3] = 0;
             }
         }
         if(spellTimer[4] < 0.9 && spellTimer[4]> 0.5) {
             if(spellTimer[5] > 0.05) {
                 fireS3S4Line(enemy.getPos(), player.getPos(),2,FastMath.PI-spellTimer[4]*3,50,3,BULLET.KNIFE_K);
                 spellTimer[5] = 0;
             }
         }
         if(spellTimer[4] < 1.5 && spellTimer[4]> 0.9) {
             if(spellTimer[5] > 0.05) {
                 fireS3S4Line(enemy.getPos(), player.getPos(),2,-spellTimer[4]*5,50,3,BULLET.KNIFE_K);
                 spellTimer[5] = 0;
             }
             s3s4tracker = new Vector3f(player.getPos());
         }
         if(spellTimer[6] > 1.7 && spellTimer[6] < 3.6) {
             if(spellTimer[7] > 0.05) {
                 fireStraightLine(enemy.getPos(), s3s4tracker,2,-spellTimer[4]*1.8f,50,2,BULLET.KNIFE_K);
                 fireStraightLine(enemy.getPos(), s3s4tracker,2,spellTimer[4]*1.8f,50,2,BULLET.KNIFE_K);
                 spellTimer[7] = 0;
             }
         }
 
         if(spellTimer[1] > 5) {
             //Reset cycles
             for(int i = 1; i < 9; i++) {
                 spellTimer[i] = 0;
             }
         }
         closeSpell(STAGE3_4,60,150,tpf);
     }
     
     GameObject[] s3s5knife;
     int s3s5knifeCount;
     int s3s5curKnife;
     private void stage3spell5(float tpf){
         openSpell(3,5,10,200,tpf);
         if(!spellFlag[1]) {
             s3s5knifeCount = 8;
             s3s5knife = new GameObject[s3s5knifeCount];
             for(int i = 0; i < s3s5knifeCount; i++) {
                 s3s5knife[i] = new GameObject("s3s5knife"+i);
                 s3s5knife[i].attachChild(knifeK.clone().scale(3));
                 bulletNode.attachChild(s3s5knife[i]);
                 s3s5knife[i].rotate(0,0,FastMath.QUARTER_PI * (i-2));
             }
             //Knife 0 is in NORTH position, knife 4 is SOUTH
             //Order is clockwise
 
             s3s5curKnife = 0;
             //This is the current knife to be dashed to.
             //The knives are dashed to in order.
             
             spellFlag[1] = true;
         }
         //2: 8-circle timer
         //2 Flag: 8-circle variation
         //3: Ring timer
         //4: Ring small timer
         
         if(spellTimer[2] < 3) {
             for(int i = 0; i < s3s5knifeCount; i++) {
                 s3s5knife[i].setLocalTranslation(FastMath.cos(FastMath.QUARTER_PI*i)*spellTimer[2]*16,FastMath.sin(FastMath.QUARTER_PI*i)*spellTimer[2]*16,0);
             }
             spellTimer[3] = 8/s3s5knifeCount;
             spellTimer[4] = 0;
             s3s5curKnife = 0;
         } else if(spellTimer[2] < 7) {
             if(spellTimer[3] > 0.05) {
                 if(s3s5curKnife < 8) {
                     enemy.moveTo(s3s5knife[s3s5curKnife].getLocalTranslation(), 50);
                     if(!spellFlag[2]) {
                         fireSpeedCircle(s3s5knife[s3s5curKnife].getPos(), 32, 1, s3s5curKnife*FastMath.QUARTER_PI, 1, 1, 30, 2, BULLET.KNIFE_W);
                     } else {
                         fireSpeedLine(s3s5knife[s3s5curKnife].getPos(), player.getPos(),8, 0, 1, 1, 50, 2, BULLET.KNIFE_B);
                         fireSpeedLine(s3s5knife[s3s5curKnife].getPos(), player.getPos(),8, 0.3f, 1, 1, 50, 2, BULLET.KNIFE_B);
                         fireSpeedLine(s3s5knife[s3s5curKnife].getPos(), player.getPos(),8, -0.3f, 1, 1, 50, 2, BULLET.KNIFE_B);
                         fireSpeedLine(s3s5knife[s3s5curKnife].getPos(), player.getPos(),8, 0, 1, 1, 40, 2, BULLET.KNIFE_B);
                         fireSpeedLine(s3s5knife[s3s5curKnife].getPos(), player.getPos(),8, 0.4f, 1, 1, 40, 2, BULLET.KNIFE_B);
                         fireSpeedLine(s3s5knife[s3s5curKnife].getPos(), player.getPos(),8, -0.4f, 1, 1, 40, 2, BULLET.KNIFE_B);
                     }
                     s3s5knife[s3s5curKnife].setLocalTranslation(400,0,0);
                     s3s5curKnife++;
                     System.out.println(s3s5curKnife);
                 }
                 spellTimer[3] = 0;
             }
             spellTimer[4] = 0;
             spellTimer[5] = 0;
         }
         if(spellTimer[4] > tpf*2 && spellTimer[4] < 8) {
                 enemy.moveTo(FastMath.cos(spellTimer[4])*25,FastMath.sin(spellTimer[4])*25,0, 3);
             if(spellTimer[5] > 0.05) {
                 fireSpeedLine(enemy.getPos(), Vector3f.ZERO,8, 0, -10, 5, 20, 2, BULLET.KNIFE_B);
                 spellTimer[5] = 0;
             }
         }
         if(spellTimer[4] > 9) {
             enemy.moveTo(Vector3f.ZERO, 3);
         }
         if(spellTimer[6] > 20) {
             
             for(int i = 0; i < 9; i++) {
                 spellTimer[i] = 0;
             }
             spellFlag[2] = !spellFlag[2];
         }
         
         closeSpell(STAGE3_5,60,150,tpf);
     }
     
     private void stage3spell6(float tpf){
         openSpell(3,6,10,250,tpf);
         if(spellTimer[1] > 0.1) {
             fireBendShot(enemy.getPos(), player.getPos(), Vector3f.UNIT_X, 90, 5, BULLET.KNIFE_W);
             fireBendShot(enemy.getPos(), player.getPos(), Vector3f.UNIT_X.mult(-1), 90, 5, BULLET.KNIFE_W);
             spellTimer[1] = 0;
         }
         closeSpell(STAGE3_6, 90, 150, tpf);
     }
     private void stage3spellL(float tpf) {
         stageClear(tpf, 3, STAGE3_L);
     }
     private void stage4(float tpf) {
         stageActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE4_0_1]) {
             System.out.println("Stage 4 Start");
             setUpEnemy(4);
             enemyAnimChan.setAnim("up",1);
             enemy.moveTo(Vector3f.ZERO, 6f);
             gameFlag[STAGE4_0_1] = true;
         }
         
         if(spellTimer[0] > 3 && !gameFlag[STAGE4_0]) {
             stageActive = false;
             gameFlag[STAGE4_0] = true;
             resetCardVars();
         }
     }
     private void stage4spell1(float tpf){
         openSpell(4,1,9,250,tpf);
         if(!spellFlag[1]) {
             enemyAnimChan.setAnim("spell",1);
             enemy.moveTo(Vector3f.ZERO,3);
             spellFlag[1] = true;
         }
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)){
             Vector3f target = new Vector3f();
             if(!spellFlag[2] && spellTimer[2] > 2.5) {
                 enemyAnimChan.setAnim("slash",1);
                 enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                 spellFlag[2] = true;
                 timescale = 0.3f;
             }
             if(enemyAnimChan.getTime() > 1.3f && spellTimer[2] > 2.5) {
                 timescale = 1f;
                 float randX = FastMath.nextRandomInt(0,20);
                 float randY = FastMath.nextRandomInt(140,180);
                 if(enemy.getX() < 0) {
                     randX += enemy.getX();
                 } else {
                     randX -= enemy.getX();
                 }
                 if(spellFlag[5]) {
                     randY = enemy.getY() + randY;
                 } else {
                     randY = enemy.getY() - randY;
                 }
                 spellFlag[5] = !spellFlag[5];
                 if(randX > 50) {
                     randX = 50;
                 } else if(randX < -50) {
                     randX = -50;
                 }
                 if(randY > playerMaxDistance) {
                     randY = playerMaxDistance;
                 } else if(randY < playerMinDistance) {
                     randY = playerMinDistance;
                 }
 
                 target = new Vector3f(randX,randY,0);
                 enemy.moveTo(target,3);
                 enemy.lookAt(target, Vector3f.UNIT_Z);
 
                 spellFlag[2] = false;
                 spellTimer[2] = 0;
 
                 Vector3f midpoint = new Vector3f((enemy.getX()*2 + randX)/3, (enemy.getY()*2 + randY)/3, 0);
                 for(int i = 0; i < 16; i++) {
                     if(spellFlag[3]) {
                         fireStraightLine(midpoint, target, 1, i*0.1f - 0.8f, 49,  2, BULLET.ARROWSHOT_T);
                     } else {
                         fireStraightLine(midpoint, target, 1, i*0.1f - 0.8f, 49,  2, BULLET.ARROWSHOT_P);
                     }
 
                 }
                 spellFlag[3] = !spellFlag[3];
             }
             float dist = enemy.getDestination().distance(enemy.getPos());
             if(dist > 5) {
                 if(spellTimer[3] > 0.03) {
                     for(int i = 0; i < dist/5; i++) {
                         float offset = FastMath.nextRandomFloat()/3;
                         if(spellFlag[5]) offset = -offset;
                         BULLET bulletType = BULLET.ARROWSHOT_T;
                         if(spellFlag[5]) bulletType = BULLET.ARROWSHOT_P;
                         fireSpeedShot(enemy.getPos(),enemy.getPos().add(1,offset,0),dist/20,1,5+Math.abs(offset)*15,1,bulletType);
                         fireSpeedShot(enemy.getPos(),enemy.getPos().add(-1,offset,0),dist/20,1,5+Math.abs(offset)*15,1,bulletType);
                     }
                     spellTimer[3] = 0;
                 }
             }
             /*if(spellTimer[5] > 0.1) {
                 Vector3f pos = new Vector3f(FastMath.nextRandomInt(-80,80),-70+FastMath.nextRandomInt(0,10),0);
                 fireScaleShot(pos, pos.add(Vector3f.UNIT_Y), 25, 3+FastMath.nextRandomInt(0,2), 7, 9, BALLSHOT_R, "");
                 fireStraightShot(pos, Vector3f.ZERO, 22, 1, BALLSHOT_R);
                 spellTimer[5] = 0;
             }*/
         }
         closeSpell(STAGE4_1,60,150,tpf);
         if(gameFlag[STAGE4_1]) {
             timescale = 1;
         }
     }
     private void stage4spell2(float tpf){
         openSpell(4,2,11,250,tpf);
         /*Poisonarms
         if(spellFlag[3]) {
             enemy.moveTo(60,0,0, 0.4f);
         } else {
             enemy.moveTo(-60,0,0,0.4f);
         }
         if(enemy.getPos().x > 37) {
             spellFlag[3] = false;
         } else if(enemy.getPos().x < -37) {
             spellFlag[3] = true;
         }
         if(spellTimer[1] > 0.5) {
             fireStraightCircle(enemy.getPos(), 32, 1, 0, 22, 1, KNIFE_K);
             
             for(int i = 0; i < 8; i++) {
                 fireStraightLine(enemy.getPos(), enemy.getPos().add(0,1,0),1,i*0.1f,30,2,BALLSHOT_W);
                 fireStraightLine(enemy.getPos(), enemy.getPos().add(0,1,0),1,-i*0.1f,30,2,BALLSHOT_W);
             }
             spellTimer[1] = 0;
         }
         if(spellTimer[2] > 0.3) {
             fireStraightShot(enemy.getPos(),player.getPos(),40,1,ARROWSHOT_G);
             spellTimer[2] = 0;
         }*/
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)){
             if(spellTimer[5] > 5 && !spellFlag[5]) {
                 spellFlag[5] = true;
                 spellTimer[5] = 0;
             }
             if(spellTimer[7] > 6 && !spellFlag[7]) {
                 spellFlag[7] = true;
             }
             enemy.moveTo(0,0,0,3);
 
             if(spellTimer[6] > 0.05 && enemy.getPos().length() < 5) {
                 fireCurveCircle1(enemy.getPos(), 8, 1, spellTimer[0]*0.1f, true, 1f, 1, 2+FastMath.nextRandomFloat()*2-1, 30, 1, BULLET.BALLSHOT_B);
                 fireCurveCircle1(enemy.getPos(), 8, 1, spellTimer[0]*0.1f, false, 1f, 1, 2+FastMath.nextRandomFloat()*2-1, 30, 1, BULLET.BALLSHOT_P);
                 spellTimer[6] = 0;
             }
             if(spellFlag[7]) {
                 if(spellTimer[8] > 2) {
                     fireCurveCircle1(enemy.getPos(), 32, 1, spellTimer[0], true, 1, 2,3, 25, 2, BULLET.BALLSHOT_R);
                     fireCurveCircle1(enemy.getPos(), 32, 1, spellTimer[0], false, 1, 2,3, 25, 2, BULLET.BALLSHOT_B);
                     spellTimer[8] = 0;
                 }
                 if(spellTimer[9] > 1) {
                     fireCurveCircle1(enemy.getPos(), FastMath.nextRandomInt(5,9), 1, FastMath.nextRandomFloat(), true, 2, 4,6,25, FastMath.nextRandomInt(4,6), BULLET.BALLSHOT_R);
                     fireCurveCircle1(enemy.getPos(), FastMath.nextRandomInt(5,7), 1, FastMath.nextRandomFloat(), true, 2, 4,6, 25, FastMath.nextRandomInt(1,4), BULLET.BALLSHOT_R);
                     spellTimer[9] = 0;
                 }
             }
         }
         closeSpell(STAGE4_2,60,150,tpf);
     }
     Vector3f targ;
     private void stage4spell3(float tpf){
         openSpell(4,3,9,250,tpf);
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)){
             if(spellTimer[7] > 0.3) {
                 Vector3f pos = new Vector3f(FastMath.nextRandomInt(-80,80),110,0);
                 fireStraightShot(pos, pos.subtract(Vector3f.UNIT_Y), 10, 1, BULLET.ARROWSHOT_P);
                 Vector3f pos2 = new Vector3f(FastMath.nextRandomInt(-80,80),110,0);
                 fireStraightShot(pos2, pos2.subtract(Vector3f.UNIT_Y), 10, 1, BULLET.ARROWSHOT_W);
                 spellTimer[7] = 0;
             }
             if(targ == null) {
                 targ = new Vector3f(0,100,0);
             }
             if(spellFlag[2]) {
                 if(spellTimer[2] < 3) {
                     if(!spellFlag[4]) {
                         enemy.moveTo(enemy.getX(),playerMinDistance, 0, 0.5f);
                         spellFlag[4] = true;
                     }
                     if(spellTimer[3] > 0.3) {
                         for(int i = 0; i < 10;i++) {
                             int speed = 0;
                             if(i >= 5) {
                                 speed = 5 - (i % 5) + 30;
                             } else {
                                 speed = i % 5 + 30;
                             }
                             speed *= 0.7f;
                             fireCurveShot1(enemy.getPos(), targ.add(targ.cross(Vector3f.UNIT_Z)), true, (0.3f+i*0.05f), 0,4, speed, 2, BULLET.BALLSHOT_P);
                             fireCurveShot1(enemy.getPos(), targ.add(targ.cross(Vector3f.UNIT_Z).mult(-1f)), false, (0.3f+i*0.05f), 0,4, speed, 2, BULLET.BALLSHOT_P);
                         }
                         spellTimer[3] = 0;
                     }
                 } else {
                     spellFlag[2] = false;
                     spellTimer[2] = 0;
                     enemy.moveTo(player.getX(),0,0,1f);
                 }
             }
             if(!spellFlag[2] && spellTimer[2] > 1) {
                 spellFlag[2] = true;
                 spellTimer[2] = 0;
                 targ.set(player.getPos().subtract(enemy.getPos()).normalize().mult(50));
             }
         }
         closeSpell(STAGE4_3,60,150,tpf);
     }
     //GameObject s4s4familiar;
     Vector3f s4s4familiar;
     ParticleEmitter s4s4dashEmitter;
     private void stage4spell4(float tpf){
         openSpell(4,4,11,250,tpf);
         //Familiar shoots balls from top center
         //enemy slashes back and forth, cutting bullets
         //bullets in enemy path get split into several smaller bullets
         int INIT = 1;
         int FAM_BULLET_TIMER = 2;
         int SLASH = 3;
         int SIDE = 4;
         int ANIM = 5;
         
         if(!spellFlag[INIT]) {
             if(s4s4familiar == null) {
                 s4s4familiar = new Vector3f(0,playerMinDistance,0);
             }
             gameFlag[STAGE4_4] = false;
             //s4s4familiar = new GameObject("s4s4familiar");
             //s4s4familiar.attachChild(ballShotW.clone().scale(2));
             //bulletNode.attachChild(s4s4familiar);
             //s4s4familiar.setPos(0,playerMinDistance,0);
             spellFlag[INIT] = true;
             enemy.moveTo(playerMaxSide, 0, 0, 4);
             
             s4s4dashEmitter = new ParticleEmitter("s4s4dashEmitter",ParticleMesh.Type.Triangle,600);
             Material dashMat = new Material(assetManager,"MatDefs/Particle.j3md");
             dashMat.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/dashH.png"));
             s4s4dashEmitter.setMaterial(dashMat);
             s4s4dashEmitter.setQueueBucket(Bucket.Translucent);
             s4s4dashEmitter.setImagesX(1);
             s4s4dashEmitter.setImagesY(1);
             s4s4dashEmitter.setLowLife(0.1f);
             s4s4dashEmitter.setHighLife(0.3f);
             s4s4dashEmitter.setStartSize(1f);
             s4s4dashEmitter.setEndSize(4f);
             s4s4dashEmitter.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.1f));   // red
             s4s4dashEmitter.setStartColor(new ColorRGBA(0f, 0f, 1f, 1f)); // yellow
             s4s4dashEmitter.setParticlesPerSec(0);
             enemy.attachChild(s4s4dashEmitter);
         }
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)){
             if(spellTimer[FAM_BULLET_TIMER] > 0.08) {
                 fireStraightLine(s4s4familiar, Vector3f.ZERO, 1,FastMath.nextRandomFloat()-0.5f,15,  FastMath.nextRandomInt(6,8), BULLET.BALLSHOT_R, "s4s4ball");
                 spellTimer[FAM_BULLET_TIMER] = 0;
             }
 
             //3 seconds between slashes
             if(!spellFlag[SLASH] && spellTimer[SLASH] > 2.7f && enemyAnimChan.getTime() > 1.3f && spellFlag[ANIM]) {
                 spellFlag[SLASH] = true;
                 spellTimer[SLASH] = 0;     
                 spellFlag[ANIM] = false;
                 enemy.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
                 enemyAnimChan.setSpeed(1f);
             }
             if(spellTimer[SLASH] > 0.5f) {
                 s4s4dashEmitter.setParticlesPerSec(0);
             }
             if(spellTimer[SLASH] > 2.7f) {
                 if(!spellFlag[ANIM]) {
                 enemyAnimChan.setAnim("slash");
                 enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                 spellFlag[ANIM] = true;
                 timescale = 0.1f;
                 //enemyAnimChan.setSpeed(0.1f);
                 }
             }
             //Slash
             if(spellFlag[SLASH]) {
                 s4s4dashEmitter.setParticlesPerSec(100);
                 timescale = 1;
                 if(spellFlag[SIDE]) {
                     enemy.moveTo(playerMaxSide,0,0,6);
                 } else {
                     enemy.moveTo(-playerMaxSide,0,0,6);
                 }
                 Iterator bulletIterator = bulletNode.getChildren().iterator();
                 while(bulletIterator.hasNext()) {
                     GameObject curBullet = (GameObject) bulletIterator.next();
                     if((curBullet.getLocalTranslation().y < 15 && curBullet.getLocalTranslation().y > -15) && curBullet.getName().contains("s4s4ball")) {
                         fireStraightCircle(curBullet.getPos(), 16, 1, (float)Math.random(), 21, FastMath.nextRandomInt(1,3), BULLET.BALLSHOT_P);
                         curBullet.detachAllChildren();
                         curBullet.removeFromParent();
                     }
                 }
                 spellFlag[SIDE] = !spellFlag[SIDE];
                 spellFlag[SLASH] = false;
             }
         }
         closeSpell(STAGE4_4,60,150,tpf);
         if((spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             timescale = 1;
             s4s4dashEmitter.removeFromParent();
             s4s4dashEmitter.setParticlesPerSec(0);
         }
     }
     GameObject s4s5familiar;
     ParticleEmitter s4s5emitter;
     Vector3f s4s4aim = new Vector3f();
     private void stage4spell5(float tpf){
         openSpell(4,5,13,250,tpf);
         int SIDE = 3;
         int SIDESHOT = 4;
         int FISSURE = 5;
         int FISSURESHOT = 6;
         int FISSURERE = 8;
         int INIT = 7;
         int ANIM = 9;
         if(!spellFlag[INIT]) {
             s4s4aim = new Vector3f(0,0,0);
             s4s5familiar = new GameObject("s4s5familiar");
             bulletNode.attachChild(s4s5familiar);
             s4s5familiar.attachChild(ballShotR.clone().scale(4));
             s4s5emitter = new ParticleEmitter("s4s5emitter", ParticleMesh.Type.Triangle, 50);
 
             Material mat_red = new Material(assetManager, "MatDefs/Particle.j3md");
             //mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/spark.png"));
             mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/dash.png"));
             mat_red.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
             //s4s5emitter.setQueueBucket(Bucket.Translucent);
             //Leaf explosion
             s4s5emitter.setMaterial(mat_red);
             s4s5emitter.setNumParticles(100);
             s4s5emitter.setImagesX(1); s4s5emitter.setImagesY(1); // 2x2 texture animation
             s4s5emitter.setEndColor(new ColorRGBA(0f, 0f, 1f, 1f));   // red
             s4s5emitter.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f)); // yellow
             //s4s5emitter.getParticleInfluencer().setInitialVelocity(new Vector3f(60,0,0));
             //s4s5emitter.setGravity(3,0,0);
             //s4s5emitter.setRotateSpeed(10);
             s4s5emitter.setParticlesPerSec(200);
             //s4s5emitter.getParticleInfluencer().setVelocityVariation(1);
             s4s5emitter.setStartSize(3f);
             s4s5emitter.setEndSize(2f);
             s4s5emitter.setLowLife(0.4f);
             s4s5emitter.setHighLife(1.2f);
             //s4s5emitter.setVelocityVariation(0f);
             s4s5emitter.move(0,0,0);
             enemy.moveTo(Vector3f.ZERO, 3);
             s4s5familiar.attachChild(s4s5emitter);
             spellFlag[INIT] = true;
         }
         if(spellTimer[SIDE] > 2) {
             if(spellTimer[SIDESHOT] > 0.03f) {
                 Vector3f shot = new Vector3f(-100,FastMath.nextRandomInt(-100,100),0);
                 Vector3f shot2 = new Vector3f(100,FastMath.nextRandomInt(-100,100),0);
                 fireStraightShot(shot, shot.add(1,0.5f,0), 10, 1, BULLET.ARROWSHOT_T);
                 fireStraightShot(shot2, shot2.add(-1,0.5f,0), 10, 1, BULLET.ARROWSHOT_P);
                 spellTimer[SIDESHOT] = 0;
             }
         }
         s4s5familiar.update(tpf);
         if(spellTimer[FISSURE] > 2.8 && !spellFlag[ANIM]) {
             enemyAnimChan.setAnim("slash");
             enemyAnimChan.setSpeed(0.5f);
             enemyAnimChan.setLoopMode(LoopMode.DontLoop);
             spellFlag[ANIM] = true;
         }
         enemy.lookAt(s4s4aim, Vector3f.UNIT_Z);
         if(spellTimer[FISSURE] > 4) {
             s4s4aim = new Vector3f(player.getPos().normalize().mult(200));
             s4s5familiar.moveTo(s4s4aim,2);
             
             spellFlag[FISSURESHOT] = true;
             spellTimer[FISSURE] = 0;
             spellFlag[ANIM] = false;
         }
         if(spellTimer[FISSURE] < 2) {
             spellTimer[FISSURERE] = 0;
         }
         if(spellTimer[FISSURERE] > 1) {
             spellTimer[FISSURERE] = 0;
         }
         if(spellTimer[FISSURE] > 2) {
             s4s5familiar.setLocalTranslation(enemy.getPos());
             spellFlag[FISSURESHOT] = false;
         }
         if(spellTimer[FISSURESHOT] > 0.001f && spellFlag[FISSURESHOT]) {
             for(int i = 0; i < 9; i++) {
                 try {
                     int orientation = 1;
                     if(i%2 == 1) {
                         orientation = -1;
                     }
                     float offset = (FastMath.nextRandomFloat()-0.5f)*0.8f;
                     fireSpeedLine(s4s5familiar.getPos().mult(FastMath.nextRandomFloat()), s4s4aim.cross(Vector3f.UNIT_Z).mult(orientation), 1,offset,0, 1-spellTimer[FISSURERE]+FastMath.nextRandomFloat(), 30, FastMath.nextRandomFloat()+0.5f, BULLET.BALLSHOT_B);
                     fireSpeedLine(s4s5familiar.getPos().mult(FastMath.nextRandomFloat()), s4s4aim.cross(Vector3f.UNIT_Z).mult(orientation),1,offset, 0, 1-spellTimer[FISSURERE]+FastMath.nextRandomFloat(), 30, FastMath.nextRandomFloat()+0.5f, BULLET.BALLSHOT_P);
                 } catch(Exception ex) {//Aim not initialized
                     
                 }
             }
             spellTimer[FISSURESHOT] = 0;
         }
         closeSpell(STAGE4_5,60,150,tpf);
     }
     GameObject[] s4s6familiar;
     GameObject[] s4s6slash;
     int s4s6count = 6;
     private void stage4spell6(float tpf){
         openSpell(4,6,20,250,tpf);
         int INIT = 1;
         int PHASE1 = 2;
         int DASH1 = 4;
         int DASH2 = 5;
         int DASH3 = 6;
         int DASH4 = 7;
         int PHASE2 = 3;
         int ANIM1 = 8;
         int PHASE1SHOT = 9;
         if(!spellFlag[INIT]) {
             s4s6familiar = new GameObject[s4s6count];
             s4s6slash = new GameObject[2];
             for(int i = 0; i < s4s6count; i++) {
                 s4s6familiar[i] = new GameObject("s4s6"+i);
                 bulletNode.attachChild(s4s6familiar[i]);
                 //s4s6familiar[i].attachChild(ballShotR.clone());
             }
             s4s6slash[0] = new GameObject("s4s6slash0");
             s4s6slash[1] = new GameObject("s4s6slash1");
             bulletNode.attachChild(s4s6slash[0]);
             //s4s6slash[0].attachChild(ballShotR.clone().scale(9));
             bulletNode.attachChild(s4s6slash[1]);
             //s4s6slash[1].attachChild(ballShotR.clone().scale(9));
             
             spellFlag[INIT] = true;
             enemy.moveTo(playerMaxSide, playerMinDistance, 0, 3);
         }
         if(!spellFlag[PHASE1]) {
             Vector3f targ = new Vector3f();
             enemy.lookAt(targ, Vector3f.UNIT_Z);
             if(spellTimer[PHASE1] > 2) {
                 if(spellTimer[PHASE1SHOT] > 0.01) {
                     fireSpeedCircle(enemy.getPos(), 6, 1, spellTimer[PHASE1],0, 30-(FastMath.nextRandomInt(0,28)), 12, 1, BULLET.BALLSHOT_W);
                     fireSpeedCircle(enemy.getPos(), 6, 1, spellTimer[PHASE1],0, 30-(FastMath.nextRandomInt(0,28)), 12, 1, BULLET.BALLSHOT_W);
                     spellTimer[PHASE1SHOT] = 0;
                 }
             }
             if(spellTimer[PHASE1] > 1.5 && !spellFlag[DASH1]) {
                 if(!spellFlag[ANIM1]) {
                     enemyAnimChan.setAnim("slash");
                     enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                     spellFlag[ANIM1] = true;
                 }
                 if(spellTimer[PHASE1] > 2) {
                     targ = new Vector3f(-playerMaxSide,playerMinDistance,0);
                     enemy.moveTo(targ, 4);
                     spellFlag[DASH1] = true;
                     spellFlag[ANIM1] = false;
                 }
             }
             if(spellTimer[PHASE1] > 2.5 && !spellFlag[DASH2]) {
                 if(!spellFlag[ANIM1]) {
                     enemyAnimChan.setAnim("slash");
                     enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                     spellFlag[ANIM1] = true;
                 }
                 if(spellTimer[PHASE1] > 3) {
                     targ = new Vector3f(-playerMaxSide,playerMaxDistance,0);
                     enemy.moveTo(targ, 4);
                     spellFlag[DASH2] = true;
                     spellFlag[ANIM1] = false;
                 }
             }
             if(spellTimer[PHASE1] > 3.5 && !spellFlag[DASH3]) {
                 if(!spellFlag[ANIM1]) {
                     enemyAnimChan.setAnim("slash");
                     enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                     spellFlag[ANIM1] = true;
                 }
                 if(spellTimer[PHASE1] > 4) {
                     targ = new Vector3f(playerMaxSide,playerMaxDistance,0);
                     enemy.moveTo(targ, 4);
                     spellFlag[DASH3] = true;
                     spellFlag[ANIM1] = false;
                 }
             }
             if(spellTimer[PHASE1] > 4.5 && !spellFlag[DASH4]) {
                 if(!spellFlag[ANIM1]) {
                     enemyAnimChan.setAnim("slash");
                     enemyAnimChan.setLoopMode(LoopMode.DontLoop);
                     spellFlag[ANIM1] = true;
                 }
                 if(spellTimer[PHASE1] > 5) {
                     targ = new Vector3f(playerMaxSide,playerMinDistance,0);
                     enemy.moveTo(targ, 4);
                     spellFlag[DASH4] = true;
                     spellFlag[ANIM1] = false;
                 }
             }
             if(spellTimer[PHASE1] > 6) {
                 enemy.moveTo(0,0,0, 2);
                 spellFlag[PHASE1] = true;
                 spellTimer[PHASE2] = 0;
             }
         }
         //Phase 2
         int PHASE2SHOT = 10;
         int PHASE2SLASH1 = 11;
         
         int PHASE2SLASH2 = 12;
         int SLASH2SHOT1 = 13;
         int SLASH2SHOT2 = 14;
         if(spellFlag[PHASE1] && spellTimer[PHASE2] > 1) {
             if(spellTimer[PHASE2] < 2) {
                 for(int i = 0; i < s4s6count; i++) {
                     s4s6familiar[i].setPos(enemy.getPos());
                 }
             }
             if(spellTimer[PHASE2] > 2 && spellTimer[PHASE2] < 4) {
                 for(int i = 0; i < s4s6count; i++) {
                     s4s6familiar[i].update(tpf);
                     
                     if(!spellFlag[PHASE2SLASH1]){
                         for(int ii = 0; ii < s4s6count; ii++) {
                             Vector3f dest = new Vector3f(FastMath.cos(ii*FastMath.TWO_PI/s4s6count), FastMath.sin(ii*FastMath.TWO_PI/s4s6count),0).mult(200);
                             s4s6familiar[ii].moveTo(dest, 1f);
                             
                         }
                         spellFlag[PHASE2SLASH1] = true;
                     }
                 }
                 if(spellTimer[PHASE2SHOT] > 0.02) {
                     for(int i = 0; i < s4s6count; i++) {
                         Vector3f dest = new Vector3f(FastMath.cos(i*FastMath.TWO_PI/s4s6count)+(FastMath.nextRandomFloat()-0.5f)*1.1f, FastMath.sin(i*FastMath.TWO_PI/s4s6count)+(FastMath.nextRandomFloat()-0.5f)*1.1f,0).mult(200);
                         for(int x = 0; x < 2; x++) {
                             fireSpeedShot(s4s6familiar[i].getPos().mult(FastMath.nextRandomFloat()*0.5f + 0.5f), dest, 0, FastMath.nextRandomFloat()*4+1, 20, FastMath.nextRandomFloat()*1.5f+spellTimer[PHASE2]/2, BULLET.BALLSHOT_B);
                             fireSpeedShot(s4s6familiar[i].getPos().mult(FastMath.nextRandomFloat()*0.5f + 0.5f), dest, 0, FastMath.nextRandomFloat()*4+1, 20, FastMath.nextRandomFloat()*1.5f+spellTimer[PHASE2]/2, BULLET.BALLSHOT_W);
                             fireSpeedShot(s4s6familiar[i].getPos().mult(FastMath.nextRandomFloat()*0.5f + 0.5f), dest, 0, FastMath.nextRandomFloat()*4+1, 20, FastMath.nextRandomFloat()*1.5f+spellTimer[PHASE2]/2, BULLET.BALLSHOT_P);
                         }
                     }
                     spellTimer[PHASE2SHOT] = 0;
                 }
             }
             //Aim the 2 slashes at 3 seconds in
             if(spellTimer[PHASE2] > 3) {
                 
             }
             if(spellTimer[PHASE2] > 4) {
                 s4s6slash[0].setPos(120,player.getPos().y - 20 - FastMath.nextRandomInt(0,50),0);
                 s4s6slash[1].setPos(-160,player.getPos().y - 20 - FastMath.nextRandomInt(0,50),0);
                 spellTimer[PHASE2] = 0;
                 spellTimer[PHASE2SLASH2] = 0;
                 spellFlag[PHASE2SLASH2] = true;
                 s4s6slash[0].moveInto(player.getPos(), 2);
                 s4s6slash[1].moveInto(player.getPos(), 1.6f);
             }
             
             //If slash 2 is active, start shooting bullets from slash familiars
             if(spellFlag[PHASE2SLASH2]) {
                 s4s6slash[0].update(tpf);
                 s4s6slash[1].update(tpf);
                 if(spellTimer[SLASH2SHOT1] > 0.01) {
                     for(int i = 0; i < 7; i ++) {
                         fireScaleShot(s4s6slash[0].getPos().subtract(s4s6slash[0].getDirection().mult(FastMath.nextRandomFloat())), Vector3f.ZERO, 0, 1, 2, FastMath.nextRandomInt(2,4), BULLET.BALLSHOT_P,"");
                         fireScaleShot(s4s6slash[1].getPos().subtract(s4s6slash[1].getDirection().mult(FastMath.nextRandomFloat())), Vector3f.ZERO, 0, 1, 2, FastMath.nextRandomInt(2,4), BULLET.BALLSHOT_W,"");
                     }
                     spellTimer[SLASH2SHOT1] = 0;
                 }
                 if(spellTimer[SLASH2SHOT1] > 0.01) {
                     for(int i = 0; i < 6; i ++) {
                         fireScaleShot(s4s6slash[0].getPos().subtract(s4s6slash[0].getDirection().mult(FastMath.nextRandomFloat())), Vector3f.ZERO, 0, 1, 2, FastMath.nextRandomInt(2,4), BULLET.BALLSHOT_P,"");
                         fireScaleShot(s4s6slash[1].getPos().subtract(s4s6slash[1].getDirection().mult(FastMath.nextRandomFloat())), Vector3f.ZERO, 0, 1, 2, FastMath.nextRandomInt(2,4), BULLET.BALLSHOT_W,"");
                     }
                     spellTimer[SLASH2SHOT2] = 0;
                 }
             }
             if(spellTimer[PHASE2SLASH2] > 5) {
                 spellFlag[PHASE2SLASH2] = false;
             }                
         }
         closeSpell(STAGE4_6,60,150,tpf);
     }
     private void stage4spellL(float tpf) {
         stageClear(tpf, 4, STAGE4_L);
     }
     private void stage5(float tpf) {
         stageActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE5_0_1]) {
             System.out.println("Stage 5 Start");
             setUpEnemy(5);
             enemyAnimChan.setAnim("up",1);
             enemy.moveTo(Vector3f.ZERO, 6f);
             gameFlag[STAGE5_0_1] = true;
         }
         
         if(spellTimer[0] > 3 && !gameFlag[STAGE5_0]) {
             stageActive = false;
             gameFlag[STAGE5_0] = true;
             resetCardVars();
         }
     }
     private void stage5spell1(float tpf) {
                 openSpell(5,1,20,250,tpf);
         //Familiar shoots balls from top center
         //enemy slashes back and forth, cutting bullets
         //bullets in enemy path get split into several smaller bullets
         int INIT = 1;
         int FAM_BULLET_TIMER = 2;
 
         if(!spellFlag[INIT]) {
             gameFlag[STAGE5_1] = false;
 
             spellFlag[INIT] = true;
             enemy.moveTo(playerMaxSide, 0, 0, 4);
             spellTimer[3] = 1.5f;
         }
         if(!(spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)){
             if(spellTimer[FAM_BULLET_TIMER] > 3) {
                 fireStraightLine(enemy.getPos(), Vector3f.ZERO, 1,FastMath.nextRandomFloat()-0.5f,12,  FastMath.nextRandomInt(1,2), BULLET.ARROWSHOT_P);
                 fireSpeedCircle(Vector3f.ZERO.add(-10,10,0), 12, 12, spellTimer[3], 0.5f, 2, 19, 1, BULLET.BALLSHOT_P);
                 fireSpeedCircle(Vector3f.ZERO.add(10,10,0), 12, 12, spellTimer[3], 0.5f, 2, 19, 1, BULLET.BALLSHOT_P);
                 spellTimer[FAM_BULLET_TIMER] = 0;
             }
             if(spellTimer[3] > 3) {
                 fireSpeedCircle(Vector3f.ZERO.add(30,0,0), 12, 12, 0, 0.5f, 2, 19, 1, BULLET.BALLSHOT_P);
                 fireSpeedCircle(Vector3f.ZERO.add(-30,0,0), 12, 12, 0, 0.5f, 2, 19, 1, BULLET.BALLSHOT_P);
                 spellTimer[3] = 0;
             }
         }
         closeSpell(STAGE5_1,60,150,tpf);
         if((spellTimer[T_SPELL_MAIN] > 60 || enemy.life < 150)) {
             timescale = 1;
         }
     }
     private void stage5spell2(float tpf) {
     }
     private void stage5spell3(float tpf) {
     }
     private void stage5spell4(float tpf) {
     }
     private void stage5spell5(float tpf) {
     }
     private void stage5spell6(float tpf) {
     }
     private void stage5spellL(float tpf) {
     }
     private void stage6(float tpf) {
         stageActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE6_0_1]) {
             System.out.println("Stage 6 Start");
             setUpEnemy(6);
             enemyAnimChan.setAnim("up",1);
             enemy.moveTo(Vector3f.ZERO, 6f);
             gameFlag[STAGE6_0_1] = true;
         }
         
         if(spellTimer[0] > 3 && !gameFlag[STAGE6_0]) {
             stageActive = false;
             gameFlag[STAGE6_0] = true;
             resetCardVars();
         }
     }
     private void stage6spell1(float tpf) {
     }
     private void stage6spell2(float tpf) {
     }
     private void stage6spell3(float tpf) {
     }
     private void stage6spell4(float tpf) {
     }
     private void stage6spell5(float tpf) {
     }
     private void stage6spell6(float tpf) {
     }
     private void stage6spellL(float tpf) {
     }
     private void stage7(float tpf) {
         spellcardActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE7_0_1]) {
             System.out.println("Stage 7 Start");
             setUpEnemy(7);
             enemyAnimChan.setAnim("up",1);
             enemy.moveTo(Vector3f.ZERO, 6f);
             gameFlag[STAGE7_0_1] = true;
         }
         
         if(spellTimer[0] > 3 && !gameFlag[STAGE7_0]) {
             spellcardActive = false;
             gameFlag[STAGE7_0] = true;
             resetCardVars();
         }
     }
     private void stage7spell1(float tpf) {
     }
     private void stage7spell2(float tpf) {
     }
     private void stage7spell3(float tpf) {
     }
     private void stage7spell4(float tpf) {
     }
     private void stage7spell5(float tpf) {
     }
     private void stage7spell6(float tpf) {
     }
     private void stage7spellL(float tpf) {
     }
     private void stage8(float tpf) {
         stageActive = true;
         updateSpellTimer(tpf, 2);
         if(!gameFlag[STAGE8_0_1]) {
             System.out.println("Stage 8 Start");
             setUpEnemy(8);
             enemyAnimChan.setAnim("up",1);
             enemy.moveTo(Vector3f.ZERO, 6f);
             gameFlag[STAGE8_0_1] = true;
         }
         
         if(spellTimer[0] > 3 && !gameFlag[STAGE8_0]) {
             stageActive = false;
             gameFlag[STAGE8_0] = true;
             resetCardVars();
         }
     }
     private void stage8spell1(float tpf) {
     }
     private void stage8spell2(float tpf) {
     }
     private void stage8spell3(float tpf) {
     }
     private void stage8spell4(float tpf) {
     }
     private void stage8spell5(float tpf) {
     }
     private void stage8spell6(float tpf) {
     }
     private void stage8spellL(float tpf) {
     }
     private void enemyDeathSequence() {
         enemyDeathEmitter = new ParticleEmitter("enemyDeathEmitter", ParticleMesh.Type.Triangle, 50);
 
         Material mat_red = new Material(assetManager, "MatDefs/Particle.j3md");
         //mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/spark.png"));
         mat_red.setTexture("m_Texture", assetManager.loadTexture("Textures/game/particle/leaf.png"));
         mat_red.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         enemyDeathEmitter.setQueueBucket(Bucket.Translucent);
         //Leaf explosion
         enemyDeathEmitter.setMaterial(mat_red);
         enemyDeathEmitter.setNumParticles(100);
         enemyDeathEmitter.setImagesX(2); enemyDeathEmitter.setImagesY(1); // 2x2 texture animation
         enemyDeathEmitter.setEndColor(new ColorRGBA(1f, 1f, 0.1f, 0.9f));   // red
         enemyDeathEmitter.setStartColor(new ColorRGBA(0.7f, 0f, 0f, 0.2f)); // yellow
         enemyDeathEmitter.getParticleInfluencer().setInitialVelocity(new Vector3f(60,0,0));
         //enemyDeathEmitter.setGravity(3,0,0);
         enemyDeathEmitter.setRotateSpeed(10);
         enemyDeathEmitter.setParticlesPerSec(200);
         enemyDeathEmitter.getParticleInfluencer().setVelocityVariation(1);
         enemyDeathEmitter.setStartSize(12.3f);
         enemyDeathEmitter.setEndSize(0.2f);
         enemyDeathEmitter.setLowLife(0.4f);
         enemyDeathEmitter.setHighLife(1.2f);
         //enemyDeathEmitter.setVelocityVariation(0f);
         enemyDeathEmitter.move(0,0,0);
 
         enemy.attachChild(enemyDeathEmitter);
     }
 
     boolean gameFaded = false;
     boolean gameUnfaded = true;
     private void fadeGame(float tpf, boolean menu) {
         if(screenFadeOverlayAlpha < 0.9 && !gameFaded) {
             screenFadeOverlayAlpha += tpf*3;
             screenFadeOverlayMat.setColor("Color", new ColorRGBA(0,0,0,screenFadeOverlayAlpha));
             if(menu)fadePauseMenuIn(tpf);
         }
         if(screenFadeOverlayAlpha >= 0.9) {
             screenFadeOverlayAlpha = 0.9f;
             gameFaded = true;
             gameUnfaded = false;
         }
     }
     private void unfadeGame(float tpf, boolean menu) {
         if(screenFadeOverlayAlpha > 0 && !gameUnfaded) {
             screenFadeOverlayAlpha -= tpf*3;
             screenFadeOverlayMat.setColor("Color", new ColorRGBA(0,0,0,screenFadeOverlayAlpha));
             if(menu)fadePauseMenuOut(tpf);
         }
         if(screenFadeOverlayAlpha <= 0) {
             screenFadeOverlayAlpha = 0;
             gameUnfaded = true;
             gameFaded = false;
         }
     }
 
     private void updateSpellTimer(float tpf, int count) {
         for(int i = 0; i < count; i++) {
             spellTimer[i] += tpf;
         }
     }
     private Spatial createBullet(BULLET type){
         switch(type) {
             case TALISMAN_R: return talismanR.clone();
             case TALISMAN_B: return talismanW.clone();
             case BALLSHOT_W: return ballShotW.clone();
             case BALLSHOT_R: return ballShotR.clone();
             case BALLSHOT_B: return ballShotB.clone();
             case BEAMSHOT_R: return pillShotR.clone();
             case PETALSHOT_R: return petalShotR.clone();
             case ARROWSHOT_R: return arrowShotR.clone();
             case ARROWSHOT_B: return arrowShotB.clone();
             case ARROWSHOT_P: return arrowShotP.clone();
             case ARROWSHOT_G: return arrowShotG.clone();
             case ARROWSHOT_T: return arrowShotT.clone();
             case ARROWSHOT_W: return arrowShotW.clone();
             case ARROWSHOT_O: return arrowShotO.clone();
             case ARROWSHOT_Y: return arrowShotY.clone();
             case BALLSHOT_P: return ballShotP.clone();
             case KNIFE_B: return knifeB.clone();
             case KNIFE_K: return knifeK.clone();
             case KNIFE_W: return knifeW.clone();
             default: return talismanR.clone();
         }
     }
     private void firePlayerShot1(Vector3f src, Vector3f targ, float speed, float scale, BULLET type) {
         /*ParticleEmitter emit = new ParticleEmitter("Emitter", Type.Triangle, 200);
         emit.setLowLife(1);
         emit.setHighLife(1);
         emit.setImagesX(15);
         Material mat = new Material(assetManager, "MatDefs/Particle.j3md");
         mat.setTexture("m_Texture", assetManager.loadTexture("Effects/Smoke/Smoke.png"));
         emit.setMaterial(mat);
 
         rootNode.attachChild(emit);*/
         StaticBullet sS = new StaticBullet("shot" + shotCount++, 50.5f);
         if(type == BULLET.ARROWSHOT_R) {
             sS.attachChild(arrowShotR.clone().scale(3));
         } else {
             sS.attachChild(arrowShotR.clone().scale(3));
         }
         sS.scale(scale);
         //StraightShot straightControl = new StraightShot(sS,src,targ.subtract(src),speed);
         VarSpeedShot straightControl = new VarSpeedShot(sS,src,new Vector3f(0,-5,0),speed, 1f, 60);
         sS.addControl(straightControl);
 
         ParticleEmitter shotFlame = new ParticleEmitter("shotEmitter" + (shotCount- 1),ParticleMesh.Type.Triangle, 90);
         Material flameMat = new Material(assetManager, "MatDefs/Particle.j3md");
         flameMat.setTexture("m_Texture", assetManager.loadTexture("Textures/game/flame.png"));
         shotFlame.setMaterial(flameMat);
         shotFlame.setQueueBucket(Bucket.Translucent);
         shotFlame.setNumParticles(250);
         shotFlame.setImagesX(2);
         shotFlame.setImagesY(2);
         //shotFlame.getParticleInfluencer().setInitialVelocity(player.getLocalTranslation().subtract(enemy.getLocalTranslation()).normalize().mult(10));
         shotFlame.getParticleInfluencer().setInitialVelocity(new Vector3f(0,20,0));
         shotFlame.setLowLife(0.6f);
         shotFlame.setHighLife(1f);
         shotFlame.setStartSize(2);
         shotFlame.setEndSize(0.1f);
         shotFlame.setEndColor(new ColorRGBA(1f, 0f, 0f, 0.1f));   // red
         shotFlame.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.2f)); // yellow
         shotFlame.setParticlesPerSec(300);
         shotFlame.setVelocityVariation(0.9f);
         sS.attachChild(shotFlame);
         heat -= 500;
         shotNode.attachChild(sS);
     }
     private void fireCurveShot1(Vector3f src, Vector3f initialVector, boolean right, float tr, float t1, float t2,float speed, float scale, BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         CurveShot1 curve1Control = new CurveShot1(sS,src,initialVector,right, tr, t1, t2,speed);
         sS.addControl(curve1Control);
         bulletNode.attachChild(sS);
     }
     private void fireCurveLine1(Vector3f src, Vector3f targ, int shotCount, float offset, boolean right, float tr, float t1, float t2, float speed, float scale, BULLET type) {
         Vector3f target = new Vector3f(targ);
         Vector3f source = new Vector3f(src);
         source.set(source.normalize());
         target.set(target.normalize());
         Vector3f aim = new Vector3f(targ.subtract(src).normalize());
         float targAngle;
         if(aim.x <= 0 && aim.y > 0) {
             targAngle = FastMath.asin(aim.x);
         } else if(aim.x > 0 && aim.y > 0) {
             targAngle = FastMath.acos(aim.y);
         } else if(aim.x < 0 && aim.y <= 0) {
             targAngle = -FastMath.acos(aim.y);
         } else {
             targAngle = FastMath.PI-FastMath.asin(aim.x);
         }
 
         float angle = offset + targAngle;
         for(int i = 0; i < shotCount; i++) {
             target.set(new Vector3f(FastMath.sin(angle),FastMath.cos(angle),0));
             target.set(target.normalize().add(src));
             fireCurveShot1(src, target, right, tr, t1, t2, speed+i, scale, type);
         }
     }
     private void fireCurveCircle1(Vector3f src, int lines, int shotCount, float offset, boolean right, float tr, float t1, float t2,  float speed, float scale, BULLET type) {
         float angle = 0;
         for(int i = 0; i < lines; i++) {
             angle = ((i+1)*(FastMath.TWO_PI/lines)) + offset;
             fireCurveLine1(src, src.add(0,1,0), shotCount, angle, right, tr, t1, t2, speed, scale, type);
         }
     }
     
     private void fireBendShot(Vector3f src, Vector3f targ, Vector3f targ2, float speed, float scale,BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         BendShot bendControl = new BendShot(sS,src,targ.subtract(src), targ2, speed);
         sS.addControl(bendControl);
         bulletNode.attachChild(sS);
     }
     private void fireS3S2Shot(float offset, Vector3f targ2, float speed, float s2, float radius, float t1, float t2, boolean ccw, float scale,BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         S3S2Shot s2s3control = new S3S2Shot(sS, offset, speed, radius, t1, t2, targ2, s2, ccw);
         sS.addControl(s2s3control);
         bulletNode.attachChild(sS);
     }
     private void fireS3S4Shot(Vector3f src, Vector3f targ, float speed, float scale, BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         S3S4Shot bounceControl = new S3S4Shot(sS,src,targ.subtract(src),speed,playerMaxSide,playerMaxDistance, playerMinDistance);
         sS.addControl(bounceControl);
         bulletNode.attachChild(sS);
     }
     private void fireS3S4Line(Vector3f src, Vector3f targ, int shotCount, float offset, float speed, float scale, BULLET type) {
         Vector3f target = new Vector3f(targ);
         Vector3f source = new Vector3f(src);
         source.set(source.normalize());
         target.set(target.normalize());
         Vector3f aim = new Vector3f(targ.subtract(src).normalize());
         float targAngle;
         if(aim.x <= 0 && aim.y > 0) {
             targAngle = FastMath.asin(aim.x);
         } else if(aim.x > 0 && aim.y > 0) {
             targAngle = FastMath.acos(aim.y);
         } else if(aim.x < 0 && aim.y <= 0) {
             targAngle = -FastMath.acos(aim.y);
         } else {
             targAngle = FastMath.PI-FastMath.asin(aim.x);
         }
 
         float angle = offset + targAngle;
         for(int i = 0; i < shotCount; i++) {
             target.set(new Vector3f(FastMath.sin(angle),FastMath.cos(angle),0));
             target.set(target.normalize().add(src));
             fireS3S4Shot(src, target, speed+i, scale, type);
         }
     }
     private void fireScaleShot(Vector3f src, Vector3f targ, float speed, int t1, int t2, float scale,BULLET type, String tag) {
         StaticBullet sS = new StaticBullet(tag+"bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         ScaleShot straightControl = new ScaleShot(sS,src,targ.subtract(src),t1,t2,speed);
         sS.addControl(straightControl);
         bulletNode.attachChild(sS);
     }
     private void fireStraightShot(Vector3f src, Vector3f targ, float speed, float scale, BULLET type) {
         fireStraightShot(src,targ,speed,scale,type,"");
     }
     private void fireStraightShot(Vector3f src, Vector3f targ, float speed, float scale,BULLET type, String tag) {
         StaticBullet sS = new StaticBullet(tag+"bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         StraightShot straightControl = new StraightShot(sS,src,targ.subtract(src),speed);
         sS.addControl(straightControl);
         bulletNode.attachChild(sS);
     }
     private void fireUncannySealCircle(Vector3f src, int lines, float t1, float t2, float d1, float d2, boolean dir, float offset, float speed, float scale, BULLET type) {
         float angle = 0;
         for(int i = 0; i < lines; i++) {
             angle = (i*(FastMath.TWO_PI/lines)) + offset;
             fireUncannySealLine(src, src.add(0,1,0), t1,t2,d1,d2,dir, angle, speed, scale, type);
         }
     }
     private void fireUncannySealLine(Vector3f src, Vector3f targ, float t1, float t2, float d1, float d2, boolean dir, float offset, float speed, float scale, BULLET type) {
         Vector3f target = new Vector3f(targ);
         Vector3f source = new Vector3f(src);
         source.set(source.normalize());
         target.set(target.normalize());
         Vector3f aim = new Vector3f(targ.subtract(src).normalize());
         float targAngle;
         if(aim.x <= 0 && aim.y > 0) {
             targAngle = FastMath.asin(aim.x);
         } else if(aim.x > 0 && aim.y > 0) {
             targAngle = FastMath.acos(aim.y);
         } else if(aim.x < 0 && aim.y <= 0) {
             targAngle = -FastMath.acos(aim.y);
         } else {
             targAngle = FastMath.PI-FastMath.asin(aim.x);
         }
 
         float angle = offset + targAngle;
             target.set(new Vector3f(FastMath.sin(angle),FastMath.cos(angle),0));
             target.set(target.normalize().add(src));
             fireUncannySealShot(src, target, t1,t2,d1,d2, dir, speed, scale, type);
     }
     private void fireUncannySealShot(Vector3f src, Vector3f targ, float t1, float t2, float d1, float d2, boolean dir, float speed, float scale,BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         UncannySealShot straightControl = new UncannySealShot(sS,src,targ.subtract(src),speed, t1,t2,d1,d2,dir);
         sS.addControl(straightControl);
         bulletNode.attachChild(sS);
     }
     private void fireReflectShot(Vector3f targ, float speed, BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++, 50.5f);
         if(type == BULLET.TALISMAN_R) {
             sS.attachChild(talismanR.clone());
         } else {
             sS.attachChild(talismanW.clone());
         }
 
         ReflectShotControl straightControl = new ReflectShotControl(sS,Vector3f.ZERO,targ,speed,24,24,1.7f,false);
         sS.addControl(straightControl);
         bulletNode.attachChild(sS);
     }
 
     private void fireReflectLine(Vector3f targ, int shotCount, float speed, BULLET type) {
         for(int i = 0; i < shotCount; i++) {
             fireReflectShot(targ,speed+i, type);
         }
     }
 
     private void fireReflectCircle(Vector3f targ, int lines, int shotCount, float offset, float speed, BULLET type) {
         float angle = 0;
         Vector3f angleTarg = new Vector3f();
         for(int i = 0; i < lines; i++) {
             angle = (i*(FastMath.TWO_PI/lines)) + offset;
             angleTarg.set(new Vector3f(FastMath.sin(angle),FastMath.cos(angle),0));
             fireReflectLine(angleTarg, shotCount, speed,type);
         }
     }
     private void fireStraightLine(Vector3f src, Vector3f targ, int shotCount, float offset, float speed, float scale, BULLET type) {
         fireStraightLine(src,targ,shotCount,offset,speed,scale,type,"");
     }
     private void fireStraightLine(Vector3f src, Vector3f targ, int shotCount, float offset, float speed, float scale, BULLET type, String tag) {
         Vector3f target = new Vector3f(targ);
         Vector3f source = new Vector3f(src);
         source.set(source.normalize());
         target.set(target.normalize());
         Vector3f aim = new Vector3f(targ.subtract(src).normalize());
         float targAngle;
         if(aim.x <= 0 && aim.y > 0) {
             targAngle = FastMath.asin(aim.x);
         } else if(aim.x > 0 && aim.y > 0) {
             targAngle = FastMath.acos(aim.y);
         } else if(aim.x < 0 && aim.y <= 0) {
             targAngle = -FastMath.acos(aim.y);
         } else {
             targAngle = FastMath.PI-FastMath.asin(aim.x);
         }
 
         float angle = offset + targAngle;
         for(int i = 0; i < shotCount; i++) {
             target.set(new Vector3f(FastMath.sin(angle),FastMath.cos(angle),0));
             target.set(target.normalize().add(src));
             fireStraightShot(src, target, speed+i, scale, type,tag);
         }
     }
     private void fireStraightCircle(Vector3f src, int lines, int shotCount, float offset, float speed, float scale, BULLET type) {
         fireStraightCircle(src,lines,shotCount,offset,speed,scale,type,"");
     }
     private void fireStraightCircle(Vector3f src, int lines, int shotCount, float offset, float speed, float scale, BULLET type, String tag) {
         float angle = 0;
         for(int i = 0; i < lines; i++) {
             angle = (i*(FastMath.TWO_PI/lines)) + offset;
             fireStraightLine(src, src.add(0,1,0), shotCount, angle, speed, scale, type, tag);
         }
     }
 
     private void fireSpeedShot(Vector3f src, Vector3f targ, float speed, float t1, float s1, float scale, BULLET type) {
         StaticBullet sS = new StaticBullet("bullet"+bulletCount++,50.5f);
         sS.attachChild(createBullet(type));
         sS.scale(scale);
         VarSpeedShot varSpeedControl = new VarSpeedShot(sS, src, targ.subtract(src), speed, t1, s1);
         sS.addControl(varSpeedControl);
         bulletNode.attachChild(sS);
     }
     private void fireSpeedLine(Vector3f src, Vector3f targ, int shotCount, float offset, float speed, float t1, float s1, float scale, BULLET type) {
         Vector3f target = new Vector3f(targ);
         Vector3f source = new Vector3f(src);
         source.set(source.normalize());
         target.set(target.normalize());
         Vector3f aim = new Vector3f(targ.subtract(src).normalize());
         float targAngle;
         if(aim.x <= 0 && aim.y > 0) {
             targAngle = FastMath.asin(aim.x);
         } else if(aim.x > 0 && aim.y > 0) {
             targAngle = FastMath.acos(aim.y);
         } else if(aim.x < 0 && aim.y <= 0) {
             targAngle = -FastMath.acos(aim.y);
         } else {
             targAngle = FastMath.PI-FastMath.asin(aim.x);
         }
 
         float angle = offset + targAngle;
         for(int i = 0; i < shotCount; i++) {
             target.set(new Vector3f(FastMath.sin(angle),FastMath.cos(angle),0));
             target.set(target.normalize().add(src));
             fireSpeedShot(src, target, speed+i, t1, s1, scale, type);
         }
     }
     private void fireSpeedCircle(Vector3f src, int lines, int shotCount, float offset, float speed, float t1, float s1, float scale, BULLET type) {
         float angle = 0;
         for(int i = 0; i < lines; i++) {
             angle = (i*(FastMath.TWO_PI/lines)) + offset;
             fireSpeedLine(src, src.add(0,1,0), shotCount, angle, speed, t1, s1, scale, type);
         }
     }
     //----------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------
     public void updateEndGame(float tpf) {
         cam.setLocation(Vector3f.ZERO.add(0,0,50));
         cam.lookAt(Vector3f.ZERO.add(0,0,0), Vector3f.UNIT_Y);
         
         showDialogue = true;
         updateGamePortraits(tpf);
         say("Are we finished tonight?", 1);
         
         //System.out.println("update end game");
         if(stateFade) {
             if(endgameTextColor.a - tpf > 0) {
                 endgameTextColor.a -= tpf;
             } else {
                 endgameTextColor.a = 0;
             }
             
             if(endgameImageColor.a - tpf > 0) {
                 endgameImageColor.a -= tpf;
             } else {
                 endgameImageColor.a = 0;
             }
             
             endgameText.setColor(endgameTextColor);
             endgameImageMat.setColor("Color", endgameImageColor);
         } else {
             if(fadeFilter.getValue() > 0.95f) {
                 if(endgameTextColor.a + tpf < 0.6) {
                     endgameTextColor.a += tpf;
                 } else {
                     endgameTextColor.a = 0.6f;
                 }
                 
                 if(endgameImageColor.a + tpf < 1) {
                     endgameImageColor.a += tpf/2;
                 } else {
                     endgameImageColor.a = 1;
                 }
                 
                 endgameText.setColor(endgameTextColor);
                 endgameImageMat.setColor("Color", endgameImageColor);
             }
         }
 
         endgameImageMat.setColor("Color", endgameImageColor);
         
         endgameImage.move(0,-tpf * 20,0);
         /* SCROLL BACKGROUND
         if(endBackground.getY() - tpf*20 > -64)
             endBackground.move(0,-tpf * 20,0);
         else
             endBackground.setY(-64);
          * 
          */
     }
 
     //Input handling inner classes
     private ActionListener openSplashListener = new ActionListener() {
         public void onAction(String name, boolean keyPressed, float tpf) {
             if (name.equals("advance")) {
                 openSplashState.complete();
                 System.out.println("Advancing to state " + (currentGameState.name()));
                 inputManager.deleteMapping("advance");
             }
         }
     };
     private ActionListener mainMenuListener = new ActionListener() {
         public void onAction(String name, boolean keyPressed, float tpf) {
             if((name.equals("select") || name.equals("mselect"))&& keyPressed) {
                 System.out.println("Click detected");
                 //Perform main menu action depending on active button
                 System.out.println(curMainMenuItem);
                 if(curMainMenuItem == MAINMENU_START) {
                     System.out.println("Start button clicked");
                 }
                 switch(curMainMenuItem) {
                     case MAINMENU_START:
                         if(timer[T_MAINMENU_TIME] > MAINMENU_MIN_TIME) {
                             mainMenuState.complete();
                             System.out.println("Advancing to state " + (currentGameState.next()));
                             inputManager.deleteMapping("select");
                             inputManager.deleteMapping("mselect");
                         }
                         break;
                     case MAINMENU_EXIT:
                         stop();
                         System.out.println("Exiting");
                         break;
                 }
             }
             if(name.equals("up") && keyPressed) {
                 //Up key pressed during main menu
                 curMainMenuItem--;
                 if(curMainMenuItem < 0) {
                     curMainMenuItem = mainMenuSize;
                 }
             }
             if(name.equals("down") && keyPressed) {
                 curMainMenuItem++;
                 if(curMainMenuItem > mainMenuSize) {
                     curMainMenuItem = 0;
                 }
             }
         }
     };
     private ActionListener gameStartListener = new ActionListener() {
         public void onAction(String name, boolean keyPressed, float tpf) {
             if(name.equals("advance") && keyPressed) {
                 gameStartState.complete();
                 System.out.println("Advancing to state " + (currentGameState.next()));
                 inputManager.deleteMapping("advance");
             }
         }
     };
     private ActionListener gameListener = new ActionListener() {
         private void correctAnim() {
             if(MOVE_LEFT) {
                 playerAnimChan.setAnim("left",0.7f);
             }
             if(MOVE_RIGHT) {
                 playerAnimChan.setAnim("right",0.7f);
             }
             if(MOVE_UP) {
                 playerAnimChan.setAnim("up",0.7f);
             }
             if(MOVE_DOWN) {
                 playerAnimChan.setAnim("down",0.7f);
             }
         }
         public void onAction(String name, boolean keyPressed, float tpf) {
             if(name.equals("left") && !gamePause) {
                 if(keyPressed) {
                     MOVE_LEFT = true;
                     MOVE_STAND = false;
                     try {
                         playerAnimChan.setAnim("left",0.7f);
                     } catch(Exception ex) {
                         //Game not ready.
                     }
                 } else {
                     MOVE_LEFT = false;
                     correctAnim();
                 }
             }
             if(name.equals("right") && !gamePause) {
                 if(keyPressed) {
                     MOVE_RIGHT = true;
                     MOVE_STAND = false;
                     try {
                         playerAnimChan.setAnim("right",0.7f);
                     } catch(Exception ex) {
                         //Game not ready.
                     }
                 } else {
                     MOVE_RIGHT = false;
                     correctAnim();
                 }
             }
             if(name.equals("up")) {
                 if(gamePause && keyPressed) {//Handle for pause menu
                     curGameMenuItem--;
                     if(curGameMenuItem < 0) {
                         curGameMenuItem = pauseMenuSize;
                     }
                 } else {//Handle for game
                     if(keyPressed) {
                         MOVE_UP = true;
                         MOVE_STAND = false;
                         try {
                             playerAnimChan.setAnim("up",0.7f);
                         } catch(Exception ex) {
                             //Game not ready.
                         }
                     } else {
                         MOVE_UP = false;
                         correctAnim();
                     }
                 }
             }
             if(name.equals("down")) {
                 if(gamePause && keyPressed) {//Handle for pause menu
                     curGameMenuItem++;
                     if(curGameMenuItem > pauseMenuSize) {
                         curGameMenuItem = 0;
                     }
                 } else {//Handle for game
                     if(keyPressed) {
                         MOVE_DOWN = true;
                         MOVE_STAND = false;
                         try {
                             playerAnimChan.setAnim("down",0.7f);
                         } catch(Exception ex) {
                             //Game not ready.
                         }
                     } else {
                         MOVE_DOWN = false;
                         correctAnim();
                     }
                 }
             }
             if(name.equals("pause") && keyPressed) {
                 if(gamePause) {
                     unpauseGame();
                 } else {
                     pauseGame();
                 }
                 System.out.println("Pause: " + gamePause);
             }
             if(name.equals("shoot") && keyPressed) {
                 if(spellFlag[SFLAG_ZWAIT]) {
                     spellFlag[SFLAG_SCORE] = true;
                 }
                 if(!gamePause){//Handle Z key for gameplay
                     if(!advanceEventTime) {//Handle Z for continuing dialogue
                         advanceEventTime = true;
                         dialogue.setText("");
                         dialoguePlayer = false;
                         dialogueEnemy = false;
                         dialogueActive = false;
                     } else {//Handle z key for shooting: [03 27 2011]
                         if(heat > 100) {
                             firePlayerShot1(player.getLocalTranslation(), enemy.getLocalTranslation(), 35, 2, BULLET.ARROWSHOT_R);
                         } else {
                             try {
                                 playerHeatEmitter.emitAllParticles();
                                 playerHeatEmitter.setParticlesPerSec(0);
                                 heat -= 200;
                             } catch(Exception ex) {}
                         }
                     }
                 } else {//If we're paused, use it to select menu items.
                     switch(curGameMenuItem) {
                         case GAMEMENU_CONTINUE:
                             if(continueCount < MAX_CONTINUE) {
                                 if(gamePause) {
                                     unpauseGame();
                                 } else {
                                     pauseGame();
                                 }                            
                                 player.setLife(player.MAX_LIFE);
                                 graze = 0;
                                 heat = 0;
                                 if(gameOverFlag) {
                                     continueCount++;
                                 }
                                 gameOverFlag = false;
                             }
                             break;
                         case GAMEMENU_RETRY:
                             gameState.complete();
                             filtPostProc.removeFilter(radialBlur);
                             fadeFilter.fadeOut();
                             guiNode.detachAllChildren();
                             break;
                         case GAMEMENU_RETURN:
                             gameState.complete();
                             filtPostProc.removeFilter(radialBlur);
                             fadeFilter.fadeOut();
                             guiNode.detachAllChildren();
                             break;
                     }
                 }
             }
         }
     };
 
     private void pauseGame() {
         gamePause = true;
         System.out.println("Pausing...");
         //Set the mark position
         gameMouseMenuVec.set(screenWidth/2,screenHeight/2,50);
         //Pause every bullet
         StaticBullet currBullet;
         Iterator bulletIterator = bulletNode.getChildren().iterator();
 
         while(bulletIterator.hasNext()) {
             try{
                 currBullet = (StaticBullet)bulletIterator.next();
                 currBullet.getControl(StraightShot.class).setPause(true);
             }catch(Exception ex) {}
         }
         ground1Control.setEnabled(false);
         ground2Control.setEnabled(false);
         ground3Control.setEnabled(false);
     }
     private void unpauseGame() {
         gamePause = false;
         System.out.println("Unpausing...");
         //unpause every bullet
         StaticBullet currBullet;
         Iterator bulletIterator = bulletNode.getChildren().iterator();
 
         while(bulletIterator.hasNext()) {
             try{
                 currBullet = (StaticBullet)bulletIterator.next();
                 currBullet.getControl(StraightShot.class).setPause(false);
             }catch(Exception ex) {}
         }
         ground1Control.setEnabled(true);
         ground2Control.setEnabled(true);
         ground3Control.setEnabled(true);
         try {guiNode.detachChild(menuMark);}catch(Exception ex) {}
     }
 
     private ActionListener focusListener = new ActionListener() {
         public void onAction(String name, boolean isPressed, float tpf) {
             if(name.equals("focus") && !gamePause && isPressed) {
                 playerFocus = true;
             } else if(name.equals("focus") && !gamePause && !isPressed) {
                 playerFocus = false;
             }
         }
     };
     private AnimEventListener playerAnimListener = new AnimEventListener() {
         public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
         }
         public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         }
     };
     private AnimEventListener enemyAnimListener = new AnimEventListener() {
         public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
         }
         public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         }
     };
 
     protected void initMark() {
         Box box = new Box(5,5,1);
         Box box1 = new Box(0.4f,0.4f,0.01f);
         mark = new Geometry("mark", box1);
         Material markMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         markMat.setTexture("ColorMap", assetManager.loadTexture("Textures/cursor.png"));
         markMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         mark.setQueueBucket(Bucket.Translucent);
         mark.setMaterial(markMat);
         menuMark = new Geometry("guiMark",box);
         Material menuMarkMat = new Material(assetManager, "MatDefs/Unshaded.j3md");
         menuMarkMat.setTexture("ColorMap", assetManager.loadTexture("Textures/cursor.png"));
         menuMarkMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
         menuMark.setMaterial(menuMarkMat);
     }
     
     float lastX;
     float lastY;
     Vector2f menuMarkVec;
     public void handleMainMenuMouse(float tpf) {
         menuMark.move(inputManager.getCursorPosition().x - lastX, inputManager.getCursorPosition().y - lastY, 0);
         if(menuMarkVec == null) {
             menuMarkVec = new Vector2f();
         }
         menuMarkVec.set(menuMark.getLocalTranslation().x, menuMark.getLocalTranslation().y);
         Vector3f origin    = cam.getWorldCoordinates(menuMarkVec, 0.0f);
         Vector3f direction = cam.getWorldCoordinates(menuMarkVec, 0.3f);
 
         direction.subtractLocal(origin).normalizeLocal();
         String activeItem = new String();
         Ray ray = new Ray(origin, direction);
         CollisionResults results = new CollisionResults();
         titleUIElements.collideWith(ray, results);
         //menuMark.setLocalTranslation(inputManager.getCursorPosition().x,inputManager.getCursorPosition().y,0);
         
         if(!guiNode.hasChild(menuMark)) {
             guiNode.attachChild(menuMark);
         }
         if (results.size() > 0 && titleAlpha[2] >= 0.5f) {
             CollisionResult closest = results.getClosestCollision();
             Quaternion q = new Quaternion();
             q.lookAt(closest.getContactNormal(), Vector3f.UNIT_Y);
             activeItem = closest.getGeometry().toString();
         } else {
             activeItem = new String();
             curMainMenuItem = MAINMENU_NULL;
         }
         if(activeItem.contains("start")) {
             curMainMenuItem = MAINMENU_START;
             if(titleAlpha[1] < 1) {
                 titleAlpha[1] += 3*1/60f;
                 startButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[1]));
             }
         } else {
             if(titleAlpha[1] > 0.6) {
                 titleAlpha[1] -= 3*1/60f;
                 startButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[1]));
             }
         }
         if(activeItem.contains("exit")) {
             curMainMenuItem = MAINMENU_EXIT;
             if(titleAlpha[2] < 0.8) {
                 titleAlpha[2] += 3*1/60f;
                 exitButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[2]));
             }
         } else {
             if(titleAlpha[2] > 0.6) {
                 titleAlpha[2] -= 3*1/60f;
                 exitButtonMat.setColor("Color", new ColorRGBA(1,1,1, titleAlpha[2]));
             }
         }
         
         lastX = inputManager.getCursorPosition().x;
         lastY = inputManager.getCursorPosition().y;
     }
     
     Vector3f curMouseVec = new Vector3f();
     Vector3f lastMouseVec = new Vector3f();
     Vector3f gameMouseDelta = new Vector3f();
     Vector3f gameMouseMenuVec = new Vector3f();
     final static float cursorMoveFactorX = 20;
     final static float cursorMoveFactorY = 20;
     final static float cursorMoveMenuFactorX = 200;
     final static float cursorMoveMenuFactorY = 200;
     public void handleGameMouse() {
         //Calculate the value that the mouse coordinates changed.
         //Get the current mouse vector from the camera
         curMouseVec.set(cam.getWorldCoordinates(inputManager.getCursorPosition(), 0));
 
         //Set the delta vector to the current location minus the last location
         gameMouseDelta.set(curMouseVec.subtract(lastMouseVec));
 
         //Set the last location vector for the next pass
         lastMouseVec.set(curMouseVec);
         
         
         if(!gamePause){
             gameMouseDelta.multLocal(cursorMoveFactorX, cursorMoveFactorY, 0);
             Vector3f vecD = new Vector3f(gameMouseDelta.x, gameMouseDelta.y, 0);
             if(gameFlag[GFLAG_MOVE_ENABLED]) {
                 gameMouseLoc.set(gameMouseLoc.add(vecD));
             }
             if(gameMouseLoc.x > playerMaxSide) {
                gameMouseLoc.setX(playerMaxSide);
             }
             if(gameMouseLoc.x < -playerMaxSide) {
                gameMouseLoc.setX(-playerMaxSide);
             }
             if(gameMouseLoc.y < playerMinDistance) {
                gameMouseLoc.setY(playerMinDistance);
             }
             if(gameMouseLoc.y > playerMaxDistance) {
                gameMouseLoc.setY(playerMaxDistance);
             }
             rootNode.attachChild(mark);
             if(guiNode.hasChild(menuMark)) {
                 guiNode.detachChild(menuMark);
             }
         } else {
             switch(curGameMenuItem) {
                 case GAMEMENU_CONTINUE:
                         if(continueCount < MAX_CONTINUE) {
                             menuAlpha[3] = 1;
                         } else {
                             menuAlpha[3] = 0.7f;
                         }
                         menuAlpha[4] = 0.7f;
                         menuAlpha[5] = 0.7f;
                     break;
                 case GAMEMENU_RETRY:
                         menuAlpha[4] = 1;
                         menuAlpha[3] = 0.7f;
                         menuAlpha[5] = 0.7f;
                     break;
                 case GAMEMENU_RETURN:
                         menuAlpha[5] = 1;
                         menuAlpha[3] = 0.7f;
                         menuAlpha[4] = 0.7f;
                     break;
                 default:
                     menuAlpha[3] = 0.7f;
                     menuAlpha[4] = 0.7f;
                     menuAlpha[5] = 0.7f;
             }
             //1: pause 2: gameover 3: continue 4: retry 5: return
             menuPause.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[1]));
             menuGameOver.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[2]));
             menuContinue.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[3]));
             menuRetry.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[4]));
             menuReturn.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[5]));
             
             //Handle for pause menu
             gameMouseMenuVec.set(gameMouseMenuVec.add(gameMouseDelta.multLocal(-cursorMoveMenuFactorX, -cursorMoveMenuFactorY, 0)));
             Vector3f pos = gameMouseMenuVec;
             Vector3f origin    = new Vector3f(pos.x,pos.y, 30.0f);
             Vector3f direction = new Vector3f(pos.x,pos.y, 29.3f);
             menuMark.setLocalTranslation(origin.add(0,0,50));   
             
             direction.subtractLocal(origin).normalizeLocal();
             Ray ray = new Ray(origin, direction);
             CollisionResults results = new CollisionResults();
             guiNode.collideWith(ray, results);
             
             guiNode.attachChild(menuMark);
             
             if (results.size() > 0) {
                 CollisionResult closest = results.getClosestCollision();
                 String name = closest.getGeometry().getName();
                 if(name.contains("Continue")) {
                     curGameMenuItem = GAMEMENU_CONTINUE;
                 } else if(name.contains("Return")) {
                     curGameMenuItem = GAMEMENU_RETURN;
                 } else if(name.contains("Retry")) {
                     curGameMenuItem = GAMEMENU_RETRY;
                 } else {
                     curGameMenuItem = GAMEMENU_NULL;
                 }
             } else {
                  curGameMenuItem = GAMEMENU_NULL;
             }
         }
     }
     
     private void fadePauseMenuIn(float tpf) {
             if(!gameOverFlag) {
                 menuAlpha[1] += 1/60f;
             } else {
                 menuAlpha[2] += 1/60f;
             }
             menuAlpha[3] += 1/60f*1.5;
             menuAlpha[4] += 1/60f*2;
             menuAlpha[5] += 1/60f*2.5;
             for(int i = 2; i < 9; i++) {
                 if(menuAlpha[i] > 0.7) {
                     menuAlpha[i] = 0.7f;
                 }
             }
             //1: pause 2: gameover 3: continue 4: retry 5: return
             menuPause.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[1]));
             menuGameOver.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[2]));
             menuContinue.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[3]));
             menuRetry.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[4]));
             menuReturn.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[5]));
     }
     private void fadePauseMenuOut(float tpf){
         tpf = 3f * 1/60f;
         menuAlpha[1] -= tpf*1;
         menuAlpha[2] -= tpf*1.5;
         menuAlpha[3] -= tpf*2;
         menuAlpha[4] -= tpf*2.5;
         menuAlpha[5] -= tpf*3;
         for(int i = 0; i < 9; i++) {
             if(menuAlpha[i] < 0) {
                 menuAlpha[i] = 0;
             }
         }
         //1: pause 2: gameover 3: continue 4: retry 5: return
         menuPause.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[1]));
         menuGameOver.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[2]));
         menuContinue.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[3]));
         menuRetry.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[4]));
         menuReturn.getMat().setColor("Color", new ColorRGBA(1,1,1,menuAlpha[5]));
     }
 }
