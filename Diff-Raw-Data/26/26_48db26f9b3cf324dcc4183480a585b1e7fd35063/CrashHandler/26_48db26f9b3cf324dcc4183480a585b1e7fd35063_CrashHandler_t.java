 /**
  * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
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
 package com.crashnote.jul;
 
 import com.crashnote.ICrashAppender;
 import com.crashnote.core.model.types.LogLevel;
 import com.crashnote.jul.impl.JulEvt;
 import com.crashnote.logger.config.LoggerConfig;
 import com.crashnote.logger.config.LoggerConfigFactory;
 import com.crashnote.logger.report.LoggerReporter;
 
 import java.util.Map;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 /**
  * Appender that writes logs from 'Java Logging API (JUL)' to the cloud
  */
 public class CrashHandler
         extends Handler implements ICrashAppender {
 
     // VARS =======================================================================================
 
     private boolean started;
     private Level logLevel = Level.INFO;
 
    //private MDCAdapter mdc;
     private LoggerReporter<LoggerConfig> reporter;
 
     // config
     private LoggerConfig config;
     private final LoggerConfigFactory configFactory;
 
 
     // SETUP ======================================================================================
 
     public CrashHandler() {
         this(new LoggerConfigFactory());
     }
 
     public CrashHandler(final LoggerConfigFactory configFactory) {
         this.configFactory = configFactory;
         init();
     }
 
     public CrashHandler(final LoggerConfig config, final LoggerReporter reporter) {
         this.config = config;
         this.reporter = reporter;
         this.configFactory = null;
         init();
     }
 
     private void init() {
        /*
         try {
             mdc = MDC.getMDCAdapter();
         } catch (Exception ignored) {
             mdc = null;
         }
        */
         start();
     }
 
 
     // INTERFACE ==================================================================================
 
     @Override
     public void publish(final LogRecord record) {
         if (isLoggable(record))
             getReporter().reportLog(new JulEvt(record, getContext()));
     }
 
     @Override
     public void flush() {
         // nothing to do
     }
 
     @Override
     public boolean isStarted() {
         return started;
     }
 
     /**
      * Set the minimum log level for the handler, so it does not accept records with lower level.
      * Cannot use setLevel() from base class since it throws an AccessControlException on AppEngine.
      */
     @Override
     public void setLogLevel(final LogLevel lvl) {
         if (lvl == LogLevel.DEBUG)
             logLevel = Level.FINE;
         else if (lvl == LogLevel.INFO)
             logLevel = Level.INFO;
         else if (lvl == LogLevel.WARN)
             logLevel = Level.WARNING;
         else
             logLevel = Level.SEVERE;
     }
 
     @Override
     public boolean isLoggable(final LogRecord record) {
         return record.getLevel().intValue() >= logLevel.intValue() &&
                 getReporter().doAcceptLog(record.getLoggerName());
     }
 
     @Override
     public void close() throws SecurityException {
         if (started) {
             getReporter().stop();
             started = false;
         }
     }
 
     public static Logger getTargetLogger(final Class clazz) {
         return Logger.getLogger(clazz.getName());
     }
 
 
     // INTERNALS ==================================================================================
 
     private void start() {
         if (!started) {
             setLogLevel(getConfig().getLogLevel());
             getReporter().start();
             started = true;
         }
     }
 
     private LoggerConfig getConfig() {
         if (config == null)
             config = (LoggerConfig) configFactory.get();
         return config;
     }
 
     private LoggerReporter<LoggerConfig> getReporter() {
         if (reporter == null) reporter = getConfig().getReporter();
         return reporter;
     }
 
     private Map getContext() {
        return null;
        //return (mdc != null) ? mdc.getCopyOfContextMap() : null;
     }
 
 }
