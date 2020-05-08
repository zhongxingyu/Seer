 package com.angrykings.activities;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 
 import com.angrykings.Action;
 import com.angrykings.GameConfig;
 import com.angrykings.GameStatus;
 import com.angrykings.IPlayerTurnListener;
 import com.angrykings.Installation;
 import com.angrykings.Keyframe;
 import com.angrykings.KeyframeData;
 import com.angrykings.PhysicalEntity;
 import com.angrykings.PhysicsManager;
 import com.angrykings.R;
 import com.angrykings.ServerConnection;
 import com.angrykings.cannons.Cannonball;
 import com.angrykings.utils.ServerMessage;
 
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.engine.options.EngineOptions;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 
 public class OnlineGameActivity extends GameActivity implements ServerConnection.OnMessageHandler {
 
     private static final String TAG = "OnlineGameActivity";
 
     // Camera Positions
     private static final int MIDDLE = 0;
     private static final int OWNCANNONBALL = 1;
     private static final int ENEMYCANNONBALL = 2;
     private static final int ENEMYCANNON = 3;
     private static final int OFF = 4;
     private int followCamera = OFF;
 
     // Network
     private ServerConnection serverConnection;
 
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
         private float timeElapsedSinceKeyframe;
         private boolean keyframeInterpolationDone;
 
         @Override
         public void onHandleTurn(int x, int y, ArrayList<Keyframe> keyframes) {
             partner.getCannon().pointAt(x, y);
             partner.getCannonball().getBody().setActive(false);
 
             this.keyframes = keyframes;
             this.keyframeIndex = -1; // because onKeyframe is called at t=0
             this.timeElapsedSinceKeyframe = 0;
             this.keyframeInterpolationDone = false;
 
             //followCamera = ENEMYCANNONBALL;
         }
 
         @Override
         public void onEndTurn() {
             this.keyframes = null;
 
             me.getKing().getSprite().setCurrentTileIndex(0);
             partner.getKing().getSprite().setCurrentTileIndex(1);
 
             me.getKing().jump();
             //followCamera = MIDDLE;
         }
 
         @Override
         public void onKeyframe(float time) {
             if (this.keyframeIndex == this.keyframes.size() - 1) {
                 return;
             }
 
             this.keyframeIndex++;
             this.timeElapsedSinceKeyframe = 0;
 
             if (this.keyframeIndex == this.keyframes.size() - 1) {
                 this.keyframeInterpolationDone = true;
             }
         }
 
         @Override
         public void onUpdate(float dt) {
             if (this.keyframes == null || this.keyframeInterpolationDone)
                 return;
 
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
 
             for (int i = 0; i < currentCastleData.size(); i++) {
 
                 KeyframeData currentKeyframeData = currentCastleData.get(i);
 
                 KeyframeData nextKeyframeData = nextCastleData.get(i);
                 KeyframeData interpolatedKeyframeData = currentKeyframeData.interpolate(nextKeyframeData, t);
 
                 PhysicalEntity block = PhysicsManager.getInstance().getEntityById(currentKeyframeData.entityId);
 
                 block.setKeyframeData(interpolatedKeyframeData);
 
             }
         }
 
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
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
                     serverConnection.sendTextMessage(ServerMessage.leaveGame());
                     dialog.dismiss();
                     Intent intent = new Intent(OnlineGameActivity.this, MainActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                     startActivity(intent);
                 }
             });
             dialog.show();
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
     public void onMessage(String payload) {
         try {
             JSONObject jObj = new JSONObject(payload);
 
             if (jObj.getInt("action") == Action.Server.TURN) {
 
                 final int x = Integer.parseInt(jObj.getString("x"));
                 final int y = Integer.parseInt(jObj.getString("y"));
 
                 ArrayList<Keyframe> keyframes = null;
 
                 if (jObj.has("keyframes")) {
                     JSONArray jsonKeyframes = jObj.getJSONArray("keyframes");
                     keyframes = new ArrayList<Keyframe>();
 
                     for (int i = 0; i < jsonKeyframes.length(); ++i) {
                         keyframes.add(new Keyframe(jsonKeyframes.getJSONObject(i)));
                     }
 
                     Log.i(getClass().getName(), "received " + keyframes.size() + " keyframes");
                     turn();
                 } else {
                     Log.w(getClass().getName(), "received 0 keyframes");
                 }
 
                 partner.handleTurn(x, y, keyframes);
 
             } else if (jObj.getInt("action") == Action.Server.YOU_WIN) {
 
                 onWin();
 
             } else if (jObj.getInt("action") == Action.Server.NEW_GAME) {
 
                 Log.i(TAG, "enter new game");
 
                 JSONObject meJson = jObj.getJSONObject("you");
                 JSONObject partnerJson = jObj.getJSONObject("opponent");
 
                 initializePlayer(meJson.getBoolean("left"), meJson.getString("name"), partnerJson.getString("name"));
 
                 this.me.setPlayerTurnListener(new MyTurnListener());
                 this.partner.setPlayerTurnListener(new PartnerTurnListener());
 
                 turn();
                 fadeIn();
 
             }
 
             if (jObj.getInt("action") == Action.Server.EXISTING_GAME) {
 
                 Log.i(TAG, "enter existing game");
 
                 JSONObject meJson = jObj.getJSONObject("you");
                 JSONObject partnerJson = jObj.getJSONObject("opponent");
 
                 initializePlayer(meJson.getBoolean("left"), meJson.getString("name"), partnerJson.getString("name"));
 
                 this.me.setPlayerTurnListener(new MyTurnListener());
                 this.partner.setPlayerTurnListener(new PartnerTurnListener());
 
                 JSONObject data_you = meJson.getJSONObject("data");
                 JSONObject data_opponent = partnerJson.getJSONObject("data");
 
                 if (data_you.length() > 1) {
                     JSONArray arr = data_you.getJSONArray("keyframes");
                     JSONObject lastFrameJson = arr.getJSONObject(arr.length() - 1);
                     Keyframe lastFrame = new Keyframe(lastFrameJson);
                     me.getCastle().setKeyframeData(lastFrame.getCastleKeyframeData());
                 }
                 if (data_opponent.length() > 1) {
                     JSONArray arr = data_opponent.getJSONArray("keyframes");
                     JSONObject lastFrameJson = arr.getJSONObject(arr.length() - 1);
                     Keyframe lastFrame = new Keyframe(lastFrameJson);
                     partner.getCastle().setKeyframeData(lastFrame.getCastleKeyframeData());
                 }
 
                 int myTurns = meJson.getInt("turn");
                 int partnerTurns = partnerJson.getInt("turn");
                 if(myTurns <= partnerTurns){
                     turn();
                 }
                 fadeIn();
 
             }
         } catch (JSONException e) {
 
             Log.e(getClass().getName(), "JSONException: " + e);
 
         }
     }
 
     @Override
     public EngineOptions onCreateEngineOptions() {
         this.serverConnection = ServerConnection.getInstance();
         return super.onCreateEngineOptions();
     }
 
     @Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
 
         super.onCreateScene(pOnCreateSceneCallback);
 
         this.serverConnection.setHandler(this);
 
         Log.i(TAG, "connected="+this.serverConnection.isConnected());
 
         if(!this.serverConnection.isConnected()) {
             this.serverConnection.start(new ServerConnection.OnStartHandler() {
                 @Override
                 public void onStart() {
                     enterGame();
                 }
             });
         } else {
             enterGame();
         }
 
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
     }
 
     private void enterGame() {
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 
         final String id = Installation.id(this);
         final String registrationId = settings.getString("registrationId", "");
 
         Log.i(TAG, "The installation ID is " + id);
 
         ServerConnection
                 .getInstance()
                 .sendTextMessage(ServerMessage.setId(id, registrationId));
 
         Log.i(TAG, "enterGame");
         Bundle extras = getIntent().getExtras();
         String partnerIdStr = extras.getString("partnerId");
 
         Log.i(TAG, "entering game with partnerId="+partnerIdStr);
         Log.i(TAG, "EXTRAS=" + extras.toString());
 
         Log.i(TAG, "serverConnection=" + this.serverConnection);
         this.serverConnection.sendTextMessage(ServerMessage.enterGame(Integer.valueOf(partnerIdStr)));
     }
 
     private void deactivateFollowCamera(String s) {
         ZoomCamera camera = (ZoomCamera) gc.getCamera();
         camera.setChaseEntity(null);
         float cameraX = camera.getCenterX();
         float cameraY = camera.getCenterY();
         float difX;
         float difY;
         if (s.equals("mitte")) {
             difX = cameraX - (GameConfig.CAMERA_X + GameConfig.CAMERA_WIDTH / 2);
             difY = cameraY - (GameConfig.CAMERA_Y + GameConfig.CAMERA_HEIGHT / 2);
         } else {
             difX = cameraX - (partner.getCannon().getX());
             difY = cameraY - (partner.getCannon().getY());
         }
         boolean rightPositionX = false;
         boolean rightPositionY = false;
         if (difX < -10) {
             cameraX += Math.abs(difX) / 5;
             camera.setCenter(cameraX, cameraY);
         } else if (difX > 10) {
             cameraX -= Math.abs(difX) / 5;
             camera.setCenter(cameraX, cameraY);
         } else {
             rightPositionX = true;
         }
         if (difY < -10) {
             cameraY += Math.abs(difY) / 5;
             camera.setCenter(cameraX, cameraY);
         } else if (difY > 10) {
             cameraY -= Math.abs(difY) / 5;
             camera.setCenter(cameraX, cameraY);
         } else {
             rightPositionY = true;
         }
         if (rightPositionX && rightPositionY && s.equals("mitte")) {
             camera.setCenter(GameConfig.CAMERA_X + GameConfig.CAMERA_WIDTH / 2, GameConfig.CAMERA_Y + GameConfig.CAMERA_HEIGHT / 2);
             camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
             followCamera = OFF;
         } else if (rightPositionX && rightPositionY && s.equals("gegner")) {
             camera.setCenter(partner.getCannon().getX(), partner.getCannon().getY());
             camera.setZoomFactor(GameConfig.CAMERA_STARTUP_ZOOM);
             followCamera = OFF;
         }
     }
 
     @Override
     protected void onPause() {
         serverConnection.sendTextMessage(ServerMessage.leaveGame());
         super.onPause();
     }
 
     @Override
     protected void onResign() {
         super.onResign();
         serverConnection.sendTextMessage(ServerMessage.lose());
 
     }
 
     @Override
     protected void onLose() {
 
         serverConnection.sendTextMessage(ServerMessage.lose());
         super.onLose();
 
     }
 }
