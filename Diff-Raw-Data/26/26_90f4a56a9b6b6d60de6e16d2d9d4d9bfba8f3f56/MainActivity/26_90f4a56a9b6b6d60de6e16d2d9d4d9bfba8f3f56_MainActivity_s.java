 package com.example.MultiBalance;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.KeyEvent;
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.music.MusicFactory;
 import org.anddev.andengine.audio.sound.Sound;
 import org.anddev.andengine.audio.sound.SoundFactory;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.modifier.MoveModifier;
 import org.anddev.andengine.entity.modifier.MoveXModifier;
 import org.anddev.andengine.entity.scene.CameraScene;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
 import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 
 public class MainActivity extends BaseGameActivity implements Scene.IOnSceneTouchListener, SensorEventListener {
 
     private Camera mCamera;
     private Scene mMainScene;
 
     private BitmapTextureAtlas mBitmapTextureAtlas;
     private TextureRegion mPlayerTextureRegion;
     private TextureRegion mBarTextureRegion;
     //private Sprite player;
     private Sprite bar;
 
     private TextureRegion mTargetTextureRegion;
 
     //private LinkedList projectileLL;
     //private LinkedList projectilesToBeAdded;
     //private TextureRegion mProjectileTextureRegion;
     private TextureRegion mPausedTextureRegion;
     private CameraScene mPauseScene;
 
     private CameraScene mResultScene;
     private boolean runningFlag = false;
     private boolean pauseFlag = false;
     private BitmapTextureAtlas mFontTexture;
     private Font mFont;
     private ChangeableText score;
     private int hitCount;
     private final int maxScore = 10;
     private Sprite winSprite;
     private Sprite failSprite;
     private TextureRegion mWinTextureRegion;
     private TextureRegion mFailTextureRegion;
 
     private float rotation;
 
     private SensorManager sensorManager;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //setContentView(R.layout.main);
     }
 
     @Override
     public Engine onLoadEngine() {
 
         final Display display = getWindowManager().getDefaultDisplay();
         int cameraWidth = display.getWidth();
         int cameraHeight = display.getHeight();
 
         mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
 
         return new Engine(new EngineOptions(true, EngineOptions.ScreenOrientation.LANDSCAPE,
                 new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera)
                 .setNeedsMusic(true).setNeedsSound(true));
     }
 
     @Override
     public void onLoadResources() {
         mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512,
                 TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
         mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(this.mBitmapTextureAtlas, this, "Target.png",
                         128, 0);
 
         mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(this.mBitmapTextureAtlas, this, "Player.png",
                         0,0);
 
         mBarTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(this.mBitmapTextureAtlas, this, "bar.png",
                         0, 0);
         mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
 
        // mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory
        //         .createFromAsset(this.mBitmapTextureAtlas, this,
       //                  "Projectile.png", 64, 0);
 
         mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(this.mBitmapTextureAtlas, this, "paused.png",
                         0, 64);
 
         mWinTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(this.mBitmapTextureAtlas, this, "win.png", 0,
                         128);
         mFailTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(this.mBitmapTextureAtlas, this, "fail.png", 0,
                         256);
 
         mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
         mFontTexture = new BitmapTextureAtlas(256, 256,
                 TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT,
                 Typeface.BOLD), 40, true, Color.BLACK);
         mEngine.getTextureManager().loadTexture(mFontTexture);
         mEngine.getFontManager().loadFont(mFont);
     }
 
     @Override
     public Scene onLoadScene() {
         mEngine.registerUpdateHandler(new FPSLogger());
 
         final int barX = (int) ((mCamera.getWidth() - mBarTextureRegion.getWidth()) / 2);
         final int barY = (int) ((mCamera.getHeight() - mBarTextureRegion.getHeight()) / 2);
 
         //player = new Sprite(PlayerX, PlayerY, mPlayerTextureRegion);
         //player.setScale(2);
 
         bar = new Sprite(barX, barY, mBarTextureRegion);
         bar.setScale(4,2.5f);
 
 
         mMainScene = new Scene();
         mMainScene
                 .setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
 
         //mMainScene.attachChild(player);
         mMainScene.attachChild(bar);
 
         mMainScene.registerUpdateHandler(detect);
 
         //projectileLL = new LinkedList();
         //projectilesToBeAdded = new LinkedList();
 
         mMainScene.setOnSceneTouchListener(this);
 
         mPauseScene = new CameraScene(mCamera);
         final int x = (int) (mCamera.getWidth() / 2 - mPausedTextureRegion
                 .getWidth() / 2);
         final int y = (int) (mCamera.getHeight() / 2 - mPausedTextureRegion
                 .getHeight() / 2);
         final Sprite pausedSprite = new Sprite(x, y, mPausedTextureRegion);
         mPauseScene.attachChild(pausedSprite);
         mPauseScene.setBackgroundEnabled(false);
 
         mResultScene = new CameraScene(mCamera);
         winSprite = new Sprite(x, y, mWinTextureRegion);
         failSprite = new Sprite(x, y, mFailTextureRegion);
         mResultScene.attachChild(winSprite);
         mResultScene.attachChild(failSprite);
         mResultScene.setBackgroundEnabled(false);
 
         winSprite.setVisible(false);
         failSprite.setVisible(false);
         score = new ChangeableText(0, 0, mFont, "ButtsButtsButts");
         score.setPosition(5,5);//mCamera.getWidth() - score.getWidth() - 5, 5);
         score.setWidth(2000);
         mMainScene.attachChild(score);
 
         sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
         sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), 1);
 
 
         return mMainScene;
     }
 
     @Override
     public void onLoadComplete() {
 
     }
 
     IUpdateHandler detect = new IUpdateHandler() {
         @Override
         public void onUpdate(float v) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public void reset() {
         }
     };
 
 
     @Override
     public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 
         if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
             final float touchX = pSceneTouchEvent.getX();
             final float touchY = pSceneTouchEvent.getY();
 
             return true;
         }
         return false;
     }
 
     public void restart() {
 
         runOnUpdateThread(new Runnable() {
 
             @Override
             public void run() {
                 mMainScene.detachChildren();
                //mMainScene.attachChild(player, 0);
                 mMainScene.attachChild(score);
             }
         });
 
         hitCount = 0;
         score.setText(String.valueOf(hitCount));
        //projectileLL.clear();
        //projectilesToBeAdded.clear();
     }
 
     public void fail() {
         if (mEngine.isRunning()) {
             winSprite.setVisible(false);
             failSprite.setVisible(true);
             mMainScene.setChildScene(mResultScene, false, true, true);
             mEngine.stop();
         }
     }
 
     public void win() {
         if (mEngine.isRunning()) {
             failSprite.setVisible(false);
             winSprite.setVisible(true);
             mMainScene.setChildScene(mResultScene, false, true, true);
             mEngine.stop();
         }
     }
 
 
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR)
             return;
        float mSensorX = 0, mSensorY = 0, mSensorZ = 0;
         rotation -= event.values[1];//it's dumb, just go with it.
         score.setText(""+rotation);
         bar.setRotation(rotation);
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
