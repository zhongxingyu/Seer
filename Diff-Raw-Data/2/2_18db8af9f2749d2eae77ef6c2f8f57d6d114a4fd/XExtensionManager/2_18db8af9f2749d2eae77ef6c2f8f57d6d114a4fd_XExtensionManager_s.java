 
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
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.polyvi.xface.core.XConfiguration;
 import com.polyvi.xface.core.XRuntime;
 import com.polyvi.xface.extension.XExtensionResult.Status;
 import com.polyvi.xface.plugin.api.XIWebContext;
 import com.polyvi.xface.util.XLog;
 import com.umeng.analytics.MobclickAgent;
 
 /**
  * 系统扩展管理器 ，程序所有js扩展的执行入口以及装载所有的扩展插件
  * 
  */
 public class XExtensionManager {
 
     private static final String CLASS_NAME = XExtensionManager.class
             .getSimpleName();
     private static final String EMPTY_STRING = "";
     private static final int THREAD_POOL_TERMINATION_TIMEOUT = 500;
 
     private final ExecutorService mExecThreadPool = Executors
             .newCachedThreadPool();
     /**
      * 存放所有扩展的列表
      */
     private HashMap<String, XExtension> mExtensionMap;
     private XExtensionContext mExtensionContext;
     private XIWebContext mWebContext;
 
     public XExtensionManager(XIWebContext webContext,
             XExtensionContext extensionContext) {
         mExtensionMap = new HashMap<String, XExtension>();
         mExtensionContext = extensionContext;
         mWebContext = webContext;
     }
 
     /**
      * 执行扩展的入口
      * @param service 需要执行的扩展名字
      * @param action 扩展对应的行为
      * @param callbackId 回调id 从js传下来
      * @param arguments 执行扩展的参数
      * @return 扩展是否同步执行
      */
     public boolean exec(String service, final String action, String callbackId,
             String arguments) {
         final XExtension extension = mExtensionMap.get(service);
         if (null == extension) {
            XLog.d(CLASS_NAME, "%s not registered!", service);
             XExtensionResult er = new XExtensionResult(
                     XExtensionResult.Status.CLASS_NOT_FOUND_EXCEPTION);
             mWebContext.sendExtensionResult(er, callbackId);
             return true;
         }
         try {
             JSONArray args = new JSONArray(arguments);
             final XCallbackContext callbackCtx = new XCallbackContext(
                     mWebContext, callbackId);
             exec(extension, action, args, callbackCtx);
             return callbackCtx.isFinished();
         } catch (JSONException e) {
             XExtensionResult er = new XExtensionResult(
                     XExtensionResult.Status.JSON_EXCEPTION);
             mWebContext.sendExtensionResult(er, callbackId);
             return true;
         }
 
     }
 
     private String exec(final XExtension extension, final String action,
             final JSONArray args, final XCallbackContext ctx) {
         XExtensionResult result = null;
         try {
             boolean runAync = extension.isAsync(action);
             if (runAync) {
                 mExecThreadPool.execute(new Runnable() {
                     public void run() {
                         XExtensionResult result;
                         try {
                             result = extension.exec(action, args, ctx);
                         } catch (JSONException e) {
                             result = new XExtensionResult(
                                     XExtensionResult.Status.ERROR, e
                                             .getMessage());
                             XLog.d(CLASS_NAME, e.getMessage());
                         } catch (Exception e) {
                             result = new XExtensionResult(
                                     XExtensionResult.Status.ERROR, e
                                             .getMessage());
                             XLog.d(CLASS_NAME, e.getMessage());
                             e.printStackTrace();
                             // 将异常的栈信息发送到服务器
                             reportError(e);
                         }
                         if (Status.NO_RESULT.ordinal() != result.getStatus()) {
                             ctx.sendExtensionResult(result);
                         }
 
                     }
                 });
                 return EMPTY_STRING;
             } else {
                 // 处理同步执行业务
                 result = extension.exec(action, args, ctx);
                 if (Status.NO_RESULT.ordinal() == result.getStatus()) {
                     return EMPTY_STRING;
                 }
             }
         } catch (JSONException e) {
             result = new XExtensionResult(XExtensionResult.Status.ERROR,
                     e.getMessage());
             XLog.d(CLASS_NAME, e.getMessage());
         } catch (Exception e) {
             XLog.d(CLASS_NAME, e.getMessage());
             e.printStackTrace();
             // 将异常的栈信息发送到服务器
             reportError(e);
         }
         if (null != result) {
             ctx.sendExtensionResult(result);
         }
         return EMPTY_STRING;
     }
 
     /**
      * 设置ams扩展对象，ams对象在{@link XRuntime}中创建，然后设置到XExtensionManager中
      * 
      * @param ams
      *            ams扩展对象
      */
     public void registerExtension(String extName, XExtension extension) {
         mExtensionMap.put(extName, extension);
     }
 
     private XExtension createExtension(String className) {
         try {
             Class<?> cls = Class.forName(className);
             Object obj = cls.newInstance();
             if (!(obj instanceof XExtension)) {
                 XLog.e(CLASS_NAME, "Class (" + className
                         + ") not a sub class of XExtension!");
                 return null;
             }
             XExtension extension = (XExtension) obj;
             extension.init(mExtensionContext, mWebContext);
             return extension;
         } catch (ClassNotFoundException e) {
             XLog.e(CLASS_NAME, "Class:" + className + " not found!");
         } catch (InstantiationException e) {
             XLog.e(CLASS_NAME, "Can't create object of class " + className);
         } catch (IllegalAccessException e) {
             XLog.e(CLASS_NAME, "Can't create object of class " + className);
         }
         return null;
     }
 
     /**
      * 加载扩展对象
      */
     public void loadExtensions() {
         HashMap<String, XExtensionEntry> loadingExtensions = XConfiguration
                 .getInstance().readLoadingExtensions(mExtensionContext);
         Iterator<Entry<String, XExtensionEntry>> iter = loadingExtensions
                 .entrySet().iterator();
         while (iter.hasNext()) {
             Entry<String, XExtensionEntry> entry = iter.next();
             String extName = entry.getKey();
             String className = entry.getValue().getExtClassName();
             XExtension ext = createExtension(className);
             if (null == ext) {
                 continue;
             }
             registerExtension(extName, ext);
         }
     }
 
     public void destroy() {
         Iterator<Entry<String, XExtension>> extensionIterator = mExtensionMap
                 .entrySet().iterator();
         while (extensionIterator.hasNext()) {
             HashMap.Entry<String, XExtension> entry = (HashMap.Entry<String, XExtension>) extensionIterator
                     .next();
             XExtension extension = (XExtension) entry.getValue();
             extension.destroy();
         }
         shutdownAndAwaitTermination(mExecThreadPool);
     }
 
     /**
      * 关闭线程池并等待退出
      */
     private void shutdownAndAwaitTermination(ExecutorService pool) {
         pool.shutdown(); // Disable new tasks from being submitted
         try {
             // Wait a while for existing tasks to terminate
             if (!pool.awaitTermination(THREAD_POOL_TERMINATION_TIMEOUT,
                     TimeUnit.MILLISECONDS)) {
                 pool.shutdownNow(); // Cancel currently executing tasks
                 // Wait a while for tasks to respond to being cancelled
                 if (!pool.awaitTermination(THREAD_POOL_TERMINATION_TIMEOUT,
                         TimeUnit.MILLISECONDS)) {
                     XLog.d(CLASS_NAME, "Thread Pool did not terminate");
                 }
 
             }
         } catch (InterruptedException ie) {
             // (Re-)Cancel if current thread also interrupted
             pool.shutdownNow();
             // Preserve interrupt status
             Thread.currentThread().interrupt();
         }
     }
 
     /**
      * 当页面切换时，通知每个 ext 回调
      */
     public void onPageStarted() {
         Iterator<XExtension> extensionIterator = mExtensionMap.values()
                 .iterator();
         while (extensionIterator.hasNext()) {
             extensionIterator.next().onPageStarted();
         }
     }
 
     /**
      * 当退出app时，通知每个 ext 回调
      */
     public void onAppClosed() {
         Iterator<XExtension> extensionIterator = mExtensionMap.values()
                 .iterator();
         while (extensionIterator.hasNext()) {
             extensionIterator.next().onAppClosed();
         }
     }
 
     /**
      * 当卸载app时，通知每个 ext 回调
      */
     public void onAppUninstalled() {
         Iterator<XExtension> extensionIterator = mExtensionMap.values()
                 .iterator();
         while (extensionIterator.hasNext()) {
             extensionIterator.next().onAppUninstalled();
         }
     }
 
     /**
      * 当app在后台时，通知每个ext回调
      * 
      * @param appId
      */
     public void onPause() {
         Iterator<XExtension> extensionIterator = mExtensionMap.values()
                 .iterator();
         while (extensionIterator.hasNext()) {
             extensionIterator.next().onPause();
         }
     }
 
     /**
      * 当app恢复到前天时，通知每个ext回调
      * 
      * @param appId
      */
     public void onResume() {
         Iterator<XExtension> extensionIterator = mExtensionMap.values()
                 .iterator();
         while (extensionIterator.hasNext()) {
             extensionIterator.next().onResume();
         }
     }
 
     public XExtensionContext getExtensionContext() {
         return mExtensionContext;
     }
 
     /**
      * 将异常信息发送到umeng的服务器端
      * 
      * @param e
      */
     public void reportError(Exception e) {
         try {
             // 将异常的栈信息写入Writer中
             Writer writer = new StringWriter();
             PrintWriter printWriter = new PrintWriter(writer);
             e.printStackTrace(printWriter);
             printWriter.flush();
             writer.flush();
             String stackTrackInfo = writer.toString();
             printWriter.close();
             writer.close();
             MobclickAgent.reportError(mExtensionContext.getSystemContext()
                     .getContext(), stackTrackInfo);
         } catch (IOException ioE) {
             XLog.e(CLASS_NAME, ioE.getMessage());
         } catch (Exception genericE) {
             XLog.e(CLASS_NAME, genericE.getMessage());
         }
     }
 }
