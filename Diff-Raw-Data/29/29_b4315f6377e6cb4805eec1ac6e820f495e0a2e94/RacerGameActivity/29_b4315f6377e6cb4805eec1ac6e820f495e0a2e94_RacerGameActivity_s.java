 package org.anddev.andengine.examples.game.racer;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.OnScreenControlListener;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.layer.ILayer;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.sprite.TiledSprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.MathUtils;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class RacerGameActivity extends BaseGameActivity {
   private static final int CAMERA_WIDTH = 768;
   private static final int CAMERA_HEIGHT = 384;
 
   private static final int INSET = 128;
 
   private static final int LAYER_RACERTRACK = 0;
   private static final int LAYER_BORDERS = LAYER_RACERTRACK + 1;
   private static final int LAYER_CARS = LAYER_BORDERS + 1;
 
   private Camera mCamera;
 
   private PhysicsWorld mPhysicsWorld;
 
   private Texture mVehiclesTexture;
   private TiledTextureRegion mVehiclesTextureRegion;
 
   private Texture mRacetrackTexture;
   private TextureRegion mRacetrackStraightTextureRegion;
   private TextureRegion mRacetrackCurveTextureRegion;
 
   private Texture mOnScreenControlTexture;
   private TextureRegion mOnScreenControlBaseTextureRegion;
   private TextureRegion mOnScreenControlKnobTextureRegion;
 
   private Body mCarBody;
   private TiledSprite mCar;
 
   @Override
   public Engine onLoadEngine() {
     mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
     return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
         new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera));
   }
 
   @Override
   public void onLoadResources() {
    TextureRegionFactory.setAssetBasePath("gfx/");

     mVehiclesTexture = new Texture(128, 16, TextureOptions.BILINEAR);
     mVehiclesTextureRegion = TextureRegionFactory.createTiledFromAsset(
        mVehiclesTexture, this, "vehicles.png", 0, 0, 6, 1);
 
     mRacetrackTexture = new Texture(128, 256, TextureOptions.REPEATING_BILINEAR);
     mRacetrackStraightTextureRegion = TextureRegionFactory.createFromAsset(
        mRacetrackTexture, this, "racetrack_straight.png", 0, 0);
     mRacetrackCurveTextureRegion = TextureRegionFactory.createFromAsset(
        mRacetrackTexture, this, "racetrack_curve.png", 0, 128);
 
     mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR);
     mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(
        mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
     mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(
        mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
 
     mEngine.getTextureManager().loadTextures(mVehiclesTexture, mRacetrackTexture,
         mOnScreenControlTexture);
   }
 
   @Override
   public Scene onLoadScene() {
     mEngine.registerPostFrameHandler(new FPSLogger());
 
     final Scene scene = new Scene(3);
     scene.setBackground(new ColorBackground(0, 0, 0));
 
     mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
 
     initRacetrack(scene);
     initRacetrackBorders(scene);
     initCar(scene);
     initOnScreenControls(scene);
 
     scene.registerPreFrameHandler(mPhysicsWorld);
 
     return scene;
   }
 
   private void initOnScreenControls(final Scene scene) {
     final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0,
         CAMERA_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight(), mCamera,
         mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion,
         0.1f, new OnScreenControlListener() {
           private final Vector2 mTemp = new Vector2();
 
           @Override
           public void onControlChange(
               final BaseOnScreenControl pBaseOnScreenControl,
               final float pValueX, final float pValueY) {
             mTemp.set(pValueX * 200, pValueY * 200);
             mCarBody.setLinearVelocity(mTemp);
             mCar.setRotation(MathUtils.radToDeg(
                 (float)Math.atan2(-pValueX, pValueY)));
           }
         });
 
     analogOnScreenControl.getControlBase().setAlpha(0.5f);
     analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
     analogOnScreenControl.getControlBase().setScale(1.25f);
     analogOnScreenControl.getControlKnob().setScale(1.25f);
     analogOnScreenControl.refreshControlKnobPosition();
 
     scene.setChildScene(analogOnScreenControl);
   }
 
   private void initCar(final Scene scene) {
     mCar = new TiledSprite(20, 20, 32, 32, mVehiclesTextureRegion);
     mCar.setCurrentTileIndex(0);
 
     final FixtureDef carFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f,
         0.5f);
 
     mCarBody = PhysicsFactory.createBoxBody(mPhysicsWorld, mCar,
         BodyType.DynamicBody, carFixtureDef);
     mCar.setUpdatePhysics(false);
 
     scene.getLayer(LAYER_CARS).addEntity(mCar);
     mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mCar, mCarBody,
         true, false, true, false));
   }
 
   private void initRacetrack(final Scene scene) {
     final ILayer racetrackLayer = scene.getLayer(LAYER_RACERTRACK);
 
     // straights
     {
       final TextureRegion racetrackHorizontalStraightTextureRegion =
           mRacetrackStraightTextureRegion.clone();
       racetrackHorizontalStraightTextureRegion.setWidth(512);
 
       final TextureRegion racetrackVerticalStraightTextureRegion =
           mRacetrackStraightTextureRegion;
 
       // top straight
       racetrackLayer.addEntity(new Sprite(INSET, 0,
           racetrackHorizontalStraightTextureRegion));
 
       // bottom straight
       racetrackLayer.addEntity(new Sprite(INSET, CAMERA_HEIGHT - INSET,
           racetrackHorizontalStraightTextureRegion));
 
       // left straight
       final Sprite leftVerticalStraight = new Sprite(0, INSET,
           racetrackVerticalStraightTextureRegion);
       leftVerticalStraight.setRotation(90);
       racetrackLayer.addEntity(leftVerticalStraight);
 
       // right straight
       final Sprite rightVerticalStraight = new Sprite(CAMERA_WIDTH - INSET, INSET,
           racetrackVerticalStraightTextureRegion);
       rightVerticalStraight.setRotation(90);
       racetrackLayer.addEntity(rightVerticalStraight);
     }
 
     // edges
     {
       final TextureRegion racetrackCurveTextureRegion = mRacetrackCurveTextureRegion;
 
       // upper left
       final Sprite upperLeftCurve = new Sprite(0, 0, racetrackCurveTextureRegion);
       upperLeftCurve.setRotation(90);
       racetrackLayer.addEntity(upperLeftCurve);
 
       // upper right
       final Sprite upperRightCurve = new Sprite(CAMERA_WIDTH - INSET, 0,
           racetrackCurveTextureRegion);
       upperRightCurve.setRotation(180);
       racetrackLayer.addEntity(upperRightCurve);
 
       // lower right
       final Sprite lowerRigthCurve = new Sprite(CAMERA_WIDTH - INSET,
           CAMERA_HEIGHT - INSET, racetrackCurveTextureRegion);
       lowerRigthCurve.setRotation(270);
       racetrackLayer.addEntity(lowerRigthCurve);
 
       // lower left
       final Sprite lowerLeftCurve = new Sprite(0, CAMERA_HEIGHT - INSET,
           racetrackCurveTextureRegion);
       racetrackLayer.addEntity(lowerLeftCurve);
     }
   }
 
   private void initRacetrackBorders(final Scene scene) {
     final Shape bottomOuter = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
     final Shape topOuter = new Rectangle(0, 0, CAMERA_WIDTH, 2);
     final Shape leftOuter = new Rectangle(0, 0, 2, CAMERA_WIDTH);
     final Shape rightOuter = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
 
     final Shape bottomInner = new Rectangle(INSET, CAMERA_HEIGHT - 2 - INSET,
         CAMERA_WIDTH - 2 * INSET, 2);
     final Shape topInner = new Rectangle(INSET, INSET, CAMERA_WIDTH - 2 * INSET, 2);
     final Shape leftInner = new Rectangle(INSET, INSET, 2,
         CAMERA_HEIGHT - 2 * INSET);
     final Shape rightInner = new Rectangle(CAMERA_WIDTH - 2 - INSET, INSET,
         2, CAMERA_HEIGHT - 2 * INSET);
 
     final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f,
         0.5f);
 
     PhysicsFactory.createBoxBody(mPhysicsWorld, bottomOuter,
         BodyType.StaticBody, wallFixtureDef);
     PhysicsFactory.createBoxBody(mPhysicsWorld, topOuter,
         BodyType.StaticBody, wallFixtureDef);
     PhysicsFactory.createBoxBody(mPhysicsWorld, leftOuter,
         BodyType.StaticBody, wallFixtureDef);
     PhysicsFactory.createBoxBody(mPhysicsWorld, rightOuter,
         BodyType.StaticBody, wallFixtureDef);
 
     PhysicsFactory.createBoxBody(mPhysicsWorld, bottomInner,
         BodyType.StaticBody, wallFixtureDef);
     PhysicsFactory.createBoxBody(mPhysicsWorld, topInner,
         BodyType.StaticBody, wallFixtureDef);
     PhysicsFactory.createBoxBody(mPhysicsWorld, leftInner,
         BodyType.StaticBody, wallFixtureDef);
     PhysicsFactory.createBoxBody(mPhysicsWorld, rightInner,
         BodyType.StaticBody, wallFixtureDef);
 
     final ILayer bottomLayer =  scene.getLayer(LAYER_BORDERS);
     bottomLayer.addEntity(bottomOuter);
     bottomLayer.addEntity(topOuter);
     bottomLayer.addEntity(leftOuter);
     bottomLayer.addEntity(rightOuter);
 
     bottomLayer.addEntity(bottomInner);
     bottomLayer.addEntity(topInner);
     bottomLayer.addEntity(leftInner);
     bottomLayer.addEntity(rightInner);
   }
 
   @Override
   public void onLoadComplete() {
   }
 }
