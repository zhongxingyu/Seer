 /*
  * Licensed to Scoreflex (www.scoreflex.com) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. Scoreflex licenses this
  * file to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package com.scoreflex;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.scoreflex.Scoreflex.Response;
 import com.scoreflex.facebook.ScoreflexFacebookWrapper;
 import com.scoreflex.facebook.ScoreflexFacebookWrapper.FacebookException;
 import com.scoreflex.google.ScoreflexGoogleWrapper;
 import com.scoreflex.model.JSONParcelable;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.webkit.CookieManager;
 import android.webkit.CookieSyncManager;
 import android.webkit.ValueCallback;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.webkit.WebChromeClient;
 import android.webkit.ConsoleMessage;
 
 /**
  * An Android view that displays Scoreflex content. A ScoreflexView
  * will hold a WebView pointing to Scoreflex web content.
  *
  *
  */
 @SuppressLint("SetJavaScriptEnabled")
 public class ScoreflexView extends FrameLayout {
 	WebView mWebView;
 	Activity mParentActivity;
 	ViewGroup mErrorLayout;
 	TextView mMessageView;
 	ProgressBar mProgressBar;
 	ScoreflexWebCallbackHandler mWebCallbackHandler = new ScoreflexWebCallbackHandler();
 	ImageButton mCloseButton;
 	ScoreflexView mSubview;
 	Uri mOutputFileUri;
 	UserInterfaceState mUserInterfaceState;
 	ValueCallback<Uri> mUploadMessage;
 	String mInitialResource;
 	Scoreflex.RequestParams mInitialRequestParams;
 	boolean mIsPreloading;
 	protected boolean isLoginSource;
 
 	/**
 	 * The constructor of the view.
 	 * @param activity The activity holding the view.
 	 * @param attrs
 	 * @param defStyle
 	 */
 	@SuppressWarnings("deprecation")
 	public ScoreflexView(Activity activity, AttributeSet attrs, int defStyle) {
 		super(activity, attrs, defStyle);
 
 		// Keep a reference on the activity
 		mParentActivity = activity;
 
 		// Default layout params
 		if (null == getLayoutParams()) {
 			setLayoutParams(new ViewGroup.LayoutParams(
 					ViewGroup.LayoutParams.MATCH_PARENT,
 					Scoreflex.getDensityIndependantPixel(R.dimen.scoreflex_panel_height)));
 		}
 
 		// Set our background color
 		setBackgroundColor(getContext().getResources().getColor(
 				R.color.scoreflex_background_color));
 
 		// Create the top bar
 		View topBar = new View(getContext());
 		topBar.setLayoutParams(new FrameLayout.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT, getResources()
 						.getDimensionPixelSize(R.dimen.scoreflex_topbar_height),
 				Gravity.TOP));
 		topBar.setBackgroundDrawable(getResources().getDrawable(
 				R.drawable.scoreflex_gradient_background));
 		addView(topBar);
 
 		// Create the retry button
 		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 
 		mErrorLayout = (ViewGroup) inflater.inflate(
 				R.layout.scoreflex_error_layout, null);
 		if (mErrorLayout != null) {
 
 			// Configure refresh button
 			Button refreshButton = (Button) mErrorLayout
 					.findViewById(R.id.scoreflex_retry_button);
 			if (null != refreshButton) {
 				refreshButton.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View arg0) {
 						if (null == Scoreflex.getPlayerId()) {
 							setUserInterfaceState(new LoadingState());
 							loadUrlAfterLoggedIn(mInitialResource, mInitialRequestParams);
 						} else if (null == mWebView.getUrl()) {
 							setResource(mInitialResource);
 						} else {
 							mWebView.reload();
 						}
 					}
 				});
 			}
 
 			// Configure cancel button
 			Button cancelButton = (Button) mErrorLayout
 					.findViewById(R.id.scoreflex_cancel_button);
 			if (null != cancelButton) {
 				cancelButton.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View arg0) {
 						close();
 					}
 				});
 			}
 
 			// Get hold of the message view
 			mMessageView = (TextView) mErrorLayout
 					.findViewById(R.id.scoreflex_error_message_view);
 			addView(mErrorLayout);
 
 		}
 
 		// Create the close button
 		mCloseButton = new ImageButton(getContext());
 		Drawable closeButtonDrawable = getResources().getDrawable(
 				R.drawable.scoreflex_close_button);
 		int closeButtonMargin = (int) ((getResources().getDimensionPixelSize(
 				R.dimen.scoreflex_topbar_height) - closeButtonDrawable
 				.getIntrinsicHeight()) / 2.0f);
 		FrameLayout.LayoutParams closeButtonLayoutParams = new FrameLayout.LayoutParams(
 				ViewGroup.LayoutParams.WRAP_CONTENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.RIGHT);
 		closeButtonLayoutParams.setMargins(0, closeButtonMargin, closeButtonMargin,
 				0);
 		mCloseButton.setLayoutParams(closeButtonLayoutParams);
 		mCloseButton.setImageDrawable(closeButtonDrawable);
 		mCloseButton.setBackgroundDrawable(null);
 		mCloseButton.setPadding(0, 0, 0, 0);
 		mCloseButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				close();
 			}
 		});
 		addView(mCloseButton);
 
 		// Create the web view
 		mWebView = new WebView(mParentActivity);
 		mWebView.setLayoutParams(new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				ViewGroup.LayoutParams.MATCH_PARENT));
 		// mWebView.setBackgroundColor(Color.RED);
 		mWebView.setWebViewClient(new ScoreflexWebViewClient());
 		mWebView.setWebChromeClient(new WebChromeClient() {
 
 			@Override
 			public boolean onConsoleMessage(ConsoleMessage cm) {
 				 Log.d(
 						             "Scoreflex",
 						             "javascript Error: "
 						                 + String.format("%s @ %d: %s", cm.message(), cm.lineNumber(),
 						                     cm.sourceId()));
 				return true;
 			}
 
 			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
 				// mtbActivity.mUploadMessage = uploadMsg;
 				mUploadMessage = uploadMsg;
 
 				String fileName = "picture.jpg";
 				ContentValues values = new ContentValues();
 				values.put(MediaStore.Images.Media.TITLE, fileName);
 				// mOutputFileUri = mParentActivity.getContentResolver().insert(
 				// MediaStore.Images.Media.DATA, values);
 				final List<Intent> cameraIntents = new ArrayList<Intent>();
 				final Intent captureIntent = new Intent(
 						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 				final PackageManager packageManager = mParentActivity
 						.getPackageManager();
 				final List<ResolveInfo> listCam = packageManager.queryIntentActivities(
 						captureIntent, 0);
 				for (ResolveInfo res : listCam) {
 					final String packageName = res.activityInfo.packageName;
 					final Intent intent = new Intent(captureIntent);
 					intent.setComponent(new ComponentName(res.activityInfo.packageName,
 							res.activityInfo.name));
 					intent.setPackage(packageName);
 					cameraIntents.add(intent);
 				}
 
 				// Filesystem.
 				final Intent galleryIntent = new Intent();
 				galleryIntent.setType("image/*");
 				galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
 
 				// Chooser of filesystem options.
 				final Intent chooserIntent = Intent.createChooser(galleryIntent,
 						"Select Source please");
 
 				// Add the camera options.
 				chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
 						cameraIntents.toArray(new Parcelable[] {}));
 
 				mParentActivity.startActivityForResult(chooserIntent,
 						Scoreflex.FILECHOOSER_RESULTCODE);
 			}
 
 			public void openFileChooser(ValueCallback<Uri> uploadMsg,
 					String acceptType) {
 				openFileChooser(uploadMsg);
 			}
 
 			public void openFileChooser(ValueCallback<Uri> uploadMsg,
 					String acceptType, String capture) {
 				openFileChooser(uploadMsg);
 			}
 		});
 		mWebView.getSettings().setJavaScriptEnabled(true);
 		mWebView.getSettings().setDomStorageEnabled(true);
 		mWebView.getSettings().setDatabasePath(
 				getContext().getFilesDir().getPath() + "/data/"
 						+ getContext().getPackageName() + "/databases/");
 		addView(mWebView);
 
 		TypedArray a = mParentActivity.obtainStyledAttributes(attrs,
 				R.styleable.ScoreflexView, defStyle, 0);
 		String resource = a.getString(R.styleable.ScoreflexView_resource);
 		if (null != resource)
 			setResource(resource);
 
 		a.recycle();
 
 		// Create the animated spinner
 
 		mProgressBar = (ProgressBar) ((LayoutInflater) getContext()
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
 				R.layout.scoreflex_progress_bar, null);
 		mProgressBar.setLayoutParams(new FrameLayout.LayoutParams(
 				ViewGroup.LayoutParams.WRAP_CONTENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
 		addView(mProgressBar);
 		LocalBroadcastManager.getInstance(activity).registerReceiver(mLoginReceiver,
 	      new IntentFilter(Scoreflex.INTENT_USER_LOGED_IN));
 		setUserInterfaceState(new InitialState());
 	}
 
 	/**
 	 * The constructor of the view.
 	 * @param activity The activity hodling the view.
 	 * @param attrs
 	 */
 	public ScoreflexView(Activity activity, AttributeSet attrs) {
 		this(activity, attrs, 0);
 	}
 
 	/**
 	 * The constructor of the view.
 	 * @param activity The activity hodling the view.
 	 */
 	public ScoreflexView(Activity activity) {
 		this(activity, null);
 	}
 
 	private void setUserInterfaceState(UserInterfaceState state) {
 		if (null != mUserInterfaceState)
 			mUserInterfaceState.onLeaveState();
 		mUserInterfaceState = state;
 		state.onEnterState();
 		requestLayout();
 	}
 
 	/**
 	 * Can the webview go back (is its history stack non-empty) ?
 	 *
 	 * @return Wether the view can go back or not.
 	 */
 	public boolean canGoBack() {
 		return mWebView.canGoBack();
 	}
 
 	/**
 	 * Makes the webview go back.
 	 */
 	public void goBack() {
 		mWebView.goBack();
 	}
 
 
 	private void loadUrlAfterLoggedIn(final String resource, final Scoreflex.RequestParams params)
 	{
 
 		ScoreflexRestClient.fetchAnonymousAccessToken(new Scoreflex.ResponseHandler() {
 
 			@Override
 			public void onFailure(Throwable e, Response errorResponse) {
 				mMessageView.setText(R.string.scoreflex_network_error);
 				setUserInterfaceState(new ErrorState());
 			}
 
 			@Override
 			public void onSuccess(Response response) {
 				ScoreflexRequestParamsDecorator.decorate(resource, params);
 				String url = String.format(Locale.getDefault(), "%s?%s",
 					ScoreflexUriHelper.getNonSecureAbsoluteUrl(resource),
 					params.getURLEncodedString());
 				mWebView.loadUrl(url);
 			}
 		}, 0);
 	}
 
 	/**
 	 * Preloads all the static resources of the selected url.
 	 *
 	 * @param resource The url to preload..
 	 */
 	public void preloadResource(String resource) {
 
 		setLayoutParams(new ViewGroup.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
 		mIsPreloading = true;
 		mInitialResource = resource;
 		Scoreflex.RequestParams params = new Scoreflex.RequestParams();
 		if (Scoreflex.getPlayerId() != null) {
 			ScoreflexRequestParamsDecorator.decorate(resource, params);
 			String url = String.format(Locale.getDefault(), "%s?%s",
 					ScoreflexUriHelper.getNonSecureAbsoluteUrl(resource),
 					params.getURLEncodedString());
 
 			mWebView.loadUrl(url);
 		} else {
 			loadUrlAfterLoggedIn(resource,  new Scoreflex.RequestParams());
 		}
 	}
 
 	public void startOpeningAnimation() {
 		int gravity = getLayoutGravity();
 		int anim = (Gravity.TOP == (gravity & Gravity.VERTICAL_GRAVITY_MASK)) ? R.anim.scoreflex_enter_slide_down
 				: R.anim.scoreflex_enter_slide_up;
 		Animation animation = AnimationUtils.loadAnimation(mParentActivity, anim);
 		this.startAnimation(animation);
 	}
 
 	/**
 	 * Closes this ScoreflexView. If the view is attached to the developer
 	 * hierarchy it will be detached. If the view is attached to a
 	 * ScoreflexActivity, this activity will be terminated.
 	 */
 	public void close() {
 		// If we're displayed in the developer view hierarchy,
 		// remove ourselves.
 		final ViewParent parent = this.getParent();
 		ViewGroup.LayoutParams currentLayoutParams = getLayoutParams();
 		boolean isFullscreen = currentLayoutParams.height == LayoutParams.MATCH_PARENT;
 
 		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mLoginReceiver);
 
 		if (isFullscreen && null != parent && parent instanceof ViewGroup) {
 			int gravity = getLayoutGravity();
 			int anim = (Gravity.TOP == (gravity & Gravity.VERTICAL_GRAVITY_MASK)) ? R.anim.scoreflex_exit_slide_up
 					: R.anim.scoreflex_exit_slide_down;
 			Animation animation = AnimationUtils.loadAnimation(mParentActivity, anim);
 			animation.setAnimationListener(new AnimationListener() {
 
 				@Override
 				public void onAnimationEnd(Animation arg0) {
 					ViewGroup parentGroup = (ViewGroup) parent;
 					parentGroup.removeView(ScoreflexView.this);
 				}
 
 				@Override
 				public void onAnimationRepeat(Animation arg0) {
 
 				}
 
 				@Override
 				public void onAnimationStart(Animation arg0) {
 
 				}
 
 			});
 			startAnimation(animation);
 		} else if (null != parent && parent instanceof ViewGroup) {
 			ViewGroup parentGroup = (ViewGroup) parent;
 			parentGroup.removeView(this);
 		}
 	}
 
 	/**
 	 * Sets the resource for the web content displayed in this ScoreflexView's
 	 * webview.
 	 *
 	 * @param resource
 	 */
 	public void setResource(String resource) {
 		setResource(resource, null, false);
 	}
 
 	protected int getLayoutGravity() {
 		ViewGroup.LayoutParams currentLayoutParams = getLayoutParams();
 		int gravity = Scoreflex.getDefaultGravity() & Gravity.VERTICAL_GRAVITY_MASK;
 		if (currentLayoutParams instanceof FrameLayout.LayoutParams) {
 			FrameLayout.LayoutParams frameLayoutParams = (FrameLayout.LayoutParams) currentLayoutParams;
 			gravity = (Gravity.TOP == (frameLayoutParams.gravity & Gravity.VERTICAL_GRAVITY_MASK)) ? Gravity.TOP
 					: Gravity.BOTTOM;
 		}
 		return gravity;
 	}
 
 	protected View openNewView(String resource, Scoreflex.RequestParams params,
 			boolean forceFullScreen) {
 		int gravity = getLayoutGravity();
 		int anim = (Gravity.TOP == (gravity & Gravity.VERTICAL_GRAVITY_MASK)) ? R.anim.scoreflex_enter_slide_down
 				: R.anim.scoreflex_enter_slide_up;
 		Animation animation = AnimationUtils.loadAnimation(mParentActivity, anim);
 
 		ScoreflexView webview = new ScoreflexView(mParentActivity);
 		webview.setResource(resource, params, forceFullScreen);
 		ViewGroup contentView = (ViewGroup) mParentActivity.getWindow()
 				.getDecorView().findViewById(android.R.id.content);
 		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				ViewGroup.LayoutParams.MATCH_PARENT, gravity);
 		webview.setLayoutParams(layoutParams);
 		contentView.addView(webview);
 		webview.startAnimation(animation);
 		Scoreflex.setCurrentScoreflexView(this);
 		return webview;
 	}
 
 	/**
 	 * Sets the resource for the web content displayed in this ScoreflexView's
 	 * webview.
 	 *
 	 * @param resource
 	 * @param params
 	 * @param forceFullScreen
 	 */
 	public void setResource(String resource, Scoreflex.RequestParams params,
 			boolean forceFullScreen) {
 		if (null == resource) {
 			Log.e("Scoreflex", "null resource provided to ScoreflexView");
 			return;
 		}
 		mInitialResource = resource;
 		mInitialRequestParams = params;
 		mIsPreloading = false;
 		if (null == params)
 			params = new Scoreflex.RequestParams();
 
 		ScoreflexRequestParamsDecorator.decorate(resource, params);
 
 		ViewGroup.LayoutParams currentLayoutParams = getLayoutParams();
 		boolean isFullscreen = currentLayoutParams.height == LayoutParams.MATCH_PARENT;
 
 		if (isFullscreen && forceFullScreen) {
 			Scoreflex.setCurrentScoreflexView(this);
 		}
 
 		if (isFullscreen || forceFullScreen == false) {
 			if (Scoreflex.getPlayerId() != null) {
 				String url = String.format(Locale.getDefault(), "%s?%s",
 						ScoreflexUriHelper.getNonSecureAbsoluteUrl(resource),
 						params.getURLEncodedString());
 				mWebView.loadUrl(url);
 			} else {
 				loadUrlAfterLoggedIn(resource, params);
 			}
 
 
 			return;
 		}
 
 
 		if (Scoreflex.getPlayerId() != null) {
 			String url = String.format(Locale.getDefault(), "%s?%s#start",
 					ScoreflexUriHelper.getNonSecureAbsoluteUrl(resource),
 					params.getURLEncodedString());
 			mWebView.loadUrl(url);
 		} else {
 			loadUrlAfterLoggedIn(resource, params);
 		}
 		Scoreflex.setCurrentScoreflexView(this);
 		// String fullUrl = String.format("%s?%s",
 		// ScoreflexUriHelper.getNonSecureAbsoluteUrl(resource),
 		// params != null ? params.getURLEncodedString(): "");
 		int gravity = getLayoutGravity();
 		FrameLayout.LayoutParams newLayoutParams = new FrameLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, gravity);
 		this.setLayoutParams(newLayoutParams);
 
 		this.startOpeningAnimation();
 		return;
 
 	}
 
 	/**
 	 * Sets the resource for the web content displayed in this ScoreflexView's
 	 * webview.
 	 *
 	 * @param resource
 	 * @param params
 	 */
 	public void setResource(String resource, Scoreflex.RequestParams params) {
 		setResource(resource, params, false);
 	}
 
 	/**
 	 * Sets the full URL for the web content displayed in this ScoreflexView's
 	 * webview.
 	 *
 	 * @param fullUrl
 	 *          A full Scoreflex URL.
 	 * @throws IllegalArgumentException
 	 *           When the provided URL is not a Scoreflex URL.
 	 */
 	public void setFullUrl(String fullUrl) throws IllegalArgumentException {
 		if (fullUrl == null)
 				return;
 
 		Uri parsedUri = Uri.parse(fullUrl);
 
 		if (!ScoreflexUriHelper.isAPIUri(parsedUri))
 			throw new IllegalArgumentException(String.format(
 					"Not a Scoreflex URL: %s", fullUrl));
 
 
 
 		setResource(ScoreflexUriHelper.getResource(parsedUri),
 				ScoreflexUriHelper.getParams(parsedUri));
 	}
 
 	/**
 	 * Sets the full URL for the web content displayed in this ScoreflexView's
 	 * webview.
 	 *
 	 * @param fullUrl
 	 *          A full Scoreflex URL
 	 * @throws IllegalArgumentException
 	 *           When the provided URL is not a Scoreflex URL
 	 */
 	public void setFullUrl(String fullUrl, boolean forceFullScreen,
 			boolean newView) throws IllegalArgumentException {
 		Uri parsedUri = Uri.parse(fullUrl);
 
 		if (!ScoreflexUriHelper.isAPIUri(parsedUri))
 			throw new IllegalArgumentException(String.format(
 					"Not a Scoreflex URL: %s", fullUrl));
 		if (!newView) {
 			setResource(ScoreflexUriHelper.getResource(parsedUri),
 					ScoreflexUriHelper.getParams(parsedUri), forceFullScreen);
 			return;
 		}
 		openNewView(ScoreflexUriHelper.getResource(parsedUri),
 				ScoreflexUriHelper.getParams(parsedUri), forceFullScreen);
 	}
 
 	/**
 	 * Removes the drop shadow.
 	 *
 	 */
 	@SuppressWarnings("deprecation")
 	public void removeDropShadow() {
 		setBackgroundDrawable(null);
 		setPadding(0, 0, 0, 0);
 	}
 
 	/**
 	 * Adds a drop shadow to this view.
 	 *
 	 * @param gravity
 	 *          The gravity of the Scoreflex view (not the drop shadow's).
 	 */
 	@SuppressWarnings("deprecation")
 	public void addDropShadow(int gravity) {
 		boolean top = Gravity.TOP != (gravity & Gravity.VERTICAL_GRAVITY_MASK);
 		setBackgroundDrawable(getContext().getResources().getDrawable(
 				top ? R.drawable.scoreflex_top_shadow
 						: R.drawable.scoreflex_bottom_shadow));
 		int height = getResources().getDimensionPixelSize(
 				R.dimen.scoreflex_shadow_height);
 		setPadding(0, top ? height : 0, 0, top ? 0 : height);
 	}
 
 	/**
 	 * An implementation of {@link android.webkit.WebViewClient} that queries the
 	 * {@link ScoreflexWebCallbackHandler} before loading any URL.
 	 *
 	 *
 	 *
 	 */
 	private class ScoreflexWebViewClient extends WebViewClient {
 
 		private boolean mError;
 		private boolean mIsLoading;
 		private Timer mWebviewTimer;
 		private Handler mMainThreadHandler;
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			Uri uri = Uri.parse(url);
 
 			// Handle market urls
 			// https://play.google.com/store/apps/details?id=com.yakaz.mtb
 			if ("market".equals(uri.getScheme())
 					|| "play.google.com".equals(uri.getHost())) {
 
 				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace(
 						"https://play.google.com/store/apps", "market:/")));
 				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				Scoreflex.getApplicationContext().startActivity(intent);
 				return true;
 			}
 			// Handle web callback
 			if (mWebCallbackHandler.handleWebCallback(uri)) {
 				return true;
 			}
 
 			return false;
 		}
 
 		@Override
 		public void onPageStarted(final WebView view, String url, Bitmap favicon) {
 			super.onPageStarted(view, url, favicon);
 
 			// OnPageStarted is called twice on error
 			if (!mIsLoading) {
 				mError = false;
 				setUserInterfaceState(new LoadingState());
 			}
 			if (null != mWebviewTimer) {
 				mWebviewTimer.cancel();
 				mWebviewTimer = null;
 			}
 			mMainThreadHandler = new Handler();
 			mWebviewTimer = new Timer("webviewTimeout", true);
 			mWebviewTimer.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 					mMainThreadHandler.post(new Runnable(){
 
 						@Override
 						public void run() {
 							view.stopLoading();
 							mMessageView.setText(R.string.scoreflex_network_error);
 							setUserInterfaceState(new ErrorState());
 							// if we are preloading we must remove the preloaded view from the preloaded view pool
 							if (ScoreflexView.this.mIsPreloading) {
 								Scoreflex.freePreloadedResources(ScoreflexView.this.mInitialResource);
 							}
 						}});
 				}}, Scoreflex.WEBVIEW_REQUEST_TOTAL_TIMEOUT);
 
 			mIsLoading = true;
 		}
 
 		@Override
 		public void onReceivedError(WebView view, int errorCode,
 				String description, String failingUrl) {
 			super.onReceivedError(view, errorCode, description, failingUrl);
 
 			if (null != failingUrl && failingUrl.equals(view.getUrl())) {
 				mError = true;
 				if (null != mMessageView)
 					mMessageView.setText(description);
 				setUserInterfaceState(new ErrorState());
 			}
 		}
 
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			super.onPageFinished(view, url);
 			if (mWebviewTimer != null) {
 				mWebviewTimer.cancel();
 			}
 			mMainThreadHandler = null;
 			mWebviewTimer = null;
 			mIsLoading = false;
 			if (mIsPreloading) {
 				Uri parsedUri = Uri.parse(url);
 				String resource = ScoreflexUriHelper.getResource(parsedUri);
 				Intent intent = new Intent(Scoreflex.INTENT_RESOURCE_PRELOADED);
 				intent.putExtra(Scoreflex.INTENT_RESOURCE_PRELOADED_EXTRA_PATH,
 						resource);
 
 				LocalBroadcastManager.getInstance(Scoreflex.getApplicationContext())
 						.sendBroadcast(intent);
 			}
 			if (null != url && url.equals(view.getUrl())) {
 				if (!mError) {
 					setUserInterfaceState(new WebContentState());
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * A class that optionally handles a web callback uri. The callback uri should
 	 * be "/web/callback" and provide a code and a status. Depending on these 2
 	 * parameters, this object will trigger various behaviors.
 	 *
 	 *
 	 *
 	 */
 	private class ScoreflexWebCallbackHandler {
 		private static final String sCallbackResource = "/web/callback";
 
 		private String mAuthGrantedNextUrlString;
 		private String mAuthState;
 
 		protected boolean handleWebCallback(Uri uri) {
 			// Only handle when uri is the callback resource
 			if (!sCallbackResource.equals(ScoreflexUriHelper.getResource(uri)))
 				return false;
 
 			// Parse status and code
 			String statusString = uri.getQueryParameter("status");
 			String codeString = uri.getQueryParameter("code");
 			int status, code;
 			try {
 				status = Integer.parseInt(statusString);
 				code = Integer.parseInt(codeString);
 			} catch (NumberFormatException e) {
 				Log.e("Scoreflex", String.format(
 						"Invalid status or code (should be an int): %s %s", statusString,
 						codeString), e);
 				return false;
 			}
 
 			// Successes
 			if (300 > status) {
 
 				if (handleMoveToNewUrl(uri, status, code))
 					return true;
 
 				if (handleCloseWebView(uri, status, code))
 					return true;
 
 				if (handleNeedsAuth(uri, status, code))
 					return true;
 
 				if (handleAuthGranted(uri, status, code))
 					return true;
 
 				if (handleLogout(uri, status, code))
 					return true;
 
 				if (handleNeedsClientAuth(uri, status, code))
 					return true;
 
 				if (handleStartChallenge(uri, status, code))
 					return true;
 
 				if (handlePlayLevel(uri, status, code))
 					return true;
 
 				if (handleLinkService(uri, status, code))
 					return true;
 
 				if (handleSocialInvite(uri, status, code))
 					return true;
 
 				if (handleSocialShare(uri, status, code))
 					return true;
 
 			} else {
 
 				// 404 errors: let the user see them
 				if (404 == status)
 					return false;
 
 				// Generic errors
 				if (handleGenericError(uri, status, code)) {
 					return true;
 				}
 
 				// Invalid SID
 				if (handleInvalidSIDError(uri, status, code))
 					return true;
 
 				// HTTPS required
 				if (handleSecureConnectionRequiredError(uri, status, code))
 					return true;
 
 				// Inactive game
 				if (handleInactiveGameError(uri, status, code))
 					return true;
 			}
 
 			return false;
 		}
 
 		/*
 		 * case Scoreflex.ERROR_SECURE_CONNECTION_REQUIRED: res =
 		 * R.string.SCOREFLEX_ERROR_MISSING_MANDATORY_PARAMETER; break;
 		 *
 		 * case Scoreflex.ERROR_INACTIVE_GAME: res =
 		 * R.string.SCOREFLEX_ERROR_INACTIVE_GAME; break;
 		 */
 
 
 		private boolean handlePlayLevel(Uri uri, int status, int code) {
 			if (Scoreflex.SUCCESS_PLAY_LEVEL != code)
 				return false;
 
 			try {
 				JSONObject data = new JSONObject(uri.getQueryParameter("data"));
 				Intent intent = new Intent(Scoreflex.INTENT_PLAY_LEVEL);
 				intent.putExtra(Scoreflex.INTENT_PLAY_LEVEL_EXTRA_LEADERBOARD_ID,
 						data.getString("leaderboardId"));
 				LocalBroadcastManager.getInstance(Scoreflex.getApplicationContext())
 						.sendBroadcast(intent);
 				close();
 			} catch (Exception e) {
 
 			}
 			return false;
 		}
 
 		private boolean handleStartChallenge(Uri uri, int status, int code) {
 			if (Scoreflex.SUCCESS_START_CHALLENGE != code)
 				return false;
 
 			try {
 				JSONObject data = new JSONObject(uri.getQueryParameter("data"));
 				Scoreflex.RequestParams params = new Scoreflex.RequestParams();
 				params.put("fields","core,turn,outcome,config");
 				Scoreflex.get(
 						"/challenges/instances/" + data.getString("challengeInstanceId"),
 						params, new Scoreflex.ResponseHandler() {
 							public void onFailure(Throwable e, Response errorResponse) {
 
 							}
 
 							public void onSuccess(Response response) {
 								JSONParcelable challengeInstance = new JSONParcelable(response.getJSONObject());
 								Intent intent = new Intent(Scoreflex.INTENT_START_CHALLENGE);
 								intent.putExtra(Scoreflex.INTENT_START_CHALLENGE_EXTRA_INSTANCE, challengeInstance);
 								LocalBroadcastManager.getInstance(
 										Scoreflex.getApplicationContext()).sendBroadcast(intent);
 								Scoreflex.startPlayingSession();
 								close();
 							}
 						});
 				return true;
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 
 		private boolean handleInactiveGameError(Uri uri, int status, int code) {
 			if (Scoreflex.ERROR_INACTIVE_GAME != code)
 				return false;
 
 			close();
 
 			return true;
 
 		}
 
 		private boolean handleSecureConnectionRequiredError(Uri uri, int status,
 				int code) {
 			if (Scoreflex.ERROR_SECURE_CONNECTION_REQUIRED != code)
 				return false;
 
 			mWebView.loadUrl(uri.toString().replace("http:", "https:"));
 			return true;
 		}
 
 		private boolean handleInvalidSIDError(Uri uri, int status, int code) {
 
 			if (Scoreflex.ERROR_INVALID_SID != code)
 				return false;
 
 			// Invalidate access token
 			ScoreflexRestClient.setAccessToken(null, true);
 
 			// Invalidate SID
 			ScoreflexRestClient.setSID(null);
 
 			// Request a new anonymous access token
 			ScoreflexRestClient.fetchAnonymousAccessTokenIfNeeded();
 
 			return true;
 		}
 
 		private boolean handleGenericError(Uri uri, int status, int code) {
 			int res;
 			switch (code) {
 			case Scoreflex.ERROR_INVALID_PARAMETER:
 				res = R.string.SCOREFLEX_ERROR_INVALID_PARAMETER;
 				break;
 			case Scoreflex.ERROR_MISSING_MANDATORY_PARAMETER:
 				res = R.string.SCOREFLEX_ERROR_INVALID_PARAMETER;
 				break;
 			case Scoreflex.ERROR_INVALID_PREV_NEXT_PARAMETER:
 				res = R.string.SCOREFLEX_ERROR_INVALID_PREV_NEXT_PARAMETER;
 				break;
 			case Scoreflex.ERROR_SANDBOX_URL_REQUIRED:
 				res = R.string.SCOREFLEX_ERROR_INVALID_PREV_NEXT_PARAMETER;
 				break;
 			case Scoreflex.ERROR_MISSING_PERMISSIONS:
 				res = R.string.SCOREFLEX_ERROR_MISSING_PERMISSIONS;
 				break;
 			case Scoreflex.ERROR_PLAYER_DOES_NOT_EXIST:
 				res = R.string.SCOREFLEX_ERROR_PLAYER_DOES_NOT_EXIST;
 				break;
 			case Scoreflex.ERROR_DEVELOPER_DOES_NOT_EXIST:
 				res = R.string.SCOREFLEX_ERROR_DEVELOPER_DOES_NOT_EXIST;
 				break;
 			case Scoreflex.ERROR_GAME_DOES_NOT_EXIST:
 				res = R.string.SCOREFLEX_ERROR_GAME_DOES_NOT_EXIST;
 				break;
 			case Scoreflex.ERROR_LEADERBOARD_CONFIG_DOES_NOT_EXIST:
 				res = R.string.SCOREFLEX_ERROR_LEADERBOARD_CONFIG_DOES_NOT_EXIST;
 				break;
 			case Scoreflex.ERROR_SERVICE_EXCEPTION:
 				res = R.string.SCOREFLEX_ERROR_SERVICE_EXCEPTION;
 				break;
 			default:
 				return false;
 			}
 
 			Log.e("Scoreflex", getContext().getString(res));
 
 			// In sandbox mode, we let the developer see the error page.
 			if (Scoreflex.usesSandbox())
 				return false;
 
 			// In production, the error is handled by doing nothing
 			return true;
 		}
 
 		private boolean handleLogout(Uri uri, int status, int code) {
 
 			if (code != Scoreflex.SUCCESS_LOGOUT)
 				return false;
 
 			// Invalidate access token
 			ScoreflexRestClient.setAccessToken(null, true);
 
 			// Invalidate SID
 			ScoreflexRestClient.setSID(null);
 
 			// if (null != mParentActivity)
 			// ScoreflexGoogleWrapper.logout(mParentActivity);
 
 			if (null != mParentActivity)
 				try {
 					ScoreflexFacebookWrapper.logout(mParentActivity);
 				} catch (FacebookException e) {
 					Log.e("Scoreflex", "Error signing out of Facebook SDK", e);
 				}
 
 			// Remove all cookies
 			CookieSyncManager cookieSyncManager = CookieSyncManager
 					.createInstance(getContext());
 			CookieManager cookieManager = CookieManager.getInstance();
 			cookieManager.removeAllCookie();
 			cookieManager.removeSessionCookie();
 			cookieSyncManager.sync();
 
 			// Request a new anonymous access token
 			ScoreflexRestClient.fetchAnonymousAccessTokenIfNeeded();
 
 			close();
 
 			return true;
 
 		}
 
 		private boolean handleAuthGranted(Uri uri, int status, int code) {
 
 			if (code != Scoreflex.SUCCESS_AUTH_GRANTED)
 				return false;
 
 			String dataString = uri.getQueryParameter("data");
 
 			JSONObject dataJson;
 			try {
 				dataJson = new JSONObject(dataString);
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 				// Handle by doing nothing
 				return true;
 			}
 
 			// Check returned state against our recorded state
 			String state = dataJson.optString("state");
 			if (null == mAuthState) {
 				Log.e("Scoreflex",
 						"Error authenticating as we have no recorded state to check against");
 				return true;
 			}
 
 			if (!mAuthState.equals(state)) {
 				Log.e(
 						"Scoreflex",
 						String
 								.format(
 										"Error authenticating as the returned state doesn't match the one we recorded: %s != %s",
 										mAuthState, state));
 				return true;
 			}
 
 			// Get the oauth code
 			String codeString = dataJson.optString("code");
 
 			if (null == codeString) {
 				Log.e("Scoreflex", "Error authenticating, no oauth code returned");
 				return true;
 			}
 
 			// Transform that code into a token
 			Scoreflex.RequestParams params = new Scoreflex.RequestParams();
 			params.put("code", codeString);
 			params.put("clientId", Scoreflex.getClientId());
 			params.put("devicePlatform", "Android");
 			params.put("deviceModel", Scoreflex.getDeviceModel());
 			String udid = Scoreflex.getUDID();
 			if (null != udid)
 				params.put("deviceId", udid);
 
 			ScoreflexRestClient.post("/oauth/accessToken", params,
 					new Scoreflex.ResponseHandler() {
 						public void onFailure(Throwable e, Response errorResponse) {
 							Log.e(
 									"Scoreflex",
 									String
 											.format(
 													"Error authenticating, could not transform code into accessToken: %s",
 													errorResponse.getErrorMessage()), e);
 						}
 
 						public void onSuccess(Response response) {
 							JSONObject json = response.getJSONObject();
 							if (null == json) {
 								Log.e("Scoreflex",
 										"Error authenticating, server didn't return a JSON response");
 								return;
 							}
 
 							JSONObject accessToken = json.optJSONObject("accessToken");
 							if (null == accessToken || !accessToken.has("token")) {
 								Log.e("Scoreflex",
 										"Error authenticating, server didn't return an access token");
 								return;
 							}
 
 							if (!json.has("sid")) {
 								Log.e("Scoreflex",
 										"Error authenticating, server didn't return an sid");
 								return;
 							}
 
 							String token = accessToken.optString("token");
 							String sid = json.optString("sid");
							String playerId = json.optString("playerId");
 							ScoreflexRestClient.setAccessToken(token, false);
 							ScoreflexRestClient.setSID(sid);
 							ScoreflexRestClient.setPlayerId(playerId);
 
 							Intent intent = new Intent(Scoreflex.INTENT_USER_LOGED_IN);
 							intent.putExtra(Scoreflex.INTENT_USER_LOGED_IN_EXTRA_SID, sid);
 							intent.putExtra(
 									Scoreflex.INTENT_USER_LOGED_IN_EXTRA_ACCESS_TOKEN, token);
 							isLoginSource = true;
 							LocalBroadcastManager.getInstance(
 									Scoreflex.getApplicationContext()).sendBroadcast(intent);
 
 							if (null != mAuthGrantedNextUrlString) {
 								openFullUrl(mAuthGrantedNextUrlString, false, false);
 								mWebView.clearHistory();
 							}
 
 						}
 					});
 
 			return true;
 
 		}
 
 		private boolean handleNeedsClientAuth(Uri uri, int status, int code) {
 			if (code != Scoreflex.SUCCESS_NEEDS_CLIENT_AUTH)
 				return false;
 
 			Scoreflex.RequestParams params = new Scoreflex.RequestParams();
 			// Pass the anonymous access token
 			String accessToken = ScoreflexRestClient.getAccessToken();
 			if (ScoreflexRestClient.getAccessTokenIsAnonymous()
 					&& null != accessToken)
 				params.put("anonymousAccessToken", accessToken);
 
 			return nativeLogin(uri, "/oauth/web/authorizeExternallyAuthenticated",
 					params);
 		}
 
 		private boolean handleLinkService(Uri uri, int status, int code) {
 			if (code != Scoreflex.SUCCESS_LINK_SERVICE)
 				return false;
 
 			JSONObject dataJson;
 			String dataString = uri.getQueryParameter("data");
 			try {
 				dataJson = new JSONObject(dataString);
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 				// Handle by doing nothing
 				return true;
 			}
 			String service = dataJson.optString("service");
 			if (null == service) {
 				Log.w("Scoreflex", "handleLinkService: No service specified");
 				return false;
 			}
 
 			return nativeLogin(uri, "/web/linkExternallyAuthenticated/" + service,
 					new Scoreflex.RequestParams());
 		}
 
 		private boolean nativeLogin(Uri uri, final String nextResource,
 				final Scoreflex.RequestParams params) {
 
 			String dataString = uri.getQueryParameter("data");
 
 			JSONObject dataJson;
 			try {
 				dataJson = new JSONObject(dataString);
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 				// Handle by doing nothing
 				return true;
 			}
 
 			// Manage next url
 			final String nextUrlString = dataJson.optString("nextUrl");
 			if (null != nextUrlString && 0 < nextUrlString.length()) {
 				Uri nextUri = Uri.parse(nextUrlString);
 				mAuthGrantedNextUrlString = String.format("%s?%s",
 						ScoreflexUriHelper.getNonSecureAbsoluteUrl(nextUri.getPath()),
 						nextUri.getQuery());
 			}
 
 			final String service = dataJson.optString("service");
 
 			if (!"Facebook".equals(service) && !"Google".equals(service)) {
 				Log.d("Scoreflex", "Unknown service: "
 						+ (service != null ? service : "null"));
 				return false;
 			}
 
 			if ("Facebook".equals(service)
 					&& !ScoreflexFacebookWrapper.isFacebookAvailable(getContext()))
 				return false;
 
 			if ("Google".equals(service)
 					&& !ScoreflexGoogleWrapper.isGoogleAvailable(getContext()))
 				return false;
 
 			// Handle facebook when we live in a scoreflex activity only
 			if (null == mParentActivity) {
 				return false;
 			}
 
 			try {
 				SocialCallback callback = new SocialCallback() {
 
 					@Override
 					public void call(String accessToken, Exception exception) {
 
 						if (exception != null) {
 							Log.e("Scoreflex", "Error with native login", exception);
 						}
 
 						if (null != accessToken && 0 < accessToken.length()) {
 
 							// Generate a new auth state
 							mAuthState = UUID.randomUUID().toString();
 
 							params.put("clientId", Scoreflex.getClientId());
 							params.put("service", service);
 							params.put("serviceAccessToken", accessToken);
 							params.put("state", mAuthState);
 							params.put("devicePlatform", "Android");
 							params.put("deviceModel", Scoreflex.getDeviceModel());
 							params.put("deviceId", Scoreflex.getUDID());
 							if (nextUrlString != null) {
 								params.put("next", nextUrlString);
 							}
 							setResource(nextResource, params);
 						}
 					}
 				};
 				if ("Facebook".equals(service))
 					ScoreflexFacebookWrapper.login(mParentActivity, callback);
 
 				if ("Google".equals(service))
 					ScoreflexGoogleWrapper.login(mParentActivity, callback);
 			} catch (Exception e) {
 				Log.e("Scoreflex", "Native login exception", e);
 				webLogin(true, true, service);
 			}
 			return true;
 		}
 
 		private List<String> JSONArrayToList(JSONArray array) throws JSONException {
 			ArrayList<String> targetsList = new ArrayList<String>();
 			if (array != null) {
 			   for (int i=0;i<array.length();i++){
 			  	 targetsList.add(array.get(i).toString());
 			   }
 			}
 			return targetsList;
 		}
 
 		private boolean handleSocialInvite(Uri uri, int status, int code) {
 			if (code != Scoreflex.SUCCESS_INVITE_WITH_SERVICE)
 				return false;
 
 			JSONObject dataJson;
 			String dataString = uri.getQueryParameter("data");
 			try {
 				dataJson = new JSONObject(dataString);
 				String service = dataJson.optString("service");
 				if ("Facebook".equals(service)) {
 					List<String> suggestedList = JSONArrayToList(dataJson.optJSONArray("targetIds"));
 					String data = dataJson.optString("data");
 					Scoreflex.sendFacebookInvitation(mParentActivity, dataJson.getString("text"), null, suggestedList, data);
 //					ScoreflexFacebookWrapper.sendInvitation(
 				}
 				if ("Google".equals(service)) {
 					List<String> friendIds = JSONArrayToList(dataJson.optJSONArray("targetIds"));
 					Scoreflex.sendGoogleInvitation(mParentActivity, dataJson.getString("text"),friendIds, dataJson.optString("url"), "/invite");
 				}
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 
 				return true;
 			}
 
 			return true;
 		}
 
 		private boolean handleSocialShare(Uri uri, int status, int code) {
 			if (code != Scoreflex.SUCCESS_SHARE_WITH_SERVICE)
 				return false;
 
 			JSONObject dataJson;
 			String dataString = uri.getQueryParameter("data");
 			try {
 				dataJson = new JSONObject(dataString);
 				String text = dataJson.optString("text");
 				String url = dataJson.optString("url");
 				if ("Facebook".equals(dataJson.optString("service"))) {
 					String title = dataJson.optString("title");
 					Scoreflex.shareOnFacebook(mParentActivity, title, text, url);
 				}
 				if ("Google".equals(dataJson.optString("service"))) {
 					Scoreflex.shareOnGoogle(mParentActivity, text, url);
 				}
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 				// Handle by doing nothing
 				return true;
 			}
 
 
 			return true;
 		}
 
 		private boolean handleNeedsAuth(Uri uri, int status, int code) {
 			if (code != Scoreflex.SUCCESS_NEEDS_AUTH)
 				return false;
 
 			String dataString = uri.getQueryParameter("data");
 
 			JSONObject dataJson;
 			try {
 				dataJson = new JSONObject(dataString);
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 				// Handle by doing nothing
 				return true;
 			}
 
 			// Remember where we ought to go once auth is successful
 			// Note that we can't use the "redirectUri" parameter of the
 			// authorize call
 			// as it is intended to be registered on the server side (see oauth
 			// spec).
 			String nextUrlString = dataJson.optString("nextUrl");
 			if (null != nextUrlString) {
 				Uri nextUri = Uri.parse(nextUrlString);
 				mAuthGrantedNextUrlString = String.format("%s?%s",
 						ScoreflexUriHelper.getNonSecureAbsoluteUrl(nextUri.getPath()),
 						nextUri.getQuery());
 			} else {
 				mAuthGrantedNextUrlString = null;
 			}
 
 			// full mode ?
 			String modeString = dataJson.optString("mode");
 
 			String service = dataJson.optString("service");
 			webLogin("full".equals(modeString), false, service);
 
 			return true;
 
 		}
 
 		private void webLogin(boolean fullScreen, boolean disableNativeLogin,
 				String service) {
 			// Build the uri
 			Scoreflex.RequestParams params = new Scoreflex.RequestParams();
 			params.put("clientId", Scoreflex.getClientId());
 			params.put("devicePlatform", "Android");
 			params.put("deviceModel", Scoreflex.getDeviceModel());
 
 			// If native login disabled, override the default handledServices
 			// parameter
 			if (disableNativeLogin)
 				params.put("handledServices", "");
 
 			// Pass the anonymous access token
 			String accessToken = ScoreflexRestClient.getAccessToken();
 			if (ScoreflexRestClient.getAccessTokenIsAnonymous()
 					&& null != accessToken)
 				params.put("anonymousAccessToken", accessToken);
 
 			if (null != service)
 				params.put("service", service);
 
 			// A random state
 			mAuthState = UUID.randomUUID().toString();
 			params.put("state", mAuthState);
 
 			// Open authorize resource in the web view
 			openResource("/oauth/web/authorize", params, fullScreen, false);
 		}
 
 		private boolean handleCloseWebView(Uri uri, int status, int code) {
 			if (code != Scoreflex.SUCCESS_CLOSE_WEBVIEW)
 				return false;
 
 			close();
 
 			return true;
 
 		}
 
 		private boolean handleMoveToNewUrl(Uri uri, int status, int code) {
 
 			if (code != Scoreflex.SUCCESS_MOVE_TO_NEW_URL)
 				return false;
 
 			String dataString = uri.getQueryParameter("data");
 
 			JSONObject dataJson;
 			try {
 				dataJson = new JSONObject(dataString);
 			} catch (JSONException e) {
 				Log.e("Scoreflex", "Invalid json received in the data parameter", e);
 				// Handle by doing nothing
 				return true;
 			}
 
 			String urlString = dataJson.optString("url");
 
 			// No URL provided, handle by doing nothing
 			if (null == urlString) {
 				Log.e("Scoreflex", "Move to new URL requested but no url provided");
 				return true;
 			}
 
 			String modeString = dataJson.optString("mode");
 			openFullUrl(urlString, "full".equals(modeString), true);
 
 			return true;
 		}
 
 		private boolean openResource(String resource,
 				Scoreflex.RequestParams params, boolean forceFullScreen, boolean newView) {
 
 			return openFullUrl(String.format("%s%s?%s", Scoreflex.getBaseURL(),
 					resource, params != null ? params.getURLEncodedString() : ""),
 					forceFullScreen, newView);
 		}
 
 		private boolean openFullUrl(String fullUrl, boolean forceFullScreen,
 				boolean newView) {
 			// If we don't absolutely need full screen, open the url in the
 			// current screen configuration
 			setFullUrl(fullUrl, forceFullScreen, newView);
 			return true;
 		}
 	}
 
 	private abstract class UserInterfaceState {
 		public abstract void onEnterState();
 
 		public void onLeaveState() {
 		}
 	}
 
 	private class InitialState extends UserInterfaceState {
 		@Override
 		public void onEnterState() {
 			mProgressBar.setVisibility(View.VISIBLE);
 			mWebView.setVisibility(View.GONE);
 			mErrorLayout.setVisibility(View.GONE);
 			mCloseButton.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		public void onLeaveState() {
 			mProgressBar.setVisibility(View.GONE);
 		}
 	}
 
 	private class LoadingState extends UserInterfaceState {
 		@Override
 		public void onEnterState() {
 			mProgressBar.setVisibility(View.VISIBLE);
 		}
 
 		@Override
 		public void onLeaveState() {
 			mProgressBar.setVisibility(View.GONE);
 		}
 	}
 
 	private class WebContentState extends UserInterfaceState {
 		@Override
 		public void onEnterState() {
 			mWebView.setVisibility(View.VISIBLE);
 			mProgressBar.setVisibility(View.GONE);
 
 			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
 					ViewGroup.LayoutParams.MATCH_PARENT,
 					ViewGroup.LayoutParams.MATCH_PARENT, Gravity.TOP);
 			if (ScoreflexUriHelper.isAPIUri(Uri.parse(mWebView.getUrl()))) {
 				layoutParams.setMargins(0, 0, 0, 0);
 			} else {
 				layoutParams.setMargins(
 						0,
 						getResources().getDimensionPixelSize(
 								R.dimen.scoreflex_topbar_height), 0, 0);
 			}
 			mWebView.setLayoutParams(layoutParams);
 
 			// If the scoreflex view is gone, fade it in
 			if (View.GONE == getVisibility()) {
 				setVisibility(View.VISIBLE);
 
 				AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
 				alphaAnimation.setFillBefore(true);
 				alphaAnimation.setFillAfter(true);
 				alphaAnimation.setDuration(1000);
 				startAnimation(alphaAnimation);
 			}
 		}
 	}
 
 	private class ErrorState extends UserInterfaceState {
 		@Override
 		public void onEnterState() {
 			mWebView.setVisibility(View.GONE);
 			mErrorLayout.setVisibility(View.VISIBLE);
 			if (!ScoreflexView.this.mIsPreloading) {
 				Toast.makeText(getContext(), R.string.scoreflex_network_error,
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 		@Override
 		public void onLeaveState() {
 			mErrorLayout.setVisibility(View.GONE);
 		}
 
 	}
 	/**
 	 * Method called when a picture has been chosen for a input type file / image.
 	 *
 	 * @param activity
 	 * @param requestCode
 	 * @param resultCode
 	 * @param intent
 	 */
 	public void onActivityResult(Activity activity, int requestCode,
 			int resultCode, Intent intent) {
 		Uri result = intent == null || resultCode != activity.RESULT_OK ? null
 				: intent.getData();
 		if (result == null) {
 			return;
 		}
 		mUploadMessage.onReceiveValue(result);
 		mUploadMessage = null;
 		mOutputFileUri = null;
 
 	}
 	private BroadcastReceiver mLoginReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(final Context context,final Intent intent) {
     	 if (ScoreflexView.this.isLoginSource) {
     		 ScoreflexView.this.isLoginSource = false;
     		 return;
     	 }
     	 setFullUrl(mWebView.getUrl());
      }
   };
 }
