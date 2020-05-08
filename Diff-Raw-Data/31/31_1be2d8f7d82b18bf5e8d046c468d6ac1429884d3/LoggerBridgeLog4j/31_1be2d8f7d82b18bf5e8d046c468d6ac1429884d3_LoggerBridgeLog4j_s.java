 /////////////////////////////////////////////////////////////////////////////
 //
 // Project ProjectForge Community Edition
 //         www.projectforge.org
 //
 // Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
 //
 // ProjectForge is dual-licensed.
 //
 // This community edition is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License as published
 // by the Free Software Foundation; version 3 of the License.
 //
 // This community edition is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 // Public License for more details.
 //
 // You should have received a copy of the GNU General Public License along
 // with this program; if not, see http://www.gnu.org/licenses/.
 //
 /////////////////////////////////////////////////////////////////////////////
 
 package org.projectforge.common;
 
 /**
  * Implementation of log4j.
  * 
  * @author Kai Reinhard (k.reinhard@micromata.de)
  * 
  */
 public class LoggerBridgeLog4j extends Logger
 {
   private org.apache.log4j.Logger log;
 
   public LoggerBridgeLog4j()
   {
   }
 
   public LoggerBridgeLog4j(final org.apache.log4j.Logger logger)
   {
     this.log = logger;
   }
 
   /**
    * @see org.projectforge.common.Logger#isDebugEnabled()
    */
   @Override
   public boolean isDebugEnabled()
   {
     return log.isDebugEnabled();
   }
 
   /**
    * @see org.projectforge.common.Logger#info(java.lang.Object)
    */
   @Override
   public void debug(final Object message)
   {
    log.debug(message);
   }
 
   /**
    * @see org.projectforge.common.Logger#info(java.lang.Object)
    */
   @Override
   public void info(final Object message)
   {
    log.info(message);
   }
 
   /**
    * @see org.projectforge.common.Logger#warn(java.lang.Object)
    */
   @Override
   public void warn(final Object message)
   {
    log.warn(message);
   }
 
   /**
    * @see org.projectforge.common.Logger#error(java.lang.Object)
    */
   @Override
   public void error(final Object message)
   {
    log.error(message);
   }
 
   /**
    * @see org.projectforge.common.Logger#error(java.lang.Object, java.lang.Throwable)
    */
   @Override
   public void error(final Object message, final Throwable t)
   {
    log.error(message, t);
   }
 
   /**
    * @see org.projectforge.common.Logger#fatal(java.lang.Object)
    */
   @Override
   public void fatal(final Object message)
   {
    log.fatal(message);
   }
 
   /**
    * @see org.projectforge.common.Logger#fatal(java.lang.Object, java.lang.Throwable)
    */
   @Override
   public void fatal(final Object message, final Throwable t)
   {
    log.fatal(message, t);
   }
 
   /**
    * @see org.projectforge.common.Logger#getInternalLogger(java.lang.String)
    */
   @Override
   protected Logger getInternalLogger(final String name)
   {
     return new LoggerBridgeLog4j(org.apache.log4j.Logger.getLogger(name));
   }
 }
