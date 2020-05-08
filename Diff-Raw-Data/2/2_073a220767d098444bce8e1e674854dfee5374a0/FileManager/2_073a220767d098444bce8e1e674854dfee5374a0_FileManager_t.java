 /**
  * @author ElecEntertainment
  * @team Larry1123, Joshtmathews, Sinzo, Xalbec
  * @lastedit Aug 13, 2013 8:28:10 AM
  */
 package net.larry1123.util.logger;
 
 import org.apache.commons.lang3.time.DateFormatUtils;
 import org.apache.commons.lang3.time.DateUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 
 public class FileManager {
 
    private static final LoggerSettings config = new LoggerSettings();
 
     private static final HashMap<String, FileHandler> fileHandlers = new HashMap<String, FileHandler>();
     private static final HashMap<FileHandler, UtilFilter> fileFilters = new HashMap<FileHandler, UtilFilter>();
     private static final HashMap<EELogger, String> loggerspaths = new HashMap<EELogger, String>();
     private static final HashMap<LoggerLevel, String> levelpaths = new HashMap<LoggerLevel, String>();
 
     /**
      * Returns the TimeDate that should be used for files at this time
      *
      * @return
      */
     public static String dateTime() {
         Date currentTime = null;
         String set = DateFormatUtils.SMTP_DATETIME_FORMAT.format(System.currentTimeMillis());
         try {
             currentTime = DateUtils.parseDate(set, DateFormatUtils.SMTP_DATETIME_FORMAT.getPattern());
         } catch (ParseException e) {
             e.printStackTrace();
         }
 
         if (!(config.getCurrentSplit().equals(null) || config.getCurrentSplit().equals(""))) {
             Date currentsplit = null;
             try {
                 currentsplit = DateUtils.parseDate(config.getCurrentSplit(), DateFormatUtils.SMTP_DATETIME_FORMAT.getPattern());
             } catch (ParseException e) {
                 config.setCurrentSplit(set);
                 return set.replace(":", "_");
             }
 
             Date test;
             switch (config.getSplit()) {
                 case HOUR:
                     test = DateUtils.addHours(currentTime, 1);
                     test = DateUtils.setMinutes(test, 0);
                     test = DateUtils.setSeconds(test, 0);
                     test = DateUtils.setMilliseconds(test, 0);
                     if (test.after(currentsplit)) {
                         set = config.getCurrentSplit();
                     }
                     break;
                 case DAY:
                     if (DateUtils.isSameDay(currentTime, currentsplit)) {
                         set = config.getCurrentSplit();
                     }
                     break;
                 case WEEK:
                     test = DateUtils.ceiling(currentTime, Calendar.WEEK_OF_MONTH);
                     if (test.after(currentsplit)) {
                         set = config.getCurrentSplit();
                     }
                     break;
                 case MONTH:
                     test = DateUtils.ceiling(currentTime, Calendar.MONTH);
                     if (test.after(currentsplit)) {
                         set = config.getCurrentSplit();
                     }
                     break;
                 default:
                     break;
             }
         }
         config.setCurrentSplit(set);
         return set.replace(":", "_");
     }
 
     /**
      * Sets up the file for a Logger to use
      *
      * @param logger      What Logger needs setup
      * @param logpathPath Where to Log to
      * @return
      */
     public static FileHandler setUpFile(EELogger logger, String logpathPath) {
         createDirectoryFromPath(logger.logpath);
         try {
             FileHandler handler = gethandler(logger, logpathPath);
             UtilFilter filter = fileFilters.get(handler);
             filter.setLogAll(true);
             return handler;
         } catch (SecurityException e) {
             EELogger.log.logCustom(EELogger.fileHandlerError, "SecurityException", e);
         } catch (IOException e) {
             EELogger.log.logCustom(EELogger.fileHandlerError, "IOException", e);
         }
         return null;
     }
 
     /**
      * Sets up a file for a LoggerLevel to use Under a Logger
      *
      * @param logger    What Logger owns the LoggerLevel
      * @param lvl       What Level needs setup
      * @param levelPath Where To Log to
      * @return
      */
     public static FileHandler setUpFile(EELogger logger, LoggerLevel lvl, String levelPath) {
         createDirectoryFromPath(logger.path);
 
         FileHandler handler = setUpFile(logger, levelPath);
         UtilFilter filter = fileFilters.get(handler);
         filter.setLogAll(false);
         filter.addLogLevel(lvl);
 
         return handler;
     }
 
     /**
      * TODO
      *
      * @param logger
      * @param lvl
      * @param pathName
      * @return
      * @throws SecurityException
      * @throws IOException
      */
     public static FileHandler gethandler(EELogger logger, LoggerLevel lvl, String pathName) throws SecurityException,
             IOException {
 
         FileHandler handler = null;
 
         if (!config.getSplit().equals(FileSplits.NONE)) {
             pathName = pathName + "_" + FileManager.dateTime();
         }
 
         pathName = pathName + ".log";
 
         if (!fileHandlers.containsKey(pathName)) {
 
             handler = new FileHandler(pathName, true);
             UtilsLogFormat lf = new UtilsLogFormat();
             UtilFilter uf = new UtilFilter();
             fileFilters.put(handler, uf);
             handler.setFilter(uf);
             handler.setLevel(lvl);
             handler.setFormatter(lf);
             handler.setEncoding("UTF-8");
 
             logger.addHandler(handler);
 
             fileHandlers.put(pathName, handler);
             levelpaths.put(lvl, pathName);
         }
         if (handler == null) {
             handler = fileHandlers.get(pathName);
         }
 
         fileFilters.get(handler).addLogLevel(lvl);
         return fileHandlers.get(pathName);
 
     }
 
     /**
      * TODO
      *
      * @param logger
      * @param pathName
      * @return
      * @throws SecurityException
      * @throws IOException
      */
     public static FileHandler gethandler(EELogger logger, String pathName) throws SecurityException,
             IOException {
 
         FileHandler handler = null;
 
         if (!config.getSplit().equals(FileSplits.NONE)) {
             pathName = pathName + "_" + FileManager.dateTime();
         }
 
         pathName = pathName + ".log";
 
         if (!fileHandlers.containsKey(pathName)) {
 
             handler = new FileHandler(pathName, true);
             UtilsLogFormat lf = new UtilsLogFormat();
             UtilFilter uf = new UtilFilter();
             fileFilters.put(handler, uf);
             handler.setFilter(uf);
             handler.setLevel(Level.ALL);
             handler.setFormatter(lf);
             handler.setEncoding("UTF-8");
 
             logger.addHandler(handler);
 
             fileHandlers.put(pathName, handler);
             loggerspaths.put(logger, pathName);
         }
         if (handler == null) {
             handler = fileHandlers.get(pathName);
         }
         return handler;
     }
 
     /**
      * Removes A Level from the file filter that it is linked to
      *
      * @param lvl
      */
     public static void removeLoggerLevel(LoggerLevel lvl) {
         fileFilters.get(fileHandlers.get(levelpaths.remove(lvl))).removeLogLevel(lvl);
     }
 
     public static void updateFileHandlers() {
         for (EELogger logger : loggerspaths.keySet()) {
             for (Handler handlerm : logger.getHandlers()) {
                 if (handlerm instanceof FileHandler) {
                     FileHandler handler = (FileHandler) handlerm;
                     UtilFilter filter = (UtilFilter) handler.getFilter();
                     if (!handler.getLevel().equals(Level.ALL)) {
                         LoggerLevel lvl = (LoggerLevel) handler.getLevel();
                         logger.removeHandler(handler);
                         fileHandlers.remove(levelpaths.get(lvl));
                         handler = setUpFile(logger, lvl, levelpaths.get(lvl));
                         handler.setFilter(filter);
                     } else {
                         fileHandlers.remove(loggerspaths.get(logger));
                         logger.removeHandler(handler);
                         handler = setUpFile(logger, logger.logpath);
                         handler.setFilter(filter);
                     }
                 } else {
                     // TODO
                     continue;
                 }
             }
         }
     }
 
     private static void createDirectoryFromPath(String path) {
         File logDir = new File(path.substring(0, path.lastIndexOf('/')));
         if (!logDir.exists()) {
             logDir.mkdirs();
         }
     }
 
 }
