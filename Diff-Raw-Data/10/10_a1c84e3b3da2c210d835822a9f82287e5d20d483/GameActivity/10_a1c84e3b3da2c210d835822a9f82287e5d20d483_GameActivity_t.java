 package com.angrykings.activities;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 
 import com.angrykings.AngryParallaxBackground;
 import com.angrykings.GameConfig;
 import com.angrykings.GameContext;
 import com.angrykings.GameHUD;
 import com.angrykings.GameStatus;
 import com.angrykings.PhysicsManager;
 import com.angrykings.Player;
 import com.angrykings.R;
 import com.angrykings.ResourceManager;
 import com.angrykings.ServerConnection;
 import com.angrykings.castles.Castle;
 import com.angrykings.maps.BasicMap;
 import com.angrykings.utils.ServerMessage;
 
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.ParallaxBackground;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.PinchZoomDetector;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.andengine.ui.IGameInterface;
 import org.andengine.ui.activity.BaseGameActivity;
 
 public class GameActivity extends BaseGameActivity implements
         IOnSceneTouchListener, ScrollDetector.IScrollDetectorListener,
         PinchZoomDetector.IPinchZoomDetectorListener  {
 
     //
     // Core
     //
 
     protected GameContext gc;
     protected GameHUD hud;
     protected ResourceManager rm;
     protected Scene scene;
     protected ZoomCamera camera;
 
     //
     // Game Objects
     //
 
     protected GameStatus status;
     protected Player me;
     protected Player partner;
     protected BasicMap map;
     protected AngryParallaxBackground parallaxBackground;
     protected boolean isLeft;
     protected int aimX, aimY;
 
     //
     // Navigation Attributes
     //
 
     protected SurfaceScrollDetector scrollDetector;
     protected PinchZoomDetector pinchZoomDetector;
     protected float pinchZoomStartedCameraZoomFactor;
     protected boolean isAiming = true;
 
    private static final String TAG = "GameActivity";
 
 
 
     @Override
     public EngineOptions onCreateEngineOptions() {
         GameContext.clear();
         gc = GameContext.getInstance();
 
         camera = new ZoomCamera(GameConfig.CAMERA_X, GameConfig.CAMERA_Y, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);
 
         camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
         camera.setBounds(
                 GameConfig.CAMERA_MIN_X, GameConfig.CAMERA_MIN_Y,
                 GameConfig.CAMERA_MAX_X, GameConfig.CAMERA_MAX_Y
         );
         camera.setBoundsEnabled(true);
 
         gc.setCamera(camera);
 
         return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                 new RatioResolutionPolicy(GameConfig.CAMERA_WIDTH,
                         GameConfig.CAMERA_HEIGHT), camera);
     }
 
     @Override
     public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
         this.rm = ResourceManager.getInstance();
 
         this.rm.load(this);
 
         pOnCreateResourcesCallback.onCreateResourcesFinished();
     }
 
     @Override
     public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
         gc.setGameActivity(this);
 
         gc.setVboManager(this.getVertexBufferObjectManager());
 
         if (GameConfig.LOG_FPS)
             mEngine.registerUpdateHandler(new FPSLogger());
 
         //
         // initialize the scene
         //
 
         scene = new Scene();
 
         parallaxBackground = new AngryParallaxBackground(0f, 0f, 0f, 0.5f);
         parallaxBackground.attachParallaxEntity(
                 new ParallaxBackground.ParallaxEntity(0f, rm.getBackgroundSprite())
         );
 
         parallaxBackground.attachParallaxEntity(
                 new ParallaxBackground.ParallaxEntity(20f, new Sprite(0, 0, rm.getParallax2(), gc.getVboManager()))
         );
 
         parallaxBackground.attachParallaxEntity(
                 new ParallaxBackground.ParallaxEntity(10f, new Sprite(0, 100, rm.getParallax1(), gc.getVboManager()))
         );
 
 
         scene.setBackground(parallaxBackground);
 
         scene.setOnSceneTouchListener(this);
 
         gc.setScene(scene);
 
         //
         // initialize the physics engine
         //
 
         PhysicsManager.clear();
         PhysicsManager pm = PhysicsManager.getInstance();
         pm.clearEntities();
 
         //
         // initialize the entities
         //
 
         String myName = "";
         String partnerName = "";
 
         Bundle extras = getIntent().getExtras();
 
         if (extras != null) {
             Boolean amILeft = extras.getBoolean("myTurn");
             this.isLeft = amILeft;
 
             myName = extras.getString("username");
             partnerName = extras.getString("partnername");
 
             Log.i(getClass().getName(), "this client is " + (amILeft ? "left" : "right"));
         }
 
         map = new BasicMap();
         scene.attachChild(map);
 
         // This is important because the entity ids are incremented in the order in which we
         // create the entities :(
 
         if(isLeft) {
             this.me = new Player(myName, isLeft);
             this.partner = new Player(partnerName, !isLeft);
         } else {
             this.partner = new Player(partnerName, !isLeft);
             this.me = new Player(myName, isLeft);
         }
 
         //
         // initialize navigation
         //
 
         scrollDetector = new SurfaceScrollDetector(this);
         pinchZoomDetector = new PinchZoomDetector(this);
 
         scene.setOnSceneTouchListener(this);
         scene.setTouchAreaBindingOnActionDownEnabled(true);
 
         //
         // initialize HUD
         //
 
         hud = new GameHUD();
 
         hud.setOnWhiteFlagTouched(new Runnable() {
             @Override
             public void run() {
                 onResignDialog();
             }
         });
 
         gc.setHud(hud);
         gc.getCamera().setHUD(hud);
 
         hud.setLeftPlayerName(isLeft ? myName : partnerName);
         hud.setRightPlayerName(!isLeft ? myName : partnerName);
 
         final Castle leftCastle = isLeft ? me.getCastle() : partner.getCastle();
         final Castle rightCastle = !isLeft ? me.getCastle() : partner.getCastle();
 
         final float initialLeftCastleHeight = leftCastle.getInitialHeight();
         final float initialRightCastleHeight = rightCastle.getInitialHeight();
 
         final boolean left = isLeft;
 
         scene.registerUpdateHandler(new IUpdateHandler() {
             @Override
             public void onUpdate(float pSecondsElapsed) {
 
                 float leftLife = leftCastle.getHeight() / initialLeftCastleHeight;
                 float rightLife = rightCastle.getHeight() / initialRightCastleHeight;
 
                 hud.getLeftLifeBar().setValue(1.0f - ((1.0f - leftLife) * 2.0f));
                 hud.getRightLifeBar().setValue(1.0f - ((1.0f - rightLife) * 2.0f));
 
                 if ((left && leftLife < 0.5f || !left && rightLife < 0.5f) && status != GameStatus.LOST) {
                     onLose();
                 }
             }
 
             @Override
             public void reset() {
 
             }
         });
 
 
         scene.registerUpdateHandler(pm.getPhysicsWorld());
 
         me.getCastle().freeze();
         partner.getCastle().freeze();
 
         scene.registerUpdateHandler(me);
         scene.registerUpdateHandler(partner);
 
         pOnCreateSceneCallback.onCreateSceneFinished(scene);
 
         hud.setStatus(getString(R.string.yourTurn));
 
     }
 
     @Override
     public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 
         pOnPopulateSceneCallback.onPopulateSceneFinished();
 
     }
 
     @Override
     public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
         if (gc.getPhysicsWorld() == null)
             return false;
 
         double cannonDistanceX = pSceneTouchEvent.getX() - this.me.getCannon().getX();
         double cannonDistanceY = pSceneTouchEvent.getY() - this.me.getCannon().getY();
         double cannonDistanceR = Math.sqrt(cannonDistanceX*cannonDistanceX + cannonDistanceY*cannonDistanceY);
 
         if (cannonDistanceR < rm.getAimCircleTexture().getHeight() &&
                 ((isLeft && cannonDistanceX > 0) || (!isLeft && cannonDistanceX < 0))) {
 
             //
             // aim and fire
             //
 
             float x = pSceneTouchEvent.getX();
             float y = pSceneTouchEvent.getY();
 
             int iX = (int) x;
             int iY = (int) y;
 
             if(me.getCannon().pointAt(iX, iY)) {
                 aimX = iX;
                 aimY = iY;
             }
 
             if (pSceneTouchEvent.isActionUp() && status == GameStatus.MY_TURN) {
                Log.i(TAG, "fire");
                 me.handleTurn(aimX, aimY, null);
             }
 
         } else {
 
             //
             // pinch and zoom
             //
 
             if (pSceneTouchEvent.isActionDown()) {
                 scrollDetector.setEnabled(true);
             }
 
             pinchZoomDetector.onTouchEvent(pSceneTouchEvent);
 
             if (pinchZoomDetector.isZooming()) {
                 scrollDetector.setEnabled(false);
             } else {
                 scrollDetector.onTouchEvent(pSceneTouchEvent);
             }
 
         }
 
         return true;
 
     }
 
     @Override
     public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector, TouchEvent pSceneTouchEvent) {
 
         this.pinchZoomStartedCameraZoomFactor = camera.getZoomFactor();
 
     }
 
     @Override
     public void onPinchZoom(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
 
         float factor = pinchZoomStartedCameraZoomFactor * pZoomFactor;
         if (factor > GameConfig.CAMERA_ZOOM_MIN && factor < GameConfig.CAMERA_ZOOM_MAX)
             camera.setZoomFactor(factor);
 
     }
 
     @Override
     public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
         float factor = pinchZoomStartedCameraZoomFactor * pZoomFactor;
         if (factor > GameConfig.CAMERA_ZOOM_MIN && factor < GameConfig.CAMERA_ZOOM_MAX)
             camera.setZoomFactor(factor);
 
     }
 
     @Override
     public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 
         final float zoomFactor = camera.getZoomFactor();
         camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 
     }
 
     @Override
     public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 
         final float zoomFactor = camera.getZoomFactor();
         camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 
     }
 
     @Override
     public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
 
         final float zoomFactor = camera.getZoomFactor();
         camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 
     }
 
     protected void onLose() {
         status = GameStatus.LOST;
         gc.getHud().setStatus(getString(R.string.hasLost));
 
         Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
         intent.putExtra("hasWon", false);
         intent.putExtra("isLeft", GameActivity.this.isLeft);
         intent.putExtra("username", me.getName());
         intent.putExtra("partnername", partner.getName());
         intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 
         startActivity(intent);
     }
 
     protected void onWin() {
 
         this.status = GameStatus.WON;
 
         Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
         intent.putExtra("hasWon", true);
         intent.putExtra("isLeft", GameActivity.this.isLeft);
         intent.putExtra("username", me.getName());
         intent.putExtra("partnername", partner.getName());
         intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 
         startActivity(intent);
 
     }
 
     protected void onResignDialog() {
         new Handler().post(new Runnable() {
             @Override
             public void run() {
                 final Dialog dialog = new Dialog(GameActivity.this);
                 dialog.setContentView(R.layout.quit_dialog);
                 dialog.setCancelable(true);
                 dialog.getWindow().setBackgroundDrawable(
                         new ColorDrawable(android.graphics.Color.TRANSPARENT));
 
                 Button bCancel = (Button) dialog.findViewById(R.id.bCancel);
                 Button bResign = (Button) dialog.findViewById(R.id.bResign);
 
                 bCancel.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         dialog.dismiss();
                     }
                 });
 
                 bResign.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         dialog.dismiss();
                         onResign();
                     }
                 });
                 dialog.show();
             }
         });
     }
 
     protected void onResign() {
 
         Intent intent = new Intent(GameActivity.this, EndGameActivity.class);
 
         intent.putExtra("hasWon", false);
         intent.putExtra("isLeft", GameActivity.this.isLeft);
         intent.putExtra("username", me.getName());
         intent.putExtra("partnername", partner.getName());
         intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 
         startActivity(intent);
 
         hud.setStatus(getString(R.string.youResigned));
 
     }
 
 }
