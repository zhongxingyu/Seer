 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf;
 
 import java.io.InputStream;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.tcf.te.runtime.interfaces.IDisposable;
 import org.eclipse.tcf.te.runtime.processes.ProcessLauncher;
 import org.eclipse.tcf.te.runtime.processes.ProcessOutputReaderThread;
 import org.eclipse.tcf.te.runtime.utils.Host;
 
 /**
  * TCF tests agent launcher implementation.
  */
 public class AgentLauncher extends ProcessLauncher implements IDisposable {
 	// The path of the agent to launch
 	private final IPath path;
 	// The process handle
 	private Process process;
 	// The process output reader
 	private ProcessOutputReaderThread outputReader;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param path The agent path. Must not be <code>null</code>.
 	 */
 	public AgentLauncher(IPath path) {
 		super(null, null, 0);
 		Assert.isNotNull(path);
 		this.path = path;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.interfaces.IDisposable#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (process != null) {
 			process.destroy();
 			process = null;
 		}
 	}
 
 	/**
 	 * Returns the process handle.
 	 *
 	 * @return The process handle or <code>null</code>.
 	 */
 	public Process getProcess() {
 		return process;
 	}
 
 	/**
 	 * Returns the process output reader.
 	 *
 	 * @return The process output reader or <code>null</code>.
 	 */
 	public ProcessOutputReaderThread getOutputReader() {
 		return outputReader;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.processes.ProcessLauncher#launch()
 	 */
 	@Override
 	public void launch() throws Throwable {
 		IPath dir = path.removeLastSegments(1);
		String cmd = Host.isWindowsHost() ? path.toOSString() : "./" + path.lastSegment(); //$NON-NLS-1$
 		process = Runtime.getRuntime().exec(new String[] { cmd, "-S" }, null, dir.toFile()); //$NON-NLS-1$
 
 		// Launch the process output reader
 		outputReader = new ProcessOutputReaderThread(null, new InputStream[] { process.getInputStream() });
 		outputReader.start();
 	}
 
 }
