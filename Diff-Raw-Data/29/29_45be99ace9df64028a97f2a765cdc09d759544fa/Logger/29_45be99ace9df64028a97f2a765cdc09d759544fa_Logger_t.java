 package util;
 
 import controllers.TMController;
 
 /**
  * Logger with categories.
  *
  * TODO append category name before each line of a message.
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Logger {
 
     public enum LogType {SECURITY, AUTHENTICATION, ADMIN, TECHNICAL}
 
     public static void debug(String message, Object... args) {
         play.Logger.debug(addUser(message), args);
     }
 
     public static void debug(Throwable e, String message, Object... args) {
         play.Logger.debug(e, addUser(message), args);
     }
 
     public static void info(LogType type, String message, Object... args) {
         play.Logger.info(addUser(message), args);
     }
 
    public static void info(LogType type, boolean logUser, String message, Object... args) {
        play.Logger.info(logUser ? addUser(message) : message, args);
    }

     public static void info(LogType type, Throwable e, String message, Object args) {
         play.Logger.info(e, addUser(message), args);
     }
 
     public static void warn(LogType type, String message, Object... args) {
         play.Logger.warn(addUser(message), args);
     }
 
    public static void warn(LogType type, boolean logUser, String message, Object... args) {
        play.Logger.warn(logUser?addUser(message):message, args);
    }

     public static void warn(LogType type, Throwable e, String message, Object... args) {
         play.Logger.warn(e, addUser(message), args);
     }
 
     public static void error(LogType type, String message, Object... args) {
         play.Logger.error(addUser(message), args);
     }
 
     public static void error(LogType type, Throwable e, String message, Object... args) {
         play.Logger.error(e, addUser(message), args);
     }
 
     public static void fatal(LogType type, String message, Object... args) {
         play.Logger.fatal(addUser(message), args);
     }
 
     public static void fatal(LogType type, Throwable e, String message, Object... args) {
         play.Logger.fatal(e, addUser(message), args);
     }
 
     private static String addUser(String message) {
         return new StringBuilder().append("[").append(TMController.getConnectedUser().user.getDebugString()).append("] ").append(message).toString();
     }
 
 
 }
