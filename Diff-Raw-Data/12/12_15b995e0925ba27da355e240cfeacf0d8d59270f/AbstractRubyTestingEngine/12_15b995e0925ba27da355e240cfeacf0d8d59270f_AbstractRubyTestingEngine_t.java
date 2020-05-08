 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ruby.testing.internal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.testing.AbstractTestingEngine;
 import org.eclipse.dltk.testing.DLTKTestingPlugin;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.osgi.framework.Bundle;
 
 public abstract class AbstractRubyTestingEngine extends AbstractTestingEngine {
 
 	protected static final String RUBY_TESTING_PORT = "RUBY_TESTING_PORT"; //$NON-NLS-1$
 	protected static final String RUBY_TESTING_PATH = "RUBY_TESTING_PATH"; //$NON-NLS-1$
 
 	protected static final String RUNNER_PATH = "/testing/"; //$NON-NLS-1$
 
 	protected Bundle getBundle() {
 		return RubyTestingPlugin.getDefault().getBundle();
 	}
 
 	protected File getRunnerFile(final Bundle bundle, final String runnerPath,
 			final String runnerName) throws CoreException {
 		URL runnerScript = bundle.getEntry(runnerPath + runnerName);
 		if (runnerScript == null) {
 			final String msg = NLS.bind(Messages.Delegate_runnerNotFound,
 					runnerName);
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyTestingPlugin.PLUGIN_ID, msg, null));
 		}
 		try {
 			runnerScript = FileLocator.toFileURL(runnerScript);
 		} catch (IOException e) {
 			final String msg = NLS.bind(
 					Messages.Delegate_errorExtractingRunner, runnerName);
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyTestingPlugin.PLUGIN_ID, msg, e));
 		}
 		try {
			return new File(runnerScript.toURI());
 		} catch (IllegalArgumentException e) {
 			final String msg = NLS
 					.bind(Messages.Delegate_internalErrorExtractingRunner,
 							runnerName);
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyTestingPlugin.PLUGIN_ID, msg, e));
 		} catch (URISyntaxException e) {
 			final String msg = NLS
 					.bind(Messages.Delegate_internalErrorExtractingRunner,
 							runnerName);
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyTestingPlugin.PLUGIN_ID, msg, e));
 		}
 	}
 
 	protected boolean isDevelopmentMode(InterpreterConfig config,
 			String runnerName) {
 		return config.getScriptFilePath() != null
 				&& config.getScriptFilePath().lastSegment().equals(runnerName);
 	}
 
 	/**
 	 * Returns a free port number on localhost, or -1 if unable to find a free
 	 * port.
 	 * 
 	 * @return a free port number or -1
 	 */
 	private static int findFreePort() {
 		ServerSocket socket = null;
 		try {
 			socket = new ServerSocket(0);
 			return socket.getLocalPort();
 		} catch (IOException e) {
 		} finally {
 			if (socket != null) {
 				try {
 					socket.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return -1;
 	}
 
 	protected int allocatePort() throws CoreException {
 		int port = findFreePort();
 		if (port == -1) {
 			informAndAbort(
 					"No socket available", //$NON-NLS-1$
 					null,
 					ScriptLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
 		}
 		return port;
 	}
 
 	protected void informAndAbort(String message, Throwable exception, int code)
 			throws CoreException {
 		IStatus status = new Status(IStatus.INFO, RubyTestingPlugin.PLUGIN_ID,
 				code, message, exception);
 		if (showStatusMessage(status)) {
 			// Status message successfully shown
 			// -> Abort with INFO exception
 			// -> Worker.run() will not write to log
 			throw new CoreException(status);
 		} else {
 			// Status message could not be shown
 			// -> Abort with original exception
 			// -> Will write WARNINGs and ERRORs to log
 			throw new CoreException(new Status(IStatus.ERROR,
 					RubyTestingPlugin.PLUGIN_ID, code, message, exception));
 		}
 	}
 
 	protected boolean showStatusMessage(final IStatus status) {
 		final boolean[] success = new boolean[] { false };
 		getDisplay().syncExec(new Runnable() {
 			public void run() {
 				Shell shell = DLTKTestingPlugin.getActiveWorkbenchShell();
 				if (shell == null)
 					shell = getDisplay().getActiveShell();
 				if (shell != null) {
 					MessageDialog.openInformation(shell,
 							"Problems Launching Unit Tests", //$NON-NLS-1$
 							status.getMessage());
 					success[0] = true;
 				}
 			}
 		});
 		return success[0];
 	}
 
 	private Display getDisplay() {
 		Display display;
 		display = Display.getCurrent();
 		if (display == null)
 			display = Display.getDefault();
 		return display;
 	}
 
 	/**
 	 * @param info
 	 * @param string
 	 * @return
 	 */
 	protected static IStatus createStatus(int severity, String message) {
 		return new Status(severity, RubyTestingPlugin.PLUGIN_ID, message);
 	}
 
 }
