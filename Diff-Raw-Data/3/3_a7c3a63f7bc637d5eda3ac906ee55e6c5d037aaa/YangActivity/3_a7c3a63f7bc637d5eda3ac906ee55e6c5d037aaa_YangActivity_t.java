 package yang.android.graphics;
 
 import yang.android.AndroidSensor;
 import yang.model.App;
 import yang.model.DebugYang;
 import yang.model.callback.ExitCallback;
 import yang.surface.YangSurface;
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.media.AudioManager;
 import android.os.Bundle;
 
 public abstract class YangActivity extends Activity implements ExitCallback {
 
 	public static boolean PRINT_ACTIVITY_DEBUG = true;
 
 	public YangActivity() {
 
 	}
 
 	protected static YangTouchSurface mGLView;
 
 	public void defaultInit(YangTouchSurface androidSurface) {
 		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 
 		activityOut("INIT");
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		mGLView = androidSurface;
		if (mGLView.getParent() == null) setContentView(mGLView);
 	}
 
 	protected void activityOut(Object msg) {
 		if(PRINT_ACTIVITY_DEBUG)
 			DebugYang.println("--------------------------("+(""+this).split("@")[1]+") "+msg+"---------------------------",1);
 	}
 
 	protected void setSurface(YangSurface yangSurface) {
 		App.sensor = new AndroidSensor(this);
 		if(DebugYang.PLAY_MACRO_FILENAME!=null)
 			yangSurface.setMacroFilename(DebugYang.PLAY_MACRO_FILENAME);
 		mGLView.setSurface(yangSurface);
 	}
 
 	public void defaultInit() {
 		if(mGLView!=null)
 			defaultInit(mGLView);
 		else
 			defaultInit(new YangTouchSurface(this));
 	}
 
 	public void defaultInit(boolean useDebugEditText) {
 		if(mGLView==null && useDebugEditText)
 			defaultInit(new YangKeyTouchSurface(this));
 		else
 			defaultInit();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		activityOut("PAUSED");
 		mGLView.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		mGLView.mSceneRenderer.mSurface.stop();
 		activityOut("STOP");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		activityOut("RESUME");
 		mGLView.onResume();
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		activityOut("START");
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		activityOut("DESTROY");
 		System.exit(0);
 	}
 
 	@Override
 	protected void onRestoreInstanceState (Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		activityOut("RESTORE INSTANCE STATE");
 	}
 
 	@Override
 	protected void onSaveInstanceState (Bundle outState) {
 		super.onSaveInstanceState(outState);
 		activityOut("SAVED INSTANCE STATE");
 	}
 
 	@Override
 	public void onBackPressed() {
 		mGLView.onBackPressed();
 	}
 
 	@Override
 	public void exit() {
 		activityOut("EXIT");
 		finish();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration config) {
 		super.onConfigurationChanged(config);
 		activityOut("CONFIG_CHANGED: "+config);
 	}
 }
