 package com.example.testgame;
 
 import com.testgame.scene.*;
 import org.andengine.engine.Engine;
 import org.andengine.engine.LimitedFPSEngine;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.SmoothCamera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.WakeLockOptions;
 import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.parse.Parse;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParsePush;
 import com.parse.ParseUser;
 import com.testgame.resource.ResourcesManager;
 import com.testgame.scene.SceneManager;
 
 public class MainActivity extends BaseGameActivity {
 
 	 final int mCameraWidth = 480;  
      final int mCameraHeight = 800;
      private BoundCamera mCamera;
      private BroadcastReceiver newTurn;
      private IntentFilter turnIntent;
      private ResourcesManager resourcesManager;
 
      @Override
      public Engine onCreateEngine(EngineOptions pEngineOptions) 
      {
          return new LimitedFPSEngine(pEngineOptions, 60);
      }
             
              @Override
              public EngineOptions onCreateEngineOptions() {
             	 
             	 
             	 newTurn = new BroadcastReceiver(){
 
      				@Override
      				public void onReceive(Context context, Intent intent) {
      					if(resourcesManager.inGame == false){
      						return;
      					}
      					
      					JSONObject json;
     					try {
     						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
      					if(json.getString("deviceId").equals(resourcesManager.opponentDeviceID)){
 	     					if(SceneManager.getInstance().getGameScene() != null)
 	     						Log.d("Turn", "New Turn starting");
 	     						((GameScene) SceneManager.getInstance().getGameScene()).startCompTurn();
 	    					}
     					}
     					catch(Exception e){
     						
     					}
      					
      				}
               		
               	};
               	
               	BroadcastReceiver invite = new BroadcastReceiver(){
 
   				@Override
   				public void onReceive(Context context, Intent intent) {
   					
   					
   					
   					JSONObject json;
 					try {
 						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
 						if(resourcesManager.inGame == true){
 	  						try {
 	  							JSONObject data = new JSONObject("{\"alert\": \"Invitation Denied\", \"action\": \"com.testgame.CANCEL\", \"name\": \""+ParseUser.getCurrentUser().getString("Name")+"\"}");
 	  							 ParsePush push = new ParsePush();
 	  				             push.setChannel("user_"+json.getString("userid")); 
 	  				             push.setData(data);
 	  				             push.sendInBackground();
 	  				             return;
 	  		                } catch (JSONException e) { 
 	  							e.printStackTrace();
 	  						}	
 	  					}
 						if(SceneManager.getInstance().getMainMenuScene() != null)
 							((MainMenuScene) SceneManager.getInstance().getMainMenuScene()).createInvite(json);
 					} catch (JSONException e) {
 						
 						e.printStackTrace();
 					}
   					
   					
   					
   				}
            		
               	};
               	
               	BroadcastReceiver deny = new BroadcastReceiver(){
 
       				@Override
       				public void onReceive(Context context, Intent intent) {
       					
       					JSONObject json;
     					try {
     						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
     						if(SceneManager.getInstance().getMainMenuScene() != null)
    							((MainMenuScene) SceneManager.getInstance().getMainMenuScene()).createDialog(json.getString("name")+ " does not wish to play.");
     					} catch (JSONException e) {
     						
     						e.printStackTrace();
     					}
 	
       				}
                		
                	};
                	
                	BroadcastReceiver accept = new BroadcastReceiver(){
                		
       				@Override
       				public void onReceive(Context context, Intent intent) {
       					JSONObject json;
     					try {
     						
     						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
     						
     						
     						if(SceneManager.getInstance().getMainMenuScene() != null)
     						((MainMenuScene) SceneManager.getInstance().getMainMenuScene()).createAcceptDialog(json);
     					} catch (JSONException e) {
     						
     						e.printStackTrace();
     					}
  	
       				}
                		
                	};
                	
                	BroadcastReceiver quit = new BroadcastReceiver(){
                		
       				@Override
       				public void onReceive(Context context, Intent intent) {
       					
       					JSONObject json;
     					try {
     						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));	
     						if(SceneManager.getInstance().getGameScene() != null && resourcesManager.gameId.equals(json.getString("gameId")))
     							((GameScene) SceneManager.getInstance().getGameScene()).quitDialog("Opponent has quit the game! You win!");
     					}
       					catch(Exception e){
       						
       					}
     					
  	
       				}
                		
                	};
               	
               	IntentFilter inviteFilter = new IntentFilter();
               	inviteFilter.addAction("com.testgame.INVITE");
               	IntentFilter denyFilter = new IntentFilter();
               	denyFilter.addAction("com.testgame.CANCEL");
               	IntentFilter acceptFilter = new IntentFilter();
               	acceptFilter.addAction("com.testgame.ACCEPT");
               	IntentFilter quitFilter = new IntentFilter();
               	quitFilter.addAction("com.testgame.QUIT");
               	turnIntent = new IntentFilter();
               	turnIntent.addAction("com.testgame.NEXT_TURN");
               	registerReceiver(newTurn, turnIntent);
               	registerReceiver(invite, inviteFilter);
               	registerReceiver(deny, denyFilter);
               	registerReceiver(accept, acceptFilter);
               	registerReceiver(quit, quitFilter);
             	Parse.initialize(this, "QFJ1DxJol0sSIq068kUDgbE5IVDnADHO2tJbiRQH", "ARuOWkguSH0ndGjMVCDcDc39hBsNQ3J6g6X7slpY");  
             	ParseFacebookUtils.initialize("248250035306788");
             	mCamera = new SmoothCamera(0, 0, mCameraWidth, mCameraHeight, 500, 500, 50.0f);
                 final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(), this.mCamera);
                 engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
                 engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON); 
                 return engineOptions;
              }
             
              @Override
              public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) {
             	 	ResourcesManager.prepareManager(mEngine, this, mCamera, getVertexBufferObjectManager());
             	    resourcesManager = ResourcesManager.getInstance();
                      pOnCreateResourcesCallback.onCreateResourcesFinished();
              }
             
              @Override
              public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
 
             	 SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
             	 
             	 
  
              }
             
              @Override
              public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) {
 
             	 mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() 
             	    {
             	        public void onTimePassed(final TimerHandler pTimerHandler) 
             	        {
             	            mEngine.unregisterUpdateHandler(pTimerHandler);
             	            SceneManager.getInstance().createMenuScene();
             	        }
             	    }));
             	    pOnPopulateSceneCallback.onPopulateSceneFinished();
              }
              @Override
              protected void onDestroy()
              {
                  super.onDestroy();
                  System.exit(0);
              }
              
              @Override
              public boolean onKeyDown(int keyCode, KeyEvent event) 
              {  
                  if (keyCode == KeyEvent.KEYCODE_BACK)
                  {
                      SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
                  }
                  return false; 
              }
          	public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
                 mCamera.setCenter(mCamera.getCenterX() - pDistanceX, mCamera.getCenterY() - pDistanceY);
         }
 
          	@Override
          	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          	  super.onActivityResult(requestCode, resultCode, data);
          	  ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
          	}
 
          	
          	@Override
          	public void onPause() {
          		super.onPause();
          		if (resourcesManager != null) resourcesManager.pause_music();
          	}
          	
          	@Override
          	public void onResume() {
          		super.onResume();
          		if (resourcesManager != null)resourcesManager.play_music();
          	}
 }
