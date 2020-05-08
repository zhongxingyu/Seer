 /**
  * 
  */
 package com.andune.minecraft.commonlib;
 
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import org.slf4j.helpers.FormattingTuple;
 import org.slf4j.helpers.MessageFormatter;
 
 /**
  * Logger implementation that binds to java.util.logging.
  * 
  * @author andune
  *
  */
 public class LoggerJUL implements Logger {
     static String SELF = LoggerJUL.class.getName();
     
     private final java.util.logging.Logger log;
     
     protected LoggerJUL(java.util.logging.Logger log) {
         this.log = log;
     }
     protected LoggerJUL(String name) {
         this.log = java.util.logging.Logger.getLogger(name);
     }
     
     @Override
     public boolean isDebugEnabled() {
         return log.isLoggable(Level.FINE);
     }
 
     /**
      * Log the message at the specified level with the specified throwable if any.
      * This method creates a LogRecord and fills in caller date before calling
      * this instance's JDK14 logger.
      * 
      * Borrowed from SLF4J.
      * 
      * @param level
      * @param msg
      * @param t
      */
     private void log(String callerFQCN, Level level, String msg, Throwable t) {
       // millis and thread are filled by the constructor
       LogRecord record = new LogRecord(level, msg);
       record.setLoggerName(log.getName());
       record.setThrown(t);
       fillCallerData(callerFQCN, record);
       log.log(record);
     }
 
     /**
      * Fill in caller data if possible. Borrowed from SLF4J.
      * 
      * @param record
      *          The record to update
      */
     final private void fillCallerData(String callerFQCN, LogRecord record) {
       StackTraceElement[] steArray = new Throwable().getStackTrace();
 
       int selfIndex = -1;
       for (int i = 0; i < steArray.length; i++) {
         final String className = steArray[i].getClassName();
         if (className.equals(callerFQCN)) {
           selfIndex = i;
           break;
         }
       }
 
       int found = -1;
       for (int i = selfIndex + 1; i < steArray.length; i++) {
         final String className = steArray[i].getClassName();
         if (!(className.equals(callerFQCN))) {
           found = i;
           break;
         }
       }
 
       if (found != -1) {
         StackTraceElement ste = steArray[found];
         // setting the class name has the side effect of setting
         // the needToInferCaller variable to false.
         record.setSourceClassName(ste.getClassName());
         record.setSourceMethodName(ste.getMethodName());
       }
     }
 
     @Override
     public void info(String msg) {
         if (log.isLoggable(Level.INFO)) {
             log(SELF, Level.INFO, msg, null);
         }
     }
 
     @Override
     public void info(String format, Object obj1) {
         if (log.isLoggable(Level.INFO)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1);
             log(SELF, Level.INFO, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void info(String format, Object obj1, Object obj2) {
         if (log.isLoggable(Level.INFO)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1, obj2);
             log(SELF, Level.INFO, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void info(String format, Object... args) {
         if (log.isLoggable(Level.INFO)) {
            FormattingTuple ft = MessageFormatter.format(format, args);
             log(SELF, Level.INFO, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void warn(String msg) {
         if (log.isLoggable(Level.WARNING)) {
             log(SELF, Level.WARNING, msg, null);
         }
     }
 
     @Override
     public void warn(String format, Object obj1) {
         if (log.isLoggable(Level.WARNING)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1);
             log(SELF, Level.WARNING, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void warn(String format, Object obj1, Object obj2) {
         if (log.isLoggable(Level.WARNING)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1, obj2);
             log(SELF, Level.WARNING, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void warn(String format, Object... args) {
         if (log.isLoggable(Level.WARNING)) {
            FormattingTuple ft = MessageFormatter.format(format, args);
             log(SELF, Level.WARNING, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void warn(String msg, Throwable t) {
         if (log.isLoggable(Level.WARNING)) {
             log(SELF, Level.WARNING, msg, t);
         }
     }
 
     @Override
     public void error(String msg) {
         if (log.isLoggable(Level.SEVERE)) {
             log(SELF, Level.SEVERE, msg, null);
         }
     }
 
     @Override
     public void error(String format, Object obj1) {
         if (log.isLoggable(Level.SEVERE)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1);
             log(SELF, Level.SEVERE, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void error(String format, Object obj1, Object obj2) {
         if (log.isLoggable(Level.SEVERE)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1, obj2);
             log(SELF, Level.SEVERE, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void error(String format, Object... args) {
         if (log.isLoggable(Level.SEVERE)) {
            FormattingTuple ft = MessageFormatter.format(format, args);
             log(SELF, Level.SEVERE, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void error(String msg, Throwable t) {
         if (log.isLoggable(Level.SEVERE)) {
             log(SELF, Level.SEVERE, msg, t);
         }
     }
 
     @Override
     public void debug(String msg) {
         if (log.isLoggable(Level.FINE)) {
             log(SELF, Level.FINE, msg, null);
         }
     }
 
     @Override
     public void debug(String format, Object obj1) {
         if (log.isLoggable(Level.FINE)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1);
             log(SELF, Level.FINE, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void debug(String format, Object obj1, Object obj2) {
         if (log.isLoggable(Level.FINE)) {
             FormattingTuple ft = MessageFormatter.format(format, obj1, obj2);
             log(SELF, Level.FINE, ft.getMessage(), ft.getThrowable());
         }
     }
 
     @Override
     public void debug(String format, Object... args) {
         if (log.isLoggable(Level.FINE)) {
            FormattingTuple ft = MessageFormatter.format(format, args);
             log(SELF, Level.FINE, ft.getMessage(), ft.getThrowable());
         }
     }

 }
