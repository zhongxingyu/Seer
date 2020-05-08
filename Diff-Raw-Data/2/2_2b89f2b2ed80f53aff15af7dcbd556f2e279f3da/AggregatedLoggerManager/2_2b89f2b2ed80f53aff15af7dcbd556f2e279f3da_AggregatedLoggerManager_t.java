 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
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
 package org.xwiki.contrib.mailarchive.internal;
 
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.slf4j.Logger;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.logging.LogLevel;
 import org.xwiki.logging.LoggerManager;
 
 /**
  * A Logger Manager that brings additional features:<br/>
  * <ul>
  * <li>Manages log level of a group of loggers, to avoid having to manage them one by one</li>
  * <li>Ability to restore loggers log levels at their previous state.</li>
  * </ul>
  * The idea is to provide an easy way to manage logs at Application level, from runtime. There is no link to loggers
  * initial configuration.<br/>
  * For example:
  * <p>
  * <blockquote>
  * 
  * <pre>
  *   private IAggregatedLoggerManager myAppLoggerManager;
  *   
  *   ...
  *   
  *   
  *   // Define groupings
  *   myAppLoggerManager.addLogger("myAppPackage1")
  *   myAppLoggerManager.addLogger("myAppPackage2")
  *   ...
  *   
  *   // Enters debug mode
  *   myAppLoggerManager.pushLogLevel(LogLevel.DEBUG)
  *   
  *   // ... from now on everything is logged at DEBUG level for configured loggers
  *   
  *   // Quits debug mode
  *   myAppLoggerManager.popLogLevel()
  *   
  *   // ... from now on loggers log levels are back to their values previous to pushLogLevel()
  * }
  * </pre>
  * 
  * </blockquote>
  * </p>
  * 
  * @author jbousque
  * @version $Id$
  */
 @Component
 @Singleton
 public class AggregatedLoggerManager implements IAggregatedLoggerManager
 {
 
     /**
      * A Map linking logger names to their previous log level.
      */
     private Map<String, LogLevel> loggers;
 
     @Inject
     LoggerManager loggerManager;
 
     @Inject
     Logger privateLogger;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#pushLogLevel(org.xwiki.logging.LogLevel)
      */
     @Override
     public void pushLogLevel(LogLevel logLevel)
     {
         if (this.loggers != null) {
             for (String logger : loggers.keySet()) {
                 privateLogger
                     .warn("Saving log level " + loggerManager.getLoggerLevel(logger) + " for logger " + logger);
                 loggers.put(logger, loggerManager.getLoggerLevel(logger));
                 privateLogger.warn("Setting log level " + logLevel + " to logger " + logger);
                 loggerManager.setLoggerLevel(logger, logLevel);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#popLogLevel()
      */
     @Override
     public void popLogLevel()
     {
         if (this.loggers != null) {
             for (String logger : loggers.keySet()) {
                 final LogLevel origLogLevel = loggers.get(logger);
                 if (origLogLevel != null) {
                     loggerManager.setLoggerLevel(logger, origLogLevel);
                 }
             }
         }
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#addLogger(java.lang.String)
      */
     @Override
     public void addLogger(String loggerName)
     {
         if (this.loggers == null) {
             this.loggers = new HashMap<String, LogLevel>();
         }
         // do not overwrite log level if that logger was already added
         if (!this.loggers.containsKey(loggerName)) {
             final LogLevel previousLogLevel = this.loggerManager.getLoggerLevel(loggerName);
             this.loggers.put(loggerName, previousLogLevel);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#removeLogger(java.lang.String)
      */
     @Override
     public void removeLogger(String loggerName)
     {
         if (this.loggers != null && this.loggers.containsKey(loggerName)) {
             this.loggers.remove(loggerName);
         }
     }
 
     @Override
     public void addComponentLogger(Type roleType)
     {
         privateLogger.warn("addComponentLogger(Type=" + roleType + ')');
        final String clazzName = ((Class< ? >) roleType).getCanonicalName();
         privateLogger.warn("clazzName=" + clazzName);
         String packageName = clazzName.substring(0, clazzName.lastIndexOf('.'));
         if (packageName.endsWith("internal")) {
             packageName = packageName.substring(0, packageName.length() - 9);
         }
         privateLogger.warn("adding managed logger for package " + packageName);
         addLogger(packageName);
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#setLoggers(java.util.List)
      */
     @Override
     public void setLoggers(Set<String> loggers)
     {
         cleanLoggers();
         for (String logger : loggers) {
             addLogger(logger);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#getLoggers()
      */
     @Override
     public Set<String> getLoggers()
     {
         return this.loggers.keySet();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.contrib.mailarchive.internal.IAggregatedLoggerManager#cleanLoggers()
      */
     @Override
     public void cleanLoggers()
     {
         this.loggers = new HashMap<String, LogLevel>();
     }
 
 }
