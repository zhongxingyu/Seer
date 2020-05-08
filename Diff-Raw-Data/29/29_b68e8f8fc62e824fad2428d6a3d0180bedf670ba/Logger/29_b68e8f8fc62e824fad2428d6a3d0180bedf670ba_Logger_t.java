 
 package info.freelibrary.util;
 
 import org.slf4j.LoggerFactory;
 import org.slf4j.Marker;
 
 /**
 * Create a SLF4J logger that is backed by a {@link java.util.ResourceBundle}.
  * 
  * @author <a href="mailto:ksclarke@gmail.com">Kevin S. Clarke</a>
  */
 public class Logger extends I18nObject implements org.slf4j.Logger {
 
     private static org.slf4j.Logger LOGGER;
 
     /**
      * Creates a logger using the supplied class as the name.
      * 
      * @param aClass A class to use as the name of the logger
      */
     public Logger(Class<?> aClass) {
         LOGGER = LoggerFactory.getLogger(aClass);
     }
 
     /**
      * Creates a logger using the supplied name.
      * 
      * @param aLoggerName The name of the logger to construct
      */
     public Logger(String aLoggerName) {
         LOGGER = LoggerFactory.getLogger(aLoggerName);
     }
 
     /**
      * Creates a logger from the supplied class name that uses the supplied
     * {@link java.util.ResourceBundle} as the message source.
      * 
      * @param aClass The class whose name should be used as the logger name
      * @param aBundleName The <code>ResourceBundle</code> to pull text from
      */
     public Logger(Class<?> aClass, String aBundleName) {
         super(aBundleName);
         LOGGER = LoggerFactory.getLogger(aClass);
     }
 
     /**
      * Creates a logger from the supplied name that uses the supplied
     * {@link java.util.ResourceBundle} as the message source.
      * 
      * @param aLoggerName The name of the logger to be constructed
      * @param aBundleName The <code>ResourceBundle</code> to pull text from
      */
     public Logger(String aLoggerName, String aBundleName) {
         super(aBundleName);
         LOGGER = LoggerFactory.getLogger(aLoggerName);
     }
 
     @Override
     public void debug(String aMessage) {
         LOGGER.debug(getI18n(aMessage));
     }
 
     @Override
     public void debug(String aMessage, Object aDetail) {
         LOGGER.debug(getI18n(aMessage), aDetail);
     }
 
     @Override
     public void debug(String aMessage, Object... aVararg) {
         LOGGER.debug(getI18n(aMessage), aVararg);
     }
 
     @Override
     public void debug(String aMessage, Throwable aThrowable) {
         LOGGER.debug(getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void debug(Marker aMarker, String aMessage) {
         LOGGER.debug(aMarker, getI18n(aMessage));
     }
 
     @Override
     public void debug(String aMessage, Object a1stDetail, Object a2ndDetail) {
         LOGGER.debug(getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void debug(Marker aMarker, String aMessage, Object aDetail) {
         LOGGER.debug(aMarker, getI18n(aMessage), aDetail);
     }
 
     @Override
     public void debug(Marker aMarker, String aMessage, Object... aVararg) {
         LOGGER.debug(aMarker, getI18n(aMessage), aVararg);
     }
 
     @Override
     public void debug(Marker aMarker, String aMessage, Throwable aThrowable) {
         LOGGER.debug(aMarker, getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void debug(Marker aMarker, String aMessage, Object a1stDetail,
             Object a2ndDetail) {
         LOGGER.debug(aMarker, getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void error(String aMessage) {
         LOGGER.error(getI18n(aMessage));
     }
 
     @Override
     public void error(String aMessage, Object aDetail) {
         LOGGER.error(getI18n(aMessage), aDetail);
     }
 
     @Override
     public void error(String aMessage, Object... aVararg) {
         LOGGER.error(getI18n(aMessage), aVararg);
     }
 
     @Override
     public void error(String aMessage, Throwable aThrowable) {
         LOGGER.error(getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void error(Marker aMarker, String aMessage) {
         LOGGER.error(aMarker, getI18n(aMessage));
     }
 
     @Override
     public void error(String aMessage, Object a1stDetail, Object a2ndDetail) {
         LOGGER.error(getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void error(Marker aMarker, String aMessage, Object aDetail) {
         LOGGER.error(aMarker, getI18n(aMessage), aDetail);
     }
 
     @Override
     public void error(Marker aMarker, String aMessage, Object... aVararg) {
         LOGGER.error(aMarker, getI18n(aMessage), aVararg);
     }
 
     @Override
     public void error(Marker aMarker, String aMessage, Throwable aThrowable) {
         LOGGER.error(aMarker, getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void error(Marker aMarker, String aMessage, Object a1stDetail,
             Object a2ndDetail) {
         LOGGER.error(aMarker, getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public String getName() {
         return LOGGER.getName();
     }
 
     @Override
     public void info(String aMessage) {
         LOGGER.info(getI18n(aMessage));
     }
 
     @Override
     public void info(String aMessage, Object aDetail) {
         LOGGER.info(getI18n(aMessage), aDetail);
     }
 
     @Override
     public void info(String aMessage, Object... aVararg) {
         LOGGER.info(getI18n(aMessage), aVararg);
     }
 
     @Override
     public void info(String aMessage, Throwable aThrowable) {
         LOGGER.info(getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void info(Marker aMarker, String aMessage) {
         LOGGER.info(aMarker, getI18n(aMessage));
     }
 
     @Override
     public void info(String aMessage, Object a1stDetail, Object a2ndDetail) {
         LOGGER.info(getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void info(Marker aMarker, String aMessage, Object aDetail) {
         LOGGER.info(aMarker, getI18n(aMessage), aDetail);
     }
 
     @Override
     public void info(Marker aMarker, String aMessage, Object... aVararg) {
         LOGGER.info(aMarker, getI18n(aMessage), aVararg);
     }
 
     @Override
     public void info(Marker aMarker, String aMessage, Throwable aThrowable) {
         LOGGER.info(aMarker, getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void info(Marker aMarker, String aMessage, Object a1stDetail,
             Object a2ndDetail) {
         LOGGER.info(aMarker, getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public boolean isDebugEnabled() {
         return LOGGER.isDebugEnabled();
     }
 
     @Override
     public boolean isDebugEnabled(Marker aMarker) {
         return LOGGER.isDebugEnabled(aMarker);
     }
 
     @Override
     public boolean isErrorEnabled() {
         return LOGGER.isErrorEnabled();
     }
 
     @Override
     public boolean isErrorEnabled(Marker aMarker) {
         return LOGGER.isErrorEnabled(aMarker);
     }
 
     @Override
     public boolean isInfoEnabled() {
         return LOGGER.isInfoEnabled();
     }
 
     @Override
     public boolean isInfoEnabled(Marker aMarker) {
         return LOGGER.isInfoEnabled(aMarker);
     }
 
     @Override
     public boolean isTraceEnabled() {
         return LOGGER.isTraceEnabled();
     }
 
     @Override
     public boolean isTraceEnabled(Marker aMarker) {
         return LOGGER.isTraceEnabled(aMarker);
     }
 
     @Override
     public boolean isWarnEnabled() {
         return LOGGER.isWarnEnabled();
     }
 
     @Override
     public boolean isWarnEnabled(Marker aMarker) {
         return LOGGER.isWarnEnabled(aMarker);
     }
 
     @Override
     public void trace(String aMessage) {
         LOGGER.trace(getI18n(aMessage));
     }
 
     @Override
     public void trace(String aMessage, Object aDetail) {
         LOGGER.trace(getI18n(aMessage), aDetail);
     }
 
     @Override
     public void trace(String aMessage, Object... aVararg) {
         LOGGER.trace(getI18n(aMessage), aVararg);
     }
 
     @Override
     public void trace(String aMessage, Throwable aThrowable) {
         LOGGER.trace(getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void trace(Marker aMarker, String aMessage) {
         LOGGER.trace(aMarker, getI18n(aMessage));
     }
 
     @Override
     public void trace(String aMessage, Object a1stDetail, Object a2ndDetail) {
         LOGGER.trace(getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void trace(Marker aMarker, String aMessage, Object aDetail) {
         LOGGER.trace(aMarker, getI18n(aMessage), aDetail);
     }
 
     @Override
     public void trace(Marker aMarker, String aMessage, Object... aVararg) {
         LOGGER.trace(aMarker, getI18n(aMessage), aVararg);
     }
 
     @Override
     public void trace(Marker aMarker, String aMessage, Throwable aThrowable) {
         LOGGER.trace(aMarker, getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void trace(Marker aMarker, String aMessage, Object a1stDetail,
             Object a2ndDetail) {
         LOGGER.trace(aMarker, getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void warn(String aMessage) {
         LOGGER.warn(getI18n(aMessage));
     }
 
     @Override
     public void warn(String aMessage, Object aDetail) {
         LOGGER.warn(getI18n(aMessage), aDetail);
     }
 
     @Override
     public void warn(String aMessage, Object... aVararg) {
         LOGGER.warn(getI18n(aMessage), aVararg);
     }
 
     @Override
     public void warn(String aMessage, Throwable aThrowable) {
         LOGGER.warn(getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void warn(Marker aMarker, String aMessage) {
         LOGGER.warn(aMarker, getI18n(aMessage));
     }
 
     @Override
     public void warn(String aMessage, Object a1stDetail, Object a2ndDetail) {
         LOGGER.warn(getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
     @Override
     public void warn(Marker aMarker, String aMessage, Object aDetail) {
         LOGGER.warn(aMarker, getI18n(aMessage), aDetail);
     }
 
     @Override
     public void warn(Marker aMarker, String aMessage, Object... aVararg) {
         LOGGER.warn(aMarker, getI18n(aMessage), aVararg);
     }
 
     @Override
     public void warn(Marker aMarker, String aMessage, Throwable aThrowable) {
         LOGGER.warn(aMarker, getI18n(aMessage), aThrowable);
     }
 
     @Override
     public void warn(Marker aMarker, String aMessage, Object a1stDetail,
             Object a2ndDetail) {
         LOGGER.warn(aMarker, getI18n(aMessage), a1stDetail, a2ndDetail);
     }
 
 }
