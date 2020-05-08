 
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
 
 package com.polyvi.xface.core;
 
 import java.io.File;
 import java.util.Iterator;
 
 import com.polyvi.xface.XSecurityPolicy;
 import com.polyvi.xface.app.XApplication;
 import com.polyvi.xface.util.XConstant;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XLog;
import com.polyvi.xface.util.XStrings;
 
 /**
  * 本地模式，应用的所有文件都在本地目录 指应用放到本地磁盘 离散的形式
  */
 public class XLocalMode extends XAppRunningMode implements XAppCheckListener {
     private static final String CLASS_NAME = XLocalMode.class.getName();
 
     @Override
     public String getAppUrl(XApplication app) {
         if (null == app) {
             XLog.e(CLASS_NAME, "Application object is null!");
             return null;
         }
         String appSourceRoot = app.getAppInfo().getSrcRoot();
         if (null == appSourceRoot) {
             appSourceRoot = XConstant.FILE_SCHEME
                     + XConfiguration.getInstance().getAppInstallDir()
                     + app.getAppId();
         }
         String startPage = app.getAppInfo().getEntry();
         if (startPage.startsWith(File.separator)) {
            return appSourceRoot + startPage;
         } else {
            return appSourceRoot + File.separator + startPage;
         }
     }
 
     @Override
     public void loadApp(XApplication app, XSecurityPolicy policy) {
         policy.checkAppStart(app, this);
     }
 
     @Override
     public RUNNING_MODE getRunningMode() {
         return RUNNING_MODE.LOCAL;
     }
 
     @Override
     public Iterator<byte[]> createResourceIterator(XApplication app,
             XIResourceFilter filter) {
         return new XLocalResourceIterator(app.getIntalledDir(), filter);
     }
 
     @Override
     public void onCheckSuccess(XApplication app, XISystemContext ctx) {
        String appUrl = getAppUrl(app);
        if (XFileUtils.fileExists(ctx.getContext(), appUrl)) {
            app.loadAppIntoView(appUrl);
         } else {
            app.getSystemContext().toast(
                    XStrings.getInstance().getString(XStrings.ENTRY_NOT_FOUND));
             app.loadErrorPage();
         }
     }
 
     @Override
     public void onCheckError(XApplication app, XISystemContext ctx) {
         app.loadErrorPage();
     }
 
     @Override
     public void onCheckStart(XApplication app, XISystemContext ctx) {

     }
 
 }
