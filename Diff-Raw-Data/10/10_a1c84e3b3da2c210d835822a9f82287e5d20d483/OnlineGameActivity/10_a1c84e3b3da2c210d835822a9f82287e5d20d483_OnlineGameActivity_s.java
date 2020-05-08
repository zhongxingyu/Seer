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
 
 import com.angrykings.Action;
 import com.angrykings.AngryParallaxBackground;
 import com.angrykings.BuildConfig;
 import com.angrykings.GameConfig;
 import com.angrykings.GameContext;
 import com.angrykings.GameHUD;
 import com.angrykings.GameStatus;
 import com.angrykings.IPlayerTurnListener;
 import com.angrykings.Keyframe;
 import com.angrykings.KeyframeData;
 import com.angrykings.PhysicalEntity;
 import com.angrykings.PhysicsManager;
 import com.angrykings.Player;
 import com.angrykings.R;
 import com.angrykings.ResourceManager;
 import com.angrykings.ServerConnection;
 import com.angrykings.cannons.Cannonball;
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
 public class OnlineGameActivity extends GameActivity {
 
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
 
	GameStatus status;
     private BasicMap map;
     private AngryParallaxBackground parallaxBackground;
 
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
 
 					onWin();
 
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
             //followCamera = OWNCANNONBALL;
 		}
 
 		@Override
 		public void onEndTurn() {
 			serverConnection.sendTextMessage(ServerMessage.endTurn(aimX, aimY, this.keyframes));
 
             partner.getCastle().freeze();
 			hud.setStatus(getString(R.string.enemyTurn));
 
 			me.getKing().getSprite().setCurrentTileIndex(0);
 			partner.getKing().getSprite().setCurrentTileIndex(1);
 
 			partner.getKing().jump();
             //followCamera = ENEMYCANNON;
 		}
 
 		@Override
 		public void onKeyframe(float time) {
 			try {
 				Keyframe k = new Keyframe(time, me.getCannonball(), partner.getCastle());
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
         private boolean keyframeInterlpolationDone;
 
 		@Override
 		public void onHandleTurn(int x, int y, ArrayList<Keyframe> keyframes) {
 			partner.getCannon().pointAt(x, y);
 			//me.getCastle().unfreeze();
 
             partner.getCannonball().getBody().setActive(false);
 
 			this.keyframes = keyframes;
 			this.keyframeIndex = -1; // because onKeyframe is called at t=0
 			this.timeElapsed = 0;
 			this.timeElapsedSinceKeyframe = 0;
             this.keyframeInterlpolationDone = false;
 
             //followCamera = ENEMYCANNONBALL;
 		}
 
 		@Override
 		public void onEndTurn() {
 			this.keyframes = null;
 			me.getCastle().freeze();
 
 			me.getKing().getSprite().setCurrentTileIndex(0);
 			partner.getKing().getSprite().setCurrentTileIndex(1);
 
 			me.getKing().jump();
             //followCamera = MIDDLE;
 
 			if(status != GameStatus.LOST)
 				serverConnection.sendTextMessage(ServerMessage.ready());
 		}
 
 		@Override
 		public void onKeyframe(float time) {
             if(this.keyframeIndex == this.keyframes.size() - 1) {
                 return;
             }
 
             this.keyframeIndex++;
             this.timeElapsedSinceKeyframe = 0;
 
             if(this.keyframeIndex == this.keyframes.size() - 1) {
                 this.keyframeInterlpolationDone = true;
             }
         }
 
 		@Override
 		public void onUpdate(float dt) {
 			if(this.keyframes == null || this.keyframeInterlpolationDone)
 				return;
 
 			this.timeElapsed += dt;
 			this.timeElapsedSinceKeyframe += dt;
 
             Keyframe currentKeyframe = this.keyframes.get(this.keyframeIndex);
             Keyframe nextKeyframe = this.keyframes.get(this.keyframeIndex + 1);
             Cannonball cannonball = partner.getCannonball();
 
             float deltaT = (float) (nextKeyframe.getTimestampSec() - currentKeyframe.getTimestampSec());
             float t = this.timeElapsedSinceKeyframe / deltaT;
 
             KeyframeData interpolated = currentKeyframe.getCannonballKeyframeData()
                                         .interpolate(
                                                 nextKeyframe.getCannonballKeyframeData(),
                                                 t
                                         );
 
             cannonball.setKeyframeData(interpolated);
 
             ArrayList<KeyframeData> currentCastleData = currentKeyframe.getCastleKeyframeData();
             ArrayList<KeyframeData> nextCastleData = nextKeyframe.getCastleKeyframeData();
 
             for(int i = 0; i < currentCastleData.size(); i++) {
 
                 KeyframeData currentKeyframeData = currentCastleData.get(i);
 
                 KeyframeData nextKeyframeData = nextCastleData.get(i);
                 KeyframeData interpolatedKeyframeData = currentKeyframeData.interpolate(nextKeyframeData, t);
 
                 PhysicalEntity block = PhysicsManager.getInstance().getEntityById(currentKeyframeData.entityId);
 
                 block.setKeyframeData(interpolatedKeyframeData);
 
             }
 		}
 
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
 
 		super.onCreateScene(pOnCreateSceneCallback);
 
 		ServerConnection.getInstance().setHandler(new AngryKingsMessageHandler());
 
 		this.me.setPlayerTurnListener(new MyTurnListener());
 		this.partner.setPlayerTurnListener(new PartnerTurnListener());
 
         this.serverConnection = ServerConnection.getInstance();
 		this.serverConnection.sendTextMessage(ServerMessage.ready());
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
 	protected void onResign() {
 
         super.onResign();
         serverConnection.sendTextMessage(ServerMessage.lose());
 
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 			super.onResignDialog();
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
 
     @Override
 	protected void onLose() {
 
 		serverConnection.sendTextMessage(ServerMessage.lose());
         super.onLose();
 
 	}
 
 	@Override
 	public synchronized void onPauseGame() {
 		Log.i(getClass().getName(), "onPauseGame()");
 
 		super.onPauseGame();
 		if(BuildConfig.DEBUG) {
 			Debug.d(this.getClass().getSimpleName() + ".onPauseGame lalala" + " @(Thread: '" + Thread.currentThread().getName() + "')");
 		}
 	}
 }
