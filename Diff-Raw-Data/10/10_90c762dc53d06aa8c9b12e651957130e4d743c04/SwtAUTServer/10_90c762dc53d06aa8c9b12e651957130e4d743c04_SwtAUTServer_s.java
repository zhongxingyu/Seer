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
 package org.eclipse.jubula.rc.swt;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.jubula.rc.common.AUTServer;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.listener.BaseAUTListener;
 import org.eclipse.jubula.rc.swt.driver.RobotFactoryConfig;
 import org.eclipse.jubula.rc.swt.driver.RobotFactorySwtImpl;
 import org.eclipse.jubula.rc.swt.driver.RobotSwtImpl;
 import org.eclipse.jubula.rc.swt.listener.AbstractAutSwtEventListener;
 import org.eclipse.jubula.rc.swt.listener.CheckListener;
 import org.eclipse.jubula.rc.swt.listener.ComponentHandler;
 import org.eclipse.jubula.rc.swt.listener.FocusTracker;
 import org.eclipse.jubula.rc.swt.listener.MappingListener;
 import org.eclipse.jubula.rc.swt.listener.RecordListener;
 import org.eclipse.jubula.rc.swt.listener.TableSelectionTracker;
 import org.eclipse.jubula.tools.constants.AUTServerExitConstants;
 import org.eclipse.jubula.tools.constants.DebugConstants;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Listener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
 * The AutServer controling the AUT. <br>
  * A quasi singleton: the instance is created from main(). <br>
  * Expected arguments to main are, see also
  * StartAUTServerCommand.createCmdArray():
  * <ul>
  * <li>The name of host the Client is running on, must be InetAddress conform.</li>
  * <li>The port the Client is listening to.</li>
  * <li>The main class of the AUT.</li>
  * <li>Any further arguments are interpreted as arguments to the AUT.</li>
  * <ul>
 * When a connetion to the JubulaClient could made, any errors will send as a
  * message to the JubulaClient.
  * 
  * Changing the mode to OBJECT_MAPPING results in installing an AWTEventListener
  * (an instance of <code>MappingListener</code>). For simplification the virtual
  * machine is closed without sending a message to the client when an error
 * occurs during the installation of the SWTEventListener. The exitcode is the
 * appopriate EXIT_* constant
  * 
  * Changing the mode to TESTING removes the installed MappingListener.
  * 
  * @author BREDEX GmbH
  * @created 20.04.2006
  */
 public class SwtAUTServer extends AUTServer {
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(SwtAUTServer.class);
     /** the aut display */
     private Display m_display = null;
     
     /** the robot */
     private RobotSwtImpl m_robot = null;
     
     /** 
      * private constructor
      * instantiates the listeners
      */
     public SwtAUTServer() {
         super(new MappingListener(), new RecordListener(), new CheckListener());
     }
 
     /**
      * Starts the AUTServer in its own Thread with its own ClassLoader.
      */
     public void startAUT() {
         if (isRcpAccessible()) {
             return;
         }
         super.startAUT();
         // wait until aut is started
         getAutDisplay();
         if (!isAgentSet()) { //already done in AutServer if Agent is in use
             super.addToolKitEventListenerToAUT();
         }
     }
     
     /**
      * @return the aut display.
      */
     public Display getAutDisplay() {
         if (isRcpAccessible()) {
             while (m_display == null) {
                 m_display = Display.findDisplay(Thread.currentThread());
                 try {
                     Thread.sleep(50);
                 } catch (InterruptedException e) {
                     // OK here!
                 }
             }
         } else {
             return getSwtAutDisplay();
         }
         return m_display;
     }
 
     /**
      * @return Display in case of SWT-Application
      */
     private Display getSwtAutDisplay() {
         final ClassLoader oldCL = Thread.currentThread()
             .getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(getAutThread()
                 .getContextClassLoader());
             while (m_display == null) {
                 m_display = Display.findDisplay(getAutThread());
                 try {
                     Thread.sleep(50);
                 } catch (InterruptedException e) {
                     // OK here!
                 }
             }
             return m_display;
         } finally {
             if (m_display == null) {
                 LOG.error("SWT Display not found"); //$NON-NLS-1$
             }
             Thread.currentThread().setContextClassLoader(oldCL);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void startToolkitThread() {
         // do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     protected void addToolkitEventListeners() {
         // install the component handler
         addToolkitEventListener(new ComponentHandler());
         // install the focus tracker
         addToolkitEventListener(new FocusTracker());
         // install the table selection tracker
         addToolkitEventListener(TableSelectionTracker.getInstance());
     }
 
     /**
      * {@inheritDoc}
      * @param listener
      */
     protected void addToolkitEventListener(final BaseAUTListener listener) {
         try {
             getAutDisplay().syncExec(new Runnable() {
                 public void run() {
                     long[] mask = listener.getEventMask();
                     for (int i = 0; i < mask.length; i++) {
                         getAutDisplay().addFilter((int)mask[i], 
                                 (Listener)listener);
                     }
                     if (LOG.isInfoEnabled()) {
                         LOG.info("installing SWTEventListener " //$NON-NLS-1$ 
                                 + listener.toString());
                     }
                 }
             });
         } catch (NullPointerException se) {
             // no permission to remove an SWTEventListener,
             // should not occur, because addSWTEventListener() should be called 
             // first. But just in case, close the vm
             LOG.error(DebugConstants.ERROR, se);
             System.exit(AUTServerExitConstants
                     .EXIT_SECURITY_VIOLATION_AWT_EVENT_LISTENER);
         }
     }
 
     /**
      * {@inheritDoc}
      * @param listener
      */
     protected void removeToolkitEventListener(final BaseAUTListener listener) {
         try {
             getAutDisplay().syncExec(new Runnable() {
                 public void run() {
                     for (int i = 0; 
                         i < ((AbstractAutSwtEventListener)listener)
                             .getEventMask().length; i++) {
                         
                         getAutDisplay().removeFilter(
                                 (int)((AbstractAutSwtEventListener)
                                         listener).getEventMask()[i], 
                                     (AbstractAutSwtEventListener)listener); 
                         if (LOG.isInfoEnabled()) {
                             LOG.info("uninstalling SWTEventListener " //$NON-NLS-1$ 
                                     + listener.toString());
                         }
                     }
                 }
             });
         } catch (NullPointerException se) {
             // no permission to remove an SWTEventListener,
             // should not occur, because addSWTEventListener() should be called 
             // first. But just in case, close the vm
             LOG.error(DebugConstants.ERROR, se);
             System.exit(AUTServerExitConstants
                     .EXIT_SECURITY_VIOLATION_AWT_EVENT_LISTENER);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void startTasks() throws ExceptionInInitializerError, 
         InvocationTargetException, NoSuchMethodException {
 
         super.invokeAUT();
         if (getCommunicator() != null) {
             getCommunicator().close();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected boolean closeAUT() {
         // FIXME Clemens: not implemented yet
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public synchronized IRobot getRobot() {
         if (m_robot == null) {
             RobotFactorySwtImpl robotFactory = 
                 new RobotFactoryConfig().getRobotFactory();
             m_robot = (RobotSwtImpl) robotFactory.getRobot();
         }
         
         return m_robot;
     }
 
     /**
      * <HR NOSHADE><CENTER><FONT color="#FF0000"><b>ONLY TO USE FOR SWT-JUNIT TESTS<br>
      * AND<br>FOR "RcpAccessor" PLUG-IN !!!</b></FONT></CENTER><HR NOSHADE>
      * @param display the display to set
      */
     public void setDisplay(Display display) {
         m_display = display;
     }
 }
