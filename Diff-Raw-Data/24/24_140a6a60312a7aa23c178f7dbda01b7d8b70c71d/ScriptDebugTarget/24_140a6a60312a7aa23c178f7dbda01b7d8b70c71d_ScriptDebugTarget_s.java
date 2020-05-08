 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 /**
  * 
  */
 package org.eclipse.dltk.internal.debug.core.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IBreakpointManager;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IMemoryBlock;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTargetListener;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 import org.eclipse.dltk.debug.core.model.MethodEntryManager;
 
 public class ScriptDebugTarget extends ScriptDebugElement implements
 		IScriptDebugTarget, IScriptThreadManagerListener {
 
 	private static final String ON_EXIT = "suspendOnExit";
 
 	private static final String SUSPEND_ON_ENTRY = "suspendOnEntry";
 
 	private static final int THREAD_TERMINATION_TIMEOUT = 5000; // 5 seconds
 
 	private static final String SUSPEND_ON_EXIT = ON_EXIT;
 
 	private final ListenerList listeners;
 
 	private IScriptDebugTargetStreamManager manager;
 
 	private final IProcess process;
 
 	private final ILaunch launch;
 
 	private String name;
 
 	private boolean suspendOnMethodEntry;
 
 	private boolean suspendOnMethodExit;
 
 	private boolean disconnected;
 
 	private final IScriptThreadManager threadManager;
 
 	private final ScriptBreakpointManager breakpointManager;
 
 	private final IDbgpService dbgpService;
 	private final String sessionId;
 
 	private final String mondelId;
 
 	private static int findFirstNonEmptyScriptLine(IFile file)
 			throws CoreException {
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new InputStreamReader(file
 					.getContents(), file.getCharset()));
 
 			int lineNumber = 1;
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				if (line.length() > 0) {
 					break;
 				}
 
 				++lineNumber;
 			}
 
 			return lineNumber;
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 
 		return -1;
 	}
 
 	private URI scriptUri;
 	private int scriptNonEmptyLine = -1;
 
 	private boolean isRemote;
 
 	private static final String ATTR_PROJECT = "project";
 	private static final String ATTR_SCRIPT = "mainScript";
 
 	private void setupScriptParameters(ILaunchConfiguration configuration)
 			throws CoreException {
 		final String projectName = configuration.getAttribute(ATTR_PROJECT,
 				(String) null);
 
 		final String scriptName = configuration.getAttribute(ATTR_SCRIPT,
 				(String) null);
 
 		if (projectName != null && scriptName != null) {
 			final IProject project = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(projectName);
 
 			if (project != null) {
 				final IResource resource = project.findMember(scriptName);
 
 				if (resource instanceof IFile) {
 					scriptUri = ScriptLineBreakpoint.makeUri((IFile) resource);			
 					scriptNonEmptyLine = findFirstNonEmptyScriptLine((IFile) resource);
 				}
 			}
 		} else {
 			isRemote = true;
 		}
 			
 	}
 
 	static WeakHashMap targets = new WeakHashMap();
 
 	public static List getAllTargets() {
 		return new ArrayList(targets.keySet());
 	}
 
 	public ScriptDebugTarget(String modelId, IDbgpService dbgpService,
 			String sessionId, ILaunch launch, IProcess process)
 			throws CoreException {
 
 		setupScriptParameters(launch.getLaunchConfiguration());
 
 		this.mondelId = modelId;
 
 		this.listeners = new ListenerList();
 
 		this.process = process;
 		this.launch = launch;
 
 		this.threadManager = new /* New */ScriptThreadManager(this);
 		this.sessionId = sessionId;
 		this.dbgpService = dbgpService;
 		this.dbgpService.registerAcceptor(this.sessionId, this.threadManager);
 
 		this.disconnected = false;
 
 		this.breakpointManager = new ScriptBreakpointManager(this);
 
 		this.threadManager.addListener(this);
 
 		DebugEventHelper.fireCreateEvent(this);
 		targets.put(this, "");
 		boolean suspendOnMethodEntry2 = MethodEntryManager
 				.isSuspendOnMethodEntry();
 		boolean suspendOnMethodExit2 = MethodEntryManager
 				.isSuspendOnMethodExit();
 		setSuspendOnMethodEntry(suspendOnMethodEntry2);
 		setSuspendOnMethodExit(suspendOnMethodExit2);
 	}
 
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	public IDebugTarget getDebugTarget() {
 		return this;
 	}
 
 	public String getModelIdentifier() {
 		return mondelId;
 	}
 
 	public ILaunch getLaunch() {
 		return launch;
 	}
 
 	// IDebugTarget
 	public IProcess getProcess() {
 		return process;
 	}
 
 	public boolean hasThreads() throws DebugException {
 		return threadManager.hasThreads();
 	}
 
 	public IThread[] getThreads() throws DebugException {
 		return threadManager.getThreads();
 	}
 
 	public String getName() throws DebugException {
 		return name;
 	}
 
 	// ITerminate
 	public boolean canTerminate() {
 		return threadManager.canTerminate();
 	}
 
 	public boolean isTerminated() {
 		return threadManager.isTerminated();
 	}
 
 	protected boolean waitTermianted() {
 		final int WAIT_CHUNK = 400; // 200 ms
 
 		int all = 0;
 		while (all < THREAD_TERMINATION_TIMEOUT) {
 			try {
 				Thread.sleep(WAIT_CHUNK);
 				all += WAIT_CHUNK;
 			} catch (InterruptedException e) {
 
 			}
 			if (threadManager.isTerminated()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void terminate() throws DebugException {
 		dbgpService.unregisterAcceptor(sessionId);
 		threadManager.terminate();
 		if (waitTermianted()) {
 			threadManager.removeListener(this);
 
 			IBreakpointManager manager = DebugPlugin.getDefault()
 					.getBreakpointManager();
 
 			manager.removeBreakpointListener(this);
 			manager.removeBreakpointManagerListener(breakpointManager);
 
 			DebugEventHelper.fireTerminateEvent(this);
 		}
 	}
 
 	// ISuspendResume
 	public boolean canSuspend() {
 		return threadManager.canSuspend();
 	}
 
 	public boolean isSuspended() {
 		return threadManager.isSuspended();
 	}
 
 	public void suspend() throws DebugException {
 		threadManager.suspend();
 	}
 
 	public boolean canResume() {
 		return threadManager.canResume();
 	}
 
 	public void resume() throws DebugException {
 		threadManager.resume();
 	}
 
 	// IDisconnect
 	public boolean canDisconnect() {
 		// Detach feature support!!!
 		return false;
 	}
 
 	public void disconnect() throws DebugException {
 		disconnected = true;
 	}
 
 	public boolean isDisconnected() {
 		return disconnected;
 	}
 
 	// IMemoryBlockRetrieval
 	public boolean supportsStorageRetrieval() {
 		return false;
 	}
 
 	public IMemoryBlock getMemoryBlock(long startAddress, long length)
 			throws DebugException {
 		return null;
 	}
 
 	public IScriptVariable findVariable(String variableName)
 			throws DebugException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// Request timeout
 	public int getRequestTimeout() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public void setRequestTimeout(int timeout) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// IBreakpointListener
 	public void breakpointAdded(IBreakpoint breakpoint) {
 		breakpointManager.breakpointAdded(breakpoint);
 	}
 
 	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
 		breakpointManager.breakpointChanged(breakpoint, delta);
 	}
 
 	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
 		breakpointManager.breakpointRemoved(breakpoint, delta);
 	}
 
 	// Streams
 	public IScriptDebugTargetStreamManager getStreamManager() {
 		return manager;
 	}
 
 	public void setStreamManager(IScriptDebugTargetStreamManager manager) {
 		this.manager = manager;
 	}
 
 	// IDbgpThreadManagerListener
 	public void threadAccepted(IScriptThread thread, boolean first) {
 		if (first) {
 			breakpointManager.setupDeferredBreakpoints();
 
 			IBreakpointManager manager = DebugPlugin.getDefault()
 					.getBreakpointManager();
 
 			manager.addBreakpointListener(this);
 			manager.addBreakpointManagerListener(breakpointManager);
 
 			// DebugEventHelper.fireCreateEvent(this);
 			fireTargetInitialized();
 		}
 		try {
 			initSuspends(thread);
 		} catch (DbgpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void initSuspends(IScriptThread thread) throws DbgpException {
 		thread.getDbgpSession().getCoreCommands().setProperty(SUSPEND_ON_ENTRY,
 				-1, Boolean.toString(suspendOnMethodEntry));
 		thread.getDbgpSession().getCoreCommands().setProperty(SUSPEND_ON_EXIT,
 				-1, Boolean.toString(suspendOnMethodExit));
 	}
 
 	public void allThreadsTerminated() {
 		try {
 			terminate();
 		} catch (DebugException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public String toString() {
 		return "Debugging engine (id = " + this.sessionId + ")";
 	}
 
 	// IScriptDebugTarget
 	public void runToLine(URI uri, int lineNumber) throws DebugException {
 		breakpointManager.setBreakpointUntilFirstSuspend(uri, lineNumber);
 		resume();
 	}
 
 	public boolean isInitialized() {
 		return !threadManager.isWaitingForThreads();
 	}
 
 	protected void fireTargetInitialized() {
 		Object[] list = listeners.getListeners();
 		for (int i = 0; i < list.length; ++i) {
 			((IScriptDebugTargetListener) list[i]).targetInitialized();
 		}
 	}
 
 	public void addListener(IScriptDebugTargetListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(IScriptDebugTargetListener listener) {
 		listeners.remove(listener);
 	}
 
 	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
 		return breakpointManager.supportsBreakpoint(breakpoint);
 	}
 
 	public boolean isSuspendOnMethodEntry() {
 		return suspendOnMethodEntry;
 	}
 
 	public boolean isSuspendOnMethodExit() {
 		return suspendOnMethodExit;
 	}
 
 	public void setSuspendOnMethodEntry(boolean suspend) {
 		boolean ok = true;
 		if (!isDisconnected()) {
 			IThread[] threads;
 			try {
 				threads = this.getThreads();
 				l2: for (int a = 0; a < threads.length; a++) {
 					IScriptThread scr = (IScriptThread) threads[a];
 					try {
 						scr.getDbgpSession().getCoreCommands()
 								.setProperty(SUSPEND_ON_ENTRY, -1,
 										Boolean.toString(suspend));
 					} catch (DbgpException e) {
 						e.printStackTrace();
 						ok = false;
 						break l2;
 					}
 				}
 
 			} catch (DebugException e) {
 				e.printStackTrace();
 			}
 		}
 		if (ok)
 			this.suspendOnMethodEntry = suspend;
 	}
 
 	public void setSuspendOnMethodExit(boolean suspend) {
 		boolean ok = true;
 		if (!isDisconnected()) {
 			IThread[] threads;
 			try {
 				threads = this.getThreads();
 				l2: for (int a = 0; a < threads.length; a++) {
 					IScriptThread scr = (IScriptThread) threads[a];
 					try {
 						scr.getDbgpSession().getCoreCommands().setProperty(
 								ON_EXIT, -1, Boolean.toString(suspend));
 					} catch (DbgpException e) {
 						e.printStackTrace();
 						ok = false;
 						break l2;
 					}
 				}
 			} catch (DebugException e) {
 				e.printStackTrace();
 			}
 		}
 		if (ok)
 			this.suspendOnMethodExit = suspend;
 	}
 }
