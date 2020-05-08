 package com.secondhand.controller;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.SmoothCamera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.view.KeyEvent;
 import android.widget.EditText;
 
 import com.secondhand.model.resource.HighScoreList;
 import com.secondhand.model.resource.LocalizationStrings;
 import com.secondhand.view.loader.FontLoader;
 import com.secondhand.view.loader.SoundLoader;
 import com.secondhand.view.loader.TextureRegionLoader;
 import com.secondhand.view.scene.AllScenes;
 
 public class MainActivity extends BaseGameActivity {
 
 	public static final int CAMERA_WIDTH = 800;
 	public static final int CAMERA_HEIGHT = 480;
 	
 	public static final int TEXT_INPUT_DIALOG = 1;
 	
 	private SceneController sceneController;
 
 	@Override
 	public Engine onLoadEngine() {
 		
 		// configure camera
 	    final SmoothCamera camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, Float.MAX_VALUE, Float.MAX_VALUE, 1.0f);
 	    
 	    // configure engine
 	    final EngineOptions engineOptions = new EngineOptions(
 	    		true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
 	    engineOptions.setNeedsSound(true);
 	    engineOptions.setNeedsMusic(true);
 	    final Engine engine = new Engine(engineOptions);
 	    
 	    InputDialogManager.getInstance().initialize(this);
 	    
 	    // initialize loader classes:
 	    HighScoreList.getInstance().initialize(this);
 	    FontLoader.getInstance().initialize(this, engine);
 	    SoundLoader.getInstance().initialize(this, engine);
 	    TextureRegionLoader.getInstance().initialize(this, engine);
 	    LocalizationStrings.getInstance().initialize(this);
 
 	 
 	    // initialze scene controller. 
 	    this.sceneController = new SceneController(engine, this);
 	    
 	     return engine;	
 	}
 	
 	@Override
 	public void onLoadResources() {
 		// not handled here, instead handled by singelton classes. 
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		// the FPS logger is useful for optimizing performance.(the FPS is shown in LogCat)
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 	
 		this.sceneController.switchScene(AllScenes.LOADING_SCENE);	
 		return this.sceneController.getCurrentScene();
 	}
 	

	@SuppressWarnings("deprecation")
     @Override
     protected Dialog onCreateDialog(final int pID) {
             if (pID == TEXT_INPUT_DIALOG) {
             	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
             	alert.setTitle("Made it to high score!");
             	alert.setMessage("Your Name");
 
             	// Set an EditText view to get user input 
             	final EditText input = new EditText(this);
             	alert.setView(input);
 
             	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
             	public void onClick(final DialogInterface dialog, final int whichButton) {
             	  InputDialogManager.input = input.getText().toString();
             	  }
             	});
             	
             	return alert.create();
 
 			} else {
 				// it's apparently deprecated, but I don't really give shit.
 				return super.onCreateDialog(pID);
 			}
     }
 
 
 	@Override
 	public void onLoadComplete() {
 
 		// nothing
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if(sceneController.sendOnKeyDownToCurrentScene(pKeyCode, pEvent)) {
 			return true;
 		} else {
 			// else let AndEngine handle it.
 			return super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		
 	    super.onDestroy();
 	    
 	    // ensure that the app is always shut down when exited. 
 	    // otherwise we get weird behaviour when restarting the app.
 	    if(sceneController.isGameLoaded())
 
 	    	System.exit(0);    
 	}
 }
 
