 package com.example.homokaasuthegame;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.util.GLState;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.graphics.Color;
 import android.hardware.SensorManager;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class MainActivity extends BaseGameActivity {
     protected static final int CAMERA_WIDTH = 1024;
     protected static final int CAMERA_HEIGHT = 550;
 
     //tunes
     private Music theme;
     private Music gameoverTheme;
     private Music menuTheme;
 
 	//List of enemies
 	final LinkedList<Enemy> enemies = new LinkedList<Enemy>();
 	final LinkedList<Enemy> removeList = new LinkedList<Enemy>();
 
 	private Pie pie;
 	Vector2 target;
 	private ITextureRegion backgroundTextureRegion;
 	boolean gameOver = false;
 	int score;
 	Text scoreText;
 
     static PhysicsWorld physicsWorld;
 
     private BitmapTextureAtlas mFontTexture;
     private Font mFont;
 
     /* Scenes */
     private Scene splashScene;
     static public Scene mainScene;
     private Scene endScene;
     private static Scene menuScene;
 
     public static MainActivity mainActivity;
 
     /* Splash screen resources */
     Sprite splashSprite;
 
     private enum SceneType
     {
         SPLASH,
         MAIN,
         MENU,
         END,
         OPTIONS,
         WORLD_SELECTION,
         LEVEL_SELECTION,
         CONTROLLER
     }
     private SceneType currentScene = SceneType.SPLASH;
 
     public MainActivity() {
         mainActivity = this;
     }
 
     @Override
     public EngineOptions onCreateEngineOptions() {
         Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
         EngineOptions options = new EngineOptions(
                 true,
                 ScreenOrientation.LANDSCAPE_FIXED,
                 new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                 // new FillResolutionPolicy(),
                 mCamera);
         options.getAudioOptions().setNeedsMusic(true);
         options.getAudioOptions().setNeedsSound(true);
 
 
         return options;
     }
 
     @Override
     public void onCreateResources(
             OnCreateResourcesCallback pOnCreateResourcesCallback)
             throws Exception {
         loadSplashScreen();
         pOnCreateResourcesCallback.onCreateResourcesFinished();
     }
 
     private void loadSplashScreen() {
         BitmapTextureAtlas splashTexture;
         ITextureRegion splashTextureRegion;
 
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
         splashTexture = new BitmapTextureAtlas(getTextureManager(), 1024, 550,
                 TextureOptions.DEFAULT);
         splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTexture, this, "kerola.png", 0, 0);
         splashTexture.load();
 
         splashSprite = new Sprite(splashTextureRegion.getWidth(),
                 splashTextureRegion.getHeight(), splashTextureRegion,
                 mEngine.getVertexBufferObjectManager())
         {
             @Override
             protected void preDraw(GLState pGLState, Camera pCamera)
             {
                 super.preDraw(pGLState, pCamera);
                 pGLState.enableDither();
             }
         };
     }
 
     @Override
     public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
             throws Exception {
         initSplashScene();
 
 
         pOnCreateSceneCallback.onCreateSceneFinished(MainActivity.menuScene);
     }
 
     @Override
     public void onPopulateScene(Scene pScene,
             OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 
         mEngine.registerUpdateHandler(new TimerHandler(2.0f, new ITimerCallback()
         {
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler)
             {
                 mEngine.unregisterUpdateHandler(pTimerHandler);
                 loadResources();
                 loadScenes();
                 populateMainScene();
                 splashScene.detachSelf();
                 populateMenuScene();
                 mEngine.setScene(menuScene);
                 currentScene = SceneType.MENU;
             }
         }));
 
         pOnPopulateSceneCallback.onPopulateSceneFinished();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getAction() == KeyEvent.ACTION_DOWN)
         {
             switch (currentScene)
             {
                 case SPLASH:
                     break;
                 case MAIN:
                 	System.exit(0);
                     //break;
                 case END:
                 	System.exit(0);
             }
         }
         return false;
     }
 
 /* External game state methods ************************************************/
 
     public void spawnAnt(float x, float y) {
         if (enemies.size() > 10) {
             return;
         }
 
         Ant a = new Ant(x, y, false, this.getVertexBufferObjectManager());
         mainScene.registerTouchArea(a);
         a.setTarget(target);
         enemies.add(a);
     }
 
     public void spawnFly(float x, float y) {
         if (enemies.size() > 10) {
             return;
         }
 
         Fly f = new Fly(x, y, this.getVertexBufferObjectManager());
         mainScene.registerTouchArea(f);
         Vector2 t = new Vector2((float)(target.x + Math.random() * 10f), target.y);
         f.setTarget(t);
         enemies.add(f);
     }
 
     public boolean eatPie() {
         if (pie != null) {
             return pie.eat();
         }
         return false;
     }
 
     public void removeEnemy(Enemy e) {
         removeList.add(e);
     }
 
     /**
      * This is called if pie.eat() returns false
      */
     public void gameOVer() {
         if (!gameOver)  {
             gameOver = true;
             populateEndScene();
             mEngine.setScene(endScene);
             theme.stop();
             gameoverTheme.play();
             Text gameOverText = new Text(CAMERA_WIDTH / 2 - 100, 200,
                     mFont, "PELI OHI", this.getVertexBufferObjectManager());
             gameOverText.setZIndex(100);
             MainActivity.mainScene.attachChild(gameOverText);
         }
     }
 
 
 /* Load Resources *************************************************************/
 
     public void loadResources() {
         loadFonts();
         loadGfx();
         loadMfx();
     }
 
 	private void loadFonts() {
         /* Load the font we are going to use. */
         FontFactory.setAssetBasePath("fonts/");
         this.mFontTexture = new BitmapTextureAtlas(
                 this.getTextureManager(),
                 1024, 1024,
                 TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         this.mFont = FontFactory.createFromAsset(
                 this.getFontManager(),
                 mFontTexture,
                 this.getAssets(),
                 "BistroSketch.ttf",
                 80.0f, true, Color.BLACK);
         this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
         this.getFontManager().loadFont(this.mFont);
     }
 
     private void loadGfx() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
        // width and height power of 2^x
        backgroundTextureRegion = loadTexture("bg.png", 1024, 600, 0, 0);
 
        /* Call static initializers */
        Ant.init(this);
        Fly.init(this);
        Pie.init(this);
        Paussi.init(this);
     }
 
     private void loadMfx() {
     	MusicFactory.setAssetBasePath("mfx/");
     	try {
             this.theme = MusicFactory.createMusicFromAsset(
                     this.mEngine.getMusicManager(), this, "themeofpie.ogg");
             this.theme.setLooping(true);
             this.gameoverTheme = MusicFactory.createMusicFromAsset
             		(this.mEngine.getMusicManager(), this, "gameover.ogg");
             this.gameoverTheme.setLooping(false);
             this.menuTheme = MusicFactory.createMusicFromAsset
             		(this.mEngine.getMusicManager(), this, "menumusic.ogg");
             this.menuTheme.setLooping(true);
     	} catch (final IOException e) {
     		Debug.e("Unable to load the tunes!", e);
     	}
     }
 
     /**
      * Load texture
      * @param name Filename
      * @param pTextureX
      * @param pTextureY
      * @return ITextureRegion
      */
     public ITextureRegion loadTexture(String name, int width, int height,
             int pTextureX, int pTextureY) {
         BitmapTextureAtlas texture;
         ITextureRegion textureRegion;
 
         texture = new BitmapTextureAtlas(getTextureManager(), width, height);
         textureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
                 texture, this, name, pTextureX, pTextureY);
 
         texture.load();
         return textureRegion;
     }
 
 
 /* Load Scenes ****************************************************************/
 
     private void loadScenes() {
         enemies.clear();
         removeList.clear();
         score = 0;
 
         MainActivity.menuScene = new Scene();
         MainActivity.menuScene.setBackground(new Background(0,125,58));
 
         loadMainScene();
 
         endScene = new Scene();
         endScene.setBackground(new Background(0, 125, 58));
 
         createWalls();
     }
 
     private void loadMainScene() {
         MainActivity.mainScene = new Scene();
         MainActivity.mainScene.setBackground(new Background(0, 125, 58));
         physicsWorld = new PhysicsWorld(
                 new Vector2(0, SensorManager.GRAVITY_EARTH), false);
         MainActivity.mainScene.registerUpdateHandler(physicsWorld);
         mainScene.setTouchAreaBindingOnActionDownEnabled(true);
         physicsWorld.setContactListener(new PieContactListener(this));
     }
 
     private void createWalls() {
         FixtureDef WALL_FIX = PhysicsFactory.createFixtureDef(0.0f, 0.5f, 0.2f);
 
         {
             Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 15, CAMERA_WIDTH,
                     15, this.mEngine.getVertexBufferObjectManager());
                 ground.setColor(new org.andengine.util.color.Color(15, 50, 0));
             PhysicsFactory.createBoxBody(physicsWorld, ground,
                     BodyType.StaticBody, WALL_FIX);
             MainActivity.mainScene.attachChild(ground);
         }
     }
 
     public void paussi() {
             getEngine().registerUpdateHandler(new TimerHandler(0.3f, new ITimerCallback()
             {
                 @Override
                 public void onTimePassed(final TimerHandler pTimerHandler)
                 {
                    if (enemies.size() == 0)
                        return;
                     Enemy e = enemies.getFirst();
                     removeList.add(e);
                     enemies.remove(e); /* Must remove here because of race
                                         * condition */
                     if (enemies.size() > 0) {
                         paussi();
                     }
                 }
             }));
     }
 
 
 /* Populate Scenes ************************************************************/
 
     private void populateMainScene() {
         /* Create background */
         Sprite bg = new Sprite(0, 0,
                 backgroundTextureRegion,
                 this.mEngine.getVertexBufferObjectManager());
         MainActivity.mainScene.attachChild(bg);
         target = new Vector2(15, 13);
 
         pie = new Pie(16f, 14.35f, this.getVertexBufferObjectManager());
 
         Text text = new Text(CAMERA_WIDTH - 800, 10, mFont, "The Life of Pie",
                 this.getVertexBufferObjectManager());
         text.setZIndex(100);
         MainActivity.mainScene.attachChild(text);
 
         scoreText = new Text(CAMERA_WIDTH - 100, 10, mFont,
                 "00000", this.getVertexBufferObjectManager());
         scoreText.setZIndex(100);
         MainActivity.mainScene.attachChild(scoreText);
 
         new Paussi(640f, -10f, this.getVertexBufferObjectManager());
 
         mainScene.registerUpdateHandler(new IUpdateHandler() {
             @Override
             public void onUpdate(float pSecondsElapsed) {
                 for (Enemy e : removeList) {
                     enemies.remove(e);
                     physicsWorld.destroyBody(e.body);
                     mainScene.unregisterTouchArea(e);
                     e.detachChildren();
                     e.detachSelf();
                     if (!gameOver)
                         score++;
                 }
                 removeList.clear();
 
                 scoreText.setText(String.valueOf(score));
                 mainScene.sortChildren();
             }
 
             @Override
             public void reset() {
                 // TODO Auto-generated method stub
 
             }});
 
         // Z-indexit kuntoon
         mainScene.sortChildren();
     }
 
     public void populateEndScene() {
         Sprite ebg = new Sprite(0, 0,
                 backgroundTextureRegion,
                 this.mEngine.getVertexBufferObjectManager());
 
         endScene.attachChild(ebg);
         theme.stop();
         gameoverTheme.play();
         mEngine.setScene(endScene);
         currentScene = SceneType.END;
         Text gameOverText = new Text(CAMERA_WIDTH / 2 + 100, CAMERA_HEIGHT / 2 - 100,
                 mFont, "   PELI OHI\nPisteet: " + score, this.getVertexBufferObjectManager());
         Text credits = new Text(CAMERA_WIDTH / 2 - 450, CAMERA_HEIGHT / 2 - 200,
                 mFont, "CREDITS:\n" +
                 		"Olli Vanhoja\n" +
                 		"Kalle Viiri\n" +
                 		"Juha Lindqvist\n" +
                 		"Juuso Stromberg\n" +
                 		"Sami Koskinen\n",this.getVertexBufferObjectManager());
         credits.setScale(0.6f);
         endScene.attachChild(gameOverText);
         endScene.attachChild(credits);
         mainScene.registerUpdateHandler(new IUpdateHandler() {
             @Override
             public void onUpdate(float pSecondsElapsed) {
                 for (Enemy e : removeList) {
                     enemies.remove(e);
                     physicsWorld.destroyBody(e.body);
                     mainScene.unregisterTouchArea(e);
                     e.detachChildren();
                     e.detachSelf();
                 }
                 removeList.clear();
 
                 scoreText.setText(String.valueOf(score));
             }
 
 			@Override
 			public void reset() {
 				// TODO Auto-generated method stub
 
 			}
         });
 
         new MenuButton(650f, 350f,
                 "menu_exit_button.png", this.getVertexBufferObjectManager(),
                 endScene,
                 new MenuButton.IAction() {
                     @Override
                     public void run() {
                         gameoverTheme.stop();
                         loadMainScene();
                         populateMainScene();
                         mEngine.setScene(menuScene);
                         currentScene = SceneType.MENU;
                     }
                 });
 	}
 
 	private void populateMenuScene() {
     	menuTheme.play();
     	Sprite bg = new Sprite(0, 0,
                 backgroundTextureRegion,
                 this.mEngine.getVertexBufferObjectManager());
         MainActivity.menuScene.attachChild(bg);
 
         ITextureRegion cloudTextureRegion = loadTexture("cloudbanner.png",350,171,0,0);
         Sprite cloud = new Sprite(190f, 0,
                 cloudTextureRegion,this.mEngine.getVertexBufferObjectManager());
         MainActivity.menuScene.attachChild(cloud);
         new MenuButton(70f, 350f,
                 "menu_newgame_button.png", this.getVertexBufferObjectManager(),
                 menuScene,
                 new MenuButton.IAction() {
                     @Override
                     public void run() {
                     	menuTheme.stop();
                     	theme.play();
                         mEngine.setScene(mainScene);
                         currentScene = SceneType.MAIN;
                     }
                 });
         new MenuButton(650f, 350f,
                 "menu_exit_button.png", this.getVertexBufferObjectManager(),
                 menuScene,
                 new MenuButton.IAction() {
                     @Override
                     public void run() {
                         mEngine.setScene(mainScene);
                         System.exit(0);
                     }
                 });
         menuScene.sortChildren();
     }
 
     public int getScore() {
     	return this.score;
     }
 
 
 /* Initialize Splash Screen ***************************************************/
 
     private void initSplashScene() {
         splashScene = new Scene();
         splashScene.setBackgroundEnabled(false);
 
         splashScene.attachChild(splashSprite);
     }
 }
