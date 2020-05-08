 package bode.moritz.footfinger;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import org.cocos2d.layers.CCColorLayer;
 import org.cocos2d.layers.CCScene;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.opengl.CCGLSurfaceView;
 
 import android.app.Activity;
 import android.content.Context;
 import android.media.SoundPool;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.View.OnKeyListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Toast;
 import bode.moritz.footfinger.network.NetworkControllerClient;
 import bode.moritz.footfinger.sound.SoundManager;
 
 public class FootFingerActivity extends Activity {
 	private static CCGLSurfaceView _glSurfaceView;
 	private static InputMethodManager inputMethodManager;
 	private static WifiManager wim;
 	private static Vibrator vibrator;
 	private static Context context;
 	private static CCScene currentScene;
 	private static ArrayList<Class<?>> layerStack = new ArrayList<Class<?>>();
 	private static Socket currentClient;
 	private boolean backgroundMusicPaused = false;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		context = getApplicationContext();
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		_glSurfaceView = new CCGLSurfaceView(this);
 
 		CCDirector.sharedDirector().attachInView(_glSurfaceView);
 
 		CCDirector.sharedDirector().setDisplayFPS(true);
 
 		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);
 		CCDirector.sharedDirector().setDeviceOrientation(
 				CCDirector.kCCDeviceOrientationPortrait);
 
 		this.setContentView(_glSurfaceView);
 
 		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		wim = (WifiManager) getSystemService(WIFI_SERVICE);
 		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 
 		CCScene scene = CCScene.node();
 		scene.addChild(new IndexPageLayer());
 		CCDirector.sharedDirector().runWithScene(scene);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		SoundManager.getInstance();
 		SoundManager.initSounds(this);
 		SoundManager.loadSounds(new SoundPool.OnLoadCompleteListener() {
 
 			@Override
 			public void onLoadComplete(SoundPool soundPool, int sampleId,
 					int status) {
 				if (sampleId == SoundManager
 						.getSampleId(R.raw.background_music)) {
 					SoundManager.playSound(R.raw.background_music, 1f, -1);
 				}
 
 			}
 		});
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		SoundManager.pauseSound(R.raw.background_music);
 		this.backgroundMusicPaused = true;
 		currentScene = CCDirector.sharedDirector().getRunningScene();
 		CCDirector.sharedDirector().onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		CCDirector.sharedDirector().onResume();
 
 		if (this.backgroundMusicPaused) {
 			SoundManager.resumeSound(R.raw.background_music);
 			this.backgroundMusicPaused = false;
 		}
 		// check if first not start
 		if (currentScene != null) {
 			CCDirector.sharedDirector().runWithScene(currentScene);
 		}
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		try {
 			NetworkControllerClient.getInstance().close();
 		} catch (IOException e) {
 			// do nothing
 		}
 		SoundManager.cleanup();
 		CCDirector.sharedDirector().end();
 	}
	
	@Override
	protected void onDestroy() {
		currentScene =null;
		super.onDestroy();
	}
 
 	public static void setNextView(Class<?> thisLayer, Class<?> nextLayer) {
 		layerStack.add(thisLayer);
 		setViewByClass(nextLayer);
 	}
 
 	public static void gotoPreviousView() {
 		Class<?> layer = layerStack.remove(layerStack.size() - 1);
 
 		if (layer == ShooterConnectLayer.class) {
 			layerStack.add(ShooterConnectLayer.class);
 			CCDirector.sharedDirector().getActivity().finish();
 		} else if (layer == GoalKeeperWaitingConnectionLayer.class) {
 			layerStack.add(GoalKeeperWaitingConnectionLayer.class);
 			CCDirector.sharedDirector().getActivity().finish();
 		} else {
 			setViewByClass(layer);
 		}
 	}
 
 	private static void setViewByClass(Class<?> newLayer) {
 		try {
 			CCScene scene = CCScene.node();
 			CCColorLayer ccColorLayer = (CCColorLayer) newLayer.newInstance();
 			scene.addChild(ccColorLayer);
 			CCDirector.sharedDirector().replaceScene(scene);
 		} catch (InstantiationException e) {
 			// do nothing
 		} catch (IllegalAccessException e) {
 			// do nothing
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (layerStack.size() < 1) {
 			super.onBackPressed();
 		} else {
 			gotoPreviousView();
 		}
 	}
 
 	public static Vibrator getVibrator() {
 		return vibrator;
 	}
 
 	public static String getIPAdresse() {
 		int ipAdresse = wim.getConnectionInfo().getIpAddress();
 		return (ipAdresse & 0xFF) + "." + ((ipAdresse >> 8) & 0xFF) + "."
 				+ ((ipAdresse >> 16) & 0xFF) + "." + ((ipAdresse >> 24) & 0xFF);
 	}
 
 	public static void showSoftInput(OnKeyListener keyListener) {
 		_glSurfaceView.setOnKeyListener(keyListener);
 		inputMethodManager.showSoftInput(_glSurfaceView,
 				InputMethodManager.SHOW_FORCED);
 	}
 
 	public static void hideSoftInput() {
 		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
 	}
 
 	public static void setClient(Socket client) {
 		currentClient = client;
 	}
 
 	public static Socket getClient() {
 		return currentClient;
 	}
 
 	public static void makeToast(final String string) {
 		CCDirector.sharedDirector().getActivity().runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 }
