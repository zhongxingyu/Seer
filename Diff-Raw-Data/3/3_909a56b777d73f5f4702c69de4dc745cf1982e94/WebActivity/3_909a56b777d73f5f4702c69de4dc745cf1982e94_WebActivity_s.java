 package cstdr.ningningcat;
 
 import java.util.LinkedList;
 
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.app.SearchManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.DownloadListener;
 import android.webkit.JsResult;
 import android.webkit.WebBackForwardList;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebSettings.PluginState;
 import android.webkit.WebStorage.QuotaUpdater;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.umeng.analytics.MobclickAgent;
 import com.umeng.fb.NotificationType;
 import com.umeng.fb.UMFeedbackService;
 import com.umeng.update.UmengUpdateAgent;
 import com.umeng.update.UmengUpdateListener;
 import com.umeng.update.UpdateResponse;
 
 import cstdr.ningningcat.constants.Constants;
 import cstdr.ningningcat.constants.EventConstant;
 import cstdr.ningningcat.receiver.ConnectivityReceiver;
 import cstdr.ningningcat.receiver.DownloadCompleteReceiver;
 import cstdr.ningningcat.receiver.DownloadNotificationClickReceiver;
 import cstdr.ningningcat.receiver.GotoReceiver;
 import cstdr.ningningcat.util.CacheUtil;
 import cstdr.ningningcat.util.DatabaseUtil;
 import cstdr.ningningcat.util.DialogUtil;
 import cstdr.ningningcat.util.DownloadUtil;
 import cstdr.ningningcat.util.LOG;
 import cstdr.ningningcat.util.NetworkUtil;
 import cstdr.ningningcat.util.ShareUtil;
 import cstdr.ningningcat.util.ToastUtil;
 import cstdr.ningningcat.util.UIUtil;
 import cstdr.ningningcat.util.UrlUtil;
 import cstdr.ningningcat.widget.DRAutoCompleteTextView;
 import cstdr.ningningcat.widget.DRWebView;
 import cstdr.ningningcat.widget.DRWebView.ScrollInterface;
 import cstdr.ningningcat.widget.layout.WebLayout;
 
 /**
  * 宁宁猫主界面
  * 
  * @author cstdingran@gmail.com
  */
 public class WebActivity extends Activity implements EventConstant {
 
 	private static final String TAG = "WebActivity";
 
 	private final Context mContext = this;
 
 	private WebLayout webLayout;
 
 	private RelativeLayout mWebsiteNavigation;
 
 	private ImageView mAddFavorite;
 
 	private DRAutoCompleteTextView mWebsite;
 
 	private ImageView mRewrite;
 
 	private DRWebView mWebView;
 
 	private WebSettings mWebSettings;
 
 	private long mLastBackPressTimeMillis = 0L;
 
 	private long mLastScrollTimeMillis = 0L;
 
 	private static final int NAVIGATION_HIDE = 1;
 
 	private static final int NAVIGATION_SHOW = 2;
 
 	private BroadcastReceiver mConnectitvityReceiver;
 
 	private BroadcastReceiver mGotoReceiver;
 
 	private BroadcastReceiver mDownloadCompleteReceiver;
 
 	private BroadcastReceiver mDownloadNotificationClickReceiver;
 
 	private static WebBackForwardList mWebBackForwardList;
 
 	private static ArrayAdapter<String> mAutoCompleteAdapter;
 
 	private static LinkedList<String> mHistoryUrlList; // 现在每次加载页面都清空mAutoCompleteAdapter再添加，历史记录暂时保存
 
 	private Animation animNavigationFadeOut;
 
 	private Animation animNavigationFadeIn;
 
 	/** 导航栏显示与隐藏的handler **/
 	private Handler navigationHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			LOG.cstdr(TAG, "msg.what = " + msg.what);
 			switch (msg.what) {
 			case NAVIGATION_HIDE:
 				if (mWebsiteNavigation.isShown()
 						&& (System.currentTimeMillis() - mLastScrollTimeMillis) > 1000) {
 					mWebsiteNavigation.startAnimation(animNavigationFadeOut);
 					mLastScrollTimeMillis = System.currentTimeMillis();
 				}
 				break;
 			case NAVIGATION_SHOW:
 				if (!mWebsiteNavigation.isShown()
 						&& (System.currentTimeMillis() - mLastScrollTimeMillis) > 1000) {
 					mWebsiteNavigation.startAnimation(animNavigationFadeIn);
 					mLastScrollTimeMillis = System.currentTimeMillis();
 				}
 				hideNavigation();
 				break;
 			}
 		}
 	};
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============onCreate============");
 		}
 		super.onCreate(savedInstanceState);
 		UIUtil.initUI(WebActivity.this);
 		initWebLayout();
 		initReceiver();
 		// WebIconDatabase.getInstance().open(getDir("icon",
 		// MODE_PRIVATE).getPath()); // 允许请求网页icon
 		processData();
 	}
 
 	/**
 	 * 初始化浏览网页的主Layout
 	 */
 	private void initWebLayout() {
 		webLayout = new WebLayout(mContext);
 		setContentView(webLayout);
 		initNavigation();
 		initAddFavorite();
 		initWebsite();
 		initRewrite();
 		initWebView();
 	}
 
 	/**
 	 * 初始化广播接收
 	 */
 	private void initReceiver() {
 		if (mConnectitvityReceiver == null) {
 			mConnectitvityReceiver = new ConnectivityReceiver();
 			registerReceiver(mConnectitvityReceiver, new IntentFilter(
 					ConnectivityReceiver.ACTION_CONNECT_CHANGE));
 		}
 		if (mGotoReceiver == null) {
 			mGotoReceiver = new GotoReceiver();
 			registerReceiver(mGotoReceiver, new IntentFilter(
 					GotoReceiver.ACTION_GOTO));
 		}
 		if (mDownloadCompleteReceiver == null) {
 			mDownloadCompleteReceiver = new DownloadCompleteReceiver();
 			registerReceiver(mDownloadCompleteReceiver, new IntentFilter(
 					DownloadCompleteReceiver.ACTION_DOWNLOAD_COMPLETE));
 		}
 		if (mDownloadNotificationClickReceiver == null) {
 			mDownloadNotificationClickReceiver = new DownloadNotificationClickReceiver();
 			registerReceiver(
 					mDownloadNotificationClickReceiver,
 					new IntentFilter(
 							DownloadNotificationClickReceiver.ACTION_NOTIFICATION_CLICK));
 		}
 	}
 
 	/**
 	 * 取消广播接收
 	 */
 	private void unregisterReceiver() {
 		unregisterReceiver(mConnectitvityReceiver);
 		unregisterReceiver(mGotoReceiver);
 		unregisterReceiver(mDownloadCompleteReceiver);
 		unregisterReceiver(mDownloadNotificationClickReceiver);
 	}
 
 	/**
 	 * 初始化重写按钮
 	 */
 	private void initRewrite() {
 		mRewrite = webLayout.getRewrite();
 		mRewrite.setVisibility(View.GONE);
 		mRewrite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				MobclickAgent.onEvent(mContext, NAVIGATION_REWRITE);
 				if (navigationHandler.hasMessages(NAVIGATION_HIDE)) {
 					navigationHandler.removeMessages(NAVIGATION_HIDE);
 				}
 				mWebsite.setText("");
 			}
 		});
 	}
 
 	/**
 	 * 初始化WebView
 	 */
 	private void initWebView() {
 		mWebView = webLayout.getWebview();
 
 		/** WebSettings配置 **/
 		mWebSettings = mWebView.getSettings();
 		mWebSettings.setJavaScriptEnabled(true); // 支持JavaScript
 		mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true); // JS打开新窗口
 		mWebSettings.setBuiltInZoomControls(true); // 支持页面放大缩小按钮
 		mWebSettings.setSupportZoom(true);
 		// mWebSettings.setSupportMultipleWindows(true); // TODO 多窗口
 		mWebSettings.setDefaultTextEncodingName("utf-8"); // 页面编码
 		mWebSettings.setAppCacheEnabled(true); // 支持缓存
 		mWebSettings.setAppCacheMaxSize(Constants.CACHE_MAX_SIZE); // 缓存最大值
 		mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 根据cache-control决定是否从网络上取数据
 		// mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 不使用缓存
 		mWebSettings.setDomStorageEnabled(true); // 设置可以使用localStorage
 		mWebSettings.setPluginState(PluginState.ON); // 若打开flash则需要使用插件
 		// mWebSettings.setEnableSmoothTransition(true); // webview放大缩小更平滑，需要API
 		// 11以上
 		mWebSettings.setLoadsImagesAutomatically(true); // 当GPRS下提示是否加载图片
 		mWebSettings.setUseWideViewPort(true); // 设置页面宽度和屏幕一样
 		mWebSettings.setLoadWithOverviewMode(true); // 设置页面宽度和屏幕一样
 		// mWebSettings.setNeedInitialFocus(true); //
 		// （无效）当webview调用requestFocus时为webview设置节点，这样系统可以自动滚动到指定位置
 		mWebSettings.setSaveFormData(true); // 保存表单数据
 		mWebSettings.setSavePassword(true); // 保存密码
 
 		/** WebView配置 **/
 		mWebView.setScrollbarFadingEnabled(true); // 滚动条自动消失
 		mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // WebView右侧无空隙
 		// mWebView.setInitialScale(100); // 初始缩放比例
 		// mWebView.requestFocusFromTouch(); // 接收触摸焦点
 		mWebView.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				if (!mWebView.hasFocus() || mWebsite.hasFocus()) {
 					mWebsite.clearFocus();
 					mWebView.requestFocusFromTouch(); // 不能用requestFocus()，焦点会乱跑
 				}
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					// 使用户在点击顶部区域可以显示导航栏，再点击隐藏，这样处理其实不恰当
 					if (event.getY() < 48) {
 						if (mWebsiteNavigation.isShown()) {
 							navigationHandler.sendEmptyMessage(NAVIGATION_HIDE);
 						} else {
 							navigationHandler.sendEmptyMessage(NAVIGATION_SHOW);
 						}
 					}
 					break;
 				}
 				return false;
 			}
 		});
 		mWebView.setOnFocusChangeListener(new OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (!hasFocus && navigationHandler.hasMessages(NAVIGATION_HIDE)) {
 					navigationHandler.removeMessages(NAVIGATION_HIDE); // 点击输入框后焦点才发生变化
 				}
 			}
 		});
 		mWebView.setOnScrollChangedListener(new ScrollInterface() {
 
 			@Override
 			public void onScrollChange(int l, int t, int oldl, int oldt) {
 				if ((t - oldt) > 5) {
 					navigationHandler.sendEmptyMessage(NAVIGATION_HIDE);
 				} else if ((oldt - t) > 5) {
 					navigationHandler.sendEmptyMessage(NAVIGATION_SHOW);
 				}
 			}
 		});
 		mWebView.setWebChromeClient(new DRWebChromeClient());
 		mWebView.setWebViewClient(new DRWebViewClient());
 		mWebView.setDownloadListener(new DRDownloadListener());
 	}
 
 	/**
 	 * 初始化导航栏中EditText输入框的配置
 	 */
 	private void initWebsite() {
 		mWebsite = webLayout.getWebsite();
 		mWebsite.setImeOptions(EditorInfo.IME_ACTION_GO);
 		mWebsite.setOnEditorActionListener(new EditText.OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				if (LOG.DEBUG) {
 					LOG.cstdr(TAG, "onEditorAction : actionId=" + actionId);
 				}
 				if (actionId == EditorInfo.IME_ACTION_GO) {
 					MobclickAgent.onEvent(mContext, NAVIGATION_GOTO);
 					gotoByEditText();
 					return true;
 				}
 				return false;
 			}
 		});
 		mAutoCompleteAdapter = new ArrayAdapter<String>(mContext,
 				R.layout.list_autocomplete); // TODO 修改布局文件
 		mHistoryUrlList = new LinkedList<String>();
 		mWebsite.setThreshold(1); // 最小匹配字符为1个字符
 		// mWebsite.setTokenizer(new
 		// MultiAutoCompleteTextView.CommaTokenizer());
 		// 用户必须提供一个MultiAutoCompleteTextView.Tokenizer用来区分不同的子串
 		mWebsite.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view,
 					int position, long arg3) {
 				String titleAndUrl = (String) adapter
 						.getItemAtPosition(position);
 				String url = titleAndUrl.substring(titleAndUrl.indexOf("\n") + 1);
 				if (LOG.DEBUG) {
 					LOG.cstdr(TAG,
 							"mWebsite.setOnItemClickListener : onItemClick -> "
 									+ UrlUtil.httpUrl2Url(url));
 				}
 				loadUrlStr(UrlUtil.url2HttpUrl(url));
 			}
 		});
 		mWebsite.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				if (s.length() == 0) {
 					mRewrite.setVisibility(View.GONE);
 				} else {
 					mRewrite.setVisibility(View.VISIBLE);
 				}
 			}
 		});
 	}
 
 	/**
 	 * 添加导航栏中收藏按鈕配置
 	 */
 	private void initAddFavorite() {
 		mAddFavorite = webLayout.getAdd();
 		mAddFavorite.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				MobclickAgent.onEvent(mContext, NAVIGATION_ADD_FAVORITE);
 				if (navigationHandler.hasMessages(NAVIGATION_HIDE)) {
 					navigationHandler.removeMessages(NAVIGATION_HIDE);
 				}
 				NncApp.getInstance().getHandler().post(new Runnable() {
 
 					@Override
 					public void run() {
 						String title = NncApp.getInstance().getCurrentTitle();
 						String url = NncApp.getInstance().getCurrentUrl();
 						if (FavoriteActivity.hasUrlInDB(url)) {
 							FavoriteActivity
 									.deleteFavorite(mContext, url, null);
 							mAddFavorite
 									.setImageResource(R.drawable.navigation_add_favorite);
 						} else {
 							FavoriteActivity.addFavorite(mContext, title, url);
 							mAddFavorite
 									.setImageResource(R.drawable.navigation_add_favorite_pressed);
 						}
 					}
 				});
 			}
 		});
 	}
 
 	/**
 	 * 初始化RelativeLayout导航栏
 	 */
 	private void initNavigation() {
 		mWebsiteNavigation = webLayout.getNavLayout();
 		animNavigationFadeOut = AnimationUtils.loadAnimation(mContext,
 				R.anim.navigation_fade_out);
 		animNavigationFadeIn = AnimationUtils.loadAnimation(mContext,
 				R.anim.navigation_fade_in);
 		animNavigationFadeOut.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				mWebsiteNavigation.setVisibility(View.GONE);
 			}
 		});
 		animNavigationFadeIn.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				mWebsiteNavigation.setVisibility(View.VISIBLE);
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 			}
 		});
 	}
 
 	/**
 	 * 跳转到在EditText中输入的网址
 	 */
 	private void gotoByEditText() {
 		String url = UrlUtil.checkEditUrl(mWebsite.getText().toString().trim()); // 只有用户输入的URL才应该检查
 		loadUrlStr(url);
 	}
 
 	/**
 	 * 在WebView中跳转到传入的URL
 	 * 
 	 * @param url
 	 */
 	private void loadUrlStr(String url) {
 		UIUtil.hideInputWindow(mWebView);
 		if (url != null) {
 			mWebView.loadUrl(url);
 		} else {
 			ToastUtil.shortToast(mContext, getString(R.string.msg_no_url));
 		}
 	}
 
 	/**
 	 * WebView的WebChromeClient
 	 * 
 	 * @author cstdingran@gmail.com
 	 */
 	class DRWebChromeClient extends WebChromeClient {
 
 		@Override
 		public void onProgressChanged(WebView view, int newProgress) {
 			webLayout.setProgress(newProgress);
 			if (newProgress == 100) {
 				webLayout.setProgressVisibility(View.GONE);
 			} else {
 				webLayout.setProgressVisibility(View.VISIBLE);
 			}
 			super.onProgressChanged(view, newProgress);
 		}
 
 		@Override
 		public void onReceivedIcon(WebView view, Bitmap icon) {
 			// Drawable drawableIcon=new BitmapDrawable(icon); // Bitmap 转换为
 			// Drawable
 			// setFeatureDrawable(Window.FEATURE_LEFT_ICON, drawableIcon);
 			super.onReceivedIcon(view, icon);
 		}
 
 		@Override
 		public void onReceivedTitle(WebView view, String title) {
 			NncApp.getInstance().setCurrentTitle(title);
 			super.onReceivedTitle(view, title);
 		}
 
 		@Override
 		public boolean onJsAlert(WebView view, String url, String message,
 				JsResult result) {
 			DialogUtil.showJsAlertDialog(mContext, message, result);
 			return true;
 		}
 
 		@Override
 		public boolean onJsConfirm(WebView view, String url, String message,
 				JsResult result) {
 			DialogUtil.showJsConfirmDialog(mContext, message, result);
 			return true;
 		}
 
 		@Override
 		public boolean onJsTimeout() {
 			ToastUtil.shortToast(mContext, getString(R.string.msg_timeout));
 			return true;
 		}
 
 		@Override
 		public void onReachedMaxAppCacheSize(long requiredStorage, long quota,
 				QuotaUpdater quotaUpdater) {
 			// 当达到上限时，清理缓存
 			new Thread() {
 
 				@Override
 				public void run() {
 					CacheUtil.clearCache(mContext);
 				}
 			}.start();
 			super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
 		}
 
 		// @Override
 		// public void onGeolocationPermissionsShowPrompt(String origin,
 		// Callback callback) {
 		// callback.invoke(origin, true, false);
 		// super.onGeolocationPermissionsShowPrompt(origin, callback);
 		// }
 
 	}
 
 	/**
 	 * 主WebView的WebViewClient
 	 * 
 	 * @author cstdingran@gmail.com
 	 */
 	class DRWebViewClient extends WebViewClient {
 
 		@Override
 		public boolean shouldOverrideUrlLoading(WebView view, String url) {
 			loadUrlStr(url);
 			return true;
 		}
 
 		@Override
 		public void onPageStarted(WebView view, String url, Bitmap favicon) {
 			if (LOG.DEBUG) {
 				LOG.cstdr(TAG, "onPageStarted -> url = " + url);
 			}
 			mWebsite.setText(UrlUtil.httpUrl2Url(url)); // url除去协议http://
 			NncApp.getInstance().setCurrentUrl(url);
 			if (FavoriteActivity.hasUrlInDB(url)) {
 				mAddFavorite
 						.setImageResource(R.drawable.navigation_add_favorite_pressed);
 			}
 			super.onPageStarted(view, url, favicon);
 		}
 
 		@Override
 		public void onReceivedError(WebView view, int errorCode,
 				String description, String failingUrl) {
 			DialogUtil.showNoConnectDialog(mContext);
 		}
 
 		@Override
 		public void onPageFinished(WebView view, String url) {
 			navigationHandler.sendEmptyMessage(NAVIGATION_SHOW);
 			setAutoComplete(); // 这个位置需要考虑
 		}
 	}
 
 	/**
 	 * 监听下载链接
 	 * 
 	 * @author cstdingran@gmail.com
 	 */
 	class DRDownloadListener implements DownloadListener {
 
 		@Override
 		public void onDownloadStart(String url, String userAgent,
 				String contentDisposition, String mimetype, long contentLength) {
 			if (LOG.DEBUG) {
 				LOG.cstdr(TAG, "MyDownloadListener : mimetype -> " + mimetype);
 			}
 			if (mimetype.equals(Constants.APK_MIMETYPE)) {
 				ToastUtil.shortToast(mContext,
 						getString(R.string.msg_download_start, url));
 				DownloadUtil.startDownload(url, userAgent, contentDisposition,
 						mimetype, contentLength);
 			} else {
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				Uri uri = Uri.parse(url);
 				intent.setDataAndType(uri, mimetype); // 只用setType方法会清除先前放入的data数据
 				startActivity(intent);
 			}
 		}
 	}
 
 	/**
 	 * 初始化菜单
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	/**
 	 * 菜单选项
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG,
 					"onOptionsItemSelected : itemId = " + item.getItemId());
 		}
 		switch (item.getItemId()) {
 		case R.id.menu_favorite: // 查看已收藏页面
 			MobclickAgent.onEvent(mContext, MENU_GOTO_FAVORITE_LIST);
 			Intent intent = new Intent(WebActivity.this, FavoriteActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			startActivity(intent);
 			break;
 		case R.id.menu_share: // 分享
 			MobclickAgent.onEvent(mContext, MENU_SHARE);
 			ShareUtil.shareFavorite(mContext, NncApp.getInstance()
 					.getCurrentTitle(), NncApp.getInstance().getCurrentUrl());
 			break;
 		case R.id.menu_exit: // 退出
 			MobclickAgent.onEvent(mContext, MENU_EXIT);
 			exit();
 			break;
 		case R.id.menu_more: // 更多设置
 			MobclickAgent.onEvent(mContext, MENU_MORE);
 			break;
 		// case R.id.menu_nightmode: // 切换夜间模式（暂时不做） TODO
 		// UIUtil.changeBrightMode(mContext, mActivity);
 		// break;
 		case R.id.menu_download_list:
 			MobclickAgent.onEvent(mContext, MENU_DOWNLOAD_LIST);
 			Intent downloadsIntent = new Intent(
 					DownloadManager.ACTION_VIEW_DOWNLOADS);
 			startActivity(downloadsIntent);
 			break;
 		case R.id.menu_update: // 更新
 			MobclickAgent.onEvent(mContext, MENU_UPDATE);
 			update();
 			break;
 		case R.id.menu_clear_cachedata: // 清除缓存数据（包括缓存文件、表单数据和Cookie）
 			MobclickAgent.onEvent(mContext, MENU_CLEAR_CACHEDATA);
 			new Thread() {
 
 				@Override
 				public void run() {
 					CacheUtil.clearCache(mContext);
 					CacheUtil.clearFormData(mContext);
 					CacheUtil.clearCookie();
 					NncApp.getInstance().getHandler().post(new Runnable() {
 
 						@Override
 						public void run() {
 							ToastUtil.shortToast(mContext,
 									getString(R.string.msg_delete_cachedata));
 						}
 					});
 				}
 			}.start();
 			break;
 		case R.id.menu_report: // 反馈
 			MobclickAgent.onEvent(mContext, MENU_REPORT);
 			UMFeedbackService.enableNewReplyNotification(mContext,
 					NotificationType.NotificationBar);
 			UMFeedbackService.openUmengFeedbackSDK(mContext);
 			break;
 		case R.id.menu_about: // 关于
 			MobclickAgent.onEvent(mContext, MENU_ABOUT);
 			ToastUtil.shortToast(mContext, getString(R.string.msg_about));
 			loadUrlStr(Constants.WEIBO_URL);
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (mWebView.canGoBack()) {
 				mWebView.goBack();
 			} else if (System.currentTimeMillis() - mLastBackPressTimeMillis > 2000) {
 				ToastUtil.shortToast(mContext, getString(R.string.msg_exit));
 				mLastBackPressTimeMillis = System.currentTimeMillis();
 			} else {
 				MobclickAgent.onEvent(this, EXIT_BACK);
 				exit();
 			}
 			return true;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * 更新宁宁猫
 	 */
 	private void update() {
 		if (NetworkUtil.checkNetwork(mContext)) {
 			UmengUpdateAgent.update(mContext);
 			UmengUpdateAgent.setUpdateAutoPopup(false);
 			UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
 
 				@Override
 				public void onUpdateReturned(int updateStatus,
 						UpdateResponse updateInfo) {
 					switch (updateStatus) {
 					case 0: // 有更新
 						UmengUpdateAgent.showUpdateDialog(mContext, updateInfo);
 						break;
 					case 1: // 没有更新
 						ToastUtil.shortToast(mContext,
 								getString(R.string.msg_update_no));
 						break;
 					case 2: // 非Wifi下
 						ToastUtil.shortToast(mContext,
 								getString(R.string.msg_update_nowifi));
 						break;
 					case 3: // 连接超时
 						ToastUtil.shortToast(mContext,
 								getString(R.string.msg_update_timeout));
 						break;
 					}
 				}
 			});
 		} else {
 			ToastUtil.shortToast(mContext, getString(R.string.msg_no_connect));
 		}
 	}
 
 	/**
 	 * 退出前处理数据
 	 */
 	private void exit() {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============exit============");
 		}
 		UIUtil.hideInputWindow(mWebView);
 		finish();
 		MobclickAgent.onKillProcess(mContext);
 		android.os.Process.killProcess(android.os.Process.myPid());
 	}
 
 	@Override
 	protected void onPause() {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============onPause============");
 		}
 		super.onPause();
 		MobclickAgent.onPause(this);
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============onDestroy============");
 		}
 		super.onDestroy();
 		unregisterReceiver();
 	}
 
 	@Override
 	protected void onStop() {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============onStop============");
 		}
 		// if(mSp == null) {
 		// mSp=SPUtil.getSP(mContext, getString(R.string.sp_main));
 		// }
 		// if(mSp.getInt(getString(R.string.spkey_bright_mode_now), -2) == -1) {
 		// UIUtil.changeBrightMode(mContext, mActivity); // 若退出时为夜间模式，再进来也要保持此模式
 		// }
 		super.onStop();
 	}
 
 	@Override
 	protected void onResume() {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============onResume============");
 		}
 		// if(mSp == null) {
 		// mSp=SPUtil.getSP(mContext, getString(R.string.sp_main));
 		// }
 		// if(mSp.getInt(getString(R.string.spkey_bright_mode_last), -2) == -1)
 		// {
 		// UIUtil.changeBrightMode(mContext, mActivity); // 若退出时为夜间模式，再进来也要保持此模式
 		// }
 		UMFeedbackService.enableNewReplyNotification(mContext,
 				NotificationType.NotificationBar);
 		super.onResume();
 		MobclickAgent.onResume(this);
 	}
 
 	/**
 	 * 处理横竖屏切换，在Android 3.2（API
 	 * 13）以后，在manifest中的configChanges需要添加screenSize，否则不会调用该方法
 	 */
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "===========onConfigurationChanged===========");
 		}
 		super.onConfigurationChanged(newConfig);
 	}
 
 	/**
 	 * 得到浏览历史记录
 	 */
 	private void setAutoComplete() {
 		if (!mAutoCompleteAdapter.isEmpty()) {
 			mAutoCompleteAdapter.clear();
 		}
 		mWebBackForwardList = mWebView.copyBackForwardList();
 		String url;
 		String title;
 		int size = mWebBackForwardList.getSize();
 		for (int i = size - 1; i >= 0; i--) {
 			title = mWebBackForwardList.getItemAtIndex(i).getTitle();
 			if (title == null) {
 				title = Constants.TITLE_NULL;
 			}
 			url = mWebBackForwardList.getItemAtIndex(i).getUrl();
 			if (!mHistoryUrlList.contains(url)) {
 				mHistoryUrlList.add(url);
 				if (LOG.DEBUG) {
 					LOG.cstdr(TAG,
 							"setAutoComplete -> " + UrlUtil.httpUrl2Url(url));
 				}
 				mAutoCompleteAdapter.add(title + "\n"
 						+ UrlUtil.httpUrl2Url(url));
 			}
 		}
 		mWebsite.setAdapter(mAutoCompleteAdapter);
 	}
 
 	/**
 	 * 因为singleTask模式，启动时会调用此类
 	 */
 	@Override
 	protected void onNewIntent(Intent intent) {
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "============onNewIntent============");
 		}
 		setIntent(intent);
 		processData();
 	}
 
 	/**
 	 * 处理intent传来的数据
 	 * 
 	 * @param intent
 	 */
 	private void processData() {
 		Intent intent = getIntent();
 		String action = intent.getAction();
 		if (LOG.DEBUG) {
 			LOG.cstdr(TAG, "processData : action =  " + action);
 		}
 		if (action != null) {
 			if (action.equals(GotoReceiver.ACTION_GOTO)) { // 内部跳转请求，如收藏夹点击
 				MobclickAgent.onEvent(mContext,
 						ACTION_GOTO_FAVORITE_LIST_ITEM_CLICK);
 				String url = intent.getStringExtra(DatabaseUtil.COLUMN_URL);
 				if (LOG.DEBUG) {
 					LOG.cstdr(TAG, "processData : url =  " + url);
 				}
 				loadUrlStr(url);
 			} else if (action.equals(Intent.ACTION_VIEW)
 					|| action.equals(Intent.ACTION_MAIN)) { // 处理外部请求，包括链接请求和桌面快捷方式请求
 				MobclickAgent.onEvent(mContext, ACTION_GOTO_INTENT);
 				Uri uri = intent.getData();
 				if (LOG.DEBUG) {
 					LOG.cstdr(TAG, "processData : uri =  " + uri);
 				}
 				loadUrlStr(UrlUtil.url2HttpUrl(uri.toString()));
 			} else if (action.equals(Intent.ACTION_WEB_SEARCH)) { // 处理谷歌搜索请求
 				MobclickAgent.onEvent(mContext, ACTION_GOTO_WEB_SEARCH);
 				String words = intent.getStringExtra(SearchManager.QUERY);
 				if (LOG.DEBUG) {
 					LOG.cstdr(TAG, "processData : words =  " + words);
 				}
 				loadUrlStr(Constants.GOOGLE_URL + words);
 			}
 		} else {
 			loadUrlStr(NncApp.getInstance().getCurrentUrl()); // 加载在initSharedPreferences方法中获取到的首页
 		}
 	}
 
 	/**
 	 * 隐藏导航栏
 	 */
 	private void hideNavigation() {
 		// 2秒后导航栏自动消失
 		navigationHandler.removeMessages(NAVIGATION_HIDE);
 		navigationHandler.sendEmptyMessageDelayed(NAVIGATION_HIDE, 2000);
 	}
 
 }
