 package com.angrykings.activities;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import com.angrykings.*;
 import com.angrykings.cannons.Cannonball;
 import com.angrykings.castles.Castle;
 import com.angrykings.maps.BasicMap;
 import com.angrykings.utils.ServerMessage;
 import com.badlogic.gdx.math.Vector2;
 
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.PinchZoomDetector;
 import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.debug.Debug;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 
 /**
  * OnlineGameActivity
  *
  * @author Shivan Taher <zn31415926535@gmail.com>
  * @date 06.06.13
  */
 public class OnlineGameActivity extends BaseGameActivity implements
 		IOnSceneTouchListener, IScrollDetectorListener,
 		IPinchZoomDetectorListener {
 
 	private GameContext gc;
 	private Handler handler;
 	private GameHUD hud;
 	private ResourceManager rm;
     private boolean myTurn;
     private float timeElapsed;
 
 	//
 	// Game Objects
 	//
 
 	private Player me;
 	private Player partner;
 
 	//
 	// Navigation Attributes
 	//
 
 	private SurfaceScrollDetector scrollDetector;
 	private PinchZoomDetector pinchZoomDetector;
 	private float pinchZoomStartedCameraZoomFactor;
 	boolean isAiming = true;
 
     //
     // Camera Positions
     //
 
     private static final int MIDDLE = 0;
     private static final int OWNCANNONBALL = 1;
     private static final int ENEMYCANNONBALL = 2;
     private static final int ENEMYCANNON = 3;
     private static final int OFF = 4;
     private int followCamera = OFF;
 
 	//
 	// Network
 	//
 
 	private ServerConnection serverConnection;
 	int aimX, aimY;
 	boolean isLeft;
 
 	GameStatus status;
 
 	private class AngryKingsMessageHandler extends ServerConnection.OnMessageHandler {
 		@Override
 		public void onMessage(String payload) {
 			try {
 				JSONObject jObj = new JSONObject(payload);
 				if (jObj.getInt("action") == Action.Server.TURN) {
 
 					turn();
 
 				}else if (jObj.getInt("action") == Action.Server.END_TURN) {
 
 					final int x = Integer.parseInt(jObj.getString("x"));
 					final int y = Integer.parseInt(jObj.getString("y"));
 
 					ArrayList<Keyframe> keyframes = null;
 
 					if(jObj.has("keyframes")) {
 						JSONArray jsonKeyframes = jObj.getJSONArray("keyframes");
 						keyframes = new ArrayList<Keyframe>();
 
 						for(int i = 0; i < jsonKeyframes.length(); ++i) {
 							keyframes.add(new Keyframe(jsonKeyframes.getJSONObject(i)));
 						}
 
 						Log.i(getClass().getName(), "received "+keyframes.size()+" keyframes");
 					} else {
 						Log.i(getClass().getName(), "received 0 keyframes");
 					}
 
 					partner.handleTurn(x, y, keyframes);
 
 				} else if (jObj.getInt("action") == Action.Server.YOU_WIN || jObj.getInt("action") == Action.Server.PARTNER_LEFT) {
 
 					won();
 
 				}
 			} catch (JSONException e) {
 
 				Log.w(getClass().getName(), "JSONException: " + e);
 
 			}
 		}
 	}
 
 	private class MyTurnListener implements IPlayerTurnListener {
 		private ArrayList<Keyframe> keyframes;
 
 		public MyTurnListener() {
 			this.keyframes = new ArrayList<Keyframe>();
 		}
 
 		@Override
 		public void onHandleTurn(int x, int y, ArrayList<Keyframe> keyframes) {
 			this.keyframes.clear();
 			status = GameStatus.PARTNER_TURN;
 			me.getCannon().hideAimCircle();
 			partner.getCastle().unfreeze();
             followCamera = OWNCANNONBALL;
             timeElapsed = 0f;
 		}
 
 		@Override
 		public void onEndTurn() {
 			serverConnection.sendTextMessage(ServerMessage.endTurn(aimX, aimY, this.keyframes));
 
 			// TODO: send keyframes
 
 			hud.setStatus(getString(R.string.enemyTurn));
 
 			me.getKing().getSprite().setCurrentTileIndex(0);
 			partner.getKing().getSprite().setCurrentTileIndex(1);
 
 			partner.getKing().jump();
             followCamera = ENEMYCANNON;
             myTurn = false;
             timeElapsed = 0f;
 		}
 
 		@Override
 		public void onKeyframe(float time) {
 			try {
 				// Log.i("Player", "me.onKeyframe: "+time);
 				//Log.i("Player", "cannonball = "+me.getCannonball().getAreaShape().getX()+", "+me.getCannonball().getAreaShape().getY());
 				Keyframe k = new Keyframe(time, me.getCannonball(), partner.getCastle());
 				//Log.i("KEYFRAME", "ball: "+me.getCannonball()+", castle: "+me.getCastle());
 				//Log.i("KEYFRAME", "keyframe: "+k.getCannonballJson().toString());
 				this.keyframes.add(k);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void onUpdate(float dt) {
 
 		}
 	}
 
 	private class PartnerTurnListener implements IPlayerTurnListener {
 		private ArrayList<Keyframe> keyframes;
 		private int keyframeIndex;
 		private float timeElapsed;
 		private float timeElapsedSinceKeyframe;
 
 		@Override
 		public void onHandleTurn(int x, int y, ArrayList<Keyframe> keyframes) {
 			partner.getCannon().pointAt(x, y);
			me.getCastle().unfreeze();
 
 			this.keyframes = keyframes;
 			this.keyframeIndex = 0;
 			this.timeElapsed = 0;
 			this.timeElapsedSinceKeyframe = 0;
 
             followCamera = ENEMYCANNONBALL;
 		}
 
 		@Override
 		public void onEndTurn() {
 			this.keyframes = null;
 			me.getCastle().freeze();
 
 			me.getKing().getSprite().setCurrentTileIndex(0);
 			partner.getKing().getSprite().setCurrentTileIndex(1);
 
 			me.getKing().jump();
             followCamera = MIDDLE;
             myTurn = true;
 
 			if(status != GameStatus.LOST)
 				serverConnection.sendTextMessage(ServerMessage.ready());
 		}
 
 		@Override
 		public void onKeyframe(float time) {
 
 			/*Keyframe k = this.keyframes.get(this.keyframeIndex);
 
 			Log.i("keyframe", "simulate partner key frame "+time+" ("+k.getTimestampSec()+")");
 
 			Cannonball cannonball = partner.getCannonball();
 
 			try {
 				JSONObject cannonballJson = k.getCannonballJson();
 				cannonball.fromJson(cannonballJson);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			this.keyframeIndex++;*/
 
 		}
 
 		@Override
 		public void onUpdate(float dt) {
 			if(this.keyframes == null || this.keyframeIndex >= this.keyframes.size())
 				return;
 
 			this.timeElapsed += dt;
 			this.timeElapsedSinceKeyframe += dt;
 
 			Keyframe k = this.keyframes.get(this.keyframeIndex);
 
 			if(this.timeElapsed > k.getTimestampSec()) {
 				//Log.i("keyframe", "simulate partner key frame "+this.timeElapsed+" ("+k.getTimestampSec()+")");
 
 				Cannonball cannonball = partner.getCannonball();
 
 				try {
 					cannonball.fromJson(k.getCannonballJson());
 					me.getCastle().fromJson(k.getCastleJson());
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 				this.keyframeIndex++;
 				this.timeElapsedSinceKeyframe = 0;
 			} /*else if(this.keyframeIndex > 0) {
 				Keyframe currentKeyframe = this.keyframes.get(this.keyframeIndex - 1);
 				Keyframe nextKeyframe = this.keyframes.get(this.keyframeIndex);
 				Cannonball cannonball = partner.getCannonball();
 
 				try {
 
 					float x0 = (float) currentKeyframe.getCannonballJson().getDouble("x");
 					float y0 = (float) currentKeyframe.getCannonballJson().getDouble("y");
 					float x1 = (float) nextKeyframe.getCannonballJson().getDouble("x");
 					float y1 = (float) nextKeyframe.getCannonballJson().getDouble("y");
 					float angle0 = (float) currentKeyframe.getCannonballJson().getDouble("r");
 					float angle1 = (float) nextKeyframe.getCannonballJson().getDouble("r");
 					float linX0 = (float) currentKeyframe.getCannonballJson().getDouble("l");
 					float linY0 = (float) currentKeyframe.getCannonballJson().getDouble("m");
 					float linX1 = (float) nextKeyframe.getCannonballJson().getDouble("l");
 					float linY1 = (float) nextKeyframe.getCannonballJson().getDouble("m");
 					float angular0 = (float) currentKeyframe.getCannonballJson().getDouble("a");
 					float angular1 = (float) nextKeyframe.getCannonballJson().getDouble("a");
 
 					float deltaT = (float) (nextKeyframe.getTimestampSec() - currentKeyframe.getTimestampSec());
 
 					float t = this.timeElapsedSinceKeyframe / deltaT;
 
 					float x = lerp(x0, x1, t);
 					float y = lerp(y0, y1, t);
 					float angle = lerp(angle0, angle1, t);
 					float linX = lerp(linX0, linX1, t);
 					float linY = lerp(linY0, linY1, t);
 					float angular = lerp(angular0, angular1, t);
 
 					//Log.i("interpolate", "t: "+t+", timeElapsed: "+timeElapsed+", deltaT: "+deltaT);
 
 					cannonball.getBody().setTransform(x, y, angle);
 					cannonball.getBody().setLinearVelocity(linX, linY);
 					cannonball.getBody().setAngularVelocity(angular);
 
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 			}*/
 		}
 
 		private float lerp(float v0, float v1, float t) {
 			return v0+(v1-v0)*t;
 		}
 
 		Vector2 lerp(Vector2 A, Vector2 B, float t ){
 			return A.mul(t).add(B.mul(1f - t));
 		}
 	}
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
         GameContext.clear();
 		gc = GameContext.getInstance();
 		handler = new Handler();
 
 		ZoomCamera camera = new ZoomCamera(GameConfig.CAMERA_X, GameConfig.CAMERA_Y, GameConfig.CAMERA_WIDTH, GameConfig.CAMERA_HEIGHT);
 
 		camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
 		camera.setBounds(
 				GameConfig.CAMERA_MIN_X, GameConfig.CAMERA_MIN_Y,
 				GameConfig.CAMERA_MAX_X, GameConfig.CAMERA_MAX_Y
 		);
 		camera.setBoundsEnabled(true);
 
 		gc.setCamera(camera);
 
 		this.serverConnection = ServerConnection.getInstance();
 
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
 
 		//
 		// initialize network
 		//
 
 		ServerConnection.getInstance().setHandler(new AngryKingsMessageHandler());
 
 		gc.setVboManager(this.getVertexBufferObjectManager());
 
 		if (GameConfig.LOG_FPS)
 			this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		//
 		// initialize the scene
 		//
 
 		Scene scene = new Scene();
 		scene.setBackground(rm.getSkySprite());
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
 
 		BasicMap map = new BasicMap();
 		scene.attachChild(map);
 
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
 
 		//
 		// This is important because the entity ids are incremented in the order in which we
 		// create the entities :(
 		//
 
 		if(isLeft) {
 			this.me = new Player(myName, isLeft);
 			this.partner = new Player(partnerName, !isLeft);
 		} else {
 			this.partner = new Player(partnerName, !isLeft);
 			this.me = new Player(myName, isLeft);
 		}
 
 		this.me.setPlayerTurnListener(new MyTurnListener());
 		this.partner.setPlayerTurnListener(new PartnerTurnListener());
 
 		//
 		// initialize navigation
 		//
 
 		this.scrollDetector = new SurfaceScrollDetector(this);
 		this.pinchZoomDetector = new PinchZoomDetector(this);
 
 		scene.setOnSceneTouchListener(this);
 		scene.setTouchAreaBindingOnActionDownEnabled(true);
 
 		hud = new GameHUD();
 
 		hud.setOnAimTouched(new Runnable() {
 			@Override
 			public void run() {
 				isAiming = !isAiming;
 			}
 		});
 
 		hud.setOnWhiteFlagTouched(new Runnable() {
 			@Override
 			public void run() {
 				resign();
 			}
 		});
 
 		gc.setHud(hud);
 		gc.getCamera().setHUD(hud);
 
 		final Castle leftCastle = isLeft ? me.getCastle() : partner.getCastle();
 		final Castle rightCastle = !isLeft ? me.getCastle() : partner.getCastle();
 
 		final float initialLeftCastleHeight = leftCastle.getInitialHeight();
 		final float initialRightCastleHeight = rightCastle.getInitialHeight();
 
 		final boolean left = isLeft;
 
 		hud.setLeftPlayerName(isLeft ? myName : partnerName);
 		hud.setRightPlayerName(!isLeft ? myName : partnerName);
 
         myTurn = true;
         timeElapsed = 0f;
 
 		scene.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 
 				float leftLife = leftCastle.getHeight() / initialLeftCastleHeight;
 				float rightLife = rightCastle.getHeight() / initialRightCastleHeight;
 
 				hud.getLeftLifeBar().setValue(1.0f - ((1.0f - leftLife) * 2.0f));
 				hud.getRightLifeBar().setValue(1.0f - ((1.0f - rightLife) * 2.0f));
 
                 if(myTurn){
                     timeElapsed += pSecondsElapsed;
                     hud.setCountdown(30 - Math.round(timeElapsed));
                     if(timeElapsed > 30f){
                         autoshot();
                     }
                 }
 
 				if ((left && leftLife < 0.5f || !left && rightLife < 0.5f) && status != GameStatus.LOST) {
 					lost();
 				}
                 if(followCamera == OWNCANNONBALL){
                     me.getCannon().activateFollowCamera();
                 }else if(followCamera == ENEMYCANNONBALL){
                     partner.getCannon().activateFollowCamera();
                 }else if(followCamera == MIDDLE){
                     deactivateFollowCamera("mitte");
                 }else if(followCamera == ENEMYCANNON){
                     deactivateFollowCamera("gegner");
                 }
 			}
 
 			@Override
 			public void reset() {
 
 			}
 		});
 
 		scene.registerUpdateHandler(pm.getPhysicsWorld());
 
 		this.me.getCastle().freeze();
 		this.partner.getCastle().freeze();
 
 		scene.registerUpdateHandler(this.me);
 		scene.registerUpdateHandler(this.partner);
 
 		pOnCreateSceneCallback.onCreateSceneFinished(scene);
 
 		this.serverConnection.sendTextMessage(ServerMessage.ready());
 		hud.setStatus(this.getString(R.string.yourTurn));
 	}
 
     private void deactivateFollowCamera(String s) {
         ZoomCamera camera = (ZoomCamera) gc.getCamera();
         camera.setChaseEntity(null);
         float cameraX = camera.getCenterX();
         float cameraY = camera.getCenterY();
         float difX;
         float difY;
         if(s.equals("mitte")){
             difX = cameraX - (GameConfig.CAMERA_X + GameConfig.CAMERA_WIDTH/2);
             difY = cameraY - (GameConfig.CAMERA_Y + GameConfig.CAMERA_HEIGHT/2);
         }else{
             difX = cameraX - (partner.getCannon().getX());
             difY = cameraY - (partner.getCannon().getY());
         }
         boolean rightPositionX = false;
         boolean rightPositionY = false;
         if(difX < -10){
             cameraX += Math.abs(difX)/5;
             camera.setCenter(cameraX, cameraY);
         }else if(difX > 10){
             cameraX -= Math.abs(difX)/5;
             camera.setCenter(cameraX, cameraY);
         }else{
             rightPositionX = true;
         }
         if(difY < -10){
             cameraY += Math.abs(difY)/5;
             camera.setCenter(cameraX, cameraY);
         }else if(difY > 10){
             cameraY -= Math.abs(difY)/5;
             camera.setCenter(cameraX, cameraY);
         }else{
             rightPositionY = true;
         }
         if(rightPositionX && rightPositionY && s.equals("mitte")){
             camera.setCenter(GameConfig.CAMERA_X + GameConfig.CAMERA_WIDTH/2, GameConfig.CAMERA_Y + GameConfig.CAMERA_HEIGHT/2);
             camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
             followCamera = OFF;
         }else if(rightPositionX && rightPositionY && s.equals("gegner")){
             camera.setCenter(partner.getCannon().getX(), partner.getCannon().getY());
             camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
             followCamera = OFF;
         }
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
 
 		// TODO: refactor constant
 
 		if (cannonDistanceR < rm.getAimCircleTexture().getHeight() &&
                 ((isLeft && cannonDistanceX > 0) || (!isLeft && cannonDistanceX < 0))) {
 
 			//
 			// aim and fire
 			//
 
 			float x = pSceneTouchEvent.getX();
 			float y = pSceneTouchEvent.getY();
 
 			int iX = (int) x;
 			int iY = (int) y;
 
 			if (this.me.getCannon().pointAt(iX, iY)) {
 				this.aimX = iX;
 				this.aimY = iY;
 			}
 
 			if (pSceneTouchEvent.isActionUp() && this.status == GameStatus.MY_TURN) {
 				this.me.handleTurn(this.aimX, this.aimY, null);
 			}
 
 		} else {
 
 			//
 			// pinch and zoom
 			//
 
 			if (pSceneTouchEvent.isActionDown()) {
 				this.scrollDetector.setEnabled(true);
 			}
 
 			this.pinchZoomDetector.onTouchEvent(pSceneTouchEvent);
 
 			if (this.pinchZoomDetector.isZooming()) {
 				this.scrollDetector.setEnabled(false);
 			} else {
 				this.scrollDetector.onTouchEvent(pSceneTouchEvent);
 			}
 
 		}
 
 		return true;
 	}
 
 	@Override
 	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
 								   TouchEvent pSceneTouchEvent) {
 		ZoomCamera camera = (ZoomCamera) gc.getCamera();
 		this.pinchZoomStartedCameraZoomFactor = camera.getZoomFactor();
 	}
 
 	@Override
 	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
 							TouchEvent pTouchEvent, float pZoomFactor) {
 		ZoomCamera camera = (ZoomCamera) gc.getCamera();
 
 		float factor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
 		if (factor > GameConfig.CAMERA_ZOOM_MIN
 				&& factor < GameConfig.CAMERA_ZOOM_MAX)
 			camera.setZoomFactor(factor);
 	}
 
 	@Override
 	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
 									TouchEvent pTouchEvent, float pZoomFactor) {
 		ZoomCamera camera = (ZoomCamera) gc.getCamera();
 
 		float factor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
 		if (factor > GameConfig.CAMERA_ZOOM_MIN
 				&& factor < GameConfig.CAMERA_ZOOM_MAX)
 			camera.setZoomFactor(factor);
 	}
 
 	@Override
 	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
 								float pDistanceX, float pDistanceY) {
 		ZoomCamera camera = (ZoomCamera) gc.getCamera();
 		final float zoomFactor = camera.getZoomFactor();
 
 		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
 						 float pDistanceX, float pDistanceY) {
 		ZoomCamera camera = (ZoomCamera) gc.getCamera();
 		final float zoomFactor = camera.getZoomFactor();
 
 		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 	}
 
 	@Override
 	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
 								 float pDistanceX, float pDistanceY) {
 		ZoomCamera camera = (ZoomCamera) gc.getCamera();
 		final float zoomFactor = camera.getZoomFactor();
 
 		camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
 	}
 
 	private void resign() {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				final Dialog dialog = new Dialog(OnlineGameActivity.this);
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
 						hud.setStatus(getString(R.string.youResigned));
 						serverConnection.sendTextMessage(ServerMessage.lose());
 						dialog.dismiss();
 						Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 						intent.putExtra("hasWon", false);
 						intent.putExtra("isLeft", OnlineGameActivity.this.isLeft);
 						intent.putExtra("username", me.getName());
 						intent.putExtra("partnername", partner.getName());
 						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 						startActivity(intent);
 					}
 				});
 				dialog.show();
 			}
 		});
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			resign();
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void turn() {
 
 		Log.i(getClass().getName(), "turn()");
 
 		this.hud.setStatus(getString(R.string.yourTurn));
 		this.status = GameStatus.MY_TURN;
 
 		this.me.getCannon().showAimCircle();
 
 	}
 
 	private void won() {
 
 		Log.i(getClass().getName(), "won()");
 
 		this.status = GameStatus.WON;
 
 		Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 		intent.putExtra("hasWon", true);
 		intent.putExtra("isLeft", OnlineGameActivity.this.isLeft);
 		intent.putExtra("username", me.getName());
 		intent.putExtra("partnername", partner.getName());
 		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 
 		startActivity(intent);
 
 	}
 
 	private void lost() {
 		Log.i(getClass().getName(), "lost()");
 
 		this.status = GameStatus.LOST;
 		serverConnection.sendTextMessage(ServerMessage.lose());
 
 		gc.getHud().setStatus("Du hast verloren!");
 		gc.getHud().setStatus(getString(R.string.hasLost));
 
 		Intent intent = new Intent(OnlineGameActivity.this, EndGameActivity.class);
 		intent.putExtra("hasWon", false);
 		intent.putExtra("isLeft", OnlineGameActivity.this.isLeft);
 		intent.putExtra("username", me.getName());
 		intent.putExtra("partnername", partner.getName());
 		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
 
 		startActivity(intent);
 
 	}
 
 	@Override
 	public synchronized void onPauseGame() {
 		Log.i(getClass().getName(), "onPauseGame()");
 
 		super.onPauseGame();
 		if(BuildConfig.DEBUG) {
 			Debug.d(this.getClass().getSimpleName() + ".onPauseGame lalala" + " @(Thread: '" + Thread.currentThread().getName() + "')");
 		}
 	}
 
     private void autoshot() {
         if (this.me.getCannon().pointAt(100, 100)) {
             this.aimX = 100;
             this.aimY = 100;
         }
         hud.setStatus(getString(R.string.waitTooLong));
 
         myTurn = false;
 
         if (this.status == GameStatus.MY_TURN) {
             this.me.handleTurn(this.aimX, this.aimY, null);
         }
     }
 }
