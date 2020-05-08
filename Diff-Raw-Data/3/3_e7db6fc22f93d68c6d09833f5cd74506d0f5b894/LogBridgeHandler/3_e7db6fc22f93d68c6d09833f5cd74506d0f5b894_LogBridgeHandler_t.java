 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2009, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.logbridge;
 
 import java.util.Map;
 import java.util.Collections;
 import java.util.WeakHashMap;
 import java.util.Enumeration;
 import java.util.ResourceBundle;
 import java.util.MissingResourceException;
 import java.text.MessageFormat;
 
 import java.util.logging.Handler;
 import java.util.logging.LogRecord;
 import java.util.logging.Filter;
 import java.util.logging.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 import org.apache.log4j.spi.LoggerRepository;
 
 /**
  *
  */
 public final class LogBridgeHandler extends Handler {
 
     private final LevelMapper levelMapper = new LevelMapper();
 
     @SuppressWarnings({ "NonConstantLogger" })
     private final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
 
     /**
      * A weak-key map of log4j loggers to JDK loggers.  Keep this map so that the configuration setting is
      * retained even if the JDK logger is otherwise unreferenced.
      */
     private final Map<Logger, java.util.logging.Logger> loggerMap = Collections.synchronizedMap(new WeakHashMap<Logger,java.util.logging.Logger>());
 
     private static final Logger log = Logger.getLogger(LogBridgeHandler.class);
     private static final String LOGGER_CLASS_NAME = java.util.logging.Logger.class.getName();
 
     public void setFilter(final Filter newFilter) throws SecurityException {
     }
 
     public void setLevel(final Level newLevel) throws SecurityException {
     }
 
     public boolean isLoggable(final LogRecord record) {
         return true;
     }
 
     public void publish(final LogRecord record) {
        final String name = record.getLoggerName();
        Logger targetLogger = Logger.getLogger(name == null ? "anonymous" : name);
         final Priority targetLevel = levelMapper.getTargetLevelForSourceLevel(record.getLevel());
         final String text = formatRecord(record);
         targetLogger.log(LOGGER_CLASS_NAME, targetLevel, text, record.getThrown());
     }
 
     private String formatRecord(final LogRecord record) {
         final ResourceBundle bundle = record.getResourceBundle();
         String msg = record.getMessage();
         if (bundle != null) {
             try {
                 msg = bundle.getString(msg);
             } catch (MissingResourceException ex) {
                 // ignore
             }
         }
         final Object[] parameters = record.getParameters();
         return parameters != null &&
                 parameters.length > 0 &&
                 msg.indexOf('{') >= 0 ? MessageFormat.format(msg, parameters) : msg;
     }
 
     public void flush() {
     }
 
     public void close() throws SecurityException {
     }
 
     public void start() {
         rootLogger.addHandler(this);
         updateLoggers();
     }
 
     public void stop() {
         rootLogger.removeHandler(this);
     }
 
     LevelMapper getLevelMapper() {
         return levelMapper;
     }
 
     public void updateLoggers() {
         log.trace("Syncing up JDK logger levels from Log4j");
         final Map<Logger, java.util.logging.Logger> loggerMap = this.loggerMap;
         final LevelMapper levelMapper = this.levelMapper;
         loggerMap.clear();
         final Logger rootLogger = Logger.getRootLogger();
         final LoggerRepository repository = rootLogger.getLoggerRepository();
         final Enumeration loggers = repository.getCurrentLoggers();
         while (loggers.hasMoreElements()) {
             final Logger logger = (Logger) loggers.nextElement();
             final String name = logger.getName();
             final java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger(name);
             final org.apache.log4j.Level targetLevel = logger.getLevel();
             if (targetLevel == null) {
                 if (log.isTraceEnabled()) {
                     log.trace("Remapping logger \"" + name + "\" with null level");
                 }
                 jdkLogger.setLevel(null);
             } else {
                 final Level sourceLevel = levelMapper.getSourceLevelForTargetLevel(targetLevel);
                 if (log.isTraceEnabled()) {
                     log.trace("Remapping logger \"" + name + "\" to JDK level \"" + sourceLevel + "\"");
                 }
                 loggerMap.put(logger, jdkLogger);
                 jdkLogger.setLevel(sourceLevel);
             }
         }
         final Level sourceLevel = levelMapper.getSourceLevelForTargetLevel(rootLogger.getLevel());
         if (log.isTraceEnabled()) {
             log.trace("Remapping root logger to JDK level \"" + sourceLevel + "\"");
         }
         this.rootLogger.setLevel(sourceLevel);
     }
 }
