 package util.logger;
 
 import java.io.OutputStream;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 
 /**
 * A class that handles a logger. 
  * This class does not propagate LogRecords to parent Loggers, so if the
  * It sets a console handler as a default handler
  * 
  * By default, the logger starts at ALL level so any type of message is logged
  * 
  * Note that the user does not need to use any other class in this package
  * except this one to manage the logger
  * 
  * @author Henrique Moraes
  */
 public class LoggerManager {
     /**
      * This logger serves as a quick reference in case the user
      * wants to log a message in one line of code
      * The logger used was initialized with this class' name
      */
     public static final Logger DEFAULT_LOGGER =
             Logger.getLogger(LoggerManager.class.getName());
 
     private static final String LOG_EXT = ".log";
     public static final String DEFAULT_FILE_NAME = "Logger";
 
     private Logger myLogger;
     private IVoogaHandler myDefaultHandler = new HandlerConsole();
     private Level myDefaultLevel = Level.ALL;
 
     /**
      * Constructor
      * Sets a logger using reflection to find the name of the calling class
      * The Logger is initialized with the name of the calling class
      * By default, a console handler is set
      */
     public LoggerManager () {
         initializeLogger(LoggerReflection.getCallerClassName());
     }
 
     /**
      * Constructor.
      * Sets a logger according to the given class name
      */
     public LoggerManager (String loggerName) {
         initializeLogger(loggerName);
     }
 
     /**
      * Initializes a logger with default parameters of this manager
      * 
      * @param loggerName Name of the logger to initialize
      */
     private void initializeLogger (String loggerName) {
         myLogger = Logger.getLogger(loggerName);
         myLogger.setUseParentHandlers(false);
         myLogger.setLevel(myDefaultLevel);
         addHandler(myDefaultHandler);
     }
 
     /**
      * Adds a handler to the logger and sets it to the logger level
      * All add handler methods eventually pass through this method
      * Can be used to set customized handlers for the logger in case
      * the user wants to extend the design
      * 
      * @param handlerType the type of handler to be added
      */
     public void addHandler (IVoogaHandler hand) {
         Handler handler = hand.getHandler();
         handler.setLevel(myLogger.getLevel());
         myLogger.addHandler(handler);
     }
 
     /**
      * Adds a memory handler to the logger depending on given handler and
      * constraints. A memory handler pushes all log records after a message of
      * the specified threshold level is logged
      * This API was designed to be able to combine any other handler to the
      * memoryHandler
      * WARNING the type of handler added will have level INFO.
      * Once it is set, its level cannot be changed. If the user wishes to set
      * the handler from memory, he should set it manually and call the regular
      * addHandler()
      * 
      * @param handler the type of handler to have records pushed to
      * @param size Number of maximum records this handler will maintain
      * @param pushLevel push to handler as soon as a message of the given
      *        level is issued
      */
     public void addMemoryHandler (IVoogaHandler handler, int size, Level pushLevel) {
         addHandler(new HandlerMemory(handler, size, pushLevel));
     }
 
     /**
      * Adds a memory handler to the logger depending on given handler and
      * constraints. A memory handler pushes all log records after a message of
      * the specified threshold level is logged
      * This API was designed to be able to combine any other handler to the
      * memoryHandler
      * WARNING the type of handler added will have level INFO.
      * Once it is set, its level cannot be changed. If the user wishes to set
      * the handler from memory, he should set it manually and call the regular
      * addHandler()
      * 
      * @param handler the type of handler to have records pushed to
      */
     public void addMemoryHandler (IVoogaHandler handler) {
         HandlerMemory memory = new HandlerMemory();
         memory.setHandler(handler);
         addHandler(memory);
     }
 
     /**
      * 
      * Adds a handler that sends log records to the Console
      */
     public void addConsoleHandler () {
         addHandler(new HandlerConsole());
     }
 
     /**
      * 
      * Adds a handler that records messages in a txt file with a
      * default file name
      */
     public void addTxtHandler () {
         addHandler(new HandlerTxt());
     }
 
     /**
      * 
      * Adds a handler that records messages in a txt file
      * 
      * @param fileName Name of the file to have records written to
      */
     public void addTxtHandler (String fileName) {
         addHandler(new HandlerTxt(fileName));
     }
 
     /**
      * 
      * Adds a handler that records messages in a file with user-defined extension
      * 
      * @param fileName Name of the file to have records written to
      * @param ext The extension of the file
      */
     public void addCustomExtensionFileHandler (String fileName, String ext) {
         addHandler(new HandlerTxt(fileName, ext));
     }
 
     /**
      * 
      * Adds a handler that records messages in a .log file
      * 
      * @param fileName Name of the file to have records written to
      */
     public void addLogHandler (String fileName) {
         addCustomExtensionFileHandler(fileName, LOG_EXT);
     }
 
     /**
      * 
      * Adds a handler that records messages in an XML file
      * 
      * @param fileName Name of the file to have records written to
      */
     public void addXMLHandler (String fileName) {
         addHandler(new HandlerXML(fileName));
     }
 
     /**
      * 
      * Adds a handler that records messages in an XML file
      */
     public void addXMLHandler () {
         addHandler(new HandlerXML());
     }
 
     /**
      * 
      * Adds a handler that sends log records across a given stream
      * 
      * @param out Outputstream that this handler should write to
      */
     public void addStreamHandler (OutputStream out) {
         addHandler(new HandlerStream(out));
     }
 
     /**
      * 
      * Adds a handler that sends log records across a given stream
      * If no stream is given, the manager chooses System.out by default
      */
     public void addStreamHandler () {
         addHandler(new HandlerStream());
     }
 
     /**
      * 
      * Adds a handler that sends log records through a given socket
      * 
      * @param host string with the name of the host of this connection
      * @param port number of the port to be used
      */
     public void addSocketHandler (String host, int port) {
         addHandler(new HandlerSocket(host, port));
     }
 
     /**
      * 
      * Adds a handler that sends log records via e-mail
      * 
      * @param from Address from which the e-mail is sent
      * @param to String array with recipients to send e-mail to
      * @param server Server address
      * @param subject Subject of e-mail
      * @param message Text in e-mail
      */
     public void addMailHandler (String from, String[] to,
                                 String server, String subject, String message) {
         addHandler(new HandlerMail(from, to, server, subject, message));
     }
 
     /**
      * Sets the level of the logger and all its handlers
      * 
      * @param level
      */
     public void setLevel (Level level) {
         myLogger.setLevel(level);
         for (Handler h : myLogger.getHandlers()) {
             h.setLevel(level);
         }
     }
 
     /**
      * Logs a message on the logger of this manager
      * 
      * @param level the level of the LogRecord
      * @param message The message to log
      */
     public void log (Level level, String message) {
         LogRecord l = new LogRecord(level, message);
         l.setSourceClassName(LoggerReflection.getCallerClassName());
         l.setSourceMethodName(LoggerReflection.getCallerMethodName());
         myLogger.log(l);
     }
 
     /**
      * Logs a FINER level message on the logger of this manager
      * 
      * @param message The message to log
      */
     public void finer (String message) {
         log(Level.FINER, message);
     }
 
     /**
      * Logs a INFO level message on the logger of this manager
      * 
      * @param message The message to log
      */
     public void info (String message) {
         log(Level.INFO, message);
     }
 
     /**
      * Logs a WARNING level message on the logger of this manager
      * 
      * @param message The message to log
      */
     public void warning (String message) {
         log(Level.WARNING, message);
     }
 
     /**
      * Logs a SEVERE level message on the logger of this manager
      * 
      * @param message The message to log
      */
     public void severe (String message) {
         log(Level.SEVERE, message);
     }
 
     /**
      * 
      * @return The logger associated with this API
      */
     public Logger getLogger () {
         return myLogger;
     }
 
     /**
      * Removes all handlers from the current logger of this manager
      */
     public void clearHandlers () {
         for (Handler h : myLogger.getHandlers()) {
             myLogger.removeHandler(h);
         }
     }
 
 }
