 
 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.polyvi.xface.view;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebChromeClient.CustomViewCallback;
 import android.widget.FrameLayout;
 
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.app.XWhiteList;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.core.XJSNativeBridge;
 import com.polyvi.xface.event.XIWebAppEventListener;
 import com.polyvi.xface.plugin.api.XPluginBase;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XLog;
 import com.polyvi.xface.util.XStringUtils;
 import com.polyvi.xface.util.XStrings;
 import com.polyvi.xface.util.XUtils;
 
 /**
  * application对应的WebView封装，用于显示application
  */
 public class XAppWebView extends WebView{
 
     public static final String CLASS_NAME = "XAppWebView";
 
     public static final int EMPTPY_VIEW_ID = Integer.MAX_VALUE;
     private static final String JS_INTERFACE_NAME = "_addJavaInterface";
 
 
     private int mViewId; /** < view id，每个view的id唯一 */
     private XIWebAppEventListener mWebAppEventListener; /** app事件监听器 */
     private XTouchEventHandler mTouchEventHandler;  /** < 用来处理touch事件，目前处理了双击时的缩放 */
     private XISystemContext mSystemContext;
 
     private boolean mIsJsInitFinshed = false;/**< js是否加载完成的标志*/
     /** 该视图是否有效 */
     private boolean mIsValid;
 
     private XApplication mOwnerApp;
 
     /** 自定义HTML5视频视图 */
     private View mCustomVideoView;
     private CustomViewCallback mCustomViewCallback;
     /** HTML5视频视图的布局 */
     static final FrameLayout.LayoutParams COVER_SCREEN_GRAVITY_CENTER =
             new FrameLayout.LayoutParams(
             ViewGroup.LayoutParams.MATCH_PARENT,
             ViewGroup.LayoutParams.MATCH_PARENT,
             Gravity.CENTER);
 
     public XAppWebView(XISystemContext systemContext,
             XApplication app) {
         super(systemContext.getContext());
         mOwnerApp = app;
         mViewId = XUtils.generateRandomId();
         mTouchEventHandler = new XTouchEventHandler();
         mSystemContext = systemContext;
         init();
     }
 
     /**
      * view的初始化 主要设置一些view的回调函数以及设置view本身的属性
      */
     private void init() {
         WebSettings settings = getSettings();
         settings.setJavaScriptEnabled(true);
         settings.setJavaScriptCanOpenWindowsAutomatically(true);
 
         settings.setDomStorageEnabled(true);
         settings.setDatabaseEnabled(true);
         String databasePath = this.getContext().getApplicationContext()
                 .getDir("database", Context.MODE_PRIVATE).getPath();
         settings.setDatabasePath(databasePath);
 
         // 用于实现用户手动的缩放
         settings.setBuiltInZoomControls(false);
 
         // 使viewport中的width标签生效
         settings.setUseWideViewPort(true);
         settings.setLoadWithOverviewMode(true);
         settings.setNeedInitialFocus(false);
 
         setWebChromeClient(new XWebChromeClient(getContext(), this));
         setWebViewClient(new XWebViewClient(mSystemContext, this));
         this.requestFocus();
         this.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
         // TODO: 其它的初始化工作
     }
 
     public void exposeJsInterface( XJSNativeBridge js )
     {
           //当在2.3的模拟器上使用的时候 会有问题 所以不使用addJavaInterface
             if(Build.VERSION.RELEASE.startsWith("2.3") && Build.MANUFACTURER.equals("unknown"))
             {
                 return;
             }
            this.addJavascriptInterface(js, JS_INTERFACE_NAME);
     }
     /**
      * 处理双击以及多次连击时自动放大的情况
      *
      * @param event 交互事件
      *
      * @return 如果事件被处理了返回true，否则返回false
      */
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if( event.getAction() == MotionEvent.ACTION_DOWN) {
             mOwnerApp.resetIdleWatcher();
         }
         mTouchEventHandler.handleTouchEvent(event, this);
         return super.onTouchEvent(event);
     }
 
     /**
      * 加载app
      */
     private void loadApp(String url) {
         /**白名单检查*/
         XWhiteList whiteList = mOwnerApp.getAppInfo().getWhiteList();
         if(!url.startsWith("file://")  &&
                 (null != whiteList && !whiteList.isUrlWhiteListed(url))) {
             XLog.e(CLASS_NAME, "url is not in white list");
             return;
         }
         loadTimeoutCheck();
         this.loadUrl(url);
         this.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 // 重载onLongClick，阻止Android 4.0系统中出现默认text selection行为.
                 // 经过测试，不影响touchstart, touchmove, touchend等events
                 return true;
             }
         });
     }
 
     private void loadTimeoutCheck() {
         String timeoutValue = XConfiguration.getInstance().readLoadUrlTimeout();
         if (!XStringUtils.isEmptyString(timeoutValue)) {
             final int loadUrlTimeoutValue = Integer.parseInt(timeoutValue);
             final Runnable timeoutCheck = new Runnable() {
                 public void run() {
                     try {
                         synchronized (this) {
                             wait(loadUrlTimeoutValue);
                         }
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     } finally {
                         if (!isJsInitFinished()) {
                             mSystemContext.runOnUiThread(new Runnable() {
                                 public void run() {
                                     mSystemContext
                                             .toast(XStrings
                                                     .getInstance()
                                                     .getString(
                                                             XStrings.NO_XFACE_JS_MESSAGE));
                                     mSystemContext.waitingDialogForAppStartFinished();
                                     setVisibility(View.VISIBLE);
                                     requestFocus();
                                 }
                             });
                         }
                     }
 
                 }
             };
             Thread thread = new Thread(timeoutCheck);
             thread.start();
         }
     }
 
     public int getViewId() {
         return mViewId;
     }
 
     /**
      * 设置适配是否完成标志
      * @param adapt
      * adapt为true则表示适配完成，false表示适配未完成这时不响应双击时将viewport设置为false
      */
     public void setAdapated(boolean adapt)
     {
        mTouchEventHandler.setAdapated(adapt);
     }
 
     public void setJsInitFinished(boolean isFinished){
         mIsJsInitFinshed = isFinished;
     }
 
     public boolean isJsInitFinished(){
         return mIsJsInitFinshed;
     }
 
     public void setValid(boolean isValid) {
         this.mIsValid = isValid;
     }
 
     public boolean isValid() {
         return mIsValid;
     }
 
     public void loadUrl(String url, boolean openExternal,boolean clearHistory,Context context) {
         XLog.d(CLASS_NAME, "loadUrl(%s, %b, %b, HashMap", url, openExternal, clearHistory);
         // If clearing history
         if (clearHistory) {
             this.clearHistory();
         }
         // 通过app方式加载url
         if (!openExternal) {
             XWhiteList whiteList  = mOwnerApp.getAppInfo().getWhiteList();
             if (url.startsWith("file://") || whiteList.isUrlWhiteListed(url)) {
                 // 加载app
                 this.loadApp(url);
                 return;
             }
             else {
                 XLog.w(CLASS_NAME, "loadUrl: Cannot load URL by app, Loading into browser instead. (URL=" + url + ")");
             }
         }
         // 由于前面加载url未成功所以通过浏览器方式加载url
         try {
             Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setDataAndType(Uri.parse(url), XFileUtils.getMIMEType(url));
             context.startActivity(intent);
         } catch (android.content.ActivityNotFoundException e) {
             XLog.e(CLASS_NAME, "Error loading url " + url, e);
         }
     }
 
     public boolean backHistory() {
         if (super.canGoBack()) {
             super.goBack();
             return true;
         }
         return false;
     }
 
     public void clearHistory() {
         super.clearHistory();
     }
 
     public void clearCache(boolean includeDiskFile) {
        super.clearCache(includeDiskFile);
     }
 
     public void loadApp(String url, boolean showWaiting) {
         loadApp(url);
         if( showWaiting ) {
             mSystemContext.waitingDialogForAppStart();
         }
     }
 
 
     public void bindJSNativeBridge(
             ConcurrentHashMap<String, XPluginBase> plugin) {
         Iterator<Map.Entry<String, XPluginBase>> iter = plugin.entrySet().iterator();
         while (iter.hasNext()) {
             Map.Entry<String, XPluginBase> entry = iter.next();
             this.addJavascriptInterface(entry.getValue(), entry.getKey());
         }
     }
 
     /**
      * 获取ownerApp
      * @return
      */
     public XApplication getOwnerApp() {
         return mOwnerApp;
     }
 
     public void registerAppEventListener(XIWebAppEventListener listener) {
         mWebAppEventListener =listener;
     }
 
     public void unRegisterAppEventListener(XIWebAppEventListener listener) {
         mWebAppEventListener = null;
     }
 
     public XIWebAppEventListener getAppEventListener() {
        return mWebAppEventListener;
     }
 
     /**
      * 显示HTML5视频自定义视图
      *
      * @param view
      * @param callback
      */
     public void showCustomView(View view, CustomViewCallback callback) {
         // 如果存在一个view，则立即消除新建的view
         XLog.d(CLASS_NAME, "Showing Custom Video View");
         if (mCustomVideoView != null) {
             callback.onCustomViewHidden();
             return;
         }
         mCustomVideoView = view;
         mCustomViewCallback = callback;
         // 在它的container中增加CustomVideoView
         ViewGroup parent = (ViewGroup) this.getParent();
         parent.addView(view, COVER_SCREEN_GRAVITY_CENTER);
         // 隐藏CustomVideoView
         this.setVisibility(View.GONE);
         // 显示container
         parent.setVisibility(View.VISIBLE);
         parent.bringToFront();
     }
 
     /**
      * 隐藏HTML5视频自定义视图
      */
     public boolean hideCustomView() {
         XLog.d(CLASS_NAME, "Hidding Custom Video View");
         if (mCustomVideoView == null) {
             return false;
         }
         // 隐藏mCustomVideoView
         mCustomVideoView.setVisibility(View.GONE);
         // 从mCustomVideoView的container中删除它
         ViewGroup parent = (ViewGroup) this.getParent();
         parent.removeView(mCustomVideoView);
         mCustomVideoView = null;
         mCustomViewCallback.onCustomViewHidden();
         // 显示content view.
         this.setVisibility(View.VISIBLE);
         return true;
     }
 
 }
