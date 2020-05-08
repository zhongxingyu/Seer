 
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
 
 import android.content.Context;
 import android.util.Pair;
 
 import com.polyvi.xface.ams.XAppList;
 import com.polyvi.xface.ams.XAppManagement;
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.app.XApplicationCreator;
 import com.polyvi.xface.app.XIApplication;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XISystemEventReceiver;
 import com.polyvi.xface.event.XSystemEventCenter;
 
 public class XStartAppView extends XAppWebView implements XISystemEventReceiver {
 
     private final static int ONRESUME = 0;
     private final static int ONPAUSE = 1;
     private final static int ONDESTROY = 2;
 
     private XAppManagement mAms;
 
     public XStartAppView(XISystemContext systemContext) {
         super(systemContext);
         registerSystemEventReceiver();
     }
 
     /**
      * 注册事件接收器
      */
     private void registerSystemEventReceiver() {
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.XAPP_MESSAGE);
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.CLOSE_APP);
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.CLEAR_MEMORY_CACHE);
     }
 
     public void setAppManagement(XAppManagement ams) {
         mAms = ams;
     }
 
     @Override
     public void handlePause(boolean keepRunning) {
         if (mAms != null) {
             iterateApp(ONPAUSE, keepRunning);
         }
         super.handlePause(keepRunning);
     }
 
     @Override
     public void handleResume(boolean keepRunning,
             boolean activityResultKeepRunning) {
         if (mAms != null) {
             iterateApp(ONRESUME, keepRunning, activityResultKeepRunning);
         }
         super.handleResume(keepRunning, activityResultKeepRunning);
     }
 
     private void iterateApp(int operate, boolean... objs) {
         XAppList list = mAms.getAppList();
         Iterator<XIApplication> it = list.iterator();
         while (it.hasNext()) {
             XIApplication app = it.next();
             XApplication webApp = XApplicationCreator.toWebApp(app);
             if (null != webApp) {
                 doOperate(webApp.getView(), operate, objs);
             }
         }
     }
 
     private void doOperate(XAppWebView appView, int operate, boolean... objs) {
         if (null == appView) {
             return;
         }
         switch (operate) {
         case ONRESUME:
             appView.handleResume(objs[0], objs[1]);
             break;
         case ONPAUSE:
             appView.handlePause(objs[0]);
             break;
         case ONDESTROY:
             appView.handleDestroy();
             break;
         }
     }
 
     @Override
     public void handleDestroy() {
         if (mAms != null) {
             iterateApp(ONDESTROY);
         }
         super.handleDestroy();
     }
 
     /**
      * 关闭app
      *
      * @param viewId
      *            须要被关闭app对应的viewid
      */
     @Override
     public void handleCloseApplication(int viewId) {
         if (getViewId() != viewId) {
             mAms.closeApp(viewId);
             return;
         }
         super.handleCloseApplication(viewId);
     }
 
     /**
      * 处理xapp发送的消息数据
      *
      * @param view
      *            发消息app对应的view
      * @param msgData
      *            消息数据
      */
     public void handleXAppMessage(XAppWebView view, String msgData) {
         if (null != mAms) {
             mAms.handleAppMessage(mOwnerApp, view, msgData);
         }
     }
 
     @Override
     public void onReceived(Context context, XEvent evt) {
         if (evt.getType() == XEventType.XAPP_MESSAGE) {
             @SuppressWarnings("unchecked")
             Pair<XAppWebView, String> data = (Pair<XAppWebView, String>) evt
                     .getData();
             handleXAppMessage(data.first, data.second);
         } else if (evt.getType() == XEventType.CLOSE_APP) {
             int viewId = (Integer) evt.getData();
             handleCloseApplication(viewId);
         } else if (evt.getType() == XEventType.CLEAR_MEMORY_CACHE) {
             mSystemCtx.runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mOwnerApp.clearCache(false);
                 }
             });
        } else {
            super.onReceived(context, evt);
         }
     }
 }
