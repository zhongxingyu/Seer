 package com.smaxll.apps.android.flickrautobackup;
 
 import android.app.Application;
 import android.content.Context;
 
 import com.googlecode.androidannotations.api.BackgroundExecutor;
 
 import org.acra.ACRA;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
import java.util.Date;
import java.util.Locale;
 
 import ch.qos.logback.classic.Logger;
 import ch.qos.logback.classic.android.LogcatAppender;
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import ch.qos.logback.core.rolling.RollingFileAppender;
 import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
 
 /**
  * For global settings
  * Created by smaxllimit on 1/09/13.
  */
 public class FlickrAutoBackup extends Application {
     static final org.slf4j.Logger LOG = LoggerFactory.getLogger(FlickrAutoBackup.class);
 
     private static Context context;
 
     @Override
     public void onCreate() {
         super.onCreate();
         FlickrAutoBackup.context = getApplicationContext();
         BackgroundExecutor.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     initLogs();
                     ACRA.init(FlickrAutoBackup.this);
                     ACRA.getConfig().setApplicationLogFile(Utils.getLogFile().getAbsolutePath());
                     long versionCode = Utils.getLongProperty(STR.versionCode);
                     if (Config.VERSION != versionCode) {
                         if (versionCode == 0) {
//                            Mixpanel.track("First install");
                         }
 //                        Utils.saveAndroidDevice();
                         Utils.setLongProperty(STR.versionCode, (long) Config.VERSION);
                     }
                 } catch (Throwable e) {
                     LOG.error(e.getMessage(), e);
                 }
             }
         });
     }
 
     public static Context getAppContext() {
         return context;
     }
 
     private static synchronized void initLogs() {
         try {
             File logFile = Utils.getLogFile();
             ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            RollingFileAppender<ILoggingEvent> appender = (RollingFileAppender<ILoggingEvent>) root.getAppender("lfile");
             appender.setFile(logFile.getAbsolutePath());
             @SuppressWarnings("unchecked")
             TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = (TimeBasedRollingPolicy<ILoggingEvent>) appender.getRollingPolicy();
             rollingPolicy.setFileNamePattern(logFile.getParent() + "/log/flickrautobackup.%d{yyyy-MM-dd}.%i.log");
 
             if (!Config.isDebug()) {
                 LogcatAppender logcatAppender = (LogcatAppender) root.getAppender("logcat");
                 if (logcatAppender != null) {
                     logcatAppender.stop();
                 }
             }
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
 
     public static void deleteOldLogs() {
         try {
             File logFile = Utils.getLogFile();
             File logDir = new File(logFile.getParent(), "log");
             if (logDir.exists() && logDir.isDirectory()) {
                 for (File file : logDir.listFiles()) {
                     file.delete();
                 }
             }
         } catch (Throwable e) {
             e.printStackTrace();
         }
     }
 
 
 }
