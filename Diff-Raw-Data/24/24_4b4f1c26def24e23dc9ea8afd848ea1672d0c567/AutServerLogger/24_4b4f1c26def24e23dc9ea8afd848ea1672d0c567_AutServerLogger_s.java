 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.logger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
 * Wrapper class for org.apache.commons.logging
  * This class is used in the implementation class context.
  * This class will NOT be loaded by the ImplClassClassLoader but by its parent!
 * The methods in this class delegate to the org.apache.commons.logging.Log.
  * This is neccessary because in the ImplClassClassLoader context u can only 
  * use plain java code!
  * @author BREDEX GmbH
  * @created 10.05.2006
  */
 public class AutServerLogger {
     /** The Logger */
     private  Logger m_log;
     
     /**
      * Constructor
      * @param clazz the caller class
      */
     public AutServerLogger(Class clazz) {
         m_log = LoggerFactory.getLogger(clazz); 
     }
     
     /**
      * {@inheritDoc}
      * @param message
      * @param t
      */
     public void debug(Object message, Throwable t) {
         m_log.debug(String.valueOf(message), t);
     }
 
     /**
      * {@inheritDoc}
      * @param message
      */
     public void debug(Object message) {
         m_log.debug(String.valueOf(message));
     }
 
     /**
      * {@inheritDoc}
      * @param message
      * @param t
      */
     public void error(Object message, Throwable t) {
         m_log.error(String.valueOf(message), t);
     }
 
     /**
      * {@inheritDoc}
      * @param message
      */
     public void error(Object message) {
         m_log.error(String.valueOf(message));
     }
 
     /**
      * {@inheritDoc}
      * @param message
      * @param t
      */
     public void info(Object message, Throwable t) {
         m_log.info(String.valueOf(message), t);
     }
 
     /**
      * {@inheritDoc}
      * @param message
      */
     public void info(Object message) {
         m_log.info(String.valueOf(message));
     }
 
     /**
      * {@inheritDoc}
      * @param message
      * @param t
      */
     public void trace(Object message, Throwable t) {
         m_log.trace(String.valueOf(message), t);
     }
 
     /**
      * {@inheritDoc}
      * @param message
      */
     public void trace(Object message) {
         m_log.trace(String.valueOf(message));
     }
 
     /**
      * {@inheritDoc}
      * @param message
      * @param t
      */
     public void warn(Object message, Throwable t) {
         m_log.warn(String.valueOf(message), t);
     }
 
     /**
      * {@inheritDoc}
      * @param message
      */
     public void warn(Object message) {
         m_log.warn(String.valueOf(message));
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     public boolean isDebugEnabled() {
         return m_log.isDebugEnabled();
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     public boolean isErrorEnabled() {
         return m_log.isErrorEnabled();
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     public boolean isInfoEnabled() {
         return m_log.isInfoEnabled();
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     public boolean isTraceEnabled() {
         return m_log.isTraceEnabled();
     }
 
     /**
      * {@inheritDoc}
      * @return
      */
     public boolean isWarnEnabled() {
         return m_log.isWarnEnabled();
     }
     
     
 
 }
