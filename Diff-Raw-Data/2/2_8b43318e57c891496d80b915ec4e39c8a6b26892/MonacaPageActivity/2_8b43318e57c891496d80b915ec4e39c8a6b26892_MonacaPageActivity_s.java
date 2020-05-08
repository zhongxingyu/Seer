 package mobi.monaca.framework;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import mobi.monaca.framework.bootloader.LocalFileBootloader;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.UpdateStyleQuery;
 import mobi.monaca.framework.nativeui.component.Component;
 import mobi.monaca.framework.nativeui.component.PageComponent;
 import mobi.monaca.framework.nativeui.component.PageOrientation;
 import mobi.monaca.framework.nativeui.container.Container;
 import mobi.monaca.framework.nativeui.container.ToolbarContainer;
 import mobi.monaca.framework.nativeui.exception.NativeUIException;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentation;
 import mobi.monaca.framework.psedo.R;
 import mobi.monaca.framework.transition.BackgroundDrawable;
 import mobi.monaca.framework.transition.ClosePageIntent;
 import mobi.monaca.framework.transition.TransitionParams;
 import mobi.monaca.framework.util.AssetUriUtil;
 import mobi.monaca.framework.util.BenchmarkTimer;
 import mobi.monaca.framework.util.InputStreamLoader;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.framework.util.UrlUtil;
 import mobi.monaca.framework.view.MonacaPageGingerbreadWebViewClient;
 import mobi.monaca.framework.view.MonacaPageHoneyCombWebViewClient;
 import mobi.monaca.framework.view.MonacaWebView;
 import mobi.monaca.utils.TimeStamp;
 import mobi.monaca.utils.gcm.GCMPushDataset;
 import mobi.monaca.utils.log.LogItem;
 import mobi.monaca.utils.log.LogItem.LogLevel;
 import mobi.monaca.utils.log.LogItem.Source;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.CordovaWebViewClient;
 import org.apache.cordova.DroidGap;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import receiver.ScreenReceiver;
 import android.R.color;
 import android.annotation.TargetApi;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Paint;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.os.Build.VERSION;
 import android.os.Build.VERSION_CODES;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 /**
  * This class represent a page of Monaca application.
  */
 public class MonacaPageActivity extends DroidGap {
 	public static final String TRANSITION_PARAM_NAME = "monaca.transition";
 	public static final String URL_PARAM_NAME = "monaca.url";
 	public static final String TAG = MonacaPageActivity.class.getSimpleName();
 
 	protected MonacaURI currentMonacaUri;
 
 	protected Drawable background = null;
 
 	protected Handler handler = new Handler();
 
 	protected int pageIndex = 0;
 
 	protected Dialog monacaSplashDialog;
 
 	private boolean isOnDestroyMonacaCalled = false;
 
 	protected BroadcastReceiver closePageReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			int level = intent.getIntExtra("level", 0);
 			if (pageIndex >= level) {
 				finish();
 			}
 			//MyLog.d(MonacaPageActivity.this.getClass().getSimpleName(), "close intent received: " + getCurrentUriWithoutOptions());
 			//MyLog.d(MonacaPageActivity.this.getClass().getSimpleName(), "page index: " + pageIndex);
 		}
 	};
 
 	protected BroadcastReceiver pushReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			MyLog.d(TAG, "push broadcast received");
 			if (isIndex()) {
 				GCMPushDataset p = (GCMPushDataset)intent.getExtras().get(GCMPushDataset.KEY);
 				sendPushToWebView(p);
 			}
 		}
 	};
 
 	/** If this flag is true, activity is capable of transition. */
 	protected boolean isCapableForTransition = true;
 
 	protected UIContext uiContext = null;
 
 	protected TransitionParams transitionParams;
 
 	protected JSONObject infoForJavaScript = new JSONObject();
 	protected String mCurrentHtml;
 	private ScreenReceiver mScreenReceiver;
 	protected GCMPushDataset pushData;
 	protected MonacaApplication mApp;
 	private PageComponent mPageComponent;
 
 	@Override
 	public void onCreate(Bundle savedInstance) {
 		mApp = (MonacaApplication) getApplication();
 		registerReceiver(pushReceiver, new IntentFilter(MonacaNotificationActivity.ACTION_RECEIVED_PUSH));
 
 		prepare();
 
 		if(VERSION.SDK_INT > VERSION_CODES.JELLY_BEAN){
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
 		}
 
 		// initialize receiver
 		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
 		filter.addAction(Intent.ACTION_SCREEN_OFF);
 		mScreenReceiver = new ScreenReceiver();
 		registerReceiver(mScreenReceiver, filter);
 
 		super.onCreate(savedInstance);
 		// to clear
 		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
 		// WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); in DroidGap
 		// class
 		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 //		MyLog.v(TAG, "MonacaApplication.getPages().size():" + MonacaApplication.getPages().size());
 
 		// currentMonacaUri is set in prepare()
 		if (MonacaApplication.getPages().size() == 1) {
 			init();
 			loadUri(currentMonacaUri.getOriginalUrl(), false);
 		} else {
 			init();
 			loadUiFile(getCurrentUriWithoutOptions());
 			handler.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					loadUri(currentMonacaUri.getOriginalUrl(), true);
 				}
 			}, 100);
 		}
 
 		// dirty fix for android4's strange bug
 		if (transitionParams.animationType == TransitionParams.TransitionAnimationType.MODAL) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_dialog_open_enter, mobi.monaca.framework.psedo.R.anim.monaca_dialog_open_exit);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.SLIDE_LEFT) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_open_enter, mobi.monaca.framework.psedo.R.anim.monaca_slide_open_exit);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.SLIDE_RIGHT) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_right_open_enter, mobi.monaca.framework.psedo.R.anim.monaca_slide_right_open_exit);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.NONE) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_none, mobi.monaca.framework.psedo.R.anim.monaca_none);
 		}
 		// root.setBackgroundColor(Color.WHITE);
 
 	}
 	
 
 
 	protected boolean isIndex() {
 		return pageIndex == MonacaApplication.getPages().size() - 1;
 	}
 
 
 	protected Drawable getSplashDrawable() throws IOException {
 		InputStream is = getResources().getAssets().open(MonacaSplashActivity.SPLASH_IMAGE_PATH);
 		return Drawable.createFromStream(is, "splash_default");
 
 	}
 
 	public void showMonacaSplash() {
 		final MonacaPageActivity activity = this;
 
 		Runnable runnable = new Runnable() {
 			public void run() {
 
 				// Get reference to display
 				Display display = activity.getWindowManager().getDefaultDisplay();
 
 				// Create the layout for the dialog
 				FrameLayout root = new FrameLayout(activity.getActivity());
 				root.setMinimumHeight(display.getHeight());
 				root.setMinimumWidth(display.getWidth());
 				root.setBackgroundColor(mApp.getAppJsonSetting().getSplashBackgroundColor());
 				root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
 
 				try {
 					ImageView splashImageView;
 					splashImageView = new ImageView(MonacaPageActivity.this);
 					splashImageView.setImageDrawable(activity.getSplashDrawable());
 					splashImageView.setScaleType(ScaleType.FIT_CENTER);
 
 					root.addView(splashImageView);
 				} catch (IOException e) {
 					MyLog.e(TAG, e.getMessage());
 				}
 
 				// Create and show the dialog
 				monacaSplashDialog = new Dialog(MonacaPageActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
 				// check to see if the splash screen should be full screen
 				if ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
 					monacaSplashDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 				}
 				monacaSplashDialog.setContentView(root);
 				monacaSplashDialog.setCancelable(false);
 				monacaSplashDialog.show();
 			}
 		};
 		this.runOnUiThread(runnable);
 	}
 
 	public void removeMonacaSplash() {
 		if (monacaSplashDialog != null && monacaSplashDialog.isShowing()) {
 			monacaSplashDialog.dismiss();
 			monacaSplashDialog = null;
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (mPageComponent != null) {
 			menu.clear();
 			MenuRepresentation menuRepresentation = MonacaApplication.findMenuRepresentation(mPageComponent.menuName);
 			if (menuRepresentation != null) {
 				menuRepresentation.configureMenu(uiContext, menu);
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	protected void prepare() {
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			Serializable s = bundle.getSerializable(GCMPushDataset.KEY);
 			if (s != null) {
 				pushData = (GCMPushDataset)s;
 			}
 		}
 
 		loadParams();
 
 		MonacaApplication.addPage(this);
 		pageIndex = MonacaApplication.getPages().size() - 1;
 
 		AppJsonSetting appJsonSetting = mApp.getAppJsonSetting();
 		boolean autoHide = false;
 		if(appJsonSetting != null){
 			autoHide = appJsonSetting.getAutoHide();
 		}
 		if (pageIndex == 0 && autoHide == false) {
 			showMonacaSplash();
 		}
 
 		registerReceiver(closePageReceiver, ClosePageIntent.createIntentFilter());
 		uiContext = new UIContext(getCurrentUriWithoutOptions(), this);
 
 		// override theme
 		if (transitionParams.animationType == TransitionParams.TransitionAnimationType.NONE) {
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.MODAL) {
 			setTheme(mobi.monaca.framework.psedo.R.style.MonacaDialogTheme);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.SLIDE_LEFT) {
 			setTheme(mobi.monaca.framework.psedo.R.style.MonacaSlideTheme);
 		} else {
 		}
 
 		try {
 			infoForJavaScript.put("display", createDisplayInfo());
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 
 		// loadBackground(getResources().getConfiguration());
 	}
 
 	/** Load background drawable from transition params and device orientation. */
 	protected void loadBackground(Configuration config) {
 		if (transitionParams != null && transitionParams.hasBackgroundImage()) {
 			String path = null;
 			String preferedPath = "www/" + UIContext.getPreferredPath(transitionParams.backgroundImagePath);
 			if (AssetUriUtil.existsAsset(this, preferedPath)) {
 				path = preferedPath;
 			} else {
 				path = "www/" + transitionParams.backgroundImagePath;
 			}
 
 //			MyLog.v(TAG, "loadBackground(). path:" + path);
 
 			try {
 
 				Bitmap bitmap = BitmapFactory.decodeStream(LocalFileBootloader.openAsset(this.getApplicationContext(), path));
 				background = new BackgroundDrawable(bitmap, getWindowManager().getDefaultDisplay(), config.orientation);
 			} catch (Exception e) {
 				MyLog.e(TAG, e.getMessage());
 			}
 		}
 	}
 
 	/** Release background drawable. */
 	protected void unloadBackground() {
 		if (background != null) {
 			appView.setBackgroundDrawable(null);
 			root.setBackgroundDrawable(null);
 			background.setCallback(null);
 			background = null;
 			System.gc();
 		}
 	}
 
 	public void initMonaca() {
 		appView.setFocusable(true);
 		appView.setFocusableInTouchMode(true);
 
 		/*
 		 * to initialize cordova webView, MonacaWebView detects this symbol and
 		 * passes "javascript:" to CordovaWebView#loadUrlIntoView and
 		 * MonacaPageActivity supresses timeout error message caused by this
 		 */
 		this.loadUrl(MonacaWebView.INITIALIZATION_REQUEST_URL);
 
 		root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
 			@Override
 			public void onGlobalLayout() {
 				try {
 					int height = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight() - root.getHeight();
 					infoForJavaScript.put("statusbarHeight", height);
 				} catch (JSONException e) {
 					MyLog.e(getClass().getSimpleName(), "fail to get statusbar height.");
 				}
 			}
 		});
 
 		// setupBackground();
 
 		// for focus problem between native component and webView
 		appView.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 				case MotionEvent.ACTION_UP:
 					if (!v.hasFocus()) {
 						v.requestFocus();
 					}
 					break;
 				}
 				return false;
 			}
 		});
 
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void init() {
 		CordovaWebView webView = new MonacaWebView(this);
 		// Fix webview bug on ICS_MR1 where webview background is always white when hardware accerleration is on
 		if (VERSION.SDK_INT == VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
 			webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 		}
 
 		if (uiContext.getSettings().forceDisableWebviewGPU) {
     		Method method;
     		try {
     		    method = webView.getClass().getMethod("setLayerType", new Class[]{ int.class, Paint.class });
     		    method.invoke(webView, new Object[]{ View.LAYER_TYPE_SOFTWARE, null });
     		} catch (Exception e) {
         		MyLog.e(TAG, "webview.setLayerType() is fail.");
             }
 		}
 		CordovaWebViewClient webViewClient = (CordovaWebViewClient) createWebViewClient(this, webView);
 		MonacaChromeClient webChromeClient = new MonacaChromeClient(this, webView);
 		this.init(webView, webViewClient, webChromeClient);
 		this.initMonaca();
 	}
 
 	/** Setup background drawable for app View and root view. */
 	public void setupBackground(Drawable background) {
 		appView.setBackgroundColor(0x00000000);
 		if (background != null) {
 			if (appView != null) {
 				appView.setBackgroundDrawable(background);
 			}
 
 			if (root != null) {
 				root.setBackgroundDrawable(background);
 
 				if (root.getParent() == null) {
 					setContentView(root);
 				}
 			}
 		} else {
 			if (appView != null) {
 				// Default background
 				appView.setBackgroundResource(color.white);
 			}
 		}
 	}
 
 	protected void loadLayoutInformation() {
 		appView.loadUrl("javascript: window.__layout = " + infoForJavaScript.toString());
 	}
 
 	protected JSONObject createDisplayInfo() {
 		JSONObject result = new JSONObject();
 
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 		Display display = getWindowManager().getDefaultDisplay();
 		try {
 			result.put("width", display.getWidth());
 			result.put("height", display.getHeight());
 		} catch (JSONException e) {
 		}
 
 		return result;
 	}
 
 	protected void loadParams() {
 
 		Intent intent = getIntent();
 		transitionParams = (TransitionParams) intent.getSerializableExtra(TRANSITION_PARAM_NAME);
 
 		if (transitionParams == null) {
 			transitionParams = TransitionParams.createDefaultParams(this.getRequestedOrientation());
 		}
 
 		String startPage = intent.hasExtra(URL_PARAM_NAME) ? intent.getStringExtra(URL_PARAM_NAME) : "file:///android_asset/www/index.html";
 		if (shouldLoadExtractedIndex()) {
 			startPage = "file://" + LocalFileBootloader.getFullPath(getContext(), "www/index.html");
 		}
 
 		setCurrentUri(startPage);
 
 //		MyLog.v(TAG, "uri without query:" + getCurrentUriWithoutOptions());
 //		MyLog.v(TAG, "uri with query:" + currentMonacaUri.getOriginalUrl());
 	}
 
 
 
 	protected boolean shouldLoadExtractedIndex() {
 		return !getIntent().hasExtra(URL_PARAM_NAME) && (mApp.getAppJsonSetting().shouldExtractAssets() || MonacaSplashActivity.usesLocalFileBootloader);
 	}
 
 	public JSONObject getInfoForJavaScript() {
 		return infoForJavaScript;
 	}
 
 
 	/** Load local ui file */
 	public void loadUiFile(String uri) {
 		JSONObject uiJSON = getUIJSON(uri);
 		if(uiJSON != null){
 			try {
 				mPageComponent = new PageComponent(uiContext, uiJSON);
 			}catch (Exception e) {
 				e.printStackTrace();
 				LogItem logItem = new LogItem(TimeStamp.getCurrentTimeStamp(), Source.SYSTEM, LogLevel.ERROR, "NativeComponent:" + e.getMessage(), "", 0);
 				MyLog.sendBroadcastDebugLog(getContext(), logItem);
 				return;
 			}
 		}
 		
 		applyUiToView();
 	}
 
 
 
 	protected JSONObject getUIJSON(String uri) {
 		String uiJSONString = null;
 		try {
 			uiJSONString = getUIFile(UrlUtil.getUIFileUrl(uri));
 		} catch (IOException e1) {
 			MyLog.d(TAG, "UI file not found");
 			return null;
 		}
 
 		JSONObject uiJSON;
 		try {
 			uiJSON = new JSONObject(uiJSONString);
 			return uiJSON;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			UIUtil.reportJSONParseError(getApplicationContext(), e.getMessage());
 			return null;
 		}
 	}
 
 	protected void applyScreenOrientation(PageOrientation pageOrientation){
 		if(pageOrientation == null){
 			MyLog.v(TAG, "null -> apply from manifest");
 			applyScreenOrientationFromManifest();
 			return;
 		}
 		
 		switch (pageOrientation) {
 		case PORTRAIT:
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			break;
 
 		case LANDSCAPE:
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			break;
 
 		case SENSOR:
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 			break;
 
 		case INHERIT:
 			// no override. Do nothing.
 			MyLog.v(TAG, "inherit -> apply from manifest");
 			applyScreenOrientationFromManifest();
 			break;
 		default:
 			break;
 		}
 	}
 	
 	protected void applyScreenOrientationFromManifest(){
 		try {
 			PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
 			int screenOrientation = getScreenOrientationOfMonacaPageActivity(packageInfo);
 			setRequestedOrientation(screenOrientation);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	private int getScreenOrientationOfMonacaPageActivity(PackageInfo packageInfo) {
 		ActivityInfo[] activies = packageInfo.activities;
 		if(activies != null){
 			for (int i = 0; i < activies.length; i++) {
 				ActivityInfo activityInfo = activies[i];
 				if(activityInfo.name.equalsIgnoreCase(MonacaPageActivity.class.getName())){
 					MyLog.v(TAG, "found screenorientation for MonacaPageAcitivyt");
 					return activityInfo.screenOrientation;
 				}
 			}
 		}
 		// not found -> use sensor
 		return ActivityInfo.SCREEN_ORIENTATION_SENSOR;
 	}
 
 
 	protected void applyUiToView() {
 
 		if(mPageComponent == null){
 			applyScreenOrientationFromManifest();
 			return;
 		}
 		setupBackground(mPageComponent.getBackgroundDrawable());
 		applyScreenOrientation(mPageComponent.getScreenOrientation());
 
 		// clean up
 		root.removeAllViews();
 		ViewGroup appViewParent = ((ViewGroup) appView.getParent());
 		if (appViewParent != null) {
 			appViewParent.removeAllViews();
 		}
 
 		RelativeLayout container = new RelativeLayout(this);
 		root.addView(container, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
 
 		// top
 		ToolbarContainer topComponent = (ToolbarContainer) mPageComponent.getTopComponent();
 		int topComponentViewId = 0;
 		if (topComponent != null){
 			// view
 			topComponentViewId = topComponent.getView().getId();
 			RelativeLayout.LayoutParams topComponentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			topComponentParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			container.addView(topComponent.getView(), topComponentParams);
 
 			// shadow
 			RelativeLayout.LayoutParams shadowViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			shadowViewParams.addRule(RelativeLayout.BELOW, topComponentViewId);
 			container.addView(topComponent.getShadowView(), shadowViewParams);
 		}
 
 		// bottom
 		Container bottomComponentContainer = (Container) mPageComponent.getBottomComponent();
 		int bottomComponentContainerViewId = 0;
 		if(bottomComponentContainer != null){
 			bottomComponentContainerViewId = bottomComponentContainer.getView().getId();
 			RelativeLayout.LayoutParams bottomComponentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			bottomComponentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 			container.addView(bottomComponentContainer.getView(), bottomComponentParams);
 
 			// shadow
 			RelativeLayout.LayoutParams shadowViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			shadowViewParams.addRule(RelativeLayout.ABOVE, bottomComponentContainerViewId);
 			container.addView(bottomComponentContainer.getShadowView(), shadowViewParams);
 		}
 
 		// webview
 		RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		webViewParams.alignWithParent = true;
 		if(topComponent != null && topComponent.isTransparent()){
 			webViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		}else{
 			webViewParams.addRule(RelativeLayout.BELOW, topComponentViewId);
 		}
 
 		if(bottomComponentContainer != null){
 			webViewParams.addRule(RelativeLayout.ABOVE, bottomComponentContainerViewId);
 		}
 		container.addView(appView, 0,  webViewParams);
 
 	}
 
 	public UIContext getUiContext() {
 		return uiContext;
 	}
 
 	protected String getUIFile(String path) throws IOException {
 		Reader reader;
 		InputStream stream = null;
 
 		if (path == null) {
 			return "";
 		}
 
 		MyLog.d(getClass().getSimpleName(), "ui file loading: " + path);
 
 		if (path.startsWith("file:///android_asset/")) {
 			stream = LocalFileBootloader.openAsset(this.getApplicationContext(), path.substring("file:///android_asset/".length()));
 			reader = new InputStreamReader(stream);
 
 		} else if (path.startsWith("file://")) {
 
 			path = new File(path.substring(7)).getCanonicalPath();
 			reader = new FileReader(new File(path));
 
 		} else {
 			stream = InputStreamLoader.loadAssetFile(this, path);
 			reader = new InputStreamReader(stream, "UTF-8");
 		}
 		Writer writer = new StringWriter();
 
 		char[] buffer = new char[1024];
 		try {
 			int n;
 			while ((n = reader.read(buffer)) != -1) {
 				writer.write(buffer, 0, n);
 			}
 		} finally {
 			reader.close();
 			if (stream != null) {
 				stream.close();
 			}
 		}
 		return writer.toString();
 	}
 
 	/** Retrieve a style of Native UI Framework component. */
 	public JSONObject getStyle(String componentId) {
 		if (mPageComponent.getComponentIDsMap().containsKey(componentId)) {
 			Component component = mPageComponent.getComponentIDsMap().get(componentId);
 			return component.getStyle();
 		}
 
 		return null;
 	}
 
 	/** Update a style of Native UI Framework component. */
 	public void updateStyle(final UpdateStyleQuery query) {
 		List<UpdateStyleQuery> queries = new ArrayList<UpdateStyleQuery>();
 		queries.add(query);
 		updateStyleBulkily(queries);
 	}
 
 	/** Update bulkily the styles of Native UI Framework components. */
 	public void updateStyleBulkily(final List<UpdateStyleQuery> queries) {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 //				MyLog.d(MonacaPageActivity.class.getSimpleName(), "updateStyleBulkily() start");
 				for (UpdateStyleQuery query : queries) {
 					for (int i = 0; i < query.ids.length(); i++) {
 						String componentId = query.ids.optString(i, "");
 
 						if (mPageComponent != null && mPageComponent.getComponentIDsMap() != null && mPageComponent.getComponentIDsMap().containsKey(componentId)) {
 							Component component = mPageComponent.getComponentIDsMap().get(componentId);
 							if (component != null) {
 								try {
 									component.updateStyle(query.style);
 								} catch (NativeUIException e) {
 									e.printStackTrace();
 								}
 							} else {
 								Log.e(MonacaPageActivity.class.getSimpleName(), "update fail => id: " + componentId + ", style: " + query.style.toString());
 							}
 						} else {
 							Log.e(MonacaPageActivity.class.getSimpleName(), "no such component id: " + componentId);
 						}
 					}
 				}
 				MyLog.d(MonacaPageActivity.class.getSimpleName(), "updateStyleBulkily() done");
 			}
 		});
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus) {
 			BenchmarkTimer.mark("visible");
 			BenchmarkTimer.finish();
 			requestJStoProcessMessages();
 		}
 	}
 	
 	/*
 	 * current Native2JS bridge use window.online event to signal to js side to process message.
 	 * there is a bug that when resumed from other page activity, the online/offline event is not triggered
 	 * it will trigger if there is more than one js statment in the queue -> we queue a dummy console.log
 	 */
 	private void requestJStoProcessMessages(){
 		appView.sendJavascript("console.log(' ')");
 	}
 
 	public void onPageFinished(View view, String url) {
 		// BenchmarkTimer.mark("page finish:" + url);
 		// if(!url.startsWith("about:blank")){
 		// BenchmarkTimer.finish();
 		// }
 
 		// for android4's strange bug.
 		sendJavascript("console.log(' ');");
 
 		// check if this is 404 page
 		String errorUrl = getIntent().getStringExtra("error_url");
 		processMonacaReady(url);
 		if (errorUrl != null && url.endsWith("/404/404.html")) {
 			String backButtonText = getString(R.string.back_button_text);
 			errorUrl = UrlUtil.cutHostInUri(errorUrl);
 			MyLog.v(TAG, "error url:" + errorUrl);
 			appView.loadUrl("javascript:$('#url').html(\"" + errorUrl + "\"); $('#backButton').html('" + backButtonText + "')");
 		}
 
 		if (url.equals(getCurrentUriWithoutOptions()) && UrlUtil.isMonacaUri(this, url) && currentMonacaUri.hasUnusedFragment()) {
 			// process pushed url fragment
 			// TODO refactor MonacaURI not to use this checkment.noncritical bug with nativecomponent remains that does not work with nativecomponent remains.
 			appView.loadUrl("javascript:window.location.hash = '" + currentMonacaUri.popFragment() + "';");
 		}
 	}
 
 	public void onPageStarted(View view, String url) {
 		ViewGroup.LayoutParams params = this.appView.getLayoutParams();
 		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
 		params.height = ViewGroup.LayoutParams.MATCH_PARENT;
 		this.appView.setLayoutParams(params);
 	}
 
 	@Override
 	protected void onRestart() {
 		MyLog.i(TAG, "onRestart");
 		super.onRestart();
 		loadBackground(getResources().getConfiguration());
 		// setupBackground();
 		if (background != null) {
 			background.invalidateSelf();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		MyLog.i(TAG, "onResume");
 		try {
 			WebView.class.getMethod("onResume").invoke(this);
 		} catch (Exception e) {
 		}
 
 		// only when screen turns on
 		if (!ScreenReceiver.wasScreenOn) {
 			// this is when onResume() is called due to a screen state change
 			System.out.println("SCREEN TURNED ON");
 		} else {
 			// this is when onResume() is called when the screen state has not
 			// changed
 			// if (appView != null && appView.callbackServer != null &&
 			// appView.pluginManager != null) {
 			if (appView != null && appView.pluginManager != null) {
 				appView.loadUrl("javascript: window.onReactivate && onReactivate();");
 			}
 		}
 
 		isCapableForTransition = true;
 		mApp.showMonacaSpinnerDialogIfAny();
 		
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		MyLog.i(TAG, "onPause");
 		super.onPause();
 		this.removeMonacaSplash();
 		mApp.hideMonacaSpinnerDialog();
 
 		if (isFinishing()) {
 			onDestroyMonacaCaller();
 		}
 	}
 
 	/**
 	 * @see MonacaPageActivity#onDestroyMonaca()
 	 */
 	private final void onDestroyMonacaCaller() {
 		//MyLog.d(TAG, "monacaOnDestroyCaller()");
 		if (!isOnDestroyMonacaCalled) {
 			// prevent from multiple calls
 			onDestroyMonaca();
 			isOnDestroyMonacaCalled = true;
 		}
 	}
 
 	/**
 	 * to call onDestroy surely, this is called in onPause or onDestroy
 	 * since Activity#finish doesn't guarantee calling onDestroy.
 	 * this should be called through onDestroyMonacaCaller
 	 * @see MonacaPageActivity#onDestroyMonacaCaller()
 	 */
 	protected void onDestroyMonaca() {
 		MyLog.d(TAG, "onDestroyMonaca");
 		unregisterReceiver(pushReceiver);
 		appView.setBackgroundDrawable(null);
 		root.setBackgroundDrawable(null);
 		this.removeMonacaSplash();
 
 		MonacaApplication.removePage(this);
 		unregisterReceiver(closePageReceiver);
 
 		if (background != null) {
 			background.setCallback(null);
 			background = null;
 		}
 
 		if (mPageComponent != null) {
 			mPageComponent.getComponentIDsMap().clear();
 			mPageComponent = null;
 		}
 		appView.setBackgroundDrawable(null);
 		root.setBackgroundDrawable(null);
 		closePageReceiver = null;
 		unregisterReceiver(mScreenReceiver);
 
 		root.removeView(appView);
 		appView.stopLoading();
 //		appView.setWebChromeClient(null);  // this caused Android 2.3.5 to crash. Null Pointer Exception
 		appView.setWebViewClient(null);
 
 		// this causes null pointer on some devices
 		// for DroidGap posts delayed message to appView
 		// unregisterForContextMenu(appView);
 		// appView.destroy();
 		// appView = null;
 	}
 
 	@Override
 	public void onDestroy() {
 		MyLog.i(TAG, "onDestroy");
 		onDestroyMonacaCaller();
 		super.onDestroy();
 	}
 
 	/** Reload current URI. */
 	public void reload() {
 		appView.stopLoading();
 		loadUri(getCurrentUriWithoutOptions(), false);
 	}
 
 	public String getCurrentHtml() {
 		return mCurrentHtml;
 	}
 
 	protected String buildCurrentUriHtml() throws IOException {
 		String html = AssetUriUtil.assetToString(this, getCurrentUriWithoutOptions());
 
 		if (UrlUtil.isMonacaUri(this, currentMonacaUri.getOriginalUrl()) && currentMonacaUri.hasQueryParams()) {
 			html = currentMonacaUri.getQueryParamsContainingHtml(html);
 		}
 
 		return html;
 	}
 
 	/** Load current URI. */
 	public void loadUri(String uri, final boolean withoutUIFile) {
 		setCurrentUri(uri);
 		String currentUriWithoutQuery = getCurrentUriWithoutOptions();
 		MyLog.v(TAG, "loadUri() uri:" + currentUriWithoutQuery);
 
 		// check for 404
 		if (currentUriWithoutQuery.equalsIgnoreCase("file:///android_asset/www/404/404.html")) {
 			String failingUrl = getIntent().getStringExtra("error_url");
 			show404Page(failingUrl);
 			return;
 		}
 
 		if (!withoutUIFile) {
 			mPageComponent = null;
 			loadUiFile(getCurrentUriWithoutOptions());
 		}
 
 		try {
 			mCurrentHtml = buildCurrentUriHtml();
 			appView.loadDataWithBaseURL(getCurrentUriWithoutOptions(), mCurrentHtml, "text/html", "UTF-8", this.getCurrentUriWithoutOptions());
 
 		} catch (IOException e) {
 			MyLog.w(TAG, "Maybe Not MonacaURI : " + e.getMessage());
 			MyLog.w(TAG, "load as nomal url:" + currentUriWithoutQuery);
 			if (uri.startsWith("file://")) {
 				show404Page(uri);
 				return;
 			}
 
 			appView.setBackgroundColor(0x00000000);
 			// setupBackground();
 			loadLayoutInformation();
 
 			appView.loadUrl(currentMonacaUri.getOriginalUrl());
 			appView.clearView();
 			appView.invalidate();
 		}
 	}
 
 	public void show404Page(String failingUrl) {
 		try {
 			InputStream is = getResources().openRawResource(R.raw.error404);
 			String html = IOUtils.toString(is);
 			html = html.replaceFirst("url_place_holder", UrlUtil.cutHostInUri(failingUrl));
 			html = html.replaceFirst("back_button_text", getString(R.string.back_button_text));
 			appView.loadDataWithBaseURL("file:///android_res/raw/error404.html", html, "text/html", "utf-8", null);
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 	public void push404Page(String errorUrl) {
 		Intent intent = new Intent(this, getClass());
 		intent.putExtra(URL_PARAM_NAME, "file:///android_asset/www/404/404.html");
 		intent.putExtra("error_url", errorUrl);
 
 		TransitionParams params = TransitionParams.from(new JSONObject(), "none");
 		intent.putExtra(TRANSITION_PARAM_NAME, params);
 		startActivity(intent);
 		finish();
 	}
 
 	public void pushPageWithIntent(String url, TransitionParams params) {
 		if (isCapableForTransition) {
 			Intent intent = createIntentForNextPage(url, params);
 
 			isCapableForTransition = false;
 			startActivity(intent);
 			if (params.needsToClearStack()) {
 				/*intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) works similarly but shows blank screen in transit animation*/
 				new Handler().postDelayed(new Runnable() {
 					@Override
 					public void run() {
 						finish();
 					}
 				}, 500);
 			}
 		}
 	}
 
 	protected Intent createIntentForNextPage(String url, TransitionParams params) {
 		Intent intent = new Intent(this, getClass());
 
 		intent.putExtra(URL_PARAM_NAME, UrlUtil.getResolvedUrl(url));
 		if (params != null) {
 			intent.putExtra(TRANSITION_PARAM_NAME, params);
 		}
 		return intent;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (hasOnTapBackButtonAction()) {
 				mPageComponent.eventer.onTapBackButton();
 			} else if (hasBackButtonEventer()) {
 				PageComponent.BACK_BUTTON_EVENTER.onTap();
 			} else if (appView.isBackButtonBound()){
 				return super.onKeyDown(keyCode, event);
 			} else {
 				popPage();
 			}
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && (hasBackButtonEventer() || hasOnTapBackButtonAction())) {
 			return true;
 		} else {
 			return super.onKeyUp(keyCode, event);
 		}
 	}
 	public boolean hasBackButtonEventer() {
 		return mPageComponent != null && PageComponent.BACK_BUTTON_EVENTER != null;
 	}
 
 	public boolean hasOnTapBackButtonAction() {
 		return mPageComponent != null && mPageComponent.eventer != null && mPageComponent.eventer.hasOnTapBackButtonAction();
 	}
 
 	public void pushPageAsync(String relativePath, final TransitionParams params) {
 		final String url = getCurrentUriWithoutOptions() + "/../" + relativePath;
 		BenchmarkTimer.start();
 
 		BenchmarkTimer.mark("pushPageAsync");
 		handler.postAtFrontOfQueue(new Runnable() {
 			@Override
 			public void run() {
 				BenchmarkTimer.mark("monaca.pushPageAsync.run");
 				pushPageWithIntent(url, params);
 			}
 		});
 	}
 
 	public void popPage() {
 		int pageNum = MonacaApplication.getPages().size();
 		finish();
 
 		if (pageNum > 1) {
 			// dirty fix for android4's strange bug
 			if (transitionParams.animationType == TransitionParams.TransitionAnimationType.MODAL) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_enter,
 						mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_exit);
 			} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.SLIDE_LEFT) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_close_enter,
 						mobi.monaca.framework.psedo.R.anim.monaca_slide_close_exit);
 			} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.SLIDE_RIGHT) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_right_close_enter,
 						mobi.monaca.framework.psedo.R.anim.monaca_slide_right_close_exit);
 			} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.NONE) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_none, mobi.monaca.framework.psedo.R.anim.monaca_none);
 			}
 		}
 	}
 
 	public void _popPage() {
 		int pageNum = MonacaApplication.getPages().size();
 		finish();
 
 		if (pageNum > 1) {
 			// dirty fix for android4's strange bug
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_close_enter, mobi.monaca.framework.psedo.R.anim.monaca_slide_close_exit);
 		}
 	}
 
 	public void dismissPage() {
 		int pageNum = MonacaApplication.getPages().size();
 		finish();
 
 		if (pageNum > 1) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_enter, mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_exit);
 		}
 	}
 
 	public void popPageAsync(final TransitionParams params) {
 		handler.postAtFrontOfQueue(new Runnable() {
 			@Override
 			public void run() {
 
 				if (params.animationType == TransitionParams.TransitionAnimationType.POP) {
 					_popPage();
 				} else if (params.animationType == TransitionParams.TransitionAnimationType.DISMISS) {
 					dismissPage();
 				} else {
 					_popPage();
 				}
 			}
 		});
 	}
 
 	public void goHomeAsync(JSONObject options) {
 		final String homeUrl = getHomeUrl(options);
 
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				int numPages = MonacaApplication.getPages().size();
 				for (int i = numPages - 1; i > 0; i--) {
 					MonacaPageActivity page = MonacaApplication.getPages().get(i);
 					page.finish();
 				}
 			}
 		});
 	}
 
 	protected WebViewClient createWebViewClient(MonacaPageActivity page, CordovaWebView webView) {
 		MonacaPageGingerbreadWebViewClient client = null;
 
 		if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 11) {
 			client = new MonacaPageGingerbreadWebViewClient(page, webView);
 		} else {
 			client = new MonacaPageHoneyCombWebViewClient(page, webView);
 		}
 		return client;
 	}
 
 	protected String getHomeUrl(JSONObject options) {
 		if (options == null) {
 			return "file:///android_asset/www/index.html";
 		}
 		return options.optString("url", "").equals("") ? "file:///android_asset/www/index.html" : getCurrentUriWithoutOptions() + "/../"
 				+ options.optString("url");
 	}
 
 	@Override
 	protected void onStop() {
 		MyLog.d(TAG, "onStop");
 		super.onStop();
 		unloadBackground();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 
 		MyLog.d(getClass().getSimpleName(), "onConfigurationChanged()");
 
 		// handling orieantation change for background image.
 		if (background != null) {
 			loadBackground(newConfig);
 			// setupBackground();
 			if (background != null) {
 				background.invalidateSelf();
 			}
 		}
 
 		uiContext.fireOnRotateListeners(newConfig.orientation);
 
		appView.clearView();
 		appView.invalidate();
 
 		Display display = getWindowManager().getDefaultDisplay();
 		MyLog.d(getClass().getSimpleName(), "metrics width: " + display.getWidth() + ", height: " + display.getHeight());
 	}
 
 	/*
 	 * Called from WebViewClient -> can be used in DeubggerPageActivity to
 	 * publish log message
 	 */
 	public void onLoadResource(WebView view, String url) {
 		//MyLog.d(TAG, "onLoadResource :" + url);
 	}
 
 	protected void processMonacaReady(String url) {
 		if (pushData != null) {
 			if (UrlUtil.isMonacaUri(this, url)) {
 				sendPushToWebView(pushData);
 				pushData = null;
 			}
 		} else {
 			MyLog.d(TAG, "no Push");
 		}
 	}
 
 	protected void sendPushToWebView(GCMPushDataset pushData) {
 		appView.loadUrl("javascript:monaca.cloud.Push.send(" + pushData.getExtraJSONString() + ")");
 	}
 
 	public MonacaURI getCurrentMonacaUri() {
 		return currentMonacaUri;
 	}
 
 	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 	}
 
 	@Override
 	public void onReceivedError(int errorCode, String description, String failingUrl) {
 		// MyLog.d(TAG, "got error :" + Integer.toString(errorCode) + ", " +
 		// description + ", " + failingUrl);
 		if (isInitializationMessage(errorCode, description, failingUrl)) {
 			MyLog.d(TAG, "supressed initialize message");
 			return;
 		} else {
 			super.onReceivedError(errorCode, description, failingUrl);
 		}
 	}
 
 	protected boolean isInitializationMessage(int errorCode, String description, String failingUrl) {
 		return (errorCode == MonacaWebView.INITIALIZATION_ERROR_CODE && description.contains(MonacaWebView.INITIALIZATION_DESCRIPTION) && failingUrl
 				.startsWith(MonacaWebView.INITIALIZATION_MADIATOR));
 	}
 
 	public String getCurrentUriWithoutOptions() {
 		return currentMonacaUri.getUrlWithoutOptions();
 	}
 
 	/**
 	 * update uri and currentMonacaURI
 	 *
 	 * @param uri
 	 */
 	public void setCurrentUri(String uri) {
 		MyLog.v(TAG, "setCurrentUri:" + uri);
 		currentMonacaUri = new MonacaURI(uri);
 		uiContext = new UIContext(getCurrentUriWithoutOptions(), this);
 	}
 
 }
