 
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
 
 import java.io.File;
 
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.LOG;
 
 import android.content.Context;
 import android.view.MotionEvent;
 import android.view.View;
 
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XISystemEventReceiver;
 import com.polyvi.xface.event.XSystemEventCenter;
 import com.polyvi.xface.util.XUtils;
 
 /**
  * application对应的WebView封装，用于显示application
  */
 public class XAppWebView extends CordovaWebView implements
         XISystemEventReceiver {
 
     public static final String CLASS_NAME = "XAppWebView";
 
     public static final int EMPTPY_VIEW_ID = Integer.MAX_VALUE;
 
     private int mViewId;
     /** < view id，每个view的id唯一 */
     private XTouchEventHandler mTouchEventHandler;
 
     private XWebViewClient mWebViewClient;
 
     /** 该视图是否有效 */
     private boolean mIsValid;
 
     protected XApplication mOwnerApp;
     protected XISystemContext mSystemCtx;
 
     public XAppWebView(XISystemContext systemContext) {
         super(systemContext.getContext());
         mSystemCtx = systemContext;
         setAppCachePath(XConfiguration.getInstance().getOfflineCachePath());
         mViewId = XUtils.generateRandomId();
         mTouchEventHandler = new XTouchEventHandler();
         registerSystemEventReceiver();
        getSettings().setUseWideViewPort(true);
     }
 
     private void setAppCachePath(String path) {
         File file = new File(path);
         if (!file.exists()) {
             file.mkdirs();
         }
         getSettings().setAppCachePath(path);
     }
 
     private void registerSystemEventReceiver() {
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.MSG_RECEIVED);
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.CALL_RECEIVED);
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.PUSH_MSG_RECEIVED);
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.EXTERNAL_MESSAGE_RECEIVED);
     }
 
     private void unRegisterSystemEventReceiver() {
         XSystemEventCenter.getInstance().unregisterReceiver(this);
     }
 
     @Override
     public void onReceived(Context context, XEvent evt) {
         if (evt.getType() == XEventType.MSG_RECEIVED) {
             String msgs = (String) evt.getData();
             handleMsgEvent(msgs);
         } else if (evt.getType() == XEventType.CALL_RECEIVED) {
             int callStatus = (Integer) evt.getData();
             handleCallReceived(callStatus);
         } else if (evt.getType() == XEventType.EXTERNAL_MESSAGE_RECEIVED) {
             String msgs = (String) evt.getData();
             handleExternalMessage(msgs);
         }
     }
 
     /**
      * 处理短信事件
      *
      * @param msgs
      */
     private void handleMsgEvent(String msgs) {
         String jsScript = "try{ cordova.require('cordova/channel').onMsgReceived.fire('"
                 + msgs + "');}catch(e){console.log('msg rcv : ' + e);}";
         sendJavascript(jsScript);
     }
 
     /**
      * 处理来电事件
      *
      * @param callStatus
      */
     private void handleCallReceived(int callStatus) {
         String jsScript = "try{ cordova.require('cordova/channel').onCallReceived.fire('"
                 + callStatus + "');}catch(e){console.log('call rcv : ' + e);}";
         sendJavascript(jsScript);
     }
 
     /**
      * 处理通知事件
      *
      * @param message
      */
     public void handleNotificationReceived(String message) {
         String jsScript = "try{ cordova.require('com.polyvi.xface.extension.push.PushNotification').fire('"
                 + message + "');}catch(e){console.log('call rcv : ' + e);}";
 
         sendJavascript(jsScript);
     }
 
     /**
      * 处理外部程序发过来参数的消息
      *
      * @param message
      */
     private void handleExternalMessage(String message) {
         String jsScript = "try{xFace.require('xFace/app').fireAppEvent('client','"
                 + message
                 + "');}catch(e){console.log('exception in fireAppEvent:' + e);}";
         jsScript = jsScript.replaceAll("'", "\\\\'");
         jsScript = jsScript.replaceAll("\"", "\\\\\"");
         sendJavascript(jsScript);
     }
 
     @Override
     public void loadUrlIntoView(String url, int time) {
         // If not first page of app, then load immediately
         // Add support for browser history if we use it.
         if ((url.startsWith("javascript:")) || this.canGoBack()) {
         }
 
         // If first page, then show splashscreen
         else {
 
             LOG.d(TAG, "loadUrlIntoView(%s, %d)", url, time);
 
             // Send message to show splashscreen now if desired
             if (this instanceof XStartAppView) {
                 this.postMessage("splashscreen", "show");
             }
         }
 
         // Load url
         this.loadUrlIntoView(url);
     }
 
     /**
      * 处理双击以及多次连击时自动放大的情况
      *
      * @param event
      *            交互事件
      * @return 如果事件被处理了返回true，否则返回false
      */
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             mOwnerApp.resetIdleWatcher();
         }
         mTouchEventHandler.handleTouchEvent(event, this);
         return super.onTouchEvent(event);
     }
 
     public int getViewId() {
         return mViewId;
     }
 
     @Override
     public void loadUrl(String url) {
         super.loadUrl(url);
         this.setOnLongClickListener(new View.OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 // 重载onLongClick，阻止Android 4.0系统中出现默认text selection行为.
                 // 经过测试，不影响touchstart, touchmove, touchend等events
                 return true;
             }
         });
     }
 
     /**
      * 设置适配是否完成标志
      *
      * @param adapt
      *            adapt为true则表示适配完成，false表示适配未完成这时不响应双击时将viewport设置为false
      */
     public void setAdapated(boolean adapt) {
         mTouchEventHandler.setAdapated(adapt);
     }
 
     public void setValid(boolean isValid) {
         this.mIsValid = isValid;
     }
 
     public boolean isValid() {
         return mIsValid;
     }
 
     /**
      * 获取ownerApp
      *
      * @return
      */
     public XApplication getOwnerApp() {
         return mOwnerApp;
     }
 
     public void setOwnerApp(XApplication app) {
         this.mOwnerApp = app;
 
     }
 
     public XWebViewClient getWebViewClient() {
         return mWebViewClient;
     }
 
     /**
      * 关闭app
      *
      * @param viewId
      *            须要被关闭app对应的viewid
      */
     public void handleCloseApplication(int viewId) {
         // 调用父类的消息发送
         super.postMessage("exit_engine", null);
     }
 
     public void willClosed() {
         this.postMessage("spinner", "stop");
         unRegisterSystemEventReceiver();
     }
 
     /**
      * 调用父类的clearHistory，避免super在UI线程的语法错误
      */
     private void callSuperClearHistory() {
         super.clearHistory();
     }
 
     @Override
     public void clearHistory() {
         mSystemCtx.getActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 callSuperClearHistory();
             }
         });
     }
 
 }
