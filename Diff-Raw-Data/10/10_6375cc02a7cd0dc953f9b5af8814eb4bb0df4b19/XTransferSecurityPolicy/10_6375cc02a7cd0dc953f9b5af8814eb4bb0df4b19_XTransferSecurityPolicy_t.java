 
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
 
 package com.polyvi.xface.app.transferpolicy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.polyvi.xface.XSecurityPolicy;
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.core.XAppCheckListener;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XSystemEventCenter;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XCryptor;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XFileVisitor;
 import com.polyvi.xface.util.XLog;
 
 /**
  * app转移的安全策略
  */
 public class XTransferSecurityPolicy implements XSecurityPolicy, XFileVisitor {
     private static final String CLASS_NAME = XTransferSecurityPolicy.class
             .getSimpleName();
     private XISystemContext mSysCtx;
     private ArrayList<String> mMd5Array;
     private boolean mIsContinueTraverse;
     private XCryptor mCryptor;
 
     public XTransferSecurityPolicy(XISystemContext sysCtx) {
         mIsContinueTraverse = true;
         mSysCtx = sysCtx;
         mMd5Array = new ArrayList<String>();
         mCryptor = new XCryptor();
     }
 
     @Override
     public boolean checkAppStart(XApplication app, XAppCheckListener listener) {
         String transferedAppMd5 = calTransferedAppMd5(app.getAppInfo()
                 .getSrcRoot());
         if (null != transferedAppMd5) {
             String prefMd5 = readPrefMd5(app.getAppId());
             if (transferedAppMd5.equals(prefMd5)) {
                 listener.onCheckSuccess(app, mSysCtx);
                 return true;
             }
         }
         sendMd5InvalidEvt(app);
         listener.onCheckSuccess(app, mSysCtx);
         return true;
     }
 
     @Override
     public boolean checkAppClose(XApplication app) {
         return true;
     }
 
     /**
      * 从apps.pref中读取对应app的md5值
      *
      * @param appId
      * @return
      */
     private String readPrefMd5(String appId) {
         SharedPreferences preference = mSysCtx.getContext()
                 .getSharedPreferences(
                         XPreInstallAppsTransferPolicy.APPS_MD5_FILE_NAME,
                         Context.MODE_WORLD_READABLE
                                 | Context.MODE_WORLD_WRITEABLE);
         String md5 = preference.getString(appId, null);
         if (null == md5) {
             XLog.d(CLASS_NAME, "App id: " + appId + " not found in "
                     + XPreInstallAppsTransferPolicy.APPS_MD5_FILE_NAME);
             return null;
         }
         return md5;
     }
 
     /**
      * 计算转移后的app的md5值
      *
      * @param appSrcRoot
      * @return
      */
     private String calTransferedAppMd5(String appSrcRoot) {
         if (null == appSrcRoot) {
             XLog.w(CLASS_NAME, "calTransferedAppMd5 app src_Root is null!");
             return null;
         }
         // src前面有file协议会无法找到相应路径，所以需要去掉file协议头
         appSrcRoot = appSrcRoot.split(XConstant.FILE_SCHEME)[1];
         XFileUtils.walkDirectory(appSrcRoot, this);
        String md5 = XTransferPolicyUtils.calAppMd5(mMd5Array, mCryptor);
        mMd5Array.clear();
        return md5;
     }
 
     /**
      * 同步发送md5值未匹配的事件
      *
      * @param app
      */
     private void sendMd5InvalidEvt(XApplication app) {
         XEvent evt = new XEvent(XEventType.MD5_INVALID, app.getAppId());
         XSystemEventCenter.getInstance().sendEventSync(evt);
     }
 
     @Override
     public void visit(String filePath) {
         // 计算md5值
         try {
             if (filePath.endsWith(".html") || filePath.endsWith(".js")
                     || filePath.endsWith(".htm")) {
                 FileInputStream is = new FileInputStream(new File(filePath));
                 String md5 = mCryptor.calMD5Value(is);
                 mMd5Array.add(md5);
             }
         } catch (FileNotFoundException e) {
             XLog.e(CLASS_NAME, filePath + " Not Found!");
             mMd5Array = null;
             mIsContinueTraverse = false;
         }
     }
 
     @Override
     public boolean isContinueTraverse() {
         return mIsContinueTraverse;
     }
 
 }
