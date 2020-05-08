 package org.eclipse.dltk.debug.internal.core.model;
 
 import java.net.URI;
 
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IBreakpointListener;
 import org.eclipse.debug.core.IBreakpointManager;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.dltk.dbgp.breakpoints.DbgpBreakpointConfig;
 import org.eclipse.dltk.dbgp.commands.IDbgpBreakpointCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 
 public class DbgpBreakpointManager implements IBreakpointListener {
 	private ScriptThreadManager threadManager;
 
 	protected static IBreakpointManager getBreakpointManager() {
 		return DebugPlugin.getDefault().getBreakpointManager();
 	}
 
 	private static IDbgpBreakpointCommands getBreakpointCommands(
 			IScriptThread thread) {
 		return thread.getDbgpSession().getCoreCommands();
 	}
 
 	public static boolean supportsBreakpoint(IBreakpoint breakpoint) {
 		if (breakpoint instanceof IScriptBreakpoint) {
 			return true;
 		}
 
 		return false;
 	}
 
 	protected static void addBreakpoint(IDbgpBreakpointCommands commands,
 			IScriptBreakpoint breakpoint) throws CoreException, DbgpException {
 
 		if (breakpoint instanceof IScriptLineBreakpoint) {
 			IScriptLineBreakpoint b = (IScriptLineBreakpoint) breakpoint;
 
 			boolean enabled = b.isEnabled()
 					&& getBreakpointManager().isEnabled();
 
 			DbgpBreakpointConfig config = new DbgpBreakpointConfig(enabled, b
 					.getHitValue(), b.getHitCondition());
 
 			String id = commands.setLineBreakpoint(b.getResourceURI(), b
 					.getLineNumber(), config);
 
 			b.setIdentifier(id);
 
 			// TODO: why?
 			b.setHitValue(config.getHitValue());
 			b.setHitCondition(config.getHitCondition());
 		}
 	}
 
 	protected void addBreakpoint(IBreakpoint breakpoint) throws CoreException {
 		if (!supportsBreakpoint(breakpoint)) {
 			return;
 		}
 
 		IThread[] threads = threadManager.getThreads();
 		for (int i = 0; i < threads.length; ++i) {
 			try {
 				addBreakpoint(
 						getBreakpointCommands((IScriptThread) threads[i]),
 						(IScriptBreakpoint) breakpoint);
 			} catch (DbgpException e) {
 				breakpoint.delete();
 			}
 		}
 	}
 
 	protected static void changeBreakpoint(IDbgpBreakpointCommands commands,
 			IScriptBreakpoint breakpoint) throws DbgpException, CoreException {
 		boolean enabled = breakpoint.isEnabled()
 				&& getBreakpointManager().isEnabled();
 
 		IScriptBreakpoint b = (IScriptBreakpoint) breakpoint;
 
 		int hitValue = b.getHitValue();
 		int hitCondition = b.getHitCondition();
 		DbgpBreakpointConfig config = new DbgpBreakpointConfig(enabled,
 				hitValue, hitCondition);
 		commands.updateBreakpoint(b.getIdentifier(), config);
 	}
 
 	protected void changeBreakpoint(IBreakpoint breakpoint)
 			throws CoreException {
 		if (!supportsBreakpoint(breakpoint)) {
 			return;
 		}
 
 		IThread[] threads = threadManager.getThreads();
 		for (int i = 0; i < threads.length; ++i) {
 			try {
 				changeBreakpoint(
 						getBreakpointCommands((IScriptThread) threads[i]),
 						(IScriptBreakpoint) breakpoint);
 			} catch (DbgpException e) {
 				breakpoint.delete();
 			}
 		}
 	}
 
 	protected static void removeBreakpoint(IDbgpBreakpointCommands commands,
 			IScriptBreakpoint breakpoint) throws DbgpException {
 		IScriptBreakpoint b = (IScriptBreakpoint) breakpoint;
 		commands.removeBreakpoint(b.getIdentifier());
 	}
 
 	protected void removeBreakpoint(IBreakpoint breakpoint)
 			throws DbgpException {
 		if (!supportsBreakpoint(breakpoint)) {
 			return;
 		}
 
 		IThread[] threads = threadManager.getThreads();
 		for (int i = 0; i < threads.length; ++i) {
 			removeBreakpoint(getBreakpointCommands((IScriptThread) threads[i]),
 					(IScriptBreakpoint) breakpoint);
 		}
 	}
 
 	public DbgpBreakpointManager(ScriptThreadManager manager) {
 		if (manager == null) {
 			throw new IllegalArgumentException();
 		}
 
 		this.threadManager = manager;
 	}
 
 	public void setupDeferredBreakpoints(IScriptThread thread)
 			throws CoreException {
 		IBreakpoint[] breakpoints = getBreakpointManager().getBreakpoints(
 				ScriptModelConstants.MODEL_ID);
 
 		for (int i = 0; i < breakpoints.length; i++) {
 			IBreakpoint breakpoint = breakpoints[i];
 
 			if (supportsBreakpoint(breakpoint)) {
 				try {
 					addBreakpoint(getBreakpointCommands(thread),
 							(IScriptBreakpoint) breakpoint);
 				} catch (DbgpException e) {
 					breakpoint.delete();
 				}
 			}
 		}
 	}
 
 	public void setupTemporaryBreakpoint(URI uri, int lineNumber)
 			throws DbgpException, CoreException {
 
 		DbgpBreakpointConfig config = new DbgpBreakpointConfig(true, -1, -1,
				true);
 
 		IThread[] threads = threadManager.getThreads();
 		for (int i = 0; i < threads.length; ++i) {
 			IDbgpBreakpointCommands commands = getBreakpointCommands((IScriptThread) threads[i]);
 			commands.setLineBreakpoint(uri, lineNumber, config);
 		}
 	}
 
 	// IBreakpointListener
 	public void breakpointAdded(IBreakpoint breakpoint) {
 		try {
 			addBreakpoint(breakpoint);
 		} catch (CoreException e) {
 			DLTKDebugPlugin.log(e);
 		}
 	}
 
 	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
 		try {
 			changeBreakpoint(breakpoint);
 		} catch (CoreException e) {
 			DLTKDebugPlugin.log(e);
 		}
 	}
 
 	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
 		try {
 			removeBreakpoint(breakpoint);
 		} catch (DbgpException e) {
 			DLTKDebugPlugin.log(e);
 		}
 	}
 }
