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
 
 package com.polyvi.xface;
 
 import java.io.IOException;
 
 import org.apache.cordova.CordovaActivity;
 import org.apache.cordova.CordovaChromeClient;
 import org.apache.cordova.CordovaInterface;
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.CordovaWebViewClient;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.telephony.TelephonyManager;
 import android.view.Display;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.polyvi.xface.app.XAppInfo;
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.app.XApplicationCreator;
 import com.polyvi.xface.app.XIApplication;
 import com.polyvi.xface.configXml.XTagNotFoundException;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XSystemEventCenter;
 import com.polyvi.xface.ssl.XSSLManager;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XLog;
 import com.polyvi.xface.util.XNotification;
 import com.polyvi.xface.view.XAppWebView;
 import com.polyvi.xface.view.XIceCreamWebViewClient;
 import com.polyvi.xface.view.XStartAppView;
 import com.polyvi.xface.view.XWebChromeClient;
 import com.polyvi.xface.view.XWebViewClient;
 
 /**
  * 该类是android程序的主activity，也是整个程序的入口. 主要管理整个程序的生命周期以及执行程序的初始化操作
  */
 
 public class XFaceMainActivity extends CordovaActivity implements
         XISystemContext {
 
     private static final String CLASS_NAME = XFaceMainActivity.class.getName();
 
     private static final int ANDROID4_2_API_LEVEL = 17;
 
     protected TextView mVersionText;
 
     protected LinearLayout.LayoutParams mVersionParams;
 
     private XNotification mWaitingNotification = new XNotification(this);;
 
     private XStartParams mStartParams;
 
     private XSecurityPolicy mSecurityPolicy;
 
     /** App生成器 */
     private XApplicationCreator mCreator;
 
     /**
      * 标示startapp
      */
     private XApplication mStartApp;
 
     private XApplication mCurrentApp;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         systemBoot();
     }
 
     /**
      * 初始化startapp
      *
      * @param appInfo
      *            startapp的信息
      * @return
      */
     public boolean initStartApp(XAppInfo appInfo) {
         XIApplication app = mCreator.create(appInfo);
         mStartApp = XApplicationCreator.toWebApp(app);
         return true;
     }
 
     private void setCurrentAppView(XAppWebView curAppView) {
         if (curAppView == null) {
             this.appView = null;
             this.webViewClient = null;
             return;
         }
         this.appView = curAppView;
         this.webViewClient = curAppView.getWebViewClient();
         this.cancelLoadUrl = false;
         this.appView.requestFocus();
     }
 
     @Override
     public XApplication getStartApp() {
         return mStartApp;
     }
 
     @Override
     protected CordovaWebView makeWebView() {;
         XAppWebView  webView = this.appView == null ? new XStartAppView(this) : new XAppWebView(this);
         mCurrentApp.setView(webView);
         return webView;
     }
 
     @Override
     protected CordovaWebViewClient makeWebViewClient(CordovaWebView webView) {
         CordovaWebViewClient webViewClient;
         if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
             webViewClient = new XWebViewClient(this, webView);
         } else {
             webViewClient = new XIceCreamWebViewClient(this, webView);
         }
         return webViewClient;
     }
 
     @Override
     protected CordovaChromeClient makeChromeClient(CordovaWebView webView) {
         XWebChromeClient chrom = new XWebChromeClient(this, webView);
         webView.setWebChromeClient(chrom);
         return chrom;
     }
 
     public XApplicationCreator getAppFactory() {
         return mCreator;
     }
 
     @Override
     public Object onMessage(String id, Object data) {
         if ("exit".equals(id)) {
             XAppWebView xAppView = ((XAppWebView) appView);
             int viewId = xAppView.getViewId();
             XEvent evt = XEvent.createEvent(XEventType.CLOSE_APP, viewId);
             XSystemEventCenter.getInstance().sendEventSync(evt);
             return true;
         } else if ("exit_engine".equals(id)) {
             endActivity();
             return true;
         }
         return super.onMessage(id, data);
     }
 
     /**
      * 初始化系统事件处理器
      */
     private void initSystemEventCenter() {
         XSystemEventCenter.init(this);
     }
 
     /**
      * 创建app安全策略
      *
      * @return 安全策略
      */
     protected XSecurityPolicy createSecurityPolicy() {
         return new XDefaultSecurityPolicy(this);
     }
 
     /**
      * 创建系统启动组件
      *
      * @return
      */
     protected XSystemBootstrap createSystemBootstrap() {
         return new XSystemInitializer(this);
     }
 
     /**
      * 创建管理与https有关证书库对象
      */
     protected void createSSLManager() {
         XSSLManager.createInstance(this);
     }
 
     /**
      * 程序的入口函数
      */
     private void systemBoot() {
         initSystemEventCenter();
         mCreator = new XApplicationCreator(this);
         createSSLManager();
         mSecurityPolicy = createSecurityPolicy();
         XConfiguration.getInstance().loadPlatformStrings(getContext());
         mStartParams = XStartParams.parse(getIntent().getStringExtra(
                 XConstant.TAG_APP_START_PARAMS));
         // 解析系统配置
         try {
             initSystemConfig();
         } catch (IOException e) {
             this.toast("Loading System Config Failure.");
             XLog.e(CLASS_NAME, "Loading system config failure!");
             e.printStackTrace();
             return;
         } catch (XTagNotFoundException e) {
             this.toast("Loading System Config Failure.");
             XLog.e(CLASS_NAME, "parse config.xml error:" + e.getMessage());
             e.printStackTrace();
             return;
         }
         // 启动splash
         startSplashScreen();
         // 配置系统LOG等级
         XLog.setLogLevel(XConfiguration.getInstance().readLogLevel());
         // 配置系统的工作目录
         XConfiguration.getInstance()
                 .configWorkDirectory(this, getWorkDirName());
         XSystemBootstrap bootstrap = createSystemBootstrap();
         new XPrepareWorkEnvronmentTask(bootstrap, this).execute();
     }
 
     /**
      * 开始splash操作
      */
     protected void startSplashScreen() {
         this.splashscreenTime = this.getIntegerProperty("SplashScreenDelay",
                 this.splashscreenTime);
         if (this.splashscreenTime > 0) {
             this.splashscreen = this.getIntegerProperty("SplashScreen", 0);
             if (this.splashscreen != 0) {
                 showSplashScreen(this.splashscreenTime);
             }
         }
     }
 
     @Override
     public void unloadView(XAppWebView view) {
         view.loadUrl("about:blank");
         removeView(view);
     }
 
     /**
      * 解析系统配置
      *
      * @throws IOException
      * @throws XTagNotFoundException
      */
     private void initSystemConfig() throws IOException, XTagNotFoundException {
         XConfiguration.getInstance().readConfig(this);
     }
 
     /**
      * 获得手机的deviceId
      *
      * @return
      */
     protected String getKey() {
         if (Build.VERSION.SDK_INT == ANDROID4_2_API_LEVEL) {
             return null;
         }
         TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         String deviceID = tm.getDeviceId();
         return deviceID;
     }
 
     /**
      * 添加一个子视图到Activity的content view，如果view是可见的，则view会被显示在屏幕上
      *
      * @param view
      *            子视图
      */
     public void addView(XAppWebView view) {
         if (view instanceof View) {
             View subView = (View) view;
             subView.setLayoutParams(new LinearLayout.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
             setCurrentAppView(view);
             root.addView(subView);
         }
     }
 
     @Override
     public XAppWebView getCurAppView() {
         return (XAppWebView) this.appView;
     }
 
     @Override
     public void loadView(XApplication app, String url) {
         mCurrentApp = app;
         if (this.appView != null) {
             this.init();
         }
         loadUrl(url);
         app.setCachePolicy(this.appView.getSettings());
     }
 
     /**
      * 从Activity的content view中remove掉一个子视图
      *
      * @param view
      *            子视图
      */
     public void removeView(XAppWebView view) {
         if (view instanceof View) {
             root.removeView((View) view);
             View pView = root.getChildAt(root.getChildCount() - 1);
             if (pView != null && pView instanceof XAppWebView) {
                 XAppWebView pWebView = (XAppWebView) pView;
                 setCurrentAppView(pWebView);
             } else {
                 setCurrentAppView(null);
             }
         }
     }
 
     /**
      * 获取工作目录的名字
      *
      * @return
      */
     protected String getWorkDirName() {
         String packageName = getPackageName();
         String workDir = XConfiguration.getInstance().getWorkDirectory(this,
                 packageName)
                 + XConstant.PRE_INSTALL_SOURCE_ROOT;
         return workDir;
     }
 
     @Override
     public void runStartApp() {
         getStartApp().start(getStartParams());
 
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     public Context getContext() {
         return this;
     }
 
     @Override
     public void toast(String message) {
         mWaitingNotification.toast(message);
     }
 
     @Override
     public XStartParams getStartParams() {
         return mStartParams;
     }
 
     @Override
 	protected void showSplashScreen(int time) {
 		if(splashDialog != null && splashDialog.isShowing()){
 			return;
 		}
 		super.showSplashScreen(time);
 	}
 
 	@Override
     public Activity getActivity() {
         return this;
     }
 
     @Override
     public XSecurityPolicy getSecurityPolicy() {
         return mSecurityPolicy;
     }
 
     @Override
     public CordovaInterface getCordovaInterface() {
         return this;
     }
 }
