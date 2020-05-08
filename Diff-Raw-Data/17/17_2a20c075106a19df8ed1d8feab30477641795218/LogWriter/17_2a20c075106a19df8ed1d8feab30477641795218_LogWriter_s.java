 /*
  * Copyright (c) 2008
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.io;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.slf4j.Logger;
 
 /**
  * Writer that outputs to the current logging facility (SLF4J).
  */
 public class LogWriter extends Writer {
 
   private final Logger _logger;
 
   private final LogLevel _level;
 
   private final StringBuilder _text = new StringBuilder();
 
   /**
    * Log levels that {@link LogWriter} can output to.
    */
   public enum LogLevel {
    TRACE, DEUBG, INFO, WARN, ERROR
   }
 
   /**
    * @param logger the logger to log to
    * @param level the level to log at
    * @throws NullPointerException if logger or level is null
    */
   public LogWriter(final Logger logger, final LogLevel level) {
     if (null == logger
         || null == level) {
       throw new NullPointerException();
     }
 
     this._logger = logger;
     this._level = level;
   }
 
   /**
    * @param lock the lock
    * @param logger the logger to log to
    * @param level the level to log at
    * @throws NullPointerException if logger or level is null
    */
   public LogWriter(final Object lock, final Logger logger, final LogLevel level) {
     super(lock);
     if (null == logger
         || null == level) {
       throw new NullPointerException();
     }
     this._logger = logger;
     this._level = level;
   }
 
   /**
    * @see java.io.Writer#close()
    */
   @Override
   public void close() throws IOException {
     flush();
   }
 
   /**
    * @see java.io.Writer#flush()
    */
   @Override
   public void flush() throws IOException {
     if (isLogLevelEnabled()) {
       final String s = _text.toString();
       if (s.length() > 0) {
         log(s);
       }
     }
     _text.setLength(0);
   }
 
   /**
    * @see java.io.Writer#write(char[], int, int)
    */
   @Override
   public void write(final char[] cbuf, final int off, final int len) throws IOException {
     if (isLogLevelEnabled()) {
       _text.append(cbuf, off, len);
     }
   }
 
   /**
    * Is the logging level that was specified in the constructor enabled?
    */
   private boolean isLogLevelEnabled() {
     switch (_level) {
     case TRACE:
       return _logger.isTraceEnabled();
    case DEUBG:
       return _logger.isDebugEnabled();
     case INFO:
       return _logger.isInfoEnabled();
     case WARN:
       return _logger.isWarnEnabled();
     case ERROR:
       return _logger.isErrorEnabled();
     default:
       throw new RuntimeException("Unknown log level: "
           + _level);
     }
   }
 
   /**
    * Do the logging.
    */
   private void log(final String s) {
     switch (_level) {
     case TRACE:
       _logger.trace(s);
    case DEUBG:
       _logger.debug(s);
     case INFO:
       _logger.info(s);
     case WARN:
       _logger.warn(s);
     case ERROR:
       _logger.error(s);
     default:
       throw new RuntimeException("Unknown log level: "
           + _level);
     }
   }
 
 }
