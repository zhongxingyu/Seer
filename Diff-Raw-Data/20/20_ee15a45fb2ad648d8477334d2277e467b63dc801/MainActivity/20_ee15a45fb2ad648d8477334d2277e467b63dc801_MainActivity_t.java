 package com.example.testgame;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
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
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.Signature;
 import android.util.Base64;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.parse.Parse;
 import com.parse.ParseFacebookUtils;
 import com.testgame.resource.ResourcesManager;
 import com.testgame.scene.SceneManager;
 
 public class MainActivity extends BaseGameActivity {
 
 	 final int mCameraWidth = 480;  
      final int mCameraHeight = 800;
      private BoundCamera mCamera;
      private BroadcastReceiver newTurn;
      private IntentFilter turnIntent;
      //public Scene mScene;
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
      					
      					
     					if(SceneManager.getInstance().getGameScene() != null)
     						((GameScene) SceneManager.getInstance().getGameScene()).startCompTurn();
      					
      				}
               		
               	};
               	
               	BroadcastReceiver invite = new BroadcastReceiver(){
 
   				@Override
   				public void onReceive(Context context, Intent intent) {
   					JSONObject json;
 					try {
 						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
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
       					resourcesManager.inGame = false;
       					JSONObject json;
     					try {
     						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
    						if(SceneManager.getInstance().getMainMenuScene() != null)
    							((MainMenuScene) SceneManager.getInstance().getMainMenuScene()).createDialog(json.getString("name")+ "does not wish to play.");
     					} catch (JSONException e) {
     						
     						e.printStackTrace();
     					}
 	
       				}
                		
                	};
                	
                	BroadcastReceiver accept = new BroadcastReceiver(){
                		
       				@Override
       				public void onReceive(Context context, Intent intent) {
       					resourcesManager.inGame = true;
       					JSONObject json;
     					try {
     						
     						json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
     						if(json.getString("turn").equals("true")){
     							resourcesManager.turn = true;
     						}
     						else 
     							resourcesManager.turn = false;
     						
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
       					
     						
      					if(SceneManager.getInstance().getGameScene() != null)
     						((GameScene) SceneManager.getInstance().getGameScene()).quitDialog();
     					
  	
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
             	 
             	// Add code to print out the key hash
             	    try {
             	        PackageInfo info = getPackageManager().getPackageInfo(
             	                "com.example.testgame", 
             	                PackageManager.GET_SIGNATURES);
             	        for (Signature signature : info.signatures) {
             	            MessageDigest md = MessageDigest.getInstance("SHA");
             	            md.update(signature.toByteArray());
             	            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
             	            }
             	    } catch (NameNotFoundException e) {
 
             	    } catch (NoSuchAlgorithmException e) {
 
             	    }
             	 
             	 
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
                 
               //   PushService.unsubscribe(this, "user_"+ParseUser.getCurrentUser().getObjectId());
                 // ParseUser.logOut();
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
 
 }
