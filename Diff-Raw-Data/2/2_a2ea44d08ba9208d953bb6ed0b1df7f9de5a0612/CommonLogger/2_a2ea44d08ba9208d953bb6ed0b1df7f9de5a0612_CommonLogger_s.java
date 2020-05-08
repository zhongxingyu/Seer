 package logger;
 
 import java.io.PrintStream;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 /**
  * Defines a wrapper for {@link Logger} with a given format.
  * TODO(cmihail): explain the format
  *
  * @author cmihail
  */
 public class CommonLogger {
 
   private static Logger logger = null; // TODO(cmihail): maybe use a file
 
   /**
    * @param header the name of the logger used as a header at printing the message
    */
   private static void initiate(String header) {
    if (header == logger.getName()) {
       return;
     }
     logger = Logger.getLogger(header);
 
     // Remove any existing handlers.
     for (Handler handler : logger.getHandlers().clone()) {
       logger.removeHandler(handler);
     }
 
     // Add logger handler with a given formatting.
     logger.addHandler(new Handler() {
       @Override
       public void publish(LogRecord record) {
         Level level = record.getLevel();
         System.out.println("[" + record.getLoggerName() + "] (" + record.getSourceClassName() + ", "
             + level.getName() + "): " + record.getMessage());
         if (level == Level.SEVERE) {
           System.exit(1);
         }
       }
 
       @Override
       public void flush() {
         System.out.flush();
       }
 
       @Override
       public void close() throws SecurityException {
       }
     });
   }
 
   /**
    * Gets the logger associated with the header
    * @param header
    * @return the logger
    */
   public static Logger getLogger(String header) {
     initiate(header);
     return logger;
   }
 
   /**
    * TODO(cmihail): doesn't work for now
    * @return
    */
   public static PrintStream getPrintStream() {
     return System.err;
   }
 }
