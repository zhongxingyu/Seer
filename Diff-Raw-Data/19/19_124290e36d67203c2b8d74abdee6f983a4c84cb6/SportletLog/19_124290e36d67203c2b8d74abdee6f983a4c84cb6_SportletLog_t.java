 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
import org.apache.log4j.Level;
 import org.gridlab.gridsphere.portlet.PortletLog;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  * The PortletLog provides the portlet with the ability to log information, warning, or error texts.
  * The log is maintained by the portlet container. The type and location of of the log is a matter of implementation.
  * Also, whether logging is enabled or not is at the discretion of the portlet container.
  * ...
  * PortletLog log = iContext.getLog ();
  *
  * if (log.isWarnEnabled())
  *    log.warn ("Can access the content source");
  *
  * ...
  * This PortletLogImpl is a proxy for the Log 4J Logger object
  */
 public class SportletLog implements PortletLog {
 
     private static Map logMap = new Hashtable();
    private static String thisClassName = SportletLog.class.getName() + ".";
     private Logger logger;
 
     /**
      * Constructor not accessible. Use getInstance instead.
      */
     private SportletLog(Class clazz) {
         logger = Logger.getLogger(clazz);
     }
 
     /**
      * Return an instance of the PortletLog for a particular class
      *
      * @return the PortletLogImpl
      */
     public static synchronized PortletLog getInstance(Class clazz) {
         SportletLog log = (SportletLog)logMap.get(clazz);
         if (log != null) return log;
         log = new SportletLog(clazz);
         logMap.put(clazz, new SportletLog(clazz));
         return log;
     }
 
     /**
      * Returns whether the debug level of the portlet log is enabled.
      *
      * @return true if debbuging is enabled, false otherwise
      */
     public boolean isDebugEnabled() {
         return logger.isDebugEnabled();
     }
 
     /**
      * Logs the given informational text in the portlet log.
      *
      * @param text the informational text to log
      */
     public void debug(String text) {
        logger.log(thisClassName, Level.DEBUG, text, null);
     }
 
     /**
      * Returns whether the info level of the portlet log is enabled.
      *
      * @return true if the log is enabled, false otherwise
      */
     public boolean isInfoEnabled() {
         return logger.isInfoEnabled();
     }
 
     /**
      * Logs the given informational text in the portlet log.
      *
      * @param the informational text to log
      */
     public void info(String text) {
        logger.log(thisClassName, Level.INFO, text, null);
     }
 
     /**
      * Returns whether the warn level of the portlet log is enabled.
      *
      * @return true for the moment
      */
     public boolean isWarnEnabled() {
         return logger.isEnabledFor(Priority.WARN);
     }
 
     /**
      * Logs the given warning text in the portlet log.
      *
      * @param text the warning text to log
      */
     public void warn(String text) {
        logger.log(thisClassName, Level.WARN, text, null);
     }
 
     /**
      * Returns whether the error level of the portlet log is enabled.
      *
      * @return true if the log is enabled, false otherwise
      */
     public boolean isErrorEnabled() {
         return logger.isEnabledFor(Priority.ERROR);
     }
 
     /**
      * Logs the given error text in the portlet log.
      *
      * @param text the error text to log
      */
     public void error(String text) {
        logger.log(thisClassName, Level.ERROR, text, null);
     }
 
     /**
      * Logs the given error text, cause, and a stack trace in the portlet log.
      *
      * @param text the error text to log
      * @param cause the cause for logging
      */
     public void error(String text, Throwable cause) {
        logger.log(thisClassName, Level.INFO, text, cause);
     }
 
 }
