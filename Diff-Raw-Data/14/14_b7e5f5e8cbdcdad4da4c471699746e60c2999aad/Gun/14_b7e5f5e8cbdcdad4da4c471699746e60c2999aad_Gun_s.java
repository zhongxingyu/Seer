 package com.example.ship.game;
 
 import android.graphics.PointF;
 import com.example.ship.R;
 import com.example.ship.RootActivity;
 import com.example.ship.level.PausableTimerHandler;
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 import static android.util.FloatMath.cos;
 import static android.util.FloatMath.sin;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Denis
  * Date: 5/5/13
  * Time: 3:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Gun {
 
     public  static final float GRAD_TO_RADIAN_KOEF = 3.1415f / 180f;
     private static final float ROTATION_VELOCITY   = 0.4f;
     private static final float ROTATION_MAX_ANGLE  = 40.0f;
     private static final float GUN_PART_ON_SCENE   = 0.9f;
     private static final float BULLET_RELATIVE_START_POINT = 1.0f;
     private static final int DEFAULT_DAMAGE = 100;
     private static final float DEFAULT_PERSPECTIVE_SCALE = 0.5f;
     private static float FIRE_DELAY = 1.5f;
     private float perspectiveScale;
     private boolean rotateLeft;
     private boolean rotationEnabled;
     private Sprite gunSprite;
     private Sprite gunMask;
     private RootActivity activity;
     private PausableTimerHandler fireTimerHandler;
     private boolean fireAvailable = true;
     private int damage = DEFAULT_DAMAGE;
     private int reloadProgress   = ProgressBar.FULL_PROGRESS;
     private boolean autoFire = true;
 
     public Gun(RootActivity activity) {
         autoFire = RootActivity.DEBUG_GAME_SCENE;
         this.activity = activity;
         rotationEnabled = false;
         ITextureRegion gunTexture = activity.getResourceManager().getLoadedTextureRegion(R.drawable.gun);
         ITextureRegion gunMaskTexture = activity.getResourceManager().getLoadedTextureRegion(R.drawable.gunmask);
 
         ZoomCamera camera = activity.getCamera();
 
         PointF gunPosition = new PointF( camera.getCenterX() - gunTexture.getWidth()  * 0.5f
                                        , camera.getYMax() - gunTexture.getHeight() * GUN_PART_ON_SCENE );
 
         perspectiveScale = DEFAULT_PERSPECTIVE_SCALE;
         fireAvailable = true;
         createTimer();
         activity.getEngine().registerUpdateHandler(fireTimerHandler);
 
         gunMask = new Sprite( gunPosition.x
                             , gunPosition.y
                             , gunMaskTexture
                             , activity.getEngine().getVertexBufferObjectManager());
         gunSprite = new Sprite( 0f
                               , 0f
                               , gunTexture
                               , activity.getEngine().getVertexBufferObjectManager()) {
             @Override
             protected void onManagedUpdate(float pSecondsElapsed) {
                 super.onManagedUpdate(pSecondsElapsed);
 
                 if (autoFire) {
                     fire();
                 }
 
                 if (!rotationEnabled) {
                     return;
                 }
 
                 if (rotateLeft) {
                     if (gunSprite.getRotation() > -ROTATION_MAX_ANGLE) {
                         gunSprite.setRotation(gunSprite.getRotation() - ROTATION_VELOCITY);
                     }
                 } else {
                     if (gunSprite.getRotation() < ROTATION_MAX_ANGLE) {
                         gunSprite.setRotation(gunSprite.getRotation() + ROTATION_VELOCITY);
                     }
                 }
             }
 
         };
 
         gunMask.attachChild( gunSprite );
         gunMask.setScaleCenterY( gunMask.getHeight() );
         gunMask.setScaleY( perspectiveScale );
         gunSprite.setRotationCenter( gunSprite.getWidth() * 0.5f, gunSprite.getHeight() );
     }
 
     public void rotateLeft() {
         rotateLeft = true;
         rotationEnabled = true;
     }
 
     public void rotateRight() {
         rotateLeft = false;
         rotationEnabled = true;
     }
 
     public void stopRotate() {
         rotationEnabled = false;
     }
 
     public PointF getShootStartPoint() {
         float gunAngle = gunSprite.getRotation();
         PointF beginGunPoint = new PointF( gunMask.getX() + gunSprite.getWidth() * 0.5f
                                          , gunMask.getY() + gunSprite.getHeight());
 
         float bulletStartPointDistance = BULLET_RELATIVE_START_POINT * gunSprite.getHeight();
         PointF shootStartPoint =
                 new PointF( beginGunPoint.x + bulletStartPointDistance * sin( gunAngle * GRAD_TO_RADIAN_KOEF )
                           , beginGunPoint.y
                             - perspectiveScale * bulletStartPointDistance * cos( gunAngle * GRAD_TO_RADIAN_KOEF ) );
         return shootStartPoint;
     }
 
     public float getGunAngle() {
         float gunClearAngle = gunSprite.getRotation();
         float gunRearSightDistance = gunSprite.getHeight();
         PointF distortedRotationShift =
                 new PointF( gunRearSightDistance * sin( ( float ) Math.toRadians( gunClearAngle ) )
                           , perspectiveScale * gunRearSightDistance * cos( ( float ) Math.toRadians( gunClearAngle ) ) );
         double gunDistortedAngle = Math.atan2( distortedRotationShift.x , distortedRotationShift.y );
         return (float) Math.toDegrees( gunDistortedAngle );
     }
 
     public Sprite getSprite() {
         return gunMask;
     }
 
     public void fire() {
         if (fireAvailable) {
             Torpedo torpedo = new Torpedo( activity
                                          , this.getShootStartPoint()
                                          , this.getGunAngle());
             activity.getSceneSwitcher().getGameScene().attachSpriteToLayer( torpedo
                                                                           , GameScene.LAYER_TORPEDO);
             fireAvailable = false;
             reloadProgress  = 0;
             activity.getSceneSwitcher().getGameScene().getGameGUD().updateProgressBar(reloadProgress );
             fireTimerHandler.reset();
         }
     }
 
     public void setPerspectiveScale( float scale ) {
         perspectiveScale = scale;
     }
 
     public int getDamage() {
         return damage;
     }
 
     public float getPerspectiveScale() {
         return perspectiveScale;
     }
 
     public void pauseFireTimer() {
         fireTimerHandler.pause();
     }
 
     public void resumeFireTimer() {
         fireTimerHandler.resume();
     }
 
     public void setFireDelay(float fireDelay) {
         FIRE_DELAY = fireDelay;
     }
 
     public float getFireDelay() {
         return FIRE_DELAY;
     }
 
     public void setAutoFire(boolean autoFire) {
         this.autoFire = autoFire;
     }
 
     private void createTimer() {
         fireTimerHandler = new PausableTimerHandler(FIRE_DELAY / ProgressBar.FULL_PROGRESS, new ITimerCallback() {
             @Override
             public void onTimePassed(final TimerHandler timerHandler) {
                 if (reloadProgress  == ProgressBar.FULL_PROGRESS - 1) {
                     reloadProgress ++;
                     activity.getSceneSwitcher().getGameScene().getGameGUD().updateProgressBar(reloadProgress );
                     fireAvailable = true;
                 }
                 if (reloadProgress  < ProgressBar.FULL_PROGRESS - 1) {
                     reloadProgress ++;
                     activity.getSceneSwitcher().getGameScene().getGameGUD().updateProgressBar(reloadProgress );
                     fireTimerHandler.reset();
                 }
             }
         });
     }
 }
