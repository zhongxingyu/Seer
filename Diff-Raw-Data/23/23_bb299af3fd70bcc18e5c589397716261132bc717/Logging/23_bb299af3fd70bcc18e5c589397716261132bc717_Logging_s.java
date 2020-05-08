 package jp.gr.java_conf.neko_daisuki.fsyscall;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Logging {
 
     private abstract static class Destination {
 
         public abstract void verbose(String message);
         public abstract void debug(String message);
         public abstract void info(String message);
         public abstract void warn(String message);
         public abstract void err(String message);
     }
 
     private static class FileDestination extends Destination {
 
         private PrintWriter mWriter;
         private DateFormat mDateFormatter;
 
         public FileDestination(String path) throws IOException {
             Writer out = new BufferedWriter(new FileWriter(path, true));
             mWriter = new PrintWriter(out, true);
             mDateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         }
 
         public void verbose(String message) {
             mWriter.println(formatMessage("VERB", message));
         }
 
         public void debug(String message) {
             mWriter.println(formatMessage("DEBUG", message));
         }
 
         public void info(String message) {
             mWriter.println(formatMessage("INFO", message));
         }
 
         public void warn(String message) {
             mWriter.println(formatMessage("WARN", message));
         }
 
         public void err(String message) {
             mWriter.println(formatMessage("ERR", message));
         }
 
         private String formatMessage(String level, String message) {
             String timestamp = mDateFormatter.format(new Date());
             return String.format("[%s] %s: %s", timestamp, level, message);
         }
     }
 
     public static class Logger {
 
         private String mTag;
 
         public Logger(String tag) {
             mTag = tag;
         }
 
         public void verbose(String message) {
             mDestination.verbose(formatMessage(message));
         }
 
         public void debug(String message) {
             mDestination.debug(formatMessage(message));
         }
 
         public void info(String message) {
             mDestination.info(formatMessage(message));
         }
 
         public void warn(String message) {
             mDestination.warn(formatMessage(message));
         }
 
         public void err(String message) {
             mDestination.err(formatMessage(message));
         }
 
         private String formatMessage(String message) {
             return String.format("%s: %s", mTag, message);
         }
     }
 
     private static Destination mDestination;
 
     public static void initialize() throws IOException {
         mDestination = new FileDestination("fsyscall.log");
     }
 
     private Logging() {
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
