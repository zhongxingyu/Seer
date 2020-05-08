 package org.ccci.deployment.maven;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
 import org.apache.log4j.spi.LoggingEvent;
 import org.apache.maven.plugin.logging.Log;
 
 public class MavenPluginAppender extends AppenderSkeleton implements Appender
 {
 
     private final Log log;
 
     public MavenPluginAppender(Log log)
     {
         this.log = log;
     }
 
     @Override
     protected void append(LoggingEvent event)
     {
        Level level = event.getLevel();
         String content = event.getLoggerName() + " " + event.getMessage().toString();
         Throwable throwable = event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable();
         
        if (level.toInt() < Level.INFO.toInt())
         {
             debug(content, throwable);
         }
        else if (level.toInt() < Level.ERROR.toInt())
         {
             info(content, throwable);
         }
         else
         {
             error(content, throwable);
         }
     }
 
     private void error(String content, Throwable throwable)
     {
         if (throwable != null)
         {
             log.error(content, throwable);
         }
         else
         {
             log.error(content);
         }
     }
 
     private void info(String content, Throwable throwable)
     {
         if (throwable != null)
         {
             log.info(content, throwable);
         }
         else
         {
             log.info(content);
         }
     }
 
     private void debug(String content, Throwable throwable)
     {
         if (throwable != null)
         {
             log.debug(content, throwable);
         }
         else
         {
             log.debug(content);
         }
     }
 
     @Override
     public void close()
     {
     }
 
     @Override
     public boolean requiresLayout()
     {
         return false;
     }
 
 }
