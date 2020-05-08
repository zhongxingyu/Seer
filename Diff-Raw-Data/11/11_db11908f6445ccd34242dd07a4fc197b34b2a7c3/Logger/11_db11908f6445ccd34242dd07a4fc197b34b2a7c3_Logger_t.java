 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package at.irian.webstack.support.cdi.logging;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.ResourceBundle;
 import java.util.logging.*;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class Logger implements Serializable {
     transient java.util.logging.Logger logger;
 
     String loggerName = null;
     boolean first = true;
 
     public Logger(String loggerName) {
         //TODO check varioud meta loggers how they handle the original class problematic
 
         this.logger = java.util.logging.Logger.getLogger(loggerName);
         this.loggerName = loggerName;
     }
 
 
     public Logger(java.util.logging.Logger logger) {
         this.logger = logger;
     }
 
     public Logger getLogger(String s) {
         return new Logger(logger.getLogger(s));
     }
 
     public Logger getLogger(String s, String s1) {
         return new Logger(logger.getLogger(s, s1));
     }
 
     public Logger getAnonymousLogger() {
         return new Logger(logger.getAnonymousLogger());
     }
 
     public java.util.logging.Logger getAnonymousLogger(String s) {
         return logger.getAnonymousLogger(s);
     }
 
     public ResourceBundle getResourceBundle() {
         return logger.getResourceBundle();
     }
 
     public String getResourceBundleName() {
         return logger.getResourceBundleName();
     }
 
     public void setFilter(Filter filter) throws SecurityException {
         logger.setFilter(filter);
     }
 
     public Filter getFilter() {
         return logger.getFilter();
     }
 
     public void log(LogRecord logRecord) {
         logger.log(logRecord);
     }
 
     public void log(Level level, String s) {
        logger.logp( level, loggerName, "", s);
     }
 
     public void log(Level level, String s, Object o) {
        logger.logp( level, loggerName, "", s, o);
     }
 
     public void log(Level level, String s, Object[] objects) {
        logger.logp( level, loggerName, "", s, objects);
     }
 
     public void log(Level level, String s, Throwable throwable) {
        logger.logp( level, loggerName, "", s, throwable);
     }
 
     public void logp(Level level, String s, String s1, String s2) {
         logger.logp(level, s, s1, s2);
     }
 
     public void logp(Level level, String s, String s1, String s2, Object o) {
         logger.logp(level, s, s1, s2, o);
     }
 
     public void logp(Level level, String s, String s1, String s2, Object[] objects) {
         logger.logp(level, s, s1, s2, objects);
     }
 
     public void logp(Level level, String s, String s1, String s2, Throwable throwable) {
         logger.logp(level, s, s1, s2, throwable);
     }
 
     public void logrb(Level level, String s, String s1, String s2, String s3) {
         logger.logrb(level, s, s1, s2, s3);
     }
 
     public void logrb(Level level, String s, String s1, String s2, String s3, Object o) {
         logger.logrb(level, s, s1, s2, s3, o);
     }
 
     public void logrb(Level level, String s, String s1, String s2, String s3, Object[] objects) {
         logger.logrb(level, s, s1, s2, s3, objects);
     }
 
     public void logrb(Level level, String s, String s1, String s2, String s3, Throwable throwable) {
         logger.logrb(level, s, s1, s2, s3, throwable);
     }
 
     public void entering(String s, String s1) {
         logger.entering(s, s1);
     }
 
     public void entering(String s, String s1, Object o) {
         logger.entering(s, s1, o);
     }
 
     public void entering(String s, String s1, Object[] objects) {
         logger.entering(s, s1, objects);
     }
 
     public void exiting(String s, String s1) {
         logger.exiting(s, s1);
     }
 
     public void exiting(String s, String s1, Object o) {
         logger.exiting(s, s1, o);
     }
 
     public void throwing(String s, String s1, Throwable throwable) {
         logger.throwing(s, s1, throwable);
     }
 
     public void severe(String s) {
          logger.logp(Level.SEVERE,loggerName, "", s);
     }
 
     public void warning(String s) {
          logger.logp(Level.WARNING,loggerName, "", s);
     }
 
     public void info(String s) {
 
         logger.logp(Level.INFO,loggerName, "", s);
     }
 
     public void config(String s) {
         logger.config(s);
     }
 
     public void fine(String s) {
          logger.logp(Level.FINE,loggerName, "", s);
     }
 
     public void finer(String s) {
          logger.logp(Level.FINER,loggerName, "", s);
     }
 
     public void finest(String s) {
          logger.logp(Level.FINEST,loggerName, "", s);
     }
 
     public void setLevel(Level level) throws SecurityException {
         logger.setLevel(level);
     }
 
     public Level getLevel() {
         return logger.getLevel();
     }
 
     public boolean isLoggable(Level level) {
         return logger.isLoggable(level);
     }
 
     public String getName() {
         return logger.getName();
     }
 
     public void addHandler(Handler handler) throws SecurityException {
         logger.addHandler(handler);
     }
 
     public void removeHandler(Handler handler) throws SecurityException {
         logger.removeHandler(handler);
     }
 
     public Handler[] getHandlers() {
         return logger.getHandlers();
     }
 
     public void setUseParentHandlers(boolean b) {
         logger.setUseParentHandlers(b);
     }
 
     public boolean getUseParentHandlers() {
         return logger.getUseParentHandlers();
     }
 
     public java.util.logging.Logger getParent() {
         return logger.getParent();
     }
 
     public void setParent(java.util.logging.Logger logger) {
         this.logger.setParent(logger);
     }
 
     /**
      * since logger is a class not an interface in jul we have to delegate fully blown
      *
      * @param in
      * @throws IOException
      * @throws ClassNotFoundException
      */
     private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         /*we have to get injection up and running to eliminate this code here*/
         in.defaultReadObject();
         logger = java.util.logging.Logger.getLogger(loggerName);
     }
 
 }
