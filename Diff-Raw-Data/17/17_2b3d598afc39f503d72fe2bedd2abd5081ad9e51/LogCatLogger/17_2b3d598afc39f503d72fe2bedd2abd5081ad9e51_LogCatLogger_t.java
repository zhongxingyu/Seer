 /*
  * Copyright 2010 CodeGist.org
  *
  *     Licensed under the Apache License, Version 2.0 (the "License");
  *     you may not use this file except in compliance with the License.
  *     You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *     Unless required by applicable law or agreed to in writing, software
  *     distributed under the License is distributed on an "AS IS" BASIS,
  *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *     See the License for the specific language governing permissions and
  *     limitations under the License.
  *
  *  ===================================================================
  *
  *  More information at http://www.codegist.org.
  */
 
 package org.codegist.common.log;
 
 import android.util.Log;
import org.codegist.common.lang.Strings;
 
 import java.io.Serializable;
 
 import static java.lang.String.valueOf;
 
 /**
  * @author laurent.gilles@codegist.org
  */
 public class LogCatLogger extends AbstractLogger implements Serializable {
 
     private final String name;
 
     public LogCatLogger(String name) {
        this.name = Strings.substringRight(name, 23);
     }
 
     @Override
     protected void logError(Object message, Throwable e) {
         Log.e(name, valueOf(message), e);
     }
 
     @Override
     protected void logError(Object message) {
         Log.e(name, valueOf(message));
     }
 
     @Override
     protected void logWarn(Object message, Throwable e) {
         Log.w(name, valueOf(message), e);
     }
 
     @Override
     protected void logWarn(Object message) {
         Log.w(name, valueOf(message));
     }
 
     @Override
     protected void logInfo(Object message, Throwable e) {
         Log.i(name, valueOf(message), e);
     }
 
     @Override
     protected void logInfo(Object message) {
         Log.i(name, valueOf(message));
     }
 
     @Override
     protected void logDebug(Object message, Throwable e) {
         Log.d(name, valueOf(message), e);
     }
 
     @Override
     protected void logDebug(Object message) {
         Log.d(name, valueOf(message));
     }
 
     @Override
     protected void logTrace(Object message, Throwable e) {
         Log.v(name, valueOf(message), e);
     }
 
     @Override
     protected void logTrace(Object message) {
         Log.v(name, valueOf(message));
     }
 
     @Override
     public boolean isErrorOn() {
         return Log.isLoggable(name, Log.ERROR);
     }
 
     @Override
     public boolean isWarnOn() {
         return Log.isLoggable(name, Log.WARN);
     }
 
     @Override
     public boolean isInfoOn() {
         return Log.isLoggable(name, Log.INFO);
     }
 
     @Override
     public boolean isDebugOn() {
         return Log.isLoggable(name, Log.DEBUG);
     }
 
     @Override
     public boolean isTraceOn() {
         return Log.isLoggable(name, Log.VERBOSE);
     }
 }
