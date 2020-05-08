 package com.marcwi.eclipse.jdt.justmycode;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.ISourceLocator;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.jdt.core.dom.Message;
 import org.eclipse.jdt.debug.core.IJavaBreakpoint;
 import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
 import org.eclipse.jdt.debug.core.IJavaDebugTarget;
 import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
 import org.eclipse.jdt.debug.core.IJavaStackFrame;
 import org.eclipse.jdt.debug.core.IJavaThread;
 import org.eclipse.jdt.debug.core.IJavaType;
 
 /**
  * This breakpoint listener skips breakpoints outside user code.
  */
 public class JustMyCodeBreakpointListener implements IJavaBreakpointListener {
 
 	@Override
 	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
 	}
 
 	@Override
 	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
 		return DONT_CARE;
 	}
 
 	@Override
 	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
 	}
 
 	@Override
 	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
 		try {
 			ISourceLocator sourceLocator = thread.getLaunch().getSourceLocator();
 			IStackFrame[] frames = thread.getStackFrames();
 			for (int i = 0; i < frames.length; i++) {
 				IJavaStackFrame frame = (IJavaStackFrame) frames[i];
 				Object sourceElement = sourceLocator.getSourceElement(frame);
 				if (sourceElement instanceof IFile) {
 					Log.log(Status.INFO, "breakpointHit %s %s", thread.getName(), frame.getName());
 					// TODO should focus on this frame when suspended
 					return DONT_CARE;
 				}
 			}
 		} catch (DebugException e) {
			Log.log(Status.ERROR, e, JustMyCodePlugin.PLUGIN_ID, "breakpointHit error");
 		}
 		return DONT_SUSPEND;
 	}
 
 	@Override
 	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
 	}
 
 	@Override
 	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
 	}
 
 	@Override
 	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
 	}
 
 }
