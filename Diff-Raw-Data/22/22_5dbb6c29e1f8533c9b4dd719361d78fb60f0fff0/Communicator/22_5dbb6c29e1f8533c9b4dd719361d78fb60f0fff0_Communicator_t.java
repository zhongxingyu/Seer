 package edgruberman.bukkit.simpletimestamp;
 
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Centralized and standardized logging and player communication class.
  */
 public class Communicator {
     
     private Main main;
     
     public Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
     
     public Communicator(Main main) {
         this.main = main;
     }
     
     /**
      * Configures logging to display more or less than the default of INFO.</br>
     * <b>Known Bug:</b> Logging output to file in Minecraft does not include level prefix despite it displaying in the console.
     * @param level Minimum logging level to show.
      */
     public void setLogLevel(Level level) {
        // Only set the parent handler lower if necessary, otherwise leave it alone for other configurations that have set it.
         for (Handler h : this.logger.getParent().getHandlers()) {
            if (h.getLevel().intValue() > level.intValue()) h.setLevel(level);
         }
         this.logger.setLevel(level);
     }
     
     /**
      * Determines if current logging level will display log entries of the specified level or higher.
      * 
      * @param level = Logging level to determine if it will be displayed in the log or not.
      * @return = true if current logging level will display this level; false otherwise.
      */
     public Boolean isLogLevel(Level level) {
         if (this.logger.getLevel().intValue() > level.intValue()) { return false; }
         return true;
     }
     
     /**
      * Generate a normal information log entry.
      * 
      * @param message = Text to display in log entry. Time and level will be prefixed automatically by Minecraft.
      */
     public void log(String message) {
         this.log(Level.INFO, message, null);
     }
     
     /**
      * Generate a log entry of the specified level. Useful for warnings, errors, and debug entries.
      * 
      * @param level = Logging level of log entry. Standard Java logging levels used.
      * @param message = Text to display in log entry. Time and level will be prefixed automatically by Minecraft.
      */
     public void log(Level level, String message) {
         this.log(level, message, null);
     }
     
     /**
      * Generate a log entry that has an associated error to display at the same time.
      * 
      * @param level = Logging level of log entry. Standard Java logging levels used.
      * @param message = Text to display in log entry. Time and level will be prefixed automatically by Minecraft.
      * @param e = Related error message to output along with log entry.
      */
     public void log(Level level, String message, Throwable e) {
         this.logger.log(level, "[" + this.main.getDescription().getName() + "] " + message, e);
     }
     
 }
