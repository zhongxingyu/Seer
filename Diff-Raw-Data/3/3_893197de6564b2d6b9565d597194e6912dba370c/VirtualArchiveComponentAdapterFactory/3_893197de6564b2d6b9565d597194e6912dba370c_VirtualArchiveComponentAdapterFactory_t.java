 package org.eclipse.jst.j2ee.internal.common;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 
 public class VirtualArchiveComponentAdapterFactory implements IAdapterFactory {
 
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 		if (adapterType == VirtualArchiveComponent.ADAPTER_TYPE ) {
 			VirtualArchiveComponent component = (VirtualArchiveComponent) adaptableObject;
			IPath path = J2EEProjectUtilities.getResolvedPathForArchiveComponent(component.getName());
 			return path;
 		}
 		return null;
 	}
 
 	
 	public Class[] getAdapterList() {
 		return new Class[]{VirtualArchiveComponent.class};
 	}	
 
 }
