 
 /*
  This file was modified from or inspired by Apache Cordova.
 
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */
 
 package com.polyvi.xface.extension;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.polyvi.xface.extension.XExtensionResult.Status;
 import com.polyvi.xface.plugin.api.XIWebContext;
 import com.polyvi.xface.util.XFileUtils;
 import com.polyvi.xface.util.XLog;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 
 public class XStorageExt extends XExtension {
 
     private static final String CLASS_NAME = XStorageExt.class.getSimpleName();
 
     private static final String COMMAND_OPEN_DATABASE = "openDatabase";
 
     private static final String COMMAND_EXECUTE_SQL = "executeSql";
 
     private static final String DDL_COMMAND_ALTER = "alter";
 
     private static final String DDL_COMMAND_CREATE = "create";
 
     private static final String DDL_COMMAND_DROP = "drop";
 
     private static final String DDL_COMMAND_TRUNCATE = "truncate";
 
     private SQLiteDatabase mMyDb = null; /**<Database object*/
 
     private static final String DB_DIR_NAME = "database";
 
     @Override
     public void sendAsyncResult(String result) {
     }
 
     @Override
     public boolean isAsync(String action) {
         return false;
     }
 
     @Override
     public XExtensionResult exec(String action,
             JSONArray args, XCallbackContext callbackCtx) throws JSONException {
         try{
             Status status = Status.OK;
             if(COMMAND_OPEN_DATABASE.equals(action)){
                 boolean isDatabaseopen = openDatabase(mWebContext, args.getString(0));
                 if (!isDatabaseopen) {
                     status = Status.ERROR;
                 }
             } else if(COMMAND_EXECUTE_SQL.equals(action)){
                 status = Status.NO_RESULT;
                 String[] params = null;
                 if(args.isNull(1)){
                     params = new String[0];
                 }else{
                     JSONArray paramJsonArray = args.getJSONArray(1);
                     int len = paramJsonArray.length();
                     params = new String[len];
                     for (int i = 0; i < len; i++) {
                         params[i] = paramJsonArray.getString(i);
                     }
                 }
                 executeSql(args.getString(0), params, args.getString(2), callbackCtx);
             }
             return new XExtensionResult(status);
         }catch(JSONException e){
             return new XExtensionResult(Status.ERROR);
         }
     }
 
     /**
      * 打开数据库.
      *
      * @param db
      *            数据库名
      */
     private boolean openDatabase(XIWebContext webContext, String db) {
         if (null != mMyDb) {
             mMyDb.close();
         }
 
         File dbDir = new File(webContext.getApplication().getDataDir(),DB_DIR_NAME);
         if(!dbDir.exists()){
             dbDir.mkdirs();
         }
         boolean isValidDbPath = true;
         try {
             // 根据浏览器上面openDatabase，数据库名为null则创建名为null的数据库，数据库名为空串则创建名为空串的数据库
             String dbPath = new File(dbDir, db).getCanonicalPath();
             // 验证数据库路径是否合法（是否在appData下面的database中）
             if (!XFileUtils.isFileAncestorOf(dbDir.getAbsolutePath(), dbPath)) {
                 XLog.e(CLASS_NAME, dbPath);
                 isValidDbPath = false;
             } else {
                 mMyDb = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
             }
         } catch (IOException e) {
             isValidDbPath = false;
             XLog.e(CLASS_NAME, e.toString());
         }
         return isValidDbPath;
     }
 
     /**
      * 执行SQL语句.
      *
      * @param query
      *            SQL查询语句
      * @param params
      *            SQL查询语句的参数
      * @param tx_id
      *            事务 id
      * @param callbackCtx
      *            回调上下文环境
      */
     private void executeSql(String query, String[] params,
             String tx_id, XCallbackContext callbackCtx) {
         try {
             if (isDDL(query)) {
                 mMyDb.execSQL(query);
                 String jsScript = "xFace.require('xFace/extension/android/storage').completeQuery('"
                + tx_id + "', '');";
                mWebContext.getApplication().loadJavascript(jsScript);
             } else {
                 Cursor myCursor = mMyDb.rawQuery(query, params);
                 processResults(myCursor, tx_id, callbackCtx);
                 myCursor.close();
             }
         } catch (SQLiteException ex) {
             XLog.e(CLASS_NAME, "Storage.executeSql(): Error=" + ex.getMessage());
             String jsScript = "xFace.require('xFace/extension/android/storage').failQuery('"
                     + ex.getMessage() + "','" + tx_id + "');";
             callbackCtx.sendExtensionResult(jsScript);
         }
     }
 
     /**
      * 检查SQL语句是否是数据定义语言
      *
      * @param query 待执行的SQL语句
      * @return true 如果是数据定义语言, false 不是数据定义语言（数据控制语言）
      */
     private boolean isDDL(String query) {
         String cmd = query.toLowerCase();
         if (cmd.startsWith(DDL_COMMAND_ALTER)
                 || cmd.startsWith(DDL_COMMAND_CREATE)
                 || cmd.startsWith(DDL_COMMAND_DROP)
                 || cmd.startsWith(DDL_COMMAND_TRUNCATE)) {
             return true;
         }
         return false;
     }
 
     /**
      * 处理查询结果.
      *
      * @param cur
      *            查询结果的游标
      * @param tx_id
      *            事务 id
      * @param callbackCtx
      *            回调上下文环境
      */
     private void processResults(Cursor cur, String tx_id, XCallbackContext callbackCtx) {
 
         String result = "[]";
 
         if (cur.moveToFirst()) {
             JSONArray fullresult = new JSONArray();
             String key = "";
             String value = "";
             int colCount = cur.getColumnCount();
 
             do {
                 JSONObject row = new JSONObject();
                 try {
                     for (int i = 0; i < colCount; ++i) {
                         key = cur.getColumnName(i);
                         value = cur.getString(i);
                         row.put(key, value);
                     }
                     fullresult.put(row);
 
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
 
             } while (cur.moveToNext());
 
             result = fullresult.toString();
         }
 
         String jsScript = "xFace.require('xFace/extension/android/storage').completeQuery('"
                 + tx_id + "', " + result + ");";
        mWebContext.getApplication().loadJavascript(jsScript);
     }
 }
