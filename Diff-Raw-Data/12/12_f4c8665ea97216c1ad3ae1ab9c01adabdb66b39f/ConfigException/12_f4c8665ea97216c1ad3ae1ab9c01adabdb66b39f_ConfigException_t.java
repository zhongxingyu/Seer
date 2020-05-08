 package fig;
 
 public class ConfigException extends RuntimeException {
   private static final long serialVersionUID = 1L;
 
   public ConfigException(Throwable e) {
    super(e);
   }
 
   public ConfigException(String msg) {
    super(msg);
   }
 
   public ConfigException(String msg, Throwable e) {
    super(msg, e);
   }
 }
