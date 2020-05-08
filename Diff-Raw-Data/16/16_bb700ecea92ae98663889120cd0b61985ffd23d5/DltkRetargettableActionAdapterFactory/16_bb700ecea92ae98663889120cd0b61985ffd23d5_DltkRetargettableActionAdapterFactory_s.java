 package org.eclipse.dltk.internal.debug.ui;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.debug.ui.actions.IRunToLineTarget;
 import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
 
 public class DltkRetargettableActionAdapterFactory implements IAdapterFactory {
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 		if (adapterType == IRunToLineTarget.class) {
 			return new DltkRunToLineAdapter();
		}
		else if (adapterType == IToggleBreakpointsTarget.class) {
 			return new DltkToggleBreakpointAdapter();
 		}
		
 		return null;
 	}
 
 	public Class[] getAdapterList() {
 		return new Class[] { IRunToLineTarget.class,
 				IToggleBreakpointsTarget.class };
 	}
 }
