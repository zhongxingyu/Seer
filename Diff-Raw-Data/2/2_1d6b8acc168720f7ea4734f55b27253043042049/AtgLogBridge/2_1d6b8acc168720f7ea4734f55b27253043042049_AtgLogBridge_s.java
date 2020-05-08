 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.atg;
 
 import org.znerd.logdoc.AbstractLogBridge;
 import org.znerd.logdoc.internal.ContextIdSupport;
 import org.znerd.util.log.LogLevel;
 
 import atg.nucleus.logging.ApplicationLogging;
 import atg.nucleus.logging.ApplicationLoggingImpl;
 
 public final class AtgLogBridge extends AbstractLogBridge {
 
     private static final AtgLogBridge SINGLETON_INSTANCE = new AtgLogBridge();
     private final ContextIdSupport contextIdSupport = new ContextIdSupport();
 
     private AtgLogBridge() {
     }
 
     public static AtgLogBridge getInstance() {
         return SINGLETON_INSTANCE;
     }
 
     @Override
     public void putContextId(String newContextId) {
         contextIdSupport.putContextId(newContextId);
     }
 
     @Override
     public void unputContextId() {
         contextIdSupport.unputContextId();
     }
 
     @Override
     public String getContextId() {
         return contextIdSupport.getContextId();
     }
 
     @Override
     public boolean shouldLog(String domain, String groupId, String entryId, LogLevel level) {
         return shouldLog(getApplicationLogging(domain, groupId), level);
     }
 
     private boolean shouldLog(ApplicationLogging logger, LogLevel level) {
         if (! getLevel().isSmallerThanOrEqualTo(level)) {
             return false;
         } else if (LogLevel.DEBUG.equals(level)) {
             return logger.isLoggingDebug();
         } else if (LogLevel.INFO.equals(level) || LogLevel.NOTICE.equals(level)) {
             return logger.isLoggingInfo();
         } else if (LogLevel.WARNING.equals(level)) {
             return logger.isLoggingWarning();
         } else {
             return logger.isLoggingError();
         }
     }
 
     @Override
     public void logOneMessage(String fqcn, String domain, String groupId, String entryId, LogLevel level, String message, Throwable exception) {
         ApplicationLogging logger = getApplicationLogging(domain, groupId);
         log(logger, entryId, level, message, exception);
     }
 
     private ApplicationLogging getApplicationLogging(String domain, String groupId) {
         String componentId = domain + '.' + groupId;
        return new ApplicationLoggingImpl(componentId);
     }
 
     private void log(ApplicationLogging logger, String entryId, LogLevel level, String message, Throwable exception) {
         if (LogLevel.DEBUG.equals(level)) {
             logDebug(logger, message, exception);
         } else if (LogLevel.INFO.equals(level)) {
             logInfo(logger, message, exception);
         } else if (LogLevel.NOTICE.equals(level)) {
             logInfo(logger, "(NOTICE) " + message, exception);
         } else if (LogLevel.WARNING.equals(level)) {
             logWarning(logger, message, exception);
         } else if (LogLevel.ERROR.equals(level)) {
             logError(logger, message, exception);
         } else {
             logError(logger, "(FATAL) " + message, exception);
         }
     }
 
     private void logDebug(ApplicationLogging logger, String message, Throwable exception) {
         if (exception == null) {
             logger.logDebug(message);
         } else {
             logger.logDebug(message, exception);
         }
     }
 
     private void logInfo(ApplicationLogging logger, String message, Throwable exception) {
         if (exception == null) {
             logger.logInfo(message);
         } else {
             logger.logInfo(message, exception);
         }
     }
 
     private void logWarning(ApplicationLogging logger, String message, Throwable exception) {
         if (exception == null) {
             logger.logWarning(message);
         } else {
             logger.logWarning(message, exception);
         }
     }
 
     private void logError(ApplicationLogging logger, String message, Throwable exception) {
         if (exception == null) {
             logger.logError(message);
         } else {
             logger.logError(message, exception);
         }
     }
 }
