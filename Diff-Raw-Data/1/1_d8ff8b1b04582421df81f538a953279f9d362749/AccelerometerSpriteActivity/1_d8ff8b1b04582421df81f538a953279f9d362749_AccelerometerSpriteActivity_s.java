 package com.circularvale.wallpaperegs;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.TextureRegion;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 
 public class AccelerometerSpriteActivity extends BaseLiveWallpaperService implements SensorEventListener {
 	 
     // ===========================================================
     // Constants
     // ===========================================================
 
 	private static final float CAMERA_WIDTH = 480;
 	private static final float CAMERA_HEIGHT = 720;
 
     // ===========================================================
     // Fields
     // ===========================================================
 
     private Sprite sprite;
     private BitmapTextureAtlas mTexture;
     private TextureRegion mSpriteTextureRegion;
     private SensorManager sensorManager;
 
     private int accellerometerSpeedX;
     private int accellerometerSpeedY;
     private float sX, sY; // Sprite coordinates
 
     // ===========================================================
     // Constructors
     // ===========================================================
 
     // ===========================================================
     // Getter & Setter
     // ===========================================================
 
     // ===========================================================
     // Methods for/from SuperClass/Interfaces
     // ===========================================================
 
    
     public EngineOptions onCreateEngineOptions() {
 		final Camera camera = new Camera(0, 0, AccelerometerSpriteActivity.CAMERA_WIDTH, AccelerometerSpriteActivity.CAMERA_HEIGHT);
 
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(AccelerometerSpriteActivity.CAMERA_WIDTH, AccelerometerSpriteActivity.CAMERA_HEIGHT), camera);
 	}
 
     public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws Exception {
     	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
     	
         this.mTexture = new BitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         this.mSpriteTextureRegion = BitmapTextureAtlasTextureRegionFactory
                         .createFromAsset(this.mTexture, this, "face_box.png", 0, 0);
 
         this.mEngine.getTextureManager().loadTexture(this.mTexture);
         pOnCreateResourcesCallback.onCreateResourcesFinished();
     }
 
     @SuppressWarnings("static-access")
     public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws Exception {
             sensorManager = (SensorManager) this
                             .getSystemService(this.SENSOR_SERVICE);
             sensorManager.registerListener(this, sensorManager
                             .getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                             sensorManager.SENSOR_DELAY_GAME);
 
             this.mEngine.registerUpdateHandler(new FPSLogger());
             this.mEngine.registerUpdateHandler(new IUpdateHandler() {
                     public void onUpdate(float pSecondsElapsed) {
                             updateSpritePosition();
                     }
 
                     public void reset() {
                             // TODO Auto-generated method stub
                     }
             });
 
             final Scene scene = new Scene();
             scene.setBackground(new Background(0.2f, 0.3f, 0.4f));
 
             sX = (CAMERA_WIDTH - this.mSpriteTextureRegion.getWidth()) / 2.0f;
             sY = (CAMERA_HEIGHT - this.mSpriteTextureRegion.getHeight()) / 2.0f;
 
             sprite = new Sprite(sX, sY, this.mSpriteTextureRegion, this.getVertexBufferObjectManager());
             sprite.setScale(1);
             scene.attachChild(sprite);
 
             pOnCreateSceneCallback.onCreateSceneFinished(scene);
     }
 
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 		pOnPopulateSceneCallback.onPopulateSceneFinished();
 		
 	}
 
     public void onSensorChanged(SensorEvent event) {
             synchronized (this) {
                     switch (event.sensor.getType()) {
                     case Sensor.TYPE_ACCELEROMETER:
                             accellerometerSpeedX = (int) event.values[0];
                             accellerometerSpeedY = (int) event.values[1];
                             break;
                     }
             }
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
             //
     }
 
     // ===========================================================
     // Methods
     // ===========================================================
 
     private void updateSpritePosition() {
             if ((accellerometerSpeedX != 0) || (accellerometerSpeedY != 0)) {
                     // Set the Boundary limits
                     float tL = 0;
                     float lL = 0;
                     float rL = CAMERA_WIDTH - sprite.getWidth();
                     float bL = CAMERA_HEIGHT - sprite.getHeight();
 
                     // Calculate New X,Y Coordinates within Limits
                     if (sX >= lL)
                             sX += accellerometerSpeedX;
                     else
                             sX = lL;
                     if (sX <= rL)
                             sX += accellerometerSpeedX;
                     else
                             sX = rL;
                     if (sY >= tL)
                             sY += accellerometerSpeedY;
                     else
                             sY = tL;
                     if (sY <= bL)
                             sY += accellerometerSpeedY;
                     else
                             sY = bL;
 
                     // Double Check That New X,Y Coordinates are within Limits
                     if (sX < lL)
                             sX = lL;
                     else if (sX > rL)
                             sX = rL;
                     if (sY < tL)
                             sY = tL;
                     else if (sY > bL)
                             sY = bL;
 
                     sprite.setPosition(sX, sY);
             }
     }
 
     // ===========================================================
     // Inner and Anonymous Classes
     // ===========================================================
 }
