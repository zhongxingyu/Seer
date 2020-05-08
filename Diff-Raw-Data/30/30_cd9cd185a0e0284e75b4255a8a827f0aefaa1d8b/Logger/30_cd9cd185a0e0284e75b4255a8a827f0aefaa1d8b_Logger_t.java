 package org.inigma.lwrest.logger;
 
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 public class Logger {
     private static class MyLevel extends Level {
         public MyLevel(String name, int level) {
             super(name, level);
         }
     }
 
     private static final Formatter FORMATTER = new DetailedLogger();
 
     private static final Level TRACE = new MyLevel("TRACE", Level.FINER.intValue() + 1);
     private static final Level DEBUG = new MyLevel("DEBUG", Level.FINE.intValue() + 1);
     private static final Level INFO = new MyLevel("INFO", Level.INFO.intValue() + 1);
     private static final Level WARN = new MyLevel("WARN", Level.WARNING.intValue() + 1);
     private static final Level ERROR = new MyLevel("ERROR", Level.SEVERE.intValue() + 1);
     private static final Level FATAL = new MyLevel("FATAL", Level.ALL.intValue());
 
     public static Logger getLogger() {
         return new Logger(java.util.logging.Logger.getAnonymousLogger());
     }
 
     public static Logger getLogger(Class<?> clazz) {
         return getLogger(clazz.getName());
     }
 
     public static Logger getLogger(String clazz) {
         return new Logger(java.util.logging.Logger.getLogger(clazz));
     }
 
     private java.util.logging.Logger logger;
 
     private Logger(java.util.logging.Logger logr) {
         logger = logr;
     }
 
     public void debug(String pattern, Object... args) {
         log(DEBUG, pattern, args);
     }
 
     public void error(String pattern, Object... args) {
         log(ERROR, pattern, args);
     }
 
     public void fatal(String pattern, Object... args) {
         log(FATAL, pattern, args);
     }
 
     public void info(String pattern, Object... args) {
         log(INFO, pattern, args);
     }
 
     private void log(Level level, String pattern, Object... args) {
         if (logger.isLoggable(level)) {
             LogRecord log = new LogRecord(level, String.format(pattern, args));
             if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
                 log.setThrown((Throwable) args[args.length - 1]);
             }
            inferCaller(log);
             logger.log(log);
         }
     }
 
     public void trace(String pattern, Object... args) {
         log(TRACE, pattern, args);
     }
 
     public void warn(String pattern, Object... args) {
         log(WARN, pattern, args);
     }

    private void inferCaller(LogRecord log) {
        // Get the stack trace.
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        // First, search back to a method in the Logger class.
        int ix = 0;
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (cname.equals(getClass().getName())) {
                break;
            }
            ix++;
        }
        // Now search for the first frame before the "Logger" class.
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix];
            String cname = frame.getClassName();
            if (!cname.equals(getClass().getName())) {
                // We've found the relevant frame.
                log.setSourceClassName(cname);
                log.setSourceMethodName(frame.getMethodName());
                return;
            }
            ix++;
        }
        // We haven't found a suitable frame, so just punt.
    }
 }
