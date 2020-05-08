 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.releng;
 
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.eclipse.ui.plugin.*;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.osgi.framework.BundleContext;
 
 /**
  * The main plugin class to be used in the desktop.
  */
 public class ReleaseEngineeringPlugin extends AbstractUIPlugin {
     private static ReleaseEngineeringPlugin plugin;
 
     public static final String kPluginID= "org.eclipse.imp.releng";
 
     /**
      * Returns the shared instance.
      */
     public static ReleaseEngineeringPlugin getInstance() {
         return plugin;
     }
 
    private MessageConsoleStream fMsgStream;
 
     /**
      * The constructor.
      */
     public ReleaseEngineeringPlugin() {
         plugin= this;
     }
 
     /**
      * This method is called upon plug-in activation
      */
     public void start(BundleContext context) throws Exception {
         super.start(context);
 
         MessageConsole myConsole= findConsole();
        fMsgStream= myConsole.newMessageStream();
     }
 
     /**
      * This method is called when the plug-in is stopped
      */
     public void stop(BundleContext context) throws Exception {
         super.stop(context);
         plugin= null;
     }
 
     private static final String RELEASE_CONSOLE= "Release Engineering";
 
     private MessageConsole findConsole() {
         MessageConsole myConsole= null;
         final IConsoleManager consoleManager= ConsolePlugin.getDefault().getConsoleManager();
         IConsole[] consoles= consoleManager.getConsoles();
         for(int i= 0; i < consoles.length; i++) {
             IConsole console= consoles[i];
             if (console.getName().equals(RELEASE_CONSOLE))
                 myConsole= (MessageConsole) console;
         }
         if (myConsole == null) {
             myConsole= new MessageConsole(RELEASE_CONSOLE, null);
             consoleManager.addConsoles(new IConsole[] { myConsole });
         }
         consoleManager.showConsoleView(myConsole);
         return myConsole;
     }
 
     public static MessageConsoleStream getMsgStream() {
        return getInstance().fMsgStream;
     }
 
     public static void logError(Exception e) {
         final Status status= new Status(IStatus.ERROR, ReleaseEngineeringPlugin.kPluginID, 0, e.getMessage(), e);
         ReleaseEngineeringPlugin.getInstance().getLog().log(status);
     }
 
     /**
      * Returns an image descriptor for the image file at the given plug-in relative path.
      * 
      * @param path
      *            the path
      * @return the image descriptor
      */
     public static ImageDescriptor getImageDescriptor(String path) {
         return AbstractUIPlugin.imageDescriptorFromPlugin(kPluginID, path);
     }
 }
