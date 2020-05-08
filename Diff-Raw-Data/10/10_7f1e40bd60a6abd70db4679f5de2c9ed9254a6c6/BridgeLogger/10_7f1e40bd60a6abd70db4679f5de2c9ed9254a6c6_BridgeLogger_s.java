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
 
 package org.jboss.logmanager.log4j;
 
 import org.jboss.logmanager.Logger;
 import org.jboss.logmanager.ExtLogRecord;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.Priority;
 import org.apache.log4j.Level;
 import org.apache.log4j.spi.LoggingEvent;
 
 import java.util.Enumeration;
 import java.util.ResourceBundle;
 import java.util.Collections;
 
 /**
  * A log4j logger which bridges to a LogManager logger.
  */
 public final class BridgeLogger extends org.apache.log4j.Logger {
 
     private static final String FQCN = BridgeLogger.class.getName();
 
     private final Logger logger;
 
     public BridgeLogger(final Logger logger) {
         super(logger.getName());
         this.logger = logger;
     }
 
     public void addAppender(final Appender newAppender) {
         // ignored
     }
 
     public void callAppenders(final LoggingEvent event) {
         // ignored for now (TODO?)
     }
 
     public void fatal(final Object message) {
         logger.log(FQCN, org.jboss.logmanager.Level.FATAL, String.valueOf(message), null);
     }
 
     public void fatal(final Object message, final Throwable t) {
         logger.log(FQCN, org.jboss.logmanager.Level.FATAL, String.valueOf(message), t);
     }
 
     public void error(final Object message) {
         logger.log(FQCN, org.jboss.logmanager.Level.ERROR, String.valueOf(message), null);
     }
 
     public void error(final Object message, final Throwable t) {
         logger.log(FQCN, org.jboss.logmanager.Level.ERROR, String.valueOf(message), t);
     }
 
     public void warn(final Object message) {
         logger.log(FQCN, org.jboss.logmanager.Level.WARN, String.valueOf(message), null);
     }
 
     public void warn(final Object message, final Throwable t) {
         logger.log(FQCN, org.jboss.logmanager.Level.WARN, String.valueOf(message), t);
     }
 
     public void info(final Object message) {
         logger.log(FQCN, org.jboss.logmanager.Level.INFO, String.valueOf(message), null);
     }
 
     public void info(final Object message, final Throwable t) {
         logger.log(FQCN, org.jboss.logmanager.Level.INFO, String.valueOf(message), t);
     }
 
     public boolean isInfoEnabled() {
         return logger.isLoggable(org.jboss.logmanager.Level.INFO);
     }
 
     public void debug(final Object message) {
         logger.log(FQCN, org.jboss.logmanager.Level.DEBUG, String.valueOf(message), null);
     }
 
     public void debug(final Object message, final Throwable t) {
         logger.log(FQCN, org.jboss.logmanager.Level.DEBUG, String.valueOf(message), t);
     }
 
     public boolean isDebugEnabled() {
         return logger.isLoggable(org.jboss.logmanager.Level.DEBUG);
     }
 
     public void trace(final Object message) {
         logger.log(FQCN, org.jboss.logmanager.Level.TRACE, String.valueOf(message), null);
     }
 
     public void trace(final Object message, final Throwable t) {
         logger.log(FQCN, org.jboss.logmanager.Level.TRACE, String.valueOf(message), t);
     }
 
     public boolean isTraceEnabled() {
         return logger.isLoggable(org.jboss.logmanager.Level.TRACE);
     }
 
     protected void forcedLog(final String fqcn, final Priority level, final Object message, final Throwable t) {
         // ignored
     }
 
     public boolean getAdditivity() {
         return logger.getUseParentHandlers();
     }
 
     public Enumeration getAllAppenders() {
         return Collections.enumeration(Collections.emptySet());
     }
 
     public Appender getAppender(final String name) {
         // ignored
         return null;
     }
 
     public Level getEffectiveLevel() {
         return LevelMapping.getPriorityFor(logger.getLevel());
     }
 
     public Priority getChainedPriority() {
         return getEffectiveLevel();
     }
 
     public ResourceBundle getResourceBundle() {
         return logger.getResourceBundle();
     }
 
     public boolean isAttached(final Appender appender) {
         return false;
     }
 
     public boolean isEnabledFor(final Priority level) {
         return logger.isLoggable(LevelMapping.getLevelFor(level));
     }
 
     public void l7dlog(final Priority priority, final String key, final Throwable t) {
        logger.log(FQCN, LevelMapping.getLevelFor(level), key, t);
     }
 
     public void l7dlog(final Priority priority, final String key, final Object[] params, final Throwable t) {
        logger.log(FQCN, LevelMapping.getLevelFor(level), key, ExtLogRecord.FormatStyle.MESSAGE_FORMAT, params, t);
     }
 
     public void log(final Priority priority, final Object message, final Throwable t) {
        logger.log(FQCN, LevelMapping.getLevelFor(level), String.valueOf(message), t);
     }
 
     public void log(final Priority priority, final Object message) {
     }
 
     public void log(final String callerFQCN, final Priority level, final Object message, final Throwable t) {
         logger.log(callerFQCN, LevelMapping.getLevelFor(level), String.valueOf(message), t);
     }
 
     public void removeAllAppenders() {
         // ignored
     }
 
     public void removeAppender(final Appender appender) {
         // ignored
     }
 
     public void removeAppender(final String name) {
         // ignored
     }
 
     public void setAdditivity(final boolean additive) {
         // ignored
     }
 
     @SuppressWarnings({ "deprecation" })
     public void setLevel(final Level level) {
         setPriority(level);
     }
 
     @Deprecated
     public void setPriority(final Priority priority) {
         logger.setLevel(LevelMapping.getLevelFor(priority));
     }
 
     public void setResourceBundle(final ResourceBundle bundle) {
         // ignored
     }
 }
