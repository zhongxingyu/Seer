 package cstdr.ningningcat;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Process;
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
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.EditorInfo;
 import android.webkit.DownloadListener;
 import android.webkit.GeolocationPermissions.Callback;
 import android.webkit.JsResult;
 import android.webkit.WebBackForwardList;
 import android.webkit.WebChromeClient;
 import android.webkit.WebIconDatabase;
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
 import com.umeng.fb.util.FeedBackListener;
 import com.umeng.update.UmengUpdateAgent;
 import com.umeng.update.UmengUpdateListener;
 import com.umeng.update.UpdateResponse;
 
 import cstdr.ningningcat.constants.Constants;
 import cstdr.ningningcat.constants.EventConstant;
 import cstdr.ningningcat.receiver.ConnectivityReceiver;
 import cstdr.ningningcat.receiver.GotoReceiver;
 import cstdr.ningningcat.util.DatabaseUtil;
 import cstdr.ningningcat.util.DialogUtil;
 import cstdr.ningningcat.util.LOG;
 import cstdr.ningningcat.util.SPUtil;
 import cstdr.ningningcat.util.ShareUtil;
 import cstdr.ningningcat.util.ShortcutUtil;
 import cstdr.ningningcat.util.ToastUtil;
 import cstdr.ningningcat.util.UIUtil;
 import cstdr.ningningcat.util.UrlUtil;
 import cstdr.ningningcat.widget.MyAutoCompleteTextView;
 import cstdr.ningningcat.widget.MyWebView;
 import cstdr.ningningcat.widget.MyWebView.ScrollInterface;
 
 /**
  * 宁宁猫主界面
  * @author cstdingran@gmail.com
  */
 public class MainActivity extends Activity implements EventConstant {
 
     private static final String TAG="MainActivity";
 
     private final Context mContext=this;
 
     private static MainActivity mInstance;
 
     private FavoriteActivity mFavorite;
 
     private RelativeLayout mWebsiteNavigation;
 
     private ImageView mAddFavorite=null;
 
     private MyAutoCompleteTextView mWebsite=null;
 
     private ImageView mRewrite=null;
 
     private ImageView mGoto=null;
 
     private MyWebView mWebView=null;
 
     private WebSettings mWebSettings=null;
 
     private String mEditUrl=null;
 
     private String mCurrentUrl=null;
 
     private String mCurrentTitle=null;
 
     private long mLastBackPressTimeMillis=0L;
 
     private long mLastScrollTimeMillis=0L;
 
     private static final int NAVIGATION_HIDE=1;
 
     private static final int NAVIGATION_SHOW=2;
 
     private SharedPreferences mSp=null;
 
     private BroadcastReceiver mConnectitvityReceiver=null;
 
     private BroadcastReceiver mGotoReceiver=null;
 
     private boolean isNetworkMode=false;
 
     private static WebBackForwardList mWebBackForwardList;
 
     private static ArrayAdapter<String> mAutoCompleteAdapter;
 
     private static List<String> mHistoryUrlList; // 现在每次加载页面都清空mAutoCompleteAdapter再添加，历史记录暂时保存
 
     private Animation animNavigationFadeOut;
 
     private Animation animNavigationFadeIn;
 
     private Handler handler;
 
     /** 导航栏显示与隐藏的handler **/
     private Handler navigationHandler=new Handler() {
 
         @Override
         public void handleMessage(Message msg) {
             LOG.cstdr(TAG, "msg.what = " + msg.what);
             switch(msg.what) {
                 case NAVIGATION_HIDE:
                     if(mWebsiteNavigation.isShown() && (System.currentTimeMillis() - mLastScrollTimeMillis) > 1000) {
                         mWebsiteNavigation.startAnimation(animNavigationFadeOut);
                         mLastScrollTimeMillis=System.currentTimeMillis();
                     }
                     break;
                 case NAVIGATION_SHOW:
                     if(!mWebsiteNavigation.isShown() && (System.currentTimeMillis() - mLastScrollTimeMillis) > 1000) {
                         mWebsiteNavigation.startAnimation(animNavigationFadeIn);
                         mLastScrollTimeMillis=System.currentTimeMillis();
                     }
                     hideNavigation();
                     break;
             }
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if(LOG.DEBUG) {
             LOG.cstdr(TAG, "============onCreate============");
         }
         // 友盟在线更新配置
         MobclickAgent.updateOnlineConfig(this);
 
         // 必须开始就设置
         requestWindowFeature(Window.FEATURE_PROGRESS);
         requestWindowFeature(Window.FEATURE_LEFT_ICON);
 
         setContentView(R.layout.activity_main);
 
         mInstance=this;
         handler=new Handler();
         mFavorite=new FavoriteActivity();
 
         initUMeng();
         initReceiver();
         initView();
         initSharedPreferences();
 
         WebIconDatabase.getInstance().open(getDir("icon", MODE_PRIVATE).getPath()); // 允许请求网页icon
 
         processData();
 
     }
 
     /**
      * 初始化友盟组件
      */
     private void initUMeng() {
         FeedBackListener listener=new FeedBackListener() {
 
             @Override
             public void onSubmitFB(Activity activity) {
                 EditText phoneText=(EditText)activity.findViewById(R.id.feedback_phone);
                 EditText qqText=(EditText)activity.findViewById(R.id.feedback_qq);
                 EditText nameText=(EditText)activity.findViewById(R.id.feedback_name);
                 EditText emailText=(EditText)activity.findViewById(R.id.feedback_email);
                 Map<String, String> contactMap=new HashMap<String, String>();
                 contactMap.put("phone", phoneText.getText().toString());
                 contactMap.put("qq", qqText.getText().toString());
                 UMFeedbackService.setContactMap(contactMap);
                 Map<String, String> remarkMap=new HashMap<String, String>();
                 remarkMap.put("name", nameText.getText().toString());
                 remarkMap.put("email", emailText.getText().toString());
                 UMFeedbackService.setRemarkMap(remarkMap);
             }
 
             @Override
             public void onResetFB(Activity activity, Map<String, String> contactMap, Map<String, String> remarkMap) {
                 EditText phoneText=(EditText)activity.findViewById(R.id.feedback_phone);
                 EditText qqText=(EditText)activity.findViewById(R.id.feedback_qq);
                 EditText nameText=(EditText)activity.findViewById(R.id.feedback_name);
                 EditText emailText=(EditText)activity.findViewById(R.id.feedback_email);
                 if(remarkMap != null) {
                     nameText.setText(remarkMap.get("name"));
                     emailText.setText(remarkMap.get("email"));
                 }
                 if(contactMap != null) {
                     phoneText.setText(contactMap.get("phone"));
                     qqText.setText(contactMap.get("qq"));
                 }
             }
         };
         UMFeedbackService.setFeedBackListener(listener);
     }
 
     /**
      * 初始化广播接收
      */
     private void initReceiver() {
         if(mConnectitvityReceiver == null) {
             mConnectitvityReceiver=new ConnectivityReceiver();
         }
         IntentFilter filter=new IntentFilter(ConnectivityReceiver.ACTION_CONNECT_CHANGE);
         registerReceiver(mConnectitvityReceiver, filter);
 
         if(mGotoReceiver == null) {
             mGotoReceiver=new GotoReceiver();
         }
         filter=new IntentFilter(GotoReceiver.ACTION_GOTO);
         registerReceiver(mGotoReceiver, filter);
     }
 
     /**
      * 取消广播接收
      */
     private void unregisterReceiver() {
         unregisterReceiver(mConnectitvityReceiver);
         unregisterReceiver(mGotoReceiver);
     }
 
     /**
      * 初始化SharedPreferences
      */
     private void initSharedPreferences() {
         mSp=SPUtil.getSP(mContext, getString(R.string.sp_main));
         if(mSp.getString(getString(R.string.spkey_first_launch_time), null) != null) {
             SPUtil.commitStrArrayToSP(mSp, new String[]{getString(R.string.spkey_last_launch_time)},
                 new String[]{String.valueOf(System.currentTimeMillis())});
         } else {
             // if(!ShortcutUtil.hasShortcut(mContext)) { // 测试发现某些机型报错
             ShortcutUtil.addShortcut(mContext);
             // }
             ToastUtil.longToast(mContext, getString(R.string.msg_first_launch));
             SPUtil.commitStrArrayToSP(mSp, new String[]{getString(R.string.spkey_first_launch_time)},
                 new String[]{String.valueOf(System.currentTimeMillis())});
         }
 
         mCurrentUrl=mSp.getString(getString(R.string.spkey_index), getString(R.string.index)); // 获取首页
     }
 
     /**
      * 初始化各种View
      */
     private void initView() {
 
         initNavigation();
 
         initAddFavorite();
 
         initWebsite();
 
         initRewrite();
 
         initGoto();
 
         initWebView();
 
     }
 
     /**
      * 初始化重写按钮
      */
     private void initRewrite() {
         mRewrite=(ImageView)findViewById(R.id.iv_rewrite);
         mRewrite.setVisibility(View.GONE);
         mRewrite.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 MobclickAgent.onEvent(mContext, NAVIGATION_REWRITE);
                 mWebsite.setText("");
             }
         });
     }
 
     /**
      * 初始化WebView
      */
     private void initWebView() {
         mWebView=(MyWebView)findViewById(R.id.wv_web);
 
         /** WebSettings配置 **/
         mWebSettings=mWebView.getSettings();
         mWebSettings.setJavaScriptEnabled(true); // 支持JavaScript
         mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true); // JS打开新窗口
         mWebSettings.setBuiltInZoomControls(true); // 支持页面放大缩小按钮
         mWebSettings.setSupportZoom(true);
         // mWebSettings.setSupportMultipleWindows(true); // TODO 多窗口
         mWebSettings.setDefaultTextEncodingName("utf-8"); // 页面编码
         mWebSettings.setAppCacheEnabled(false); // 支持缓存，不使用缓存
         // mWebSettings.setAppCacheMaxSize(Constants.CACHE_MAX_SIZE); // 缓存最大值
         // mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 优先使用缓存，在程序退出时清理
         mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 不使用缓存
         mWebSettings.setDomStorageEnabled(true); // 设置可以使用localStorage
         mWebSettings.setPluginState(PluginState.ON); // 若打开flash则需要使用插件
         mWebSettings.setPluginsEnabled(true);
         mWebSettings.setLoadsImagesAutomatically(true); // 当GPRS下提示是否加载图片
         // mWebSettings.setUseWideViewPort(true); // 设置页面宽度和屏幕一样
         // mWebSettings.setLoadWithOverviewMode(true); // 设置页面宽度和屏幕一样
         // mWebSettings.setNeedInitialFocus(true); // （无效）当webview调用requestFocus时为webview设置节点，这样系统可以自动滚动到指定位置
         mWebSettings.setSaveFormData(true); // 保存表单数据
         mWebSettings.setSavePassword(true); // 保存密码
 
         /** WebView配置 **/
         mWebView.setScrollbarFadingEnabled(true); // 滚动条自动消失
         mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); // WebView右侧无空隙
         mWebView.setVisibility(View.VISIBLE);
         // mWebView.setInitialScale(100); // 初始缩放比例
         // mWebView.requestFocusFromTouch(); // 接收触摸焦点
         mWebView.setOnTouchListener(new OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 if(!mWebView.hasFocus() || mWebsite.hasFocus()) {
                     mWebsite.clearFocus();
                     mWebView.requestFocusFromTouch(); // 不能用requestFocus()，焦点会乱跑
                 }
                 switch(event.getAction()) {
                     case MotionEvent.ACTION_DOWN:
                        // 使用户在点击顶部区域可以显示导航栏，再点击隐藏，这样处理其实不恰当
                        if(event.getY() < 48) {
                            if(mWebsiteNavigation.isShown()) {
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
                 if(!hasFocus) {
                     navigationHandler.removeMessages(1); // 点击输入框后焦点才发生变化
                 }
             }
         });
         mWebView.setOnScrollChangedListener(new ScrollInterface() { // TODO
 
                 @Override
                 public void onScrollChange(int l, int t, int oldl, int oldt) {
                     if((t - oldt) > 5) {
                         navigationHandler.sendEmptyMessage(NAVIGATION_HIDE);
                     } else if((oldt - t) > 5) {
                         navigationHandler.sendEmptyMessage(NAVIGATION_SHOW);
                     }
                 }
             });
         mWebView.setWebChromeClient(new MyWebChromeClient());
         mWebView.setWebViewClient(new MyWebViewClient());
         mWebView.setDownloadListener(new MyDownloadListener());
     }
 
     /**
      * 初始化导航栏中跳转按钮的配置
      */
     private void initGoto() {
         mGoto=(ImageView)findViewById(R.id.iv_goto);
         mGoto.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 MobclickAgent.onEvent(mContext, NAVIGATION_GOTO);
                 gotoByEditText();
             }
         });
     }
 
     /**
      * 初始化导航栏中EditText输入框的配置
      */
     private void initWebsite() {
         mWebsite=(MyAutoCompleteTextView)findViewById(R.id.actv_website);
         mWebsite.setImeOptions(EditorInfo.IME_ACTION_GO);
         mWebsite.setOnEditorActionListener(new EditText.OnEditorActionListener() {
 
             @Override
             public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                 if(LOG.DEBUG) {
                     LOG.cstdr(TAG, "onEditorAction : actionId=" + actionId);
                 }
                 if(actionId == EditorInfo.IME_ACTION_GO) {
                     MobclickAgent.onEvent(mContext, NAVIGATION_GOTO);
                     gotoByEditText();
                     return true;
                 }
                 return false;
             }
         });
         mAutoCompleteAdapter=new ArrayAdapter<String>(mContext, R.layout.list_autocomplete);
         mHistoryUrlList=new LinkedList<String>();
         mWebsite.setThreshold(1); // 最小匹配字符为1个字符
         // mWebsite.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer()); //
         // 用户必须提供一个MultiAutoCompleteTextView.Tokenizer用来区分不同的子串
         mWebsite.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                 // if(position == 0) { // 第一个位置用来百度搜索输入字段
                 // String str=(String)adapter.getItemAtPosition(0);
                 // if(LOG.DEBUG) {
                 // LOG.cstdr("mWebsite.setOnItemClickListener-str=" + str);
                 // }
                 // String searchStr=str.substring(5);
                 // String searchUrl="http://wap.baidu.com/s?word=" + searchStr;
                 // loadUrl(searchUrl);
                 // } else {
                 String titleAndUrl=(String)adapter.getItemAtPosition(position);
                 String url=titleAndUrl.substring(titleAndUrl.indexOf("\n") + 1);
                 if(LOG.DEBUG) {
                     LOG.cstdr(TAG, "mWebsite.setOnItemClickListener : onItemClick -> " + UrlUtil.httpUrl2Url(url));
                 }
                 loadUrl(UrlUtil.url2HttpUrl(url));
                 // }
             }
         });
         // mWebsite.setDropDownAnchor(R.id.tv_dropdown);
         mWebsite.addTextChangedListener(new TextWatcher() {
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
             }
 
             @Override
             public void afterTextChanged(Editable s) { // TODO
                 // mAutoCompleteAdapter.clear();
                 // mAutoCompleteAdapter.add("百度搜索:" + s);
                 // mAutoCompleteAdapter.insert("百度搜索:" + s.toString(), 0);
                 // mDropdown.setText("百度搜索:" + s.toString());
                 // setAutoComplete();
                 if(s.length() == 0) {
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
         mAddFavorite=(ImageView)findViewById(R.id.iv_add);
         mAddFavorite.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 MobclickAgent.onEvent(mContext, NAVIGATION_ADD_FAVORITE);
                 MainActivity.getInstance().getHandler().post(new Runnable() {
 
                     @Override
                     public void run() {
                         android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                         mFavorite.insertFavorite(mCurrentTitle, mCurrentUrl);
                     }
                 });
             }
         });
     }
 
     /**
      * 初始化RelativeLayout导航栏
      */
     private void initNavigation() {
         mWebsiteNavigation=(RelativeLayout)findViewById(R.id.rl_website_navigation);
         animNavigationFadeOut=AnimationUtils.loadAnimation(mContext, R.anim.navigation_fade_out);
         animNavigationFadeIn=AnimationUtils.loadAnimation(mContext, R.anim.navigation_fade_in);
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
         mEditUrl=mWebsite.getText().toString().trim();
         String url=UrlUtil.checkEditUrl(mEditUrl); // 只有用户输入的URL才应该检查
         loadUrl(url);
     }
 
     /**
      * 在WebView中跳转到传入的URL
      * @param url
      */
     private void loadUrl(String url) {
         UIUtil.hideInputWindow(mWebView);
         if(url != null) {
             mWebView.loadUrl(url);
         } else {
             ToastUtil.shortToast(mContext, getString(R.string.msg_no_url));
         }
     }
 
     /**
      * WebView的WebChromeClient
      * @author cstdingran@gmail.com
      */
     class MyWebChromeClient extends WebChromeClient {
 
         @Override
         public void onProgressChanged(WebView view, int newProgress) {
             setProgress(newProgress * 100);
             super.onProgressChanged(view, newProgress);
         }
 
         @Override
         public void onReceivedIcon(WebView view, Bitmap icon) {
             Drawable drawableIcon=new BitmapDrawable(icon); // Bitmap 转换为 Drawable
             setFeatureDrawable(Window.FEATURE_LEFT_ICON, drawableIcon);
             super.onReceivedIcon(view, icon);
         }
 
         @Override
         public void onReceivedTitle(WebView view, String title) {
             mCurrentTitle=title;
             setTitle(title + "-" + getString(R.string.app_name));
             super.onReceivedTitle(view, title);
         }
 
         @Override
         public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
             DialogUtil.showJsAlertDialog(mContext, message, result);
             return true;
         }
 
         @Override
         public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
             DialogUtil.showJsConfirmDialog(mContext, message, result);
             return true;
         }
 
         @Override
         public boolean onJsTimeout() {
             ToastUtil.shortToast(mContext, getString(R.string.msg_timeout));
             return true;
         }
 
         @Override
         public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
             ToastUtil.shortToast(mContext, getString(R.string.msg_cache_max_size));
             super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
         }
 
         @Override
         public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
             callback.invoke(origin, true, false);
             super.onGeolocationPermissionsShowPrompt(origin, callback);
         }
 
     }
 
     /**
      * 主WebView的WebViewClient
      * @author cstdingran@gmail.com
      */
     class MyWebViewClient extends WebViewClient {
 
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             loadUrl(url);
             return true;
         }
 
         @Override
         public void onPageStarted(WebView view, String url, Bitmap favicon) {
             if(LOG.DEBUG) {
                 LOG.cstdr(TAG, "onPageStarted -> url = " + url);
             }
             if(!url.equals(Constants.ERROR_URL)) { // 当显示出错页面时，输入框网址不变
                 mWebsite.setText(UrlUtil.httpUrl2Url(url)); // url除去协议http://
                 mCurrentUrl=url;
             }
             super.onPageStarted(view, url, favicon);
         }
 
         @Override
         public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
             // 不显示默认出错信息，采用自己的出错页面
             view.clearView();
             view.loadUrl(Constants.ERROR_URL);
         }
 
         @Override
         public void onPageFinished(WebView view, String url) {
             navigationHandler.sendEmptyMessage(NAVIGATION_SHOW);
             setAutoComplete(); // 这个位置需要考虑
         }
     }
 
     /**
      * 监听下载链接
      * @author cstdingran@gmail.com
      */
     class MyDownloadListener implements DownloadListener {
 
         @Override
         public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
             if(LOG.DEBUG) {
                 LOG.cstdr(TAG, "MyDownloadListener : mimetype -> " + mimetype);
             }
             Intent intent=new Intent(Intent.ACTION_VIEW);
             Uri uri=Uri.parse(url);
             // 下载链接时调用系统浏览器下载 TODO
             if(mimetype.equals("application/vnd.android.package-archive")) {
                 ToastUtil.longToast(mContext, getString(R.string.msg_download));
                 intent.setData(uri);
                 startActivity(intent);
             } else {
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
         if(LOG.DEBUG) {
             LOG.cstdr(TAG, "onOptionsItemSelected : itemId = " + item.getItemId());
         }
         switch(item.getItemId()) {
             case R.id.menu_favorite: // 查看已收藏页面
                 MobclickAgent.onEvent(mContext, MENU_GOTO_FAVORITE_LIST);
                 Intent intent=new Intent(MainActivity.this, FavoriteActivity.class);
                 startActivity(intent);
                 break;
             case R.id.menu_share: // 分享
                 MobclickAgent.onEvent(mContext, MENU_SHARE);
                 ShareUtil.shareFavorite(mContext, mCurrentTitle, mCurrentUrl);
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
             case R.id.menu_report: // 反馈
                 MobclickAgent.onEvent(mContext, MENU_REPORT);
                 UMFeedbackService.enableNewReplyNotification(mContext, NotificationType.NotificationBar);
                 UMFeedbackService.openUmengFeedbackSDK(mContext);
                 break;
             case R.id.menu_update: // 更新
                 MobclickAgent.onEvent(mContext, MENU_UPDATE);
                 update();
                 break;
            case R.id.menu_about: // 关于
                 MobclickAgent.onEvent(mContext, MENU_ABOUT);
                 ToastUtil.shortToast(mContext, "正在跳转至我的微博:)");
                 loadUrl(Constants.WEIBO_URL);
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if(keyCode == KeyEvent.KEYCODE_BACK) {
             if(mWebView.canGoBack()) {
                 mWebView.goBack();
             } else if(System.currentTimeMillis() - mLastBackPressTimeMillis > 2000) {
                 ToastUtil.shortToast(mContext, getString(R.string.msg_exit));
                 mLastBackPressTimeMillis=System.currentTimeMillis();
             } else {
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
         UmengUpdateAgent.update(mContext);
         UmengUpdateAgent.setUpdateAutoPopup(false);
         UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
 
             @Override
             public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                 switch(updateStatus) {
                     case 0: // 有更新
                         UmengUpdateAgent.showUpdateDialog(mContext, updateInfo);
                         break;
                     case 1: // 没有更新
                         ToastUtil.shortToast(mContext, "宁宁猫正在努力开发中，暂时没有更新哦~");
                         break;
                     case 2: // 非Wifi下
                         ToastUtil.shortToast(mContext, "没有连接Wifi，还是省点流量吧~");
                         break;
                     case 3: // 连接超时
                         ToastUtil.shortToast(mContext, "联网出现超时，等会再试吧~");
                         break;
                 }
             }
         });
     }
 
     /**
      * 退出前处理数据
      */
     private void exit() {
         MobclickAgent.onEvent(this, EXIT_BACK);
         UIUtil.hideInputWindow(mWebView);
         unregisterReceiver();
         finish();
     }
 
     @Override
     protected void onPause() {
         if(LOG.DEBUG) {
             LOG.cstdr(TAG, "============onPause============");
         }
         super.onPause();
         MobclickAgent.onPause(this);
     }
 
     @Override
     protected void onDestroy() {
         LOG.cstdr(TAG, "============onDestroy============");
         MobclickAgent.onKillProcess(mContext);
         android.os.Process.killProcess(android.os.Process.myPid());
         super.onDestroy();
     }
 
     @Override
     protected void onStop() {
         if(LOG.DEBUG) {
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
         if(LOG.DEBUG) {
             LOG.cstdr(TAG, "============onResume============");
         }
         // if(mSp == null) {
         // mSp=SPUtil.getSP(mContext, getString(R.string.sp_main));
         // }
         // if(mSp.getInt(getString(R.string.spkey_bright_mode_last), -2) == -1) {
         // UIUtil.changeBrightMode(mContext, mActivity); // 若退出时为夜间模式，再进来也要保持此模式
         // }
         UMFeedbackService.enableNewReplyNotification(mContext, NotificationType.NotificationBar);
         super.onResume();
         MobclickAgent.onResume(this);
     }
 
     /**
      * 得到浏览历史记录
      */
     private void setAutoComplete() {
         if(!mAutoCompleteAdapter.isEmpty()) {
             mAutoCompleteAdapter.clear();
         }
         mWebBackForwardList=mWebView.copyBackForwardList();
         String url;
         String title;
         int size=mWebBackForwardList.getSize();
         for(int i=size - 1; i >= 0; i--) {
             title=mWebBackForwardList.getItemAtIndex(i).getTitle();
             if(title == null) {
                 title=Constants.TITLE_NULL;
             }
             url=mWebBackForwardList.getItemAtIndex(i).getUrl();
             if(!mHistoryUrlList.contains(url)) {
                 mHistoryUrlList.add(url);
                 if(LOG.DEBUG) {
                     LOG.cstdr(TAG, "setAutoComplete -> " + UrlUtil.httpUrl2Url(url));
                 }
                 mAutoCompleteAdapter.add(title + "\n" + UrlUtil.httpUrl2Url(url));
             }
         }
         mWebsite.setAdapter(mAutoCompleteAdapter);
     }
 
     /**
      * 因为singleTask模式，启动时会调用此类
      */
     @Override
     protected void onNewIntent(Intent intent) {
         setIntent(intent);
         processData();
     }
 
     /**
      * 处理intent传来的数据
      * @param intent
      */
     private void processData() {
         Intent intent=getIntent();
         String action=intent.getAction();
         if(LOG.DEBUG) {
             LOG.cstdr(TAG, "processData : action =  " + action);
         }
         if(action != null) {
             if(action.equals(GotoReceiver.ACTION_GOTO)) { // 内部跳转请求，如收藏夹点击
                 String url=intent.getStringExtra(DatabaseUtil.COLUMN_URL);
                 MobclickAgent.onEvent(mContext, ACTION_GOTO_FAVORITE_LIST_ITEM_CLICK);
                 loadUrl(url);
             } else if(action.equals(GotoReceiver.ACTION_VIEW) || action.equals(Intent.ACTION_MAIN)) { // 处理外部请求，包括链接请求和桌面快捷方式请求
                 Uri uri=intent.getData();
                 if(LOG.DEBUG) {
                     LOG.cstdr(TAG, "processData : uri =  " + uri);
                 }
                 if(uri != null) {
                     MobclickAgent.onEvent(mContext, ACTION_GOTO_INTENT);
                     loadUrl(UrlUtil.url2HttpUrl(uri.toString()));
                 } else {
                     loadUrl(getString(R.string.index)); // 加载首页
                 }
             }
         } else {
             loadUrl(mCurrentUrl); // 加载在initSharedPreferences方法中获取到的首页
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
 
     /**
      * 重新加载当前网址，用于网络重新连通后
      */
     public void reload() {
         loadUrl(mCurrentUrl);
     }
 
     // //////////////////////////////////////////////////////////////////////////////////
     // get and set
     // //////////////////////////////////////////////////////////////////////////////////
 
     public Context getContext() {
         return mContext;
     }
 
     /**
      * 获取实例
      * @return
      */
     public static MainActivity getInstance() {
         if(mInstance == null) {
             mInstance=new MainActivity();
         }
         return mInstance;
     }
 
     /**
      * 是否网络模式
      * @return
      */
     public boolean isNetworkMode() {
         return isNetworkMode;
     }
 
     public void setNetworkMode(boolean isNetworkMode) {
         this.isNetworkMode=isNetworkMode;
     }
 
     public WebView getWebView() {
         return mWebView;
     }
 
     public Handler getHandler() {
         if(handler == null) {
             handler=new Handler();
         }
         return handler;
     }
 
     public SharedPreferences getSp() {
         if(mSp == null) {
             mSp=SPUtil.getSP(mContext, getString(R.string.sp_main));
         }
         return mSp;
     }
 
     public String getCurrentUrl() {
         return mCurrentUrl;
     }
 
 }
