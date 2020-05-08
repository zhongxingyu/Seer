 /*
  * 
  */
 package org.smartly.commons.logging;
 
 import org.smartly.IConstants;
 import org.smartly.commons.event.Event;
 import org.smartly.commons.event.Events;
 import org.smartly.commons.event.IEventListener;
 import org.smartly.commons.util.FileUtils;
 import org.smartly.commons.util.PathUtils;
 import org.smartly.commons.util.StringUtils;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * This is an utility class for logging storage.<br/>
  * Properties:<br/>
  * <ul>
  * <li>FileEnabled: (Boolean - default=true) Enable/Disable writing on file (./logs/logging.log)</li><br/>
  * <li>MaxItems: (Integer - default=500) Max rows in list.</li><br/>
  * </ul>
  *
  * @author angelo.geminiani
  */
 public final class LoggingRepository {
 
     // ------------------------------------------------------------------------
     //                      Constants
     // ------------------------------------------------------------------------
     private static final String DEFAULT = "default";
     // ------------------------------------------------------------------------
     //                      Variables
     // ------------------------------------------------------------------------
     private Level _level = Level.INFO;
     private final Map<String, List<LogItem>> _data;
     private final Map<String, String> _customPaths;
     private final List<IEventListener> _listeners;
     private String _root;
     private boolean _fileEnabled;
     private boolean _consoleEnabled;
     private int _maxItems;
 
     // ------------------------------------------------------------------------
     //                      Constructor
     // ------------------------------------------------------------------------
     public LoggingRepository() {
         _data = Collections.synchronizedMap(new HashMap<String, List<LogItem>>());
         _listeners = Collections.synchronizedList(new LinkedList<IEventListener>());
         _customPaths = Collections.synchronizedMap(new HashMap<String, String>());
         _fileEnabled = true;
         _consoleEnabled = true;
         _maxItems = 500;
         this.setFilePath(IConstants.PATH_LOG + "/logging.log");
     }
 
     @Override
     public String toString() {
         synchronized (_data) {
             final StringBuilder result = new StringBuilder();
             final List<LogItem> list = this.getLogItems(DEFAULT);
             for (final LogItem item : list) {
                 if (result.length() > 0) {
                     result.append(IConstants.LINE_SEPARATOR);
                 }
                 result.append(item.toString());
             }
             return result.toString();
         }
     }
 
     public String toString(final Logger logger) {
         final String key = this.getKey(logger);
         return this.toString(key);
     }
 
     public String toString(final String key) {
         synchronized (_data) {
             final StringBuilder result = new StringBuilder();
             final List<LogItem> list = this.getLogItems(key);
             for (final LogItem item : list) {
                 if (result.length() > 0) {
                     result.append(IConstants.LINE_SEPARATOR);
                 }
                 result.append(item.toString());
             }
             return result.toString();
         }
     }
 
     // ------------------------------------------------------------------------
     //                      public
     // ------------------------------------------------------------------------
 
     /**
      * Set default file path
      *
      * @param path
      */
     public void setFilePath(final String path) {
         _root = PathUtils.getParent(path);
         this.setLogFileName(DEFAULT, path);
     }
 
     /**
      * Set a custom log file.
      *
      * @param aclass   Class of instance that logs in a separate file.
      * @param fileName File Name. i.e. "./memory.log"
      */
     public void setLogFileName(final Class aclass, final String fileName) {
         final String key = this.getKey(aclass);
         this.setLogFileName(key, fileName);
     }
 
     /**
      * Set a custom log file.
      *
      * @param key      Name of logger. Usually is name of calling class.
      * @param fileName File Name. i.e. "./memory.log"
      */
     public void setLogFileName(final String key, final String fileName) {
         synchronized (_customPaths) {
             final String name = PathUtils.getFilename(fileName, true);
             _customPaths.put(key, PathUtils.join(_root, name));
         }
         synchronized (_data) {
             this.createLogItems(key);
         }
     }
 
     public String getAbsoluteLogFileName(final String name) {
         final String path = this.getCleanLogFileName(name);
         return PathUtils.getAbsolutePath(path);
     }
 
     public void setLevel(final Level level) {
         _level = level;
     }
 
     public Level getLevel() {
         return _level;
     }
 
     public boolean isFileEnabled() {
         return _fileEnabled;
     }
 
     public void setFileEnabled(boolean fileEnabled) {
         this._fileEnabled = fileEnabled;
     }
 
     public boolean isConsoleEnabled() {
         return _consoleEnabled;
     }
 
     public void setConsoleEnabled(boolean consoleEnabled) {
         this._consoleEnabled = consoleEnabled;
     }
 
     public int getMaxItems() {
         return _maxItems;
     }
 
     public void setMaxItems(int maxItems) {
         this._maxItems = maxItems;
     }
 
     public void addListener(final IEventListener listener) {
         synchronized (_listeners) {
             if (null != listener && !_listeners.contains(listener)) {
                 _listeners.add(listener);
             }
         }
     }
 
     public void clear() {
         synchronized (_data) {
             _data.clear();
         }
     }
 
     public boolean isLoggable(final Level level) {
         return _level.getNumValue() <= level.getNumValue();
     }
 
     public void log(final Logger logger, final Throwable t) {
         this.log(logger, Level.SEVERE, t, null);
     }
 
     public void log(final Logger logger, final String message) {
         this.log(logger, Level.INFO, null, message);
     }
 
     public void log(final Logger logger, final Level level, final String message) {
         this.log(logger, level, null, message);
     }
 
     public void log(final Logger logger, final Level level, final Throwable t) {
         this.log(logger, level, t, null);
     }
 
     public void log(final Logger logger, final Level level,
                     final Throwable t, final String message) {
         synchronized (_data) {
             if (this.isLoggable(level)) {
                 final String key = this.getKey(logger);
                 final List<LogItem> logitems = this.getLogItems(logger.getName());
                 final LogItem item = new LogItem(logger.getName(), level, t, message);
                 logitems.add(item);
                 if (logitems.size() > _maxItems) {
                     logitems.remove(logitems.size() - 1);
                 }
                 this.invokeListeners(item);
                 this.writeFile(logger);
                 this.writeConsole(item);
             }
         }
     }
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
     private String getKey(final Class aclass) {
         if (null != aclass) {
             final Logger logger = new LogItemRepositoryLogger(aclass.getName(), "");
             return this.getKey(logger);
         }
         return DEFAULT;
     }
 
     private String getKey(final Logger logger) {
         if (null != logger) {
             return logger.getName();
         }
         return DEFAULT;
     }
 
     private String getRelativeLogFileName(final Class aclass) {
         final String key = this.getKey(aclass);
         return this.getCleanLogFileName(key);
     }
 
     private String getCleanLogFileName(final String name) {
         if (_customPaths.containsKey(name)) {
             final String path = _customPaths.get(name);
            return path; //path.startsWith(".")?path.substring(1):path;
         }
         return _customPaths.get(DEFAULT);
     }
 
     private List<LogItem> getLogItems(final String loggerName) {
         final String key = StringUtils.hasText(loggerName) ? loggerName : DEFAULT;
         if (_data.containsKey(key)) {
             return _data.get(key);
         } else {
             return _data.get(DEFAULT);
         }
     }
 
     private void createLogItems(final String key) {
         if (!_data.containsKey(key)) {
             final List<LogItem> result = new LinkedList<LogItem>();
             _data.put(key, result);
         }
     }
 
     private List<LogItem> getAllLogItems() {
         final List<LogItem> result = new LinkedList<LogItem>();
         final Collection<List<LogItem>> logs = _data.values();
         for (final List<LogItem> list : logs) {
             for (final LogItem item : list) {
                 result.add(item);
             }
         }
         return result;
     }
 
     private void invokeListeners(final LogItem item) {
         synchronized (_listeners) {
             final Event event = new Event(this, Events.ON_ERROR, item);
             for (final IEventListener listener : _listeners) {
                 listener.on(event);
             }
         }
     }
 
     private void writeFile(final Logger sender) {
         if (_fileEnabled) {
             final String fileName = this.getAbsoluteLogFileName(sender.getName());
             final String text = this.toString(sender);
             try {
                 FileUtils.mkdirs(fileName);
                 FileUtils.copy(text, new FileWriter(fileName));
             } catch (IOException ex) {
                 System.out.println(ex);
             }
         }
     }
 
     private void writeConsole(final LogItem item) {
         if (_consoleEnabled) {
             System.out.println(item);
         }
     }
 
     // ------------------------------------------------------------------------
     //                      S T A T I C
     // ------------------------------------------------------------------------
     private static LoggingRepository __instance;
 
     public static LoggingRepository getInstance() {
         if (null == __instance) {
             __instance = new LoggingRepository();
         }
         return __instance;
     }
 }
