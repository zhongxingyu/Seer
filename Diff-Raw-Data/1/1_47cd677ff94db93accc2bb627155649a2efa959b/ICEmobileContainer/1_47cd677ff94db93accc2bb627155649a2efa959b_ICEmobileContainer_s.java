 /*
 * Copyright 2004-2011 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions an
 * limitations under the License.
 */ 
 
 package org.icemobile.client.android;
 
 import java.io.FileWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.TreeSet;
 import java.util.Arrays;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.content.res.Configuration;
 import android.view.Window;
 import android.webkit.WebView;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.webkit.GeolocationPermissions;
 import android.content.res.AssetManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuInflater;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.content.ComponentName;
 
 import android.media.MediaPlayer;
 import android.widget.VideoView;
 import android.widget.FrameLayout;
 import android.view.View;
 import android.webkit.DownloadListener;
 import android.net.Uri;
 import android.content.ActivityNotFoundException;
 
 import org.icemobile.client.android.c2dm.C2dmHandler;
 
 public class ICEmobileContainer extends Activity 
     implements SharedPreferences.OnSharedPreferenceChangeListener {
 
     /* Container configuration constants */
     protected static final String HOME_URL = "http://www.icemobile.org/demos.html";
     protected static final boolean INCLUDE_CAMERA = true;
     protected static final boolean INCLUDE_AUDIO = true;
     protected static final boolean INCLUDE_VIDEO = true;
     protected static final int HISTORY_SIZE = 20;
     protected static final String C2DM_SENDER = "icec2dm@gmail.com";
     /* Intent Return Codes */
     protected static final int TAKE_PHOTO_CODE = 1;
     protected static final int TAKE_VIDEO_CODE = 2;
     protected static final int HISTORY_CODE = 3;
 
     /* progress bar config */
     protected static final int PROGRESS_DIALOG = 0;
     private static final String PREFERENCE_PROGRESS_BAR_KEY = "progressBar";
 
     private WebView mWebView;
     private Handler mHandler = new Handler();
     private UtilInterface utilInterface;
     private CameraHandler mCameraHandler;
     private CameraInterface mCameraInterface;
     private AudioInterface mAudioInterface;
     private AudioRecorder mAudioRecorder;
     private AudioPlayer mAudioPlayer;
     private C2dmHandler mC2dmHandler;
     private VideoHandler mVideoHandler;
     private VideoInterface mVideoInterface;
     private AssetManager assetManager;
     private FileLoader fileLoader;
     private Activity self;
     private String currentURL="";
     private String newURL;
     private SharedPreferences prefs;
     private HistoryManager historyManager;
     private Vector history;
     private boolean showLoadProgress;
     private ProgressDialog progressDialog;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 	self = this;
 	requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.main);
 
         /* Initialize the WebView */
 	mWebView = (WebView) findViewById(R.id.webview);
         mWebView.clearCache(true);
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
         mWebView.getSettings().setPluginsEnabled(true);
         mWebView.getSettings().setDomStorageEnabled(true);
         mWebView.setWebViewClient(new ICEfacesWebViewClient());
         mWebView.setWebChromeClient(new ICEfacesWebChromeClient());
         mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
 
         assetManager = getAssets();
 	fileLoader = new FileLoader(assetManager);
         mWebView.addJavascriptInterface(fileLoader, "ICEassets");
 
 	mWebView.setDownloadListener(new DownloadListener() {
 		public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long size)
 		{
 		    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
 		    viewIntent.setDataAndType(Uri.parse(url), mimeType);
 		    try {
 			startActivity(viewIntent);
 		    } catch (ActivityNotFoundException ex) {
 			Log.e("ICEcontainer", "Couldn't find activity to view mimetype: " + mimeType);
 		    }
 		}
 	    });
 
 	/* Establish initial container configuration */
 	prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	includeUtil();
 	if (INCLUDE_CAMERA) includeCamera();
 	if (INCLUDE_AUDIO) includeAudio();
 	if (INCLUDE_VIDEO) includeVideo();
 	if (prefs.getBoolean("c2dm",true)) {
 	    includeC2dm();
 	}
 
 	historyManager = new HistoryManager(HISTORY_SIZE);
 	prefs.registerOnSharedPreferenceChangeListener(this);
 	SharedPreferences.Editor editor = prefs.edit();
 	if (!prefs.contains("url")) {
 	    editor.putString("url", HOME_URL);
 	    editor.commit();
 	} 
 	newURL = prefs.getString("url",HOME_URL);
 	setGallery(prefs.getBoolean("gallery", false));
 
         progressDialog = new ProgressDialog(ICEmobileContainer.this);
         progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         showLoadProgress = prefs.getBoolean(PREFERENCE_PROGRESS_BAR_KEY, false);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	if (resultCode == RESULT_OK) {
 	    switch(requestCode){
 	    case TAKE_PHOTO_CODE:
 		mCameraHandler.gotPhoto();
 		break;
 	    case TAKE_VIDEO_CODE:
 		mVideoHandler.gotVideo(data);
 		break;
 	    case HISTORY_CODE:
 		newURL = data.getStringExtra("url");
 		historyManager.add(newURL);
 		loadUrl();
 		break;
 	    }
 	}
     }
 
     @Override
     protected void onResume() {
 	super.onResume();
 	if (!newURL.equals(currentURL)) {
 	    loadUrl();
 	} else {
 	    utilInterface.loadURL("javascript:ice.push.resumeBlockingConnection();");
 	}
     }
 
     @Override
     protected void onPause() {
 	super.onPause();
 	mAudioPlayer.release();
 	utilInterface.loadURL("javascript:ice.push.pauseBlockingConnection('" + getCloudNotificationId() + "');");
     }
 
     @Override
     protected void onStop() {
 	historyManager.save();
 	super.onStop();
     }
 
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	MenuInflater inflater = getMenuInflater();
 	inflater.inflate(R.menu.dev_menu, menu);
 	return true;
     }
 
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	switch (item.getItemId()) {
 	case R.id.reload:
 	    mWebView.reload();
 	    return true;
 	case R.id.preferences:
 	    Intent intent = new Intent(this, ContainerPreferences.class);
 	    intent.putExtra("url",currentURL);
 	    startActivity(intent);
 	    return true;
 	case R.id.history:
 	    Intent historyIntent = new Intent();
 	    historyIntent.setClass(this, HistoryList.class);
 	    String[] historyList = (String[])history.toArray(new String[history.size()]);
 	    historyIntent.putExtra("history", historyList);
 	    startActivityForResult(historyIntent, HISTORY_CODE);
 	    return true;
 	case R.id.stop:
 	    if (mC2dmHandler != null) {
 		mC2dmHandler.stop();
 	    }
 	    finish();
 	    return true;
 	default:
 	    return super.onOptionsItemSelected(item);
 	}
     }
 
     @Override
 	protected void onSaveInstanceState(Bundle outState) {
 	super.onSaveInstanceState(outState);
 	historyManager.save();
     }
 
     @Override
 	protected void onRestoreInstanceState(Bundle outState) {
 	super.onRestoreInstanceState(outState);
 	historyManager.load();
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
             mWebView.goBack();
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
 	public void onConfigurationChanged(Configuration config) {
 	//don't want to recreate the activity;
 	super.onConfigurationChanged(config);
     } 
 
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 	if (key.equals("url")) {
 	    newURL = prefs.getString(key,HOME_URL);
 	    historyManager.add(newURL);
 	} else if (key.equals("gallery")) {
 	    setGallery(prefs.getBoolean(key,false));
 	} else if (key.equals("c2dm")) {
 	    if (prefs.getBoolean(key,false)) {
 		// Turn on C2DM
 		includeC2dm();
 	    } else {
 		// Turn off C2DM
 		if (mC2dmHandler != null) {
 		    mC2dmHandler.stop();
 		}
 	    }
 	} else if (key.equals(PREFERENCE_PROGRESS_BAR_KEY)) {
             showLoadProgress = prefs.getBoolean(PREFERENCE_PROGRESS_BAR_KEY, true);
         }
     }
 
 
     protected String getCloudNotificationId() {
 	String id=null;
 	if (mC2dmHandler != null) {
 	    id = mC2dmHandler.getRegistrationId();
 	}
 	if (id == null) {
 	    id = prefs.getString("email",null);
 	    if (id != null) {
 		if (id.contains("@")) {
 		    id = new String("mail:" + id);
 		} else {
 		    id = null;
 		}
 	    }
 	}
 	if (id == null) {
 	    id = new String("");
 	}
 	return id;
     }
 	    
     private void setGallery(boolean gallery) {
 	if (mCameraHandler != null) mCameraHandler.setGallery(gallery);	
 	if (mVideoHandler != null) mVideoHandler.setGallery(gallery);
     }
 
     private class ICEfacesWebViewClient extends WebViewClient {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             view.loadUrl(url);
             return true;
         }
         
         @Override  
         public void onPageFinished(WebView view, String url){
             view.loadUrl("javascript:eval(' ' + window.ICEassets.loadAssetFile('native-interface.js'));");  
 	    utilInterface.loadURL("javascript:ice.push.parkInactivePushIds('" + getCloudNotificationId() + "');");
         }  
 
 	@Override
 	    public void onLoadResource(WebView view, String url) {
 	    super.onLoadResource(view, url);
 	}
     }    
 
     final class ICEfacesWebChromeClient extends WebChromeClient 
 	implements MediaPlayer.OnCompletionListener, 
 		   MediaPlayer.OnErrorListener,
 		   WebChromeClient.CustomViewCallback {
 
 	private VideoView video;
 	private View view;
 	private CustomViewCallback callback;
 
         @Override
 	public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
 	    callback.invoke(origin, true, false);
 	}
 
         @Override
         public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
             Log.e("ICEcontainer", "Alert: " +  message);
             result.confirm();
             return true;
         }
 
 	@Override
 	    public void onShowCustomView(View view, CustomViewCallback callback) {
 	    super.onShowCustomView(view, callback);
 	    this.view = view;
         this.callback = callback;
 	    if (view instanceof FrameLayout){
 		FrameLayout frame = (FrameLayout) view;
 		if (frame.getFocusedChild() instanceof VideoView){
 		    video = (VideoView) frame.getFocusedChild();
 		    frame.removeView(video);
 		    self.setContentView(video);
 		    video.setOnCompletionListener(this);
 		    video.setOnErrorListener(this);
 		    video.start();
 		}
 	    }
 	}
 
 	public void onCompletion(MediaPlayer mp) {
 	    returnToWebView();
 	}
 
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 	    returnToWebView();
 	    return true;
 	}
 
 	public void onCustomViewHidden() {
 	}
 
 	private void returnToWebView() {
 	    video.stopPlayback();
         callback.onCustomViewHidden();
 	    self.setContentView(mWebView);
 	}
 
         /**
          * Listen for content loading progress state.  Dialog will show
          * as progress events come in and hide when 100% is reached.
          * @param view current webview.
          * @param progress progress value in 0.0 - 1.0
          */
         @Override
         public void onProgressChanged(WebView view, int progress) {
             self.setProgress(progress * 100);
             progressDialog.setProgress(progress);
             if (!showLoadProgress || progress >= 100){
                progressDialog.hide();
             }else{
                 progressDialog.show();
             }
         }
 
     }
 
     private void loadUrl() {
 	currentURL = newURL;
 	utilInterface.setUrl(currentURL);
 	SharedPreferences.Editor editor = prefs.edit();
 	editor.putString("url", currentURL);
 	editor.commit();
 	mWebView.loadUrl(currentURL);
     }
 	
     private void includeUtil() {
 	utilInterface = new UtilInterface(this, mWebView);
         mWebView.addJavascriptInterface(utilInterface, "ICEutil");
     }
 
     private void includeCamera() {
         mCameraHandler = new CameraHandler(this,mWebView, utilInterface, TAKE_PHOTO_CODE);
         mCameraInterface = new CameraInterface(mCameraHandler);
         mWebView.addJavascriptInterface(mCameraInterface, "ICEcamera");
     }
 
     private void includeVideo() {
         mVideoHandler = new VideoHandler(this,mWebView, utilInterface, TAKE_VIDEO_CODE);
         mVideoInterface = new VideoInterface(mVideoHandler);
         mWebView.addJavascriptInterface(mVideoInterface, "ICEvideo");
     }
 
     private void includeAudio() {
 	mAudioRecorder = new AudioRecorder(this, utilInterface);
 	mAudioPlayer = new AudioPlayer();
 	mAudioInterface = new AudioInterface(mAudioRecorder, mAudioPlayer);
         mWebView.addJavascriptInterface(mAudioInterface, "ICEaudio");
     }
 
     private void includeC2dm() {
 	if (mC2dmHandler == null) {
 	    mC2dmHandler = new C2dmHandler(this, R.drawable.c2dm_icon, "ICE", "ICEmobile", "C2DM Notification");
 	}
 	mC2dmHandler.start(C2DM_SENDER);
     }
 
     private class HistoryManager {
 	private File historyFile;
 	private int maxSize;
 
 	HistoryManager(int maxSize) {
 	    this.maxSize = maxSize;
 	    history = new Vector(maxSize);
 	    load();
 	}
 
 	public void add(String url) {
 	    int i=0;
 	    while (i<history.size() && !url.equals((String)history.get(i))) {
 		i++;
 	    }
 	    boolean changed = false;
 	    if (i<history.size()) {
 		if (i!=0) {
 		    history.remove(i);
 		    changed = true;
 		}
 	    } else {
 		changed = true;
 		if (history.size() == maxSize) {
 		    history.remove(maxSize-1);
 		}
 	    }
 
 	    if (changed && url != null) {
 		history.add(0,url);
 		save();
 	    }
 	}
 
 	public void load() {
 	    File historyFile = new File(utilInterface.getTempPath(), "history.log");
 	    try {
 		String historyList = fileLoader.readTextFile(new FileInputStream(historyFile));
 		history = new Vector(Arrays.asList(historyList.split(" ")));
 	    } catch (IOException e) {
 	    }
 	}
 
 	public void save() {
 	    FileWriter out;
 	    try {
 
 		File historyFile = new File(utilInterface.getTempPath(), "history.log");
 		out = new FileWriter(historyFile);
 		for (Enumeration i = history.elements(); i.hasMoreElements();) {
 		    out.write((String) i.nextElement());
 		    out.write(" ");
 		}
 		out.close();
 	    } catch (IOException e) {
 		Log.e("ICEcontainer", "Can't write URL history");
 	    }
 	}	    
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch(id) {
             case PROGRESS_DIALOG:
                 progressDialog.setProgress(0);
         }
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch(id) {
         case PROGRESS_DIALOG:
             progressDialog = new ProgressDialog(ICEmobileContainer.this);
             progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             progressDialog.setMessage("Loading...");
             return progressDialog;
         default:
             return null;
         }
     }
 }
