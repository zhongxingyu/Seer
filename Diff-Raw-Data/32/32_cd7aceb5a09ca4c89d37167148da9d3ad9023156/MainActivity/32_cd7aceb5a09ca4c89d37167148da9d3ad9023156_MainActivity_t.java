 package at.blooo;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.FixedStepEngine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.Entity;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.sensor.acceleration.AccelerationData;
 import org.andengine.input.sensor.acceleration.IAccelerationListener;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.adt.color.Color;
 
 import android.view.KeyEvent;
 import at.blooo.minigame.MiniGameManager;
 import at.blooo.tetris.Tetris;
 
 public class MainActivity extends BaseGameActivity implements
     IAccelerationListener {
 
   public static final int WIDTH = 1280;
   public static final int HEIGHT = 720;
 
   public static final int FIGURE_SIZE = 5;
 
   float mVerTime = 0.0f;
   float mHorTime = 0.0f;
 
   Camera mCamera;
   ITextureRegion mBlock;
   ITextureRegion mTetrisBackground;
   Scene mScene;
   int mBlocksize = 64;
 
   float xVal = 0;
   float yVal = 0;
   float mVerSpeed = 0.5f;
   float mHorSpeed = 0.2f;
 
   Tetris mTetris;
   Entity mMiniGameFrame;
 
   @Override
   public void onResumeGame() {
     super.onResumeGame();
     this.enableAccelerationSensor(this);
   }
 
   @Override
   public void onPauseGame() {
     super.onPauseGame();
     this.disableAccelerationSensor();
   }
 
   @Override
   public EngineOptions onCreateEngineOptions() {
     mCamera = new Camera(0, 0, WIDTH, HEIGHT);
 
     EngineOptions engineOptions = new EngineOptions(true,
         ScreenOrientation.LANDSCAPE_FIXED, new FillResolutionPolicy(), mCamera);
 
     return engineOptions;
 
   }
 
   @Override
   public Engine onCreateEngine(EngineOptions eo) {
 
     return new FixedStepEngine(eo, 60);
   }
 
   @Override
   public void onCreateResources(
       OnCreateResourcesCallback pOnCreateResourcesCallback) {
     BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
     BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
         mEngine.getTextureManager(), 1024, 1024, BitmapTextureFormat.RGBA_8888,
         TextureOptions.BILINEAR);
 
     this.mBlock = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
         mBuildableBitmapTextureAtlas, this, "blocks.png");
 
     BuildableBitmapTextureAtlas tetrisBgTextureAtlas = new BuildableBitmapTextureAtlas(
         mEngine.getTextureManager(), 64, 64, TextureOptions.REPEATING_BILINEAR);
 
     mTetrisBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
         tetrisBgTextureAtlas, this, "tetrisBgTile.png");
 
     try {
       mBuildableBitmapTextureAtlas
           .build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
               0, 1, 1));
       tetrisBgTextureAtlas
           .build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
               0, 0, 0));
     } catch (TextureAtlasBuilderException e) {
       e.printStackTrace();
     }
 
     mBuildableBitmapTextureAtlas.load();
     tetrisBgTextureAtlas.load();
 
     pOnCreateResourcesCallback.onCreateResourcesFinished();
 
   }
 
   @Override
   public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
 
     mScene = new Scene();
     mMiniGameFrame = new Entity();
     mMiniGameFrame.setSize(WIDTH / 2, HEIGHT);
     mMiniGameFrame.setPosition(WIDTH / 2 + WIDTH / 4, HEIGHT / 2);
     mScene.attachChild(mMiniGameFrame);
 
     MiniGameManager mgm = new MiniGameManager(this, mTetris,
         mMiniGameFrame);
 
     mTetris = new Tetris(this, mgm);
     mTetris.initField();
 
     mScene.attachChild(mTetris);
     mTetris.setPosition(0, 0);
     mTetris.setScale(0.55f);
 
     pOnCreateSceneCallback.onCreateSceneFinished(mScene);
 
   }
 
   @Override
   public void onPopulateScene(Scene pScene,
       OnPopulateSceneCallback pOnPopulateSceneCallback) {
 
     mScene.setTouchAreaBindingOnActionDownEnabled(true);
 
     mScene.registerUpdateHandler(new IUpdateHandler() {
 
       @Override
       public void onUpdate(float pSecondsElapsed) {
         MainActivity.this.mVerTime += pSecondsElapsed;
         MainActivity.this.mHorTime += pSecondsElapsed;
 
         if (MainActivity.this.mVerTime >= mVerSpeed) {
           mTetris.moveDown();
           MainActivity.this.mVerTime = 0;
         }
         
         if (MainActivity.this.mHorTime >= mHorSpeed) {
           int val = (int) (MainActivity.this.xVal);
 
           if (val < 0) {
             for (int i = val; i < 0; i++)
               mTetris.moveLeft();
           }
 
           if (val > 0) {
             for (int i = val; i > 0; i--)
               mTetris.moveRight();
           }
           MainActivity.this.mHorTime = 0;
         }
         
       }
 
       @Override
       public void reset() {
 
       }
 
     });
 
     pOnPopulateSceneCallback.onPopulateSceneFinished();
   }
 
   @Override
   public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
   }
 
   @Override
   public void onAccelerationChanged(AccelerationData pAccelerationData) {
     this.yVal = pAccelerationData.getY();
     this.xVal = pAccelerationData.getX();
   }
 
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
 
     if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
       mTetris.moveLeft();
       return false;
     }
     if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
       mTetris.moveRight();
       return false;
     }
     if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
       mTetris.rotateRight();
       return false;
     }
     if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
       mTetris.moveToBottom();
       return false;
     }
 
     return super.onKeyDown(keyCode, event);
   }
 
   public void detachFromScene(final Entity e) {
     runOnUpdateThread(new Runnable() {
 
       @Override
       public void run() {
         e.detachSelf();
       }
     });
   }
 
   public Sprite createBlock(int x, int y) {
     Sprite s = new Sprite(x, y, mBlock, mEngine.getVertexBufferObjectManager());
     return s;
   }
 
   public ITextureRegion createTetrisBG() {
     
     return mTetrisBackground;
   }
 
   public void registerTouchArea(final Entity e) {
 
         mScene.registerTouchArea(e);
 
   }
 }
