 package net.tgxn.bukkit.backup.utils;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Logging utilities, also handles try-catch exceptions.
  * 
  * @author Domenic Horner (gamerx)
  */
 public class LogUtils {
     
     private static Level logLevel = Level.INFO;
     private static Logger logger;
     private static Plugin plugin;
     private static boolean logToConsole;
     private static boolean logToFile;
     private static String logFileName;
 
     /**
      * Main Constructor for LogUtils.
      * Creates logger, sets default log level and variables.
      * 
      * @param plugin The plugin's object.
      */
     public static void initLogUtils(Plugin plugin) {
         if (LogUtils.logger == null) {
             if (plugin != null) {
                 LogUtils.logger = Logger.getLogger(plugin.getServer().getLogger().getName() + "." + plugin.getServer().getName());
             }
             
             LogUtils.logLevel = Level.INFO;
             LogUtils.logger.setLevel(Level.INFO);
             LogUtils.plugin = plugin;
         }
     }
     
     public static void finishInitLogUtils(boolean logToConsole, boolean logtofile, String logFileName) {
         LogUtils.logToConsole = logToConsole;
         LogUtils.logToFile = logtofile;
         LogUtils.logFileName = logFileName;
     }
 
     /**
      * Exception handling, Called instead of sendLog so it can be debugged.
      * 
      * @param ste The stack trace.
      * @param message Message accompanying it.
      */
     public static void exceptionLog(StackTraceElement ste[], String message) {
         sendLog(message, Level.SEVERE, true);
         exceptionLog(ste);
     }
     
     /**
      * Parse the STE and print the error.
      * 
      * @param ste The stack trace element.
      */
     public static void exceptionLog(StackTraceElement ste[]) {
         String tags = "[" + plugin.getDescription().getName()  + "] ";
        try {
            String toSystemOut = "";
            String[] error = null;
            for (int pos = 0; pos < ste.length; pos++) {

                toSystemOut =  ste[pos].getFileName() + ":" + ste[pos].getLineNumber();

                error[0] = ste[pos].getFileName() + ":" + ste[pos].getLineNumber() + " ==>" + ste[pos].getMethodName() + "()";
                error[1] = ste[pos].toString();
            }
            System.out.println(tags + "Error: "+toSystemOut);
        } catch(NullPointerException npe) {
            System.out.println(tags + "Error: Null Pointer in exception class!");
        }
     }
    
 
     /**
      * Sends log message.
      * 
      * @param message 
      */
     public static void sendLog(String message) {
         sendLog(message, Level.INFO, true);
     }
     /**
      * Sends log message.
      * 
      * @param message
      * @param tags 
      */
     public static void sendLog(String message, boolean tags) {
         sendLog(message, Level.INFO, tags);
     }
     
     /**
      * Sends log message.
      * 
      * @param message
      * @param tags 
      */
     public static void sendLog(Level level, String message) {
         sendLog(message, level, true);
     }
 
     /**
      * Sends log message.
      * 
      * @param logLevel Logger level for this item.
      * @param message The text of the log.
      * @param addTags Should we add tags to the string.
      */
     public static void sendLog(String message, Level logLevel, boolean addTags) {
         final String nameTag = ("[" + plugin.getDescription().getName()  + "] ");
         if(addTags)
             message = nameTag + message;
         if(logToConsole)
             logger.log(logLevel, message);
         if(logToFile) {}
             //@TODO Log to file code. using
     }
     
     /**
      * Sets the default LoglLevel.
      * 
      * @param logLevel The level to set the logger to.
      */
     public static void setLogLevel(Level logLevel) {
         LogUtils.logLevel = logLevel;
         LogUtils.logger.setLevel(logLevel);
     }
     
     /**
      * Gets the current LogLevel.
      * 
      * @return The current log level.
      */
     public static Level getLogLevel() {
         return logLevel;
     }
 }
