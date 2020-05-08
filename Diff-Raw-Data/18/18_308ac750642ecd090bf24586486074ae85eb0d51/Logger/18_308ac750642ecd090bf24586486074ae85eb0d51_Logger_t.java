 /*******************************************************************************
  * $Id$
  * $Author$
  * $Date$
  *
  * Copyright 2002 - YAJUL Developers, Joshua Davis, Kent Vogel.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  ******************************************************************************/
 
 package org.yajul.log;
 
 import org.apache.log4j.Level;
 
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 /**
  * Extends 'Writer' so that it can be used as a 'DataSource' logger.
  */
 public class Logger extends Writer
 {
 
     /** Logging level for 'debug' messages. */
     public static final int LEVEL_DEBUG = 4;
 
     /** Logging level for 'info' messages. */
     public static final int LEVEL_INFO = 3;
 
     /** Logging level for 'warning' messages. */
     public static final int LEVEL_WARNING = 2;
 
     /** Logging level for 'error' messages. */
     public static final int LEVEL_ERROR = 1;
 
     /** Logging level for 'fatal' messages. */
     public static final int LEVEL_FATAL = 0;
 
     /** The delegate log object. */
    private org.apache.log4j.Logger log;
 
     /** A print writer that is piped to this log. */
     private PrintWriter printWriter;
 
     /** A string buffer for the Writer implementation. **/
     private StringBuffer lineBuffer;
 
     /** A map of existing logger instances. **/
     private static Map loggerMap = new WeakHashMap();
 
     /** An array of Log4J LEVELS to make decoding
      * the LEVEL_xxx values easier. */
     private static final Level[] LEVELS =
             {
                 Level.FATAL,
                 Level.ERROR,
                 Level.WARN,
                 Level.INFO,
                 Level.DEBUG
             };
 
 
     /**
      * Creates a new logger with a delegate that logs to the Log4J category
      * 'loggerName'
      * @param loggerName      The name of the category.
      */
     private Logger(String loggerName)
     {
         log = (loggerName == null || loggerName.length() == 0) ?
                org.apache.log4j.Logger.getRootLogger() :
                org.apache.log4j.Logger.getLogger(loggerName);
         printWriter = new PrintWriter(this);
         lineBuffer = new StringBuffer();
     }
 
     // --- java.io.Writer implementation ---
 
     /**
      * java.io.Writer implementation - Write the characters to the log.
      * @param buf       The array of characters to write.
      * @param off       The offset in array where the writable characters start.
      * @param len       The number of characters to write.
      */
     public void write(char[] buf, int off, int len)
     {
         for (int i = off; i < len; i++)
         {
             if (buf[i] == '\n')
                 flush();
             else
                 lineBuffer.append(buf[i]);
         }
     }
 
     /**
      * java.io.Writer implementation - Flush any buffered output.
      */
     public void flush()
     {
         if (lineBuffer.length() <= 0)
             return;
         log.info(lineBuffer.toString());
         lineBuffer.setLength(0);
     }
 
     /**
      * java.io.Writer implementation - Close the writer.
      */
     public void close()
     {
     }
 
     // -- End of Writer implementation --
 
     // --- JDK1.4 compatible methods ---
 
     /**
      * Returns the 'root' logger, to which *all* messages are sent.
      * @return Logger       The root logger.
      */
     public static Logger getRootLogger()
     {
         return internalGetLogger(null); // Null indicates the root.
     }
 
     /**
      * Returns an instance of a logger, given the category name.
      * @param categoryName      The name of the category.
      * @return Logger           The logger for the specified category.
      */
     public static Logger getLogger(String categoryName)
     {
         if (categoryName == null || categoryName.length() == 0)
             throw new IllegalArgumentException(
                     "A logging category name was not specified.  " +
                     "Specify a name, or call Logger.getRootLogger()");
         return internalGetLogger(categoryName);
     }
 
     /**
      * Returns an instance of a logger, given the category class.  The name of
      *  the category
      * will be the name of the class
      * @param categoryClass     The class of the category.
      * @return Logger           The logger for the specified category.
      */
     public static Logger getLogger(Class categoryClass)
     {
         return
                 getLogger((categoryClass != null) ?
                     categoryClass.getName() : null);
     }
 
     /** Returns true if DEBUG level is enabled for this logger.
      * @return boolean      True if 'debug' is enabled.*/
     public boolean isDebugEnabled()
     {
         return log.isDebugEnabled();
     }
 
     /**
      * Set the logging level for the logger,  messages sent to this logger that
      * are *less than* this category will not be shown.
      * @param level - The level (LEVEL_xxx) for this log.
      */
     public void setLevel(int level)
     {
         if (level < 0 || level >= LEVELS.length)
             throw new IllegalArgumentException(
                     "The supplied logging level was not valid: " + level);
        log.setLevel(LEVELS[level]);
     }
 
     /**
      * Returns this logger as if it was a print writer.
      * @return PrintWriter  The logger as a PrintWriter.
      */
     public PrintWriter getPrintWriter()
     {
         return printWriter;
     }
 
     /** Log the message with a level of DEBUG.
      * @param message   The message.
      **/
     public void debug(Object message)
     {
         log.debug(message);
     }
 
     /** Log the message and the exception with a level of DEBUG.
      * @param message   The message.
      * @param t         The exception.
      **/
     public void debug(Object message, Throwable t)
     {
         log.debug( message, t);
     }
 
     /** Returns true if INFO level is enabled for this logger. */
     public boolean isInfoEnabled()
     {
         return log.isInfoEnabled();
     }
 
     /** Log the message with a level of INFO.
      * @param message   The message.
      */
     public void info(Object message)
     {
         log.info(message);
     }
 
     /** Log the message and the exception with a level of INFO.
      * @param message   The message.
      * @param t         The exception.
      */
     public void info(Object message, Throwable t)
     {
         log.info(message, t);
     }
 
     /** Log the message with a level of WARN.
      * @param message   The message.
      */
     public void warn(Object message)
     {
         log.warn(message);
     }
 
     /** Log the message and the exception with a level of WARN.
      * @param message   The message.
      * @param t         The exception.
      */
     public void warn(Object message, Throwable t)
     {
         log.warn(message, t);
     }
 
     /** Log the message with a level of ERROR.
      * @param message   The message.
      */
     public void error(Object message)
     {
         log.error(message);
     }
 
     /** Log the message and the exception with a level of ERROR.
      * @param message   The message.
      * @param t         The exception.
      */
     public void error(Object message, Throwable t)
     {
         log.error(message, t);
     }
 
     /** Log the message with a level of FATAL.
      * @param message   The message.
      */
     public void fatal(Object message)
     {
         log.fatal(message);
     }
 
     /** Log the message and the exception with a level of FATAL.
      * @param message   The message.
      * @param t         The exception.
      */
     public void fatal(Object message, Throwable t)
     {
         log.fatal(message, t);
     }
 
     /**
      * Log a standard 'unexpected exception' message.
      * @param throwable - An exception.
      */
     public void unexpected(Throwable throwable)
     {
         error("Unexpected exception: " + throwable.getMessage(), throwable);
     }
 
     // --- Implementaiton methods ---
 
     /**
      * Internal: Returns a logger for the category name, or the root if the category name is null.
      * Also configures Log4J, if it has not already been configured.
      * @param categoryName - The category requested.
      * @return Logger - The requested logger.
      */
     private static Logger internalGetLogger(String categoryName)
     {
         LogUtil.configure();
 
         // Before making a new logger, look for an existing one.
         synchronized (loggerMap)
         {
             Logger logger = (Logger) loggerMap.get(categoryName);
             if (logger == null)
             {
                 logger = new Logger(categoryName);
                 loggerMap.put(categoryName, logger);
             }
             return logger;
         }
     }
 
 }
