 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IBreakpointsListener;
 import org.eclipse.debug.core.IDebugEventSetListener;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchListener;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.dltk.dbgp.breakpoints.IDbgpBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpointListener;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.viewers.ILabelProvider;
 
 public class ScriptDebugOptionsManager implements IDebugEventSetListener,
 		IPropertyChangeListener, IScriptBreakpointListener, ILaunchListener,
 		IBreakpointsListener {
 
 	private static ScriptDebugOptionsManager instance;
 
 	private static ILabelProvider fLabelProvider = DebugUITools
 			.newDebugModelPresentation();
 
 	private static interface IBreakpointUpdater {
 		void update(IScriptBreakpoint breakpoint) throws CoreException;
 	}
 
 	protected void updateBreakpoints(final IBreakpoint[] breakpoints,
 			final IBreakpointUpdater updater) {
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				for (int i = 0; i < breakpoints.length; i++) {
 					IBreakpoint breakpoint = breakpoints[i];
 					if (breakpoint instanceof IScriptBreakpoint) {
 						try {
 							updater.update((IScriptBreakpoint) breakpoint);
 						} catch (CoreException e) {
 							DLTKDebugUIPlugin.log(e);
 						}
 					}
 				}
 			}
 		};
 
 		try {
 			ResourcesPlugin.getWorkspace().run(runnable, null);
 		} catch (CoreException e) {
 			DLTKUIPlugin.log(e);
 		}
 	}
 
 	private void updateBreakpointMessages(final IBreakpoint[] breakpoints) {
 		updateBreakpoints(breakpoints, new IBreakpointUpdater() {
 			public void update(IScriptBreakpoint breakpoint)
 					throws CoreException {
 				final String message = fLabelProvider.getText(breakpoint);
 				breakpoint.setMessage(message);
 			}
 		});
 	}
 
 	protected void updateBreakpointHitCounts(final IBreakpoint[] breakpoints,
 			final IScriptThread thread) {
 		updateBreakpoints(breakpoints, new IBreakpointUpdater() {
 			public void update(IScriptBreakpoint breakpoint)
 					throws CoreException {
 				IDbgpBreakpoint br = thread.getDbgpBreakpoint(breakpoint
 						.getIdentifier());
 				breakpoint.setHitCount(br.getHitCount());
 			}
 		});
 	}
 
	protected void updateBreakpoinInfoToDefualt(IBreakpoint[] breakpoints) {
 		updateBreakpoints(breakpoints, new IBreakpointUpdater() {
 			public void update(IScriptBreakpoint breakpoint)
 					throws CoreException {
				breakpoint.setIdentifier("");
 				breakpoint.setHitCount(-1);
 			}
 		});
 	}
 
 	public void handleDebugEvents(DebugEvent[] events) {
 		for (int i = 0; i < events.length; ++i) {
 			DebugEvent event = events[i];
 			final int kind = event.getKind();
 			final Object source = event.getSource();
 
 			if (kind == DebugEvent.SUSPEND) {
 				if (source instanceof IScriptThread) {
 					final IScriptThread thread = (IScriptThread) source;
 					final IBreakpoint[] breakpoints = thread.getBreakpoints();
 
 					updateBreakpointHitCounts(breakpoints, thread);
 					updateBreakpointMessages(breakpoints);
 				}
 			} else if (kind == DebugEvent.TERMINATE) {
 				if (source instanceof IScriptDebugTarget) {
 					final String debugModelId = ((IScriptDebugTarget) source)
 							.getModelIdentifier();
 
 					final IBreakpoint[] breakpoints = DebugPlugin.getDefault()
 							.getBreakpointManager()
 							.getBreakpoints(debugModelId);
 
					updateBreakpoinInfoToDefualt(breakpoints);
 					updateBreakpointMessages(breakpoints);
 				}
 			}
 		}
 	}
 
 	public void launchAdded(ILaunch launch) {
 		// TODO Auto-generated method stub
 	}
 
 	public void launchChanged(ILaunch launch) {
 		// TODO Auto-generated method stub
 	}
 
 	public void launchRemoved(ILaunch launch) {
 		// TODO Auto-generated method stub
 	}
 
 	public void breakpointsAdded(IBreakpoint[] breakpoints) {
 		List list = new ArrayList();
 		for (int i = 0; i < breakpoints.length; i++) {
 			try {
 				IBreakpoint breakpoint = breakpoints[i];
 				if (breakpoint instanceof IScriptBreakpoint
 						&& ((IScriptBreakpoint) breakpoint).getMessage() == null) {
 					list.add(breakpoint);
 				}
 			} catch (CoreException e) {
 				DLTKDebugUIPlugin.log(e);
 			}
 		}
 
 		if (!list.isEmpty()) {
 			updateBreakpointMessages((IBreakpoint[]) list
 					.toArray(new IBreakpoint[list.size()]));
 		}
 	}
 
 	public void breakpointsChanged(IBreakpoint[] breakpoints,
 			IMarkerDelta[] deltas) {
 		updateBreakpointMessages(breakpoints);
 	}
 
 	public void breakpointsRemoved(IBreakpoint[] breakpoints,
 			IMarkerDelta[] deltas) {
 	}
 
 	public static ScriptDebugOptionsManager getDefault() {
 		if (instance == null) {
 			instance = new ScriptDebugOptionsManager();
 		}
 
 		return instance;
 	}
 
 	public void startup() {
 		// lazy initialization will occur on the first launch
 		DebugPlugin debugPlugin = DebugPlugin.getDefault();
 		debugPlugin.addDebugEventListener(this);
 		debugPlugin.getLaunchManager().addLaunchListener(this);
 		debugPlugin.getBreakpointManager().addBreakpointListener(this);
 
 		ScriptEvaluationContextManager.startup();
 	}
 
 	public void shutdown() {
 		DebugPlugin debugPlugin = DebugPlugin.getDefault();
 		debugPlugin.removeDebugEventListener(this);
 		debugPlugin.getLaunchManager().removeLaunchListener(this);
 		debugPlugin.getBreakpointManager().removeBreakpointListener(this);
 		if (!DLTKDebugUIPlugin.getDefault().isShuttingDown()) {
 			// avert restoring the preference store at shutdown
 			DLTKDebugUIPlugin.getDefault().getPreferenceStore()
 					.removePropertyChangeListener(this);
 		}
 		// ScriptDebugModel.removeJavaBreakpointListener(this);
 		System.getProperties().remove(
 				DLTKDebugUIPlugin.getUniqueIdentifier() + ".debuggerActive"); //$NON-NLS-1$
 	}
 
 	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
 		// TODO:
 	}
 }
