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
 import com.crashnote.logger.config.*;
 import com.crashnote.logger.report.LoggerReporter;
 import org.slf4j.MDC;
 import org.slf4j.spi.MDCAdapter;
 
 import java.util.Map;
 import java.util.logging.*;
 
 /**
  * Appender that writes logs from 'Java Logging API (JUL)' to the cloud
  */
 public class CrashHandler
     extends Handler implements ICrashAppender {
 
     private boolean started;
 
     private MDCAdapter mdc;

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
         try {
             mdc = MDC.getMDCAdapter();
         } catch (Exception ignored) {
             mdc = null;
         }
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
 
     public boolean isStarted() {
         return started;
     }
 
     public void setLogLevel(final LogLevel lvl) {
         if (lvl == LogLevel.DEBUG)
            setLevel(Level.FINE);
         else if (lvl == LogLevel.INFO)
            setLevel(Level.INFO);
         else if (lvl == LogLevel.WARN)
            setLevel(Level.WARNING);
         else
            setLevel(Level.SEVERE);
     }
 
     @Override
     public boolean isLoggable(final LogRecord record) {
        return record.getLevel().intValue() >= getLevel().intValue() &&
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
         if (config == null) config = (LoggerConfig) configFactory.get();
         return config;
     }
 
     private LoggerReporter<LoggerConfig> getReporter() {
         if (reporter == null) reporter = getConfig().getReporter();
         return reporter;
     }
 
     private Map getContext() {
         return (mdc != null) ? mdc.getCopyOfContextMap() : null;
     }
 
     // PARAMETERS =================================================================================
 
     public void setPort(final String port) {
         configFactory.setPort(port);
     }
 
     public void setHost(final String host) {
         configFactory.setHost(host);
     }
 
     public void setKey(final String key) {
         configFactory.setKey(key);
     }
 
     public void setEnabled(final String enabled) {
         configFactory.setEnabled(enabled);
     }
 
     public void setSslPort(final String sslPort) {
         configFactory.setSslPort(sslPort);
     }
 
     public void setSecure(final String secure) {
         configFactory.setSecure(secure);
     }
 
     public void setSync(final String on) {
         configFactory.setSync(on);
     }
 }
