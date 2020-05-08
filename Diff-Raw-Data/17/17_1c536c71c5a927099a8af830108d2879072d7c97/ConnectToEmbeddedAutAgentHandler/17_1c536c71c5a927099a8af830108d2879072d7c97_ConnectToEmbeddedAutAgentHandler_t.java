 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.autagent.handlers;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jubula.autagent.AutStarter;
 import org.eclipse.jubula.autagent.AutStarter.Verbosity;
 import org.eclipse.jubula.client.autagent.Activator;
 import org.eclipse.jubula.client.autagent.preferences.PreferenceInitializer;
 import org.eclipse.jubula.client.ui.actions.StartServerAction;
 import org.eclipse.jubula.client.ui.utils.ServerManager.Server;
import org.eclipse.jubula.tools.i18n.I18n;
import org.eclipse.ui.statushandlers.StatusManager;
 
 /**
  * Handler for "Connect to Embedded AUT Agent" command.
  * 
  * @author BREDEX GmbH
  * @created Jun 29, 2011
  */
 public class ConnectToEmbeddedAutAgentHandler extends AbstractHandler 
         implements IHandler {
 
     /** ID of "Connect to Embedded AUT Agent" command */
     public static final String CONNECT_TO_EMBEDDED_AGENT_CMD_ID = 
         "org.eclipse.jubula.client.autagent.commands.ConnectToEmbeddedAutAgent"; //$NON-NLS-1$
     
     /** 
      * hostname to use for starting and accessing the embedded AUT Agent 
      */
     private static final String EMBEDDED_AGENT_HOSTNAME = "localhost"; //$NON-NLS-1$
     
     /**
      * 
      * {@inheritDoc}
      */
     public Object execute(ExecutionEvent event) throws ExecutionException {
         
         if (AutStarter.getInstance().getCommunicator() == null) {
             // Embedded Agent is not running. We need to start it before
             // trying to connect to it.
             final int port = Platform.getPreferencesService().getInt(
                     Activator.PLUGIN_ID, 
                     PreferenceInitializer.PREF_EMBEDDED_AGENT_PORT, 
                     PreferenceInitializer.DEFAULT_EMBEDDED_AGENT_PORT, null);
             try {
                 AutStarter.getInstance().start(
                         port, false, Verbosity.QUIET, false);
             } catch (Exception e) {
                ExecutionException execException = new ExecutionException(
                         "An error occurred while starting the embedded AUT Agent", e); //$NON-NLS-1$
                StatusManager.getManager().handle(
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
                                I18n.getString("AUTAgent.StartCommErrorText", //$NON-NLS-1$
                                        new String[] {String.valueOf(port)}), 
                                e), 
                        StatusManager.SHOW);
                throw execException;
             }
         }
     
         StartServerAction connectToServerAction = 
             new StartServerAction(
                 new Server(EMBEDDED_AGENT_HOSTNAME, 
                     AutStarter.getInstance().getCommunicator().getLocalPort()), 
                 IAction.AS_CHECK_BOX);
         connectToServerAction.run();
         return null;
     }
 
 }
