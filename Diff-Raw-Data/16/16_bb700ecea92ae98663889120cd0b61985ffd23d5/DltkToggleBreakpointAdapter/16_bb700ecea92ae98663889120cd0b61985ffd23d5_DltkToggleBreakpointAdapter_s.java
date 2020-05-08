 package org.eclipse.dltk.internal.debug.ui;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.ILineBreakpoint;
 import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
 import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
 import org.eclipse.dltk.debug.internal.core.model.ScriptModelConstants;
 import org.eclipse.dltk.debug.ui.BreakpointUtils;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class DltkToggleBreakpointAdapter implements IToggleBreakpointsTarget {
 	
 	protected ITextEditor getPartEditor(IWorkbenchPart part) {
 		if (part instanceof ITextEditor) {
 			return (ITextEditor) part;
 		}
 
 		return null;
 	}
 
 	protected IResource getPartResource(IWorkbenchPart part) {
 		ITextEditor textEditor = getPartEditor(part);
 		if (textEditor != null) {
 			IResource resource = (IResource) textEditor.getEditorInput()
 					.getAdapter(IResource.class);
 			return resource;
 		}
 
 		return null;
 	}
 
 	public DltkToggleBreakpointAdapter() {
 
 	}
 
 	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection)
 			throws CoreException {
 		if (selection instanceof ITextSelection) {
 
 			ITextSelection textSelection = (ITextSelection) selection;			
 			int lineNumber = textSelection.getStartLine() + 1; //one based
 				
			//System.out.println("Selection: " + textSelection);
			//System.out.println("Line number: " + lineNumber);
			//System.out.println("Offset: " + textSelection.getOffset());
			//System.out.println("Length: " + textSelection.getLength());
			
 			IResource resource = getPartResource(part);
 
 			if (resource != null) {
 				IBreakpoint[] breakpoints = DebugPlugin.getDefault()
 						.getBreakpointManager().getBreakpoints(
 								ScriptModelConstants.MODEL_ID);
 
 				for (int i = 0; i < breakpoints.length; i++) {
 					IBreakpoint breakpoint = breakpoints[i];
 					if (resource.equals(breakpoint.getMarker().getResource())) {
 						if (((ILineBreakpoint) breakpoint).getLineNumber() == lineNumber) {
 							// delete existing breakpoint
 							breakpoint.delete();
 							return;
 						}
 					}
 				}
 				
 				BreakpointUtils.addLineBreakpoint(getPartEditor(part), lineNumber);
 			}
 		} 
 	}
 
 	public boolean canToggleLineBreakpoints(IWorkbenchPart part,
 			ISelection selection) {
 		return getPartResource(part) != null;
 	}
 
 	public void toggleMethodBreakpoints(IWorkbenchPart part,
 			ISelection selection) throws CoreException {
 	}
 
 	public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
 			ISelection selection) {
 		return false;
 	}
 
 	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
 			throws CoreException {
 	}
 
 	public boolean canToggleWatchpoints(IWorkbenchPart part,
 			ISelection selection) {
 		return false;
 	}
 }
