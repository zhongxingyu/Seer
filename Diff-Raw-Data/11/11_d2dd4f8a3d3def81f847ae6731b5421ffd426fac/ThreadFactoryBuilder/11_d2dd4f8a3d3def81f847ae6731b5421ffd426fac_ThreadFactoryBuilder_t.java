 package polly.util.concurrent;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.concurrent.ThreadFactory;
 import java.util.regex.Pattern;
 
 
 public class ThreadFactoryBuilder implements ThreadFactory {
 
     private final static Pattern PATTERN = Pattern.compile("%n%");
     private String name;
     private int threadNum;
     private int priority;
     private UncaughtExceptionHandler exceptionHandler;
     
     
     
     public ThreadFactoryBuilder() {
        this("");
     }
     
     
     public ThreadFactoryBuilder(String name) {
         this.name = name;
        this.priority = Thread.NORM_PRIORITY;
     }
     
     
     public ThreadFactoryBuilder setName(String name) {
         this.name = name;
         return this;
     }
     
     
     
     public ThreadFactoryBuilder setPriority(int priority) {
         this.priority = priority;
         return this;
     }
     
     
     
     public ThreadFactoryBuilder setUncaughtExceptionHandler(UncaughtExceptionHandler h) {
         this.exceptionHandler = h;
         return this;
     }
     
     
     
     @Override
     public Thread newThread(Runnable r) {
         String name = ThreadFactoryBuilder.PATTERN.matcher(this.name).replaceFirst(
                 "" + this.threadNum++);
         Thread result = new Thread(r, name);
         result.setPriority(this.priority);
         if (this.exceptionHandler != null) {
             result.setUncaughtExceptionHandler(this.exceptionHandler);
         }
         return result;
     }
 
 }
