 /**
  * Copyright (C) 2012 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.report;
 
 import com.crashnote.core.Lifecycle;
 import com.crashnote.core.config.CrashConfig;
 import com.crashnote.core.log.LogLog;
 import com.crashnote.core.model.log.ILogSession;
 import com.crashnote.core.model.log.LogEvt;
 import com.crashnote.core.report.impl.ThrowableLogEvt;
 import com.crashnote.core.report.impl.processor.Processor;
 import com.crashnote.core.report.impl.processor.impl.AsyncProcessor;
 import com.crashnote.core.report.impl.processor.impl.SyncProcessor;
 import com.crashnote.core.report.impl.session.LocalLogSession;
 
 /**
  * The Grand Central station of the library, every log event passes through here.
  *
 * It's main job is to take these events and put them into the {@link ILogSession},
  * the same goes for context data. It can automatically or manually flush the session
  * in order to send out a crash report by calling the internal {@link Processor}.
  */
 public class Reporter
     implements Thread.UncaughtExceptionHandler, Lifecycle {
 
     // VARS =======================================================================================
 
     private boolean started;
     private boolean initialized;
 
     private final LogLog logger;
     private Thread.UncaughtExceptionHandler defaultHandler;
 
     private final ILogSession session;
     private final Processor processor;
 
     // configuration settings:
     private final boolean enabled;
 
 
     // SETUP ======================================================================================
 
     public <C extends CrashConfig> Reporter(final C config) {
         this.initialized = false;
         this.enabled = config.isEnabled();
 
         this.logger = config.getLogger(this.getClass());
         this.session = createSessionStore(config);
         this.processor = createProcessor(config);
     }
 
     // LIFECYCLE ==================================================================================
 
     @Override
     public boolean start() {
         if (!started) {
             started = true;
             logger.debug("starting module [reporter]");
 
             processor.start();
             startSession();
         }
         return started;
     }
 
     @Override
     public boolean stop() {
         if (started) {
             logger.debug("stopping module [reporter]");
             endSession();
             processor.stop();
             started = false;
         }
         return started;
     }
 
 
     // INTERFACE ==================================================================================
 
     // ===== Session
 
     public void startSession() {
         if (isOperable())
             initSession();
     }
 
     public void flushSession() {
         if (isOperable() && !isSessionEmpty())
             processor.process(session);
     }
 
     public void endSession() {
         if (isOperable()) {
             flushSession();
             clearSession();
         }
     }
 
     public boolean isSessionEmpty() {
         return session.isEmpty();
     }
 
     // ===== Log Context
 
     public Reporter put(final String key, final Object val) {
         if (isOperable())
             session.putCtx(key, val);
         return this;
     }
 
     public Reporter remove(final String key) {
         if (isOperable())
             session.removeCtx(key);
         return this;
     }
 
     public Reporter clear() {
         if (isOperable())
             clearSession();
         return this;
     }
 
     // ===== Log Events
 
     public void reportLog(final LogEvt<?> evt) {
         if (isOperable()) {
             // add event to session
             session.addEvent(evt);
 
             // decide whether to send it immediately
             if (isAutoFlush()) endSession();
         }
     }
 
     // ===== Uncaught Exceptions
 
     @Override
     public void uncaughtException(final Thread t, final Throwable th) {
         // first call custom handler ...
         if (isOperable() && isInitialized())
             reportLog(new ThrowableLogEvt(t, th));
 
         // ... then call default handler
         callUncaughtExceptionToDefaultHandler(t, th);
     }
 
     public final void callUncaughtExceptionToDefaultHandler(final Thread t, final Throwable th) {
         if (defaultHandler != null)
             defaultHandler.uncaughtException(t, th);
     }
 
     public void registerAsDefaultExcpHandler() {
         final Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
         if (!(currentHandler instanceof Reporter)) {
             defaultHandler = currentHandler; // remember default handler
             Thread.setDefaultUncaughtExceptionHandler(this);
         }
     }
 
     public void unregisterAsDefaultExcpHandler() {
         if (defaultHandler != null)
             Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
     }
 
 
     // SHARED =====================================================================================
 
     protected boolean isAutoFlush() {
         return true;
     }
 
     protected final boolean isOperable() {
         return isEnabled() && isStarted();
     }
 
     protected final boolean isInitialized() {
         return initialized;
     }
 
 
     // FACTORY ====================================================================================
 
     protected <C extends CrashConfig> ILogSession createSessionStore(final C config) {
         return new LocalLogSession(); // SharedLogSession
     }
 
     protected <C extends CrashConfig> Processor createProcessor(final C config) {
         final SyncProcessor syncPrc = new SyncProcessor(config);
         if (config.isSync())
             return syncPrc;
         else
             return new AsyncProcessor(config, syncPrc);
     }
 
 
     // INTERNAL ===================================================================================
 
     private void initSession() {
         clearSession();
         initialized = true;
     }
 
     private void clearSession() {
         session.clear();
         initialized = false;
     }
 
 
     // GET ========================================================================================
 
     public Processor getProcessor() {
         return processor;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public boolean isStarted() {
         return started;
     }
 
     public ILogSession getSession() {
         return session;
     }
 
     public LogLog getLogger() {
         return logger;
     }
 }
