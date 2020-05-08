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
 package org.eclipse.jubula.rc.swing.listener;
 
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ComponentEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.PaintEvent;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JList;
 import javax.swing.plaf.basic.ComboPopup;
 
 import org.eclipse.jubula.communication.message.ObjectMappedMessage;
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.AUTServerConfiguration;
 import org.eclipse.jubula.rc.common.exception.ComponentNotFoundException;
 import org.eclipse.jubula.rc.common.exception.NoIdentifierForComponentException;
 import org.eclipse.jubula.rc.common.exception.UnsupportedComponentException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swing.tester.util.TesterUtil;
 import org.eclipse.jubula.tools.exception.CommunicationException;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 
 
 /**
  * The AWTEventListener for mode OBJECT_MAPPING. <br>
  * 
  * This listener listens to mouse- an key events. 
  *  The component is marked by calling the methods
  * highLight() and lowLight() respectively of the corresponding implementation
  * class. <br>
  * 
  * The key events are tapped for selecting the <code>m_currentComponent</code>
  * to be used for the object mapping. The method <code>accept(KeyEvent)</code>
  * from the <code>MappingAcceptor</code> is queried to decide, whether the
  * event suits the active configuration. <br>
  * 
  * A <code>ComponentHandler</code> is used to determine the identifaction of
  * the component. See the <code>ComponentHandler</code> for details.
  * 
  * @author BREDEX GmbH
  * @created 23.08.2004
  */
 public class MappingListener extends AbstractAutSwingEventListener {
     
    
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
         MappingListener.class);
 
 
     /**
      * Handles the given AWTEvent
      * @param event the event to handle.
      */
     protected void handleEvent(AWTEvent event) {
         
         if (event.equals(getLastEvent())) {
             return;
         }
         setLastEvent(event);
         try {
             if (event instanceof KeyEvent
                     || (event instanceof MouseEvent
                             && ((MouseEvent)event).getID() 
                                 == MouseEvent.MOUSE_CLICKED)) {
                 handleInputEvent((InputEvent)event);
             } else {
             
                 Component source = getEventSource(event);
                 if (source == null || isComboPopup(source)) {
                     return;
                 }
                 Component parent = source.getParent();
                 Object implClass = null;
                 
                 if (log.isDebugEnabled()) {
                     log.debug(event.paramString()
                         + ",source:" + source.toString() //$NON-NLS-1$
                         + ",class:" + source.getClass().getName()); //$NON-NLS-1$ 
                 }
                 // First check parent
                 if (parent != null) {
     
                     try {
                         implClass = AUTServerConfiguration.getInstance()
                             .getImplementationClass(parent.getClass());
                         source = parent;
                     } catch (UnsupportedComponentException uce) { // NOPMD by zeb on 10.04.07 12:24
                         /* 
                          * This means that the parent of the source of the
                          * event is not a supported component. The original
                          * source is used, rather than the parent component.
                          */
                     }
                 }
                 if (implClass == null) {
                     try {
                         implClass = AUTServerConfiguration.getInstance()
                             .getImplementationClass(source.getClass());
                     } catch (UnsupportedComponentException uce2) {
                         return;
                     }
                 }
                 switchEvent(event, source, implClass);
             }
         } catch (ClassCastException cce) {
             // exception from cast in KEY_RELEASED branch -> just log 
             log.error(cce);
         }
     }
 
     /**
      * special handling for the JList-object in a JComboBox
      * @param source the source to check
      * @return true, if a parent of the given source is a ComboPopup
      */
     private boolean isComboPopup(Component source) {
         if (source instanceof ComboPopup) {
             return true;
         } else if (source instanceof JList) {
             Component parent = source.getParent();
             while (parent != null) {
                 if (parent instanceof ComboPopup) {
                     return true;
                 }
                 parent = parent.getParent();
             }
         }
         return false;
     }
     
     /**
      * @param event     AWTEvent
      * @param source    Component
      * @param implClass IImplementationClass
      */
     protected void switchEvent(AWTEvent event, Component source, 
         final Object implClass) {
         
         final Color highlightColor = null;
         switch(event.getID()) {
             case MouseEvent.MOUSE_RELEASED:
             case MouseEvent.MOUSE_PRESSED:
                 highlightClicked(implClass, highlightColor);
                 break;
             case MouseEvent.MOUSE_ENTERED:
             case MouseEvent.MOUSE_MOVED:
                 highlight(source, implClass, highlightColor);
                 break;
             default:
                 if (log.isDebugEnabled()) {
                     log.debug("event occured: " + event.paramString());  //$NON-NLS-1$
                 }
         }
         final int eventId = event.getID();
         if ((eventId >= ComponentEvent.COMPONENT_FIRST 
                 && eventId <= ComponentEvent.COMPONENT_LAST)
                 || (eventId >= PaintEvent.PAINT_FIRST 
                 && eventId <= PaintEvent.PAINT_LAST)
                 || (eventId >= WindowEvent.WINDOW_FIRST 
                 && eventId <= WindowEvent.WINDOW_LAST)) {
             
             updateHighlighting(source, implClass, highlightColor);
         }
     }
     
     
     
     /**
      * method handling the tapped event. <br>
      * Asks the <code>m_acceptor</code> to accept the event. If it's so, send
      * a message with the identifier to the client.
      * 
      * @param event
      *            the occured KeyEvent
      */
     private void handleInputEvent(InputEvent event) {
         if (event.getID() != KeyEvent.KEY_PRESSED
                 && event.getID() != MouseEvent.MOUSE_CLICKED) {
             return;
         }
         if (log.isInfoEnabled()) {
             log.info("handleKeyEvent: event = " + event.paramString()); //$NON-NLS-1$
         }
        synchronized (getComponentLock()) {
             // is a component selected? AND the right keys pressed?
             if (getCurrentComponent() != null 
                     && getAcceptor().accept(event) == KeyAcceptor
                         .MAPPING_KEY_COMB) {
                 
                 IComponentIdentifier id;
                 try {
                     id = ComponentHandler.getIdentifier(getCurrentComponent());
                     if (log.isInfoEnabled()) {
                         log.info("send a message with identifier for the component '"  //$NON-NLS-1$
                             + id + "'"); //$NON-NLS-1$
                     }
                     // send a message with the identifier of the selected component
                     ObjectMappedMessage message = new ObjectMappedMessage();
                     message.setComponentIdentifier(id);
                     AUTServer.getInstance().getCommunicator().send(message);
                 } catch (NoIdentifierForComponentException nifce) {
                     // no identifier for the component, log this as an error
                     log.error("no identifier for '" + getCurrentComponent()); //$NON-NLS-1$
                 } catch (CommunicationException ce) {
                     log.error(ce);
                     // do nothing here: a closed connection is handled by the
                     // AUTServer
                 }
             }
         }
     }
     
     /**
      * repaints the border
      *
      */
     public void update() {
         final Color highlightColor = null;
         if (getCurrentComponent() != null) {
             try {
 
                 AUTServerConfiguration.getInstance()
                         .getImplementationClass(
                                 getComponentClass(getCurrentComponent()));
                 TesterUtil.highLight(getCurrentComponent(), highlightColor);
 
             } catch (IllegalArgumentException e) {
                 log.error("unexpected exception", e); //$NON-NLS-1$
             } catch (UnsupportedComponentException e) {
                 /* This means that the component that we wish to highlight is 
                  * not supported.
                  * The component will not be highlighted
                  */
             }
         }
         
     }
     /**
      * highlights a component
      * @param compId Component
      * @return boolean succsessful?
      */
     public boolean highlightComponent(IComponentIdentifier compId) {
         final Color highlightColor = null;
         Component component = null;
 
         try {
             // lowlight old lightened
             if (getCurrentComponent() != null) {
                 AUTServerConfiguration.getInstance()
                     .getImplementationClass(
                             getComponentClass(
                                     getCurrentComponent()));
                 TesterUtil.lowLight(getCurrentComponent());
 
                 setHighLighted(false);
 
             }
             component = ComponentHandler.findComponent(compId, false, 0);
             if (component != null) {
                 setCurrentComponent(component);
                 if (getCurrentComponent() != null
                         && getCurrentComponent().isShowing()
                         && getCurrentComponent().isVisible()) {
                     
                     AUTServerConfiguration.getInstance()
                             .getImplementationClass(
                                     getComponentClass(
                                             getCurrentComponent()));
                     TesterUtil.highLight(getCurrentComponent(), highlightColor);
                     
                     setHighLighted(true);
                     return true;
                 }
             } else {
                 return false;
             }
 
         } catch (ComponentNotFoundException e) {
             log.warn(e);
         } catch (IllegalArgumentException e) {
             log.warn(e);
         } catch (UnsupportedComponentException uce) {
             log.warn(uce);
         }
         return false;
     }
     
 
     
 }
