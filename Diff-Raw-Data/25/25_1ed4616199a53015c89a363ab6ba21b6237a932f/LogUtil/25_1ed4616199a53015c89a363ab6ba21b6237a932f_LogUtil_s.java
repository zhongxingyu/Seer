 package de.datagenerator;
 
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.SimpleLayout;
 
 public class LogUtil {
 
   private static Logger logger = Logger.getRootLogger();
 
    public static final Logger getLogger() {
      init();
       return logger;
    }
 
    private static void init() {
       try {
          SimpleLayout layout = new SimpleLayout();
          FileAppender fileAppender =
             new FileAppender(layout, "logs/LogFile.log", false);
          logger.addAppender(fileAppender);
          // ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
          logger.setLevel(Level.ERROR);
          System.out.println("Es sind Fehler aufgetreten. Sie wurden in LogFile.log ausgegeben.");
       } catch (Exception ex) {
          System.out.println(ex);
       }
    }
 
 }
