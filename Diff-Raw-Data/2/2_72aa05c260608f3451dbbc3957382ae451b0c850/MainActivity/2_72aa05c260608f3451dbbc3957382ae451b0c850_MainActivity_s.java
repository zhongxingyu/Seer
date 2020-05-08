 package com.lolbro.nian;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 public class MainActivity extends SimpleBaseGameActivity {
 	
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	
 	private static final int CAMERA_WIDTH = 720;
 	private static final int CAMERA_HEIGHT = 480;
 	
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	
 	private Camera mCamera;
 	
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
 	}
 	
 	@Override
 	protected void onCreateResources() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	protected Scene onCreateScene() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 	
 }
