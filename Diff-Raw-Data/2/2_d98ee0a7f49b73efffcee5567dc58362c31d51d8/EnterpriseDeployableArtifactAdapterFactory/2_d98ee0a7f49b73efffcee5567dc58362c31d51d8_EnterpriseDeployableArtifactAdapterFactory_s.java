 /*
  * Created on Jan 18, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.debug.ui.actions.ILaunchable;
 import org.eclipse.wst.server.core.IModuleArtifact;
 
 public class EnterpriseDeployableArtifactAdapterFactory implements IAdapterFactory {
 
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 		IModuleArtifact moduleArtifact = null;
 		if (adapterType == IModuleArtifact.class)
 			moduleArtifact = EnterpriseApplicationDeployableAdapterUtil.getModuleObject(adaptableObject);
 		else if (adapterType == ILaunchable.class) {
 			if (adaptableObject instanceof EObject) {
 				return adaptableObject;
 			}
 		}
 		return moduleArtifact;
 	}
 
 	public Class[] getAdapterList() {
 		return new Class[] { IModuleArtifact.class, ILaunchable.class };
 	}
 
 }
