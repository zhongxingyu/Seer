 package org.bridgejs.android.phonebridge.library;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.bridgejs.android.phonebridge.library.browser.BridgeJSBrowser;
 import org.bridgejs.android.phonebridge.library.pluginmanager.activitymodifiers.ActivityEventsModifier;
 import org.bridgejs.android.phonebridge.library.pluginmanager.activitymodifiers.ActivityResultCallback;
 import org.bridgejs.android.phonebridge.library.pluginmanager.activitymodifiers.SpawnActivityForResult;
 import org.bridgejs.android.phonebridge.library.ui.HandlerWithLog;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Window;
 
 public class DroidBridge extends Activity{
 
 	protected BridgeJSBrowser browser;
 	
 	private AtomicBoolean isPaused;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		isPaused = new AtomicBoolean(false);
 		this.browser.init(this, new HandlerWithLog(), false);
 	}
 
 	public String getUrlFromIntent(Intent i, String defaultUrl){
 		String bridgeURI = i.getDataString();
 		String webURI = i.getStringExtra(Intent.EXTRA_TEXT);
 		if (bridgeURI != null){
 			defaultUrl = parseBridgeURL(i.getDataString());
 		}
 		else if (webURI != null){
 			defaultUrl = webURI;
 		}
 
 		System.out.println("URI: " + defaultUrl);
 
 		return defaultUrl;
 	}
 
 	public String parseBridgeURL(String bridgeURI) {
 
 		bridgeURI = bridgeURI.replaceFirst("bridge://.*?/+", "");
 		return "http://" + bridgeURI;
 	}
 
 	public void loadUrlWithPlugins(String url){
 		browser.loadUrlWithPlugins(url);
 	}
 	public void loadUrlWithoutPlugins(final String url){
 		browser.loadUrlWithoutPlugins(url);
 	}
 
 	/* Note: we do have to get the activityEventsModifier in *EVERY* event listener we override 
 	 * 			since we don't know if the pointer to the runnable will have changed at some point */
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnResumeModifier().run();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		isPaused.set(false);
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnResumeModifier().run();
 	}
 
 	@Override
 	public void onPause() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnPauseModifier().run();
 		isPaused.set(true);
 		super.onPause();
 	}
 	
 	public AtomicBoolean getIsPaused() {
 		return isPaused;
 	}
 
 	@Override
 	public void onStop() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnStopModifier().run();
 		try {
 			browser.stopAndDestroyWebView();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		super.onStop();
 	}
 
 	@Override
 	public void onDestroy() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnDestroyModifier().run();
 		try {
			browser.gotoBlankWebPage();
			browser.stopAndDestroyWebView();
			browser.destroyWebView();
 		} catch (Exception e){
 			
 		}
 		super.onDestroy();
 	}
 
 	@Override
 	public void onRestart() {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		activityEventsModifier.getOnRestartModifier().run();
 		super.onRestart();
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 		SpawnActivityForResult spawnActivityInstance = activityEventsModifier.getSpawnActivityInstance();
 		int originalRequestCode = spawnActivityInstance.getRequestCode();
 		ActivityResultCallback callbackToPlugin = spawnActivityInstance.getActivityResultCallback();
 
 		if (originalRequestCode == requestCode) {
 			callbackToPlugin.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event){
 		ActivityEventsModifier activityEventsModifier = browser.getActivityEventsModifier();
 
 		if (keyCode == KeyEvent.KEYCODE_BACK){
 			return activityEventsModifier.getOnBackButtonModifier().run(event);
 		}
 		else if (keyCode == KeyEvent.KEYCODE_MENU) {
 			return activityEventsModifier.getOnMenuButtonModifier().run(event);
 		}
 		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
 			return activityEventsModifier.getOnVolumedownButtonModifier().run(event);
 		}
 		else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
 			return activityEventsModifier.getOnVolumeupButtonModifier().run(event);
 		}
 		else if (keyCode == KeyEvent.KEYCODE_HOME) {
 			return activityEventsModifier.getOnHomeButtonModifier().run(event);
 		}
 		else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 	
 	public boolean superOnKeyDown(int keyCode, KeyEvent event) {
 		return super.onKeyDown(keyCode, event);
 	}
 }
