 package com.pigote.objectpool;
 
 import java.io.IOException;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.LimitedFPSEngine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.WakeLockOptions;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import android.view.KeyEvent;
 
 
 public class MainActivity extends BaseGameActivity {
 
 	private static final int CAMERA_WIDTH = 960;
     private static final int CAMERA_HEIGHT = 540;
     private Camera camera;
         
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		camera = new Camera (0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		EngineOptions engineOptions = new EngineOptions(true,ScreenOrientation.LANDSCAPE_FIXED, 
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);
 		//engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
 		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws IOException {
 		ResourcesManager.prepareManager(mEngine, this, camera, getVertexBufferObjectManager());
 		pOnCreateResourcesCallback.onCreateResourcesFinished();		
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws IOException {
 		SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);		
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException {
 		mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback()
 		{
 			 public void onTimePassed(final TimerHandler pTimerHandler) 
 	            {
 	                mEngine.unregisterUpdateHandler(pTimerHandler);
	                SceneManager.getInstance().createMenuScene();
	                //SceneManager.getInstance().createGameScene();
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
 	
 	public Engine OnCreateEngine(EngineOptions pEngineOptions) { 
 		return new LimitedFPSEngine(pEngineOptions, 60);
 	}
 
 }
