 package org.eclipse.jdt.internal.debug.ui.actions;
 
 /*
  * (c) Copyright IBM Corp. 2000, 2001.
  * All Rights Reserved.
  */
  
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IBreakpointListener;
 import org.eclipse.debug.core.IBreakpointManager;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.jdt.debug.core.IJavaBreakpoint;
 import org.eclipse.jdt.internal.debug.ui.ExceptionHandler;
 import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IWorkbenchPart;
 
 public abstract class BreakpointToggleAction implements IObjectActionDelegate, IBreakpointListener, IPartListener {
 	
 	private IStructuredSelection fSelection;
 	private IAction fAction;
 	private IWorkbenchPart fPart;
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		IStructuredSelection selection= getStructuredSelection();
 		Iterator enum= selection.iterator();
 		while (enum.hasNext()) {
 			try {
 				IJavaBreakpoint breakpoint= (IJavaBreakpoint) enum.next();
 				doAction(breakpoint);
 			} catch (CoreException e) {
 				String title= ActionMessages.getString("BreakpointAction.Breakpoint_configuration_1"); //$NON-NLS-1$
 				String message= ActionMessages.getString("BreakpointAction.Exceptions_occurred_attempting_to_modify_breakpoint._2"); //$NON-NLS-1$
 				ExceptionHandler.handle(e, title, message);
 			}			
 		}
 	}
 
 	/**
 	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		setAction(action);
 		if (selection.isEmpty()) {
 			setStructuredSelection(null);
 			return;
 		}
 		if (selection instanceof IStructuredSelection) {
 			setStructuredSelection((IStructuredSelection)selection);
 			boolean enabled= getStructuredSelection().size() == 1 
 				&& isEnabledFor(getStructuredSelection().getFirstElement());
 			action.setEnabled(enabled);
 			if (enabled) {
 				IBreakpoint breakpoint= (IBreakpoint)getStructuredSelection().getFirstElement();
 				if (breakpoint instanceof IJavaBreakpoint) {
 					try {
 						action.setChecked(getToggleState((IJavaBreakpoint) breakpoint));
 					} catch (CoreException e) {
 						JDIDebugUIPlugin.log(e);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Toggle the state of this action
 	 */
 	public abstract void doAction(IJavaBreakpoint breakpoint) throws CoreException;
 	
 	/**
 	 * Returns whether this action is currently toggled on
 	 */
 	protected abstract boolean getToggleState(IJavaBreakpoint breakpoint) throws CoreException;
 	
 	/**
 	 * Get the current selection
 	 */
 	protected IStructuredSelection getStructuredSelection() {
 		return fSelection;
 	}
 	
 	protected void setStructuredSelection(IStructuredSelection selection) {
 		fSelection= selection;
 	}
 	
 	public abstract boolean isEnabledFor(Object element);
 
 	/**
 	 * Get the breakpoint manager for the debug plugin
 	 */
 	protected IBreakpointManager getBreakpointManager() {
 		return DebugPlugin.getDefault().getBreakpointManager();		
 	}
 	
 	/**
 	 * Get the breakpoint associated with the given marker
 	 */
 	protected IBreakpoint getBreakpoint(IMarker marker) {
 		return getBreakpointManager().getBreakpoint(marker);
 	}
 
 	protected IAction getAction() {
 		return fAction;
 	}
 
 	protected void setAction(IAction action) {
 		fAction = action;
 	}
 	
 	/**
 	 * @see IBreakpointListener#breakpointAdded(IBreakpoint)
 	 */
 	public void breakpointAdded(IBreakpoint breakpoint) {
 	}
 
 	/**
 	 * @see IBreakpointListener#breakpointChanged(IBreakpoint, IMarkerDelta)
 	 */
 	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
 		if (getAction() != null) {
			IStructuredSelection selection= getStructuredSelection();
			if (selection != null) {
				IBreakpoint selectedBreakpoint= (IBreakpoint)selection.getFirstElement();
				if (selectedBreakpoint.equals(breakpoint)) {
					selectionChanged(getAction(), selection);
				}
			}			
 		}
 	}
 
 	/**
 	 * @see IBreakpointListener#breakpointRemoved(IBreakpoint, IMarkerDelta)
 	 */
 	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
 	}
 	
 	protected IWorkbenchPart getPart() {
 		return fPart;
 	}
 
 	protected void setPart(IWorkbenchPart part) {
 		fPart = part;
 	}
 	
 	/**
 	 * @see IPartListener#partActivated(IWorkbenchPart)
 	 */
 	public void partActivated(IWorkbenchPart part) {
 	}
 
 	/**
 	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
 	 */
 	public void partBroughtToTop(IWorkbenchPart part) {
 	}
 
 	/**
 	 * @see IPartListener#partClosed(IWorkbenchPart)
 	 */
 	public void partClosed(IWorkbenchPart part) {
 		if (part == getPart()) {
 			getBreakpointManager().removeBreakpointListener(this);
 			part.getSite().getPage().removePartListener(this);
 		}
 	}
 
 	/**
 	 * @see IPartListener#partDeactivated(IWorkbenchPart)
 	 */
 	public void partDeactivated(IWorkbenchPart part) {
 	}
 
 	/**
 	 * @see IPartListener#partOpened(IWorkbenchPart)
 	 */
 	public void partOpened(IWorkbenchPart part) {
 	}
 	
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		IWorkbenchPart oldPart= getPart();
 		if (oldPart != null) {
 			getPart().getSite().getPage().removePartListener(this);			
 		}	
 		
 		getBreakpointManager().addBreakpointListener(this);
 		setPart(targetPart);
 		targetPart.getSite().getPage().addPartListener(this);	
 	}
 }
 
