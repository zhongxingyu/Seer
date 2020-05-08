 
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
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.res.XmlResourceParser;
 
 /**
  * 该类主要是从xml文件中读取所有的字符串常量保存下来
  */
 public class XStrings {
 
     public static String EXIT_MESSAGE_TITLE = "exit_message_title";
 
     public static String EXIT_MESSAGE_CONTENT = "exit_message_content";
 
     public static String CONFIRM = "confirm";
 
     public static String WAITING_MESSAGE_TITLE = "waiting_message_title";
 
     public static String WAITING_MESSAGE_CONTENT = "waiting_message_content";
 
     public static String CAN_NOT_FIND_HTML_MEAASGE = "can_not_find_html_message";
 
     public static String NO_XFACE_JS_MESSAGE = "no_xface_js_message";
 
     public static String SPLASH_LOAD_TEXT = "splash_load_text";
 
     public static String APP_CONFIG_ERROR = "app_config_error";
 
     private static String TAG_RESOURCES = "resources";
 
     private static String XML_NAME = "xface_string";
 
     private static String XML_DIRECTORY = "xml";
 
     private static XStrings instance = null;
 
     private HashMap<String, String> mStringPreferences;
 
     public static XStrings getInstance() {
         if (null == instance) {
             instance = new XStrings();
         }
         return instance;
     }
 
     /**
      * 读取平台定义的字符串常量存储下来
      *
      * @param context
      */
     public void loadPlatformStrings(Context context) {
         mStringPreferences = new HashMap<String, String>();
         try {
             int id = context.getResources().getIdentifier(XML_NAME,
                     XML_DIRECTORY, context.getPackageName());
             XmlResourceParser xml = context.getResources().getXml(id);
             int eventType = -1;
             String key = null;
             while (eventType != XmlResourceParser.END_DOCUMENT
                     && !TAG_RESOURCES.equals(xml.getName())) {
                 eventType = xml.next();
             }
             while (isLoadFinished(xml, eventType)) {
                 if (eventType == XmlResourceParser.TEXT) {
                     String text = xml.getText();
                     mStringPreferences.put(key, text);
                 }
                 if (eventType == XmlResourceParser.START_TAG) {
                     if (0 != xml.getAttributeCount()) {
                         key = xml.getAttributeValue(0);
                     }
                 }
                 eventType = xml.next();
             }
         } catch (XmlPullParserException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * 判断是否到达xml文件的结尾或者遇到resources的结束标签
      *
      * @param xml
      * @param eventType
      * @return
      */
     private boolean isLoadFinished(XmlResourceParser xml, int eventType) {
         return eventType != XmlResourceParser.END_DOCUMENT
                 && !(eventType == XmlResourceParser.END_TAG && TAG_RESOURCES
                         .equals(xml.getName()));
     }
 
     /**
      * 获取字符村常量的值
      *
      * @param key关键字
      * @return 字符串常量的值
      */
     public String getString(String key) {
         return mStringPreferences.get(key);
 
     }
 
 }
