 package fi.lolcatz.profiler;
 
 import java.io.IOException;
 import java.util.logging.*;
 
 public class RootLogger {
     private static Logger rootLogger;
     private static Handler loggerHandler;
 
     public static void initLogger() {
         rootLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
         loggerHandler = new ConsoleHandler();
         loggerHandler.setLevel(Level.ALL);
         rootLogger.addHandler(loggerHandler);
         rootLogger.setLevel(Level.ALL);
     }
 
     public static void setLoggingLevel(Level level) {
         rootLogger.setLevel(level);
     }
 
     public static void logToFile(String filename) {
         try {
             Handler fileHandler = new FileHandler(filename);
             switchLoggerHandlerTo(fileHandler);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void logToConsole() {
         switchLoggerHandlerTo(new ConsoleHandler());
     }
 
     private static void switchLoggerHandlerTo(Handler newHandler) {
         rootLogger.removeHandler(loggerHandler);
         loggerHandler = newHandler;
         rootLogger.addHandler(newHandler);
     }
 }
