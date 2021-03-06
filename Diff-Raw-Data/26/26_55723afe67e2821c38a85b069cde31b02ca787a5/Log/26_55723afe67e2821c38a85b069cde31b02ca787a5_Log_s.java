 package dungeon;
 
 /**
  * Log messages to STDOUT.
  *
  * By default no messages will be displayed, but you may lower the log level with #setLevel() so that stuff will
  * actually be printed.
  */
 public class Log {
   public enum Level {
     NOTICE(3),
     WARNING(4),
     ERROR(5),
     NONE(6);
 
     private final int severity;
 
     Level (int severity) {
       this.severity = severity;
     }
   }
 
   private static Level level = Level.NONE;
 
   public static void setLevel (Level level) {
     Log.level = level;
   }
 
   public static void notice (String message) {
     notice(message, null);
   }
 
   public static void notice (String message, Exception exception) {
    if (level.severity > Level.NOTICE.severity) {
       return;
     }
 
     System.out.println(message);
 
     if (exception != null) {
       exception.printStackTrace();
     }
   }
 }
