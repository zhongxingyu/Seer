 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 
 /**
  * Created by Silvestrov Ilya
  * Date: Sep 11, 2010
  * Time: 11:17:51 PM
  */
 public class Log {
     private static final boolean enabled;
 
     private static final boolean error;
 
     static {
         boolean log = Boolean.valueOf(System.getProperty("log", "false"));
         if (!log)
             log = Boolean.valueOf(System.getProperty("debug", "false"));
         enabled = log;
         if (!log) {
             log = Boolean.valueOf(System.getProperty("error", "false"));
         }
         error = log;
     }
     private static Writer writer;
     private static long startTime = System.currentTimeMillis();
 
     private static Writer writer() {
         if (writer == null) {
             try {
                 writer = new FileWriter("bot.log");
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
         return writer;
     }
 
     public static void log(String message) {
         log(MyBot.turn, message);
     }
     public static void log(int turn, String message) {
         if (enabled) {
             StringBuilder sb = new StringBuilder();
             sb.append(System.currentTimeMillis() - startTime).append(", ").append(turn).append(": ").
                     append(message).append("\n");
             try {
                 writer().write(sb.toString());
                 writer().flush();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     public static void error(String msg) {
        if (MyBot.SKIP_ON_ERROR)
            return;
        
         try {
             writer().write(msg);
             writer().write("\n");
             writer().flush();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static void error(int turn, Throwable t) {
        if (MyBot.SKIP_ON_ERROR)
            return;
         t.printStackTrace(new PrintWriter(writer()));
         try {
             writer.flush();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static boolean isEnabled() {
         return enabled;
     }
 }
