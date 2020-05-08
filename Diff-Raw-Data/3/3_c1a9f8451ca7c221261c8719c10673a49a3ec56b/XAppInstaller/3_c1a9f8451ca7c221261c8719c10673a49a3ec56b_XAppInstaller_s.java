 
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
 
 package com.polyvi.xface.ams;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.content.Context;
 
 import com.polyvi.xface.ams.XAMSError.AMS_ERROR;
 import com.polyvi.xface.ams.XInstallListener.AMS_OPERATION_TYPE;
 import com.polyvi.xface.ams.XInstallListener.InstallStatus;
 import com.polyvi.xface.app.XAppInfo;
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.app.XApplicationCreator;
 import com.polyvi.xface.app.XIApplication;
 import com.polyvi.xface.configXml.XAbstractAppConfigParser;
 import com.polyvi.xface.configXml.XXmlOperatorFactory;
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XISystemContext;
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XSystemEventCenter;
 import com.polyvi.xface.util.XAppUtils;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XLog;
 
 /**
  * 负责应用安装，将安装进度发送给({@link XAppInstallListener})
  */
 public class XAppInstaller {
 
     private static final String CLASS_NAME = XAppInstaller.class
             .getSimpleName();
 
     /** 对所有app的引用 ,XAppList的装饰者*/
     private XAppList mAppList;
 
     private XISystemContext mSysContext;
 
     /** application的创建者 */
     XApplicationCreator mCreator;
 
     public XAppInstaller(XISystemContext context, XApplicationCreator creator) {
         this.mSysContext = context;
         mCreator = creator;
         this.mAppList = new XPersistentAppList(context.getContext(), mCreator, new XAppList());
     }
 
     /**
      * 获取installer安装的应用列表
      *
      * @return
      */
     public XAppList getInstalledAppList() {
         return mAppList;
     }
 
     /**
      * 安装app
      *
      * @param appId
      *            app的id
      * @param appInfo
      *            app配置对应的AppInfo
      * @return
      */
     private void doInstall(String packagePath, XAppInfo appInfo,
             XInstallListener listener) {
         String appId = appInfo.getAppId();
         // 如果appInfo已经存在 则更新程序
         if (mAppList.getAppById(appId) != null) {
             doUpdate(packagePath, listener, appInfo);
             return;
         }
         appInfo.setSrcRoot(XConstant.FILE_SCHEME
                 + XConfiguration.getInstance().getAppInstallDir() + appId);
         // 创建相关的数据结构
         XIApplication app = (XIApplication) mCreator.create(appInfo);
         mAppList.add(app);
         // 处理icon以及内嵌的js文件
         // 移动应用的icon到workdir目录下的XApplication#APPS_ICON_DIR_NAME/$appId目录中，便于defaultApp
         // 访问所有应用的icon
         File iconFile = new File(getAppInstallDir(appId), appInfo.getIcon());
         File destFile = new File(XAppUtils.generateAppIconPath(appId,
                 appInfo.getIcon()));
         moveFile(iconFile, destFile);
         copyEmbeddedJsFileToApp(appId);
         // 安装错误处理页面
         installErrorPage(XApplicationCreator.toWebApp(app), mSysContext.getContext());
         // 存配置文件
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL,
                 InstallStatus.INSTALL_WRITE_CONFIGURATION);
         // 安装成功 删除安装包
         new File(packagePath).delete();
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL,
                 InstallStatus.INSTALL_FINISHED);
         listener.onSuccess(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL, appId);
     }
 
     /**
      * 安装app应用
      *
      * @param packagePath
      *            安装包路径
      * @param appId
      *            安装app的id
      * @param listener
      *            安装监听器
      */
     public synchronized String install(String packagePath, String appId,
             XInstallListener listener) {
         String ret = null;
         // 1. 安装初始化
         if (!installInitialize(packagePath, listener,
                 AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL))
             return ret;
 
         // 2. 解压文件到目的目录
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL,
                 InstallStatus.INSTALL_UNZIP_PACKAGE);
         String appDirPath = getAppInstallDir(appId);
 
         boolean successed = XFileUtils.unzipFile(appDirPath, packagePath);
         if (!successed) {
             // 如果解压失败 则删除中间文件
             XFileUtils.deleteFileRecursively(appDirPath);
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL, appId,
                     AMS_ERROR.IO_ERROR);
             XLog.e(CLASS_NAME, "unzip package failure!");
             return ret;
         }
 
         // 3.解析app的配置信息
         XAppInfo appInfo = parseAppXml(appId, ret);
         if (appInfo == null) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL, appId,
                     AMS_ERROR.IO_ERROR);
             return ret;
         }
 
         if (!appInfo.getAppId().equals(appId)) {
             XLog.e(CLASS_NAME, "app id error, not consistent.");
             return ret;
         }
         doInstall(packagePath, appInfo, listener);
         return appId;
     }
 
     /**
      * 安装初始化
      *
      * @param packagePath
      * @param listener
      * @return
      */
     private boolean installInitialize(String packagePath,
             XInstallListener listener, AMS_OPERATION_TYPE type) {
         if (!isPackageExists(packagePath)) {
             // TODO: 拿不到id该如何通知错误信息
             listener.onError(type, "noId", AMS_ERROR.NO_SRC_PACKAGE);
             return false;
         }
         listener.onProgressUpdated(type, InstallStatus.INSTALL_INITIALIZE);
         return true;
     }
 
     /**
      * 解析app的配置文件
      *
      * @param appId
      * @param listener
      * @param ret
      * @return
      */
     private XAppInfo parseAppXml(String appId, String ret) {
         // 解析app.xml得到appInfo
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(new File(XConfiguration.getInstance()
                     .getAppInstallDir(), appId + File.separator
                     + XConstant.APP_CONFIG_FILE_NAME));
         } catch (FileNotFoundException e) {
             XLog.w(CLASS_NAME, "parse app.xml failure in insallApp");
             return null;
         }
         XAppInfo appInfo = null;
         XAbstractAppConfigParser appConfigParser = XXmlOperatorFactory
                 .createAppConfigParser();
         appConfigParser.setInput(fis);
         appInfo = appConfigParser.parseConfig();
         return appInfo;
     }
 
     /**
      * 安装app应用
      *
      * @param packagePath
      *            安装包路径
      * @param listener
      *            安装监听器
      */
     public synchronized String install(String packagePath,
             XInstallListener listener) {
         assert (null != listener);
         String ret = null;
         // 1. 安装初始化
         if (!installInitialize(packagePath, listener,
                 AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL))
             return ret;
 
         // 2.获得app的描述信息
         XAppInfo appInfo = XAppUtils.getAppInfoFromAppPackage(packagePath);
         if (null == appInfo) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL, "noId",
                     AMS_ERROR.NO_APP_CONFIG_FILE);
             XLog.e(CLASS_NAME, "invalid package！");
             return ret;
         }
 
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL,
                 InstallStatus.INSTALL_UNZIP_PACKAGE);
 
         // 3.解压app的文件到安装目录
         String appId = appInfo.getAppId();
         String appDirPath = getAppInstallDir(appId);
         boolean successed = XFileUtils.unzipFile(appDirPath, packagePath);
         if (!successed) {
             // 如果解压失败 则删除中间文件
             XFileUtils.deleteFileRecursively(appDirPath);
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_INSTALL, appId,
                     AMS_ERROR.IO_ERROR);
             XLog.e(CLASS_NAME, "unzip package failure!");
             return ret;
         }
 
         doInstall(packagePath, appInfo, listener);
         return appInfo.getAppId();
     }
 
     /**
      * 获得app的安装目录
      *
      * @param appId
      * @return
      */
     private String getAppInstallDir(String appId) {
         String path = XConfiguration.getInstance().getAppInstallDir() + appId;
         File appDirFile = new File(path);
         if (!appDirFile.exists()) {
             appDirFile.mkdirs();
             XFileUtils.setPermission(XFileUtils.EXECUTABLE_BY_OTHER,
                     appDirFile.getAbsolutePath());
         }
 
         return path;
     }
 
     /**
      * 判断安装包是否存在
      *
      * @param packagePath
      * @return
      */
     private boolean isPackageExists(String packagePath) {
         File packageFile = new File(packagePath);
         // 安装包不存在
         if (!packageFile.exists()) {
             XLog.e(CLASS_NAME, "package  %s is not exsit.", packagePath);
             return false;
         }
         return true;
     }
 
     /** 拷贝源文件内容到目标文件，如果目标文件不存在，创建目标文件，如果源文件不存在或者是一个目录，不做任何操作 */
     private void moveFile(File srcFile, File destFile) {
         if (!srcFile.exists() || srcFile.isDirectory()) {
             return;
         }
         destFile.getParentFile().mkdirs();
         if (!srcFile.renameTo(destFile)) {
             XLog.e(CLASS_NAME, "Move file: " + srcFile.getAbsolutePath()
                     + " to path: " + destFile.getAbsolutePath() + " failed!");
         }
     }
 
     // TODO:以后考虑将安装和更新合并成一个接口，由实现内部进行处理应该进行安装还是更新，这样会减少一些错误处理逻辑，使用也更简单
     /**
      * 更新一个应用
      *
      * 只有在应用已经安装过并且新的应用安装包版本号更大的情况下才会进行更新操作
      *
      * @param path
      *            安装包路径
      * @param listener
      *            安装监听器
      */
     public synchronized void update(String packagePath,
             XInstallListener listener) {
         assert (null != listener);
         // 1. 初始化
         if (!installInitialize(packagePath, listener,
                 AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE))
             return;
 
         // 2.从安装包中获取app的配置信息
         XAppInfo appInfo = XAppUtils.getAppInfoFromAppPackage(packagePath);
         if (null == appInfo) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE, "noId",
                     AMS_ERROR.NO_APP_CONFIG_FILE);
             XLog.e(CLASS_NAME, "invalid package！");
             return;
         }
 
         // 3.判断更新是否合理
         String appId = appInfo.getAppId();
         XIApplication oldApp = mAppList.getAppById(appId);
         if (null == oldApp) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE, appId,
                     AMS_ERROR.NO_TARGET_APP);
             return;
         }
         if (oldApp instanceof XApplication) {
             //发送清除webview缓存的事件
             XEvent evt = new XEvent(XEventType.CLEAR_MEMORY_CACHE);
             XSystemEventCenter.getInstance().sendEventSync(evt);
         }
 
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE,
                 InstallStatus.INSTALL_UNZIP_PACKAGE);
         // 解压文件到临时目录
         String tempFileDir = XFileUtils.createTempDir(XConfiguration
                 .getInstance().getAppInstallDir());
         if (null == tempFileDir) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE, appId,
                     AMS_ERROR.IO_ERROR);
             return;
         }
 
         boolean successed = XFileUtils.unzipFile(tempFileDir, packagePath);
 
         if (!successed) {
             XFileUtils.deleteFileRecursively(tempFileDir);
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE, appId,
                     AMS_ERROR.IO_ERROR);
             XLog.e(CLASS_NAME, "unzip package failure!");
             return;
         }
         // 将临时目录的文件 拷贝到安装目录
         String appDirPath = getAppInstallDir(appId);
         try {
             XFileUtils.copy(new File(tempFileDir), new File(appDirPath));
             XFileUtils.deleteFileRecursively(tempFileDir);
         } catch (IOException e) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE, appId,
                     AMS_ERROR.IO_ERROR);
             e.printStackTrace();
             return;
         }
         appInfo.setSrcRoot(XConstant.FILE_SCHEME
                 + XConfiguration.getInstance().getAppInstallDir()
                 + appInfo.getAppId());
         updateAppInfo(packagePath, listener, appInfo, appDirPath, oldApp);
     }
 
     /**
      * 更新app相关的信息
      *
      * @param packagePath
      * @param listener
      * @param appInfo
      */
     private void doUpdate(String packagePath, XInstallListener listener,
             XAppInfo appInfo) {
         String appId = appInfo.getAppId();
         String appsDirPath = getAppInstallDir(appId);
         XIApplication oldApp = mAppList.getAppById(appId);
         updateAppInfo(packagePath, listener, appInfo, appsDirPath, oldApp);
     }
 
     /**
      * 更新app相关的信息
      *
      * @param packagePath
      * @param listener
      * @param appInfo
      * @param appsDirPath
      * @param oldApp
      */
     private void updateAppInfo(String packagePath, XInstallListener listener,
             XAppInfo appInfo, String appsDirPath, XIApplication oldApp) {
         String appId = appInfo.getAppId();
         // 删除XApplication#APPS_ICON_DIR_NAME下的appId目录
         XFileUtils.deleteFileRecursively(new File(XAppUtils.generateAppIconPath(appId,
                 "")).getAbsolutePath());
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE,
                 InstallStatus.INSTALL_WRITE_CONFIGURATION);
         // 更新XApplication对象中的应用配置信息
         mAppList.updateApp(appInfo, oldApp);
         // 移动应用的icon到workdir目录下的XApplication#APPS_ICON_DIR_NAME/$appId目录中，便于defaultApp
         // 访问所有应用的icon
         File iconFile = new File(appsDirPath, appInfo.getIcon());
         File destFile = new File(XAppUtils.generateAppIconPath(appId,
                 appInfo.getIcon()));
         moveFile(iconFile, destFile);
         copyEmbeddedJsFileToApp(appId);
 
         listener.onProgressUpdated(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE,
                 InstallStatus.INSTALL_FINISHED);
         new File(packagePath).delete();
         // FIXME:广播一个更新成功的消息 扩展模块可能需要使用
         listener.onSuccess(AMS_OPERATION_TYPE.OPERATION_TYPE_UPDATE, appId);
     }
 
     /**
      * 卸载app应用
      *
      * @param appId
      *            需要卸载的appid
      * @param listener
      *            卸载监听器
      */
     public synchronized void uninstall(String appId, XInstallListener listener) {
         assert (null != listener);
         XIApplication app = mAppList.getAppById(appId);
         if (null == app) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UNINSTALL,
                     appId, AMS_ERROR.NO_TARGET_APP);
             return;
         }
 
         if (app instanceof XApplication) {
             XApplicationCreator.toWebApp(app).releaseData(mSysContext.getContext());
         }
         String appsDirPath = XConfiguration.getInstance().getAppInstallDir();
         File appDir = new File(appsDirPath, appId);
         if (!appDir.exists()) {
             listener.onError(AMS_OPERATION_TYPE.OPERATION_TYPE_UNINSTALL,
                     appId, AMS_ERROR.IO_ERROR);
             return;
         }
 
         XFileUtils.deleteFileRecursively(appDir.getAbsolutePath());
         mAppList.removeAppById(appId);
 
         // 删除XApplication#APPS_ICON_DIR_NAME下的appId目录
         XFileUtils.deleteFileRecursively(XAppUtils.generateAppIconPath(appId, ""));
         listener.onSuccess(AMS_OPERATION_TYPE.OPERATION_TYPE_UNINSTALL, appId);
     }
 
     /**
      * 将内置的xface.js拷贝指定应用的根目录下
      *
      * @param context
      *            android程序运行时上下文环境
      * @param appId
      *            xface.js要拷贝到的目标应用的id
      */
     private void copyEmbeddedJsFileToApp(String appId) {
         XApplication app = XApplicationCreator.toWebApp(mAppList
                 .getAppById(appId));
         if (null == app) {
             return;
         }
         String startAppDir = mSysContext.getStartApp().getAppInfo().getSrcRoot();
         File appDir = new File(XConfiguration.getInstance().getAppInstallDir(), appId);
         String indexDir = new File(appDir, app.getAppInfo().getEntry()).getParent();
        if (app.getAppInfo().getEntry().toLowerCase().contains("http:")) {
             XLog.i(CLASS_NAME,
                     "Online mode, skip copying embedded js files");
             return;
         }
         XFileUtils.copyEmbeddedJsFile(mSysContext, startAppDir, indexDir);
     }
 
     /**
      * 安装错误处理页面
      *
      * @param ctx
      */
     private void installErrorPage(XApplication app, Context context) {
         if (null == app) {
             return;
         }
         try {
             // 1. copy from asset to data dir
             InputStream is = context.getAssets()
                     .open(XConstant.ERROR_PAGE_NAME);
             String filePath = app.getDataDir() + File.separator
                     + XConstant.ERROR_PAGE_NAME;
             File file = new File(filePath);
             XFileUtils.createFileByData(file.getAbsolutePath(), is);
         } catch (IOException e) {
             XLog.e(CLASS_NAME, "install error page exception!");
         }
     }
 }
