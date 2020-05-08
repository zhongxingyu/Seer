 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.exceptions;
 
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.LogLevel;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.logging.Log;
 
 /**
  * Base class for all [fleXive] Exceptions, supports localized messages and converting.
  * Refer to the property files in package com.flexive.shared.exception.messages for keys to use.
  * All property files in this package will be concatenated to one big FxExceptionMessages.properties file
  * upon deployment.
  * If a (not null) Logger is passed as the first argument the message will be logged at level "error" in the default
  * locale (usually english) as well.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxApplicationException extends Exception implements FxLocalizedException {
     private static final long serialVersionUID = 2510161167326772282L;
 
     /**
      * the localized message
      */
     protected FxExceptionMessage message;
 
     /**
      * was the message logged?
      */
     private boolean messageLogged = false;
     private String affectedXPath = "";
 
     /**
      * Localized exception constructor
      *
      * @param converted the message to convert
      */
     public FxApplicationException(FxApplicationException converted) {
        super(converted.message.getKey());
         this.message = converted.message;
         this.affectedXPath = converted.affectedXPath;
     }
 
     /**
      * Localized exception constructor
      *
      * @param log       Log to use
      * @param converted exception to convert
      */
     public FxApplicationException(Log log, FxApplicationException converted) {
         this(converted);
         if (log != null && !converted.messageLogged())
             this.logMessage(log, this.getMessage(), null);
     }
 
     /**
      * Has the message been logged?
      *
      * @return message logged
      */
     protected boolean messageLogged() {
         return this.messageLogged;
     }
 
     /**
      * Get the XPath that has caused this Exception to be thrown
      *
      * @return affected XPath or empty String if not XPath related
      */
     public String getAffectedXPath() {
         return affectedXPath;
     }
 
     /**
      * Is this exception related to an XPath?
      *
      * @return exception related to an XPath
      */
     public boolean hasAffectedXPath() {
         return !"".equals(affectedXPath);
     }
 
     /**
      * Set the affected XPath that caused this Exception to be thrown
      *
      * @param affectedXPath XPath that caused this Exception to be thrown
      * @return this
      */
     public FxApplicationException setAffectedXPath(String affectedXPath) {
         if (affectedXPath != null)
             this.affectedXPath = affectedXPath;
         return this;
     }
 
     /**
      * Log a message at a given level (or error if no level given)
      *
      * @param log     Log to use
      * @param message magges to LOG
      * @param level   log4j level to apply
      */
     private void logMessage(Log log, String message, LogLevel level) {
         this.messageLogged = true;
         if( FxContext.get() != null && FxContext.get().isTestDivision())
             return; //dont log exception traces during automated tests
         final Throwable cause = getCause() != null ? getCause() : this;
         if (level == null)
             log.error(message, cause);
         else {
             switch (level) {
                 case DEBUG:
                     if (log.isDebugEnabled())
                         log.debug(message);
                     break;
                 case ERROR:
                     if (log.isErrorEnabled())
                         log.error(message, cause);
                     break;
                 case FATAL:
                     if (log.isFatalEnabled())
                         log.fatal(message, cause);
                     break;
                 case INFO:
                     if (log.isInfoEnabled())
                         log.info(message);
                     break;
 //                case Level.WARN_INT:
                 default:
                     if (log.isWarnEnabled())
                         log.warn(message);
             }
         }
     }
 
     /**
      * Has the message been logged?
      *
      * @return message logged
      */
     public boolean isMessageLogged() {
         return messageLogged;
     }
 
     /**
      * Localized exception constructor
      *
      * @param key    exception messge resource key
      * @param values value parameters for resource message
      */
     public FxApplicationException(String key, Object... values) {
         super(key);
         this.message = new FxExceptionMessage(key, values);
     }
 
     /**
      * Localized exception constructor
      *
      * @param log    Log to use
      * @param key    exception messge resource key
      * @param values value parameters for resource message
      */
     public FxApplicationException(Log log, String key, Object... values) {
         super(key);
         this.message = new FxExceptionMessage(key, values);
         if (log != null)
             this.logMessage(log, this.getMessage(), null);
     }
 
     /**
      * Localized exception constructor
      *
      * @param cause  causing exception
      * @param key    exception messge resource key
      * @param values value parameters for resource message
      */
     public FxApplicationException(Throwable cause, String key, Object... values) {
         super(key, cause);
         this.message = new FxExceptionMessage(key, values);
     }
 
     /**
      * Localized exception constructor
      *
      * @param log    Log to use
      * @param cause  causing message
      * @param key    exception messge resource key
      * @param values value parameters for resource message
      */
     public FxApplicationException(Log log, Throwable cause, String key, Object... values) {
         super(key, cause);
         this.message = new FxExceptionMessage(key, values);
         if (log != null)
             this.logMessage(log, this.getMessage(), null);
     }
 
     /**
      * Localized exception constructor
      *
      * @param key exception messge resource key
      */
     public FxApplicationException(String key) {
         super(key);
         this.message = new FxExceptionMessage(key);
     }
 
     /**
      * Localized exception constructor
      *
      * @param log Log to use
      * @param key exception messge resource key
      */
     public FxApplicationException(Log log, String key) {
         super(key);
         this.message = new FxExceptionMessage(key);
         if (log != null)
             this.logMessage(log, this.getMessage(), null);
     }
 
     /**
      * Localized exception constructor
      *
      * @param message resource key
      * @param cause   causing exception
      */
     public FxApplicationException(String message, Throwable cause) {
         super(message, cause);
         this.message = new FxExceptionMessage(message);
     }
 
     /**
      * Localized exception constructor
      *
      * @param log     Log to use
      * @param message resource key
      * @param cause   exception cause
      */
     public FxApplicationException(Log log, String message, Throwable cause) {
         super(message, cause);
         this.message = new FxExceptionMessage(message);
         if (log != null)
             this.logMessage(log, this.getMessage(), null);
     }
 
     /**
      * Localized exception constructor
      *
      * @param cause exception cause
      */
     public FxApplicationException(Throwable cause) {
         super(cause);
         assignMessage(cause);
     }
 
     /**
      * Assign a message from another Exception
      *
      * @param cause the cause to assign the message to this exception
      */
     private void assignMessage(Throwable cause) {
         if (cause instanceof FxApplicationException)
             this.message = ((FxApplicationException) cause).message;
         else
             this.message = new FxExceptionMessage("ex.native", cause.getClass().getName(), cause.getMessage());
     }
 
     /**
      * Localized exception constructor
      *
      * @param log   Log to use
      * @param cause causing exception
      */
     public FxApplicationException(Log log, Throwable cause) {
         super(cause);
         assignMessage(cause);
         if (log != null)
             this.logMessage(log, this.getMessage(), null);
     }
 
     /**
      * {@inheritDoc}
      */
     public final String getMessage() {
         return message.getLocalizedMessage(FxLanguage.DEFAULT_ID, FxLanguage.DEFAULT_ISO) + evaluateCause(FxLanguage.DEFAULT_ID);
     }
 
 
     /**
      * Return the localized messages of any chained exceptions that are derived from FxException
      *
      * @param langCode the language code
      * @return the localized messages of any chained exceptions that are derived from FxException
      */
     private String evaluateCause(long langCode) {
         Throwable org = this.getCause();
         String msg = "";
         while (org != null) {
             if (org instanceof FxApplicationException) {
                 if( !this.message.equals(((FxApplicationException) org).message))
                     msg += ((FxApplicationException) org).message.getLocalizedMessage(langCode);
             }
             org = org.getCause();
         }
         return msg;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getMessage(FxLanguage locale) {
         return message.getLocalizedMessage(locale) + evaluateCause(locale.getId());
     }
 
     /**
      * {@inheritDoc}
      */
     public String getMessage(long localeId) {
         return message.getLocalizedMessage(localeId) + evaluateCause(localeId);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getMessage(UserTicket ticket) {
         if (ticket != null)
             return message.getLocalizedMessage(ticket.getLanguage()) + evaluateCause(ticket.getLanguage().getId());
         else
             return this.getMessage();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxExceptionMessage getExceptionMessage() {
         return message;
     }
 
     /**
      * Wraps this exception in a FxRuntimeException.
      *
      * @return this exception wrapped in a FxRuntimeException.
      */
     public FxRuntimeException asRuntimeException() {
         FxRuntimeException exception = new FxRuntimeException(this);
         // keep our stack trace
         exception.setStackTrace(this.getStackTrace());
         return exception;
     }
 }
