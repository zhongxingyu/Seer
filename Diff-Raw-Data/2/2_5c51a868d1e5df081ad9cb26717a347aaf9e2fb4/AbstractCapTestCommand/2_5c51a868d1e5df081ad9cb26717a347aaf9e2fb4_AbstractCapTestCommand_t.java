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
 package org.eclipse.jubula.rc.common.commands;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.communication.ICommand;
 import org.eclipse.jubula.communication.message.CAPTestMessage;
 import org.eclipse.jubula.communication.message.CAPTestResponseMessage;
 import org.eclipse.jubula.communication.message.ChangeAUTModeMessage;
 import org.eclipse.jubula.communication.message.Message;
 import org.eclipse.jubula.communication.message.MessageCap;
 import org.eclipse.jubula.communication.message.MessageParam;
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.AUTServerConfiguration;
 import org.eclipse.jubula.rc.common.exception.ComponentNotFoundException;
 import org.eclipse.jubula.rc.common.exception.EventSupportException;
 import org.eclipse.jubula.rc.common.exception.ExecutionEvent;
 import org.eclipse.jubula.rc.common.exception.MethodParamException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.exception.StepVerifyFailedException;
 import org.eclipse.jubula.rc.common.exception.UnsupportedComponentException;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.tools.constants.DebugConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * This class gets an message with ICommand action parameter triples. It invokes
  * the implementation class and executes the method. Then it creates a
  * <code>CAPTestResponseMessage</code> and sends it back to the client. The
  * <code>CAPTestResponseMessage</code> contains an error event only if the
  * test step fails, due to a problem prior to or during the execution of the
  * implementation class action method.
  * @author BREDEX GmbH
  * @created 02.01.2007
  * 
  */
 public abstract class AbstractCapTestCommand implements ICommand {
 
     /** The logger */
     private static final Logger LOG = LoggerFactory.getLogger(
         AbstractCapTestCommand.class);
     
     /** The message. */
     private CAPTestMessage m_capTestMessage;
 
     /**
      * {@inheritDoc}
      */
     public Message getMessage() {
         return m_capTestMessage;
     }
     /**
      * {@inheritDoc}
      */
     public void setMessage(Message message) {
         m_capTestMessage = (CAPTestMessage)message;
     }
     
     /**
      * Is called if the graphics component cannot be found. Logs the error and
      * sets the action error event into the message.
      * @param response The response message
      * @param e The exception.
      */
     private void handleComponentNotFound(CAPTestResponseMessage response,
         Throwable e) {
         if (LOG.isWarnEnabled()) {
             LOG.warn(DebugConstants.ERROR, e);
         }
         response.setTestErrorEvent(EventFactory
                 .createComponentNotFoundErrorEvent());
     }
 
     /**
      * Is called if one or more CAP parameters are invalid. Logs the error and
      * sets the action error event into the message.
      * @param e The error message.
      */
     private void handleInvalidInput(String e) {
         throw new StepExecutionException(e, EventFactory
                 .createImplClassErrorEvent());
     }
 
     /**
      * Gets the implementation class. 
      * @param response The response message.
      * @return the implementation class or null if an error occurs.
      */
     protected Object getImplClass(CAPTestResponseMessage response) {
         Object implClass = null;
         IComponentIdentifier ci = m_capTestMessage.getMessageCap().getCi();
         if (LOG.isInfoEnabled()) {
             LOG.info("component class name: " //$NON-NLS-1$
                 + (ci == null ? "(none)" : ci.getComponentClassName())); //$NON-NLS-1$
         }
         try {
             Validate.notNull(ci);
             // FIXME : Extra handling for waitForComponent and verifyExists
             int timeout = 500;
             
             boolean isWaitForComponent = m_capTestMessage.getMessageCap()
                 .getMethod().equals("gdWaitForComponent"); //$NON-NLS-1$
             if (isWaitForComponent) { 
                 MessageParam timeoutParam = (MessageParam)m_capTestMessage.
                     getMessageCap().getMessageParams().get(0);
                 try {
                     timeout = Integer.parseInt(timeoutParam.getValue());
                 } catch (NumberFormatException e) {
                     LOG.warn("Error while parsing timeout parameter. " //$NON-NLS-1$
                         + "Using default value.", e); //$NON-NLS-1$
                 }
             }
             Object component = findComponent(ci, timeout);
             implClass = AUTServerConfiguration.getInstance()
                 .prepareImplementationClass(component, component.getClass());
 
             if (isWaitForComponent) {
                 MessageParam delayParam = (MessageParam)m_capTestMessage.
                     getMessageCap().getMessageParams().get(1);
                 try {
                     int delay = Integer.parseInt(delayParam.getValue());
                     TimeUtil.delay(delay);
                 } catch (IllegalArgumentException iae) {
                     handleInvalidInput("Invalid input: " //$NON-NLS-1$
                         + CompSystemI18n.getString("CompSystem.DelayAfterVisibility") //$NON-NLS-1$
                         + " must be a non-negative integer."); //$NON-NLS-1$
                 }
             }
         } catch (IllegalArgumentException e) {
             handleComponentNotFound(response, e);
         } catch (ComponentNotFoundException e) {
             MessageCap cap = m_capTestMessage.getMessageCap();
             if ("gdVerifyExists".equals(cap.getMethod())) { //$NON-NLS-1$
                 MessageParam isVisibleParam = 
                     (MessageParam)cap.getMessageParams().get(0);
                 handleComponentDoesNotExist(response, 
                     Boolean.valueOf(isVisibleParam.getValue()).booleanValue());
             } else {
                 handleComponentNotFound(response, e);
             }
         } catch (UnsupportedComponentException buce) {
             LOG.error(DebugConstants.ERROR, buce);
             response.setTestErrorEvent(EventFactory.createConfigErrorEvent());
         } catch (Throwable e) {
             if (LOG.isErrorEnabled()) {
                 LOG.error(DebugConstants.ERROR, e);
             }
             response.setTestErrorEvent(
                     EventFactory.createImplClassErrorEvent());
         } 
         return implClass;
     }
     
     /**
      * Handles the scenario where a component does not exist, but may also
      * not be expected to exist.
      * Is called if the graphics component cannot be found and the current 
      * request is attempting to verify the existence/non-existence of that 
      * component.
      * Sets the status of the response to Verification Error if the component is
      * expected to exist. Otherwise continues normal operation.
 
      * @param response The response message
      * @param shouldExist <code>True</code> if the component is expected to
      *                    exist. Otherwise, <code>false</code>.
      */
     private void handleComponentDoesNotExist(CAPTestResponseMessage response, 
         boolean shouldExist) {
         try {
             Verifier.equals(shouldExist, false);
         } catch (StepVerifyFailedException svfe) {
             response.setTestErrorEvent(EventFactory.createVerifyFailed(
                     String.valueOf(shouldExist), String.valueOf(false)));
             
         }
     }
 
     /**
      * @param ci
      *            the component identifier
      * @param timeout
      *            the timeout
      * @return the found component
      * @throws IllegalArgumentException
      *             if error occured
      * @throws ComponentNotFoundException
      *             if component could not found in compHierarchy
      */
     protected abstract Object findComponent(IComponentIdentifier ci, 
         int timeout) throws ComponentNotFoundException, 
         IllegalArgumentException;
     
     /**
      * calls the method of the implementation class per reflection
      * {@inheritDoc}
      */
     public Message execute() {
         final int oldMode = AUTServer.getInstance().getMode();
         TestErrorEvent event = null;
         CAPTestResponseMessage response = new CAPTestResponseMessage();
         if (oldMode != ChangeAUTModeMessage.TESTING) {
             AUTServer.getInstance().setMode(ChangeAUTModeMessage.TESTING);
         } 
         try {
             response.setMessageCap(m_capTestMessage.getMessageCap());
         
             // get the implementation class
             Object implClass = getImplClass(response);
             if (implClass == null) {
                 return response;
             }
             MethodInvoker invoker = new MethodInvoker(m_capTestMessage
                 .getMessageCap());
             Object returnValue = invoker.invoke(implClass);
             response.setReturnValue((String)returnValue);
         } catch (NoSuchMethodException nsme) {
             LOG.error("implementation class method not found", nsme); //$NON-NLS-1$
            event = EventFactory.createUnsupportedActionError();
         } catch (IllegalAccessException iae) {
             LOG.error("Failed accessing implementation class method", iae); //$NON-NLS-1$
             event = EventFactory.createConfigErrorEvent();
         } catch (InvocationTargetException ite) {
             if (ite.getTargetException() instanceof EventSupportException) {
                 EventSupportException e = (EventSupportException)
                     ite.getTargetException();
                 event = e.getEvent();
                 if (LOG.isDebugEnabled()) {
                     LOG.debug(DebugConstants.ERROR, e);
                 }
             } else if (ite.getTargetException() instanceof ExecutionEvent) {
                 ExecutionEvent e = (ExecutionEvent)ite.getTargetException();
                 response.setState(e.getEvent());
                 if (LOG.isDebugEnabled()) {
                     LOG.debug(DebugConstants.ERROR, e);
                 }
             } else {
                 event = EventFactory.createConfigErrorEvent();
                 if (LOG.isErrorEnabled()) {
                     LOG.error("InvocationTargetException: ", ite); //$NON-NLS-1$
                     LOG.error("TargetException: ", ite.getTargetException()); //$NON-NLS-1$
                 }
             }
         } catch (IllegalArgumentException e) {
             LOG.error(DebugConstants.ERROR, e);
         } catch (MethodParamException e) {
             LOG.error(DebugConstants.ERROR, e);
         } finally {
             if (AUTServer.getInstance().getMode() != oldMode) {
                 AUTServer.getInstance().setMode(oldMode);
             }
         }
         if (event != null) {
             response.setTestErrorEvent(event);
         }
         if (m_capTestMessage.isRequestAnswer()) {
             return response;
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void timeout() {
         LOG.error(this.getClass().getName() + "timeout() called"); //$NON-NLS-1$
     }
 }
