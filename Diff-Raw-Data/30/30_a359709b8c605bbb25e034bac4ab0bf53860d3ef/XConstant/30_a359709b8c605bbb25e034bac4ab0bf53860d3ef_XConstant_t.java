 
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
 
 package com.polyvi.xface.util;
 
 import java.io.File;
 
 public class XConstant {
 
     /** 默认buffer长度 */
     public static final int BUFFER_LEN = 2048;
 
     /** 一秒内的毫秒数 */
     public static final int MILLISECONDS_PER_SECOND = 1000;
 
     /** file scheme */
     public static final String FILE_SCHEME = "file://";
 
     /** http scheme */
     public static final String HTTP_SCHEME = "http://";
 
     /** https scheme */
     public static final String HTTPS_SCHEME = "https://";
 
     /** content scheme */
     public static final String CONTENT_SCHEME = "content:";
 
     /** sms scheme*/
     public static final String SCHEME_SMS = "sms:";
 
     /** app的默认启动页面 */
     public static final String DEFAULT_START_PAGE_NAME = "index.html";
 
     /** portal安装成功 */
     public static final int PORTAL_INSTALL_SUCCESS = 1;
 
     /** portal安装失败 */
     public static final int PORTAL_INSTALL_FAIL = 0;
 
     /** assets目录下需要解压的zip包名 */
     public static final String ASSET_PACKAGE_FILE_NAME = "xFaceInstalledPackage.zip";
 
     /** 系统配置文件名称 */
     public static final String CONFIG_FILE_NAME = "config.xml";
 
     /** 预安装的app所在目录名称 */
     public static final String PREINSTALL_PACKAGE_DIR_NAME = "pre_install";
 
     /** 默认app类型 */
     public static final String APP_TYPE_DEFAULT = "app";
 
     /** 应用的配置文件名称 */
     public static final String APP_CONFIG_FILE_NAME = "app.xml";
 
     /** 应用的工作目录名称 */
     public static final String APP_WORK_DIR_NAME = "workspace";
 
     /** 应用在工作目录中内置数据包名*/
     public static final String APP_DATA_PACKAGE_NAME_IN_WORKSAPCE = "workspace.zip";
 
     /** 存储应用数据的目录名称 */
     public static final String APP_DATA_DIR_NAME = "data";
 
     /** 离线应用缓存路径 */
     public static final String APP_CACHE_PATH = "app_cache";
 
     /** 预置应用包目录名称*/
     public static final String PRE_SET_APP_PACKAGE_DIR_NAME = "pre_set";
     /**加密数据包目录名称*/
     public static final String ENCRYPT_CODE_DIR_NAME = "encrypt_code";
 
     /** 安装包的后缀名*/
     public static final String APP_PACKAGE_SUFFIX = ".zip";
     /**native安装包的后缀名*/
     public static final String NATIVE_APP_SUFFIX_NPA = ".npa";
     /** 安装包的后缀名,离散文件方式*/
     public static final String APP_PACKAGE_SUFFIX_XPA = ".xpa";
     /** 安装包的后缀名,SingleFile方式*/
     public static final String APP_PACKAGE_SUFFIX_XSPA = ".xspa";
 
     /**js框架实现文件的文件名*/
     public static final String XFACE_JS_FILE_NAME = "xface.js";
 
     /** 默认错误文件名 **/
     public static final String ERROR_PAGE_NAME = "xFaceError.html";
 
     /** app启动参数*/
     public static final String TAG_APP_START_PARAMS = "start_params";
 
     /**存放程序数据的文件夹*/
     public static final String ANDROID_DIR = "Android";
 
     public static final String ASSERT_PROTACAL =  "file:///android_asset/" ;
     public static final String PREF_SETTING_FILE_NAME = "preference.pref";
     public static final String TAG_WD_STRATEGY = "wd_strategy";
 
    public static final String PRE_INSTALL_SOURCE_ROOT = "xface3" + File.separator;

    public static final String PLUGIN_JS_METADATA_FILE = "cordova_plugins.js";
 
 }
