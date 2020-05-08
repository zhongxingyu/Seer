 package name.richardson.james.bukkit.utilities.logging;
 
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 
 import name.richardson.james.bukkit.utilities.localisation.Localisation;
 
 public final class ConsoleLogger extends AbstractLogger {
 
   private final java.util.logging.Logger logger;
   
   private Localisation localisation;
 
   public ConsoleLogger(Object owner, Localisation localisation) {
     this.logger = java.util.logging.Logger.getLogger(owner.getClass().getName());
     this.logger.setLevel(Logger.DEFAULT_LEVEL);
     this.localisation = localisation;
   }
   
   public ConsoleLogger(java.util.logging.Logger logger, Localisation localisation) {
     this.logger = logger;
     this.logger.setLevel(Logger.DEFAULT_LEVEL);
     this.localisation = localisation;
   }
 
   public void config(Object object, String message, Object... elements) {
     if (!this.logger.isLoggable(Level.CONFIG)) return;
     final String formattedMessage = this.localisation.getMessage(object, message, elements);
     this.logger.config(ChatColor.stripColor(formattedMessage));
   }
 
   public void debug(Object object, String message, Object... elements) {
     if (!this.logger.isLoggable(Level.ALL)) return;
     String formattedMessage = this.getDebugPrefix(object) + this.localisation.getMessage(object, message, elements);
     this.logger.fine((ChatColor.stripColor(formattedMessage)));
   }
   
   private String getDebugPrefix(Object object) {
     if (object instanceof Class) {
       Class<?> c = (Class<?>) object;
       return "<" + c.getSimpleName() + "> ";
     } else {
       return "<" + object.getClass().getSimpleName() + "> ";
     }
   }
 
   public void info(Object object, String message, Object... elements) {
     if (!this.logger.isLoggable(Level.INFO)) return;
     String formattedMessage = this.localisation.getMessage(object, message, elements);
    this.logger.info(ChatColor.stripColor(formattedMessage));
   }
 
   public boolean isDebugging() {
     return this.logger.isLoggable(Logger.DEBUG_LEVEL);
   }
 
   public void setDebugging(boolean debugging) {
     if (debugging) {
       this.logger.setLevel(Logger.DEBUG_LEVEL);
     } else {
       this.logger.setLevel(Logger.DEFAULT_LEVEL);
     }
   }
   
   public void severe(Object object, String message, Object... elements) {
     if (!this.logger.isLoggable(Level.SEVERE)) return;
     String formattedMessage = this.localisation.getMessage(object, message, elements);
    this.logger.severe(ChatColor.stripColor(formattedMessage));
   }
 
   public void warning(Object object, String message, Object... elements) {
     if (!this.logger.isLoggable(Level.WARNING)) return;
     String formattedMessage = this.localisation.getMessage(object, message, elements);
    this.logger.warning(ChatColor.stripColor(formattedMessage));
   }
 
   public String getName() {
     return this.logger.getName();
   }
   
 }
