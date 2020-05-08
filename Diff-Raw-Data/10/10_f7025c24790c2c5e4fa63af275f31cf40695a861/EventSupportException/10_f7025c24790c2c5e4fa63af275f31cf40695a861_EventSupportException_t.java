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
 package org.eclipse.jubula.rc.common.exception;
 
import org.apache.commons.lang.exception.ExceptionUtils;
 import org.eclipse.jubula.tools.exception.JBRuntimeException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 /**
  * This exception supports notification and processing of test error events by
  * the AUT server and the <code>CAPTestCommand</code> respectively. All method
  * involved in a test step execution may throw this exception.
  * 
  * @author BREDEX GmbH
  * @created 06.04.2005
  */
 public abstract class EventSupportException 
     extends JBRuntimeException {
     
     /** the error message id */
     private Integer m_id = MessageIDs.E_EVENT_SUPPORT;
     
     /**
      * The test error event.
      */
     private TestErrorEvent m_event;
     /**
      * @param cause The cause exception.
      * @param id An ErrorMessage.ID.
      * {@inheritDoc}
      */
     public EventSupportException(Throwable cause, Integer id) {
         super(cause, id);
        String message = cause.getMessage();
        if (message == null) {
            message = ExceptionUtils.getFullStackTrace(cause);
        }
         m_event = EventFactory.createActionError(
                TestErrorEvent.EXECUTION_ERROR, new Object[] { message });
     }
     /**
      * @param message The message.
      * @param event The test error event.
      * @param id An ErrorMessage.ID.
      * {@inheritDoc}
      */
     public EventSupportException(
         String message, TestErrorEvent event, Integer id) {
         super(message, id);
         m_event = event;
     }
     /**
      * @return The test error event, maybe <code>null</code>.
      */
     public TestErrorEvent getEvent() {
         return m_event;
     }
     /**
      * @return Returns the error message id.
      */
     public Integer getErrorId() {
         return m_id;
     }
 }
