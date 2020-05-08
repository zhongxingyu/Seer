 package fig;
 
 public class ConfigException extends RuntimeException {
   private static final long serialVersionUID = 1L;
  private final Throwable wrappedException;
  private final String msg;
 
   public ConfigException(Throwable e) {
    this(null, e);
   }
 
   public ConfigException(String msg) {
    this(msg, null);
   }
 
   public ConfigException(String msg, Throwable e) {
    this.msg = msg;
    this.wrappedException = e;
   }
 }
