 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.terminals.local.types;
 
 import java.io.File;
 
 import org.eclipse.cdt.utils.pty.PTY;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.services.interfaces.ITerminalServiceOutputStreamMonitorListener;
 import org.eclipse.tcf.te.runtime.services.interfaces.constants.ILineSeparatorConstants;
 import org.eclipse.tcf.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
 import org.eclipse.tcf.te.runtime.utils.Host;
 import org.eclipse.tcf.te.ui.terminals.internal.SettingsStore;
 import org.eclipse.tcf.te.ui.terminals.process.ProcessSettings;
 import org.eclipse.tcf.te.ui.terminals.types.AbstractConnectorType;
 import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
 import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
 import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
 
 /**
  * Streams terminal connector type implementation.
  */
 @SuppressWarnings("restriction")
 public class LocalConnectorType extends AbstractConnectorType {
 
 	/**
 	 * Returns the default shell to launch. Looks at the environment
 	 * variable "SHELL" first before assuming some default default values.
 	 *
 	 * @return The default shell to launch.
 	 */
 	private final File defaultShell() {
		String shell = System.getenv("SHELL"); //$NON-NLS-1$
 		if (shell == null) {
			shell = Host.isWindowsHost() ? "cmd.exe" : "/bin/sh"; //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return new File(shell);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.terminals.interfaces.IConnectorType#createTerminalConnector(org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
 	public ITerminalConnector createTerminalConnector(IPropertiesContainer properties) {
 		Assert.isNotNull(properties);
 
 		// Check for the terminal connector id
 		String connectorId = properties.getStringProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID);
 		if (connectorId == null) connectorId = "org.eclipse.tcf.te.ui.terminals.local.LocalConnector"; //$NON-NLS-1$
 
 		// Extract the process properties using defaults
 		String image;
 		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_PATH) ||
 						properties.getStringProperty(ITerminalsConnectorConstants.PROP_PROCESS_PATH) == null){
 
 			File defaultShell = defaultShell();
 			image = defaultShell.isAbsolute() ? defaultShell.getAbsolutePath() : defaultShell.getPath();
 		} else {
 			image = properties.getStringProperty(ITerminalsConnectorConstants.PROP_PROCESS_PATH);
 		}
 		boolean localEcho;
 		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_LOCAL_ECHO) ||
 						properties.getStringProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO) == null){
 			localEcho = Host.isWindowsHost();
 		} else {
 			localEcho = properties.getBooleanProperty(ITerminalsConnectorConstants.PROP_LOCAL_ECHO);
 		}
 
 		String lineSeparator;
 		if (!properties.containsKey(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR) ||
 						properties.getStringProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR) == null){
 			lineSeparator = Host.isWindowsHost() ? ILineSeparatorConstants.LINE_SEPARATOR_CRLF : ILineSeparatorConstants.LINE_SEPARATOR_LF;
 		} else {
 			lineSeparator = properties.getStringProperty(ITerminalsConnectorConstants.PROP_LINE_SEPARATOR);
 		}
 
 		String arguments = properties.getStringProperty(ITerminalsConnectorConstants.PROP_PROCESS_ARGS);
 		Process process = (Process)properties.getProperty(ITerminalsConnectorConstants.PROP_PROCESS_OBJ);
 		PTY pty = (PTY)properties.getProperty(ITerminalsConnectorConstants.PROP_PTY_OBJ);
 		ITerminalServiceOutputStreamMonitorListener[] stdoutListeners = (ITerminalServiceOutputStreamMonitorListener[])properties.getProperty(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS);
 		ITerminalServiceOutputStreamMonitorListener[] stderrListeners = (ITerminalServiceOutputStreamMonitorListener[])properties.getProperty(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS);
 		String workingDir = properties.getStringProperty(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR);
 
 		String[] envp = null;
 		if (properties.containsKey(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT) &&
 						properties.getProperty(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT) != null &&
 						properties.getProperty(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT) instanceof String[]){
 			envp = (String[])properties.getProperty(ITerminalsConnectorConstants.PROP_PROCESS_ENVIRONMENT);
 		}
 
 		Assert.isTrue(image != null || process != null);
 
 		// Construct the terminal settings store
 		ISettingsStore store = new SettingsStore();
 
 		// Construct the process settings
 		ProcessSettings processSettings = new ProcessSettings();
 		processSettings.setImage(image);
 		processSettings.setArguments(arguments);
 		processSettings.setProcess(process);
 		processSettings.setPTY(pty);
 		processSettings.setLocalEcho(localEcho);
 		processSettings.setLineSeparator(lineSeparator);
 		processSettings.setStdOutListeners(stdoutListeners);
 		processSettings.setStdErrListeners(stderrListeners);
 		processSettings.setWorkingDir(workingDir);
 		processSettings.setEnvironment(envp);
 
 		// And save the settings to the store
 		processSettings.save(store);
 
 		// Construct the terminal connector instance
 		ITerminalConnector connector = TerminalConnectorExtension.makeTerminalConnector(connectorId);
 		if (connector != null) {
 			// Apply default settings
 			connector.makeSettingsPage();
 			// And load the real settings
 			connector.load(store);
 		}
 
 		return connector;
 	}
 }
