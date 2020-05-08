 
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
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import com.polyvi.xface.ams.XAppList;
 import com.polyvi.xface.app.XAppInfo;
 import com.polyvi.xface.app.XApplicationCreator;
 import com.polyvi.xface.app.XIApplication;
 import com.polyvi.xface.configXml.XPreInstallPackageItem;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XISystemEventReceiver;
 import com.polyvi.xface.event.XSystemEventCenter;
 import com.polyvi.xface.util.XAssetsFileUtils;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XCryptor;
 import com.polyvi.xface.util.XLog;
 import com.polyvi.xface.util.XStringUtils;
 
 /**
  * 预装app的转移策略
  */
 public class XPreInstallAppsTransferPolicy implements XISystemEventReceiver {
     private static final String CLASS_NAME = XPreInstallAppsTransferPolicy.class
             .getSimpleName();
     public static final String APP_ID = "appId";
     public static final String APP_SRC_ROOT = "srcRoot";
     public static final String APP_MD5 = "md5";
     /**
      * 保存应用信息的文件名称, 应用的md5值. 例如: KEY:app1
      * VALUE:{a00d766b012b8ffc2c00210245f65b3d}
      */
     public static final String APPS_MD5_FILE_NAME = "apps_md5.pref";
 
     private XAppList mAppList;
     private XISystemContext mSysContext;
     private Context mCtx;
     private ArrayList<String> mMd5Array;
     private XCryptor mCryptor;
 
     public XPreInstallAppsTransferPolicy(XAppList list,
             XISystemContext sysContext) {
         mAppList = list;
         mSysContext = sysContext;
         mCtx = mSysContext.getContext();
         mMd5Array = new ArrayList<String>();
         mCryptor = new XCryptor();
         registerSystemEventReceiver();
     }
 
     /**
      * 注册转移预装app系统事件
      */
     private void registerSystemEventReceiver() {
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.TRANSFER_COMPLETE);
         XSystemEventCenter.getInstance().registerReceiver(this,
                 XEventType.MD5_INVALID);
     }
 
     /**
      * 转移assets下的预装app
      */
     public void transfer() {
         List<XPreInstallPackageItem> preinstallPackages = XConfiguration
                 .getInstance().getPreinstallPackages();
         int packagesSize = preinstallPackages.size();
         ArrayList<String> appIds = new ArrayList<String>(packagesSize - 1);
         for (int index = 1; index < packagesSize; index++) {
             String appId = preinstallPackages.get(index).appId;
             appIds.add(appId);
         }
         transferApp(appIds);
     }
 
     /**
      * 转移对应appId的app
      *
      * @param appIds
      */
     private void transferApp(final ArrayList<String> appIds) {
         Thread transferThread = new Thread() {
             public void run() {
                 if (null == appIds) {
                     XLog.e(CLASS_NAME, "Transfer app error!");
                     return;
                 }
                 for (String appId : appIds) {
                     // 已经转移完成，不需要再次转移
                     if (!mAppList.getAppById(appId).getAppInfo().getSrcRoot()
                             .contains(XConstant.ASSERT_PROTACAL)) {
                         XApplicationCreator
                                 .toWebApp(mAppList.getAppById(appId))
                                 .setAppSecurityPolicy(
                                         new XTransferSecurityPolicy(mSysContext));
                         XLog.d(CLASS_NAME, "App was already transfered");
                         return;
                     }
                     XLog.d(CLASS_NAME, "Start transfer app");
                     // 拷贝预装app
                     String assetsAppPath = XConstant.APP_DATA_DIR_NAME
                             + File.separator + appId;
                     String transferPath = XConfiguration.getInstance()
                             .getWorkDirectory() + appId;
                     File transferApp = new File(transferPath);
                     if (!transferApp.exists()) {
                         transferApp.mkdirs();
                     }
                     boolean ret = copyAssetsDirAndCalMd5(assetsAppPath,
                             transferPath);
                     if (ret) {
                         // 发送转移完成事件
                         HashMap<String, String> data = new HashMap<String, String>();
                         data.put(APP_ID, appId);
                         data.put(APP_SRC_ROOT, XConstant.FILE_SCHEME
                                 + transferPath);
                         data.put(APP_MD5, XTransferPolicyUtils.calAppMd5(
                                 mMd5Array, mCryptor));
                         XEvent evt = XEvent.createEvent(
                                 XEventType.TRANSFER_COMPLETE, data);
                         XSystemEventCenter.getInstance().sendEventAsync(evt);
                         XLog.d(CLASS_NAME, "Transfer app finish");
                     }
                 }
             }
         };
         transferThread.setPriority(Thread.MIN_PRIORITY);
         transferThread.start();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void onReceived(Context context, XEvent evt) {
         if (evt.getType() == XEventType.TRANSFER_COMPLETE) {
             HashMap<String, String> evtData = (HashMap<String, String>) evt
                     .getData();
             String appId = evtData.get(APP_ID);
             String md5 = evtData.get(APP_MD5);
             if (null != md5
                     && updateAppSrcRoot(appId, evtData.get(APP_SRC_ROOT))) {
                 recordAppMd5(appId, md5);
                 XApplicationCreator.toWebApp(mAppList.getAppById(appId))
                         .setAppSecurityPolicy(
                                 new XTransferSecurityPolicy(mSysContext));
             } else {
                 mSysContext.toast("System Md5 Security Policy Error");
                 exitEngine();
             }
         } else if (evt.getType() == XEventType.MD5_INVALID) {
             String appId = (String) evt.getData();
             String srcRoot = XConstant.ASSERT_PROTACAL
                     + XConstant.APP_DATA_DIR_NAME + File.separator + appId;
             if (!updateAppSrcRoot(appId, srcRoot)) {
                 mSysContext.toast("System Update App Info Error");
                 exitEngine();
                 return;
             }
             XApplicationCreator.toWebApp(mAppList.getAppById(appId))
                     .setAppSecurityPolicy(null);
             // 开始转移app
             ArrayList<String> appIds = new ArrayList<String>(1);
             appIds.add(appId);
             transferApp(appIds);
         }
     }
 
     /**
      * 更新app的srcRoot
      *
      * @param appId
      * @param srcRoot
      * @return 更新成功返回true，反之返回false
      */
     private boolean updateAppSrcRoot(String appId, String srcRoot) {
         if (XStringUtils.isEmptyString(appId)
                 || XStringUtils.isEmptyString(srcRoot)) {
             XLog.e(CLASS_NAME,
                     "Method updateAppSrcRoot: update app src root error!");
             return false;
         }
         XIApplication app = mAppList.getAppById(appId);
         if (null == app) {
             XLog.e(CLASS_NAME,
                     "Method updateAppSrcRoot: app id not found in app list");
             return false;
         }
         XAppInfo info = app.getAppInfo();
         info.setSrcRoot(srcRoot);
         mAppList.updateApp(info, app);
         return true;
     }
 
     /**
      * 退出引擎
      */
     private void exitEngine() {
         Timer timer = new Timer();
         TimerTask task = new TimerTask() {
             @Override
             public void run() {
                 XEvent evt = XEvent.createEvent(XEventType.CLOSE_ENGINE);
                 XSystemEventCenter.getInstance().sendEventSync(evt);
             }
         };
         timer.schedule(task, 5000);
     }
 
     /**
      * 递归拷贝assets下指定的文件夹到指定路径，并求出app的md5值
      *
      * @param context
      * @param srcDir
      * @param desPath
      * @return 拷贝成功返回true，反之返回false
      */
     private boolean copyAssetsDirAndCalMd5(String srcDir, String desPath) {
         String md5 = null;
         try {
             String childrens[] = mCtx.getAssets().list(srcDir);
             for (String child : childrens) {
                 String srcFilePath = srcDir + File.separator + child;
                 if (XAssetsFileUtils.isFile(mCtx, srcFilePath)) {
                     // 求出.html, .js, .htm文件的md5值
                     if (srcFilePath.endsWith(".html")
                             || srcFilePath.endsWith(".js")
                             || srcFilePath.endsWith(".htm")) {
                         md5 = mCryptor.calMD5Value(mCtx.getAssets().open(
                                 srcFilePath));
                         mMd5Array.add(md5);
                     }
                     // 拷贝到目标路径
                     if (!XAssetsFileUtils.copyAssetsToTarget(mCtx, srcFilePath,
                             desPath + File.separator + child)) {
                         XLog.e(CLASS_NAME, "Copy assets: " + srcDir
                                 + File.separator + child + "failed");
                         return false;
                     }
                 } else {
                     copyAssetsDirAndCalMd5(srcFilePath, desPath
                             + File.separator + child);
                 }
             }
 
         } catch (IOException e) {
             XLog.e(CLASS_NAME, "Copy assets folder or calculate app md5 failed");
             return false;
         }
         return true;
     }
 
     /**
      * 记录应用md5值
      *
      * @param appId
      * @param md5
      */
     private void recordAppMd5(String appId, String md5) {
         SharedPreferences preference = mCtx.getSharedPreferences(
                 APPS_MD5_FILE_NAME, Context.MODE_WORLD_READABLE
                         | Context.MODE_WORLD_WRITEABLE);
         // 写入pref,这里不考虑是否之前已经存在，都进行覆写。因为之前存在的有可能被更改
         SharedPreferences.Editor editor = preference.edit();
         editor.putString(appId, md5);
         editor.commit();
     }
 }
