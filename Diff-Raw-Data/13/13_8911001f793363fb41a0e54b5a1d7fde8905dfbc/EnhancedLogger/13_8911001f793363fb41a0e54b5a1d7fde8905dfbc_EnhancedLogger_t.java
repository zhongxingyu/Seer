 package org.nohope.logging;
 
 import org.slf4j.Marker;
 import org.slf4j.helpers.FormattingTuple;
 import org.slf4j.helpers.MessageFormatter;
 
 /**
  * This class is just wrapper for foreign {@link org.slf4j.Logger}
  * implementation with small improvements.
  *
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 2012-02-22 12:16
  */
 class EnhancedLogger implements Logger {
     private final org.slf4j.Logger logger;
 
     EnhancedLogger(final org.slf4j.Logger logger) {
         this.logger = logger;
     }
 
     @Override
     public void trace(final Throwable t, final String format, final Object... args) {
         if (isTraceEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
             logger.trace(ft.toString(), t);
         }
     }
 
     @Override
     public void debug(final Throwable t, final String format, final Object... args) {
         if (isDebugEnabled()) {
             final FormattingTuple ft = MessageFormatter.format(format, args);
             logger.debug(ft.toString(), t);
         }
     }
 
     @Override
     public void info(final Throwable t, final String format, final Object... args) {
         if (isInfoEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
             logger.info(ft.toString(), t);
         }
     }
 
     @Override
     public void warn(final Throwable t, final String format, final Object... args) {
         if (isWarnEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
            logger.warn(ft.getMessage(), t);
         }
     }
 
     @Override
     public void error(final Throwable t, final String format, final Object... args) {
         if (isErrorEnabled()) {
            final FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
             logger.error(ft.toString(), t);
         }
     }
 
     // the rest is just proxying
 
     @Override
     public void trace(final String format, final Object... args) {
         logger.trace(format, args);
     }
 
     @Override
     public void trace(final Marker marker, final String format, final Object... args) {
         logger.trace(marker, format, args);
     }
 
     @Override
     public void debug(final String format, final Object... args) {
         logger.debug(format, args);
     }
 
     @Override
     public void debug(final Marker marker, final String format, final Object... args) {
         logger.debug(marker, format, args);
     }
 
     @Override
     public void info(final String format, final Object... args) {
         logger.debug(format, args);
     }
 
     @Override
     public void info(final Marker marker, final String format, final Object... args) {
         logger.info(marker, format, args);
     }
 
     @Override
     public void warn(final String format, final Object... args) {
         logger.warn(format, args);
     }
 
     @Override
     public void warn(final Marker marker, final String format, final Object... args) {
         logger.warn(marker, format, args);
     }
 
     @Override
     public void error(final String format, final Object... args) {
         logger.error(format, args);
     }
 
     @Override
     public void error(final Marker marker, final String format, final Object... args) {
         logger.error(marker, format, args);
     }
 
     @Override
     public String getName() {
         return logger.getName();
     }
 
     @Override
     public boolean isTraceEnabled() {
         return logger.isTraceEnabled();
     }
 
     @Override
     public boolean isTraceEnabled(final Marker marker) {
         return logger.isTraceEnabled(marker);
     }
 
     @Override
     public void trace(final String msg) {
         logger.trace(msg);
     }
 
     @Override
     public void trace(final String format, final Object arg) {
         logger.trace(format, arg);
     }
 
     @Override
     public void trace(final String format, final Object arg1, final Object arg2) {
         logger.trace(format, arg1, arg2);
     }
 
     @Override
     public void trace(final String msg, final Throwable t) {
         logger.trace(msg, t);
     }
 
     @Override
     public void trace(final Marker marker, final String msg) {
         logger.trace(marker, msg);
     }
 
     @Override
     public void trace(final Marker marker, final String format, final Object arg) {
         logger.trace(marker, format, arg);
     }
 
     @Override
     public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
         logger.trace(marker, format, arg1, arg2);
     }
 
     @Override
     public void trace(final Marker marker, final String msg, final Throwable t) {
         logger.trace(marker, msg, t);
     }
 
     @Override
     public boolean isDebugEnabled() {
         return logger.isDebugEnabled();
     }
 
     @Override
     public boolean isDebugEnabled(final Marker marker) {
         return logger.isDebugEnabled(marker);
     }
 
     @Override
     public void debug(final String msg) {
         logger.debug(msg);
     }
 
     @Override
     public void debug(final String format, final Object arg) {
         logger.debug(format, arg);
     }
 
     @Override
     public void debug(final String format, final Object arg1, final Object arg2) {
         logger.debug(format, arg1, arg2);
     }
 
     @Override
     public void debug(final String msg, final Throwable t) {
         logger.debug(msg, t);
     }
 
     @Override
     public void debug(final Marker marker, final String msg) {
         logger.debug(marker, msg);
     }
 
     @Override
     public void debug(final Marker marker, final String format, final Object arg) {
         logger.debug(marker, format, arg);
     }
 
     @Override
     public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
         logger.debug(marker, format, arg1, arg2);
     }
 
     @Override
     public void debug(final Marker marker, final String msg, final Throwable t) {
         logger.debug(marker, msg, t);
     }
 
     @Override
     public boolean isInfoEnabled() {
         return logger.isInfoEnabled();
     }
 
     @Override
     public boolean isInfoEnabled(final Marker marker) {
         return logger.isInfoEnabled(marker);
     }
 
     @Override
     public void info(final String msg) {
         logger.info(msg);
     }
 
     @Override
     public void info(final String format, final Object arg) {
         logger.info(format, arg);
     }
 
     @Override
     public void info(final String format, final Object arg1, final Object arg2) {
         logger.info(format, arg1, arg2);
     }
 
     @Override
     public void info(final String msg, final Throwable t) {
         logger.info(msg, t);
     }
 
     @Override
     public void info(final Marker marker, final String msg) {
         logger.info(marker, msg);
     }
 
     @Override
     public void info(final Marker marker, final String format, final Object arg) {
         logger.info(marker, format, arg);
     }
 
     @Override
     public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
         logger.info(marker, format, arg1, arg2);
     }
 
     @Override
     public void info(final Marker marker, final String msg, final Throwable t) {
         logger.info(marker, msg, t);
     }
 
     @Override
     public boolean isWarnEnabled() {
         return logger.isWarnEnabled();
     }
 
     @Override
     public boolean isWarnEnabled(final Marker marker) {
         return logger.isWarnEnabled(marker);
     }
 
     @Override
     public void warn(final String msg) {
         logger.warn(msg);
     }
 
     @Override
     public void warn(final String format, final Object arg) {
         logger.warn(format, arg);
     }
 
     @Override
     public void warn(final String format, final Object arg1, final Object arg2) {
         logger.warn(format, arg1, arg2);
     }
 
     @Override
     public void warn(final String msg, final Throwable t) {
         logger.warn(msg, t);
     }
 
     @Override
     public void warn(final Marker marker, final String msg) {
         logger.warn(marker, msg);
     }
 
     @Override
     public void warn(final Marker marker, final String format, final Object arg) {
         logger.warn(marker, format, arg);
     }
 
     @Override
     public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
         logger.warn(marker, format, arg1, arg2);
     }
 
     @Override
     public void warn(final Marker marker, final String msg, final Throwable t) {
         logger.warn(marker, msg, t);
     }
 
     @Override
     public boolean isErrorEnabled() {
         return logger.isErrorEnabled();
     }
 
     @Override
     public boolean isErrorEnabled(final Marker marker) {
         return logger.isErrorEnabled(marker);
     }
 
     @Override
     public void error(final String msg) {
         logger.error(msg);
     }
 
     @Override
     public void error(final String format, final Object arg) {
         logger.error(format, arg);
     }
 
     @Override
     public void error(final String format, final Object arg1, final Object arg2) {
         logger.error(format, arg1, arg2);
     }
 
     @Override
     public void error(final String msg, final Throwable t) {
         logger.error(msg, t);
     }
 
     @Override
     public void error(final Marker marker, final String msg) {
         logger.error(marker, msg);
     }
 
     @Override
     public void error(final Marker marker, final String format, final Object arg) {
         logger.error(marker, format, arg);
     }
 
     @Override
     public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
         logger.error(marker, format, arg1, arg2);
     }
 
     @Override
     public void error(final Marker marker, final String msg, final Throwable t) {
         logger.error(marker, msg, t);
     }
 }
 
 
