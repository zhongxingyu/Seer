 /*
  * #%L
  * xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 package com.sap.prd.mobile.ios.mios;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.logging.ErrorManager;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.plugin.logging.SystemStreamLog;
 
 
 public class XCodePluginLogger extends Logger
 {
   private Log log = new SystemStreamLog();
   
   private final static Collection<Level> DEBUG_LEVELS = Collections.unmodifiableCollection(Arrays.asList(Level.ALL,
         Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG));
 
   public XCodePluginLogger()
   {
     super(getLoggerName(), null);
     setUseParentHandlers(false);
 
     for(Handler h : getHandlers()) {
       super.removeHandler(h);
     }
 
     addHandler(new Handler() {
 
       @Override
       public void close() throws SecurityException
       {
       }
 
       @Override
       public void flush()
       {
       }
 
       @Override
       public boolean isLoggable(LogRecord record)
       {
         if(DEBUG_LEVELS.contains(record.getLevel()))
         {
           return log.isDebugEnabled();
         }
 
         return super.isLoggable(record);
       }
 
       @Override
       public void publish(LogRecord record)
       {
         final Level level = record.getLevel();
 
         if(DEBUG_LEVELS.contains(level))
         {
           if(record.getThrown() == null)
           {
             log.debug(record.getMessage());
           } 
           else
           { 
             log.debug(record.getMessage(), record.getThrown());
           }
         } 
         else if(level == Level.INFO)
         {
           if(record.getThrown() == null)
           {
             log.info(record.getMessage());
           }
           else 
           {
             log.info(record.getMessage(), record.getThrown());
           }
         }
         else if(level == Level.WARNING) 
         {
           if(record.getThrown() == null)
           {
             log.warn(record.getMessage());
           }
           else 
           {
             log.warn(record.getMessage(), record.getThrown());
           }
         }
         else if(level == Level.SEVERE) 
         {
           if(record.getThrown() == null)
           {
             log.error(record.getMessage());
           }
           else 
           {
             log.error(record.getMessage(), record.getThrown());
           }
         }
         else
           getErrorManager().error("Cannot handle log message with level '" + record.getLevel() + "'.", null, ErrorManager.GENERIC_FAILURE);
       }
     });
   }
 
  @Override
  public synchronized void removeHandler(Handler handler) throws SecurityException
  {
    throw new UnsupportedOperationException();
  }
  
   public void setLog(Log log) {
 
     if(log == null)
       throw new NullPointerException();
 
     this.log = log;
   }
   
   public final static String getLoggerName() {
     return XCodePluginLogger.class.getPackage().getName();
   }
 }
